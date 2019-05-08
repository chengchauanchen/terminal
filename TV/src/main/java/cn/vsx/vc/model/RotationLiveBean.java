package cn.vsx.vc.model;

import java.io.Serializable;

public class RotationLiveBean implements Serializable {

    private int type;
    private RorationLiveData data;

    public RotationLiveBean() {
    }

    public RotationLiveBean(int type, RorationLiveData data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RorationLiveData getData() {
        return data;
    }

    public void setData(RorationLiveData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RotationLiveBean{" +
                "type='" + type + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
