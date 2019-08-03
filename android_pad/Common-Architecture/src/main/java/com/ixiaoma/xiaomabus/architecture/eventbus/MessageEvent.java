package com.ixiaoma.xiaomabus.architecture.eventbus;

/**
 * 创建日期：2017/6/2 0002 on 15:50
 * 作者:penny
 */
public class MessageEvent<T> {
    private int isJump;
    private T data;

    public MessageEvent(int isJump, T data) {
        this.isJump = isJump;
        this.data = data;
    }

    public int getIsJump() {
        return isJump;
    }

    public void setIsJump(int isJump) {
        this.isJump = isJump;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public MessageEvent(int b) {
        this.isJump = b;
    }

    public int codeJump() {
        return isJump;
    }

    public void setJump(int jump) {
        isJump = jump;
    }
}
