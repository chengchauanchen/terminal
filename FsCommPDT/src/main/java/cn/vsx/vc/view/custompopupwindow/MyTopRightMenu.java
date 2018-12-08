package cn.vsx.vc.view.custompopupwindow;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.DeleteTemporaryGroupMemberActivity;
import cn.vsx.vc.activity.IncreaseTemporaryGroupMemberActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverActivePushVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.DensityUtil;
import cn.vsx.vc.utils.SystemUtil;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.DialogUtil;

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
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopRightMenu = new TopRightMenu(context);
                final MenuItem pushItem = new MenuItem(R.drawable.shipin_up, "上报图像");
                final MenuItem pullItem = new MenuItem(R.drawable.shipin_hc, "请求图像");
                final MenuItem createItem = new MenuItem(R.drawable.create_temporary_group,"创建临时组");
                final List<MenuItem> items = new ArrayList<MenuItem>();
                mTopRightMenu.addMenuItem(pullItem);
                mTopRightMenu.addMenuItem(pushItem);
                mTopRightMenu.addMenuItem(createItem);
                items.add(pullItem);
                items.add(pushItem);
                items.add(createItem);
                if(items.size() == 1) {
                    mTopRightMenu.setHeight(240);
                }
                else if (items.size() == 2){
                    mTopRightMenu.setHeight(480);
                }
                else if (items.size() == 3){
                    mTopRightMenu.setHeight(720);
                }
                mTopRightMenu.setHeight(120)
                        .setWidth(DensityUtil.dip2px(context,200))      //默认宽度wrap_content
                        .showIcon(true)     //显示菜单图标，默认为true
                        .dimBackground(true)           //背景变暗，默认为true
                        .needAnimationStyle(true)   //显示动画，默认为true
                        .setAnimationStyle(R.style.TRM_ANIM_STYLE)  //默认为R.style.TRM_ANIM_STYLE
//                        .addMenuItem(new MenuItem(R.drawable.onekey_alarm, "一键告警"))
//                        .addMenuItem(new MenuItem(R.drawable.emergency_call, "紧急呼叫"))
//                        .addMenuItem(new MenuItem(R.drawable.popupwindow_add_contacts, "添加联系人"))
                        .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                            @Override
                            public void onMenuItemClick(int position) {
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
                                                        ToastUtil.showToast(context,"没有图像上报功能权限");
                                                        return;
                                                    }
                                                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverActivePushVideoHandler.class, new ArrayList<Integer>());
                                                }


                                            }else if (items.get(position) == pullItem){
                                                if (!MyApplication.instance.isPttPress) {
                                                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                                                        CheckMyPermission.permissionPrompt(context, Manifest.permission.RECORD_AUDIO);
                                                        return;
                                                    }
                                                    //判断终端权限
                                                    if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
                                                        ToastUtil.showToast(context,"没有图像请求功能权限");
                                                        return;
                                                    }
                                                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, new Member());
                                                }

                                            }
                                            else if(items.get(position) ==createItem){
                                                if (!MyApplication.instance.isPttPress){
                                                    if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_TEMP_CREATE.name())) {
                                                        ToastUtil.showToast(context,"没有创建临时组功能权限");
                                                        return;
                                                    }
                                                    Intent intent = new Intent(context, IncreaseTemporaryGroupMemberActivity.class);
                                                    intent.putExtra("type",0);
                                                    context.startActivity(intent);
                                                }
                                            }
                                            break;
                                        case SPEAKING:
                                            ToastUtil.showToast(context, "个呼中，不能进行其他业务");
                                            break;
                                        case RINGING:
                                            ToastUtil.showToast(context, "个呼中，不能进行其他业务");
                                            break;

                                        default:
                                            break;
                                    }

                                }else {
                                    ToastUtil.showToast(context,"您已处于图像业务中");
                                }

                            }
                        })
                        .showAsDropDown(view, -DensityUtil.px2dip(MyApplication.instance, MyApplication.instance.getResources().getDimension(R.dimen.x150))
                                , DensityUtil.px2dip(MyApplication.instance, MyApplication.instance.getResources().getDimension(R.dimen.y20)));
            }
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
                ToastUtil.showToast(activity,"请前往设置打开相机权限");
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
