package cn.vsx.vc.jump.command;

import java.util.Map;

import cn.vsx.vc.jump.constant.CommandEnum;

public interface IJumpCommand  {

    void jumpPage(Map<Object,Object> map);

    CommandEnum getCommandType(CommandEnum commandType);
}
