package ptt.terminalsdk.bean;

import java.io.Serializable;

public class PowerSaveTimeBean implements Serializable {
    private static final long serialVersionUID = 3183350232810421848L;

    /**
     * success : true
     * msg : 请求成功！
     * data : {"deviceId":34598,"activeTime":"5","dormancyTime":"30","lockScreenTime":"10"}
     */

    private boolean success;
    private String msg;
    private DataBean data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * deviceId : 34598
         * activeTime : 5
         * dormancyTime : 30
         * lockScreenTime : 10
         */

        private int deviceId;
        private String activeTime;
        private String dormancyTime;
        private String lockScreenTime;

        public int getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(int deviceId) {
            this.deviceId = deviceId;
        }

        public String getActiveTime() {
            return activeTime;
        }

        public void setActiveTime(String activeTime) {
            this.activeTime = activeTime;
        }

        public String getDormancyTime() {
            return dormancyTime;
        }

        public void setDormancyTime(String dormancyTime) {
            this.dormancyTime = dormancyTime;
        }

        public String getLockScreenTime() {
            return lockScreenTime;
        }

        public void setLockScreenTime(String lockScreenTime) {
            this.lockScreenTime = lockScreenTime;
        }
    }
}
