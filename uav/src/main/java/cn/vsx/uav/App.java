package cn.vsx.uav;

import android.content.Context;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.vc.application.MyApplication;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/5/13
 * 描述：
 * 修订历史：
 */
public class App extends MyApplication{

    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        com.secneo.sdk.Helper.install(this);
    }

    @Override
    public void setTerminalMemberType(){
        MyTerminalFactory.getSDK().putParam(UrlParams.TERMINALMEMBERTYPE, TerminalMemberType.TERMINAL_UAV.toString());
    }
}
