package cn.vsx.vc.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingDataBean;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyAddVideoMeetingMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyVideoMeetingMessageReduceUnreadCountHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import ptt.terminalsdk.context.MyTerminalFactory;

public class VideoMeetingInvitationActivity extends BaseActivity {
  protected Logger logger = Logger.getLogger(this.getClass());
  private final String TAG = this.getClass().getName();

  private TextView mTvLiveReportName;
  private TextView mTvLiveReportId;
  private LinearLayout mLlLiveRespondRefuseTotal;
  private LinearLayout mLlLiveRespondAcceptTotal;

  private VideoMeetingMessage meetingMessage;

  @SuppressWarnings("HandlerLeak")
  private Handler mHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    meetingMessage =
        (VideoMeetingMessage) getIntent().getSerializableExtra(Constants.VIDEO_MEETING_MESSAGE);
    if (meetingMessage == null) {
      ToastUtil.showToast(MyApplication.instance,
          getString(R.string.text_video_meeting_data_error));
      finish();
      return;
    }
    super.onCreate(savedInstanceState);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  @Override
  public int getLayoutResId() {
    return R.layout.activity_video_meeting_invitation;
  }

  @Override
  protected void setOrientation() {
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  public void initView() {
    ImageView mIvAvatarReport = findViewById(R.id.iv_avatar_report);
    mIvAvatarReport.setImageResource(BitmapUtil.getUserPhoto());
    mTvLiveReportName = findViewById(R.id.tv_live_report_name);
    mTvLiveReportId = findViewById(R.id.tv_live_report_id);
    mLlLiveRespondRefuseTotal = findViewById(R.id.ll_live_respond_refuse_total);
    mLlLiveRespondAcceptTotal = findViewById(R.id.ll_live_respond_accept_total);
  }

  @Override
  public void initData() {
    mTvLiveReportName.setText(meetingMessage.getCreateTerminalName());
    mTvLiveReportId.setText(HandleIdUtil.handleId(meetingMessage.getCreateTerminalNo()));
    mHandler.postDelayed(() -> {
      PromptManager.getInstance().VideoLiveInCommimgRing();
    },1500);
    mHandler.postDelayed(() -> {
      VideoMeetingInvitationActivity.this.finish();
    },45 * 1000);
  }

  @Override
  public void initListener() {
    mLlLiveRespondAcceptTotal.setOnClickListener(acceptOnClickListener);
    mLlLiveRespondRefuseTotal.setOnClickListener(refuseOnClickListener);
    MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
  }


  private View.OnClickListener acceptOnClickListener = v -> {
    //接受
    PromptManager.getInstance().stopRing();
    mHandler.removeCallbacksAndMessages(null);
    if(meetingMessage!=null&&meetingMessage.getRoomId()>0){
      //判断业务逻辑
      MyApplication.instance.stopAllBusiness();
      Intent intent = new Intent(this, VideoMeetingActivity.class);
      intent.putExtra(Constants.ROOM_ID,meetingMessage.getRoomId());
      startActivity(intent);
    }else{
      ToastUtil.showToast(VideoMeetingInvitationActivity.this,getString(R.string.text_video_meeting_data_error));
    }
    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveNotifyVideoMeetingMessageReduceUnreadCountHandler.class);
    VideoMeetingInvitationActivity.this.finish();
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
    VideoMeetingInvitationActivity.this.finish();
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
          mHandler.post(() -> VideoMeetingInvitationActivity.this.finish());
        }
      }
    });
  };

  @Override
  public void doOtherDestroy() {
    logger.info(TAG + "--doOtherDestroy");
    PromptManager.getInstance().stopRing();
    MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
    mHandler.removeCallbacksAndMessages(null);
  }
}
