package ptt.terminalsdk.manager.powersave;

import android.app.AlarmManager;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.powersave.IPowerSaveManager;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePowerSaveStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePowerSaveTimeChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.bean.PowerSaveStatus;
import ptt.terminalsdk.bean.PowerSaveTimeBean;
import ptt.terminalsdk.broadcastreceiver.PowerSaveReceiver;
import ptt.terminalsdk.receiveHandler.ReceiverAppFrontAndBackStatusHandler;
import ptt.terminalsdk.receiveHandler.ReceiverBusinessInServiceStatusHandler;
import ptt.terminalsdk.receiveHandler.ReceiverPowerSaveCountDownHandler;
import ptt.terminalsdk.tools.StringUtil;

import static android.content.Context.ALARM_SERVICE;

/**
 * 省电管理
 */
public class PowerSaveManager implements IPowerSaveManager {
    protected Logger logger = Logger.getLogger(this.getClass());
    private static final String TAG = "PowerSaveManager---";
    //记得判空处理
    private Application context;
    //-------------------状态标记----------------------
    //省电是否开启（设备类型）
    private boolean isOpenByDevices = false;
    //是否是省电的模式
    private boolean isSave = false;
    //是否在前台
    private boolean isForeground = true;
    //是否是解锁
    private boolean isScreenPresent = true;
    //存储service的集合
    private List<String> services = new ArrayList<>();

    private AlarmManager alarmManager;

    //-------------------action----------------------
    //准备进入省电模式
    private final String INTENT_ACTION_PRE_SAVE_STATUS = "vsxin.action.powersave.presavestatus";
    //进入省电模式
    private final String INTENT_ACTION_SAVE_STATUS = "vsxin.action.powersave.savestatus";
    //进入活动模式
    private final String INTENT_ACTION_ACTIVITY_STATUS = "vsxin.action.powersave.activitytatus";

    private List<String> actions = Arrays.asList(INTENT_ACTION_PRE_SAVE_STATUS, INTENT_ACTION_SAVE_STATUS, INTENT_ACTION_ACTIVITY_STATUS);

    //-------------------handler----------------------
    //屏幕状态变化
//    private final int HANDLER_CODE_SCREEN_STATUS = 1;
    //前后台变化
//    private final int HANDLER_CODE_FRONT_BACK_STATUS = 2;
    //service的业务变化
//    private final int HANDLER_CODE_BUSINESS_IN_SERVICE_STATUS = 3;

//    private final int HANDLER_CODE_ACTIVITY_STATUS = 4;
    //更新时间
//    private final int HANDLER_CODE_UPDTAE_TIME = 5;

    //准备进入省电模式的倒计时（触发条件手机黑屏或App调到后台）
    private final int HANDLER_CODE_READY_TO_SAVE_STATUS = 1;

    //-------------------倒计时----------------------
    //准备进入省电模式的倒计时（10分钟）
    private final long DELAYED_TIME_READY_TO_SAVE_STATUS = 10*60*1000;
//    private final long DELAYED_TIME_READY_TO_SAVE_STATUS = 0;
    //省电模式的倒计时（55分钟）
    private final long DELAYED_TIME_SAVE_STATUS = 55*60*1000;
//    private final long DELAYED_TIME_SAVE_STATUS = 20*1000;
    //活动模式的倒计时（5分钟）
    private final long DELAYED_TIME_ACTIVITY_STATUS = 5*60*1000;
//    private final long DELAYED_TIME_ACTIVITY_STATUS = 0;
    //如果省电模式的倒计时和活动模式的倒计时都是0
    private final int DELAYED_TIME_STATUS_TIME_IS_ZERO = 60*1000;

    //记录准备进入省电模式的倒计时
    private long tempReadyToSaveStatusTime = DELAYED_TIME_READY_TO_SAVE_STATUS;
    //记录省电模式的倒计时
    private long tempSaveStatusTime = DELAYED_TIME_SAVE_STATUS;
    //活动模式的倒计时
    private long tempActivityStatusTime = DELAYED_TIME_ACTIVITY_STATUS;

    //记录当前的状态
    private PowerSaveStatus tempStatus = PowerSaveStatus.ACTIVITY;
    //记录倒计时开始的时间
    private long tempTime = 0L;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what){
//                case HANDLER_CODE_SCREEN_STATUS:
//                    break;
//                case HANDLER_CODE_FRONT_BACK_STATUS:
//                    break;
//                case HANDLER_CODE_BUSINESS_IN_SERVICE_STATUS:
//                    break;
//                case HANDLER_CODE_ACTIVITY_STATUS:
//                    //清空所有的倒计时，更新为活动状态，通知其他地方
////                    activityStatus();
//                    break;
//                default:break;
//            }

        }
    };

    public PowerSaveManager(Application context) {
        this.context = context;
    }

    @Override
    public void start(){
        isOpenByDevices = true;
        isScreenPresent = true;
        services.clear();
        tempStatus = PowerSaveStatus.ACTIVITY;
        getAlarmManager();
        // 注册屏幕状态的监听
        registerScreenStatusReceiver();
        //注册前后台的通知
        TerminalFactory.getSDK().registReceiveHandler(receiverAppFrontAndBackStatusHandler);
        //注册service的创建和销毁的通知
        TerminalFactory.getSDK().registReceiveHandler(receiverBusinessInServiceStatusHandler);
        //注册倒计时结束的通知
        TerminalFactory.getSDK().registReceiveHandler(receiverPowerSaveCountDownHandler);
        //注册省电模式的时间变动的通知
        TerminalFactory.getSDK().registReceiveHandler(receivePowerSaveTimeChangedHandler);

    }

    @Override
    public void stop(){
        isOpenByDevices = false;
        isScreenPresent = false;
        services.clear();
        tempStatus = PowerSaveStatus.ACTIVITY;
        //清空所有的倒计时
        clearAllAlarm();
        //注销屏幕状态的监听
        unRegisterScreenStatusReceiver();
        //注销前后台的通知
        TerminalFactory.getSDK().unregistReceiveHandler(receiverAppFrontAndBackStatusHandler);
        //注销service的创建和销毁的通知
        TerminalFactory.getSDK().unregistReceiveHandler(receiverBusinessInServiceStatusHandler);
        //注销倒计时结束的通知
        TerminalFactory.getSDK().unregistReceiveHandler(receiverPowerSaveCountDownHandler);
        //注销省电模式的时间变动的通知
        TerminalFactory.getSDK().unregistReceiveHandler(receivePowerSaveTimeChangedHandler);
    }

    /**
     * 注册屏幕状态的监听
     */
    private void registerScreenStatusReceiver(){
        if(context!=null){
            IntentFilter filterLock = new IntentFilter();
            filterLock.addAction(Intent.ACTION_SCREEN_OFF);
            filterLock.addAction(Intent.ACTION_SCREEN_ON);
            filterLock.addAction(Intent.ACTION_USER_PRESENT);
            context.registerReceiver(screenStatusReceiver, filterLock);
        }
    }
    /**
     * 注销屏幕状态的监听
     */
    private void unRegisterScreenStatusReceiver(){
        if(context!=null){
            context.unregisterReceiver(screenStatusReceiver);
        }
    }

    /**
     * 前后台切换的通知
     */
    private ReceiverAppFrontAndBackStatusHandler receiverAppFrontAndBackStatusHandler = new ReceiverAppFrontAndBackStatusHandler() {
        @Override
        public void handler(boolean isFront) {
            logger.debug(TAG+"receiverAppFrontAndBackStatusHandler--isFront:"+isFront);
            if(!isOpenByDevices){
                return;
            }
            isForeground = isFront;
            if(isFront){
//                //判断是否在锁屏页面
                if(!isScreenPresent){
                    return;
                }
                //前台
                activityStatus(true);
                return;
            }
            //后台
            //需要判断是否有在service中实现的业务（个呼，上报图像（包括没有注册的上报图像），观看图像，视频会商，邀请人员页面）
            if(checkBusinessInServiceIsWorking()){
                //有业务在执行
                if(checkIsCountDowning()){
                    activityStatus(true);
                }
            }else{
                //没有执行的业务,开启准备进入省电模式的倒计时
                preSaveStatus();
            }
        }
    };

    /**
     * service中实现的业务（个呼，上报图像（包括没有注册的上报图像），观看图像，视频会商，邀请人员页面）
     * 是否在工作中的通知
     */
    private ReceiverBusinessInServiceStatusHandler receiverBusinessInServiceStatusHandler = new ReceiverBusinessInServiceStatusHandler() {
        @Override
        public void handler(String name,boolean create) {
            logger.debug(TAG+"receiverBusinessInServiceStatusHandler--name:"+name+"--create:"+create);
            if(!isOpenByDevices){
                return;
            }
            updataBusinessInServiceIsWorking(name,create);
            boolean isWorking = checkBusinessInServiceIsWorking();
            if(isWorking){
                //有业务中执行
                activityStatus(true);
                return;
            }
            //没有执行的业务
            //判断应用是否在前台
            if(isForeground){
                //在前台,判断是否有准备进入省电模式的倒计时，有就停掉
                activityStatus(true);
            }else{
                //在后台,判断是否有准备进入省电模式的倒计时
                preSaveStatus();
            }
        }
    };
    /**
     * 屏幕状态的通知
     */
    private BroadcastReceiver screenStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logger.info(TAG+"screenStatusReceiver:"+intent.getAction());
            if(!isOpenByDevices){
                return;
            }
            if (TextUtils.equals(Intent.ACTION_SCREEN_ON,intent.getAction())|| TextUtils.equals(Intent.ACTION_USER_PRESENT,intent.getAction())) {
                if(TextUtils.equals(Intent.ACTION_USER_PRESENT,intent.getAction())){
                    isScreenPresent = true;
                }
                //屏幕点亮 (判断是否在前台)
                if(isForeground){
                    //在前台：
                    activityStatus(true);
                    return;
                }
                //判断是否有业务在执行
                if(checkBusinessInServiceIsWorking()){
                    //有执行的业务
                    activityStatus(true);
                }else{
                    //没有业务在执行
                    //1.不到省电模式时间，不处理因为息屏或者切换后台导致的准备进入省电模式的倒计时；
                    //2.超过省电模式倒计时（就是正在省电模式的中，或者在省电模式和活动模式切换的状态），不处理
                }
            }else if (TextUtils.equals(Intent.ACTION_SCREEN_OFF,intent.getAction())){
                isScreenPresent = false;
                //屏幕熄灭
                if(checkBusinessInServiceIsWorking()){
                    //有执行的业务
                    activityStatus(true);
                }else{
                    //没有业务在执行
                    preSaveStatus();
                }
            }
        }
    };

    /**
     * 倒计时结束的通知
     */
    private ReceiverPowerSaveCountDownHandler receiverPowerSaveCountDownHandler = new ReceiverPowerSaveCountDownHandler() {
        @Override
        public void handler(String action) {
            logger.info(TAG+"receiverPowerSaveCountDownHandler--action:"+action);
            if(!isOpenByDevices){
                return;
            }
            switch (action){
                case INTENT_ACTION_PRE_SAVE_STATUS:
                    //准备进入省电模式的倒计时结束，请求服务器获取省电模式和活动模式的时间
                    handler.post(() -> { requestSaveStatusAndActivityStatusTime(false); });
                    break;
                case INTENT_ACTION_SAVE_STATUS:
                    //省电模式的倒计时结束，进入活动模式
                    activityStatus(false);
                    break;
                case INTENT_ACTION_ACTIVITY_STATUS:
                    //活动模式的倒计时结束，准备切换进入省电模式，（需要请求服务器获取省电模式和活动模式的时间）
                    handler.postDelayed(() -> requestSaveStatusAndActivityStatusTime(false),
                            (tempSaveStatusTime == 0 && tempActivityStatusTime == 0)?DELAYED_TIME_STATUS_TIME_IS_ZERO:0);
                    break;
                default:break;
            }
        }
    };

    /**
     * 省电模式的时间变动的通知
     */
    private ReceivePowerSaveTimeChangedHandler receivePowerSaveTimeChangedHandler = new ReceivePowerSaveTimeChangedHandler() {
        @Override
        public void handler() {
            logger.debug(TAG+"receivePowerSaveTimeChangedHandler");
            if(!isOpenByDevices){
                return;
            }
            //状态变更
            //请求省电模式的时间
            requestSaveStatusAndActivityStatusTime(true);
        }
    };

    /**
     * 准备进入省电模式
     * 1.
     */
    private synchronized void preSaveStatus(){
        logger.info(TAG+"preSaveStatus-tempReadyToSaveStatusTime:"+tempReadyToSaveStatusTime);
        if(checkIsCountDowning()){
            //在准备进入省电模式的倒计时中，或者正在省电模式中，或者在省电模式和活动模式切换的状态，不处理
        }else{
            //记录状态
            tempStatus = PowerSaveStatus.PRE_SAVE;
            //更新省电模式的状态，并通知其他地方
            updatePowerSaveStatusChanged(false,true);
            //判断时间是否大于0
            if(tempReadyToSaveStatusTime>0){
                //开启准备进入省电模式的倒计时
                startAlarmManager(INTENT_ACTION_PRE_SAVE_STATUS,getCurrentTime()+ tempReadyToSaveStatusTime);
            }else{
                //直接获取省电模式和活动模式的时间
                getCurrentTime();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,INTENT_ACTION_PRE_SAVE_STATUS);
            }
        }
    }

    /**
     * 省电模式
     * 请求省电模式和活动模式的时间
     * 开启省电模式的倒计时
     * 更新省电模式的状态，并通知其他地方
     */
    private synchronized void saveStatus(){
        logger.info(TAG+"saveStatus-tempSaveStatusTime:"+tempSaveStatusTime);
        if(tempSaveStatusTime > 0){
            //记录状态
            tempStatus = PowerSaveStatus.SAVE;
            //更新省电模式的状态，并通知其他地方
            updatePowerSaveStatusChanged(true,true);
            //开启省电模式的倒计时
            startAlarmManager(INTENT_ACTION_SAVE_STATUS,getCurrentTime()+tempSaveStatusTime);
        } else {
            //直接进入下一个状态，活动状态
            getCurrentTime();
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,INTENT_ACTION_SAVE_STATUS);
        }
    }

    /**
     * 活动模式
     * 清空所有的倒计时，更新为活动状态，通知其他地方
     *   1.不到省电模式时间，停止因为息屏或者切换后台导致的准备进入省电模式的倒计时；
     *   2.超过省电模式倒计时（就是正在省电模式的中，或者在省电模式和活动模式切换的状态），切换为活动模式，停止省电模式或者活动的倒计时
     */
    private synchronized void activityStatus(boolean clearAllAlarm) {
        logger.info(TAG+"activityStatus--tempActivityStatusTime:"+tempActivityStatusTime+"-clearAllAlarm:"+clearAllAlarm);
        //记录状态
        tempStatus = PowerSaveStatus.ACTIVITY;
        if(clearAllAlarm){
            //更新省电模式的状态，并通知其他地方
            updatePowerSaveStatusChanged(false,true);
            //停止所有的倒计时
            clearAllAlarm();
        } else {
            if(tempActivityStatusTime>0){
                //更新省电模式的状态，并通知其他地方
                updatePowerSaveStatusChanged(false,true);
                startAlarmManager(INTENT_ACTION_ACTIVITY_STATUS,getCurrentTime()+tempActivityStatusTime);
            }else{
                //直接进入下一个状态，获取省电模式和活动模式的时间
                getCurrentTime();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,INTENT_ACTION_ACTIVITY_STATUS);
            }
        }
    }

    /**
     * 请求服务端省电模式和活动模式的时间
     */
    @Override
    public synchronized void requestSaveStatusAndActivityStatusTime(boolean isChangeNow) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            try{
                //{"success":true,"msg":"请求成功！","data":{"deviceId":34598,"activeTime":"5","dormancyTime":"30","lockScreenTime":"10"}}
                String httpIp = TerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                int httpPort = TerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                long uniqueNo = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L);
                String url = "http://" + httpIp + ":" + httpPort + "/file/terminal/findDeviceConfigByDeviceNo?deviceNo="+uniqueNo;
                if(TextUtils.isEmpty(httpIp)||httpPort<=0){
                    return;
                }
                String result = TerminalFactory.getSDK().getHttpClient().sendGet(url);
//            logger.debug("获取省电模式相关的时间result：" + result);
                PowerSaveTimeBean response = JSONObject.parseObject(result, PowerSaveTimeBean.class);
                //给获取到的时间赋值(如果没有获取到就使用默认的值)
                if(response!=null&&response.isSuccess()&&response.getData()!=null){
                    tempReadyToSaveStatusTime = getTime(response.getData().getLockScreenTime());
                    tempSaveStatusTime = getTime(response.getData().getDormancyTime());
                    tempActivityStatusTime = getTime(response.getData().getActiveTime());
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                logger.info(TAG+"requestSaveStatusAndActivityStatusTime-tempReadyToSaveStatusTime:"
                        +tempReadyToSaveStatusTime+"-tempSaveStatusTime:"+tempSaveStatusTime+"-tempActivityStatusTime:"+tempActivityStatusTime);
                //获取之后需要判断是否是需要立即生效的
                responseSaveStatusAndActivityStatusTime(isChangeNow);
            }
        });
    }

    private long getTime(String time){
        return StringUtil.toLong(time)*60*1000;
    }

    /**
     * 响应送服务端获取到省电模式和活动模式的时间的操作
     * 1.更新倒计时
     * 2.进入省电模式
     * @param isChangeNow
     */
    private synchronized void responseSaveStatusAndActivityStatusTime(boolean isChangeNow){
        //判断是否是来自时间修改的通知
        if(!isChangeNow){
            //不是来自时间修改的通知，直接进入省电模式
            saveStatus();
            return;
        }
        //判断是否正在倒计时
        if(!checkIsCountDowning()){
            //如果当前倒计时没有开启，不处理，（时间已经修改，下次开启倒计时就会用最新的时间）
            return;
        }
        //判断当前的状态，执行不同的倒计时
        updateStatusInCountDowning();
    }

    /**
     * 当在倒计时的情况下更新状态
     */
    private synchronized void updateStatusInCountDowning(){
        if(tempStatus == PowerSaveStatus.PRE_SAVE){
            //准备进入省电模式的状态
            long time = getTempTime()+ tempReadyToSaveStatusTime;
            if(time>System.currentTimeMillis()){
                startAlarmManager(INTENT_ACTION_PRE_SAVE_STATUS,time);
            } else {
                //直接转到获取时间的状态，停止倒计时
                clearAllAlarm();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,INTENT_ACTION_ACTIVITY_STATUS);
            }
        }else if(tempStatus == PowerSaveStatus.SAVE){
            //正在省电模式中
            long time = getTempTime()+ tempSaveStatusTime;
            if(time>System.currentTimeMillis()){
                startAlarmManager(INTENT_ACTION_SAVE_STATUS,time);
            }else{
                //直接转到活动模式的状态，停止倒计时
                clearAllAlarm();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,INTENT_ACTION_SAVE_STATUS);
            }
        }else if(tempStatus == PowerSaveStatus.ACTIVITY){
            //活动模式中
            long time = getTempTime()+ tempActivityStatusTime;
            if(time>System.currentTimeMillis()){
                startAlarmManager(INTENT_ACTION_ACTIVITY_STATUS,time);
            }else{
                //直接转到获取时间的状态，停止倒计时
                clearAllAlarm();
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverPowerSaveCountDownHandler.class,INTENT_ACTION_ACTIVITY_STATUS);
            }
        }
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
            clearAlarmExceptAction(alarmManager,action);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 清空倒计时除了传入的action之外的倒计时
     */
    private void clearAlarmExceptAction(AlarmManager alarmManager,String actionStr) {
        logger.info(TAG+"clearAlarmExceptAction--actionStr:"+actionStr);
        try{
            if(alarmManager!=null){
                for (String action: actions) {
                    if(!TextUtils.equals(action,actionStr)){
                        alarmManager.cancel(getPendingIntent(action));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.debug(TAG+"clearAlarmExceptAction--e:"+e.toString());
        }
    }

    /**
     * 清空所有的倒计时
     */
    private void clearAllAlarm() {
        logger.info(TAG+"clearAllAlarm");
        try{
            if(alarmManager!=null){
                for (String action: actions) {
                    alarmManager.cancel(getPendingIntent(action));
                }
            }
            alarmManager = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *更新service集合
     * @return
     */
    private synchronized void updataBusinessInServiceIsWorking(String name,boolean create){
        if(create){
            if(!services.contains(name)){
                services.add(name);
            }
        }else{
            services.remove(name);
        }
        logger.info(TAG+"updataBusinessInServiceIsWorking--services:"+services);
    }

    /**
     * 检查是否有
     * service中实现的业务（个呼，上报图像（包括没有注册的上报图像），观看图像，视频会商，邀请人员页面）
     * @return
     */
    private boolean checkBusinessInServiceIsWorking(){
        return !services.isEmpty();
    }

    /**
     * 检查是否已经开始准备进入省电模式的倒计时
     * @return
     */
    private synchronized boolean checkIsCountDowning(){
        return (alarmManager!=null);
    }

    /**
     * 是否在锁屏的状态
     * @return
     */
    private boolean checkIsLockScreen(){
        boolean result = false;
        KeyguardManager mKeyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if(mKeyguardManager!=null){
            result =  mKeyguardManager.inKeyguardRestrictedInputMode();
        }
        logger.info(TAG+"checkIsLockScreen-result:"+result);
        return result;
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
        Intent intent = new Intent(context, PowerSaveReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    /**
     * 改变省电模式的状态
     * @param isSave
     */
    private void updatePowerSaveStatusChanged(boolean isSave,boolean isNotify){
        boolean notify = isNotify && (this.isSave!=isSave);
        //更新状态
        this.isSave = isSave;
        //通知其他位置
        if(notify){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceivePowerSaveStatusChangedHandler.class,isSave);
        }
    }

    /**
     * 获取当前的时间
     * @return
     */
    private long getCurrentTime(){
        tempTime = System.currentTimeMillis();
        return tempTime;
    }

    /**
     * 获取记录的时间
     * @return
     */
    private long getTempTime(){
        if(tempTime == 0){
            tempTime = System.currentTimeMillis();
        }
        return tempTime;
    }

    /**
     * 是否开启（设备类型）
     * @return
     */
    @Override
    public boolean isOpenByDevices(){
        return isOpenByDevices;
    }

    @Override
    public boolean isSave() {
        return isSave;
    }
}
