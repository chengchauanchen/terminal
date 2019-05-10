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
import android.widget.RelativeLayout;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthModel;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
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
import cn.vsx.vc.utils.NetworkUtil;
import cn.vsx.vc.utils.SetToListUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;

public class RegistNFCActivity extends BaseActivity implements RecvCallBack, Actions {


    @Bind(R.id.regist)
    RelativeLayout ll_regist;

    private AlertDialog netWorkDialog;
    private int reAuthCount;
    private Timer timer = new Timer();
    private ProgressDialog myProgressDialog;
    private Handler myHandler = new Handler();
    public Logger logger = Logger.getLogger(getClass());

    public static final int REQUEST_PERMISSION_SETTING = 1235;
    public static final int OPEN_NET_CODE = 1236;

    @Override
    protected void onResume() {
        super.onResume();
        KeyboarUtils.getKeyBoardHeight(this);
    }

/**============================================================================handler=================================================================================**/

    /**
     * 网络连接状态
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> RegistNFCActivity.this.runOnUiThread(() -> {
        if (!MyTerminalFactory.getSDK().getParam(Params.IS_FORBID, false)) {
            if (!connected) {
//                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "网络异常");
            } else {
                ToastUtil.closeToast();
                if (TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.REGIST_URL, ""))) {
                    againReAuth();
                }
            }
        }
    });

    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final boolean isRegisted) {
            logger.info("receiveSendUuidResponseHandler------resultCode：" + resultCode + "；   resultDesc：" + resultDesc + "；   isRegisted：" + isRegisted);
            myHandler.post(() -> {
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        if (isRegisted) {//卸载后重装，应该显示注册过了,直接去登录
                            ll_regist.setVisibility(View.GONE);
                            changeProgressMsg("正在登入...");
//                            if(MyTerminalFactory.getSDK().isServerConnected()){
//                                login();
//                            }
                        } else {//没注册
                            MyTerminalFactory.getSDK().putParam(Params.MESSAGE_VERSION, 0l);
                            if (availableIPlist.size() < 1) {
                                //重新探测
                                againReAuth();
                                changeProgressMsg("正在找服务器");
                            } else {
                                ll_regist.setVisibility(View.VISIBLE);
                                hideProgressDialog();
                            }
                        }
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
                    }else if(resultCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()){
                        //发生异常的时候重试几次，因为网络原因经常导致一个io异常
                        sendUuid(null, null);
                    } else {
                        if (isRegisted) {
                            sendUuid(null, null);
                        } else {//第一次登录
                            if (availableIPlist.size() < 1) {
                                logger.info("第一次登陆App,开始探测ip列表");
                                //重新探测
                                againReAuth();
                                changeProgressMsg("正在找服务器");
                            } else {
                                ll_regist.setVisibility(View.VISIBLE);
                                hideProgressDialog();
                            }
                        }
                    }
                //版本的文字提示：内网、西城、东城
                logger.info("地点是：" + TerminalFactory.getSDK().getParam(Params.PLACE));
//                    tvVersionPrompt.setText(TerminalFactory.getSDK().getParam(Params.PLACE, "zectec") + " " + DataUtil.getVersion(RegistActivity.this));
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
                updateData();
            } else {
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
            AuthModel authModel = null;
            for (Map.Entry<String, AuthModel> entry : availableIP.entrySet()) {
                authModel = entry.getValue();
            }
            if (authModel != null) {
                MyTerminalFactory.getSDK().getAuthManagerTwo().reAuth(false, authModel.getIp(), authModel.getPort());
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
    public void judgePermission() {
        if (CheckMyPermission.selfPermissionGranted(this, permission.WRITE_EXTERNAL_STORAGE)) {//SD卡读写权限
            if (CheckMyPermission.selfPermissionGranted(this, permission.READ_PHONE_STATE)) {//手机权限，获取uuid
                MyApplication.instance.getSpecificSDK().configLogger();
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
                return CheckMyPermission.getDesForPermission(permissionName);
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
        if(DataUtil.getNFCBean() != null){
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            MyTerminalFactory.getSDK().start();
            PromptManager.getInstance().start();
        });
            changeProgressMsg("正在获取信息");
            ll_regist.setVisibility(View.GONE);
            sendUuid(null, null);
        }
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
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
        MyApplication.instance.startAccountValidClock();
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReturnAvailableIPHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverStartAuthHandler);
        myHandler.removeCallbacksAndMessages(null);
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
            myProgressDialog = null;
        }
        reAuthCount = 0;

    }

    private void hideProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
        }
    }

//    private void regist(final String memberName, final String orgBlockCode) {
//        if (!Util.isEmpty(memberName)) {
//            changeProgressMsg("正在注册...");
//            ll_regist.setVisibility(View.GONE);
//        }
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                MyTerminalFactory.getSDK().getAuthManagerTwo()
//                        .regist(memberName, orgBlockCode);
//            }
//        }, 500);
//    }

    private void updateData() {
        changeProgressMsg("正在更新数据...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MyTerminalFactory.getSDK().getConfigManager().updateAll();
            }
        }, 500);
    }

    private void login() {
        changeProgressMsg("正在登入...");
        TerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getAuthManagerTwo().login());
    }

    private void sendUuid(final String ip, final String port) {
        reAuthCount++;
        if (reAuthCount > 3) {
            changeProgressMsg("登录失败，请检查网络是否连接");
            myHandler.postDelayed(() -> {
                hideProgressDialog();
                exit();
            }, 500);
        } else {
            changeProgressMsg("正在尝试第" + reAuthCount + "次连接");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MyTerminalFactory.getSDK().getAuthManagerTwo().reAuth(false, ip, port);
                }
            }, 500);
        }
    }

    private void againReAuth() {
        MyTerminalFactory.getSDK().getAuthManagerTwo().reAuthOne();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            judgePermission();
        } else if (requestCode == OPEN_NET_CODE) {
            judgePermission();
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

        MyTerminalFactory.getSDK().exit();//停止服务
        PromptManager.getInstance().stop();
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopPTTButtonEventService();
        MyTerminalFactory.getSDK().stop();
        Process.killProcess(Process.myPid());
    }

}
