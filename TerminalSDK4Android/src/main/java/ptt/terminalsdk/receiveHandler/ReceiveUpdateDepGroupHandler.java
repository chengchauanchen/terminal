package ptt.terminalsdk.receiveHandler;

import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import ptt.terminalsdk.bean.GroupBean;

public interface ReceiveUpdateDepGroupHandler extends ReceiveHandler{
	public void handler(List<GroupBean> groupList );
}
