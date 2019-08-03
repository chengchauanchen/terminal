package com.vsxin.terminalpad.mvp.contract.constant;

import com.vsxin.terminalpad.R;

/**
 * @author qzw
 *  图层类型
 */
public enum MemberTypeEnum {
    /**
     * 图层类型 暂只支持这7个
     */
    PATROL("巡逻船", MemberTypeConstans.PATROL, R.mipmap.ic_patrol_layer,true),
    PHONE("警务通", MemberTypeConstans.PHONE,R.mipmap.ic_phone_layer,true),
    HAND("电台", MemberTypeConstans.HAND,R.mipmap.ic_hand_layer,true),
    LTE("LTE", MemberTypeConstans.LTE,R.mipmap.ic_lte_layer,true),
    UAV("无人机", MemberTypeConstans.UAV,R.mipmap.ic_uav_layer,true),
    VIDEO("执法仪", MemberTypeConstans.VIDEO,R.mipmap.iv_video_layer,true),
    CAMERA("摄像头", MemberTypeConstans.CAMERA,R.mipmap.ic_camera_layer,true);

    private String name;
    private String type;
    private int resId;
    private boolean isCheck;

    MemberTypeEnum(String name, String type,int resId,boolean isCheck) {
        this.name = name;
        this.type = type;
        this.resId = resId;
        this.isCheck = isCheck;
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


