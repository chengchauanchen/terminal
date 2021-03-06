package cn.vsx.vc.activity;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.cybertech.pdk.UserInfo;
import cn.com.cybertech.pdk.api.IPstoreHandler.Response;
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
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginModel;
import cn.vsx.hamster.terminalsdk.model.IdentifyType;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExitHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetNameByOrgHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRegistCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReturnAvailableIPHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.JudgeWhetherConnect;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ProgressDialog;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.utils.ApkUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.KeyboarUtils;
import cn.vsx.vc.utils.NetworkUtil;
import cn.vsx.vc.utils.SelfStartupPermissionUtils;
import cn.vsx.vc.utils.SetToListUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.XCDropDownListView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DeleteData;
import ptt.terminalsdk.tools.DialogUtil;
import sec.vpn.ISecVpnService;

public class RegistGroupCallCenterActivity extends BaseActivity implements RecvCallBack, Actions {

    private AlertDialog netWorkDialog;

    EditText userOrg;

    EditText userName;

    LinearLayout llreAuthInfo;

    Button btnAddMember;

    EditText account;

    EditText edtName;

    EditText departmentId;

    EditText departmentName;

    Button btn_confirm;

    TextView tvVersionPrompt;

    XCDropDownListView xcd_available_ip;

    LinearLayout ll_regist;

    View view_pop;
    private int reAuthCount;
    private ProgressDialog myProgressDialog;
    private Handler myHandler = new Handler(Looper.getMainLooper());
    private String orgHint;
    private String nameHint;
    public Logger logger = Logger.getLogger(getClass());
    private ViewHolder viewHolder;
    private PopupWindow popupWindow;
    private boolean isCheckSuccess;//联通校验是否通过
    private boolean isCheckFinished;//联通校验是否完成
    // 安全VPN服务名称
    private static final String SEC_VPN_SERVICE_ACTION_NAME = "sec.vpn.service";

    private ISecVpnService secVpnService;
    private ServiceConnection secVpnServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            secVpnService = ISecVpnService.Stub.asInterface(service);
            try{
                //获取身份证号
                String userID = secVpnService.getTfInfo().getUserID();
                String userName = secVpnService.getTfInfo().getUserName();
                logger.info("获取到身份证号为："+userID+"------姓名："+userName);
                MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, userID);
                MyTerminalFactory.getSDK().putParam(UrlParams.NAME, userName);
                MyTerminalFactory.getSDK().putParam(UrlParams.XIANGYANG_STORE,true);
                requestDrawOverLays();
            }catch(RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            secVpnService = null;
        }
    };
    private String apkType;
    private Button btn_idcard_login;
    private LinearLayout ll_idcard_info;
    private EditText et_idcard;
    private EditText et_name;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // 把状态变为登陆 能使父类不会走 protectApp()
        MyApplication.instance.mAppStatus = Constants.LOGINED;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboarUtils.getKeyBoardHeight(this);
    }

    private String company = "添加单位";

    /**
     * ============================================================================handler=================================================================================
     **/


    //认证回调
    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final boolean isRegisted) {

            logger.info("receiveSendUuidResponseHandler------resultCode：" + resultCode + "；   resultDesc：" + resultDesc + "；   isRegisted：" + isRegisted);
            myHandler.post(() -> {
                if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                    //认证成功，去连接接入服务
                    changeProgressMsg(getResources().getString(R.string.connecting_server));
                } else if (resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()) {
                    AlertDialog alerDialog = new AlertDialog.Builder(RegistGroupCallCenterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_temporarily_unavailable_permissions)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                    alerDialog.show();
                } else if (resultCode == TerminalErrorCode.DEPT_EXPIRED.getErrorCode()) {
                    AlertDialog alerDialog = new AlertDialog.Builder(RegistGroupCallCenterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_departmental_delegation_expires)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                    alerDialog.show();
                } else if (resultCode == TerminalErrorCode.TERMINAL_TYPE_ERROR.getErrorCode()) {
                    AlertDialog alerDialog = new AlertDialog.Builder(RegistGroupCallCenterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_terminal_type_error)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                    alerDialog.show();
                } else if (resultCode == TerminalErrorCode.TERMINAL_REPEAT.getErrorCode()) {
                    AlertDialog alerDialog = new AlertDialog.Builder(RegistGroupCallCenterActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_terminal_repeat)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                    alerDialog.show();
                } else if (resultCode == TerminalErrorCode.EXCEPTION.getErrorCode()) {
                    if (reAuthCount < 3) {
                        reAuthCount++;
                        //发生异常的时候重试几次，因为网络原因经常导致一个io异常
                        TerminalFactory.getSDK().getAuthManagerTwo().startAuth(TerminalFactory.getSDK().getAuthManagerTwo().getTempIp(), TerminalFactory.getSDK().getAuthManagerTwo().getTempPort());
                    } else {
                        changeProgressMsg(getResources().getString(R.string.auth_fail));
                        myHandler.postDelayed(() -> exit(), 3000);
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
                        ToastUtils.showShort(R.string.please_regist_account);
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
     * 注册完成的消息
     */
    private ReceiveRegistCompleteHandler receiveRegistCompleteHandler = new ReceiveRegistCompleteHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            myHandler.post(() -> {
                if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                    changeProgressMsg(getResources().getString(R.string.connecting_server));
                } else {
                    if (errorCode == TerminalErrorCode.REGISTER_PARAMETER_ERROR.getErrorCode()) {
                        changeProgressMsg(getString(R.string.text_invitation_code_wrong_please_regist_again));
                    } else if (errorCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()) {
                        changeProgressMsg(errorCode + getString(R.string.text_regist_fail_please_check_all_info_is_correct));
                    } else {
                        ToastUtil.showToast(RegistGroupCallCenterActivity.this, errorDesc);
                    }
                    myHandler.postDelayed(() -> hideProgressDialog(), 3000);
                }
            });
        }
    };

    /**
     * 连接接入服务器回调
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
        @Override
        public void handler(boolean connected) {
            logger.info("RegistActivity收到信令是否连接的通知" + connected);
            if (connected) {
                changeProgressMsg(getResources().getString(R.string.logining));
            }
        }
    };

    /**
     * 登陆响应的消息
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = (resultCode, resultDesc) -> {
        logger.info("RegistActivity---收到登录的消息---resultCode:" + resultCode + "     resultDesc:" + resultDesc);
        myHandler.post(() -> {
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                changeProgressMsg(getResources().getString(R.string.updating_data));
            } else {
                myHandler.post(() -> {
                    hideProgressDialog();
                    AlertDialog alerDialog = new AlertDialog.Builder(RegistGroupCallCenterActivity.this).setTitle(R.string.text_prompt).setMessage(resultDesc).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                    alerDialog.show();
                });
            }
        });
    };

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = (errorCode, errorDesc) -> myHandler.post(() -> {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {
            goOn();
        }
    });

    private void exit() {
        finish();
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult", "0");
        SendRecvHelper.send(getApplicationContext(), stoppedCallIntent);

        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyApplication.instance.stopHandlerService();
    }

    private ReceiveExitHandler receiveExitHandler = new ReceiveExitHandler() {
        @Override
        public void handle(String msg, boolean isExit) {
            exit();
        }
    };


    ArrayList<String> availableIPlist = new ArrayList<>();
    Map<String, LoginModel> availableIPMap = new HashMap<>();
    /**
     * 获取可用的IP列表
     **/
    private ReceiveReturnAvailableIPHandler receiveReturnAvailableIPHandler = new ReceiveReturnAvailableIPHandler() {
        @Override
        public void handler(final Map<String, LoginModel> availableIP) {
            myHandler.post(() -> {
                logger.info("收到可用IP列表");
                availableIPlist.clear();
                String tempName = xcd_available_ip.getText();
                if (availableIP.size() > 0) {
                    availableIPMap = availableIP;
                    availableIPlist.add(getString(R.string.text_selection_unit));
                    availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
                    availableIPlist.add(company);
                    xcd_available_ip.setItemsData(availableIPlist);
                } else {
                    availableIPlist.add(getString(R.string.text_selection_unit));
                    availableIPlist.add(company);
                    xcd_available_ip.setItemsData(availableIPlist);
                }
                if (!TextUtils.isEmpty(tempName)) {
                    xcd_available_ip.setText(tempName);
                }
                ll_regist.setVisibility(View.VISIBLE);
                hideProgressDialog();
            });

        }
    };

    private ReceiveGetNameByOrgHandler receiveGetNameByOrgHandler = new ReceiveGetNameByOrgHandler() {
        @Override
        public void handler(final String returnMemberName, final int resultCoed, final String resultDesc) {
            myHandler.post(() -> {
                if (resultCoed == 0) {
                    userName.setText(returnMemberName);
                } else {
//                        ToastUtil.showToast(RegistActivity.this, resultDesc);
                }
            });
        }
    };


    /**
     * ============================================================================Listener=================================================================================
     **/

    private final class OnClickListenerImplementation implements
            OnClickListener {
        @Override
        public void onClick(View v) {
            String itemsData = xcd_available_ip.getItemsData();
            if (company.equals(itemsData)) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_please_select_unit));
                return;
            }
            if (llreAuthInfo.getVisibility() == View.VISIBLE) {
                //模拟警员
                String useAccount = account.getText().toString().trim();
                String useDepartmentId = departmentId.getText().toString().trim();
                String useDepartmentName = departmentName.getText().toString().trim();
                String name = edtName.getText().toString().trim();
                if(!checkName(name)){
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
            } else if(ll_idcard_info.getVisibility() == View.VISIBLE){
                //身份证号登陆
                String userID = et_idcard.getText().toString().trim();
                String userName = et_name.getText().toString().trim();
                MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, userID);
                MyTerminalFactory.getSDK().putParam(UrlParams.NAME, userName);
                MyTerminalFactory.getSDK().putParam(UrlParams.XIANGYANG_STORE,true);
                int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(selectIp, selectPort);
                if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                    changeProgressMsg(getString(R.string.authing));
                }
            }
            else {
                //注册
                String useOrg = userOrg.getText().toString().trim();
                String useName = userName.getText().toString().trim();

                if (TextUtils.isEmpty(useOrg)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_please_input_invitation_code));
                    return;
                } else if (!DataUtil.isLegalOrg(useOrg) || useOrg.length() != 6) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_please_input_invitation_code_by_six_number));
                    return;
                } else if (TextUtils.isEmpty(useName)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_please_input_name));
                    return;
                } else if (!DataUtil.isLegalName(useName) || useName.length() > 12 || useName.length() < 2) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_please_input_correct_name));
                    return;
                }
                String registIP = TerminalFactory.getSDK().getAuthManagerTwo().getTempIp();
                String registPort = TerminalFactory.getSDK().getAuthManagerTwo().getTempPort();
                if (TextUtils.isEmpty(registIP) || TextUtils.isEmpty(registPort)) {
                    ToastUtils.showShort(R.string.text_please_select_unit);
                } else {
                    changeProgressMsg(getString(R.string.text_registing));

                    TerminalFactory.getSDK().getAuthManagerTwo().regist(useName, useOrg);
                }
            }
        }
    }

    /**
     * 检测姓名
     * @param name
     * @return
     */
    private boolean checkName(String name) {
        boolean result = false;
        if (!TextUtils.isEmpty(name)&&name.length()>=2&&name.length()<=7) {
            String pas = "^[a-zA-Z\\u4e00-\\u9fa5][a-zA-Z0-9\\u4e00-\\u9fa5]*$";
            Pattern p = Pattern.compile(pas);
            Matcher m = p.matcher(name);
            if (m.matches()) {
                result = true;
            }
        }
        if(!result){
            ptt.terminalsdk.tools.ToastUtil.showToast(RegistGroupCallCenterActivity.this,getString(R.string.text_input_name_regex));
        }
        return result;
    }

    private class IdcardLoginClickListener implements OnClickListener{
        @Override
        public void onClick(View v){
            if(ll_idcard_info.getVisibility() == View.GONE){
                userOrg.setVisibility(View.GONE);
                userName.setVisibility(View.GONE);
                ll_idcard_info.setVisibility(View.VISIBLE);
                btn_idcard_login.setText(R.string.text_invitation_code_regist);
            }else {
                userOrg.setVisibility(View.VISIBLE);
                userName.setVisibility(View.VISIBLE);
                ll_idcard_info.setVisibility(View.GONE);
                btn_idcard_login.setText("模拟身份证号登陆");
            }
            llreAuthInfo.setVisibility(View.GONE);
        }
    }

    //模拟警员
    private class OnSwitchingModeClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (llreAuthInfo.getVisibility() == View.GONE && userName.getVisibility() == View.VISIBLE && userOrg.getVisibility() == View.VISIBLE) {
                userOrg.setVisibility(View.GONE);
                userName.setVisibility(View.GONE);
                btn_confirm.setVisibility(View.VISIBLE);
                llreAuthInfo.setVisibility(View.VISIBLE);
                btnAddMember.setText(R.string.text_invitation_code_regist);
            } else {
                userOrg.setVisibility(View.VISIBLE);
                userName.setVisibility(View.VISIBLE);
                btn_confirm.setVisibility(View.VISIBLE);
                llreAuthInfo.setVisibility(View.GONE);
                btnAddMember.setText(R.string.text_simulated_police_officer);
            }
            ll_idcard_info.setVisibility(View.GONE);
        }
    }


    /**
     * 邀请码用户名输入框焦点
     */
    private OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                if (v.getId() == R.id.userOrg) {
                    ((TextView) v).setHint(orgHint);
                }
                if (v.getId() == R.id.userName) {
                    ((TextView) v).setHint(nameHint);
                }
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

    //监听输入的验证码，去服务端请求名字
    private final class TextWatcherImpOrg implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!TextUtils.isEmpty(s)) {//邀请码不为空
                if (!DataUtil.isLegalOrg(s)) {//邀请码不合法
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_invitation_code_not_correct));
                } else {
                    if (s.length() == 6) {//长度是六的时候，请求名字
                        TerminalFactory.getSDK().getAuthManagerTwo().getNameByOrg(s + "");
                    }
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }


    //监听输入的名字
    private final class TextWatcherImpName implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    public String selectIp;
    public String selectPort;

    //控件XCDropDownListView的点击事件
    private final class XCDClickListener implements XCDropDownListView.XCDropDownListViewClickListeren {

        @Override
        public void onXCDropDownListViewClickListeren(final int position) {
            myHandler.post(() -> {
                if (availableIPlist.size() == 2) {
                    if (position != 0) {
                        popupWindow.showAsDropDown(view_pop);
                        viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
                        viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
                    }
                }
                if (availableIPlist.size() > 2) {
                    if (position == (availableIPlist.size() - 1)) {
                        popupWindow.showAsDropDown(view_pop);
                        viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
                        viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
                    } else if (position != 0) {
                        String name = availableIPlist.get(position);
                        availableIPlist.remove(name);
                        availableIPlist.remove(getString(R.string.text_selection_unit));
                        availableIPlist.add(0, name);
                        xcd_available_ip.setItemsData(availableIPlist);
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
        }
    }

    /**
     * 输入ip和端口界面的返回按钮
     **/
    private final class ImportIPPortReturn implements OnClickListener {

        @Override
        public void onClick(View v) {
            myHandler.post(() -> popupWindow.dismiss());
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
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_add_unit_hint_name));
            } else {
                String text = viewHolder.userUnit.getText().toString().trim();
                if (!DataUtil.isLegalName(text) || text.length() < 2) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unit_name_is_not_correct));
                    return;
                }

                if (TextUtils.isEmpty(viewHolder.userIP.getText())) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_add_unit_hint_ip));
                } else {
                    if (TextUtils.isEmpty(viewHolder.userPort.getText())) {
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_add_unit_hint_port));
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
                                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_connect_fail_please_input_corrent_ip_and_port));
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
            availableIPlist.add(company);
            xcd_available_ip.setItemsData(availableIPlist);
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

    private final class EfficacyOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(viewHolder.userUnit.getText())) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_add_unit_hint_name));
            } else {
                String text = viewHolder.userUnit.getText().toString().trim();
                if (!DataUtil.isLegalName(text) || text.length() < 2) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unit_name_is_not_correct));
                    return;
                }

                if (TextUtils.isEmpty(viewHolder.userIP.getText())) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_add_unit_hint_ip));
                } else {
                    if (TextUtils.isEmpty(viewHolder.userPort.getText())) {
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_add_unit_hint_port));
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
                                myHandler.post(() -> ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_input_ip_and_port_is_not_used)));
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
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unit_name_is_not_correct_one));
                }
                if (!DataUtil.isLegalSearch(s)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unit_name_is_not_correct_two));
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


    @Override
    public int getLayoutResId() {
        return R.layout.activity_regist_group_call_center;
    }

    @Override
    public void initView() {
        userOrg = findViewById(R.id.userOrg);
        userName = findViewById(R.id.userName);
        llreAuthInfo = findViewById(R.id.ll_reauth_info);
        btnAddMember = findViewById(R.id.btn_addMember);
        account = findViewById(R.id.account);
        edtName = findViewById(R.id.name);
        departmentId = findViewById(R.id.departmentId);
        departmentName = findViewById(R.id.departmentName);
        btn_confirm = findViewById(R.id.btn_confirm);

        tvVersionPrompt = findViewById(R.id.tv_version_prompt);
        xcd_available_ip = findViewById(R.id.xcd_available_ip);
        ll_regist = findViewById(R.id.ll_regist);
        view_pop = findViewById(R.id.view_pop);
        btn_idcard_login = findViewById(R.id.btn_idcard_login);
        ll_idcard_info = findViewById(R.id.ll_idcard_info);
        et_idcard = findViewById(R.id.et_idcard);
        et_name = findViewById(R.id.et_name);
        if (!this.isTaskRoot()) { //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            //如果你就放在launcher Activity中话，这里可以直接return了
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;//finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
            }
        }
        apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
        //市局包隐藏模拟警员（武汉市公安局、襄阳、天津）
//        if (ApkUtil.isAppStore() || AuthManagerTwo.XIANGYANG.equals(apkType)) {
            btnAddMember.setVisibility(View.GONE);
            btn_idcard_login.setVisibility(View.GONE);
//        }
        initPopupWindow();
        initDialog();
        isCheckSuccess = false;

        orgHint = getResources().getString(R.string.regist_org_hint);
        nameHint = getResources().getString(R.string.regist_name_hint);
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(RegistGroupCallCenterActivity.this);
            myProgressDialog.setCancelable(false);
        }

        ll_regist.setVisibility(View.GONE);
        judgePermission();
    }

    private void startVPNService(){
        try{
            logger.info("开始绑定VPN");
            // 绑定安全VPN服务
            Intent intent = new Intent();
            intent.setAction(SEC_VPN_SERVICE_ACTION_NAME);
            Intent intent1 = new Intent(createExplicitFromImplicitIntent(this,intent));
            startService(intent1);
            bindService(intent1, secVpnServiceConnection, BIND_AUTO_CREATE);
        }catch(Exception e){
            logger.error("绑定安全VPN服务失败："+e);
            requestDrawOverLays();
        }
    }

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
            RegistGroupCallCenterActivity.this.finish();
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
            changeProgressMsg(getString(R.string.text_get_info_now));
            //襄阳、武铁包启动安全VPN服务
            if(TextUtils.equals(AuthManagerTwo.XIANGYANGPOLICESTORE,apkType) || TextUtils.equals(AuthManagerTwo.XIANGYANG,apkType)
                || TextUtils.equals(AuthManagerTwo.WUTIE,apkType)){
                startVPNService();
            }else if(TextUtils.equals(AuthManagerTwo.TIANJIN,apkType)){
                getTianJinStringToken();
                requestDrawOverLays();
            }else {
                authorize();//认证并获取user信息
                requestDrawOverLays();
            }
        } else {
            if (netWorkDialog != null && !netWorkDialog.isShowing()) {
                netWorkDialog.show();
            }
        }
    }

    /**
     * 获取票据
     */
    private void getTianJinStringToken() {
        MyTerminalFactory.getSDK().putParam(Params.IDENTIFY_TYPE, "");
        MyTerminalFactory.getSDK().putParam(UrlParams.TIANJIN_STORE,false);
        Cursor cursor = getContentResolver().query(Uri.parse(Constants.AUTH_TIAN_JIN_TOKEN_URI),null,null,null,null);
        logger.info("getTianJinStringToken--cursor:"+cursor+"--count:"+((cursor!=null)?cursor.getCount():0));
        if (cursor != null && cursor.moveToFirst()) {
            do{
                int resultCode = cursor.getInt(cursor.getColumnIndex("resultCode"));
                String message = cursor.getString(cursor.getColumnIndex("message"));
                String billStr = cursor.getString(cursor.getColumnIndex("billStr"));
                logger.info("getTianJinStringToken--resultCode"+resultCode+"--message:"+message+"--billStr:"+billStr);
                if(resultCode == BaseCommonCode.SUCCESS_CODE){
                    if(!TextUtils.isEmpty(billStr)){
                        //传给服务端获取警员信息
                        MyTerminalFactory.getSDK().putParam(UrlParams.TIANJIN_STORE,true);
                        MyTerminalFactory.getSDK().putParam(UrlParams.TIANJIN_STRTOKEN,billStr);
                        MyTerminalFactory.getSDK().putParam(Params.IDENTIFY_TYPE, IdentifyType.IDENTIFY_TYPE_TOKEN_OUTER.toString());
                    }else{
                        ToastUtil.showToast(this,getString(R.string.text_get_token_fail));
                    }
                } else {
                    ToastUtil.showToast(this,TextUtils.isEmpty(message)?getString(R.string.text_get_token_fail):message);
                }
            }while(cursor.moveToNext());
        }else{
            ToastUtil.showToast(this,getString(R.string.text_get_token_fail));
        }
        if(cursor!=null){
            cursor.close();
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
                return RegistGroupCallCenterActivity.this;
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
                RegistGroupCallCenterActivity.this.finish();
            }
        }.showDialog();
    }

    private void start() {
        String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        logger.info("apkType:"+apkType+"----deviceType:"+deviceType+"----versionName:"+ MyApplication.instance.getVersionName());
        //vpn没有启动时先启动VPN
        try{
            if(secVpnService == null){
                logger.info("vpn没有绑定成功");
            }
            if(secVpnService != null && !secVpnService.sv_isStarted()){
                secVpnService.sv_setServerAddr("20.50.0.11", "10009");
                secVpnService.sv_start();
                logger.info("vpn没有启动，开始启动vpn");
            }
        }catch(RemoteException e){
            e.printStackTrace();
        }
        PromptManager.getInstance().start();
        //进入注册界面了，先判断有没有认证地址
        String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
        System.out.println("注册服务器的地址:" + authUrl);
        if (TextUtils.isEmpty(authUrl)) {
            //平台包或者没获取到类型，直接用AuthManager中的地址,
            String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            if (ApkUtil.isAppStore()|| AuthManagerTwo.POLICETEST.equals(apkType) || TextUtils.isEmpty(apkType)) {
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

    private void initPopupWindow() {
        View popupWindowView = View.inflate(RegistGroupCallCenterActivity.this, R.layout.regist_import_ip, null);
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


    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveExitHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetNameByOrgHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReturnAvailableIPHandler);
        userOrg.setOnFocusChangeListener(onFocusChangeListener);
        userOrg.addTextChangedListener(new TextWatcherImpOrg());//监听输入内容的变化
        userName.setOnFocusChangeListener(onFocusChangeListener);
        userName.addTextChangedListener(new TextWatcherImpName());
        btnAddMember.setOnClickListener(new OnSwitchingModeClickListener());
        btn_confirm.setOnClickListener(new OnClickListenerImplementation());
        xcd_available_ip.setOnXCDropDownListViewClickListeren(new XCDClickListener());
        btn_idcard_login.setOnClickListener(new IdcardLoginClickListener());
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
    }

    @Override
    public void initData() {
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveExitHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetNameByOrgHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReturnAvailableIPHandler);
        myHandler.removeCallbacksAndMessages(null);
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
            myProgressDialog = null;
        }
        reAuthCount = 0;

        viewHolder = null;
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }

    }

    private void hideProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
        }
    }


    private void goOn() {
        changeProgressMsg(getString(R.string.text_start_success));
        myHandler.removeCallbacksAndMessages(null);
        //第三方app包名
        String third_app_package_name = getIntent().getStringExtra(UrlParams.THIRD_APP_PACKAGE_NAME);

        try{
            Class clazz;
            String type = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
            if(type.equals(TerminalMemberType.TERMINAL_UAV.toString())){
                clazz = Class.forName("cn.vsx.uav.activity.UavMainActivity");
            }else {
//                clazz = Class.forName("cn.vsx.vc.activity.NewMainActivity");
                clazz = Class.forName("cn.vsx.vc.activity.GroupCallCenterActivity");
            }
            Intent intent = new Intent(this,clazz);
            intent.putExtra(UrlParams.THIRD_APP_PACKAGE_NAME,third_app_package_name);
            intent.putExtra(UrlParams.IS_REGIST_ACTIVITY_JOIN,true);
            startActivity(intent);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            userUnit = view.findViewById(R.id.userUnit);
            userIP = view.findViewById(R.id.userIP);
            userPort = view.findViewById(R.id.userPort);
            btnCustomIpOk = view.findViewById(R.id.btn_custom_ip_ok);
            ll_regist_return = view.findViewById(R.id.ll_regist_return);
            rl_regist_connect_efficacy = view.findViewById(R.id.rl_regist_connect_efficacy);
            tv_regist_connect_efficacy = view.findViewById(R.id.tv_regist_connect_efficacy);
            iv_regist_connect_efficacy_ok = view.findViewById(R.id.iv_regist_connect_efficacy_ok);
        }
    }

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    public static final int REQUEST_PERMISSION_SETTING = 1235;
    public static final int OPEN_NET_CODE = 1236;

    public static final int SELF_STARTUP_PERMISSION_CODE = 1237;

    public void requestDrawOverLays() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(RegistGroupCallCenterActivity.this)) {
//                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.open_overlay_permisson));
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
//            } else {
//                MyApplication.instance.startHandlerService();
//                start();
//            }
//        } else {
//            MyApplication.instance.startHandlerService();
//            start();
//        }
        start();
    }


    /**
     * 开启自启动权限 设置页面
     */
    private void startSelfStartupPermission(){
        logger.info("开启自启动权限返回--startSelfStartupPermission");
        SelfStartupPermissionUtils.startToAutoStartSetting(this,SELF_STARTUP_PERMISSION_CODE);
        String string = String.format(getResources().getString(R.string.open_self_permisson),getResources().getString(R.string.app_name));
        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), string);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            //根据打包类型，市局才走这个
            if(AuthManagerTwo.POLICESTORE.equals(apkType) || AuthManagerTwo.POLICETEST.equals(apkType)
                    || AuthManagerTwo.POLICESTOREOUT.equals(apkType)|| AuthManagerTwo.TIANJIN.equals(apkType)){
                startSelfStartupPermission();
            }else{
                // 创建个呼直播服务
//                MyApplication.instance.startHandlerService();
                start();
            }
        } else if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            judgePermission();
        } else if (requestCode == OPEN_NET_CODE) {
            checkIfAuthorize();
        }else if(requestCode == SELF_STARTUP_PERMISSION_CODE){
            logger.info("开启自启动权限返回");
            // 创建个呼直播服务
//            MyApplication.instance.startHandlerService();
            start();
        }
    }

    // 替换为应用在PSTORE中注册时生成的值
    protected static final String CLIENT_ID = "40B2984FC648ECA7F4CEE84C0F234F80";//"B8994F7212536DEBB21D8BE1FDE75F22"

    private void authorize() {

        Map<String, String> userInfo = UserInfo.getUserInfo(RegistGroupCallCenterActivity.this);

        logger.info("请求userInfo：" + userInfo);

        if (userInfo != null) {
            if (!(userInfo.get("account") + "").equals(MyTerminalFactory.getSDK().getParam(UrlParams.ACCOUNT, ""))) {
                logger.error("获取到的警号变了，删除所有数据！！！！");
                DeleteData.deleteAllData();
            }
            TerminalFactory.getSDK().putParam(Params.POLICE_STORE_APK, true);
            MyApplication.instance.setApkType();
            MyApplication.instance.setAppKey();
            MyApplication.instance.setTerminalMemberType();
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
            MyApplication.instance.setTerminalMemberType();
            String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            if(TextUtils.equals(AuthManagerTwo.POLICESTORE,apkType)){
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_please_open_wuhan_police_work_first));
            }



            //获取身份证号
//            String userID = "420800197103163958";
//            String userName = "身份证测试";
//            logger.info("获取到身份证号为："+userID+"------姓名："+userName);
//            MyTerminalFactory.getSDK().putParam(UrlParams.IDCARD, userID);
//            MyTerminalFactory.getSDK().putParam(UrlParams.NAME, userName);
//            MyTerminalFactory.getSDK().putParam(UrlParams.XIANGYANG_STORE,true);
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
            pstoreAPI.requestUserInfo(RegistGroupCallCenterActivity.this, response, CLIENT_ID, accessToken.getToken());
        }

        @Override
        public void onPstoreException(PstoreException pstoreException) {
            if (pstoreException instanceof PstoreAuthException) {
                PstoreAuthException e = (PstoreAuthException) pstoreException;
                switch (e.getErrorCode()) {
                    // client_id 为空
                    case PstoreAuthException.ERROR_CLIENTID_NULL:
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_client_id_is_empty));
                        break;
                    // client_id 非法
                    case PstoreAuthException.ERROR_CLIENTID_ILLEGAL:
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_client_id_is_illegal));
                        break;
                    // 授权码非法
                    case PstoreAuthException.ERROR_GRANT_CODE_ILLEGAL:
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_authorization_code_illegal));
                        break;
                    default:
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unknown_error));
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

    private Response response = new Response() {
        @Override
        public void onResponse(Bundle bundle) {
            Map<String, String> userInfo = UserInfo.getUserInfo(RegistGroupCallCenterActivity.this);
            logger.info("请求userInfo：" + userInfo);

            String userJson = GsonUtils.toJson(UserInfo.getUser(RegistGroupCallCenterActivity.this));
            logger.info("请求userJson：" + userJson);

            logger.error("请求user0：" + bundle.toString());
            UserObject user = UserObject.fromBundle(bundle);
            logger.info("请求user1：" + user);
            MyApplication.instance.setTerminalMemberType();
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
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_access_token_expire));
                        reauthorize();
                        break;
                    // 资源未授权
                    case PstoreUserException.ERROR_RES_UNAUTHORIZED:
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unauthorized_resources));
                        break;
                    // 未知错误
                    case PstoreUserException.ERROR_UNKONWN:
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), getString(R.string.text_unknown_error));
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
}
