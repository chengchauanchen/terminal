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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.view.DialPopupwindow;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

@SuppressLint("ValidFragment")
public class ContactsFragmentNew extends BaseFragment implements View.OnClickListener {
    @Bind(R.id.setting_group_name)
    TextView setting_group_name;
    @Bind(R.id.add_icon)
    ImageView add_icon;
    @Bind(R.id.is_group)
    RelativeLayout isGroup;
    @Bind(R.id.is_jingwutong)
    RelativeLayout is_jingwutong;
    @Bind(R.id.group_line)
    View groupLine;
    @Bind(R.id.shoutai_line)
    View shoutai_line;
    @Bind(R.id.jingwutong_line)
    View jingwutong_line;
    @Bind(R.id.contacts_viewPager)
    FrameLayout viewPager;
    @Bind(R.id.icon_laba)
    ImageView icon_laba;
    @Bind(R.id.voice_image)
    ImageView voice_image;
    @Bind(R.id.speaking_name)
    TextView speaking_name;
    @Bind(R.id.group_tv)
    TextView group_tv;
    @Bind(R.id.jingwutong_tv)
    TextView jingwutong_tv;
    @Bind(R.id.shoutai_tv)
    TextView shoutai_tv;
    @Bind(R.id.imgbtn_dial)
    ImageButton imgbtn_dial;

    private Fragment currentFragment;
    private NewGroupFragment groupFragmentNew;
    //    private PoliceAffairsFragment policeAffairsFragment;
    private NewPoliceAffairsFragment policeAffairsFragment;
    //    private HandPlatformFragment handPlatformFragment;
    private NewHandPlatformFragment handPlatformFragment;
    private FragmentManager childFragmentManager;
    NewMainActivity activity;
    private Handler mHandler = new Handler();
    private DialPopupwindow dialPopupwindow;
    private boolean soundOff;

    public ContactsFragmentNew(){}

    @Override
    public int getContentViewId() {
        return R.layout.fragment_contacts;
    }

    @Override
    public void initView() {
        activity = (NewMainActivity) getActivity();
        setVideoIcon();
        childFragmentManager = getChildFragmentManager();
        initFragment();
        setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
        TextViewCompat.setTextAppearance(group_tv,R.style.contacts_title_checked_text);
        TextViewCompat.setTextAppearance(shoutai_tv,R.style.contacts_title_unchecked_text);
        TextViewCompat.setTextAppearance(jingwutong_tv,R.style.contacts_title_unchecked_text);
        group_tv .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        shoutai_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        jingwutong_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        dialPopupwindow = new DialPopupwindow(context);
        voice_image.setOnClickListener(view -> {
            if(!soundOff){
                voice_image.setImageResource(R.drawable.volume_off_call);
                TerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true,1);
                soundOff =true;
            }else {
                voice_image.setImageResource(R.drawable.horn);
                TerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,1);
                soundOff =false;
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
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
        imgbtn_dial.setOnClickListener(v -> {
            if (dialPopupwindow == null){
                dialPopupwindow = new DialPopupwindow(context);
            }
            dialPopupwindow.showAtLocation(((Activity)context).findViewById(R.id.rg), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        });

        activity.setOnBackListener(() -> {
                if (groupFragmentNew.isVisible()) {
                    groupFragmentNew.onBack();
                }else if (handPlatformFragment.isVisible()){
                    handPlatformFragment.onBack();
                }else {
                    policeAffairsFragment.onBack();
                }
        });

    }

    /*** 自己组呼返回的消息 **/
    private ReceiveRequestGroupCallConformationHandler mReceiveRequestGroupCallConformationHandler = (methodResult, resultDesc) -> mHandler.post(() -> {
        if (methodResult == 0) {
            showViewWhenGroupCall(getString(R.string.text_I_am_talking));
            setViewEnable(false);
        }
    });
    private void setViewEnable (boolean isEnable) {
//        newsList.setEnabled(isEnable);
        add_icon.setEnabled(isEnable);
    }
    /***  停止组呼的时候隐藏View **/
    private void hideViewWhenStopGroupCall () {
        speaking_name.setVisibility(View.GONE);
        icon_laba.setVisibility(View.GONE);
    }

    /***  组呼的时候显示View **/
    private void showViewWhenGroupCall (final String speakerName) {
        speaking_name.setVisibility(View.VISIBLE);
        icon_laba.setVisibility(View.VISIBLE);
        speaking_name.setText(speakerName);
    }
    @Override
    public void initData() {
    }
    private void setVideoIcon() {
        MyTopRightMenu.offerObject().initview(add_icon,activity );
        //没有上报和回传图像的权限
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())
                && !MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
            add_icon.setVisibility(View.INVISIBLE);
        }else {
            add_icon.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
        super.onDestroyView();
    }
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> mHandler.post(() -> setVideoIcon());
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            mHandler.post(() -> setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name));
        }
    };
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, String memberName, int groupId, final String groupName, CallMode currentCallMode) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                mHandler.post(() -> {
                    setting_group_name.setText(groupName);
                    speaking_name.setVisibility(View.VISIBLE);
                    icon_laba.setVisibility(View.VISIBLE);
                    speaking_name.setText(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
                });
            }

        }
    };
    /**音量改变*/
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(boolean isVolumeOff,int status) {
            if(isVolumeOff||MyTerminalFactory.getSDK().getAudioProxy().getVolume()==0){
                voice_image.setImageResource(R.drawable.volume_off_call);
                soundOff=true;
            }else {
                voice_image.setImageResource(R.drawable.horn);
                soundOff=false;
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
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler(){

        @Override
        public void handler(int reasonCode) {
            mHandler.post(() -> {
                setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
                speaking_name.setVisibility(View.GONE);
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
                setting_group_name.setText(DataUtil.getGroupByGroupNo(currentGroupId).name);
            });
        }
    };

    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId,boolean forceSwitchGroup,String tempGroupType) {
            if(!forceSwitchGroup){
                return;
            }
            mHandler.post(() -> {
                int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                setting_group_name.setText(DataUtil.getGroupByGroupNo(currentGroupId).name);
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

        if (handPlatformFragment == null){
            handPlatformFragment = new NewHandPlatformFragment();
        }
        transaction.add(R.id.contacts_viewPager, groupFragmentNew)
                .add(R.id.contacts_viewPager, policeAffairsFragment)
                .add(R.id.contacts_viewPager, handPlatformFragment)
                .hide(policeAffairsFragment)
                .hide(handPlatformFragment)
                .show(groupFragmentNew);
        transaction.commit();

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

    @OnClick({R.id.is_group, R.id.is_jingwutong,R.id.is_shoutai})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.is_group:
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                    return;
                if (groupFragmentNew == null) {
                    groupFragmentNew = new NewGroupFragment();
                }
                TextViewCompat.setTextAppearance(group_tv,R.style.contacts_title_checked_text);
                TextViewCompat.setTextAppearance(shoutai_tv,R.style.contacts_title_unchecked_text);
                TextViewCompat.setTextAppearance(jingwutong_tv,R.style.contacts_title_unchecked_text);
                group_tv .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                shoutai_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                jingwutong_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                switchFragment(currentFragment, groupFragmentNew);
                groupLine.setVisibility(View.VISIBLE);
                shoutai_line.setVisibility(View.INVISIBLE);
                jingwutong_line.setVisibility(View.INVISIBLE);
                imgbtn_dial.setVisibility(View.GONE);
                MyApplication.instance.setIsContactsPersonal(false);
                break;
            case R.id.is_shoutai:
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                    return;
                if (handPlatformFragment == null){
                    handPlatformFragment = new NewHandPlatformFragment();
                }
                TextViewCompat.setTextAppearance(group_tv,R.style.contacts_title_unchecked_text);
                TextViewCompat.setTextAppearance(shoutai_tv,R.style.contacts_title_checked_text);
                TextViewCompat.setTextAppearance(jingwutong_tv,R.style.contacts_title_unchecked_text);
                group_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                shoutai_tv .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                jingwutong_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                switchFragment(currentFragment, handPlatformFragment);
                groupLine.setVisibility(View.INVISIBLE);
                shoutai_line.setVisibility(View.VISIBLE);
                jingwutong_line.setVisibility(View.INVISIBLE);
                imgbtn_dial.setVisibility(View.VISIBLE);
                MyApplication.instance.setIsContactsPersonal(true);
                break;
            case R.id.is_jingwutong:
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                    return;
                if (policeAffairsFragment == null) {
                    policeAffairsFragment = new NewPoliceAffairsFragment();
                }
                TextViewCompat.setTextAppearance(group_tv,R.style.contacts_title_unchecked_text);
                TextViewCompat.setTextAppearance(shoutai_tv,R.style.contacts_title_unchecked_text);
                TextViewCompat.setTextAppearance(jingwutong_tv,R.style.contacts_title_checked_text);
                group_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                shoutai_tv .setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                jingwutong_tv .setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                switchFragment(currentFragment, policeAffairsFragment);
                jingwutong_line.setVisibility(View.VISIBLE);
                groupLine.setVisibility(View.INVISIBLE);
                shoutai_line.setVisibility(View.INVISIBLE);
                imgbtn_dial.setVisibility(View.VISIBLE);
                MyApplication.instance.setIsContactsPersonal(true);
                break;
            default:
                break;
        }
    }


}
