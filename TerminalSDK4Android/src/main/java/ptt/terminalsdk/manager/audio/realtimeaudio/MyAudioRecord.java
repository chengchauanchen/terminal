package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.manager.audio.IAudioRecord;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/22
 * 描述：
 * 修订历史：
 */
public class MyAudioRecord implements IAudioRecord{

    private final AudioRecord audioRecord;
    private Logger logger = Logger.getLogger(getClass());
    /** 采样率 */
    private int sampleRate = 8000;

    public MyAudioRecord(int audioRecordBufferSize){
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioRecordBufferSize);
    }

    @Override
    public boolean checkStateRecoding(){
        return audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    @Override
    public void startRecording(){
        audioRecord.startRecording();
    }

    @Override
    public int read(short[] audioData, int offsetInShorts, int sizeInShorts){
        return audioRecord.read(audioData,offsetInShorts,sizeInShorts);
    }

    @Override
    public void release(){
        audioRecord.release();
    }

    @Override
    public boolean checkStateInitialized(){
        return audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }
}
