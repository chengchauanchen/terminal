package cn.vsx.SpecificSDK.instruction;

import android.content.Context;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public abstract class BaseInstruction {

    private Logger logger = Logger.getLogger(getClass());

    private Context context;

    public BaseInstruction(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * 绑定 回调
     */
    public abstract void bindReceiveHandler();

    /**
     * 取消绑定 回调
     */
    public abstract void unBindReceiveHandler();

    /**
     * 注册 回调
     * @param receiveHandler
     */
    public void registerReceiveHandler(ReceiveHandler receiveHandler){
        MyTerminalFactory.getSDK().registReceiveHandler(receiveHandler);
    }

    /**
     * 取消注册的回调
     * @param receiveHandler
     */
    public void unRegisterReceiveHandler(ReceiveHandler receiveHandler){
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveHandler);
    }
}
