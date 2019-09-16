package cn.vsx.vc.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.TextViewCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
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
import cn.vsx.vc.utils.ApkUtil;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.DialPopupwindow;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

@SuppressLint("ValidFragment")
public class ContactsFragmentNew extends BaseFragment implements View.OnClickListener {

    TextView setting_group_name;

    ImageView add_icon;

    RelativeLayout isGroup;

    RelativeLayout is_jingwutong;

    View groupLine;

    View shoutai_line;
    View jingwutong_line;
    View lte_line;
    View recoder_line;

    FrameLayout viewPager;

    ImageView icon_laba;

    ImageView voice_image;

    TextView group_tv;

    TextView jingwutong_tv;
    TextView lte_tv;
    TextView shoutai_tv;
    TextView recoder_tv;



    ImageButton imgbtn_dial;

    private Fragment currentFragment;
    private NewGroupFragment groupFragmentNew;//群组
    //    private PoliceAffairsFragment policeAffairsFragment;
    private NewPoliceAffairsFragment policeAffairsFragment;//警务通
    //    private HandPlatformFragment handPlatformFragment;
    private NewHandPlatformFragment handPlatformFragment;//电台
    private LteFragment lteFragment;//lte
    private RecorderFragment recorderFragment;//执法记录仪
    private FragmentManager childFragmentManager;
    NewMainActivity activity;
    private Handler mHandler = new Handler();
    private DialPopupwindow dialPopupwindow;
    private boolean soundOff;

    public ContactsFragmentNew() {
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_contacts;
    }

    @Override
    public void initView() {
        imgbtn_dial = (ImageButton) mRootView.findViewById(R.id.imgbtn_dial);
        shoutai_tv = (TextView) mRootView.findViewById(R.id.shoutai_tv);
        jingwutong_tv = (TextView) mRootView.findViewById(R.id.jingwutong_tv);
        group_tv = (TextView) mRootView.findViewById(R.id.group_tv);
        voice_image = (ImageView) mRootView.findViewById(R.id.voice_image);
        icon_laba = (ImageView) mRootView.findViewById(R.id.icon_laba);
        viewPager = (FrameLayout) mRootView.findViewById(R.id.contacts_viewPager);
        jingwutong_line = (View) mRootView.findViewById(R.id.jingwutong_line);
        shoutai_line = (View) mRootView.findViewById(R.id.shoutai_line);
        groupLine = (View) mRootView.findViewById(R.id.group_line);
        is_jingwutong = (RelativeLayout) mRootView.findViewById(R.id.is_jingwutong);
        isGroup = (RelativeLayout) mRootView.findViewById(R.id.is_group);
        add_icon = (ImageView) mRootView.findViewById(R.id.add_icon);
        setting_group_name = (TextView) mRootView.findViewById(R.id.setting_group_name);
        lte_line = mRootView.findViewById(R.id.lte_line);
        lte_tv = (TextView) mRootView.findViewById(R.id.lte_tv);

        recoder_line = mRootView.findViewById(R.id.recoder_line);
        recoder_tv = (TextView) mRootView.findViewById(R.id.recoder_tv);

        activity = (NewMainActivity) getActivity();
        mRootView.findViewById(R.id.is_shoutai).setOnClickListener(this);
        mRootView.findViewById(R.id.is_jingwutong).setOnClickListener(this);
        mRootView.findViewById(R.id.is_group).setOnClickListener(this);
        mRootView.findViewById(R.id.is_lte).setOnClickListener(this);
        mRootView.findViewById(R.id.is_recoder).setOnClickListener(this);
        setVideoIcon();
        childFragmentManager = getChildFragmentManager();
        initFragment();
        setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
        TextViewCompat.setTextAppearance(group_tv, R.style.contacts_title_checked_text);
        TextViewCompat.setTextAppearance(shoutai_tv, R.style.contacts_title_unchecked_text);
        TextViewCompat.setTextAppearance(jingwutong_tv, R.style.contacts_title_unchecked_text);
        TextViewCompat.setTextAppearance(lte_tv, R.style.contacts_title_unchecked_text);
        TextViewCompat.setTextAppearance(recoder_tv, R.style.contacts_title_unchecked_text);
        initTabView();
        group_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        shoutai_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        jingwutong_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        lte_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        recoder_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

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

    private void initTabView(){
        RelativeLayout is_shoutai = mRootView.findViewById(R.id.is_shoutai);
        RelativeLayout isLte = mRootView.findViewById(R.id.is_lte);
        RelativeLayout is_recoder = mRootView.findViewById(R.id.is_recoder);
        if(ApkUtil.isAnjian()){
            is_shoutai.setVisibility(View.GONE);
            isLte.setVisibility(View.GONE);
            is_recoder.setVisibility(View.GONE);
            jingwutong_tv.setText(getString(R.string.text_person));
        }else{
            jingwutong_tv.setText(getString(R.string.text_police_service));
            is_shoutai.setVisibility(View.VISIBLE);
            is_recoder.setVisibility(View.VISIBLE);
            if(ApkUtil.showLteApk()){
                isLte.setVisibility(View.VISIBLE);
            }else {
                isLte.setVisibility(View.GONE);
            }
        }
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
            } else if (handPlatformFragment.isVisible()) {
                handPlatformFragment.onBack();
            } else if (policeAffairsFragment.isVisible()) {
                policeAffairsFragment.onBack();
            } else if (recorderFragment.isVisible()) {
                recorderFragment.onBack();
            } else if(lteFragment != null && lteFragment.isAdded() && lteFragment.isVisible()){
                lteFragment.onBack();
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

    private void initFragment() {
        FragmentTransaction transaction = childFragmentManager.beginTransaction();
        if (groupFragmentNew == null) {
            groupFragmentNew = new NewGroupFragment();
        }
        if (policeAffairsFragment == null) {
            policeAffairsFragment = new NewPoliceAffairsFragment();
        }
        if (handPlatformFragment == null) {
            handPlatformFragment = new NewHandPlatformFragment();
        }
        if (recorderFragment == null) {
            recorderFragment = new RecorderFragment();
        }
        if (lteFragment == null) {
            lteFragment = new LteFragment();
        }
        //判断是否是安监
        if(ApkUtil.isAnjian()){
            transaction.add(R.id.contacts_viewPager, groupFragmentNew)
                    .add(R.id.contacts_viewPager, policeAffairsFragment)
                    .hide(policeAffairsFragment)
                    .show(groupFragmentNew);
            transaction.commit();
        }else{
            //只有市局的包才有LTE
            if(ApkUtil.showLteApk()){
                transaction.add(R.id.contacts_viewPager, groupFragmentNew)
                        .add(R.id.contacts_viewPager, policeAffairsFragment)
                        .add(R.id.contacts_viewPager, handPlatformFragment)
                        .add(R.id.contacts_viewPager, lteFragment)
                        .add(R.id.contacts_viewPager, recorderFragment)
                        .hide(policeAffairsFragment)
                        .hide(handPlatformFragment)
                        .hide(lteFragment)
                        .hide(recorderFragment)
                        .show(groupFragmentNew);
                transaction.commit();
            }else {
                transaction.add(R.id.contacts_viewPager, groupFragmentNew)
                        .add(R.id.contacts_viewPager, policeAffairsFragment)
                        .add(R.id.contacts_viewPager, handPlatformFragment)
                        .add(R.id.contacts_viewPager, recorderFragment)
                        .hide(policeAffairsFragment)
                        .hide(handPlatformFragment)
                        .hide(recorderFragment)
                        .show(groupFragmentNew);
                transaction.commit();
            }
        }
        currentFragment = groupFragmentNew;
        imgbtn_dial.setVisibility(View.GONE);
    }


    public void switchFragment(Fragment from, Fragment to) {
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
        int i = v.getId();
        if(i == R.id.is_group){
            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            if(groupFragmentNew == null){
                groupFragmentNew = new NewGroupFragment();
            }
            TextViewCompat.setTextAppearance(group_tv, R.style.contacts_title_checked_text);
            TextViewCompat.setTextAppearance(shoutai_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(jingwutong_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(lte_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(recoder_tv, R.style.contacts_title_unchecked_text);
            group_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            shoutai_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            jingwutong_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            lte_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            recoder_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            switchFragment(currentFragment, groupFragmentNew);
            groupLine.setVisibility(View.VISIBLE);
            shoutai_line.setVisibility(View.INVISIBLE);
            jingwutong_line.setVisibility(View.INVISIBLE);
            lte_line.setVisibility(View.INVISIBLE);
            recoder_line.setVisibility(View.INVISIBLE);
            imgbtn_dial.setVisibility(View.GONE);
            MyApplication.instance.setIsContactsPersonal(false);
        }else if(i == R.id.is_shoutai){
            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            if(handPlatformFragment == null){
                handPlatformFragment = new NewHandPlatformFragment();
            }
            TextViewCompat.setTextAppearance(group_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(shoutai_tv, R.style.contacts_title_checked_text);
            TextViewCompat.setTextAppearance(jingwutong_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(lte_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(recoder_tv, R.style.contacts_title_unchecked_text);
            group_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            shoutai_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            jingwutong_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            lte_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            recoder_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            switchFragment(currentFragment, handPlatformFragment);
            groupLine.setVisibility(View.INVISIBLE);
            shoutai_line.setVisibility(View.VISIBLE);
            jingwutong_line.setVisibility(View.INVISIBLE);
            lte_line.setVisibility(View.INVISIBLE);
            recoder_line.setVisibility(View.INVISIBLE);
            imgbtn_dial.setVisibility(View.VISIBLE);
            MyApplication.instance.setIsContactsPersonal(true);
        }else if(i == R.id.is_jingwutong){
            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            if(policeAffairsFragment == null){
                policeAffairsFragment = new NewPoliceAffairsFragment();
            }
            TextViewCompat.setTextAppearance(group_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(shoutai_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(jingwutong_tv, R.style.contacts_title_checked_text);
            TextViewCompat.setTextAppearance(lte_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(recoder_tv, R.style.contacts_title_unchecked_text);
            group_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            shoutai_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            jingwutong_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            lte_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            recoder_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            switchFragment(currentFragment, policeAffairsFragment);
            jingwutong_line.setVisibility(View.VISIBLE);
            groupLine.setVisibility(View.INVISIBLE);
            shoutai_line.setVisibility(View.INVISIBLE);
            lte_line.setVisibility(View.INVISIBLE);
            recoder_line.setVisibility(View.INVISIBLE);
            imgbtn_dial.setVisibility(View.VISIBLE);
            MyApplication.instance.setIsContactsPersonal(true);
        }else if(i == R.id.is_lte){
            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            if(lteFragment == null){
                lteFragment = new LteFragment();
            }
            TextViewCompat.setTextAppearance(group_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(shoutai_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(jingwutong_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(recoder_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(lte_tv, R.style.contacts_title_checked_text);
            group_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            shoutai_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            jingwutong_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            recoder_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            lte_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            switchFragment(currentFragment, lteFragment);
            jingwutong_line.setVisibility(View.INVISIBLE);
            groupLine.setVisibility(View.INVISIBLE);
            shoutai_line.setVisibility(View.INVISIBLE);
            recoder_line.setVisibility(View.INVISIBLE);
            lte_line.setVisibility(View.VISIBLE);
            imgbtn_dial.setVisibility(View.VISIBLE);
            MyApplication.instance.setIsContactsPersonal(true);
        }else if(i == R.id.is_recoder){

            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            if(recorderFragment == null){
                recorderFragment = new RecorderFragment();
            }
            TextViewCompat.setTextAppearance(group_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(shoutai_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(jingwutong_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(lte_tv, R.style.contacts_title_unchecked_text);
            TextViewCompat.setTextAppearance(recoder_tv, R.style.contacts_title_checked_text);
            group_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            shoutai_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            jingwutong_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            lte_tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            recoder_tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            switchFragment(currentFragment, recorderFragment);
            jingwutong_line.setVisibility(View.INVISIBLE);
            groupLine.setVisibility(View.INVISIBLE);
            shoutai_line.setVisibility(View.INVISIBLE);
            lte_line.setVisibility(View.INVISIBLE);
            recoder_line.setVisibility(View.VISIBLE);
            imgbtn_dial.setVisibility(View.VISIBLE);
            MyApplication.instance.setIsContactsPersonal(true);
        }
    }


}
