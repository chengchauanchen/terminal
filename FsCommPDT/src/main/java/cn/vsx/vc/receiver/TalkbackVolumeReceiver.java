package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by zckj on 2017/3/28.
 */

public class TalkbackVolumeReceiver extends BroadcastReceiver {

    private ImageView iv_volume_off_call;
    private TextView tv_volume_status_bar;
    public TalkbackVolumeReceiver(){}
    public TalkbackVolumeReceiver(ImageView iv_volume_off_call, TextView tv_volume_status_bar) {
       this.iv_volume_off_call = iv_volume_off_call;
        this.tv_volume_status_bar = tv_volume_status_bar;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
//        if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
//            int maxVolumeC = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//            int currentVolumeC = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) ;// 当前的听筒音量
//            iv_volume_off_call.setVisibility(View.VISIBLE);
//            if (currentVolumeC == 0) {
//                iv_volume_off_call.setImageResource(R.drawable.volume_off_call);
//                tv_volume_status_bar.setVisibility(View.GONE);
//            }else {
//                iv_volume_off_call.setImageResource(R.drawable.volume_silence);
//                tv_volume_status_bar.setVisibility(View.VISIBLE);
//                tv_volume_status_bar.setText(currentVolumeC*100/maxVolumeC + "%");
//            }
//        }
    }
}
