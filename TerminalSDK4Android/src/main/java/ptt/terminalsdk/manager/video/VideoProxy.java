package ptt.terminalsdk.manager.video;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;


import org.easydarwin.push.InitCallback;
import org.easydarwin.push.MediaStream;

import java.util.List;

import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.VideoUtil;


/**
 * Created by ysl on 2017/6/8.
 */

public class VideoProxy{

    public static Bus sMainBus;
    private MediaStream mediaStream;
    private boolean isCameraBack;
    int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public VideoProxy(){
    }

    public void createCamera() {
        mediaStream.createCamera();
    }

    public void destroyCamera() {
        mediaStream.destroyCamera();
    }

    public void startPreview() {
        mediaStream.startPreview();
    }

    public void stopPreview() {
        mediaStream.stopPreview();
    }


    public void startStream(String ip, String port, String streamName, InitCallback callback) {
        mediaStream.startStream(ip, port, streamName, callback);
    }

//    public void reStartStream() {
//        mediaStream.reStartStream();
//    }

    public void stopStream() {
        mediaStream.stopStream();
    }

    public boolean isStreaming() {
        return mediaStream.isStreaming();
    }

    public void switchCamera() {
        mediaStream.switchCamera();
    }

    public void setDgree(int dgree) {
        mediaStream.setDgree(dgree);
    }

    public void updateResolution(int width, int height) {
        mediaStream.updateResolution(width, height);
    }

    public Bus start() {
        if (sMainBus == null){
            sMainBus = new Bus(ThreadEnforcer.ANY);
        }
        return sMainBus;
    }

    public void saveCamareResolution() {
        if (VideoUtil.getSupportResolution().size() == 0) {//把摄像头支持的分辨率保存起来
            StringBuilder stringBuilder = new StringBuilder();
            Camera camera = null;
            try {
                camera = Camera.open();
                Camera.Parameters mParameters = camera.getParameters(); //针对魅族手机
                camera.setParameters(mParameters);
                if (camera == null || mParameters == null){
                    Toast.makeText(MyTerminalFactory.getSDK().getApplication(), "相机不可用！", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MyTerminalFactory.getSDK().getApplication(), "相机不可用！", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Size> supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
            for (Size str : supportedPreviewSizes) {
                stringBuilder.append(str.width + "x" + str.height).append(";");
            }
            VideoUtil.saveSupportResolution(stringBuilder.toString());
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void stop() {
        sMainBus = null;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    private Activity activity;
    public Activity getActivity(){
        return activity;
    }

}
