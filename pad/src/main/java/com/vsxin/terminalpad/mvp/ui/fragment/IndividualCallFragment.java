package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.StartCallManager;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.IndividualCallPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IIndividualCallView;
import com.vsxin.terminalpad.mvp.entity.MemberInfoBean;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;
import com.vsxin.terminalpad.utils.BitmapUtil;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.HandleIdUtil;

import butterknife.BindView;
import cn.vsx.hamster.common.TerminalMemberType;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-发起个呼 等待对方接听
 */
public class IndividualCallFragment extends MvpFragment<IIndividualCallView, IndividualCallPresenter> implements IIndividualCallView {

    private static final String FRAGMENT_TAG = "IndividualCallFragment";

    private String memberName;//警员名称
    private String memberId;//警员编号

    @BindView(R.id.ll_individual_call_hangup_request)
    LinearLayout ll_individual_call_hangup_request;

    @BindView(R.id.iv_member_portrait_request)
    ImageView iv_member_portrait_request;//头像

    @BindView(R.id.tv_member_name_request)
    TextView tv_member_name_request;//名称

    @BindView(R.id.tv_member_id_request)
    TextView tv_member_id_request;//警号


    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_individual_call_request;
    }


    @Override
    protected void initViews(View view) {
        getPresenter().registerReceiveHandler();

        memberName = getArguments().getString(Constants.MEMBER_NAME);
        memberId = getArguments().getString(Constants.MEMBER_ID);
        ll_individual_call_hangup_request.setOnClickListener(v -> stopAndDestroy());
    }

    @Override
    protected void initData() {
        //呼手台
        //getPresenter().startIndividualCall("72020850", TerminalMemberType.TERMINAL_PDT);
        //呼警务通
        //StartCallManager startCallManager = new StartCallManager(getContext());
        //startCallManager.startIndividualCall("10000201",TerminalMemberType.TERMINAL_PHONE);
        setMemberinfo();
    }

    @Override
    public void stopAndDestroy() {
        getPresenter().getStartCallManager().stopIndividualCall();
        closeIndividualCallFragment(getActivity());
    }

    @Override
    public void startFullDuplexIndividualCall() {
        FullDuplexIndividualCallFragment.startFullDuplexIndividualCallFragment(getActivity(),memberName,memberId);
    }

    public void setMemberinfo(){
        iv_member_portrait_request.setImageResource(R.drawable.member_icon_new);
        tv_member_name_request.setText(memberName);
        tv_member_id_request.setText(HandleIdUtil.handleId(memberId));
    }


    /**
     * 开始半双工个呼
     */
    @Override
    public void startHalfDuplexIndividualCall() {
        HalfDuplexIndividualCallFragment fragment = new HalfDuplexIndividualCallFragment();
        Bundle args = new Bundle();
        args.putString(Constants.MEMBER_NAME, memberName);
        args.putString(Constants.MEMBER_ID, memberId);
        fragment.setArguments(args);
        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, fragment, HalfDuplexIndividualCallFragment.HDICFragment_TAG);
        fragmentTransaction.commit();
    }

    @Override
    public IndividualCallPresenter createPresenter() {
        return new IndividualCallPresenter(getContext());
    }

    /**
     * 开启 IndividualCallFragment
     *
     * @param fragmentActivity
     * @param memberName
     * @param memberId
     */
    public static void startIndividualCallFragment(FragmentActivity fragmentActivity, String memberName,String memberId) {
        IndividualCallFragment individualCallFragment = new IndividualCallFragment();
        Bundle args = new Bundle();
        args.putString(Constants.MEMBER_NAME, memberName);
        args.putString(Constants.MEMBER_ID,memberId);
        individualCallFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, individualCallFragment,FRAGMENT_TAG);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unRegisterReceiveHandler();
    }
}
