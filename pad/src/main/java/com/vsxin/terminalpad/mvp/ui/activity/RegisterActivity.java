package com.vsxin.terminalpad.mvp.ui.activity;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpActivity;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.constant.AppStatusConstants;
import com.vsxin.terminalpad.mvp.contract.presenter.RegisterPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IRegisterView;
import com.vsxin.terminalpad.mvp.ui.widget.ProgressDialog;
import com.vsxin.terminalpad.mvp.ui.widget.XCDropDownListView;
import com.vsxin.terminalpad.mvp.ui.widget.XCDropDownListView.XCDropDownListViewClickListeren;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.NetworkUtil;
import com.vsxin.terminalpad.utils.SetToListUtil;
import com.vsxin.terminalpad.utils.SystemUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginModel;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRegistCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReturnAvailableIPHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 注册登录
 */
public class RegisterActivity extends MvpActivity<IRegisterView, RegisterPresenter> implements IRegisterView {

    public Logger logger = Logger.getLogger(getClass());
    private int reAuthCount = 0;
    private Handler myHandler = new Handler();

    @BindView(R.id.xcd_available_ip)//选择环境
    XCDropDownListView xcDropDownListView;

    @BindView(R.id.btn_confirm)
    Button btn_confirm;//注册

    @BindView(R.id.et_invitation_code)
    EditText et_invitation_code;//邀请码

    @BindView(R.id.et_user_name)
    EditText et_user_name;//姓名


    @Override
    protected int getLayoutResID() {
        return R.layout.activity_register;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        //将app设置为登录状态
        PadApplication.setmAppStatus(AppStatusConstants.LOGINED);
        initDialog();

        xcDropDownListView.setOnXCDropDownListViewClickListeren(new XCDropDownListViewClickListeren() {
            @Override
            public void onXCDropDownListViewClickListeren(int position) {
                if (availableIPlist.size() == 2) {
                    if (position != 0) {

                    }
                }
                if (availableIPlist.size() > 2) {
                    if (position == (availableIPlist.size() - 1)) {
                    } else if (position != 0) {
                        String name = availableIPlist.get(position);
                        availableIPlist.remove(name);
                        availableIPlist.remove(getString(R.string.text_selection_unit));
                        availableIPlist.add(0, name);
                        xcDropDownListView.setItemsData(availableIPlist);
                        String selectIp = availableIPMap.get(name).getIp();
                        String selectPort = availableIPMap.get(name).getPort();
                        //认证
                        int resultCode = MyTerminalFactory.getSDK().getAuthManagerTwo().startAuth(selectIp, selectPort);
                        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                            changeProgressMsg(getString(R.string.authing));
                        }
                    }
                }
            }
        });

        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(RegisterActivity.this);
            myProgressDialog.setCancelable(false);
        }

        btn_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        judgePermission();
    }

    /**
     * 注册用户
     */
    private void registerUser() {
        String invitationCode = et_invitation_code.getText().toString().trim();
        String userName = et_user_name.getText().toString().trim();
        getPresenter().registerUser(userName,invitationCode);
    }

    @Override
    protected void initData() {
        //1获取可用的IP列表
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReturnAvailableIPHandler);
        //2认证回调
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        //3连接接入服务器回调
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        //4注册完成的消息
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);
        //5登陆响应的消息
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        //6更新所有数据信息的消息
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
        //退出消息
        MyTerminalFactory.getSDK().registReceiveHandler(receiveExitHandler);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReturnAvailableIPHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveExitHandler);
    }

    @Override
    public RegisterPresenter createPresenter() {
        return new RegisterPresenter(this);
    }


    ArrayList<String> availableIPlist = new ArrayList<>();
    Map<String, LoginModel> availableIPMap = new HashMap<>();
    private String company = "添加单位";


    /**
     * 必须要有SD卡和读取电话状态的权限，APP才能使用
     */
    private void judgePermission() {
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
        if (NetworkUtil.isConnected(PadApplication.getApplication().getApplicationContext())) {
            if (netWorkDialog != null) {
                netWorkDialog.dismiss();
            }
            changeProgressMsg(getString(R.string.text_get_info_now));
            //authorize();//取第三方app（武汉警务平台）认证并获取user信息
            requestDrawOverLays();
        } else {
            if (netWorkDialog != null && !netWorkDialog.isShowing()) {
                netWorkDialog.show();
            }
        }
    }

    private AlertDialog netWorkDialog;
    public static final int OPEN_NET_CODE = 1236;

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_network_is_not_connected_check_network_is_open);
        builder.setPositiveButton(getString(R.string.text_sure), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivityForResult(intent, OPEN_NET_CODE);
        });
        builder.setNegativeButton(getString(R.string.text_cancel), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            finish();
        });
        builder.setCancelable(false);
        netWorkDialog = builder.create();
    }

    private ProgressDialog myProgressDialog;

    @Override
    public void changeProgressMsg(final String msg) {
        if (myProgressDialog != null && !isFinishing()) {
            myHandler.post(() -> {
                myProgressDialog.setMsg(msg);
                if (!myProgressDialog.isShowing()) {
                    myProgressDialog.show();
                }
            });
        }
    }

    private void hideProgressDialog() {
        if (myProgressDialog != null && !isFinishing()) {
            myHandler.post(() -> {
                if (myProgressDialog.isShowing()) {
                    myProgressDialog.dismiss();
                }
            });
        }
    }

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

    public void requestDrawOverLays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(RegisterActivity.this)) {
                ToastUtil.showToast(PadApplication.getApplication().getApplicationContext(), getString(R.string.open_overlay_permisson));
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            } else {
                //MyApplication.instance.startHandlerService();
                start();
            }
        } else {
            //MyApplication.instance.startHandlerService();
            start();
        }
    }

    private void start() {
        //sdk提示音管理类
        PromptManager.getInstance().start();
        //进入注册界面了，先判断有没有认证地址
        String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
        System.out.println("服务器的地址:" + authUrl);
        if (TextUtils.isEmpty(authUrl)) {
            //平台包或者没获取到类型，直接用AuthManager中的地址,
            String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            if (AuthManagerTwo.POLICESTORE.equals(apkType) || AuthManagerTwo.POLICETEST.equals(apkType) ||
                    AuthManagerTwo.XIANGYANGPOLICESTORE.equals(apkType) || TextUtils.isEmpty(apkType)) {
                String[] defaultAddress = TerminalFactory.getSDK().getAuthManagerTwo().getDefaultAddress();
                if (defaultAddress.length >= 2) {
                    int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(defaultAddress[0], defaultAddress[1]);
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        changeProgressMsg(getString(R.string.authing));
                    } else {
                        //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                    }
                } else {
                    //没有注册服务地址，去探测地址
                    TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                }
            } else {
                //没有注册服务地址，去探测地址
                TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
            }
        } else {
            //有注册服务地址，去认证
            int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getParam(Params.REGIST_IP, ""), TerminalFactory.getSDK().getParam(Params.REGIST_PORT, ""));
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg(getString(R.string.authing));
            } else {
                //状态机没有转到正在认证，说明已经在状态机中了，不用处理
            }
        }
    }

    public static final int REQUEST_PERMISSION_SETTING = 1235;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            // 创建个呼直播服务
//            MyApplication.instance.startHandlerService();
            start();
        } else if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            judgePermission();
        } else if (requestCode == OPEN_NET_CODE) {
            checkIfAuthorize();
        }
    }

    /**
     * 获取可用的IP列表
     * 回调在 子线程
     **/
    private ReceiveReturnAvailableIPHandler receiveReturnAvailableIPHandler = new ReceiveReturnAvailableIPHandler() {
        @Override
        public void handler(final Map<String, LoginModel> availableIP) {
            logger.info("1.RegisterActivity---收到可用IP列表" + ",主线程" + SystemUtils.isMainThread());
            availableIPlist.clear();
            String tempName = xcDropDownListView.getText();
            if (availableIP.size() > 0) {
                availableIPMap = availableIP;
                availableIPlist.add(getString(R.string.text_selection_unit));
                availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
                availableIPlist.add(company);
                xcDropDownListView.setItemsData(availableIPlist);
            } else {
                availableIPlist.add(getString(R.string.text_selection_unit));
                availableIPlist.add(company);
                xcDropDownListView.setItemsData(availableIPlist);
            }
            if (!TextUtils.isEmpty(tempName)) {
                xcDropDownListView.setText(tempName);
            }
            hideProgressDialog();
        }
    };


    /**
     * 认证回调
     * 回调在 子线程
     */

    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final boolean isRegisted) {

            logger.info("2.RegisterActivity---认证回调---resultCode：" + resultCode + "；   resultDesc：" + resultDesc + "；   isRegisted：" + isRegisted + ",主线程" + SystemUtils.isMainThread());
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                //认证成功，去连接接入服务
                changeProgressMsg(getResources().getString(R.string.connecting_server));
            } else if (resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()) {
                AlertDialog alerDialog = new AlertDialog.Builder(RegisterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_temporarily_unavailable_permissions)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                alerDialog.show();
            } else if (resultCode == TerminalErrorCode.DEPT_EXPIRED.getErrorCode()) {
                AlertDialog alerDialog = new AlertDialog.Builder(RegisterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_departmental_delegation_expires)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                alerDialog.show();
            } else if (resultCode == TerminalErrorCode.TERMINAL_TYPE_ERROR.getErrorCode()) {
                AlertDialog alerDialog = new AlertDialog.Builder(RegisterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_terminal_type_error)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                alerDialog.show();
            } else if (resultCode == TerminalErrorCode.TERMINAL_REPEAT.getErrorCode()) {
                AlertDialog alerDialog = new AlertDialog.Builder(RegisterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_terminal_repeat)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                alerDialog.show();
            } else if (resultCode == TerminalErrorCode.EXCEPTION.getErrorCode()) {
                if (reAuthCount < 3) {
                    reAuthCount++;
                    //发生异常的时候重试几次，因为网络原因经常导致一个io异常
                    TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getAuthManagerTwo().getTempIp(), TerminalFactory.getSDK().getAuthManagerTwo().getTempPort());
                } else {
                    changeProgressMsg(getResources().getString(R.string.auth_fail));
                    //myHandler.postDelayed(() -> exit(), 3000);

                }
            } else if (resultCode == TerminalErrorCode.TERMINAL_FAIL.getErrorCode()) {
                changeProgressMsg(getResources().getString(R.string.auth_fail));
                myHandler.postDelayed(() -> {
                    hideProgressDialog();
                }, 2000);
//                hideProgressDialog();
            } else {
                //没有注册服务地址，去探测地址
                if (availableIPlist.isEmpty()) {
                    TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                }
                if (!isRegisted) {
                    ToastUtils.showShort(R.string.please_regist_account);
                    hideProgressDialog();
                } else {
                    changeProgressMsg(resultDesc);
                    hideProgressDialog();
                }
            }
        }
    };


    /**
     * 连接接入服务器回调
     * 回调在 子线程
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
        @Override
        public void handler(boolean connected) {
            logger.info("3.RegisterActivity---收到信令是否连接的通知 ReceiveServerConnectionEstablishedHandler" + connected + ",主线程" + SystemUtils.isMainThread());
            if (connected) {
                changeProgressMsg(getResources().getString(R.string.logining));
            }
        }
    };

    /**
     * 注册完成的消息
     * 回调在 子线程
     */
    private ReceiveRegistCompleteHandler receiveRegistCompleteHandler = new ReceiveRegistCompleteHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            logger.info("4.RegisterActivity---注册完成的消息 ReceiveRegistCompleteHandler" + "errorCode:" + errorCode + "errorDesc:" + errorDesc + ",主线程" + SystemUtils.isMainThread());
            if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg(getResources().getString(R.string.connecting_server));
            } else {
                if (errorCode == TerminalErrorCode.REGISTER_PARAMETER_ERROR.getErrorCode()) {
                    changeProgressMsg(getString(R.string.text_invitation_code_wrong_please_regist_again));
                } else if (errorCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()) {
                    changeProgressMsg(errorCode + getString(R.string.text_regist_fail_please_check_all_info_is_correct));
                } else {
                    ToastUtil.showToast(RegisterActivity.this, errorDesc);
                }
                myHandler.postDelayed(() -> hideProgressDialog(), 3000);
            }
        }
    };

    /**
     * 登陆响应的消息
     * 回调在 子线程
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> {
        logger.info("5.RegisterActivity---收到登录的消息---resultCode:" + resultCode + "     resultDesc:" + resultDesc + ",主线程" + SystemUtils.isMainThread());
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            changeProgressMsg(getResources().getString(R.string.updating_data));
        } else {
            myHandler.post(() -> {
                hideProgressDialog();
                AlertDialog alerDialog = new AlertDialog.Builder(RegisterActivity.this).setTitle(R.string.text_prompt).setMessage(resultDesc).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                alerDialog.show();
            });
        }
    };

    /**
     * 更新所有数据信息的消息
     * 回调在 MainThread
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        logger.info("6.RegisterActivity---更新所有数据信息的消息---errorCode:" + errorCode + "     errorDesc:" + errorDesc + ",主线程" + SystemUtils.isMainThread());
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {
            goOn();
        }
    });

    private void goOn() {
        changeProgressMsg(getString(R.string.text_start_success));
        myHandler.removeCallbacksAndMessages(null);
        myHandler.postDelayed(() -> hideProgressDialog(), 3000);

        MainMapActivity.startActivity(this);
        finish();
//        try{
//            Class clazz;
//            String type = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
//            if(type.equals(TerminalMemberType.TERMINAL_UAV.toString())){
//                clazz = Class.forName("cn.vsx.uav.activity.UavMainActivity");
//            }else {
//                clazz = Class.forName("cn.vsx.vc.activity.NewMainActivity");
//            }
//            Intent intent = new Intent(this,clazz);
//            intent.putExtra(UrlParams.THIRD_APP_PACKAGE_NAME,third_app_package_name);
//            intent.putExtra(UrlParams.IS_REGIST_ACTIVITY_JOIN,true);
//            startActivity(intent);
//        }catch(ClassNotFoundException e){
//            e.printStackTrace();
//        }
//        finish();
    }

    /**
     * 退出消息
     */
    private ReceiveExitHandler receiveExitHandler = new ReceiveExitHandler() {
        @Override
        public void handle(String msg, boolean isExit) {
            //exit();
        }
    };
}
