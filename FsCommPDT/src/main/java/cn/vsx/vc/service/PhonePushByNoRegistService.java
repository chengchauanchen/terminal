package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.MediaStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeByNoRegistHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyOtherStopVideoMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSupportResolutionHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.NetworkUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 没有注册的手机上报视频
 */
public class PhonePushByNoRegistService extends BaseService{

    private RelativeLayout mPopupMiniLive;
    private TextureView mSvLivePop;
    private RelativeLayout mRlPhonePushLive;
    private TextureView mSvLive;
    private TextView mTvLiveRealtime;
    private ImageView mIvLiveRetract;
    private LinearLayout mLlLiveChageCamera;
    private ImageView mIvLiveChageCamera;
    private LinearLayout mLlLiveHangupTotal;
    private MediaStream mMediaStream;
    private List<String> listResolution;
    private PushCallback pushCallback;
    private String ip;
    private String port;
    private String id;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;
    private static final int CURRENTTIME = 0;
    private static final int HIDELIVINGVIEW = 1;
    private static final int AUTOFOCUS = 2;
    private static final int SHOWERRORVIEW = 3;
    private int width = 640;
    private int height = 480;
    protected TextView tvNoNetwork;
    protected LinearLayout llNoNetwork;

    public PhonePushByNoRegistService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_phone_push_by_no_regist, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
//        mSvLive.setRotation(90.0f);
        if(screenWidth < screenHeight){
            layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    }

    @Override
    protected void findView(){
        //小窗口
        mPopupMiniLive = rootView.findViewById(R.id.popup_mini_live);
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
        //大窗口
        mRlPhonePushLive = rootView.findViewById(R.id.rl_phone_push_live);
        mSvLive = rootView.findViewById(R.id.sv_live);
        mTvLiveRealtime = rootView.findViewById(R.id.tv_live_realtime);
        mIvLiveRetract = rootView.findViewById(R.id.iv_live_retract);
        mLlLiveChageCamera = rootView.findViewById(R.id.ll_live_chage_camera);
        mIvLiveChageCamera = rootView.findViewById(R.id.iv_live_chage_camera);
        mLlLiveHangupTotal = rootView.findViewById(R.id.ll_live_hangup_total);

        tvNoNetwork = rootView.findViewById(R.id.tv_no_network);
        llNoNetwork = rootView.findViewById(R.id.ll_no_network);
        showNoNetworkView(getString(R.string.text_liveing_error_recording));
        if(NetworkUtil.isConnected(MyApplication.getApplication())){
            llNoNetwork.setVisibility(View.GONE);
        }else{
            llNoNetwork.setVisibility(View.VISIBLE);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSupportResolutionHandler);


        mSvLive.setSurfaceTextureListener(surfaceTextureListener);
//        mSvLive.setOnClickListener(svOnClickListener);
        mSvLive.setOnTouchListener(svOnTouchListener);
        mSvLivePop.setSurfaceTextureListener(surfaceTextureListener);
        mLlLiveHangupTotal.setOnClickListener(hangUpOnClickListener);
        mIvLiveRetract.setOnClickListener(retractOnClickListener);
        mPopupMiniLive.setOnTouchListener(miniPopOnTouchListener);
        mIvLiveChageCamera.setOnClickListener(changeCameraOnClickListener);
    }

    @Override
    protected void initView(Intent intent){
        hideAllView();
        mRlPhonePushLive.setVisibility(View.VISIBLE);
        showLivingView();
        mHandler.sendEmptyMessage(CURRENTTIME);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        if(requestCode != BaseCommonCode.SUCCESS_CODE){
            ToastUtil.livingFailToast(this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
            finishVideoLive();
        }
    }

    @Override
    protected void showPopMiniView(){
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        windowManager.removeView(rootView);
        windowManager.addView(rootView, layoutParams);
        hideAllView();
        MyApplication.instance.isMiniLive = true;
        mPopupMiniLive.setVisibility(View.VISIBLE);
        mSvLivePop.setRotation(90.0f);
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case CURRENTTIME:
                setCurrentTime();
                break;
            case HIDELIVINGVIEW:
                mHandler.removeMessages(HIDELIVINGVIEW);
                hideLivingView();
                break;
            case OFF_LINE:
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.exit_push));
                finishVideoLive();
                break;
            case AUTOFOCUS:
                mHandler.removeMessages(AUTOFOCUS);
                if(mMediaStream != null){
                    handleFocus(mMediaStream.getCamera());
                }
                mHandler.sendEmptyMessageDelayed(AUTOFOCUS,5000);
                break;
            case SHOWERRORVIEW:
                mHandler.removeMessages(SHOWERRORVIEW);
                showErrorView();
                break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @Override
    protected void initBroadCastReceiver(){}

    @Override
    protected void initData(){
        listResolution = new ArrayList<>(Arrays.asList("1920x1080", "1280x720", "640x480", "320x240"));
    }

    @Override
    public void onDestroy(){
        //处理当正在录像的时候，异常退出处理
        finishVideoLive();
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyOtherStopVideoMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSupportResolutionHandler);
    }

    private void hideAllView(){
        if(mPopupMiniLive !=null){
            mPopupMiniLive.setVisibility(View.GONE);
        }
        if(mRlPhonePushLive !=null){
            mRlPhonePushLive.setVisibility(View.GONE);
        }
    }

    private ReceiveNetworkChangeByNoRegistHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeByNoRegistHandler(){
        @Override
        public void handler(boolean connected){
            mHandler.post(() -> {
                if(!connected){
                    showNoNetworkView(getString(R.string.text_liveing_error_recording));
                }
                if(llNoNetwork!=null) {
                    llNoNetwork.setVisibility((!connected) ? View.VISIBLE : View.GONE);
                }
            });
        }
    };

    /**
     * 自己发起直播的响应
     **/
    private ReceiveResponseMyselfLiveHandler receiveResponseMyselfLiveHandler = (resultCode, resultDesc) -> mHandler.post(() -> {
        if(resultCode == BaseCommonCode.SUCCESS_CODE){
        }else{
            ToastUtil.showToast(MyTerminalFactory.getSDK().application, resultDesc);
            finishVideoLive();
        }
    });

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = (liveMemberId, callId, methodResult, resultDesc) -> mHandler.post(() -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_stoped));
        finishVideoLive();
    });

    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = (streamMediaServerIp, streamMediaServerPort, callId) -> mHandler.postDelayed(() -> {
        logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort + "---callId:" + callId);
        ip = streamMediaServerIp;
        port = String.valueOf(streamMediaServerPort);
        id = TerminalFactory.getSDK().getUuid() ;
        //如果是组内上报，在组内发送一条上报消息
//        sendGroupMessage(streamMediaServerIp,streamMediaServerPort,callId,pushMemberList,isGroupPushLive);
        startPush();
//        startRecord();
    }, 1000);

    /**
     *通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = memorySize -> mHandler.post(() -> {
        if (memorySize < 100) {
            ToastUtil.showToast(PhonePushByNoRegistService.this, getString(R.string.toast_tempt_insufficient_storage_space));
            PromptManager.getInstance().startExternNoStorage();
            if(mMediaStream!=null&&mMediaStream.isRecording()){
                //停止录像
                mMediaStream.stopRecord();
            }
            //上传没有上传的文件，删除已经上传的文件
            MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();
        } else if (memorySize < 200){
            PromptManager.getInstance().startExternStorageNotEnough();
            ToastUtil.showToast(PhonePushByNoRegistService.this, getString(R.string.toast_tempt_storage_space_is_in_urgent_need));
        }
    });
    /**
     * 收到上报停止的通知
     */
    private ReceiveNotifyOtherStopVideoMessageHandler receiveNotifyOtherStopVideoMessageHandler = (message) -> {
        logger.info("收到停止上报通知");
        mHandler.post(() -> finishVideoLive());
    };

    private ReceiveSupportResolutionHandler receiveSupportResolutionHandler = new ReceiveSupportResolutionHandler(){
        @Override
        public void Handle(){
            setResolution();
            if(mMediaStream !=null){
                mMediaStream.updateResolution(width, height);
            }
        }
    };

    private void setResolution(){
        List<String> supportListResolution = org.easydarwin.util.Util.getSupportResolution(getApplicationContext());
        if(null == supportListResolution || supportListResolution.isEmpty()){
            return;
        }
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        String setResolution = listResolution.get(position);
        if(supportListResolution.contains(setResolution)){
            String[] splitR = setResolution.split("x");
            width = Integer.parseInt(splitR[0]);
            height = Integer.parseInt(splitR[1]);
        }else {
            logger.info("支持的分辨率："+supportListResolution);
            String[] splitR = supportListResolution.get(0).split("x");
            width = Integer.parseInt(splitR[0]);
            height = Integer.parseInt(splitR[1]);
        }
    }

    private void startPush(){
        if(TextUtils.isEmpty(ip) || TextUtils.isEmpty(port) || TextUtils.isEmpty(id)){
            return;
        }
        logger.info("mMediaStream:" + mMediaStream + "----SurfaceTexture:" + mSvLive.getSurfaceTexture());
        if(mMediaStream == null){
            if(mSvLive.getSurfaceTexture() != null){
                pushStream(mSvLive.getSurfaceTexture());
            }else{
                ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.push_failed));
                finishVideoLive();
                return;
            }
        }
        if(null == pushCallback){
            pushCallback = new PushCallback();
        }
        mMediaStream.startStream(ip, port, id, pushCallback);
        String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();
        String url = String.format("rtsp://%s:%s/%s"+sdp, ip, port, id);
        logger.info("推送地址：" + url);

    }

    /**
     * 开始录像
     */
    private void startRecord(){
        //开始录像
        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory())) {
//            if(!mMediaStream.isRecording()){
                mMediaStream.startRecord();
//            }
        }
    }

    private class PushCallback implements InitCallback{

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
                    mHandler.post(PhonePushByNoRegistService.this::dissmissErrorView);
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    resultData.putString("event-msg", "EasyRTSP 连接失败--");
                    mHandler.sendEmptyMessageDelayed(SHOWERRORVIEW,3000);
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断--");
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
                    default:break;
            }
            logger.info("PhonePushService--PushCallback--msg:"+resultData.getString("event-msg")+"--code:"+code);
        }
    }



    private View.OnClickListener hangUpOnClickListener = v-> finishVideoLive();

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();

    private View.OnClickListener changeCameraOnClickListener = v -> changeCamera();

    private View.OnClickListener svOnClickListener = v->{
        if(null != mMediaStream && mMediaStream.isStreaming() && null != mMediaStream.getCamera()){
            mMediaStream.getCamera().autoFocus(null);//屏幕聚焦
        }
        showLivingView();
        mHandler.removeMessages(HIDELIVINGVIEW);
        mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
    };

    private float oldDist;
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener svOnTouchListener = (v,event)->{

        if (event.getPointerCount() == 1) {
            if(mMediaStream != null){
                handleFocus(mMediaStream.getCamera());
            }
            showLivingView();
            mHandler.removeMessages(HIDELIVINGVIEW);
            mHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW, 5000);
        } else {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = getFingerSpacing(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newDist = getFingerSpacing(event);
                    if(Math.abs(newDist-oldDist) > 5f){
                        if (newDist > oldDist) {
                            handleZoom(false);
                        } else if (newDist < oldDist) {
                            handleZoom(true);
                        }
                        oldDist = newDist;
                    }
                    break;
            }
        }
        return true;
    };

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            logger.info("onSurfaceTextureAvailable----->" + surface);
            pushStream(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
            logger.info("onSurfaceTextureDestroyed----->" + surface);

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface){
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener miniPopOnTouchListener = (v,event)->{
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
                    if(screenWidth < screenHeight){
                        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
                    windowManager.removeView(rootView);
                    windowManager.addView(rootView,layoutParams1);
                    hideAllView();
                    MyApplication.instance.isMiniLive = false;
                    mRlPhonePushLive.setVisibility(View.VISIBLE);
                }
                break;
        }
        return true;
    };

    private void handleFocus(Camera camera){
        if(null != mMediaStream && mMediaStream.isStreaming() && camera != null && mMediaStream.isPreView()){
            camera.autoFocus(null);//屏幕聚焦
        }
    }

    private void handleZoom(boolean isScale){
        if(null !=mMediaStream && mMediaStream.isStreaming()){
            mMediaStream.ZoomOrReduceVideo(isScale);
        }
    }

    private float getFingerSpacing(MotionEvent event){
        return (float)Math.sqrt(event.getX(0)*event.getX(1)+event.getY(0)*event.getY(1));
    }

    private void changeCamera(){
        logger.info("开始转换摄像头");
        mMediaStream.setDgree(getDgree());
        mMediaStream.switchCamera();
    }


    private void finishVideoLive(){
        mHandler.removeCallbacksAndMessages(null);
        PromptManager.getInstance().stopRing();//停止响铃
        stopPush();
        hideAllView();
        stopBusiness();
    }

    private void pushStream(SurfaceTexture surface){
        if(mMediaStream != null){    // switch from background to front
            mMediaStream.stopPreview();
//            if(mMediaStream.isRecording()){
//                mMediaStream.stopRecord();
//            }
            mMediaStream.setSurfaceTexture(surface);
            mMediaStream.startPreview();
//            startRecord();
            if(mMediaStream.isStreaming()){
                ToastUtil.showToast(PhonePushByNoRegistService.this, getResources().getString(R.string.pushing_stream));
            }
        }else{
            mMediaStream = new MediaStream(MyTerminalFactory.getSDK().application, surface, true);
            startCamera();
        }
    }

    private void stopPush(){
        mHandler.removeMessages(CURRENTTIME);
        if(mMediaStream != null){
            mMediaStream.stopPreview();
            mMediaStream.stopStream();
            mMediaStream.stopRecord();
            mMediaStream.release();
            mMediaStream = null;
            logger.info("---->>>>页面关闭，停止推送视频");
        }
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    private void startCamera(){
        setResolution();
        logger.error("分辨率--width:" + width + "----height:" + height);
        mMediaStream.updateResolution(width, height);
        mMediaStream.setDgree(getDgree());
        mMediaStream.createCamera();
        mMediaStream.startPreview();
        startRecord();
        if(mMediaStream.isStreaming()){
            ToastUtil.showToast(PhonePushByNoRegistService.this, getResources().getString(R.string.pushing_stream));
        }
        mHandler.sendEmptyMessage(AUTOFOCUS);
    }

    private int getDgree(){
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch(rotation){
            case Surface.ROTATION_0:
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
            default:
                break;
        }
        return degrees;
    }

    @SuppressLint("SimpleDateFormat")
    private void setCurrentTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        mTvLiveRealtime.setText(dateString);
        mHandler.sendEmptyMessageDelayed(CURRENTTIME, 10000);
    }

    private void hideLivingView(){
        mIvLiveRetract.setVisibility(View.GONE);
        mLlLiveChageCamera.setVisibility(View.GONE);
        mLlLiveHangupTotal.setVisibility(View.GONE);
    }

    private void showLivingView(){
        mTvLiveRealtime.setVisibility(View.VISIBLE);
        mIvLiveRetract.setVisibility(View.VISIBLE);
        mLlLiveChageCamera.setVisibility(View.VISIBLE);
        mLlLiveHangupTotal.setVisibility(View.VISIBLE);
    }

    /**
     * 显示错误提示
     */
    private void showErrorView(){
        try{
            showNoNetworkView((NetworkUtil.isConnected(MyApplication.getApplication()))
                    ?getString(R.string.text_media_server_error_recording)
                    :getString(R.string.text_liveing_error_recording));
            if(llNoNetwork!=null&&llNoNetwork.getVisibility() == View.GONE){
                llNoNetwork.setVisibility(View.VISIBLE);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 隐藏错误提示
     */
    private void dissmissErrorView(){
        try{
            if(llNoNetwork!=null&&llNoNetwork.getVisibility() == View.VISIBLE){
                llNoNetwork.setVisibility(View.GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 显示断流的文字提示
     * @param string
     */
    private void showNoNetworkView(String string){
        try{
            if(tvNoNetwork!=null){
                tvNoNetwork.setText(string);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
