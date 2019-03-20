package org.easydarwin.easypusher;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.TextureView;
import android.view.WindowManager;

import org.apache.log4j.Logger;
import org.easydarwin.push.BITMediaStream;
import org.easydarwin.push.UVCMediaStream;

public class BITBackgroundCameraService extends Service implements TextureView.SurfaceTextureListener {
    private Logger logger = Logger.getLogger(getClass());
    private static final String TAG = BITBackgroundCameraService.class.getSimpleName();
    public static final String EXTRA_RR = "extra_rr";
    /**
     * 表示后台是否正在渲染
     */
    public static final String EXTRA_STREAMING = "extra_streaming";
    private TextureView mOutComeVideoView;
    private WindowManager mWindowManager;
    private BroadcastReceiver mReceiver = null;
    private BITMediaStream mMediaStream;
    private UVCMediaStream uvcMediaStream;
    private boolean uvcPush;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private SurfaceTexture mTexture;
    private boolean mPenddingStartPreview;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mTexture = surface;
        if(!uvcPush){
            if (mPenddingStartPreview && mMediaStream != null){
                mMediaStream.setSurfaceTexture(mTexture);
                mMediaStream.startPreview();
            }
        }else {
            if (mPenddingStartPreview && uvcMediaStream != null){
                uvcMediaStream.setSurfaceTexture(mTexture);
                uvcMediaStream.startPreview();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mTexture = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public BITMediaStream getMediaStream() {
        return mMediaStream;
    }

    public void setMediaStream(BITMediaStream ms) {
        mMediaStream = ms;
    }

    public UVCMediaStream getUvcMediaStream(){
        return uvcMediaStream;
    }

    public void setUVCMediaStream(UVCMediaStream uvcMediaStream){
        this.uvcMediaStream = uvcMediaStream;
        uvcPush = null != uvcMediaStream;
    }

    public void activePreview() {
        if (mMediaStream != null) {
            mMediaStream.stopPreview();
            if (mTexture != null){
                mMediaStream.setSurfaceTexture(mTexture);
                mMediaStream.startPreview();
            }else{
                mPenddingStartPreview = true;
            }
        }
    }

    public void activeUVCPreview(){
        if (uvcMediaStream != null) {
            uvcMediaStream.stopPreview();
            if (mTexture != null){
                uvcMediaStream.setSurfaceTexture(mTexture);
                uvcMediaStream.startPreview();
            }else{
                mPenddingStartPreview = true;
            }
        }
    }

    public void inActivePreview(){
        if (mOutComeVideoView != null) {
            if (mOutComeVideoView.getParent() != null) {
                mWindowManager.removeView(mOutComeVideoView);
            }
        }
        stopForeground(true);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public BITBackgroundCameraService getService() {
            return BITBackgroundCameraService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        logger.info("BackgroundCameraService执行onCreate()");


        // Create new SurfaceView, set its size to 1x1, move it to the top left
        // corner and set this service as a callback
        try{
            mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            mOutComeVideoView = new TextureView(getApplicationContext());
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.RGBA_8888);
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH ;
            mWindowManager.addView(mOutComeVideoView, layoutParams);
            mOutComeVideoView.setSurfaceTextureListener(this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("BackgroundCameraService执行onStartCommand()");
        if (intent == null) {
            return START_NOT_STICKY;
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {

        logger.info("BackgroundCameraService执行onDestroy()");
        if (mOutComeVideoView != null) {
            if (mOutComeVideoView.getParent() != null) {
                mWindowManager.removeView(mOutComeVideoView);
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        logger.info("BackgroundCameraService执行onUnbind()");
        return super.onUnbind(intent);
    }
}
