package cn.vsx.vc.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IGotaKeyHandler;
import android.app.IGotaKeyMonitor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.widget.TextViewCompat;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.speechsynthesizer.SpeechSynthesizer;
import com.baidu.speechsynthesizer.SpeechSynthesizerListener;
import com.baidu.speechsynthesizer.publicutility.SpeechError;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.TimerTask;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.GroupScanType;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupByNoHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupScanResultHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEnvironmentMonitorHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyZfyBoundPhoneMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageAdd1Handler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.GroupMemberActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.UnbindDialog;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.CommonGroupUtil;
import cn.vsx.vc.utils.MyDataUtil;
import cn.vsx.vc.view.ChangeGroupView;
import cn.vsx.vc.view.ChangeGroupView.OnGroupChangedListener;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import ptt.terminalsdk.bean.GroupBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.MyDataManager;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.receiveHandler.ReceiveTestGroupCallHandler;
import ptt.terminalsdk.receiveHandler.ReceiveUpdateDepGroupHandler;
import ptt.terminalsdk.service.BluetoothLeService;
import ptt.terminalsdk.service.TestGroupCallService;
import ptt.terminalsdk.tools.PhoneAdapter;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState.IDLE;

@SuppressLint("ValidFragment")
public class TalkbackFragment extends BaseFragment {


    /**
     * 60s倒计时
     */
    @SuppressWarnings("HandlerLeak")
    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                timeProgress--;
                if (timeProgress <= 0) {
                    myHandler.removeMessages(1);
                    pttUpDoThing();
                } else {
                    talkback_time_progress.setText(String.valueOf(timeProgress));
                    myHandler.sendEmptyMessageDelayed(1, 1000);
                }
            }
        }
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            logger.info("主动方停止组呼的消息ReceiveCeaseGroupCallConformationHander" + "/" + MyApplication.instance.getGroupListenenState());
            myHandler.post(() -> {
                if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                    if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
                        isScanGroupCall = false;
                        change2Listening();
                    } else {
                        //如果是停止组呼
                        MyApplication.instance.isPttPress = false;
                        myHandler.removeMessages(1);
                        timeProgress = 60;
                        change2Silence();
                    }
                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    setCurrentGroupScanView(currentGroupId);
                    tx_ptt_group_name.setText("");
                }
                setViewEnable(true);
            });
        }
    };

    private ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = new ReceiveResponseGroupActiveHandler() {
        @Override
        public void handler(boolean isActive, int responseGroupId) {
            //如果时间到了，还在响应组会话界面，将PTT禁止
            Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(responseGroupId);
            if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == responseGroupId && !isActive &&
                    !groupByGroupNo.isHighUser()) {
                myHandler.post(() -> change2Forbid());
            }
        }
    };

    private ReceiveGetGroupByNoHandler receiveGetGroupByNoHandler = group -> myHandler.post(new Runnable() {
        @Override
        public void run() {
            myHandler.post(() -> {
                tv_current_group.setText(group.getName());
                tv_current_folder.setText(group.getDepartmentName());
            });
        }
    });

    private ReceiveUnreadMessageAdd1Handler receiveUnreadMessageAdd1Handler = isAdd -> {

    };
    private boolean soundOff;


    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, final String resultDesc, int groupId) {
            logger.info("主动方请求组呼的消息：" + methodResult + "-------" + resultDesc);
            logger.info("主动方请求组呼的消息：" + MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode());

            speechSynthesizer.pause();//停止读出组名

            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {

                if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
                    myHandler.post(() -> {
                        myHandler.removeMessages(1);
                        timeProgress = 60;
                        talkback_time_progress.setText(String.valueOf(timeProgress));
                        myHandler.sendEmptyMessageDelayed(1, 1000);
                        if (PhoneAdapter.isF25()) {
                            allViewDefault();
                            TextViewCompat.setTextAppearance(tv_speak_text_me, R.style.red);
                        }
                        if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                            ll_pre_speaking.setVisibility(View.GONE);
                        }
                        change2Speaking();
                        MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                        setViewEnable(false);
                        setCurrentGroupScanView(groupId);
                        tx_ptt_group_name.setText(DataUtil.getGroupName(groupId));
                    });
                } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {//响应组为禁用状态，低级用户无法组呼

                    ToastUtil.showToast(getContext(), resultDesc);
                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
                    if (!groupByGroupNo.isHighUser()) {
                        myHandler.post(() -> change2Forbid());
                    } else {
                        myHandler.post(() -> change2Silence());
                    }
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    myHandler.post(() -> {
                        ToastUtil.showToast(context, getString(R.string.cannot_talk));
                        change2Silence();
                    });
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    myHandler.post(() -> change2Waiting());
                } else {
                    myHandler.post(() -> {
                        if (MyApplication.instance.getGroupListenenState() != LISTENING) {
                            change2Silence();
                        } else {
                            isScanGroupCall = false;
                            change2Listening();
                        }
                    });
                }
            } else {
                ToastUtil.toast((Activity) context, resultDesc);
                myHandler.post(() -> {
                    if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.LISTENING) {
                        change2Silence();
                    } else {
                        change2Listening();
                    }
                });

            }

        }
    };
    private IGotaKeyMonitor keyMointor;
    private IGotaKeyHandler gotaKeyHandler;
    private boolean isScanGroupCall;//是否扫描组在组呼
    private ImageView iv_group_call_bg;
    private RelativeLayout rl_group_call;
    private TextView tx_ptt_time;
    private TextView tx_ptt_group_name;
    private RelativeLayout rl_uav_push;

    public void setViewEnable(boolean isEanble) {
        to_current_group.setEnabled(isEanble);
        talkback_change_session.setEnabled(isEanble);
        iv_volume_off_call.setEnabled(isEanble);
    }

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> {
                setCurrentGroupView();//当前的组和文件夹名字重置
                setVideoIcon();
//                setScanGroupIcon();
                if (groupScanId != 0 && MyApplication.instance.getGroupListenenState() == LISTENING) {
                    setCurrentGroupScanView(groupScanId);
                }
            });
        }
    };

    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            myHandler.post(() -> {
                if (isAdd && isLocked) {
                    //加入临时租，被锁定
                    MyApplication.instance.isLocked = true;
                }
                if (!isAdd) {
                    MyApplication.instance.isLocked = false;
                }
            });
        }
    };

    /**
     * 强制切组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {

        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            logger.info("TalkbackFragment收到强制切组： toGroupId：" + toGroupId);
            myHandler.post(() -> {
                setCurrentGroupView();
                if ((!MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false) && !MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false)) || MyApplication.instance.getGroupListenenState() != LISTENING) {
                    change2Silence();
                }
            });
        }
    };

    /**
     * 转组消息
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {

        @Override
        public void handler(final int errorCode, final String errorDesc) {
            logger.info("转组消息：" + "errorCode:" + errorCode + "/" + errorDesc);
            if (errorCode != 0 && errorCode != SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                myHandler.post(() -> {
                    if (errorCode == SignalServerErrorCode.RESOURCES_NOT_ENOUGH.getErrorCode()) {
                        ToastUtil.showToast(MyApplication.instance, getString(R.string.text_no_radio_resources_available));
                    } else if (errorCode == SignalServerErrorCode.TEMP_GROUP_LOCKED.getErrorCode()) {
                        ToastUtil.showToast(MyApplication.instance, SignalServerErrorCode.TEMP_GROUP_LOCKED.getErrorDiscribe());
                    } else {
                        ToastUtil.showToast(MyApplication.instance, errorDesc + "");
                    }
                });

            } else {
                int currentGroup = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                CommonGroupUtil.setCatchGroupIdList(currentGroup);

                myHandler.post(() -> {
                    setCurrentGroupView();
                    setPttText();
                });
            }
        }
    };
    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {

        @Override
        public void handler(int reasonCode) {
            myHandler.post(() -> {
                groupScanId = 0;
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                setCurrentGroupView();
                logger.info("被动方停止组呼" + "/" + MyApplication.instance.getGroupListenenState() + MyApplication.instance.isPttPress);
                if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                    if (MyApplication.instance.isPttPress && MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.IDLE) {
                        //别人停止组呼，如果自己还是在按着，重新请求组呼
                        change2Speaking();
                    } else {
                        change2Silence();
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
        public void handler(int memberId, String memberName, int groupId, String groupName,CallMode currentCallMode, long uniqueNo) {
            logger.info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + "curreneCallMode " + currentCallMode + "-----" + MyApplication.instance.getGroupSpeakState());
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                ToastUtil.showToast(activity, getString(R.string.text_has_no_group_call_listener_authority));
            }
            myHandler.post(() -> {
                //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                if (groupId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                    isScanGroupCall = true;
                    groupScanId = groupId;
                    setCurrentGroupScanView(groupId, groupName);
                }
                //是当前组的组呼,且扫描组有人说话，变文件夹和组名字
                if (groupId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) && MyApplication.instance.getGroupListenenState() == LISTENING) {
                    isScanGroupCall = false;
                    setCurrentGroupScanView(groupId, groupName);
                }
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
            });

            speakingId = groupId;
            speakingName = memberName;

            if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
                myHandler.post(() -> {
                    if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING || MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
                        change2Waiting();
                    } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                        //什么都不用做
                    } else {
                        change2Listening();
                    }
                });
            }
        }
    };
    private int speakingId;
    private String speakingName;

    /**
     * 组扫描的消息,只改变界面
     */
    private ReceiveGroupScanResultHandler receiveGroupScanResultHandler = new ReceiveGroupScanResultHandler() {
        @Override
        public void handler(final int groupScanType, final boolean enable, int errorCode, String errorDesc) {
            myHandler.post(() -> {
                if (groupScanType == GroupScanType.GROUP_SCANNING.getCode()
                        && MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_SCAN.name())) {//组扫描
//                    if (enable) {//打开组扫描Icon
//                        setChangeGroupScan(true);
//                    } else {//关闭组扫描Icon
//                        setChangeGroupScan(false);
//                    }
                }
            });

        }
    };

    /**
     * 环境监听是否打开
     */
    private ReceiveNotifyEnvironmentMonitorHandler receiveNotifyEnvironmentMonitorHandler = new ReceiveNotifyEnvironmentMonitorHandler() {
        @Override
        public void handler(final boolean enable) {
            myHandler.post(() -> {
                if (enable) {
                    iv_environment_monitor.setVisibility(View.VISIBLE);

                } else {
                    iv_environment_monitor.setVisibility(View.GONE);
                }
            });
        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {

        @Override
        public void handler(final boolean connected) {
            logger.info("主fragment收到服务是否连接的通知----->" + connected);
            myHandler.post(() -> setCurrentGroupView());
        }
    };

    /**
     * 是否静音状态
     */
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(final boolean isVolumeOff, int status) {
            logger.info("是否静音的状态：receiveVolumeOffCallHandler " + isVolumeOff);
            if (isVolumeOff) {
                iv_volume_off_call.setImageResource(R.drawable.volume_off_call);
                soundOff = true;
            } else {
                iv_volume_off_call.setImageResource(R.drawable.volume_silence);
                soundOff = false;
            }
        }
    };

    private ReceiveUpdateDepGroupHandler receiveUpdateDepGroupHandler = new ReceiveUpdateDepGroupHandler(){
        @Override
        public void handler(List<GroupBean> groupList){
            myHandler.post(()-> setChangeGroupView(groupList));
        }
    };

    /**
     * 登陆响应的消息
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = new ReceiveLoginResponseHandler() {
        @Override
        public void handler(int resultCode, String resultDes) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                myHandler.post(() -> {
                    //当前文件夹、组数据的显示设置
                    setCurrentGroupView();
                });
            }
        }
    };

    /**
     * 更新所有数据信息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = new ReceiveUpdateAllDataCompleteHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                myHandler.post(() -> {
                    //当前文件夹、组数据的显示设置
                    setCurrentGroupView();
                });
            }
        }
    };

    /**
     * 接收到绑定/解绑的结果
     */
   private ReceiveNotifyZfyBoundPhoneMessageHandler receiveNotifyZfyBoundPhoneMessageHandler = new ReceiveNotifyZfyBoundPhoneMessageHandler(){
        @Override
        public void handler(boolean isBound,boolean isShow) {
            if(!isBound){
                if (isShow) {
                   ToastUtil.showToast(getContext(),getString(R.string.text_unbind_success));
                 }
                myHandler.post(() -> rlBind.setVisibility(View.GONE));
            }else{
                if(isShow){
                   ToastUtil.showToast(getContext(),getString(R.string.text_bind_success));
                }
                myHandler.post(() -> rlBind.setVisibility(View.VISIBLE));
           }
        }
   };

    /**
     * 手势滑动监听器
     */
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            logger.info("onFling:" + (e1.getY() - e2.getY()) + "/" + (e2.getY() - e1.getY()));
            if (!MyApplication.instance.isPttPress && Math.abs(e1.getY() - e2.getY()) < 50) {
                int verticalMinDistance = 10;
                if (e1.getX() - e2.getX() > verticalMinDistance) {
                    if (MyApplication.instance.isLocked) {
                        ToastUtil.showToast(context, context.getString(R.string.group_locked_can_not_change_group));
                    } else {
                        change_group_view.addLeft(1);
                    }
                    MyApplication.instance.isMoved = true;
                } else if (e2.getX() - e1.getX() > verticalMinDistance) {
                    if (MyApplication.instance.isLocked) {
                        ToastUtil.showToast(context, context.getString(R.string.group_locked_can_not_change_group));
                    } else {
                        change_group_view.addRight(1);
                    }
                    MyApplication.instance.isMoved = true;
                }
            }
            return true;
        }
    };

    /**
     * 上下滑动改变音量
     */
    private final class OnTouchListenerImpChengeVolume implements OnTouchListener {
        private float downY;
        private int downCurrentVolumeC;
        private float downX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = event.getY();
                    downX = event.getX();
                    downCurrentVolumeC = MyTerminalFactory.getSDK().getAudioProxy().getVolume();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = event.getY();
                    float moveX = event.getX();
                    //位移距离
                    float disY = moveY - downY;
                    float disX = moveX - downX;
                    logger.info("位移距离X：" + Math.abs(disX));
                    logger.info("位移距离Y：" + Math.abs(disY));

                    if (Math.abs(disY) > Math.abs(disX)) {

                        //手指在屏幕上划过距离百分比 = 划过距离/屏幕高度
                        float disPercent = -disY / (getResources().getDisplayMetrics().heightPixels / 3);
                        //偏移音量 = 手指在屏幕上划过距离百分比*最大音量
                        float disVolumeC = disPercent * IAudioProxy.VOLUME_MAX;
                        int endVolumeC = ((int) (downCurrentVolumeC + disVolumeC)) / IAudioProxy.VOLUME_STEP * IAudioProxy.VOLUME_STEP;
                        if (endVolumeC > IAudioProxy.VOLUME_MAX) {
                            endVolumeC = IAudioProxy.VOLUME_MAX;
                        } else if (endVolumeC < 0) {
                            endVolumeC = 0;
                        }

                        int screenWidth = getResources().getDisplayMetrics().widthPixels;
                        if (moveX < screenWidth / 3 || moveX > 2 * screenWidth / 3) {// 左右两侧进行通话音量的设置
                            MyTerminalFactory.getSDK().getAudioProxy().setVolume(endVolumeC);

                            if (endVolumeC == 0) {
                                iv_volume_off_call.setImageResource(R.drawable.volume_off_call);
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                            } else {
                                iv_volume_off_call.setImageResource(R.drawable.volume_silence);
                                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                            }


                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    myHandler.postDelayed(() -> ll_sliding_chenge_volume.setVisibility(View.GONE), 1000);

                    break;
                default:
                    break;
            }
            return true;
        }
    }


    private final class OnGroupChangedListenerImplementation implements OnGroupChangedListener {
        @Override
        public void onGroupChanged(final int groupId, String groupName) {
            myHandler.post(() -> {
                if(groupId>0){
                    MyTerminalFactory.getSDK().getGroupManager().changeGroup(groupId);
                }else{
                    ToastUtil.showToast(context,getString(R.string.text_change_group_data_wrong));
                }
            });
        }
    }


    /**
     * PTT按下时不可切换
     */
    private ReceiveCallingCannotClickHandler receiveCallingCannotClickHandler = new ReceiveCallingCannotClickHandler() {

        @Override
        public void handler(final boolean isCannotCheck) {
            myHandler.post(() -> {
                logger.info("change_group_show_area被禁了 ？ isCannotCheck：" + isCannotCheck);
                change_group_show_area.setEnabled(!isCannotCheck);
                if (!isCannotCheck) {
                    talkback_add_icon.setEnabled(true);
                }
            });
        }
    };

    public void setPttCurrentState(GroupCallSpeakState groupCallSpeakState) {
        change2CurrentPttStatus(groupCallSpeakState);
    }

    private synchronized void change2CurrentPttStatus(GroupCallSpeakState groupCallSpeakState) {
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            if (groupCallSpeakState == GroupCallSpeakState.IDLE) {
                change2Silence();
            } else if (groupCallSpeakState == GroupCallSpeakState.GRANTING) {
                change2PreSpeaking();
            } else if (groupCallSpeakState == GroupCallSpeakState.GRANTED) {
                change2Speaking();
                setCurrentGroupView();
            } else if (groupCallSpeakState == GroupCallSpeakState.WAITING) {
                isScanGroupCall = false;
                change2Listening();//等待状态时，上方仍然显示某人说话的文字
            }
        }

    }

    float downX;

    /**
     * 触摸事件
     */
    private final class OnTouchListenerImplementation implements OnTouchListener {


        @Override
        public boolean onTouch(View v, MotionEvent event) {

//            mGestureDetector.onTouchEvent(event);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    synchronized (MyApplication.instance) {
                        logger.info("------down------" + MyApplication.instance.isChanging);
                        MyApplication.instance.isChanging = true;
                    }
                    MyApplication.instance.isMoved = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    logger.info("滑动切组控件-----up cancel事件--------isMoved：" + MyApplication.instance.isMoved);
                    if (!MyApplication.instance.isMoved) {
                        float upX = event.getRawX();
                        if (upX - downX > 5) {
                            change_group_view.addRight(1);
                        } else if (downX - upX > 5) {
                            change_group_view.addLeft(1);
                        }
                        synchronized (MyApplication.instance) {
                            logger.info("--up---" + MyApplication.instance.isChanging);
                            MyApplication.instance.isChanging = false;
                            MyApplication.instance.notifyAll();
                        }
                    }

                    break;

                default:
                    break;
            }

            startTimerToLock();


            return true;
        }

    }

    private final class OnPttTouchListenerImplementation implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MyApplication.instance.isPttPress = true;
                    logger.error("ACTION_DOWN，ptt按钮按下，开始组呼：" + MyApplication.instance.folatWindowPress + MyApplication.instance.volumePress);
                    if (!MyApplication.instance.folatWindowPress && !MyApplication.instance.volumePress) {

                        pttDownDoThing();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    MyApplication.instance.isPttPress = false;
                    logger.error("ACTION_UP，ACTION_CANCEL，ptt按钮抬起，停止组呼：" + MyApplication.instance.isPttPress);
                    pttUpDoThing();
                    break;

                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 语音合成监听器
     */
    private SpeechSynthesizerListener speechSynthesizerListener = new SpeechSynthesizerListener() {

        @Override
        public void onBufferProgressChanged(SpeechSynthesizer synthesizer, int arg1) {
        }

        @Override
        public void onCancel(SpeechSynthesizer synthesizer) {
        }

        @Override
        public void onError(SpeechSynthesizer synthesizer, SpeechError error) {
        }

        @Override
        public void onNewDataArrive(SpeechSynthesizer synthesizer, byte[] audioData, boolean isLastData) {
        }

        @Override
        public void onSpeechFinish(SpeechSynthesizer synthesizer) {
        }

        @Override
        public void onSpeechPause(SpeechSynthesizer synthesizer) {
        }

        @Override
        public void onSpeechProgressChanged(SpeechSynthesizer synthesizer, int arg1) {
        }

        @Override
        public void onSpeechResume(SpeechSynthesizer synthesizer) {
        }

        @Override
        public void onSpeechStart(SpeechSynthesizer synthesizer) {
        }

        @Override
        public void onStartWorking(SpeechSynthesizer synthesizer) {
        }

        @Override
        public void onSynthesizeFinish(SpeechSynthesizer synthesizer) {
        }
    };
    private int online_number;

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            int mainGroupId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);
            MyTerminalFactory.getSDK().getGroupManager().changeGroup(mainGroupId);
        }
    };
    /**
     * 成员信息改变
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {

        @Override
        public void handler(MemberChangeType memberChangeType) {
            logger.info("触发了receiveNotifyMemberChangeHandler：" + memberChangeType);
            online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
            if (memberChangeType == MemberChangeType.MEMBER_ACTIVE_GROUP_CALL) {
                myHandler.post(() -> change2Silence());

            } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
                myHandler.post(() -> change2Forbid());
            }
            myHandler.post(() -> tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number)));


        }
    };

    private ReceivePTTUpHandler receivePTTUpHandler = new ReceivePTTUpHandler() {
        @Override
        public void handler() {
            logger.info("ppt.触发了ReceivePTTUpHandler");
            //手雷上的抬起和按下，通知界面当作屏幕button抬起按下一样处理
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
            myHandler.post(() -> {
                MyApplication.instance.isClickVolumeToCall = false;
                if (MyApplication.instance.getGroupListenenState() == LISTENING) {
                    isScanGroupCall = false;
                    change2Listening();
                } else {
                    change2Silence();
                }
            });
        }
    };
    private ReceivePTTDownHandler receivePTTDownHandler = new ReceivePTTDownHandler() {
        @Override
        public void handler(int requestGroupCall) {
            logger.info("ppt.触发了receivePTTDownHandler");
            if (requestGroupCall == BaseCommonCode.SUCCESS_CODE) {
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
                myHandler.post(() -> {
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                        CheckMyPermission.permissionPrompt((NewMainActivity) context, Manifest.permission.RECORD_AUDIO);
                        return;
                    }
                    change2PreSpeaking();
                    MyApplication.instance.isClickVolumeToCall = true;
                });
            } else if (requestGroupCall == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
                change2Waiting();
            } else {//组呼失败的提示
                ToastUtil.groupCallFailToast(context, requestGroupCall);
            }
        }
    };

    /**
     * 设置音量键为ptt键时的监听
     */
    private final class OnPTTVolumeBtnStatusChangedListenerImp implements BaseActivity.OnPTTVolumeBtnStatusChangedListener {
        @Override
        public void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState) {
            if (groupCallSpeakState == IDLE) {
                //半双工个呼、视频观看中不能组呼
                pttDownDoThing();
            } else {
                myHandler.removeMessages(1);
                pttUpDoThing();
            }
        }
    }

    /**
     * 取消和监听通知
     */
    private ReceiveSetMonitorGroupViewHandler receiveSetMonitorGroupViewHandler = new ReceiveSetMonitorGroupViewHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> {

            });
        }
    };

    /**
     * 收到测试组呼
     */
    private ReceiveTestGroupCallHandler receiveTestGroupCallHandler = new ReceiveTestGroupCallHandler() {
        @Override
        public void handler(boolean isStart) {
            myHandler.post(() -> {
                if(isStart){
                    //开始组呼
                    pttDownDoThing();
                }else{
                    //结束组呼
                    pttUpDoThing();
                }
            });
        }
    };



    //GH880手机PTT按钮事件
    public class GotaKeHandler extends IGotaKeyHandler.Stub {

        @Override
        public void onPTTKeyDown() throws RemoteException {
            myHandler.post(() -> {
                try {
                    pttDownDoThing();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }

        @Override
        public void onPTTKeyUp() throws RemoteException {
            myHandler.post(() -> {
                try {
                    pttUpDoThing();
                } catch (Exception e) {
                    e.printStackTrace();
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

    Activity activity;

    public TalkbackFragment() {
    }

    public TalkbackFragment(Activity activity) {
        this.activity = activity;
    }


    ImageView talkback_change_session;

    ImageView to_current_group;

    Button ptt;


    LinearLayout ll_show_area;//主页上半部分总布局

    TextView talkback_time_progress;//时间进度数字

    LinearLayout ll_status_bar;//最上边四个状态显示

    ImageView iv_volume_off_call;//听筒静音


    ImageView iv_open_group_scan;//组扫描

    TextView tv_group_scan;

    ImageView iv_environment_monitor;//环境监听


    LinearLayout ll_scanGroup_speak;//组扫描时，说话时的布局

    TextView tv_scanGroup_speak;//组扫描时，说话时的组

    LinearLayout ll_folder;//文件夹

    TextView tv_current_folder;//当前文件夹名

    TextView tv_current_online;


    TextView tv_current_group;//当前组名

    ImageView talkback_add_icon;

    LinearLayout ll_speak_state;//当前语音状态

    LinearLayout ll_speaking;//我正在说话

    TextView tv_speak_text_me;

    LinearLayout ll_listening;//别人在说话

    TextView incomming_call_current_speaker;//说话人名字

    LinearLayout ll_pre_speaking;//准备说话

    LinearLayout ll_silence;//静默状态

    LinearLayout ll_forbid;//禁言禁听状态

    LinearLayout ll_waiting;//排队中状态


    LinearLayout change_group_show_area;//转组区域

    ChangeGroupView change_group_view;//转组控件


    LinearLayout ll_sliding_chenge_volume;

    ImageView iv_volume_fw;

    TextView tv_volume_fw;

    RelativeLayout title_bar;

    //解绑布局
    RelativeLayout rlBind;
    //    private GestureDetector mGestureDetector;
    private Logger logger = Logger.getLogger(getClass());
    private SpeechSynthesizer speechSynthesizer;
    private int groupScanId;
    private boolean isFlex = false;
    private TimerTask timerTaskLock;


    @Override
    public int getContentViewId() {
        return R.layout.fragment_main_talkback;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void initView() {

        //电源键监听
        title_bar = (RelativeLayout) mRootView.findViewById(R.id.title_bar);
        tv_volume_fw = (TextView) mRootView.findViewById(R.id.tv_volume_fw);
        iv_volume_fw = (ImageView) mRootView.findViewById(R.id.iv_volume_fw);
        ll_sliding_chenge_volume = (LinearLayout) mRootView.findViewById(R.id.ll_sliding_chenge_volume);
        change_group_view = (ChangeGroupView) mRootView.findViewById(R.id.change_group_view);
        change_group_show_area = (LinearLayout) mRootView.findViewById(R.id.change_group_show_area);
        ll_waiting = (LinearLayout) mRootView.findViewById(R.id.ll_waiting);
        ll_forbid = (LinearLayout) mRootView.findViewById(R.id.ll_forbid);
        ll_silence = (LinearLayout) mRootView.findViewById(R.id.ll_silence);
        ll_pre_speaking = (LinearLayout) mRootView.findViewById(R.id.ll_pre_speaking);
        incomming_call_current_speaker = (TextView) mRootView.findViewById(R.id.incomming_call_current_speaker);
        ll_listening = (LinearLayout) mRootView.findViewById(R.id.ll_listening);
        tv_speak_text_me = (TextView) mRootView.findViewById(R.id.tv_speak_text_me);
        ll_speaking = (LinearLayout) mRootView.findViewById(R.id.ll_speaking);
        ll_speak_state = (LinearLayout) mRootView.findViewById(R.id.ll_speak_state);
        talkback_add_icon = (ImageView) mRootView.findViewById(R.id.talkback_add_icon);
        tv_current_group = (TextView) mRootView.findViewById(R.id.tv_current_group);
        tv_current_online = (TextView) mRootView.findViewById(R.id.tv_current_online);
        tv_current_folder = (TextView) mRootView.findViewById(R.id.tv_current_folder);
        ll_folder = (LinearLayout) mRootView.findViewById(R.id.ll_folder);
        tv_scanGroup_speak = (TextView) mRootView.findViewById(R.id.tv_scanGroup_speak);
        ll_scanGroup_speak = (LinearLayout) mRootView.findViewById(R.id.ll_scanGroup_speak);
        iv_environment_monitor = (ImageView) mRootView.findViewById(R.id.iv_environment_monitor);
        tv_group_scan = (TextView) mRootView.findViewById(R.id.tv_group_scan);
        iv_open_group_scan = (ImageView) mRootView.findViewById(R.id.iv_open_group_scan);
        iv_volume_off_call = (ImageView) mRootView.findViewById(R.id.iv_volume_off_call);
        ll_status_bar = (LinearLayout) mRootView.findViewById(R.id.ll_status_bar);
        talkback_time_progress = (TextView) mRootView.findViewById(R.id.talkback_time_progress);
        ll_show_area = (LinearLayout) mRootView.findViewById(R.id.ll_show_area);
        ptt = (Button) mRootView.findViewById(R.id.ptt);
        to_current_group = (ImageView) mRootView.findViewById(R.id.to_current_group);
        talkback_change_session = (ImageView) mRootView.findViewById(R.id.talkback_change_session);
        rlBind = (RelativeLayout) mRootView.findViewById(R.id.rl_bind);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        iv_group_call_bg = mRootView.findViewById(R.id.iv_group_call_bg);
        rl_group_call = mRootView.findViewById(R.id.rl_group_call);
        tx_ptt_time = mRootView.findViewById(R.id.tx_ptt_time);
        rl_uav_push = mRootView.findViewById(R.id.rl_uav_push);
        tx_ptt_group_name = mRootView.findViewById(R.id.tx_ptt_group_name);

        getContext().registerReceiver(mBatInfoReceiver, filter);
        //GH880手机按键服务
        keyMointor = (IGotaKeyMonitor) context.getSystemService("gotakeymonitor");

        getActivity().registerReceiver(mbtBroadcastReceiver, makeGattUpdateIntentFilter());
//        mGestureDetector = new GestureDetector(context, gestureListener);
        iv_group_call_bg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE){
                    pttUpDoThing();
                    int oldGroupId = TerminalFactory.getSDK().getParam(Params.OLD_CURRENT_GROUP_ID, 0);
                    TerminalFactory.getSDK().putParam(Params.CURRENT_GROUP_ID,oldGroupId);
                }else{
                    int currentGroup = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    TerminalFactory.getSDK().putParam(Params.OLD_CURRENT_GROUP_ID,currentGroup);
                    //市局宽带组111
                    TerminalFactory.getSDK().putParam(Params.CURRENT_GROUP_ID,72088905);
                    pttDownDoThing();
                }
            }
        });
        iv_volume_off_call.setVisibility(View.VISIBLE);
        iv_volume_off_call.setImageResource(BitmapUtil.getVolumeImageResourceByValue(true));
        iv_volume_off_call.setOnClickListener(view -> {
            if (!soundOff) {
                iv_volume_off_call.setImageResource(R.drawable.volume_off_call);
                MyTerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                soundOff = true;
            } else {
                iv_volume_off_call.setImageResource(R.drawable.volume_silence);
                MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                soundOff = false;
            }
        });


        if (MyTerminalFactory.getSDK().getParam(Params.ENVIRONMENT_MONITOR, false)) {
            iv_environment_monitor.setVisibility(View.VISIBLE);
        } else {
            iv_environment_monitor.setVisibility(View.GONE);
        }
//        if (MyTerminalFactory.getSDK().isRegisted()) {
        setCurrentGroupView();
//        } else {
//            waitAndFinish();
//        }
        setChangeGroupView(MyDataManager.getDepAllGroup());

        speechSynthesizer = new SpeechSynthesizer(context, "holder", speechSynthesizerListener);
        // 此处需要将setApiKey方法的两个参数替换为你在百度开发者中心注册应用所得到的apiKey和secretKey
        speechSynthesizer.setApiKey("Bu2YElm9bEgA58WsSipd0HWk", "725e212c5f6330568fbf7df06db37416");
        speechSynthesizer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        ((NewMainActivity) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            isScanGroupCall = false;
            change2Listening();
        }
        //警务通和执法记录仪的绑定关系
        boolean isBind = MyTerminalFactory.getSDK().getParam(Params.RECORDER_BIND_STATE, false);
        rlBind.setVisibility(isBind?View.VISIBLE:View.GONE);

        //测试组呼
        mRootView.findViewById(R.id.tv_test_group_call).setOnClickListener(v -> {
            Intent intent = new Intent(context, TestGroupCallService.class);
            context.startService(intent);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initListener() {
        change_group_show_area.setOnTouchListener(new OnTouchListenerImplementation());
        change_group_view.setOnGroupChangedListener(new OnGroupChangedListenerImplementation());
        ll_show_area.setOnTouchListener(new OnTouchListenerImpChengeVolume());
        rlBind.setOnClickListener(v -> new UnbindDialog(getActivity(), "", () ->
                TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getRecorderBindManager().requestUnBind())).show());
        if (keyMointor != null) {
            try {
                gotaKeyHandler = keyMointor.setHandler(new GotaKeHandler());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //注册系统音量改变的广播接受者
//        context.registerReceiver(talkbackVolumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCallingCannotClickHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateDepGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyEnvironmentMonitorHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupScanResultHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupByNoHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUnreadMessageAdd1Handler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyZfyBoundPhoneMessageHandler);
        ptt.setOnTouchListener(new OnPttTouchListenerImplementation());
        talkback_change_session.setOnClickListener(v -> GroupCallNewsActivity.startCurrentActivity(context, MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0), DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)), speakingId, speakingName));
        to_current_group.setOnClickListener(view -> {
            if (DataUtil.isExistGroup(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0))) {
                Intent intent = new Intent(MyApplication.instance, GroupMemberActivity.class);
                intent.putExtra("groupId", MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
                intent.putExtra("groupName", DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
                startActivity(intent);
            } else {
                ToastUtil.showToast(context, getString(R.string.text_unkown_error_member_not_in_this_group));
            }
        });

        rl_uav_push.setOnClickListener(v -> {
            int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0);
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,
                    MyDataUtil.getPushInviteMemberData(currentGroupId, ReceiveObjectMode.GROUP.toString()) ,true);
        });
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupViewHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveTestGroupCallHandler);

        ((BaseActivity) context).setOnPTTVolumeBtnStatusChangedListener(new OnPTTVolumeBtnStatusChangedListenerImp());

    }

    @Override
    public void initData() {

        // 如果是F25机型，则将PTT按键隐藏
        if (PhoneAdapter.isF25()) {
//            view_pager.setVisibility(View.GONE);
        }

        MyApplication.instance.isPttViewPager = true;
        online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        startTimerToLock();
        setVideoIcon();//设置视频回传上报相关图标
        setPttText();
        String type = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        if(android.text.TextUtils.equals(type, TerminalMemberType.TERMINAL_UAV.name())){
            rl_uav_push.setVisibility(View.VISIBLE);
        }else {
            rl_uav_push.setVisibility(View.GONE);
        }
//        setScanGroupIcon();//设置组扫描相关图标
    }

    private void setPttText() {
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
        //响应组  普通用户  不在响应状态
        if (ResponseGroupType.RESPONSE_TRUE.toString().equals(groupByGroupNo.getResponseGroupType()) &&
                !groupByGroupNo.isHighUser() &&
                !TerminalFactory.getSDK().getGroupCallManager().getActiveResponseGroup().contains(currentGroupId)) {
            change2Forbid();
        } else if (MyApplication.instance.getGroupListenenState() != GroupCallListenState.IDLE) {
            change2Listening();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTING) {
            change2PreSpeaking();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.WAITING) {
            change2Waiting();
        } else if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
            change2Speaking();
        } else {
            change2Silence();
        }
    }

    private void setScanGroupIcon() {
        if (MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false) && MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_SCAN.name())) {
            setChangeGroupScan(true);
        } else {
            setChangeGroupScan(false);
        }
    }

    private void setVideoIcon() {
        MyTopRightMenu.offerObject().initview(talkback_add_icon, (BaseActivity) getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 40, 0);
        talkback_add_icon.setVisibility(View.VISIBLE);

    }

    private void setCurrentGroupView() {
        int currentGroupNo = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        String groupName = DataUtil.getGroupName(currentGroupNo);
        String groupDepartmentName = DataUtil.getGroupDepartmentName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
        if (android.text.TextUtils.isEmpty(groupName) || android.text.TextUtils.isEmpty(groupDepartmentName)) {
            TerminalFactory.getSDK().getDataManager().getGroupByNo(currentGroupNo);
        } else {
            tv_current_group.setText(groupName);
            tv_current_folder.setText(groupDepartmentName);
        }
        online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
    }

    public void setChangeGroupScan(boolean bool) {
        if (bool) {
            iv_open_group_scan.setVisibility(View.VISIBLE);
            tv_group_scan.setVisibility(View.VISIBLE);
        } else {
            iv_open_group_scan.setVisibility(View.GONE);
            tv_group_scan.setVisibility(View.GONE);
        }
    }

    private void setCurrentGroupScanView(final int groupId) {
        tv_current_group.setText(DataUtil.getGroupName(groupId));
        tv_current_folder.setText(DataUtil.getGroupDepartmentName(groupId));
    }

    private void setCurrentGroupScanView(final int groupId, String groupName) {
        String name = DataUtil.getGroupName(groupId);
        if (android.text.TextUtils.isEmpty(name)) {
            tv_current_group.setText(groupName);
            tv_current_folder.setText(getString(R.string.text_temporary_group));
        } else {
            tv_current_group.setText(name);
            tv_current_folder.setText(DataUtil.getGroupDepartmentName(groupId));
        }
    }

    private void setChangeGroupView(List<GroupBean> groupList) {
        if (groupList.size() > 0 && null != change_group_view) {
            change_group_view.setData(groupList, MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
        }
    }

    /**
     * Silence 沉默、无声状态
     */
    private void change2Silence() {
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            return;
        }
        online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
        layoutDefault();
        ll_show_area.setVisibility(View.VISIBLE);
        allViewDefault();
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        change_group_show_area.setVisibility(View.VISIBLE);
        talkback_time_progress.setVisibility(View.GONE);
        if (!GroupUtils.currentIsForbid()) {
            ll_silence.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER))) {
                ll_listening.setVisibility(View.GONE);
                incomming_call_current_speaker.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
            }
            //只有当前组不是禁呼的才恢复PPT的状态
            ptt.setText(R.string.press_blank_space_talk_text);
            TextViewCompat.setTextAppearance(ptt, R.style.pttSilenceText);
            ptt.setBackgroundResource(R.drawable.ptt_silence);
            ptt.setEnabled(true);
//        talkback_add_icon.setEnabled(true);
        }
    }

    /**
     * 等待
     */
    private void change2Waiting() {
        layoutDefault();
        ll_show_area.setVisibility(View.VISIBLE);
        allViewDefault();
        ll_waiting.setVisibility(View.VISIBLE);
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        logger.info("ptt.change2Waiting准备说话");
        ll_pre_speaking.setVisibility(View.VISIBLE);
        ptt.setBackgroundResource(R.drawable.ptt_pre_speaking);
        ptt.setText(R.string.text_ready_to_speak);
        TextViewCompat.setTextAppearance(ptt, R.style.pttPreSpeakText);
        ptt.setEnabled(true);
    }

    /**
     * 准备说话
     */
    private void change2PreSpeaking() {
        logger.info("ptt.change2PreSpeaking()准备说话");
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {

            return;
        }
        layoutDefault();
        ll_show_area.setVisibility(View.VISIBLE);
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        allViewDefault();
        if (MyApplication.instance.getGroupListenenState() != LISTENING) {
            ll_pre_speaking.setVisibility(View.VISIBLE);
        } else {
            ll_pre_speaking.setVisibility(View.GONE);
        }

        ptt.setBackgroundResource(R.drawable.ptt_pre_speaking);
        ptt.setText(R.string.text_ready_to_speak);
        TextViewCompat.setTextAppearance(ptt, R.style.pttPreSpeakText);
        ptt.setEnabled(true);
    }

    /**
     * 开始说话
     */
    private void change2Speaking() {
        talkback_add_icon.setEnabled(false);
        logger.info("change2Speaking");
        allViewDefault();
        ll_speaking.setVisibility(View.VISIBLE);
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        change_group_show_area.setVisibility(View.GONE);
        talkback_time_progress.setVisibility(View.VISIBLE);
        ptt.setBackgroundResource(R.drawable.ptt_speaking);
        ptt.setText(R.string.button_release_end);
        TextViewCompat.setTextAppearance(ptt, R.style.pttSpeakingText);
        logger.info("主界面，ptt被禁 ？  isClickVolumeToCall：" + MyApplication.instance.isClickVolumeToCall);
        ptt.setEnabled(!MyApplication.instance.isClickVolumeToCall);
        if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
            MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
        }
    }

    /**
     * 禁止组呼
     */
    private void change2Forbid() {
        logger.info("ptt.change2Forbid()按住排队");
        layoutDefault();
        ll_show_area.setVisibility(View.VISIBLE);
        allViewDefault();
        ll_forbid.setVisibility(View.VISIBLE);
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));
        ptt.setText(R.string.text_no_group_calls);
        TextViewCompat.setTextAppearance(ptt, R.style.pttWaitingText);
        ptt.setBackgroundResource(R.drawable.ptt_listening);
        logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        ptt.setEnabled(false);
        if (MyApplication.instance.isPttPress) {
            pttUpDoThing();
        }
    }

    /**
     * 听
     */
    private void change2Listening() {
        layoutDefault();
        ll_show_area.setVisibility(View.VISIBLE);
        allViewDefault();
//        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        String speakMemberName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
        if (!TextUtils.isEmpty(speakMemberName)) {
            //设置说话人名字,在组呼来的handler中设置
            ll_listening.setVisibility(View.VISIBLE);
            incomming_call_current_speaker.setText(speakMemberName);
        }
        tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number));

        if (isScanGroupCall) {
            if (GroupUtils.currentIsForbid()) {
                //如果当前组是禁呼的，不需要改变PPT的样式
                return;
            }
            logger.info("扫描组在组呼");
            ptt.setText(R.string.press_blank_space_talk_text);
            TextViewCompat.setTextAppearance(ptt, R.style.pttSilenceText);
            ptt.setBackgroundResource(R.drawable.ptt_silence);
        } else {
            ptt.setText(R.string.button_press_to_line_up);
            TextViewCompat.setTextAppearance(ptt, R.style.pttWaitingText);
            ptt.setBackgroundResource(R.drawable.ptt_listening);
            logger.info("主界面，ptt被禁了  isPttPress：" + MyApplication.instance.isPttPress);
        }
    }

    private void allViewDefault() {
        if (ll_silence != null) {
            ll_speaking.setVisibility(View.GONE);
            ll_silence.setVisibility(View.GONE);
            ll_pre_speaking.setVisibility(View.GONE);
            ll_listening.setVisibility(View.GONE);
            ll_forbid.setVisibility(View.GONE);
            ll_waiting.setVisibility(View.GONE);
        }
    }

    private int timeProgress;


    private void layoutDefault() {
        ll_show_area.setVisibility(View.GONE);
        talkback_time_progress.setVisibility(View.GONE);
    }

    private void waitAndFinish() {
        ToastUtil.showToast(context, getString(R.string.text_system_exception_wait_one_seconds_closed_please_restart));
        myHandler.postDelayed(() -> ((NewMainActivity) context).finish(), 1000);
    }

    /**
     * 百度在线语音合成
     */
    @SuppressWarnings("unused")
    private void setSpeechSynthesizer(String speakString) {
        setParams();
        int ret = speechSynthesizer.speak(speakString);
        if (ret != 0) {
            logger.error("开始合成器失败：" + errorCodeAndDescription(ret));
        }
    }

    private void setParams() {
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");//发音人，目前支持女声(0)和男声(1) ，取值详见随后常量声明
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5");//音量，取值范围[0, 9]，数值越大，音量越大
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "3");//朗读语速，取值范围[0, 9]，数值越大，语速越快
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");//音调，取值范围[0, 9]，数值越大，音量越高
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_ENCODE, SpeechSynthesizer.AUDIO_ENCODE_AMR);
        speechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUDIO_RATE, SpeechSynthesizer.AUDIO_BITRATE_AMR_15K85);
    }

    private String errorCodeAndDescription(int errorCode) {
        String errorDescription = SpeechError.errorDescription(errorCode);
        return errorDescription + "(" + errorCode + ")";
    }

    private void startTimerToLock() {
        if (timerTaskLock != null) {
            timerTaskLock.cancel();
            timerTaskLock = null;
        }
        timerTaskLock = new TimerTask() {
            @Override
            public void run() {
                myHandler.post(() -> {
                    change_group_show_area.setVisibility(View.VISIBLE);
                    logger.info(" open_lock_change_group" + " startTimerToLock" + isFlex + MyApplication.instance.isPttViewPager);
                    if (!isFlex || MyApplication.instance.isPttViewPager) {
                        logger.info(" open_lock_change_group " + " startTimerToLock 进入判断");
                    }
                });
            }
        };
        MyTerminalFactory.getSDK().getTimer().schedule(timerTaskLock, 5000);
    }

    @Override
    public void onPause() {
        super.onPause();
        myHandler.removeMessages(1);
        if (null != keyMointor) {
            try {
                keyMointor.setHandler(gotaKeyHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {

        logger.info("TalkBackFragment---onDestroy");
        myHandler.removeCallbacksAndMessages(null);
        if (timerTaskLock != null) {
            timerTaskLock.cancel();
            timerTaskLock = null;
        }

        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCallingCannotClickHandler);

        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateDepGroupHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyEnvironmentMonitorHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupScanResultHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupByNoHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUnreadMessageAdd1Handler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseGroupActiveHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupViewHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyZfyBoundPhoneMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveTestGroupCallHandler);
        try {
            getContext().unregisterReceiver(mBatInfoReceiver);
            getActivity().unregisterReceiver(mbtBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 切文件夾組，上下切文件夾的第一個組
     *
     * @param upDown true 為向上切換；false為向下切
     */

    private void changeFolderGroup(boolean upDown) {


        List<Group> allGroups = MyTerminalFactory.getSDK().getConfigManager().getAllGroups();
        // 如果只是一个文件夹，不涉及文件夹切换，则忽略
        if (allGroups.size() <= 1) {
            return;
        }


        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        int targetGroupId = 0;
        if (upDown) {
            for (int i = 0; i < allGroups.size(); i++) {
                if (allGroups.get(i).getNo() == currentGroupId) {
                    if (i == 0) {
                        targetGroupId = allGroups.get(allGroups.size() - 1).getNo();
                    } else {
                        targetGroupId = allGroups.get(i - 1).getNo();
                    }
                }
            }

        } else {
            for (int i = 0; i < allGroups.size(); i++) {
                if (allGroups.get(i).getNo() == currentGroupId) {
                    if (i == allGroups.size() - 1) {
                        targetGroupId = allGroups.get(0).getNo();
                    } else {
                        targetGroupId = allGroups.get(i + 1).getNo();
                    }
                }
            }
        }
        TerminalFactory.getSDK().getGroupManager().changeGroup(targetGroupId);
    }

    @Override
    public void onMyKeyDown(KeyEvent event) {
        logger.info("TalkbackFragment keyEvent:" + event.getKeyCode());
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP: // 文件夹级向上切组
            {
                changeFolderGroup(true);
            }
            break;
            case KeyEvent.KEYCODE_DPAD_DOWN: // 文件夹级向下切组
            {
                changeFolderGroup(false);
            }
            break;
            case KeyEvent.KEYCODE_DPAD_LEFT: // 左切组
            {
                change_group_view.addRight(1);
                MyApplication.instance.isMoved = true;
            }
            break;
            case KeyEvent.KEYCODE_DPAD_RIGHT: //右切组
            {
                change_group_view.addLeft(1);
                MyApplication.instance.isMoved = true;
            }
            break;
            default:
                break;
        }
    }


    private void stateView() {
        if (MyApplication.instance.getGroupSpeakState() == null || MyApplication.instance.getGroupListenenState() == null) {
            change2Silence();
            return;
        }
        switch (MyApplication.instance.getGroupSpeakState()) {
            case IDLE:
                change2Silence();
                break;
            case GRANTING:
                change2PreSpeaking();
                break;
            case GRANTED:
                change2Speaking();
                break;
            case WAITING:
                change2Waiting();
                break;
            default:
                break;
        }
        switch (MyApplication.instance.getGroupListenenState()) {
            case IDLE:
                change2Silence();
                break;
            case LISTENING:
                isScanGroupCall = false;
                change2Listening();
                break;
            default:
                break;
        }
    }

    //PTT抬起以后
    private void pttUpDoThing() {
        logger.info("ptt.pttUpDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);
//        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            return;
        }

        if (MyApplication.instance.getGroupListenenState() == GroupCallListenState.LISTENING) {
            isScanGroupCall = false;
            change2Listening();
        } else {
            change2Silence();

        }
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
    }

    //PTT按下以后
    private void pttDownDoThing() {
        logger.info("ptt.pttDownDoThing执行了 isPttPress：" + MyApplication.instance.isPttPress);

        if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt((NewMainActivity) context, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(context, getString(R.string.text_has_no_group_call_authority));
            return;
        }
        //半双工个呼中在别的组不能组呼、全双工个呼中不能组呼
        if (MyApplication.instance.getIndividualState() != IndividualCallState.IDLE) {

        }
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
        logger.info("PTT按下以后resultCode:" + resultCode);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            change2PreSpeaking();
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            change2Waiting();
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(context, resultCode);
        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                logger.info("Intent.ACTION_SCREEN_OFF，ptt按钮抬起，停止组呼：" + MyApplication.instance.isPttPress);
                if (MyApplication.instance.isPttPress) {
                    pttUpDoThing();
                }
            }
        }
    };

    /**
     * 断开连接了
     */
    BroadcastReceiver mbtBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                if (MyApplication.instance.getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                    MyApplication.instance.isClickVolumeToCall = false;
                    pttUpDoThing();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setPttText();
    }

}
