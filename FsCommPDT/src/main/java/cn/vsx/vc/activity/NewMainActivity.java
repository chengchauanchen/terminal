package cn.vsx.vc.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hytera.api.SDKException;
import com.hytera.api.SDKManager;
import com.hytera.api.base.common.CallManager;
import com.hytera.api.base.common.CommonManager;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;
import org.easydarwin.easypusher.BackgroundCameraService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.StopGroupCallReason;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTDownHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePTTUpHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePopBackStackHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageAdd1Handler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.application.UpdateManager;
import cn.vsx.vc.fragment.ContactsFragmentNew;
import cn.vsx.vc.fragment.GroupSearchFragment;
import cn.vsx.vc.fragment.LocalMemberSearchFragment;
import cn.vsx.vc.fragment.NewsFragment;
import cn.vsx.vc.fragment.PersonSearchFragment;
import cn.vsx.vc.fragment.SettingFragmentNew;
import cn.vsx.vc.fragment.TalkbackFragment;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.receiveHandle.ReceiveUnReadCountChangedHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentDestoryHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowGroupFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowPersonFragmentHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowPopupwindowHandler;
import cn.vsx.vc.receiver.HeadsetPlugReceiver;
import cn.vsx.vc.service.LockScreenService;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.HeadSetUtil;
import cn.vsx.vc.utils.SystemUtil;
import cn.vsx.vc.view.BottomView;
import cn.vsx.vc.view.IndividualCallTimerView;
import cn.vsx.vc.view.TimerView;
import cn.vsx.vc.view.custompopupwindow.ChangeNamePopupwindow;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.PhoneAdapter;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class NewMainActivity extends BaseActivity implements SettingFragmentNew.sendPttState {
    /**============================================================================handler============================================================================================================================**/
    private ReceiveUnReadCountChangedHandler receiveUnReadCountChangedHandler = new ReceiveUnReadCountChangedHandler() {
        @Override
        public void handler(final int unReadCount) {
            myHandler.post(() -> bv_person_contacts.setBadgeViewCount(unReadCount));
        }
    };

    /**
     * 收到通知，邀请自己去观看直播
     */
    TimerTask liveTimerTask;

    private boolean onRecordAudioDenied;
    private boolean onLocationDenied;
    private boolean onCameraDenied;

    /**当提示框自己消失，或者手动点击关闭按钮；未读消息+1；
     * 通知TalkbackFragment消息图标上小气泡的数字+1；*/

    /**
     * 视频来了，前往观看
     **/

    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> {
            });
        }
    };
    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> {

    };
    /**
     * 信令服务发送NotifyForceRegisterMessage消息时，先去reAuth(false)，然后login()
     */
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(int resultCode, final String resultDesc, boolean isRegisted) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                logger.info("信令服务器通知NotifyForceRegisterMessage消息，在MainActivity: isRegisted" + isRegisted);
                if (isRegisted) {//注册过，在后台登录，session超时也走这
                    TerminalFactory.getSDK().getAuthManagerTwo().login();
                    logger.info("信令服务器通知NotifyForceRegisterMessage消息，在MainActivity中登录了");
                    MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecord();
                } else {//没注册过，关掉主界面，去注册界面
                    startActivity(new Intent(NewMainActivity.this, RegistActivity.class));
                    NewMainActivity.this.finish();
                    stopService(new Intent(NewMainActivity.this, LockScreenService.class));
                }
            }
        }
    };

    /**
     * 转组消息,切换到主界面
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            logger.info("转组成功回调消息, isChanging:" + MyApplication.instance.isChanging);
            synchronized (MyApplication.instance) {
                MyApplication.instance.isChanging = false;
                if (MyApplication.instance.isPttPress) {
                    logger.info("转组成功回调消息：isPttPress" + MyApplication.instance.isPttPress);
                    int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
                    if (resultCode != BaseCommonCode.SUCCESS_CODE) {
                        ToastUtil.groupCallFailToast(NewMainActivity.this, resultCode);
                    }
//					MyTerminalFactory.getSDK().getGroupCallManager().requestCall();
                }
                MyApplication.instance.notifyAll();
            }
            if (errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
                myHandler.post(() -> setTabSelection(R.id.bv_talk_back));
            }
        }
    };

    private ReceiveLoginResponseHandler receiveLoginResponseHandler = new ReceiveLoginResponseHandler(){
        @Override
        public void handler(int resultCode, String resultDesc){
            if(resultCode == BaseCommonCode.SUCCESS_CODE){
                myHandler.post(() -> noNetWork.setVisibility(View.GONE));
                MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecord();
            }
        }
    };

    /**
     * 网络连接状态
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
        @Override
        public void handler(final boolean connected) {
            logger.info("主界面收到服务是否连接的通知ServerConnectionEstablishedHandler" + connected);
            NewMainActivity.this.runOnUiThread(() -> {
                if (!connected) {
                    noNetWork.setVisibility(View.VISIBLE);
                    if (ll_emergency_prompt != null && ll_emergency_prompt.getVisibility() == View.VISIBLE) {
                        ll_emergency_prompt.setVisibility(View.GONE);
                        ICTV_emergency_time.onStop();
                    }
                    if (ll_groupCall_prompt != null && ll_groupCall_prompt.getVisibility() == View.VISIBLE) {
                        ll_groupCall_prompt.setVisibility(View.GONE);
                        ICTV_groupCall_time.stop();
                    }
                }
            });
        }
    };

    /**
     * PTT按下时不可切换raidobutton
     */
    private ReceiveCallingCannotClickHandler receiveCallingCannotClickHandler = new ReceiveCallingCannotClickHandler() {
        @Override
        public void handler(final boolean isCannotCheck) {
            logger.info("raidobutton被禁了 ？ isCannotCheck：" + isCannotCheck);
            bv_talk_back.setEnabled(!isCannotCheck);
            bv_talk_back.setClickable(!isCannotCheck);
            bv_person_contacts.setEnabled(!isCannotCheck);
            bv_person_contacts.setClickable(!isCannotCheck);
            bv_group_contacts.setEnabled(!isCannotCheck);
            bv_group_contacts.setClickable(!isCannotCheck);
            bv_setting.setEnabled(!isCannotCheck);
            bv_setting.setClickable(!isCannotCheck);
            if (!MyApplication.instance.isMoved) {
                synchronized (MyApplication.instance) {
                    logger.info("-------ReceiveCallingCannotClickHandler--------" + MyApplication.instance.isChanging);
                    MyApplication.instance.isChanging = false;
                    MyApplication.instance.notifyAll();
                }
            }
        }
    };

    protected ReceiveExitHandler receiveExitHandler = new ReceiveExitHandler() {
        @Override
        public void handle(String msg){
            cn.vsx.vc.utils.ToastUtil.showToast(NewMainActivity.this,msg);
            myHandler.postDelayed(() -> {
                Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
                stoppedCallIntent.putExtra("stoppedResult","0");
                SendRecvHelper.send(getApplicationContext(),stoppedCallIntent);

                MyTerminalFactory.getSDK().exit();//停止服务
                PromptManager.getInstance().stop();
                for (Activity activity : ActivityCollector.getAllActivity().values()) {
                    activity.finish();
                }
                TerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, true);
                TerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, true);
                MyApplication.instance.isClickVolumeToCall = false;
                MyApplication.instance.isPttPress = false;
                MyApplication.instance.stopIndividualCallService();
                Process.killProcess(Process.myPid());
            },2000);
        }
    };

    /** PTT按下 **/
    private ReceivePTTDownHandler receivePTTDownHandler = new ReceivePTTDownHandler() {
        @Override
        public void handler(final int requestGroupCall) {
            myHandler.post(() -> {
                if (requestGroupCall == BaseCommonCode.SUCCESS_CODE) {
                    if (!MyApplication.instance.isPttPress ) {
                        MyApplication.instance.isPttPress = true;
                        logger.info("小手雷pttDown事件，开始说话");
                        if (onPTTVolumeBtnStatusChangedListener != null) {
                            logger.warn("小手雷ptt按钮，按下时：ptt的当前状态是："+MyApplication.instance.getGroupSpeakState());
                            onPTTVolumeBtnStatusChangedListener.onPTTVolumeBtnStatusChange(MyApplication.instance.getGroupSpeakState());
                        }
                        if( PhoneAdapter.isF25() ){
                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
                        }
                    }
                }else if(requestGroupCall == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()){
                }else {//组呼失败的提示
                    ToastUtil.groupCallFailToast(NewMainActivity.this, requestGroupCall);
                }
            });
        }
    };

    /**
     * PTT抬起
     **/
    private ReceivePTTUpHandler receivePTTUpHandler = new ReceivePTTUpHandler() {
        @Override
        public void handler() {
            myHandler.post(() -> {
                if (MyApplication.instance.isPttPress) {
                    MyApplication.instance.isPttPress = false;

                    logger.info("小手雷pttUp事件，停止说话");
                    if (PhoneAdapter.isF25()) {
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
                    }
                }
            });
        }
    };

    /**
     * 显示添加成员列表界面popupwindow的消息监听
     */
    private ReceiverShowPopupwindowHandler mReceiverShowPopupwindowHandler = new ReceiverShowPopupwindowHandler() {
        @Override
        public void handler(final String className) {
            myHandler.post(() -> {

                if (className.equals(PersonSearchFragment.class.getName())) {
                    fl_fragment_container_main.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container_main, new PersonSearchFragment()).commit();
                }
                else if (className.equals(ChangeNamePopupwindow.class.getName())) {
                    if(my_view == null)
                        my_view = findViewById(R.id.my_view);
                    new ChangeNamePopupwindow(NewMainActivity.this).showAtLocation(my_view,Gravity.BOTTOM,0,0);
                }
            });
        }
    };

    private ReceivePopBackStackHandler mReceivePopBackStackHandler = new ReceivePopBackStackHandler(){
        @Override
        public void handle(){
            myHandler.post(() -> {
                ll_content.setVisibility(View.VISIBLE);
                getSupportFragmentManager().popBackStack();
            });
        }
    };
    /**  弹出好友列表的搜索fragment **/
    private ReceiverShowPersonFragmentHandler mReceiverShowPersonFragmentHandler = new ReceiverShowPersonFragmentHandler() {
        @Override
        public void handler(List<Member> memberList) {
            LocalMemberSearchFragment localMemberSearchFragment = new LocalMemberSearchFragment();
            localMemberSearchFragment.setMemberList(memberList);
            fl_fragment_container_main.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container_main, localMemberSearchFragment).commit();
            myHandler.postDelayed(() -> ll_content.setVisibility(View.GONE),500);

        }
    };

    private ReceiverShowGroupFragmentHandler mReceiverShowGroupFragmentHandler = new ReceiverShowGroupFragmentHandler() {
        @Override
        public void handler(List<Group> groupList,boolean isScanGroupSearch) {
            GroupSearchFragment groupSearchFragment = new GroupSearchFragment();
            fl_fragment_container_main.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container_main, groupSearchFragment).commit();
            myHandler.postDelayed(() -> ll_content.setVisibility(View.GONE),500);

        }
    };

    private ReceiverFragmentDestoryHandler mReceiverFragmentDestoryHandler = new ReceiverFragmentDestoryHandler() {
        @Override
        public void handler() {
            fl_fragment_container_main.setVisibility(View.GONE);
        }
    };

    /**
     * 通知被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {
        @Override
        public void handler(int reasonCode) {

            if (reasonCode == StopGroupCallReason.TIMEOUT_STOP_GROUP_CALL.getCode()) {
                //				MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
            }
            myHandler.post(() -> {
            });
        }
    };
    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {

        @Override
        public void handler(final int memberId, final String memberName, final int groupId,
                            String version, final CallMode currentCallMode) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                NewMainActivity.this.currentCallMode = currentCallMode;
                logger.info("组呼时，被动方接收组呼类型：" + currentCallMode);
                myHandler.post(() -> {
                    if (currentCallMode == CallMode.EMERGENCY_CALL_MODE) {
                        //弹出紧急组呼的呼叫界面
                    } else if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
                    }
                });
            }
        }
    };
    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, String resultDesc) {
            myHandler.post(() -> {

                currentCallMode = MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode();
                logger.info("接收组呼类型" + currentCallMode);
                if (methodResult == 0) {//请求成功，开始组呼
                    if (currentCallMode == CallMode.EMERGENCY_CALL_MODE) {//主动方的紧急组呼
                        int emergencyGroupId = MyTerminalFactory.getSDK().getParam(Params.EMERGENCYID, 0);
                        emergencyGroup = DataUtil.getGroupByGroupNo(emergencyGroupId);
                        //弹出紧急组呼的呼叫界面
                        ll_emergency_prompt.setVisibility(View.VISIBLE);
                        tv_emergency_member.setText(String.format(getString(R.string.text_in_emergency_group_now),emergencyGroup.name));
                        ICTV_emergency_time.onStart();
                        if (timerTask != null) {
                            timerTask.cancel();
                            timerTask = null;
                        }
                        popupWindow.showAsDropDown(my_view);
                    } else {//普通组呼成功

                        if (!MyApplication.instance.isPttViewPager || !MyApplication.instance.isTalkbackFragment) {
                            if (!ll_groupCall_prompt.isShown()) {
                                tv_current_group.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
                                ICTV_groupCall_time.start();
                            }
                        }
                    }
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    myHandler.post(() -> ToastUtil.showToast(NewMainActivity.this, getString(R.string.text_current_group_only_listener_can_not_speak)));
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                } else {
                }
            });
        }
    };
    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            myHandler.post(() -> {
                if (currentCallMode == CallMode.EMERGENCY_CALL_MODE) {
                    ICTV_emergency_time.onStop();
                    ll_emergency_prompt.setVisibility(View.GONE);
                    popupWindow.dismiss();

                }
                if (ll_groupCall_prompt != null && ll_groupCall_prompt.isShown()) {
                    ICTV_groupCall_time.stop();
                    ll_groupCall_prompt.setVisibility(View.GONE);
                }
            });
        }
    };
    /**=====================================================================================================Listener================================================================================================================================**/
    private CallManager callManager;
    private UpdateManager updateManager;

    /**
     * 视频来了，关闭提示框
     **/
    private class LiveReturnOnClickListeren implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            rl_livecome.setVisibility(View.GONE);
            if (liveTimerTask != null) {
                liveTimerTask.cancel();
                liveTimerTask = null;
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveUnreadMessageAdd1Handler.class, true);//点击叉；关闭提示框，未读消息+1；
            }
        }
    }


    /**
     * 悬浮按钮的长按事件
     */
    private final class OnLongClickListenerImplementationToGroupCall implements
            View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            logger.info("----悬浮按钮-----" + isCircleTouchEvent + "    volumePress" + MyApplication.instance.volumePress);
            if (!isCircleTouchEvent && !MyApplication.instance.volumePress) {
                int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("");
                if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
                    imgbtn_ptt.setBackgroundResource(R.drawable.fw_c_yellow);
                    talkbackFragment.setPttCurrentState(GroupCallSpeakState.GRANTING);
                    isLongClickToGroupCall = true;
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
                    MyApplication.instance.folatWindowPress = true;
                } else {
                    cn.vsx.vc.utils.ToastUtil.groupCallFailToast(NewMainActivity.this, resultCode);
                }
            }
            return true;
        }
    }

    /**
     * 闪电手势监听
     */
    private final class OnGesturePerformedListenerImplementation implements
            GestureOverlayView.OnGesturePerformedListener {
        @Override
        public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
            //创建加载手势库的工具
            GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(NewMainActivity.this, R.raw.gestures);
            //加载手势库
            boolean loadStatus = gestureLibrary.load();
            //如果手势库加载成功
            if (loadStatus) {
                //识别手势  Prediction是一个相似度对象,集合中的相似度是从高到低进行排列
                ArrayList<Prediction> pres = gestureLibrary.recognize(gesture);
                if (!pres.isEmpty()) {
                    //拿到相似度最高的对象
                    Prediction pre = pres.get(0);
                    //用整型的数表示百分比  >30%
                    if (pre.score > 3) {
                        //拿到手势的名字判断进行下一步逻辑
                        if ("lightning".equals(pre.name)) {
                            //处理紧急呼叫事件
                            isEmergencyCall = true;
                            MyTerminalFactory.getSDK().getEmergencyCallManager().emergencyCall();
                            //跟据紧急呼叫的类型，展示不同的呼叫界面
                            int emergencyType = MyTerminalFactory.getSDK().getParam(Params.EMERGENCYTYPE, 0);
                            if (emergencyType == MessageType.GROUP_CALL.getCode()) {
                                //紧急呼叫到组，屏蔽组呼，个呼，显示组呼通话状态
                                //在收到receiveRequestGroupCallConformationHandler的消息的时候处理
                            } else if (emergencyType == MessageType.PRIVATE_CALL.getCode()) {
                                //紧急呼叫到人，屏蔽组呼，个呼，显示个呼通话状态
                                myHandler.post(() -> {
                                    int emergencyMemberId = MyTerminalFactory.getSDK().getParam(Params.EMERGENCYID, 0);
                                    calleeMember = DataUtil.getMemberByMemberNo(emergencyMemberId);

                                    //弹出个呼的呼叫请求界面
                                    ll_emergency_prompt.setVisibility(View.VISIBLE);
                                    tv_emergency_member.setText(String.format(getString(R.string.text_in_emergency_call_now),calleeMember.getName()));
                                    ICTV_emergency_time.setVisibility(View.GONE);

                                    if (timerTask != null) {
                                        timerTask.cancel();
                                        timerTask = null;
                                    }
                                    popupWindow.showAsDropDown(my_view);

                                });
                            }
                        }
                    } else {
                        cn.vsx.vc.utils.ToastUtil.showToast(NewMainActivity.this, getString(R.string.text_gesture_mismatch));
                    }
                } else {
                    cn.vsx.vc.utils.ToastUtil.showToast(NewMainActivity.this, getString(R.string.text_load_gesture_lib_fail));
                }
            }
        }
    }

    /**音量改变*/
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(boolean isVolumeOff,int status) {
            logger.info("sjl_:"+status);
            myHandler.removeMessages(RECEIVEVOICECHANGED);
            if (status == 0){
                ll_sliding_chenge_volume.setVisibility(View.GONE);
            }else if (status ==1){
                ll_sliding_chenge_volume.setVisibility(View.VISIBLE);
            }
            tv_volume_fw.setText(MyTerminalFactory.getSDK().getAudioProxy().getVolume() + "%");
            myHandler.sendEmptyMessageDelayed(RECEIVEVOICECHANGED,2000);
        }
    };

    //音量上下键为PTT按钮状态改变的监听接口
    private OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener;

    public interface OnPTTVolumeBtnStatusChangedListener {
        void onPTTVolumeBtnStatusChange(GroupCallSpeakState groupCallSpeakState);
    }


    public void setOnPTTVolumeBtnStatusChangedListener(OnPTTVolumeBtnStatusChangedListener onPTTVolumeBtnStatusChangedListener) {
        this.onPTTVolumeBtnStatusChangedListener = onPTTVolumeBtnStatusChangedListener;
    }

    /**
     * 圆形ptt触摸事件，移动
     */
    private final class OnTouchListenerImplementationToRemovePttFloatWindow implements
            View.OnTouchListener {
        private int downX;
        private int downY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //  按住时移动控件
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    MyApplication.instance.isPttFlowPress = true;

                    downX = (int) event.getRawX();
                    downY = (int) event.getRawY();

                    // 获取上一次上 下 左 右各边与父控件的距离
                    moveLeft = imgbtn_ptt.getLeft();
                    moveTop = imgbtn_ptt.getTop();
                    moveRight = imgbtn_ptt.getRight();
                    moveBottom = imgbtn_ptt.getBottom();

                    isCircleTouchEvent = false;
                    isLongClickToGroupCall = false;
                    break;
                case MotionEvent.ACTION_MOVE:

                    int moveX = (int) event.getRawX();
                    int moveY = (int) event.getRawY();
                    int dx = moveX - downX;
                    int dy = moveY - downY;

                    if (Math.abs(dx) > 15 || Math.abs(dy) > 15) {
                        if (!isLongClickToGroupCall) {
                            moveLeft += dx;
                            moveTop += dy;
                            moveRight += dx;
                            moveBottom += dy;

                            if (moveLeft > 0 && moveRight < width && moveTop > 0 && moveBottom < height) {
                                imgbtn_ptt.layout(moveLeft, moveTop, moveRight, moveBottom);
                            }
                            // 本次移动的结尾作为下一次移动的开始
                            downX = (int) event.getRawX();
                            downY = (int) event.getRawY();
                            isCircleTouchEvent = true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
                    MyApplication.instance.isPttFlowPress = false;
                    showFolatWindow();
                    imgbtn_ptt.addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                        if (moveLeft > 0 && moveRight < width && moveTop > 0 && moveBottom < height) {
                            imgbtn_ptt.layout(moveLeft, moveTop, moveRight, moveBottom);
                        }
                    });

                    if (!isCircleTouchEvent) {
                        if (isLongClickToGroupCall) {
                            isLongClickToGroupCall = false;
                            //                            MyTerminalFactory.getSDK().getGroupCallManager().ceaseCall();
                        }
                    }
                    isCircleTouchEvent = false;
                    break;
                default:
                    break;
            }
            return false;
        }
    }



    //其他页面组呼圆形按钮
    @Bind(R.id.main_page)
    RelativeLayout rl_main_activity;
    @Bind(R.id.imgbtn_ptt)
    Button imgbtn_ptt;
    @Bind(R.id.ll_sliding_chenge_volume)
    LinearLayout ll_sliding_chenge_volume;
    @Bind(R.id.tv_volume_fw)
    TextView tv_volume_fw;
    @Bind(R.id.my_view)
    View my_view;//popuwindow依附的view
    @Bind(R.id.pop_view)
    View pop_view;
    @Bind(R.id.no_network)
    LinearLayout noNetwork;

    //紧急呼叫提示风格
    @Bind(R.id.ll_emergency_prompt)
    LinearLayout ll_emergency_prompt;
    @Bind(R.id.ICTV_emergency_time)
    IndividualCallTimerView ICTV_emergency_time;
    @Bind(R.id.tv_emergency_member)
    TextView tv_emergency_member;

    //其他页面组呼提示
    @Bind(R.id.ll_groupCall_prompt)
    LinearLayout ll_groupCall_prompt;
    @Bind(R.id.tv_current_group)
    TextView tv_current_group;
    @Bind(R.id.incomming_call_current_speaker)
    TextView incomming_call_current_speaker;
    @Bind(R.id.ICTV_groupCall_time)
    TimerView ICTV_groupCall_time;

    //视频来了提示
    @Bind(R.id.rl_livecome)
    RelativeLayout rl_livecome;
    @Bind(R.id.tv_live_theme)
    TextView tv_live_theme;
    @Bind(R.id.lv_live_return)
    ImageView lv_live_return;
    @Bind(R.id.tv_live_name)
    TextView tv_live_name;
    @Bind(R.id.btn_live_gowatch)
    Button btn_live_gowatch;
    @Bind(R.id.noNetWork)
    LinearLayout noNetWork;

    @Bind(R.id.fl_fragment_container_main)
    FrameLayout fl_fragment_container_main;

    @Bind(R.id.bv_talk_back)
    BottomView bv_talk_back;
    @Bind(R.id.bv_person_contacts)
    BottomView bv_person_contacts;
    @Bind(R.id.bv_group_contacts)
    BottomView bv_group_contacts;
    @Bind(R.id.bv_setting)
    BottomView bv_setting;
    @Bind(R.id.ll_content)
    LinearLayout ll_content;
    public static boolean isForeground=false;

    private TalkbackFragment talkbackFragment;
    private NewsFragment newsFragment;
    private ContactsFragmentNew contactsFragmentNew;
    private SettingFragmentNew settingFragmentNew;
    private FragmentManager fragmentManager;
    private Fragment currentFragment;
    /**
     * 当前Fragment代号
     *
     * TalkbackFragment     1
     * NewsFragment         2
     * ContactsFragmentNew  3
     * SettingFragmentNew   4
     */
    public static int mCurrentFragmentCode;
    private Logger logger = Logger.getLogger(getClass());
    private boolean isPressedBackOnce = false;
    private long firstTime = 0;
    private long secondTime = 0;
    private HeadsetPlugReceiver headsetPlugReceiver;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;// 传感器管理对象,调用距离传感器，控制屏幕
    private Timer timer = new Timer();

    private PopupWindow popupWindow;
    private int moveLeft, moveTop, moveRight, moveBottom;
    private static final int RECEIVEVOICECHANGED = 0;
    public Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == RECEIVEVOICECHANGED){
                ll_sliding_chenge_volume.setVisibility(View.GONE);
            }
        }
    };
    private boolean isShowPtt = false;
    private boolean isEmergencyCall;//紧急呼叫的标志
    private Member calleeMember;//主叫用的被叫成员对象
    private Group emergencyGroup;
    private CallMode currentCallMode;
    private int currentCheckedId;
    private boolean isCircleTouchEvent;
    private boolean isLongClickToGroupCall;
    private TimerTask timerTask;
    private int width;
    private int height;

    private BackListener mBackListener;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_new_main;
    }

    @Override
    public void initView() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 电源管理对象,屏幕开关
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 设置底部导航图片的大小
        initBottomImage();
        //        initBadgeView();
        fragmentManager = getSupportFragmentManager();
        //距离感应器的电源锁
        wakeLock = powerManager.newWakeLock(32, "wakeLock");
        //正常情况下的电源锁
        PowerManager.WakeLock wakeLockActivity = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeLock");
        wakeLockActivity.acquire(2 * 60 * 1000);
        // 先初始化界面加载第一个Fragment
        initFragment();

        imgbtn_ptt.setVisibility(View.GONE);
        ll_emergency_prompt.setVisibility(View.GONE);
    }

    @Override
    public void initListener() {
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowPopupwindowHandler);
        //视频提示按钮监听
        lv_live_return.setOnClickListener(new LiveReturnOnClickListeren());
        //悬浮按钮
        imgbtn_ptt.setOnTouchListener(new OnTouchListenerImplementationToRemovePttFloatWindow());
        imgbtn_ptt.setOnLongClickListener(new OnLongClickListenerImplementationToGroupCall());

        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCallingCannotClickHandler);

        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceivePopBackStackHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowPersonFragmentHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverShowGroupFragmentHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverFragmentDestoryHandler);
        //
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveExitHandler);

        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveUnReadCountChangedHandler);

        bv_talk_back.setOnClickListener(new BottomViewClickListener());
        bv_person_contacts.setOnClickListener(new BottomViewClickListener());
        bv_group_contacts.setOnClickListener(new BottomViewClickListener());
        bv_setting.setOnClickListener(new BottomViewClickListener());

        if(null != callManager){
            callManager.addPhysicalPttListener(b -> {
                Intent intent = new Intent();
                if(b){
                    logger.info("海能达按下ptt");
                    intent.setAction("HyteraPttDown");
                }else {
                    logger.info("海能达松开ptt");
                    intent.setAction("HyteraPttUp");
                }
                sendBroadcast(intent);
            });
        }
    }

    class BottomViewClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            setTabSelection(view.getId());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            //文件
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + MyApplication.instance.getApplicationInfo()
                    .loadLabel(MyApplication.instance.getPackageManager()) + File.separator + "logs"
                    + File.separator + "log.txt");
            if (!file.exists()) {
                MyApplication.instance.getSpecificSDK().configLogger();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isForeground=false;
        if (MyApplication.instance.isPttPress && !TerminalFactory.getSDK().isExit()) {
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        }
        MyApplication.instance.isPttPress = false;
        talkbackFragment.setPttCurrentState(GroupCallSpeakState.IDLE);
    }

    @Override
    public void initData() {
        //先判断悬浮窗权限，没打开就关闭
        if(!checkFloatPermission(this)){
            Log.e("NewMainActivity", "未开启悬浮窗权限");
            // SYSTEM_ALERT_WINDOW permission not granted...
            ToastUtil.showToast(NewMainActivity.this, getString(R.string.open_overlay_permisson));
            exitApp();
            return;
        }
        WindowManager windowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        width = windowManager.getDefaultDisplay().getWidth();
        height = windowManager.getDefaultDisplay().getHeight();
        if (PhoneAdapter.isF25()) {
            MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0);
        }

        //开启服务，开启锁屏界面
        startService(new Intent(NewMainActivity.this, LockScreenService.class));
        MyApplication.instance.startUVCCameraService();

        initVoip();

        MyTerminalFactory.getSDK().getVideoProxy().setActivity(this);

        String machineType = android.os.Build.MODEL;
        MyTerminalFactory.getSDK().putParam(Params.ANDROID_BUILD_MODEL,machineType);
        logger.error("machineType :"+machineType);
        if (machineType.equals("PDC760")) {
            //监听海能达PTT按钮
            try{
                CommonManager mCommonManager = SDKManager.getCommonManager(getApplicationContext());
                callManager = mCommonManager.getCallManager(new CallManager.CallManagerConnectStateListener(){
                    @Override
                    public void onApiConnectResult(int i){
                        logger.info("onApiConnectResult:"+i);
                        if(i ==0){
                            callManager.interceptPtt();
                        }
                    }

                    @Override
                    public void onApiDisconnected(int i){
                        logger.info("onApiDisconnected:"+i);
                        if(null != callManager){
                            callManager.cancelInterceptPtt();
                        }
                    }
                });
            }catch(SDKException e){
                e.printStackTrace();
            }
            //版本自动更新检测
            if (MyTerminalFactory.getSDK().getParam(Params.IS_AUTO_UPDATE, false) && !MyApplication.instance.isUpdatingAPP) {
                updateManager = new UpdateManager(NewMainActivity.this);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateManager.checkUpdate(MyTerminalFactory.getSDK().getParam(Params.UPDATE_URL,""),false);
                    }
                }, 4000);
            }
        }

        judgePermission();
    }

    private void initVoip(){
        String account = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)+"";
        String voipServerIp = MyTerminalFactory.getSDK().getParam(Params.VOIP_SERVER_IP, "");
        String voipServerPort = MyTerminalFactory.getSDK().getParam(Params.VOIP_SERVER_PORT, 0)+"";
        String server = voipServerIp+":"+voipServerPort;
        if(account.contains("@lzy")){
            account=account.substring(0,6);
        }
        if(account.startsWith("88")|| account.startsWith("86")){
            account = account.substring(2);
        }
        logger.info("voip账号："+account+",密码："+ account+"，服务器地址："+server);
        MyTerminalFactory.getSDK().getVoipCallManager().clearCache();
        if(!TextUtils.isEmpty(account)){
            MyTerminalFactory.getSDK().getVoipCallManager().login(account,account,server);
        }
    }

    public boolean checkFloatPermission(Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }

    private void exitApp() {
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult","0");
        SendRecvHelper.send(NewMainActivity.this,stoppedCallIntent);

        MyTerminalFactory.getSDK().exit();//停止服务
        PromptManager.getInstance().stop();
        for (Activity activity : ActivityCollector.getAllActivity().values()) {
            activity.finish();
        }
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopIndividualCallService();
    }


    private void initFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        talkbackFragment = new TalkbackFragment(this);
        newsFragment = new NewsFragment();
        contactsFragmentNew = new ContactsFragmentNew();
        settingFragmentNew = new SettingFragmentNew();
        settingFragmentNew.setSendPttState(this);
        transaction.add(R.id.ll_fragment, talkbackFragment).add(R.id.ll_fragment, newsFragment)
                .add(R.id.ll_fragment, contactsFragmentNew).add(R.id.ll_fragment, settingFragmentNew);
        transaction.show(talkbackFragment).hide(newsFragment).hide(contactsFragmentNew).hide(settingFragmentNew);
        transaction.commit();
        currentFragment = talkbackFragment;
        mCurrentFragmentCode=1;

    }


    public void switchFragment(Fragment from, Fragment to) {
        if (currentFragment != to) {
            currentFragment = to;

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.ll_fragment, to).commit(); // 隐藏当前的fragment，add下一个Fragment
            } else {
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }


    // 设置底部导航图片的大小
    private void initBottomImage() {
        bv_talk_back.setSelected(true);
        bv_group_contacts.setSelected(false);
        bv_person_contacts.setSelected(false);
        bv_setting.setSelected(false);
    }

    /**
     * 设置Fragment状态
     */
    private void setTabSelection(int checkedId) {
        switch (checkedId) {
            case R.id.bv_talk_back:
                if (talkbackFragment == null) {
                    talkbackFragment = new TalkbackFragment(this);
                }
                ll_sliding_chenge_volume.setVisibility(View.GONE);
                switchFragment(currentFragment, talkbackFragment);
                mCurrentFragmentCode=1;
                MyApplication.instance.isTalkbackFragment = true;
                showFolatWindow();
                bv_talk_back.setSelected(true);
                bv_person_contacts.setSelected(false);
                bv_group_contacts.setSelected(false);
                bv_setting.setSelected(false);
                break;

            case R.id.bv_person_contacts:
                if (newsFragment == null) {
                    newsFragment = new NewsFragment();
                }
                switchFragment(currentFragment, newsFragment);
                mCurrentFragmentCode=2;
                MyApplication.instance.isTalkbackFragment = false;
                showFolatWindow();
                bv_talk_back.setSelected(false);
                bv_person_contacts.setSelected(true);
                bv_group_contacts.setSelected(false);
                bv_setting.setSelected(false);
                break;
            case R.id.bv_group_contacts:
                if (contactsFragmentNew == null) {
                    contactsFragmentNew = new ContactsFragmentNew();
                }
                switchFragment(currentFragment, contactsFragmentNew);
                mCurrentFragmentCode=3;
                MyApplication.instance.isTalkbackFragment = false;
                showFolatWindow();
                bv_talk_back.setSelected(false);
                bv_person_contacts.setSelected(false);
                bv_group_contacts.setSelected(true);
                bv_setting.setSelected(false);
                break;
            case R.id.bv_setting:
                if (settingFragmentNew == null) {
                    settingFragmentNew = new SettingFragmentNew();
                }
                switchFragment(currentFragment, settingFragmentNew);
                mCurrentFragmentCode=4;
                MyApplication.instance.isTalkbackFragment = false;
                showFolatWindow();
                bv_talk_back.setSelected(false);
                bv_group_contacts.setSelected(false);
                bv_person_contacts.setSelected(false);
                bv_setting.setSelected(true);
                break;
            default:
                break;
        }
        currentCheckedId = checkedId;
    }

    /**
     * 显示悬浮按钮状态
     */
    public void showFolatWindow() {
        myHandler.post(() -> {
            if (!MyApplication.instance.isTalkbackFragment && isShowPtt) {
                imgbtn_ptt.setVisibility(View.VISIBLE);
            } else {
                imgbtn_ptt.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void sendPttState(Boolean isShow) {
        isShowPtt = isShow;
        showFolatWindow();
    }

    private ServiceConnection conn;
    private IBinder myIBinder;

    public IBinder getMyIBinder() {
        return myIBinder;
    }

    private void startLiveService() {
        // 创建直播服务
        MyTerminalFactory.getSDK().getVideoProxy().start().register(this);
        startService(new Intent(this, BackgroundCameraService.class));

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                BackgroundCameraService mService = ((BackgroundCameraService.LocalBinder) iBinder).getService();
                myIBinder = iBinder;
                logger.error("绑定视频服务成功:"+myIBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
            }
        };
        //BIND_AUTO_CREATE  如果没有服务就自己创建一个，执行onCreate()；
        bindService(new Intent(this, BackgroundCameraService.class), conn, BIND_AUTO_CREATE);
    }

    private PopupWindow setPopupwindow(View view) {
        PopupWindow mPopWindow = new PopupWindow(view);

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        //是否响应touch事件
        mPopWindow.setTouchable(true);
        //是否具有获取焦点的能力
        //		mPopWindow.setFocusable(true);
        mPopWindow.getContentView().setFocusable(true);
        mPopWindow.getContentView().setFocusableInTouchMode(true);

        //外部是否可以点击
        mPopWindow.setOutsideTouchable(false);

        //在Android 6.0以上 ，只能通过拦截事件来解决
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPopWindow.setTouchInterceptor((v, event) -> {
                final int x = (int) event.getX();
                final int y = (int) event.getY();
                if ((event.getAction() == MotionEvent.ACTION_DOWN)
                        && ((x < 0) || (x >= metrics.widthPixels) || (y < 0) || (y >= metrics.heightPixels))) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    return true;
                }
                return false;
            });
        }

        return mPopWindow;
    }

    /**
     * 距离感应器
     */
    @SuppressLint("Wakelock")
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] its = event.values;
            if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                // 经过测试，当手贴近距离感应器的时候its[0]返回值为0.0，当手离开时返回1.0
                if (its[0] == 0.0) {// 贴近手机
                    logger.info("hands up in calling activity贴近手机");
                    if (!wakeLock.isHeld()) {
                        wakeLock.acquire();// 申请设备电源锁
                    }
                } else {// 远离手机
                    logger.info("hands moved in calling activity远离手机");
                    if (wakeLock.isHeld()) {
                        wakeLock.release(); // 释放设备电源锁
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    /**
     * 紧急组呼时，主动挂断
     */
    private final class OnClickListenerImplementationEmergencyGroupHangup implements
            View.OnClickListener {
        @Override
        public void onClick(View v) {
            MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
            //会收到挂断的消息，在里面处理界面
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isForeground=true;
        onRecordAudioDenied = false;
        onLocationDenied = false;
        onCameraDenied = false;
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        if (MyApplication.instance.getIndividualState() == IndividualCallState.IDLE) {
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
        }

        if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.GRANTED) {
            ll_groupCall_prompt.setVisibility(View.GONE);
            ICTV_groupCall_time.stop();
        }

        //清楚所有通知
        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }




    @Override
    protected void onStop() {
        super.onStop();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
    }

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    public static final int REQUEST_INSTALL_PACKAGES_CODE = 1235;
    public static final int GET_UNKNOWN_APP_SOURCES = 1236;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    cn.vsx.vc.utils.ToastUtil.showToast(NewMainActivity.this, getString(R.string.open_overlay_permisson));
                } else {
                    // 创建直播服务
                    startLiveService();
                }
            }
        }else if(requestCode == GET_UNKNOWN_APP_SOURCES){
            if(null !=updateManager){
                updateManager.checkIsAndroidO(false);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CheckMyPermission.REQUEST_RECORD_AUDIO:
                onRecordAudioDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case CheckMyPermission.REQUEST_CAMERA:
                onCameraDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case CheckMyPermission.REQUEST_LOCATION:
                onLocationDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case REQUEST_INSTALL_PACKAGES_CODE:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            default:
                break;

        }
    }

    private void permissionDenied(int requestCode){
        if(requestCode == CheckMyPermission.REQUEST_RECORD_AUDIO){
            cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_audio_frequency_is_not_open_audio_is_not_used));
        }else if(requestCode ==CheckMyPermission.REQUEST_CAMERA){
            cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_camera_not_open_audio_is_not_used));
        }else if(requestCode ==CheckMyPermission.REQUEST_LOCATION){
            cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_location_not_open_locat_is_not_used));
        }
        //        judgePermission();
    }

    /**
     * 必须要有录音和相机的权限，APP才能去视频页面
     */
    private void judgePermission() {

        //6.0以下判断相机权限
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M  ){
            if(!SystemUtil.cameraIsCanUse()){
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CheckMyPermission.REQUEST_CAMERA);
            }
        }else {
            if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)){
                if(CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                        CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                    }
                    //                    else {
                    //                        CheckMyPermission.permissionPrompt(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    //                    }
                }else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }else {
                //如果权限被拒绝，申请下一个权限
                if(onRecordAudioDenied){
                    if(CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                        }
                    }else {
                        if(onLocationDenied){
                            if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                                if(!onCameraDenied){
                                    CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                                }
                            }
                        }else {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }
                }else{
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.RECORD_AUDIO);
                }
            }
        }

    }

    public void requestDrawOverLays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(NewMainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            } else {
                startLiveService();
            }
        }else {
            startLiveService();
        }
    }
    /**
     * 注销耳机插入拔出的监听
     */
    private void unregisterHeadsetPlugReceiver() {
        if (headsetPlugReceiver != null) {
            unregisterReceiver(headsetPlugReceiver);
        }
    }

    @Override
    public void doOtherDestroy() {
        if(null != callManager){
            callManager.cancelInterceptPtt();
        }
        HeadSetUtil.getInstance().close(this);// 关闭耳机线控监听
        if (wakeLock != null && sensorManager != null && wakeLock.isHeld()) {// 注销距离监听
            wakeLock.release();// 释放电源锁，如果不释放finish这个acitivity后仍然会有自动锁屏的效果
            sensorManager.unregisterListener(sensorEventListener);// 注销传感器监听
        }

        unregisterHeadsetPlugReceiver();

        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCallingCannotClickHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler );

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler );
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler );

        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTDownHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePTTUpHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceivePopBackStackHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveExitHandler);

        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveUnReadCountChangedHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowPopupwindowHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowPersonFragmentHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverShowGroupFragmentHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverFragmentDestoryHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);

        if(timerTask != null){
            timerTask.cancel();
            timerTask = null;
        }
        myHandler.removeCallbacksAndMessages(null);

        PromptManager.getInstance().stopRing();

        stopService(new Intent(NewMainActivity.this, LockScreenService.class));
        MyApplication.instance.stopUVCCameraService();
        if (conn != null) {
            unbindService(conn);
        }
        stopService(new Intent(NewMainActivity.this, BackgroundCameraService.class));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if(fl_fragment_container_main.getVisibility() == View.VISIBLE) {
                    ll_content.setVisibility(View.VISIBLE);
                    return super.onKeyDown(keyCode, event);
                }
                if (contactsFragmentNew.isVisible()){
                    if (mBackListener!=null){
                        mBackListener.onBack();
                    }
                }else {
                    exit();
                }

                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        currentCheckedId = savedInstanceState.getInt("currentCheckedId");
        if(currentCheckedId != 0){
            setTabSelection(currentCheckedId);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //记录当前的checkedId
        outState.putInt("currentCheckedId", currentCheckedId);
    }


    public void exit(){


        if ( !cn.vsx.vc.utils.PhoneAdapter.isF25() ) {
            // 判断是否点了一次后退
            if (isPressedBackOnce) {
                // 已经点了一次，这是第二次
                // 判断一下跟上一次点击的时间间隔，如果大于2秒，再谈一次吐司，小于2秒 直接finish
                secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    // 第一次点击
                    cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_click_again_to_exit));
                    isPressedBackOnce = true;
                    firstTime = System.currentTimeMillis();
                } else {
                    if (MyApplication.instance.getIndividualState() != IndividualCallState.SPEAKING) {
                        // 在2秒之内点击第二次
                        moveTaskToBack(true);//把程序变成后台的
                        // finish完成之后当前进程依然在
                        isPressedBackOnce = false;
                        firstTime = 0;
                        secondTime = 0;
                    }
                }
            } else {
                // 第一次点击
                cn.vsx.vc.utils.ToastUtil.showToast(this, getString(R.string.text_click_again_to_exit));
                isPressedBackOnce = true;
                firstTime = System.currentTimeMillis();
            }
        }else {
            // 如果是F25机型，返回到主通话界面
            imgbtn_ptt.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
        }
    }

    public void setOnBackListener(BackListener backListener){
        this.mBackListener=backListener;
    }

    public interface BackListener{
        void onBack();
    }
}
