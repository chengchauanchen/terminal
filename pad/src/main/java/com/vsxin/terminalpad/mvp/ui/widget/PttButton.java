package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.ixiaoma.xiaomabus.architecture.mvp.view.widget.MvpButton;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.presenter.PttButtonPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IPttButton;

/**
 * @author qzw
 * 自定义ptt按钮控件
 * <p>
 * 组呼功能
 */
public class PttButton extends MvpButton<IPttButton, PttButtonPresenter> implements IPttButton {

    private SendGroupCallListener sendGroupCallListener;

    public PttButton(Context context) {
        super(context);
    }

    public PttButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PadApplication.getPadApplication().isPttPress) {
                    PadApplication.getPadApplication().isPttPress = false;
                    pttUpDoThing();
                } else {
                    PadApplication.getPadApplication().isPttPress = true;
                    pttDownDoThing();
                }
            }
        });
    }

    /**
     * 设置ptt组呼状态监听
     *
     * @param sendGroupCallListener
     */
    public void setPttListener(SendGroupCallListener sendGroupCallListener) {
        this.sendGroupCallListener = sendGroupCallListener;
    }


    private void pttDownDoThing() {
        if (sendGroupCallListener != null) {
            getPresenter().pttDownDoThing(sendGroupCallListener);
        }
    }

    private void pttUpDoThing() {
        getPresenter().pttUpDoThing();
    }

    @Override
    public PttButtonPresenter createPresenter() {
        return new PttButtonPresenter(getContext());
    }

}
