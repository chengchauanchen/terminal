package com.vsxin.terminalpad.mvp.entity;

/**
 * 终端/装备
 *
 * "terminalType": "TERMINAL_PHONE",
 *  "terminalGroupNo": "100040",
 *  "terminalUniqueNo": "156015518354393289"
 */
public class TerminalBean extends BaseBean {
    private String terminalType;//终端类型
    private String terminalGroupNo;//阻断组号
    private String terminalUniqueNo;//终端UniqueNo

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getTerminalGroupNo() {
        return terminalGroupNo;
    }

    public void setTerminalGroupNo(String terminalGroupNo) {
        this.terminalGroupNo = terminalGroupNo;
    }

    public String getTerminalUniqueNo() {
        return terminalUniqueNo;
    }

    public void setTerminalUniqueNo(String terminalUniqueNo) {
        this.terminalUniqueNo = terminalUniqueNo;
    }
}
