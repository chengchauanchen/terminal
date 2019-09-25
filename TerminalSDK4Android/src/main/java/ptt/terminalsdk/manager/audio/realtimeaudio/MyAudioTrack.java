package ptt.terminalsdk.manager.audio.realtimeaudio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.manager.audio.Command;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioTrack;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/22
 * 描述：
 * 修订历史：
 */
public class MyAudioTrack implements IAudioTrack{

    private Logger logger = Logger.getLogger(getClass());
    private AudioTrack audioTrack;
    private int currentStreamType;

    public MyAudioTrack(Command.CmdType cmdType,int sampleRate,int audioTrackBufferSize){
        getAudioTrack(cmdType,sampleRate,audioTrackBufferSize);
    }
    @Override
    public void release(){
        if(audioTrack != null){
            audioTrack.release();
            audioTrack = null;
        }
    }

    @Override
    public int write(short[] audioData, int offsetInShorts, int sizeInShorts){
        return audioTrack.write(audioData, offsetInShorts, sizeInShorts);
    }

    @Override
    public boolean checkStatePlaying(){
        if(audioTrack != null){
            return audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING;
        }else {
            return false;
        }
    }

    @Override
    public void pause(){
        audioTrack.pause();
    }

    @Override
    public void play(){
        try{
            audioTrack.play();
        }catch(Exception e){
            logger.error("播放音频出错：",e);
        }
    }

    public AudioTrack getAudioTrack(Command.CmdType cmdType,int sampleRate,int audioTrackBufferSize){
        if(cmdType == Command.CmdType.RESUME_RECEIVER) {//单工通信使用媒体声音模式
            return getAudioTrack(AudioManager.STREAM_MUSIC,sampleRate,audioTrackBufferSize);
        }
        else if(cmdType == Command.CmdType.START_DUPLEX_COMMUNICATION){//双工通信使用电话声音模式
            return getAudioTrack(AudioManager.STREAM_VOICE_CALL,sampleRate,audioTrackBufferSize);
        }
        else{//其它模式暂不支持
            throw new IllegalArgumentException("不支持命令类型：" + cmdType);
        }
    }

    private AudioTrack getAudioTrack(int streamType,int sampleRate,int audioTrackBufferSize){
        if(audioTrack == null){
            audioTrack = new AudioTrack(streamType, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, audioTrackBufferSize, AudioTrack.MODE_STREAM);
            if(audioTrack.getState() != AudioTrack.STATE_INITIALIZED){//如果初始化失败，则再试一次
                logger.warn("audioTrack 初始化失败，state = " + audioTrack.getState() + "，再试一次");
                audioTrack = new AudioTrack(streamType, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, audioTrackBufferSize, AudioTrack.MODE_STREAM);
            }
            currentStreamType = streamType;
        }
        else if(streamType != currentStreamType){
            releaseAudioTrack();
            return getAudioTrack(streamType,sampleRate,audioTrackBufferSize);
        }
        return audioTrack;
    }

    private void releaseAudioTrack(){
        if(audioTrack != null){
            audioTrack.release();
            audioTrack = null;
        }
    }
}
