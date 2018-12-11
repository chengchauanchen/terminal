package cn.vsx.vc.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverRemoveWindowViewHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.cameralibrary.JCameraView;
import cn.vsx.vc.view.cameralibrary.listener.ClickListener;
import cn.vsx.vc.view.cameralibrary.listener.ErrorListener;
import cn.vsx.vc.view.cameralibrary.listener.JCameraListener;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by weishixin on 2018/4/11.
 */

public class CameraActivity extends BaseActivity {

    private JCameraView jCameraView;
    private static final int CODE_IMAGE_RESULT=0;
    private static final int CODE_VIDEO_RESULT=1;
    private static final int CODE_FAIL=-1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBottomUIMenu();
    }

    private void hideBottomUIMenu(){
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }

    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_camera;
    }

    @Override
    public void initView() {
        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        //设置视频保存路径
        jCameraView.setSaveVideoPath(TerminalFactory.getSDK().getVideoRecordDirectory());
        //设置只能录像或只能拍照或两种都可以（默认两种都可以）
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);

        //设置视频质量
        jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

        //JCameraView监听
        jCameraView.setErrorLisenter(new ErrorListener() {
                                         @Override
                                         public void onError() {
                                             Log.i("CJT", "open camera error");
                                         }

                                         @Override
                                         public void AudioPermissionError() {
                                             ToastUtil.showToast(CameraActivity.this,"录音机被占用");
                                             Log.i("CJT", "AudioPermissionError");
                                         }
                                     }
        );

        jCameraView.setJCameraLisenter(new JCameraListener() {
                                           @Override
                                           public void captureSuccess(Bitmap bitmap) {
                                               Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
                                               int photoNum = TerminalFactory.getSDK().getParam(Params.PHOTO_NUM, 0);
                                               photoNum++;
                                               TerminalFactory.getSDK().putParam(Params.PHOTO_NUM, photoNum);
                                               BitmapUtil.saveBitmapFile(bitmap,TerminalFactory.getSDK().getPhotoRecordDirectory(),"image"+photoNum+".jpg");
                                               CameraActivity.this.setResult(CODE_IMAGE_RESULT);
                                               CameraActivity.this.finish();
                                           }

                                           @Override
                                           public void recordSuccess(String url, Bitmap firstFrame) {
                                               Log.i("CJT", "url = " + url+"/"+firstFrame.getWidth()+"/"+firstFrame.getHeight());
                                               Intent data=new Intent();
                                               Bundle b = new Bundle();
                                               b.putString("url", url);
                                               CameraActivity.this.setResult(CODE_VIDEO_RESULT,data.putExtras(b));
                                               CameraActivity.this.finish();
                                           }

                                           @Override
                                           public void quit(){
                                               if(!CameraActivity.this.isFinishing()){
                                                   Log.e("CameraActivity", "quit");
                                                   CameraActivity.this.setResult(CODE_FAIL);
                                                   CameraActivity.this.finish();
                                               }
                                           }
                                       }

        );
        //左边按钮点击事件
        jCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                Log.d("CJT","左边按钮被点击");
                CameraActivity.this.setResult(CODE_FAIL);
                CameraActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        setResult(CODE_FAIL);
        finish();
        super.onBackPressed();
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiverRemoveWindowViewHandler);//请求开视频
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyIndividualCallIncommingHandler);//请求开视频
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        jCameraView.onPause();
    }

    @Override
    public void initData() {

    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiverRemoveWindowViewHandler);//收到个呼请求
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyIndividualCallIncommingHandler);//收到个呼请求
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
    }

    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    jCameraView.onStop();
                }
            });
        }
    };

    private ReceiveNotifyIndividualCallIncommingHandler mReceiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler(){
        @Override
        public void handler(String mainMemberName, int mainMemberId, int individualCallType){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    jCameraView.onStop();
                }
            });
        }
    };

    private ReceiverRemoveWindowViewHandler mReceiverRemoveWindowViewHandler = new ReceiverRemoveWindowViewHandler(){
        @Override
        public void handle(){
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    hideBottomUIMenu();
                }
            });
        }
    };
}
