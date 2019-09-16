package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.presenter.FullDuplexIndividualCallPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IFullDuplexIndividualCallView;
import com.vsxin.terminalpad.mvp.ui.widget.IndividualCallView;
import com.vsxin.terminalpad.utils.Constants;

import butterknife.BindView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 *
 * 全双工个呼
 */
public class FullDuplexIndividualCallFragment extends MvpFragment<IFullDuplexIndividualCallView, FullDuplexIndividualCallPresenter> implements IFullDuplexIndividualCallView {

    private static final String FRAGMENT_TAG = "FullDuplexIndividualCallFragment";

    private String memberName;
    private String memberId;

    @BindView(R.id.tv_speaking_toast)
    TextView tv_speaking_toast;//对方已拒绝

    @BindView(R.id.ictv_speaking_time_speaking)
    IndividualCallView mIctvSpeakingTimeSpeaking;//计时

    @BindView(R.id.iv_individual_call_micro_mute)
    ImageView iv_individual_call_micro_mute;//静音

    @BindView(R.id.iv_individual_call_hangup_speaking)
    ImageView iv_individual_call_hangup_speaking;//挂断

    @BindView(R.id.iv_individual_call_hand_free)
    ImageView iv_individual_call_hand_free;//免提

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_individual_call_speaking;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registerReceiveHandler();

        //默认免提
        getPresenter().setSpeakPhoneOn(iv_individual_call_hand_free,true);
        //默认非静音
        getPresenter().setMicrophoneMute(iv_individual_call_micro_mute,false);

        memberName = getArguments().getString(Constants.MEMBER_NAME);
        memberId = getArguments().getString(Constants.MEMBER_ID);
        mIctvSpeakingTimeSpeaking.onStart();//开启倒计时

        //挂断
        iv_individual_call_hangup_speaking.setOnClickListener(v -> {
            ToastUtil.showToast(getContext(),"已挂断");
            getPresenter().stopIndividualCall();
            closeFragment();
        });

        //免提
        iv_individual_call_hand_free.setOnClickListener(v -> {
            boolean isSpeakerphoneOn = MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn();
            getPresenter().setSpeakPhoneOn(iv_individual_call_hand_free,!isSpeakerphoneOn);
        });

        //静音
        iv_individual_call_micro_mute.setOnClickListener(v -> {
            boolean isMicrophoneMute = MyTerminalFactory.getSDK().getAudioProxy().isMicrophoneMute();
            getPresenter().setMicrophoneMute(iv_individual_call_micro_mute,!isMicrophoneMute);
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void setSpeakingToast(String des) {
        tv_speaking_toast.setText(des);
    }

    @Override
    public void closeFragment() {
        closeIndividualCallFragment(getActivity());
    }

    @Override
    public FullDuplexIndividualCallPresenter createPresenter() {
        return new FullDuplexIndividualCallPresenter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLogger().info("TerminalInfoFragment 销毁了");
        getPresenter().unRegisterReceiveHandler();
        mIctvSpeakingTimeSpeaking.onStop();
    }

    /**
     * 开启 IndividualCallFragment
     *
     * @param fragmentActivity
     * @param memberName
     * @param memberId
     */
    public static void startFullDuplexIndividualCallFragment(FragmentActivity fragmentActivity, String memberName, String memberId) {
        FullDuplexIndividualCallFragment fullDuplexIndividualCallFragment = new FullDuplexIndividualCallFragment();
        Bundle args = new Bundle();
        args.putString(Constants.MEMBER_NAME, memberName);
        args.putSerializable(Constants.MEMBER_ID,memberId);
        fullDuplexIndividualCallFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, fullDuplexIndividualCallFragment,FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * 关闭 IndividualCallFragment
     * @param fragmentActivity
     */
    public static void closeIndividualCallFragment(FragmentActivity fragmentActivity){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        Fragment memberInfo = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(memberInfo!=null){
            fragmentTransaction.remove(memberInfo);
        }
        fragmentTransaction.commit();
    }
}
