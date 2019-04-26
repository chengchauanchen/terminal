package cn.vsx.vc.model;

import java.io.Serializable;

public class TransponToBean implements Serializable {
    private static final long serialVersionUID = -7746663846154030677L;
    private int no;
    private String name;

    public TransponToBean(int no, String name) {
        this.no = no;
        this.name = name;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
