package ptt.terminalsdk.manager.audio.record;

import android.media.AudioManager;
import android.media.AudioTrack;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioPlayComplateHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.zectec.speex.Speex;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by zc on 2017/4/1.
 */

public class Record {
    private Logger logger = Logger.getLogger(getClass());
    private final String recordFileHeaderFlag = "zectec";
    public final static int AUDIO_CODEC_TYPE_AAC = 1;
    public final static int AUDIO_CODEC_TYPE_SPEEX = 2;
    private boolean playing;
    private boolean stopped;

    public synchronized void playRecord(final String fileName, final IAudioPlayComplateHandler handler){
        if(playing){
            stopped = true;
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        stopped = false;
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (Record.this) {
                    if (!playing) {
                        playing = true;
                    } else {
                        return;
                    }
                }
                _playRecord(fileName, handler);
                synchronized (Record.this) {
                    playing = false;
                    Record.this.notify();
                }
            }
        });
    }

    public synchronized void stopPlayRecord(){
        stopped = true;
    }

    private void _playRecord(String fileName, IAudioPlayComplateHandler handler){
        FileInputStream fis = null;
        int readResult;
        try {
            fis = new FileInputStream(fileName);
            byte[] headerFlag = new byte[6];
            fis.read(headerFlag);
            if(recordFileHeaderFlag.equals(new String(headerFlag))){//判断文件头标记是否是zectec
                int version = fis.read();
                switch (version){
                    case 1 : playRecordVersion1(fis, handler);
                }
            }
        } catch (FileNotFoundException e) {
            logger.warn("录音文件：" + fileName + "不存在", e);
        } catch (IOException e) {
            logger.warn("录音文件：" + fileName + "解析时发生异常", e);
        } finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void playRecordVersion1(FileInputStream fis, IAudioPlayComplateHandler handler) throws IOException {
        int codec = fis.read();
        switch (codec){
            case AUDIO_CODEC_TYPE_SPEEX : playRecordVersion1Speex(fis, handler);
        }
    }

    private void playRecordVersion1Speex(FileInputStream fis, IAudioPlayComplateHandler handler) throws IOException {
        int sampleRate = fis.read() * 1000;
        int compression = fis.read();
        int channelOut = fis.read();
        int encodingPcm = fis.read();
        Speex speex = new Speex();
        speex.init(compression, sampleRate);
        int bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelOut, encodingPcm)*4;
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, channelOut, encodingPcm, bufferSize, AudioTrack.MODE_STREAM);
        try{
            audioTrack.play();
            int len;
            int readLen;
            int decodeLen;
            byte[] data;
            short[] sout = new short[960];
            while ((len = fis.read()) != -1 && !stopped){
                data = new byte[len];
                readLen = fis.read(data);
                if(readLen == len){
                    decodeLen = speex.decode4File(data, 0, sout, len);
                    if(decodeLen > 0){
                        audioTrack.write(sout, 0, decodeLen);
                    }
                }
            }
            if ((len = fis.read()) == -1){
                MyTerminalFactory.getSDK().putParam(Params.IS_PLAY_END,true);
            }else {
                MyTerminalFactory.getSDK().putParam(Params.IS_PLAY_END,false);
            }
        } catch (IllegalStateException e){
            logger.error("录音播放失败：audioTrack状态异常", e);
        }
        finally {
            audioTrack.release();
            speex.close();
            if(handler != null){
                handler.handle();
            }
        }
    }
}
