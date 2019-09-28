package com.vsxin.terminalpad.mvp.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.TextViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.presenter.GroupMessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IGroupMessageView;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.FragmentManage;
import com.vsxin.terminalpad.view.RoundProgressBarWidthNumber;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.http.util.TextUtils;

import java.util.List;

import butterknife.BindView;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：消息记录界面
 * 修订历史：
 */
public class GroupMessageFragment extends MessageBaseFragment<IGroupMessageView, GroupMessagePresenter> implements IGroupMessageView{

    @BindView(R.id.group_call_activity_member_info)
    ImageView onlineMembers;

    @BindView(R.id.progress_group_call)
    RelativeLayout progressGroupCall;

    @BindView(R.id.tv_pre_speak)
    TextView tv_pre_speak;

    @BindView(R.id.ll_living)
    LinearLayout ll_living;

    @BindView(R.id.tv_living_number)
    TextView tv_living_number;

    @BindView(R.id.group_live_history)
    ImageView group_live_history;

    @BindView(R.id.group_call_time_progress)
    RoundProgressBarWidthNumber groupCallTimeProgress;

    private boolean isCurrentGroup;
    protected static final int REQUEST_RECORD_CODE = 999;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    //获取组内正在上报人数的间隔时间
    private static final long GET_GROUP_LIVING_INTERVAL_TIME = 20*1000;

    @Override
    protected int getLayoutResID(){
        return R.layout.fragment_message;
    }

    @Override
    protected void initViews(View view){
        super.initViews(view);
        group_live_history.setOnClickListener(v -> {
            goToVideoLiveList(false);
        });
        ll_living.setOnClickListener(v -> {
            goToVideoLiveList(true);
        });
        ptt.setOnTouchListener(mOnTouchListener);

        //组内在线成员列表
        onlineMembers.setOnClickListener(v -> {
            GroupMemberFragment.startGroupMemberFragment(getActivity(),userId,userName);
        });
    }

    @Override
    protected void initData(){
        super.initData();
        isCurrentGroup = (userId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
//        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateMainFrgamentPTTButtonHandler.class,false);
        getGroupLivingList();
        refreshPtt();
    }

    /**
     * 获取组内正在上报人数
     */
    private void getGroupLivingList(){
        long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(userId);
        MyTerminalFactory.getSDK().getGroupManager().getGroupLivingList(String.valueOf(groupUniqueNo),true);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
//        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUpdateMainFrgamentPTTButtonHandler.class,true);
    }

    public static GroupMessageFragment newInstance(int userId, String userName, long uniqueNo){
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        args.putString("userName", userName);
        args.putBoolean("isGroup", true);
        args.putLong("uniqueNo",uniqueNo);
         GroupMessageFragment fragment = new GroupMessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public GroupMessagePresenter createPresenter(){
        return new GroupMessagePresenter(getContext());
    }

    /**
     * ptt按钮触摸监听
     */
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //            if (MyTerminalFactory.getSDK().getAuthManagerTwo().getLoginStatus() != AuthManagerTwo.ONLINE) {
            //                ToastUtil.showToast(GroupCallNewsActivity.this, GroupCallNewsActivity.this.getResources().getString(R.string.net_work_disconnect));
            //                return true;
            //            }

            if (!isCurrentGroup) {
//                if (MyApplication.instance.isMiniLive) {
//                    ToastUtil.showToast(GroupCallNewsActivity.this, "小窗口模式不能进行其他业务");
//                } else {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        MyTerminalFactory.getSDK().getGroupManager().changeGroup(userId);
                    }
//                }
                return true;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //先判断有没有录音的权限，没有就申请
                    if (!CheckMyPermission.selfPermissionGranted(getActivity(), Manifest.permission.RECORD_AUDIO)){
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[] {Manifest.permission.RECORD_AUDIO},REQUEST_RECORD_CODE);
                    }else {
//                        if (!PadApplication.getPadApplication().folatWindowPress && !MyApplication.instance.volumePress) {
                            pttDownDoThing();
//                        }
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getX() + v.getWidth() / 4 < 0 || event.getX() - v.getWidth() * 1.25 > 0 ||
                            event.getY() + v.getHeight() / 8 < 0 || event.getY() - v.getHeight() * 1.125 > 0) {
//                        logger.info("ACTION_MOVE，ptt按钮移动，停止组呼：" + MyApplication.instance.isPttPress);

                        if (PadApplication.getPadApplication().isPttPress) {
                            if (CheckMyPermission.selfPermissionGranted(getActivity(), Manifest.permission.RECORD_AUDIO)){
                                pttUpDoThing();
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (PadApplication.getPadApplication().isPttPress) {
                        pttUpDoThing();
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    };

    private void pttDownDoThing() {
        if (!CheckMyPermission.selfPermissionGranted(getActivity(), Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt(getActivity(), Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(getContext(), getString(R.string.text_has_no_group_call_authority));
            return;
        }

        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestGroupCall("",userId);

        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {//打开扬声器
                MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
            }
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            PadApplication.getPadApplication().isPttPress = true;
            change2PreSpeaking();
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            change2Waiting();
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(getActivity(), resultCode);
        }

    }

    @Override
    public void change2Waiting() {
        if(isCurrentGroup){
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_pre);
        }
        ptt.setEnabled(true);
    }

    public void change2PreSpeaking() {
        if(isCurrentGroup){
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_pre);
            ptt.setText("PTT");
            TextViewCompat.setTextAppearance(ptt, R.style.white);
        }
        ptt.setEnabled(true);
        if (PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.LISTENING) {
            return;
        }
        allViewDefault();
        if (PadApplication.getPadApplication().getGroupListenenState() != GroupCallListenState.LISTENING) {
            tv_pre_speak.setVisibility(View.VISIBLE);
        } else {
            tv_pre_speak.setVisibility(View.GONE);
        }
    }

    @Override
    public void change2Speaking() {
        if(isCurrentGroup){
            ptt.setText("PTT");
            TextViewCompat.setTextAppearance(ptt, R.style.white);
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_speak);
        }

        allViewDefault();
        progressGroupCall.setVisibility(View.VISIBLE);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //                groupCallTimeProgress = new RoundProgressBarWidthNumber(context);
                int progress = groupCallTimeProgress.getProgress();
                groupCallTimeProgress.setProgress(--progress);
                if (progress < 0) {
                    this.removeMessages(1);
                }
                sendEmptyMessageDelayed(1, 100);
            }
        };
        mHandler.sendEmptyMessage(1);
    }

    @Override
    public void change2Listening() {
        if(isCurrentGroup){
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
            ptt.setText(R.string.button_press_to_line_up);
            TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
        }

        if (PadApplication.getPadApplication().isPttPress) {
            pttUpDoThing();
        }

        allViewDefault();
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {//没有组呼听的功能不显示通知
            //            if (speakingId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
            //                //设置说话人名字,在组呼来的handler中设置
            //                ll_speaker.setVisibility(View.VISIBLE);
            //                tv_scan.setVisibility(View.VISIBLE);
            //                tv_scan.setText(DataUtil.getGroupByGroupNo(speakingId).name);
            //                img_scan.setVisibility(View.VISIBLE);
            //                tv_speaker.setText(speakingName + "");
            //                ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
            //                ptt.setText(R.string.button_change_to_this_group_and_speak);
            //                TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
            //
            //            } else {
//            ll_speaker.setVisibility(View.VISIBLE);
//            tv_scan.setVisibility(View.GONE);
//            img_scan.setVisibility(View.GONE);
//            tv_speaker.setText(speakingName + "");
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
            ptt.setText(R.string.button_press_to_line_up);
            TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
            //            }
        }
    }

    @Override
    public void refreshPtt(){
        //先判断权限
        if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            //再判断是否为当前组
            if(isCurrentGroup){
                Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(userId);
                //响应组  普通用户  不在响应状态
                if(ResponseGroupType.RESPONSE_TRUE.toString().equals(groupByGroupNo.getResponseGroupType()) &&
                        !groupByGroupNo.isHighUser() &&
                        !TerminalFactory.getSDK().getGroupCallManager().getActiveResponseGroup().contains(userId)){
                    change2Forbid();
                }else {
                    stateView();
                }
            }else {
                switchToGroup();
            }
        }else {
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
            ptt.setText(R.string.text_no_group_calls);
        }
    }

    @Override
    public void changeGroup(int errorCode, String errorDesc) {
        if (errorCode == 0 || errorCode == SignalServerErrorCode.INVALID_SWITCH_GROUP.getErrorCode()) {
            TextViewCompat.setTextAppearance(ptt, R.style.funcation_top_btn_text);
            isCurrentGroup = true;
            refreshPtt();
        } else {
            isCurrentGroup = false;
            ToastUtil.showToast(getActivity(), errorDesc);
        }
    }

    private void change2Forbid() {//禁止组呼，不是遥毙
        if(isCurrentGroup){
            ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
            ptt.setText(R.string.text_no_group_calls);
            TextViewCompat.setTextAppearance(ptt, R.style.function_wait_text);
        }
        ptt.setEnabled(false);
        if (PadApplication.getPadApplication().isPttPress) {
            pttUpDoThing();
        }
    }

    private void stateView() {
        switch (TerminalFactory.getSDK().getGroupCallManager().getGroupCallSpeakStateMachine().getCurrentState()) {
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
        switch (TerminalFactory.getSDK().getGroupCallManager().getGroupCallListenStateMachine().getCurrentState()) {
            case IDLE:
                change2Silence();
                break;
            case LISTENING:
                change2Listening();
                break;
            default:
                break;
        }
    }

    @Override
    public void change2Silence() {
        if (ptt != null) {
            if(isCurrentGroup){
                ptt.setBackgroundResource(R.drawable.shape_news_ptt_listen);
                ptt.setText(R.string.text_ptt);
                TextViewCompat.setTextAppearance(ptt, R.style.funcation_top_btn_text);
            }
            ptt.setEnabled(true);
        }

        if (PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.LISTENING) {
            return;
        }
        allViewDefault();

        if (!TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER))) {
//            ll_speaker.setVisibility(View.GONE);
//            tv_speaker.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
        }
    }

    private void allViewDefault() {
        mHandler.removeMessages(1);
        if (tv_pre_speak == null){
            return;
        }
        tv_pre_speak.setVisibility(View.GONE);
        progressGroupCall.setVisibility(View.GONE);
        groupCallTimeProgress.setProgress(605);
//        ll_speaker.setVisibility(View.GONE);
    }

    private void switchToGroup(){
        ptt.setText("切到此组 说话");
        ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
        TextViewCompat.setTextAppearance(ptt, R.style.ptt_gray);
    }

    private void pttUpDoThing() {
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            return;
        }

        if (PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.LISTENING) {
            change2Listening();
        } else {
            change2Silence();
        }
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
    }


    @Override
    public void getGroupLivingList(List<TerminalMessage> beanList, int resultCode, String resultDesc, boolean forNumber) {
        mHandler.post(() -> {
            if(resultCode == BaseCommonCode.SUCCESS_CODE && !beanList.isEmpty()){
                //正在上报的人
                if(tv_living_number!=null){
                    tv_living_number.setText(String.format(GroupMessageFragment.this.getString(R.string.group_living_number),beanList.size()));
                }
                if(ll_living!=null){
                    ll_living.setVisibility(View.VISIBLE);
                }
            }else{
                //没有正在上报的人
                if(tv_living_number!=null){
                    tv_living_number.setText("");
                }
                if(ll_living!=null){
                    ll_living.setVisibility(View.GONE);
                }
            }
        });
        if(forNumber){
            mHandler.postDelayed(() -> getGroupLivingList(),GET_GROUP_LIVING_INTERVAL_TIME);
        }
    }

    /**
     * 跳转到组内上报列表
     * @param isGroupVideoLiving 是否是正在上报列表
     */
    private void goToVideoLiveList(boolean isGroupVideoLiving){
        GroupVideoLiveListFragment fragment = new GroupVideoLiveListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.IS_GROUP_VIDEO_LIVING,isGroupVideoLiving);
        bundle.putInt(Constants.GROUP_ID,userId);
        fragment.setArguments(bundle);
        FragmentManage.startFragment(getActivity(), fragment);
    }



}
