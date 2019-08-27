package cn.vsx.vc.jump.sendMessage;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Hashtable;
import java.util.Map;

import cn.vsx.vsxsdk.IReceivedVSXMessage;

public class Connect3rdParty {

    private static Connect3rdParty connect3rdParty;
    /**
     * 线程安全的连接池对象
     */
    private static Map<String, IReceivedVSXMessage> conns = new Hashtable<>();

    public static Connect3rdParty getInstance() {
        if(connect3rdParty==null){
            connect3rdParty = new Connect3rdParty();
        }
        return connect3rdParty;
    }

    private Connect3rdParty(){}


    public static Map<String, IReceivedVSXMessage> getConns(){
        return conns;
    }

    /**
     * 通过包名，添加连接对象
     *
     * @param packageName
     * @param iReceivedVSXMessage
     */
    public void addConnect(String packageName, IReceivedVSXMessage iReceivedVSXMessage) {
        conns.put(packageName, iReceivedVSXMessage);
        String packageNames = "";
        for (String key : conns.keySet()) {//keySet获取map集合key的集合  然后在遍历key即可
            packageNames +=key+";";
        }
        Log.e("Connect3rdParty连接池", packageNames);
    }

    /**
     * 根据包名获取对应连接对象
     */
    public IReceivedVSXMessage getConnectForPackageName(String packageName) {
        return conns.get(packageName);
    }

    /**
     * 遍历连接池map,向所有连接的对象发送消息
     * @param messageJson
     * @param messageType
     */
    public void receivedMessage(String messageJson, int messageType) {
        for (String key : conns.keySet()) {//keySet获取map集合key的集合  然后在遍历key即可
            try {
                getConnectForPackageName(key).receivedMessage(messageJson,messageType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
