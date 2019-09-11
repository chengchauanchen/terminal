package com.vsxin.terminalpad.mvp.ui.fragment;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.presenter.SelectMemberPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ISelectMemberView;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;
import com.vsxin.terminalpad.mvp.entity.InviteMemberExceptList;
import com.vsxin.terminalpad.mvp.entity.InviteMemberLiverMember;
import com.vsxin.terminalpad.mvp.ui.adapter.SelectAdapter;
import com.vsxin.terminalpad.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ApkUtil;
import ptt.terminalsdk.tools.ToastUtil;


/**
 * @author <p>
 * 选择成员
 */
public class SelectMemberFragment extends MvpFragment<ISelectMemberView, SelectMemberPresenter> implements ISelectMemberView {

    private static final String FRAGMENT_TAG = "selectMember";

    private Handler mHandler = new Handler(Looper.getMainLooper());
    //取消按钮
    @BindView(R.id.bt_cancel)
    TextView btCancel;
    //确定按钮
    @BindView(R.id.bt_sure)
    TextView btSure;
    //已经选择的成员布局
    @BindView(R.id.ll_select)
    LinearLayout llSelect;
    //已经选择的成员的列表
    @BindView(R.id.select_recyclerview)
    RecyclerView selectRecyclerview;
    //已经选择的成员布局分割线
    @BindView(R.id.ll_select_line)
    View llSelectLine;

    private int[] tabLayoutIds = new int[]{R.id.is_pc, R.id.is_jingwutong, R.id.is_uav, R.id.is_recoder, R.id.is_lte};
    private List<TextView> tabs = new ArrayList<>();
    private int[] tabIds = new int[]{R.id.pc_tv, R.id.jingwutong_tv, R.id.uav_tv, R.id.recoder_tv, R.id.lte_tv};
    private List<View> lines = new ArrayList<>();
    private int[] lineIds = new int[]{R.id.pc_line, R.id.jingwutong_line, R.id.uav_line, R.id.recoder_line, R.id.lte_line};

    //类型：请求图像，上报图像，创建临时组，推送图像等等
    private String type;
    //是否是在上报图像中
    private boolean pushing;
    //是否是在观看图像中
    private boolean pulling;
    //不包含的成员列表
    private List<Integer> exceptList = new ArrayList<>();
    //是否观看的是gb28181
    private boolean gb28181Pull;
    //是否是组内上报图像
    private boolean isGroupPushLive;
    //观看的gb28181时的收到的message
    private TerminalMessage oldTerminalMessage;
    //观看时上报人的信息
    private InviteMemberLiverMember liverMember;

    //根据不同的业务显示不同的tab类型
    private List<String> tabTypes = new ArrayList<>();
    //tab的文字类型
    private List<String> tabText = new ArrayList<>();
    //搜索页面的类型
    private List<Integer> searchTypes = new ArrayList<>();

    //fragment集合
    private List<BaseFragment> fragments = new ArrayList<>();

    //当前显示的列表
    private int currentIndex = 0;
    //当前显示的fragment
    private BaseFragment currentFragment;

    //已选择的成员列表的adapter
    private SelectAdapter selectAdapter;
    //已选择的成员列表的数据
    private ArrayList<ContactItemBean> selectedMembers = new ArrayList<>();

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_select_member;
    }

    @Override
    protected void initViews(View view) {
        getPresenter().registReceiveHandler();
        //获取传参
        getArgumentsData();
        //初始化已经选择的布局
        initSelectedView();
        //根据类型和状态显示不同的布局
        setTabTextView(view);
        //初始化显示的fragment
        initFragment();
    }
    /**
     * 获取传参
     */
    private void getArgumentsData() {
        Bundle bundle = getArguments();
        //是请求图像还是上报图像
        type = bundle.getString(Constants.TYPE);
        //是否正在上报图像
        pushing = bundle.getBoolean(Constants.PUSHING, false);
        //是否正在观看图像
        pulling = bundle.getBoolean(Constants.PULLING, false);
        //是否是组内上报图像
        isGroupPushLive = bundle.getBoolean(Constants.IS_GROUP_PUSH_LIVING, false);
        //是否是观看GB28181
        gb28181Pull = bundle.getBoolean(Constants.GB28181_PULL, false);
        if (gb28181Pull) {
            oldTerminalMessage = (TerminalMessage) bundle.getSerializable(Constants.TERMINALMESSAGE);
        }
        //观看时上报人的信息
        if (pulling) {
            liverMember = (InviteMemberLiverMember) bundle.getSerializable(Constants.LIVE_MEMBER);
        }
        //获取不显示的设备
        InviteMemberExceptList bean = (InviteMemberExceptList) bundle.getSerializable(Constants.INVITE_MEMBER_EXCEPT_UNIQUE_NO);
        if (bean != null && bean.getList() != null) {
            exceptList.clear();
            exceptList.addAll(bean.getList());
        }
    }
    /**
     * 初始化已经选择的布局
     */
    private void initSelectedView() {
        selectRecyclerview.setLayoutManager(new LinearLayoutManager(MyTerminalFactory.getSDK().application, OrientationHelper.HORIZONTAL, false));
        selectAdapter = new SelectAdapter(this.getContext(), selectedMembers);
        selectRecyclerview.setAdapter(selectAdapter);
        llSelect.setVisibility(View.GONE);
    }

    /**
     * 根据类型和状态显示不同的布局
     * <p>
     * 请求图像：警务通、无人机、执法记录仪
     * 上报图像：组、PC、警务通、HDMI
     * 推送图像：组、PC、警务通、HDMI
     */
    private void setTabTextView(View view) {
        tabTypes.clear();
        List<Integer> mSearchTypes = null;
        List<String> mTabTypes = null;
        if (Constants.PUSH.equals(type)) {
            //推送图像和上报图像
            if (ApkUtil.isAnjian()) {
                mSearchTypes = Arrays.asList(Constants.TYPE_CHECK_SEARCH_GROUP, Constants.TYPE_CHECK_SEARCH_PC,
                        Constants.TYPE_CHECK_SEARCH_POLICE, Constants.TYPE_CHECK_SEARCH_HDMI);
                mTabTypes = Arrays.asList(Constants.TYPE_GROUP_STRING, TerminalMemberType.TERMINAL_PC.toString(),
                        TerminalMemberType.TERMINAL_PHONE.toString(), TerminalMemberType.TERMINAL_HDMI.toString());
                tabText = Arrays.asList(getResources().getStringArray(R.array.invite_member_tab_text_push_image_anjian));
            } else {
                mSearchTypes = Arrays.asList(Constants.TYPE_CHECK_SEARCH_GROUP, Constants.TYPE_CHECK_SEARCH_PC,
                        Constants.TYPE_CHECK_SEARCH_POLICE, Constants.TYPE_CHECK_SEARCH_HDMI);
                mTabTypes = Arrays.asList(Constants.TYPE_GROUP_STRING, TerminalMemberType.TERMINAL_PC.toString(),
                        TerminalMemberType.TERMINAL_PHONE.toString(), TerminalMemberType.TERMINAL_HDMI.toString());
                tabText = Arrays.asList(getResources().getStringArray((ApkUtil.showLteApk()) ?
                        R.array.invite_member_tab_text_push_image_lte : R.array.invite_member_tab_text_push_image));
            }
        } else if (Constants.PULL.equals(type)) {
            if (pulling) {
                //推送图像
                if (ApkUtil.isAnjian()) {
                    mSearchTypes = Arrays.asList(Constants.TYPE_CHECK_SEARCH_GROUP, Constants.TYPE_CHECK_SEARCH_PC,
                            Constants.TYPE_CHECK_SEARCH_POLICE, Constants.TYPE_CHECK_SEARCH_HDMI);
                    mTabTypes = Arrays.asList(Constants.TYPE_GROUP_STRING, TerminalMemberType.TERMINAL_PC.toString(),
                            TerminalMemberType.TERMINAL_PHONE.toString(), TerminalMemberType.TERMINAL_HDMI.toString());
                    tabText = Arrays.asList(getResources().getStringArray(R.array.invite_member_tab_text_push_image_anjian));
                } else {
                    mSearchTypes = Arrays.asList(Constants.TYPE_CHECK_SEARCH_GROUP, Constants.TYPE_CHECK_SEARCH_PC,
                            Constants.TYPE_CHECK_SEARCH_POLICE, Constants.TYPE_CHECK_SEARCH_HDMI);
                    mTabTypes = Arrays.asList(Constants.TYPE_GROUP_STRING, TerminalMemberType.TERMINAL_PC.toString(),
                            TerminalMemberType.TERMINAL_PHONE.toString(), TerminalMemberType.TERMINAL_HDMI.toString());
                    tabText = Arrays.asList(getResources().getStringArray((ApkUtil.showLteApk()) ?
                            R.array.invite_member_tab_text_push_image_lte : R.array.invite_member_tab_text_push_image));
                }
            } else {
                //请求图像
                if (ApkUtil.isAnjian()) {
                    mSearchTypes = Arrays.asList(Constants.TYPE_CHECK_SEARCH_POLICE, Constants.TYPE_CHECK_SEARCH_UAV);
                    mTabTypes = Arrays.asList(TerminalMemberType.TERMINAL_PHONE.toString(), TerminalMemberType.TERMINAL_UAV.toString());
                    tabText = Arrays.asList(getResources().getStringArray(R.array.invite_member_tab_text_pull_anjian));
                } else {
                    mSearchTypes = Arrays.asList(Constants.TYPE_CHECK_SEARCH_POLICE, Constants.TYPE_CHECK_SEARCH_UAV,
                            Constants.TYPE_CHECK_SEARCH_RECODER);
                    mTabTypes = Arrays.asList(TerminalMemberType.TERMINAL_PHONE.toString(), TerminalMemberType.TERMINAL_UAV.toString(),
                            TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString());
                    tabText = Arrays.asList(getResources().getStringArray((ApkUtil.showLteApk()) ?
                            R.array.invite_member_tab_text_pull_lte : R.array.invite_member_tab_text_pull));
                }

            }
        }
        searchTypes.addAll(mSearchTypes);
        tabTypes.addAll(mTabTypes);
        //包含lte
        if ((!ApkUtil.isAnjian()) && ApkUtil.showLteApk()) {
            searchTypes.add(Constants.TYPE_CHECK_SEARCH_LTE);
            tabTypes.add(TerminalMemberType.TERMINAL_LTE.toString());
        }
        tabs.clear();
        lines.clear();
        for (int i = 0; i < tabIds.length; i++) {
            RelativeLayout relativeLayout = view.findViewById(tabLayoutIds[i]);
            TextView textView = view.findViewById(tabIds[i]);
            textView.setOnClickListener(new MyTabOnClickListener(i));
            View line = view.findViewById(lineIds[i]);
            if (i < tabTypes.size()) {
                textView.setText(tabText.get(i));
                tabs.add(textView);
                lines.add(line);
                relativeLayout.setVisibility(View.VISIBLE);
            } else {
                relativeLayout.setVisibility(View.GONE);
            }
        }
        //设置tab切换之后view的改变
        setTabView(currentIndex);
    }

    /**
     * 初始化fragment
     */
    private void initFragment() {
        fragments.clear();
        for (String type: tabTypes) {
            fragments.add(null);
        }
        currentFragment = getBaseFragmentByType();
            //显示Fragment
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.contacts_viewPager, currentFragment).show(currentFragment).commit();
        fragments.set(currentIndex,currentFragment);
    }

    /**
     * 根据类型获取对应的fragment
     * @return
     */
    private BaseFragment getBaseFragmentByType(){
        if (currentIndex<0||currentIndex >= tabTypes.size()) {
            currentIndex = 0;
        }
        if (TextUtils.equals(Constants.TYPE_GROUP_STRING, tabTypes.get(currentIndex))) {
            return new GroupListFragment();
        } else {
           return MemberListSelectFragment.newInstance(tabTypes.get(currentIndex));
        }
    }

    /**
     * 设置tab切换之后view的改变
     *
     * @param currentIndex
     */
    private void setTabView(int currentIndex) {
        for (int i = 0; i < tabs.size(); i++) {
            if (currentIndex == i) {
                tabs.get(i).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                lines.get(i).setVisibility(View.VISIBLE);
            } else {
                tabs.get(i).setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                lines.get(i).setVisibility(View.GONE);
            }
        }
    }

    /**
     * 切换fragment
     *
     * @param to
     */
    private void switchFragment(BaseFragment to) {
        if (currentFragment != to) {
            if (to.isAdded()) {
                getChildFragmentManager().beginTransaction().hide(currentFragment).show(to).commit();
            } else {
                getChildFragmentManager().beginTransaction().hide(currentFragment).add(R.id.contacts_viewPager, to).show(to).commit();
            }
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public SelectMemberPresenter createPresenter() {
        return new SelectMemberPresenter(getContext());
    }

    /**
     * 点击事件
     *
     * @param view
     */
    @OnClick({R.id.bt_cancel, R.id.bt_sure,R.id.ll_select})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_cancel:
                getFragmentManager().popBackStack();
                break;
            case R.id.bt_sure:
                goToSure();
                break;
            case R.id.ll_select:
                //点击查看已经选择的成员列表
                SelectedMemberFragment.startSelectedMemberFragment(getActivity(),selectedMembers);
                break;
        }
    }

    /**
     * 点击确定按钮
     */
    private void goToSure() {
        if (Constants.PUSH.equals(type)) {
            if (pushing) {
                getPresenter().inviteToWatchLive(pulling,liverMember);
            } else {
                if (PadApplication.getPadApplication().usbAttached) {
                    getPresenter().requestStartLive(Constants.UVC_PUSH);
                } else {
                    if (Constants.HYTERA.equals(Build.MODEL)) {
                        getPresenter().requestStartLive(Constants.RECODER_PUSH);
                    } else {
                        getPresenter().requestStartLive(Constants.PHONE_PUSH);
                    }
                }
            }
        } else if (Constants.PULL.equals(type)) {
            if (pulling) {
                if (gb28181Pull) {
                    getPresenter().inviteOtherMemberToWatch(oldTerminalMessage);
                } else {
                    getPresenter().inviteToWatchLive(pulling,liverMember);
                }
            } else {
                getPresenter().requestOtherStartLive();
            }
        }
    }

    /**
     * tab点击事件
     */
    private class MyTabOnClickListener implements View.OnClickListener {

        private int index = 0;

        public MyTabOnClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onClick(View v) {
            currentIndex = index;
           //显示、隐藏fragment
            if(currentIndex<fragments.size()){
                BaseFragment fragment = fragments.get(currentIndex);
                if (fragment == null) {
                    fragment = getBaseFragmentByType();
                    fragments.set(currentIndex,fragment);
                }
                switchFragment(fragment);
                currentFragment = fragment;
            }
            setTabView(currentIndex);
        }
    }

    /**
     * 设置按钮显示已选择的数量
     */
    private void setButtonCount() {
        if(selectedMembers.isEmpty()){
            btSure.setText(getString(R.string.text_sure));
            llSelect.setVisibility(View.GONE);
        }else{
            btSure.setText(String.format(getString(R.string.button_sure_number),selectedMembers.size()));
            llSelect.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 关闭页面
     */
    @Override
    public void removeView() {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unregistReceiveHandler();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 组列表的选择变化通知
     * @param selectedMembers
     */
    @Override
    public void selectedView(List<ContactItemBean> selectedMembers) {
        mHandler.post(() -> {
            selectAdapter.notifyDataSetChanged();
            setButtonCount();
//            refreshAllSelectedStatus();
//            fromSearchViewGoBackToSeletView();
        });
    }

    /**
     * 获取已选择的成员的数据
     * @return
     */
    @Override
    public List<ContactItemBean> getSelectedMembers() {
        return selectedMembers;
    }

    @Override
    public void showMsg(String msg) {
        ToastUtil.showToast(getActivity(), msg);
    }

    @Override
    public void showMsg(int resouce) {
        ToastUtil.showToast(getActivity(), getString(resouce));
    }

    /**
     * 开启 startSelectMemberFragment
     *
     * @param fragmentActivity
     */
    public static void startSelectMemberFragment(FragmentActivity fragmentActivity,Bundle bundle) {
        SelectMemberFragment selectMemberFragment = new SelectMemberFragment();
//        Bundle args = new Bundle();
//        args.putSerializable(PARAM_JSON, json);
//        args.putSerializable(PARAM_ENUM,typeEnum);
        selectMemberFragment.setArguments(bundle);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        //replace 会将上一个Fragment干掉
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_layer_member_info, selectMemberFragment, FRAGMENT_TAG);
        fragmentTransaction.addToBackStack(fragmentActivity.getClass().getName());
        fragmentTransaction.commit();
    }
}
