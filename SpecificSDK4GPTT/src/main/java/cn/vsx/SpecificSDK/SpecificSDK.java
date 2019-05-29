package cn.vsx.SpecificSDK;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.context.TerminalSDK4Android;

/**
 * Created by dragon on 2017/9/20.
 */

public class SpecificSDK extends TerminalSDK4Android {

    private static SpecificSDK specificSDK;
    private static final String TAG = "SpecificSDK";

    private SpecificSDK(Application mapplication) {
        super(mapplication);
        MyTerminalFactory.setTerminalSDK(this);
    }

    public static SpecificSDK getInstance(){
        return specificSDK;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected String newUuid() {
        String account = TerminalFactory.getSDK().getParam(UrlParams.ACCOUNT);
        if (Util.isEmpty(account)){
            TelephonyManager telephonyManager = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
            WifiManager wm = (WifiManager)application.getSystemService(Context.WIFI_SERVICE);
            account = telephonyManager.getDeviceId() == null ?
                    wm.getConnectionInfo().getMacAddress().hashCode()+"" : telephonyManager.getDeviceId().hashCode()+"";
        }
        String terminalType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
        logger.info(" SpecificSDK--------> account = "+account+"---terminalType = "+terminalType);
        return account+terminalType;
    }

    /**
     * 发送短文本
     * @param text 发送的文字内容
     * @param groupOrMemberName 会话的组或者个人名字
     * @param groupOrMemberNo  组编号或者个人编号
     * @param isGroup 是否是组消息
     */
    public void sendShortTextMessage(String text,String groupOrMemberName,int groupOrMemberNo,boolean isGroup){
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(JsonParam.SEND_STATE, SendState.SENDING);
        jsonObject.put(JsonParam.CONTENT, text);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        if (isGroup) {
            mTerminalMessage.messageToId = NoCodec.encodeGroupNo(groupOrMemberNo);
        } else {
            mTerminalMessage.messageToId = NoCodec.encodeMemberNo(groupOrMemberNo);
        }
        mTerminalMessage.messageToName = groupOrMemberName;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.SHORT_TEXT.getCode();
        mTerminalMessage.messageBody = jsonObject;
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", mTerminalMessage);
        //发送成功handler：ReceiveSendDataMessageSuccessHandler
        //发送失败handler: ReceiveSendDataMessageFailedHandler
    }

    /**
     * 发送长文本
     * @param text 发送的文字内容
     * @param groupOrMemberName 会话的组或者个人名字
     * @param groupOrMemberNo  组编号或者个人编号
     * @param isGroup 是否是组消息
     */
    public void sendLongTextMessage(String text,String groupOrMemberName,int groupOrMemberNo,boolean isGroup){
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(JsonParam.SEND_STATE, SendState.SENDING);
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        File file = saveString2File(text, jsonObject.getIntValue(JsonParam.TOKEN_ID));
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        if (isGroup) {
            mTerminalMessage.messageToId = NoCodec.encodeGroupNo(groupOrMemberNo);
        } else {
            mTerminalMessage.messageToId = NoCodec.encodeMemberNo(groupOrMemberNo);
        }
        mTerminalMessage.messageToName = groupOrMemberName;
        mTerminalMessage.messageBody = jsonObject;
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.LONG_TEXT.getCode();
        mTerminalMessage.messagePath = file.getPath();

        MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL), file, mTerminalMessage, false);
        //发送成功handler：ReceiveSendDataMessageSuccessHandler
        //发送失败handler: ReceiveSendDataMessageFailedHandler
    }

    /**
     *
     * @param msg 长文本消息
     * @return 将长Text转换为file
     */
    private File saveString2File (String msg, int token) {
        String fileName = "longmsg"+token+".txt";
        String fileDir = MyTerminalFactory.getSDK().getWordRecordDirectory();
        File file = new File(fileDir, fileName);
        file.deleteOnExit();
        FileOutputStream fos = null;
        try{
            File dir = new File(fileDir);
            if (! dir.exists()){
                dir.mkdir();
            }
            file = new File(fileDir, fileName);
            fos = new FileOutputStream(file);
            fos.write(msg.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return file;
    }

    /**
     * 发送文件、照片、录音、视频消息
     * @param messageTypeCode 发送的消息类型
     * @param file 上传的文件或者照片
     * @param groupOrMemberName 会话的组或者个人名字
     * @param groupOrMemberNo  组编号或者个人编号
     * @param isGroup 是否是组消息
     * @param isNeedUi 是否需要在界面显示上传进度，如果需要，还要注册ReceiveUploadProgressHandler
     */
    public void sendFileMessage(int messageTypeCode,File file, String groupOrMemberName, int groupOrMemberNo, boolean isGroup, boolean isNeedUi){
        TerminalMessage mTerminalMessage = new TerminalMessage();
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(JsonParam.SEND_STATE, SendState.SENDING);
        if (messageTypeCode == MessageType.PICTURE.getCode()){
            jsonObject.put(JsonParam.PICTURE_NAME, file.getName());
            jsonObject.put(JsonParam.PICTURE_SIZE, file.length());
        }else if(messageTypeCode == MessageType.FILE.getCode()){
            jsonObject.put(JsonParam.FILE_NAME, file.getName());
            jsonObject.put(JsonParam.FILE_SIZE, file.length());
        }
        else if(messageTypeCode == MessageType.AUDIO.getCode()){
            jsonObject.put(JsonParam.FILE_NAME, file.getName());
            jsonObject.put(JsonParam.FILE_SIZE, file.length());

            //            jsonObject.put(JsonParam.START_TIME,startTime);
            //            jsonObject.put(JsonParam.END_TIME,endTime);

        }else if(messageTypeCode == MessageType.VIDEO_CLIPS.getCode()){
            jsonObject.put(JsonParam.FILE_NAME, file.getName());
            jsonObject.put(JsonParam.FILE_SIZE, file.length());
        }

        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());

        mTerminalMessage.messageBody = jsonObject;
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToName = groupOrMemberName;
        if (isGroup) {
            mTerminalMessage.messageToId = NoCodec.encodeGroupNo(groupOrMemberNo);
        } else {
            mTerminalMessage.messageToId = NoCodec.encodeMemberNo(groupOrMemberNo);
        }
        mTerminalMessage.messagePath = file.getPath();
        mTerminalMessage.sendTime = System.currentTimeMillis();
        if (messageTypeCode == MessageType.PICTURE.getCode()) {
            mTerminalMessage.messageType = MessageType.PICTURE.getCode();
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.IMAGE_UPLOAD_URL, ""), file, mTerminalMessage, isNeedUi);
        } else if (messageTypeCode == MessageType.FILE.getCode()) {
            mTerminalMessage.messageType = MessageType.FILE.getCode();
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, mTerminalMessage, isNeedUi);
        }else if (messageTypeCode == MessageType.AUDIO.getCode()) {
            mTerminalMessage.messageType = MessageType.AUDIO.getCode();
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, mTerminalMessage, isNeedUi);
        } else if (messageTypeCode == MessageType.VIDEO_CLIPS.getCode()) {
            mTerminalMessage.messageType = MessageType.VIDEO_CLIPS.getCode();
            MyTerminalFactory.getSDK().upload(MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, ""), file, mTerminalMessage, isNeedUi);
        }
    }

    /**
     * 发送位置信息
     * @param longitude 经度
     * @param latitude 纬度
     * @param groupOrMemberName 会话的组或者个人名字
     * @param groupOrMemberNo  组编号或者个人编号
     * @param isGroup 是否是组消息
     */
    public void sendLocationMessage(double longitude, double latitude, String groupOrMemberName, int groupOrMemberNo, boolean isGroup){
        boolean getLocationSuccess = false;
        TerminalMessage mTerminalMessage = new TerminalMessage();
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(JsonParam.SEND_STATE, SendState.SENDING);
        if(longitude!=0 && latitude!=0){
            jsonObject.put(JsonParam.LONGITUDE, longitude);
            jsonObject.put(JsonParam.LATITUDE, latitude);
            getLocationSuccess = true;
        }
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        jsonObject.put(JsonParam.ACTUAL_SEND, true);
        jsonObject.put(JsonParam.GET_LOCATION_FAIL, !getLocationSuccess);
        mTerminalMessage.messageBody = jsonObject;
        mTerminalMessage.messageFromId =  MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToName = groupOrMemberName;
        if (isGroup) {
            mTerminalMessage.messageToId = NoCodec.encodeGroupNo(groupOrMemberNo);
        } else {
            mTerminalMessage.messageToId = NoCodec.encodeMemberNo(groupOrMemberNo);
        }
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.POSITION.getCode();
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", mTerminalMessage);
    }

    /**
     * 发起个呼
     * @param memberId 对方memberId
     * @return 状态码 0 表示发起个呼成功
     */
    public int requestPersonalCall(int memberId,long uniqueNo){
        return MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(memberId,uniqueNo,"");
    }

    /**
     * 发起组呼
     * @return 状态码 0 表示允许组呼
     */
    public int requestGroupCall(int groupId){
        return MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",groupId);
    }

    /**
     * 注册 ReceiveHandler，必须和{@link SpecificSDK#unregistHandler(ReceiveHandler)}一起使用，避免内存泄漏
     * @param handler
     */
    public void registHandler(ReceiveHandler handler){
        MyTerminalFactory.getSDK().registReceiveHandler(handler);
    }

    /**
     * 反注册 ReceiveHandler
     * @param handler
     */
    public void unregistHandler(ReceiveHandler handler){
        MyTerminalFactory.getSDK().unregistReceiveHandler(handler);
    }

    /**
     * 提供给第三方用户的初始化入口
     */
    public static void init(Application application){
        specificSDK = new SpecificSDK(application);
        MyTerminalFactory.getSDK().setLoginFlag();
        setAppKey(application);
        MyTerminalFactory.getSDK().start();
    }

    private static void setAppKey(Application application){
        try{
            ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
            String platformKey=appInfo.metaData.getString("cn.vsx.sdk.API_KEY");
            Log.d(TAG, "platform_key == " + platformKey);
            TerminalFactory.getSDK().putParam(Params.PLATFORM_KEY,platformKey);
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void setTerminalMemberType(String type){
        MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, type);
    }
}
