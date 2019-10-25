package cn.vsx.uav.receiveHandler;

import java.util.List;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.uav.bean.FileBean;

/**
 *是否显示checkbox
 */
public interface ReceiveSendFileFinishHandler extends ReceiveHandler{
    void handler(List<FileBean> selectFiles);
}
