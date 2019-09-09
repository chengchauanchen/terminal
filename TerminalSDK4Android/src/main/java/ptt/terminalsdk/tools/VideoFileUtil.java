package ptt.terminalsdk.tools;

import java.io.File;
import java.io.IOException;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/9/4
 * 描述：
 * 修订历史：
 */
public class VideoFileUtil{

    public static int getVideoDuration(File file){
        int videoTime = 0;
        android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
        try {
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            //获得了视频的时长（以毫秒为单位）
            videoTime = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return videoTime;
    }

    public static int getVideoDuration(String path){
        int videoTime = 0;
        android.media.MediaPlayer mediaPlayer = new android.media.MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            //获得了视频的时长（以毫秒为单位）
            videoTime = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        return videoTime;
    }
}
