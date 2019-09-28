package com.vsxin.terminalpad.manager.operation;

/**
 * @author qzw
 *
 * 拉视频 按钮 操作
 */
public class PullLiveOperation implements IOperation {

    @Override
    public void handle() {

    }

    @Override
    public String[] someTypes() {
        //pad能拉哪些类型流？？
        //警务通、执法仪、无人机、LTE、布控球、城市摄像头
        return OperationConfig.PULL_LIVE_CONFIG;
    }
}