package cn.vsx.vc.jump.command;

import android.content.Context;

import java.util.Map;

import cn.vsx.vc.activity.IncreaseTemporaryGroupMemberActivity;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.utils.Constants;

/**
 * 个人会话
 */
public class CreateTemoGroup extends BaseCommand implements IJumpCommand {

    public CreateTemoGroup(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }


    @Override
    public void jumpPage(Map<Object, Object> map) {
        IncreaseTemporaryGroupMemberActivity.startActivity(context, Constants.CREATE_TEMP_GROUP,0);
    }
}
