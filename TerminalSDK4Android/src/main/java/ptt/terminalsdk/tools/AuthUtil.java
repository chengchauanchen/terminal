package ptt.terminalsdk.tools;

import android.content.Context;
import android.text.TextUtils;

import org.apache.log4j.Logger;

import java.util.Map;

import cn.com.cybertech.pdk.UserInfo;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.auth.AuthManagerTwo;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.R;
import ptt.terminalsdk.context.BaseApplication;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/30
 * 描述：
 * 修订历史：
 */
public class AuthUtil{

    private static Logger logger = Logger.getLogger("AuthUtil");

    /**
     * 获取移动警务平台信息
     * @param context
     */
    public static void setOauthInfo(Context context) {
        Map<String, String> userInfo = UserInfo.getUserInfo(context);
        logger.info("请求userInfo：" + userInfo);
        if (userInfo != null) {
            if (!(userInfo.get("account") + "").equals(MyTerminalFactory.getSDK().getParam(UrlParams.ACCOUNT, ""))) {
                logger.error("获取到的警号变了，删除所有数据！！！！");
                DeleteData.deleteAllData();
            }
            TerminalFactory.getSDK().putParam(Params.POLICE_STORE_APK, true);
            BaseApplication.getApplication().setApkType();
            BaseApplication.getApplication().setAppKey();
            BaseApplication.getApplication().setTerminalMemberType();
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
            BaseApplication.getApplication().setTerminalMemberType();
            String apkType = TerminalFactory.getSDK().getParam(Params.APK_TYPE, AuthManagerTwo.POLICESTORE);
            if(TextUtils.equals(AuthManagerTwo.POLICESTORE,apkType)){
                ToastUtil.showToast(BaseApplication.getApplication().getApplicationContext(), context.getString(R.string.text_please_open_wuhan_police_work_first));
            }
        }
    }
}
