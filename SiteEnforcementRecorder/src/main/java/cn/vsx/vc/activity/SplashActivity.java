package cn.vsx.vc.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cn.vsx.SpecificSDK.SpecificSDK;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receive.Actions;
import cn.vsx.vc.receive.RecvCallBack;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.KeyboarUtils;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;

public class SplashActivity extends BaseActivity implements RecvCallBack, Actions {

//    @Bind(R.id.progress_dialog_img)
//    ImageView progress_dialog_img;

    public static final int REQUEST_PERMISSION_SETTING = 1235;
    public static final int OPEN_NET_CODE = 1236;
    public static final int REQUEST_CODE_SCAN = 1237;
    public static final int PROGRESS_TIME = 1*1000;
    /**
     * Handler
     */
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // 把状态变为登陆 能使父类不会走 protectApp()
        MyApplication.instance.mAppStatus = Constants.LOGINED;
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_splash;
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
//        Animation anim = AnimationUtils.loadAnimation(this, R.anim.loading_dialog_progressbar_anim);
//        progress_dialog_img.setAnimation(anim);
        myJudgePermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboarUtils.getKeyBoardHeight(this);
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void doOtherDestroy() {
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void startLiveService() {}

    @Override
    protected void requestStartLive() {}

    @Override
    protected void finishVideoLive() {}

    /**
     * 必须要有SD卡和读取电话状态的权限，APP才能使用
     */
    private void myJudgePermission() {
        if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//SD卡读写权限
            if (CheckMyPermission.selfPermissionGranted(this, Manifest.permission.READ_PHONE_STATE)) {//手机权限，获取uuid
                SpecificSDK.getInstance().configLogger();
                startSDK();
            } else {
                CheckMyPermission.permissionPrompt(this, Manifest.permission.READ_PHONE_STATE);
            }

        } else {
            CheckMyPermission.permissionPrompt(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    /**
     * 启动
     */
    private void startSDK(){
        if(!MyTerminalFactory.getSDK().isStart()){
            MyTerminalFactory.getSDK().start();
            MyTerminalFactory.getSDK().getDataManager().clearMemberNo();
        }
        PromptManager.getInstance().start();
        handler.postDelayed(this::jump, PROGRESS_TIME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CheckMyPermission.REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    myJudgePermission();
                } else {
                    // Permission Denied
                    permissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            case CheckMyPermission.REQUEST_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    myJudgePermission();
                } else {
                    // Permission Denied
                    permissionDenied(Manifest.permission.READ_PHONE_STATE);
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
                return CheckMyPermission.getDesForPermission(permissionName,getResources().getString(R.string.app_name));
            }

            @Override
            public Context getContext() {
                return SplashActivity.this;
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
                SplashActivity.this.finish();
            }
        }.showDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            // 从设置界面返回时再判断权限是否开启
            myJudgePermission();
        } else if (requestCode == OPEN_NET_CODE) {
            myJudgePermission();
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//    }

    /**
     * 跳转逻辑
     */
    private void jump() {
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        finish();
    }
}
