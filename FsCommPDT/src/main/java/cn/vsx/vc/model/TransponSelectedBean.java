package cn.vsx.vc.model;

import java.io.Serializable;
import java.util.ArrayList;

public class TransponSelectedBean implements Serializable {
    private static final long serialVersionUID = 2776614365704442518L;

    private ArrayList<ContactItemBean> list;

    public TransponSelectedBean(ArrayList<ContactItemBean> list) {
        this.list = list;
    }

    public ArrayList<ContactItemBean> getList() {
        return list;
    }

    public void setList(ArrayList<ContactItemBean> list) {
        this.list = list;
    }
}
