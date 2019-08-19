package cn.vsx.uav.receiveHandler;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import dji.common.flightcontroller.CompassCalibrationState;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/19
 * 描述：无人机指南针校准回调
 * 修订历史：
 */
public interface ReceiveCalibrationStateCallbackHandler extends ReceiveHandler{
    void handler(CompassCalibrationState compassCalibrationState);
}
