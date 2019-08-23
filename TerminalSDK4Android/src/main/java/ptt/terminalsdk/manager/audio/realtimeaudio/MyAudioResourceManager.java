package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;

import cn.vsx.hamster.terminalsdk.manager.audio.AudioResourceManager;
import cn.vsx.hamster.terminalsdk.manager.audio.Command;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioRecord;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioTrack;
import cn.vsx.hamster.terminalsdk.manager.audio.ISpeex;
import cn.zectec.speex.Speex;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/22
 * 描述：
 * 修订历史：
 */
public class MyAudioResourceManager extends AudioResourceManager{

    private int sampleRate = 8000;

    //private IAudioRecord audioRecord;

    //private IAudioTrack audioTrack;

    public MyAudioResourceManager(){
        super();
    }

    @Override
    public ISpeex getSpeex4Sender(){
        if(speex4Sender == null){
            speex4Sender = new Speex();
            speex4Sender.init();
        }
        return speex4Sender;
    }

    @Override
    public ISpeex getSpeex4Receiver(){
        if(speex4Receiver == null){
            speex4Receiver = new Speex();
            speex4Receiver.init();
        }
        return speex4Receiver;
    }

    @Override
    public IAudioRecord getAudioRecord(){
        if(audioRecord == null){
            audioRecord = new MyAudioRecord(getAudioRecordBufferSize());
            if(!audioRecord.checkStateInitialized()){
                audioRecord = new MyAudioRecord(getAudioRecordBufferSize());
            }

        }
        return audioRecord;
    }

    @Override
    public IAudioTrack getAudioTrack(Command.CmdType cmdType){
        if(audioTrack == null){
            audioTrack = new MyAudioTrack(cmdType, sampleRate, getAudioTrackBufferSize());
        }
        return audioTrack;
    }

    @Override
    public int getAudioTrackBufferSize(){
        return AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)*4;
    }

    @Override
    public int getAudioRecordBufferSize(){
        return AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*4;
    }
}
