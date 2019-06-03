package cn.vsx.vc.utils;

import android.media.MediaPlayer;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/5/31
 * 描述：
 * 修订历史：
 */
public class MediaPlayerUtil{

    private MediaPlayerUtil(){

    }
    private static MediaPlayerUtil mediaPlayerUtil = new MediaPlayerUtil();

    //单例模式
    public synchronized static MediaPlayerUtil getInstance() {
        return mediaPlayerUtil;
    }

    //播放方法
    public void play(MediaPlayer mediaplayer) {
        mediaplayer.start();
    }


    //暂停
    public void pause(MediaPlayer mediaplayer) {
        mediaplayer.pause();
    }


    //判断是否正在播放中
    public boolean isplay(MediaPlayer mediaplayer) {
        return mediaplayer.isPlaying();
    }

    //获取播放时长
    public long getDuring(MediaPlayer mediaplayer) {
        return mediaplayer.getDuration();
    }

    //获取当前的播放进度
    public long getCurrentduring(MediaPlayer mediaplayer) {
        return mediaplayer.getCurrentPosition();
    }

    //获取位置
    public int position(int current) {
        return current;
    }

    //更上进度，设置进度..
    public void curento(int position, MediaPlayer mediaplayer) {
        mediaplayer.seekTo(position);
    }


    /**
     * 关闭播放器
     */
    public void closeMedia(MediaPlayer mediaplayer) {
        if (mediaplayer != null) {
            if (mediaplayer.isPlaying()) {
                mediaplayer.stop();
            }
            mediaplayer.release();
        }

    }
}
