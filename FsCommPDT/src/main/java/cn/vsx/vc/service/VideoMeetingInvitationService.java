package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;

import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingDataBean;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyAddVideoMeetingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyVideoMeetingMessageReduceUnreadCountHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverEntityKeyEventInServiceHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 视频会商
 */
public class VideoMeetingInvitationService extends BaseService {

    private TextView mTvLiveReportName;
    private TextView mTvLiveReportId;
    private TextView mTvDeptmentName;
    private LinearLayout mLlLiveRespondRefuseTotal;
    private LinearLayout mLlLiveRespondAcceptTotal;
    private VideoMeetingMessage meetingMessage;

    public VideoMeetingInvitationService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_video_meeting_invitation, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
    }

    @Override
    protected void findView(){
        ImageView mIvAvatarReport = rootView.findViewById(R.id.iv_avatar_report);
        //boolean mode = MyTerminalFactory.getSDK().getParam(Params.DAYTIME_MODE, false);
        Glide.with(this)
            .load(TerminalFactory.getSDK().getParam(UrlParams.AVATAR_URL))
            .asBitmap()
            .placeholder(R.drawable.user_photo_work_night)//加载中显示的图片
            .error(R.drawable.user_photo_work_night)//加载失败时显示的图片
            .into(mIvAvatarReport);
        mTvLiveReportName = rootView.findViewById(R.id.tv_live_report_name);
        mTvLiveReportId = rootView.findViewById(R.id.tv_live_report_id);
        mTvDeptmentName = rootView.findViewById(R.id.tv_deptment_name);
        mLlLiveRespondRefuseTotal = rootView.findViewById(R.id.ll_live_respond_refuse_total);
        mLlLiveRespondAcceptTotal = rootView.findViewById(R.id.ll_live_respond_accept_total);

        //网络状态布局
        mLlNoNetwork = rootView.findViewById(R.id.ll_network_state);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        mLlLiveRespondAcceptTotal.setOnClickListener(acceptOnClickListener);
        mLlLiveRespondRefuseTotal.setOnClickListener(refuseOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverEntityKeyEventInServiceHandler);
    }

    @Override
    protected void initView(Intent intent){
        meetingMessage =
            (VideoMeetingMessage) intent.getSerializableExtra(Constants.VIDEO_MEETING_MESSAGE);
        if (meetingMessage == null) {
            ToastUtil.showToast(MyApplication.instance,
                getString(R.string.text_video_meeting_data_error));
            stopBusiness();
            return;
        }

        mTvLiveReportName.setText(meetingMessage.getCreateTerminalName());
        mTvLiveReportId.setText(HandleIdUtil.handleId(meetingMessage.getCreateTerminalNo()));
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(meetingMessage.getCreateTerminalNo());
            if(account==null||(TextUtils.isEmpty(account.getDepartmentName()))){
                account = DataUtil.getAccountByMemberNo(meetingMessage.getCreateTerminalNo(),true);
            }
            Account finalAccount = account;
            mHandler.post(() -> {
                mTvDeptmentName.setText((finalAccount !=null&& !TextUtils.isEmpty(finalAccount.getDepartmentName()))? finalAccount
                    .getDepartmentName():"");
            });
        });

        mHandler.postDelayed(() -> {
            PromptManager.getInstance().VideoLiveInCommimgRing();
        },1500);
        mHandler.postDelayed(() -> {
            stopBusiness();
        },45 * 1000);
    }

    @Override
    protected void showPopMiniView(){
        PromptManager.getInstance().stopRing();
        stopBusiness();
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case OFF_LINE:
                ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.net_work_disconnect));
                stopBusiness();
                break;
            default:break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
        if(!connected){
            if(!mHandler.hasMessages(OFF_LINE)){
                mHandler.sendEmptyMessageDelayed(OFF_LINE,3000);
            }
        }else {
            mHandler.removeMessages(OFF_LINE);
        }
    }

    @Override
    protected void initBroadCastReceiver(){}

    @Override
    protected void initData(){

    }

    private View.OnClickListener acceptOnClickListener = v -> {
        //接受
        PromptManager.getInstance().stopRing();
        mHandler.removeCallbacksAndMessages(null);
        if(meetingMessage!=null&&meetingMessage.getRoomId()>0){
            //判断业务逻辑
            MyApplication.instance.stopAllBusiness();
            TerminalFactory.getSDK().getVideoMeetingManager().setMeetingStatus(true);
            Intent intent = new Intent(this, VideoMeetingService.class);
            intent.putExtra(Constants.ROOM_ID,meetingMessage.getRoomId());
            intent.putExtra(Constants.VIDEO_MEETING_TYPE,3);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(intent);
        }else{
            ToastUtil.showToast(this,getString(R.string.text_video_meeting_data_error));
        }
        TerminalFactory.getSDK().notifyReceiveHandler(
            ReceiveNotifyVideoMeetingMessageReduceUnreadCountHandler.class);
        stopBusiness();
    };


    private View.OnClickListener refuseOnClickListener = v -> {
        //拒绝
        //回复状态给服务端
        if(meetingMessage!=null){
            TerminalFactory.getSDK().getVideoMeetingManager().responseInvitationMessage(meetingMessage.getRoomId(),0);
        }
        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyVideoMeetingMessageReduceUnreadCountHandler.class);
        PromptManager.getInstance().stopRing();
        mHandler.removeCallbacksAndMessages(null);
        stopBusiness();
    };

    /**
     * 通知终端加入视频会商会议室
     */
    private ReceiveNotifyAddVideoMeetingMessageHandler receiveNotifyAddVideoMeetingMessageHandler = (notifyMessage) -> {
        //1.保存到数据库中，2.从数据库中查到所有正在会议的和时间最近的一条消息，3.刷新UI
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            if(notifyMessage!=null&&meetingMessage!=null){
                if(notifyMessage.getRoomId() == meetingMessage.getRoomId()&&!notifyMessage.getAddOrOutMeeting()){
                    VideoMeetingDataBean bean =  JSONObject.parseObject(notifyMessage.getMeetingDescribe(), VideoMeetingDataBean.class);
                    if(bean!=null&&bean.getStatus() == 2){
                        ToastUtil.showToast(MyApplication.instance, getString(R.string.text_video_meeting_end));
                    }else{
                        ToastUtil.showToast(MyApplication.instance, getString(R.string.text_you_were_kicked_outvideo_meeting));
                    }
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyVideoMeetingMessageReduceUnreadCountHandler.class);
                    mHandler.post(() -> stopBusiness());
                }
            }
        });
    };

    /**
     * 实体按键点击事件的回调
     */
    private ReceiverEntityKeyEventInServiceHandler receiverEntityKeyEventInServiceHandler = new ReceiverEntityKeyEventInServiceHandler(){

        @Override
        public void handler(KeyEvent event) {
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    mHandler.post(() -> {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                            PromptManager.getInstance().stopRing();
                            stopBusiness();
                        }
                    });
                    break;
                    default:break;
            }
        }
    };

    @Override
    public void onDestroy(){
        PromptManager.getInstance().stopRing();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverEntityKeyEventInServiceHandler);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
