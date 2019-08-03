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
import com.vsxin.terminalpad.mvp.contract.presenter.MemberInfoPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MemberListPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.contract.view.IMemberInfoView;
import com.vsxin.terminalpad.mvp.contract.view.IMemberListView;
import com.vsxin.terminalpad.mvp.ui.activity.MainMapActivity;

import butterknife.BindView;

/**
 * @author qzw
 *
 * 地图气泡点击-成员详情页
 */
public class MemberListFragment extends MvpFragment<IMemberListView, MemberListPresenter> implements IMemberListView {

    private static final String PARAM_JSON = "paramJson";
    private static final String FRAGMENT_TAG = "MemberList";

    @BindView(R.id.iv_close)
    ImageView iv_close;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_member_list_info;
    }

    @Override
    protected void initViews(View view) {
        iv_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMemberInfoFragment(getActivity());
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public MemberListPresenter createPresenter() {
        return new MemberListPresenter(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLogger().info("MemberListFragment 销毁了");
    }

    /**
     * 开启 MemberInfoFragment
     * @param fragmentActivity
     * @param json
     */
    public static void startMemberInfoFragment(FragmentActivity fragmentActivity, String json) {
        MemberListFragment memberListFragment = new MemberListFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_JSON, json);
        memberListFragment.setArguments(args);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, memberListFragment,FRAGMENT_TAG);
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
