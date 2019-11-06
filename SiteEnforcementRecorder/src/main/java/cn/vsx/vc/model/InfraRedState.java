package cn.vsx.vc.model;

/**
 * 比特星存储目录
 */
public enum InfraRedState {
    CLOSE(0),
    OPEN(1),
    AUTO(2);
    private int code;
    InfraRedState(int code) {
        this.setCode(code);
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
