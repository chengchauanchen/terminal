package com.vsxin.terminalpad.mvp.contract.constant;

import com.vsxin.terminalpad.R;

import cn.vsx.hamster.common.TerminalMemberType;

/**
 * @author qzw
 *  图层类型
 */
public enum MemberTypeEnum {
    /**
     * 图层类型 暂只支持这7个
     */
    PATROL("巡逻船", MemberTypeConstans.PATROL, R.mipmap.ic_patrol_layer,true,null),
    PHONE("警务通", MemberTypeConstans.PHONE,R.mipmap.ic_phone_layer,true,TerminalMemberType.TERMINAL_PHONE),
    HAND("电台", MemberTypeConstans.HAND,R.mipmap.ic_hand_layer,true,null),
    LTE("LTE", MemberTypeConstans.LTE,R.mipmap.ic_lte_layer,true,TerminalMemberType.TERMINAL_LTE),
    UAV("无人机", MemberTypeConstans.UAV,R.mipmap.ic_uav_layer,true,TerminalMemberType.TERMINAL_UAV),
    VIDEO("执法仪", MemberTypeConstans.VIDEO,R.mipmap.iv_video_layer,true,TerminalMemberType.TERMINAL_BODY_WORN_CAMERA),
    CAMERA("摄像头", MemberTypeConstans.CAMERA,R.mipmap.ic_camera_layer,true,null),
    BALL("布控球", MemberTypeConstans.BALL,R.mipmap.ic_camera_layer,true,null),
    DRONE_OPERATOR("飞手", MemberTypeConstans.DRONE_OPERATOR,0,true,null);

    private String name;
    private String type;
    private int resId;
    private boolean isCheck;
    private TerminalMemberType terminalMemberType;

    MemberTypeEnum(String name, String type, int resId, boolean isCheck, TerminalMemberType terminalMemberType) {
        this.name = name;
        this.type = type;
        this.resId = resId;
        this.isCheck = isCheck;
        this.terminalMemberType = terminalMemberType;
    }


    public TerminalMemberType getTerminalMemberType() {
        return terminalMemberType;
    }

    public void setTerminalMemberType(TerminalMemberType terminalMemberType) {
        this.terminalMemberType = terminalMemberType;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }


    @Override
    public String toString() {
        return "name:"+name+",type:"+type+",resId:"+resId+",isCheck:"+isCheck;
    }}


