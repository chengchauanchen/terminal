package cn.vsx.uav.activity;

import android.Manifest;

import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.bean.Permissions;
import com.qw.soul.permission.callbcak.CheckRequestPermissionsListener;

import java.util.Arrays;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.uav.UavApplication;
import cn.vsx.uav.receiveHandler.ReceiveProductRegistHandler;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.vc.activity.NewMainActivity;
import ptt.terminalsdk.tools.DialogUtils;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/17
 * 描述：
 * 修订历史：
 */
public class UavMainActivity extends NewMainActivity{

    @Override
    public void initData(){
        super.initData();
        logger.info("UavMainActivity---initData");
        //申请电话权限，上报时拦截电话
        Permissions permissions = Permissions.build(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE);
        SoulPermission.getInstance().checkAndRequestPermissions(permissions, new CheckRequestPermissionsListener(){
            @Override
            public void onAllPermissionOk(Permission[] allPermissions){
                logger.info("申请成功权限:"+Arrays.toString(allPermissions));
            }

            @Override
            public void onPermissionDenied(Permission[] refusedPermissions){
                logger.info("申请被拒绝权限:"+Arrays.toString(refusedPermissions));
            }
        });
        AirCraftUtil.startSDKRegistration();
        UavApplication.getApplication().startPushService();
    }

    @Override
    public void initListener(){
        super.initListener();
        TerminalFactory.getSDK().registReceiveHandler(receiveProductRegistHandler);
    }

    @Override
    public void doOtherDestroy(){
        super.doOtherDestroy();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveProductRegistHandler);
    }

    private ReceiveProductRegistHandler receiveProductRegistHandler = (success, description) -> {
        if(!success){
            DialogUtils.showDialog(UavMainActivity.this,description);
        }
    };
}
