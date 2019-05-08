package cn.vsx.vc.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.log4j.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFinishActivityHandler;
import cn.vsx.vc.utils.ActivityCollector;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * author: zjx.
 * data:on 2017/11/10
 */

public class VlcPlayActivity extends Activity {

    private final int HIDE_PROGRESS_VIEW = 180205;

    @Bind(R.id.fl_container)
    FrameLayout fl_container;
    @Bind(R.id.svLive)
    TextureView svLive;
    @Bind(R.id.video_loading)
    LinearLayout video_loading;
    @Bind(R.id.video_loading_progress)
    ProgressBar video_loading_progress;
    @Bind(R.id.video_loading_text)
    TextView video_loading_text;

    private String streamMediaServerUrl;

    private Logger logger = Logger.getLogger(getClass());
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HIDE_PROGRESS_VIEW) {
                handler.removeMessages(HIDE_PROGRESS_VIEW);
            }
        }
    };

    public static void startVlcPlayActivity (Activity activity, String playUrl, String title) {
        Intent intent = new Intent(activity, VlcPlayActivity.class);
        intent.putExtra("playUrl", playUrl);
        intent.putExtra("title", title);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this, getClass());
        // 没有标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        hideSmartBar();
        setContentView(R.layout.activity_vlc_play);
        ButterKnife.bind(this);

        streamMediaServerUrl = getIntent().getStringExtra("playUrl");

        MyTerminalFactory.getSDK().registReceiveHandler(mReceiverFinishActivityHandler);


        svLive.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if(streamMediaServerUrl != null){
//                    startPull(surface);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//                stopPull();
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }


//    private EasyPlayerClient mStreamRender;
//    private int mType;
//    private void startPull(SurfaceTexture surface) {
//        video_loading.setVisibility(View.VISIBLE);
//        mStreamRender = new EasyPlayerClient(VlcPlayActivity.this, "79393674363536526D3430416365592B77526C6E38504A6A626935365A574E305A574D7563485230567778576F4E6A7734456468646D6C754A6B4A68596D397A595541794D4445325257467A65555268636E6470626C526C5957316C59584E35",
//                new Surface(surface), mResultReceiver);
//        try {
//            if (streamMediaServerUrl != null){
//                mStreamRender.start(streamMediaServerUrl, mType, Client.EASY_SDK_VIDEO_FRAME_FLAG | Client.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(VlcPlayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            return;
//        }
//    }

//    private ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            super.onReceiveResult(resultCode, resultData);
//            logger.error("mResultReceiver"+resultCode+"---"+resultData);
//            if (resultCode == EasyPlayerClient.RESULT_VIDEO_DISPLAYED) {
//                video_loading.setVisibility(View.GONE);
//            } else if (resultCode == EasyPlayerClient.RESULT_VIDEO_SIZE) {
//
//            } else if (resultCode == EasyPlayerClient.RESULT_TIMEOUT) {
//                new AlertDialog.Builder(VlcPlayActivity.this).setMessage("试播时间到").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
//            } else if (resultCode == EasyPlayerClient.RESULT_UNSUPPORTED_AUDIO) {
//                new AlertDialog.Builder(VlcPlayActivity.this).setMessage("音频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
//            } else if (resultCode == EasyPlayerClient.RESULT_UNSUPPORTED_VIDEO) {
//                new AlertDialog.Builder(VlcPlayActivity.this).setMessage("视频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
//            }else if (resultCode == EasyPlayerClient.RESULT_EVENT){
//                int errorcode = resultData.getInt("errorcode");
//                if (errorcode != 0){
//                    stopPull();
//                    ToastUtil.showToast(VlcPlayActivity.this, "网络连接错误，停止播放！！！");
//                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverFinishActivityHandler.class);
//                }
//            }else if (resultCode == EasyPlayerClient.RESULT_RECORD_BEGIN){
//            }else if (resultCode == EasyPlayerClient.RESULT_RECORD_END){
//            }
//        }
//    };

//    private void stopPull() {
//        if (video_loading != null) {
//            video_loading.setVisibility(View.GONE);
//        }
//        MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                if (mStreamRender != null) {
//                    mStreamRender.stop();
//                    mStreamRender = null;
//                }
//                stopService(new Intent(VlcPlayActivity.this, BackgroundCameraService.class));
//            }
//        });
//    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        logger.error("vlc进行资源释放1");
        ButterKnife.unbind(this);
        ActivityCollector.removeActivity(this);
        logger.error("vlc进行资源释放5");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (MyTerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine().getCurrentState() != VideoLivePlayingState.IDLE) {
//            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
//            logger.error("vlc进行资源释放4");
//        }
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverFinishActivityHandler.class);
        deleteMessage();
    }

    /**  隐藏虚拟按键 */
    private void hideSmartBar () {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if (Build.VERSION.SDK_INT >= 19) {
            uiFlags |= View.SYSTEM_UI_FLAG_IMMERSIVE;//0x00001000; // SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }

        try {
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void deleteMessage () {
        MyTerminalFactory.getSDK().putParam("callId", 0L);
        MyTerminalFactory.getSDK().putParam("sendMemberId", 0);
        MyTerminalFactory.getSDK().putParam("sendMemberName","");
        MyTerminalFactory.getSDK().putParam("messageUrl","");
        MyTerminalFactory.getSDK().putParam("allMessageVersion", 0L);
        MyTerminalFactory.getSDK().putParam("liveTitle", "");
        MyTerminalFactory.getSDK().putParam("cameraNo", "");
        MyTerminalFactory.getSDK().putParam("cameraName", "");
        MyTerminalFactory.getSDK().putParam("isCamera", false);
//        MyTerminalFactory.getSDK().putParam(Params.VIDEO_LIVE_PULL_URL, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }



    private ReceiverFinishActivityHandler mReceiverFinishActivityHandler = new ReceiverFinishActivityHandler() {
        @Override
        public void handler() {
            if (MyTerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine().getCurrentState() != VideoLivePlayingState.IDLE) {
                MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handler.removeCallbacksAndMessages(null);
                    MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiverFinishActivityHandler);
                    finish();
                }
            });
        }
    };

}
