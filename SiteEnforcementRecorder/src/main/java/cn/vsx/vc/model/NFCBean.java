package cn.vsx.vc.model;

import java.io.Serializable;

public class NFCBean implements Serializable {
    private static final long serialVersionUID = 4850535638972746777L;
    private String policeSentimentId;
    private int groupId;
    private String nfcUuid;

    public NFCBean(String nfcUuid) {
        this.nfcUuid = nfcUuid;
    }

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

    public String getNfcUuid() {
        return nfcUuid;
    }

    public void setNfcUuid(String nfcUuid) {
        this.nfcUuid = nfcUuid;
    }

    @Override
    public String toString() {
        return "NFCBean{" +
                "policeSentimentId='" + policeSentimentId + '\'' +
                ", groupId=" + groupId +
                ", nfcUuid='" + nfcUuid + '\'' +
                '}';
    }
}
