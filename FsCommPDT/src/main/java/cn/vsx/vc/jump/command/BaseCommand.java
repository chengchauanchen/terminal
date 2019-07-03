package cn.vsx.vc.jump.command;

import android.content.Context;
import android.util.Log;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.utils.GsonUtils;

abstract class BaseCommand implements IJumpCommand {
    protected Context context;

    public BaseCommand(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void jumpPage(String sendJson) {
        Log.e("JumpService", "sendJson:" + sendJson);
        SendBean sendBean = GsonUtils.sendJsonToBean(sendJson);
        String appKey = sendBean.getAppKey();
        Log.e("JumpService", "APP_KEY:" + appKey);
        TerminalFactory.getSDK().putParam(Params.APP_KEY,appKey);
        jumpPage(sendBean);
    }

    protected abstract void jumpPage(SendBean sendBean);
}
