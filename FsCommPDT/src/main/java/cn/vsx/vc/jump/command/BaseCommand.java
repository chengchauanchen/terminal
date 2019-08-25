package cn.vsx.vc.jump.command;

import android.content.Context;
import android.util.Log;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.utils.AppKeyUtils;
import cn.vsx.vc.jump.utils.GsonUtils;
import ptt.terminalsdk.context.MyTerminalFactory;
import skin.support.SkinCompatManager;

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
        AppKeyUtils.setAppKey(appKey);
        jumpPage(sendBean);
        //SDK进入，将页面设为白天模式
        MyTerminalFactory.getSDK().putParam(Params.DAYTIME_MODE, true);
        SkinCompatManager.getInstance().loadSkin("daytime.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS);
    }

    protected abstract void jumpPage(SendBean sendBean);
}
