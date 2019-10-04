package cn.vsx.vsxsdk.constant;

/**
 *
 */
public enum  CommandEnum {
    SdkProcess("返回sdk进程状态",0),//组会话页面
    GroupChat("组会话页面",1),//组会话页面
    IndividualCall("个呼",2),//个呼
    OtherLive("请求别人上报",3),//请求别人上报
    PersonChat("个人会话",4),//个人会话
    SelfLive("自己上报",5),//自己上报
    VoipCall("拨打voip电话",6),
    CreateTempGroup("创建临时组",7),
    changeGroup("转组",8),
    monitorGroup("设置监听组",9),
    pushVideoLive("推送视频",10),
    AddMemberToGroup("添加成员到临时组",11);

    private String name;
    private int type;

    CommandEnum(String name,int type) {
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
