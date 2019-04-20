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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthModel;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetNameByOrgHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRegistCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveReturnAvailableIPHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSendUuidResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
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
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;

public class RegistActivity extends BaseActivity implements RecvCallBack, Actions {

    private AlertDialog netWorkDialog;
    @Bind(R.id.userOrg)
    EditText userOrg;
    @Bind(R.id.userName)
    EditText userName;
    @Bind(R.id.btn_confirm)
    Button btn_confirm;
    //    @Bind(R.id.xcd_available_ip)
//    XCDropDownListView xcd_available_ip;
    @Bind(R.id.ll_regist)
    LinearLayout ll_regist;
    //    @Bind(R.id.view_pop)
//    View view_pop;
    private int reAuthCount;
    private Timer timer = new Timer();
    private ProgressDialog myProgressDialog;
    private Handler myHandler = new Handler();
    public Logger logger = Logger.getLogger(getClass());
    //邀请码是否填写
    private boolean orgIsInput = false;
    //姓名是否填写
    private boolean nameIsInput = false;

    public static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
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
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = new ReceiveOnLineStatusChangedHandler() {

        @Override
        public void handler(final boolean connected) {
            RegistActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (!MyTerminalFactory.getSDK().getParam(Params.IS_FORBID, false)) {
                        if (!connected) {
                            ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "网络异常");
                        } else {
                            ToastUtil.closeToast();
                            if (TextUtils.isEmpty(MyTerminalFactory.getSDK().getParam(Params.REGIST_URL, ""))) {
                                againReAuth();
                            }
                        }
                    }
                }
            });
        }
    };

    private ReceiveSendUuidResponseHandler receiveSendUuidResponseHandler = new ReceiveSendUuidResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc, final boolean isRegisted) {

            logger.info("receiveSendUuidResponseHandler------resultCode：" + resultCode + "；   resultDesc：" + resultDesc + "；   isRegisted：" + isRegisted);

            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    //平台
                    if (MyTerminalFactory.getSDK().getParam(Params.POLICE_STORE_APK, false)) {
                        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                            changeProgressMsg("正在登入...");
//                            login();
                        } else if (resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()) {
                            AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this)
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
                            AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this)
                                    .setTitle("提示")
                                    .setMessage(resultCode + ":部门授权过期")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    })
                                    .create();
                            alerDialog.show();
                        } else {
                            hideProgressDialog();
//                            ToastUtil.showToast(MyApplication.instance.getApplicationContext(), resultDesc);
//                            finishActivity();
                            sendUuid(null, null);
                        }
                    } else {//测试
                        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                            if (isRegisted) {//卸载后重装，应该显示注册过了,直接去登录
                                ll_regist.setVisibility(View.GONE);
                                changeProgressMsg("正在登入...");
//                                login();
                            } else {//没注册
                                MyTerminalFactory.getSDK().putParam(Params.MESSAGE_VERSION, 0l);
                                if (availableIPlist.size() < 1) {
                                    //重新探测
                                    againReAuth();
                                    changeProgressMsg("正在找服务器");
                                } else {
                                    ll_regist.setVisibility(View.VISIBLE);
                                    btn_confirm.setVisibility(View.VISIBLE);
                                    hideProgressDialog();
                                }
                            }
                        } else if (resultCode == TerminalErrorCode.DEPT_NOT_ACTIVATED.getErrorCode()) {
                            AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this)
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
                            AlertDialog alerDialog = new AlertDialog.Builder(RegistActivity.this)
                                    .setTitle("提示")
                                    .setMessage(resultCode + ":部门授权过期")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            finish();
                                        }
                                    })
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
                                    btn_confirm.setVisibility(View.VISIBLE);
                                    hideProgressDialog();
                                }
                            }
                        }
                    }
                    //版本的文字提示：内网、西城、东城
                    logger.info("地点是：" + TerminalFactory.getSDK().getParam(Params.PLACE));
//                    tvVersionPrompt.setText(TerminalFactory.getSDK().getParam(Params.PLACE, "zectec") + " " + DataUtil.getVersion(RegistActivity.this));
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
                        logger.info("注册完成的回调----注册成功，直接登录");
                        changeProgressMsg("正在登入...");
//                        login();
                    } else {//注册失败，提示并关界面

                        if (errorCode == TerminalErrorCode.REGISTER_PARAMETER_ERROR.getErrorCode()) {
                            changeProgressMsg("邀请码错误，请重新注册！");
                        } else if (errorCode == TerminalErrorCode.REGISTER_UNKNOWN_ERROR.getErrorCode()) {
                            changeProgressMsg(errorCode + "注册失败，请检查各项信息是否正确！");
                        } else {
                            ToastUtil.showToast(RegistActivity.this, errorDesc);
                        }
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ll_regist.setVisibility(View.VISIBLE);
                                hideProgressDialog();
                            }
                        }, 2000);
                    }
                }
            });

        }
    };


    /**
     * 登陆响应的消息
     */
    private ReceiveLoginResponseHandler receiveLoginResponseHandler = new ReceiveLoginResponseHandler() {
        @Override
        public void handler(final int resultCode, final String resultDesc) {
            logger.info("RegistActivity---收到登录的消息---resultCode:" + resultCode + "     resultDesc:" + resultDesc);
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                        if (!MyTerminalFactory.getSDK().getParam(Params.IS_FIRST_LOGIN, true)
                                && !MyTerminalFactory.getSDK().getParam(Params.IS_UPDATE_DATA, true)) {
                            logger.info("不是第一次登录，也不需要更新数据，直接进入主界面");
                            MyTerminalFactory.getSDK().putParam(Params.FORBID, false);
                            MyTerminalFactory.getSDK().getConfigManager().updateMemberConfig();
                            goOn();
                        } else {
                            logger.info("第一次登录，更新所有数据");
                            updateData();
                        }
                        //登录响应成功，把第一次登录标记置为false；
                        MyTerminalFactory.getSDK().putParam(Params.IS_FIRST_LOGIN, false);
                    } else {
                        changeProgressMsg(resultDesc);
                        myHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressDialog();
                                exit();
                            }
                        }, 3000);
                    }
                }
            });
        }
    };

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = new ReceiveUpdateAllDataCompleteHandler() {
        @Override
        public void handler(final int errorCode, final String errorDesc) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                        logger.info("更新数据成功！");
                        goOn();
                        MyTerminalFactory.getSDK().putParam(Params.IS_UPDATE_DATA, false);//数据更新成功，把是否要更新数据的标记置为false；
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
                }
            });

        }
    };

    ArrayList<String> availableIPlist = new ArrayList<>();
    Map<String, AuthModel> availableIPMap = new HashMap<>();
    /**
     * 获取可用的IP列表
     **/
    private ReceiveReturnAvailableIPHandler receiveReturnAvailableIPHandler = new ReceiveReturnAvailableIPHandler() {
        @Override
        public void handler(final Map<String, AuthModel> availableIP) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    logger.info("收到可用IP列表");
//                    availableIPlist.clear();
                    if (availableIP.size() > 0) {
//                        availableIPMap = availableIP;
//                        availableIPlist.add("选择单位");
                        availableIPlist.addAll(SetToListUtil.setToArrayList(availableIP));
//                        availableIPlist.add(company);
//                        xcd_available_ip.setItemsData(availableIPlist);
//                    } else {
//                        availableIPlist.add("选择单位");
//                        availableIPlist.add(company);
//                        xcd_available_ip.setItemsData(availableIPlist);
                    }
                    //拿到可用IP列表，遍历取第一个
                    if (availableIP != null && availableIP.size() > 0) {
                        AuthModel authModel = null;
                        for (Map.Entry<String, AuthModel> entry : availableIP.entrySet()) {
                            authModel = entry.getValue();
                        }
                        if (authModel != null) {
//                            sendUuid(authModel.getIp(),authModel.getPort());
                            MyTerminalFactory.getSDK().getAuthManagerTwo().reAuth(false, authModel.getIp(), authModel.getPort());
                        }
                    } else {
                        ToastUtil.showToast(RegistActivity.this, "服务器地址不可用");
                        ll_regist.setVisibility(View.VISIBLE);
                        hideProgressDialog();
                    }
                }
            });

        }
    };

    private ReceiveGetNameByOrgHandler receiveGetNameByOrgHandler = new ReceiveGetNameByOrgHandler() {
        @Override
        public void handler(final String returnMemberName, final int resultCoed, final String resultDesc) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (resultCoed == 0) {
                        userName.setText(returnMemberName);
                    } else {
//                        ToastUtil.showToast(RegistActivity.this, resultDesc);
                    }
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
            //startActivity(new Intent (RegistActivity.this,MainActivity.class));
            //finish();

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
            } else if (!DataUtil.isLegalName(useName) || useName.length() > 12 || useName.length() < 2) {
                ToastUtil.showToast(MyApplication.instance.getApplicationContext(), "请输入合法的姓名；\n2-7个字符，支持中英文，数字；\n首位不能是数字");
                return;
            }

            regist(useName, useOrg);
        }
    }

    /**
     * 监听输入的验证码，去服务端请求名字
     */
    private final class TextWatcherImpOrg implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            orgIsInput = !TextUtils.isEmpty(s);
            setEnableButton();

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

    /**
     * 监听输入的姓名
     */
    private final class TextWatcherImpName implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            nameIsInput = !TextUtils.isEmpty(s);
            setEnableButton();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    /**
     * 设置注册按钮是否可用
     */
    private void setEnableButton() {
        btn_confirm.setEnabled(orgIsInput && nameIsInput);
    }

    public String selectIp;
    public String selectPort;

    //控件XCDropDownListView的点击事件
//    private final class XCDClickListener implements XCDropDownListView.XCDropDownListViewClickListeren {
//
//        @Override
//        public void onXCDropDownListViewClickListeren(final int position) {
//            myHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (availableIPlist.size() == 2) {
//                        if (position != 0) {
//                            popupWindow.showAsDropDown(view_pop);
//                            viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
//                            viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
////                            viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.liantong));
//                        }
//                    }
//                    if (availableIPlist.size() > 2) {
//                        if (position == (availableIPlist.size() - 1)) {
//                            popupWindow.showAsDropDown(view_pop);
//                            viewHolder.iv_regist_connect_efficacy_ok.setVisibility(View.GONE);
//                            viewHolder.tv_regist_connect_efficacy.setVisibility(View.VISIBLE);
////                            viewHolder.rl_regist_connect_efficacy.setBackgroundColor(getResources().getColor(R.color.liantong));
//                        } else if (position != 0) {
//                            String name = availableIPlist.get(position);
//                            availableIPlist.remove(name);
//                            availableIPlist.remove("选择单位");
//                            availableIPlist.add(0, name);
//                            xcd_available_ip.setItemsData(availableIPlist);
//                            selectIp = availableIPMap.get(name).getIp();
//                            selectPort = availableIPMap.get(name).getPort();
//
//                            MyTerminalFactory.getSDK().getAuthManagerTwo().reAuth(false, availableIPMap.get(name).getIp(), availableIPMap.get(name).getPort());
//
//                        }
//                    }
//                }
//            });
//        }
//    }


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

        initDialog();

        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(RegistActivity.this);
            myProgressDialog.setCancelable(false);
        }

        ll_regist.setVisibility(View.GONE);


        judgePermission();
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
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                MyTerminalFactory.getSDK().start();
                PromptManager.getInstance().start();
            }
        });
        //发送认证消息，uuid到注册服务器，判断是注册还是登录
        sendUuid(null, null);
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveSendUuidResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRegistCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetNameByOrgHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveReturnAvailableIPHandler);
        userOrg.addTextChangedListener(new TextWatcherImpOrg());//监听输入内容的变化
        userName.addTextChangedListener(new TextWatcherImpName());
        btn_confirm.setOnClickListener(new OnClickListenerImplementation());
//        xcd_available_ip.setOnXCDropDownListViewClickListeren(new XCDClickListener());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetNameByOrgHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveReturnAvailableIPHandler);
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

    private void regist(final String memberName, final String orgBlockCode) {
        if (!Util.isEmpty(memberName)) {
            changeProgressMsg("正在注册...");
            ll_regist.setVisibility(View.GONE);
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MyTerminalFactory.getSDK().getAuthManagerTwo()
                        .regist(memberName, orgBlockCode);
            }
        }, 500);
    }

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
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                MyTerminalFactory.getSDK().getAuthManagerTwo().login();
            }
        });
    }

    private void sendUuid(final String ip, final String port) {
        reAuthCount++;
        if (reAuthCount > 3) {
            changeProgressMsg("登录失败，请检查网络是否连接");
            myHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    exit();
                }
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
        startActivity(new Intent(RegistActivity.this, MainActivity.class));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            // 创建个呼直播服务
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
