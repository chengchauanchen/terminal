package cn.vsx.vc.jump.command;

import android.content.Context;

import cn.vsx.vc.activity.IncreaseTemporaryGroupMemberActivity;
import cn.vsx.vc.jump.bean.SendBean;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.utils.Constants;

/**
 * 个人会话
 */
public class CreateTemoGroup extends BaseCommand {

    public CreateTemoGroup(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }

    @Override
    protected void jumpPage(SendBean sendBean) {
        IncreaseTemporaryGroupMemberActivity.startActivity(context, Constants.CREATE_TEMP_GROUP, 0, true);
    }
}
