package cn.vsx.uav.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import cn.vsx.uav.bean.FileBean;

/**
 *是否显示预览图片或者视频Fragment
 */
public interface ReceiveShowPreViewHandler extends ReceiveHandler{
    void handler(boolean show, FileBean fileBean);
}
