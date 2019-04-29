package cn.vsx.vc.model;

import java.io.Serializable;
import java.util.List;

/**
 * 传递邀请观看成员的uniqueNo集合对象
 */
public class PushLiveMemberList implements Serializable {
    private List<String> list;

    public PushLiveMemberList() {
    }

    public PushLiveMemberList(List<String> list) {
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
