package cn.vsx.vc.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.IGotaKeyHandler;
import android.app.IGotaKeyMonitor;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.PhotoUtils;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;
import org.easydarwin.easypusher.BackgroundCameraService;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.MediaStream;
import org.easydarwin.push.UVCMediaStream;
import org.easydarwin.video.EasyRTSPClient;
import org.easydarwin.video.RTSPClient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.IndividualCallType;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.model.VideoMember;
import cn.vsx.hamster.terminalsdk.receiveHandler.IndividualCallPttStatusHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetAboutLiveMemberListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetRtspStreamUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetVideoPushUrlHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberJoinOrExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberNotLivingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNobodyRequestVideoLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyIndividualCallStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyInviteToWatchHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingStoppedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseMyselfLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUVCCameraConnectChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.LiveHistoryActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.activity.TransparentActivity;
import cn.vsx.vc.adapter.LiveContactsAdapter;
import cn.vsx.vc.adapter.MemberEnterAdapter;
import cn.vsx.vc.adapter.StackViewAdapter;
import cn.vsx.vc.adapter.WatchMemberAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.IBroadcastRecvHandler;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverRemoveWindowViewHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.receiver.NotificationClickReceiver;
import cn.vsx.vc.view.IndividualCallTimerView;
import cn.vsx.vc.view.IndividualCallView;
import cn.vsx.vc.view.flingswipe.SwipeFlingAdapterView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.service.KeepLiveManager;
import ptt.terminalsdk.tools.PhoneAdapter;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

/**
 * Created by jamie on 2017/10/18.
 * 后台service
 */

public class IndividualCallService extends Service implements RecvCallBack,Actions {
    private FrameLayout view;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;//小窗口
    private WindowManager.LayoutParams layoutParams1;//大窗口
    private WindowManager.LayoutParams layoutParams2;//弹窗
    private boolean viewAdded = false;
    private boolean switchCameraViewAdd = false;
    private boolean dialogAdded;
    private IndividualCallBinder individualCallBinder = new IndividualCallBinder();
    private int status = 0;
    private String comingName;
    private int stopMethodResult ;
    private String callResultDesc;
    private int groupSpeak;
    private int speakId;
    private PowerManager.WakeLock wakeLockComing;
    private String live_theme;
    float downX = 0;
    float downY = 0;
    int oddOffsetX = 0;
    int oddOffsetY = 0;
    private static OnClickListener mListener;
    private int screenWidth;
    //监听Home
    private HomeWatcherReceiver mHomeKeyReceiver;
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private boolean isPulling;//正在看直播
    private boolean isPushing;//正在上报
    private RelativeLayout video_dialog;
    private StackViewAdapter stackViewAdapter;
    private List<Member> selectItem = new ArrayList<>();
    private String streamMediaServerUrl;
    private static final String STATE = "state";
    private static final int MSG_STATE = 1;
    private static final int CURRENTTIME = 2;
    private static final int AUTOHANGUP = 3;
    private static final int HIDELIVINGVIEW = 4;
    private static final int CLOSEPRIVATECALL = 5;
    private static final int DISSMISS_CURRENT_DIALOG = 6;
    private static final int WATCH_LIVE = 7;

    //页面
    RelativeLayout pop_minimize;
    RelativeLayout individual_call_request;

    RelativeLayout individual_call_chooice;

    LinearLayout individual_call_speaking;

    LinearLayout emergency_group_call;
    RelativeLayout layout_call_prompt;

    IndividualCallTimerView popup_ICTV_speaking_time;
    //请求个呼页面

    TextView tv_request_prompt;

    TextView tv_member_name_request;

    TextView tv_member_id_request;

    TextView tv_requestCall;

    LinearLayout ll_individual_call_hangup_request;

    ImageView ll_individual_call_retract_request;

    View individual_call_request_view;

    //个呼选择页面

    TextView tv_chooice_prompt;
    TextView tv_comingCall;

    TextView tv_member_name_chooice;

    TextView tv_member_id_chooice;

    LinearLayout ll_individual_call_accept;

    LinearLayout ll_individual_call_refuse;
    ImageView individual_call_retract_emergency;

    //个呼说话页面

    TextView tv_speaking_prompt;
    ImageView red_phone;


    TextView tv_member_name_speaking;
    TextView tv_member_id_speaking;
    TextView tv_speaking_toast;
    ImageView ll_individual_call_hangup_speaking;
    ImageView ll_individual_call_retract_speaking;
    IndividualCallView individualCallView;
    TextView tv_waiting;
    View individual_call_speak_view;

    //个呼半双工
    RelativeLayout individual_call_half_duplex;
    ImageView ll_individual_call_retract_half_duplex;
    TextView tv_member_name_half_duplex;
    TextView tv_member_id_half_duplex;
    TextView tv_half_duplex_prompt;
    ImageView ll_individual_call_hangup_half_duplex;
    IndividualCallView ICTV_half_duplex_time_speaking;
    Button individual_call_half_duplex_ptt;
    View individual_call_half_duplex_view;

    //直播请求界面
    @Bind(R.id.live_request)
    RelativeLayout live_request;
    @Bind(R.id.tv_live_request_name)
    TextView tv_live_request_name;
    @Bind(R.id.tv_live_request_id)
    TextView tv_live_request_id;
    @Bind(R.id.ll_live_request_stop_total)
    LinearLayout ll_live_request_stop_total;
    @Bind(R.id.ll_live_request_stop)
    LinearLayout ll_live_request_stop;
    @Bind(R.id.iv_avatar_request)
    ImageView ivAvaTarRequest;

    //直播应答界面
    @Bind(R.id.live_report)
    RelativeLayout live_report;
    @Bind(R.id.tv_live_report_name)
    TextView tv_live_report_name;
    @Bind(R.id.tv_live_report_id)
    TextView tv_live_report_id;
    @Bind(R.id.ll_live_respond_refuse_total)
    LinearLayout ll_live_respond_refuse_total;
    @Bind(R.id.ll_live_respond_refuse)
    LinearLayout ll_live_respond_refuse;
    @Bind(R.id.ll_live_respond_accept_total)
    LinearLayout ll_live_respond_accept_total;
    @Bind(R.id.ll_live_respond_accept)
    LinearLayout ll_live_respond_accept;
    @Bind(R.id.iv_avatar_report)
    ImageView ivAvaTarReport;

    //直播选择成员列表
    @Bind(R.id.live_select_member)
    LinearLayout live_select_member;
    @Bind(R.id.iv_live_selectmember_return)
    ImageView iv_live_selectmember_return;
    @Bind(R.id.et_search_member_reported)
    AutoCompleteTextView et_search_member_reported;
    @Bind(R.id.iv_live_selectmember_search)
    ImageView iv_live_selectmember_search;
    @Bind(R.id.btn_live_selectmember_start)
    Button btn_live_selectmember_start;
    @Bind(R.id.ll_live_selectmember_theme)
    LinearLayout ll_live_selectmember_theme;
    @Bind(R.id.tv_live_selectmember_theme)
    TextView tv_live_selectmember_theme;
    @Bind(R.id.iv_live_selectmember_theme)
    ImageView iv_live_selectmember_theme;
    @Bind(R.id.tv_checktext)
    TextView tv_checktext;
    @Bind(R.id.search_select)
    ImageView search_select;
    @Bind(R.id.et_search_allcontacts)
    EditText et_search_allcontacts;
    @Bind(R.id.horizonMenu)
    HorizontalScrollView horizonMenu;
    @Bind(R.id.img_cencle)
    ImageView img_cencle;
    @Bind(R.id.ll_no_info)
    LinearLayout ll_no_info;
    @Bind(R.id.tv_no_user)
    TextView tv_no_user;
    @Bind(R.id.lv_live_selsectmember_listview)
    ListView lv_live_selsectmember_listview;
    //编辑主题
    @Bind(R.id.ll_live_edit_theme)
    LinearLayout live_edit_theme;
    @Bind(R.id.iv_live_edit_return)
    ImageView iv_live_edit_return;
    @Bind(R.id.et_live_edit_import_theme)
    EditText et_live_edit_import_theme;
    @Bind(R.id.btn_live_edit_confirm)
    Button btn_live_edit_confirm;
    //直播主界面
    @Bind(R.id.live)
    RelativeLayout live;
    @Bind(R.id.sv_live)
    TextureView svLive;
    @Bind(R.id.my_view2)
    View my_view2;
    @Bind(R.id.live_vedioTheme)
    TextView live_vedioTheme;
    @Bind(R.id.tv_live_groupName)
    TextView tv_live_groupName;
    @Bind(R.id.live_vedioName)
    TextView live_vedioName;
    @Bind(R.id.live_vedioIcon)
    ImageView live_vedioIcon;
    @Bind(R.id.live_vedioId)
    TextView live_vedioId;
    @Bind(R.id.tv_live_speakingName)
    TextView tv_live_speakingName;
    @Bind(R.id.tv_live_speakingId)
    TextView tv_live_speakingId;
    @Bind(R.id.iv_live_retract)
    ImageView iv_live_retract;
    @Bind(R.id.tv_live_realtime)
    TextView tv_live_realtime;
    @Bind(R.id.tv_spn_resolution)
    TextView tv_spn_resolution;
    @Bind(R.id.spn_resolution)
    Spinner spnResolution;
    @Bind(R.id.lv_live_member_info)
    ListView lv_live_member_info;
    @Bind(R.id.ll_live_chage_camera)
    LinearLayout ll_live_chage_camera;
    @Bind(R.id.iv_live_chage_camera)
    ImageView iv_live_chage_camera;
    @Bind(R.id.ll_live_hangup_total)
    LinearLayout ll_live_hangup_total;
    @Bind(R.id.ll_live_invite_member)
    LinearLayout ll_live_invite_member;
    @Bind(R.id.iv_live_addmember)
    ImageView iv_live_addmember;
    @Bind(R.id.ll_live_look_hangup)
    LinearLayout ll_live_look_hangup;
    @Bind(R.id.ll_live_look_invite_member)
    LinearLayout ll_live_look_invite_member;
    @Bind(R.id.iv_live_look_addmember)
    ImageView iv_live_look_addmember;
    @Bind(R.id.btn_live_look_ptt)
    Button btn_live_look_ptt;
    @Bind(R.id.ll_live_group_call)
    LinearLayout ll_live_group_call;
    //直播pop
    @Bind(R.id.popup_mini_live)
    RelativeLayout popup_mini_live;
    @Bind(R.id.sv_live_pop)
    TextureView sv_live_pop;
    //弹窗
    @Bind(R.id.swipeFlingAdapterView)
    SwipeFlingAdapterView swipeFlingAdapterView;

    @Bind(R.id.usb_live)
    RelativeLayout usbLive;
    @Bind(R.id.sv_uvc_live)
    TextureView sv_uvc_live;
    @Bind(R.id.tv_uvc_live_time)
    TextView tv_uvc_live_time;
    @Bind(R.id.tv_uvc_liveTheme)
    TextView tv_uvc_liveTheme;
    @Bind(R.id.iv_uvc_live_retract)
    ImageView iv_uvc_live_retract;
    @Bind(R.id.lv_uvc_live_member_info)
    ListView lv_uvc_live_member_info;
    @Bind(R.id.iv_uvc_hangup)
    ImageView iv_uvc_hangup;
    @Bind(R.id.iv_uvc_invite_member)
    ImageView iv_uvc_invite_member;
    @Bind(R.id.ll_uvc_speak_state)
    LinearLayout ll_uvc_speak_state;
    @Bind(R.id.tv_uvc_live_speakingName)
    TextView tv_uvc_live_speakingName;
    @Bind(R.id.tv_uvc_live_groupName)
    TextView tv_uvc_live_groupName;
    @Bind(R.id.tv_uvc_live_speakingId)
    TextView tv_uvc_live_speakingId;
    @Bind(R.id.ll_function)
    LinearLayout ll_function;
    ImageView iv_phone_camera;
    ImageView iv_out_camera;

    @SuppressWarnings("HandlerLeak,SimpleDateFormat")
    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case AUTOHANGUP:
                    myHandler.removeMessages(AUTOHANGUP);
                    logger.error("执行了半双工超时机制；挂断个呼！！！");
                    MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
                    popup_ICTV_speaking_time.pause();
                    ICTV_half_duplex_time_speaking.pause();
                    tv_half_duplex_prompt.setText("通话结束");
                    removeView();
                    break;
                case MSG_STATE:
                    String state = msg.getData().getString("state");
                    ToastUtil.showToast(IndividualCallService.this, state);
                    break;
                case CURRENTTIME:
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date currentTime = new Date();
                    dateString = formatter.format(currentTime);
                    if(currentType == MyApplication.TYPE.PUSH){
                        tv_live_realtime.setText(dateString);
                    }else if(currentType == MyApplication.TYPE.UVCPUSH){
                        tv_uvc_live_time.setText(dateString);
                    }
                    sendEmptyMessageDelayed(CURRENTTIME, 10000);
                    break;
                case HIDELIVINGVIEW:
                    myHandler.removeMessages(HIDELIVINGVIEW);
                    if(currentType == MyApplication.TYPE.PUSH){
                        hideLivingView();
                    }else if(currentType == MyApplication.TYPE.UVCPUSH){
                        hideUVCLiveView();
                    }
                    break;
                case CLOSEPRIVATECALL:
                    callMember = null;
                    tv_request_prompt.setText(callResultDesc);
                    tv_speaking_toast.setVisibility(View.VISIBLE);
                    tv_speaking_toast.setText(callResultDesc);
                    popup_ICTV_speaking_time.stop();
                    logger.info("CLOSEPRIVATECALL:"+callResultDesc);
                    removeView();
                    ToastUtil.showToast(getApplicationContext(),callResultDesc);
                    break;
                case DISSMISS_CURRENT_DIALOG:
                    TerminalMessage terminalMessage = (TerminalMessage) msg.obj;
                    if(dialogAdded && null !=stackViewAdapter && data.contains(terminalMessage)){
                        data.remove(terminalMessage);
                        stackViewAdapter.setData(data);
                    }
                    break;
                case WATCH_LIVE:
                    TerminalMessage terminalMessage1 = (TerminalMessage) msg.obj;
                    int position = msg.arg1;
                    PTTProtolbuf.NotifyDataMessage.Builder builder = PTTProtolbuf.NotifyDataMessage.newBuilder();
                    builder.setMessageUrl(terminalMessage1.messageUrl);
                    builder.setMessageFromName(terminalMessage1.messageFromName);
                    builder.setMessageFromNo(terminalMessage1.messageFromId);
                    builder.setMessageToName(terminalMessage1.messageToName);
                    builder.setMessageToNo(terminalMessage1.messageToId);
                    builder.setMessageType(terminalMessage1.messageType);
                    builder.setMessageVersion(terminalMessage1.messageVersion);
                    builder.setResultCode(terminalMessage1.resultCode);
                    builder.setSendingTime(terminalMessage1.sendTime);
                    builder.setMessageBody(terminalMessage1.messageBody.toString());
                    PTTProtolbuf.NotifyDataMessage message = builder.build();
                    int resultCode = MyTerminalFactory.getSDK().getLiveManager().requestToWatchLiving(message);
                    if (resultCode == 0) {
                        live_theme = terminalMessage1.messageFromName+"上报图像";
                        if (TextUtils.isEmpty(terminalMessage1.messageBody.getString(JsonParam.TITLE))){
                            String liver = (String) data.get(position).messageBody.get("liver");
                            if(!TextUtils.isEmpty(liver)){
                                if(liver.contains("_")){
                                    String[] split = liver.split("_");
                                    if(split.length>0){
                                        live_theme = split[1]+"上报图像";
                                    }
                                }
                            }
                        }else {
                            live_theme = terminalMessage1.messageBody.getString(JsonParam.TITLE);
                        }
                        data.remove(stackViewAdapter.getItem(position));
                        stackViewAdapter.remove(position);
                        video_dialog.setVisibility(View.GONE);
                    } else {
                        ToastUtil.livingFailToast(IndividualCallService.this, resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private String theme;

    private List<Member> memberList = new ArrayList<>();
    private MyApplication.TYPE currentType;
    private LiveContactsAdapter liveContactsAdapter;
    private List<VideoMember> memberEnterList = new ArrayList<>();
    private List<VideoMember> watchLiveList = new ArrayList<>();
    private MemberEnterAdapter memberEnterAdapter ;
    private WatchMemberAdapter watchMemberAdapter ;
    private Logger logger = Logger.getLogger(this.getClass());
    private ServiceConnection conn;
    private BackgroundCameraService mService;
    private EasyRTSPClient mStreamRender;
    private int total = 0;
    private List<Integer> pushMemberList;
    private List<TerminalMessage> data = new ArrayList<>();
    private Member callMember;//个呼的成员
    private IGotaKeyMonitor keyMointor;
    private IGotaKeyHandler gotaKeyHandler;
    private int pushcount;
    private View switchCameraView;
    private boolean activePush;//是否主动上报，区别于被动收到图像请求


    public static void setOnClickListener(OnClickListener listener){
        mListener = listener;
    }

    interface OnClickListener{
        void onClick(View view);
    }

    public class IndividualCallBinder extends Binder {
        public void removeServiceView(){
            popup_ICTV_speaking_time.stop();
            if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                individualCallView.pause();
            }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                ICTV_half_duplex_time_speaking.pause();
            }
            removeView();
        }
        /**个呼到来页面*/
        private void showPop(){
//            btn_live_selectmember_start.setText("开始");
//            btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
            tv_checktext.setText("");
            selectItem.clear();
            tv_comingCall.setText("来电...");
            refresh();
            hideAllView();
            individual_call_chooice.setVisibility(View.VISIBLE);

            hideKey();
            tv_member_id_chooice.setText(HandleIdUtil.handleId(callMember.getNo()));
            tv_member_name_chooice.setText(HandleIdUtil.handleName(comingName));
            ll_individual_call_accept.setEnabled(true);
            tv_chooice_prompt.setVisibility(View.GONE);
            PromptManager.getInstance().IndividualCallNotifyRing();
        }
        /**主动发起个呼*/
        private void showIndividualCallRequest(final Member member) {

            refresh();
            hideAllView();

            tv_requestCall.setText("正在拨号...");
            tv_request_prompt.setVisibility(View.GONE);
            individual_call_request.setVisibility(View.VISIBLE);
            tv_member_name_request.setText(member.getName());
            tv_member_id_request.setText(HandleIdUtil.handleId(member.no));
            PromptManager.getInstance().IndividualCallRequestRing();

            if (PhoneAdapter.isF25()) {
                individual_call_request_view.setVisibility(View.GONE);
            } else {
                individual_call_request_view.setVisibility(View.VISIBLE);
            }


        }


        /**主叫时，对方接听*/
        private void callAnswer(){
            status = 2;
            if (pop_minimize.getVisibility()==View.VISIBLE){
                PromptManager.getInstance().stopRing();
                tv_waiting.setVisibility(View.GONE);
                //开始计时
                individualCallView.stop();
                individualCallView.start();
                individualCallView.setVisibility(View.VISIBLE);
                popup_ICTV_speaking_time.stop();
                popup_ICTV_speaking_time.start();
                popup_ICTV_speaking_time.setVisibility(View.VISIBLE);

                if(callType==IndividualCallType.FULL_DUPLEX.getCode()){

                    tv_member_id_speaking.setText(HandleIdUtil.handleId(MyApplication.instance.calleeMember.getNo()));
                    tv_member_name_speaking.setText(MyApplication.instance.calleeMember.getName());
                    popup_ICTV_speaking_time.stop();
                    popup_ICTV_speaking_time.start();
                    recoverSpeakingPop();
                    individualCallView.stop();
                    individualCallView.start();

                    PromptManager.getInstance().stopRing();

                }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                    ICTV_half_duplex_time_speaking.stop();
                    ICTV_half_duplex_time_speaking.start();
                    tv_member_id_half_duplex.setText(HandleIdUtil.handleId(MyApplication.instance.calleeMember.no));
                    tv_member_name_half_duplex.setText(MyApplication.instance.calleeMember.getName());
                    popup_ICTV_speaking_time.stop();
                    popup_ICTV_speaking_time.start();
                    recoverSpeakingPop();
                    ICTV_half_duplex_time_speaking.stop();
                    ICTV_half_duplex_time_speaking.start();
                    individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                    individual_call_half_duplex_ptt.setEnabled(true);
                    PromptManager.getInstance().stopRing();
                    startAutoHangUpTimer();//半双工对方接听，启动超时检测

                }


            }else{
                refresh();
                hideAllView();
                if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                    individual_call_speaking.setVisibility(View.VISIBLE);
                    tv_member_id_speaking.setText(HandleIdUtil.handleId(MyApplication.instance.calleeMember.getNo()));
                    tv_member_name_speaking.setText(MyApplication.instance.calleeMember.getName());
                    popup_ICTV_speaking_time.stop();
                    popup_ICTV_speaking_time.start();
                    popup_ICTV_speaking_time.setVisibility(View.VISIBLE);
                    recoverSpeakingPop();
                    individualCallView.setVisibility(View.VISIBLE);
                    individualCallView.stop();
                    individualCallView.start();
                    tv_waiting.setVisibility(View.GONE);

                    PromptManager.getInstance().stopRing();

                }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                    ICTV_half_duplex_time_speaking.setVisibility(View.VISIBLE);
                    ICTV_half_duplex_time_speaking.stop();
                    ICTV_half_duplex_time_speaking.start();
                    individual_call_half_duplex.setVisibility(View.VISIBLE);
                    tv_member_id_half_duplex.setText(HandleIdUtil.handleId(MyApplication.instance.calleeMember.no));
                    tv_member_name_half_duplex.setText(MyApplication.instance.calleeMember.getName());
                    popup_ICTV_speaking_time.stop();
                    popup_ICTV_speaking_time.start();
                    popup_ICTV_speaking_time.setVisibility(View.VISIBLE);
                    recoverSpeakingPop();
                    ICTV_half_duplex_time_speaking.stop();
                    ICTV_half_duplex_time_speaking.start();
                    ICTV_half_duplex_time_speaking.setVisibility(View.VISIBLE);
                    tv_waiting.setVisibility(View.GONE);
                    individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                    individual_call_half_duplex_ptt.setEnabled(true);
                    PromptManager.getInstance().stopRing();
                    startAutoHangUpTimer();//半双工对方接听，启动超时检测

                }

            }

        }
        /**个呼时对方繁忙*/
        private void busyWithEachOther(){
            myHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    tv_request_prompt.setText(callResultDesc);
                    tv_speaking_toast.setVisibility(View.VISIBLE);
                    tv_speaking_toast.setText(callResultDesc);
                    popup_ICTV_speaking_time.stop();
                    logger.info("CLOSEPRIVATECALL:"+callResultDesc);
                    removeView();
                    ToastUtil.showToast(getApplicationContext(),callResultDesc);
                    PromptManager.getInstance().IndividualHangUpRing();
                    PromptManager.getInstance().delayedStopRing();
                }
            },1000);

            //关闭个呼呼叫界面
//            myHandler.sendEmptyMessageDelayed(CLOSEPRIVATECALL,10000);
        }
        private void callRejection(){

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    tv_request_prompt.setVisibility(View.VISIBLE);
                    tv_request_prompt.setText(callResultDesc);
                }
            });

            //关闭个呼呼叫界面
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv_request_prompt.setText("正在呼叫");
                    individual_call_request.setVisibility(View.GONE);

                    MyApplication.instance.isPopupWindowShow = false;
                    removeView();
                    PromptManager.getInstance().stopRing();
                }
            },1000);
        }
        private void individualCallStopped(){
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    popup_ICTV_speaking_time.pause();
                    if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                        individualCallView.pause();
                        tv_speaking_prompt.setText("通话结束");
                        tv_speaking_toast.setVisibility(View.VISIBLE);
                        tv_speaking_toast.setText("对方已挂断，通话结束");
                    }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                        ICTV_half_duplex_time_speaking.pause();
                        tv_half_duplex_prompt.setText("通话结束");
                    }

                    if (SignalServerErrorCode.getInstanceByCode(stopMethodResult) != null) {
                        tv_chooice_prompt.setText(SignalServerErrorCode.getInstanceByCode(stopMethodResult).getErrorDiscribe());
                    } else {
                        tv_chooice_prompt.setVisibility(View.VISIBLE);
                        tv_chooice_prompt.setText("对方已取消");
                        popup_ICTV_speaking_time.stop();
                    }
                    //如果弹窗已经添加，显示弹窗内容
                    if(dialogAdded){
                        showDialogView();
                    }else {
                        removeView();
                    }
                    popup_ICTV_speaking_time.stop();
                    finishTransparentActivity();
                    ll_individual_call_accept.setEnabled(false);;
                    PromptManager.getInstance().IndividualHangUpRing();
                    PromptManager.getInstance().delayedStopRing();
                }
            });
        }
    }

    private void cancelAutoHangUpTimer(){
        logger.info("取消半双工超时检测");
        myHandler.removeMessages(AUTOHANGUP);
    }
    private void startAutoHangUpTimer() {
        logger.error("lls/"+MyApplication.instance.getIndividualState());
        if(MyApplication.instance.getIndividualState() == IndividualCallState.SPEAKING||MyApplication.instance.getIndividualState()==IndividualCallState.RINGING){
            logger.info("启动了半双工超时检测机制；10秒后将自动挂断！！！");
            myHandler.removeMessages(AUTOHANGUP);
            myHandler.sendEmptyMessageDelayed(AUTOHANGUP,10000);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        KeepLiveManager.getInstance().setServiceForeground(this);
        MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
            @Override
            public void run(){
                MyTerminalFactory.getSDK().start();
            }
        });
        return individualCallBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // service被异常停止后，系统尝试重启service，不能保证100%重启成功
//        KeepLiveManager.getInstance().setServiceForeground(this);
        return START_STICKY;
    }

    public IndividualCallService() {
        super();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //GH880手机按键服务
        keyMointor =(IGotaKeyMonitor)getSystemService("gotakeymonitor");

        if(powerManager!=null){
            wakeLockComing = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "wakeLock3");//
            logger.info("wakeLock3 = "+wakeLockComing);
        }

        if(keyMointor !=null){
            try{
                gotaKeyHandler = keyMointor.setHandler(new GotaKeHandler());
            }catch (Exception e){

            }
        }

        regBroadcastRecv(CALL_COMING_NAME,CALL_REFUSE_TO_ANSWER,CALL_STOPPED,KILL_ACT_CALL,SEND_LIVE_THEME);
        createFloatView();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCurrentGroupIndividualCallHandler);


        MyTerminalFactory.getSDK().registReceiveHandler(receiveUVCCameraConnectChangeHandler);//直播成功
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseMyselfLiveHandler);//直播成功
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);//请求时，对方拒绝
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingStoppedHandler);//通知停止直播
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);//直播应答超时
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetVideoPushUrlHandler);//自己发起直播的响应
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberJoinOrExitHandler);//通知有人加入或离开
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetRtspStreamUrlHandler);//观看视频
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);//请求开视频
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberNotLivingHandler);//观看时，发现没有在直播
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetAboutLiveMemberListHandler);//推送列表
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNobodyRequestVideoLiveHandler);//对方取消请求
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverActivePushVideoHandler);//上报视频
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverRequestVideoHandler);//请求视频

        MyTerminalFactory.getSDK().registReceiveHandler(individualCallPttStatusHandler);//手台按下PTT
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyInviteToWatchHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveUpdateConfigHandler);

        //注册Home和最近任务监听广播
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mHomeKeyReceiver, homeFilter);
        initResolution();
        startLiveService();
        memberEnterAdapter= new MemberEnterAdapter(getApplicationContext(), memberEnterList);
        watchMemberAdapter= new WatchMemberAdapter(getApplicationContext(), watchLiveList);
    }

    /**
     * 根据权限设置组呼PTT图标
     */
    private void setGroupCallAuthority() {
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            if(null !=btn_live_look_ptt &&viewAdded){
                btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            }
        }else{
            if(null !=btn_live_look_ptt &&viewAdded){
                btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_silence);
            }
        }
        //图像推送
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            ll_live_look_invite_member.setVisibility(View.GONE);
        }else {
            if(ll_live_look_hangup.getVisibility()==View.VISIBLE){
                ll_live_look_invite_member.setVisibility(View.VISIBLE);
            }

        }
        //图像上报
        if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            if(ll_live_hangup_total.getVisibility()==View.VISIBLE){
                ll_live_invite_member.setVisibility(View.VISIBLE);
            }

        }else{
            ll_live_invite_member.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
        //取消监听
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mHomeKeyReceiver);
        MyTerminalFactory.getSDK().stop();
    }


    @SuppressWarnings("ClickableViewAccessibility,InflateParams")
    public void createFloatView(){
        regBroadcastRecv(CALL_COMING_NAME,CALL_REFUSE_TO_ANSWER,CALL_STOPPED,KILL_ACT_CALL,SEND_LIVE_THEME);

        view = (FrameLayout) LayoutInflater.from(MyApplication.instance.getApplicationContext()).inflate(R.layout.individual_call,null);

        pop_minimize = (RelativeLayout) view.findViewById(R.id.pop_minimize);//小窗
        tv_waiting = (TextView) view.findViewById(R.id.tv_waiting);
        popup_ICTV_speaking_time = (IndividualCallTimerView) view.findViewById(R.id.popup_ICTV_speaking_time);

        individual_call_chooice = (RelativeLayout) view.findViewById(R.id.individual_call_chooice);//选择页面
        tv_member_name_chooice = (TextView) view.findViewById(R.id.tv_member_name_chooice);
        tv_member_id_chooice = (TextView) view.findViewById(R.id.tv_member_id_chooice);
        ll_individual_call_refuse = (LinearLayout) view.findViewById(R.id.ll_individual_call_refuse);
        ll_individual_call_accept = (LinearLayout) view.findViewById(R.id.ll_individual_call_accept);
        tv_chooice_prompt = (TextView) view.findViewById(R.id.tv_chooice_prompt);
        tv_comingCall = (TextView) view.findViewById(R.id.tv_comingCall);
        individual_call_retract_emergency = (ImageView) view.findViewById(R.id.individual_call_retract_emergency);

        individual_call_request = (RelativeLayout) view.findViewById(R.id.individual_call_request);//请求个呼
        individual_call_request_view = view.findViewById(R.id.individual_call_request_view);
        tv_request_prompt = (TextView) view.findViewById(R.id.tv_request_prompt);
        tv_member_name_request = (TextView) view.findViewById(R.id.tv_member_name_request);
        tv_member_id_request = (TextView) view.findViewById(R.id.tv_member_id_request);
        tv_requestCall =(TextView) view.findViewById(R.id.tv_requestCall);
        ll_individual_call_retract_request = (ImageView) view.findViewById(R.id.ll_individual_call_retract_request);
        ll_individual_call_hangup_request = (LinearLayout) view.findViewById(R.id.ll_individual_call_hangup_request);

        individual_call_speaking = (LinearLayout) view.findViewById(R.id.individual_call_speaking);//说话页面
        red_phone = (ImageView)view.findViewById(R.id.red_phone);
        ll_individual_call_retract_speaking = (ImageView) view.findViewById(R.id.ll_individual_call_retract_speaking);
        individual_call_speak_view = view.findViewById(R.id.individual_call_speak_view);
        individualCallView = (IndividualCallView) view.findViewById(R.id.ICTV_speaking_time_speaking);
        tv_speaking_prompt = (TextView) view.findViewById(R.id.tv_speaking_prompt);
        tv_member_id_speaking = (TextView) view.findViewById(R.id.tv_member_id_speaking);
        tv_member_name_speaking = (TextView) view.findViewById(R.id.tv_member_name_speaking);
        tv_speaking_toast =(TextView) view.findViewById(R.id.tv_speaking_toast);
        ll_individual_call_hangup_speaking = (ImageView) view.findViewById(R.id.ll_individual_call_hangup_speaking);

        individual_call_half_duplex =(RelativeLayout) view.findViewById(R.id.individual_call_half_duplex);//半双工页面
        ll_individual_call_retract_half_duplex = (ImageView)view.findViewById(R.id.ll_individual_call_retract_half_duplex);
        tv_member_name_half_duplex = (TextView)view.findViewById(R.id.tv_member_name_half_duplex);
        tv_member_id_half_duplex =(TextView) view.findViewById(R.id.tv_member_id_half_duplex);
        tv_half_duplex_prompt = (TextView)view.findViewById(R.id.tv_half_duplex_prompt);
        individual_call_half_duplex_view = view.findViewById(R.id.individual_call_half_duplex_view);
        ll_individual_call_hangup_half_duplex =(ImageView) view.findViewById(R.id.ll_individual_call_hangup_half_duplex);
        ICTV_half_duplex_time_speaking = (IndividualCallView)view.findViewById(R.id.ICTV_half_duplex_time_speaking);
        individual_call_half_duplex_ptt =(Button) view.findViewById(R.id.individual_call_half_duplex_ptt);

        emergency_group_call = (LinearLayout) view.findViewById(R.id.emergency_group_call);//紧急呼叫
        layout_call_prompt = (RelativeLayout) view.findViewById(R.id.layout_call_prompt);
        video_dialog = (RelativeLayout) view.findViewById(R.id.video_dialog);

        ButterKnife.bind(this, view);

        switchCameraView = LayoutInflater.from(MyApplication.instance.getApplicationContext()).inflate(R.layout.layout_switch_camera,null);

        iv_phone_camera = switchCameraView.findViewById(R.id.iv_phone_camera);
        iv_out_camera = switchCameraView.findViewById(R.id.iv_out_camera);
        iv_phone_camera.setOnClickListener(new PhoneCameraClickListener());
        iv_out_camera.setOnClickListener(new OutCameraClickListener());

        individual_call_half_duplex_ptt.setOnTouchListener(new HalfDuplexPttOnTouchListener());
        svLive.setOpaque(false);
        svLive.setSurfaceTextureListener(new SurfaceTextureListener());
        sv_uvc_live.setSurfaceTextureListener(new SurfaceTextureListener());
        sv_live_pop.setSurfaceTextureListener(new SurfaceTextureListener());
        svLive.setOnClickListener(new OnClickListenerAutoFocus());
        sv_uvc_live.setOnClickListener(new OnClickListenerAutoFocus());
        img_cencle.setOnClickListener(new ImgCancelOnClickListener());
        et_search_allcontacts.addTextChangedListener(new EditChangeListener());
        et_search_allcontacts.setOnFocusChangeListener(new EditListener());
        //被叫请求界面
        ll_live_respond_accept.setOnClickListener(new OtherReportAgreeOnClickListener());
        ll_live_respond_refuse.setOnClickListener(new OtherReportHangUpOnClickListener());
        //主叫请求界面
        ll_live_request_stop.setOnClickListener(new RequestOtherHangUpOnClickListener());
        //选择推送成员界面
        ll_live_selectmember_theme.setOnClickListener(new EditThemeOnclickListener());
        btn_live_selectmember_start.setOnClickListener(new SelectMemberOKOnClickListener());
        iv_live_selectmember_return.setOnClickListener(new SelectMemberReturnOnClickListener());
//        lv_live_selsectmember_listview.setOnItemClickListener(new SelectMemberOnItemClickListener());
        iv_live_selectmember_search.setOnClickListener(new SearchMemberOnClickListener());
        et_search_member_reported.addTextChangedListener(new SearchMemberTextChangeListener());
        //直播界面
        ll_live_chage_camera.setOnClickListener(new ChangeCameraOnClickListener());
//        lv_live_member_info.setOnItemClickListener(new MemberListViewOnClickListener());
        lv_live_member_info.setOnTouchListener(new LiveMemberListTouchListener());
        ll_live_hangup_total.setOnClickListener(new LiveHangUpOnClikListener());
        iv_uvc_hangup.setOnClickListener(new LiveHangUpOnClikListener());
        live_vedioIcon.setOnClickListener(new LiveToMemberInfoOnClickListener());
        // 发起图像推送的页面挂断/邀请成员按钮
        ll_live_look_invite_member.setOnClickListener(new AddLookMemberOnClickListener());
        ll_live_look_hangup.setOnClickListener(new LiveHangUpLookOnClikListener() );
        //ptt按钮
        btn_live_look_ptt.setOnTouchListener(new PttOnTouchListener());
        ll_live_invite_member.setOnClickListener(new InviteMemberOnClickListener());
        iv_uvc_invite_member.setOnClickListener(new InviteMemberOnClickListener());
        iv_live_retract.setOnClickListener(new RetractOnClickListener());
        iv_uvc_live_retract.setOnClickListener(new RetractOnClickListener());
        popup_mini_live.setOnTouchListener(new PopMiniLiveOnTouchListener());
        iv_live_edit_return.setOnClickListener(new EditThemeReturnOnClickListener());
        btn_live_edit_confirm.setOnClickListener(new EditThemeConfirmOnClickListener());
        et_live_edit_import_theme.addTextChangedListener(new LiveThemeTextWatcher());
        et_search_allcontacts.addTextChangedListener(new SearchContactsTextWatcher());
        //监听方法
        ll_individual_call_accept.setOnTouchListener(new OnTouchListenerImplementationChooiceAccept());
        ll_individual_call_refuse.setOnTouchListener(new OnTouchListenerImplementationChooiceRefuse());
        ll_individual_call_hangup_speaking.setOnClickListener(new OnClickListenerImplementationSpeakingHangup());
        ll_individual_call_hangup_half_duplex.setOnClickListener(new OnClickListenerImplementationSpeakingHangup());
        ll_individual_call_hangup_request.setOnClickListener(new OnClickListenerImplementationRequestHangup());
        ll_individual_call_retract_request.setOnClickListener(new OnClickListenerCallRequsetWindow());//请求个呼窗口化
        ll_individual_call_retract_speaking.setOnClickListener(new OnClickListenerCallSpeakingWindow());//正在讲话窗口化
        ll_individual_call_retract_half_duplex.setOnClickListener(new OnClickListenerCallSpeakingWindow());//半双工窗口化
        individual_call_retract_emergency.setOnClickListener(new OnClickListenerCallCommingWindow());
        pop_minimize.setOnTouchListener(new PopMiniOnTouchListener());

        lv_uvc_live_member_info.setOnTouchListener(new LiveMemberListTouchListener());
        layout_call_prompt.setVisibility(View.GONE);

        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.gravity = Gravity.RIGHT|Gravity.TOP;
        //小窗口type，要让下层view可以获取焦点
        layoutParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        layoutParams1 = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED, PixelFormat.RGBA_8888);

        //大窗口
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams1.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams1.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams1.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        layoutParams1.gravity = Gravity.CENTER;
        //大窗口type，下层view不获取焦点
        layoutParams1.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        //弹窗
        layoutParams2 = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);
        //type为"TYPE_TOAST"在sdk19之前不接收事件,之后可以
        //type为"TYPE_PHONE"需要"SYSTEM_ALERT_WINDOW"权限.在sdk19之前不可以直接申明使用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams2.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams2.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams2.gravity = Gravity.CENTER;
        layoutParams2.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
    }

    private void getSv(final MyApplication.TYPE currentType, final TextureView textureView) {
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (textureView.getSurfaceTexture() != null){
                    if (currentType == MyApplication.TYPE.PUSH ){
                        pushStream(textureView.getSurfaceTexture());
                    }else if(currentType == MyApplication.TYPE.UVCPUSH){
                        uvcMediaStream.setSurfaceTexture(textureView.getSurfaceTexture());
                    }
                }else {
                    getSv(currentType, textureView);
                }
            }
        }, 300);
    }

    private void refresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(IndividualCallService.this)) {
                ToastUtil.showToast(IndividualCallService.this, "请打开悬浮窗权限，否则私密呼叫和图像功能无法使用！");
            }
        }
        // 如果已经添加了就只更新view
        Log.i("sjl_", "refresh:viewAdded为 "+viewAdded);
        if (viewAdded || dialogAdded) {
            windowManager.updateViewLayout(view, layoutParams1);
            viewAdded = true;
        } else {
            Log.i("sjl_", "refresh:视图添加");
            windowManager.addView(view, layoutParams1);
            viewAdded = true;
        }
        setGroupCallAuthority();//根据权限设置组呼PTT图标和图像推送按钮
    }
    private void hideAllView(){
        layout_call_prompt.setVisibility(View.GONE);
        individual_call_request.setVisibility(View.GONE);
        individual_call_chooice.setVisibility(View.GONE);
        individual_call_speaking.setVisibility(View.GONE);
        individual_call_half_duplex.setVisibility(View.GONE);


        emergency_group_call.setVisibility(View.GONE);
        pop_minimize.setVisibility(View.GONE);

//        layout_volume.setVisibility(View.GONE);
        live_select_member.setVisibility(View.GONE);
        live_edit_theme.setVisibility(View.GONE);
        live_request.setVisibility(View.GONE);
        live_report.setVisibility(View.GONE);
        live.setVisibility(View.GONE);
        popup_mini_live.setVisibility(View.GONE);
        video_dialog.setVisibility(View.GONE);
        ll_live_look_invite_member.setVisibility(View.GONE);
        ll_live_look_hangup.setVisibility(View.GONE);
        usbLive.setVisibility(View.GONE);
    }
    public void removeView() {
        logger.info("sjl_视图移除viewAdded为："+viewAdded);
        if (viewAdded || dialogAdded) {
            logger.info("sjl_视图移除"+"currentType:"+currentType);
            data.clear();
            windowManager.removeView(view);
            viewAdded = false;
            dialogAdded = false;
            MyApplication.instance.isMiniLive = false;
            if(isPulling){
                isPulling = false;
                TerminalFactory.getSDK().getLiveManager().ceaseWatching();
            }
            logger.debug("isPushing:"+isPushing);
            if(isPushing){
                isPushing = false;
                TerminalFactory.getSDK().getLiveManager().ceaseLiving();
            }
            callType = IndividualCallType.IDLE.getCode() ;
            currentType = MyApplication.TYPE.IDLE;
            PromptManager.getInstance().stopRing();
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverRemoveWindowViewHandler.class);
        }
        if(switchCameraViewAdd){
            windowManager.removeView(switchCameraView);
            switchCameraViewAdd = false;
        }
    }
    private void recoverSpeakingPop() {
        if (PhoneAdapter.isF25()) {
            individual_call_speak_view.setVisibility(View.GONE);
            individual_call_half_duplex_view.setVisibility(View.GONE);
        } else {

            if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                individualCallView.stop();
                tv_speaking_prompt.setText("正在通话...");
                tv_speaking_toast.setVisibility(View.GONE);
                individual_call_speak_view.setVisibility(View.VISIBLE);
            }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                ICTV_half_duplex_time_speaking.stop();
                tv_half_duplex_prompt.setText("按住PTT说话");
                individual_call_half_duplex_view.setVisibility(View.VISIBLE);
            }
        }




    }

    /** 广播 */
    protected IBroadcastRecvHandler mBroadcastReceiv;
    /** 广播过滤 */
    protected IntentFilter mReceivFilter;
    /**
     * 注册广播
     */
    public void regBroadcastRecv(String... actions) {
        if (mBroadcastReceiv == null || mReceivFilter == null) {
            mBroadcastReceiv = new IBroadcastRecvHandler(this);
            mReceivFilter = new IntentFilter();
        }
        if (actions != null) {
            for (String act : actions) {
                mReceivFilter.addAction(act);
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiv, mReceivFilter);
    }

    /** 广播回调.空实现 */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            onSafeReceive(intent, action);
        }
    }
    /** 广播回调.空实现 */
    public void onSafeReceive(Intent intent, String action) {
        if (action.equals(KILL_ACT_CALL)){
            //如果弹窗已经添加，显示弹窗内容
            if(dialogAdded){
                showDialogView();
            }else {
                removeView();
            }
            popup_ICTV_speaking_time.stop();
            if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                individualCallView.pause();
            }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                ICTV_half_duplex_time_speaking.pause();
            }

        }
        if (action.equals(SEND_LIVE_THEME)){
            live_theme = intent.getStringExtra("live_theme");
        }
    }
    /** 个呼到来，选择接听*/
    private final class OnTouchListenerImplementationChooiceAccept implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    hideAllView();
                    ll_individual_call_refuse.setClickable(false);
                    if (PhoneAdapter.isF25()) {
                        individual_call_speak_view.setVisibility(View.GONE);
                        individual_call_half_duplex_view.setVisibility(View.GONE);
                    } else {
                        if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                            individual_call_speak_view.setVisibility(View.VISIBLE);
                            tv_waiting.setVisibility(View.GONE);
                            popup_ICTV_speaking_time.setVisibility(View.VISIBLE);
                            tv_member_name_speaking.setText(comingName);
                            tv_member_id_speaking.setText(HandleIdUtil.handleId(callMember.getNo()));
                            individualCallView.stop();
                            individualCallView.start();
                            popup_ICTV_speaking_time.stop();
                            popup_ICTV_speaking_time.start();
                            tv_speaking_prompt.setText("正在通话...");
                            tv_speaking_toast.setVisibility(View.GONE);
                            individual_call_speaking.setVisibility(View.VISIBLE);
                        }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                            individual_call_half_duplex_view.setVisibility(View.INVISIBLE);
                            tv_waiting.setVisibility(View.GONE);
                            popup_ICTV_speaking_time.setVisibility(View.VISIBLE);
                            tv_member_name_half_duplex.setText(comingName);
                            tv_member_id_half_duplex.setText(HandleIdUtil.handleId(callMember.getNo()));
                            ICTV_half_duplex_time_speaking.stop();
                            ICTV_half_duplex_time_speaking.start();
                            popup_ICTV_speaking_time.stop();
                            popup_ICTV_speaking_time.start();
                            tv_half_duplex_prompt.setText("按住PTT说话");
                            individual_call_half_duplex.setVisibility(View.VISIBLE);

                            individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                            individual_call_half_duplex_ptt.setEnabled(true);
                            startAutoHangUpTimer();//半双工选择接听，开始超时检测
                        }
                    }
                    MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(true);
                    PromptManager.getInstance().stopRing();
                    MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                    break;
                case MotionEvent.ACTION_UP:
                    ll_individual_call_refuse.setClickable(true);
                    break;
            }

            return false;
        }



    }

    private void finishTransparentActivity(){
        Intent intent = new Intent("FINISH_TRANSPARENT");
        sendBroadcast(intent);
    }

    /**
     * 说话界面的挂断
     */
    private final class OnClickListenerImplementationSpeakingHangup implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
            finishTransparentActivity();
            popup_ICTV_speaking_time.pause();
            MyApplication.instance.isMiniLive = false;
            if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                individualCallView.pause();
                tv_speaking_prompt.setText("通话结束");
                tv_speaking_toast.setVisibility(View.GONE);
                individual_call_speaking.setVisibility(View.GONE);

            }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                ICTV_half_duplex_time_speaking.pause();
                tv_half_duplex_prompt.setText("通话结束");
                cancelAutoHangUpTimer();
                individual_call_half_duplex.setVisibility(View.GONE);
            }
            hideAllView();
            myHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    ll_individual_call_accept.setClickable(false);

                    //如果弹窗已经添加，显示弹窗内容
                    if(dialogAdded){
                        showDialogView();
                    }else {
                        removeView();
                    }
                }
            },1000);

        }
    }
    /** 个呼到来，选择挂断*/
    private final class OnTouchListenerImplementationChooiceRefuse implements
            View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    finishTransparentActivity();
                    //如果弹窗已经添加，显示弹窗内容
                    if(dialogAdded){
                        showDialogView();
                    }else {
                        removeView();
                    }
                    ll_individual_call_accept.setClickable(false);
                    popup_ICTV_speaking_time.stop();
                    MyTerminalFactory.getSDK().getIndividualCallManager().responseIndividualCall(false);
                    PromptManager.getInstance().stopRing();
                    MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                    break;
                case MotionEvent.ACTION_UP:
                    ll_individual_call_accept.setClickable(true);
                    //抬起
                    break;
            }

            return false;
        }
    }

    /**
     * 主动方请求个呼时，自己挂断
     */
    private final class OnClickListenerImplementationRequestHangup implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Log.e("callservice", "OnClickListenerImplementationRequestHangup取消呼叫");
//            myHandler.removeMessages(CLOSEPRIVATECALL);
            tv_request_prompt.setText("已取消");

            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
                    popup_ICTV_speaking_time.stop();
                    PromptManager.getInstance().stopRing();
                    hideAllView();
                    removeView();
                }
            },1000);

        }
    }


    private final class OnClickListenerCallRequsetWindow implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            status = 1;
            windowManager.updateViewLayout(view,layoutParams);
            logger.info("sjl_layoutParams:"+layoutParams);
            hideAllView();
            pop_minimize.setVisibility(View.VISIBLE);
            tv_waiting.setVisibility(View.VISIBLE);
            popup_ICTV_speaking_time.setVisibility(View.GONE);
            MyApplication.instance.isMiniLive = true;

        }
    }
    //个呼缩小界面
    private final class OnClickListenerCallSpeakingWindow implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            status = 2;
            windowManager.updateViewLayout(view,layoutParams);
            hideAllView();
            pop_minimize.setVisibility(View.VISIBLE);
            tv_waiting.setVisibility(View.GONE);
            popup_ICTV_speaking_time.setVisibility(View.VISIBLE);
            MyApplication.instance.isMiniLive=true;
        }
    }

    /**
     * 关闭当前弹窗
     */
    private final class OnClickListenerCloseDialog implements StackViewAdapter.CloseDialogListener{

        @Override
        public void onCloseDialogClick(int position){
            data.remove(stackViewAdapter.getItem(position));
            stackViewAdapter.remove(position);
            if(data.isEmpty()){
                removeView();
            }
        }
    }

    /**  上报图像或者去看警情  **/
    private final class OnClickListenerGoWatch implements StackViewAdapter.GoWatchListener{
        @Override
        public void onGoWatchClick(final int position){
            //判断是否有接受图像功能权限
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ACCEPT.name())){
                ToastUtil.showToast(getApplicationContext(),"您还没有图像接受权限");
                removeView();
                return;
            }
            if(position > data.size()-1){
                return;
            }
            final TerminalMessage terminalMessage = stackViewAdapter.getItem(position);
            //判断消息类型
            if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){
                MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
                    @Override
                    public void run(){
                        String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                        int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                        String url = "http://"+serverIp+":"+serverPort+"/file/download/isLiving";
                        Map<String,String> paramsMap = new HashMap<>();
                        paramsMap.put("callId",terminalMessage.messageBody.getString(JsonParam.CALLID));
                        paramsMap.put("sign", SignatureUtil.sign(paramsMap));
                        logger.info("查看视频播放是否结束url："+url);
                        String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
                        logger.info("查看视频播放是否结束结果："+result);
                        if(!Util.isEmpty(result)){
                            JSONObject jsonObject = JSONObject.parseObject(result);
                            boolean living = jsonObject.getBoolean("living");
                            Long endChatTime = jsonObject.getLong("endChatTime");
                            if(living){
                                Message msg = Message.obtain();
                                msg.what = WATCH_LIVE;
                                msg.obj = terminalMessage;
                                msg.arg1 = position;
                                myHandler.sendMessage(msg);
                            }else {
                                removeView();
                                Intent intent = new Intent(getApplicationContext(),LiveHistoryActivity.class);
                                intent.putExtra("terminalMessage",terminalMessage);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //                                intent.putExtra("endChatTime",endChatTime);
                                startActivity(intent);
                            }
                        }
                    }
                });

            }else if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
                // TODO: 2018/5/4 去看警情
            }
        }
    }
    private final class OnClickListenerCallCommingWindow implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            status = 3;
            hideAllView();
            showPopMiniView();
            finishTransparentActivity();
        }
    }
    private final class OnClickListenerFullScreen implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            hideAllView();
            if (status == 1){
                windowManager.updateViewLayout(view,layoutParams1);
                individual_call_request.setVisibility(View.VISIBLE);
            }else if (status ==2){
                windowManager.updateViewLayout(view,layoutParams1);
                individual_call_speaking.setVisibility(View.VISIBLE);
            }else if (status == 3){
                windowManager.updateViewLayout(view,layoutParams1);
                individual_call_chooice.setVisibility(View.VISIBLE);
            }
        }
    }

    private int callType ;
    /**
     * 紧急个呼时，被动方强制接听
     */
    private ReceiveNotifyEmergencyIndividualCallHandler receiveNotifyEmergencyIndividualCallHandler = new ReceiveNotifyEmergencyIndividualCallHandler() {
        @Override
        public void handler(final int mainMemberId) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };
    /**
     * 被动方个呼来了，选择接听或挂断
     */
    private ReceiveNotifyIndividualCallIncommingHandler receiveNotifyIndividualCallIncommingHandler = new ReceiveNotifyIndividualCallIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId, int individualCallType) {
            comingName = mainMemberName;
            Log.e("IndividualCallService", "mainMemberId:" + mainMemberId);

            callMember = DataUtil.getMemberByMemberNo(mainMemberId);
            callType =individualCallType;
            logger.info("被动方个呼来了，individualCallType："+individualCallType);

            //隐藏所有软键盘
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(null !=imm &&InputMethodUtil.inputMethodSate(IndividualCallService.this)){
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
            wakeLockComing.acquire(10000);
            logger.info("main点亮屏幕");
//            if (MyApplication.instance.isScreenOff) {//个呼时，点亮锁屏
//                sendBroadcast(new Intent("MainActivityfinish"));
//            }
//            if (isBackground(getApplicationContext())) {//程序处于后台
////                sendBroadcast(new Intent("MainActivityfinish"));
//                logger.info("main程序拿到前台");
//
//            }

            //判断是否锁屏
            KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
            if(flag){
//                //无屏保界面
                if(MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0) != 1){
                    Intent intent = new Intent(IndividualCallService.this,TransparentActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }
            InputMethodUtil.hideInputMethod(getApplicationContext(),et_search_allcontacts);
            //延时显示window，防止TransparentActivity还没创建完成
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    individualCallBinder.showPop();
                }
            },200);

        }
    };
    /**
     * 被动方个呼答复超时
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = new ReceiveAnswerIndividualCallTimeoutHandler() {

        @Override
        public void handler() {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    individualCallBinder.individualCallStopped();
                }
            });

        }
    };
    /**
     * 被动方通知个呼停止，界面---------静默状态
     */
    private ReceiveNotifyIndividualCallStoppedHandler receiveNotifyIndividualCallStoppedHandler = new ReceiveNotifyIndividualCallStoppedHandler() {
        @Override
        public void handler(final int methodResult, String resultDesc) {
            stopMethodResult = methodResult;
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    popup_ICTV_speaking_time.pause();
                    if(callType==IndividualCallType.FULL_DUPLEX.getCode()){
                        individualCallView.pause();
                        tv_speaking_prompt.setText("通话结束");
                        tv_speaking_toast.setVisibility(View.VISIBLE);
                        tv_speaking_toast.setText("对方已挂断，通话结束");
                    }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                        ICTV_half_duplex_time_speaking.pause();
                        tv_half_duplex_prompt.setText("通话结束");
                    }

                    if (SignalServerErrorCode.getInstanceByCode(stopMethodResult) != null) {
                        tv_chooice_prompt.setText(SignalServerErrorCode.getInstanceByCode(stopMethodResult).getErrorDiscribe());
                    } else {
                        tv_chooice_prompt.setVisibility(View.VISIBLE);
                        tv_chooice_prompt.setText("对方已取消");
                        popup_ICTV_speaking_time.stop();
                    }
                    //如果弹窗已经添加，显示弹窗内容
                    if(dialogAdded){
                        showDialogView();
                    }else {
                        removeView();
                    }
                    popup_ICTV_speaking_time.stop();
                    finishTransparentActivity();
                    MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
                    ll_individual_call_accept.setEnabled(false);
                    PromptManager.getInstance().IndividualHangUpRing();
                    PromptManager.getInstance().delayedStopRing();
                    cancelAutoHangUpTimer();

                }
            });
        }
    };
    /**
     * 主动方请求个呼开始
     */
    private ReceiveResponseStartIndividualCallHandler receiveResponseStartIndividualCallHandler = new ReceiveResponseStartIndividualCallHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final int individualCallType) {
            logger.info("ReceiveResponseStartIndividualCallHandler===="+"resultCode:"+resultCode+"=====resultDesc:"+resultDesc);
            callType = individualCallType;
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {//对方接听
                logger.info("对方接受了你的个呼:" + resultCode + resultDesc+"callType;"+callType);
                //打开个呼通话界面，同时关闭个呼呼叫界面
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        individualCallBinder.callAnswer();
                        wakeLockComing.acquire(10000);
                    }
                });
            } else if (resultCode == SignalServerErrorCode.CALLED_MEMBER_OFFLINE. getErrorCode()) { //您已经发起个呼了
                SignalServerErrorCode.getInstanceByCode(resultCode);
                callResultDesc = "对方不在线";
                logger.info("ReceiveResponseStartIndividualCallHandler:"+resultCode+"="+ callResultDesc);
//                if (SignalServerErrorCode.getInstanceByCode(resultCode) != null) {
//                    individualCallBinder.busyWithEachOther();
//                }
                individualCallBinder.busyWithEachOther();
                MyApplication.instance.isCallState = false;

            } else if (resultCode == SignalServerErrorCode.SLAVE_BUSY.getErrorCode() ){
                callResultDesc = "对方繁忙";
                individualCallBinder.busyWithEachOther();
            }else if(resultCode == SignalServerErrorCode.MAIN_MEMBER_IN_INDIVIDUAL_CALL.getErrorCode()){
                callResultDesc = "通话结束";
                individualCallBinder.busyWithEachOther();
            }else if (resultCode == TerminalErrorCode.INDIVIDUAL_CALL_NO_RESPONSE.getErrorCode()){
                callResultDesc = "对方无应答";
                individualCallBinder.busyWithEachOther();
            }else if (resultCode == SignalServerErrorCode.TERMINAL_OFFLINE_LOGOUT.getErrorCode()){
                callResultDesc = "对方不在线";
                individualCallBinder.callRejection();
            }else {//对方拒绝
                callResultDesc = resultDesc;
                individualCallBinder.callRejection();
            }
        }
    };


    private ReceiveUpdateConfigHandler mReceiveUpdateConfigHandler= new ReceiveUpdateConfigHandler(){

        @Override
        public void handler() {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    setGroupCallAuthority();
                }
            });
        }
    };



    boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                logger.info("此app =" + appProcess.importance + ",context.getClass().getName()=" + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    logger.info("处于后台" + appProcess.processName);
                    return true;
                } else {
                    logger.info("处于前台" + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }
    /**
     * 界面个呼请求的监听
     */
    private ReceiveCurrentGroupIndividualCallHandler receiveCurrentGroupIndividualCallHandler = new ReceiveCurrentGroupIndividualCallHandler() {
        @Override
        public void handler(final Member member) {
            MyApplication.instance.calleeMember = member;
            MyApplication.instance.isInitiativeCall = true;
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    logger.info("当前呼叫对象:" + member);
                    individualCallBinder.showIndividualCallRequest(member);
                }
            });
        }
    };

    //GH880手机PTT按钮事件
    public class GotaKeHandler extends IGotaKeyHandler.Stub{

        @Override
        public void onPTTKeyDown() throws RemoteException {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        pttDownDoThing();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        @Override
        public void onPTTKeyUp() throws RemoteException {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        pttUpDoThing();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onSOSKeyDown() throws RemoteException {

        }

        @Override
        public void onSOSKeyUp() throws RemoteException {

        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        logger.info("sjl_停止个呼服务");
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerIndividualCallTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyIndividualCallStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCurrentGroupIndividualCallHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUVCCameraConnectChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseMyselfLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingStoppedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetVideoPushUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberJoinOrExitHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetRtspStreamUrlHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberNotLivingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetAboutLiveMemberListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNobodyRequestVideoLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyInviteToWatchHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(individualCallPttStatusHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);

        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverActivePushVideoHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverRequestVideoHandler);

        try{
            keyMointor.setHandler(gotaKeyHandler);
        }catch (Exception e){

        }

        popup_ICTV_speaking_time.stop();
        individualCallView.stop();
        ICTV_half_duplex_time_speaking.stop();
        removeView();

        stopLiveService();
        return super.onUnbind(intent);
    }


    private final class PttOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!MyApplication.instance.folatWindowPress && !MyApplication.instance.volumePress) {
                        pttDownDoThing();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getX() + v.getWidth() / 4 < 0 || event.getX() - v.getWidth() * 1.25 > 0 ||
                            event.getY() + v.getHeight() / 8 < 0 || event.getY() - v.getHeight() * 1.125 > 0) {
                        if (MyApplication.instance.isPttPress) {
                            pttUpDoThing();
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (MyApplication.instance.isPttPress) {
                        pttUpDoThing();
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    }

    //收到没人请求我开视频的消息，关闭界面和响铃
    private ReceiveNobodyRequestVideoLiveHandler receiveNobodyRequestVideoLiveHandler = new ReceiveNobodyRequestVideoLiveHandler() {
        @Override
        public void handler() {
            showToast("对方已取消");
            finishVideoLive();
        }
    };

    private ReceiveNotifyInviteToWatchHandler receiveNotifyInviteToWatchHandler = new ReceiveNotifyInviteToWatchHandler() {
        @Override
        public void handler(final PTTProtolbuf.NotifyDataMessage message) {

        }
    };
    //ptt个呼等待
    private IndividualCallPttStatusHandler individualCallPttStatusHandler = new IndividualCallPttStatusHandler() {
        @Override
        public void handler(final boolean pttIsDown, final int outerMemberId) {
            logger.info("PTT个呼等待"+"pttIsDown:"+pttIsDown);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (outerMemberId != TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                        if (pttIsDown){
                            if (pop_minimize.getVisibility() == View.VISIBLE){
                                layout_call_prompt.setVisibility(View.GONE);
                            }else {
                                layout_call_prompt.setVisibility(View.VISIBLE);
                            }
                            tv_half_duplex_prompt.setText("对方正在说话");
                            tv_half_duplex_prompt.setTextColor(Color.YELLOW);
                            individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.ptt_individual_call_wait);
                            individual_call_half_duplex_ptt.setEnabled(false);
                            ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().playPromptCalled();
                            cancelAutoHangUpTimer();//对方按下开始说话，取消时间检测
                        }else{
                            layout_call_prompt.setVisibility(View.GONE);
                            tv_half_duplex_prompt.setText("按住PTT说话");
                            tv_half_duplex_prompt.setTextColor(Color.WHITE);
                            individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.ptt_individual_call);
                            individual_call_half_duplex_ptt.setEnabled(true);
                            startAutoHangUpTimer();//对方抬起，启动时间检测机制
                        }
                    }
                }
            });
        }
    };
    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(final int memberId, final String memberName, final int groupId,
                            String version, CallMode currentCallMode) {
            //如果在半双工个呼中来组呼，就是对方在说话
            Log.e("IndividualCallService", "收到组呼：callType:" + callType);
            if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                myHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        logger.info("半双工个呼时来组呼，对方正在说话");
                        individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                        tv_half_duplex_prompt.setText("对方正在说话");
                        tv_half_duplex_prompt.setTextColor(Color.YELLOW);
                        individual_call_half_duplex_ptt.setEnabled(false);
                        cancelAutoHangUpTimer();//对方按下开始说话，取消时间检测
                    }
                });
            }else {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                    ToastUtil.showToast(getApplicationContext(),"没有组呼听的权限");
                }else{
                    PromptManager.getInstance().groupCallCommingRing();
                    logger.info("组呼来了");
                    groupSpeak = groupId;
                    speakId=memberId;
                    myHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                            if (groupId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                                //                        groupScanId = groupId;
                                //                        setCurrentGroupScanView(groupId);
                                logger.info("扫描组组呼来了");
                                if(viewAdded){
                                    btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                                    ll_live_group_call.setVisibility(View.VISIBLE);
                                    tv_live_groupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                                    tv_live_speakingName.setText(memberName);
                                    tv_live_speakingId.setText(HandleIdUtil.handleId(memberId));

                                    ll_uvc_speak_state.setVisibility(View.VISIBLE);
                                    tv_uvc_live_speakingName.setText(memberName);
                                    tv_uvc_live_groupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                                    tv_uvc_live_speakingId.setText(HandleIdUtil.handleId(memberId));
                                }
                            }
                            //是当前组的组呼,且扫描组有人说话，变文件夹和组名字
                            if (groupId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) && MyApplication.instance.getGroupListenenState() == LISTENING) {
                                //                        setCurrentGroupScanView(groupId);
                                logger.info("当前组组呼来了");
                                if(viewAdded){
                                    //                                TextViewCompat.setTextAppearance(btn_live_look_ptt,R.style.);
                                    btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                                    ll_live_group_call.setVisibility(View.VISIBLE);
                                    tv_live_groupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                                    tv_live_speakingName.setText(memberName);
                                    tv_live_speakingId.setText(HandleIdUtil.handleId(memberId));

                                    ll_uvc_speak_state.setVisibility(View.VISIBLE);
                                    tv_uvc_live_speakingName.setText(memberName);
                                    tv_uvc_live_groupName.setText(DataUtil.getGroupByGroupNo(groupId).name);
                                    tv_uvc_live_speakingId.setText(HandleIdUtil.handleId(memberId));
                                }

                            }

                            MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
                            change2Listening();
                        }
                    });
                }
            }

        }
    };
    private void change2Listening() {
        String speakMemberName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
        if (!TextUtils.isEmpty(speakMemberName) && viewAdded) {
            //设置说话人名字,在组呼来的handler中设置
            ll_live_group_call.setVisibility(View.VISIBLE);
            tv_live_groupName.setText(DataUtil.getGroupByGroupNo(groupSpeak).name);
            tv_live_speakingName.setText(speakMemberName);
            tv_live_speakingId.setText(HandleIdUtil.handleId(speakId));
        }
    }
    private ReceivePTTUpHandler receivePTTUpHandler = new ReceivePTTUpHandler() {
        @Override
        public void handler() {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
                        return;
                    }
                    MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                    btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                }
            });
        }
    };
    private ReceivePTTDownHandler receivePTTDownHandler = new ReceivePTTDownHandler() {
        @Override
        public void handler(int requestGroupCall) {
            if (requestGroupCall == BaseCommonCode.SUCCESS_CODE) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!CheckMyPermission.selfPermissionGranted(IndividualCallService.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
//                            CheckMyPermission.permissionPrompt(IndividualCallService.this, Manifest.permission.RECORD_AUDIO);
                            return;
                        }
                        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
                            return;
                        }
                        if (MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.PLAYING) {
                            MyTerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                        }
                        btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
                    }
                });
            } else if (requestGroupCall == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
                btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
            } else {//组呼失败的提示
                ToastUtil.groupCallFailToast(IndividualCallService.this, requestGroupCall);
            }
        }
    };

    String dateString;


    /**
     * 获取到可推送成员列表
     */
    private ReceiveGetAboutLiveMemberListHandler receiveGetAboutLiveMemberListHandler = new ReceiveGetAboutLiveMemberListHandler() {
        @Override
        public void handler(final List<Member> liveMemberList) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    logger.info("推送列表成员-----memberList" + liveMemberList);
                    memberList.clear();
                    memberList.addAll(liveMemberList);
                    //过滤已经在观看的人

                    Iterator<Member> iterator = memberList.iterator();
                    while(iterator.hasNext()){
                        Member member = iterator.next();
                        for(int j = 0; j < watchLiveList.size(); j++){
                            if(member.getId() == watchLiveList.get(j).getId()){
                                iterator.remove();
                                break;
                            }
                        }
                        //过滤正在上报的人
                        if(liveMember !=null && member.getId()== liveMember.getId()){
                            iterator.remove();
                        }
                    }
                    liveContactsAdapter.refreshLiveContactsAdapter(-1, memberList);
                }
            });
        }
    };

    /**
     * 组成员遥毙消息
     */
    private ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = new ReceiveNotifyMemberKilledHandler() {
        @Override
        public void handler(boolean forbid) {
            logger.info("收到遥毙，此时forbid" + forbid);
            if (forbid) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        PromptManager.getInstance().stopRing();
                        finishVideoLive();
                    }
                });
            }
        }
    };

    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc, boolean isRegisted) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
//                stopPull();
//                stopPush();
                finishVideoLive();
            }
        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {

        @Override
        public void handler(final boolean connected) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    logger.info("网络：" + connected);
                    if (!connected) {
                        MyTerminalFactory.getSDK().putParam(Params.NET_OFFLINE, true);
                        if (MyApplication.instance.getIndividualState() != IndividualCallState.IDLE) {
                            removeView();
                            individualCallView.pause();
                            ICTV_half_duplex_time_speaking.pause();
                            PromptManager.getInstance().stopRing();
                            popup_ICTV_speaking_time.stop();
                            cancelAutoHangUpTimer();
                        }
                        showToast("网络连接已断开");
                        memberEnterList.clear();
//                        finishVideoLive();

                    } else {
                        MyTerminalFactory.getSDK().putParam(Params.NET_OFFLINE, false);
                        if (svLive.getSurfaceTexture()!=null){
                            if( currentType == MyApplication.TYPE.PULL){
                                startPull(svLive.getSurfaceTexture());
                            }else if(currentType == MyApplication.TYPE.PUSH ){
                                pushStream(svLive.getSurfaceTexture());
                            }else if(currentType == MyApplication.TYPE.UVCPUSH){
                                pushUVCStream(sv_uvc_live.getSurfaceTexture());
                            }
                        }
                    }
                }
            });
        }
    };

    /**
     * 去观看时，发现没有在直播，关闭界面吧
     */
    private ReceiveMemberNotLivingHandler receiveMemberNotLivingHandler = new ReceiveMemberNotLivingHandler() {
        @Override
        public void handler(Long callId) {
            sendMessage("上报已结束");
            TerminalFactory.getSDK().getLiveManager().ceaseWatching();
            finishVideoLive();
        }
    };
    /**
     * 收到别人请求我开启直播的通知
     **/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //如果正在拍摄视频时
                    if (isBackground(getApplicationContext())) {//程序处于后台
                        //                        sendBroadcast(new Intent("MainActivityfinish"));
                        logger.info("main程序拿到前台");
                        //无屏保界面
                        Intent transparentIntent = new Intent(IndividualCallService.this,TransparentActivity.class);
                        transparentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(transparentIntent);
                    }

                    activePush = false;
                    refresh();
                    hideAllView();
                    live_report.setVisibility(View.VISIBLE);
                    tv_live_report_name.setText(HandleIdUtil.handleName(mainMemberName));
                    tv_live_report_id.setText(HandleIdUtil.handleId(mainMemberId));
                    ivAvaTarReport.setImageResource(R.drawable.user_photo);

                    btn_live_selectmember_start.setText("开始");
                    btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
                    tv_checktext.setText("");
                    selectItem.clear();
                    hideKey();
                    wakeLockComing.acquire();
                    logger.info("main点亮屏幕");


                    PromptManager.getInstance().VideoLiveInCommimgRing();
                }
            });
        }
    };

    private int pullcount;
    /**
     * 获取到rtsp地址，开始播放视频
     */
    private ReceiveGetRtspStreamUrlHandler receiveGetRtspStreamUrlHandler = new ReceiveGetRtspStreamUrlHandler() {
        @Override
        public void handler(final String rtspUrl, final Member liveMember,long callId) {
            myHandler.post(new Runnable(){
                @Override
                public void run(){
                    if (Util.isEmpty(rtspUrl)) {
                        showToast("没有获取到数据流地址或图像上传已结束");
                        finishVideoLive();
                    }else {
                        logger.info("rtspUrl ----> " + rtspUrl);
                        refresh();
                        hideAllView();
                        live.setVisibility(View.VISIBLE);

                        IndividualCallService.this.liveMember = liveMember;
                        if (liveMember != null) {
                            pullView(liveMember,live_theme);
                        }
                        currentType = MyApplication.TYPE.PULL;
                        streamMediaServerUrl = rtspUrl;

                        if (svLive.getSurfaceTexture() != null){
                            startPull(svLive.getSurfaceTexture());
                        }

                        PromptManager.getInstance().stopRing();
                    }
                }
            });
        }
    };


    /**
     * 自己发起直播的响应
     **/
    private ReceiveGetVideoPushUrlHandler receiveGetVideoPushUrlHandler = new ReceiveGetVideoPushUrlHandler() {
        @Override
        public void handler(final String streamMediaServerIp, final int streamMediaServerPort, final long callId) {
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logger.info("自己发起直播，服务端返回的ip：" + streamMediaServerIp + "端口：" + streamMediaServerPort);
                    String ip = streamMediaServerIp;
                    String port = streamMediaServerPort + "";
                    String id = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) + "_" + callId;
                    if(currentType == MyApplication.TYPE.PUSH){
                        startPush(ip,port,id);
                    }else if(currentType == MyApplication.TYPE.UVCPUSH){
                        startUVCPush(ip,port,id);
                    }


                }
            },1000);
        }
    };

    private void startUVCPush(String ip, String port, String id){
        if(uvcMediaStream == null){
            if(null != sv_uvc_live.getSurfaceTexture()){
                pushUVCStream(sv_uvc_live.getSurfaceTexture());
            }else {
                showToast("图像上传失败");
                finishVideoLive();
            }
        }

        uvcMediaStream.startStream(ip, port, id, new InitCallback() {
            @Override
            public void onCallback(int code) {
                switch (code) {
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                        logger.info("无效Key");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                        logger.info("激活成功");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                        logger.info("连接中");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                        logger.info("连接成功");
                        pushcount = 0;
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                        logger.info("连接失败");
                        if(pushcount<=10){
                            pushcount++;
                        }else {
                            finishVideoLive();
                        }

                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                        logger.info("连接异常中断");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                        logger.info("推流中...");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                        logger.info("断开连接");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                        logger.info("平台不匹配");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                        logger.info("断授权使用商不匹配");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                        logger.info("进程名称长度不匹配");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void pushUVCStream(SurfaceTexture surfaceTexture){
        if (mService != null) {
            UVCMediaStream ms = mService.getUvcMediaStream();
            if (ms != null) {    // switch from background to front
                ms.stopPreview();
                mService.inActivePreview();
//                ms.destroyCamera();
                ms.setSurfaceTexture(surfaceTexture);
//                ms.createCamera();
                ms.startPreview();
                uvcMediaStream = ms;

            } else {
                ms = new UVCMediaStream(getApplicationContext(), surfaceTexture);
                uvcMediaStream = ms;
                startUVCCamera();
                mService.setUVCMediaStream(ms);
            }
            isPushing = true;
        } else {
            showToast("服务启动失败");
            finishVideoLive();
        }
    }

    private void startPush(String ip,String port,String id){
        logger.info("sjl_:"+mMediaStream+"----"+svLive.getSurfaceTexture());
        if (mMediaStream == null) {
            if (svLive.getSurfaceTexture() != null) {
                pushStream(svLive.getSurfaceTexture());
            } else {
                showToast("图像上传失败");
                finishVideoLive();
                return;
            }
        }

        mMediaStream.startStream(ip, port, id, new InitCallback() {
            @Override
            public void onCallback(int code) {
                switch (code) {
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                        logger.info("无效Key");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                        logger.info("激活成功");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                        logger.info("连接中");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                        logger.info("连接成功");
                        pushcount = 0;
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                        logger.info("连接失败");
                        if(pushcount<=10){
                            pushcount++;
                        }else {
                            finishVideoLive();
                        }

                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                        logger.info("连接异常中断");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                        logger.info("推流中...");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                        logger.info("断开连接");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                        logger.info("平台不匹配");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                        logger.info("断授权使用商不匹配");
                        break;
                    case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                        logger.info("进程名称长度不匹配");
                        break;
                    default:
                        break;
                }
            }
        });
        String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
        logger.info("推送地址："+url);
    }

    /**
     * 自己发起直播的响应
     **/
    private ReceiveResponseMyselfLiveHandler receiveReaponseMyselfLiveHandler = new ReceiveResponseMyselfLiveHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc) {
            myHandler.post(new Runnable(){
                @Override
                public void run(){
                    if(resultCode == 0){
                        logger.info("自己发起直播成功,要推送的列表：" + pushMemberList);
                        if (pushMemberList != null) {
                            MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(pushMemberList,MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
                        }
                    }else {
                        showToast(resultDesc);
                        finishVideoLive();
                    }
                }
            });
        }
    };

    private ReceiveUVCCameraConnectChangeHandler receiveUVCCameraConnectChangeHandler = new ReceiveUVCCameraConnectChangeHandler(){
        @Override
        public void handle(boolean connected){
            MyApplication.instance.usbAttached = connected;
            if(!connected){
                if(currentType == MyApplication.TYPE.UVCPUSH){
                    showToast("上报已停止");
                    finishVideoLive();
                }
            }
        }
    };

    private void showToast(final String resultDesc) {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(IndividualCallService.this, resultDesc);
            }
        });
    }

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = new ReceiveResponseStartLiveHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc) {
            if(liveContactsAdapter != null){
                liveContactsAdapter.notifyLiveMember();
            }
            showToast(resultDesc);
            finishVideoLive();
        }
    };

    private void finishVideoLive() {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                pullcount = 0;
                finishTransparentActivity();
                recoverStateMachine();//恢复直播状态
                PromptManager.getInstance().stopRing();//停止响铃

                if (currentType == MyApplication.TYPE.PULL) {
                    stopPull();
                } else if (currentType == MyApplication.TYPE.PUSH) {
                    stopPush();
                }else if(currentType == MyApplication.TYPE.UVCPUSH){
                    stopUVCPush();
                }
                live_theme = null;
                liveMember = null;
                watchLiveList.clear();
                memberEnterList.clear();
                memberList.clear();

                currentType = MyApplication.TYPE.IDLE;
                hideAllView();
                //如果弹窗已经添加，显示弹窗内容
                if(switchCameraViewAdd){
                    windowManager.removeView(switchCameraView);
                }
                if(dialogAdded){
                    showDialogView();
                }else {
                    removeView();
                }
            }
        });

    }

    private void stopUVCPush(){
        myHandler.removeMessages(CURRENTTIME);
        isPushing = false;
        boolean isStreaming = uvcMediaStream != null && uvcMediaStream.isStreaming();
        if (uvcMediaStream != null) {
//            uvcMediaStream.stopPreview();
        } else {
            return;
        }
        logger.info("isFinishing() = " + "isFinishing()" + "    isStreaming = " + isStreaming);
        if (uvcMediaStream != null) {
            uvcMediaStream.stopStream();
            uvcMediaStream.stopPreview();
            uvcMediaStream.release();
            uvcMediaStream = null;
            mService.setUVCMediaStream(null);
            //            stopService(new Intent(VideoLiveActivity.this, BackgroundCameraService.class));
            logger.info("---->>>>页面关闭，停止推送视频");
        } else {
            if (isStreaming) {
                mService.activeUVCPreview();
                logger.info("---->>>>退到后台，继续推送视频");
            }
        }
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    /**
     * 观看成员的进入和退出
     **/
    @SuppressLint("SimpleDateFormat")
    private ReceiveMemberJoinOrExitHandler receiveMemberJoinOrExitHandler = new ReceiveMemberJoinOrExitHandler() {
        @Override
        public void handler(final String memberName, final int memberId, final boolean joinOrExit) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("IndividualCallService", memberName+",memberId:"+memberId);
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                    Date currentTime = new Date();
                    String enterTime = formatter.format(currentTime);
                    VideoMember videoMember = new VideoMember(memberId, memberName, enterTime, joinOrExit);
                    if (joinOrExit) {//进入直播间
                        watchLiveList.add(videoMember);
                    } else {//退出直播间
                        int position = -1;
                        for (int i = 0; i < watchLiveList.size(); i++) {
                            if (watchLiveList.get(i).getId() == memberId) {
                                position = i;
                            }
                        }
                        if (position != -1) {
                            watchLiveList.remove(position);
                        }
                    }
                    memberEnterList.add(videoMember);
                    lv_uvc_live_member_info.setAdapter(memberEnterAdapter);
                    lv_live_member_info.setAdapter(memberEnterAdapter);
                    memberEnterAdapter.notifyDataSetChanged();
                    if (memberEnterList.size() > 0) {
                        lv_live_member_info.smoothScrollToPosition(memberEnterList.size() - 1);
                    }
                    watchMemberAdapter.refreshWatchMemberAdapter();
                }
            });

        }
    };

    /**
     * 通知直播停止 通知界面关闭视频页
     **/
    private ReceiveNotifyLivingStoppedHandler receiveNotifyLivingStoppedHandler = new ReceiveNotifyLivingStoppedHandler() {
        @Override
        public void handler(int methodResult, String resultDesc) {
            showToast("上报已结束");
            finishVideoLive();
        }
    };

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = new ReceiveAnswerLiveTimeoutHandler() {
        @Override
        public void handler() {
            logger.info("sjl_推流还是拉流"+currentType);
            if (currentType == MyApplication.TYPE.PULL){
                showToast("对方无应答");
            }else if (currentType == MyApplication.TYPE.PUSH ||
                    currentType == MyApplication.TYPE.UVCPUSH){
                showToast("对方已取消");
            }
            if(liveContactsAdapter != null){
                liveContactsAdapter.notifyLiveMember();
            }
            finishVideoLive();
        }
    };
    private Member liveMember;


    /**
     * 编辑主题按钮点击事件
     **/
    private final class EditThemeOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            live_edit_theme.setVisibility(View.VISIBLE);
            if (android.text.TextUtils.isEmpty(et_live_edit_import_theme.getText().toString())){
                et_live_edit_import_theme.setText(String.format(getString(R.string.current_push_member),TerminalFactory.getSDK().getParam(Params.MEMBER_NAME,"")));
            }else {
                et_live_edit_import_theme.setText(theme);

            }
            et_live_edit_import_theme.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() > 29) { //判断EditText中输入的字符数是不是已经大于6
//                        viewHolder.et_live_edit_import_theme.setText(s.toString().substring(0,30)); //设置EditText只显示前面6位字符
//                        viewHolder.et_live_edit_import_theme.setSelection(30);//让光标移至末端
                        ToastUtil.showToast(IndividualCallService.this, "输入字数已达上限");
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }


    /**
     * 编辑主题界面返回按钮
     **/
    private final class EditThemeReturnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    live_edit_theme.setVisibility(View.GONE);
                    InputMethodUtil.hideInputMethod(IndividualCallService.this, et_live_edit_import_theme);
                }
            });
        }
    }

    /**
     * 编辑主题界面确定按钮
     **/
    private final class EditThemeConfirmOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    theme = et_live_edit_import_theme.getText().toString().trim();
                    if (!TextUtils.isEmpty(theme)) {
                        live_edit_theme.setVisibility(View.GONE);
                        tv_live_selectmember_theme.setText(theme);
                        InputMethodUtil.hideInputMethod(IndividualCallService.this, et_live_edit_import_theme);
                    } else {
                        showToast("输入主题不能为空");
                    }
                }
            });

        }
    }

    /**
     * 选择成员界面确定按钮
     **/
    private final class SelectMemberOKOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            pushMemberList = liveContactsAdapter.getPushMemberList();

            if(liveMember ==null){
                liveMember=liveContactsAdapter.getLiveMember();
            }

            if(currentType == MyApplication.TYPE.PULL && liveMember == null){
                ToastUtil.showToast(MyApplication.instance,"没有选择上报的人");
                return;
            }

            tv_checktext.setText("");
            et_search_allcontacts.setText("");
            selectItem.clear();
            total = 0;
            InputMethodUtil.hideInputMethod(IndividualCallService.this, et_search_allcontacts);


            logger.error(pushMemberList+"=====ok======="+liveMember+"======"+currentType);

            //如果是上报类型 1、当前没有上报，请求我自己开启上报  2、当前在上报，通知别人来观看
            if(currentType == MyApplication.TYPE.PUSH
                    ||currentType == MyApplication.TYPE.UVCPUSH){
                if(isPushing){
                    inviteToWatchLive();
                }else {
                    if(MyApplication.instance.usbAttached){
                        showSwitchCameraView();
                    }else {
                        requestStartLive();
                    }
                }
            }
            //如果是观看类型 1、当前不是正在观看，请求别人开启直播 2、当前正在观看，邀请别人来观看
            if(currentType == MyApplication.TYPE.PULL){
                if(isPulling){
                    inviteToWatchLive();
                }else {
                    requestOtherStartLive();
                }
            }

        }
    }

    /**
     * 请求别人开启直播
     */
    private void requestOtherStartLive(){
        if (liveMember != null) {
            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(liveMember.id,"");
            logger.error("请求图像：requestCode="+requestCode);
            if (requestCode == BaseCommonCode.SUCCESS_CODE){
                refresh();
                hideAllView();
                live_request.setVisibility(View.VISIBLE);
                //开始响铃
                PromptManager.getInstance().IndividualCallRequestRing();
                tv_live_request_name.setText(HandleIdUtil.handleName(liveMember.getName()));
                tv_live_request_id.setText(HandleIdUtil.handleId(liveMember.id));
                PhotoUtils.loadNetBitmap(getApplicationContext(),liveMember.avatarUrl,ivAvaTarRequest, R.drawable.user_photo);
            }else {
                ToastUtil.livingFailToast(IndividualCallService.this, requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
            }
        } else {
            showToast("请选择请求图像人");
        }
        selectItem.clear();
    }

    /**
     * 请求自己开始上报
     */
    private void requestStartLive(){
        theme = tv_live_selectmember_theme.getText().toString().trim();
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive(theme,"bbbb");
        logger.error("上报图像：requestCode=" + requestCode);
        if (requestCode == BaseCommonCode.SUCCESS_CODE) {
            refresh();
            hideAllView();
            if(currentType == MyApplication.TYPE.PUSH){
                live.setVisibility(View.VISIBLE);
                pushView(theme);
            }else if(currentType == MyApplication.TYPE.UVCPUSH){
                usbLive.setVisibility(View.VISIBLE);
                uvcPushView(theme);
            }
        }else {
            ToastUtil.livingFailToast(IndividualCallService.this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
        }
    }

    /**
     * 邀请别人来观看视频上报
     */
    private void inviteToWatchLive(){
        logger.info("通知别人来观看的列表：" + pushMemberList);
        refresh();
        hideAllView();

        if(currentType == MyApplication.TYPE.PUSH||currentType == MyApplication.TYPE.PULL){
            live.setVisibility(View.VISIBLE);
        }else if(currentType == MyApplication.TYPE.UVCPUSH){
            usbLive.setVisibility(View.VISIBLE);
        }
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
            ToastUtil.showToast(MyApplication.instance,"没有图像推送权限");
            return ;
        }
        if (pushMemberList != null && !pushMemberList.isEmpty()) {
            if(liveMember == null){
                liveMember = DataUtil.getMemberByMemberNo(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
            }
            MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(pushMemberList,liveMember.id);
        }
    }

    /**
     * 选择成员界面返回按钮
     **/
    private final class SelectMemberReturnOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            hideKey();
            selectItem.clear();
            tv_checktext.setText("");
            et_search_allcontacts.setText("");
            total = 0;
            btn_live_selectmember_start.setText("开始");
            btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
            if (isPushing||isPulling) {
                refresh();
                hideAllView();
                if(currentType == MyApplication.TYPE.UVCPUSH){
                    usbLive.setVisibility(View.VISIBLE);
                }else {
                    live.setVisibility(View.VISIBLE);
                }
            } else {
                finishVideoLive();
            }
            InputMethodUtil.hideInputMethod(getApplicationContext(),et_search_allcontacts);
        }
    }

    /**
     * 主动视频请求界面挂断
     **/
    private final class RequestOtherHangUpOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            MyTerminalFactory.getSDK().getLiveManager().stopRequestMemberLive(liveMember.id);
            PromptManager.getInstance().stopRing();
            hideAllView();
            if(liveContactsAdapter != null){
                liveContactsAdapter.notifyLiveMember();
            }
            showToast("已取消");
        }
    }

    private final class OtherReportHangUpOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    showToast("已拒绝");
                    MyTerminalFactory.getSDK().getLiveManager().responseLiving(false);
                    PromptManager.getInstance().stopRing();
                    MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
                    hideAllView();
                    finishVideoLive();
                }
            });

        }
    }

    private final class OtherReportAgreeOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(MyApplication.instance.usbAttached){
                showSwitchCameraView();
            }else {
                currentType = MyApplication.TYPE.PUSH;
                MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);

                refresh();
                hideAllView();
                live.setVisibility(View.VISIBLE);
                pushView("");

                PromptManager.getInstance().stopRing();
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
            }
        }
    }

    private void showSwitchCameraView(){
        if(!switchCameraViewAdd){
            windowManager.addView(switchCameraView,layoutParams1);
            switchCameraViewAdd = true;
        }
    }

    /**
     * 转换摄像头的按钮的点击事件
     **/
    private final class ChangeCameraOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            logger.info("开始转换摄像头");
            mMediaStream.setDgree(getDgree());
            mMediaStream.switchCamera();
        }
    }

    private final class LiveMemberListTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.performClick();
                    myHandler.removeMessages(HIDELIVINGVIEW);
                    break;
                case MotionEvent.ACTION_UP:
                    Message message = Message.obtain();
                    message.what = HIDELIVINGVIEW;
                    myHandler.sendMessageDelayed(message,5000);
                    break;
            }
            return false;
        }
    }
    /**
     * 成员进入ListView
     **/
    private final class MemberListViewOnClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    }

    //观看直播时邀请按钮
    private final class AddLookMemberOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            logger.info("sjl_观看方点击了邀请成员");
            hideAllView();
            total = 0;
            live_select_member.setVisibility(View.VISIBLE);
//            btn_live_selectmember_start.setText("开始(" + total + ")");
            btn_live_selectmember_start.setText("开始");
            btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
            //adapter中走上报的逻辑
            inviteSelectMember();
        }
    }

    /**停止直播*/
    private final class LiveHangUpOnClikListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showToast("上报已结束");
            finishVideoLive();
        }
    }
    /**结束观看*/
    private final class LiveHangUpLookOnClikListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            showToast("观看已结束");
            finishVideoLive();
        }
    }

    /**个人信息*/
    private final class LiveToMemberInfoOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            //暂不实现
        }
    }

    private final class OnClickListenerAutoFocus implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                if(currentType == MyApplication.TYPE.PUSH){
                    mMediaStream.getCamera().autoFocus(null);//屏幕聚焦
                    livingViewHideOrShow(true);
                }else if(currentType == MyApplication.TYPE.UVCPUSH){
                    myHandler.removeMessages(HIDELIVINGVIEW);
                    showUVCLiveView();
                    myHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW,5000);
                }else if(currentType == MyApplication.TYPE.PULL){
                    livingViewHideOrShow(false);
                }
            } catch (Exception e) {

            }
        }
    }

    /**
     * 选择成员界面，搜索按钮的点击事件
     */
    private final class SearchMemberOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            et_search_member_reported.setVisibility(View.VISIBLE);
            //打开软键盘
            InputMethodUtil.showInputMethod(IndividualCallService.this);
        }
    }

    /**
     * 搜索edittext输入搜索字的改变监听
     */
    private final class SearchMemberTextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (s.length() > 0 && !DataUtil.isLegalSearch(s)) {
                ToastUtil.showToast(IndividualCallService.this, "搜索的内容不合法！");
            } else {
                autoTextViewDao();
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private List<String> memberNames = new ArrayList<>();

    private void autoTextViewDao() {
        String string = et_search_member_reported.getText().toString().trim();
        memberNames.clear();
        for (int i = 0; i < memberList.size(); i++) {
            String name = memberList.get(i).getName();
            String id = memberList.get(i).id + "";
            if (string != null) {
                if ((!Util.isEmpty(name) && !Util.isEmpty(string) && name.toLowerCase().contains(string.toLowerCase())) || id.contains(string)) {
                    memberNames.add(name);
                }
            }
        }
        cn.vsx.vc.adapter.ArrayAdapter<String> arrayAdapter = new cn.vsx.vc.adapter.ArrayAdapter<>(IndividualCallService.this,
                R.layout.search_hail_fellow, memberNames);
        et_search_member_reported.setThreshold(1);
        et_search_member_reported.setAdapter(arrayAdapter);
        et_search_member_reported.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 选择搜索到的人
                // 选择搜索到的组
                TextView groupNameText = (TextView) view;
                String groupName = groupNameText.getText().toString();

                for (int i = 0; i < memberList.size(); i++) {
                    if (groupName.equals(memberList.get(i).getName())) {
                        lv_live_selsectmember_listview.setSelection(i);
                        myHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                et_search_member_reported.setText("");
                                et_search_member_reported.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
                InputMethodUtil.hideInputMethod(IndividualCallService.this, et_search_member_reported);
            }
        });
    }

    private final class SurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, int width, int height) {
            //view第一次显示的时候回调
            logger.error("onSurfaceTextureAvailable   currentType = "+currentType);
            logger.error("SurfaceTexture：宽"+width+"高"+height);
            //宽高就是手机屏幕的宽高
            //界面可见时，就会执行

            if (currentType == MyApplication.TYPE.PULL) {
                startPull(surface);
            } else if (currentType == MyApplication.TYPE.PUSH) {
                pushStream(surface);
            }else if(currentType == MyApplication.TYPE.UVCPUSH){
                if(null == uvcMediaStream){
                    pushUVCStream(surface);
                }else {
                    uvcMediaStream.setSurfaceTexture(surface);
                }
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            logger.info("onSurfaceTextureDestroyed----->" + surface);
            //界面不可见，就会销毁
            if (currentType == MyApplication.TYPE.PULL) {
                stopPull();
            } else if (currentType == MyApplication.TYPE.PUSH) {
//                mMediaStream.setSurfaceTexture(null);
                stopPush();
            }else if(currentType == MyApplication.TYPE.UVCPUSH){
                //                uvcMediaStream.setSurfaceTexture(null);
                stopUVCPush();
            }

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //视频一帧一帧刷新，会执行此方法
        }

    }

    private void stopPull() {
        if (mStreamRender != null) {
            mStreamRender.stop();
            mStreamRender = null;
            isPulling = false;
            TerminalFactory.getSDK().getLiveManager().ceaseLiving();
        }
    }

    ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            logger.error("currentType:" + currentType+"---"+resultCode + "---" + resultData);
            if (resultCode == EasyRTSPClient.RESULT_VIDEO_DISPLAYED) {
                pullcount = 0;
            } else if (resultCode == EasyRTSPClient.RESULT_VIDEO_SIZE) {

            } else if (resultCode == EasyRTSPClient.RESULT_TIMEOUT) {
                new AlertDialog.Builder(IndividualCallService.this).setMessage("试播时间到").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_AUDIO) {
                new AlertDialog.Builder(IndividualCallService.this).setMessage("音频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
            } else if (resultCode == EasyRTSPClient.RESULT_UNSUPPORTED_VIDEO) {
                new AlertDialog.Builder(IndividualCallService.this).setMessage("视频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
            } else if (resultCode == EasyRTSPClient.RESULT_EVENT) {

                int errorcode = resultData.getInt("errorcode");
                String resultDataString = resultData.getString("event-msg");
                logger.error("-------------->" + errorcode + "=========" + resultDataString+"-----count:"+pullcount);
                if(currentType != MyApplication.TYPE.PULL){
                    return;
                }
                if (errorcode != 0) {
                    stopPull();
                }
                if (errorcode == -101){
                    sendMessage("请检查网络连接");
                    TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                    finishVideoLive();
                }
                if (errorcode == 500 || errorcode == 404) {
                    if (pullcount < 100) {
                        try {
                            Thread.sleep(300);
                            logger.error("请求第" + pullcount + "次");
                            if (svLive != null && svLive.getVisibility() == View.VISIBLE && svLive.getSurfaceTexture() != null) {
                                startPull(svLive.getSurfaceTexture());
                                pullcount++;
                                svLive.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        livingViewHideOrShow(currentType == MyApplication.TYPE.PUSH ? true : false);
                                    }
                                });
                            }else if (sv_live_pop != null  && sv_live_pop.getVisibility() == View.VISIBLE && sv_live_pop.getSurfaceTexture() != null) {
                                startPull(sv_live_pop.getSurfaceTexture());
                                pullcount++;
                            }else{
                                TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        sendMessage("上报已结束");
                        TerminalFactory.getSDK().getLiveManager().ceaseWatching();
                        finishVideoLive();
                    }
                } /*else {
                    logger.info("sjl_错误日志");
                    sendMessage(resultDataString);
                    finishVideoLiveActivity();
                }*/

            } else if (resultCode == EasyRTSPClient.RESULT_RECORD_BEGIN) {
            } else if (resultCode == EasyRTSPClient.RESULT_RECORD_END) {
            }
        }
    };

    private void startPull(SurfaceTexture surface) {
        livingViewHideOrShow(false);

        mStreamRender = new EasyRTSPClient(IndividualCallService.this, Constants.PLAYKEY,
                surface, mResultReceiver);

        try {
            if (streamMediaServerUrl != null) {
                mStreamRender.start(streamMediaServerUrl, RTSPClient.TRANSTYPE_TCP, RTSPClient.EASY_SDK_VIDEO_FRAME_FLAG | RTSPClient.EASY_SDK_AUDIO_FRAME_FLAG, "", "", null);
                isPulling = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(IndividualCallService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            logger.error("IndividualCallService :"+e.toString());
        }
    }

    private void stopPush() {
        myHandler.removeMessages(CURRENTTIME);
        isPushing = false;
        boolean isStreaming = mMediaStream != null && mMediaStream.isStreaming();
        if (mMediaStream != null) {
            mMediaStream.stopPreview();
        } else {
            return;
        }
        logger.info("isFinishing() = " + "isFinishing()" + "    isStreaming = " + isStreaming);
        if (mMediaStream != null) {
            mMediaStream.stopStream();
            mMediaStream.release();
            mMediaStream = null;
            mService.setMediaStream(null);
//            stopService(new Intent(VideoLiveActivity.this, BackgroundCameraService.class));
            logger.info("---->>>>页面关闭，停止推送视频");
        } else {
            if (isStreaming) {
                mService.activePreview();
                logger.info("---->>>>退到后台，继续推送视频");
            }
        }
        TerminalFactory.getSDK().getLiveManager().ceaseLiving();
    }

    private void pushStream(SurfaceTexture surface) {
//        final File easyPusher = new File(Environment.getExternalStorageDirectory() + ("/EasyPusher"));
//        easyPusher.mkdir();

        livingViewHideOrShow(true);
        if (mService != null) {
            MediaStream ms = mService.getMediaStream();
            if (ms != null) {    // switch from background to front
                ms.stopPreview();
                mService.inActivePreview();
//                ms.destroyCamera();
                ms.setSurfaceTexture(surface);
//                ms.createCamera();
                ms.startPreview();
                mMediaStream = ms;
                if (ms.isStreaming()) {
                    String ip = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_IP, "");
                    int port = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_PORT, 0);
                    int id = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
                    String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
                    sendMessage("推流中.");
                    logger.info("推流地址:"+url);

                }
            } else {
                ms = new MediaStream(getApplicationContext(), surface, true);
                mMediaStream = ms;
                startCamera();
                mService.setMediaStream(ms);
            }
            pushcount =0;
            isPushing = true;
        } else {
            showToast("服务启动失败");
            finishVideoLive();
        }
    }

    private void halfPttUpDothing(){
        if (MyApplication.instance.isPttPress) {
            logger.info("PTT松开了，结束说话");
            MyApplication.instance.isPttPress = false;

            if (MyApplication.instance.getGroupListenenState() == LISTENING) {
                individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
//                btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            }
            if(MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED){

                startAutoHangUpTimer();
            }
            //            else {
//            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            //MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
        }

    }

    private void pttUpDoThing() {
        if (MyApplication.instance.isPttPress) {
            MyApplication.instance.isPttPress = false;
            //没有组呼权限
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
                return;
            }

            if (MyApplication.instance.getGroupListenenState() == LISTENING) {
                btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
            } else {
                btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
            }
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
            livingViewHideOrShow(false);
        }
        setViewEnable(true);
    }

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {

        @Override
        public void handler(final int methodResult, String resultDesc) {
            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE
                    &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
                if (MyApplication.instance.isPttPress) {
                    myHandler.post(new Runnable(){
                        @Override
                        public void run(){

                            if(callType == IndividualCallType.HALF_DUPLEX.getCode()){
                                if(methodResult == 0){
                                    tv_half_duplex_prompt.setText("我正在讲话");
                                    individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_speaking);
                                }else if(methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()){
                                    startAutoHangUpTimer();
                                    ToastUtil.showToast(IndividualCallService.this, "当前组是只听组，不能发起组呼");
                                    tv_half_duplex_prompt.setText("按住PTT说话");
                                    startAutoHangUpTimer();
                                    individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                                }else if(methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                                    tv_half_duplex_prompt.setText("按住PTT说话");
                                    startAutoHangUpTimer();
                                    individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
                                }else{
                                    tv_half_duplex_prompt.setText("按住PTT说话");
                                    startAutoHangUpTimer();
                                    if (MyApplication.instance.getGroupListenenState() != LISTENING) {
                                        individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                                    } else {
                                        individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                                    }
                                }
                            }else{
                                if (methodResult == 0) {//请求成功，开始组呼
                                    btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_speaking);
                                    ll_live_group_call.setVisibility(View.GONE);
                                    // tv_live_groupName.setText(DataUtil.getGroupByGroupNo(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)).name);
                                    // tv_live_speakingName.setText(TerminalFactory.getSDK().getParam(Params.MEMBER_NAME, ""));
                                    // tv_live_speakingId.setText(TerminalFactory.getSDK().getParam(Params.MEMBER_ID,"")+"");
                                    if (MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.PLAYING) {
                                        MyTerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                                    }
                                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                                    ToastUtil.showToast(IndividualCallService.this, "当前组是只听组，不能发起组呼");
                                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                                    btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
                                } else {//请求失败
                                    if (MyApplication.instance.getGroupListenenState() != LISTENING) {
                                        btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                                    } else {
                                        btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_gray);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        }
    };

    /**
     * 接收到消息
     */
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler=new ReceiveNotifyDataMessageHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            logger.info("接收到消息"+terminalMessage.toString());
            //判断是否是第一次安装
            boolean isAppFirstInStall = TerminalFactory.getSDK().getParam("is_app_first_install", true);
            //防止开始消息太多导致卡顿
            if (isAppFirstInStall){
                myHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TerminalFactory.getSDK().putParam("is_app_first_install",false);
                    }
                },30000);
                return;
            }

            //是否为别人发的消息
            boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            if (!isReceiver){
                return;
            }

            //判断消息类型，是否弹窗
            if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode() || terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.INFORM_TO_WATCH_LIVE){
                if(!viewAdded && !MyApplication.instance.isPttPress){
                    //延迟弹窗，否则判断是否在上报接口返回的是没有在上报
                    myHandler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            data.add(terminalMessage);
                            showDialogView();
                        }
                    },3000);
                    //30s没观看就取消当前弹窗
                    Message message = Message.obtain();
                    message.what = DISSMISS_CURRENT_DIALOG;
                    message.obj = terminalMessage;
                    myHandler.sendMessageDelayed(message,30*1000);
                }
            }

            if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() && terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.LIVE_WATCHING_END ||terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() &&
                    terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE ){
                return;
            }

            String callId=terminalMessage.messageBody.getString(JsonParam.CALLID);

            //如果是个呼但是接通了
            if (terminalMessage.messageType==MessageType.PRIVATE_CALL.getCode()
                    &&!TextUtils.isEmpty(callId)){
                return;
            }

            //判断是否在主页面的消息Fragment
            if (ActivityCollector.isActivityExist(NewMainActivity.class) && NewMainActivity.isForeground&&NewMainActivity.mCurrentFragmentCode==2){
                return;
            }
            //判断是否在个人当前聊天的Activity
            if (ActivityCollector.isActivityExist(IndividualNewsActivity.class) && IndividualNewsActivity.isForeground&&
                    terminalMessage.messageFromId==IndividualNewsActivity.mFromId){
                return;
            }
            //判断是否在组当前聊天的Activity
            if (ActivityCollector.isActivityExist(GroupCallNewsActivity.class) && GroupCallNewsActivity.isForeground&&
                    terminalMessage.messageToId==GroupCallNewsActivity.mGroupId){
                return;
            }


            //通知栏标题
            String noticeTitle=null;
            //通知栏内容
            String noticeContent=null;
            //通知Id
            int noticeId ;

            int unReadCount=0;
            String unReadCountText=null;


            if (terminalMessage.messageCategory== MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个呼
                noticeTitle=terminalMessage.messageFromName;
                noticeId=terminalMessage.messageFromId;

                unReadCount= TerminalFactory.getSDK().getTerminalMessageManager().getUnReadMessageCount(terminalMessage.messageFromId,MessageCategory.MESSAGE_TO_PERSONAGE.getCode())+1;
            }else {//组呼
                noticeTitle=terminalMessage.messageToName;
                noticeId=terminalMessage.messageToId;
                unReadCount= TerminalFactory.getSDK().getTerminalMessageManager().getUnReadMessageCount(terminalMessage.messageToId,MessageCategory.MESSAGE_TO_GROUP.getCode())+1;
                //当前组消息也不通知
                if (MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)==terminalMessage.messageToId){
                    return;
                }

            }

            if (unReadCount>99){
                unReadCountText="[99+条] ";
            }else if (unReadCount<=0){
                unReadCountText=" ";
            }else {
                unReadCountText="["+unReadCount+"条] ";
            }


            if(terminalMessage.messageType ==  MessageType.SHORT_TEXT.getCode()) {
                String content = terminalMessage.messageBody.getString(JsonParam.CONTENT);
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+content;
                }else {
                    noticeContent=content;
                }
            }

            if(terminalMessage.messageType ==  MessageType.LONG_TEXT.getCode()) {
                String path = terminalMessage.messagePath;
                File file = new File(path);
                if (!file.exists()) {
                    MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                    MyTerminalFactory.getSDK().download(terminalMessage, true);
                }
                String content = FileUtil.getStringFromFile(file);
                logger.info("长文本： path:"+path+"    content:"+content);
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+content;
                } else {
                    noticeContent=content;
                }
            }

            if(terminalMessage.messageType ==  MessageType.PICTURE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                   noticeContent=terminalMessage.messageFromName+":"+"[图片]";
                } else {
                   noticeContent="[图片]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[语音]";
                } else {
                    noticeContent="[语音]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[视频]";
                } else {
                    noticeContent="[视频]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.FILE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[文件]";
                } else {
                    noticeContent="[文件]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.POSITION.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[位置]";
                } else {
                    noticeContent="[位置]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.AFFICHE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[公告]";
                } else {
                    noticeContent="[公告]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.WARNING_INSTANCE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[警情]";
                } else {
                    noticeContent="[警情]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.PRIVATE_CALL.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[个呼]";
                } else {
                    noticeContent="[个呼]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.VIDEO_LIVE.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                   noticeContent=terminalMessage.messageFromName+":"+"[图像]";
                } else {
                    noticeContent="[图像]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.GROUP_CALL.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[组呼]";
                } else {
                    noticeContent="[组呼]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[录音]";
                } else {
                   noticeContent="[录音]";
                }
            }
            if(terminalMessage.messageType ==  MessageType.HYPERLINK.getCode()) {//人脸识别
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                    noticeContent=terminalMessage.messageFromName+":"+"[人脸识别]";
                } else {
                    noticeContent="[人脸识别]";
                }
            }

            Intent intent=new Intent(getApplicationContext(), NotificationClickReceiver.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("TerminalMessage",terminalMessage);
            intent.putExtra("bundle",bundle);
            Log.e("IndividualCallService", "通知栏消息:" + terminalMessage);
//            intent.putExtra("TerminalMessage",terminalMessage);
            PendingIntent pIntent=PendingIntent.getBroadcast(getApplicationContext(),noticeId,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder myBuilder = new Notification.Builder(getApplicationContext());
            myBuilder.setContentTitle(noticeTitle)//设置通知标题
                    .setContentText(unReadCountText+noticeContent)//设置通知内容
                    .setTicker("您有一条新消息！")//设置状态栏提示消息
                    .setSmallIcon(R.drawable.pttpdt)//设置通知图标
                    .setAutoCancel(true)//点击后取消
                    .setWhen(System.currentTimeMillis())//设置通知时间
                    .setPriority(Notification.PRIORITY_HIGH)//高优先级
                    .setContentIntent(pIntent)
                    .setDefaults(Notification.DEFAULT_SOUND); //设置通知点击事件
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //设置任何情况都会显示通知
                myBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
            Notification notification = myBuilder.build();
            //通过通知管理器来发起通知，ID区分通知
            notificationManager.notify(noticeId, notification);
        }
    };

    private void showDialogView(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(IndividualCallService.this)) {
                ToastUtil.showToast(IndividualCallService.this, "请打开悬浮窗权限，否则私密呼叫和图像功能无法使用！");
            }
        }
        hideAllView();
        // 如果已经添加了就只更新view
        if (dialogAdded) {
            windowManager.updateViewLayout(view, layoutParams2);

            video_dialog.setVisibility(View.VISIBLE);
            stackViewAdapter.setData(data);
        } else {
            windowManager.addView(view, layoutParams2);
            video_dialog.setVisibility(View.VISIBLE);
            dialogAdded = true;
            stackViewAdapter = new StackViewAdapter(getApplicationContext());
            stackViewAdapter.setData(data);
            swipeFlingAdapterView.setFlingListener(new FlingListener());
            stackViewAdapter.setCloseDialogListener(new OnClickListenerCloseDialog());
            stackViewAdapter.setGoWatchListener(new OnClickListenerGoWatch() );
            swipeFlingAdapterView.setAdapter(stackViewAdapter);
        }
    }

    private void halfPttDownDothing(){
        logger.info("pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if (!CheckMyPermission.selfPermissionGranted(IndividualCallService.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            //            CheckMyPermission.permissionPrompt(IndividualCallService.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        cancelAutoHangUpTimer();
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
        if (resultCode == BaseCommonCode.SUCCESS_CODE){//允许组呼了
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            MyApplication.instance.isPttPress = true;
            tv_half_duplex_prompt.setText("我准备说话");
            tv_half_duplex_prompt.setTextColor(Color.YELLOW);
//            ptt.terminalsdk.manager.Prompt.PromptManager.getInstance().playPrompt();
            individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(IndividualCallService.this, resultCode);
        }

    }

    private void pttDownDoThing() {
        logger.info("pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
        if (!CheckMyPermission.selfPermissionGranted(IndividualCallService.this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
//            CheckMyPermission.permissionPrompt(IndividualCallService.this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())){
            Toast.makeText(this,"没有组呼权限",Toast.LENGTH_SHORT).show();
            return;
        }

        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            MyApplication.instance.isPttPress = true;
            btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_yellow);
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(IndividualCallService.this, resultCode);
        }
        setViewEnable(false);
    }
    private void setViewEnable (boolean isEnable) {
        ll_live_look_invite_member.setEnabled(isEnable);
        ll_live_look_hangup.setEnabled(isEnable);
        iv_live_retract.setEnabled(isEnable);
    }

    private void recoverStateMachine() {
        logger.debug("recoverStateMachine:"+currentType);
        if (currentType == MyApplication.TYPE.PUSH ||
                currentType == MyApplication.TYPE.UVCPUSH) {
            MyTerminalFactory.getSDK().getVideoProxy().start().unregister(this);
            MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
            isPushing = false;
//            if (MyTerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine().getCurrentState() != VideoLivePushingState.PUSHING) {
//            }
        } else if (currentType == MyApplication.TYPE.PULL) {
            MyTerminalFactory.getSDK().getLiveManager().ceaseWatching();
            isPulling = false;
//            if (MyTerminalFactory.getSDK().getLiveManager().getVideoLivePlayingStateMachine().getCurrentState() != VideoLivePlayingState.PLAYING) {
//            }
        }
    }

    MediaStream mMediaStream;
    UVCMediaStream uvcMediaStream;
    int width = 640, height = 480;
    List<String> listResolution;
    List<String> listResolutionName;

    private int getDgree() {
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
            default:
                break;
        }
        return degrees;
    }

    private void sendMessage(String message) {
        Message msg = Message.obtain();
        msg.what = MSG_STATE;
        Bundle bundle = new Bundle();
        bundle.putString(STATE, message);
        msg.setData(bundle);
        myHandler.sendMessage(msg);
    }



    private void startCamera() {
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        String r = listResolution.get(position);
        String[] splitR = r.split("x");
        width = Integer.parseInt(splitR[0]);
        height = Integer.parseInt(splitR[1]);
        logger.error("分辨率--width:" + width+"----height:"+height);
        mMediaStream.updateResolution(width, height);
        mMediaStream.setDgree(getDgree());
        mMediaStream.createCamera();
        mMediaStream.startPreview();
        logger.info("------>>>>startCamera");
        if (mMediaStream.isStreaming()) {
            sendMessage("推流中..");
            String ip = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_IP, "");
            int port = MyTerminalFactory.getSDK().getParam(Params.VIDEO_SERVER_PORT, 0);
            int id = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

            String url = String.format("rtsp://%s:%s/%s.sdp", ip, port, id);
            logger.info("startCamera ----->  " + url);
        }

//        initSpninner();
    }

    private void startUVCCamera(){
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        String r = listResolution.get(position);
        String[] splitR = r.split("x");
        width = Integer.parseInt(splitR[0]);
        height = Integer.parseInt(splitR[1]);
        logger.error("分辨率--width:" + width+"----height:"+height);
//        uvcMediaStream.updateResolution(width, height);
//                uvcMediaStream.setDgree(getDgree());
        uvcMediaStream.createCamera();
        uvcMediaStream.startPreview();
        logger.info("------>>>>startCamera");

        //        initSpninner();
    }

    private void initSpninner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spn_item, listResolutionName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnResolution.setAdapter(adapter);
        int position = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        spnResolution.setSelection(position, false);
        spnResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String r = listResolution.get(position);
                String[] splitR = r.split("x");
                width = Integer.parseInt(splitR[0]);
                height = Integer.parseInt(splitR[1]);

                if (mMediaStream != null) {
                    logger.error("initSpninner width"+width+"height"+height);
                    mMediaStream.updateResolution(width, height);
//                    mMediaStream.reStartStream();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initResolution() {
        listResolution = new ArrayList<>(Arrays.asList("1920x1080", "1280x720", "640x480", "320x240"));
        listResolutionName = new ArrayList<>(Arrays.asList("超清", "高清", "标清", "流畅"));
//        listResolution = new ArrayList<>();
//        listResolution = VideoUtil.getSupportResolution();
        logger.info("listResolution----->"+listResolution);
        //手机支持的分辨率，暂时没有用注释掉
//        boolean supportdefault = listResolution.contains(String.format("%dx%d", width, height));
//        if (!supportdefault) {
//            String r = listResolution.get(0);
//            String[] splitR = r.split("x");
//            width = Integer.parseInt(splitR[0]);
//            height = Integer.parseInt(splitR[1]);
//        }
    }

    private void hideUVCLiveView(){
        ll_uvc_speak_state.setVisibility(View.GONE);
        iv_uvc_live_retract.setVisibility(View.GONE);
        lv_uvc_live_member_info.setVisibility(View.GONE);
        ll_function.setVisibility(View.GONE);
    }

    private void showUVCLiveView(){
        tv_uvc_live_time.setVisibility(View.VISIBLE);
        iv_uvc_live_retract.setVisibility(View.VISIBLE);
        lv_uvc_live_member_info.setVisibility(View.VISIBLE);
        ll_function.setVisibility(View.VISIBLE);
    }

    private void hideLivingView() {
        tv_live_realtime.setVisibility(View.GONE);
        iv_live_retract.setVisibility(View.GONE);
        ll_live_chage_camera.setVisibility(View.GONE);
        tv_spn_resolution.setVisibility(View.GONE);
        spnResolution.setVisibility(View.GONE);
        ll_live_hangup_total.setVisibility(View.GONE);
        ll_live_invite_member.setVisibility(View.GONE);
        if(!MyApplication.instance.isPttPress){
            btn_live_look_ptt.setVisibility(View.GONE);
        }else {
            btn_live_look_ptt.setVisibility(View.VISIBLE);
        }
        ll_live_look_invite_member.setVisibility(View.GONE);
        ll_live_look_hangup.setVisibility(View.GONE);
        lv_live_member_info.setVisibility(View.GONE);
    }

    private void showLivingView(boolean isPush) {
        if (isPush) {
            hideLivingView();
            tv_live_realtime.setVisibility(View.VISIBLE);
            iv_live_retract.setVisibility(View.VISIBLE);
            ll_live_chage_camera.setVisibility(View.VISIBLE);
            lv_live_member_info.setVisibility(View.VISIBLE);
            ll_live_hangup_total.setVisibility(View.VISIBLE);
            ll_live_invite_member.setVisibility(View.VISIBLE);

        } else {
            hideLivingView();

            ll_live_look_invite_member.setVisibility(View.VISIBLE);
            ll_live_look_hangup.setVisibility(View.VISIBLE);
            btn_live_look_ptt.setVisibility(View.VISIBLE);
            iv_live_retract.setVisibility(View.VISIBLE);
        }
    }


    private void livingViewHideOrShow(boolean isPush) {
        logger.error("isPush = "+isPush);
        myHandler.removeMessages(HIDELIVINGVIEW);
        showLivingView(isPush);
        myHandler.sendEmptyMessageDelayed(HIDELIVINGVIEW,5000);
    }

    private List<Member> get(String content) {
        List<Member> list = new ArrayList<>();
        if (memberList == null) {
            memberList = new ArrayList<>();
        } else {
            for (int i = 0; i < memberList.size(); i++) {

                if (String.valueOf(memberList.get(i).id).contains(content)) {
                    list.add(memberList.get(i));
                } else {
                    String name = memberList.get(i).getName();
                    if (!Util.isEmpty(name) && !Util.isEmpty(content) && name.toLowerCase().contains(content.toLowerCase())) {
                        list.add(memberList.get(i));
                    }
                }
            }
        }
        return list;
    }
    private final class RequestEditListener implements View.OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText etx = (EditText) v;
            if (hasFocus) {
//                search_select_request.setVisibility(View.VISIBLE);
            } else {
//                search_select_request.setVisibility(View.VISIBLE);
            }
        }
    }

    private final class EditListener implements View.OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            EditText et = (EditText) v;
            if (hasFocus) {
                search_select.setVisibility(View.VISIBLE);
            } else {
                search_select.setVisibility(View.VISIBLE);
            }
        }
    }
    private final class RequestEditChangeListener implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (String.valueOf(s).equals("")) {
                liveContactsAdapter.bind(memberList, selectItem, s.toString());
            } else if (!String.valueOf(s).equals("")) {
                if (get(s.toString()).size()<=0){
                }else{
                    liveContactsAdapter.bind(get(s.toString()), selectItem, s.toString());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
    private final class EditChangeListener implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }


        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (TextUtils.isEmpty(String.valueOf(s))) {
                lv_live_selsectmember_listview.setVisibility(View.VISIBLE);
                ll_no_info.setVisibility(View.GONE);
                liveContactsAdapter.bind(memberList, selectItem, s.toString());
                img_cencle.setVisibility(View.GONE);
            } else{
                if (get(s.toString()).size()<=0){
                    lv_live_selsectmember_listview.setVisibility(View.GONE);
                    ll_no_info.setVisibility(View.VISIBLE);
                    tv_no_user.setText(et_search_allcontacts.getText().toString());
                }else{
                    lv_live_selsectmember_listview.setVisibility(View.VISIBLE);
                    ll_no_info.setVisibility(View.GONE);
                    liveContactsAdapter.bind(get(s.toString()), selectItem, s.toString());
                }

                img_cencle.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {
        @Override
        public void handler(int reasonCode) {
            logger.info("收到ReceiveGroupCallCeasedIndicationHandler");
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(callType == IndividualCallType.HALF_DUPLEX.getCode()){
                        tv_half_duplex_prompt.setText("按住PTT说话");
                        tv_half_duplex_prompt.setTextColor(Color.WHITE);
                        individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                        individual_call_half_duplex_ptt.setEnabled(true);
                        if(MyApplication.instance.getIndividualState() ==IndividualCallState.SPEAKING){
                            startAutoHangUpTimer();//对方抬起，启动时间检测机制
                        }
                    }else {
                        ll_live_group_call.setVisibility(View.GONE);
                        btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                        ll_uvc_speak_state.setVisibility(View.GONE);
                    }
                }
            });
        }
    };
    //主动方停止组呼
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(callType == IndividualCallType.HALF_DUPLEX.getCode()){
                        tv_half_duplex_prompt.setText("按住PTT说话");
                        tv_half_duplex_prompt.setTextColor(Color.WHITE);
                        individual_call_half_duplex_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                        individual_call_half_duplex_ptt.setEnabled(true);
                        logger.info("当前个呼状态："+MyApplication.instance.getIndividualState());
                        if(MyApplication.instance.getIndividualState() == IndividualCallState.SPEAKING){
                            //只有在半双工个呼接通了才发送超时检测
                            startAutoHangUpTimer();//对方抬起，启动时间检测机制
                        }

                    }else {
                        btn_live_look_ptt.setBackgroundResource(R.drawable.rectangle_with_corners_shape_dodgerblue2);
                        ll_live_group_call.setVisibility(View.GONE);
                    }
                }
            });
        }
    };

    private void inviteSelectMember(){
        memberList.clear();
        TerminalFactory.getSDK().getLiveManager().getPushMemberList(true,true);
        ll_live_selectmember_theme.setVisibility(View.GONE);
        liveContactsAdapter = new LiveContactsAdapter(getApplicationContext(), memberList,true);
        liveContactsAdapter.setOnItemClickListener(new OnInvitaListViewItemClick());
        lv_live_selsectmember_listview.setAdapter(liveContactsAdapter);
        img_cencle.setVisibility(View.GONE);
    }

    //接收到上报视频的回调
    private ReceiverActivePushVideoHandler receiverActivePushVideoHandler = new ReceiverActivePushVideoHandler() {
        @Override
        public void handler(final List<Integer> memberIds) {
            Log.e("IndividualCallService", "ReceiverActivePushVideoHandler");
            logger.error("上报给："+memberIds);
            activePush = true;
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    currentType = MyApplication.TYPE.PUSH;
                    layoutParams1.flags =
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED|
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    if (memberIds.size() == 0){//要弹出选择成员页
                        refresh();
                        hideAllView();
                        selectItem.clear();
                        total = 0;
                        live_select_member.setVisibility(View.VISIBLE);
                        btn_live_selectmember_start.setText("开始");
                        btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                        pushSelectMember();

                    }else {//直接上报了
                        if(MyApplication.instance.usbAttached){
                            showSwitchCameraView();
                        }else {
                            int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("","");
                            if (requestCode == BaseCommonCode.SUCCESS_CODE){
                                //请求成功,直接开始推送视频
                                refresh();
                                hideAllView();
                                live.setVisibility(View.VISIBLE);

                                pushMemberList = memberIds;
                                pushView("");
                            }else {
                                ToastUtil.livingFailToast(IndividualCallService.this, requestCode, TerminalErrorCode.LIVING_PUSHING.getErrorCode());
                            }
                        }
                    }
                }
            });

        }
    };
    private void pushSelectMember() {
        memberList.clear();
        MyTerminalFactory.getSDK().getLiveManager().getPushMemberList(true,true);

        ll_live_selectmember_theme.setVisibility(View.VISIBLE);
        //theme = TerminalFactory.getSDK().getParam(Params.MEMBER_NAME,"")+"上报图像";
        tv_live_selectmember_theme.setText(theme);
        liveContactsAdapter = new LiveContactsAdapter(getApplicationContext(), memberList, true);
        liveContactsAdapter.setOnItemClickListener(new OnInvitaListViewItemClick());
        lv_live_selsectmember_listview.setAdapter(liveContactsAdapter);

        img_cencle.setVisibility(View.GONE);
    }

    private void requestSelectMember() {//请求图像
        memberList.clear();
        MyTerminalFactory.getSDK().getLiveManager().getPushMemberList(false,true);
        btn_live_selectmember_start.setText("开始");
        btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
        ll_live_selectmember_theme.setVisibility(View.GONE);
        HashMap<Integer, Boolean> isSelected = new HashMap<>();
        for(int i=0;i<memberList.size();i++){
            isSelected.put(i,false);
        }
        liveContactsAdapter = new LiveContactsAdapter(getApplicationContext(), memberList, false);
        liveContactsAdapter.setOnItemClickListener(new OnInvitaListViewItemClick());
        lv_live_selsectmember_listview.setAdapter(liveContactsAdapter);
        img_cencle.setVisibility(View.GONE);
    }

    private void pushView(String theme) {
        logger.error("=====pushView======="+theme);
        if(TextUtils.isEmpty(theme)){
            theme = "我正在上报图像";
        }

        myHandler.sendEmptyMessage(CURRENTTIME);
        live_vedioTheme.setText(theme);

        tv_live_realtime.setVisibility(View.VISIBLE);
        lv_live_member_info.setVisibility(View.VISIBLE);
        ll_live_chage_camera.setVisibility(View.VISIBLE);
        ll_live_hangup_total.setVisibility(View.VISIBLE);
        ll_live_invite_member.setVisibility(View.VISIBLE);

        live_vedioName.setVisibility(View.GONE);
        live_vedioId.setVisibility(View.GONE);
        live_vedioIcon.setVisibility(View.GONE);
        my_view2.setVisibility(View.GONE);
        btn_live_look_ptt.setVisibility(View.GONE);
        ll_live_look_invite_member.setVisibility(View.GONE);
        ll_live_look_hangup.setVisibility(View.GONE);
        ll_live_group_call.setVisibility(View.GONE);

        if(liveContactsAdapter != null){
            liveContactsAdapter.notifyLiveMember();
        }
    }

    private void uvcPushView(String theme){
        logger.error("=====pushView======="+theme);
        if(TextUtils.isEmpty(theme)){
            theme = "我正在上报图像";
        }

        myHandler.sendEmptyMessage(CURRENTTIME);
        tv_uvc_liveTheme.setText(theme);
    }
    private void pullView(Member member, String theme) {
        logger.error(member+"=====pullView======="+theme);
        if(theme == null){
            theme = member.getName()+"正在上报图像";
        }
        live_vedioTheme.setText(theme);
        live_vedioName.setVisibility(View.VISIBLE);
        live_vedioId.setVisibility(View.VISIBLE);
        live_vedioIcon.setVisibility(View.VISIBLE);
        my_view2.setVisibility(View.VISIBLE);
        live_vedioName.setText(member.getName());
        live_vedioId.setText(HandleIdUtil.handleId(member.getId()));
        btn_live_look_ptt.setVisibility(View.VISIBLE);
        ll_live_look_invite_member.setVisibility(View.VISIBLE);
        ll_live_look_hangup.setVisibility(View.VISIBLE);

        tv_live_realtime.setVisibility(View.GONE);
        lv_live_member_info.setVisibility(View.GONE);
        ll_live_chage_camera.setVisibility(View.GONE);
        ll_live_hangup_total.setVisibility(View.GONE);
        ll_live_invite_member.setVisibility(View.GONE);
        ll_live_group_call.setVisibility(View.GONE);
    }

    /**
     * 请求直播
     */
    private ReceiverRequestVideoHandler receiverRequestVideoHandler = new ReceiverRequestVideoHandler() {
        @Override
        public void handler(final Member member) {
            logger.error("请求的直播人："+member);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    currentType = MyApplication.TYPE.PULL;
                    if (member.getName() == null) {
                        refresh();
                        hideAllView();
                        total = 0;
                        live_select_member.setVisibility(View.VISIBLE);
                        btn_live_selectmember_start.setText("开始");
                        btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
                        requestSelectMember();
                    } else {//直接请求
                        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMemberLive(member.id,"");

//                        Intent intent=new Intent(mContext, LiveRequestActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.putExtra("requestCode",requestCode);
//                        intent.putExtra("data",member);
//
//                        startActivity(intent);

                        if (requestCode == BaseCommonCode.SUCCESS_CODE){
                            //请求成功,直接开始推送视频
                            refresh();
                            hideAllView();
                            live_request.setVisibility(View.VISIBLE);

                            liveMember = member;
                            tv_live_request_name.setText(HandleIdUtil.handleName(member.getName()));
                            tv_live_request_id.setText(HandleIdUtil.handleId(member.id));
                            PhotoUtils.loadNetBitmap(getApplicationContext(),member.avatarUrl,ivAvaTarRequest,R.drawable.user_photo);

                        }else {
                            ToastUtil.livingFailToast(IndividualCallService.this, requestCode, TerminalErrorCode.LIVING_REQUEST.getErrorCode());
                        }
                    }
                }
            });
        }
    };
    private void hideKey(){

        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);

    }
    private void startLiveService() {
        // 创建直播服务
        MyTerminalFactory.getSDK().getVideoProxy().start().register(this);
        startService(new Intent(this, BackgroundCameraService.class));

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((BackgroundCameraService.LocalBinder) iBinder).getService();
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        //BIND_AUTO_CREATE  如果没有服务就自己创建一个，执行onCreate()；
        bindService(new Intent(this, BackgroundCameraService.class), conn, BIND_AUTO_CREATE);
    }
    private void stopLiveService(){
        MyTerminalFactory.getSDK().getVideoProxy().start().unregister(this);
        if (conn != null){
            unbindService(conn);
        }
        stopService(new Intent(this, BackgroundCameraService.class));
    }

    /**
     * 半双工PTT按钮事件
     */
    private class HalfDuplexPttOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    logger.info("PTT按下了，开始说话");
                    halfPttDownDothing();
                    break;
                case MotionEvent.ACTION_MOVE:

                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    halfPttUpDothing();

                    break;

                default:
                    break;
            }
            return true;
        }
    }
    private class FlingListener implements SwipeFlingAdapterView.onFlingListener{

        @Override
        public void removeFirstObjectInAdapter(){
            if(stackViewAdapter.getCount()==1){
                stackViewAdapter.remove(0);
            }else {
                stackViewAdapter.setLast(0);
            }
        }

        @Override
        public void onLeftCardExit(Object dataObject){
        }

        @Override
        public void onRightCardExit(Object dataObject){
        }

        @Override
        public void onAdapterAboutToEmpty(int itemsInAdapter){
            removeView();
        }

        @Override
        public void onScroll(float progress, float scrollXProgress){
        }
    }

    private class ImgCancelOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            et_search_allcontacts.setText("");
        }
    }
    //上报图像邀请按钮点击事件
    private class InviteMemberOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            windowManager.updateViewLayout(view,layoutParams1);
            total = 0;
            hideAllView();
            live_select_member.setVisibility(View.VISIBLE);
            btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
            btn_live_selectmember_start.setText("开始");
            inviteSelectMember();
        }
    }

    private class RetractOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {//视频缩到小窗口
            layoutParams.x = 0;
            layoutParams.y = 0;
            windowManager.updateViewLayout(view,layoutParams);
            hideAllView();
            popup_mini_live.setVisibility(View.VISIBLE);
            MyApplication.instance.isMiniLive = true;
            getSv(currentType, sv_live_pop);
        }
    }

    private class PopMiniLiveOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    downX =  event.getX();
                    downY =  event.getY();
                    oddOffsetX = layoutParams.x;
                    oddOffsetY = layoutParams.y;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY =  event.getY();
                    //不除以3，拖动的view抖动的有点厉害
                    if (Math.abs(downX - moveX) > 5
                            || Math.abs(downY - moveY) > 5) {
                        // 更新浮动窗口位置参数
                        layoutParams.x = (int)(screenWidth-(x + downX));
                        layoutParams.y = (int) (y - downY);
                        windowManager.updateViewLayout(view, layoutParams);
                        return false;
                    }
                    break;


                case MotionEvent.ACTION_UP:
                    int newOffsetX = layoutParams.x;
                    int newOffsetY = layoutParams.y;
                    if(Math.abs(newOffsetX - oddOffsetX) <=30 && Math.abs(newOffsetY - oddOffsetY) <=30){
                        if(mListener != null){
                            mListener.onClick(view);
                        }

                        hideKey();
                        windowManager.updateViewLayout(view,layoutParams1);
                        hideAllView();
                        if(currentType == MyApplication.TYPE.UVCPUSH){
                            usbLive.setVisibility(View.VISIBLE);
                            getSv(currentType, sv_uvc_live);
                        }else {
                            live.setVisibility(View.VISIBLE);
                            getSv(currentType, svLive);
                        }
                        MyApplication.instance.isMiniLive = false;
                        logger.error("currentType ----------------> "+currentType);

                        finishTransparentActivity();

                    }
                    break;
            }
            return true;
        }
    }

    private class LiveThemeTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            logger.info("sjl_编辑主题的输入字数："+s.length());
            if (s.length() > 29) {
                ToastUtil.showToast(IndividualCallService.this, "输入字数已达上限");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class SearchContactsTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().contains(" ")) {
                String[] str = s.toString().split(" ");
                String str1 = "";
                for (int i = 0; i < str.length; i++) {
                    str1 += str[i];
                }
                et_search_allcontacts.setText(str1);

                et_search_allcontacts.setSelection(start);

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class PopMiniOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //触摸点到边界屏幕的距离
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    //触摸点到自身边界的距离
                    downX =  event.getX();
                    downY =  event.getY();
                    oddOffsetX = layoutParams.x;
                    oddOffsetY = layoutParams.y;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY =  event.getY();
                    //不除以3，拖动的view抖动的有点厉害
                    if (Math.abs(downX - moveX) > 5
                            || Math.abs(downY - moveY) > 5) {
                        // 更新浮动窗口位置参数
                        layoutParams.x = (int)(screenWidth-(x + downX));
                        layoutParams.y = (int) (y - downY);
                        windowManager.updateViewLayout(view, layoutParams);

                    }

                    break;
                case MotionEvent.ACTION_UP:
                    finishTransparentActivity();
                    int newOffsetX = layoutParams.x;
                    int newOffsetY = layoutParams.y;
                    if(Math.abs(newOffsetX - oddOffsetX) <=30 && Math.abs(newOffsetY - oddOffsetY) <=30){

                        if(mListener != null){

                            mListener.onClick(view);
                        }

                        hideKey();
                        hideAllView();
                        if (status == 1){
                            windowManager.updateViewLayout(view,layoutParams1);
                            individual_call_request.setVisibility(View.VISIBLE);
                        }else if (status ==2){
                            windowManager.updateViewLayout(view,layoutParams1);
                            if(callType== IndividualCallType.FULL_DUPLEX.getCode()){
                                individual_call_speaking.setVisibility(View.VISIBLE);
                            }else if(callType==IndividualCallType.HALF_DUPLEX.getCode()){
                                individual_call_half_duplex.setVisibility(View.VISIBLE);
                            }
                        }else if (status == 3){
                            windowManager.updateViewLayout(view,layoutParams1);
                            individual_call_chooice.setVisibility(View.VISIBLE);
                        }else if(status ==4){
                            windowManager.updateViewLayout(view,layoutParams1);
                            live_request.setVisibility(View.VISIBLE);
                        }else if(status ==5){
                            windowManager.updateViewLayout(view,layoutParams1);
                            live_report.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
            }
            return true;
        }
    }

    /**
     * 监听Home键
     */
    class HomeWatcherReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(!viewAdded){//如果视图还没有添加就返回
                return;
            }
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)||SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    //如果正在直播中或者观看直播，显示直播的小窗口
                    if(live.getVisibility() ==View.VISIBLE){
                        windowManager.updateViewLayout(view,layoutParams);
                        hideAllView();
                        popup_mini_live.setVisibility(View.VISIBLE);
                        MyApplication.instance.isMiniLive = true;
                        getSv(currentType, sv_live_pop);

                    }
                    //直播和个呼等待接听
                    else if(individual_call_request.getVisibility() ==View.VISIBLE){
                        status =1;
                        showPopMiniView();
                    }
                    else if(individual_call_chooice.getVisibility() ==View.VISIBLE){
                        status =3;
                        showPopMiniView();
                    }
                    else if(live_request.getVisibility() ==View.VISIBLE ){
                        status =4;
                        showPopMiniView();
                    }
                    else if(live_report.getVisibility() ==View.VISIBLE ){
                        status =5;
                        showPopMiniView();
                    }
                    //如果在个呼中，显示个呼的小窗口
                    else if(individual_call_half_duplex .getVisibility() == View.VISIBLE || individual_call_speaking.getVisibility() ==View.VISIBLE){
                        status = 2;
                        windowManager.updateViewLayout(view,layoutParams);
                        hideAllView();
                        pop_minimize.setVisibility(View.VISIBLE);
                        tv_waiting.setVisibility(View.GONE);
                        popup_ICTV_speaking_time.setVisibility(View.VISIBLE);
                        MyApplication.instance.isMiniLive = true;
                    }
                    //选择成员
                    else if(live_select_member.getVisibility() ==View.VISIBLE ){
                        //如果正在上报或者观看，显示视频的小窗口
                        if(isPulling ||isPushing){
                            windowManager.updateViewLayout(view,layoutParams);
                            hideAllView();
                            popup_mini_live.setVisibility(View.VISIBLE);
                            MyApplication.instance.isMiniLive = true;
                            getSv(currentType, sv_live_pop);
                        }else {
                            //没有观看就关掉界面
                            hideKey();
                            selectItem.clear();
                            tv_checktext.setText("");
                            et_search_allcontacts.setText("");
                            total = 0;
                            btn_live_selectmember_start.setText("开始");
                            btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                            InputMethodUtil.hideInputMethod(getApplicationContext(),et_search_allcontacts);
                            removeView();
                        }
                    }
                    //编辑主题
                    else if(live_edit_theme.getVisibility() ==View.VISIBLE){
                        InputMethodUtil.hideInputMethod(IndividualCallService.this, et_live_edit_import_theme);
                        removeView();
                    }
                }
            }
        }
    }

    private class OnInvitaListViewItemClick implements LiveContactsAdapter.OnItemClickListener{

        @Override
        public void onItemClick(int position,boolean checked,boolean isPush){
            if(isPush){
                if(checked){
                    selectItem.add(memberList.get(position));
                    total++;
                }else {
                    total--;
                    selectItem.remove(memberList.get(position));
                }
                if(total>0){
                    btn_live_selectmember_start.setText("开始(" + total + ")");
                    btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                }else {
                    btn_live_selectmember_start.setText("开始");
                    btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                }
            }else{
                if(checked){
                    total = 1;
                    selectItem.clear();
                    selectItem.add(memberList.get(position));
                }else {
                    total = 0;
                    selectItem.remove(memberList.get(position));
                }
                if(total>0){
                    btn_live_selectmember_start.setText("开始(" + total + ")");
                    btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg);
                }else {
                    btn_live_selectmember_start.setText("开始");
                    btn_live_selectmember_start.setBackgroundResource(R.drawable.live_theme_confirm_bg_no);
                }
            }




            et_search_allcontacts.setText("");

            StringBuffer sb = new StringBuffer();
            Log.e("OnInvitaListViewItemCli", "selectItem:" + selectItem);
            for (Member m : selectItem) {
                sb.append(m.getName() + "  ");
            }
            tv_checktext.setText(sb);
            //获取textview宽度
            TextPaint textPaint = new TextPaint();
            textPaint = tv_checktext.getPaint();
            float textPaintWidth = textPaint.measureText(sb.toString());
            if (textPaintWidth >= screenWidth - (screenWidth / 4)) {
                horizonMenu.setLayoutParams(new LinearLayout.LayoutParams(screenWidth - (screenWidth / 4), ViewGroup.LayoutParams.WRAP_CONTENT));
                logger.info("textView的宽度达到了屏幕的五分之四");
            } else {
                horizonMenu.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private void showPopMiniView(){
        windowManager.updateViewLayout(view,layoutParams);
        hideAllView();
        pop_minimize.setVisibility(View.VISIBLE);
        tv_waiting.setVisibility(View.VISIBLE);
        popup_ICTV_speaking_time.setVisibility(View.GONE);
        MyApplication.instance.isMiniLive = true;
    }

    private class PhoneCameraClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v){
            windowManager.removeView(switchCameraView);
            switchCameraViewAdd = false;
            currentType = MyApplication.TYPE.PUSH;
            if(activePush){
                requestStartLive();
            }else {
                MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
                refresh();
                hideAllView();
                live.setVisibility(View.VISIBLE);
                pushView("");
                PromptManager.getInstance().stopRing();
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
            }
        }
    }

    private class OutCameraClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v){
            windowManager.removeView(switchCameraView);
            switchCameraViewAdd = false;
            currentType = MyApplication.TYPE.UVCPUSH;
            if(activePush){
                requestStartLive();
            }else{
                MyTerminalFactory.getSDK().getLiveManager().responseLiving(true);
                refresh();
                hideAllView();
                usbLive.setVisibility(View.VISIBLE);
                uvcPushView("");
                PromptManager.getInstance().stopRing();
                MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
            }
        }
    }
}
