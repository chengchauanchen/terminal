package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.MemberInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMemberInfoView;
import com.vsxin.terminalpad.mvp.entity.MemberInfoBean;
import com.vsxin.terminalpad.mvp.ui.activity.MainMapActivity;
import com.vsxin.terminalpad.utils.TimeUtil;

import butterknife.BindView;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Account;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-成员详情页
 */
public class MemberInfoFragment extends MvpFragment<IMemberInfoView, MemberInfoPresenter> implements IMemberInfoView {

    private static final String PARAM_JSON = "paramJson";
    private static final String PARAM_ENUM = "paramEnum";
    private static final String FRAGMENT_TAG = "memberInfo";

    @BindView(R.id.iv_close)
    ImageView iv_close;

    @BindView(R.id.iv_type_icon)
    ImageView iv_type_icon;

    @BindView(R.id.tv_member_name)
    TextView tv_member_name;//名称

    @BindView(R.id.tv_department)
    TextView tv_department;//部门

    @BindView(R.id.tv_phone)
    TextView tv_phone;//电话号

    @BindView(R.id.tv_speed)
    TextView tv_speed;//速度

    @BindView(R.id.tv_time)
    TextView tv_time;//时间

    @BindView(R.id.iv_individual_call)
    ImageView iv_individual_call;//发起个呼

    @BindView(R.id.iv_push_video)
    ImageView iv_push_video;//视频上报

    @BindView(R.id.iv_message)
    ImageView iv_message;//个人聊天界面
    private MemberInfoBean memberInfo;
    private MemberTypeEnum memberTypeEnum;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_member_info;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registReceiveHandler();

        memberInfo = (MemberInfoBean) getArguments().getSerializable(PARAM_JSON);
        memberTypeEnum = (MemberTypeEnum) getArguments().getSerializable(PARAM_ENUM);
        if (memberTypeEnum != null) {
            getLogger().info("memberInfo:" + new Gson().toJson(memberInfo));
            getLogger().info(memberTypeEnum.toString());
            iv_type_icon.setImageResource(memberTypeEnum.getResId());
        }
        if(memberInfo!=null){
            bindMemberInfo(memberInfo);
        }

        iv_close.setOnClickListener(v -> closeMemberInfoFragment(getActivity()));

        //发起个呼
        iv_individual_call.setOnClickListener(v -> {
            //手台个呼
            if(memberTypeEnum!=null && memberTypeEnum==MemberTypeEnum.HAND){
                getPresenter().startIndividualCall("72020850", TerminalMemberType.TERMINAL_PDT);
            }else{
                ToastUtil.showToast(getContext(),"暂不支持该设备个呼");
            }
        });

        //上报视频
        iv_push_video.setOnClickListener(v -> {
            //pushVideo();
            pullVideo();
        });

        //会话界面
        iv_message.setOnClickListener(v -> {
            //pullVideo();
        });
    }

    private void bindMemberInfo(MemberInfoBean memberInfo) {
        tv_member_name.setText(memberInfo.getName());
        tv_department.setText(TextUtils.isEmpty(memberInfo.getDeptName())?"武汉市公安局":memberInfo.getDeptName());
        //tv_phone.setText(memberInfo);
//        tv_speed.setText(memberInfo.getSpeed());
        tv_time.setText("定位时间："+ TimeUtil.getCurrentTimeYMD());
    }

    /**
     * 拉取他人上报视频
     * 自己主动请求别人上报
     */
    private void pullVideo() {
        getPresenter().pullVideo(memberTypeEnum);
    }

    /**
     * 自己主动 上报视频
     */
    private void pushVideo() {
        getPresenter().pushVideo();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void showChooseDevicesDialog(Account account, int type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getPresenter().showChooseDevicesDialog(account, type);
            }
        });
    }

    @Override
    public MemberInfoPresenter createPresenter() {
        return new MemberInfoPresenter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLogger().info("MemberInfoFragment 销毁了");
        getPresenter().unregistReceiveHandler();
        if (memberInfo != null && memberTypeEnum != null) {
            String no = memberInfo.getNo();
            String type = memberTypeEnum.getType();
            ((MainMapActivity) getContext()).closeInfoBoxToMap(no, type);
        }
    }

    /**
     * 开启 MemberInfoFragment
     *
     * @param fragmentActivity
     * @param json
     */
    public static void startMemberInfoFragment(FragmentActivity fragmentActivity, MemberInfoBean json, MemberTypeEnum typeEnum) {
        MemberInfoFragment memberInfoFragment = new MemberInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_JSON, json);
        args.putSerializable(PARAM_ENUM,typeEnum);
        memberInfoFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, memberInfoFragment,FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    /**
     * 关闭 MemberInfoFragment
     * @param fragmentActivity
     */
    public static void closeMemberInfoFragment(FragmentActivity fragmentActivity){
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        Fragment memberInfo = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(memberInfo!=null){
            fragmentTransaction.remove(memberInfo);
        }
        fragmentTransaction.commit();
    }
}
