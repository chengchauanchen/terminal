package com.vsxin.terminalpad.mvp.entity;

/**
 * @author qzw
 * <p>
 *    设备bean
 *
 * 地图气泡点击-民警详情页
 * 显示民警身上绑定的终端设备
 */
public class DeviceBean extends BaseBean {

    private PersonnelBean personnel;//民警
    private TerminalBean terminal;//终端设备
    private boolean isPolice;//是否是民警

    public DeviceBean() {
    }

    public PersonnelBean getPersonnel() {
        return personnel;
    }

    public void setPersonnel(PersonnelBean personnel) {
        this.personnel = personnel;
    }

    public TerminalBean getTerminal() {
        return terminal;
    }

    public void setTerminal(TerminalBean terminal) {
        this.terminal = terminal;
    }

    public boolean isPolice() {
        return isPolice;
    }

    public void setPolice(boolean police) {
        isPolice = police;
    }
}
