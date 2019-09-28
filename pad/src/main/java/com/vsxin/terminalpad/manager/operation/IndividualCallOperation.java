package com.vsxin.terminalpad.manager.operation;

/**
 * @author qzw
 *
 * 个呼 按钮 操作
 */
public class IndividualCallOperation implements IOperation{

    @Override
    public void handle() {

    }

    @Override
    public String[] someTypes() {
        //pad能给哪些类型设备打个呼？？
        //警务通、无人机、PDT终端(350M)、LTE终端、民警
        return OperationConfig.INDIVIDUAL_CALL_CONFIG;
    }
}
