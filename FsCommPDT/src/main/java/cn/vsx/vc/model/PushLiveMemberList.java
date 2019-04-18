package cn.vsx.vc.model;

import java.io.Serializable;
import java.util.List;

/**
 * 传递邀请观看成员的uniqueNo集合对象
 */
public class PushLiveMemberList implements Serializable {
    private List<Long> list;

    public PushLiveMemberList() {
    }

    public PushLiveMemberList(List<Long> list) {
        this.list = list;
    }

    public List<Long> getList() {
        return list;
    }

    public void setList(List<Long> list) {
        this.list = list;
    }
}
