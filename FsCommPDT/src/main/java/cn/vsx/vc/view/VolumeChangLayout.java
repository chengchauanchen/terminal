package cn.vsx.vc.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 设置界面的“音量改变和静音”组件
 * Created by gt358 on 2017/8/9.
 */
public class VolumeChangLayout extends LinearLayout{
//    @Bind(R.id.btn_isNoVoice)
//    MToggleButton btnIsNoVoice;
    @Bind(R.id.voice_num)
    TextView voiceNum;
    @Bind(R.id.no_voice_text)
    TextView noVoiceText;

    private Context context;
    private int voice;
    private Boolean isNoVoice = false;

    public Logger logger = Logger.getLogger(getClass());

    public VolumeChangLayout(Context context) {
        this(context, null);
    }

    public VolumeChangLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VolumeChangLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
        initData();
        initListener();
    }

    private void initView () {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_volumechange, this, true);
        ButterKnife.bind(this, view);
    }

    private void initData () {
        voiceNum.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "");
    }

    private void initListener () {
//        btnIsNoVoice.setOnBtnClick(onBtnClickListener);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
    }

    public void unRegistLintener () {
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
    }

    /**点击事件*/
    @OnClick({R.id.voice_cut, R.id.voice_add})
    public void onClick(View view) {
        voice = Integer.parseInt(voiceNum.getText().toString());
        switch (view.getId()) {
            case R.id.voice_cut:
                MyTerminalFactory.getSDK().getAudioProxy().volumeDown();
                voiceNum.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "");
                if(MyTerminalFactory.getSDK().getAudioProxy().getVolume()==0){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true,0);
                }else {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,0);
                }

                break;
            case R.id.voice_add:
//                btnIsNoVoice.initToggleState(false);
                logger.info("声音增强时，现在音量" + MyTerminalFactory.getSDK().getAudioProxy().getVolume());
                MyTerminalFactory.getSDK().getAudioProxy().volumeUp();
                voiceNum.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "");
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,0);
                break;
        }
    }

    /**音量改变*/
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

                @Override
                public void handler(boolean isVolumeOff,int status) {
//                    if(MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0 && isNoVoice){
//                        btnIsNoVoice.initToggleState(false);
//                        noVoiceText.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
//                    }
                    voiceNum.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "");
                    if(isVolumeOff){
                        voiceNum.setText(0 + "");
                    }
                }
            };

    /**静音按钮点击*/
    private MToggleButton.OnBtnClickListener onBtnClickListener = new MToggleButton.OnBtnClickListener() {
        @Override
        public void onBtnClick(boolean currState) {
            isNoVoice = currState;
            if(currState){
                MyTerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                voiceNum.setText(0 + "");
                noVoiceText.setTextColor(context.getResources().getColor(R.color.setting_text_black));
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,1);
            }else {
                MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                voiceNum.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "");
                noVoiceText.setTextColor(context.getResources().getColor(R.color.setting_text_gray));
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,1);
            }
        }
    };

}
