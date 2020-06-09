package cn.vsx.vc.key;

import android.os.Bundle;
import android.view.KeyEvent;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.vc.receiveHandle.ReceiverAudioButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.receiveHandle.ReceiverPTTButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverPhotoButtonEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverUploadLogEventHandler;
import cn.vsx.vc.receiveHandle.ReceiverVideoButtonEventHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.VolumeToastUitl;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.recordingAudio.AudioRecordStatus;

public class BaseKey {

    //长按时间
    protected static final long LONG_PRESS_TIME = 1500;

    protected long videoKeyLongPressStartTime = 0;
    protected long audioKeyLongPressStartTime = 0;
    protected long menuKeyLongPressStartTime = 0;
    //
    protected boolean videoKeyIsLongPress = false;
    protected boolean audioKeyIsLongPress = false;
    protected boolean menuKeyIsLongPress = false;

    protected long lastVolumeUpTime = 0;
    protected long lastVolumeDownTime = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode) {

        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event){
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                // 增大音量
                volumeUp();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (!audioKeyIsLongPress) {
                    if (MyTerminalFactory.getSDK().getRecordingAudioManager().getStatus() == AudioRecordStatus.STATUS_STOPED) {
                        // 减小音量
                        volumeDown();
                    } else {
                        audioButton(false);
                    }
                }
                audioKeyIsLongPress = false;
                return true;
        }
        return false;
    }

    /**
     * ptt按键
     * @param intentAction
     */
    protected  void pttButton(String intentAction){
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverPTTButtonEventHandler.class,intentAction);
    }

    /**
     * 菜单按键
     */
    protected void menuKey(){
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_MENU,new Bundle());
    }

    /**
     * 音量+
     */
    protected void volumeUp(){
        if (System.currentTimeMillis() - lastVolumeUpTime > 500) {
            MyTerminalFactory.getSDK().getAudioProxy().volumeUp();
            if (MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
            } else {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
            }
            lastVolumeUpTime = System.currentTimeMillis();
        }
        //显示音量的Toast
        VolumeToastUitl.showToastWithImg(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
    }

    /**
     * 音量-
     */
    protected void volumeDown(){
        if (System.currentTimeMillis() - lastVolumeDownTime > 500) {
            MyTerminalFactory.getSDK().getAudioProxy().volumeDown();
            if (MyTerminalFactory.getSDK().getAudioProxy().getVolume() > 0) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
            } else {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
            }
            lastVolumeDownTime = System.currentTimeMillis();
        }
        //显示音量的Toast
        VolumeToastUitl.showToastWithImg( MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
    }

    /**
     * 录像按键
     * @param isLongPress 是否是长按
     */
    protected void videoButton(boolean isLongPress){
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverVideoButtonEventHandler.class, isLongPress);
    }
    /**
     * 录音按键
     * @param isLongPress 是否是长按
     */
    protected void audioButton(boolean isLongPress){
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverAudioButtonEventHandler.class, isLongPress);
    }

    /**
     * 拍照按键
     */
    protected void photoButton(){
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverPhotoButtonEventHandler.class);
    }

    /**
     * 上传日志
     */
    protected void uploadLog(){
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverUploadLogEventHandler.class);
    }
}
