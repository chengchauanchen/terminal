package cn.vsx.vsxsdk.constant;

/**
 * 同步给第三方app的消息类型
 */
public enum ThirdMessageType {

    //
    NOTIFY_DATA_MESSAGE("接收到普通消息",1),
    NOTIFY_INDIVIDUAL_CALL_IN_COMMING("个呼来了",2),
    GO_WATCH_RTSP("观看上报视频",3),
    NOTIFY_LIVING_IN_COMMING("收到别人请求我开启直播的通知",4),
    NOTIFY_EMERGENCY_VIDEO_LIVE_IN_COMMING("收到强制上报图像的通知",5),
    NOTIFY_MEMBER_ABOUT_TEMP_GROUP("接收警情消息",6);

    private String value;
    private int code;

    ThirdMessageType(String value, int code) {
        this.value = value;
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }}
