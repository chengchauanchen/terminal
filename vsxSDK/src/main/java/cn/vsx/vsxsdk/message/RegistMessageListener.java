package cn.vsx.vsxsdk.message;

import cn.vsx.vsxsdk.Interf.CommonMessageListener;
import cn.vsx.vsxsdk.Interf.EmergencyVideoLiveListener;
import cn.vsx.vsxsdk.Interf.GoWatchRTSPListener;
import cn.vsx.vsxsdk.Interf.IndividualCallListener;
import cn.vsx.vsxsdk.Interf.LiveInComeListener;
import cn.vsx.vsxsdk.Interf.TempGroupListener;
import cn.vsx.vsxsdk.Interf.VsxProcessStateListener;
import cn.vsx.vsxsdk.constant.ThirdMessageType;

public class RegistMessageListener {

    private EmergencyVideoLiveListener emergencyVideoLiveListener;
    private GoWatchRTSPListener goWatchRTSPListener;
    private IndividualCallListener individualCallListener;
    private LiveInComeListener liveInComeListener;
    private CommonMessageListener commonMessageListener;
    private TempGroupListener tempGroupListener;
    private VsxProcessStateListener vsxProcessStateListener;

    public void receivedMessage(String messageJson, int messageType) {
        if (messageType == ThirdMessageType.NOTIFY_DATA_MESSAGE.getCode()) {//接收到普通消息
            if (commonMessageListener != null) {
                commonMessageListener.onReceived(messageJson);
            }
        } else if (messageType == ThirdMessageType.NOTIFY_INDIVIDUAL_CALL_IN_COMMING.getCode()) {//个呼来了
            if (individualCallListener != null) {
                individualCallListener.onReceived(messageJson);
            }
        } else if (messageType == ThirdMessageType.GO_WATCH_RTSP.getCode()) {//观看上报视频
            if (goWatchRTSPListener != null) {
                goWatchRTSPListener.onReceived(messageJson);
            }
        } else if (messageType == ThirdMessageType.NOTIFY_LIVING_IN_COMMING.getCode()) {//收到别人请求我开启直播的通知
            if (liveInComeListener != null) {
                liveInComeListener.onReceived(messageJson);
            }
        } else if (messageType == ThirdMessageType.NOTIFY_EMERGENCY_VIDEO_LIVE_IN_COMMING.getCode()) {//收到强制上报图像的通知
            if (emergencyVideoLiveListener != null) {
                emergencyVideoLiveListener.onReceived(messageJson);
            }
        }else if(messageType == ThirdMessageType.NOTIFY_MEMBER_ABOUT_TEMP_GROUP.getCode()){
            if (tempGroupListener != null) {
                tempGroupListener.onReceived(messageJson);
            }
        }else if(messageType ==ThirdMessageType.HEART_BEAT.getCode()){//心跳,返回融合通信进程状态
            if(vsxProcessStateListener!=null){
                vsxProcessStateListener.onReceived(messageJson);
            }
        }
    }

    /**
     * 收到强制上报图像的通知
     *
     * @param emergencyVideoLiveListener
     */
    public void setEmergencyVideoLiveListener(EmergencyVideoLiveListener emergencyVideoLiveListener) {
        this.emergencyVideoLiveListener = emergencyVideoLiveListener;
    }

    /**
     * 观看上报视频
     *
     * @param goWatchRTSPListener
     */
    public void setGoWatchRTSPListener(GoWatchRTSPListener goWatchRTSPListener) {
        this.goWatchRTSPListener = goWatchRTSPListener;
    }

    /**
     * 个呼来了
     *
     * @param individualCallListener
     */
    public void setIndividualCallListener(IndividualCallListener individualCallListener) {
        this.individualCallListener = individualCallListener;
    }

    /**
     * 收到别人请求我开启直播的通知
     *
     * @param liveInComeListener
     */
    public void setLiveInComeListener(LiveInComeListener liveInComeListener) {
        this.liveInComeListener = liveInComeListener;
    }

    /**
     * 接收到普通消息
     *
     * @param commonMessageListener
     */
    public void setCommonMessageListener(CommonMessageListener commonMessageListener) {
        this.commonMessageListener = commonMessageListener;
    }

    /**
     * jie收警情临时组消息
     *
     * @param tempGroupListener
     */
    public void setTempGroupListener(TempGroupListener tempGroupListener) {
        this.tempGroupListener = tempGroupListener;
    }

    /**
     * 融合通信进程状态消息
     *
     * @param vsxProcessStateListener
     */
    public void setVsxProcessStateListener(VsxProcessStateListener vsxProcessStateListener) {
        this.vsxProcessStateListener = vsxProcessStateListener;
    }
}
