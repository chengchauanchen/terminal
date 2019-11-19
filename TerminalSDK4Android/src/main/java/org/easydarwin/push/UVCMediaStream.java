package org.easydarwin.push;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Surface;

import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.UVCCamera;

import org.apache.log4j.Logger;
import org.easydarwin.audio.USBAudioStream;
import org.easydarwin.easypusher.BackgroundCameraService;
import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.sw.TxtOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import dagger.Module;
import dagger.Provides;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.FileTransgerUtil;

import static android.graphics.ImageFormat.NV21;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;

@Module
public class UVCMediaStream {
    Pusher mEasyPusher;
    static final String TAG = "UVCMediaStream";
    private int width = 640, height = 480;
    //录制每个视频片段的时间
    private static final int VIDEO_RECODE_PER_TIME = 5 * 60 * 1000;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private WeakReference<SurfaceTexture> mSurfaceHolderRef;
    private UVCCamera uvcCamera;

    private USBAudioStream audioStream;
    private int mDgree;
    private Context mApplicationContext;
    private boolean mSWCodec;
    private USBVideoConsumer mVC;
    private TxtOverlay overlay;
    private EasyMuxer mMuxer;
    private final HandlerThread mCameraThread;
    private final Handler mCameraHandler;
    private Logger logger = Logger.getLogger(getClass());
    private boolean pushStream = false;//是否要推送数据
    private boolean preView = false;
    private boolean cameraOpen = false;

    public UVCMediaStream(Context context, SurfaceTexture texture) {
        mApplicationContext = context;
        mSurfaceHolderRef = new WeakReference<>(texture);
        mEasyPusher = new EasyPusher();
        mCameraThread = new HandlerThread("CAMERA") {
            public void run() {
                try {
                    super.run();
                } catch (Throwable e) {
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
            }
        };
        uvcFrameCallback = new IFrameCallback() {
            @Override
            public void onFrame(ByteBuffer frame) {
                if (uvcCamera == null)
                    return;
                Thread.currentThread().setName("UVCCamera");
                frame.clear();
                byte[] data = cache.poll();
                if (data == null) {
                    data = new byte[frame.capacity()];
                }
                frame.get(data);
                onPreviewFrame2(data, uvcCamera);
            }
        };
    }

    private BlockingQueue<byte[]> bufferQueue = new ArrayBlockingQueue<>(10);
    public static UVCMediaStream.CodecInfo info = new UVCMediaStream.CodecInfo();
    private BlockingQueue<byte[]> cache = new ArrayBlockingQueue<>(100);

    public void onPreviewFrame2(byte[] data, Object camera) {

        if (PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getBoolean("key_enable_video_overlay", true)) {
            String txt;// = String.format("drawtext=fontfile=" + mApplicationContext.getFileStreamPath("SIMYOU.ttf") + ": text='%s%s':x=(w-text_w)/2:y=H-60 :fontcolor=white :box=1:boxcolor=0x00000000@0.3", "EasyPusher", new SimpleDateFormat("yyyy-MM-ddHHmmss").format(new Date()));
            txt = " " + new SimpleDateFormat("yy-MM-dd HH:mm:ss SSS").format(new Date());
            //             overlay.overlay(data, txt);
        }
        mVC.onVideo(data, NV21);
    }

    public void startStream(String url, InitCallback callback) {
        mEasyPusher.initPush(url, mApplicationContext, callback, ~0);
        pushStream = true;
    }

    public void startStream(String ip, String port, String id, InitCallback callback) {
        mEasyPusher.initPush(mApplicationContext, callback);
        mEasyPusher.setMediaInfo(Pusher.Codec.EASY_SDK_VIDEO_CODEC_H264, 25, Pusher.Codec.EASY_SDK_AUDIO_CODEC_AAC, 1, 8000, 16);
        mEasyPusher.start(ip, port, String.format("%s", id), Pusher.TransType.EASY_RTP_OVER_TCP);
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
//        if (uvcCamera == null) return;
//        stopPreview();
//        destroyCamera();
//        createCamera();
//        startPreview();
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
        cameraOpen = true;
        mSWCodec = PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getBoolean("key-sw-codec", false);

        ArrayList<UVCMediaStream.CodecInfo> infos = listEncoders("video/avc");
        if (infos.isEmpty())
            mSWCodec = true;
        if (mSWCodec) {
        } else {
            UVCMediaStream.CodecInfo ci = infos.get(0);
            info.mName = ci.mName;
            info.mColorFormat = ci.mColorFormat;
        }
        UVCCamera value = UVCCameraService.liveData.getValue();
        if (value != null) {
            // uvc camera.
            uvcCamera = value;

            value.setPreviewSize(width, height, 1, 30, UVCCamera.PIXEL_FORMAT_NV21, 1.0f);

            //            value.startPreview();
        } else {
            logger.error("NO UVCCamera");
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
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
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
        if (preView) {
            return;
        }
        preView = true;
        UVCCamera value = uvcCamera;
        if (value != null) {
            SurfaceTexture holder = mSurfaceHolderRef.get();
            if (holder != null) {
                value.setPreviewTexture(holder);
            }
            try {
                value.setFrameCallback(uvcFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP/*UVCCamera.PIXEL_FORMAT_NV21*/);
                value.startPreview();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        overlay = new TxtOverlay(mApplicationContext);
        try {
            if (mSWCodec) {
                mVC = new USBSWConsumer(mApplicationContext, mEasyPusher);
            } else {
                mVC = new USBHWConsumer(mApplicationContext, mEasyPusher);
            }
            mVC.onVideoStart(width, height);
            overlay.init(width, height, mApplicationContext.getFileStreamPath("SIMYOU.ttf").getPath(), 12);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        if (audioStream == null) {
            audioStream = new USBAudioStream(mEasyPusher);
        }
        audioStream.startRecord();
    }

    @Provides
    @Nullable
    public EasyMuxer getMuxer() {
        return mMuxer;
    }

    private final IFrameCallback uvcFrameCallback;

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
        logger.info(Thread.currentThread().getName() + "---stopPreview");
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopPreview();
                }
            });
            return;
        }
        if (!preView) {
            return;
        }
        preView = false;

        try {
            UVCCamera value = uvcCamera;
            logger.info("uvcCamera:" + value);
            if (null != value) {
//                value.stopPreview();
                logger.info("uvcCamera.stopPreview()");
            }
            if (audioStream != null) {
                logger.info("audioStream.stop()");
                audioStream.stop();
                audioStream = null;
            }
            if (mVC != null) {
                mVC.onVideoStop();
                logger.info("Stop VC");
            }
            if (overlay != null)
                overlay.release();
            if (mMuxer != null) {
//                mMuxer.release();
//                mMuxer = null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public UVCCamera getCamera() {
        return uvcCamera;
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
        if (!cameraOpen) {
            return;
        }
        logger.info("destroyCamera");
        cameraOpen = false;
        UVCCamera value = uvcCamera;
        if (value != null) {
            //            value.destroy();
            uvcCamera = null;
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

    public void setSurfaceTexture(final SurfaceTexture texture) {
        mSurfaceHolderRef = new WeakReference<>(texture);
        if (texture == null) {
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (uvcCamera != null) {
                        uvcCamera.setPreviewDisplay((Surface) null);
                    } else {
                        stopPreview();
                    }
                }
            });
            mSurfaceHolderRef = null;
        } else {
            mSurfaceHolderRef = new WeakReference<>(texture);
            mCameraHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (uvcCamera != null) {
                        uvcCamera.setPreviewDisplay(new Surface(texture));
                    } else {
                        stopPreview();
                    }
                }
            });
        }
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

    public static class CodecInfo {
        String mName;
        int mColorFormat;
    }

    private static ArrayList<CodecInfo> listEncoders(String mime) {
        // 可能有多个编码库，都获取一下。。。
        ArrayList<CodecInfo> codecInfos = new ArrayList<>();
        int numCodecs = MediaCodecList.getCodecCount();
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

    private static int getColorFormat(MediaCodecInfo codecInfo, String mimeType) {
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