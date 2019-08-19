package cn.vsx.uav.service;

import android.content.Intent;
import android.os.Message;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MountType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.terminal.TerminalState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAirCraftStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.activity.UavPushActivity;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.util.StateMachine.IState;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.service.ReceiveHandlerService;
import cn.vsx.vc.utils.Constants;
import dji.common.flightcontroller.ConnectionFailSafeBehavior;
import dji.sdk.battery.Battery;
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
                    myHandler.postDelayed(() -> checkBattery(aircraft),2000);
                    setUploadTime();
                    //校准
                    AirCraftUtil.calibratCompass();
                    AirCraftUtil.setFileListener();
                    //无人机连上时自动位置
                    myHandler.sendEmptyMessageDelayed(UPLOAD_UAV_LOCATION,uploadTime);
                    //无人机连上时自动上报
                    if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                        Intent intent = new Intent(getApplicationContext(),UavPushActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //                    Intent intent = new Intent(getApplicationContext(), AircraftPushService.class);
                        //                    startService(intent);
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
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            aircraft.getFlightController().setConnectionFailSafeBehavior(ConnectionFailSafeBehavior.GO_HOME, djiError -> {
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
        params.put("mountType", MountType.MOUNT_UAV.toString());
        Gson gson = new Gson();
        final String json = gson.toJson(params);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getHttpClient().postJson(url,"gps="+json));
    }

    /**
     * 监听电量
     */
    private void checkBattery(Aircraft aircraft){
        //测试一下如果只有单个电池会不会走下面代码
        List<Battery> batteries = aircraft.getBatteries();
        if(batteries !=null && !batteries.isEmpty()){
            logger.info("电池个数："+batteries.size());
            Battery battery = batteries.get(0);
            battery.setStateCallback(batteryState -> {
                //电池电量小于等于10%时自动返航
                //                logger.info("无人机电池电量:"+batteryState.getChargeRemainingInPercent());
                if(batteryState.getChargeRemainingInPercent() == 10){
                    if(!aircraft.getFlightController().getState().isGoingHome()){
                        //自动返航代码
                        autoGoHome();
                    }
                }
            });
        }

        //        if(mProduct.getBattery() !=null){
        //
        //            mProduct.getBattery().setStateCallback(new BatteryState.Callback(){
        //                @Override
        //                public void onUpdate(BatteryState batteryState){
        //                    //电池电量小于等于10%时自动返航
        //                    //                logger.info("无人机电池电量:"+batteryState.getChargeRemainingInPercent());
        //                    if(batteryState.getChargeRemainingInPercent() == 10){
        //                        if(!((Aircraft) mProduct).getFlightController().getState().isGoingHome()){
        //                            //自动返航代码
        //                            autoGoHome(mProduct);
        //                        }
        //                    }
        //                }
        //            });
        //        }
    }

    /**
     * 自动返航
     */
    private void autoGoHome(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(null != aircraft){
            aircraft.getFlightController().startGoHome(djiError -> {
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else{
            Intent intent = new Intent();
            intent.putExtra(Constants.MEMBER_NAME, mainMemberName);
            intent.putExtra(Constants.MEMBER_ID, mainMemberId);
            intent.putExtra(Constants.THEME,"");
            intent.setClass(UavReceiveHandlerService.this, UavReceiveLiveCommingService.class);
            startService(intent);
        }
    }
}
