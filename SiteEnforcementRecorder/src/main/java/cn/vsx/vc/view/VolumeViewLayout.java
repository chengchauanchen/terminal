package cn.vsx.vc.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.TimerTask;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by jamie on 2017/12/1.
 */

public class VolumeViewLayout extends LinearLayout {

    private TimerTask timerTask;
    private static final int RECEIVEVOICECHANGED = 0;
    private Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == RECEIVEVOICECHANGED){
                setVisibility(View.GONE);
//                ll_sliding_chenge_volume.setVisibility(View.GONE);
            }
        }
    };
    private TextView tv_volume_fw;
    private RelativeLayout ll_volume;
//    private LinearLayout ll_sliding_chenge_volume;

    public VolumeViewLayout(Context context) {
        super(context);
    }

    public VolumeViewLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeViewLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_volume, this, true);
        ll_volume = view.findViewById(R.id.ll_volume);
        tv_volume_fw = view.findViewById(R.id.tv_volume_fw);
//        ll_sliding_chenge_volume = view.findViewById(R.id.ll_sliding_chenge_volume);
    }
    private void initData() {
        tv_volume_fw.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
    }
    private void initListener(){
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
    }
    public void unRegistLintener () {
        myHandler.removeCallbacksAndMessages(null);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
    }

    /**音量改变*/
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(boolean isVolumeOff,int status) {
            myHandler.removeMessages(RECEIVEVOICECHANGED);
            if (status == 0){
                setVisibility(View.GONE);
//                ll_sliding_chenge_volume.setVisibility(View.GONE);
            }else if (status ==1){
                setVisibility(View.VISIBLE);
//                ll_sliding_chenge_volume.setVisibility(View.VISIBLE);

            }
            tv_volume_fw.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
            if (timerTask!= null){
                timerTask.cancel();
                timerTask = null;
            }
            myHandler.sendEmptyMessageDelayed(RECEIVEVOICECHANGED,2000);
        }
    };
}
