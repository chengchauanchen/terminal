package cn.vsx.vc.utils;

import android.text.TextUtils;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo.CHUTIANYUN;
import static cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo.POLICESTORE;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/18
 * 描述：
 * 修订历史：
 */
public class ApkUtil{

    //是否为市局的包
    public static boolean showLteApk(){
        return MyTerminalFactory.getSDK().getParam(Params.SHOW_LTE, false);
    }

    //是否为安监
    public static boolean isAnjian(){
        String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE,POLICESTORE);
        return TextUtils.equals(apkType,CHUTIANYUN);
//        return MyTerminalFactory.getSDK().getParam(Params.IS_ANJIAN, false);
    }

    //是否为移动警务平台的包
    public static boolean isAppStore(){
        String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE,POLICESTORE);
        return TextUtils.equals(AuthManagerTwo.POLICESTORE,apkType) || TextUtils.equals(AuthManagerTwo.XIANGYANGPOLICESTORE,apkType)||TextUtils.equals(AuthManagerTwo.TIANJIN,apkType);
//        return MyTerminalFactory.getSDK().getParam(Params.IS_ANJIAN, false);
    }

}
