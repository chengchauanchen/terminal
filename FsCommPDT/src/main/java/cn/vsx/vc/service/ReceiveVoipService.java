package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiveVoipCallEndHandler;
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
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_receive_voip, null);
    }

    @Override
    protected void findView(){
//        mIvMemberPortraitChooice =  rootView.findViewById(R.id.iv_member_portrait_chooice);
        mTvMemberNameChooice =  rootView.findViewById(R.id.tv_member_name_chooice);
//        mTvMemberIdChooice =  rootView.findViewById(R.id.tv_member_id_chooice);
        mLlIndividualCallRefuse =  rootView.findViewById(R.id.ll_individual_call_refuse);
        mLlIndividualCallAccept =  rootView.findViewById(R.id.ll_individual_call_accept);

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
    }

    @Override
    protected void initView(Intent intent){
        wakeLock.acquire(10 * 1000);
//        PromptManager.getInstance().IndividualCallNotifyRing();
        userName = intent.getStringExtra(Constants.USER_NAME);
        mTvMemberNameChooice.setText(userName);
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
    }

    private View.OnClickListener refuseOnclickListener = v-> stopBusiness();

    private View.OnClickListener acceptOnclickListener = v->{
        Intent intent = new Intent(ReceiveVoipService.this,MultipartyCallService.class);
        intent.putExtra(Constants.USER_NAME,userName);
        startService(intent);
        mHandler.postDelayed(this::removeView,500);
    };

    private ReceiveVoipCallEndHandler receiveVoipCallEndHandler = (linphoneCall)->{
        stopBusiness();
    };
}
