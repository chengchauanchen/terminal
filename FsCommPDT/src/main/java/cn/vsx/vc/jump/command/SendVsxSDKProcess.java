package cn.vsx.vc.jump.command;

import android.content.Context;

import com.google.gson.Gson;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.sendMessage.ThirdMessageType;
import cn.vsx.vc.jump.sendMessage.ThirdSendMessage;
import ptt.terminalsdk.context.SDKProcessStateEnum;

/**
 * 绿之云询问我当前的状态
 */
public class SendVsxSDKProcess extends BaseCommand implements IJumpCommand {

    public SendVsxSDKProcess(Context context) {
        super(context);
    }

    @Override
    protected void jumpPage(SendBean sendBean) {
        SendBean send = new SendBean();
        send.setWhatVsxSDKProcess(sendBean.getWhatVsxSDKProcess());
        send.setTime(System.currentTimeMillis());
        send.setLoginState(TerminalFactory.getSDK().getParam(Params.SDK_PROCESS_STATE,SDKProcessStateEnum.DEFAULT_STATE.name()));
        String json = new Gson().toJson(send);
        ThirdSendMessage.getInstance().sendMessageToThird(json, ThirdMessageType.HEART_BEAT);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.SdkProcess;
    }
}
