package cn.vsx.vc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;

public class AuthService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new AuthServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("--vsx--AuthService--", "AuthService开始onCreate");

        String[] defaultAddress = TerminalFactory.getSDK().getAuthManagerTwo().getDefaultAddress();
        if (defaultAddress.length >= 2) {
            int resultCode = TerminalFactory.getSDK().getAuthManagerTwo().startAuth(defaultAddress[0], defaultAddress[1]);
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                Log.e("--vsx--AuthService--", "认证成功");
            } else {
                //状态机没有转到正在认证，说明已经在状态机中了，不用处理
                Log.e("--vsx--AuthService--", "状态机没有转到正在认证，说明已经在状态机中了，不用处理");
            }
        } else {
            Log.e("--vsx--AuthService--", "没有注册服务地址，去探测地址");
            //没有注册服务地址，去探测地址
            TerminalFactory.getSDK().getAuthManagerTwo().checkRegistIp();
        }

    }

    public class AuthServiceBinder extends Binder {
        public AuthService getService() {
            return AuthService.this;
        }
    }
}
