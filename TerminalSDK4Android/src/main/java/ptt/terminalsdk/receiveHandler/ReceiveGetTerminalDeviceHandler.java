package ptt.terminalsdk.receiveHandler;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * Created by zckj on 2017/7/5.
 */

public interface ReceiveGetTerminalDeviceHandler extends ReceiveHandler {
    public void handler(int depId, List<String> type, List<Department> departments, List<Member> members);
}
