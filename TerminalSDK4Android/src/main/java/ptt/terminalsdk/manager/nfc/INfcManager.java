package ptt.terminalsdk.manager.nfc;

import com.alibaba.fastjson.JSONObject;

import ptt.terminalsdk.bean.NfcBusinessType;
import ptt.terminalsdk.bean.NfcPerformBean;

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
    public String getBindString(int bindNo, String bindUniqueNo, int groupNo, String voiceString);

    //获取绑定警情业务的传输数据
    public String getBindWarningString(int bindNo, String bindUniqueNo, int groupNo, String warningId, String voiceString);

    //获取录像业务的传输数据
    public String getVideoString(int state, JSONObject tag, String voiceString);

    //获取实时上报业务的传输数据
    public String getVideoPushString(int state, JSONObject tag, String voiceString);

    //解析业务的传输数据
    public void parseData(String data);

    //执行业务
    public void performBusiness();

    public void performBusinessByTime();

    //更新业务的执行状态
    public void updatePerformBusinessState(NfcBusinessType type);

    //保存执行业务的数据
    public void savePerformBean(NfcPerformBean bean);

    //获取执行业务的数据
    public NfcPerformBean getPerformBean();

    //根据类型的code获取语音字符串
    public String getVoiceStringByCode(NfcBusinessType type);

    //检查是否是警情业务
    public boolean checkIsWarningBusiness();

    //获取标记
    public String getFileTag();

    //获取录像类型
    public int getVideoType();

}
