package cn.vsx.SpecificSDK;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.context.TerminalSDK4Android;

/**
 * Created by dragon on 2017/9/20.
 */

public class SpecificSDK extends TerminalSDK4Android {

    private static boolean isBindedUVCCameraService;
    private static SpecificSDK specificSDK;
    private static final String TAG = "SpecificSDK";

    private SpecificSDK(Application mapplication) {
        super(mapplication);
        MyTerminalFactory.setTerminalSDK(this);
    }

    public static SpecificSDK getInstance(){
        return specificSDK;
    }

    /**
     * 发送短文本
     * @param text 发送的文字内容
     * @param groupOrMemberName 会话的组或者个人名字
     * @param groupOrMemberNo  组编号或者个人编号
     * @param isGroup 是否是组消息
     */
    public static void sendShortTextMessage(String text,String groupOrMemberName,int groupOrMemberNo,boolean isGroup){
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
    public static void sendLongTextMessage(String text,String groupOrMemberName,int groupOrMemberNo,boolean isGroup){
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
    private static File saveString2File (String msg, int token) {
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
    public static void sendFileMessage(int messageTypeCode,File file, String groupOrMemberName, int groupOrMemberNo, boolean isGroup, boolean isNeedUi){
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
    public static void sendLocationMessage(double longitude, double latitude, String groupOrMemberName, int groupOrMemberNo, boolean isGroup){
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
     * 自己发起直播
     * @param theme
     * @param channelNo
     * @return
     */
    public static int requestMyselfLive(String theme,String channelNo){
        return TerminalFactory.getSDK().getLiveManager().requestMyselfLive(theme,channelNo);
    }

    /**
     * 异步发起个呼，必须放在子线程里
     * @param memberId 对方memberId
     * @return 状态码 0 表示发起个呼成功
     */
    public static int requestPersonalCall(final int memberId){
        //先根据成员查询uniqueNo
        Account account = TerminalFactory.getSDK().getConfigManager().getAccountByNo(memberId);
        if(null != account && !account.getMembers().isEmpty()){
            Member member = account.getMembers().get(0);
            return requestPersonalCall(memberId, member.getUniqueNo());
        }else {
            return TerminalErrorCode.INDIVIDUAL_CALL_FAIL.getErrorCode();
        }
    }

    /**
     * 发起个呼
     * @param memberId 对方memberId
     * @return 状态码 0 表示发起个呼成功
     */
    private static int requestPersonalCall(int memberId,long uniqueNo){
        return MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(memberId,uniqueNo,"");
    }

    /**
     * 发起组呼
     * @return 状态码 0 表示允许组呼
     */
    public static int requestGroupCall(int groupId){
        return MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",groupId);
    }

    /**
     * 注册 ReceiveHandler，必须和{@link SpecificSDK#unregistHandler(ReceiveHandler)}一起使用，避免内存泄漏
     * @param handler
     */
    public static void registHandler(ReceiveHandler handler){
        MyTerminalFactory.getSDK().registReceiveHandler(handler);
    }

    /**
     * 反注册 ReceiveHandler
     * @param handler
     */
    public static void unregistHandler(ReceiveHandler handler){
        MyTerminalFactory.getSDK().unregistReceiveHandler(handler);
    }

    /**
     * 提供给第三方用户的初始化入口
     */
    public static void init(Application application){
        //SpecificSDK是TerminalSDK4Android的子类，而TerminalSDK4Android继承TerminalSDKBaseImpl实现ITerminalSDK接口(具备了很多通用的业务功能)
        specificSDK = new SpecificSDK(application);
        //如果消息服务是开启的，直接关闭
        MyTerminalFactory.getSDK().setLoginFlag();
        //设置程序的key和type(见具体的清单文件配置)
        setAppKey(application);
        setApkType(application);
        //开启程序的所有后台服务(包含新开的消息监听进程和很多后台监听和全局控制的服务，程序的登录流程也在对应的服务中自动进行)
        MyTerminalFactory.getSDK().start();
        //根据前面获取到的type，这里设置地址(不同的类型对应的地址不一样)
        //设置注册服务地址
        String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        if(!TextUtils.isEmpty(deviceType)&&TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString())){
            MyTerminalFactory.getSDK().getAuthManagerTwo().initIpSingle();
        }else{
            MyTerminalFactory.getSDK().getAuthManagerTwo().initIp();
        }
        //保存录像，录音，照片的存储路径
        MyTerminalFactory.getSDK().getFileTransferOperation().initExternalUsableStorage();
        SpecificSDK.setTerminalMemberType(TerminalMemberType.TERMINAL_PHONE.toString());
    }

    /**
     * 设置sdk的注册服务地址
     */
    public static void setAddress(String ip,int port){
        MyTerminalFactory.getSDK().getAuthManagerTwo().setAddress(ip,port);
    }

    public static void setApkType(Application application){
        try{
            ApplicationInfo appInfo = application.getPackageManager().getApplicationInfo(application.getPackageName(), PackageManager.GET_META_DATA);
            String apkType=appInfo.metaData.getString("APKTYPE");
            Log.d("MyApplication", " APKTYPE == " + apkType);
            MyTerminalFactory.getSDK().putParam(Params.APK_TYPE,apkType);
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * 需要在Manifest中填写cn.vsx.sdk.API_KEY值
     * @param application
     */
    public static void setAppKey(Application application){
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

    public static void setPushKey(String key){
        MyTerminalFactory.getSDK().getLiveConfigManager().setPushKey(key);
    }

    public static void setPlayKey(String key){
        MyTerminalFactory.getSDK().getLiveConfigManager().setPlayKey(key);
    }

    public static void setRTMPPlayKey(String key){
        MyTerminalFactory.getSDK().getLiveConfigManager().setRTMPPlayKey(key);
    }

    /**
     * 登陆成功之后才能调用此方法
     */
    public static void initVoip(){
        String account = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L)+"";
        String voipServerIp = MyTerminalFactory.getSDK().getParam(Params.VOIP_SERVER_IP, "");
        String voipServerPort = MyTerminalFactory.getSDK().getParam(Params.VOIP_SERVER_PORT, 0)+"";
        String server = voipServerIp+":"+voipServerPort;
//        account = "1003";
        logger.info("voip账号：" + account + ",密码：" + account + "，服务器地址：" + server);
        if(!TextUtils.isEmpty(account)){
            //开启voip电话服务
            MyTerminalFactory.getSDK().getVoipCallManager().startService(MyTerminalFactory.getSDK().application.getApplicationContext());
            MyTerminalFactory.getSDK().getVoipCallManager().login(account,account,server);
        }
    }
}
