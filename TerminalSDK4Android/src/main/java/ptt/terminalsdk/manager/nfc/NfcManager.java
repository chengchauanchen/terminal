package ptt.terminalsdk.manager.nfc;

import android.app.Application;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.R;
import ptt.terminalsdk.bean.NfcBaseBean;
import ptt.terminalsdk.bean.NfcBusinessType;
import ptt.terminalsdk.bean.NfcBusinessVideoType;
import ptt.terminalsdk.bean.NfcDataBean;
import ptt.terminalsdk.bean.NfcPerformBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiverVideoEventByPassiveHandler;
import ptt.terminalsdk.receiveHandler.ReceiverVideoPushEventByPassiveHandler;
import ptt.terminalsdk.tools.ToastUtil;

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
    //标记字段 警情id String类型
    public static final String WID = "wId";
    //标记字段 录像类型 String 类型
    public static final String VTYPE = "vType";
    //标记字段 上报图像类型 String 类型
    public static final String VPTYPE = "vpType";

    //nfc传输数据的字符串
    private String transmitDataStr = "";

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
     *
     * @return
     */
    @Override
    public synchronized String getTransmitData() {
        return transmitDataStr;
    }

    /**
     * 获取绑定业务的传输数据
     *
     * @param bindNo
     * @param bindUniqueNo
     * @param groupNo
     * @return
     */
    @Override
    public String getBindString(int bindNo, String bindUniqueNo, int groupNo, String voiceString) {
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.BIND.getCode());
        baseBean.setCode(code);
        NfcDataBean dataBean = new NfcDataBean();
        dataBean.setNo(bindNo);
        dataBean.setuNo(bindUniqueNo);
        dataBean.setgNo(groupNo);
        List<String> voices = new ArrayList<>();
        voices.add(voiceString);
        dataBean.setvStr(voices);
        baseBean.setData(dataBean);
        return JSON.toJSONString(baseBean);
    }

    /**
     * 获取绑定警情业务的传输数据
     *
     * @param bindNo
     * @param bindUniqueNo
     * @param groupNo
     * @param warningId
     * @return
     */
    @Override
    public String getBindWarningString(int bindNo, String bindUniqueNo, int groupNo, String warningId, String voiceString) {
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.BIND_WARNING.getCode());
        baseBean.setCode(code);
        NfcDataBean dataBean = new NfcDataBean();
        dataBean.setNo(bindNo);
        dataBean.setuNo(bindUniqueNo);
        dataBean.setgNo(groupNo);
        JSONObject tagJsonObject = new JSONObject();
        tagJsonObject.put(WID, warningId);
        dataBean.setTag(tagJsonObject);
        List<String> voices = new ArrayList<>();
        voices.add(voiceString);
        dataBean.setvStr(voices);
        baseBean.setData(dataBean);
        return JSON.toJSONString(baseBean);
    }

    /**
     * 获取录像业务的传输数据
     *
     * @param tag
     * @return
     */
    @Override
    public String getVideoString(int state, JSONObject tag, String voiceString) {
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.VIDEO.getCode());
        baseBean.setCode(code);
        NfcDataBean dataBean = new NfcDataBean();
        dataBean.setState(state);
        dataBean.setTag((tag != null) ? tag : new JSONObject());
        List<String> voices = new ArrayList<>();
        voices.add(voiceString);
        dataBean.setvStr(voices);
        baseBean.setData(dataBean);
        return JSON.toJSONString(baseBean);
    }

    /**
     * 获取实时上报业务的传输数据
     *
     * @param tag
     * @return
     */
    @Override
    public String getVideoPushString(int state, JSONObject tag, String voiceString) {
        NfcBaseBean baseBean = new NfcBaseBean();
        List<Integer> code = new ArrayList<>();
        code.add(NfcBusinessType.VIDEO_PUSH.getCode());
        baseBean.setCode(code);
        NfcDataBean dataBean = new NfcDataBean();
        dataBean.setState(state);
        dataBean.setTag((tag != null) ? tag : new JSONObject());
        List<String> voices = new ArrayList<>();
        voices.add(voiceString);
        dataBean.setvStr(voices);
        baseBean.setData(dataBean);
        return JSON.toJSONString(baseBean);
    }

    /**
     * 解析数据
     *
     * @param content
     * @return
     */
    @Override
    public void parseData(String content) {
        logger.info(LOGGER_TAG + "parseData-" + content);
        if (TextUtils.isEmpty(content)) {
            ToastUtil.showToast(context.getString(R.string.nfc_transmit_data_error_null));
            return;
        }
        NfcBaseBean baseBean = null;
        try {
            baseBean = new Gson().fromJson(content, NfcBaseBean.class);
        } catch (Exception e) {
            e.printStackTrace();
            baseBean = null;
        }
        if (baseBean == null) {
            ToastUtil.showToast(context.getString(R.string.nfc_transmit_data_error_invalid_format));
            return;
        }
        List<Integer> code = baseBean.getCode();
        NfcDataBean data = baseBean.getData();
        if (code == null || code.isEmpty() || data == null) {
            ToastUtil.showToast(context.getString(R.string.nfc_transmit_data_error));
            return;
        }
        //保存执行的命令
        NfcPerformBean bean = new NfcPerformBean();
        LinkedHashMap<Integer, Boolean> map = new LinkedHashMap<>();
        for (Integer integer : code) {
            map.put(integer, false);
        }
        bean.setCodeState(map);
        bean.setData(data);
        savePerformBean(bean);
    }

    /**
     * 开始执行业务
     */
    @Override
    public void performBusiness() {
        try {
            NfcPerformBean bean = getPerformBean();
            logger.info(LOGGER_TAG + "performBusiness-" + bean);
            if (bean != null && bean.getCodeState() != null
                    && !bean.getCodeState().isEmpty() && bean.getData() != null) {
                LinkedHashMap<Integer, Boolean> codeState = bean.getCodeState();
                Integer performKey = -1;
                Set<Map.Entry<Integer, Boolean>> set = codeState.entrySet();
                Iterator<Map.Entry<Integer, Boolean>> iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = iterator.next();
                    Integer key = (Integer) entry.getKey();
                    Boolean state = (Boolean) entry.getValue();
                    if (!state) {
                        performKey = key;
                        break;
                    }
                }
                //判断是否有可以执行的code
                NfcBusinessType type = NfcBusinessType.getInstanceByCode(performKey);
                NfcDataBean data = bean.getData();
                if (type != null) {
                    switch (type) {
                        case BIND:
                        case BIND_WARNING:
                            performBind(type, data);
                            break;
                        case VIDEO:
                            performVideo(data);
                            break;
                        case VIDEO_PUSH:
                            performVideoPush(data);
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 延时执行业务
     */
    @Override
    public void performBusinessByTime() {
        MyTerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
            @Override
            public void run() {
                performBusiness();
            }
        }, 1000);
    }

    /**
     * 更新业务的执行状态
     *
     * @param type
     */
    @Override
    public synchronized void updatePerformBusinessState(NfcBusinessType type) {
        try {
            if (type != null) {
                NfcPerformBean bean = getPerformBean();
                if (bean != null && bean.getCodeState() != null
                        && !bean.getCodeState().isEmpty() && bean.getCodeState().containsKey(type.getCode())) {
                    bean.getCodeState().put(type.getCode(), true);
                    savePerformBean(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据类型的code获取语音字符串
     *
     * @param type
     * @return
     */
    @Override
    public String getVoiceStringByCode(NfcBusinessType type) {
        String voiceString = "";
        NfcPerformBean bean = null;
        if (type == null) {
            return voiceString;
        }
        try {
            bean = getPerformBean();
            if (bean != null && bean.getCodeState() != null
                    && !bean.getCodeState().isEmpty() && bean.getData() != null) {
                NfcDataBean dataBean = bean.getData();
                List<String> voices = dataBean.getvStr();
                if (voices == null || voices.isEmpty()) {
                    return voiceString;
                }
                int index = getCodeIndex(type.getCode(), bean);
                if (index >= 0 && index < voices.size()) {
                    voiceString = voices.get(index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(LOGGER_TAG + "getVoiceStringByCode-type:" + type + "-voiceString:" + voiceString + "-bean:" + bean);
        return voiceString;
    }

    /**
     * 检查是否是警情业务
     *
     * @return
     */
    @Override
    public boolean checkIsWarningBusiness() {
        boolean result = false;
        try {
            NfcPerformBean bean = MyTerminalFactory.getSDK().getNfcManager().getPerformBean();
            result = (bean != null && !TextUtils.isEmpty(getWarningId(bean.getData())));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getFileTag() {
        String result = "";
        try {
            NfcPerformBean bean = getPerformBean();
            if (bean != null && bean.getData() != null && bean.getData().getTag() != null) {
                result = bean.getData().getTag().toJSONString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int getVideoType() {
        try {
            NfcPerformBean bean = getPerformBean();
            if (bean != null && bean.getData() != null && bean.getData().getTag() != null
                    && bean.getData().getTag().containsKey(VTYPE)) {
                JSONObject jsonObject = bean.getData().getTag();
                NfcBusinessVideoType type = NfcBusinessVideoType.valueOf(jsonObject.getString(VTYPE));
                return type.getCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 保存执行业务的数据
     *
     * @param bean
     */
    @Override
    public synchronized void savePerformBean(NfcPerformBean bean) {
        TerminalFactory.getSDK().putParam(Params.RECORDER_NFC_PERFORM_BEAN, (bean != null) ? JSON.toJSONString(bean) : "");
    }

    /**
     * 获取执行业务的数据
     *
     * @return
     */
    @Override
    public synchronized NfcPerformBean getPerformBean() {
        String json = TerminalFactory.getSDK().getParam(Params.RECORDER_NFC_PERFORM_BEAN);
        return (TextUtils.isEmpty(json)) ? null : JSON.parseObject(json, NfcPerformBean.class);
    }

    /**
     * 绑定账号
     *
     * @param type
     * @param data
     */
    private void performBind(NfcBusinessType type, NfcDataBean data) {
        try {
            if (data != null) {
                //更新执行的状态
                updatePerformBusinessState(type);
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
                    String warningId = getWarningId(data);
                    TerminalFactory.getSDK().getRecorderBindManager().requestBind(data.getNo(), uniqueNo, data.getgNo(), warningId);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录像
     *
     * @param data
     */
    private void performVideo(NfcDataBean data) {
        try {
            if (data != null) {
                //更新执行的状态
                updatePerformBusinessState(NfcBusinessType.VIDEO);
                String type = "";
                if (data.getTag() != null && data.getTag().containsKey(VTYPE)) {
                    JSONObject jsonObject = data.getTag();
                    type = jsonObject.getString(VTYPE);
                }
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverVideoEventByPassiveHandler.class, data.getState(), type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上报图像
     *
     * @param data
     */
    private void performVideoPush(NfcDataBean data) {
        try {
            if (data != null) {
                //更新执行的状态
                updatePerformBusinessState(NfcBusinessType.VIDEO_PUSH);
                String type = "";
                if (data.getTag() != null && data.getTag().containsKey(VPTYPE)) {
                    JSONObject jsonObject = data.getTag();
                    type = jsonObject.getString(VPTYPE);
                }
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverVideoPushEventByPassiveHandler.class, data.getState(), type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取警情ID
     *
     * @param data
     * @return
     */
    public String getWarningId(NfcDataBean data) {
        String warningId = "";
        try {
            if (data != null) {
                JSONObject jsonObject = data.getTag();
                if (jsonObject != null && jsonObject.containsKey(WID)) {
                    warningId = jsonObject.getString(WID);
                }
                if (TextUtils.isEmpty(warningId)) {
                    warningId = "";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return warningId;
    }

    /**
     * 根据类型的code获取执行数据的索引
     *
     * @param key
     * @param bean
     * @return
     */
    private int getCodeIndex(int key, NfcPerformBean bean) {
        int result = -1;
        try {
            if (bean != null && bean.getCodeState() != null
                    && !bean.getCodeState().isEmpty()) {
                LinkedHashMap<Integer, Boolean> codeState = bean.getCodeState();
                Set<Map.Entry<Integer, Boolean>> set = codeState.entrySet();
                Iterator<Map.Entry<Integer, Boolean>> iterator = set.iterator();
                int index = -1;
                while (iterator.hasNext()) {
                    index++;
                    Map.Entry entry = iterator.next();
                    Integer k = (Integer) entry.getKey();
                    if (key == k) {
                        result = index;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info(LOGGER_TAG + "getCodeIndex-key:" + key + "-bean:" + bean);
        return result;
    }
}
