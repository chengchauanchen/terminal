package com.vsxin.terminalpad.mvp.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpActivity;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.js.TerminalPadJs;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.MainMapPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.entity.MemberInfoBean;
import com.vsxin.terminalpad.mvp.ui.fragment.LayerMapFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.LiveFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.MemberInfoFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.NoticeFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.SmallMapFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.VsxFragment;
import com.vsxin.terminalpad.utils.HandleIdUtil;

import org.apache.http.util.TextUtils;

import butterknife.BindView;
import cn.vsx.SpecificSDK.OperateReceiveHandlerUtilSync;
import cn.vsx.SpecificSDK.instruction.groupCall.GroupCallInstruction;
import cn.vsx.SpecificSDK.instruction.groupCall.SendGroupCallListener;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCallingCannotClickHandler;
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

    @BindView(R.id.bnt_group_call)
    Button bnt_group_call;
    private GroupCallInstruction groupCallInstruction;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, MainMapActivity.class));
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        //registReceiveHandler();
        inflaterFragment();
        initWebMap();
        initPPT();

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
        getLogger().info("http://192.168.1.187:9011/offlineMap/indexPad.html?" + format);
        web_map.loadUrl("http://192.168.1.187:9011/offlineMap/indexPad.html?" + format);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregistReceiveHandler();
        groupCallInstruction.unBindReceiveHandler();
    }

    /******************************************组呼********************************************/

    @SuppressLint("ClickableViewAccessibility")
    private void initPPT() {
        bnt_group_call.setOnTouchListener(new OnPttTouchListenerImplementation());
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
//        int resultCode = MyTerminalFactory.getSDK().getGroupCallManager().requestCurrentGroupCall("");
//        getLogger().info("PTT按下以后resultCode:" + resultCode);
//        if (resultCode == BaseCommonCode.SUCCESS_CODE) {//允许组呼了
//            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCallingCannotClickHandler.class, true);
//            change2PreSpeaking();
//        } else if (resultCode == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {
//            change2Waiting();
//        } else {//组呼失败的提示
//            ToastUtil.groupCallFailToast(this, resultCode);
//        }

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
            if (!TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER))) {

            }
            //只有当前组不是禁呼的才恢复PPT的状态
            bnt_group_call.setText(R.string.press_blank_space_talk_text);
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
        bnt_group_call.setText(R.string.text_ready_to_speak);
        bnt_group_call.setEnabled(true);
    }

    /**
     * 等待
     */
    private void change2Waiting() {
        getLogger().info("ptt.change2Waiting准备说话");
        bnt_group_call.setText(R.string.text_ready_to_speak);
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
            bnt_group_call.setText(R.string.press_blank_space_talk_text);
        } else {
            bnt_group_call.setText(R.string.button_press_to_line_up);
            getLogger().info("主界面，ptt被禁了  isPttPress：" + PadApplication.getPadApplication().isPttPress);
        }
    }


//    /**
//     * 注册监听
//     */
//    public void registReceiveHandler(){
//        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveCallingCannotClickHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveRequestGroupCallConformationHandler);
//    }
//
//    /**
//     * 取消监听
//     */
//    public void unregistReceiveHandler(){
//        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveCallingCannotClickHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRequestGroupCallConformationHandler);
//    }
//
//    /**
//     * PTT按下时不可切换
//     */
//    private ReceiveCallingCannotClickHandler receiveCallingCannotClickHandler = new ReceiveCallingCannotClickHandler() {
//
//        @Override
//        public void handler(final boolean isCannotCheck) {
//            getLogger().info("change_group_show_area被禁了 ？ isCannotCheck：" + isCannotCheck);
//        }
//    };
//
//    /**
//     * 主动方请求组呼的消息
//     */
//    private ReceiveRequestGroupCallConformationHandler receiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
//        @Override
//        public void handler(final int methodResult, final String resultDesc, int groupId) {
//            getLogger().info("主动方请求组呼的消息：" + methodResult + "-------" + resultDesc);
//            getLogger().info("主动方请求组呼的消息：" + MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode());
//
//            if (MyTerminalFactory.getSDK().getGroupCallManager().getCurrentCallMode() == CallMode.GENERAL_CALL_MODE) {
//
//                if (methodResult == BaseCommonCode.SUCCESS_CODE) {//请求成功，开始组呼
//                    change2Speaking();
//                    MyTerminalFactory.getSDK().putParam(Params.CURRENT_SPEAKER, "");
//                } else if (methodResult == SignalServerErrorCode.RESPONSE_GROUP_IS_DISABLED.getErrorCode()) {//响应组为禁用状态，低级用户无法组呼
//
//                    ToastUtil.showToast(MainMapActivity.this, resultDesc);
//                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
//                    Group groupByGroupNo = TerminalFactory.getSDK().getGroupByGroupNo(currentGroupId);
//                    if (!groupByGroupNo.isHighUser()) {
//                        change2Forbid();
//                    } else {
//                        change2Silence();
//                    }
//                } else if (methodResult == SignalServerErrorCode.CANT_SPEAK_IN_GROUP.getErrorCode()) {//只听组
//                    ToastUtil.showToast(MainMapActivity.this, getString(R.string.cannot_talk));
//                    change2Silence();
//                } else if (methodResult == SignalServerErrorCode.GROUP_CALL_WAIT.getErrorCode()) {//请求等待中
//                    change2Waiting();
//                } else {
//                    if (PadApplication.getPadApplication().getGroupListenenState() != LISTENING) {
//                        change2Silence();
//                    } else {
//                        isScanGroupCall = false;
//                        change2Listening();
//                    }
//                }
//            } else {
//                ToastUtil.toast(MainMapActivity.this, resultDesc);
//                if (PadApplication.getPadApplication().getGroupListenenState() != GroupCallListenState.LISTENING) {
//                    change2Silence();
//                } else {
//                    change2Listening();
//                }
//            }
//
//        }
//    };

    /**
     * 开始说话
     */
    private void change2Speaking() {
        getLogger().info("ptt.change2Speaking()松开结束");
        bnt_group_call.setText(R.string.button_release_end);
        if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
            MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(true);
        }
    }

    /**
     * 禁止组呼
     */
    private void change2Forbid() {
        getLogger().info("ptt.change2Forbid()按住排队");
        bnt_group_call.setText(R.string.text_no_group_calls);
        getLogger().info("主界面，ptt被禁了  isPttPress：" + PadApplication.getPadApplication().isPttPress);
        bnt_group_call.setEnabled(false);
        if (PadApplication.getPadApplication().isPttPress) {
            pttUpDoThing();
        }
    }
}
