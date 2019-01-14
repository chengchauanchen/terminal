package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseStartLiveHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.view.IndividualCallTimerView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/29
 * 描述：主动发起个呼
 * 修订历史：
 */
public class StartIndividualCallService extends BaseService{

    private ImageView mIvIndividualCallRetractRequest;
//    private ImageView mIvMemberPortraitRequest;
    private TextView mTvMemberNameRequest;
    private TextView mTvMemberIdRequest;
    private LinearLayout mLlIndividualCallHangupRequest;
    private RelativeLayout mRlIndividualCallRequest;
    private RelativeLayout mPopMinimize;
    private IndividualCallTimerView mTimerView;
    private TextView mTvWaiting;
    private String memberName;
    private int memberId;
    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_start_individual_call, null);
    }

    @Override
    protected void findView(){
        mRlIndividualCallRequest = rootView.findViewById(R.id.rl_individual_call_request);
        mIvIndividualCallRetractRequest = rootView.findViewById(R.id.iv_individual_call_retract_request);
//        mIvMemberPortraitRequest = rootView.findViewById(R.id.iv_member_portrait_request);
        mTvMemberNameRequest = rootView.findViewById(R.id.tv_member_name_request);
        mTvMemberIdRequest = rootView.findViewById(R.id.tv_member_id_request);
        mLlIndividualCallHangupRequest = rootView.findViewById(R.id.ll_individual_call_hangup_request);
        mPopMinimize = rootView.findViewById(R.id.pop_minimize);
        mTvWaiting = rootView.findViewById(R.id.tv_waiting);
        mTimerView = rootView.findViewById(R.id.popup_ICTV_speaking_time);
    }

    @Override
    protected void initData(){
    }

    @Override
    protected void initBroadCastReceiver(){
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveResponseStartIndividualCallHandler);
        mIvIndividualCallRetractRequest.setOnClickListener(retractOnClickListener);
        mLlIndividualCallHangupRequest.setOnClickListener(stopCallListener);
        mPopMinimize.setOnTouchListener(miniPopOnTouchListener);
    }

    @Override
    protected void showPopMiniView(){
        MyApplication.instance.isMiniLive = true;
        mRlIndividualCallRequest.setVisibility(View.GONE);
        mPopMinimize.setVisibility(View.VISIBLE);
        mTvWaiting.setVisibility(View.VISIBLE);
        mTimerView.setVisibility(View.GONE);
    }

    @Override
    protected void initView(Intent intent){
        PromptManager.getInstance().IndividualCallRequestRing();
        memberId = intent.getIntExtra(Constants.MEMBER_ID,0);
        int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(memberId,"");
        if (resultCode == BaseCommonCode.SUCCESS_CODE){
            mRlIndividualCallRequest.setVisibility(View.VISIBLE);
            mPopMinimize.setVisibility(View.GONE);
            memberName = intent.getStringExtra(Constants.MEMBER_NAME);
            mTvMemberNameRequest.setText(memberName);
            mTvMemberIdRequest.setText(HandleIdUtil.handleId(memberId));
        }else {
            removeView();
            ToastUtil.individualCallFailToast(getApplicationContext(), resultCode);
        }

    }

    @Override
    protected void handleMesage(Message msg){
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReaponseStartLiveHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveResponseStartIndividualCallHandler);
    }

    private View.OnClickListener retractOnClickListener = v -> showPopMiniView();

    private View.OnClickListener stopCallListener = v -> individualCallStopped();

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener miniPopOnTouchListener = (v, event) -> {
        //触摸点到边界屏幕的距离
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触摸点到自身边界的距离
                downX = event.getX();
                downY = event.getY();
                oddOffsetX = layoutParams.x;
                oddOffsetY = layoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //不除以3，拖动的view抖动的有点厉害
                if(Math.abs(downX - moveX) > 5 || Math.abs(downY - moveY) > 5){
                    // 更新浮动窗口位置参数
                    layoutParams.x = (int) (screenWidth - (x + downX));
                    layoutParams.y = (int) (y - downY);
                    windowManager.updateViewLayout(rootView, layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                int newOffsetX = layoutParams.x;
                int newOffsetY = layoutParams.y;
                if(Math.abs(newOffsetX - oddOffsetX) <= 30 && Math.abs(newOffsetY - oddOffsetY) <= 30){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
                    windowManager.removeView(rootView);
                    windowManager.addView(rootView, layoutParams1);
                    MyApplication.instance.isMiniLive = false;
                }
                break;
        }
        return true;
    };

    /**
     * 主动方请求个呼回应
     */
    private ReceiveResponseStartIndividualCallHandler receiveResponseStartIndividualCallHandler = (resultCode, resultDesc, individualCallType) -> {
        logger.info("ReceiveResponseStartIndividualCallHandler====" + "resultCode:" + resultCode + "=====resultDesc:" + resultDesc);
        mTimerView.stop();
        if(resultCode == BaseCommonCode.SUCCESS_CODE){//对方接听
            logger.info("对方接受了你的个呼:" + resultCode + resultDesc + "callType;" + individualCallType);
            mHandler.post(() -> callAnswer(individualCallType));
        }else{//对方拒绝
            ToastUtil.showToast(StartIndividualCallService.this, resultDesc);
            mHandler.postDelayed(() -> {
                PromptManager.getInstance().IndividualHangUpRing();
                PromptManager.getInstance().delayedStopRing();
                removeView();
            },500);
        }
    };

    /**
     * 对方拒绝直播，通知界面关闭响铃页
     **/
    private ReceiveResponseStartLiveHandler receiveReaponseStartLiveHandler = (resultCode, resultDesc) -> {
        mTimerView.stop();
        ToastUtil.showToast(getApplicationContext(),resultDesc);
        PromptManager.getInstance().IndividualHangUpRing();
        PromptManager.getInstance().delayedStopRing();
        removeView();
    };

    private void callAnswer(int individualCallType){
        PromptManager.getInstance().stopRing();
        Intent intent = new Intent(this,CallingService.class);
        intent.putExtra(Constants.CALL_TYPE,individualCallType);
        intent.putExtra(Constants.MEMBER_NAME,memberName);
        intent.putExtra(Constants.MEMBER_ID,memberId);
        startService(intent);
        mHandler.postDelayed(this::removeView,2000);

    }

    private void individualCallStopped(){
        mTimerView.stop();
        PromptManager.getInstance().stopRing();
        MyTerminalFactory.getSDK().getIndividualCallManager().ceaseIndividualCall();
        removeView();
    }
}