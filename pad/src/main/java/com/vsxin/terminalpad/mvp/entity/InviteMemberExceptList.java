package com.vsxin.terminalpad.mvp.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 传递邀请除去的成员uniqueNo集合对象
 */
public class InviteMemberExceptList implements Serializable {
    private List<Integer> list;

    public InviteMemberExceptList() {
    }

    public InviteMemberExceptList(List<Integer> list) {
        this.list = list;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }
}
