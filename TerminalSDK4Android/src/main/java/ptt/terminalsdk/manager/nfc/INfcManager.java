package ptt.terminalsdk.manager.nfc;

import com.google.gson.JsonObject;

public interface INfcManager {
    //开启nfc管理类
    public void start();

    //关闭nfc管理类
    public void stop();

    //设置传输数据的字符串
    public void setTransmitData(String data);

    //获取传输数据的字符串
    public String getTransmitData();

    //获取绑定业务的传输数据
    public String getBindString(int bindNo, String bindUniqueNo, int groupNo);

    //获取绑定警情业务的传输数据
    public String getBindWarningString(int bindNo, String bindUniqueNo, int groupNo, String warningId);

    //获取录像业务的传输数据
    public String getVideoString(int state, JsonObject tag);

    //获取实时上报业务的传输数据
    public String getVideoPushString(int state, JsonObject tag);

}
