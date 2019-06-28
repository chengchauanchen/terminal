package cn.vsx.vc.activity;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.manager.auth.LoginModel;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetNameByOrgHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRegistCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReturnAvailableIPHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.JudgeWhetherConnect;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ProgressDialog;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.receive.SendRecvHelper;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.KeyboarUtils;
import cn.vsx.vc.utils.NetworkUtil;
import cn.vsx.vc.utils.SetToListUtil;
import cn.vsx.vc.views.XCDropDownListView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;
import ptt.terminalsdk.tools.ToastUtil;

public class RegistActivity extends BaseActivity implements RecvCallBack, Actions {

    private AlertDialog netWorkDialog;
    @Bind(R.id.ll_regist_all)
    LinearLayout llRegistAll;

    @Bind(R.id.ll_regist)
    LinearLayout llRegist;
    @Bind(R.id.userOrg)
    EditText userOrg;
    @Bind(R.id.userName)
    EditText userName;

    @Bind(R.id.ll_reauth_info)
    LinearLayout llreAuthInfo;
    @Bind(R.id.account)
    EditText account;
    @Bind(R.id.name)
    EditText edtName;
    @Bind(R.id.departmentId)
    EditText departmentId;
    @Bind(R.id.departmentName)
    EditText departmentName;

    @Bind(R.id.btn_addMember)
    Button btnAddMember;
    @Bind(R.id.btn_confirm)
    Button btn_confirm;
    @Bind(R.id.tv_version_prompt)
    TextView tvVersionPrompt;
    @Bind(R.id.xcd_available_ip)
    XCDropDownListView xcd_available_ip;

    @Bind(R.id.view_pop)
    View view_pop;
    private int reAuthCount;
    private Timer timer = new Timer();
    private ProgressDialog myProgressDialog;
    private Handler myHandler = new Handler();
    private String orgHint;
    private String nameHint;
    private boolean isHasPermissions;
    public Logger logger = Logger.getLogger(getClass());
    private View popupWindowView;
    private ViewHolder viewHolder;
    private PopupWindow popupWindow;
    private boolean isCheckSuccess;//联通校验是否通过
    private boolean isCheckFinished;//联通校验是否完成

    @Override
    protected void onResume() {
        super.onResume();
        KeyboarUtils.getKeyBoardHeight(this);
    }

    private String company = "添加单位";

/**============================================================================handler=================================================================================**/

//    /**
//     * 网络连接状态
//     */
//    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {
//
//        @Override
//        public void handler(final boolean connected) {
//            RegistActivity.this.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (!MyTerminalFactory.getSDK().getParam(Params.IS_FORBID, false)) {
//                        if (!connected) {
////                            ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "网络异常");
//                        } else {
//                            ToastUtil.closeToast();
//                            if (TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.REGIST_URL, ""))) {
//                                againReAuth();
//                            }
//                        }
//                    }
//                }
//            });
//        }
//    };

    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final boolean isRegisted) {
            logger.info("receiveSendUuidResponseHandler------resultCode：" + resultCode + "；   resultDesc：" + resultDesc + "；   isRegisted：" + isRegisted);
            myHandler.post(() -> {
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        //认证成功，去连接接入服务
                        changeProgressMsg(getResources().getString(R.string.connecting_server));
                    } else if(resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()){
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_temporarily_unavailable_permissions)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                        alerDialog.show();
                    }else if (resultCode== TerminalErrorCode.DEPT_EXPIRED.getErrorCode()){
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_departmental_delegation_expires)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                        alerDialog.show();
                    } else if (resultCode == TerminalErrorCode.TERMINAL_TYPE_ERROR.getErrorCode()) {
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_terminal_type_error)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                        alerDialog.show();
                    } else if (resultCode == TerminalErrorCode.TERMINAL_REPEAT.getErrorCode()) {
                        AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this).setTitle(R.string.text_prompt).setMessage(resultCode + getString(R.string.text_terminal_repeat)).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
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
                        changeProgressMsg(getResources().getString(R.string.auth_fail));
                        myHandler.postDelayed(() -> {
                            llRegistAll.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        }, 2000);
                    } else {
                        //没有注册服务地址，去探测地址
                        if (availableIPlist.isEmpty()) {
                            TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
                        }
                        if (!isRegisted) {
                            ToastUtil.showToast(RegistActivity.this,getString(R.string.please_regist_account));
                            llRegistAll.setVisibility(View.VISIBLE);
                            hideProgressDialog();
                        } else {
                            changeProgressMsg(resultDesc);
                            myHandler.postDelayed(() -> {
                                llRegistAll.setVisibility(View.VISIBLE);
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
        public void handler(final int errorCode, final String errorDesc) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == BaseCommonCode.SUCCESS_CODE) {//注册成功，直接登录
                        changeProgressMsg(getResources().getString(R.string.connecting_server));
                    } else {//注册失败，提示并关界面
                        if (errorCode == TerminalErrorCode.REGISTER_PARAMETER_ERROR.getErrorCode()) {
                            changeProgressMsg(getString(R.string.text_invitation_code_wrong_please_regist_again));
                        } else if (errorCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()) {
                            changeProgressMsg(errorCode + getString(R.string.text_regist_fail_please_check_all_info_is_correct));
                        }else {
                            ToastUtil.showToast(RegistActivity.this,errorDesc);
                        }
                        myHandler.postDelayed(() -> {
                            llRegist.setVisibility(View.VISIBLE);
                            llreAuthInfo.setVisibility(View.GONE);
                            hideProgressDialog();}, 3000);
                    }
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
            logger.info("AuthManager收到信令是否连接的通知" + connected);
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
                    AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this).setTitle(R.string.text_prompt).setMessage(resultDesc).setPositiveButton(R.string.text_sure, (dialogInterface, i) -> finish()).create();
                    alerDialog.show();
                });
            }
        });
    };

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = new ReceiveUpdateAllDataCompleteHandler() {
        @Override
        public void handler(final int errorCode, final String errorDesc) {
            if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                goOn();
            }

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
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    logger.info("收到可用IP列表");
                    availableIPlist.clear();
                    if (availableIP.size() > 0) {
                        availableIPMap = availableIP;
                        availableIPlist.add("选择单位");
                        availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
                        availableIPlist.add(company);
                        xcd_available_ip.setItemsData(availableIPlist);
                    } else {
                        availableIPlist.add("选择单位");
                        availableIPlist.add(company);
                        xcd_available_ip.setItemsData(availableIPlist);
                    }
                    llRegistAll.setVisibility(View.VISIBLE);
                    llRegist.setVisibility(View.VISIBLE);
                    llreAuthInfo.setVisibility(View.GONE);
                    hideProgressDialog();
                }
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



/**============================================================================Listener================================================================================= **/

    private final class OnClickListenerImplementation implements
            OnClickListener {
        @Override
        public void onClick(View v) {
//            if(TextUtils.isEmpty(selectIp)||TextUtils.isEmpty(selectPort)) {
//                ToastUtil.showToast(RegistActivity.this,"请选择单位");
//                return;
//            }
            if(TextUtils.equals("注册",btn_confirm.getText().toString())){
                String itemsData = xcd_available_ip.getItemsData();
                if(company.equals(itemsData)){
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请选择单位");
                    return;
                }

                String useOrg = userOrg.getText().toString().trim();
                String useName = userName.getText().toString().trim();
                if (TextUtils.isEmpty(useOrg)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入邀请码");
                    return;
                } else if (!DataUtil.isLegalOrg(useOrg) || useOrg.length() != 6) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入六位数字邀请码；");
                    return;
                } else if (TextUtils.isEmpty(useName)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入姓名");
                    return;
                }
                else if (!DataUtil.isLegalName(useName) || useName.length() > 12 || useName.length() < 2) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入合法的姓名；\n2-7个字符，支持中英文，数字；\n首位不能是数字");
                    return;
                }
                String registIP = TerminalFactory.getSDK().getAuthManagerTwo().getTempIp();
                String registPort = TerminalFactory.getSDK().getAuthManagerTwo().getTempPort();
                if (TextUtils.isEmpty(registIP) || TextUtils.isEmpty(registPort)) {
                    ToastUtil.showToast(RegistActivity.this,getString(R.string.text_please_select_unit));
                } else {
                    changeProgressMsg(getString(R.string.text_registing));
                    TerminalFactory.getSDK().getAuthManagerTwo().regist(useName, useOrg);
                }
            }else{
                    //认证
                    String useAccount = account.getText().toString().trim();
                    String useDepartmentId=departmentId.getText().toString().trim();
                    String useDepartmentName=departmentName.getText().toString().trim();
                    String name = edtName.getText().toString().trim();
                    MyTerminalFactory.getSDK().putParam(UrlParams.ACCOUNT, useAccount);
                    MyTerminalFactory.getSDK().putParam(UrlParams.NAME, name);
                    MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_ID, useDepartmentId);
                    MyTerminalFactory.getSDK().putParam(UrlParams.DEPT_NAME, useDepartmentName);
                    int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(selectIp, selectPort);
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                       changeProgressMsg(getString(R.string.authing));
                    }
            }
        }
    }

    private class OnSwitchingModeClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            if(llreAuthInfo.getVisibility()==View.GONE&&llRegist.getVisibility() ==View.VISIBLE){
                llRegist.setVisibility(View.GONE);
                llreAuthInfo.setVisibility(View.VISIBLE);
                btn_confirm.setText("认证");
                btnAddMember.setText("邀请码注册");
            }else {
                llRegist.setVisibility(View.VISIBLE);
                llreAuthInfo.setVisibility(View.GONE);
                btn_confirm.setText("注册");
                btnAddMember.setText("模拟警员");
            }
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
                    ((TextView) v).setHint("请输入所在单位名称");
                }
                if (v.getId() == R.id.userIP) {
                    ((TextView) v).setHint("请输入IP地址");
                }
                if (v.getId() == R.id.userPort) {
                    ((TextView) v).setHint("请输入端口号");
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
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "邀请码不合法；\n请输入六位数字");
                } else {
                    if (s.length() == 6) {//长度是六的时候，请求名字
                        logger.info("邀请码输入六位完成；开始到服务器拿名字");
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
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (availableIPlist.size() == 2) {
                        if (position != 0) {
                            viewHolder.userUnit.requestFocus();
                            viewHolder.userUnit.setFocusable(true);
                            popupWindow.showAsDropDown(view_pop);
                            viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
                            viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
//                            viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.liantong));
                        }
                    }
                    if (availableIPlist.size() > 2) {
                        if (position == (availableIPlist.size() - 1)) {
                            viewHolder.userUnit.requestFocus();
                            viewHolder.userUnit.setFocusable(true);
                            popupWindow.showAsDropDown(view_pop);
                            viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
                            viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
//                            viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.liantong));
                        } else if (position != 0) {
                            String name = availableIPlist.get(position);
                            availableIPlist.remove(name);
                            availableIPlist.remove("选择单位");
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
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    popupWindow.dismiss();
                }
            });
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
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入所在单位名称");
            } else {
                String text = viewHolder.userUnit.getText().toString().trim();
                if (!DataUtil.isLegalName(text) || text.length() < 2) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入单位名称；\n2-10个字符，支持中英文，数字；\n首位不能是数字");
                    return;
                }

                if (TextUtils.isEmpty(viewHolder.userIP.getText())) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入IP地址");
                } else {
                    if (TextUtils.isEmpty(viewHolder.userPort.getText())) {
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入端口号");
                    } else {
                        if (!isCheckFinished) {
                            MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable() {

                                @Override
                                public void run() {
                                    isCheckFinished = false;
                                    if (JudgeWhetherConnect.isHostConnectable(viewHolder.userIP.getText().toString(), viewHolder.userPort.getText().toString())) {
                                        isCheckFinished = true;
                                        myHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.green));
                                                viewHolder.tv_regist_connect_efficacy.setVisibility(View.GONE);
                                                viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.VISIBLE);
                                                //                                            viewHolder.btnCustomIpOk.setBackgroundColor(getResources().getColor(R.color.ok_blue));
                                                isCheckSuccess = true;
                                                doAuth();
                                            }
                                        });

                                    } else {
                                        isCheckFinished = true;
                                        myHandler.post(() -> {
                                            isCheckFinished = false;
                                            ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "连通失败，请输入正确的IP和端口号");
                                        });
                                    }
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
            LoginModel authModel = new LoginModel(viewHolder.userUnit.getText().toString(),
                    viewHolder.userIP.getText().toString(), viewHolder.userPort.getText().toString());
            availableIPMap.put(viewHolder.userUnit.getText().toString(), authModel);
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
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入所在单位名称");
            } else {
                String text = viewHolder.userUnit.getText().toString().trim();
                if (!DataUtil.isLegalName(text) || text.length() < 2) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入单位名称；\n2-10个字符，支持中英文，数字；\n首位不能是数字");
                    return;
                }

                if (TextUtils.isEmpty(viewHolder.userIP.getText())) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入IP地址");
                } else {
                    if (TextUtils.isEmpty(viewHolder.userPort.getText())) {
                        ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入端口号");
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
                                myHandler.post(() -> ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "输入的IP和端口号不可用"));
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
//            viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.liantong));

            if (s.length() > 0) {
                if (DataUtil.isLegalOrg(s)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "单位名称不合法；\n首位不能是数字");
                }
                if (!DataUtil.isLegalSearch(s)) {
                    ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "单位名称不合法；\n支持中英文，数字");
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
//            viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.liantong));/
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }



    @Override
    public int getLayoutResId() {
        return R.layout.activity_regist;
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

        initPopupWindow();
        initDialog();
        isCheckSuccess = false;

        orgHint = getResources().getString(R.string.regist_org_hint);
        nameHint = getResources().getString(R.string.regist_name_hint);
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(RegistActivity.this);
            myProgressDialog.setCancelable(false);
        }
        llRegistAll.setVisibility(View.GONE);
        judgePermission();

        xcd_available_ip.requestLayoutClickFocus();
    }

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("网络没有连接，是否打开网络？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivityForResult(intent, OPEN_NET_CODE);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                RegistActivity.this.finish();
            }
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
        if (NetworkUtil.isConnected(MyApplication.instance.getApplicationContext())) {
            if (netWorkDialog != null) {
                netWorkDialog.dismiss();
            }
            changeProgressMsg("正在获取信息");
//            authorize();//认证并获取user信息
//            requestDrawOverLays();
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
                return RegistActivity.this;
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
                RegistActivity.this.finish();
            }
        }.showDialog();
    }

    private void start() {
        PromptManager.getInstance().start();
        //进入注册界面了，先判断有没有认证地址
        String authUrl = TerminalFactory.getSDK().getParam(Params.AUTH_URL, "");
        System.out.println("服务器的地址:" + authUrl);
        if (TextUtils.isEmpty(authUrl)) {
            //平台包或者没获取到类型，直接用AuthManager中的地址,
            String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            if (AuthManagerTwo.POLICESTORE.equals(apkType) || AuthManagerTwo.POLICETEST.equals(apkType) || TextUtils.isEmpty(apkType)) {
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
        popupWindowView = View.inflate(RegistActivity.this, R.layout.regist_import_ip, null);
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
        mPopWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        mPopWindow.setOutsideTouchable(true);

        return mPopWindow;
    }


    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetNameByOrgHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReturnAvailableIPHandler);
        userOrg.setOnFocusChangeListener(onFocusChangeListener);
        userOrg.addTextChangedListener(new TextWatcherImpOrg());//监听输入内容的变化
        userName.setOnFocusChangeListener(onFocusChangeListener);
        userName.addTextChangedListener(new TextWatcherImpName());
        btnAddMember.setOnClickListener(new OnSwitchingModeClickListener());
        btn_confirm.setOnClickListener(new OnClickListenerImplementation());
        xcd_available_ip.setOnXCDropDownListViewClickListeren(new XCDClickListener());

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
        viewHolder.ll_regist_return.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    ViewCompat.animate(v).scaleX(1.2f).scaleY(1.2f).start();
                }else{
                    ViewCompat.animate(v).scaleX(1.0f).scaleY(1.0f).start();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        // 把状态变为登陆 能使父类不会走 protectApp()
        MyApplication.instance.mAppStatus = Constants.LOGINED;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetNameByOrgHandler);
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
        changeProgressMsg("启动成功...");
        myHandler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(RegistActivity.this, MainActivity.class);
        Intent jump = getIntent();
        if(jump!=null){
            intent.putExtra(UrlParams.JUMP_KEY,jump.getStringExtra(UrlParams.JUMP_KEY));
            intent.putExtra(UrlParams.JUMP_VALUE,jump.getStringExtra(UrlParams.JUMP_VALUE));
        }
        startActivity(intent);
        overridePendingTransition(0, R.anim.alpha_hide);
        hideProgressDialog();
        finish();
    }

    private void changeProgressMsg(final String msg) {
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                if (myProgressDialog != null && !isFinishing()) {
                    myProgressDialog.setMsg(msg);
                    myProgressDialog.show();
                }
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
        @Bind(R.id.userUnit)
        EditText userUnit;
        @Bind(R.id.userIP)
        EditText userIP;
        @Bind(R.id.userPort)
        EditText userPort;
        @Bind(R.id.btn_custom_ip_ok)
        Button btnCustomIpOk;
        @Bind(R.id.ll_regist_return)
        LinearLayout ll_regist_return;
        @Bind(R.id.rl_regist_connect_efficacy)
        RelativeLayout rl_regist_connect_efficacy;
        @Bind(R.id.tv_regist_connect_efficacy)
        TextView tv_regist_connect_efficacy;
        @Bind(R.id.iv_regist_connect_efficacy_ok)
        ImageView iv_regist_connect_efficacy_ok;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    public static final int REQUEST_PERMISSION_SETTING = 1235;
    public static final int OPEN_NET_CODE = 1236;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            start();
        } else if (requestCode == REQUEST_PERMISSION_SETTING) {
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

    private void exit(){
        finish();
        Intent stoppedCallIntent = new Intent("stop_indivdualcall_service");
        stoppedCallIntent.putExtra("stoppedResult","0");
        SendRecvHelper.send(getApplicationContext(),stoppedCallIntent);

        PromptManager.getInstance().stop();
        MyApplication.instance.isClickVolumeToCall = false;
        MyApplication.instance.isPttPress = false;
        MyTerminalFactory.getSDK().stop();
        Process.killProcess(Process.myPid());

    }

}
