package ptt.terminalsdk.manager.gps.recoder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import cn.vsx.hamster.common.MountType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.channel.ServerMessageReceivedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangePersonLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGPSLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

public class LocationManager {

    private Context context;

    private Logger logger = Logger.getLogger(getClass());

    public static final String TAG = "LocationManager---";

    //最后一次上传位置信息的时间
    private long lastUpLocationTime;
    //最近一次定位到的位置信息
    private Location lastLocation;

    //最近一次定位到的位置信息的时间
    private long lastLocationTime;
    //最近一次定位的间隔时间
    private long lastUploadTime = -1;

    //是否是聊天页面发送位置信息
    private boolean isChatSendLocation;

    //判断定位状态之后选择定位方式
    private final int HANDLER_WHAT_LOCATION_CHECK = 0;
    //聊天页面的获取位置信息
    private final int HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT = 1;
    //聊天页面的获取位置信息-超时处理
    private final int HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT = 2;
//    //选择定位方式的防止频繁调用
//    private final int HANDLER_WHAT_LOCATION_CHECK_TIMEOUT = 3;
    //延时时间
    private final int DELAYED_TIME = 300;
//    //防止频繁调用的时间
//    private final int CHECK_TIME = 5*1000;

    public LocationManager(Context context) {
        this.context = context;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HANDLER_WHAT_LOCATION_CHECK:
                    removeMessages(HANDLER_WHAT_LOCATION_CHECK);
                    checkUploadState();
                    break;
                case HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT:
                    removeMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT);
                    setLocationUpdate(false, true);
                    break;
                case HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT:
                    removeMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT);
                    isChatSendLocation = false;
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, 0, 0);
                    //检查上传位置信息的状态（状态切换有可能时间间隔不同，有可能已经不需要上传位置信息）
                    sendEmptyMessage(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 定位服务开启
     */
    public void start() {
        logger.info(TAG + "开启了");
        MyTerminalFactory.getSDK().getRecorderBDGPSManager().init(0);
        MyTerminalFactory.getSDK().getRecorderGPSManager().init();


//        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().getClientChannel().registMessageReceivedHandler(notifyForceUploadGpsMessageReceivedHandler);
        MyTerminalFactory.getSDK().getClientChannel().registMessageReceivedHandler(responseMemberConfigMessageHandler);
    }

    /**
     * 定位服务关闭
     */
    public void stop() {
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().getClientChannel().unregistMessageReceivedHandler(responseMemberConfigMessageHandler);
        MyTerminalFactory.getSDK().getClientChannel().unregistMessageReceivedHandler(notifyForceUploadGpsMessageReceivedHandler);

        MyTerminalFactory.getSDK().getRecorderGPSManager().stop();
        MyTerminalFactory.getSDK().getRecorderBDGPSManager().stop();
        mHandler.removeCallbacksAndMessages(null);
        logger.info(TAG + "销毁了");
    }

    /**
     * 收到成员更新配置的通知
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            logger.info(TAG + "收到成员更新配置的通知，GPS开关状态为：" + MyTerminalFactory.getSDK().getParam(Params.GPS_STATE, false));
            TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_CHECK,DELAYED_TIME);
                }
            });
        }
    };

    /**
     * 通知强制上传GPS信息的消息
     */
    private ServerMessageReceivedHandler<PTTProtolbuf.NotifyForceUploadGpsMessage> notifyForceUploadGpsMessageReceivedHandler = new ServerMessageReceivedHandler<PTTProtolbuf.NotifyForceUploadGpsMessage>() {
        @Override
        public void handle(PTTProtolbuf.NotifyForceUploadGpsMessage message) {
            logger.info(TAG + "收到GPS强制上传通知命令" + message);
            //保存强制上传GPS信息的一些参数
            MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_INTERVAL, message.getUploadRate());
            MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, message.getHoldTime());
            MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_START_TIME, System.currentTimeMillis());
            mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_CHECK,DELAYED_TIME);
        }
    };
    /**
     * 获取到上传GPS信息的消息
     */
    private ServerMessageReceivedHandler<PTTProtolbuf.ResponseMemberConfigMessage> responseMemberConfigMessageHandler = new ServerMessageReceivedHandler<PTTProtolbuf.ResponseMemberConfigMessage>() {
        @Override
        public void handle(PTTProtolbuf.ResponseMemberConfigMessage message) {
            logger.info(TAG + "获取到上传GPS信息的消息" + message);
            TerminalFactory.getSDK().putParam(Params.GPS_STATE, message.getGpsEnable());//gps开关状态,打开还是关闭，默认为关闭
            TerminalFactory.getSDK().putParam(Params.GPS_UPLOAD_INTERVAL, message.getDefaultGpsFrequency());
            TerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_INTERVAL, message.getActiveGpsFrequency());
            mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_CHECK,DELAYED_TIME);
        }
    };

//    /**
//     * 收到登录的消息
//     */
//    private ReceiveLoginResponseHandler receiveLoginResponseHandler = new ReceiveLoginResponseHandler() {
//        @Override
//        public void handler(int resultCode, String resultDesc) {
//            if (resultCode == 0) {
//                logger.info(TAG + "收到登录成功的消息");
//                TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        mHandler.sendEmptyMessage(HANDLER_WHAT_LOCATION_CHECK);
//                    }
//                });
//            }
//        }
//    };

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {
        @Override
        public void handler(final boolean connected) {
            logger.info(TAG +"网络连接状态:" + connected);
            if(!connected){
                stopUpload();
            }else{
                mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_CHECK,DELAYED_TIME);
            }
        }
    };


    /**
     * 检查上传位置信息的状态
     * 优先级：强制上传 > Gps开关打开 > 普通上传
     */
    private void  checkUploadState(){
        //检查强制上传GPS信息的状态是否依然存在，若存在，则进行强制上传
        if (checkForceUploadState()) {
            logger.info(TAG + "强制上传!");
            setLocationUpdate(false,false);
        }else if(TerminalFactory.getSDK().getParam(Params.GPS_STATE, false)){
            //GPS 开关打开
            logger.info(TAG + "普通上传!");
            setLocationUpdate(true,false);
        }else{
            //GPS 开关关闭
            stopUpload();
        }
    }

    /**
     * 来自主页面的定位权限打开之后
     */
    public void requestLocationByJudgePermission() {
        mHandler.sendEmptyMessage(HANDLER_WHAT_LOCATION_CHECK);
    }


    /**
     * 来自聊天页面的请求位置信息
     */
    public void requestLocationByChat() {
        mHandler.sendEmptyMessage(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT);
    }

    /**
     * 检查是否在强制上传期间
     */
    public boolean checkForceUploadState(){
        return (System.currentTimeMillis() - MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_START_TIME, 0l)
                < MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, 0));
    }

    /**
     * 关闭定位功能
     */
    private void stopUpload(){
        MyTerminalFactory.getSDK().getRecorderGPSManager().removelocationListener();
        MyTerminalFactory.getSDK().getRecorderBDGPSManager().stop();
    }

    /**
     * 设置获取位置的方式
     * @param isCommonUpload  区分是否是普通上传或者是强制上传 true 为普通上传
     * @param isChat 区分会话页面的发送位置 true 为会话页面发送位置信息
     */
    private void setLocationUpdate(final boolean isCommonUpload, final boolean isChat) {
        logger.info(TAG + "设置获取位置的方式");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //检查权限
                if(!checkPermission()){
                    return;
                }
                //如果是会话页面上传位置信息， 判断上次获取到的位置信息的时间间隔，假如时间间隔小于5s，并且位置信息有效，就直接返回上次记录的位置信息
                if (isChat && (System.currentTimeMillis() - lastLocationTime < 5000 && lastLocation != null)&&(lastLocation.getLongitude()!=0&&lastLocation.getLatitude()!=0)) {
                    dispatchCommitLocationUrgent(lastLocation);
                    return;
                }
                //获取定位间隔时间
                long uploadTime = getUploadTime(isCommonUpload,isChat);

                //如果这次的间隔时间跟上次的间隔时间相同,就不再重新设置间隔时间
                if(lastUploadTime!=uploadTime){
                    lastUploadTime = uploadTime;
                }else{
                    return;
                }

                //通知百度和gps获取位置信息
                logger.info(TAG + "上传位置信息的间隔时间" + uploadTime + "是否是会话页面发送位置信息：" + isChat + "---之前的是否是会话页面的状态:" + isChatSendLocation);
                //判断当前是否已经是会话页面上传位置信息
                if(!isChatSendLocation){
                    isChatSendLocation = isChat;
                }
                //百度定位
                final RecorderBDGPSManager bDGPSManager = MyTerminalFactory.getSDK().getRecorderBDGPSManager();
                bDGPSManager.requestLocationInfo(uploadTime);

                //GPS定位
                final RecorderGPSManager gpsManager = MyTerminalFactory.getSDK().getRecorderGPSManager();
                if (gpsManager.checkInitCompleteAndIsChat()) {
                    MyTerminalFactory.getSDK().getRecorderGPSManager().getLocationManager().requestLocationUpdates(gpsManager.getlocationProvider(), uploadTime, 0, gpsManager.getLocationListener());
                }
                //处理5s之后都没有定位到----会话页面
                if (isChat) {
                    mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT,5000);
                }
            }
        });

    }


    /**
     * 获取到位置信息之后分发数据
     *
     * @param location
     */
    public void dispatchCommitLocation(Location location) {
        lastLocation = location;
        lastLocationTime = System.currentTimeMillis();
        if (isChatSendLocation) {
            //国产手机获取到的定位就是WGS坐标系，地图地图也是，所以不用转
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, location.getLongitude(), location.getLatitude());
            isChatSendLocation = false;
            //取消来自会话页面定位可能因为超时发送的伪定位信息
            mHandler.removeMessages(HANDLER_WHAT_LOCATION_UPDATE_BY_CHAT_TIMEOUT);
            //停止定位（省电）,如果来自会话页面的定位之前就没有开始定位，会话页面定位之后就不断的定位，因为没有停止定位
            stopUpload();
            mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_CHECK,DELAYED_TIME);
        } else {
            MyTerminalFactory.getSDK().getLocationManager().upLoadLocation(location);
        }
//        stopUpload();
        //检查上传位置信息的状态（状态切换有可能时间间隔不同，有可能已经不需要上传位置信息）
//        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                mHandler.sendEmptyMessageDelayed(HANDLER_WHAT_LOCATION_CHECK,DELAYED_TIME);
//            }
//        });
    }

    /**
     * 当是会话页面请求位置信息，并且距离上次定位到的位置信息的时间间隔小于5s，并且位置有效，就直接返回上次的保存的位置信息
     *
     * @param location
     */
    public void dispatchCommitLocationUrgent(Location location) {
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, location.getLongitude(), location.getLatitude());
    }


    /**
     * 上传位置信息
     *
     * @param location
     */
    public void upLoadLocation(Location location) {
        logger.info(TAG + "准备上传位置信息---GPS是否打开:" + TerminalFactory.getSDK().getParam(Params.GPS_STATE, false) + "---时间间隔是否大于5秒：" + (System.currentTimeMillis() - lastUpLocationTime > 5000));
        if (TerminalFactory.getSDK().getParam(Params.GPS_STATE, false) &&
                !TerminalFactory.getSDK().isExit() && System.currentTimeMillis() - lastUpLocationTime > 5000) {//配置要求GPS打开，时间间隔大于5秒
            logger.info(TAG + "开始上传位置信息---Longitude:" + location.getLongitude() + "---Latitude：" + location.getLatitude());
            lastUpLocationTime = System.currentTimeMillis();
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveChangePersonLocationHandler.class,location.getLatitude(),location.getLongitude());
            String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
            int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT,0);
//			final String url = "http://192.168.1.187:6666/save";
            final String url = "http://"+ip+":"+port+"/save";
            Map<String,Object> params = new HashMap<>();
            params.put("terminalno",MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
            params.put("memberuniqueno",MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L));
            params.put("terminalType",MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE));
            params.put("longitude",location.getLongitude());
            params.put("latitude",location.getLatitude());
            params.put("speed",location.getSpeed());
            params.put("bearing",location.getBearing());
            params.put("altitude",location.getAltitude());
            params.put("mountType", MountType.MOUNT_SELF.toString());
            Gson gson = new Gson();
            final String json = gson.toJson(params);
            MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
                @Override
                public void run(){
                    MyTerminalFactory.getSDK().getHttpClient().postJson(url,"gps="+json);
                }
            });

            //上传位置信息
//            PTTProtolbuf.UploadGpsMessage.Builder uploadGpsMessageBuilder = PTTProtolbuf.UploadGpsMessage.newBuilder();
//            uploadGpsMessageBuilder.setMemberId(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
//            uploadGpsMessageBuilder.setUniqueNo(TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L));
//            uploadGpsMessageBuilder.setSessionId(MyTerminalFactory.getSDK().getParam(Params.SESSION_ID));
//            uploadGpsMessageBuilder.setVersion(MyTerminalFactory.getSDK().getParam(Params.VERSION, 0));
//            uploadGpsMessageBuilder.setLongitude(location.getLongitude());
//            uploadGpsMessageBuilder.setLatitude(location.getLatitude());
//            uploadGpsMessageBuilder.setSpeedRate(location.getSpeed());
//            uploadGpsMessageBuilder.setElevation(location.getAltitude());
////			uploadGpsMessageBuilder.setDirection(location.getDirection());
////			uploadGpsMessageBuilder.setLocation(location.getAddrStr());
//            MyTerminalFactory.getSDK().getClientChannel().sendMessage(uploadGpsMessageBuilder.build(), new PushMessageSendResultHandler() {
//
//                @Override
//                public void handler(boolean sendOK, String uuid) {
//                    logger.error(TAG + "上传位置信息数据是否成功:" + sendOK);
//                }
//            });
        }
    }

    /**
     * 检查权限
     */
    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "请打开位置权限，以方便获取您的位置信息", Toast.LENGTH_SHORT).show();
            logger.error("请打开位置权限，以方便获取您的位置信息");
            return false;
        }
        return true;
    }


    /**
     * 获取上传位置信息的间隔时间
     *
     * @param isCommonUpload
     * @param isChat
     * @return
     */
    public long getUploadTime(boolean isCommonUpload, boolean isChat) {
        if (isChat) {
            return 2000;
        }
        if (isCommonUpload) {
//			return Math.max(1 * 60 * 1000, MyTerminalFactory.getSDK().getParam(Params.GPS_UPLOAD_INTERVAL, 5 * 60 * 1000));//普通上传最小间隔是一分钟
            return MyTerminalFactory.getSDK().getParam(Params.GPS_UPLOAD_INTERVAL, 1 * 60 * 1000);
        } else {
            return Math.max(5 * 1000, MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_INTERVAL, 5 * 1000));//强制上传最小间隔是5秒钟
        }
    }

}
