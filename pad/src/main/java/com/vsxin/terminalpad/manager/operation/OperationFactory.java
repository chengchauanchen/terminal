package com.vsxin.terminalpad.manager.operation;

public class OperationFactory {

    /**
     * 获取操作类型
     * @return
     */
    public IOperation getOperation(OperationEnum operationEnum) {
        IOperation operation;
        switch (operationEnum.getType()) {
            case OperationConstants.CALL_PHONE:
                operation = new PhoneCallOperation();
                break;
            case OperationConstants.MESSAGE:
                operation = new MessagePageOperation();
                break;
            case OperationConstants.LIVE:
                operation = new PullLiveOperation();
                break;
            case OperationConstants.INDIVIDUAL_CALL:
                operation = new IndividualCallOperation();
                break;
            default:
                operation = null;
                break;
        }
        return operation;
    }
}
