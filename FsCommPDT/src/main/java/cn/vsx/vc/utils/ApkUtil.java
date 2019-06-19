package cn.vsx.vc.utils;

import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

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
        return MyTerminalFactory.getSDK().getParam(Params.IS_AUTO_UPDATE, false);
    }
}
