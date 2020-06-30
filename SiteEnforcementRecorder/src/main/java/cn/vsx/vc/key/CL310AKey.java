package cn.vsx.vc.key;

import android.content.IntentFilter;
import android.view.KeyEvent;

import org.apache.log4j.Logger;

import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiver.KeyEventFromCL310AReceiver;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;

public class CL310AKey extends BaseKey{

    public  Logger logger = Logger.getLogger(CL310AKey.class);
    public  final String TAG = "CL310AKey---";
    private static final int KEY_CODE_PTT  = 285;

    private static final int KEY_EVENT_SHORT_PRESS  = 0;
    private static final int KEY_EVENT_LONG_PRESS  = 1;
    private static final int KEY_EVENT_DOUBLE_CLICK  = 2;

    private static final int KEY_STATUS_DOWN  = 0;
    private static final int KEY_STATUS_UP  = 1;

    private KeyEventFromCL310AReceiver keyEventFromCL310AReceiver;
    /**
     * 注册广播
     */
    public  void registKeyEventFromCL310AReceiver() {
        try{
            if (keyEventFromCL310AReceiver == null) {
                keyEventFromCL310AReceiver = new KeyEventFromCL310AReceiver();
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(KeyEventFromCL310AReceiver.ACTION_KEY_EVENT);
            MyApplication.instance.registerReceiver(keyEventFromCL310AReceiver, filter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 注销广播
     */
    public  void unRegistKeyEventFromCL310AReceiver() {
        try{
            if (keyEventFromCL310AReceiver != null) {
                MyApplication.instance.unregisterReceiver(keyEventFromCL310AReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 按键事件
     * @param keyCode
     * @param keyEvent
     * @param keyStatus
     */
    public  void onKeyEvent(int keyCode,int keyEvent,int keyStatus){
        logger.info(TAG+"onKeyEvent--keyCode:"+keyCode+"-keyEvent:"+keyEvent+"-keyStatus:"+keyStatus);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(keyEvent == KEY_EVENT_SHORT_PRESS){
                    //音量+
                    volumeUp();
                }else if(keyEvent == KEY_EVENT_LONG_PRESS){
                    //菜单键
                    menuKey();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (keyEvent == KEY_EVENT_SHORT_PRESS) {
                    //短按-音量-
                    if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED) {
                        // 减小音量
                        volumeDown();
                    } else {
                        //录音
                        audioButton(false);
                    }
                }else if (keyEvent == KEY_EVENT_LONG_PRESS){
                    //长按-录音
                    audioButton(true);
                }
                break;
            case KeyEvent.KEYCODE_F5:
                if (keyEvent == KEY_EVENT_SHORT_PRESS) {
                    //短按-
                    videoButton(false);
                }else if (keyEvent == KEY_EVENT_LONG_PRESS){
                    //长按-
                    videoButton(true);
                }
                break;
            case KeyEvent.KEYCODE_CAMERA:
                if (keyEvent == KEY_EVENT_SHORT_PRESS) {
                    //短按-拍照
                    photoButton();
                }else if (keyEvent == KEY_EVENT_LONG_PRESS){
                }
                break;
            case KeyEvent.KEYCODE_F3:
                if (keyEvent == KEY_EVENT_SHORT_PRESS) {
                }else if (keyEvent == KEY_EVENT_LONG_PRESS){
                    //长按-上传日志
                    uploadLog();
                }
                break;
            case KEY_CODE_PTT:
                if (keyStatus == KEY_STATUS_DOWN) {
                    //按下
                    pttButton(Constants.PTTEVEVT_ACTION_DOWN);
                }else if (keyStatus == KEY_STATUS_UP){
                    //抬起
                    pttButton(Constants.PTTEVEVT_ACTION_UP);
                }
                break;
            default:break;
        }
    }

}
