package ptt.terminalsdk.manager.voip;

import android.content.Context;

import com.xuchongyang.easyphone.EasyLinphone;
import com.xuchongyang.easyphone.callback.PhoneCallback;
import com.xuchongyang.easyphone.callback.RegistrationCallback;

import org.apache.log4j.Logger;
import org.linphone.core.LinphoneCall;

import java.io.File;
import java.io.IOException;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallStateMachine;
import cn.vsx.hamster.terminalsdk.model.CallRecord;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/8/7
 * 描述：
 * 修订历史：
 */

public class VoipManager{
    private Logger logger = Logger.getLogger(getClass());
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    private final IndividualCallStateMachine individualCallStateMachine;

    public VoipManager(){

        individualCallStateMachine = new IndividualCallStateMachine();
    }
    /**
     * 下载录音文件
     *
     * @param callRecord
     */
    public void downloadRecordFile(final CallRecord callRecord, final DownloadCompleteListener downloadCompleteListener, final boolean isNeedUi) {
         String url = callRecord.getPath();
        logger.info("录音下载地址："+url);
        if(url.equals("")||url==null){
            downloadCompleteListener.failure();
            return;
        }
//        if(TerminalFactory.getSDK().getServiceBusManager().canUseServiceBusDeviceType()){
//            url = TerminalFactory.getSDK().getServiceBusManager().getUrl(url);
//        }
        Request request = new Request.Builder().url(url).build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
                downloadCompleteListener.failure();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //用于下载时更新进度
                ResponseBody responseBody;
                if (isNeedUi) {
                    responseBody = TerminalFactory.getSDK().downloadProgress(response, null);
                } else {
                    responseBody = response.body();
                }

                BufferedSource source = responseBody.source();

                File outFile = createFile(callRecord);
                BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                source.readAll(sink);
                sink.flush();
                source.close();

                if(outFile.length()<=0){
                    downloadCompleteListener.failure();
                }else {
                    callRecord.setPath(TerminalFactory.getSDK().getAudioRecordDirectory()+callRecord.getCallId()+".amr");
                    downloadCompleteListener.succeed(callRecord);
                }

            }

        });
    }


    private File createFile(CallRecord callRecord) {
        File dir = new File(TerminalFactory.getSDK().getAudioRecordDirectory());
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(TerminalFactory.getSDK().getAudioRecordDirectory()+callRecord.getCallId()+".amr");
        return file;
    }

    /**
     * 开启服务
     */
    public void startService(Context context){
        EasyLinphone.startService(context);
    }

    /**
     * 登陆
     * @param account 账号
     * @param password 密码
     * @param serverIP voip服务地址
     */
    public void login(String account,String password,String serverIP){
        try{
            EasyLinphone.setAccount(account, password, serverIP);
            EasyLinphone.login();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 拨打电话
     * @param dialNum 电话号码
     */
    public void audioCall(String dialNum){
        try{
            EasyLinphone.callTo(dialNum, false);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 接听来电
     */
    public void acceptCall(){
        try{
            EasyLinphone.acceptCall();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 挂断电话
     */
    public void hangUp() {
        try{
            EasyLinphone.hangUp();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void refuseCall(LinphoneCall linphoneCall){
        try{
            EasyLinphone.refusedCall(linphoneCall);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 切换静音
     */
    public void toggleMute() {
        try{
            EasyLinphone.toggleMicro(!EasyLinphone.getLC().isMicMuted());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 是否免提
     */
    public void toggleSpeaker(boolean isSpeakerEnabled ) {
        try{
            EasyLinphone.toggleSpeaker(isSpeakerEnabled);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 回调
     * @param registrationCallback 注册回调
     * @param phoneCallback 电话回调
     */
    public void addCallback(RegistrationCallback registrationCallback, PhoneCallback phoneCallback){
        EasyLinphone.addCallback(registrationCallback,phoneCallback);
    }

    public void destroy(Context context){
        try{
            EasyLinphone.removeAuthInfo();
            EasyLinphone.stopService(context);
            EasyLinphone.clearUtil();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void removeAuthInfo(){
        try{
            EasyLinphone.removeAuthInfo();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void clearCache(){
        try{
            EasyLinphone.getLC().clearAuthInfos();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
