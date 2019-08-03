package com.vsxin.terminalpad.mvp.contract.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzw
 *
 * 图层类型
 */
public class MemberTypeConstans {
    /**
     *     PATROL("巡逻船", "patrol"),
     *     PHONE("警务通", "phone"),
     *     HAND("电台", "hand"),
     *     LTE("LTE", "lte"),
     *     UAV("无人机", "uav"),
     *     VIDEO("执法仪", "video"),
     *     CAMERA("摄像头", "camera");
     */

    public static final String PATROL = "patrol";//巡逻船
    public static final String PHONE = "phone";//警务通
    public static final String HAND = "hand";//电台
    public static final String LTE = "lte";//LTE
    public static final String UAV = "uav";//无人机
    public static final String VIDEO = "video";//执法仪
    public static final String CAMERA = "camera";//摄像头


    /**
     * 获取 图层类型 列表数据
     * @return
     */
    public static List<MemberTypeEnum> getMemberTypeList(){
        List<MemberTypeEnum> memberTypeEnums = new ArrayList<>();
        memberTypeEnums.add(MemberTypeEnum.PATROL);
        memberTypeEnums.add(MemberTypeEnum.PHONE);
        memberTypeEnums.add(MemberTypeEnum.HAND);
        memberTypeEnums.add(MemberTypeEnum.LTE);
        memberTypeEnums.add(MemberTypeEnum.UAV);
        memberTypeEnums.add(MemberTypeEnum.VIDEO);
        memberTypeEnums.add(MemberTypeEnum.CAMERA);
        return memberTypeEnums;

    }

}
