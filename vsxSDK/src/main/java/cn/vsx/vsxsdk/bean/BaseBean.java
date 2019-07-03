package cn.vsx.vsxsdk.bean;

import java.io.Serializable;

import cn.vsx.vsxsdk.VsxSDK;

public class BaseBean implements Serializable {
    /**
     * 平台key
     */
    private String appKey = VsxSDK.getInstance().getAppKey();

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
