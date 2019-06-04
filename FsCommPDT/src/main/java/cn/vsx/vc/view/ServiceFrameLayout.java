package cn.vsx.vc.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.receiveHandle.ReceiverEntityKeyEventInServiceHandler;

public class ServiceFrameLayout extends FrameLayout {
    public ServiceFrameLayout(@NonNull Context context) {
        super(context);
    }

    public ServiceFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ServiceFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK||event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP||event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiverEntityKeyEventInServiceHandler.class, event);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
