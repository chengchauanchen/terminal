package org.easydarwin.audio;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import org.apache.log4j.Logger;
import org.easydarwin.push.Pusher;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * author: zjx.
 * data:on 2018/11/12
 */

public class LocalFileAudioStream {
    int mSamplingRateIndex = 0;
    Pusher easyPusher;
    private Thread mThread = null;
    Logger logger = Logger.getLogger(getClass().getName());
    String TAG = "FileTransferOperation---";
    //final String path = Environment.getExternalStorageDirectory() + "/123450001.aac";

    public LocalFileAudioStream(Context context, Pusher pusher){
        easyPusher = pusher;
    }

    MediaExtractor mediaExtractor;
    long lastPresentationTimeUs;
    public void startPush (final String filePath) {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mediaExtractor = new MediaExtractor();
                try {
                    mediaExtractor.setDataSource(filePath);
                    //信道总数
                    int trackCount = mediaExtractor.getTrackCount();
                    for (int i = 0; i < trackCount; i++) {
                        MediaFormat format = mediaExtractor.getTrackFormat(i);
                        if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                            mediaExtractor.selectTrack(i);
                            break;
                        }
                    }

                    ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
                    while (true) {
                        int readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0);
                        logger.info(TAG+"audio:readSampleCount:" + readSampleCount);
                        if (readSampleCount < 0) {
                            break;
                        }
                        //保存音频信息
                        byte[] buffer = new byte[readSampleCount];
                        byteBuffer.get(buffer);
                        /************************* 用来为aac添加adts头**************************/
                        byte[] aacaudiobuffer = new byte[readSampleCount + 7];
                        addADTStoPacket(aacaudiobuffer, readSampleCount + 7);
                        System.arraycopy(buffer, 0, aacaudiobuffer, 7, readSampleCount);
                        /***************************************close**************************/
                        //  audioOutputStream.write(buffer);
                        long presentationTimeUs = mediaExtractor.getSampleTime();
                        logger.info(TAG+"获取的时间戳:" + presentationTimeUs);
                        if (lastPresentationTimeUs > 0 && !Thread.currentThread().isInterrupted()) {
                            Thread.sleep(Math.abs(presentationTimeUs-lastPresentationTimeUs)/1000);
                        }
                        lastPresentationTimeUs = presentationTimeUs;
                        easyPusher.push(buffer, 0, readSampleCount + 7, presentationTimeUs/1000, 0);
                        byteBuffer.clear();
                        if (mediaExtractor != null) {
                            mediaExtractor.advance();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    logger.info(TAG+"mediaExtractor.release!");

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
                    //推送完成
//                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveLocalVideoPushFinishHandler.class);
                }
            }
        }, "aduiopush");
        mThread.start();
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((2 - 1) << 6) + (mSamplingRateIndex << 2) + (1 >> 2));
        packet[3] = (byte) (((1 & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public void stop() {

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
        if (mediaExtractor != null) {
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }
}
