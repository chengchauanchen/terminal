package cn.vsx.vc.jump.command;

import android.content.Context;

import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.utils.AppKeyUtils;
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
        SendBean sendBean = GsonUtils.sendJsonToBean(sendJson);
        String appKey = sendBean.getAppKey();
        AppKeyUtils.setAppKey(appKey);
        jumpPage(sendBean);
        //SDK进入，将页面设为白天模式
        //MyTerminalFactory.getSDK().putParam(Params.DAYTIME_MODE, true);
        //Log.e("JumpService", "MyTerminalFactory.getSDK().putParam(Params.DAYTIME_MODE, true);");
        //SkinCompatManager.getInstance().loadSkin("daytime.skin", SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS);
        //Log.e("JumpService", "SkinCompatManager.getInstance().loadSkin");

    }

    protected abstract void jumpPage(SendBean sendBean);
}
