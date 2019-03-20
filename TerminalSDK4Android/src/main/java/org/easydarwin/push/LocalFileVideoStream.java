package org.easydarwin.push;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;

import org.apache.log4j.Logger;
import org.easydarwin.hw.EncoderDebugger;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLocalVideoPushFinishHandler;

/**
 * author: zjx.
 * data:on 2018/11/12
 */

public class LocalFileVideoStream {
    private static final String TAG = "FileTransferOperation---";
    Logger logger = Logger.getLogger(getClass().getName());
    private final Pusher easyPusher;

    private Context mContext;
    Thread mThread;

    public LocalFileVideoStream(Context context, Pusher pusher){
        this.mContext = context;
        easyPusher = pusher;
    }

    MediaExtractor mediaExtractor;
    MediaCodec mediaCodec;
    long lastPresentationTimeUs;
    public void startPush (final String filePath) {
        mThread = new Thread(new Runnable() {
            @SuppressLint("WrongConstant")
            @Override
            public void run() {
                mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(filePath);
                    //信道总数
                    int trackCount = mediaExtractor.getTrackCount();
                    MediaFormat mediaFormat = null;
                    for (int i = 0; i < trackCount; i++) {
                        MediaFormat format = mediaExtractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime.startsWith("video/")) {
                            mediaExtractor.selectTrack(i);
                            mediaFormat = mediaExtractor.getTrackFormat(i);
                            break;
                        }
                    }
                    if (mediaFormat != null) {
                        int width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                        int height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                        logger.info(TAG+"视频的宽===" + width + ", 视频的高===" + height);
                        ByteBuffer byteBuffer = ByteBuffer.allocate(width*height);
                        while (true) {
                            int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                            logger.info(TAG+"video:readSampleCount:" + readSampleCount);
                            if (readSampleCount < 0) {
                                break;
                            }
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int outputBufferIndex = 0;
                            //保存音频信息
                            byte[] h264 = new byte[readSampleCount];
                            byte[] mPpsSps = new byte[0];
                            byteBuffer.get(h264);
                            if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                                System.arraycopy(mPpsSps, 0, h264, 0, mPpsSps.length);
                            }
                            logger.info(TAG+"buffer ===" + h264.length);
                            //  audioOutputStream.write(buffer);
                            logger.info(TAG+"是啥帧==" + mediaExtractor.getSampleFlags());
                            long presentationTimeUs = mediaExtractor.getSampleTime();
                            logger.info(TAG+"获取的时间戳:" + presentationTimeUs);
                            if (lastPresentationTimeUs > 0 && !Thread.currentThread().isInterrupted()) {
                                Thread.sleep(Math.abs(presentationTimeUs-lastPresentationTimeUs)/1000);
                            }
                            lastPresentationTimeUs = presentationTimeUs;
                            easyPusher.push(h264, 0, readSampleCount , presentationTimeUs/1000, 1);
                            byteBuffer.clear();
                            if (mediaExtractor != null) {
                                mediaExtractor.advance();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    logger.info(TAG+"mediaExtractor.release!");
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                        mediaExtractor = null;
                    }
                    try {
                        Thread t = mThread;
                        mThread = null;
                        if (t != null) {
                            t.interrupt();
                            t.join();
                        }
                    } catch (InterruptedException e) {
                        e.fillInStackTrace();
                    }
                    //推流完成
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveLocalVideoPushFinishHandler.class);
                }
            }
        }, "videopush");
        mThread.start();
    }

    MediaCodec mMediaCodec;
    private void startMediaCodec(int mWidth, int mHeight) throws IOException {
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

        int bitrate = (int) (mWidth*mHeight*20*2*0.05f);
        if (mWidth >= 1920 || mHeight >= 1920) bitrate *= 0.3;
        else if (mWidth >= 1280 || mHeight >= 1280) bitrate *= 0.4;
        else if (mWidth >= 720 || mHeight >= 720) bitrate *= 0.6;
        EncoderDebugger debugger = EncoderDebugger.debug(mContext, mWidth, mHeight);
        mMediaCodec = MediaCodec.createByCodecName(debugger.getEncoderName());
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat());
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        Bundle params = new Bundle();
        params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMediaCodec.setParameters(params);
        }
    }

    public void stop() {
        try {
            mThread.interrupt();
            mThread.join();
        } catch (InterruptedException e) {
            e.fillInStackTrace();
        }
        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }
}
