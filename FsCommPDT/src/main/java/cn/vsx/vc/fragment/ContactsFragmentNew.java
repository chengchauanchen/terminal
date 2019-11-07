package cn.vsx.vc.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalContactTab;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupByNoHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.DialPopupwindow;
import cn.vsx.vc.view.MyTabLayout.MyTabLayout;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;
import static cn.vsx.vc.view.MyTabLayout.MyTabLayout.MODE_FIXED;
import static cn.vsx.vc.view.MyTabLayout.MyTabLayout.MODE_SCROLLABLE;

@SuppressLint("ValidFragment")
public class ContactsFragmentNew extends BaseFragment implements View.OnClickListener {

    TextView setting_group_name;

    ImageView add_icon;


    FrameLayout viewPager;

    ImageView icon_laba;

    ImageView voice_image;



    ImageButton imgbtn_dial;

    private Fragment currentFragment;
    NewMainActivity activity;
    private Handler mHandler = new Handler();
    private DialPopupwindow dialPopupwindow;
    private boolean soundOff;
    private MyTabLayout tabLayout;
    private List<String> titles = new ArrayList<>();
    private List<BaseFragment> fragments = new ArrayList<>();
    private BaseFragment lastFragment;
    private List<MyTabLayout.Tab> tabs = new ArrayList<>();
    private NewGroupFragment groupFragmentNew;//群组
    private NewPoliceAffairsFragment policeAffairsFragment;//警务通

    public ContactsFragmentNew() {
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_contacts;
    }

    @Override
    public void initView() {
        imgbtn_dial = (ImageButton) mRootView.findViewById(R.id.imgbtn_dial);
        tabLayout = mRootView.findViewById(R.id.tabLayout);
        voice_image = (ImageView) mRootView.findViewById(R.id.voice_image);
        icon_laba = (ImageView) mRootView.findViewById(R.id.icon_laba);
        viewPager = (FrameLayout) mRootView.findViewById(R.id.contacts_viewPager);
        add_icon = (ImageView) mRootView.findViewById(R.id.add_icon);
        setting_group_name = (TextView) mRootView.findViewById(R.id.setting_group_name);


        activity = (NewMainActivity) getActivity();

        setVideoIcon();

        setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));

        dialPopupwindow = new DialPopupwindow(context);
        voice_image.setImageResource(BitmapUtil.getVolumeImageResourceByValue(false));
        voice_image.setOnClickListener(view -> {
            if (!soundOff) {
                voice_image.setImageResource(R.drawable.volume_off_call);
                TerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true, 1);
                soundOff = true;
            } else {
                voice_image.setImageResource(R.drawable.horn);
                TerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false, 1);
                soundOff = false;
            }
        });
    }


    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupByNoHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
        imgbtn_dial.setOnClickListener(v -> {
            if (dialPopupwindow == null) {
                dialPopupwindow = new DialPopupwindow(context);
            }
            dialPopupwindow.showAtLocation(((Activity) context).findViewById(R.id.rg), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        });

        activity.setOnBackListener(() -> {
            if (groupFragmentNew.isVisible()) {
                groupFragmentNew.onBack();
            }else if (policeAffairsFragment.isVisible()) {
                policeAffairsFragment.onBack();
            }else {
                ((TerminalFragment) lastFragment).onBack();
            }
        });

    }

    /*** 自己组呼返回的消息 **/
    private ReceiveRequestGroupCallConformationHandler mReceiveRequestGroupCallConformationHandler = (methodResult, resultDesc, groupId) -> mHandler.post(() -> {
        if (methodResult == 0) {
            showViewWhenGroupCall(getString(R.string.text_I_am_talking));
            setViewEnable(false);
        }
    });

    private void setViewEnable(boolean isEnable) {
//        newsList.setEnabled(isEnable);
        add_icon.setEnabled(isEnable);
    }

    /***  停止组呼的时候隐藏View **/
    private void hideViewWhenStopGroupCall() {
        icon_laba.setVisibility(View.GONE);
        setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
    }

    /***  组呼的时候显示View **/
    private void showViewWhenGroupCall(final String speakerName) {
        icon_laba.setVisibility(View.VISIBLE);
        setting_group_name.setText(speakerName);
    }

    @Override
    public void initData() {
        imgbtn_dial.setVisibility(View.GONE);
        FragmentManager childFragmentManager = getChildFragmentManager();
        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        //添加tab
        titles.add("群组");
        titles.add("警务通");

        groupFragmentNew = new NewGroupFragment();
        fragments.add(groupFragmentNew);
        MyTabLayout.Tab groupTab = tabLayout.newTab();

        groupTab.setText(titles.get(0));
        tabLayout.addTab(groupTab);
        tabs.add(groupTab);

        policeAffairsFragment = new NewPoliceAffairsFragment();
        fragments.add(policeAffairsFragment);
        MyTabLayout.Tab policeTab = tabLayout.newTab();
        policeTab.setText(titles.get(1));
        tabLayout.addTab(policeTab);
        tabs.add(policeTab);


        List<TerminalContactTab> terminalContactTabs = TerminalFactory.getSDK().getDataManager().getTerminalContactTabs();
        for(int i = 0; i < terminalContactTabs.size(); i++){
            titles.add(terminalContactTabs.get(i).getTabName());
            MyTabLayout.Tab tab = tabLayout.newTab();
            tab.setText(terminalContactTabs.get(i).getTabName());
            tabLayout.addTab(tab);
            tabs.add(tab);
            TerminalFragment terminalFragment = TerminalFragment.newInstance(terminalContactTabs.get(i).getTerminalMemberTypes());
            fragments.add(terminalFragment);
        }

        int screenWidth=getResources().getDisplayMetrics().widthPixels;
        //滑动模式时最小宽度
        int scrollableTabMinWidth = DensityUtil.dip2px(MyApplication.instance, 72);
        int tabCount = screenWidth / scrollableTabMinWidth;
        if(tabs.size()>tabCount){
            tabLayout.setTabMode(MODE_SCROLLABLE);
            tabLayout.setTabGravity(MyTabLayout.GRAVITY_CENTER);
        }else {
            tabLayout.setTabGravity(MyTabLayout.GRAVITY_FILL);
            tabLayout.setTabMode(MODE_FIXED);
        }

        transaction.add(R.id.contacts_viewPager, groupFragmentNew).show(groupFragmentNew).commit();
        lastFragment = groupFragmentNew;

        tabLayout.addOnTabSelectedListener(new MyTabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(MyTabLayout.Tab tab){
                logger.info("onTabSelected");
                int position = tab.getPosition();
                if(position == 0){
                    imgbtn_dial.setVisibility(View.GONE);
                }else {
                    imgbtn_dial.setVisibility(View.VISIBLE);
                }
                BaseFragment currentFrgment = fragments.get(position);
                if(lastFragment !=currentFrgment ){
                    tab.setSelected(true);
                    switchFragment(lastFragment,currentFrgment);
                    lastFragment = currentFrgment;
                }
            }

            @Override
            public void onTabUnselected(MyTabLayout.Tab tab){
                tab.setSelected(false);
                logger.info("onTabUnselected");
            }

            @Override
            public void onTabReselected(MyTabLayout.Tab tab) {
                logger.info("onTabReselected");
            }
        });

    }

    private void setVideoIcon() {
        MyTopRightMenu.offerObject().initview(add_icon, activity);
        //没有上报和回传图像的权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())
                && !MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())) {
            add_icon.setVisibility(View.INVISIBLE);
        } else {
            add_icon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupByNoHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
        super.onDestroyView();
    }

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> mHandler.post(() -> setVideoIcon());
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            mHandler.post(() -> setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0))));
        }
    };
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, String memberName, int groupId, String groupName,CallMode currentCallMode, long uniqueNo) {
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                mHandler.post(() -> {
                    setting_group_name.setText(memberName);
                    icon_laba.setVisibility(View.VISIBLE);
                });
            }

        }
    };
    /**
     * 音量改变
     */
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(boolean isVolumeOff, int status) {
            if (isVolumeOff) {
                voice_image.setImageResource(R.drawable.volume_off_call);
                soundOff = true;
            } else {
                voice_image.setImageResource(R.drawable.horn);
                soundOff = false;
            }
        }
    };
    /***  自己组呼结束 **/
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> mHandler.post(() -> {
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            return;
        }
        hideViewWhenStopGroupCall();
        setViewEnable(true);
    });
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {

        @Override
        public void handler(int reasonCode) {
            mHandler.post(() -> {
                setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
                icon_laba.setVisibility(View.GONE);
            });
        }
    };
    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
            });
        }
    };

    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            mHandler.post(() -> {
                if (!TempGroupType.ACTIVITY_TEAM_GROUP.toString().equals(tempGroupType)) {
                    if (isAdd) {
                        if (isLocked || isSwitch || isScan) {
                            setting_group_name.setText(tempGroupName);
                        }
                    } else {
                        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                        setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
                        //这里播放提示音大概率会没有声音，原因是soundpool播放了多路声音，这个声音被后面的覆盖(后面调的播放，前面的不播)
                        //有两种解决办法，1.延迟播放；2.将池子的大小设为2(优先级的参数目前是无效的)
                        ToastUtil.showToast(getActivity(), tempGroupName + "到期");
                        PromptManager.getInstance().playTempGroupExpire();
                    }
                }
            });
        }
    };

    private ReceiveGetGroupByNoHandler receiveGetGroupByNoHandler = group -> mHandler.post(new Runnable() {
        @Override
        public void run() {
            setting_group_name.setText(group.getName());
        }
    });

    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            mHandler.post(() -> {
                int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
            });
        }
    };


    public void switchFragment(Fragment from, Fragment to) {
        FragmentManager childFragmentManager = getChildFragmentManager();
        if (currentFragment != to) {
            currentFragment = to;
            FragmentTransaction transaction = childFragmentManager.beginTransaction();
            if (!currentFragment.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.contacts_viewPager, to).commit(); // 隐藏当前的fragment，add下一个Fragment
            } else {
                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
    }

    /**
     * 获取是否是显示或者隐藏的状态
     * @return
     */
    public boolean getHiddenState(){
        return !(fragments.get(0)!=null && !fragments.get(0).isHidden());
    }

}
