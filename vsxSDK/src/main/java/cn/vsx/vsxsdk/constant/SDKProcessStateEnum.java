package cn.vsx.vsxsdk.constant;

/**
 * SDK 进程开启状态
 */
public enum SDKProcessStateEnum {
    DEFAULT_STATE("默认状态",0),//未开自启动权限
    SELF_STARTUP_PERMISSION("开了自启动权限",1),//开了自启动权限
    EXTERNAL_PERMISSION("开了存储权限",2),//开启权限-未开存储权限
    NO_EXTERNAL_PERMISSION("未开存储权限",3),//开启权限-未开存储权限
    NO_WINDOW_PERMISSION("未开悬浮窗权限",4),//悬浮窗权限
    HAVE_WINDOW_PERMISSION("开了悬浮窗权限",5),//悬浮窗权限
    NO_AUTH_URL("认证地址有误",4),//认证地址有误
    NO_PHONE_TYPE("不是警务通类型",5),//不是警务通类型
    SUCCESS_STATE("正常",6);

    private String name;
    private int type;

    SDKProcessStateEnum(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }}
