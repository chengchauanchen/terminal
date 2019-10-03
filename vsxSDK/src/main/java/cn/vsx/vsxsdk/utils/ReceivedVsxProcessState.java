package cn.vsx.vsxsdk.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import cn.vsx.vsxsdk.Interf.VsxProcessStateListener;
import cn.vsx.vsxsdk.VsxSDK;
import cn.vsx.vsxsdk.bean.SendBean;
import cn.vsx.vsxsdk.constant.SDKProcessStateEnum;
import cn.vsx.vsxsdk.constant.ThirdMessageType;

public class ReceivedVsxProcessState {

    private Context context;
    public static int receivedNum = 0;
    public static int sendNum = 0;
    public static SendBean sendBean;

    public ReceivedVsxProcessState(Context context) {
        this.context = context;
        setVsxProcessStateListener();
    }

    /**
     * 设置融合通信进程状态数据监听
     */
    private void setVsxProcessStateListener() {
        //临时组消息
        VsxSDK.getInstance().getRegistMessageListener().setVsxProcessStateListener(new VsxProcessStateListener() {
            @Override
            public void onReceived(String messageJson) {
                Gson gson = new Gson();
                Log.e("--SDK---",messageJson);
                try {
                    sendBean = gson.fromJson(messageJson, SendBean.class);
                    if (sendBean != null) {
                        receivedNum = sendBean.getWhatVsxSDKProcess();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("MainActivity", messageJson);
            }
        });
    }

    /**
     * 记录发送次数
     */
    public static void addSendCount() {
        Log.e("--SDK---sendNum=",sendNum+"");
        Log.e("--SDK---receivedNum=",receivedNum+"");
        sendNum++;
    }

    /**
     * 判断链接状态
     */
    public void judgeLinkState(){
        Log.e("--SDK---","sendNum="+sendNum+",receivedNum="+receivedNum);
        if(sendNum-receivedNum>3){//说明发送的次数,比接受的次数 间隔了3次,则说明没有链接上
            Toast.makeText(context,"请重新安装启融合通信",Toast.LENGTH_LONG).show();
            sendNum=0;
            receivedNum=0;
        }else if(sendNum ==receivedNum && receivedNum>0){
            getVsxProcessState();
        }
    }

    /**
     * 获取融合通信进程状态
     */
    public void getVsxProcessState() {
        if (sendBean != null) {
            long time = sendBean.getTime();
            String loginState = sendBean.getLoginState();
            int whatVsxSDKProcess = sendBean.getWhatVsxSDKProcess();
            Log.e("--SDK---","time="+time+",loginState="+loginState+",whatVsxSDKProcess="+whatVsxSDKProcess);
            SDKProcessStateEnum sdkProcessStateEnum = SDKProcessStateEnum.valueOf(loginState);
            switch (sdkProcessStateEnum) {
                case DEFAULT_STATE://默认状态
                    break;
                case SELF_STARTUP_PERMISSION://开了自启动权限
                case EXTERNAL_PERMISSION://开了存储权限
                case HAVE_WINDOW_PERMISSION://开了悬浮窗权限
                    break;

                case NO_EXTERNAL_PERMISSION://未开存储权限
                case NO_WINDOW_PERMISSION://未开悬浮窗权限
                case NO_AUTH_URL://认证地址有误
                case NO_PHONE_TYPE://不是警务通类型
                    Toast.makeText(context,"请开启融合通信",Toast.LENGTH_LONG).show();
                    break;
                case SUCCESS_STATE://正常
                    break;
                default:
                    break;
            }
        }else{
            Log.e("--SDK---","sendBean=null");
        }
    }
}
