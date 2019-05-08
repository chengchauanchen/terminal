package cn.vsx.vc.model;

public class RorationLiveData {

    /**
     * liveNo : 123456789
     * liveUniqueNo : 123456789
     * liveName : 名字
     * callId : 9057663006341661848
     * deviceId : 9057663006341661848
     */

    private int liveNo;
    private long liveUniqueNo;
    private String liveName;
    private String callId;
    private String deviceId;

    public int getLiveNo() {
        return liveNo;
    }

    public void setLiveNo(int liveNo) {
        this.liveNo = liveNo;
    }

    public long getLiveUniqueNo() {
        return liveUniqueNo;
    }

    public void setLiveUniqueNo(long liveUniqueNo) {
        this.liveUniqueNo = liveUniqueNo;
    }

    public String getLiveName() {
        return liveName;
    }

    public void setLiveName(String liveName) {
        this.liveName = liveName;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "RorationLiveData{" +
                "liveNo=" + liveNo +
                ", liveUniqueNo=" + liveUniqueNo +
                ", liveName='" + liveName + '\'' +
                ", callId='" + callId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
