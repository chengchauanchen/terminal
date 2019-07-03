package cn.vsx.vc.jump.command;

import android.content.Context;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;

/**
 * 个人会话
 */
public class ChangeGroup extends BaseCommand implements IJumpCommand {

    public ChangeGroup(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.changeGroup;
    }


    @Override
    public void jumpPage(SendBean  sendBean) {
        String groupNo = sendBean.getGroupNo();
        TerminalFactory.getSDK().getGroupManager().changeGroup(Integer.valueOf(groupNo));
    }

}
