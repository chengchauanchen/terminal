package ptt.terminalsdk.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo.LANGFANG;
import static cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo.POLICESTORE;
import static cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo.POLICETEST;
import static cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo.WUTIE;

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
        return MyTerminalFactory.getSDK().getParam(Params.IS_ANJIAN, false);
    }

    //是否为武铁
    public static boolean isWuTie(){
        String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE,POLICESTORE);
        return TextUtils.equals(apkType,WUTIE);
//        return MyTerminalFactory.getSDK().getParam(Params.IS_ANJIAN, false);
    }

    //是否为廊坊
    public static boolean isLangFang(){
        String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE,POLICESTORE);
        return TextUtils.equals(apkType,LANGFANG);
    }

    //是否为武汉市公安局，以及武汉市公安局测试环境
    public static boolean isWuHanPoliceStore(){
        String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE,POLICESTORE);
        return TextUtils.equals(apkType,POLICESTORE)||TextUtils.equals(apkType,POLICETEST);
    }

    /**
     * 获取应用名称
     * @param context
     * @return
     */
    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取不同终端的自动更新的路径
     * @return
     */
    public static String getAPKUpdateAddress(String path){
        StringBuffer address = new StringBuffer();
        address.append(path);
        String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        if(TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
            address.append("/record/");
        }else if(TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_UAV.getCode()){
            address.append("/uav/");
        }else if(TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_PAD.getCode()){
            address.append("/pad/");
        }else if(TerminalMemberType.valueOf(deviceType).getCode() == TerminalMemberType.TERMINAL_HDMI.getCode()){
            address.append("/hdmi/");
        }else{
            address.append("/apk/");
        }
        address.append("version.xml");
        return address.toString();
    }

    public static String getLogUpdateAddress(String path) {
        return path+"/file/upload/log";
    }
}
