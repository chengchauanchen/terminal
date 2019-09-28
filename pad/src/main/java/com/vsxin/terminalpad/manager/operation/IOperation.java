package com.vsxin.terminalpad.manager.operation;

public interface IOperation {

    /**
     * 操作处理
     */
    void handle();

    /**
     * 哪些设备类型 可以执行该操作
     */
    String[] someTypes();
}
