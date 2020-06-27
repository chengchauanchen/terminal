package ptt.terminalsdk.manager.gps.recoder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.MountType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.protolbuf.PTTProtolbuf.NotifyForceUploadGpsMessage;
import cn.vsx.hamster.protolbuf.PTTProtolbuf.ResponseMemberConfigMessage;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.channel.ServerMessageReceivedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangePersonLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGPSLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePowerSaveStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.bean.LocationType;
import ptt.terminalsdk.broadcastreceiver.LocationRequestReceiver;
import ptt.terminalsdk.context.BaseApplication;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverLocationCountDownHandler;
import ptt.terminalsdk.tools.AppUtil;
import ptt.terminalsdk.tools.ToastUtil;

import static android.content.Context.ALARM_SERVICE;

public class LocationManager  {

    private Context context;

    private Logger logger = Logger.getLogger(getClass());

    public static final String TAG = "LocationManager---";
    //传递地理位置信息的key
    public static final String BUNDLE_ADDRESS = "bundle_address";
    //定位是否开启
    private boolean isStarted;
    //是否在定位中
    private boolean isLocationing;

    //最后一次上传位置信息的时间
    private long lastUpLocationTime;
    //最近一次定位到的位置信息
    private Location lastLocation;

    //最近一次定位到的位置信息的时间
    private long lastLocationTime;
    //最近一次定位的间隔时间
//    private long lastUploadTime = -1;

    //是否是聊天页面发送位置信息
    private boolean isChatSendLocation;

    //判断定位状态之后选择定位方式
//    private final int HANDLER_WHAT_LOCATION_CHECK = 0;
    //聊天页面的获取位置信息
    private final int HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT = 1;
    //聊天页面的获取位置信息-超时处理
    private final int HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT = 2;
//    //选择定位方式的防止频繁调用
//    private final int HANDLER_WHAT_LOCATION_CHECK_TIMEOUT = 3;

    //请求定位
//    private final int HANDLER_WHAT_REQUEST_LOCATION = 4;

    //通知GPS失败
    private final int HANDLER_WHAT_GPS_FAIL = 5;
    private final int DELAYED_TIME_GPS_FAIL = 20*1000;
    //延时时间
    private final int DELAYED_TIME = 300;
    //记录定位类型失败的集合
    private List<LocationType> failList = new ArrayList<>();
    private int locationFailCount = 0;
    //连续定位不到GPS的最大次数，达到这个次数之后如果再定位就要增加定位间隔时间
    private final int LOCATION_FAIL_MAX_COUNT = 3;
    private final int LOCATION_FAIL_MAX_INTERVAL_TIME = 10*60*1000;
    //记录每次定位的时间间隔
    private long  uploadTime;

    private AlarmManager alarmManager;
    //记录倒计时开始的时间
    private long tempTime = 0L;
//    //防止频繁调用的时间
//    private final int CHECK_TIME = 5*1000;

    //请求定位
    private final String INTENT_ACTION_REQUEST = "vsxin.action.location.request";

//    private HandlerThread thread = new HandlerThread("LocationManager-Thread");

    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT:
                    getHandler().removeMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT);
                    TerminalFactory.getSDK().getThreadPool().execute(() -> requestLocationUpdate(true));
                    break;
                case HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT:
                    getHandler().removeMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT);
                    isChatSendLocation = false;
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, 0, 0);
                    //检查上传位置信息的状态（状态切换有可能时间间隔不同，有可能已经不需要上传位置信息）
                    stopUpload();
                    startLocation(true,false,true,false);
                    break;
                case HANDLER_WHAT_GPS_FAIL:
                    getHandler().removeMessages(HANDLER_WHAT_GPS_FAIL);
                    //发送失败的消息
                    checkLocationFailType();
                    break;
                default:
                    break;
            }
        }
    };

    public LocationManager(Context context) {
        this.context = context;
    }

    /**
     * 定位服务开启
     */
    public void start() {
        isStarted = true;
        logger.info(TAG + "开启了-thread:"+Thread.currentThread());
        getHandler();
        //gps
        MyTerminalFactory.getSDK().getRecorderGPSManager().init();
        //顺丰
        MyTerminalFactory.getSDK().getRecorderSfGPSManager().init(0);
        //百度
        MyTerminalFactory.getSDK().getRecorderBDGPSManager().init(0);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePowerSaveStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverLocationCountDownHandler);
        MyTerminalFactory.getSDK().getClientChannel().registMessageReceivedHandler(notifyForceUploadGpsMessageReceivedHandler);
        MyTerminalFactory.getSDK().getClientChannel().registMessageReceivedHandler(responseMemberConfigMessageHandler);

    }

    /**
     * 定位服务关闭
     */
    public void stop() {
        try{
            isStarted = true;
            //清空所有的倒计时
            clearAllAlarm();
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receivePowerSaveStatusChangedHandler);
            MyTerminalFactory.getSDK().unregistReceiveHandler(receiverLocationCountDownHandler);
            MyTerminalFactory.getSDK().getClientChannel().unregistMessageReceivedHandler(responseMemberConfigMessageHandler);
            MyTerminalFactory.getSDK().getClientChannel().unregistMessageReceivedHandler(notifyForceUploadGpsMessageReceivedHandler);

            MyTerminalFactory.getSDK().getRecorderGPSManager().stop();
            MyTerminalFactory.getSDK().getRecorderBDGPSManager().stop();
            MyTerminalFactory.getSDK().getRecorderSfGPSManager().stop();
            if(mHandler!=null){
                mHandler.removeCallbacksAndMessages(null);
            }
            logger.info(TAG + "销毁了");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 收到成员更新配置的通知
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            logger.info(TAG + "收到成员更新配置的通知，GPS开关状态为：" + MyTerminalFactory.getSDK().getParam(Params.GPS_STATE, false));
            startLocation(true,false,true,false);
        }
    };

    /**
     * 通知强制上传GPS信息的消息
     */
    private ServerMessageReceivedHandler<NotifyForceUploadGpsMessage> notifyForceUploadGpsMessageReceivedHandler = new ServerMessageReceivedHandler<NotifyForceUploadGpsMessage>() {
        @Override
        public void handle(PTTProtolbuf.NotifyForceUploadGpsMessage message) {
            logger.info(TAG + "收到GPS强制上传通知命令" + message);
            //保存强制上传GPS信息的一些参数
            MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_INTERVAL, message.getUploadRate());
            MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, message.getHoldTime());
            MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_START_TIME, System.currentTimeMillis());
            startLocation(true,false,true,false);
        }
    };
    /**
     * 获取到上传GPS信息的消息
     */
    private ServerMessageReceivedHandler<ResponseMemberConfigMessage> responseMemberConfigMessageHandler = new ServerMessageReceivedHandler<ResponseMemberConfigMessage>() {
        @Override
        public void handle(PTTProtolbuf.ResponseMemberConfigMessage message) {
            logger.info(TAG + "获取到上传GPS信息的消息" + message);
            TerminalFactory.getSDK().putParam(Params.GPS_STATE, message.getGpsEnable());//gps开关状态,打开还是关闭，默认为关闭
            TerminalFactory.getSDK().putParam(Params.GPS_UPLOAD_INTERVAL, message.getDefaultGpsFrequency());
            TerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_INTERVAL, message.getActiveGpsFrequency());
            startLocation(true,false,true,false);
        }
    };

    /**
     * 真实的网络
     */
    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
        @Override
        public void handler(boolean connected){
            if(!connected){
                stopUpload();
            }else{
                startLocation(true,false,false,false);
            }
        }
    };

    /**
     * 省电模式状态改变的通知
     */
    private ReceivePowerSaveStatusChangedHandler receivePowerSaveStatusChangedHandler = new ReceivePowerSaveStatusChangedHandler() {
        @Override
        public void handler(boolean isSave) {
            logger.info(TAG +"receivePowerSaveStatusChangedHandler-isSave:" + isSave);
            if(isSave){
                stopUpload();
            }else{
                startLocation(false,false,false,false);
            }
        }
    };

    /**
     * 请求定位的通知（到间隔时间之后）
     */
    private ReceiverLocationCountDownHandler receiverLocationCountDownHandler = new ReceiverLocationCountDownHandler() {
        @Override
        public void handler(String action) {
            logger.debug(TAG +"receiverLocationCountDownHandler-action:" + action);
            if(!TextUtils.isEmpty(action)){
                switch (action){
                    case INTENT_ACTION_REQUEST:
                        //开始请求定位
                        requestLocationUpdate(false);
                        break;
                    default:break;
                }
            }
        }
    };

    /**
     * 开始定位
     * @param isCheckSaveModule 是否检查省电模式
     * @param isDelay 是否延迟请求定位
     * @param isClear 是否清除之前的倒计时
     *
     *
     */
    public synchronized void startLocation(boolean isCheckSaveModule,boolean isDelay,boolean isClear,boolean icChat){
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            //判断是否定位开启
            if(!isStarted){
                return;
            }
            //检查权限
            if(!checkPermission()){
                return;
            }
            //判断是否需要检查省电模式
            if(isCheckSaveModule && TerminalFactory.getSDK().getPowerSaveManager().isSave()){
                stopUpload();
                return;
            }
            //判断是否清除之前的定位
            if(isClear){
                clearAllAlarm();
            }
            //已经在定位倒计时中
            if (checkIsCountDowning()) {
                return;
            }
            //如果正在定位中不在请求定位
            if (isLocationing) {
                return;
            }
            uploadTime = 0;
            if(icChat){
                requestLocationUpdate(true);
            }else{
                checkUploadState(isDelay);
            }
        });
    }



    /**
     * 检查上传位置信息的状态
     * 优先级：强制上传 > Gps开关打开 > 普通上传
     */
    private void  checkUploadState(boolean isDelayed){
        //检查强制上传GPS信息的状态是否依然存在，若存在，则进行强制上传
        if (checkForceUploadState()) {
            //获取定位间隔时间
            uploadTime = getUploadTime(false,false);
            logger.info(TAG + "强制上传-设置上传位置信息的间隔时间" + uploadTime );
            if(isDelayed){
                startAlarmManager(INTENT_ACTION_REQUEST,getCurrentTime()+ uploadTime);
            }else {
                requestLocationUpdate(false);
            }
        }else if(TerminalFactory.getSDK().getParam(Params.GPS_STATE, false)){
            //GPS 开关打开
            uploadTime = getUploadTime(true,false);
            logger.info(TAG + "普通上传-设置上传位置信息的间隔时间" + uploadTime );
            if(isDelayed){
                startAlarmManager(INTENT_ACTION_REQUEST,getCurrentTime()+ uploadTime);
            }else {
                requestLocationUpdate(false);
            }
        }else{
            //GPS 开关关闭
            stopUpload();
        }
    }

    /**
     * 来自聊天页面的请求位置信息
     */
    public void requestLocationByChat() {
        //如果是会话页面上传位置信息， 判断上次获取到的位置信息的时间间隔，假如时间间隔小于5s，并且位置信息有效，就直接返回上次记录的位置信息
        if(isStarted){
            if (System.currentTimeMillis() - lastLocationTime < 5000 && (lastLocation != null)&&(lastLocation.getLongitude()!=0&&lastLocation.getLatitude()!=0)) {
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, lastLocation.getLongitude(), lastLocation.getLatitude());
                return;
            }
            stopUpload();
            //如果在休眠模式下，不上报
            startLocation(true,false,true,true);
        }else{
            //没有启动定位就直接返回定位失败
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, 0, 0);
        }
    }

    /**
     * 检查是否在强制上传期间
     */
    public boolean checkForceUploadState(){
        return (System.currentTimeMillis() - MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_START_TIME, 0L)
                < MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, 0));
    }

    /**
     * 关闭定位功能
     */
    private void stopUpload(){
        try{
            isLocationing = false;
            clearAllAlarm();
            boolean shijuSDP = TerminalFactory.getSDK().getLiveManager().isShijuSDP();
            if(shijuSDP){
                MyTerminalFactory.getSDK().getRecorderSfGPSManager().stop();
            }
            MyTerminalFactory.getSDK().getRecorderGPSManager().stop();
            MyTerminalFactory.getSDK().getRecorderBDGPSManager().stop();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置获取位置的方式
     * @param isChat 区分会话页面的发送位置 true 为会话页面发送位置信息
     */
    @SuppressLint("MissingPermission")
    private synchronized void requestLocationUpdate(final boolean isChat) {
        try{
            mHandler.post(() -> {
                //如果在定位中就不再请求定位
                if(isLocationing){
                    return;
                }
                //设置正在定位
                isLocationing = true;
                logger.info(TAG + "是否是会话页面发送位置信息：" + isChat + "---之前的是否是会话页面的状态:" + isChatSendLocation);
                //判断当前是否已经是会话页面上传位置信息
                if(!isChatSendLocation){
                    isChatSendLocation = isChat;
                }
//        if (!isChat) {
                failList.clear();
//        }
                boolean shijuSDP = TerminalFactory.getSDK().getLiveManager().isShijuSDP();
                logger.info(LocationManager.TAG + "是否市局环境---"+shijuSDP);
                if(shijuSDP){
                    //顺丰
                    RecorderSfGPSManager recorderSfGPSManager = MyTerminalFactory.getSDK().getRecorderSfGPSManager();
                    recorderSfGPSManager.requestLocationInfo(0);
                }
                //百度定位
                final RecorderBDGPSManager bDGPSManager = MyTerminalFactory.getSDK().getRecorderBDGPSManager();
                bDGPSManager.requestLocationInfo(0);

                //GPS定位
                final RecorderGPSManager gpsManager = MyTerminalFactory.getSDK().getRecorderGPSManager();
                if (gpsManager.checkInitCompleteAndIsChat()) {
                    logger.info(LocationManager.TAG + "gpsManager-requestLocationUpdates");
//                    MyTerminalFactory.getSDK().getRecorderGPSManager().getLocationManager().requestLocationUpdates(gpsManager.getlocationProvider(), uploadTime, 0, gpsManager.getLocationListener());
                    MyTerminalFactory.getSDK().getRecorderGPSManager().getLocationManager().requestSingleUpdate(gpsManager.getlocationProvider(),  gpsManager.getLocationListener(),null);
                }
                //处理5s之后都没有定位到----会话页面
                if (isChat) {
                    getHandler().sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT,DELAYED_TIME_GPS_FAIL);
                }else{
                    if(getHandler().hasMessages(HANDLER_WHAT_GPS_FAIL)){
                        getHandler().removeMessages(HANDLER_WHAT_GPS_FAIL);
                    }
                    getHandler().sendEmptyMessageDelayed(HANDLER_WHAT_GPS_FAIL,DELAYED_TIME_GPS_FAIL);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 获取到位置信息之后分发数据
     *
     * @param location
     */
    public synchronized void dispatchCommitLocation(Location location) {
        logger.info(TAG+"dispatchCommitLocation-isChatSendLocation:"+isChatSendLocation);
        //记录定位类型的数量
//        locationTypeFailCount = -10;
        //初始化定位失败的次数
        locationFailCount = 0;
        uploadTime = 0;
        //清空GPS定位失败的倒计时
        if(getHandler().hasMessages(HANDLER_WHAT_GPS_FAIL)){
            getHandler().removeMessages(HANDLER_WHAT_GPS_FAIL);
        }
        lastLocation = location;
        lastLocationTime = System.currentTimeMillis();
        if (isChatSendLocation) {
            //国产手机获取到的定位就是WGS坐标系，地图地图也是，所以不用转
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, location.getLongitude(), location.getLatitude());
            isChatSendLocation = false;
            //取消来自会话页面定位可能因为超时发送的伪定位信息
            getHandler().removeMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT);
        } else {
            MyTerminalFactory.getSDK().getLocationManager().upLoadLocation(location);
        }
        internalRequestLocation();
    }

    /**
     * 定位失败
     * @param type
     */
    public synchronized void locationFail(LocationType type){
        int count = getCheckLocationType();
        failList.add(type);
        if(failList.size() >= count){
            //所有定位都失败，判断最后一次记录的位置信息是否有效，如果有效，上传位置信息
            if(lastLocation!=null && lastLocation.getLatitude()!=0 && lastLocation.getLongitude()!=0){
                MyTerminalFactory.getSDK().getLocationManager().upLoadLocation(lastLocation);
            }
            //计数1次。
            locationFailCount++;
            if(!mHandler.hasMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT)){
                internalRequestLocation();
            }
        }
        logger.info(LocationManager.TAG + "locationFail-LocationType:"+type+"-failList:"+failList+"-locationFailCount:"+locationFailCount);
    }

    /**
     * 间隔时间获取
     */
    private void internalRequestLocation(){
        //停止定位（省电）,如果来自会话页面的定位之前就没有开始定位，会话页面定位之后就不断的定位，因为没有停止定位
        stopUpload();
        //如果在休眠模式下，不上报
        startLocation(true,true,false,false);
    }
    /**
     * 检查定位失败的类型
     */
    private void checkLocationFailType() {
        logger.info(TAG+"checkLocationFailType");
        int count = getCheckLocationType();
        List<LocationType> list ;
        if(count == 3){
             list = Arrays.asList(LocationType.SF, LocationType.BD, LocationType.GPS);
        }else{
            list = Arrays.asList( LocationType.BD, LocationType.GPS);
        }
        for (LocationType type: list) {
            if(type!=null && !failList.contains(type)){
                locationFail(type);
            }
        }
    }

    /**
     * 获取定位类型的数量
     * @return
     */
    private int getCheckLocationType(){
        boolean shijuSDP = TerminalFactory.getSDK().getLiveManager().isShijuSDP();
        return shijuSDP?3:2;
    }

    /**
     * 上传位置信息
     *
     * @param location
     */
    public void upLoadLocation(Location location) {
        MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                logger.info(TAG + "准备上传位置信息---GPS是否打开:" + TerminalFactory.getSDK().getParam(Params.GPS_STATE, false) + "---时间间隔是否大于5秒：" + (System.currentTimeMillis() - lastUpLocationTime > 5000));
                //配置要求GPS打开，时间间隔大于5秒
                if (TerminalFactory.getSDK().getParam(Params.GPS_STATE, false) &&
                        !TerminalFactory.getSDK().isExit() && System.currentTimeMillis() - lastUpLocationTime > 5000) {
                    logger.info(TAG + "开始上传位置信息---Longitude:" + location.getLongitude() + "---Latitude：" + location.getLatitude());
                    lastUpLocationTime = System.currentTimeMillis();
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveChangePersonLocationHandler.class,location.getLatitude(),location.getLongitude());
                    String ip = MyTerminalFactory.getSDK().getParam(Params.HTTP_IP);
                    int port = MyTerminalFactory.getSDK().getParam(Params.HTTP_PORT,0);
                    if(TextUtils.isEmpty(ip)||port<=0){
                        return;
                    }
                    final String url = "http://"+ip+":"+port+"/gatherService/save";
                    Map<String,Object> params = new HashMap<>();
                    params.put("terminalno", MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
                    params.put("memberuniqueno", MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L));
                    params.put("terminalType", MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE));
                    params.put("longitude",location.getLongitude());
                    params.put("latitude",location.getLatitude());
                    params.put("speed",location.getSpeed());
                    params.put("bearing",location.getBearing());
                    params.put("altitude",location.getAltitude());
                    params.put("addressStr",getAddressString(location.getExtras()));
                    params.put("mountType", MountType.MOUNT_SELF.toString());
                    //电量
                    params.put("batteryLevel", AppUtil.getBatteryLevel(context));
                    //信号状态
                    params.put("mobileDbm", AppUtil.getWifiOrPhoneDbmStatus(context));
                    Gson gson = new Gson();
                    final String json = gson.toJson(params);
                    MyTerminalFactory.getSDK().getHttpClient().postJson(url,"gps="+json);
                }
            }
        });
    }

    /**
     * 检查权限
     */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ToastUtil.showToast("请打开位置权限，以方便获取您的位置信息");
            logger.error("请打开位置权限，以方便获取您的位置信息");
            return false;
        }
        return true;
    }


    /**
     * 获取上传位置信息的间隔时间
     *
     * @param isCommonUpload  区分是否是普通上传或者是强制上传 true 为普通上传
     * @param isChat
     * @return
     */
    public long getUploadTime(boolean isCommonUpload, boolean isChat) {
        if (isChat) {
            return 2000;
        }
        if (isCommonUpload) {
//			return Math.max(1 * 60 * 1000, MyTerminalFactory.getSDK().getParam(Params.GPS_UPLOAD_INTERVAL, 5 * 60 * 1000));//普通上传最小间隔是一分钟
            return getIntervalTime((uploadTime<=0)?MyTerminalFactory.getSDK().getParam(Params.GPS_UPLOAD_INTERVAL, 1 * 60 * 1000):uploadTime);
        } else {
            return getIntervalTime((uploadTime<=0)?Math.max(5 * 1000, MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_INTERVAL, 5 * 1000)):uploadTime);//强制上传最小间隔是5秒钟
        }
    }

    /**
     *  //判断定位失败次数是否大于最大的限制，如果大于就增加时间的间隔
     * @param time
     * @return
     */
    private long getIntervalTime(long time){
        if(locationFailCount>=LOCATION_FAIL_MAX_COUNT){
            locationFailCount = 0;
            time = time * 2;
        }
        if(time>LOCATION_FAIL_MAX_INTERVAL_TIME){
            time = LOCATION_FAIL_MAX_INTERVAL_TIME;
        }
        return time;
    }

    /**
     * 获取含有地址信息的Bundle
     * @param address
     * @return
     */
    public Bundle getAddressBundle(String address){
        Bundle bundle  = new Bundle();
        bundle.putString(BUNDLE_ADDRESS,address);
        return bundle;
    }

    /**
     * 获取含有地址信息的String
     * @param bundle
     * @return
     */
    public String getAddressString(Bundle bundle){
        String address = "";
        if(bundle!=null&&bundle.containsKey(BUNDLE_ADDRESS)){
            address = bundle.getString(BUNDLE_ADDRESS);
        }
        if(TextUtils.isEmpty(address)){
            address = "";
        }
        return address;
    }

    /**
     * 获取AlarmManager
     *
     * @return
     */
    public AlarmManager getAlarmManager() {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        }
        return alarmManager;
    }

    /**
     * 获取PendingIntent
     */
    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(context, LocationRequestReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * 开启倒计时
     * @param action
     * @param time
     */
    private synchronized void startAlarmManager(String action,long time){
        logger.info(TAG+"startAlarmManager--action:"+action+"--time:"+time);
        try{
            AlarmManager alarmManager = getAlarmManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, getPendingIntent(action));
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, getPendingIntent(action));
            }else{
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, getPendingIntent(action));
            }
//            clearAlarmExceptAction(alarmManager,action);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 清空所有的倒计时
     */
    private void clearAllAlarm() {
        logger.info(TAG+"clearAllAlarm");
        try{
            if(alarmManager!=null){
                alarmManager.cancel(getPendingIntent(INTENT_ACTION_REQUEST));
            }
            alarmManager = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查是否已经开始准备进入省电模式的倒计时
     * @return
     */
    private synchronized boolean checkIsCountDowning(){
        return (alarmManager!=null);
    }

    /**
     * 获取当前的时间
     * @return
     */
    private long getCurrentTime(){
        tempTime = System.currentTimeMillis();
        return tempTime;
    }

    private Handler getHandler(){
//        if(mHandler==null){
//            mHandler = new Handler(thread.getLooper(),this);
//        }
        return mHandler;
    }


    public Location getlastLocation(){
        return lastLocation;
    }
}
