package cn.vsx.vc.jump.command;

import android.content.Context;

import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;

/**
 * 个人会话
 */
public class PushVideoLive extends BaseCommand {

    public PushVideoLive(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }

    @Override
    protected void jumpPage(SendBean sendBean) {
        pushLive();
    }

    private void pushLive() {

    }
}
