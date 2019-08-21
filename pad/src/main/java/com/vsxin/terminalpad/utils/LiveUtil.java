package com.vsxin.terminalpad.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vsxin.terminalpad.receiveHandler.ReceiveGetHistoryLiveUrlsHandler;

import org.apache.http.util.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;

public class LiveUtil {

    public static int requestToWatchLiving(TerminalMessage terminalMessage){
        PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
        builder.setMessageUrl(TextUtils.isEmpty(terminalMessage.messageUrl)?"":terminalMessage.messageUrl);
        builder.setMessageFromName(terminalMessage.messageFromName);
        builder.setMessageFromNo(terminalMessage.messageFromId);
        builder.setMessageFromUniqueNo(terminalMessage.messageFromUniqueNo);
        builder.setMessageToName(terminalMessage.messageToName);
        builder.setMessageToNo(terminalMessage.messageToId);
        builder.setMessageToUniqueNo(terminalMessage.messageToUniqueNo);
        builder.setMessageType(terminalMessage.messageType);
        builder.setMessageVersion(terminalMessage.messageVersion);
        builder.setResultCode(terminalMessage.resultCode);
        builder.setSendingTime(terminalMessage.sendTime);
        builder.setMessageBody(terminalMessage.messageBody.toString());
        PTTProtolbuf.NotifyDataMessage message = builder.build();
        return MyTerminalFactory.getSDK().getLiveManager().requestToWatchLiving(message);
    }

    /**
     * 获取历史上报图像的列表
     * @param terminalMessage
     */
    public static void getHistoryLiveUrls(TerminalMessage terminalMessage){
        //获取播放url
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            List<String> liveUrls  = new ArrayList<>();
            String serverIp = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP, "");
            String serverPort = TerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT, 0)+"";
            String url = "http://"+serverIp+":"+serverPort+"/api/v1/query_records";
            Map<String,String> paramsMap = new HashMap<>();
            paramsMap.put("id",getCallId(terminalMessage));
            String result = TerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
            //            result = "{\"msg\":\"success\",\"code\":0,\"data\":{\"list\":[{\"name\":\"88045832_6540978884229379386\",\"start_time\":\"20190517154511\",\"duration\":\"13.598\",\"hls\":\"/hls/88045832_6540978884229379386/20190517/20190517154511/88045832_6540978884229379386_record.m3u8\"}]}}";
            if(!Util.isEmpty(result)){
                JSONObject jsonResult = JSONObject.parseObject(result);
                Integer code = jsonResult.getInteger("code");
                if(code == 0){
                    JSONObject data = jsonResult.getJSONObject("data");
                    JSONArray list = data.getJSONArray("list");
                    if(!list.isEmpty()){
                        liveUrls.clear();
                        for(int i = 0; i < list.size(); i++){
                            JSONObject jsonObject = list.getJSONObject(i);
                            String hls = jsonObject.getString("hls");
                            String startTime = jsonObject.getString("start_time");
                            String fileServerIp = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_IP);
                            String port = MyTerminalFactory.getSDK().getParam(Params.MEDIA_HISTORY_SERVER_PORT,0)+"";
                            String liveUrl = "http://"+fileServerIp+":"+port+hls;
                            liveUrls.add(liveUrl);
                        }
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetHistoryLiveUrlsHandler.class,0,liveUrls,terminalMessage.messageFromName,terminalMessage.messageFromId);
                    }else{
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetHistoryLiveUrlsHandler.class,-1,liveUrls,"",0);
                    }
                }else{
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetHistoryLiveUrlsHandler.class,-1,liveUrls,"",0);
                }
            }else{
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetHistoryLiveUrlsHandler.class,-1,liveUrls,"",0);
            }
        });
    }

    /**
     * 获取callId
     * @param terminalMessage
     * @return
     */
    private static String getCallId(TerminalMessage terminalMessage){
        String id = "";
        if(terminalMessage.messageBody!=null){
            if(terminalMessage.messageBody.containsKey(JsonParam.EASYDARWIN_RTSP_URL)){
                String url = terminalMessage.messageBody.getString(JsonParam.EASYDARWIN_RTSP_URL);
                if(!android.text.TextUtils.isEmpty(url)&&url.contains("/")&&url.contains(".")){
                    int index = url.lastIndexOf("/");
                    int pointIndex = url.lastIndexOf(".");
                    id = url.substring(index+1,pointIndex);
                }
            }
        }
        return id;
    }
}
