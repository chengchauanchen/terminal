package cn.vsx.vc.jump.command;

import android.content.Context;

import java.util.Map;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.constant.ParamKey;

/**
 * 个人会话
 */
public class PersonChat extends BaseCommand implements IJumpCommand {

    public PersonChat(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }


    @Override
    public void jumpPage(Map<Object, Object> map) {
        int memberNo = (int) map.get(ParamKey.MEMBER_NO);
        jumpPersonChatActivity(memberNo);
    }

    /**
     * 跳转到个人会话
     *
     * @param memberNo
     */
    public void jumpPersonChatActivity(int memberNo) {
        TerminalFactory.getSDK().getThreadPool().execute(() -> {
            Account account = DataUtil.getAccountByMemberNo(memberNo, true);
            if (account != null) {
                IndividualNewsActivity.startCurrentActivity(getContext(), account.getNo(), account.getName(), true, 0);
            }
        });
    }
}
