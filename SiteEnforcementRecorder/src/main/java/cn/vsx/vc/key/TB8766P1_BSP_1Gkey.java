package cn.vsx.vc.key;


import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ScrrenUtils;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;

/**
 * TB8766P1_BSP_1Gkey
 *  PTT:  22
 * 拍照：27
 * 录像：21
 * sos:    24
 * 录音：25
 */
public class TB8766P1_BSP_1Gkey extends BaseKey {
    public  final String TAG = "TB8766P1_BSP_1Gkey---";
    //组呼
    private static final int KEY_CODE_PTT = 22;
    //上报图像和录像
    private static final int KEY_CODE_VIDEO = 21;
    //录音
    private static final int KEY_CODE_AUDIO = 25;
    //菜单和上传日志
    private static final int KEY_CODE_MENU_AND_UPLOAD = 24;

    //菜单和上传日志
    private static final int KEY_CODE_HOME = 4;

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

            case KEY_CODE_PTT:
                //ptt
                if (event.getRepeatCount() == 0) {
                    pttButton(Constants.PTTEVEVT_ACTION_DOWN);
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
            case KeyEvent.KEYCODE_BACK:
               ScrrenUtils.getInstance().openBacklight("2");
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
            case KEY_CODE_PTT:
                //ptt
                pttButton(Constants.PTTEVEVT_ACTION_UP);
                return true;
            case KEY_CODE_AUDIO:
                //录音按键
                audioButton((MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED));
                return true;
            case KEY_CODE_VIDEO:
                //摄像按键
//                dismissDialog()
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
            case KeyEvent.KEYCODE_BACK:
                //短按是菜单键
//                if (!powerSupplymenuKeyIsLongPress) {
//                    ScrrenUtils.getInstance().openBacklight("2");
//                }
//                powerSupplymenuKeyIsLongPress = false;
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
