package cn.vsx.yuv;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import cn.vsx.uav.R;
import dji.common.product.Model;
import dji.log.DJILog;
import dji.midware.data.model.P3.DataCameraGetPushStateInfo;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class YuvPlayer {

    // All the supported YUV format
    public static final int CV_FMT_YU12     = 0;
    public static final int CV_FMT_YV12     = 1;
    public static final int CV_FMT_NV21     = 2;
    public static final int CV_FMT_NV12     = 3;
    public static final int CV_FMT_YUYV422  = 4;
    public static final int CV_FMT_YVYU422  = 5;
    public static final int CV_FMT_YUV422P  = 6;
    public static final int CV_FMT_UYVY422  = 7;
    public static final int CV_FMT_GRAY8    = 8;
    public static final int CV_FMT_RGB565   = 9;
    public static final int CV_FMT_RGB888   = 10;
    public static final int CV_FMT_ARGB8888 = 11;
    public static final int CV_FMT_ABGR8888 = 12;
    public static final int CV_FMT_BGRA8888 = 13; // equals to Android Config.ARGB_8888
    private static final String TAG = YuvPlayer.class.getSimpleName();
    static {
        System.loadLibrary("myYuvPlayer");
    }

    private Context context;

    public YuvPlayer(Context context){
        this.context = context;
    }

    private class YuvData{
        byte[] data;
        int width;
        int height;
        private YuvData(){ }
    }

    enum Cookie{PLAY, DUAL_OUTER}
    class YuvDataObjectPool extends ObjectPool<YuvData>{

        private HashMap<YuvData, HashMap<Cookie, Boolean>> releasedStatus;

        public YuvDataObjectPool(int capacity) {
            super(capacity);
        }

        @Override
        protected YuvData createObject() {
            YuvData yuvData = new YuvData();
            HashMap<Cookie, Boolean> hashMap = new HashMap<>(Cookie.values().length);
            for (int i = 0; i < Cookie.values().length ; i++){
                hashMap.put(Cookie.values()[i], true);
            }
            if(releasedStatus == null){
                releasedStatus = new HashMap<>(capacity);
            }
            releasedStatus.put(yuvData, hashMap);
            return yuvData;
        }

        @Override
        public synchronized YuvData getObject() {
            YuvData yuvData = super.getObject();
            if(yuvData != null) {
                HashMap<Cookie, Boolean> hashMap = releasedStatus.get(yuvData);
                if (hashMap != null) {
                    for (int i = 0; i < Cookie.values().length ; i++){
                        hashMap.put(Cookie.values()[i], false);
                    }
                }
            }
            return yuvData;
        }

        public void tryRelease(YuvData yuvData, Cookie cookie){
            if(yuvData != null && cookie != null){
                synchronized (this){
                    HashMap<Cookie, Boolean> hashMap = releasedStatus.get(yuvData);
                    if (hashMap != null) {
                        hashMap.put(cookie, true);
                        boolean ret = true;
                        for (int i = 0; i < Cookie.values().length ; i++){
                            if(!hashMap.get(Cookie.values()[i])){
                                ret = false;
                                break;
                            }
                        }
                        if(ret){
//                            System.out.println("releaseObject yuvData: " + yuvData);
                            releaseObject(yuvData);
                        }
                    }
                }
            }
        }
    };

    private class PlayYuvDataTask implements Runnable{
        int yuvType;
        int playFps;
        private long startPlayTime;
        private long remainTime;
        ArrayBlockingQueue<YuvData> yuvDataArrayBlockingQueue = new ArrayBlockingQueue<>(100);
        public void _init(){
            tempCalcValue = (realFps <= playFps + 1) ? Integer.MAX_VALUE : (realFps / (realFps - playFps));
            if (tempCalcValue < 2) {
                tempCalcValue = 2;
            }
            playFps = realFps - realFps / tempCalcValue;
            System.out.println("tempCalcValue = " + tempCalcValue + ", playFps = " + playFps);
        }
        @Override
        public void run() {
            try {
                int mspf = 1000 / playFps;
                YuvData yuvData;
                setWidthHeight(surfaceWidth, surfaceHeight);
                while ((yuvData = yuvDataArrayBlockingQueue.take()) != null){
                    try {
                        remainTime = mspf - (System.currentTimeMillis() - startPlayTime);
                        if (remainTime > 0) {
//                            System.out.println("PlayYuvDataTask sleep " + remainTime + "ms");
                            Thread.sleep(remainTime);
                        }
                        startPlayTime = System.currentTimeMillis();
                        play(yuvData.data, yuvData.width, yuvData.height);
                    }
                    finally {
                        yuvDataObjectPool.tryRelease(yuvData, Cookie.PLAY);
                    }
                }
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            finally {
                close();
            }
        }

        private long frameIndex = 0;
        private int tempCalcValue = -1;
        public void inQueue(YuvData yuvData){
            if(yuvData != null) {
                //降帧操作，将realFps帧降为playFps帧
                frameIndex++;
                if(frameIndex % tempCalcValue != 0) {
                    if (!yuvDataArrayBlockingQueue.offer(yuvData)) {
                        System.err.println("PlayYuvDataTask yuvDataArrayBlockingQueue is full");
                    }
                }
                else {
                    yuvDataObjectPool.tryRelease(yuvData, Cookie.PLAY);
                }
            }
        }
    }

    private class DualOuterYuvDataTask implements Runnable{
        private ArrayBlockingQueue<YuvData> yuvDataArrayBlockingQueue = new ArrayBlockingQueue<>(100);
//        private long realFrameIndex;
//        private long startTime;
//        private float nowRealFrameTime;
        @Override
        public void run() {
            YuvData yuvData;
            try {
                while ((yuvData = yuvDataArrayBlockingQueue.take()) != null){
                    try {
                        if (yuvDataListener != null) {
//                            startTime = System.currentTimeMillis();
                            yuvDataListener.onDataRecv(yuvData.data, yuvData.width, yuvData.height);
//                            if(System.currentTimeMillis() - startTime > 0) {
//                                nowRealFrameTime = (nowRealFrameTime * realFrameIndex + (System.currentTimeMillis() - startTime)) / (realFrameIndex + 1);
//                                System.out.println("outer Data frame time = " + nowRealFrameTime + ", " + (System.currentTimeMillis() - startTime) + ", " + realFrameIndex);
//                                realFrameIndex++;
//                            }
                        }
                    }
                    finally {
                        yuvDataObjectPool.tryRelease(yuvData, Cookie.DUAL_OUTER);
                    }
                }
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        private long frameIndex = 0;
        private void inQueue(YuvData yuvData){
            //降帧操作，将32帧降为16帧
            frameIndex++;
            if(frameIndex % 2 != 0) {
                if (!yuvDataArrayBlockingQueue.offer(yuvData)) {
                    System.err.println("DualOuterYuvDataTask yuvDataArrayBlockingQueue is full");
                }
            }
            else {
                yuvDataObjectPool.tryRelease(yuvData, Cookie.DUAL_OUTER);
            }
        }
    }

    private MediaCodec.BufferInfo bufferInfo;

    private class DecodeH264DataTask implements Runnable{
//        private long realFrameIndex;
//        private long startTime;
//        private float nowRealFrameTime;
        @Override
        public void run() {
            int outIndex;
            YuvData yuvData;
            try {
                while (true) {
                    if(mediaCodec == null){
                        Thread.sleep(1000);
                    }
                    try {
                        outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 1000);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        break;
                    }
                    if (outIndex >= 0) {
//                        Log.d(TAG, "decodeFrame: outIndex: " + outIndex);
                        ByteBuffer yuvDataBuf;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            yuvDataBuf = mediaCodec.getOutputBuffer(outIndex);
                        } else {
                            yuvDataBuf = mediaCodec.getOutputBuffers()[outIndex];
                        }
                        yuvDataBuf.position(bufferInfo.offset);
                        yuvDataBuf.limit(bufferInfo.size - bufferInfo.offset);

//                        if(startTime != 0 && (System.currentTimeMillis() - startTime) > 0){
//                            nowRealFrameTime = (nowRealFrameTime * realFrameIndex + (System.currentTimeMillis() - startTime)) / (realFrameIndex + 1);
//                            System.out.println("real Data frame time = " + nowRealFrameTime + ", " + (System.currentTimeMillis() - startTime) + ", " + realFrameIndex);
//                            realFrameIndex++;
//                        }
//                        startTime = System.currentTimeMillis();

//                        System.out.println("yuvDataBuf, size = " + yuvDataBuf.remaining() + ", videoWidth = " + videoWidth + ", videoHeight = " + videoHeight);
                        yuvData = yuvDataObjectPool.getObject();
                        if(yuvData != null){
                            if(yuvData.data == null){
                                yuvData.data = new byte[videoYuvSize];
                            }
                            yuvDataBuf.get(yuvData.data);
                            yuvData.width = videoWidth;
                            yuvData.height = videoHeight;
                            dualOuterYuvDataTask.inQueue(yuvData);
                            push(yuvData);
                        }
                        else {
                            System.err.println("yuvDataObjectPool is full");
                        }
                        // All the output buffer must be release no matter whether the yuv data is output or
                        // not, so that the codec can reuse the buffer.
                        try{
                            mediaCodec.releaseOutputBuffer(outIndex, true);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    } else {
//                        System.out.println("outIndex = " + outIndex);
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private class H264Data{
        byte[] h264Data;
        int size;
        private H264Data(){ }
    }

    private class ParseH264DataTask implements Runnable{
        ArrayBlockingQueue<H264Data> h264DataArrayBlockingQueue = new ArrayBlockingQueue<>(100);
        ObjectPool<H264Data> h264DataObjectPool = new ObjectPool<H264Data>(101) {
            @Override
            protected H264Data createObject() {
                return new H264Data();
            }

            @Override
            protected void releaseT(H264Data h264Data) {
                h264Data.h264Data = null;
            }
        };
        Surface surface;
        @Override
        public void run() {
            H264Data h264Data;
            byte[] outBuff = new byte[2097152];
            try {
                init(surface);
                while ((h264Data = h264DataArrayBlockingQueue.take()) != null){
                    try {
                        parse(h264Data.h264Data, h264Data.size, outBuff);
                    }
                    finally {
                        h264DataObjectPool.releaseObject(h264Data);
                    }
                }
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        public void inQueue(byte[] data, int size){
            H264Data h264Data = h264DataObjectPool.getObject();
            if(h264Data != null) {
                h264Data.h264Data = data;
                h264Data.size = size;
                if(!h264DataArrayBlockingQueue.offer(h264Data)){
                    System.err.println("h264DataArrayBlockingQueue is full");
                }
            }
            else {
                System.err.println("h264DataObjectPool is full");
            }
        }
    }

    private final int realFps = 32;
    private Thread playYuvDataThread;
    private PlayYuvDataTask playYuvDataTask;
    private int surfaceWidth;
    private int surfaceHeight;
    private int videoWidth;
    private int videoHeight;
    private int videoYuvSize;
    private MediaCodec mediaCodec;
    private Thread decodeH264DataThread;
    private DecodeH264DataTask decodeH264DataTask;
    private YuvDataObjectPool yuvDataObjectPool;
    private Thread parseH264DataThread;
    private ParseH264DataTask parseH264DataTask;
    private DualOuterYuvDataTask dualOuterYuvDataTask;
    private Thread dualOuterYuvDataThread;

    /**
     * 启动播放，准备播放资源
     * @param width 图像宽度
     * @param height 图像高度
     * @param playFps 播放时使用的图像帧率
     * @param surface 图像显示的地方
     */
    public void start(int width, int height, int playFps, Surface surface){
        System.out.println("YuvPlayer start");
        if(width <= 0 || height <= 0 || playFps <= 0 || surface == null){
            throw new IllegalArgumentException("参数错误");
        }
        //播放线程
        if(playYuvDataThread != null){
            stop();
        }

        yuvDataObjectPool = new YuvDataObjectPool(101);

        playYuvDataTask = new PlayYuvDataTask();
        playYuvDataTask.playFps = playFps;
        surfaceWidth = width;
        surfaceHeight = height;
        playYuvDataTask.yuvType = CV_FMT_NV12;
        playYuvDataTask._init();
        playYuvDataThread = new Thread(playYuvDataTask);
        playYuvDataThread.setDaemon(true);
        playYuvDataThread.start();

        //拆分H264数据线程
        parseH264DataTask = new ParseH264DataTask();
        parseH264DataTask.surface = surface;
        parseH264DataThread = new Thread(parseH264DataTask);
        parseH264DataThread.setDaemon(true);
        parseH264DataThread.start();
    }

    public interface YuvDataListener {
        /**
         * Callback method for receiving the yuv data.
         * @param data
         * @param width
         * @param height
         */
        void onDataRecv(byte[] data, int width, int height);
    }

    /**
     * Set the yuv frame data receiving callback. The callback method will be invoked when the decoder
     * output yuv frame data. What should be noted here is that the hardware decoder would not output
     * any yuv data if a surface is configured into, which mean that if you want the yuv frames, you
     * should set "null" surface when calling the "configure" method of MediaCodec.
     * @param yuvDataListener
     */
    public void setYuvDataListener(YuvDataListener yuvDataListener) {
        this.yuvDataListener = yuvDataListener;
    }

    private YuvDataListener yuvDataListener;

    /**
     * 将一帧图像推送至播放端
     * @param yuvData 推送的图像数据
     */
    public void push(YuvData yuvData){
        if(yuvData == null || yuvData.data == null || yuvData.data.length < (yuvData.width * yuvData.height * 3 / 2)){
            throw new IllegalArgumentException("参数错误");
        }
        if(playYuvDataThread != null && playYuvDataTask != null){
            playYuvDataTask.inQueue(yuvData);
        }
    }
    /**
     * 处理一条h264数据进行切包
     * @param buf
     * @param size
     * @return
     */
    public void parseH264(byte[] buf, int size){
//        System.out.println("parseH264, size = " + size);
        if(buf == null || buf.length < size || size < 0){
            throw new IllegalArgumentException("参数错误");
        }
        if(parseH264DataThread != null && parseH264DataTask != null){
            parseH264DataTask.inQueue(buf, size);
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        System.out.println("YuvPlayer stop");
        if(parseH264DataThread != null){
            parseH264DataThread.interrupt();
            try{
                parseH264DataThread.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            parseH264DataThread = null;
            parseH264DataTask = null;
        }

        stopDecodeH264Thread();

        if(playYuvDataThread != null) {
            playYuvDataThread.interrupt();
            try {
                playYuvDataThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            playYuvDataThread = null;
            playYuvDataTask = null;
        }

        yuvDataObjectPool = null;
    }

    private void stopDecodeH264Thread(){

        System.out.println("DecodeH264Thread stop");
        if(decodeH264DataThread != null) {
            decodeH264DataThread.interrupt();
            if(mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
            }
            try {
                decodeH264DataThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            decodeH264DataThread = null;
            decodeH264DataTask = null;
            mediaCodec = null;
        }
    }

    public void setSufaceWidthHeight(int width, int height){
        setWidthHeight(width, height);
    }

    /**
     * 初始化surface
     * @param surface
     */
    private native void init(Surface surface);

    /**
     * 初始化宽高
     * @param width
     * @param height
     */
    private synchronized native void setWidthHeight(int width, int height);

    /**
     * 播放一帧数据
     * @param yuvData
     * @param width
     * @param height
     */
    private synchronized native void play(byte[] yuvData, int width, int height);

    /**
     * 推送一条h264数据进行切包
     * @param buf
     * @param size
     * @return
     */
    public native void parse(byte[] buf, int size, byte[] outBuff);

    private boolean iFrameGot = false;


    /**
     * Get the resource ID of the IDR frame.
     * @param pModel Product model of connecting DJI product.
     * @param width Width of current video stream.
     * @return Resource ID of the IDR frame
     */
    public int getIframeRawId(Model pModel, int width, int height) {
        int iframeId = R.raw.iframe_1280x720_ins;

        switch(pModel) {
            case PHANTOM_3_ADVANCED:
            case PHANTOM_3_STANDARD:
                if (width == 960) {
                    //for photo mode, 960x720, GDR
                    iframeId = R.raw.iframe_960x720_3s;
                } else if (width == 640){
                    iframeId = R.raw.iframe_640x368_osmo_gop;
                } else {
                    //for record mode, 1280x720, GDR
                    iframeId = R.raw.iframe_1280x720_3s;
                }
                break;
            case INSPIRE_1: {
                DataCameraGetPushStateInfo.CameraType cameraType = DataCameraGetPushStateInfo.getInstance().getCameraType();
                if (cameraType == DataCameraGetPushStateInfo.CameraType.DJICameraTypeCV600) { //ZENMUSE_Z3
                    if (width == 960) {
                        //for photo mode, 960x720, GDR
                        iframeId = R.raw.iframe_960x720_3s;
                    } else if (width == 640){
                        iframeId = R.raw.iframe_640x368_osmo_gop;
                    } else {
                        //for record mode, 1280x720, GDR
                        iframeId = R.raw.iframe_1280x720_3s;
                    }
                }
                break;
            }
            case Phantom_3_4K:
                switch(width) {
                    case 640:
                        //for P3-4K with resolution 640*480
                        iframeId = R.raw.iframe_640x480;
                        break;
                    case 848:
                        //for P3-4K with resolution 848*480
                        iframeId = R.raw.iframe_848x480;
                        break;
                    case 896:
                        iframeId = R.raw.iframe_896x480;
                        break;
                    case 960:
                        //DJILog.i(TAG, "Selected Iframe=iframe_960x720_3s");
                        //for photo mode, 960x720, GDR
                        iframeId = R.raw.iframe_960x720_3s;
                        break;
                    default:
                        iframeId = R.raw.iframe_1280x720_3s;
                        break;
                }
                break;
            case OSMO:
                if (DataCameraGetPushStateInfo.getInstance().getVerstion() >= 4) {
                    iframeId = -1;
                } else {
                    iframeId = R.raw.iframe_1280x720_ins;
                }
                break;
            case OSMO_PLUS:
                if (width == 960) {
                    iframeId = R.raw.iframe_960x720_osmo_gop;
                } else if (width==1280) {
                    //for record mode, 1280x720, GDR
                    //DJILog.i(TAG, "Selected Iframe=iframe_1280x720_3s");
                    //                    iframeId = R.raw.iframe_1280x720_3s;
                    iframeId = R.raw.iframe_1280x720_osmo_gop;
                } else if (width == 640){
                    iframeId = R.raw.iframe_640x368_osmo_gop;
                } else {
                    iframeId = R.raw.iframe_1280x720_3s;
                }
                break;
            case OSMO_PRO:
            case OSMO_RAW:
                iframeId = R.raw.iframe_1280x720_ins;
                break;
            case MAVIC_PRO: //product small drone
            case MAVIC_2:
                if (((Aircraft) DJISDKManager.getInstance().getProduct()).getMobileRemoteController() != null) {
                    iframeId = R.raw.iframe_1280x720_wm220;
                } else{
                    iframeId = -1;
                }
                break;
            case Spark:
                switch (width) {
                    case 1280:  // 与P4相同
                        iframeId = R.raw.iframe_1280x720_p4;
                        break;
                    case 1024:
                        iframeId = R.raw.iframe_1024x768_wm100;
                        break;
                    default:
                        iframeId = R.raw.iframe_1280x720_p4;
                        break;
                }
                break;
            case MAVIC_AIR:
                switch (height) {
                    case 960:
                        iframeId = R.raw.iframe_1280x960_wm230;
                        break;
                    case 720:
                        iframeId = R.raw.iframe_1280x720_wm230;
                        break;
                    default:
                        iframeId = R.raw.iframe_1280x720_wm230;
                        break;
                }
                break;
            case PHANTOM_4:
                iframeId = R.raw.iframe_1280x720_p4;
                break;
            case PHANTOM_4_PRO: // p4p
            case PHANTOM_4_ADVANCED: // p4p
                switch (width) {
                    case 1280:
                        iframeId = R.raw.iframe_p4p_720_16x9;
                        break;
                    case 960:
                        iframeId = R.raw.iframe_p4p_720_4x3;
                        break;
                    case 1088:
                        iframeId = R.raw.iframe_p4p_720_3x2;
                        break;
                    case 1344:
                        iframeId = R.raw.iframe_p4p_1344x720;
                        break;
                    case 1440:
                        iframeId = R.raw.iframe_1440x1088_wm620;
                        break;
                    case 1920:
                        switch (height) {
                            case 1024:
                                iframeId = R.raw.iframe_1920x1024_wm620;
                                break;
                            case 800:
                                iframeId = R.raw.iframe_1920x800_wm620;
                                break;
                            default:
                                iframeId = R.raw.iframe_1920x1088_wm620;
                                break;
                        }
                        break;
                    default:
                        iframeId = R.raw.iframe_p4p_720_16x9;
                        break;
                }
                break;
            case MATRICE_600:
            case MATRICE_600_PRO: {
                DataCameraGetPushStateInfo.CameraType cameraType = DataCameraGetPushStateInfo.getInstance().getCameraType();
                if (width == 720 && height == 480) {
                    iframeId = R.raw.iframe_720x480_m600;
                } else if (width == 720 && height == 576) {
                    iframeId = R.raw.iframe_720x576_m600;
                } else {
                    if (width == 1280 && height == 720) {
                        if (cameraType == DataCameraGetPushStateInfo.CameraType.DJICameraTypeGD600) {
                            iframeId = R.raw.iframe_gd600_1280x720;
                        } else if (cameraType == DataCameraGetPushStateInfo.CameraType.DJICameraTypeCV600) {
                            iframeId = R.raw.iframe_1280x720_osmo_gop;
                        } else if (cameraType == DataCameraGetPushStateInfo.CameraType.DJICameraTypeFC350) {
                            iframeId = R.raw.iframe_1280x720_ins;
                        } else {
                            iframeId = R.raw.iframe_1280x720_m600;
                        }
                    } else if (width == 1920 && (height == 1080 || height == 1088)) {
                        iframeId = R.raw.iframe_1920x1080_m600;
                    } else if (width == 1080 && height == 720) {
                        iframeId = R.raw.iframe_1080x720_gd600;
                    } else if (width == 960 && height == 720) {
                        iframeId = R.raw.iframe_960x720_3s;
                    } else {
                        iframeId = -1;
                    }
                }
                break;
            }
            case MATRICE_100: {
                DataCameraGetPushStateInfo.CameraType cameraType = DataCameraGetPushStateInfo.getInstance().getCameraType();
                if (cameraType == DataCameraGetPushStateInfo.CameraType.DJICameraTypeGD600) {

                    if (width == 1280 && height == 720){
                        iframeId = R.raw.iframe_gd600_1280x720;
                    }else {
                        iframeId = R.raw.iframe_1080x720_gd600;
                    }

                } else {
                    iframeId = R.raw.iframe_1280x720_ins;
                }
                break;
            }
            case MATRICE_200:
            case MATRICE_210:
            case MATRICE_210_RTK:
            case INSPIRE_2: //inspire2
                DataCameraGetPushStateInfo.CameraType cameraType = DataCameraGetPushStateInfo.getInstance().getCameraType(0);
                if(cameraType == DataCameraGetPushStateInfo.CameraType.DJICameraTypeGD600) {
                    iframeId = R.raw.iframe_1080x720_gd600;
                } else {
                    if (width == 640 && height == 368) {
                        DJILog.i(TAG, "Selected Iframe=iframe_640x368_wm620");
                        iframeId = R.raw.iframe_640x368_wm620;
                    }
                    if (width == 608 && height == 448) {
                        DJILog.i(TAG, "Selected Iframe=iframe_608x448_wm620");
                        iframeId = R.raw.iframe_608x448_wm620;
                    } else if (width == 720 && height == 480) {
                        DJILog.i(TAG, "Selected Iframe=iframe_720x480_wm620");
                        iframeId = R.raw.iframe_720x480_wm620;
                    } else if (width == 1280 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1280x720_wm620");
                        iframeId = R.raw.iframe_1280x720_wm620;
                    } else if (width == 1080 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1080x720_wm620");
                        iframeId = R.raw.iframe_1080x720_wm620;
                    } else if (width == 1088 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1088x720_wm620");
                        iframeId = R.raw.iframe_1088x720_wm620;
                    } else if (width == 960 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_960x720_wm620");
                        iframeId = R.raw.iframe_960x720_wm620;
                    } else if (width == 1360 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1360x720_wm620");
                        iframeId = R.raw.iframe_1360x720_wm620;
                    } else if (width == 1344 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1344x720_wm620");
                        iframeId = R.raw.iframe_1344x720_wm620;
                    } else if (width == 1440 && height == 1088) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1440x1088_wm620");
                        iframeId = R.raw.iframe_1440x1088_wm620;
                    } else if (width == 1632 && height == 1080){
                        DJILog.i(TAG, "Selected Iframe=iframe_1632x1080_wm620");
                        iframeId = R.raw.iframe_1632x1080_wm620;
                    } else if (width == 1760 && height == 720) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1760x720_wm620");
                        iframeId = R.raw.iframe_1760x720_wm620;
                    } else if (width == 1920 && height == 800) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1920x800_wm620");
                        iframeId = R.raw.iframe_1920x800_wm620;
                    } else if (width == 1920 && height == 1024) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1920x1024_wm620");
                        iframeId = R.raw.iframe_1920x1024_wm620;
                    } else if (width == 1920 && height == 1088) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1920x1080_wm620");
                        iframeId = R.raw.iframe_1920x1088_wm620;
                    } else if (width == 1920 && height == 1440) {
                        DJILog.i(TAG, "Selected Iframe=iframe_1920x1440_wm620");
                        iframeId = R.raw.iframe_1920x1440_wm620;
                    }
                }
                break;
            case PHANTOM_4_PRO_V2:
            case PHANTOM_4_RTK: {
                iframeId = -1;
            }
            break;
            default: //for P3P, Inspire1, etc/
                iframeId = R.raw.iframe_1280x720_ins;
                break;
        }
        return iframeId;
    }

    /** Get default black IDR frame.
     * @param width Width of current video stream.
     * @return IDR frame data
     * @throws IOException
     */
    private byte[] getDefaultKeyFrame(int width, int height) throws IOException {
        BaseProduct product = DJISDKManager.getInstance().getProduct();
        if (product == null || product.getModel() == null) {
            return null;
        }
        int iframeId=getIframeRawId(product.getModel(), width, height);
        if (iframeId >= 0){

            InputStream inputStream = context.getResources().openRawResource(iframeId);
            int length = inputStream.available();
//            logd("iframeId length=" + length);
            byte[] buffer = new byte[length];
            inputStream.read(buffer);
            inputStream.close();

            return buffer;
        }
        return null;
    }

    /**
     * 收到一帧数据时候的回调
     * @param buf
     * @param size
     * @param isKeyFrame
     * @param width
     * @param height
     */
    public void onFrameDataRecv(byte[] buf, int size, int frameNum, boolean isKeyFrame, int width, int height) {
//        if(isKeyFrame) {
//            System.out.println("onFrameDataRecv, size = " + size + ", frameNum = " + frameNum + ", isKeyFrame = " + isKeyFrame + ", width = " + width + ", height = " + height);
//        }

        if (width != 0 && height != 0) {
            if (videoHeight != height || videoWidth != width) {
                System.out.println("width or height changed, new width = " + width + ", new height = " + height);
                if (mediaCodec != null) {
                    stopDecodeH264Thread();
                }
                // Create the codec instance.
                try {
                    mediaCodec = MediaCodec.createDecoderByType("video/avc");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // create the media format
                MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
                // set the color format to YUV420SP.
                format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
                // Configure the codec. What should be noted here is that the hardware decoder would not output
                // any yuv data if a surface is configured into, which mean that if you want the yuv frames, you
                // should set "null" surface when calling the "configure" method of MediaCodec.
                mediaCodec.configure(format, null, null, 0);
                bufferInfo = new MediaCodec.BufferInfo();

                mediaCodec.start();

                //解码H264数据线程
                decodeH264DataTask = new DecodeH264DataTask();
                decodeH264DataThread = new Thread(decodeH264DataTask);
                decodeH264DataThread.setDaemon(true);
                decodeH264DataThread.start();

                //将加码后的数据推送给第三方接口的线程
                dualOuterYuvDataTask = new DualOuterYuvDataTask();
                dualOuterYuvDataThread = new Thread(dualOuterYuvDataTask);
                dualOuterYuvDataThread.setDaemon(true);
                dualOuterYuvDataThread.start();

                videoWidth = width;
                videoHeight = height;
                videoYuvSize = width * height * 3 / 2;
            }

            if (!iFrameGot) {// check the I frame flag
                if (frameNum != 1 && !isKeyFrame) {
//                    System.err.println("the timing for setting iframe has not yet come.");
                    return;
                }

                byte[] defaultKeyFrame = null;
                try {
                    defaultKeyFrame = getDefaultKeyFrame(width, height);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (defaultKeyFrame != null) {
                    inQueueMediaCodec(defaultKeyFrame, defaultKeyFrame.length);
                    System.out.println("add iframe success!!!!");
                    iFrameGot = true;
                } else if (isKeyFrame) {
                    System.out.println("onFrameQueueIn no need add i frame!!!!");
                    iFrameGot = true;
                } else {
                    System.err.println("input key frame failed");
                }
            }

            inQueueMediaCodec(buf, size);
        }
    }

    private void inQueueMediaCodec(byte[] buf, int size){

        if(mediaCodec != null) {
            ByteBuffer buffer;
            int inIndex = mediaCodec.dequeueInputBuffer(0);
//                System.out.println("inIndex = " + inIndex);
            // Decode the frame using MediaCodec
            if (inIndex >= 0) {
                //Log.d(TAG, "decodeFrame: index=" + inIndex);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buffer = mediaCodec.getInputBuffer(inIndex);
                } else {
                    buffer = mediaCodec.getInputBuffers()[inIndex];
                }
                buffer.put(buf, 0, size);
                // Feed the frame data to the decoder.
                mediaCodec.queueInputBuffer(inIndex, 0, size, System.currentTimeMillis(), 0);
            } else {
//                System.out.println("dequeueInputBuffer index = " + inIndex);
//                mediaCodec.flush();
            }
        }
    }

    public void changeSurface(Surface surface,int surfaceWidth, int surfaceHeight){
        this.surfaceWidth = surfaceWidth;
        this.surfaceHeight = surfaceHeight;
        parseH264DataTask.surface = surface;
    }

    /**
     * 结束播放，清楚资源
     */
    private native void close();
}
