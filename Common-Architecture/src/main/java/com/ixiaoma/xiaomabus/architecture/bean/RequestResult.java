package com.ixiaoma.xiaomabus.architecture.bean;

import android.text.TextUtils;

import com.ixiaoma.xiaomabus.architecture.net.MsgCode;

/**
 * Created by qiuzhiwen on 2018/11/19.
 */

public class RequestResult<T> extends BaseBean {
    private MsgBean msg;
    private T data;
    private boolean success;

    public boolean isSuccess() {
        return success && TextUtils.equals(msg.getCode(), MsgCode.SUCCESS);
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public MsgBean getMsg() {
        return msg;
    }

    public void setMsg(MsgBean msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
