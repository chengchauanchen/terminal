package com.vsxin.terminalpad.manager.operation;

/**
 * @author qzw
 *
 * 打电话 按钮 操作
 */
public class PhoneCallOperation implements IOperation{

    @Override
    public void handle() {

    }

    @Override
    public String[] someTypes() {
        //pad能给哪些类型设备打电话？？
        //警务通 == 民警
        return  OperationConfig.PHONE_CALL_CONFIG;
    }
}