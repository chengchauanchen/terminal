package cn.vsx.uav.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAirCraftStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStopUAVPatrolHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUAVPatrolHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.R;
import cn.vsx.uav.UavApplication;
import cn.vsx.uav.receiveHandler.ReceiveCalibrationStateCallbackHandler;
import cn.vsx.uav.service.PushService;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.uav.view.CustomWebView;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.adapter.MemberEnterAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.PushLiveMemberList;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.service.InviteMemberService;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ScreenState;
import cn.vsx.vc.utils.ScreenSwitchUtils;
import cn.vsx.vc.utils.ToastUtil;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.CompassCalibrationState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.keysdk.CameraKey;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.panel.CameraSettingAdvancedPanel;
import dji.ux.panel.CameraSettingExposurePanel;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.DialogUtils;

public class UavPushActivity extends BaseActivity{

    private TextureView mSvAircraftLive;
    private LinearLayout mLlAircraftLiveGroupCall;
    private TextView mTvAircraftLiveSpeakingName;
    private TextView mTvAircraftLiveGroupName;
    private TextView mTvAircraftLiveSpeakingId;
    private ImageView mIvAircraftLiveRetract;
    private ListView mLvAircraftLiveMemberInfo;
    private RelativeLayout mAircraftLive;

    private RelativeLayout mAircraftRoot;

    private ImageView imageView;

    private Button btnCheckObstacle;
    private Button btnGoHome;
    private CameraSettingAdvancedPanel cameraSettingAdvancedPanel;

    //地图控件
    private RelativeLayout mapMinRl;
    private CustomWebView mapMinWebview;
    private View mapTouch;

    private RelativeLayout mapMaxRl;
    private CustomWebView mapMaxWebview;
    private TextureView mapAircraftLive;
    private Button btnHomeLocation;
    private Button btn_auto_flight;
//    protected LinearLayout mLlNoNetwork;
    private ImageView iv_menu;
    private ImageView iv_setting;

    private Runnable hideFocusView = this::hideFocusView;
    public static WaypointMission.Builder waypointMissionBuilder;
    private WaypointMissionOperator instance;
    //巡航结束之后的动作
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    //飞机的朝向
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    private CameraSettingExposurePanel cameraSettingExposurePanel;
    //所有的巡航点
    private List<Waypoint> waypointList = new ArrayList<>();
    //默认飞行高度和速度
    private float altitude = 35.0f;
    private float mSpeed = 2.0f;
    private boolean focusViewAdd;
    private MemberEnterAdapter memberEnterAdapter;
    private ArrayList<VideoMember> watchingMembers = new ArrayList<>();
    private List<VideoMember> memberEnterList = new ArrayList<>();
    private boolean patrol;
    private long textureAvailableTime;
    private PushService pushService;
    private boolean uavConnected = false;
    private ImageView mIvPush;
    private ImageView mIvPreview;
    private ImageView mIvTakePhoto;
    private Button mBtnVoice;
    private Button mBtnStopPush;
    private LinearLayout mLlCloseVoice;
    private ImageView mIvCloseVoice;
    private TextView mVDrakBackgroupd;
    private static final int REQUEST_FILE_CODE = 99;
    private List<String> pushMemberList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        pushService = UavApplication.getApplication().getPushService();
        if(pushService == null){
            logger.info("pushService 为null!!!");
            ToastUtils.showShort("无人机没有连接，不能上报");
            finish();
        }
        ScreenSwitchUtils.init(this).setPortraitEnable(false);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent){
        setIntent(intent);
        initData();
    }

    @Override
    public Resources getResources(){
        return AdaptScreenUtils.adaptWidth(super.getResources(), 1200);
    }

    @Override
    protected void setOrientation(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
    }

    @Override
    public int getLayoutResId(){
        return R.layout.activity_uav_push;
    }

    @Override
    public void onBackPressed(){
        //        super.onBackPressed();
        //        finish();
    }

    @Override
    public void initView(){
        mAircraftLive = findViewById(R.id.aircraft_live);
        //        mRlAircraftParentView = findViewById(R.id.rl_aircraft_parent_view);
        mSvAircraftLive = findViewById(R.id.sv_aircraft_live);
        mLlAircraftLiveGroupCall = findViewById(R.id.ll_aircraft_live_group_call);
        mTvAircraftLiveSpeakingName = findViewById(R.id.tv_aircraft_live_speakingName);
        mTvAircraftLiveGroupName = findViewById(R.id.tv_aircraft_live_groupName);
        mTvAircraftLiveSpeakingId = findViewById(R.id.tv_aircraft_live_speakingId);
        mIvAircraftLiveRetract = findViewById(R.id.iv_aircraft_live_retract);
        mLvAircraftLiveMemberInfo = findViewById(R.id.lv_aircraft_live_member_info);
        iv_menu = findViewById(R.id.iv_menu);
        iv_setting = findViewById(R.id.iv_setting);
        cameraSettingAdvancedPanel = findViewById(R.id.cameraSettingAdvancedPanel);
        cameraSettingExposurePanel = findViewById(R.id.cameraSettingExposurePanel);
        mAircraftRoot = findViewById(R.id.aircraft_root);
        btnCheckObstacle = findViewById(R.id.btn_check_obstacle);
        btnGoHome = findViewById(R.id.btn_go_home);
        //地图控件
        mapMinRl = findViewById(R.id.map_min_rl);
        mapMinWebview = findViewById(R.id.map_min_webview);
        mapTouch = findViewById(R.id.map_touch);
        mapMaxRl = findViewById(R.id.map_max_rl);
        mapMaxWebview = findViewById(R.id.map_max_webview);
        mapAircraftLive = findViewById(R.id.map_aircraft_live);
        btnHomeLocation = findViewById(R.id.btn_home_location);
        btn_auto_flight = findViewById(R.id.btn_auto_flight);
        mIvPush = findViewById(R.id.iv_push);
        mIvPreview = findViewById(R.id.iv_preview);
        mIvTakePhoto = findViewById(R.id.iv_take_photo);
        mBtnVoice = findViewById(R.id.btn_voice);
        mBtnStopPush = findViewById(R.id.btn_stop_push);
        mLlCloseVoice = findViewById(R.id.ll_close_voice);
        mIvCloseVoice = findViewById(R.id.iv_close_voice);
        mVDrakBackgroupd = findViewById(R.id.v_drak_backgroupd);
        uavConnected = AirCraftUtil.getProductInstance() != null;
        if(!uavConnected){
            mVDrakBackgroupd.setVisibility(View.VISIBLE);
            //            mBtnStopPush.setVisibility(View.VISIBLE);
            mIvTakePhoto.setImageResource(R.drawable.uav_take_photo_disable);
        }else{
            mVDrakBackgroupd.setVisibility(View.GONE);
            //            mBtnStopPush.setVisibility(View.GONE);
            mIvTakePhoto.setImageResource(R.drawable.uav_take_photo);
        }
        myHandler.postDelayed((Runnable) () -> {
            mIvCloseVoice.setVisibility(View.GONE);
        },5000);
    }

    @Override
    public void initListener(){
        mIvCloseVoice.setOnClickListener(onCloseVoiceClickListener);
        mBtnStopPush.setOnClickListener(oStopButtonClickListener);
        mIvTakePhoto.setOnClickListener(onTakePhotoClickListener);
        mBtnVoice.setOnClickListener(onVoiceButtonClickListener);
        mIvPreview.setOnClickListener(onPreViewClickListener);
        mIvPush.setOnClickListener(onInviteMemberClickListener);
        mIvAircraftLiveRetract.setOnClickListener(onRetractClickListener);
        mSvAircraftLive.setSurfaceTextureListener(surfaceTextureListener);
        mapAircraftLive.setSurfaceTextureListener(surfaceTextureListener);
        iv_menu.setOnClickListener(onMenuClickListener);
        iv_setting.setOnClickListener(onSettingClickListener);
        mSvAircraftLive.setOnTouchListener(surfaceOnTouchListener);
        btnCheckObstacle.setOnClickListener(onObstacleClickListener);
        btnGoHome.setOnClickListener(onGoHomeClickListener);
        btnHomeLocation.setOnClickListener(setHomeLocationListener);
        btn_auto_flight.setOnClickListener(v -> {
            List<String> locations = new ArrayList<>();
            locations.add("30.4758019985,114.4150239528");
            locations.add("30.4758489044,114.4155492583");
            locations.add("30.4758950341,114.4159361981");
            autoFlight(locations);
        });
        //地图控件
        mapTouch.setOnTouchListener(mapOnTouchListener);
        mapAircraftLive.setOnTouchListener(mapOnTouchListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveStopUAVPatrolHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUAVPatrolHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);//通知有人加入或离开
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCalibrationStateCallbackHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAirCraftStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
    }

    @Override
    public void initData(){
        pushService.initYuvPlayer();
        pushService.initAirCraftMediaStream();
        //        mIvAircraftLiveRetract.setVisibility(View.GONE);
        mLlAircraftLiveGroupCall.setVisibility(View.GONE);
        memberEnterAdapter = new MemberEnterAdapter(getApplicationContext(), memberEnterList);
        mLvAircraftLiveMemberInfo.setAdapter(memberEnterAdapter);
        initFocusView();
        initCameraSettingView();
        initAircraft();
        TerminalFactory.getSDK().getDataManager().setUavVoiceOpen(true);
        Map currentStateMap = MyTerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap();
        logger.info("当前状态机：" + currentStateMap);
        String type = getIntent().getStringExtra(Constants.TYPE);
        if(TextUtils.equals(type, Constants.RECEIVE_PUSH)){
            MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
            PromptManager.getInstance().stopRing();
            MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
        }else if(TextUtils.equals(type, Constants.ACTIVE_PUSH)){
            PushLiveMemberList list = (PushLiveMemberList) getIntent().getSerializableExtra(Constants.PUSH_MEMBERS);
            if(list != null && list.getList() != null){
                pushMemberList.clear();
                pushMemberList.addAll(list.getList());
            }
            if(MyTerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap().isEmpty()){
                requestStartLive();
            }else{
                logger.info("当前终端正在其他业务，不能发起视频上报");
                ToastUtils.showShort("当前终端正在其他业务，不能发起视频上报");
                finish();
            }
        }
        if(AirCraftUtil.checkIsAircraftConnected()){
            AirCraftUtil.loginToActivationIfNeeded();
        }
    }

    private void initAircraft(){
        initGoHomeView();
        initObstacle();
        initHomeLocationView();
        addListener();
        pushService.setVideoFeederListeners();
        initFlightControllerState();
    }

    @Override
    public void doOtherDestroy(){
        mapMinWebview.destroy();
        mapMinWebview.destroy();
        pushService.finishVideoLive();
        AdaptScreenUtils.closeAdapt(getResources());
        removeListener();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveStopUAVPatrolHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUAVPatrolHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);//通知有人加入或离开
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAirCraftStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapMinWebview.onResume();
        mapMaxWebview.onResume();
        ScreenSwitchUtils.init(this).setCurrentState(ScreenState.getInstanceByCode(getRequestedOrientation()));
        ScreenSwitchUtils.init(this).start(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        ScreenSwitchUtils.init(this).stop();
        mapMinWebview.onPause();
        mapMaxWebview.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_FILE_CODE && resultCode == RESULT_OK){
            int oritation = data.getIntExtra(Constants.ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            logger.info("onActivityResult:" + oritation);
            ScreenSwitchUtils.init(this).setCurrentState(ScreenState.getInstanceByCode(oritation));
            setRequestedOrientation(oritation);
        }
    }

    /**
     * 请求自己开始上报
     */
    private void requestStartLive(){
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        logger.error(TAG + "上报图像：requestCode=" + requestCode);
        if(requestCode != BaseCommonCode.SUCCESS_CODE){
            ToastUtil.livingFailToast(getApplicationContext(), requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
            myHandler.post(this::finish);
        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener(){
        if(getWaypointMissionOperator() != null){
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener(){
        if(getWaypointMissionOperator() != null){
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener(){
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent downloadEvent){
        }

        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent uploadEvent){
        }

        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent executionEvent){
            logger.info(TAG + "onExecutionUpdate--" + executionEvent);
        }

        @Override
        public void onExecutionStart(){
            logger.info(TAG + "--onExecutionStart");
            patrol = true;
            ToastUtil.showToast(getApplicationContext(), "开始巡航");
        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error){
            waypointList.clear();
            patrol = false;
            logger.error(TAG + "Execution finished: " + (error == null ? "Success!" : error.getDescription()));
            ToastUtil.showToast(getApplicationContext(), "Execution finished: " + (error == null ? "Success!" : error.getDescription()));
        }
    };

    private void initHomeLocationView(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null){
            flightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>(){
                @Override
                public void onSuccess(LocationCoordinate2D locationCoordinate2D){
                    logger.info(TAG + "获取返航位置成功：" + "--Latitude:" + locationCoordinate2D.getLatitude() + "--Longitude:" + locationCoordinate2D.getLongitude());
                    if(locationCoordinate2D.getLatitude() != 0.0 && !Double.isNaN(locationCoordinate2D.getLatitude()) && locationCoordinate2D.getLongitude() != 0.0 && !Double.isNaN(locationCoordinate2D.getLongitude())){
                        btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_true));
                    }else{
                        btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_false));
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                    logger.error(TAG + "获取返航位置失败：" + djiError);
                    btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_false));
                }
            });
        }else{
            logger.error(TAG + "获取返航位置失败：无人机连接异常--initHomeLocationView");
            btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_false));
        }
    }

    private void initFocusView(){
        imageView = new ImageView(getApplicationContext());
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.focus));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        imageView.setLayoutParams(params);
    }

    private void initGoHomeView(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null){
            if(flightController.getState().isGoingHome()){
                btnGoHome.setBackground(getResources().getDrawable(R.drawable.going_home));
            }else{
                btnGoHome.setBackground(getResources().getDrawable(R.drawable.not_go_home));
            }
        }else {
            logger.error(TAG +"无人机连接异常--initGoHomeView");
        }
    }

    private void initObstacle(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(null != flightController && null != flightController.getFlightAssistant()){
            flightController.getFlightAssistant().getCollisionAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>(){
                @Override
                public void onSuccess(Boolean aBoolean){
                    logger.info(TAG + "防碰撞是否开启：" + aBoolean);
                    if(aBoolean){
                        btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_open));
                    }else{
                        btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_close));
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                    logger.info(TAG + "获取防碰撞状态失败" + djiError);
                }
            });
        }else{
            ToastUtils.showShort("无人机连接异常");
            logger.error(TAG +"无人机连接异常--initObstacle");
        }
    }

    private void initFlightControllerState(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null){
            flightController.setStateCallback(flightControllerState -> {
                if(flightControllerState.isGoingHome()){
                    // TODO: 2019/4/1 返航的提示
                    logger.info(TAG + "正在返航");
                }else if(flightControllerState.isLandingConfirmationNeeded()){
                    // TODO: 2019/4/1 如果飞机和地面的间隙小于0.3米，则需要用户确认继续着陆
                    ToastUtil.showToast(getApplicationContext(), "请确认是否着陆");
                }
            });
        }else{
            logger.error(TAG +"无人机连接状态异常---initFlightControllerState");
        }
    }

    /**
     * 取消自动返航
     */
    private void cancelGoHome(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null){
            flightController.cancelGoHome(djiError -> {
                if(djiError == null){
                    myHandler.post(() -> btnGoHome.setBackground(getResources().getDrawable(R.drawable.not_go_home)));
                    logger.info(TAG + "取消自动返航成功");
                }else{
                    myHandler.post(() -> {
                        btnGoHome.setBackground(getResources().getDrawable(R.drawable.going_home));
                        ToastUtil.showToast(getApplicationContext(), "取消自动返航失败");
                    });
                    logger.error(TAG + "取消自动返航失败--" + djiError.getDescription());
                }
            });
        }else{
            logger.error(TAG +"无人机连接状态异常---cancelGoHome");
        }
    }

    /**
     * 自动返航
     */
    private void autoGoHome(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null){
            flightController.startGoHome(djiError -> {
                if(djiError == null){
                    myHandler.post(() -> btnGoHome.setBackground(getResources().getDrawable(R.drawable.going_home)));
                    logger.info(TAG + "自动返航成功");
                    ToastUtil.showToast(getApplicationContext(), "自动返航成功");
                }else{
                    myHandler.post(() -> {
                        btnGoHome.setBackground(getResources().getDrawable(R.drawable.not_go_home));
                        ToastUtil.showToast(getApplicationContext(), "自动返航失败");
                    });
                    logger.error(TAG + "自动返航失败--" + djiError.getDescription());
                    ToastUtil.showToast(getApplicationContext(), "自动返航失败");
                }
            });
        }else{
            logger.error(TAG +"无人机连接状态异常---autoGoHome");
        }
    }

    private void setHomeLocation(){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null){
            flightController.setHomeLocationUsingAircraftCurrentLocation(djiError -> {
                if(djiError == null){
                    logger.info(TAG + "返航位置设置成功");
                }else{
                    logger.error(TAG + "返航位置设置失败：" + djiError);
                }
            });
        }else{
            logger.error(TAG +"无人机连接状态异常---setHomeLocation");
        }
    }

    private void stopWaypointMission(){
        getWaypointMissionOperator().stopMission(error -> {
            logger.info(TAG + "Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            ToastUtil.showToast(getApplicationContext(), "Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            waypointList.clear();
        });
    }

    public void autoFlight(List<String> locations){
        setWayPoint(locations);
        configWayPointMission();
        uploadWayPointMission();
    }

    private void setWayPoint(List<String> locations){
        //先把无人机自己的位置设置到航点里
        String aircraftLocation = AirCraftUtil.getAircraftLocation();
        double aircraftLatitude = AirCraftUtil.getLatitude(aircraftLocation);
        double aircraftLongitude = AirCraftUtil.getLongitude(aircraftLocation);
        if(aircraftLatitude != 0.0 && aircraftLongitude != 0.0){
            Waypoint mWaypoint = new Waypoint(aircraftLatitude, aircraftLongitude, altitude);
            //Add Waypoints to Waypoint arraylist;
            addWaypoint(mWaypoint);
        }
        for(String location : locations){
            if(!TextUtils.isEmpty(location) && location.contains(",")){
                String[] split = location.split(",");
                double latitude = Double.parseDouble(split[0]);
                double longitude = Double.parseDouble(split[1]);
                if(AirCraftUtil.checkLatitude(latitude) && AirCraftUtil.checkLongitude(longitude)){
                    //每一个巡航点
                    Waypoint mWaypoint = new Waypoint(latitude, longitude, altitude);
                    //Add Waypoints to Waypoint arraylist;
                    addWaypoint(mWaypoint);
                }
            }
        }
    }

    private void addWaypoint(Waypoint mWaypoint){
        if(waypointMissionBuilder != null){
            waypointList.add(mWaypoint);
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        }else{
            waypointMissionBuilder = new WaypointMission.Builder();
            waypointList.add(mWaypoint);
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        }
    }

    private void uploadWayPointMission(){
        getWaypointMissionOperator().uploadMission(error -> {
            if(error == null){
                logger.info(TAG + "--Mission upload successfully!");
                ToastUtil.showToast(getApplicationContext(), TAG + "--Mission upload successfully!");
                startWaypointMission();
            }else{
                logger.info(TAG + "--Mission upload failed, error: " + error.getDescription() + " retrying...");
                getWaypointMissionOperator().retryUploadMission(null);
                waypointList.clear();
            }
        });
    }

    public WaypointMissionOperator getWaypointMissionOperator(){
        if(instance == null){
            if(DJISDKManager.getInstance().getMissionControl() != null){
                instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return instance;
    }

    private void startWaypointMission(){
        getWaypointMissionOperator().startMission(error -> {
            logger.info(TAG + "Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            //                ToastUtil.showToast(getApplicationContext(),"Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            //                setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            if(error != null){
                waypointList.clear();
                logger.info("开始巡航任务失败:" + error.getDescription());
            }
        });
    }

    private void configWayPointMission(){
        if(waypointMissionBuilder == null){
            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction).headingMode(mHeadingMode).autoFlightSpeed(mSpeed).maxFlightSpeed(mSpeed).flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        }else{
            waypointMissionBuilder.finishedAction(mFinishedAction).headingMode(mHeadingMode).autoFlightSpeed(mSpeed).maxFlightSpeed(mSpeed).flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        }
        logger.info(TAG + "Set Waypoint attitude successfully");
        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if(error == null){
            logger.info(TAG + "loadWaypoint succeeded");
        }else{
            ToastUtil.showToast(getApplicationContext(), error.getDescription());
            logger.error(TAG + "loadWaypoint failed " + error.getDescription());
        }
    }

    private View.OnClickListener onInviteMemberClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), InviteMemberService.class);
        intent.putExtra(Constants.TYPE, Constants.PUSH);
        intent.putExtra(Constants.PUSHING, true);
        //        intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO, MyDataUtil.getInviteMemberExceptList(watchMembers));
        startService(intent);
    };

    private View.OnClickListener onPreViewClickListener = v -> {
        Intent intent = new Intent(UavPushActivity.this, UavFileListActivity.class);
        intent.putExtra(Constants.ORIENTATION, getRequestedOrientation());
        startActivityForResult(intent, REQUEST_FILE_CODE);
    };

    private View.OnClickListener onVoiceButtonClickListener = v -> {
        if(TerminalFactory.getSDK().getDataManager().isUavVoiceOpen()){
            mBtnVoice.setBackground(getResources().getDrawable(R.drawable.uav_voice_close));
            TerminalFactory.getSDK().getDataManager().setUavVoiceOpen(false);
        }else{
            mBtnVoice.setBackground(getResources().getDrawable(R.drawable.uav_voice_open));
            TerminalFactory.getSDK().getDataManager().setUavVoiceOpen(true);
        }
    };

    private View.OnClickListener onTakePhotoClickListener = v -> {
        if(AirCraftUtil.getAircraftInstance() == null){
            ToastUtils.showShort(R.string.uav_disconnect);
            return;
        }
        AirCraftUtil.startShootPhoto();
    };

    private View.OnClickListener oStopButtonClickListener = v -> {
        pushService.finishVideoLive();
        finish();
    };

    private View.OnClickListener onCloseVoiceClickListener = v -> mLlCloseVoice.setVisibility(View.GONE);

    /**
     * 开启防碰撞
     *
     * @param open 是否开启
     */
    private void checkObstacle(boolean open){
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController != null && flightController.getFlightAssistant() != null){
            flightController.getFlightAssistant().setCollisionAvoidanceEnabled(open, djiError -> {
                if(null == djiError){
//                    ToastUtil.showToast(getApplicationContext(), "开启或关闭防碰撞成功:" + open);
                    logger.info(TAG + "开启或关闭防碰撞成功:" + open);
                }else{
//                    ToastUtil.showToast(getApplicationContext(), "开启或关闭防碰撞失败:--" + open + "-----" + djiError.getDescription());
                    logger.info(TAG + "开启或关闭防碰撞失败:--" + open + "-----" + djiError.getDescription());
                }
            });
        }else{
            logger.error(TAG +"无人机连接状态异常---checkObstacle");
        }
    }

    private void hideFocusView(){
        if(focusViewAdd){
            mAircraftRoot.removeView(imageView);
            focusViewAdd = false;
        }
    }

    private void showFocusView(MotionEvent event){
        if(!focusViewAdd){
            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(imageView.getLayoutParams());
            margin.leftMargin = (int) event.getX();
            margin.topMargin = (int) event.getY();
            RelativeLayout.LayoutParams focusLayoutParams = new RelativeLayout.LayoutParams(margin);
            focusLayoutParams.height = 100;//设置图片的高度
            focusLayoutParams.width = 100; //设置图片的宽度
            imageView.setLayoutParams(focusLayoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);//使图片充满控件大小
            mAircraftRoot.addView(imageView);
            focusViewAdd = true;
        }
    }

    private void initCameraSettingView(){
        cameraSettingAdvancedPanel.initKey();
        //使用CameraKey.MODE就变成了拍照模式
        CameraKey cameraKey = CameraKey.create(CameraKey.VIDEO_STANDARD);
        cameraSettingAdvancedPanel.updateWidget(cameraKey);
    }

    private boolean retract = false;
    private View.OnClickListener onRetractClickListener = v -> {
        retract = true;
        pushService.stopYuvPlayer();
        pushService.showView();
        finish();
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener surfaceOnTouchListener = (v, event) -> {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            checkIsAutoFocusMode(event);
        }
        return true;
    };

    private void checkIsAutoFocusMode(MotionEvent event){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(null == aircraft){
            return;
        }
        Camera camera = aircraft.getCamera();
        if(null == camera){
            return;
        }
        if(camera.isAdjustableFocalPointSupported()){
            camera.getFocusMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.FocusMode>(){
                @Override
                public void onSuccess(SettingsDefinitions.FocusMode focusMode){
                    if(focusMode == SettingsDefinitions.FocusMode.AUTO || focusMode == SettingsDefinitions.FocusMode.AFC){
                        myHandler.post(() -> {
                            if(focusViewAdd){
                                hideFocusView();
                                myHandler.removeCallbacks(hideFocusView);
                            }
                            showFocusView(event);
                            myHandler.postDelayed(hideFocusView, 2000);
                            setFocus(camera, event.getRawX() / ScreenUtils.getScreenWidth(),event.getRawY() / ScreenUtils.getScreenHeight());
                        });
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                }
            });
        }
    }

    @SuppressWarnings("unused")
    protected void handleMesage(Message msg){
    }

    /**
     * x和y的范围是0-1，屏幕左上角为[0.0,0.0]
     */
    private void setFocus(Camera camera, float x, float y){
        if(camera.isAdjustableFocalPointSupported()){
            camera.getFocusMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.FocusMode>(){
                @Override
                public void onSuccess(SettingsDefinitions.FocusMode focusMode){
                    if(focusMode == SettingsDefinitions.FocusMode.AUTO || focusMode == SettingsDefinitions.FocusMode.AFC){
                        setAutoFocusTarget(x, y, camera);
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                    setAutoFocusTarget(x, y, camera);
                    logger.info("获取对焦模式失败" + djiError.getDescription());
                }
            });
        }
    }

    private void setAutoFocusTarget(float x, float y, Camera camera){
        PointF pointF = new PointF(x, y);
        camera.setFocusTarget(pointF, djiError -> {
            if(djiError == null){
                logger.info(TAG + "设置焦点成功：" + "--x:" + x + "--y" + y);
            }else{
                logger.error(TAG + "设置焦点失败：" + djiError.getDescription());
            }
        });
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int surfaceWidth, int surfaceHeight){
            logger.info(TAG + "---onSurfaceTextureAvailable");
            //    private long count;
            Surface surface1 = new Surface(surface);
            pushService.initYuvPlayer();
            pushService.startPush(surfaceWidth, surfaceHeight, surface1);
            pushService.startRecord();
            textureAvailableTime = System.currentTimeMillis();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
            pushService.setSufaceWidthHeight(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
            logger.info(TAG + "---onSurfaceTextureDestroyed");
            if(!retract){
                pushService.stopYuvPlayer();
                textureAvailableTime = 0L;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    private ReceiveUAVPatrolHandler receiveUAVPatrolHandler = new ReceiveUAVPatrolHandler(){
        @Override
        public void handler(List<String> locations, float speed, float height){
            if(patrol){
                ToastUtil.showToast(getApplicationContext(), "正在巡航中，请稍后再试");
                return;
            }
            if(speed != 0){
                mSpeed = speed;
            }
            if(height != 0){
                altitude = height;
            }
            autoFlight(locations);
        }
    };

    private View.OnClickListener onMenuClickListener = v -> {
        if(AirCraftUtil.getAircraftInstance() == null){
            ToastUtils.showShort(R.string.uav_disconnect);
            return;
        }
        if(cameraSettingAdvancedPanel.getVisibility() == View.VISIBLE){
            cameraSettingAdvancedPanel.setVisibility(View.GONE);
            iv_menu.setImageResource(R.drawable.camera_menu_uncheck);
        }else{
            if(cameraSettingExposurePanel.getVisibility() == View.VISIBLE){
                cameraSettingExposurePanel.setVisibility(View.GONE);
                iv_menu.setImageResource(R.drawable.camera_menu_uncheck);
                iv_setting.setImageResource(R.drawable.camera_setting_uncheck1);
            }else{
                initCameraSettingView();
                cameraSettingAdvancedPanel.setVisibility(View.VISIBLE);
                iv_menu.setImageResource(R.drawable.camera_menu_checked);
            }
        }
    };

    private View.OnClickListener onSettingClickListener = v -> {
        if(AirCraftUtil.getAircraftInstance() == null){
            ToastUtils.showShort(R.string.uav_disconnect);
            return;
        }
        if(cameraSettingExposurePanel.getVisibility() == View.VISIBLE){
            cameraSettingExposurePanel.setVisibility(View.GONE);
            iv_setting.setImageResource(R.drawable.camera_setting_uncheck1);
        }else{
            if(cameraSettingAdvancedPanel.getVisibility() == View.VISIBLE){
                cameraSettingAdvancedPanel.setVisibility(View.GONE);
                iv_menu.setImageResource(R.drawable.camera_menu_uncheck1);
                iv_setting.setImageResource(R.drawable.camera_setting_uncheck1);
            }else{
                cameraSettingExposurePanel.setVisibility(View.VISIBLE);
                iv_setting.setImageResource(R.drawable.camera_setting_checked);
            }
        }
    };

    private View.OnClickListener onObstacleClickListener = v -> {
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController == null){
            ToastUtils.showShort(R.string.uav_disconnect);
            return;
        }
        if(null != flightController.getFlightAssistant()){
            flightController.getFlightAssistant().getCollisionAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>(){
                @Override
                public void onSuccess(Boolean aBoolean){
                    if(aBoolean){
                        btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_close));
                        ToastUtil.showToast(getApplicationContext(), "防碰撞关闭");
                    }else{
                        btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_open));
                        ToastUtil.showToast(getApplicationContext(), "防碰撞开启");
                    }
                    logger.info(TAG + "防碰撞是否开启：" + aBoolean);
                    checkObstacle(!aBoolean);
                }

                @Override
                public void onFailure(DJIError djiError){
                    logger.info(TAG +"获取防碰撞状态失败" + djiError);
                }
            });
        }else {
            logger.error(TAG +"flightController.getFlightAssistant()为null ！！！");
        }
    };

    private View.OnClickListener onGoHomeClickListener = v -> {
        FlightController flightController = AirCraftUtil.getFlightController();
        if(flightController == null){
            ToastUtils.showShort(R.string.uav_disconnect);
            return;
        }
        if(flightController.getState().isGoingHome()){
            cancelGoHome();
        }else{
            autoGoHome();
        }
    };

    private View.OnClickListener setHomeLocationListener = v -> {
        if(AirCraftUtil.getAircraftInstance() == null){
            ToastUtils.showShort(R.string.uav_disconnect);
            return;
        }
        setHomeLocation();
    };

    @SuppressLint("ClickableViewAccessibility")
    public View.OnTouchListener mapOnTouchListener = (v, event) -> {
        if(event.getAction() == MotionEvent.ACTION_UP){
            if(mapMaxRl.getVisibility() == View.VISIBLE || mapMinRl.getVisibility() == View.GONE){
                //地图缩小
                mapMaxWebview.clearCache(true);
                mapMaxWebview.loadUrl("about:blank");
                mapMaxWebview.setVisibility(View.GONE);
                mapMaxRl.setVisibility(View.GONE);
                mapAircraftLive.setVisibility(View.GONE);
                mAircraftRoot.setVisibility(View.VISIBLE);
                mapMinRl.setVisibility(View.VISIBLE);
                mapMinWebview.setVisibility(View.VISIBLE);
                mAircraftLive.removeView(mAircraftRoot);
                mAircraftLive.removeView(mapMaxRl);
                mapMinWebview.refreshMap(CustomWebView.MAP_MIN_TYPE);
                mAircraftLive.addView(mAircraftRoot);
            }else{
                //地图放大
                mapMinWebview.clearCache(true);
                mapMinWebview.loadUrl("about:blank");
                mapMinWebview.setVisibility(View.GONE);
                mapMinRl.setVisibility(View.GONE);
                mAircraftRoot.setVisibility(View.GONE);
                mapMaxRl.setVisibility(View.VISIBLE);
                mAircraftLive.removeView(mAircraftRoot);
                mAircraftLive.removeView(mapMaxRl);
                mAircraftLive.addView(mapMaxRl);
                mapMaxWebview.setVisibility(View.VISIBLE);
                mapAircraftLive.setVisibility(View.VISIBLE);
                mapMaxWebview.refreshMap(CustomWebView.MAP_MAX_TYPE);
            }
        }
        return true;
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> myHandler.post(() -> {
        ToastUtils.showShort(R.string.push_stoped);
        pushService.finishVideoLive();
        finish();
    });

    private ReceiveStopUAVPatrolHandler receiveStopUAVPatrolHandler = this::stopWaypointMission;

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler(){
        @Override
        public void handler(int reasonCode){
            logger.info(TAG + "收到ReceiveGroupCallCeasedIndicationHandler");
            myHandler.post(() -> mLlAircraftLiveGroupCall.setVisibility(View.GONE));
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler(){
        @Override
        public void handler(final int memberId, final String memberName, final int groupId, String version, CallMode currentCallMode, long uniqueNo){
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                ToastUtil.showToast(getApplicationContext(), "没有组呼听的权限");
            }
//            PromptManager.getInstance().groupCallCommingRing();
            logger.info(TAG + "组呼来了");
            myHandler.post(() -> {
                //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                mLlAircraftLiveGroupCall.setVisibility(View.VISIBLE);
                mTvAircraftLiveGroupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                mTvAircraftLiveSpeakingName.setText(memberName);
                mTvAircraftLiveSpeakingId.setText(HandleIdUtil.handleId(memberId));
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
            });
        }
    };

    /**
     * 观看成员的进入和退出
     **/
    @SuppressLint("SimpleDateFormat")
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = (memberName, memberId, joinOrExit) -> {
        logger.info(TAG + "receiveMemberJoinOrExitHandler" + memberName + ",memberId:" + memberId);
        myHandler.post(() -> {
            mLvAircraftLiveMemberInfo.setVisibility(View.VISIBLE);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            Date currentTime = new Date();
            String enterTime = formatter.format(currentTime);
            VideoMember videoMember = new VideoMember(memberId, memberName, enterTime, joinOrExit);
            if(joinOrExit){//进入直播间
                watchingMembers.add(videoMember);
            }else{//退出直播间
                int position = -1;
                for(int i = 0; i < watchingMembers.size(); i++){
                    if(watchingMembers.get(i).getId() == memberId){
                        position = i;
                    }
                }
                if(position != -1){
                    watchingMembers.remove(position);
                }
            }
            memberEnterList.add(videoMember);
            memberEnterAdapter.notifyDataSetChanged();
            if(memberEnterList.size() > 0){
                mLvAircraftLiveMemberInfo.smoothScrollToPosition(memberEnterList.size() - 1);
            }
        });
    };

    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> myHandler.postDelayed(() -> {
        logger.info(TAG + "自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        pushService.startAircraftPush(streamMediaServerIp, String.valueOf(streamMediaServerPort), TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "_" + callId);
    }, 1000);

    private ReceiveAirCraftStatusChangedHandler receiveAirCraftStatusChangedHandler = new ReceiveAirCraftStatusChangedHandler(){
        @Override
        public void handler(boolean connected){
            myHandler.post(() -> {
                if(!connected){
                    uavConnected = false;
                    setUavDisconnectParams();
                    mVDrakBackgroupd.setText(R.string.uav_disconnect);
                    mVDrakBackgroupd.setVisibility(View.VISIBLE);
                    //                    mBtnStopPush.setVisibility(View.VISIBLE);
                    mIvTakePhoto.setImageResource(R.drawable.uav_take_photo_disable);
                }else{
                    AirCraftUtil.loginToActivationIfNeeded();
                    initAircraft();
                    uavConnected = true;
                    if(TerminalFactory.getSDK().getAuthManagerTwo().isOnLine()){
                        logger.info("GONE---ReceiveAirCraftStatusChangedHandler");
                        mVDrakBackgroupd.setVisibility(View.GONE);
                    }else {
                        mVDrakBackgroupd.setVisibility(View.VISIBLE);
                    }
                    //                    mBtnStopPush.setVisibility(View.GONE);
                    mIvTakePhoto.setImageResource(R.drawable.uav_take_photo);
                }
            });
        }
    };

    private void onNetworkChanged(boolean connected){
        //        if(!connected){
        //            pushService.finishVideoLive();
        //            finish();
        //        }
    }

    private ReceiveResponseMyselfLiveHandler receiveResponseMyselfLiveHandler = (resultCode, resultDesc) -> {
        if(resultCode != BaseCommonCode.SUCCESS_CODE){
            ToastUtil.showToast(getApplicationContext(), resultDesc);
            pushService.finishVideoLive();
            finish();
        }else{
            if(pushMemberList != null && !pushMemberList.isEmpty()){
                logger.info("自己发起直播成功,要推送的列表：" + pushMemberList);
                MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(pushMemberList, MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0), TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0l));
            }
        }
    };

    private CompassCalibrationState lastState;
    private ReceiveCalibrationStateCallbackHandler receiveCalibrationStateCallbackHandler = this::showDialog;

    private boolean checkShowDialog(CompassCalibrationState lastState, CompassCalibrationState currentState){
        return lastState != currentState;
    }

    private void showDialog(CompassCalibrationState compassCalibrationState){
        if(compassCalibrationState == CompassCalibrationState.HORIZONTAL){
            if(checkShowDialog(lastState, compassCalibrationState)){
                //指南针水平校准。用户应水平握住飞机并将其旋转360度。
                myHandler.post(() -> DialogUtils.showDialog(UavPushActivity.this, "水平握住飞机并将其旋转360度"));
            }
        }else if(compassCalibrationState == CompassCalibrationState.VERTICAL){
            if(checkShowDialog(lastState, compassCalibrationState)){
                //指南针垂直校准。使用者应垂直握住飞机，使机头指向地面，并将飞机旋转360度。
                myHandler.post(() -> DialogUtils.showDialog(UavPushActivity.this, "垂直握住飞机，使机头指向地面，并将飞机旋转360度"));
            }
        }else if(compassCalibrationState == CompassCalibrationState.SUCCESSFUL){
            if(checkShowDialog(lastState, compassCalibrationState)){
                //指南针校准成功
                myHandler.post(() -> DialogUtils.showDialog(UavPushActivity.this, "指南针校准成功"));
            }
        }else if(compassCalibrationState == CompassCalibrationState.FAILED){
            if(checkShowDialog(lastState, compassCalibrationState)){
                //指南针校准失败。确保指南针附近没有磁铁或金属物体，然后重试
                myHandler.post(() -> DialogUtils.showDialog(UavPushActivity.this, "指南针校准失败。确保指南针附近没有磁铁或金属物体，然后重试"));
            }
        }else if(compassCalibrationState == CompassCalibrationState.NOT_CALIBRATING){
            if(checkShowDialog(lastState, compassCalibrationState)){
                //正常状态。指南针不在校准中
                myHandler.post(() -> DialogUtils.showDialog(UavPushActivity.this, "正常状态。指南针不在校准中"));
            }
        }else if(compassCalibrationState == CompassCalibrationState.UNKNOWN){
            if(checkShowDialog(lastState, compassCalibrationState)){
                //指南针校准状态未知
                myHandler.post(() -> DialogUtils.showDialog(UavPushActivity.this, "指南针校准状态未知"));
            }
        }
        lastState = compassCalibrationState;
    }

    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler(){
        @Override
        public void handler(boolean connected){
            myHandler.post(()->{
                mVDrakBackgroupd.setVisibility(View.VISIBLE);
                if(uavConnected){
                    setUavConnectedParams();
                    if(!connected){
                        //无人机连上，网络断开
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.net_work_disconnect));
                        mVDrakBackgroupd.setText(R.string.net_work_disconnect);
                    }else {
                        if(TerminalFactory.getSDK().isServerConnected()){
                            logger.info("GONE---ReceiveOnLineStatusChangedHandler");
                            mVDrakBackgroupd.setVisibility(View.GONE);
                        }
                    }
                }else {
                    setUavDisconnectParams();
                    if(!connected){
                        //无人机没连上，接入也没连上
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.net_work_disconnect));
                        mVDrakBackgroupd.setText(R.string.net_work_disconnect);
                    }else {
                        mVDrakBackgroupd.setText(R.string.uav_disconnect);
                    }
                }
                onNetworkChanged(connected);
            });
        }
    };

    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler(){
        @Override
        public void handler(int resultCode, String resultDesc, boolean isRegisted){
            myHandler.post(()->{
                mVDrakBackgroupd.setVisibility(View.VISIBLE);
                if(uavConnected){
                    setUavConnectedParams();
                }else {
                    setUavDisconnectParams();
                }
                if(resultCode == BaseCommonCode.SUCCESS_CODE){
                    mVDrakBackgroupd.setText(R.string.connecting_server);
                }else {
                    mVDrakBackgroupd.setText(resultDesc);
                }
            });
        }
    };

    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler(){
        @Override
        public void handler(boolean connected){
            myHandler.post(()->{
                mVDrakBackgroupd.setVisibility(View.VISIBLE);
                if(uavConnected){
                    //无人机连上，接入也连上，设为登陆
                    setUavConnectedParams();
                    if(connected){
                        mVDrakBackgroupd.setText(R.string.logining);
                    }else {
                        //无人机连上，接入断开
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.net_work_disconnect));
                        mVDrakBackgroupd.setText(R.string.net_work_disconnect);
                    }
                }else {
                    //无人机没连上，接入连上，设为登陆
                    setUavDisconnectParams();
                    if(connected){
                        mVDrakBackgroupd.setText(R.string.logining);
                    }else {
                        //无人机没连上，接入也没连上
                        ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.net_work_disconnect));
                        mVDrakBackgroupd.setText(R.string.net_work_disconnect);
                    }
                }
                onNetworkChanged(connected);
            });
        }
    };

    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> myHandler.post(()->{

        if(uavConnected){
            setUavConnectedParams();
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                //登陆成功
                mVDrakBackgroupd.setText(R.string.login_success);
                logger.info("GONE---ReceiveLoginResponseHandler");
                mVDrakBackgroupd.setVisibility(View.GONE);
            }else {
                mVDrakBackgroupd.setText(resultDesc);
                mVDrakBackgroupd.setVisibility(View.VISIBLE);
            }
        }else {
            setUavDisconnectParams();
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                mVDrakBackgroupd.setText(R.string.uav_disconnect);
                mVDrakBackgroupd.setVisibility(View.VISIBLE);
            }else {
                mVDrakBackgroupd.setText(resultDesc);
                mVDrakBackgroupd.setVisibility(View.VISIBLE);
            }
        }
    });

    private void setUavDisconnectParams(){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVDrakBackgroupd.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mVDrakBackgroupd.setLayoutParams(layoutParams);
        mVDrakBackgroupd.setGravity(Gravity.CENTER);
        mVDrakBackgroupd.setBackgroundResource(R.color.black);
    }

    private void setUavConnectedParams(){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVDrakBackgroupd.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mVDrakBackgroupd.setLayoutParams(layoutParams);
        mVDrakBackgroupd.setGravity(Gravity.CENTER);
        mVDrakBackgroupd.setBackgroundResource(R.color.red_40);
    }
}
