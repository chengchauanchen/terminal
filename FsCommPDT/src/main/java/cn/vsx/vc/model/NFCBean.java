package cn.vsx.vc.model;

import java.io.Serializable;

public class NFCBean implements Serializable {
    private String policeSentimentId;
    private int groupId;

//    public NFCBean(String policeSentimentId) {
//        this.policeSentimentId = policeSentimentId;
//    }

    public NFCBean(String policeSentimentId, int groupId) {
        this.policeSentimentId = policeSentimentId;
        this.groupId = groupId;
    }

    public String getPoliceSentimentId() {
        return policeSentimentId;
    }

    public void setPoliceSentimentId(String policeSentimentId) {
        this.policeSentimentId = policeSentimentId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
