package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallActiveEndHandler;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallEndHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/11
 * 描述：
 * 修订历史：
 */
public class ReceiveVoipService extends BaseService{

//    private ImageView mIvMemberPortraitChooice;
    private TextView mTvMemberNameChooice;
//    private TextView mTvMemberIdChooice;
    private LinearLayout mLlIndividualCallRefuse;
    private LinearLayout mLlIndividualCallAccept;
    private String userName;

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_receive_voip, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
        //如果屏幕宽度小于高度就开启横屏
        layoutParams1.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    @Override
    protected void findView(){
        ImageView mIvMemberPortraitChooice =  rootView.findViewById(R.id.iv_member_portrait_chooice);
        mIvMemberPortraitChooice.setImageResource(BitmapUtil.getUserPhoto());
        mTvMemberNameChooice =  rootView.findViewById(R.id.tv_member_name_chooice);
//        mTvMemberIdChooice =  rootView.findViewById(R.id.tv_member_id_chooice);
        mLlIndividualCallRefuse =  rootView.findViewById(R.id.ll_individual_call_refuse);
        mLlIndividualCallAccept =  rootView.findViewById(R.id.ll_individual_call_accept);

        //网络状态布局
        mLlNoNetwork = rootView.findViewById(R.id.ll_network_state);

    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @Override
    protected void initListener(){
        mLlIndividualCallRefuse.setOnClickListener(refuseOnclickListener);
        mLlIndividualCallAccept.setOnClickListener(acceptOnclickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipCallEndHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveVoipCallActiveEndHandler);
    }

    @Override
    protected void initView(Intent intent){
        wakeLock.acquire(10 * 1000);
//        PromptManager.getInstance().IndividualCallNotifyRing();
        userName = intent.getStringExtra(Constants.USER_NAME);
        logger.info("userName:"+userName);
//        mTvMemberNameChooice.setText(userName);
    }

    @Override
    protected void showPopMiniView(){
    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    protected void onNetworkChanged(boolean connected){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipCallEndHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveVoipCallActiveEndHandler);
    }

    private View.OnClickListener refuseOnclickListener = v-> {
        if(null != MyApplication.instance.linphoneCall){
            MyTerminalFactory.getSDK().getVoipCallManager().refuseCall(MyApplication.instance.linphoneCall);
            MyApplication.instance.linphoneCall = null;
        }
        stopBusiness();
    };

    private View.OnClickListener acceptOnclickListener = v->{
        Intent intent = new Intent(ReceiveVoipService.this,MultipartyCallService.class);
        intent.putExtra(Constants.USER_NAME,userName);
        logger.info("incomingCall-userName:"+userName);
        startService(intent);
        mHandler.postDelayed(this::removeView,500);
    };

    private ReceiveVoipCallEndHandler receiveVoipCallEndHandler = (linphoneCall)-> mHandler.post(this::stopBusiness);

    private ReceiveVoipCallActiveEndHandler receiveVoipCallActiveEndHandler = ()-> mHandler.post(this::stopBusiness);
}
