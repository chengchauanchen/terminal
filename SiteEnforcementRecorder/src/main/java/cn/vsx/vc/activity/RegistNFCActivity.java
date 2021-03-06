package cn.vsx.vc.activity;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginModel;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRegistCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReturnAvailableIPHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ProgressDialog;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.receiveHandle.ReceiverStartAuthHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.KeyboarUtils;
import ptt.terminalsdk.tools.NetworkUtil;
import cn.vsx.vc.utils.SetToListUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;

public class RegistNFCActivity extends BaseActivity implements RecvCallBack, Actions {


    @Bind(R.id.regist)
    RelativeLayout ll_regist;
    @Bind(R.id.iv_nfc)
    ImageView iv_nfc;


    private AlertDialog netWorkDialog;
    private int reAuthCount;
    private Timer timer = new Timer();
    private ProgressDialog myProgressDialog;
    private Handler myHandler = new Handler();
    public Logger logger = Logger.getLogger(getClass());

    public static final int REQUEST_PERMISSION_SETTING = 1235;
    public static final int OPEN_NET_CODE = 1236;
    public static final int REQUEST_CODE_SCAN = 1237;

    @Override
    protected void onResume() {
        super.onResume();
        KeyboarUtils.getKeyBoardHeight(this);
        Glide.with(this).load(R.drawable.ic_nfc)
                .asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(iv_nfc);
    }

/**============================================================================handler=================================================================================**/

//    /**
//     * 网络连接状态
//     */
//    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> RegistNFCActivity.this.runOnUiThread(() -> {
//        if (!MyTerminalFactory.getSDK().getParam(Params.IS_FORBID, false)) {
//            if (!connected) {
////                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "网络异常");
//            } else {
//                ToastUtil.closeToast();
//                if (TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.REGIST_URL, ""))) {
//                    againReAuth();
//                }
//            }
//        }
//    });

    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final boolean isRegisted) {
            logger.info("receiveSendUuidResponseHandler------resultCode：" + resultCode + "；   resultDesc：" + resultDesc + "；   isRegisted：" + isRegisted);
            myHandler.post(() -> {
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        changeProgressMsg("正在连接服务器");
                    } else if (resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()) {
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistNFCActivity.this)
                                .setTitle("提示")
                                .setMessage(resultCode + ":暂时未开通权限")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                })
                                .create();
                        alerDialog.show();
                    } else if (resultCode == TerminalErrorCode.DEPT_EXPIRED.getErrorCode()) {
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistNFCActivity.this)
                                .setTitle("提示")
                                .setMessage(resultCode + ":部门授权过期")
                                .setPositiveButton("确定", (dialogInterface, i) -> finish())
                                .create();
                        alerDialog.show();
                    } else if (resultCode == TerminalErrorCode.TERMINAL_TYPE_ERROR.getErrorCode()) {
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistNFCActivity.this)
                                .setTitle("提示")
                                .setMessage(resultCode + ":认证失败,类型不匹配")
                                .setPositiveButton("确定", (dialogInterface, i) -> finish())
                                .create();
                        alerDialog.show();
                    }else if(resultCode == TerminalErrorCode.TERMINAL_REPEAT.getErrorCode()){
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistNFCActivity.this)
                                .setTitle("提示")
                                .setMessage(resultCode + ":登录失败,找到多个重复用户")
                                .setPositiveButton("确定", (dialogInterface, i) -> finish())
                                .create();
                        alerDialog.show();
                    } else if(resultCode == TerminalErrorCode.EXCEPTION.getErrorCode()){
                            if(reAuthCount < 3){
                                reAuthCount++;
                                //发生异常的时候重试几次，因为网络原因经常导致一个io异常
                                TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getAuthManagerTwo().getTempIp(),TerminalFactory.getSDK().getAuthManagerTwo().getTempPort());
                            }else{
                                changeProgressMsg("认证失败");
                                myHandler.postDelayed(() -> exit(), 3000);
                            }
                    }else if(resultCode == TerminalErrorCode.TERMINAL_FAIL.getErrorCode()){
                        changeProgressMsg("认证失败");
                        myHandler.postDelayed(()->{
                            ll_regist.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        },2000);
                    } else {
                        //没有注册服务地址，去探测地址
                        if(availableIPlist.isEmpty()){
                            TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                        }
                        if(!isRegisted){
                            ToastUtils.showShort("请注册账号");
                            ll_regist.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }else{
                            changeProgressMsg(resultDesc);
                            myHandler.postDelayed(()->{
                                ll_regist.setVisibility(View.VISIBLE);
                                hideProgressDialog();
                            },2000);
                        }
                    }
            });
        }
    };

    /**
     * 注册完成的消息
     */
    private ReceiveRegistCompleteHandler receiveRegistCompleteHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {//注册成功，直接登录
            logger.info("注册完成的回调----注册成功，直接登录");
            changeProgressMsg("正在登入...");
//            if(MyTerminalFactory.getSDK().isServerConnected()){
//                login();
//            }
        } else {//注册失败，提示并关界面

            if (errorCode == TerminalErrorCode.REGISTER_PARAMETER_ERROR.getErrorCode()) {
                changeProgressMsg("邀请码错误，请重新注册！");
            } else if (errorCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()) {
                changeProgressMsg(errorCode + "注册失败，请检查各项信息是否正确！");
            } else {
                ToastUtil.showToast(RegistNFCActivity.this, errorDesc);
            }
            myHandler.postDelayed(() -> {
                ll_regist.setVisibility(View.VISIBLE);
                hideProgressDialog();
            }, 2000);
        }
    });


    /**
     * 登陆响应的消息
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> {
        logger.info("RegistActivity---收到登录的消息---resultCode:" + resultCode + "     resultDesc:" + resultDesc);
        myHandler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg("正在更新数据");
            } else if(resultCode == Params.JOININ_WARNING_GROUP_ERROR_CODE||resultCode == Params.JOININ_GROUP_ERROR_CODE) {
                changeProgressMsg(resultDesc);
                myHandler.postDelayed(() -> {
                    hideProgressDialog();
                    ll_regist.setVisibility(View.VISIBLE);
                }, 2000);
            }else{
                changeProgressMsg(resultDesc);
                myHandler.postDelayed(() -> {
                    hideProgressDialog();
                    exit();
                }, 3000);
            }
        });
    };

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {
            logger.info("更新数据成功！");
            goOn();
        } else {
            changeProgressMsg("更新数据时：" + errorDesc);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    hideProgressDialog();
                    finish();
                }
            }, 3000);
        }
    });

    ArrayList<String> availableIPlist = new ArrayList<>();
    /**
     * 获取可用的IP列表
     **/
    private ReceiveReturnAvailableIPHandler receiveReturnAvailableIPHandler = availableIP -> myHandler.post(() -> {
        logger.info("收到可用IP列表");
        if (availableIP.size() > 0) {
            availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
        }
        //拿到可用IP列表，遍历取第一个
        if (availableIP != null && availableIP.size() > 0) {
            LoginModel authModel = null;
            for (Map.Entry<String, LoginModel> entry : availableIP.entrySet()) {
                authModel = entry.getValue();
            }
            if (authModel != null) {

                int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(authModel.getIp(),authModel.getPort());
                if(resultCode == BaseCommonCode.SUCCESS_CODE){
                    changeProgressMsg("正在认证...");
                    ll_regist.setVisibility(View.GONE);
                }else {
                    //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                }
            }
        } else {
            ToastUtil.showToast(RegistNFCActivity.this, "服务器地址不可用");
            ll_regist.setVisibility(View.VISIBLE);
            hideProgressDialog();
        }
    });

    /**
     * 刷入NFC开始认证
     */
    private ReceiverStartAuthHandler receiverStartAuthHandler = (showMessage) -> myHandler.post(() -> {
        logger.info("刷入NFC开始认证");
        judgePermission();
    });

    /**
     * ============================================================================Listener=================================================================================
     **/

    @Override
    public int getLayoutResId() {
        return R.layout.activity_regist_nfc;
    }

    @Override
    public void initView() {
        if (!this.isTaskRoot()) { //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            //如果你就放在launcher Activity中话，这里可以直接return了
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;//finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
            }
        }
        initDialog();
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(RegistNFCActivity.this);
            myProgressDialog.setCancelable(false);
        }
//        ll_regist.setVisibility(View.GONE);
        judgePermission();

    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("网络没有连接，是否打开网络？");
        builder.setPositiveButton("确定", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivityForResult(intent, OPEN_NET_CODE);
        });
        builder.setNegativeButton("取消", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            RegistNFCActivity.this.finish();
        });
        builder.setCancelable(false);
        netWorkDialog = builder.create();
    }

    @OnClick(R.id.ll_qr_recoder_scan)
    public void onClick(View view){
        switch (view.getId()){
             case R.id.ll_qr_recoder_scan:
//                 goToQRCodeScan();
//                 String imsi = MyTerminalFactory.getSDK().getIMSI();
//                 logger.debug("获取imsi:"+imsi);
                 break;
         }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CheckMyPermission.REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    judgePermission();
                } else {
                    // Permission Denied
                    permissionDenied(permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            case CheckMyPermission.REQUEST_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    judgePermission();
                } else {
                    // Permission Denied
                    permissionDenied(permission.READ_PHONE_STATE);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 必须要有SD卡和读取电话状态的权限，APP才能使用
     */
    @Override
    public void judgePermission() {
        if (CheckMyPermission.selfPermissionGranted(this, permission.WRITE_EXTERNAL_STORAGE)) {//SD卡读写权限
            if (CheckMyPermission.selfPermissionGranted(this, permission.READ_PHONE_STATE)) {//手机权限，获取uuid
                SpecificSDK.getInstance().configLogger();
                checkIfAuthorize();
            } else {
                CheckMyPermission.permissionPrompt(this, permission.READ_PHONE_STATE);
            }

        } else {
            CheckMyPermission.permissionPrompt(this, permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * 根据是否有网络连接去登录或者打开设置
     */
    private void checkIfAuthorize() {
        if (NetworkUtil.isConnected(MyApplication.instance.getApplicationContext())) {
            if (netWorkDialog != null) {
                netWorkDialog.dismiss();
            }
            start();
        } else {
            if (netWorkDialog != null && !netWorkDialog.isShowing()) {
                netWorkDialog.show();
            }
        }
    }

    private void permissionDenied(final String permissionName) {
        new DialogUtil() {
            @Override
            public CharSequence getMessage() {
//                            return "4GPTT需要访问您设备上的照片、媒体内容和文件，否则无法工作；去打开权限?";
                return CheckMyPermission.getDesForPermission(permissionName,getResources().getString(R.string.app_name));
            }

            @Override
            public Context getContext() {
                return RegistNFCActivity.this;
            }

            @Override
            public void doConfirmThings() {
                //点击确定时跳转到设置界面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
            }

            @Override
            public void doCancelThings() {
                RegistNFCActivity.this.finish();
            }
        }.showDialog();
    }

    private void start() {
        //发送认证消息，uuid到注册服务器，判断是注册还是登录
        if(DataUtil.getRecorderBindBean() != null){
            if(!MyTerminalFactory.getSDK().isStart()){
                TerminalFactory.getSDK().start();
                PromptManager.getInstance().start();
            }
            ll_regist.setVisibility(View.GONE);
            PromptManager.getInstance().start();
            //进入注册界面了，先判断有没有认证地址
            String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
            if(TextUtils.isEmpty(authUrl)){
                //平台包或者没获取到类型，直接用AuthManager中的地址,
                String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
                if(AuthManagerTwo.POLICESTORE.equals(apkType) || AuthManagerTwo.POLICETEST.equals(apkType) || TextUtils.isEmpty(apkType)){
                    String[] defaultAddress = TerminalFactory.getSDK().getAuthManagerTwo().getDefaultAddress();
                    if(defaultAddress.length>=2){
                        int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(defaultAddress[0],defaultAddress[1]);
                        if(resultCode == BaseCommonCode.SUCCESS_CODE){
                            changeProgressMsg("正在认证...");
                        }else {
                            //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                        }
                    }else {
                        //没有注册服务地址，去探测地址
                        TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                    }
                }else {
                    //没有注册服务地址，去探测地址
                    TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                }
            }else {
                //有注册服务地址，去认证
                int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getParam(Params.REGIST_IP,""),TerminalFactory.getSDK().getParam(Params.REGIST_PORT,""));
                if(resultCode == BaseCommonCode.SUCCESS_CODE){
                    changeProgressMsg("正在认证...");
                }else {
                    //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                }
            }
        }
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
//        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReturnAvailableIPHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverStartAuthHandler);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // 把状态变为登陆 能使父类不会走 protectApp()
        MyApplication.instance.mAppStatus = Constants.LOGINED;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {
         //开启解绑倒计时
//        MyApplication.instance.startAccountValidClock();
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);
//        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReturnAvailableIPHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverStartAuthHandler);
        myHandler.removeCallbacksAndMessages(null);
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
            myProgressDialog = null;
        }
        reAuthCount = 0;

    }

    @Override
    protected void startLiveService() {
    }

    @Override
    protected void requestStartLive() {
    }

    @Override
    protected void finishVideoLive() {
    }

    private void hideProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
        }
    }

    private void goOn() {
        changeProgressMsg("启动成功...");
        myHandler.removeCallbacksAndMessages(null);
        startActivity(new Intent(RegistNFCActivity.this, MainActivity.class));
        overridePendingTransition(0, R.anim.alpha_hide);
        hideProgressDialog();
        finish();
    }

    private void changeProgressMsg(final String msg) {
        myHandler.post(() -> {
            if (myProgressDialog != null && !isFinishing()) {
                myProgressDialog.setMsg(msg);
                myProgressDialog.show();
            }
        });
    }

    /**
     * 跳转到扫码登录页面
     */
    private void goToQRCodeScan(){
//        Intent intent = new Intent(this, CaptureActivity.class);
//        ZxingConfig config = new ZxingConfig();
//        config.setShowbottomLayout(false);//底部布局（包括闪光灯和相册）
//        config.setPlayBeep(true);//是否播放提示音
//        config.setShake(true);//是否震动
//        config.setReactColor(R.color.ok_blue);
//        config.setScanLineColor(R.color.ok_blue);
//        //config.setShowAlbum(true);//是否显示相册
//        //config.setShowFlashLight(true);//是否显示闪光灯
//        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
//        startActivityForResult(intent, REQUEST_CODE_SCAN);

        Intent intent = new Intent(this, MyCaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            judgePermission();
        } else if (requestCode == OPEN_NET_CODE) {
            judgePermission();
        } else if (requestCode == REQUEST_CODE_SCAN) {
            //拿到二维码扫描的结果
            if(resultCode == RESULT_OK && data != null){
                if (null != data) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }
                    if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        logger.info("扫描二维码结果："+result);
//                        setManualNFCBean(NfcUtil.getRecorderBindTranslateBean(result));
//                        Toast.makeText(this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                    } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                        ToastUtil.showToast(this,"解析二维码失败");
                    }
                }
//                String result = data.getStringExtra(Constant.CODED_CONTENT);
//                logger.info("扫描二维码结果："+result);
//                // TODO: 2019/4/10 给注册服务发送扫码结果
//                setManualNFCBean(NfcUtil.getRecorderBindTranslateBean(result));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    private void exit() {
        finish();
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult", "0");
        SendRecvHelper.send(getApplicationContext(), stoppedCallIntent);

        PromptManager.getInstance().stop();
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopPTTButtonEventService();
        MyTerminalFactory.getSDK().stop();
        Process.killProcess(Process.myPid());
    }



}
