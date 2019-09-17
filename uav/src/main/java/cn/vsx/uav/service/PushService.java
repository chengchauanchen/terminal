package cn.vsx.uav.service;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;
import org.easydarwin.push.AirCraftMediaStream;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.uav.R;
import cn.vsx.uav.activity.UavPushActivity;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.service.BaseService;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.yuv.YuvPlayer;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.sdkmanager.DJISDKManager;
import ptt.terminalsdk.context.MyTerminalFactory;

public class PushService extends BaseService implements YuvPlayer.YuvDataListener{

    private YuvPlayer yuvPlayer;
    private AirCraftMediaStream airCraftMediaStream;
    private long lastupdate;
    private static String TAG = "PushService---";
    private Logger logger = Logger.getLogger(getClass());
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler();
    private TextureView mSvLivePop;

    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;
    private boolean start;

    public PushService(){
    }

    @Override
    public IBinder onBind(Intent intent){
        return new PushServiceBinder();
    }

    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_aircraft_push_view, null);
    }

    @Override
    protected void findView(){
        mSvLivePop = rootView.findViewById(R.id.sv_live_pop);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    @Override
    protected void initData(){
        mSvLivePop.setSurfaceTextureListener(new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int surfaceWidth, int surfaceHeight){
                logger.info(TAG+"----onSurfaceTextureAvailable");
                Surface surface1 = new Surface(surface);
                initYuvPlayer();
                startPush(surfaceWidth, surfaceHeight, surface1);
                startRecord();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height){
                setSufaceWidthHeight(width,height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface){
                logger.info(TAG+"----onSurfaceTextureDestroyed");
                stopYuvPlayer();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface){
            }
        });
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void initListener(){
        TerminalFactory.getSDK().registReceiveHandler(mReceiveExternStorageSizeHandler);
        mSvLivePop.setOnTouchListener((v, event) -> {
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
                        hideView();
                        Intent intent = new Intent(getApplicationContext(), UavPushActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
            }
            return true;
        });
    }

    @Override
    protected void initView(Intent intent){
    }

    @Override
    protected void showPopMiniView(){
    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        TerminalFactory.getSDK().unregistReceiveHandler(mReceiveExternStorageSizeHandler);
        if(DJISDKManager.getInstance().getProduct() != null){
            VideoFeeder.getInstance().getPrimaryVideoFeed().removeVideoDataListener(mReceivedVideoDataListener);
        }
    }

    public void startPush(int surfaceWidth, int surfaceHeight, Surface surface1){
        yuvPlayer.start(surfaceWidth, surfaceHeight, 16, surface1);
        if(airCraftMediaStream != null && !airCraftMediaStream.isStreaming()){
            airCraftMediaStream.startPreView(1280, 720);
        }
        start = true;
    }

    public void startRecord(){
        if(airCraftMediaStream != null){
            airCraftMediaStream.startRecord();
        }
    }

    public void setSufaceWidthHeight(int width, int height){
        if(null != yuvPlayer){
            yuvPlayer.setSufaceWidthHeight(width, height);
        }
    }

    public void startAircraftPush(String ip,String port,String id){
        if(null != airCraftMediaStream){
            airCraftMediaStream.startStream(ip, port, id, pushCallback);
            String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
            logger.info(TAG+"推送地址：" + url);
        }
    }

    public class PushServiceBinder extends Binder {
        public PushService getService() {
            return PushService.this;
        }
    }

    public void initYuvPlayer(){
        if(null == yuvPlayer){
            yuvPlayer = new YuvPlayer(getApplicationContext());
            yuvPlayer.setYuvDataListener(this);
        }
    }

    public void initAirCraftMediaStream(){
        logger.info("initAirCraftMediaStream");
        if(null == airCraftMediaStream){
            airCraftMediaStream = new AirCraftMediaStream(getApplicationContext());
            logger.info("airCraftMediaStream:"+airCraftMediaStream);
        }
    }

    public void stopYuvPlayer(){
        if(yuvPlayer != null){
            yuvPlayer.stop();
            yuvPlayer.setYuvDataListener(null);
            yuvPlayer = null;
        }
    }

    private long lastRecvDataTime;
    @Override
    public void onDataRecv(byte[] data, int width, int height){
        if(System.currentTimeMillis() - lastRecvDataTime > 3*1000){
            logger.info("收到YUV数据"+data.length);
        }
        lastRecvDataTime = System.currentTimeMillis();
        if(airCraftMediaStream != null){
            airCraftMediaStream.push(data, width, height);
        }
    }

    public void setVideoFeederListeners(){
        VideoFeeder instance = VideoFeeder.getInstance();
        logger.info(TAG+"VideoFeeder:" + instance);
//        ToastUtil.showToast(getApplicationContext(), "VideoFeeder:" + instance);
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


    //无人机推送
    private VideoFeeder.VideoDataListener mReceivedVideoDataListener = new VideoFeeder.VideoDataListener(){
        @Override
        public void onReceive(byte[] videoBuffer, int size){
            if(System.currentTimeMillis() - lastupdate > 3000){
                logger.info("收到无人机视频数据:"+size);
            }
            lastupdate = System.currentTimeMillis();
            if(null != yuvPlayer){
                yuvPlayer.parseH264(videoBuffer, size);
            }
        }
    };

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
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    resultData.putString("event-msg", "EasyRTSP 连接失败");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    resultData.putString("event-msg", "EasyRTSP 连接异常中断");

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

    public void finishVideoLive(){
        stopAircraftPush();
    }

    private void stopAircraftPush(){
        logger.info(TAG+"stopAircraftPush");
        if(null != airCraftMediaStream){
            logger.info(TAG+"结束无人机推流");
            airCraftMediaStream.stopStream();
            airCraftMediaStream.stopRecord();
            airCraftMediaStream = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        lastupdate = 0;
        lastRecvDataTime = 0;
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    public void showView(){
        if(!dialogAdd){
            windowManager.addView(rootView, layoutParams);
            MyApplication.instance.viewAdded = true;
            dialogAdd = true;
            mSvLivePop.setVisibility(View.VISIBLE);
        }
    }

    public void hideView(){
        if(dialogAdd){
            windowManager.removeView(rootView);
            MyApplication.instance.viewAdded = false;
            dialogAdd = false;
        }
    }

    /**
     *通知存储空间不足
     */
    private ReceiveExternStorageSizeHandler mReceiveExternStorageSizeHandler = memorySize -> mHandler.post(() -> {
        if (memorySize < 100) {
            ToastUtil.showToast(PushService.this, getString(R.string.toast_tempt_insufficient_storage_space));
            PromptManager.getInstance().startExternNoStorage();
            if(airCraftMediaStream != null&& airCraftMediaStream.isRecording()){
                //停止录像
                airCraftMediaStream.stopRecord();
            }
            //上传没有上传的文件，删除已经上传的文件
            MyTerminalFactory.getSDK().getFileTransferOperation().externNoStorageOperation();
        } else if (memorySize < 200){
            PromptManager.getInstance().startExternStorageNotEnough();
            ToastUtil.showToast(PushService.this, getString(cn.vsx.vc.R.string.toast_tempt_storage_space_is_in_urgent_need));
        }
    });

    public boolean isStart(){
        return start;
    }

    public void changeSurface(Surface surface,int width,int height){
        yuvPlayer.changeSurface(surface,width,height);
    }
}
