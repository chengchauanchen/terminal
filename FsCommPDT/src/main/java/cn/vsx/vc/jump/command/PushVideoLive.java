package cn.vsx.vc.jump.command;

import android.content.Context;

import java.util.Map;

import cn.vsx.vc.jump.constant.CommandEnum;
import cn.vsx.vc.jump.constant.ParamKey;

/**
 * 个人会话
 */
public class PushVideoLive extends BaseCommand implements IJumpCommand {

    public PushVideoLive(Context context) {
        super(context);
    }

    @Override
    public CommandEnum getCommandType(CommandEnum commandType) {
        return CommandEnum.PersonChat;
    }


    @Override
    public void jumpPage(Map<Object, Object> map) {
        String appkey = (String) map.get(ParamKey.APP_KEY);
        //        List<String> memberNos = (List<String>) map.get(ParamKey.MEMBER_NOS);
        //        List<String> groupNos = (List<String>) map.get(ParamKey.GROUP_NOS);

        pushLive();
    }

    private void pushLive(){

    }
}
