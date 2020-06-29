package ptt.terminalsdk.manager.nfc;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import ptt.terminalsdk.bean.NfcBaseBean;
import ptt.terminalsdk.bean.NfcBusinessType;

/**
 * NFC数据交换管理
 */
public class NfcManager implements INfcManager {
    protected Logger logger = Logger.getLogger(this.getClass());
    private static final String LOGGER_TAG = "NfcManager---";
    //记得判空处理
    private Application context;
    //绑定方的MemberNo int类型
    private static final String NO = "no";
    //绑定方的UniqueNo String类型
    private static final String UNO = "uNo";
    //组编号  int类型
    private static final String GNO = "gNo";
    //录像业务：1：开始录像；2：结束录像  int类型
    //上报图像业务：1：开始上报图像；2：结束上报图像  int类型
    private static final String STATE = "state";
    //标记扩展字段(用于和录像文件绑定的字段)  json类型
    private static final String TAG = "tag";
    //标记字段 String类型
    private static final String WID = "wId";

    //nfc传输数据的字符串
    private String transmitDataStr = "";

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what){
//                default:break;
//            }
        }
    };

    public NfcManager(Application context) {
        this.context = context;
    }
    /**
     * 开启nfc管理类
     */
    @Override
    public void start() {
        setTransmitData(null);
    }
    /**
     * 关闭nfc管理类
     */
    @Override
    public void stop() {
        setTransmitData(null);
    }

    /**
     * 设置传输数据的字符串
     */
    @Override
    public synchronized void setTransmitData(String data) {
        transmitDataStr = data;
    }

    /**
     * 获取传输数据的字符串
     * @return
     */
    @Override
    public synchronized String getTransmitData() {
        return transmitDataStr;
    }

    /**
     * 获取绑定业务的传输数据
     * @param bindNo
     * @param bindUniqueNo
     * @param groupNo
     * @return
     */
    @Override
    public String getBindString(int bindNo,String bindUniqueNo,int groupNo){
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.BIND.getCode());
        baseBean.setCode(code);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(NO,bindNo);
        jsonObject.addProperty(UNO,bindUniqueNo);
        jsonObject.addProperty(GNO,groupNo);
        baseBean.setData(jsonObject);
        return new Gson().toJson(baseBean);
    }

    /**
     * 获取绑定警情业务的传输数据
     * @param bindNo
     * @param bindUniqueNo
     * @param groupNo
     * @param warningId
     * @return
     */
    @Override
    public String getBindWarningString(int bindNo,String bindUniqueNo,int groupNo,String warningId){
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.BIND_WARNING.getCode());
        baseBean.setCode(code);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(NO,bindNo);
        jsonObject.addProperty(UNO,bindUniqueNo);
        jsonObject.addProperty(GNO,groupNo);
        JsonObject tagJsonObject = new JsonObject();
        tagJsonObject.addProperty(WID,warningId);
        jsonObject.add(TAG,tagJsonObject);
        baseBean.setData(jsonObject);
        return new Gson().toJson(baseBean);
    }

    /**
     * 获取录像业务的传输数据
     * @param tag
     * @return
     */
    @Override
    public String getVideoString(int state,JsonObject tag){
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.VIDEO.getCode());
        baseBean.setCode(code);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(STATE,state);
        jsonObject.add(TAG,(tag!=null)?tag:new JsonObject());
        baseBean.setData(jsonObject);
        return new Gson().toJson(baseBean);
    }

    /**
     * 获取实时上报业务的传输数据
     * @param tag
     * @return
     */
    @Override
    public String getVideoPushString(int state,JsonObject tag){
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.VIDEO_PUSH.getCode());
        baseBean.setCode(code);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(STATE,state);
        jsonObject.add(TAG,(tag!=null)?tag:new JsonObject());
        baseBean.setData(jsonObject);
        return new Gson().toJson(baseBean);
    }
}
