package cn.vsx.vc.model;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 东湖 设备类型
 *
 *
 *     /**
 *      * 1：警务通 2：LTE 3：摄像头 4：手台 5：警车 6：执法仪 7：无人机 8：警员 9：巡逻船 10：布控球
 *      */
// *public static final int PHONE=1;//警务通
//        *public static final int LTE=2;//LTE
//        *public static final int CAMERA=3;//摄像头
//        *public static final int HAND=4;//电台
//        *public static final int CAR=5;//警车
//        *public static final int VIDEO=6;//执法仪
//        *public static final int UAV=7;//无人机
//        *public static final int POLICE=8;//警员
//        *public static final int PATROL=9;//巡逻船
//        *public static final int BALL=10;//布控球

public enum DongHuTerminalType implements Serializable {
    TERMINAL_PHONE("警务通终端", 1),
    TERMINAL_TETRA("Tetra终端", 2),
    TERMINAL_PAD("Pad终端", 3),
    TERMINAL_HDMI("HDMI终端", 4),
    TERMINAL_PC("PC终端", 6),
    TERMINAL_SUPER("超级终端", 7),
    TERMINAL_PROXY("代理终端", 8),
    TERMINAL_BODY_WORN_CAMERA("执法记录仪", 10),
    TERMINAL_UAV("无人机", 11),
    TERMINAL_TEST("测试终端", 100),

    //东湖绑定设备类型
    TERMINAL_PDT("PDT终端(350M)", 5),
    TERMINAL_LTE("LTE终端", 9, "@ZILTE"),
    TERMINAL_PATROL("船", 13),
    TERMINAL_CAR("警车", 14);


    private static final long serialVersionUID = 2L;

    private String value;
    private int code;
    private String name;
    private static Map<Integer, DongHuTerminalType> code2TerminalMemberType = new HashMap<>();

    DongHuTerminalType(String value, int code) {
        this.setValue(value);
        this.setCode(code);
    }

    private DongHuTerminalType(String value, int code, String name) {
        this.setValue(value);
        this.setCode(code);
        this.setName(name);
    }

    static {
        for (DongHuTerminalType TerminalMemberType : EnumSet.allOf(DongHuTerminalType.class)) {
            code2TerminalMemberType.put(TerminalMemberType.getCode(), TerminalMemberType);
        }
    }

    public static DongHuTerminalType getInstanceByCode(int code) {
        return code2TerminalMemberType.get(code);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
