package com.vsxin.terminalpad.mvp.ui.widget;

public interface SendGroupCallListener {
    /**
     * 开始
     */
    void speaking();

    /**
     * 准备说
     */
    void readySpeak();

    /**
     * 禁止组呼
     */
    void forbid();

    /**
     * 等待
     */
    void waite();

    /**
     * 沉默
     */
    void silence();

    /**
     * 听
     */
    void listening();

    /**
     * 失败
     */
    void fail();
}
