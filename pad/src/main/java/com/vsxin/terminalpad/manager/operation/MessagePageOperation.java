package com.vsxin.terminalpad.manager.operation;

/**
 * @author qzw
 *
 * 消息 按钮 操作
 */
public class MessagePageOperation implements IOperation{

    @Override
    public void handle() {

    }

    @Override
    public String[] someTypes() {
        //pad能给哪些类型设备打电话？？
        //警务通、无人机、民警
        return OperationConfig.MESSAGE_PAGE_CONFIG;
    }
}
