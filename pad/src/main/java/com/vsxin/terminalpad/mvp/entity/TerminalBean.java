package com.vsxin.terminalpad.mvp.entity;

/**
 * 终端/装备
 *
 * "terminalType": "TERMINAL_PHONE",
 *  "terminalGroupNo": "100040",
 *  "terminalUniqueNo": "156015518354393289"
 *
 *  	"account": null,
 * 	"gb28181No": null,
 * 	"group": "72020996",
 * 	"lteNo": null,
 * 	"pdtNo": "72024363",
 * 	"lat": 30.63395,
 * 	"lng": 114.257317,
 */
public class TerminalBean extends BaseBean {
    private String terminalType;//终端类型
    private String terminalGroupNo;//阻断组号
    private String terminalUniqueNo;//终端UniqueNo


    private String account;
    private String gb28181No;
    private String group;
    private String lteNo;
    private String pdtNo;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getGb28181No() {
        return gb28181No;
    }

    public void setGb28181No(String gb28181No) {
        this.gb28181No = gb28181No;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getLteNo() {
        return lteNo;
    }

    public void setLteNo(String lteNo) {
        this.lteNo = lteNo;
    }

    public String getPdtNo() {
        return pdtNo;
    }

    public void setPdtNo(String pdtNo) {
        this.pdtNo = pdtNo;
    }

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
