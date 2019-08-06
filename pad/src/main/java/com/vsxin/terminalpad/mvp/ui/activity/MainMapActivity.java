package com.vsxin.terminalpad.mvp.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpActivity;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.instruction.groupCall.GroupCallInstruction;
import com.vsxin.terminalpad.instruction.groupCall.SendGroupCallListener;
import com.vsxin.terminalpad.js.TerminalPadJs;
import com.vsxin.terminalpad.mvp.contract.presenter.MainMapPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.ui.fragment.LayerMapFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.LiveFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.NoticeFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.SmallMapFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.VsxFragment;
import com.vsxin.terminalpad.receiveHandler.ReceiveUpdateMainFrgamentPTTButtonHandler;
import com.vsxin.terminalpad.utils.HandleIdUtil;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;
import com.vsxin.terminalpad.utils.SystemUtils;

import org.apache.http.util.TextUtils;

import butterknife.BindView;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MemberChangeType;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallSpeakState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupByNoHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseChangeTempGroupProcessingStateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseGroupActiveHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

/**
 * @author qzw
 * <p>
 * 主页地图
 */
public class MainMapActivity extends MvpActivity<IMainMapView, MainMapPresenter> implements IMainMapView {

    @BindView(R.id.web_map)
    WebView web_map;
    @BindView(R.id.fl_layer_member_info)
    FrameLayout fl_layer_member_info;
    @BindView(R.id.rl_group_call)
    RelativeLayout rl_group_call;
    @BindView(R.id.iv_group_call_bg)
    ImageView bnt_group_call;
    @BindView(R.id.tx_ptt_time)
    TextView tx_ptt_time;
    @BindView(R.id.tx_ptt_group_name)
    TextView tx_ptt_group_name;
    private GroupCallInstruction groupCallInstruction;

    private int timeProgress;
    //组呼倒计时
    private static final int HANDLER_GROUP_TIME = 1;

    private boolean onRecordAudioDenied;
    private boolean onLocationDenied;
    private boolean onCameraDenied;

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    public static final int REQUEST_INSTALL_PACKAGES_CODE = 1235;
    public static final int GET_UNKNOWN_APP_SOURCES = 1236;
//    public static final int REQUEST_CODE_SCAN = 1237;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_GROUP_TIME) {
                timeProgress--;
                if (timeProgress <= 0) {
                    mHandler.removeMessages(HANDLER_GROUP_TIME);
                    pttUpDoThing();
                } else {
                    tx_ptt_time.setText(String.valueOf(timeProgress));
                    mHandler.sendEmptyMessageDelayed(HANDLER_GROUP_TIME, 1000);
                }
            }
        }
    };

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainMapActivity.class));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        registReceiveHandler();
        inflaterFragment();
        initWebMap();
        initPPT();
        judgePermission();
        groupCallInstruction = new GroupCallInstruction(this);
        groupCallInstruction.bindReceiveHandler();
    }

    /**
     * 初始化地图
     */
    private void initWebMap() {
        web_map.requestFocus();
        web_map.getSettings().setJavaScriptEnabled(true);
        web_map.getSettings().setSaveFormData(false);
        web_map.getSettings().setSavePassword(false);
        web_map.getSettings().setSupportZoom(false);
        web_map.getSettings().setUseWideViewPort(true);
        web_map.getSettings().setLoadWithOverviewMode(true);
        web_map.getSettings().setDomStorageEnabled(true); // 开启 DOM storage API 功能
        web_map.getSettings().setDatabaseEnabled(true);   //开启 database storage API 功能
        web_map.setVerticalScrollBarEnabled(false); //垂直不显示滚动条
        web_map.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 100) {
                    //地图加载完成后，显示所有图层
                    getPresenter().defaultLoadAllLayer();
                    //MemberInfoBean memberInfoBean = new Gson().fromJson("", MemberInfoBean.class);
                   // MemberInfoFragment.startMemberInfoFragment(MainMapActivity.this, memberInfoBean, MemberTypeEnum.PHONE);
                }
            }
        });


        web_map.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

        });

        // 将Android里面定义的类对象AndroidJs暴露给javascript
        web_map.addJavascriptInterface(new TerminalPadJs(this), "TerminalPadJs");

        String memberId = HandleIdUtil.handleId(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
        Long memberUniqueno = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
        int depId = MyTerminalFactory.getSDK().getParam(Params.DEP_ID, 0);
        String format = String.format("no=%s&code=%s&dept_id=%s", memberId, memberUniqueno, depId);
        getLogger().info("http://192.168.1.222:9011/offlineMapForLin/indexPad.html?" + format);
        //web_map.loadUrl("http://192.168.1.187:9011/offlineMap/indexPad.html?" + format);
        web_map.loadUrl("http://192.168.1.222:9011/offlineMapForLin/indexPad.html?" + format);
    }


    private void inflaterFragment() {
        SmallMapFragment smallMapFragment = new SmallMapFragment();
        NoticeFragment noticeFragment = new NoticeFragment();
        LiveFragment liveFragment = new LiveFragment();
        LayerMapFragment layerMapFragment = new LayerMapFragment();
        VsxFragment vsxFragment = new VsxFragment();

        //拿到fragment的manager对象
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        //事务(防止花屏)
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();

        //表示使用SoundFragment 去替换之前的fragment
        fragmentTransaction.replace(R.id.fl_small_map, smallMapFragment);
        fragmentTransaction.replace(R.id.fl_notice, noticeFragment);
        fragmentTransaction.replace(R.id.fl_live, liveFragment);

        fragmentTransaction.replace(R.id.fl_map_layer, layerMapFragment);
        fragmentTransaction.replace(R.id.fl_vsx, vsxFragment);

        //提交事务
        fragmentTransaction.commit();
    }

    @Override
    public void drawMapLayer(String type, boolean isShow) {
        web_map.loadUrl("javascript:abstractIndexObj.showResourceToMap('" + type + "'," + isShow + ")");
    }

    public void closeInfoBoxToMap(String no, String type) {
        web_map.loadUrl("javascript:abstractIndexObj.closeInfoBoxToMap('" + no + "'," + "'" + type + "')");
    }

    @Override
    protected void initData() {

    }

    @Override
    public MainMapPresenter createPresenter() {
        return new MainMapPresenter(this);
    }

    /**
     * 必须要有录音和相机的权限，APP才能去视频页面
     */
    private void judgePermission() {

        //6.0以下判断相机权限
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M  ){
            if(!SystemUtils.cameraIsCanUse()){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CheckMyPermission.REQUEST_CAMERA);
            }
        }else {
            if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)){
                if(CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                        CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                    }
                    //                    else {
                    //                        CheckMyPermission.permissionPrompt(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    //                    }
                }else {
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }else {
                //如果权限被拒绝，申请下一个权限
                if(onRecordAudioDenied){
                    if(CheckMyPermission.selfPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                        if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                        }
                    }else {
                        if(onLocationDenied){
                            if(!CheckMyPermission.selfPermissionGranted(this,Manifest.permission.CAMERA)){
                                if(!onCameraDenied){
                                    CheckMyPermission.permissionPrompt(this, Manifest.permission.CAMERA);
                                }
                            }
                        }else {
                            CheckMyPermission.permissionPrompt(this, Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }
                }else{
                    CheckMyPermission.permissionPrompt(this, Manifest.permission.RECORD_AUDIO);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    ToastUtil.showToast(MainMapActivity.this, getString(R.string.open_overlay_permisson));
                } else {
                }
            }
//        }else if(requestCode == GET_UNKNOWN_APP_SOURCES){
//            if(null !=updateManager){
//                updateManager.checkIsAndroidO(false);
//            }
//        } else if (requestCode == CODE_FNC_REQUEST) {
//            int userId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
//            checkNFC(userId,false);
//        }else if(requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK){
//            if (data != null) {
//                String result = data.getStringExtra(Constant.CODED_CONTENT);
//                logger.info("扫描二维码结果："+result);
//                int groupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
//                analysisScanData(result,groupId);
//            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CheckMyPermission.REQUEST_RECORD_AUDIO:
                onRecordAudioDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case CheckMyPermission.REQUEST_CAMERA:
                onCameraDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case CheckMyPermission.REQUEST_LOCATION:
                onLocationDenied = true;
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    judgePermission();
                }else {
                    permissionDenied(requestCode);
                }
                break;
            case REQUEST_INSTALL_PACKAGES_CODE:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, GET_UNKNOWN_APP_SOURCES);
                }
            default:
                break;

        }
    }

    private void permissionDenied(int requestCode){
        if(requestCode == CheckMyPermission.REQUEST_RECORD_AUDIO){
            ToastUtil.showToast(this, getString(R.string.text_audio_frequency_is_not_open_audio_is_not_used));
        }else if(requestCode ==CheckMyPermission.REQUEST_CAMERA){
            ToastUtil.showToast(this, getString(R.string.text_camera_not_open_audio_is_not_used));
        }else if(requestCode ==CheckMyPermission.REQUEST_LOCATION){
            ToastUtil.showToast(this, getString(R.string.text_location_not_open_locat_is_not_used));
        }
        //        judgePermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregistReceiveHandler();
        groupCallInstruction.unBindReceiveHandler();
    }

    /******************************************组呼********************************************/

    @SuppressLint("ClickableViewAccessibility")
    private void initPPT() {
//        bnt_group_call.setOnTouchListener(new OnPttTouchListenerImplementation());
        bnt_group_call.setOnClickListener(v -> {
//            TerminalFactory.getSDK().getThreadPool().execute(() -> {
//                Account account = DataUtil.getAccountByMemberNo(10000195, true);
//                Member member = MemberUtil.getMemberForTerminalMemberType(account, TerminalMemberType.TERMINAL_PC);
//                    //MyApplication.instance.isCallState = true;
//                    boolean network = MyTerminalFactory.getSDK().hasNetwork();
//                    if (network) {
//                        if (member != null) {
//                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
//                        } else {
//                            ToastUtil.showToast(this, this.getString(R.string.text_get_member_info_fail));
//                        }
//                    } else {
//                        ToastUtil.showToast(this, this.getString(R.string.text_network_connection_abnormal_please_check_the_network));
//                    }
//            });
            if(PadApplication.getPadApplication().isPttPress){
                PadApplication.getPadApplication().isPttPress = false;
                pttUpDoThing();
            }else{
                PadApplication.getPadApplication().isPttPress = true;
                pttDownDoThing();
            }
        });
        setCurrentGroupView();
        setPttText();
    }

    private final class OnPttTouchListenerImplementation implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    PadApplication.getPadApplication().isPttPress = true;
                    pttDownDoThing();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.performClick();
                    PadApplication.getPadApplication().isPttPress = false;
                    getLogger().info("ACTION_UP，ACTION_CANCEL，ptt按钮抬起，停止组呼：" + PadApplication.getPadApplication().isPttPress);
                    pttUpDoThing();
                    break;
                default:
                    break;
            }
            //返回true,避免将事件传递父级地图层
            return true;
        }
    }


    //PTT按下以后
    private void pttDownDoThing() {
        getLogger().info("ptt.pttDownDoThing执行了 isPttPress：" + PadApplication.getPadApplication().isPttPress);

        if (!CheckMyPermission.selfPermissionGranted(this, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
            CheckMyPermission.permissionPrompt(this, Manifest.permission.RECORD_AUDIO);
            return;
        }
        //没有组呼权限
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            ToastUtil.showToast(this, getString(R.string.text_has_no_group_call_authority));
            return;
        }

        //半双工个呼中在别的组不能组呼、全双工个呼中不能组呼
        if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {

        }
        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
        getLogger().info("PTT按下以后resultCode:" + resultCode);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
            com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
            change2PreSpeaking();
        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
            change2Waiting();
        } else {//组呼失败的提示
            ToastUtil.groupCallFailToast(this, resultCode);
        }

        groupCallInstruction.startGroupCall(new SendGroupCallListener() {
            @Override
            public void speaking() {
                getLogger().info("speaking");
            }

            @Override
            public void readySpeak() {
                getLogger().info("readySpeak");
            }

            @Override
            public void forbid() {
                getLogger().info("forbid");
            }

            @Override
            public void waite() {
                getLogger().info("waite");
            }

            @Override
            public void silence() {
                getLogger().info("silence");
            }

            @Override
            public void listening() {
                getLogger().info("listening");
            }

            @Override
            public void fail() {
                getLogger().info("fail");
            }
        });

    }

    private boolean isScanGroupCall;//是否扫描组在组呼

    //PTT抬起以后
    private void pttUpDoThing() {
        getLogger().info("ptt.pttUpDoThing执行了 isPttPress：" + PadApplication.getPadApplication().isPttPress);
        mHandler.post(() -> tx_ptt_time.setText(getString(R.string.text_ptt)));
        MyTerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TALK.name())) {
            return;
        }

        if (PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.LISTENING) {
            isScanGroupCall = false;
            change2Listening();
        } else {
            change2Silence();
        }
        MyTerminalFactory.getSDK().getGroupCallManager().ceaseGroupCall();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, false);
    }


    /**
     * Silence 沉默、无声状态
     */
    private void change2Silence() {
        if (PadApplication.getPadApplication().getGroupListenenState() == LISTENING) {
            return;
        }
        if (!GroupUtils.currentIsForbid()) {
            //只有当前组不是禁呼的才恢复PPT的状态
            bnt_group_call.setImageResource(R.drawable.bg_group_call_can_speak);
            bnt_group_call.setEnabled(true);
//        talkback_add_icon.setEnabled(true);
        }
    }


    /**
     * 准备说话
     */
    private void change2PreSpeaking() {
        getLogger().info("ptt.change2PreSpeaking()准备说话");
        if (PadApplication.getPadApplication().getGroupListenenState() == LISTENING) {
            return;
        }

        bnt_group_call.setImageResource(R.drawable.bg_group_call_wait);
        bnt_group_call.setEnabled(true);
    }

    /**
     * 等待
     */
    private void change2Waiting() {
        getLogger().info("ptt.change2Waiting准备说话");
        bnt_group_call.setImageResource(R.drawable.bg_group_call_wait);
        bnt_group_call.setEnabled(true);
    }

    /**
     * 听
     */
    private void change2Listening() {
        String speakMemberName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
        if (!TextUtils.isEmpty(speakMemberName)) {
            //设置说话人名字,在组呼来的handler中设置
        }
        if (isScanGroupCall) {
            if (GroupUtils.currentIsForbid()) {
                //如果当前组是禁呼的，不需要改变PPT的样式
                return;
            }
            getLogger().info("扫描组在组呼");
            bnt_group_call.setImageResource(R.drawable.bg_group_call_can_speak);
        } else {
            bnt_group_call.setImageResource(R.drawable.bg_group_call_other_speaking);
            getLogger().info("主界面，ptt被禁了  isPttPress：" + PadApplication.getPadApplication().isPttPress);
        }
    }

    /**
     * 开始说话
     */
    private void change2Speaking() {
        getLogger().info("ptt.change2Speaking()松开结束");
        bnt_group_call.setImageResource(R.drawable.bg_group_call_speaking);
        if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
            MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
        }
    }

    /**
     * 禁止组呼
     */
    private void change2Forbid() {
        getLogger().info("ptt.change2Forbid()按住排队");
        bnt_group_call.setImageResource(R.drawable.bg_group_call_other_speaking);
        getLogger().info("主界面，ptt被禁了  isPttPress：" + PadApplication.getPadApplication().isPttPress);
        bnt_group_call.setEnabled(false);
        if (PadApplication.getPadApplication().isPttPress) {
            pttUpDoThing();
        }
    }


    private void setCurrentGroupView() {
        int currentGroupNo = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        String groupName = DataUtil.getGroupName(currentGroupNo);
        String groupDepartmentName = DataUtil.getGroupDepartmentName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
        if (android.text.TextUtils.isEmpty(groupName) || android.text.TextUtils.isEmpty(groupDepartmentName)) {
            TerminalFactory.getSDK().getDataManager().getGroupByNo(currentGroupNo);
        } else {
            tx_ptt_group_name.setText(groupName);
        }
    }


    private void setPttText() {
        int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
        Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
        //响应组  普通用户  不在响应状态
        if (ResponseGroupType.RESPONSE_TRUE.toString().equals(groupByGroupNo.getResponseGroupType()) &&
                !groupByGroupNo.isHighUser() &&
                !TerminalFactory.getSDK().getGroupCallManager().getActiveResponseGroup().contains(currentGroupId)) {
            change2Forbid();
        } else if (PadApplication.getPadApplication().getGroupListenenState() != GroupCallListenState.IDLE) {
            change2Listening();
        } else if (PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.GRANTING) {
            change2PreSpeaking();
        } else if (PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.WAITING) {
            change2Waiting();
        } else if (PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
            change2Speaking();
        } else {
            change2Silence();
        }
    }

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {

        @Override
        public void handler(final boolean connected) {
            getLogger().info("主fragment收到服务是否连接的通知----->" + connected);
            mHandler.post(() -> setCurrentGroupView());
        }
    };

    /**
     * 登陆响应的消息
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = new ReceiveLoginResponseHandler() {
        @Override
        public void handler(int resultCode, String resultDes) {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                mHandler.post(() -> {
                    //当前文件夹、组数据的显示设置
                    setCurrentGroupView();
                });
            }
        }
    };

    /**
     * 更新所有数据信息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = new ReceiveUpdateAllDataCompleteHandler() {
        @Override
        public void handler(int errorCode, String errorDesc) {
            if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                mHandler.post(() -> {
                    //当前文件夹、组数据的显示设置
                    setCurrentGroupView();
                });
            }
        }
    };

    /**
     * 更新配置信息
     */
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                setCurrentGroupView();//当前的组和文件夹名字重置
//                setVideoIcon();
//                setScanGroupIcon();
//                if (groupScanId != 0 && MyApplication.instance.getGroupListenenState() == LISTENING) {
//                    setCurrentGroupScanView(groupScanId);
//                }
            });
        }
    };

    /**
     * 强制切组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {

        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            getLogger().info("TalkbackFragment收到强制切组： toGroupId：" + toGroupId);
            mHandler.post(() -> {
                setCurrentGroupView();
                if ((!MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false) && !MyTerminalFactory.getSDK().getParam(Params.GUARD_MAIN_GROUP, false))
                        || PadApplication.getPadApplication().getGroupListenenState() != LISTENING) {
                    change2Silence();
                }
            });
        }
    };

    /**
     * 主动方请求组呼的消息
     */
    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, final String resultDesc, int groupId) {
            getLogger().info("主动方请求组呼的消息：" + methodResult + "-------" + resultDesc);
            getLogger().info("主动方请求组呼的消息：" + MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode());
            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {

                if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
                    mHandler.post(() -> {
                        mHandler.removeMessages(HANDLER_GROUP_TIME);
                        timeProgress = 60;
                        tx_ptt_time.setText(String.valueOf(timeProgress));
                        mHandler.sendEmptyMessageDelayed(HANDLER_GROUP_TIME, 1000);
                        change2Speaking();
                        MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                    });
                } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {//响应组为禁用状态，低级用户无法组呼

                    ToastUtil.showToast(MainMapActivity.this, resultDesc);
                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
                    if (!groupByGroupNo.isHighUser()) {
                        mHandler.post(() -> change2Forbid());
                    } else {
                        mHandler.post(() -> change2Silence());
                    }
                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
                    mHandler.post(() -> ToastUtil.showToast(MainMapActivity.this, getString(R.string.cannot_talk)));
                    change2Silence();
                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
                    mHandler.post(() -> change2Waiting());
                } else {
                    mHandler.post(() -> {
                        if (PadApplication.getPadApplication().getGroupListenenState() != LISTENING) {
                            change2Silence();
                        } else {
                            isScanGroupCall = false;
                            change2Listening();
                        }
                    });
                }
            } else {
                ToastUtil.toast(MainMapActivity.this, resultDesc);
                mHandler.post(() -> {
                    if (PadApplication.getPadApplication().getGroupListenenState() != GroupCallListenState.LISTENING) {
                        change2Silence();
                    } else {
                        change2Listening();
                    }
                });
            }
        }
    };

    /**
     * 主动方停止组呼的消息
     */
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {
        @Override
        public void handler(int resultCode, String resultDesc) {
            getLogger().info("主动方停止组呼的消息ReceiveCeaseGroupCallConformationHander" + "/" + PadApplication.getPadApplication().getGroupListenenState());
            mHandler.post(() -> {
                if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                    if (PadApplication.getPadApplication().getGroupListenenState() == GroupCallListenState.LISTENING) {
                        isScanGroupCall = false;
                        change2Listening();
                    } else {
                        //如果是停止组呼
                        PadApplication.getPadApplication().isPttPress = false;
                        mHandler.removeMessages(HANDLER_GROUP_TIME);
                        timeProgress = 60;
                        tx_ptt_time.setText(getString(R.string.text_ptt));
                        change2Silence();
                    }
                }
            });
        }
    };

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, final String memberName, final int groupId, String groupName, CallMode currentCallMode) {
            getLogger().info("触发了被动方组呼来了receiveGroupCallIncommingHandler:" + "curreneCallMode " + currentCallMode + "-----" + PadApplication.getPadApplication().getGroupSpeakState());
            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())) {
                ToastUtil.showToast(MainMapActivity.this, getString(R.string.text_has_no_group_call_listener_authority));
            }
            mHandler.post(() -> {
                //是组扫描的组呼,且当前组没人说话，变文件夹和组名字
                if (groupId != MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                    isScanGroupCall = true;
//                    groupScanId = groupId;
//                    setCurrentGroupScanView(groupId, groupName);
                }
                //是当前组的组呼,且扫描组有人说话，变文件夹和组名字
                if (groupId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) && PadApplication.getPadApplication().getGroupListenenState() == LISTENING) {
                    isScanGroupCall = false;
//                    setCurrentGroupScanView(groupId, groupName);
                }
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, memberName);
            });

//            speakingId = groupId;
//            speakingName = memberName;

            if (currentCallMode == CallMode.GENERAL_CALL_MODE) {
                mHandler.post(() -> {
                    if (PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.GRANTING
                            ||PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.WAITING) {
                        change2Waiting();
                    } else if (PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.GRANTED) {
                        //什么都不用做
                    } else {
                        change2Listening();
                    }
                });
            }
        }
    };

    /**
     * 被动方组呼停止
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {

        @Override
        public void handler(int reasonCode) {
            mHandler.post(() -> {
//                groupScanId = 0;
                MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
                setCurrentGroupView();
                getLogger().info("被动方停止组呼" + "/" + PadApplication.getPadApplication().getGroupListenenState() + PadApplication.getPadApplication().isPttPress);
                if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
                    if (PadApplication.getPadApplication().isPttPress && PadApplication.getPadApplication().getGroupSpeakState() == GroupCallSpeakState.IDLE) {
                        //别人停止组呼，如果自己还是在按着，重新请求组呼
                        change2Speaking();
                    } else {
                        change2Silence();
                    }
                }
            });
        }
    };

    private ReceiveResponseGroupActiveHandler receiveResponseGroupActiveHandler = new ReceiveResponseGroupActiveHandler() {
        @Override
        public void handler(boolean isActive, int responseGroupId) {
            //如果时间到了，还在响应组会话界面，将PTT禁止
            Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(responseGroupId);
            if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == responseGroupId && !isActive &&
                    !groupByGroupNo.isHighUser()) {
                mHandler.post(() -> change2Forbid());
            }
        }
    };

    private ReceiveGetGroupByNoHandler receiveGetGroupByNoHandler = group -> mHandler.post(new Runnable() {
        @Override
        public void run() {
            mHandler.post(() -> tx_ptt_group_name.setText(group.getName()));
        }
    });

    /**
     * 切组后的消息回调
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            mHandler.post(() -> {
                setCurrentGroupView();
                setPttText();
            });
        }
    };

    // 警情临时组处理完成，终端需要切到主组，刷新通讯录
    private ReceiveResponseChangeTempGroupProcessingStateHandler receiveResponseChangeTempGroupProcessingStateHandler = (resultCode, resultDesc) -> {
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            int mainGroupId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);
            MyTerminalFactory.getSDK().getGroupManager().changeGroup(mainGroupId);
        }
    };

    /**
     * 成员信息改变
     */
    private ReceiveNotifyMemberChangeHandler receiveNotifyMemberChangeHandler = new ReceiveNotifyMemberChangeHandler() {

        @Override
        public void handler(MemberChangeType memberChangeType) {
            getLogger().info("触发了receiveNotifyMemberChangeHandler：" + memberChangeType);
//            online_number = MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers().size();
            if (memberChangeType == MemberChangeType.MEMBER_ACTIVE_GROUP_CALL) {
                mHandler.post(() -> change2Silence());

            } else if (memberChangeType == MemberChangeType.MEMBER_PROHIBIT_GROUP_CALL) {
                mHandler.post(() -> change2Forbid());
            }
//            mHandler.post(() -> tv_current_online.setText(String.format(getResources().getString(R.string.current_group_members), online_number)));


        }
    };

    /**
     * 更新ptt按钮的显示和隐藏
     */
    private ReceiveUpdateMainFrgamentPTTButtonHandler receiveUpdateMainFrgamentPTTButtonHandler = new ReceiveUpdateMainFrgamentPTTButtonHandler() {

        @Override
        public void handler(boolean show) {
            getLogger().info("ReceiveUpdateMainFrgamentPTTButtonHandler : "+show);
                mHandler.post(() -> rl_group_call.setVisibility(show?View.VISIBLE:View.GONE));
        }
    };

    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupByNoHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateMainFrgamentPTTButtonHandler);
    }

    public void unregistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseGroupActiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupByNoHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseChangeTempGroupProcessingStateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateMainFrgamentPTTButtonHandler);
    }
}
