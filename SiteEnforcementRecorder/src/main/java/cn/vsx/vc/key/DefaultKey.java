package cn.vsx.vc.key;

import android.view.KeyEvent;

public class DefaultKey extends BaseKey {

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.getRepeatCount() == 0) {
                    videoKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (videoKeyLongPressStartTime != 0 && ((System.currentTimeMillis() - videoKeyLongPressStartTime) >= LONG_PRESS_TIME) && !videoKeyIsLongPress) {
                        videoKeyIsLongPress = true;
                        videoButton(true);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
//                dismissDialog();
                //音量下键和录音键
                if (event.getRepeatCount() == 0) {
                    audioKeyLongPressStartTime = System.currentTimeMillis();
                } else {
                    if (audioKeyLongPressStartTime != 0 && System.currentTimeMillis() - audioKeyLongPressStartTime >= LONG_PRESS_TIME && !audioKeyIsLongPress) {
                        audioKeyIsLongPress = true;
                        audioButton(true);
                    }
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                //摄像按键
//                dismissDialog();
                if (!videoKeyIsLongPress) {
                    videoButton(false);
                }
                videoKeyIsLongPress = false;
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                //拍照按键
                photoButton();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                //数据上传按键
                uploadLog();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                //ok按键
//                dismissDialog();
                menuKey();
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
