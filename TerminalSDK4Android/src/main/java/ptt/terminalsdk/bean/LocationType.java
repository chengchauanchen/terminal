package ptt.terminalsdk.bean;

/**
 * 定位的类型
 */
public enum LocationType {
    GPS(1),
    BD(2),
    SF(3);
    private int code;
    LocationType(int code) {
        this.setCode(code);
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
