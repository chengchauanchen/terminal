package org.easydarwin.push;

import android.content.Context;

import org.apache.log4j.Logger;
import org.easydarwin.audio.LocalFileAudioStream;

/**
 * author: zjx.
 * data:on 2018/11/12
 */

public class LocalVideoPushStream {

    LocalFileVideoStream localFileVideoStream;
    LocalFileAudioStream localFileAudioStream;

    EasyPusher mEasyPusher;
    Context mApplicationContext;
    boolean pushStream;

    Logger logger = Logger.getLogger(getClass().getName());
    public static final String TAG = "FileTransferOperation---";

    public LocalVideoPushStream (Context context) {
        mApplicationContext = context;
        mEasyPusher = new EasyPusher();

        localFileVideoStream = new LocalFileVideoStream(context, mEasyPusher);
        localFileAudioStream = new LocalFileAudioStream(context, mEasyPusher);
    }


    public void startStream(String filePath, String ip, String port, String id, InitCallback callback) {
        logger.info(TAG+"推送本地文件的地址===" + filePath);
        localFileVideoStream.startPush(filePath);
        localFileAudioStream.startPush(filePath);
        mEasyPusher.initPush( mApplicationContext, callback);
        mEasyPusher.setMediaInfo(Pusher.Codec.EASY_SDK_VIDEO_CODEC_H264, 25, Pusher.Codec.EASY_SDK_AUDIO_CODEC_AAC, 1, 8000, 16);
        mEasyPusher.start(ip, port, String.format("%s", id), Pusher.TransType.EASY_RTP_OVER_TCP);
        pushStream = true;
    }

    public void stopStream() {
        try {
            localFileVideoStream.stop();
            localFileAudioStream.stop();
        } finally {
            mEasyPusher.stop();
            pushStream = false;
        }
    }
}
