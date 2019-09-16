package com.vsxin.terminalpad.mvp.contract.constant;

import com.vsxin.terminalpad.R;

/**
 * 上图 终端类型
 */
public enum TerminalEnum {

    TERMINAL_TETRA("Tetra终端", "TERMINAL_TETRA", R.mipmap.call_connected),
    TERMINAL_PAD("Pad终端", "TERMINAL_PAD", R.mipmap.call_connected),
    TERMINAL_HDMI("HDMI终端", "TERMINAL_HDMI", R.mipmap.call_connected),
    TERMINAL_PC("PC终端", "TERMINAL_PC", R.mipmap.call_connected),
    TERMINAL_SUPER("超级终端", "TERMINAL_SUPER", R.mipmap.call_connected),
    TERMINAL_PROXY("代理终端", "TERMINAL_PROXY", R.mipmap.call_connected),
    TERMINAL_TEST("测试终端", "TERMINAL_TEST", R.mipmap.call_connected),

    //上图终端
    TERMINAL_PHONE("警务通", "TERMINAL_PHONE", R.mipmap.img_phone),
    TERMINAL_BODY_WORN_CAMERA("执法记录仪", "TERMINAL_BODY_WORN_CAMERA", R.mipmap.img_recorder),
    TERMINAL_UAV("无人机", "TERMINAL_UAV", R.mipmap.img_uav),
    TERMINAL_PDT("PDT(350M)", "TERMINAL_PDT", R.mipmap.img_hand),
    TERMINAL_LTE("LTE", "TERMINAL_LTE", R.mipmap.img_lte),
    TERMINAL_BULL("布控球", "TERMINAL_BULL", R.mipmap.img_lte_cammera),
    TERMINAL_CAMERA("摄像头", "TERMINAL_CAMERA", R.mipmap.img_cammera),
    TERMINAL_PATROL("巡逻船", "patrol", R.mipmap.img_boat),
    TERMINAL_CAR("警车", "car", R.mipmap.call_connected),
    TERMINAL_PERSONNEL("民警", "personnel", R.mipmap.img_police);

    private String des;//文字介绍
    private String type;//类型标识
    private int rid;//资源图标id

    TerminalEnum(String des,String type,int rid) {
        this.des = des;
        this.type = type;
        this.rid = rid;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }
}
