package cn.vsx.vc.key;

import android.view.KeyEvent;

import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;

/**
 * 比特星I7
 */
public class BitStartI7Key extends BaseKey {

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 286:
                //摄像按键
//                dismissDialog();
                if (event.getRepeatCount() == 0) {
                    videoKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (videoKeyLongPressStartTime != 0 && ((System.currentTimeMillis() - videoKeyLongPressStartTime) >= LONG_PRESS_TIME) && !videoKeyIsLongPress) {
                        videoKeyIsLongPress = true;
                        videoButton(true);
                    }
                }
                return true;
            case 285:
                //菜单键和上传日志
                if (event.getRepeatCount() == 0) {
                    menuKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (menuKeyLongPressStartTime != 0 && System.currentTimeMillis() - menuKeyLongPressStartTime >= LONG_PRESS_TIME && !menuKeyIsLongPress) {
                        menuKeyIsLongPress = true;
                        uploadLog();
                    }
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CAMERA:
                //拍照按键
                photoButton();
                return true;
            case 287:
                //录音按键
                audioButton((MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED));
                return true;
            case 286:
                //摄像按键
//                dismissDialog();
                if (!videoKeyIsLongPress) {
                    videoButton(false);
                }
                videoKeyIsLongPress = false;
                return true;
            case 285:
                //短按是菜单键
                if (!menuKeyIsLongPress) {
                    menuKey();
                }
                menuKeyIsLongPress = false;
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
