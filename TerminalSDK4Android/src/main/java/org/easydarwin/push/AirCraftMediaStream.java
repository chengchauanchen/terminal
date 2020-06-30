package org.easydarwin.push;

import android.content.Context;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.log4j.Logger;
import org.easydarwin.audio.UAVAudioStream;
import org.easydarwin.easypusher.BackgroundCameraService;
import org.easydarwin.muxer.UAVEasyMuxer;
import org.easydarwin.sw.JNIUtil;
import org.easydarwin.sw.TxtOverlay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import dagger.Module;
import dagger.Provides;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.AppUtil;
import ptt.terminalsdk.tools.FileTransgerUtil;

import static android.graphics.ImageFormat.NV21;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
import static android.media.MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar;

@Module
public class AirCraftMediaStream {
    Pusher mEasyPusher;
    static final String TAG = "AirCraftMediaStream";
    int width = 640, height = 480;
    boolean pushStream = false;//是否要推送数据
    private Context mApplicationContext;
    private boolean mSWCodec;
    private UAVVideoConSumer mVC;
    private TxtOverlay overlay;
    private UAVEasyMuxer mMuxer;
    private UAVAudioStream audioStream ;
    public static AirCraftMediaStream.CodecInfo info = new AirCraftMediaStream.CodecInfo();
    public Logger logger = Logger.getLogger(AirCraftMediaStream.class);
    private final HandlerThread mCameraThread;
    private final Handler mCameraHandler;
    //录制每个视频片段的时间
    private static final int VIDEO_RECODE_PER_TIME = 5 * 60 * 1000;

    public AirCraftMediaStream(Context context) {
        mApplicationContext = context;
        mEasyPusher = new EasyPusher();
        mCameraThread = new HandlerThread("CAMERA") {
            @Override
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
    }

    public static ArrayList<CodecInfo> listEncoders(String mime) {
        // 可能有多个编码库，都获取一下。。。
        ArrayList<CodecInfo> codecInfos = new ArrayList<CodecInfo>();
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
                    AirCraftMediaStream.CodecInfo ci = new AirCraftMediaStream.CodecInfo();
                    ci.mName = name;
                    ci.mColorFormat = colorFormat;
                    codecInfos.add(ci);
                }
            }
        }
        return codecInfos;
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

    public static boolean codecMatch(String mimeType, MediaCodecInfo codecInfo) {
        String[] types = codecInfo.getSupportedTypes();
        for (String type : types) {
            if (type.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public void startStream(String url, InitCallback callback) {
        mEasyPusher.initPush(url, mApplicationContext, callback, ~0);
        pushStream = true;
    }

    public void startStream(String ip, String port, String id, InitCallback callback) {
        mEasyPusher.initPush( mApplicationContext, callback);
        mEasyPusher.setMediaInfo(Pusher.Codec.EASY_SDK_VIDEO_CODEC_H264, 25, Pusher.Codec.EASY_SDK_AUDIO_CODEC_AAC, 1, 8000, 16);

        String sdp = TerminalFactory.getSDK().getLiveManager().getLivePathSdp();
        mEasyPusher.start(ip, port, String.format("%s"+sdp, id), Pusher.TransType.EASY_RTP_OVER_TCP);
        pushStream = true;
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

    private String tempFile;
    private String dataStr;
    private long millis;
    private String fileIndex;

    public synchronized void startRecord() {
        if (Thread.currentThread() != mCameraThread) {
            mCameraHandler.post(() -> startRecord());
            return;
        }
        if(isRecording()){
            stopRecord();
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
            mMuxer = new UAVEasyMuxer(tempFile, millis);
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
            mCameraHandler.post(() -> stopRecord());
            return;
        }
        if (mVC == null || audioStream == null) {
            //            nothing
        } else {
            mVC.setMuxer(null);
            audioStream.setMuxer(null);
        }
        MyTerminalFactory.getSDK().renovateVideoRecord(tempFile);
        if (mMuxer != null){
            mMuxer.release();
        }
        mMuxer = null;
        tempFile = null;
    }


    @Provides
    @Nullable
    public UAVEasyMuxer getMuxer() {
        return mMuxer;
    }


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


    private String recordPath = Environment.getExternalStorageDirectory().getPath();

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }


    public boolean isStreaming() {
        return pushStream;
    }

    public void stopStream() {
        if (audioStream != null) {
            audioStream.removePusher(mEasyPusher);
            audioStream = null;
            logger.info("Stop AudioStream");
        }
        if (mVC != null) {
            mVC.onVideoStop();

            logger.info( "Stop VC");
        }
        if (overlay != null){
            overlay.release();
        }
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
        }
        mEasyPusher.stop();
        pushStream = false;

    }

    public boolean isRecording() {
        return mMuxer != null;
    }
    private long firstTime  = 0;
    String path = TerminalFactory.getSDK().getLogDirectory()+"yuv.txt";
    public void push(byte[] data, int width, int height){
        this.width = width;
        this.height = height;

        if (PreferenceManager.getDefaultSharedPreferences(mApplicationContext).getBoolean("key_enable_video_overlay", false)) {
//            String txt = String.format("drawtext=fontfile=" + mApplicationContext.getFileStreamPath("SIMYOU.ttf") + ": text='%s%s':x=(w-text_w)/2:y=H-60 :fontcolor=white :box=1:boxcolor=0x00000000@0.3", "EasyPusher", new SimpleDateFormat("yyyy-MM-ddHHmmss").format(new Date()));
            String txt = "4GPTT " + new SimpleDateFormat("yy-MM-dd HH:mm:ss SSS").format(new Date());
            //叠加水印
//            overlay.overlay(data, txt);

        }
        overLayBatteryDbm(data,mApplicationContext,12);
        mVC.onVideo(data, NV21);

    }

    //添加电量信号状态水印
    public void overLayBatteryDbm(byte[] data, Context context, int textSize){
        String status = AppUtil.getDbmStatusStr(context);
        int strWidth = this.width - AppUtil.getStrWidth(status, textSize) - 40;
        if (strWidth < 0){
            strWidth = 0;
        }
        overlay.overlay(data, status, strWidth,0);
    }

    public void startPreView(int width,int height){
        audioStream = new UAVAudioStream();
        ArrayList<CodecInfo> infos = listEncoders("video/avc");
        for(CodecInfo info : infos){
            logger.info("硬编码库："+"info.mName:"+info.mName+"---info.mColorFormat:"+info.mColorFormat);
        }
        if (!infos.isEmpty()) {
            AirCraftMediaStream.CodecInfo ci = infos.get(0);
            info.mName = ci.mName;
            info.mColorFormat = ci.mColorFormat;
            logger.info("info.mName:"+info.mName+"---info.mColorFormat:"+info.mColorFormat);
        }else {
            mSWCodec = true;
        }
        logger.info("mSWCodec:"+mSWCodec);
        overlay = new TxtOverlay(mApplicationContext);
        try {
            if (mSWCodec) {
                mVC = new AirSWConsumer(mApplicationContext, mEasyPusher);
            } else {
                mVC = new AirHWConsumer(mApplicationContext, mEasyPusher);
            }
            mVC.onVideoStart(width,height);

            overlay.init(width, height, mApplicationContext.getFileStreamPath("SIMYOU.ttf").getPath(), 12);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        audioStream.addPusher(mEasyPusher);
    }

    public static class CodecInfo {
        public String mName;
        public int mColorFormat;
    }
}