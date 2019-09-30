package com.vsxin.terminalpad.mvp.contract.constant;

import com.vsxin.terminalpad.R;

/**
 * 上图 终端类型
 */
public enum TerminalEnum {

//    TERMINAL_TETRA("Tetra终端", "TERMINAL_TETRA", R.mipmap.call_connected),
//    TERMINAL_PAD("Pad终端", "TERMINAL_PAD", R.mipmap.call_connected),
//    TERMINAL_HDMI("HDMI终端", "TERMINAL_HDMI", R.mipmap.call_connected),
//    TERMINAL_PC("PC终端", "TERMINAL_PC", R.mipmap.call_connected),
//    TERMINAL_SUPER("超级终端", "TERMINAL_SUPER", R.mipmap.call_connected),
//    TERMINAL_PROXY("代理终端", "TERMINAL_PROXY", R.mipmap.call_connected),
//    TERMINAL_TEST("测试终端", "TERMINAL_TEST", R.mipmap.call_connected),
//
//    //上图终端
//    TERMINAL_PHONE("警务通", "TERMINAL_PHONE", R.mipmap.img_phone),
//    TERMINAL_BODY_WORN_CAMERA("执法记录仪", "TERMINAL_BODY_WORN_CAMERA", R.mipmap.img_recorder),
//    TERMINAL_UAV("无人机", "TERMINAL_UAV", R.mipmap.img_uav),
//    TERMINAL_PDT("PDT(350M)", "TERMINAL_PDT", R.mipmap.img_hand),
//    TERMINAL_LTE("LTE", "TERMINAL_LTE", R.mipmap.img_lte),
//    TERMINAL_BULL("布控球", "TERMINAL_BULL", R.mipmap.img_lte_cammera),
//    TERMINAL_CAMERA("摄像头", "TERMINAL_CAMERA", R.mipmap.img_cammera),
//    TERMINAL_PATROL("巡逻船", "patrol", R.mipmap.img_boat),
//    TERMINAL_CAR("警车", "car", R.mipmap.call_connected),
//    TERMINAL_PERSONNEL("民警", "personnel", R.mipmap.img_police),

    TERMINAL_TETRA("Tetra终端", 2,0),
    TERMINAL_PAD("Pad终端", 3,0),
    TERMINAL_HDMI("HDMI终端", 4,0),
    TERMINAL_PC("PC终端", 6,0),
    TERMINAL_SUPER("超级终端", 7,0),
    TERMINAL_PROXY("代理终端", 8,0),
    TERMINAL_MOUNT_SELF("飞手", 99,0),
    TERMINAL_TEST("测试终端", 100,0),


    TERMINAL_PHONE("警务通终端", 1,R.mipmap.img_phone),
    TERMINAL_BODY_WORN_CAMERA("执法记录仪", 10,R.mipmap.img_recorder),
    TERMINAL_UAV("无人机", 11,R.mipmap.img_uav),
    TERMINAL_PDT("PDT终端(350M手台)", 5,R.mipmap.img_hand),
    TERMINAL_LTE("LTE终端", 9, R.mipmap.img_lte),
    TERMINAL_PATROL("船", 13,R.mipmap.img_boat),
    TERMINAL_CAR("警车", 14,0),
    TERMINAL_BALL("布控球", 15,R.mipmap.img_cammera),
    TERMINAL_CAMERA("摄像头", 16, R.mipmap.img_cammera),
    TERMINAL_PERSONNEL("民警", 17,R.mipmap.img_hand),
    TERMINAL_DDT("交管电台", 97,R.mipmap.img_hand),
    TERMINAL_PDT_CAR("350M车载台", 98,R.mipmap.img_hand);


    private String des;//文字介绍
    private int type;//类型标识
    private int rid;//资源图标id

    TerminalEnum(String des,int type,int rid) {
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }
}
