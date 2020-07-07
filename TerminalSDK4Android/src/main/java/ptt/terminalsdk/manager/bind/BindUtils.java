package ptt.terminalsdk.manager.bind;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.TianjinDeviceBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import ptt.terminalsdk.bean.BoundDevice;
import ptt.terminalsdk.bean.WorkBindEnum;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.listener.BandDeviceListener;

import static com.alibaba.fastjson.JSON.parseObject;

public class BindUtils {
        public static final String RESULT_CODE = "code";
        public static final String RESULT_MSG = "msg";
        public static final String RESULT_DATA = "data";
        public static final String RESULT_RECORDS = "records";

    private static String getBindUrl(){
        String serverIp = TerminalFactory.getSDK().getParam(Params.HTTP_IP, "");
        int serverPort = TerminalFactory.getSDK().getParam(Params.HTTP_PORT, 0);
        return "http://"+serverIp+":"+serverPort;
    }

    /**
     * 获取当前用户绑定设备
     *
     * @return
     */
    public static Observable<List<BoundDevice>> getbindDevicesObservable() {
        return Observable.fromCallable(BindUtils::getbindDevices);
    }

    public static List<BoundDevice> getbindDevices() {
        Map<String, String> param = new HashMap<>();
        param.put("uniqueNo", MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "");
        param.put("status", "BIND");
        param.put("from", "android");
        final String url = getBindUrl() + "/ldcs/bindRelationship/page";
        String result = MyTerminalFactory.getSDK().getHttpClient().get(url, param);
        List<BoundDevice> deviceList = new ArrayList<>();
        try {
            JSONObject jsonObject = parseObject(result);
            JSONObject data = jsonObject.getJSONObject(RESULT_DATA);
            String arrya = data.getString(RESULT_RECORDS);
            deviceList = JSON.parseArray(arrya, BoundDevice.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceList;
    }

    /**
     * 根据部门获取可绑定的列表
     * @param terminalType
     * @return
     */
    public static Observable<List<TianjinDeviceBean>> getListByTerminalType(String terminalType){
        int deptId = TerminalFactory.getSDK().getParam(Params.DIRECTLY_DEP_ID, 0);
        return  Observable.fromCallable(() -> getAllList(deptId+"", terminalType))
                .subscribeOn(Schedulers.io());
    }

    /**
     * 根据部门编号和设备类型获取可绑定的设备列表数据
     *
     * @return
     */
    public static List<TianjinDeviceBean> getAllList(String deptNo, String type) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("deptId", deptNo);
        paramsMap.put("status", "");
        paramsMap.put("deviceSubType", type);
        paramsMap.put("gps", "false");
        List<TianjinDeviceBean> tianjinDeviceBeans = new ArrayList<>();
        String url = getBindUrl() + "/ldcs/device/list";
        try {
            String result = TerminalFactory.getSDK().getHttpClient().get(url, paramsMap);
            JSONObject json = parseObject(result);
            JSONObject data = json.getJSONObject(RESULT_DATA);
            JSONArray jsonArray = data.getJSONArray(RESULT_RECORDS);
            if(jsonArray!=null&&!jsonArray.isEmpty()){
                for(int i = 0; i < jsonArray.size(); i++){
                    TianjinDeviceBean tianjinDeviceBean = new TianjinDeviceBean();
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    tianjinDeviceBean.setId(jsonObject.getString("id"));
                    tianjinDeviceBean.setName(jsonObject.getString("name"));
                    tianjinDeviceBean.setNo(jsonObject.getString("no"));
                    tianjinDeviceBean.setCarNo(jsonObject.getString("no"));
                    tianjinDeviceBean.setAccountId(jsonObject.getString("accountNo"));
                    tianjinDeviceBean.setStatus(jsonObject.getString("status"));
                    tianjinDeviceBean.setType(jsonObject.getString("deviceSubType"));
                    tianjinDeviceBean.setElectricity(jsonObject.getString("electricity"));
                    tianjinDeviceBean.setSignalStrength(jsonObject.getString("signalStrength"));
                    tianjinDeviceBeans.add(tianjinDeviceBean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tianjinDeviceBeans;
    }

    /**
     * 获取本地数据库 人数据
     *
     * @return
     */
    public static Observable<List<TianjinDeviceBean>> getTop5TianjinDevice(String type) {
        return Observable.fromCallable(() -> {
            List<TianjinDeviceBean> top5TianjinDevice = TerminalFactory.getSDK().getSQLiteDBManager().getTop5TianjinDevice(type);
            Log.e("BindUtils", "获取常用设备：" + top5TianjinDevice.size());
            return top5TianjinDevice;
        }).subscribeOn(Schedulers.io());
    }

    /**
     * 绑定设备
     *
     * @return
     */
    public static void bindDevice(String equipmentType, String equipmentNo, String equipmentName, BandDeviceListener bandDeviceListener) {

        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            int no = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            if (bandDeviceListener != null) {
                Account account = DataUtil.getAccountByMemberNo(no, true);
                if (account != null) {
                    String name = account.getName();
                    String phoneNumber = account.getPhoneNumber();
                    String department = account.getDepartmentName();
                    String uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "";
                    String accountNo = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "";
                    bandDevice(uniqueNo,accountNo,equipmentType, equipmentNo,equipmentName, name, phoneNumber, department, bandDeviceListener);
                }
            }
        });
    }

    /**
     * 绑定设备
     * @param equipmentType
     * @param equipmentNo
     * @param userName
     * @param phoneNumber
     * @param department
     * @param bandDeviceListener
     */
    public static void bandDevice(String uniqueNo,String accountNo, String equipmentType, String equipmentNo,String equipmentName, String userName, String phoneNumber, String department, BandDeviceListener bandDeviceListener) {
        int code = -1;
        String msg = "";
        try {
            Map<String, String> param = new HashMap<>();
            param.put("uniqueNo", uniqueNo);
            param.put("accountNo", accountNo);

            param.put("equipmentType", TextUtils.isEmpty(equipmentType)?"":equipmentType);
            param.put("equipmentNo", equipmentNo);
            param.put("equipmentName", TextUtils.isEmpty(equipmentName)?"":equipmentName);
            param.put("terminalType", "DEVICE_PHONE");

            param.put("userName", TextUtils.isEmpty(userName)?"":userName);
            param.put("phoneNumber", TextUtils.isEmpty(phoneNumber)?"":phoneNumber);
            param.put("department", TextUtils.isEmpty(department)?"":department);
            //final String url = getBindUrl() + "/ldcs/bindRelationship/bind";
            final String url = getBindUrl() + "/ldcs//bindRelationship/bindByNFC";
            String result = MyTerminalFactory.getSDK().getHttpClient().post(url, param);
            JSONObject jsonObject = parseObject(result);
             msg = jsonObject.getString(RESULT_MSG);
             code = jsonObject.getInteger(RESULT_CODE);
        } catch (Exception e) {
            e.printStackTrace();
             code = -1;
             msg = "请求失败";
        }finally {
            if (bandDeviceListener != null) {
                bandDeviceListener.result(msg, code);
            }
        }
    }

    /**
     * 获取当前用户绑定设备
     *
     * @return
     */
    public static Observable<Boolean> deleteBindRelationshipObservable(String id) {
        return Observable.fromCallable(() -> deleteBindRelationship(id));
    }

    /**
     * 解绑
     *
     * @param id
     * @return
     */
    public static Boolean deleteBindRelationship(String id) {
        final String url = getBindUrl() + "/ldcs/bindRelationship/unbind/"+id;
        String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url);
        try {
            JSONObject jsonObject = parseObject(result);
            int code = jsonObject.getInteger(RESULT_CODE);
            return (code == BaseCommonCode.SUCCESS_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询结果
     *
     * @return
     */
    public static Observable<List<TianjinDeviceBean>> searchDeviceObservable(List<TianjinDeviceBean> deviceBeans, String searchText,String terminalType) {
        return Observable.fromCallable(() -> {
            List<TianjinDeviceBean> tianjinDeviceBeans = searchDevice(deviceBeans, searchText,terminalType);
            Log.e("BindUtils", "查询结果" + tianjinDeviceBeans);
            return tianjinDeviceBeans;
        }).subscribeOn(Schedulers.io());
    }


    public static List<TianjinDeviceBean>  searchDevice(List<TianjinDeviceBean> deviceBeans, String searchText,String terminalType) {
        List<TianjinDeviceBean> deviceBeans1 = new ArrayList<>();
        for (TianjinDeviceBean deviceBean : deviceBeans) {
            String data = "";
            if(TextUtils.equals(WorkBindEnum.car.getType(),terminalType)){
                data = deviceBean.getCarNo();
            }else{
                data = deviceBean.getNo();
            }
            if (data.toLowerCase().contains(searchText.toLowerCase())) {
                deviceBeans1.add(deviceBean);
            }
        }
        return deviceBeans1;
    }


    /**
     * 获取选择的装备
     * @param datas
     * @return
     */
    public static TianjinDeviceBean getSelectDevice(List<Object> datas) {
        for (Object object : datas) {
            if (object instanceof TianjinDeviceBean) {
                TianjinDeviceBean deviceBean = (TianjinDeviceBean) object;
                if (deviceBean.isCheck()) {
                    return deviceBean;
                }
            }
        }
        return null;
    }

    /**
     * 获取本地数据库 人数据
     *
     * @return
     */
    public static Observable<Long> saveBindDeviceToSqlite(TianjinDeviceBean device) {
        return Observable.fromCallable(() -> {
            Long index = TerminalFactory.getSDK().getSQLiteDBManager().addBindDevice(device);
            Log.e("BindUtils", "插入数据" + index);
            return index;
        }).subscribeOn(Schedulers.io());
    }

}
