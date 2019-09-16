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

//    public static final String PATROL = "patrol";//巡逻船
//    public static final String PHONE = "phone";//警务通
//    public static final String HAND = "hand";//电台
//    public static final String LTE = "lte";//LTE
//    public static final String UAV = "uav";//无人机
//    public static final String VIDEO = "video";//执法仪
//    public static final String CAMERA = "camera";//摄像头
//    public static final String BALL = "ball";//布控球
//    public static final String DRONE_OPERATOR = "droneOperator";//飞手

    /**
     * 1：警务通 2：LTE 3：摄像头 4：手台 5：警车 6：执法仪 7：无人机 8：警员 9：巡逻船 10：布控球
     */
    public static final int PHONE = 1;//警务通
    public static final int LTE = 2;//LTE
    public static final int CAMERA = 3;//摄像头
    public static final int HAND = 4;//电台
    public static final int CAR = 5;//警车
    public static final int VIDEO = 6;//执法仪
    public static final int UAV = 7;//无人机
    public static final int POLICE = 8;//警员
    public static final int PATROL = 9;//巡逻船
    public static final int BALL = 10;//布控球

    /**
     * 获取 图层类型 列表数据
     * @return
     */
    public static List<MemberTypeEnum> getMemberTypeList(){
        List<MemberTypeEnum> memberTypeEnums = new ArrayList<>();
        memberTypeEnums.add(MemberTypeEnum.PHONE);//警务通
        memberTypeEnums.add(MemberTypeEnum.LTE);//LTE
        memberTypeEnums.add(MemberTypeEnum.CAMERA);//摄像头
        memberTypeEnums.add(MemberTypeEnum.HAND);//电台
        memberTypeEnums.add(MemberTypeEnum.CAR);//警车
        memberTypeEnums.add(MemberTypeEnum.VIDEO);//执法仪
        memberTypeEnums.add(MemberTypeEnum.UAV);//无人机
        memberTypeEnums.add(MemberTypeEnum.POLICE);//警员
        memberTypeEnums.add(MemberTypeEnum.PATROL);//巡逻船
        memberTypeEnums.add(MemberTypeEnum.BALL);//布控球
        return memberTypeEnums;

    }

}
