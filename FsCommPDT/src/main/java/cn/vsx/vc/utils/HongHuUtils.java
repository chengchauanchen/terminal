package cn.vsx.vc.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.model.BindBean;
import cn.vsx.vc.model.Relationship;
import cn.vsx.vc.receiveHandle.ReceiverBindDeviceHandler;
import cn.vsx.vc.receiveHandle.ReceiverUnBindDeviceHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class HongHuUtils {

   //public static String IP = "http://192.168.1.33:7036";
   public static String IP = "http://192.168.20.189:6062/donghu";

    /**
     * 解绑设备
     *
     * @param id
     */
    public static void unBindDevice(int id,int position) {
        Relationship relationship = new Relationship();
        //Map<String, String> paramsMap = new HashMap<>();
        relationship.setId(id);

        String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
        int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT, 0);
//			final String url = "http://192.168.1.174:6666/save";
//        final String url = "http://"+ip+":"+port+"/save";
        final String url =HongHuUtils.IP + "/management/unBundlingPhoneEquipment";
        Gson gson = new Gson();
        final String json = gson.toJson(relationship);

        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String result = MyTerminalFactory.getSDK().getHttpClient().postJson(url, "unBind=" + json);
            Log.i("BandDeviceDialog", result);
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverUnBindDeviceHandler.class, id,position);
        });
    }

    /**
     * 获取绑定装备列表
     */
    public static void getBindDevices() {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("terminalNo", MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "");
//        String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
//        int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT, 0);
        String ip = "192.168.1.20";
        int port = 9011;
//        final String url = "http://" + ip + ":" + port + "/management/getRelationshipList";
        final String url = HongHuUtils.IP +"/management/getRelationshipList";
        Gson gson = new Gson();
        final String json = gson.toJson(paramsMap);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String result = MyTerminalFactory.getSDK().getHttpClient().get(url, paramsMap);
            Log.i("BandDeviceDialog", result);
            try {
                List<BindBean> bindBeans = gson.fromJson(result, new TypeToken<List<BindBean>>() {
                }.getType());
                if (bindBeans != null) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverBindDeviceHandler.class, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
