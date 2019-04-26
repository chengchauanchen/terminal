package org.easydarwin.push;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;

import org.apache.log4j.Logger;
import org.easydarwin.muxer.EasyMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.easydarwin.push.AirCraftMediaStream.info;

/**
 * Created by apple on 2017/5/13.
 */
public class AirHWConsumer extends Thread implements UAVVideoConSumer{
    private static final String TAG = "Pusher";
    public EasyMuxer mMuxer;
    private final Context mContext;
    private final Pusher mPusher;
    private int mHeight;
    private int mWidth;
    private MediaCodec mMediaCodec;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private volatile boolean mVideoStarted;
    private MediaFormat newFormat;
    public static final String VIDEO_ENCODING_FORMAT = "video/avc";
    private Logger logger = Logger.getLogger("AirHWConsumer");

    public AirHWConsumer(Context context, Pusher pusher) {
        mContext = context;
        mPusher = pusher;
    }


    @Override
    public void onVideoStart(int width, int height) throws IOException{
        newFormat = null;
        this.mWidth = width;
        this.mHeight = height;
        startMediaCodec();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP + 1) {
            inputBuffers = outputBuffers = null;
        } else {
            inputBuffers = mMediaCodec.getInputBuffers();
            outputBuffers = mMediaCodec.getOutputBuffers();
        }
        start();
        mVideoStarted = true;
    }

    final int millisPerframe = 1000 / 24;
    long lastPush = 0;

    @Override
    public int onVideo(byte[] data, int format) {
        if (!mVideoStarted) return 0;

        try {
            if (lastPush == 0) {
                lastPush = System.currentTimeMillis();
            }
            long time = System.currentTimeMillis() - lastPush;
            if (time >= 0) {
                time = millisPerframe - time;
                if (time > 0) Thread.sleep(time / 2);
            }

//            if (info.mColorFormat == COLOR_FormatYUV420SemiPlanar) {
//                JNIYuvTools.nv12ToNv21(data,mWidth,mHeight);
//                JNIUtil.yuvConvert(data, mWidth, mHeight,2);
//                logger.info("YuvPlayer.push");
                //将一帧yuv数据推送至播放端
//                YuvPlayer.push(data, mWidth, mHeight);
//            }
//               else if (info.mColorFormat == COLOR_TI_FormatYUV420PackedSemiPlanar) {
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 6);
//            } else if (info.mColorFormat == COLOR_FormatYUV420Planar) {
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 5);
//            } else {
//                JNIUtil.yuvConvert(data, mWidth, mHeight, 5);
//            }
            int bufferIndex = mMediaCodec.dequeueInputBuffer(0);
            //            logger.info("bufferIndex:"+bufferIndex);
            if (bufferIndex >= 0) {
                ByteBuffer buffer = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    buffer = mMediaCodec.getInputBuffer(bufferIndex);
                } else {
                    buffer = inputBuffers[bufferIndex];
                }
                buffer.clear();
                buffer.put(data);
                buffer.clear();
                //                logger.info("queueInputBuffer");
                mMediaCodec.queueInputBuffer(bufferIndex, 0, data.length, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_KEY_FRAME);
            }
            if (time > 0) Thread.sleep(time / 2);
            lastPush = System.currentTimeMillis();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void run() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = 0;
        byte[] mPpsSps = new byte[0];
        byte[] h264 = new byte[mWidth * mHeight];
        do {
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);

            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                outputBuffers = mMediaCodec.getOutputBuffers();
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                synchronized (AirHWConsumer.this) {
                    newFormat = mMediaCodec.getOutputFormat();
                    EasyMuxer muxer = mMuxer;
                    if (muxer != null) {
                        // should happen before receiving buffers, and should only happen once

                        muxer.addTrack(newFormat, true);
                    }
                }
            } else if (outputBufferIndex < 0) {
                // let's ignore it
            } else {
                //                logger.info("outputBufferIndex:"+outputBufferIndex);
                ByteBuffer outputBuffer;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                } else {
                    outputBuffer = outputBuffers[outputBufferIndex];
                }
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                EasyMuxer muxer = mMuxer;
                if (muxer != null) {
                    muxer.pumpStream(outputBuffer, bufferInfo, true);
                }

                boolean sync = false;
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {// sps
                    sync = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                    if (!sync) {
                        byte[] temp = new byte[bufferInfo.size];
                        outputBuffer.get(temp);
                        mPpsSps = temp;
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        continue;
                    } else {
                        mPpsSps = new byte[0];
                    }
                }
                sync |= (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                int len = mPpsSps.length + bufferInfo.size;
                if (len > h264.length) {
                    h264 = new byte[len];
                }
                if (sync) {
                    System.arraycopy(mPpsSps, 0, h264, 0, mPpsSps.length);
                    outputBuffer.get(h264, mPpsSps.length, bufferInfo.size);
                    mPusher.push(h264, 0, mPpsSps.length + bufferInfo.size, bufferInfo.presentationTimeUs / 1000, 1);
                    //                    if (BuildConfig.DEBUG)
                    //                        Log.i(TAG, String.format("push i video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                    //                        logger.info(String.format("push i video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                } else {
                    outputBuffer.get(h264, 0, bufferInfo.size);
                    mPusher.push(h264, 0, bufferInfo.size, bufferInfo.presentationTimeUs / 1000, 1);
                    //                    if (BuildConfig.DEBUG)
                    //                        Log.i(TAG, String.format("push video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                    //                        logger.info(String.format("push video stamp:%d", bufferInfo.presentationTimeUs / 1000));
                }


                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
        }
        while (mVideoStarted);
    }

    @Override
    public void onVideoStop() {
        do {
            newFormat = null;
            mVideoStarted = false;
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (isAlive());
        if (mMediaCodec != null) {
            stopMediaCodec();
            mMediaCodec = null;
        }
    }

    @Override
    public synchronized void setMuxer(EasyMuxer muxer) {
        if (muxer != null) {
            if (newFormat != null)
                muxer.addTrack(newFormat, true);
        }
        mMuxer = (EasyMuxer) muxer;
    }


    /**
     * 初始化编码器
     */
    private void startMediaCodec() throws IOException{
            /*
        SD (Low quality) SD (High quality) HD 720p
1 HD 1080p
1
Video resolution 320 x 240 px 720 x 480 px 1280 x 720 px 1920 x 1080 px
Video frame rate 20 fps 30 fps 30 fps 30 fps
Video bitrate 384 Kbps 2 Mbps 4 Mbps 10 Mbps
        */
        int framerate = 20;
        //        if (width == 640 || height == 640) {
        //            bitrate = 2000000;
        //        } else if (width == 1280 || height == 1280) {
        //            bitrate = 4000000;
        //        } else {
        //            bitrate = 2 * width * height;
        //        }

        int bitrate = (int) (mWidth * mHeight * 20 * 2 * 0.05f);
        if (mWidth >= 1920 || mHeight >= 1920) bitrate *= 0.3;
            //0.4延迟2秒   1658880
            //0.5延迟5秒左右，卡顿   2073600

        else if (mWidth >= 1280 || mHeight >= 1280) bitrate *= 0.4;
        else if (mWidth >= 720 || mHeight >= 720) bitrate *= 0.6;
        else if(mWidth>=640 ||mHeight>=640) bitrate *=1.8;
        // 1.2延时1秒左右  737280
        // 1.5延时3秒左右  921600
        // 1.8延时4秒左右，卡顿  1105920

        //海能达分辨率848*480


        //固定码率2M，1920*1080延迟大概1s，1080*720 3s，640*480  2s  320*240 2s


        //        logger.error("bitrate:" + bitrate+"--info.mName:"+info.mName);
        try{
            mMediaCodec = MediaCodec.createByCodecName(info.mName);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        if(null != mMediaCodec){
            logger.info("bitrate:"+bitrate+"----framerate:"+framerate);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, info.mColorFormat);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();

            Bundle params = new Bundle();
            params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mMediaCodec.setParameters(params);
            }
        }else {
            logger.warn("创建info.mName："+info.mName+"  的mMediaCodec失败！");
        }
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopMediaCodec() {
        if(null != mMediaCodec){
            mMediaCodec.stop();
            mMediaCodec.release();
        }
    }

}
