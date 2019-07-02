package cn.vsx.vc.jump.command;

import android.content.Context;
import java.util.Map;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.utils.MemberUtil;

/**
 * 组会话
 */
public class GroupChat extends BaseCommand implements IJumpCommand {

    public GroupChat(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.GroupChat;
    }

    @Override
    public void jumpPage(Map<Object, Object> map) {
        int groupNo=MemberUtil.parseGroupNo(map);
        jumpGroupChatActivity(getContext(), groupNo);
    }

    /**
     * 跳转到组会话
     *
     * @param groupNo
     */
    public void jumpGroupChatActivity(Context context, int groupNo) {
        Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupNo);
        GroupCallNewsActivity.startCurrentActivity(context, groupNo, group.getName(), 0, "", true);
    }
}
