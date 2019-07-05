package cn.vsx.vc.view.custompopupwindow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.ImageView;

import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.ChatBaseActivity;
import cn.vsx.vc.activity.IncreaseTemporaryGroupMemberActivity;
import cn.vsx.vc.activity.NewMainActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.NFCBindingDialog;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.utils.SystemUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by zckj on 2017/3/16.
 */

public class MyTopRightMenu {
    private static MyTopRightMenu myTopRightMenu;
    private Activity activity;
    private static final int REQUEST_PERMISSION_SETTING = 0;

    private MyTopRightMenu(){}
    public static MyTopRightMenu offerObject(){
        synchronized (MyTopRightMenu.class) {
            if(myTopRightMenu == null){
                synchronized (MyTopRightMenu.class) {
                    myTopRightMenu = new MyTopRightMenu();
                }
            }
        }

        return myTopRightMenu;
    }
    private TopRightMenu mTopRightMenu;

    public void initview(final ImageView view, final Activity context){
        this.activity = context;
        view.setOnClickListener(v -> {
            mTopRightMenu = new TopRightMenu(context);
            final MenuItem pushItem = new MenuItem(R.drawable.shipin_up, activity.getString(R.string.text_push));
            final MenuItem pullItem = new MenuItem(R.drawable.shipin_hc, activity.getString(R.string.text_pull));
            final MenuItem createItem = new MenuItem(R.drawable.create_temporary_group,activity.getString(R.string.text_create_temporary_groups));
            final MenuItem scanItem = new MenuItem(R.drawable.scan,activity.getString(R.string.scan));
            final MenuItem nfcItem = new MenuItem(R.drawable.scan,activity.getString(R.string.scan));
            final List<MenuItem> items = new ArrayList<>();
            mTopRightMenu.addMenuItem(pullItem);
            mTopRightMenu.addMenuItem(pushItem);
            mTopRightMenu.addMenuItem(createItem);
            mTopRightMenu.addMenuItem(scanItem);
//            mTopRightMenu.addMenuItem(nfcItem);
            items.add(pullItem);
            items.add(pushItem);
            items.add(createItem);
            items.add(scanItem);
            items.add(nfcItem);
//            if(items.size() == 1) {
//                mTopRightMenu.setHeight(240);
//            }
//            else if (items.size() == 2){
//                mTopRightMenu.setHeight(480);
//            }
//            else if (items.size() == 3){
//                mTopRightMenu.setHeight(720);
//            }else if (items.size() == 4){
//                mTopRightMenu.setHeight(960);
//            }else if (items.size() == 5){
//                mTopRightMenu.setHeight(1200);
//            }
            mTopRightMenu.setHeight(120)
                    .setWidth(DensityUtil.dip2px(context,200))      //默认宽度wrap_content
                    .showIcon(true)     //显示菜单图标，默认为true
                    .dimBackground(true)           //背景变暗，默认为true
                    .needAnimationStyle(true)   //显示动画，默认为true
                    .setAnimationStyle(R.style.TRM_ANIM_STYLE)  //默认为R.style.TRM_ANIM_STYLE
//                        .addMenuItem(new MenuItem(R.drawable.onekey_alarm, "一键告警"))
//                        .addMenuItem(new MenuItem(R.drawable.emergency_call, "紧急呼叫"))
//                        .addMenuItem(new MenuItem(R.drawable.popupwindow_add_contacts, "添加联系人"))
                    .setOnMenuItemClickListener(position -> {
                        //判断权限
                        if(!checkCameraPermission()){
                            gotoSetting();
                            return;
                        }
                        if (MyApplication.instance.getVideoLivePlayingState() == VideoLivePlayingState.IDLE && MyApplication.instance.getVideoLivePushingState() == VideoLivePushingState.IDLE){
                            switch (MyApplication.instance.getIndividualState()){
                                case IDLE:
                                    if (items.get(position) == pushItem){
                                        if (!MyApplication.instance.isPttPress){
                                            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                                                CheckMyPermission.permissionPrompt(context, Manifest.permission.RECORD_AUDIO);
                                                return;
                                            }
                                            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.CAMERA)) {//没有相机权限
                                                CheckMyPermission.permissionPrompt(context, Manifest.permission.CAMERA);
                                                return;
                                            }

                                            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())) {
                                                ToastUtil.showToast(context,activity.getString(R.string.text_has_no_image_report_authority));
                                                return;
                                            }
                                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class,"",false);
                                        }


                                    }else if (items.get(position) == pullItem){
                                        if (!MyApplication.instance.isPttPress) {
                                            if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                                                CheckMyPermission.permissionPrompt(context, Manifest.permission.RECORD_AUDIO);
                                                return;
                                            }
                                            //判断终端权限
                                            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
                                                ToastUtil.showToast(context,activity.getString(R.string.text_has_no_image_request_authority));
                                                return;
                                            }
                                            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, new Member());
                                        }

                                    }
                                    else if(items.get(position) ==createItem){
                                        if (!MyApplication.instance.isPttPress){
                                            if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TEMP_CREATE.name())) {
                                                ToastUtil.showToast(context,activity.getString(R.string.text_has_no_create_temp_group_authority));
                                                return;
                                            }
                                            IncreaseTemporaryGroupMemberActivity.startActivity(context, Constants.CREATE_TEMP_GROUP,0);
                                        }
                                    }else if(items.get(position) ==scanItem){
                                        Intent intent = new Intent(context, CaptureActivity.class);
                                        ZxingConfig config = new ZxingConfig();
                                        config.setShowbottomLayout(false);//底部布局（包括闪光灯和相册）
                                        config.setPlayBeep(true);//是否播放提示音
                                        config.setShake(true);//是否震动
                                        config.setReactColor(R.color.ok_blue);
                                        config.setScanLineColor(R.color.ok_blue);
                                        //config.setShowAlbum(true);//是否显示相册
                                        //config.setShowFlashLight(true);//是否显示闪光灯
                                        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                                        context.startActivityForResult(intent, NewMainActivity.REQUEST_CODE_SCAN);
                                    }else if(items.get(position) ==nfcItem){
                                        int userId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
                                        NFCBindingDialog dialog =  new NFCBindingDialog(context, NFCBindingDialog.TYPE_WAIT);
                                        HashMap<String, String> hashMap = TerminalFactory.getSDK().getHashMap(Params.GROUP_WARNING_MAP, new HashMap<String, String>());
                                        if (hashMap.containsKey(userId + "") && !android.text.TextUtils.isEmpty(hashMap.get(userId + ""))) {
                                            dialog.showDialog(userId, DataUtil.isTempGroup(userId),hashMap.get(userId + ""));
                                        }else{
                                            dialog.showDialog(userId, DataUtil.isTempGroup(userId), "");
                                        }
                                    }
                                    break;
                                case SPEAKING:
                                    ToastUtil.showToast(context, activity.getString(R.string.text_personal_calling_can_not_do_others));
                                    break;
                                case RINGING:
                                    ToastUtil.showToast(context, activity.getString(R.string.text_personal_calling_can_not_do_others));
                                    break;

                                default:
                                    break;
                            }

                        }else {
                            ToastUtil.showToast(context,activity.getString(R.string.text_in_video_function));
                        }

                    })
                    .showAsDropDown(view, -DensityUtil.px2dip(MyApplication.instance, MyApplication.instance.getResources().getDimension(R.dimen.x150))
                            , DensityUtil.px2dip(MyApplication.instance, MyApplication.instance.getResources().getDimension(R.dimen.y20)));
        });
    }

    private void gotoSetting(){
        new DialogUtil() {
            @Override
            public CharSequence getMessage() {
                //                            return "4GPTT需要访问您设备上的照片、媒体内容和文件，否则无法工作；去打开权限?";
                return CheckMyPermission.getDesForPermission(Manifest.permission.CAMERA);
            }

            @Override
            public Context getContext() {
                return activity;
            }

            @Override
            public void doConfirmThings() {
                //点击确定时跳转到设置界面
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
            }

            @Override
            public void doCancelThings() {
                ToastUtil.showToast(activity,activity.getString(R.string.text_go_to_setting_open_camera_authority));
            }
        }.showDialog();
    }

    private boolean checkCameraPermission(){
        //6.0以下检测相机权限
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ){
            if(SystemUtil.cameraIsCanUse()){
                return true;
            }
        }else {
            //6.0以上检测相机权限
            if(CheckMyPermission.selfPermissionGranted(activity, Manifest.permission.CAMERA)){
                return true;
            }
        }
        return false;
    }
}
