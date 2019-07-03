package cn.vsx.vc.jump.bean;

import java.io.Serializable;

public class BaseBean implements Serializable {
    /**
     * 平台key
     */
    private String appKey;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
