package cn.vsx.vc.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLogFileUploadCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.AboutActivity;
import cn.vsx.vc.activity.BleActivity;
import cn.vsx.vc.activity.HelpWordActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.ChangeMainGroupLayout;
import cn.vsx.vc.view.MToggleButton;
import cn.vsx.vc.view.PhysicalButtonSet4PTT;
import cn.vsx.vc.view.VolumeChangLayout;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import cn.vsx.vc.view.view4modularization.PersonInfoLayout;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.service.BluetoothLeService;
import ptt.terminalsdk.tools.DialogUtil;
import skin.support.SkinCompatManager;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

@SuppressLint("ValidFragment")
public class SettingFragmentNew extends BaseFragment {
    Activity activity;
    @Bind(R.id.add_icon)
    ImageView add_icon;
    @Bind(R.id.btn_lock_screen_setting)
    MToggleButton btn_lock_screen_setting;
    @Bind(R.id.setting_group_name)
    TextView setting_group_name;
    @Bind(R.id.ll_video_resolution_setting)
    LinearLayout ll_video_resolution_setting;
    @Bind(R.id.changemaingrouplayout)
    ChangeMainGroupLayout changemaingrouplayout;
    @Bind(R.id.VolumeChangLayout)
    VolumeChangLayout volumeChangLayout;
    @Bind(R.id.personinfolayout)
    PersonInfoLayout personinfolayout;
    @Bind(R.id.voice_image)
    ImageView voice_image;
    @Bind(R.id.ll_log_upload)
    LinearLayout ll_log_upload;
    @Bind(R.id.physicalButtonPTT)
    PhysicalButtonSet4PTT physicalButtonSet4PTT;
    private Handler mHandler = new Handler();
    private sendPttState sendPttState;
    private Timer timer = new Timer();
    private AlertDialog dialog;

    @Bind(R.id.icon_laba)
    ImageView icon_laba;
    @Bind(R.id.speaking_name)
    TextView speaking_name;
    private boolean soundOff;//是否静音
    @Bind(R.id.btn_daytime_mode)
    MToggleButton btn_daytime_mode;
    @Bind(R.id.tv_ble_name)
    TextView tv_ble_name;

    private static final int DISABLE_KEYGUARD = 0;

    @Override
    public int getContentViewId() {
        return R.layout.fragment_setting_new;
    }

    @Override
    public void initView() {
        activity = getActivity();
        setVideoIcon();
        setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == DISABLE_KEYGUARD){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                logger.info("锁屏显示界面权限已申请");
            }else{
                ToastUtil.showToast(getContext(),getString(R.string.text_please_open_lock_screen_permission_otherwise_lock_screen_can_not_be_used));
            }
        }
    }

    @Override
    public void initListener() {
        //是否打开ptt悬浮按钮
        ll_video_resolution_setting.setOnClickListener(new OnClickListenerImpVideoResolution());
        btn_lock_screen_setting.setOnBtnClick(currState -> {
            // TODO: 2018/6/21 先判断锁屏显示界面权限
            if(currState){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.DISABLE_KEYGUARD) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.DISABLE_KEYGUARD},
                                DISABLE_KEYGUARD);
                    }else {
                        MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 1);
                    }
                }else {
                    MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 1);
                }
            }else{
                MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0);
            }
        });
        btn_daytime_mode.setOnBtnClick(currState -> {
            Log.e("SettingFragmentNew", "currState:" + currState);
            if(currState){
                MyTerminalFactory.getSDK().putParam(Params.DAYTIME_MODE, true);
                SkinCompatManager.getInstance().loadSkin("daytime.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS);
            }else {
                MyTerminalFactory.getSDK().putParam(Params.DAYTIME_MODE, false);
                SkinCompatManager.getInstance().restoreDefaultTheme();
            }
        });
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLogFileUploadCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
    }

    @Override
    public void initData() {
        getActivity().registerReceiver(mbtBroadcastReceiver, makeGattUpdateIntentFilter());
        if(MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW,0)==1){//锁屏设置判断
            btn_lock_screen_setting.initToggleState(true);
        }else {
            btn_lock_screen_setting.initToggleState(false);
        }
        if(MyTerminalFactory.getSDK().getParam(Params.DAYTIME_MODE,false)){
            btn_daytime_mode.initToggleState(true);
        }else {
            btn_daytime_mode.initToggleState(false);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLogFileUploadCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyLivingIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
        if (changemaingrouplayout != null)
            changemaingrouplayout.unRegistListener();
        if(volumeChangLayout != null)
            volumeChangLayout.unRegistLintener();
        if(personinfolayout != null)
            personinfolayout.unInitListener();
        if (physicalButtonSet4PTT !=  null)
            physicalButtonSet4PTT.unregist();
        getActivity().unregisterReceiver(mbtBroadcastReceiver);
        super.onDestroyView();
    }

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> mHandler.post(() -> setVideoIcon());
    private void setVideoIcon() {
        MyTopRightMenu.offerObject().initview(add_icon,activity );
        add_icon.setVisibility(View.VISIBLE);
    }

    /**音量改变*/
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

                @Override
                public void handler(boolean isVolumeOff,int status) {
                    logger.info("触发了receiveVolumeOffCallHandler "+isVolumeOff);
                    if(isVolumeOff&&MyTerminalFactory.getSDK().getAudioProxy().getVolume()==0){
                        voice_image.setImageResource(R.drawable.volume_off_call);
                        soundOff=true;
                    }else {
                        voice_image.setImageResource(R.drawable.horn);
                        soundOff=false;
                    }
                }
            };

    /***  组呼的时候显示View **/
    private void showViewWhenGroupCall (final String speakerName) {
        speaking_name.setVisibility(View.VISIBLE);
        icon_laba.setVisibility(View.VISIBLE);
        speaking_name.setText(speakerName);
    }

    /***  停止组呼的时候隐藏View **/
    private void hideViewWhenStopGroupCall () {
        speaking_name.setVisibility(View.GONE);
        icon_laba.setVisibility(View.GONE);
    }

    private void setViewEnable (boolean isEnable) {
        add_icon.setEnabled(isEnable);
        ll_video_resolution_setting.setEnabled(isEnable);
    }

    /*** 自己组呼返回的消息 **/
    private ReceiveRequestGroupCallConformationHandler mReceiveRequestGroupCallConformationHandler = (methodResult, resultDesc) -> mHandler.post(() -> {
        if (methodResult == 0) {
            showViewWhenGroupCall(getString(R.string.text_I_am_talking));
            setViewEnable(false);
        }
    });

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
                speaking_name.setVisibility(View.GONE);
                icon_laba.setVisibility(View.GONE);
                setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
            });
        }
    };
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, String memberName, final int groupId, String groupName, CallMode currentCallMode) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                mHandler.post(() -> {
                    logger.info("sjl_设置页面的组呼到来");
                    speaking_name.setVisibility(View.VISIBLE);
                    icon_laba.setVisibility(View.VISIBLE);
                    logger.info("sjl_设置页面的组呼到来"+speaking_name.getVisibility()+",正在说话人的名字："+MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
                    String speakingName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
                    speaking_name.setText(speakingName);
                    setting_group_name.setText(DataUtil.getGroupByGroupNo(groupId).name);
                });
            }

        }
    };
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            mHandler.post(() -> setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name));
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

    @OnClick({R.id.about, R.id.ll_exit,R.id.ll_log_upload,R.id.ll_helpAndfeedback})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.ll_log_upload://日志上传
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                    return;
                uploadLog();
                break;
            case  R.id.ll_helpAndfeedback://帮助与反馈
                Intent intent = new Intent(context, HelpWordActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_exit://退出应用
                if (MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                    return;
                exitApp();
                break;
        }
    }

    private void connectUs () {
        dialog = new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return getString(R.string.text_are_you_sure_call_customer_service);
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public void doConfirmThings() {
//                CallPhoneUtil.callPhone((Activity)context, connect_us_phone.getText().toString());
            }

            @Override
            public void doCancelThings() {
            }
        }.showDialog();
    }
    private void exitApp () {
        dialog = new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return getString(R.string.text_text_are_you_sure_exit);
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public void doConfirmThings() {
                exit();
            }

            @Override
            public void doCancelThings() {
            }
        }.showDialog();
    }

    private void exit() {
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult","0");
        SendRecvHelper.send(getActivity(),stoppedCallIntent);

        MyTerminalFactory.getSDK().exit();//停止服务
        PromptManager.getInstance().stop();
        for (Activity activity : ActivityCollector.getAllActivity().values()) {
            activity.finish();
        }
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopIndividualCallService();
        Process.killProcess(Process.myPid());
    }


    public void lockScreen () {
        final int[] item = {MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0)};
        dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.text_set_lock_screen)
                .setSingleChoiceItems(new String[]{"隐藏", "显示"}, item[0], (dialog, which) -> {
                    logger.info("点击锁屏设置选项" + which);
                    item[0] = which;
                })
                .setPositiveButton(getString(R.string.text_sure), (dialog, which) -> {
                    if (item[0] == 0) {
                        logger.info("点击确定按钮" + item[0]);
                        MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0);
                    } else {
                        logger.info("点击确定按钮" + item[0]);
                        MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 1);
                    }
                })
                .setNegativeButton(getString(R.string.text_cancel), (dialog, which) -> {
                    item[0] = MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0);
                    logger.info(" 点击取消按钮" + item[0]);
                })
                .setCancelable(false)
                .show();
    }
    private long currentTime;
    public void uploadLog () {
        dialog = new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                return getString(R.string.text_are_you_sure_upload_log);
            }

            @Override
            public Context getContext() {
                return context;
            }

            @Override
            public void doConfirmThings() {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(System.currentTimeMillis()-currentTime>5000){
                            MyTerminalFactory.getSDK().getLogFileManager().uploadAllLogFile();
                            currentTime = System.currentTimeMillis();
                        }else {
                            ToastUtil.showToast(getString(R.string.text_uploaded_log_try_again_later),getActivity());
                        }
                    }
                }, 0);
            }

            @Override
            public void doCancelThings() {
                ll_log_upload.setEnabled(true);
            }
        }.showDialog();
    }

    public interface sendPttState {
        void sendPttState(Boolean isShow);
    }

    public void setSendPttState(sendPttState sendPttState) {
        this.sendPttState = sendPttState;
    }
    private final class OnClickListenerImpVideoResolution implements View.OnClickListener {
        int item = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
        @Override
        public void onClick(View v) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.text_set_video_definition)
                    .setSingleChoiceItems(new String[]{"超清 1920x1080", "高清 1280x720", "标清 640x480", "流畅 320x240"}, item, (dialog, which) -> {
                        item = which;
                        logger.info("点击视频清晰度设置选项" + which);
                    })
                    .setPositiveButton(getString(R.string.text_sure), (dialog, which) -> {
                        logger.info("点击确定按钮" + item);

                        MyTerminalFactory.getSDK().putParam(Params.VIDEO_RESOLUTION, item);
                    })
                    .setNegativeButton(getString(R.string.text_cancel), (dialog, which) -> {
                        item = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
                        logger.info(" 点击取消按钮" + item);
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    /**日志上传是否成功的消息*/
    private ReceiveLogFileUploadCompleteHandler receiveLogFileUploadCompleteHandler = resultCode -> mHandler.post(() -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            ToastUtil.toast(getActivity(), getString(R.string.text_log_upload_success_thanks));
        } else {
            ToastUtil.showToast( getString(R.string.text_log_upload_fail_try_again_later), getActivity());
        }
    });

    /**收到别人请求我开启直播的通知**/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId) {
            mHandler.post(() -> {
                if (dialog != null){
                    dialog.dismiss();
                }
            });
        }
    };

    private BroadcastReceiver exitPstoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (cn.com.cybertech.pdk.Intent.ACTION_PSTORE_EXIT.equals(intent.getAction())){
                exit();
            }
        }
    };
    private void registerPstoreReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(cn.com.cybertech.pdk.Intent.ACTION_PSTORE_EXIT);
        context.registerReceiver(exitPstoreReceiver, intentFilter);
    }
    private void unregisterPstoreReceiver(){
        context.unregisterReceiver(exitPstoreReceiver);
    }

    @OnClick(R.id.rl_ble)
    public void bleClick(){
        Intent intent = new Intent(getContext(),BleActivity.class);
        startActivity(intent);
    }

    BroadcastReceiver mbtBroadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                String deviceName = intent.getStringExtra("deviceName");
                tv_ble_name.setText(deviceName);
            }else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                tv_ble_name.setText(R.string.text_close);
            }
        }
    };

}
