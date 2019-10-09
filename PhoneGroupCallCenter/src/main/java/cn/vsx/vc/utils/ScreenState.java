package cn.vsx.vc.utils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/19
 * 描述：
 * 修订历史：
 */
public enum ScreenState{
    SCREEN_ORIENTATION_LANDSCAPE("横屏",0),
    SCREEN_ORIENTATION_REVERSE_LANDSCAPE("反向横屏",8),
    SCREEN_ORIENTATION_PORTRAIT("竖屏",1),
    SCREEN_ORIENTATION_UNKNOWN("未知",-1);

    private String value;
    private int code;
    private String name;
    private static Map<Integer, ScreenState> code2ScreenState = new HashMap<>();

    ScreenState(String value, int code) {
        this.setValue(value);
        this.setCode(code);
    }

    ScreenState(String value, int code, String name) {
        this.setValue(value);
        this.setCode(code);
        this.setName(name);
    }

    static {
        for (ScreenState ScreenState : EnumSet.allOf(ScreenState.class)) {
            code2ScreenState.put(ScreenState.getCode(), ScreenState);
        }
    }

    public static ScreenState getInstanceByCode(int code) {
        return code2ScreenState.get(code);
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
