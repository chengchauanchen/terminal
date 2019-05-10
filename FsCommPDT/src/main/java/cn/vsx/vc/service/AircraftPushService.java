package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.easydarwin.push.AirCraftMediaStream;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveStopUAVPatrolHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUAVPatrolHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MemberEnterAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.AirCraftUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.CustomWebView;
import cn.vsx.vc.view.MyCameraSettingAdvancedPanel;
import cn.vsx.yuv.YuvPlayer;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
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
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.ux.panel.CameraSettingExposurePanel;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 无人机上报图像
 */
public class AircraftPushService extends BaseService implements YuvPlayer.YuvDataListener{

    //    private RelativeLayout mRlAircraftParentView;
    private TextureView mSvAircraftLive;
    private LinearLayout mLlAircraftLiveGroupCall;
    private TextView mTvAircraftLiveSpeakingName;
    private TextView mTvAircraftLiveGroupName;
    private TextView mTvAircraftLiveSpeakingId;
    private ImageView mIvAircraftLiveRetract;
    private ListView mLvAircraftLiveMemberInfo;
    private LinearLayout mLlAircraftLiveInviteMember;
    private ImageView mIvLiveAddmember;
    private RelativeLayout mAircraftLive;
    private TextureView mSvLivePop;
    private AirCraftMediaStream airCraftMediaStream;

    private RelativeLayout mAircraftRoot;
    private int pushcount;
    private RelativeLayout mPopupMiniLive;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;
    private MemberEnterAdapter memberEnterAdapter;
    private ArrayList<VideoMember> watchingMembers = new ArrayList<>();
    private List<VideoMember> memberEnterList = new ArrayList<>();
    private long lastupdate;
    private String ip;
    private String port;
    private String id;
    private YuvPlayer yuvPlayer;
    private boolean focusViewAdd;
    private ImageView imageView;
    private ImageView iv_menu;
    private long textureAvailableTime;
    private Runnable hideFocusView = this::hideFocusView;
    private ImageView iv_setting;
    private Button btnCheckObstacle;
    private Button btnGoHome;
    private MyCameraSettingAdvancedPanel cameraSettingAdvancedPanel;
    private CameraSettingExposurePanel cameraSettingExposurePanel;

    //地图控件
    private RelativeLayout mapMinRl;
    private CustomWebView mapMinWebview;
    private View mapTouch;

    private RelativeLayout mapMaxRl;
    private CustomWebView mapMaxWebview;
    private TextureView mapAircraftLive;
    private Button btnHomeLocation;
    private Button btn_auto_flight;

    //所有的巡航点
    private List<Waypoint> waypointList = new ArrayList<>();

    //默认飞行高度和速度
    private float altitude = 35.0f;
    private float mSpeed = 2.0f;

    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private WaypointMissionOperator instance;
    //巡航结束之后的动作
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    //飞机的朝向
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    private static String TAG = "AircraftPushService2---";
    private boolean patrol;
    /**
     * 在线状态
     */
    private ReceiveServerConnectionEstablishedHandler receiveOnLineStatusChangedHandler = connected -> {
        if(!connected){
            ToastUtil.showToast(getApplicationContext(), getResources().getString(R.string.net_work_disconnect));
        }
        mHandler.post(() -> onNetworkChanged(connected));
    };

    private ReceiveAirCraftStatusChangedHandler receiveAirCraftStatusChangedHandler = connected -> {
        if(!connected){
            mHandler.post(this::finishVideoLive);
        }
    };

    private ReceiveResponseMyselfLiveHandler receiveResponseMyselfLiveHandler = (resultCode, resultDesc) -> {
        if(resultCode != BaseCommonCode.SUCCESS_CODE){
            ToastUtil.showToast(getApplicationContext(),resultDesc);
            stopBusiness();
        }
    };

    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> mHandler.postDelayed(() -> {
        logger.info(TAG+"自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        ip = streamMediaServerIp;
        port = streamMediaServerPort + "";
        id = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "_" + callId;
        startAircraftPush();
    }, 1000);

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler(){
        @Override
        public void handler(final int memberId, final String memberName, final int groupId, String version, CallMode currentCallMode){
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                ToastUtil.showToast(getApplicationContext(), "没有组呼听的权限");
            }
            PromptManager.getInstance().groupCallCommingRing();
            logger.info(TAG+"组呼来了");
            mHandler.post(() -> {
                //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                mLlAircraftLiveGroupCall.setVisibility(View.VISIBLE);
                mTvAircraftLiveGroupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                mTvAircraftLiveSpeakingName.setText(memberName);
                mTvAircraftLiveSpeakingId.setText(HandleIdUtil.handleId(memberId));
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
            });
        }
    };

    private ReceiveStopUAVPatrolHandler receiveStopUAVPatrolHandler = new ReceiveStopUAVPatrolHandler(){
        @Override
        public void handler(){
            stopWaypointMission();
        }
    };

    private ReceiveUAVPatrolHandler receiveUAVPatrolHandler = new ReceiveUAVPatrolHandler(){
        @Override
        public void handler(List<String> locations,float speed,float height){
            if(patrol){
                ToastUtil.showToast(getApplicationContext(),"正在巡航中，请稍后再试");
                return;
            }
            if(speed !=0){
                mSpeed = speed;
            }
            if(height !=0){
                altitude = height;
            }
            autoFlight(locations);
        }
    };

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler(){
        @Override
        public void handler(int reasonCode){
            logger.info(TAG+"收到ReceiveGroupCallCeasedIndicationHandler");
            mHandler.post(() -> mLlAircraftLiveGroupCall.setVisibility(View.GONE));
        }
    };

    /**
     * 观看成员的进入和退出
     **/
    @SuppressLint("SimpleDateFormat")
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = (memberName, memberId, joinOrExit) -> {
        logger.info(TAG+"receiveMemberJoinOrExitHandler" + memberName + ",memberId:" + memberId);
        mHandler.post(() -> {
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


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void initWakeLock(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(null != powerManager){
            //noinspection deprecation
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeLock");
        }
    }

    private View.OnClickListener onInviteMemberClickListener = v -> {
        Intent intent = new Intent(getApplicationContext(), InviteMemberService.class);
        intent.putExtra(Constants.TYPE, Constants.PUSH);
        intent.putExtra(Constants.PUSHING, true);
//        intent.putExtra(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO, MyDataUtil.getInviteMemberExceptList(watchMembers));
        startService(intent);
    };

    private View.OnClickListener onMenuClickListener = v -> {
        if(cameraSettingAdvancedPanel.getVisibility() == View.VISIBLE){
            cameraSettingAdvancedPanel.setVisibility(View.GONE);
            iv_menu.setImageResource(R.drawable.camera_menu_uncheck);
        }else{
            if(cameraSettingExposurePanel.getVisibility() == View.VISIBLE){
                cameraSettingExposurePanel.setVisibility(View.GONE);
                iv_menu.setImageResource(R.drawable.camera_menu_uncheck);
                iv_setting.setImageResource(R.drawable.camera_setting_uncheck);
            }else{
                initCameraSettingView();
                cameraSettingAdvancedPanel.setVisibility(View.VISIBLE);
                iv_menu.setImageResource(R.drawable.camera_menu_checked);
            }
        }
    };

    private View.OnClickListener setHomeLocationListener =  v ->{
        setHomeLocation();
    };

    private View.OnClickListener onGoHomeClickListener = v -> {
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null && null != aircraft.getFlightController()){
            if(aircraft.getFlightController().getState().isGoingHome()){
                cancelGoHome();
            }else{
                autoGoHome();
            }
        }
    };

    private View.OnClickListener onObstacleClickListener = v -> {
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft != null && null != aircraft.getFlightController() && null != aircraft.getFlightController().getFlightAssistant()){
            aircraft.getFlightController().getFlightAssistant().getCollisionAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>(){
                @Override
                public void onSuccess(Boolean aBoolean){
                    if(aBoolean){
                        btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_close));
                        ToastUtil.showToast(getApplicationContext(), "防碰撞关闭");
                    }else{
                        btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_open));
                        ToastUtil.showToast(getApplicationContext(), "防碰撞开启");
                    }
                    logger.info(TAG+"防碰撞是否开启：" + aBoolean);
                    checkObstacle(!aBoolean);
                }

                @Override
                public void onFailure(DJIError djiError){
                    logger.info("获取防碰撞状态失败" + djiError);
                }
            });
        }
    };

    private View.OnClickListener onSettingClickListener = v -> {
        if(cameraSettingExposurePanel.getVisibility() == View.VISIBLE){
            cameraSettingExposurePanel.setVisibility(View.GONE);
            iv_setting.setImageResource(R.drawable.camera_setting_uncheck);
        }else{
            if(cameraSettingAdvancedPanel.getVisibility() == View.VISIBLE){
                cameraSettingAdvancedPanel.setVisibility(View.GONE);
                iv_menu.setImageResource(R.drawable.camera_menu_uncheck);
                iv_setting.setImageResource(R.drawable.camera_setting_uncheck);
            }else{
                cameraSettingExposurePanel.setVisibility(View.VISIBLE);
                iv_setting.setImageResource(R.drawable.camera_setting_checked);
            }
        }
    };

    private View.OnClickListener onRetractClickListener = v -> {
        //        if(yuvPlayer.isPlaying()){
        //            yuvPlayer.stop();
        //        }

    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener surfaceOnTouchListener = (v, event) -> {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                checkIsAutoFocusMode(event);
                break;
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
                        mHandler.post(() -> {
                            if(focusViewAdd){
                                hideFocusView();
                                mHandler.removeCallbacks(hideFocusView);
                            }
                            showFocusView(event);
                            mHandler.postDelayed(hideFocusView, 2000);
                            //因为是横屏，所以用RawY()/screenWidth
                            setFocus(camera, event.getRawY() / screenWidth, event.getRawX() / screenHeight);
                        });
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                }
            });
        }
    }

    private void initFocusView(){
        imageView = new ImageView(getApplicationContext());
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.focus));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        imageView.setLayoutParams(params);
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

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener miniPopOnTouchListener = (v, event) -> {
        //触摸点到边界屏幕的距离
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触摸点到自身边界的距离
                downX = event.getX();
                downY = event.getY();
                oddOffsetX = layoutParams.x;
                oddOffsetY = layoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //不除以3，拖动的view抖动的有点厉害
                if(Math.abs(downX - moveX) > 5 || Math.abs(downY - moveY) > 5){
                    // 更新浮动窗口位置参数
                    layoutParams.x = (int) (screenWidth - (x + downX));
                    layoutParams.y = (int) (y - downY);
                    windowManager.updateViewLayout(rootView, layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                int newOffsetX = layoutParams.x;
                int newOffsetY = layoutParams.y;
                if(Math.abs(newOffsetX - oddOffsetX) <= 30 && Math.abs(newOffsetY - oddOffsetY) <= 30){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
                    if(System.currentTimeMillis() - textureAvailableTime < 3000){
                        return true;
                    }else{
                        windowManager.removeView(rootView);
                        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                        windowManager.addView(rootView, layoutParams1);
                        mAircraftLive.setVisibility(View.VISIBLE);
                        mPopupMiniLive.setVisibility(View.GONE);
                        MyApplication.instance.isMiniLive = false;
                        setScreenWidth();
                    }
                }
                break;
        }
        return true;
    };

    @SuppressLint("ClickableViewAccessibility")
    public View.OnTouchListener mapOnTouchListener = (v, event) -> {
        switch(event.getAction()){
            case MotionEvent.ACTION_UP:
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
                break;
        }
        return true;
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int surfaceWidth, int surfaceHeight){
            logger.info(TAG+"onSurfaceTextureAvailable");
            //    private long count;
            Surface surface1 = new Surface(surface);
            initYuvPlayer();
            yuvPlayer.start(surfaceWidth, surfaceHeight, 16, surface1);
            if(!airCraftMediaStream.isStreaming()){
                airCraftMediaStream.startPreView(1280, 720);
            }
            textureAvailableTime = System.currentTimeMillis();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
            if(null != yuvPlayer){
                yuvPlayer.setSufaceWidthHeight(width, height);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
            logger.info(TAG+"onSurfaceTextureDestroyed");
            textureAvailableTime = 0L;
            stopYuvPlayer();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    private void stopYuvPlayer(){
        if(yuvPlayer != null){
            yuvPlayer.stop();
            yuvPlayer.setYuvDataListener(null);
            yuvPlayer = null;
        }
    }

    //无人机推送
    private VideoFeeder.VideoDataListener mReceivedVideoDataListener = new VideoFeeder.VideoDataListener(){
        @Override
        public void onReceive(byte[] videoBuffer, int size){
            if(System.currentTimeMillis() - lastupdate > 1000){
                //                logger.info(TAG+"camera recv video data size:" + size);
                lastupdate = System.currentTimeMillis();
            }
            if(null != yuvPlayer){
                yuvPlayer.parseH264(videoBuffer, size);
            }
        }
    };

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_aircraft_push_view, null);
    }

    @Override
    protected void findView(){
        mAircraftLive = rootView.findViewById(R.id.aircraft_live);
        mPopupMiniLive = rootView.findViewById(R.id.popup_mini_live);
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
        //        mRlAircraftParentView = rootView.findViewById(R.id.rl_aircraft_parent_view);
        mSvAircraftLive = rootView.findViewById(R.id.sv_aircraft_live);
        mLlAircraftLiveGroupCall = rootView.findViewById(R.id.ll_aircraft_live_group_call);
        mTvAircraftLiveSpeakingName = rootView.findViewById(R.id.tv_aircraft_live_speakingName);
        mTvAircraftLiveGroupName = rootView.findViewById(R.id.tv_aircraft_live_groupName);
        mTvAircraftLiveSpeakingId = rootView.findViewById(R.id.tv_aircraft_live_speakingId);
        mIvAircraftLiveRetract = rootView.findViewById(R.id.iv_aircraft_live_retract);
        mLvAircraftLiveMemberInfo = rootView.findViewById(R.id.lv_aircraft_live_member_info);
        mLlAircraftLiveInviteMember = rootView.findViewById(R.id.ll_aircraft_live_invite_member);
        mIvLiveAddmember = rootView.findViewById(R.id.iv_live_addmember);
        iv_menu = rootView.findViewById(R.id.iv_menu);
        iv_setting = rootView.findViewById(R.id.iv_setting);
        cameraSettingAdvancedPanel = rootView.findViewById(R.id.cameraSettingAdvancedPanel);
        cameraSettingExposurePanel = rootView.findViewById(R.id.cameraSettingExposurePanel);
        mAircraftRoot = rootView.findViewById(R.id.aircraft_root);
        btnCheckObstacle = rootView.findViewById(R.id.btn_check_obstacle);
        btnGoHome = rootView.findViewById(R.id.btn_go_home);
        //地图控件
        mapMinRl = rootView.findViewById(R.id.map_min_rl);
        mapMinWebview = rootView.findViewById(R.id.map_min_webview);
        mapTouch = rootView.findViewById(R.id.map_touch);
        mapMaxRl = rootView.findViewById(R.id.map_max_rl);
        mapMaxWebview = rootView.findViewById(R.id.map_max_webview);
        mapAircraftLive = rootView.findViewById(R.id.map_aircraft_live);
        btnHomeLocation = rootView.findViewById(R.id.btn_home_location);
        btn_auto_flight = rootView.findViewById(R.id.btn_auto_flight);
    }

    @Override
    protected void initData(){
        initYuvPlayer();
        mIvAircraftLiveRetract.setVisibility(View.GONE);
        mLlAircraftLiveGroupCall.setVisibility(View.GONE);
        memberEnterAdapter = new MemberEnterAdapter(getApplicationContext(), memberEnterList);
        mLvAircraftLiveMemberInfo.setAdapter(memberEnterAdapter);
        //        initUavResolution();
        initAirCraftMediaStream();
        initFocusView();
        initCameraSettingView();
        initObstacle();
        initGoHomeView();
        initHomeLocationView();
        addListener();
    }

    private void initHomeLocationView(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            aircraft.getFlightController().getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>(){
                @Override
                public void onSuccess(LocationCoordinate2D locationCoordinate2D){
                    logger.info(TAG+"获取返航位置成功："+"--Latitude:"+locationCoordinate2D.getLatitude()+"--Longitude:"+locationCoordinate2D.getLongitude());
                    if(locationCoordinate2D.getLatitude() != 0.0 && !Double.isNaN(locationCoordinate2D.getLatitude()) &&
                            locationCoordinate2D.getLongitude() !=0.0 && !Double.isNaN(locationCoordinate2D.getLongitude())){
                        btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_true));
                    }else {
                        btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_false));
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                    logger.error(TAG+"获取返航位置失败："+djiError);
                    btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_false));

                }
            });
        }else {
            logger.error(TAG+"获取返航位置失败：无人机连接异常");
            btnHomeLocation.setBackground(getResources().getDrawable(R.drawable.home_location_false));
        }
    }

    private void initGoHomeView(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            if(aircraft.getFlightController().getState().isGoingHome()){
                btnGoHome.setBackground(getResources().getDrawable(R.drawable.going_home));
            }else{
                btnGoHome.setBackground(getResources().getDrawable(R.drawable.not_go_home));
            }
        }
    }

    private void initObstacle(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft!=null){
            if(null != aircraft.getFlightController() && null !=aircraft.getFlightController().getFlightAssistant()){
                aircraft.getFlightController().getFlightAssistant().getCollisionAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>(){
                    @Override
                    public void onSuccess(Boolean aBoolean){
                        logger.info(TAG+"防碰撞是否开启：" + aBoolean);
                        if(aBoolean){
                            btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_open));
                        }else{
                            btnCheckObstacle.setBackground(getResources().getDrawable(R.drawable.check_obstacle_close));
                        }
                    }

                    @Override
                    public void onFailure(DJIError djiError){
                        logger.info(TAG+"获取防碰撞状态失败" + djiError);
                    }
                });
            }
        }
    }

    private void initYuvPlayer(){
        if(null == yuvPlayer){
            yuvPlayer = new YuvPlayer(getApplicationContext());
            yuvPlayer.setYuvDataListener(this);
        }
    }

    private void initCameraSettingView(){
        cameraSettingAdvancedPanel.initKey();
        //使用CameraKey.MODE就变成了拍照模式
        CameraKey cameraKey = CameraKey.create(CameraKey.VIDEO_STANDARD);
        logger.info(TAG+"cameraKey:" + cameraKey);
        cameraSettingAdvancedPanel.updateWidget(cameraKey);
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        mLlAircraftLiveInviteMember.setOnClickListener(onInviteMemberClickListener);
        mIvAircraftLiveRetract.setOnClickListener(onRetractClickListener);
        mSvAircraftLive.setSurfaceTextureListener(surfaceTextureListener);
        mSvLivePop.setSurfaceTextureListener(surfaceTextureListener);
        mapAircraftLive.setSurfaceTextureListener(surfaceTextureListener);
        mPopupMiniLive.setOnTouchListener(miniPopOnTouchListener);
        iv_menu.setOnClickListener(onMenuClickListener);
        iv_setting.setOnClickListener(onSettingClickListener);
        setVideoFeederListeners();
        mSvAircraftLive.setOnTouchListener(surfaceOnTouchListener);
        btnCheckObstacle.setOnClickListener(onObstacleClickListener);
        btnGoHome.setOnClickListener(onGoHomeClickListener);
        btnHomeLocation.setOnClickListener(setHomeLocationListener);
        btn_auto_flight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                List<String> locations = new ArrayList<>();

                locations.add("30.4758019985,114.4150239528");
                locations.add("30.4758489044,114.4155492583");
                locations.add("30.4758950341,114.4159361981");
                autoFlight(locations);
            }
        });
        //地图控件
        mapTouch.setOnTouchListener(mapOnTouchListener);
        mapAircraftLive.setOnTouchListener(mapOnTouchListener);
        initFlightControllerState();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveStopUAVPatrolHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUAVPatrolHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);//通知有人加入或离开
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAirCraftStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
    }
    private void initFlightControllerState(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if (aircraft !=null) {
            mFlightController = aircraft.getFlightController();

            if (mFlightController != null) {
                mFlightController.setStateCallback(new FlightControllerState.Callback() {

                    @Override
                    public void onUpdate(FlightControllerState flightControllerState) {
                        if(flightControllerState.isGoingHome()){
                            // TODO: 2019/4/1 返航的提示
                            logger.info(TAG+"正在返航");
                        }else if(flightControllerState.isLandingConfirmationNeeded()){
                            // TODO: 2019/4/1 如果飞机和地面的间隙小于0.3米，则需要用户确认继续着陆
                            ToastUtil.showToast(getApplicationContext(),"请确认是否着陆");
                        }
                    }
                });
            }
        }

    }

    @Override
    protected void initWindow(){
        super.initWindow();
        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    protected void onNetworkChanged(boolean connected){
        if(!connected){
            finishVideoLive();
        }
    }

    @Override
    protected void initView(Intent intent){
        mAircraftLive.setVisibility(View.VISIBLE);
        mPopupMiniLive.setVisibility(View.GONE);
        if(MyTerminalFactory.getSDK().getTerminalStateManager().getCurrentStateMap().isEmpty()){
            layoutParams1.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
            requestStartLive();
        }else{
            removeView();
        }
    }

    @Override
    protected void showPopMiniView(){
    }

    private void setVideoFeederListeners(){
        VideoFeeder instance = VideoFeeder.getInstance();
        logger.info(TAG+"VideoFeeder:" + instance);
        ToastUtil.showToast(getApplicationContext(), "VideoFeeder:" + instance);
        if(VideoFeeder.getInstance() == null){
            return;
        }
        BaseProduct product = AirCraftUtil.getProductInstance();

        if(product != null){
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            logger.info(TAG+"product:" + product);
        }else {
            logger.error("product为null！！！");
        }
    }

    /**
     * 请求自己开始上报
     */
    private void requestStartLive(){
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        logger.error(TAG+"上报图像：requestCode=" + requestCode);
        if(requestCode != BaseCommonCode.SUCCESS_CODE){
            ToastUtil.livingFailToast(getApplicationContext(), requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
            mHandler.post(this::finishVideoLive);
        }
    }

    private void finishVideoLive(){
        stopAircraftPush();
        removeView();
    }

    private void startAircraftPush(){
        if(null != airCraftMediaStream){
            airCraftMediaStream.startStream(ip, port, id, pushCallback);
            String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
            logger.info(TAG+"推送地址：" + url);
        }
    }

    private InitCallback pushCallback = new InitCallback(){

        @Override
        public void onCallback(int code){
            Bundle resultData = new Bundle();
            switch(code){
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                    resultData.putString("event-msg", "EasyRTSP 无效Key");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                    resultData.putString("event-msg", "EasyRTSP 激活成功");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                    resultData.putString("event-msg", "EasyRTSP 连接中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 连接成功");
                    pushcount = 0;
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    resultData.putString("event-msg", "EasyRTSP 连接失败");
                    if(pushcount <= 10){
                        pushcount++;
                    }else{
                        mHandler.post(() -> finishVideoLive());
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");
                    if(pushcount <= 10){
                        pushcount++;
                    }
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                    resultData.putString("event-msg", "EasyRTSP 推流中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                    resultData.putString("event-msg", "EasyRTSP 断开连接");

                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                    resultData.putString("event-msg", "EasyRTSP 平台不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 断授权使用商不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                    resultData.putString("event-msg", "EasyRTSP 进程名称长度不匹配");
                    break;
                default:
                    break;
            }
        }
    };

    private void initAirCraftMediaStream(){
        if(null == airCraftMediaStream){
            airCraftMediaStream = new AirCraftMediaStream(getApplicationContext());
        }
    }

    private void stopAircraftPush(){
        logger.info(TAG+"stopAircraftPush");
        if(null != airCraftMediaStream && airCraftMediaStream.isStreaming()){
            logger.info(TAG+"结束无人机推流");
            airCraftMediaStream.stopStream();
        }
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        removeListener();
        if(DJISDKManager.getInstance().getProduct() != null){
            VideoFeeder.getInstance().getPrimaryVideoFeed().removeVideoDataListener(mReceivedVideoDataListener);
        }
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveStopUAVPatrolHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUAVPatrolHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);//通知有人加入或离开
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAirCraftStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
    }


    @Override
    public void onDataRecv(byte[] data, int width, int height){
        airCraftMediaStream.push(data, width, height);
    }

    /**
     * x和y的范围是0-1，屏幕左上角为[0.0,0.0]
     */
    private void setFocus(Camera camera, float x, float y){
        if(camera.isAdjustableFocalPointSupported()){
            camera.getFocusMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.FocusMode>(){
                @Override
                public void onSuccess(SettingsDefinitions.FocusMode focusMode){
                    if(focusMode == SettingsDefinitions.FocusMode.AUTO){
                        setAutoFocusTarget(x, y, camera);
                    }
                }

                @Override
                public void onFailure(DJIError djiError){
                }
            });
        }
    }

    private void setAutoFocusTarget(float x, float y, Camera camera){
        PointF pointF = new PointF(x, y);
        camera.setFocusTarget(pointF, new CommonCallbacks.CompletionCallback(){
            @Override
            public void onResult(DJIError djiError){
                if(djiError == null){
                    logger.info(TAG+"设置焦点成功" + "--x:" + x + "--y" + y);
                }else{
                    logger.error(TAG+djiError.getDescription());
                }
            }
        });
    }

    /**
     * 开启防碰撞
     * @param open     是否开启
     */
    private void checkObstacle(boolean open){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            if(null != aircraft.getFlightController().getFlightAssistant()){
                aircraft.getFlightController().getFlightAssistant().setCollisionAvoidanceEnabled(open, new CommonCallbacks.CompletionCallback(){
                    @Override
                    public void onResult(DJIError djiError){
                        if(null == djiError){
                            ToastUtil.showToast(getApplicationContext(), "开启或关闭防碰撞成功:" + open);
                            logger.info(TAG+"开启或关闭防碰撞成功:" + open);
                        }else{
                            ToastUtil.showToast(getApplicationContext(), "开启或关闭防碰撞失败:--" + open + "-----" + djiError.getDescription());
                            logger.info(TAG+"开启或关闭防碰撞失败:--" + open + "-----" + djiError.getDescription());
                        }
                    }
                });
            }
        }
    }

    /**
     * 取消自动返航
     *
     */
    private void cancelGoHome(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            aircraft.getFlightController().cancelGoHome(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError){
                    if(djiError == null){
                        mHandler.post(() -> {
                            btnGoHome.setBackground(getResources().getDrawable(R.drawable.not_go_home));
                        });
                        logger.info(TAG+"取消自动返航成功");
                    }else{
                        mHandler.post(() -> {
                            btnGoHome.setBackground(getResources().getDrawable(R.drawable.going_home));
                            ToastUtil.showToast(getApplicationContext(), "取消自动返航失败");
                        });
                        logger.error(TAG+"取消自动返航失败--" + djiError.getDescription());
                    }
                }
            });
        }
    }

    /**
     * 自动返航
     */
    private void autoGoHome(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft != null){
            aircraft.getFlightController().startGoHome(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError){
                    if(djiError == null){
                        mHandler.post(() -> {
                            btnGoHome.setBackground(getResources().getDrawable(R.drawable.going_home));
                        });
                        logger.info(TAG+"自动返航成功");
                        ToastUtil.showToast(getApplicationContext(), "自动返航成功");
                    }else{
                        mHandler.post(() -> {
                            btnGoHome.setBackground(getResources().getDrawable(R.drawable.not_go_home));
                            ToastUtil.showToast(getApplicationContext(), "自动返航失败");
                        });
                        logger.error(TAG+"自动返航失败--" + djiError.getDescription());
                        ToastUtil.showToast(getApplicationContext(), "自动返航失败");
                    }
                }
            });
        }
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
        if(aircraftLatitude !=0.0 && aircraftLongitude !=0.0){
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

    private void configWayPointMission(){
        if(waypointMissionBuilder == null){
            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction).headingMode(mHeadingMode).autoFlightSpeed(mSpeed).maxFlightSpeed(mSpeed).flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        }else{
            waypointMissionBuilder.finishedAction(mFinishedAction).headingMode(mHeadingMode).autoFlightSpeed(mSpeed).maxFlightSpeed(mSpeed).flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        }
        //        if(waypointMissionBuilder.getWaypointList().size() > 0){
        //            for(int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++){
        //                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
        //            }
        //        }
        logger.info(TAG+"Set Waypoint attitude successfully");
        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if(error == null){
            logger.info(TAG+"loadWaypoint succeeded");
        }else{
            ToastUtil.showToast(getApplicationContext(),error.getDescription());
            logger.error(TAG+"loadWaypoint failed " + error.getDescription());
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener(){
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent){
        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent){
        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent){
            logger.info(TAG+"onExecutionUpdate--"+executionEvent);

        }

        @Override
        public void onExecutionStart(){
            logger.info(TAG+"--onExecutionStart");
            patrol = true;
            ToastUtil.showToast(getApplicationContext(),"开始巡航");
        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error){
            waypointList.clear();
            patrol = false;
            logger.error(TAG+"Execution finished: " + (error == null ? "Success!" : error.getDescription()));
            ToastUtil.showToast(getApplicationContext(),"Execution finished: " + (error == null ? "Success!" : error.getDescription()));
            //            setResultToToast();
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator(){
        if(instance == null){
            if(DJISDKManager.getInstance().getMissionControl() != null){
                instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
            }
        }
        return instance;
    }

    private void startWaypointMission(){
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback(){
            @Override
            public void onResult(DJIError error){
                logger.info(TAG+"Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
                //                ToastUtil.showToast(getApplicationContext(),"Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
                //                setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
                if(error !=null){
                    waypointList.clear();
                    logger.info("开始巡航任务失败:"+error.getDescription());
                }
            }
        });
    }

    private void uploadWayPointMission(){

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    logger.info(TAG+"--Mission upload successfully!");
                    ToastUtil.showToast(getApplicationContext(),TAG+"--Mission upload successfully!");
                    startWaypointMission();
                } else {
                    logger.info(TAG+"--Mission upload failed, error: " + error.getDescription() + " retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                    waypointList.clear();
                }
            }
        });

    }

    private void stopWaypointMission(){

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                logger.info(TAG+"Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
                ToastUtil.showToast(getApplicationContext(),"Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
                waypointList.clear();
            }
        });

    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null){
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private void setHomeLocation(){
        Aircraft aircraft = AirCraftUtil.getAircraftInstance();
        if(aircraft !=null){
            aircraft.getFlightController().setHomeLocationUsingAircraftCurrentLocation(djiError -> {
                if(djiError == null){
                    logger.info(TAG+"返航位置设置成功");
                }else {
                    logger.error(TAG+"返航位置设置失败："+djiError);
                }
            });
        }
    }


}
