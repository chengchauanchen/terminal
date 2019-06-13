package cn.vsx.vc.model;

import java.io.Serializable;

public class InviteMemberLiverMember implements Serializable {

    private static final long serialVersionUID = -3058182818269501485L;
    public int memberNo;
    private long uniqueNo;


    public InviteMemberLiverMember(int memberNo, long uniqueNo) {
        this.memberNo = memberNo;
        this.uniqueNo = uniqueNo;
    }

    public int getMemberNo() {
        return memberNo;
    }

    public void setMemberNo(int memberNo) {
        this.memberNo = memberNo;
    }

    public long getUniqueNo() {
        return uniqueNo;
    }

    public void setUniqueNo(long uniqueNo) {
        this.uniqueNo = uniqueNo;
    }
}
