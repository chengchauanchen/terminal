package com.vsxin.terminalpad.mvp.entity;

import com.ixiaoma.xiaomabus.architecture.bean.BaseBean;

import java.util.List;

/**
 * 民警实体Bean
 */
public class PersonnelBean extends BaseBean {
    private String personnelName;//民警名称
    private String personnelNo;//警号
    private String personnelLat;
    private String personnelLng;
    private List<TerminalBean> terminalDtoList;//终端list

    public String getPersonnelName() {
        return personnelName;
    }

    public void setPersonnelName(String personnelName) {
        this.personnelName = personnelName;
    }

    public String getPersonnelNo() {
        return personnelNo;
    }

    public void setPersonnelNo(String personnelNo) {
        this.personnelNo = personnelNo;
    }

    public String getPersonnelLat() {
        return personnelLat;
    }

    public void setPersonnelLat(String personnelLat) {
        this.personnelLat = personnelLat;
    }

    public String getPersonnelLng() {
        return personnelLng;
    }

    public void setPersonnelLng(String personnelLng) {
        this.personnelLng = personnelLng;
    }

    public List<TerminalBean> getTerminalDtoList() {
        return terminalDtoList;
    }

    public void setTerminalDtoList(List<TerminalBean> terminalDtoList) {
        this.terminalDtoList = terminalDtoList;
    }
}

