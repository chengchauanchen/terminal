package cn.vsx.vc.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.Timer;
import java.util.TimerTask;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupByNoHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLogFileUploadCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyLivingIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.AboutActivity;
import cn.vsx.vc.activity.BaseActivity;
import cn.vsx.vc.activity.BleActivity;
import cn.vsx.vc.activity.HelpWordActivity;
import cn.vsx.vc.activity.MonitorGroupListActivity;
import cn.vsx.vc.activity.SetSecondGroupActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.BitmapUtil;
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
import ptt.terminalsdk.tools.SettingUtils;
import skin.support.SkinCompatManager;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

@SuppressLint("ValidFragment")
public class SettingFragmentNew extends BaseFragment implements View.OnClickListener {
    Activity activity;

    ImageView add_icon;

    MToggleButton btn_lock_screen_setting;

    TextView setting_group_name;

    LinearLayout ll_video_resolution_setting;

    ChangeMainGroupLayout changemaingrouplayout;

    VolumeChangLayout volumeChangLayout;

    PersonInfoLayout personinfolayout;

    ImageView voice_image;

    LinearLayout ll_log_upload;

    PhysicalButtonSet4PTT physicalButtonSet4PTT;
    private Handler mHandler = new Handler();
    private sendPttState sendPttState;
    private Timer timer = new Timer();
    private AlertDialog dialog;

    ImageView icon_laba;
    private boolean soundOff;//是否静音

    MToggleButton btn_daytime_mode;

    TextView tv_ble_name;

    private static final int DISABLE_KEYGUARD = 0;
    private LinearLayout ll_group_scan;

    private TextView tvLockScreen;

    @Override
    public int getContentViewId() {
        return R.layout.fragment_setting_new;
    }

    @Override
    public void initView() {
        tv_ble_name = (TextView) mRootView.findViewById(R.id.tv_ble_name);
        btn_daytime_mode = (MToggleButton) mRootView.findViewById(R.id.btn_daytime_mode);
        icon_laba = (ImageView) mRootView.findViewById(R.id.icon_laba);
        physicalButtonSet4PTT = (PhysicalButtonSet4PTT) mRootView.findViewById(R.id.physicalButtonPTT);
        ll_log_upload = (LinearLayout) mRootView.findViewById(R.id.ll_log_upload);
        voice_image = (ImageView) mRootView.findViewById(R.id.voice_image);
        personinfolayout = (PersonInfoLayout) mRootView.findViewById(R.id.personinfolayout);
        volumeChangLayout = (VolumeChangLayout) mRootView.findViewById(R.id.VolumeChangLayout);
        changemaingrouplayout = (ChangeMainGroupLayout) mRootView.findViewById(R.id.changemaingrouplayout);
        ll_video_resolution_setting = (LinearLayout) mRootView.findViewById(R.id.ll_video_resolution_setting);
        setting_group_name = (TextView) mRootView.findViewById(R.id.setting_group_name);
        btn_lock_screen_setting = (MToggleButton) mRootView.findViewById(R.id.btn_lock_screen_setting);
        add_icon = (ImageView) mRootView.findViewById(R.id.add_icon);
        ll_group_scan = mRootView.findViewById(R.id.ll_group_scan);
        tvLockScreen = mRootView.findViewById(R.id.tv_lock_screen);
        TextView tvAbout = mRootView.findViewById(R.id.tv_about);
        tvAbout.setText(String.format(getResources().getString(R.string.text_about_app),getResources().getString(R.string.app_name)));
        activity = getActivity();
        mRootView.findViewById(R.id.rl_ble).setOnClickListener(this);
        mRootView.findViewById(R.id.ll_helpAndfeedback).setOnClickListener(this);
        mRootView.findViewById(R.id.ll_log_upload).setOnClickListener(this);
        mRootView.findViewById(R.id.ll_exit).setOnClickListener(this);
        mRootView.findViewById(R.id.about).setOnClickListener(this);
        ll_group_scan.setOnClickListener(this);
        tvLockScreen.setOnClickListener(this);
        setVideoIcon();
        setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
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
        String content = getString(R.string.text_temp_set_lock_screen)+"<font color='#95CAFF'>点我设置 >></font>";
        tvLockScreen.setText(Html.fromHtml(content));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == DISABLE_KEYGUARD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logger.info("锁屏显示界面权限已申请");
            } else {
                ToastUtil.showToast(getContext(), getString(R.string.text_please_open_lock_screen_permission_otherwise_lock_screen_can_not_be_used));
            }
        }
    }

    @Override
    public void initListener() {
        //是否打开ptt悬浮按钮
        ll_video_resolution_setting.setOnClickListener(new OnClickListenerImpVideoResolution());
        btn_lock_screen_setting.setOnBtnClick(currState -> {
            // TODO: 2018/6/21 先判断锁屏显示界面权限
            if (currState) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.DISABLE_KEYGUARD) != PackageManager.PERMISSION_GRANTED) {
//                        requestPermissions(new String[]{Manifest.permission.DISABLE_KEYGUARD},
//                                DISABLE_KEYGUARD);
//                    } else {
//                        MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 1);
//                    }
//                } else {
                    MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 1);
//                }
            } else {
                MyTerminalFactory.getSDK().putParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0);
            }
        });
        btn_daytime_mode.setOnBtnClick(currState -> {
            Log.e("SettingFragmentNew", "currState:" + currState);
            if (currState) {
                MyTerminalFactory.getSDK().putParam(Params.DAYTIME_MODE, true);
                SkinCompatManager.getInstance().loadSkin("daytime.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS);
            } else {
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
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupByNoHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        //刷新蓝牙的状态
        //这里暂时未提供方法获取当前连接的设备，直接用字符串判断
        BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            if (tv_ble_name.getText().toString().equals(getResources().getString(R.string.text_close))) {
                tv_ble_name.setText("开启");
            }
        } else {
            tv_ble_name.setText(R.string.text_close);
        }
        if (MyTerminalFactory.getSDK().getParam(Params.DAYTIME_MODE, false)) {
            btn_daytime_mode.initToggleState(true);
        } else {
            btn_daytime_mode.initToggleState(false);
        }
        physicalButtonSet4PTT.setLastGroupName();
    }

    @Override
    public void initData() {
        if (getActivity() != null) {
            IntentFilter filter = makeGattUpdateIntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(mbtBroadcastReceiver, filter);
        }

        registerPstoreReceiver();
        if (MyTerminalFactory.getSDK().getParam(Params.LOCK_SCREEN_HIDE_OR_SHOW, 0) == 1) {//锁屏设置判断
            btn_lock_screen_setting.initToggleState(true);
        } else {
            btn_lock_screen_setting.initToggleState(false);
        }
        physicalButtonSet4PTT.setChooseSecondGroupListener(new PhysicalButtonSet4PTT.ChooseSecondGroupListener(){
            @Override
            public void OnChooseSecondGroup(){
                Intent intent = new Intent(context, SetSecondGroupActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    @Override
    public void onDestroyView() {

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
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupByNoHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
        if (changemaingrouplayout != null)
            changemaingrouplayout.unRegistListener();
        if (volumeChangLayout != null)
            volumeChangLayout.unRegistLintener();
        if (personinfolayout != null)
            personinfolayout.unInitListener();
        if (physicalButtonSet4PTT != null)
            physicalButtonSet4PTT.unregist();
        getActivity().unregisterReceiver(mbtBroadcastReceiver);
        super.onDestroyView();
    }

    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> mHandler.post(() -> setVideoIcon());

    private void setVideoIcon() {
        MyTopRightMenu.offerObject().initview(add_icon, (BaseActivity) activity);
        add_icon.setVisibility(View.VISIBLE);
    }

    /**
     * 音量改变
     */
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(boolean isVolumeOff, int status) {
            logger.info("触发了receiveVolumeOffCallHandler " + isVolumeOff);
            if (isVolumeOff) {
                voice_image.setImageResource(R.drawable.volume_off_call);
                soundOff = true;
            } else {
                voice_image.setImageResource(R.drawable.horn);
                soundOff = false;
            }
        }
    };

    /***  组呼的时候显示View **/
    private void showViewWhenGroupCall(final String speakerName) {
        icon_laba.setVisibility(View.VISIBLE);
        setting_group_name.setText(speakerName);
    }

    /***  停止组呼的时候隐藏View **/
    private void hideViewWhenStopGroupCall() {
        icon_laba.setVisibility(View.GONE);
        setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
    }

    private void setViewEnable(boolean isEnable) {
        add_icon.setEnabled(isEnable);
        ll_video_resolution_setting.setEnabled(isEnable);
    }

    /*** 自己组呼返回的消息 **/
    private ReceiveRequestGroupCallConformationHandler mReceiveRequestGroupCallConformationHandler = (methodResult, resultDesc,groupId) -> mHandler.post(() -> {
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

    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {
        @Override
        public void handler(int reasonCode) {
            mHandler.post(() -> {
                icon_laba.setVisibility(View.GONE);
                setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
            });
        }
    };
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, String memberName, int groupId, String groupName,CallMode currentCallMode, long uniqueNo) {
            if (MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                mHandler.post(() -> {
                    icon_laba.setVisibility(View.VISIBLE);
                    String speakingName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
                    setting_group_name.setText(speakingName);
                });
            }

        }
    };
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            mHandler.post(() -> setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0))));
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
                            mHandler.post(() -> setting_group_name.setText(tempGroupName));
                        }
                    } else {
                        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                        setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
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


    public void onClick(View v) {
        int i = v.getId();//日志上传
        //帮助与反馈
        //退出应用
        if(i == R.id.rl_ble){
            startActivity(new Intent(getContext(), BleActivity.class));
        }else if(i == R.id.about){
            startActivity(new Intent(getActivity(), AboutActivity.class));
        }else if(i == R.id.ll_log_upload){
            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            uploadLog();
        }else if(i == R.id.ll_helpAndfeedback){
            Intent intent = new Intent(context, HelpWordActivity.class);
            startActivity(intent);
        }else if(i == R.id.ll_exit){
            if(MyApplication.instance.getGroupSpeakState() != GroupCallSpeakState.IDLE)
                return;
            exitApp();
        }else if(i == R.id.ll_group_scan){
            Intent intent = new Intent(context, MonitorGroupListActivity.class);
            startActivity(intent);
        }else if(i == R.id.tv_lock_screen){
            //跳转到设置权限页面
            Intent intent = SettingUtils.gotoPermissionActivity(this.getContext().getPackageName());
            if(intent!=null){
                startActivity(intent);
            }
        }
    }

    private void connectUs() {
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

    private void exitApp() {
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
        }.showDialog(true);
    }

    private void exit() {
        if(TerminalFactory.getSDK().isServerConnected()){
            TerminalFactory.getSDK().getAuthManagerTwo().logout();
        }
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult", "0");
        SendRecvHelper.send(getActivity(), stoppedCallIntent);

        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopHandlerService();
        for (Activity activity : ActivityCollector.getAllActivity().values()) {
            activity.finish();
        }
    }


    public void lockScreen() {
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

    public void uploadLog() {
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
                        if (System.currentTimeMillis() - currentTime > 5000) {
                            MyTerminalFactory.getSDK().getLogFileManager().uploadAllLogFile();
                            currentTime = System.currentTimeMillis();
                        } else {
                            ToastUtil.showToast(getString(R.string.text_uploaded_log_try_again_later), getActivity());
                        }
                    }
                }, 0);
            }

            @Override
            public void doCancelThings() {
                ll_log_upload.setEnabled(true);
            }
        }.showDialog(true);
    }

    public interface sendPttState {
        void sendPttState(Boolean isShow);
    }

    public void setSendPttState(sendPttState sendPttState) {
        this.sendPttState = sendPttState;
    }

    private final class OnClickListenerImpVideoResolution implements View.OnClickListener {


        @Override
        public void onClick(View v) {
            int item = MyTerminalFactory.getSDK().getParam(Params.VIDEO_RESOLUTION, 2);
            final int[] switchItem = {0};
            dialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.text_set_video_definition)
                    .setSingleChoiceItems(new String[]{"超清 1920x1080", "高清 1280x720", "标清 640x480", "流畅 320x240"}, item, (dialog, which) -> {
                        switchItem[0] = which;
                        logger.info("点击视频清晰度设置选项" + which);
                    })
                    .setPositiveButton(getString(R.string.text_sure), (dialog, which) -> {
                        logger.info("点击确定按钮" + switchItem[0]);

                        MyTerminalFactory.getSDK().putParam(Params.VIDEO_RESOLUTION, switchItem[0]);
                    })
                    .setNegativeButton(getString(R.string.text_cancel), (dialog, which) -> {
                        logger.info(" 点击取消按钮" + switchItem[0]);
                    })
                    .setCancelable(true)
                    .show();
        }
    }

    /**
     * 日志上传是否成功的消息
     */
    private ReceiveLogFileUploadCompleteHandler receiveLogFileUploadCompleteHandler = (resultCode, type) -> mHandler.post(() -> {
        if ("log".equals(type)) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
//                ToastUtil.toast(getActivity(), getString(R.string.text_log_upload_success_thanks));
                ToastUtil.showToast(getString(R.string.text_log_upload_success_thanks),getActivity());
            } else {
                ToastUtil.showToast(getString(R.string.text_log_upload_fail_try_again_later), getActivity());
            }
        }
    });

    /**
     * 收到别人请求我开启直播的通知
     **/
    private ReceiveNotifyLivingIncommingHandler receiveNotifyLivingIncommingHandler = new ReceiveNotifyLivingIncommingHandler() {
        @Override
        public void handler(final String mainMemberName, final int mainMemberId, boolean emergencyType) {
            mHandler.post(() -> {
                if (dialog != null) {
                    dialog.dismiss();
                }
            });
        }
    };

    private BroadcastReceiver exitPstoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (cn.com.cybertech.pdk.Intent.ACTION_PSTORE_EXIT.equals(intent.getAction())) {
                exit();
            }
        }
    };

    private void registerPstoreReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(cn.com.cybertech.pdk.Intent.ACTION_PSTORE_EXIT);
        context.registerReceiver(exitPstoreReceiver, intentFilter);
    }

    private void unregisterPstoreReceiver() {
        context.unregisterReceiver(exitPstoreReceiver);
    }


    public void bleClick() {
        Intent intent = new Intent(getContext(), BleActivity.class);
        startActivity(intent);
    }

    BroadcastReceiver mbtBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                String deviceName = intent.getStringExtra("deviceName");
                tv_ble_name.setText(deviceName);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                tv_ble_name.setText("开启");
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                //蓝牙状态切换
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_ON:
                        //开启
                        tv_ble_name.setText("开启");
                        break;
                    case BluetoothAdapter.STATE_OFF: // 关闭
                        tv_ble_name.setText(R.string.text_close);
                        break;
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPstoreReceiver();
    }
}
