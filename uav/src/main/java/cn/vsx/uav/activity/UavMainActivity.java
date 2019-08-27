package cn.vsx.uav.activity;

import android.Manifest;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAirCraftStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.UavApplication;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.vc.activity.NewMainActivity;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.realname.AircraftBindingState;
import dji.common.realname.AppActivationState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.DialogUtils;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/17
 * 描述：
 * 修订历史：
 */
public class UavMainActivity extends NewMainActivity{

    private static final int MSG_INFORM_ACTIVATION = 1;
    private static final int ACTIVATION_DALAY_TIME = 1000;
    private AppActivationState appActivationState;
    private AircraftBindingState bindingState;
    private AtomicBoolean hasAppActivationListenerStarted = new AtomicBoolean(false);

    @Override
    public void initData(){
        super.initData();
        logger.info("UavMainActivity---initData");
        //申请电话权限，上报时拦截电话
        Permissions permissions = Permissions.build(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE);
        SoulPermission.getInstance().checkAndRequestPermissions(permissions, new CheckRequestPermissionsListener(){
            @Override
            public void onAllPermissionOk(Permission[] allPermissions){
                logger.info("申请成功权限:"+Arrays.toString(allPermissions));
            }

            @Override
            public void onPermissionDenied(Permission[] refusedPermissions){
                logger.info("申请被拒绝权限:"+Arrays.toString(refusedPermissions));
            }
        });
        startSDKRegistration();
        UavApplication.getApplication().startPushService();
    }

    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(() -> {
                //                    ToastUtil.showToast(getApplicationContext(),"registering, pls wait...");
                DJISDKManager.getInstance().registerApp(getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                            //                                ToastUtil.showToast(getApplicationContext(),"Register Success");
                            logger.info("注册大疆sdk："+djiError.getDescription());
                            DJISDKManager.getInstance().startConnectionToProduct();
                        } else {
                            showDialog(djiError.getDescription());
                            ToastUtil.showToast(getApplicationContext(),"大疆SDK注册失败");
                            logger.error("注册大疆sdk："+djiError.getDescription());
                        }


                    }

                    @Override
                    public void onProductDisconnect() {
                        Log.d(TAG, "onProductDisconnect");
                        //                            ToastUtil.showToast(getApplicationContext(),"Product Disconnected");
                        notifyStatusChange(false);

                    }
                    @Override
                    public void onProductConnect(BaseProduct baseProduct) {
                        Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                        notifyStatusChange(true);

                    }
                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                  BaseComponent newComponent) {

                        if (newComponent != null) {
                            newComponent.setComponentListener(isConnected -> {
                                Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                notifyStatusChange(isConnected);
                            });
                        }
                        Log.d(TAG,
                                String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                        componentKey,
                                        oldComponent,
                                        newComponent));

                    }
                });
            });
        }
    }

    private void notifyStatusChange(boolean connect){
        if(connect){
            Aircraft aircraft= AirCraftUtil.getAircraftInstance();
            if (null != aircraft ) {
                addAppActivationListenerIfNeeded();
                //                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,true);
            } else {
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,false);
            }
        }else {
            UavMainActivity.this.bindingState = null;
            UavMainActivity.this.appActivationState = null;
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,false);
        }
    }

    private void addAppActivationListenerIfNeeded() {
        AppActivationState appActivationState = AppActivationManager.getInstance().getAppActivationState();
        logger.info("addAppActivationListenerIfNeeded状态："+appActivationState);
        if (appActivationState != AppActivationState.ACTIVATED) {
            myHandler.sendEmptyMessageDelayed(MSG_INFORM_ACTIVATION, ACTIVATION_DALAY_TIME);
            if (hasAppActivationListenerStarted.compareAndSet(false, true)) {
                AppActivationState.AppActivationStateListener appActivationStateListener = appActivationState1 -> {
                    logger.info("AppActivationStateListener--onUpdate:" + appActivationState1.name());
                    UavMainActivity.this.appActivationState = appActivationState1;
                    if(myHandler != null && myHandler.hasMessages(MSG_INFORM_ACTIVATION)){
                        myHandler.removeMessages(MSG_INFORM_ACTIVATION);
                    }
                    if(appActivationState1 != AppActivationState.ACTIVATED){
                        myHandler.sendEmptyMessageDelayed(MSG_INFORM_ACTIVATION, ACTIVATION_DALAY_TIME);
                    }else{
                        if(checkIsAircraftConnected()){
                            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class, true);
                        }
                    }
                };
                AircraftBindingState.AircraftBindingStateListener bindingStateListener = bindingState -> {
                    logger.info("Binding State: " + bindingState);
                    UavMainActivity.this.bindingState = bindingState;
                    if(checkIsAircraftConnected()){
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class, true);
                    }
                };
                AppActivationManager.getInstance().addAppActivationStateListener(appActivationStateListener);
                AppActivationManager.getInstance().addAircraftBindingStateListener(bindingStateListener);
            }
        }else {
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,true);
        }
    }

    private boolean checkIsAircraftConnected(){
        return appActivationState == AppActivationState.ACTIVATED && bindingState == AircraftBindingState.BOUND;
    }

    private void loginToActivationIfNeeded() {

        AppActivationState appActivationState = AppActivationManager.getInstance().getAppActivationState();
        logger.info("AppActivationManager.getInstance().getAppActivationState():" + appActivationState);
        if (AppActivationManager.getInstance().getAppActivationState() == AppActivationState.LOGIN_REQUIRED) {
            UserAccountManager.getInstance()
                    .logIntoDJIUserAccount(UavMainActivity.this,
                            new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                                @Override
                                public void onSuccess(UserAccountState userAccountState) {
                                    ToastUtil.showToast(getApplicationContext(),"Login Successed!");
                                    logger.info("大疆账号登陆成功");
                                    MyTerminalFactory.getSDK().putParam(Params.DJ_LOGINED,true);
                                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,true);
                                }

                                @Override
                                public void onFailure(DJIError djiError) {
                                    ToastUtil.showToast(getApplicationContext(),"Login Successed!");
                                    logger.info("大疆账号登陆失败"+djiError);
                                    MyTerminalFactory.getSDK().putParam(Params.DJ_LOGINED,false);
                                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,false);
                                }
                            });
        }
    }

    @Override
    protected void handleMyMessage(Message msg){
        super.handleMyMessage(msg);
        if(msg.what == MSG_INFORM_ACTIVATION){
            loginToActivationIfNeeded();
        }
    }

    private void showDialog(String msg){
        DialogUtils.showDialog(this,msg);
    }
}
