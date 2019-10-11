package com.vsxin.terminalpad.mvp.ui.activity;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpActivity;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.constant.AppStatusConstants;
import com.vsxin.terminalpad.mvp.contract.presenter.RegisterPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IRegisterView;
import com.vsxin.terminalpad.mvp.ui.widget.ProgressDialog;
import com.vsxin.terminalpad.mvp.ui.widget.XCDropDownListView;
import com.vsxin.terminalpad.prompt.PromptManager;
import com.vsxin.terminalpad.utils.InputMethodUtil;
import com.vsxin.terminalpad.utils.NetworkUtil;
import com.vsxin.terminalpad.utils.SetToListUtil;
import com.vsxin.terminalpad.utils.SystemUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import cn.com.cybertech.pdk.UserInfo;
import cn.com.cybertech.pdk.api.IPstoreHandler;
import cn.com.cybertech.pdk.api.PstoreAPIImpl;
import cn.com.cybertech.pdk.api.UserObject;
import cn.com.cybertech.pdk.auth.Oauth2AccessToken;
import cn.com.cybertech.pdk.auth.PstoreAuth;
import cn.com.cybertech.pdk.auth.PstoreAuthListener;
import cn.com.cybertech.pdk.auth.sso.SsoHandler;
import cn.com.cybertech.pdk.exception.PstoreAuthException;
import cn.com.cybertech.pdk.exception.PstoreException;
import cn.com.cybertech.pdk.exception.PstoreUserException;
import cn.com.cybertech.pdk.utils.GsonUtils;
import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.UrlParams;
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
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.JudgeWhetherConnect;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ApkUtil;
import ptt.terminalsdk.tools.DeleteData;
import ptt.terminalsdk.tools.DialogUtil;
import ptt.terminalsdk.tools.ToastUtil;
import sec.vpn.ISecVpnService;

/**
 * @author qzw
 * <p>
 * 注册登录
 */
public class RegisterActivity extends MvpActivity<IRegisterView, RegisterPresenter> implements IRegisterView {

    public Logger logger = Logger.getLogger(getClass());
    private int reAuthCount = 0;
    private Handler myHandler = new Handler(Looper.getMainLooper());

    @BindView(R.id.ll_regist)//全布局
            LinearLayout ll_regist;

    @BindView(R.id.xcd_available_ip)//选择环境
            XCDropDownListView xcDropDownListView;

    @BindView(R.id.btn_confirm)
    Button btn_confirm;//注册

    @BindView(R.id.et_invitation_code)
    EditText et_invitation_code;//邀请码

    @BindView(R.id.et_user_name)
    EditText et_user_name;//姓名

    @BindView(R.id.btn_addMember)
    Button btnAddMember;//模拟警员
    @BindView(R.id.btn_idcard_login)
    Button btn_idcard_login;//模拟身份证登录


    @BindView(R.id.ll_reauth_info)
    LinearLayout llreAuthInfo;//模拟警员
    @BindView(R.id.account)
    EditText account;//
    @BindView(R.id.name)
    EditText edtName;//
    @BindView(R.id.departmentId)
    EditText departmentId;//
    @BindView(R.id.departmentName)
    EditText departmentName;//

    @BindView(R.id.ll_idcard_info)
    LinearLayout ll_idcard_info;//模拟身份证号登陆
    @BindView(R.id.et_idcard)
    EditText et_idcard;//
    @BindView(R.id.et_name)
    EditText et_name;//

    private String apkType;
    // 安全VPN服务名称
    private static final String SEC_VPN_SERVICE_ACTION_NAME = "sec.vpn.service";

    //添加单位的PopupWindow
    private PopupWindow popupWindow;
    //添加单位的布局根view
    //添加单位的ViewHolder
    private ViewHolder viewHolder;
    private boolean isCheckSuccess;//联通校验是否通过
    private boolean isCheckFinished;//联通校验是否完成

    public String selectIp;
    public String selectPort;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_register;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        //将app设置为登录状态
        PadApplication.setmAppStatus(AppStatusConstants.LOGINED);
        //市局包隐藏模拟警员
        if (AuthManagerTwo.POLICESTORE.equals(apkType) || AuthManagerTwo.XIANGYANGPOLICESTORE.equals(apkType)
                || AuthManagerTwo.XIANGYANG.equals(apkType)) {
            btnAddMember.setVisibility(View.GONE);
            btn_idcard_login.setVisibility(View.GONE);
        }

        initPopupWindow();
        initDialog();
        isCheckSuccess = false;
        xcDropDownListView.setOnXCDropDownListViewClickListeren(position -> {
            if (availableIPlist.size() == 2) {
                if (position != 0) {
                    popupWindow.showAsDropDown(xcDropDownListView);
                    viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
                    viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
                }
            }
            if (availableIPlist.size() > 2) {
                if (position == (availableIPlist.size() - 1)) {
                    popupWindow.showAsDropDown(xcDropDownListView);
                    viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
                    viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
                } else if (position != 0) {
                    String name = availableIPlist.get(position);
                    availableIPlist.remove(name);
                    availableIPlist.remove(getString(R.string.text_selection_unit));
                    availableIPlist.add(0, name);
                    xcDropDownListView.setItemsData(availableIPlist);
                    selectIp = availableIPMap.get(name).getIp();
                    selectPort = availableIPMap.get(name).getPort();
                    //认证
                    int resultCode = MyTerminalFactory.getSDK().getAuthManagerTwo().startAuth(selectIp, selectPort);
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        changeProgressMsg(getString(R.string.authing));
                    }
                }
            }
        });
        xcDropDownListView.setOnShowPopWindowListener(() -> InputMethodUtil.hideInputMethod(RegisterActivity.this,et_invitation_code));
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(RegisterActivity.this);
            myProgressDialog.setCancelable(false);
        }

        btn_confirm.setOnClickListener(v -> registerUser());
        apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
        //襄阳包就启动安全VPN服务
        if (apkType.equals(AuthManagerTwo.XIANGYANGPOLICESTORE) || apkType.equals(AuthManagerTwo.XIANGYANG)) {
            startVPNService();
        }
        ll_regist.setVisibility(View.GONE);
        judgePermission();
    }

    /**
     * 注册用户
     */
    private void registerUser() {
        InputMethodUtil.hideInputMethod(RegisterActivity.this,et_invitation_code);
        String itemsData = xcDropDownListView.getItemsData();

        if (TextUtils.equals(getString(R.string.text_add_unit), itemsData)) {
            ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_please_select_unit));
            return;
        }
        if (llreAuthInfo.getVisibility() == View.VISIBLE) {
            //模拟警员
            String useAccount = account.getText().toString().trim();
            String useDepartmentId = departmentId.getText().toString().trim();
            String useDepartmentName = departmentName.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            if (!checkName(name)) {
                return;
            }
            MyTerminalFactory.getSDK().putParam(UrlParams.ACCOUNT, useAccount);
            MyTerminalFactory.getSDK().putParam(UrlParams.NAME, name);
            MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_ID, useDepartmentId);
            MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_NAME, useDepartmentName);
            int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(selectIp, selectPort);
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg(getString(R.string.authing));
            }
        } else if (ll_idcard_info.getVisibility() == View.VISIBLE) {
            //身份证号登陆
            String userID = et_idcard.getText().toString().trim();
            String userName = et_name.getText().toString().trim();
            MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, userID);
            MyTerminalFactory.getSDK().putParam(UrlParams.NAME, userName);
            MyTerminalFactory.getSDK().putParam(UrlParams.XIANGYANG_STORE, true);
            int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(selectIp, selectPort);
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg(getString(R.string.authing));
            }
        } else {
            //注册
            String useOrg = et_invitation_code.getText().toString().trim();
            String useName = et_user_name.getText().toString().trim();

            if (TextUtils.isEmpty(useOrg)) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_please_input_invitation_code));
                return;
            } else if (!DataUtil.isLegalOrg(useOrg) || useOrg.length() != 6) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_please_input_invitation_code_by_six_number));
                return;
            } else if (TextUtils.isEmpty(useName)) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_please_input_name));
                return;
            } else if (!DataUtil.isLegalName(useName) || useName.length() > 12 || useName.length() < 2) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_please_input_correct_name));
                return;
            }
            String registIP = TerminalFactory.getSDK().getAuthManagerTwo().getTempIp();
            String registPort = TerminalFactory.getSDK().getAuthManagerTwo().getTempPort();
            if (TextUtils.isEmpty(registIP) || TextUtils.isEmpty(registPort)) {
                ToastUtil.showToast(R.string.text_please_select_unit);
            } else {
                changeProgressMsg(getString(R.string.text_registing));

                TerminalFactory.getSDK().getAuthManagerTwo().regist(useName, useOrg);
            }
        }
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

        //注册添加单位的监听
        if (viewHolder == null) {
            return;
        }
        viewHolder.userIP.setOnFocusChangeListener(onFocusChangeListener);
        viewHolder.userUnit.setOnFocusChangeListener(onFocusChangeListener);
        viewHolder.userPort.setOnFocusChangeListener(onFocusChangeListener);
        viewHolder.ll_regist_return.setOnClickListener(new ImportIPPortReturn());
        viewHolder.rl_regist_connect_efficacy.setOnClickListener(new EfficacyOnClickListener());
        viewHolder.userUnit.addTextChangedListener(new UnitClickListener());
        viewHolder.userPort.addTextChangedListener(new IpClickListener());
        viewHolder.userIP.addTextChangedListener(new IpClickListener());
        viewHolder.btnCustomIpOk.setOnClickListener(new BtnCustomIpOkOnClickListener());

        btnAddMember.setOnClickListener(new OnSwitchingModeClickListener());
        btn_idcard_login.setOnClickListener(new IdcardLoginClickListener());
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
            //襄阳包就启动安全VPN服务
            if (apkType.equals(AuthManagerTwo.XIANGYANGPOLICESTORE) || apkType.equals(AuthManagerTwo.XIANGYANG)) {
                startVPNService();
            } else {
                // todo authorize();//认证并获取user信息
                authorize();//认证并获取user信息
                requestDrawOverLays();
            }
        } else {
            if (netWorkDialog != null && !netWorkDialog.isShowing()) {
                netWorkDialog.show();
            }
        }
    }

    private void startVPNService() {
        try {
            logger.info("开始绑定VPN");
            // 绑定安全VPN服务
            Intent intent = new Intent();
            intent.setAction(SEC_VPN_SERVICE_ACTION_NAME);
            //试一下sec.vpn这个包名
            Intent intent1 = new Intent(createExplicitFromImplicitIntent(this, intent));
            startService(intent1);
            bindService(intent1, secVpnServiceConnection, BIND_AUTO_CREATE);
        } catch (Exception e) {
            logger.error("绑定安全VPN服务失败：" + e);
            e.printStackTrace();
        }
    }

    private ISecVpnService secVpnService;
    private ServiceConnection secVpnServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            secVpnService = ISecVpnService.Stub.asInterface(service);
            try {
                //获取身份证号
                String userID = secVpnService.getTfInfo().getUserID();
                String userName = secVpnService.getTfInfo().getUserName();
                logger.info("获取到身份证号为：" + userID + "------姓名：" + userName);
                MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, userID);
                MyTerminalFactory.getSDK().putParam(UrlParams.NAME, userName);
                MyTerminalFactory.getSDK().putParam(UrlParams.XIANGYANG_STORE, true);
                requestDrawOverLays();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            secVpnService = null;
        }
    };

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
        String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        logger.info("apkType:" + apkType + "----deviceType:" + deviceType + "----versionName:" + PadApplication.getPadApplication().getVersionName());
        //vpn没有启动时先启动VPN
        try {
            if (secVpnService == null) {
                logger.info("vpn没有绑定成功");
            }
            if (secVpnService != null && !secVpnService.sv_isStarted()) {
                secVpnService.sv_setServerAddr("20.50.0.11", "10009");
                secVpnService.sv_start();
                logger.info("vpn没有启动，开始启动vpn");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

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

    private void permissionDenied(final String permissionName) {
        new DialogUtil() {
            @Override
            public CharSequence getMessage() {
//                            return "4GPTT需要访问您设备上的照片、媒体内容和文件，否则无法工作；去打开权限?";
                return CheckMyPermission.getDesForPermission(permissionName, ApkUtil.getAppName(RegisterActivity.this));
            }

            @Override
            public Context getContext() {
                return RegisterActivity.this;
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
                RegisterActivity.this.finish();
            }
        }.showDialog();
    }

    // 替换为应用在PSTORE中注册时生成的值
    protected static final String CLIENT_ID = "40B2984FC648ECA7F4CEE84C0F234F80";//"B8994F7212536DEBB21D8BE1FDE75F22"

    private void authorize() {
        Map<String, String> userInfo = UserInfo.getUserInfo(RegisterActivity.this);

        logger.info("请求userInfo：" + userInfo);

        if (userInfo != null) {
            if (!(userInfo.get("account") + "").equals(MyTerminalFactory.getSDK().getParam(UrlParams.ACCOUNT, ""))) {
                logger.error("获取到的警号变了，删除所有数据！！！！");
                DeleteData.deleteAllData();
            }
            TerminalFactory.getSDK().putParam(Params.POLICE_STORE_APK, true);
            PadApplication.getPadApplication().setApkType();
            PadApplication.getPadApplication().setAppKey();
            PadApplication.getPadApplication().setTerminalMemberType();
            MyTerminalFactory.getSDK().putParam(UrlParams.ACCOUNT, userInfo.get("account") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.NAME, userInfo.get("name") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.PHONE, userInfo.get("phone") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_ID, userInfo.get("dept_id") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_NAME, userInfo.get("dept_name") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, userInfo.get("idcard") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.SEX, userInfo.get("sex") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.EMAIL, userInfo.get("email") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.AVATAR_URL, userInfo.get("avatar_url") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.COMPANY, userInfo.get("company") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.POSITION, userInfo.get("position") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.ROLE_CODE, userInfo.get("role_code") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.ROLE_NAME, userInfo.get("role_name") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.PRIVILEGE_CODE, userInfo.get("privilege_code") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.PRIVILEGE_NAME, userInfo.get("privilege_name") + "");
            MyTerminalFactory.getSDK().putParam(UrlParams.EXTRA_1, userInfo.get("extra_1") + "");

        } else {
            TerminalFactory.getSDK().putParam(Params.POLICE_STORE_APK, false);
            PadApplication.getPadApplication().setTerminalMemberType();
            String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            if (AuthManagerTwo.POLICESTORE.equals(apkType) || AuthManagerTwo.POLICETEST.equals(apkType) || AuthManagerTwo.XIANGYANGPOLICESTORE.equals(apkType)) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_please_open_wuhan_police_work_first));
            }

        }
    }

    private void reauthorize() {
        PstoreAuth auth = new PstoreAuth(this, CLIENT_ID);
        SsoHandler mSsoHandler = new SsoHandler(this, auth);
        mSsoHandler.authorizeRefresh(new AuthListener());
    }

    private PstoreAPIImpl pstoreAPI = new PstoreAPIImpl();

    /**
     * onComplete回掉后执行：
     * 1. 使用access_token去APP SERVER登录;
     * 2. 若未绑定账号，则先绑定账号；
     * 3. 若有其他问题，则解决；
     * 4. 进入APP。
     * 具体流程见文档（android-pstore-sdk-v2.xx）2.1流程图。
     */
    class AuthListener implements PstoreAuthListener {

        @Override
        public void onComplete(Oauth2AccessToken accessToken) {
            logger.info("onComplete-------------->" + accessToken.toBundle());
            pstoreAPI.requestUserInfo(RegisterActivity.this, response, CLIENT_ID, accessToken.getToken());
        }

        @Override
        public void onPstoreException(PstoreException pstoreException) {
            if (pstoreException instanceof PstoreAuthException) {
                PstoreAuthException e = (PstoreAuthException) pstoreException;
                switch (e.getErrorCode()) {
                    // client_id 为空
                    case PstoreAuthException.ERROR_CLIENTID_NULL:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_client_id_is_empty));
                        break;
                    // client_id 非法
                    case PstoreAuthException.ERROR_CLIENTID_ILLEGAL:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_client_id_is_illegal));
                        break;
                    // 授权码非法
                    case PstoreAuthException.ERROR_GRANT_CODE_ILLEGAL:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_authorization_code_illegal));
                        break;
                    default:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_unknown_error));
                        break;
                }
                logger.error("AuthPstoreAuthException：" + pstoreException.getMessage());
            } else {
                // Unknown exception. pstoreException.getMessage()
                logger.error("Auth未知错误：" + pstoreException.getMessage());
            }
            finishActivity();
        }

        @Override
        public void onCancel() {
        }
    }

    private IPstoreHandler.Response response = new IPstoreHandler.Response() {
        @Override
        public void onResponse(Bundle bundle) {
            Map<String, String> userInfo = UserInfo.getUserInfo(RegisterActivity.this);
            logger.info("请求userInfo：" + userInfo);

            String userJson = GsonUtils.toJson(UserInfo.getUser(RegisterActivity.this));
            logger.info("请求userJson：" + userJson);

            logger.error("请求user0：" + bundle.toString());
            UserObject user = UserObject.fromBundle(bundle);
            logger.info("请求user1：" + user);
            PadApplication.getPadApplication().setTerminalMemberType();
            MyTerminalFactory.getSDK().putParam(UrlParams.ACCOUNT, user.getAccount());
            MyTerminalFactory.getSDK().putParam(UrlParams.NAME, user.getName());
            MyTerminalFactory.getSDK().putParam(UrlParams.PHONE, user.getPhone());
            MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_ID, user.getDeptId());
            MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_NAME, user.getDeptName());
            MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.SEX, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.EMAIL, "");
//            MyTerminalFactory.getSDK().putParam(UrlParams.AVATAR_URL, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.COMPANY, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.POSITION, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.ROLE_CODE, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.ROLE_NAME, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.PRIVILEGE_CODE, "");
            MyTerminalFactory.getSDK().putParam(UrlParams.PRIVILEGE_NAME, "");
        }

        @Override
        public void onPstoreException(PstoreException e) {
            if (e instanceof PstoreUserException) {
                PstoreUserException e1 = (PstoreUserException) e;
                logger.error("请求user的错误码：" + e1.getErrorCode());

                switch (e1.getErrorCode()) {
                    // access_token过期
                    case PstoreUserException.ERROR_ACCESS_TOKEN_EXPIRED:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_access_token_expire));
                        reauthorize();
                        break;
                    // 资源未授权
                    case PstoreUserException.ERROR_RES_UNAUTHORIZED:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_unauthorized_resources));
                        break;
                    // 未知错误
                    case PstoreUserException.ERROR_UNKONWN:
                        ToastUtil.showToast(PadApplication.getPadApplication().getApplicationContext(), getString(R.string.text_unknown_error));
                        break;
                    default:
                        break;
                }
            } else {
                // Unknown exception. pstoreException.getMessage()
                logger.error("请求user未知错误：" + e.getMessage());
            }
            finishActivity();
        }
    };

    private void finishActivity() {
        myHandler.postDelayed(() -> finish(), 2000);
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param context
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
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
            myHandler.post(() -> {
                if (availableIP.size() > 0) {
                    availableIPMap = availableIP;
                    availableIPlist.add(getString(R.string.text_selection_unit));
                    availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
                    availableIPlist.add(getString(R.string.text_add_unit));
                    xcDropDownListView.setItemsData(availableIPlist);
                } else {
                    availableIPlist.add(getString(R.string.text_selection_unit));
                    availableIPlist.add(getString(R.string.text_add_unit));
                    xcDropDownListView.setItemsData(availableIPlist);
                }
                if (!TextUtils.isEmpty(tempName)) {
                    xcDropDownListView.setText(tempName);
                }
                ll_regist.setVisibility(View.VISIBLE);
                hideProgressDialog();
            });
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
            myHandler.post(() -> {
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
                        myHandler.postDelayed(() -> {
                            ll_regist.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }, 3000);
                    }
                } else if (resultCode == TerminalErrorCode.TERMINAL_FAIL.getErrorCode()) {
                    //没有注册服务地址，去探测地址
                    if (availableIPlist.isEmpty()) {
                        TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                    }
                    changeProgressMsg(getResources().getString(R.string.auth_fail));
                    myHandler.postDelayed(() -> {
                        ll_regist.setVisibility(View.VISIBLE);
                        hideProgressDialog();
                    }, 2000);
                } else {
                    //没有注册服务地址，去探测地址
                    if (availableIPlist.isEmpty()) {
                        TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                    }
                    if (!isRegisted) {
                        ToastUtil.showToast(R.string.please_regist_account);
                        ll_regist.setVisibility(View.VISIBLE);
                        hideProgressDialog();
                    } else {
                        changeProgressMsg(resultDesc);
                        myHandler.postDelayed(() -> {
                            ll_regist.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }, 2000);
                    }
                }
            });
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
//        myHandler.postDelayed(() -> hideProgressDialog(), 3000);
        myHandler.postDelayed(() -> {
            hideProgressDialog();
            MainMapActivity.startActivity(this);
            finish();
        }, 3000);
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


    //模拟警员
    private class OnSwitchingModeClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (llreAuthInfo.getVisibility() == View.GONE && et_user_name.getVisibility() == View.VISIBLE && et_invitation_code.getVisibility() == View.VISIBLE) {
                et_invitation_code.setVisibility(View.GONE);
                et_user_name.setVisibility(View.GONE);
                btn_confirm.setVisibility(View.VISIBLE);
                llreAuthInfo.setVisibility(View.VISIBLE);
                btnAddMember.setText(R.string.text_invitation_code_regist);
            } else {
                et_invitation_code.setVisibility(View.VISIBLE);
                et_user_name.setVisibility(View.VISIBLE);
                btn_confirm.setVisibility(View.VISIBLE);
                llreAuthInfo.setVisibility(View.GONE);
                btnAddMember.setText(R.string.text_simulated_police_officer);
            }
            ll_idcard_info.setVisibility(View.GONE);
        }
    }

    private void initPopupWindow() {
        View popupWindowView = View.inflate(RegisterActivity.this, R.layout.regist_import_ip, null);
        viewHolder = new ViewHolder(popupWindowView);
        popupWindow = setPopupwindow(popupWindowView);
    }

    private PopupWindow setPopupwindow(View view) {
        PopupWindow mPopWindow = new PopupWindow(view);
        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);

        //是否响应touch事件
        mPopWindow.setTouchable(true);
        //是否具有获取焦点的能力
        mPopWindow.setFocusable(true);

        //外部是否可以点击
        mPopWindow.setOutsideTouchable(false);

        return mPopWindow;
    }

    static class ViewHolder {
        EditText userUnit;
        EditText userIP;
        EditText userPort;
        Button btnCustomIpOk;
        LinearLayout ll_regist_return;
        RelativeLayout rl_regist_connect_efficacy;
        TextView tv_regist_connect_efficacy;
        ImageView iv_regist_connect_efficacy_ok;

        ViewHolder(View view) {
            userUnit = (EditText) view.findViewById(R.id.userUnit);
            userIP = (EditText) view.findViewById(R.id.userIP);
            userPort = (EditText) view.findViewById(R.id.userPort);
            btnCustomIpOk = (Button) view.findViewById(R.id.btn_custom_ip_ok);
            ll_regist_return = (LinearLayout) view.findViewById(R.id.ll_regist_return);
            rl_regist_connect_efficacy = (RelativeLayout) view.findViewById(R.id.rl_regist_connect_efficacy);
            tv_regist_connect_efficacy = (TextView) view.findViewById(R.id.tv_regist_connect_efficacy);
            iv_regist_connect_efficacy_ok = (ImageView) view.findViewById(R.id.iv_regist_connect_efficacy_ok);
        }
    }

    /**
     * 邀请码用户名输入框焦点
     */
    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                if (v.getId() == R.id.userUnit) {
                    ((TextView) v).setHint(getString(R.string.text_add_unit_hint_name));
                }
                if (v.getId() == R.id.userIP) {
                    ((TextView) v).setHint(getString(R.string.text_add_unit_hint_ip));
                }
                if (v.getId() == R.id.userPort) {
                    ((TextView) v).setHint(getString(R.string.text_add_unit_hint_port));
                }
            } else {
                ((TextView) v).setHint("");
            }
        }
    };

    /**
     * 输入ip和端口界面的返回按钮
     **/
    private final class ImportIPPortReturn implements OnClickListener {

        @Override
        public void onClick(View v) {
            myHandler.post(() -> popupWindow.dismiss());
        }
    }

    private final class EfficacyOnClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(viewHolder.userUnit.getText())) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_add_unit_hint_name));
            } else {
                String text = viewHolder.userUnit.getText().toString().trim();
                if (!DataUtil.isLegalName(text) || text.length() < 2) {
                    ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_unit_name_is_not_correct));
                    return;
                }

                if (TextUtils.isEmpty(viewHolder.userIP.getText())) {
                    ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_add_unit_hint_ip));
                } else {
                    if (TextUtils.isEmpty(viewHolder.userPort.getText())) {
                        ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_add_unit_hint_port));
                    } else {
                        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                            isCheckFinished = false;
                            if (JudgeWhetherConnect.isHostConnectable(viewHolder.userIP.getText().toString(), viewHolder.userPort.getText().toString())) {
                                isCheckFinished = true;
                                myHandler.post(() -> {
                                    viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.green));
                                    viewHolder.tv_regist_connect_efficacy.setVisibility(View.GONE);
                                    viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.VISIBLE);
                                    isCheckSuccess = true;
                                });

                            } else {
                                isCheckFinished = true;
                                myHandler.post(() -> ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_input_ip_and_port_is_not_used)));
                            }
                        });
                    }
                }
            }
        }
    }

    private final class UnitClickListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
            viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);

            if (s.length() > 0) {
                if (DataUtil.isLegalOrg(s)) {
                    ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_unit_name_is_not_correct_one));
                }
                if (!DataUtil.isLegalSearch(s)) {
                    ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_unit_name_is_not_correct_two));
                }
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private final class IpClickListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
            viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * 输入自定义IP和端口的确定按钮的监听
     **/
    private final class BtnCustomIpOkOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            //原连通校验逻辑
            if (TextUtils.isEmpty(viewHolder.userUnit.getText())) {
                ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_add_unit_hint_name));
            } else {
                String text = viewHolder.userUnit.getText().toString().trim();
                if (!DataUtil.isLegalName(text) || text.length() < 2) {
                    ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_unit_name_is_not_correct));
                    return;
                }

                if (TextUtils.isEmpty(viewHolder.userIP.getText())) {
                    ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_add_unit_hint_ip));
                } else {
                    if (TextUtils.isEmpty(viewHolder.userPort.getText())) {
                        ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_add_unit_hint_port));
                    } else {
                        if (!isCheckFinished) {
                            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                                isCheckFinished = false;
                                if (JudgeWhetherConnect.isHostConnectable(viewHolder.userIP.getText().toString(), viewHolder.userPort.getText().toString())) {
                                    isCheckFinished = true;
                                    myHandler.post(() -> {
                                        viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.green));
                                        viewHolder.tv_regist_connect_efficacy.setVisibility(View.GONE);
                                        viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.VISIBLE);
                                        //                                            viewHolder.btnCustomIpOk.setBackgroundColor(getResources().getColor(R.color.ok_blue));
                                        isCheckSuccess = true;
                                        doAuth();
                                    });

                                } else {
                                    isCheckFinished = true;
                                    myHandler.post(() -> {
                                        isCheckFinished = false;
                                        ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_connect_fail_please_input_corrent_ip_and_port));
                                    });
                                }
                            });
                        }
                    }
                }
            }
//            doAuth();
        }
    }

    private void doAuth() {
        //校验完成并且通过才去登录认证
        if (isCheckFinished && isCheckSuccess) {
            availableIPlist.clear();
            LoginModel loginModel = new LoginModel(viewHolder.userUnit.getText().toString(),
                    viewHolder.userIP.getText().toString(), viewHolder.userPort.getText().toString());
            availableIPMap.put(viewHolder.userUnit.getText().toString(), loginModel);
            availableIPlist.addAll(SetToListUtil.setToArrayList(availableIPMap));
            availableIPlist.remove(viewHolder.userUnit.getText().toString());
            availableIPlist.add(0, viewHolder.userUnit.getText().toString());
            availableIPlist.add(getString(R.string.text_add_unit));
            xcDropDownListView.setItemsData(availableIPlist);
            reAuthCount = 0;
            int code = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(viewHolder.userIP.getText().toString(), viewHolder.userPort.getText().toString());
            if (code == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg(getString(R.string.authing));
            }
            popupWindow.dismiss();
            isCheckSuccess = false;
            isCheckFinished = false;
        }
    }

    private class IdcardLoginClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (ll_idcard_info.getVisibility() == View.GONE) {
                et_invitation_code.setVisibility(View.GONE);
                et_user_name.setVisibility(View.GONE);
                ll_idcard_info.setVisibility(View.VISIBLE);
                btn_idcard_login.setText(R.string.text_invitation_code_regist);
            } else {
                et_invitation_code.setVisibility(View.VISIBLE);
                et_user_name.setVisibility(View.VISIBLE);
                ll_idcard_info.setVisibility(View.GONE);
                btn_idcard_login.setText(getString(R.string.text_input_id_card));
            }
            llreAuthInfo.setVisibility(View.GONE);
        }
    }

    /**
     * 检测姓名
     *
     * @param name
     * @return
     */
    private boolean checkName(String name) {
        boolean result = false;
        if (!TextUtils.isEmpty(name) && name.length() >= 2 && name.length() <= 7) {
            String pas = "^[a-zA-Z\\u4e00-\\u9fa5][a-zA-Z0-9\\u4e00-\\u9fa5]*$";
            Pattern p = Pattern.compile(pas);
            Matcher m = p.matcher(name);
            if (m.matches()) {
                result = true;
            }
        }
        if (!result) {
            ToastUtil.showToast(RegisterActivity.this, getString(R.string.text_input_name_regex));
        }
        return result;
    }

    private void exit() {
        finish();
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult", "0");
        PadApplication.getPadApplication().isPttPress = false;
    }
}
