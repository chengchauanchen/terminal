package ptt.terminalsdk.bean;

import java.io.Serializable;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/8
 * 描述：
 * 修订历史：
 */
public class MediaServerInfoBean implements Serializable {

    private static final long serialVersionUID = -1947689681631663196L;
    private String  mediaServerIp;
    private int  mediaServerPort;
    private int mediaServerApiPort;

    public MediaServerInfoBean() {
    }

    public String getMediaServerIp() {
        return mediaServerIp;
    }

    public void setMediaServerIp(String mediaServerIp) {
        this.mediaServerIp = mediaServerIp;
    }

    public int getMediaServerPort() {
        return mediaServerPort;
    }

    public void setMediaServerPort(int mediaServerPort) {
        this.mediaServerPort = mediaServerPort;
    }

    public int getMediaServerApiPort() {
        return mediaServerApiPort;
    }

    public void setMediaServerApiPort(int mediaServerApiPort) {
        this.mediaServerApiPort = mediaServerApiPort;
    }

    @Override
    public String toString() {
        return "MediaServerInfoBean{" +
                "mediaServerIp='" + mediaServerIp + '\'' +
                ", mediaServerPort=" + mediaServerPort +
                ", mediaServerApiPort=" + mediaServerApiPort +
                '}';
    }
}
