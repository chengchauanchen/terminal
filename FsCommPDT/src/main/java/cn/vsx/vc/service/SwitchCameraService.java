package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.dsp.bean.CInt2Pracel;
import android.dsp.bean.CInt4Pracel;
import android.os.Build;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hytera.api.SDKManager;
import com.hytera.api.base.common.AccessoryListener;
import com.hytera.api.base.common.AccessoryManager;
import com.hytera.api.base.common.CommonManager;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUVCCameraConnectChangeHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiveRemoveSwitchCameraViewHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

public class SwitchCameraService extends BaseService{

    private ImageView mIvPhoneCamera;
    private ImageView mIvOutCamera;
    private TextView mTvOutCamera;
    private String type;
    private String cameraType;
    private RelativeLayout mLiveConnecting;
    private RelativeLayout mRlSwitchCamera;
    private ImageView mIvConnectingAnimate;
    private Logger logger = Logger.getLogger(this.getClass());
    private AccessoryManager mAccessoryManager;
    private static final int CANCELLIVE = 0;
    private long sendDataTime;

    private ArrayList<Integer> pushMembers;
    private String theme;

    public SwitchCameraService(){}

    @Override
    @SuppressLint("InflateParams")
    protected void setRootView(){
        rootView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_switch_camera_service, null);
    }

    @Override
    protected void findView(){
        mLiveConnecting = rootView.findViewById(R.id.live_connecting);
        mIvConnectingAnimate = rootView.findViewById(R.id.iv_connecting_animate);
        mRlSwitchCamera = rootView.findViewById(R.id.rl_switch_camera);
        mIvPhoneCamera = rootView.findViewById(R.id.iv_phone_camera);
        mIvOutCamera = rootView.findViewById(R.id.iv_out_camera);
        mTvOutCamera = rootView.findViewById(R.id.tv_out_camera);
    }

    @Override
    protected void initData(){
        if(Constants.HYTERA.equals(Build.MODEL)){
            try{
                CommonManager mCommonManager = SDKManager.getCommonManager(getApplicationContext());
                mAccessoryManager = mCommonManager.getAccessoryManager();
            }catch(Exception e){
                e.printStackTrace();
                logger.info(e);
            }
        }
    }

    @Override
    protected void initBroadCastReceiver(){}

    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            case CANCELLIVE:
                stopBusiness();
                break;
            case OFF_LINE:
                stopBusiness();
                break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
        if(!connected){
            if(!mHandler.hasMessages(OFF_LINE)){
                mHandler.sendEmptyMessageDelayed(OFF_LINE,3000);
            }
        }else {
            mHandler.removeMessages(OFF_LINE);
        }
    }

    @Override
    protected void initView(Intent intent){
        pushMembers = intent.getIntegerArrayListExtra(Constants.PUSH_MEMBERS);
        mLiveConnecting.setVisibility(View.GONE);
        mRlSwitchCamera.setVisibility(View.VISIBLE);
        cameraType = intent.getStringExtra(Constants.CAMERA_TYPE);
        type = intent.getStringExtra(Constants.TYPE);
        theme = intent.getStringExtra(Constants.THEME);
        if(Constants.RECODER_CAMERA.equals(cameraType)){
            mIvOutCamera.setImageResource(R.drawable.recoder_camera);
            mTvOutCamera.setText(getResources().getString(R.string.recoder_camera));
        }else if(Constants.UVC_CAMERA.equals(cameraType)){
            mIvOutCamera.setImageResource(R.drawable.out_camera);
            mTvOutCamera.setText(getResources().getString(R.string.out_camera));
        }
    }

    @Override
    protected void initListener(){
        mIvPhoneCamera.setOnClickListener(phoneCameraOnclickListener);
        mIvOutCamera.setOnClickListener(outCameraOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUVCCameraConnectChangeHandler);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUVCCameraConnectChangeHandler);
    }

    @Override
    protected void showPopMiniView(){}

    private View.OnClickListener phoneCameraOnclickListener = v -> {
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveRemoveSwitchCameraViewHandler.class);
        Intent intent = new Intent(SwitchCameraService.this,PhonePushService.class);
        intent.putExtra(Constants.TYPE,type);
        intent.putExtra(Constants.THEME,theme);
        intent.putExtra(Constants.PUSH_MEMBERS,pushMembers);
        startService(intent);
        mHandler.postDelayed(this::removeView,500);
    };

    private ReceiveUVCCameraConnectChangeHandler receiveUVCCameraConnectChangeHandler = connected -> {
        MyApplication.instance.usbAttached = connected;
        if(!connected){
            stopBusiness();
        }
    };

    private View.OnClickListener outCameraOnClickListener = v->{
        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveRemoveSwitchCameraViewHandler.class);
        if(Constants.RECODER_CAMERA.equals(cameraType)){
            sendDataTime = System.currentTimeMillis();
            sendData();
            mLiveConnecting.setVisibility(View.VISIBLE);
            mRlSwitchCamera.setVisibility(View.GONE);
            showConnectingAnimate();
            //如果超过10秒没有点击确认，就取消上报
            mHandler.sendEmptyMessageDelayed(CANCELLIVE,10000);

            //用普通手机测试代码
//            Intent intent = new Intent(SwitchCameraService.this,RecoderPushService.class);
//            intent.putExtra(Constants.TYPE,type);
//            intent.putExtra(Constants.THEME,theme);
//            intent.putExtra(Constants.PUSH_MEMBERS,pushMembers);
//            startService(intent);



        }else {
            Intent intent = new Intent(SwitchCameraService.this,UVCPushService.class);
            intent.putExtra(Constants.TYPE,type);
            intent.putExtra(Constants.THEME,theme);
            intent.putExtra(Constants.PUSH_MEMBERS,pushMembers);
            startService(intent);
            mHandler.postDelayed(this::removeView,500);
        }
    };
    private long lastReceiveTime = 0;

    private AccessoryListener accessoryListener  = new AccessoryListener(){
        @Override
        public void HRCPP_ACC_TransparentDataWithAP_Reply(int i){
            logger.info("HRCPP_ACC_TransparentDataWithAP_Reply"+ "AP_Reply"+"i:" + i);
        }

        @Override
        public void HRCPP_Get_CurrentAccType_Reply(int i, CInt2Pracel[] cInt2Pracels){
        }

        @Override
        public void getCurrentAccTypeAck(int i, CInt4Pracel cInt4Pracel){
            logger.info("getCurrentAccTypeAck---"+i+"--cInt4Pracel:"+cInt4Pracel);
        }

        @Override
        public void HRCPP_NB_ACC_TransparentDataWithAP(int i, int i1, byte[] bytes){

            logger.info("HRCPP_NB_ACC_TransparentDataWithAP:"+"AccType:" + i + "--DataLen:" + i1+"--hex:"+toHex(bytes));
            if(toHex(bytes).endsWith("100")){
                logger.info("被拒绝");
                hideConnectingAnimate();
                stopBusiness();

            }else if(toHex(bytes).endsWith("000")){

                if(System.currentTimeMillis() - lastReceiveTime > 1000 && System.currentTimeMillis() - sendDataTime <=10000){
                    lastReceiveTime = System.currentTimeMillis();
                    logger.info("接受");
                    hideConnectingAnimate();
                    Intent intent = new Intent(SwitchCameraService.this,RecoderPushService.class);
                    intent.putExtra(Constants.TYPE,type);
                    intent.putExtra(Constants.PUSH_MEMBERS,pushMembers);
                    startService(intent);
                    mHandler.removeMessages(CANCELLIVE);
                    mHandler.postDelayed(()->removeView(),500);
                }
            }
        }
    };

    public void sendData(){
        try {
            logger.info("sendData"+"new byte[]{1,11,0,2,0,1}");
            byte[] bytes = new byte[]{1,11,0,2,0,1};
            mAccessoryManager.accTransparentDataWithAP(0x08, bytes.length, bytes);
            mAccessoryManager.registerListener(accessoryListener,null);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void showConnectingAnimate(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.connecting_anim);
        if (anim != null){
            mIvConnectingAnimate.startAnimation(anim);
        }
    }

    private void hideConnectingAnimate(){
        mIvConnectingAnimate.clearAnimation();
    }

    private String toHex(byte[] buffer) {
        StringBuilder sb = new StringBuilder(buffer.length * 2);
        for(byte aBuffer : buffer){
            sb.append(Character.forDigit((aBuffer & 240) >> 4, 16));
            sb.append(Character.forDigit(aBuffer & 15, 16));
        }
        return sb.toString();
    }
}
