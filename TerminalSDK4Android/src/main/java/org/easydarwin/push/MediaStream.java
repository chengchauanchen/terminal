package org.easydarwin.push;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.apache.log4j.Logger;
import org.easydarwin.audio.AudioStream;
import org.easydarwin.easypusher.BackgroundCameraService;
import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.sw.TxtOverlay;
import org.easydarwin.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSupportResolutionHandler;
import dagger.Module;
import dagger.Provides;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.FileTransgerUtil;
import ptt.terminalsdk.tools.ToastUtil;

import static android.graphics.ImageFormat.NV21;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;

@Module
public class MediaStream {
    private static final int SWITCH_CAMERA = 11;
    private final boolean enanleVideo;
    private Pusher mEasyPusher;
    static final String TAG = "MediaStream";
    private int width = 640, height = 480;
    //录制每个视频片段的时间
    private static final int VIDEO_RECODE_PER_TIME = 5 * 60 * 1000;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private WeakReference<SurfaceTexture> mSurfaceHolderRef;
    private Camera mCamera;
    private boolean pushStream = false;//是否要推送数据
    private AudioStream audioStream;
    private boolean isCameraBack = true;
    private int mDgree;
    private Context mApplicationContext;
    private boolean mSWCodec;
    private VideoConsumer mVC;
    private TxtOverlay overlay;
    private EasyMuxer mMuxer;
    private final HandlerThread mCameraThread;
    private final Handler mCameraHandler;
    //    private int previewFormat;
    public static CodecInfo info = new CodecInfo();
    private boolean preView;
    public Logger logger = Logger.getLogger(getClass());

    public MediaStream(Context context, SurfaceTexture texture) {
        this(context, texture, true);
    }

    public MediaStream(Context context, SurfaceTexture texture, boolean enableVideo) {
        mApplicationContext = context;
        mSurfaceHolderRef = new WeakReference(texture);
        mEasyPusher = new EasyPusher();
        mCameraThread = new HandlerThread("CAMERA") {
            public void run() {
                try {
                    super.run();
                } catch (Throwable e) {
                    logger.debug("mCameraThread---e:" + e.toString());
                    Intent intent = new Intent(mApplicationContext, BackgroundCameraService.class);
                    mApplicationContext.stopService(intent);
                } finally {
                    logger.info("HandlerThread---执行finally");
                    stopRecord();
                    stopStream();
                    stopPreview();
                    destroyCamera();
                }
            }
        };
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == SWITCH_CAMERA) {
                    switchCameraTask.run();
                }
            }
        };
        this.enanleVideo = enableVideo;

        if (enableVideo)
            previewCallback = new Camera.PreviewCallback() {

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (mDgree == 0) {
                        Camera.CameraInfo camInfo = new Camera.CameraInfo();
                        Camera.getCameraInfo(mCameraId, camInfo);
                        int cameraRotationOffset = camInfo.orientation;

                        if (cameraRotationOffset % 180 != 0) {
                            yuvRotate(data, 1, width, height, cameraRotationOffset);
                        }
                        save2file(data, String.format("/sdcard/yuv_%d_%d.yuv", height, width));
                    }
                    if (PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getBoolean("key_enable_video_overlay", false)) {
                        String txt = String.format("drawtext=fontfile=" + mApplicationContext.getFileStreamPath("SIMYOU.ttf") + ": text='%s%s':x=(w-text_w)/2:y=H-60 :fontcolor=white :box=1:boxcolor=0x00000000@0.3", "EasyPusher", new SimpleDateFormat("yyyy-MM-ddHHmmss").format(new Date()));
                        txt = "4GPTT " + new SimpleDateFormat("yy-MM-dd HH:mm:ss SSS").format(new Date());
                        //叠加水印
                        //overlay.overlay(data, txt);
                    }
//                    logger.info(TAG+"----PreviewCallback");
                    mVC.onVideo(data, NV21);
                    mCamera.addCallbackBuffer(data);
                }

            };
    }

    public void startStream(String url, InitCallback callback) {
        mEasyPusher.initPush(url, mApplicationContext, callback, ~0);
        pushStream = true;
    }

    public void startStream(String ip, String port, String id, InitCallback callback) {
        mEasyPusher.initPush(mApplicationContext, callback);
        mEasyPusher.setMediaInfo(Pusher.Codec.EASY_SDK_VIDEO_CODEC_H264, 25, Pusher.Codec.EASY_SDK_AUDIO_CODEC_AAC, 1, 8000, 16);

        String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();
        mEasyPusher.start(ip, port, String.format("%s"+sdp, id), Pusher.TransType.EASY_RTP_OVER_TCP);
        pushStream = true;
    }

    public void setDgree(int dgree) {
        mDgree = dgree;
    }

    /**
     * 更新分辨率
     */
    public void updateResolution(final int width, final int height) {
        this.width = width;
        this.height = height;
        if (mCamera == null) return;
        stopPreview();
        destroyCamera();
        createCamera();
        startPreview();
    }


    public static int[] determineMaximumSupportedFramerate(Camera.Parameters parameters) {
        int[] maxFps = new int[]{0, 0};
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        for (Iterator<int[]> it = supportedFpsRanges.iterator(); it.hasNext(); ) {
            int[] interval = it.next();
            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }
        return maxFps;
    }

    public void createCamera() {

        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                    createCamera();
                }
            });
            return;
        }
        logger.info("createCamera");
        if (!enanleVideo) {
            return;
        }
        try {
            mSWCodec = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getBoolean("key-sw-codec", false);
            mCamera = Camera.open(mCameraId);
            mCamera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int i, Camera camera) {
                    throw new IllegalStateException("Camera Error:" + i);
                }
            });
            Log.i(TAG, "open Camera");

            Camera.Parameters parameters = mCamera.getParameters();
            int[] max = determineMaximumSupportedFramerate(parameters);
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT)
                cameraRotationOffset += 180;
            int rotate = (360 + cameraRotationOffset - mDgree) % 360;
            parameters.setRotation(rotate);
            parameters.setRecordingHint(true);


            ArrayList<CodecInfo> infos = listEncoders("video/avc");
            if (infos.isEmpty()) mSWCodec = true;
            if (mSWCodec) {
            } else {
                CodecInfo ci = infos.get(0);
                info.mName = ci.mName;
                info.mColorFormat = ci.mColorFormat;
            }
            parameters.setPreviewSize(width, height);
            parameters.setPreviewFrameRate(20);

//            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
//            if (supportedFocusModes == null) supportedFocusModes = new ArrayList<>();
//            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//            }
            mCamera.setParameters(parameters);
            Log.i(TAG, "setParameters");
            int displayRotation;
            displayRotation = (cameraRotationOffset - mDgree + 360) % 360;
            mCamera.setDisplayOrientation(displayRotation);

            Log.i(TAG, "setDisplayOrientation");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stack = sw.toString();
            destroyCamera();
            e.printStackTrace();
        }
    }

    private void save2file(byte[] data, String path) {
        if (true) return;
        try {
            FileOutputStream fos = new FileOutputStream(path, true);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    private int getTxtPixelLength(String txt, boolean zoomed) {
        int length = 0;
        int fontWidth = zoomed ? 16 : 8;
        for (int i = 0; i < txt.length(); i++) {
            length += isChinese(txt.charAt(i)) ? fontWidth * 2 : fontWidth;
        }
        return length;
    }

    private String tempFile;
    private String dataStr;
    private long millis;
    private String fileIndex;

    public synchronized void startRecord() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    startRecord();
                }
            });
            return;
        }
        logger.info(TAG + "---startRecord");
        //检测内存卡的size
        if (TextUtils.isEmpty(tempFile)) {
            FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
            operation.checkExternalUsableSize();
            dataStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            millis = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getInt("record_interval", VIDEO_RECODE_PER_TIME);
            fileIndex = FileTransgerUtil.getRecodeFileIndex(1);
            String fileName = FileTransgerUtil.getVideoRecodeFileName(dataStr, fileIndex);
            File videoRecord = new File(MyTerminalFactory.getSDK().getBITVideoRecordesDirectoty(operation.getExternalUsableStorageDirectory()), fileName);
            if (!videoRecord.exists()) {
                videoRecord.getParentFile().mkdirs();
            }
            tempFile = videoRecord.toString();
            mMuxer = new EasyMuxer(tempFile, millis);
            mMuxer.setmContext(mApplicationContext);
            mMuxer.setDateStr(dataStr, fileIndex);

            if (mVC == null || audioStream == null) {
                throw new IllegalStateException("you need to start preview before startRecord!");
            }
            logger.info(TAG + "---setMuxer");
            mVC.setMuxer(mMuxer);
            audioStream.setMuxer(mMuxer);
        }
    }


    public synchronized void stopRecord() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopRecord();
                }
            });
            return;
        }
        if (mVC == null || audioStream == null) {
//            nothing
        } else {
            mVC.setMuxer(null);
            audioStream.setMuxer(null);
        }
        MyTerminalFactory.getSDK().renovateVideoRecord(tempFile);
        if (mMuxer != null) mMuxer.release();
        mMuxer = null;
        tempFile = null;
    }

    /**
     * 开启预览
     */
    public synchronized void startPreview() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    startPreview();
                }
            });
            return;
        }
        preView = true;
        logger.info("------>>>>startPreview");
        if(audioStream == null){
            audioStream = new AudioStream();
        }
        if (mCamera != null) {
            int previewFormat = mCamera.getParameters().getPreviewFormat();
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(previewFormat) / 8;
            width = previewSize.width;
            height = previewSize.height;
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            Log.i(TAG, "setPreviewCallbackWithBuffer");

            if (Util.getSupportResolution(mApplicationContext).size() == 0) {
                StringBuilder stringBuilder = new StringBuilder();
                List<Camera.Size> supportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
                for (Camera.Size str : supportedPreviewSizes) {
                    stringBuilder.append(str.width + "x" + str.height).append(";");
                }
                Util.saveSupportResolution(mApplicationContext, stringBuilder.toString());
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveSupportResolutionHandler.class);
            }

            try {
                SurfaceTexture holder = mSurfaceHolderRef.get();
                if (holder != null) {
                    mCamera.setPreviewTexture(holder);
                    Log.i(TAG, "setPreviewTexture");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            mCamera.startPreview();
            Log.i(TAG, "startPreview");
            try {
                mCamera.autoFocus(null);
            } catch (Exception e) {
                //忽略异常
                Log.i(TAG, "auto foucus fail");
            }

            boolean rotate = false;
            if (mDgree == 0) {
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(mCameraId, camInfo);
                int cameraRotationOffset = camInfo.orientation;
                if (cameraRotationOffset == 90) {
                    rotate = true;
                } else if (cameraRotationOffset == 270) {
                    rotate = true;
                }
            }

            overlay = new TxtOverlay(mApplicationContext);
            try {

                if (mSWCodec) {
                    mVC = new SWConsumer(mApplicationContext, mEasyPusher);
                } else {
                    mVC = new HWConsumer(mApplicationContext, mEasyPusher);
                }

                if (!rotate) {
                    mVC.onVideoStart(previewSize.width, previewSize.height);
                    overlay.init(previewSize.width, previewSize.height, mApplicationContext.getFileStreamPath("SIMYOU.ttf").getPath(), 12);
                } else {
                    mVC.onVideoStart(previewSize.height, previewSize.width);
                    overlay.init(previewSize.height, previewSize.width, mApplicationContext.getFileStreamPath("SIMYOU.ttf").getPath(), 12);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        audioStream.addPusher(mEasyPusher);
    }

    @Provides
    @Nullable
    public EasyMuxer getMuxer() {
        return mMuxer;
    }


    Camera.PreviewCallback previewCallback;


    /**
     * 旋转YUV格式数据
     *
     * @param src    YUV数据
     * @param format 0，420P；1，420SP
     * @param width  宽度
     * @param height 高度
     * @param degree 旋转度数
     */
    private static void yuvRotate(byte[] src, int format, int width, int height, int degree) {
        int offset = 0;
        if (format == 0) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += (width * height);
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
            offset += width * height / 4;
            JNIUtil.rotateMatrix(src, offset, width / 2, height / 2, degree);
        } else if (format == 1) {
            JNIUtil.rotateMatrix(src, offset, width, height, degree);
            offset += width * height;
            JNIUtil.rotateShortMatrix(src, offset, width / 2, height / 2, degree);
        }
    }

    /**
     * 停止预览
     */
    public synchronized void stopPreview() {
        logger.info(Thread.currentThread().getName() + "--StopPreview");
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopPreview();
                }
            });
            return;
        }
        preView = false;
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);

            }
            if (audioStream != null) {
                audioStream.removePusher(mEasyPusher);
                audioStream = null;
                logger.info("Stop AudioStream");
            }
            if (mVC != null) {
                mVC.onVideoStop();
                logger.info("Stop VC");
            }
            if (overlay != null)
                overlay.release();
            if (mMuxer != null) {
                mMuxer.release();
                mMuxer = null;
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
    }

    public Camera getCamera() {
        return mCamera;
    }


    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        if (mCameraHandler.hasMessages(SWITCH_CAMERA)) return;
        mCameraHandler.sendEmptyMessage(SWITCH_CAMERA);
    }

    private Runnable switchCameraTask = new Runnable() {
        @Override
        public void run() {
            int cameraCount = 0;
            if (isCameraBack) {
                isCameraBack = false;
            } else {
                isCameraBack = true;
            }
            if (!enanleVideo) return;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
            stopPreview();
            destroyCamera();
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息

                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    //现在是后置，变更为前置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        createCamera();
                        startPreview();
                        break;
                    }
                } else {
                    //现在是前置， 变更为后置
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        createCamera();
                        startPreview();
                        break;
                    }
                }
            }
        }
    };

    private String recordPath = Environment.getExternalStorageDirectory().getPath();

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }

    /**
     * 销毁Camera
     */
    public synchronized void destroyCamera() {

        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    destroyCamera();
                }
            });
            return;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "release Camera");
            mCamera = null;
        }
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
        }
    }

    public boolean isStreaming() {
        return pushStream;
    }


    public void stopStream() {
        mEasyPusher.stop();
        pushStream = false;
    }

    public void setSurfaceTexture(SurfaceTexture texture) {
        mSurfaceHolderRef = new WeakReference<>(texture);
    }

    public void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mCameraThread.quitSafely();
        } else {
            if (!mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCameraThread.quit();
                }
            })) {
                mCameraThread.quit();
            }
        }
        try {
            mCameraThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isRecording() {
        return mMuxer != null;
    }

    public void ZoomOrReduceVideo(boolean isScale){
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.isZoomSupported()){
            int maxZoom = parameters.getMaxZoom();
            int currentZoom = parameters.getZoom();
            if(isScale && currentZoom < maxZoom-2){
                currentZoom+=2;
            }else if(currentZoom >1 && !isScale){
                currentZoom-=2;
            }
            parameters.setZoom(currentZoom);
            mCamera.setParameters(parameters);
        }else {
            ToastUtil.showToast(mApplicationContext,"摄像头不支持放大");
        }
    }

    public boolean isPreView(){
        return preView;
    }

    public static class CodecInfo {
        public String mName;
        public int mColorFormat;
    }

    public static ArrayList<CodecInfo> listEncoders(String mime) {
        // 可能有多个编码库，都获取一下。。。
        ArrayList<CodecInfo> codecInfos = new ArrayList<>();
        int numCodecs = MediaCodecList.getCodecCount();
        // int colorFormat = 0;
        // String name = null;
        for (int i1 = 0; i1 < numCodecs; i1++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i1);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            if (codecMatch(mime, codecInfo)) {
                String name = codecInfo.getName();
                int colorFormat = getColorFormat(codecInfo, mime);
                if (colorFormat != 0) {
                    CodecInfo ci = new CodecInfo();
                    ci.mName = name;
                    ci.mColorFormat = colorFormat;
                    codecInfos.add(ci);
                }
            }
        }
        return codecInfos;
    }

    public static boolean codecMatch(String mimeType, MediaCodecInfo codecInfo) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static int getColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        int[] cf = new int[capabilities.colorFormats.length];
        System.arraycopy(capabilities.colorFormats, 0, cf, 0, cf.length);
        List<Integer> sets = new ArrayList<>();
        for (int i = 0; i < cf.length; i++) {
            sets.add(cf[i]);
        }
        if (sets.contains(COLOR_FormatYUV420SemiPlanar)) {
            return COLOR_FormatYUV420SemiPlanar;
        } else if (sets.contains(COLOR_FormatYUV420Planar)) {
            return COLOR_FormatYUV420Planar;
        } else if (sets.contains(COLOR_FormatYUV420PackedPlanar)) {
            return COLOR_FormatYUV420PackedPlanar;
        } else if (sets.contains(COLOR_TI_FormatYUV420PackedSemiPlanar)) {
            return COLOR_TI_FormatYUV420PackedSemiPlanar;
        }
        return 0;
    }
}