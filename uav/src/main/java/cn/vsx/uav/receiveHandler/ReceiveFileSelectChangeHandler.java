package cn.vsx.uav.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.uav.bean.FileBean;

/**
 *文件是否选中
 */
public interface ReceiveFileSelectChangeHandler extends ReceiveHandler{
    void handler(boolean selected, FileBean fileBean);
}
