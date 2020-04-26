package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerLiveTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNobodyRequestVideoLiveHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSwitchCameraViewHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/25
 * 描述：收到别人请求我开启视频
 * 修订历史：
 */
public class ReceiveLiveCommingService extends BaseService{

    //    private ImageView mIvAvatarReport;
    private TextView mTvLiveReportName;
    private TextView mTvLiveReportId;
    private LinearLayout mLlLiveRespondRefuseTotal;
    private LinearLayout mLlLiveRespondAcceptTotal;
    protected String memberName;
    protected int memberId;

    public ReceiveLiveCommingService(){}


    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_receive_live, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    protected void findView(){
        ImageView mIvAvatarReport = rootView.findViewById(R.id.iv_avatar_report);
        mIvAvatarReport.setImageResource(BitmapUtil.getUserPhoto());
        mTvLiveReportName = rootView.findViewById(R.id.tv_live_report_name);
        mTvLiveReportId = rootView.findViewById(R.id.tv_live_report_id);
        mLlLiveRespondRefuseTotal = rootView.findViewById(R.id.ll_live_respond_refuse_total);
        mLlLiveRespondAcceptTotal = rootView.findViewById(R.id.ll_live_respond_accept_total);

    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void initListener(){
        mLlLiveRespondAcceptTotal.setOnClickListener(acceptOnClickListener);
        mLlLiveRespondRefuseTotal.setOnClickListener(refuseOnClickListener);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNobodyRequestVideoLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRemoveSwitchCameraViewHandler);
    }

    @Override
    protected void initView(Intent intent){
        memberName = intent.getStringExtra(Constants.MEMBER_NAME);
        memberId = intent.getIntExtra(Constants.MEMBER_ID, 0);
        mTvLiveReportName.setText(memberName);
        mTvLiveReportId.setText(HandleIdUtil.handleId(memberId));
        PromptManager.getInstance().VideoLiveInCommimgRing();
        wakeLock.acquire(10 * 1000);
    }

    @Override
    protected void showPopMiniView(){
    }

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case OFF_LINE:
                stopBusiness();
            break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveAnswerLiveTimeoutHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNobodyRequestVideoLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSwitchCameraViewHandler);
    }

//    private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler(){
//        @Override
//        public void handler(boolean connected){
////            if(!connected){
////                if(!mHandler.hasMessages(OFF_LINE)){
////                    mHandler.sendEmptyMessageDelayed(OFF_LINE,3000);
////                }
////            }else {
////                mHandler.removeMessages(OFF_LINE);
////            }
//        }
//    };

    //收到没人请求我开视频的消息，关闭界面和响铃
    private ReceiveNobodyRequestVideoLiveHandler receiveNobodyRequestVideoLiveHandler = () -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.other_cancel));
        MyTerminalFactory.getSDK().getLiveManager().ceaseLiving();
        mHandler.post(this::stopBusiness);
    };

    private ReceiveRemoveSwitchCameraViewHandler receiveRemoveSwitchCameraViewHandler = () ->{
        mHandler.post(this::removeView);
    };

    /**
     * 超时未回复answer 通知界面关闭
     **/
    private ReceiveAnswerLiveTimeoutHandler receiveAnswerLiveTimeoutHandler = () -> {
        PromptManager.getInstance().stopRing();//停止响铃
        ToastUtil.showToast(MyTerminalFactory.getSDK().application,getResources().getString(R.string.other_cancel));
        stopBusiness();
    };

    
    private View.OnClickListener acceptOnClickListener = v -> {
        onAcceptLive();
    };

    protected void onAcceptLive(){
        if(MyApplication.instance.usbAttached){
            Intent intent = new Intent(ReceiveLiveCommingService.this, SwitchCameraService.class);
            intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
            intent.putExtra(Constants.CAMERA_TYPE,Constants.UVC_CAMERA);
            intent.putExtra(Constants.THEME,"");
            startService(intent);
        }else{
            if(Constants.HYTERA.equals(Build.MODEL)){
                Intent intent = new Intent(ReceiveLiveCommingService.this,SwitchCameraService.class);
                intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
                intent.putExtra(Constants.CAMERA_TYPE,Constants.RECODER_CAMERA);
                intent.putExtra(Constants.THEME,"");
                startService(intent);
            }else{
                startPhonePushService();
            }
        }
    }

    private View.OnClickListener refuseOnClickListener = v -> {
        ToastUtil.showToast(MyTerminalFactory.getSDK().application, getResources().getString(R.string.refused));
        MyTerminalFactory.getSDK().getLiveManager().responseLiving(false);
        PromptManager.getInstance().stopRing();
        MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
        stopBusiness();
    };


    private void startPhonePushService(){
        MyApplication.instance.isPrivateCallOrVideoLiveHand = true;
        Intent intent = new Intent(MyTerminalFactory.getSDK().application, PhonePushService.class);
        intent.putExtra(Constants.TYPE,Constants.RECEIVE_PUSH);
        intent.putExtra(Constants.MEMBER_NAME, memberName);
        intent.putExtra(Constants.MEMBER_ID, memberId);
        intent.putExtra(Constants.THEME,"");
        startService(intent);
        mHandler.postDelayed(this::removeView,2000);
    }
}
