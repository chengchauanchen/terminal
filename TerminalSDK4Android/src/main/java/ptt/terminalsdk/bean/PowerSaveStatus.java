package ptt.terminalsdk.bean;

/**
 * 省电模式的状态
 */
public enum PowerSaveStatus {
    ACTIVITY(1),
    PRE_SAVE(2),
    SAVE(3);
    private int code;
    PowerSaveStatus(int code) {
        this.setCode(code);
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
