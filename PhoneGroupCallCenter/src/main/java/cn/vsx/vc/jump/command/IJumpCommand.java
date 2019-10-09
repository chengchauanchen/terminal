package cn.vsx.vc.jump.command;

import cn.vsx.vc.jump.constant.CommandEnum;

public interface IJumpCommand {

    void jumpPage(String sendJson);

    CommandEnum getCommandType(CommandEnum commandType);
}
