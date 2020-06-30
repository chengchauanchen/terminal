package cn.vsx.vc.key;

import android.view.KeyEvent;

import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;

/**
 * H40
 */
public class H40Key extends BaseKey {

    //上报图像和录像
    private static final int KEY_CODE_VIDEO = 318;
    //录音
    private static final int KEY_CODE_AUDIO = 320;
    //菜单和上传日志
    private static final int KEY_CODE_MENU_AND_UPLOAD = 323;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_CODE_VIDEO:
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
            case KEY_CODE_MENU_AND_UPLOAD:
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
            case KEY_CODE_AUDIO:
                //录音按键
                audioButton((MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED));
                return true;
            case KEY_CODE_VIDEO:
                //摄像按键
//                dismissDialog();
                if (!videoKeyIsLongPress) {
                    videoButton(false);
                }
                videoKeyIsLongPress = false;
                return true;
            case KEY_CODE_MENU_AND_UPLOAD:
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
