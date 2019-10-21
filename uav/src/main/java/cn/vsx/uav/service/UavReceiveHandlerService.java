package cn.vsx.uav.service;

import android.content.Intent;
import android.os.Message;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MountType;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAirCraftStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.R;
import cn.vsx.uav.activity.UavPushActivity;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.util.StateMachine.IState;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.service.ReceiveHandlerService;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.MyDataUtil;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.sdk.battery.Battery;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/17
 * 描述：
 * 修订历史：
 */
public class UavReceiveHandlerService extends ReceiveHandlerService{

    private static final int UPLOAD_UAV_LOCATION = 9;
    private long uploadTime = 30*1000;
    private boolean uavConnected;

    @Override
    public void onCreate(){
        super.onCreate();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAirCraftStatusChangedHandler);
    }

    @Override
    public boolean onUnbind(Intent intent){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAirCraftStatusChangedHandler);
        return super.onUnbind(intent);
    }

    private ReceiveAirCraftStatusChangedHandler receiveAirCraftStatusChangedHandler = connected -> {
        if(connected){
            Aircraft aircraft = AirCraftUtil.getAircraftInstance();
            if(aircraft == null){
                ToastUtil.showToast(getApplicationContext(),"aircraft为null");
                logger.error("aircraft为null");
            }else {
                if(!uavConnected){
                    MyTerminalFactory.getSDK().getLiveManager().sendAircraftConnectStatus(true);
                    setConnectionFailBehavior();
                    setCollisionAvoidance();
                    myHandler.postDelayed(() -> checkBattery(),2000);
                    setUploadTime();
                    //校准
                    AirCraftUtil.calibratCompass();
                    AirCraftUtil.setFileListener();
                    //无人机连上时自动位置
                    myHandler.sendEmptyMessageDelayed(UPLOAD_UAV_LOCATION,uploadTime);
                    //无人机连上时自动上报
                    if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                        pushAircraft("");
                    }else {
                        ToastUtil.showToast(getApplicationContext(),"没有图像上报的权限，不能自动上报");
                    }
                }
            }

        }else {
            if(uavConnected){
                MyTerminalFactory.getSDK().getLiveManager().sendAircraftConnectStatus(false);
                myHandler.removeMessages(UPLOAD_UAV_LOCATION);
            }
        }
        uavConnected = connected;
    };

    //开启防碰撞功能
    private void setCollisionAvoidance(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null && flightController.getFlightAssistant() != null){
            //启用向上防碰撞
            flightController.getFlightAssistant().setUpwardsAvoidanceEnabled(true, djiError -> {
                if(djiError == null){
                    logger.info("启动向上防碰撞成功");
                }else {
                    logger.error("启动向上防碰撞失败---"+djiError.getDescription());
                }
            });
            //启动主动避障，启用后，当障碍物向飞机移动时，飞机将主动飞离它。
            // 如果在主动避开移动障碍物的同时，飞机在避让路径中检测到其他障碍物，它将停止
            flightController.getFlightAssistant().setActiveObstacleAvoidanceEnabled(true, djiError -> {
                if(djiError == null){
                    logger.info("启动主动避障成功");
                }else {
                    logger.error("启动主动避障失败---"+djiError.getDescription());
                }
            });
        }
    }

    @Override
    protected void handleMyMessage(Message msg){
        super.handleMyMessage(msg);
        if(msg.what == UPLOAD_UAV_LOCATION){
             myHandler.removeMessages(UPLOAD_UAV_LOCATION);
             String aircraftLocation = AirCraftUtil.getAircraftLocation();
             double latitude = AirCraftUtil.getLatitude(aircraftLocation);
             double longitude = AirCraftUtil.getLongitude(aircraftLocation);
             float altitude = AirCraftUtil.getAltitude(aircraftLocation);
             if(latitude !=0.0 && longitude !=0.0){
                 sendLocationMessage(latitude,longitude,altitude);
             }
             myHandler.sendEmptyMessageDelayed(UPLOAD_UAV_LOCATION,uploadTime);

        }
    }

    /**
     * 设置遥控器和飞机失联动作
     */
    private void setConnectionFailBehavior(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController !=null){
            flightController.setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.GO_HOME, djiError -> {
                if(djiError == null){
                    logger.error("设置失联自动返航成功");
                }else {
                    logger.error("设置失联自动返航失败:"+djiError.getDescription());
                }
            });
        }
    }

    private void sendLocationMessage(double latitude,double longitude,double altitude){
        String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
        int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT,0);
        //final String url = "http://192.168.1.174:6666/save";
        final String url = "http://"+ip+":"+port+"/save";
        Map<String,Object> params = new HashMap<>();
        params.put("terminalno",MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
        params.put("memberuniqueno",MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L));
        params.put("longitude",longitude);
        params.put("latitude",latitude);
        params.put("speed",0.0f);
        params.put("bearing",0.0f);
        params.put("altitude",altitude);
        params.put("addressStr","");
        params.put("mountType", MountType.MOUNT_UAV.toString());
        Gson gson = new Gson();
        final String json = gson.toJson(params);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getHttpClient().postJson(url,"gps="+json));
    }

    /**
     * 监听电量
     */
    private void checkBattery(){
        //测试一下如果只有单个电池会不会走下面代码
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft != null){
            List<Battery> batteries = aircraft.getBatteries();
            if(batteries != null && !batteries.isEmpty()){
                logger.info("电池个数：" + batteries.size());
                Battery battery = batteries.get(0);
                battery.setStateCallback(batteryState -> {
                    //电池电量小于等于10%时自动返航
                    //                logger.info("无人机电池电量:"+batteryState.getChargeRemainingInPercent());
                    if(batteryState.getChargeRemainingInPercent() == 15){
                        FlightController flightController = AirCraftUtil.getFlightController();
                        if(flightController != null){
                            if(!flightController.getState().isGoingHome()){
                                //自动返航代码
                                autoGoHome();
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * 自动返航
     */
    private void autoGoHome(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(null != flightController){
            flightController.startGoHome(djiError -> {
                if(djiError == null ){
                    logger.info("自动返航成功");
                }else {
                    logger.error("autoGoHome--"+djiError.getDescription());
                }
            });
        }
    }

    /**
     * 检查上传位置信息的状态
     * 优先级：强制上传 > Gps开关打开 > 普通上传
     */
    private void setUploadTime() {
        uploadTime = 10*1000;
    }

    @Override
    protected void onVideoLiveComming(String mainMemberName, int mainMemberId, boolean emergencyType){
        if(emergencyType){
            //强制上报图像
            //如果在组呼或者听组呼时  就停止
            Map<TerminalState, IState<?>> currentStateMap = TerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
            if(currentStateMap.containsKey(TerminalState.GROUP_CALL_LISTENING)||currentStateMap.containsKey(TerminalState.GROUP_CALL_SPEAKING)){
                TerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            }
            myHandler.post(() -> PromptManager.getInstance().startReportByNotity());
            MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
            //自动打开上报
            Intent intent = new Intent(getApplicationContext(),UavPushActivity.class);
            intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            //判断无人机有没有连接上，没有连接上要响铃让用户选择,已经连上直接上报
            if(AirCraftUtil.getAircraftInstance() == null){
                if(!checkFloatPermission()){
                    startSetting();
                    return;
                }
                startTranspantActivity();
                Intent intent = new Intent();
                intent.putExtra(Constants.MEMBER_NAME, mainMemberName);
                intent.putExtra(Constants.MEMBER_ID, mainMemberId);
                intent.putExtra(Constants.THEME,"");
                intent.setClass(UavReceiveHandlerService.this, UavReceiveLiveCommingService.class);
                startService(intent);
            }else {
                //直接上报
                Intent intent = new Intent(getApplicationContext(), UavPushActivity.class);
                intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onActivePushVideo(String uniqueNoAndType, boolean isGroupPushLive){
        if(!checkFloatPermission()){
            startSetting();
            return;
        }
        if(MyApplication.instance.getVideoLivePlayingState() != VideoLivePlayingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_watching_can_not_report));
            AppKeyUtils.setAppKey(null);
            return;
        }
        if(MyApplication.instance.getVideoLivePushingState() != VideoLivePushingState.IDLE){
            ToastUtil.showToast(MyTerminalFactory.getSDK().application,getString(R.string.text_pushing_can_not_report));
            AppKeyUtils.setAppKey(null);
            return;
        }
        logger.error("上报给：" + uniqueNoAndType);
        pushAircraft(uniqueNoAndType);
    }

    private void pushAircraft(String uniqueNoAndType){
        //没有选择成员就上报到当前组
        if(android.text.TextUtils.isEmpty(uniqueNoAndType)){
            int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0);
            uniqueNoAndType = MyDataUtil.getPushInviteMemberData(currentGroupId, ReceiveObjectMode.GROUP.toString());
        }
        ArrayList<String> uniqueNos = new ArrayList<>();
        uniqueNos.add(uniqueNoAndType);
        Intent intent = new Intent(getApplicationContext(), UavPushActivity.class);
        if(!ActivityCollector.isActivityExist(UavPushActivity.class)){
            intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList(uniqueNos));
            intent.putExtra(Constants.TYPE,Constants.ACTIVE_PUSH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
