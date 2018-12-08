package com.zectec.imageandfileselector.utils;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHandler;

/**
 * 操作接收handler的工具类
 */
public class OperateReceiveHandlerUtilSync {
    private Map<Class<? extends ReceiveHandler>, List<ReceiveHandler>> receiveHandlerMap = new HashMap<>();
    private Map<Class<? extends ReceiveHandler>, Method> receiveHandlerMethodMap = new HashMap<>();
    private Logger logger = Logger.getLogger(OperateReceiveHandlerUtilSync.class);
    private static OperateReceiveHandlerUtilSync instance = null;

    private OperateReceiveHandlerUtilSync() {

    }

    public static OperateReceiveHandlerUtilSync getInstance() {
        if (instance == null) {
            instance = new OperateReceiveHandlerUtilSync();
        }
        return instance;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registReceiveHandler(ReceiveHandler receiveHandler) {
        if (receiveHandler != null) {
            Class[] interfaces = receiveHandler.getClass().getInterfaces();
            Class targetInterface = null;
            if (interfaces != null && interfaces.length > 0) {
                for (Class inf : interfaces) {
                    if (inf != ReceiveHandler.class && ReceiveHandler.class.isAssignableFrom(inf)) {
                        targetInterface = inf;
                    }
                }
            }
            if (targetInterface != null) {
                if (!receiveHandlerMap.containsKey(targetInterface)) {
                    receiveHandlerMap.put(targetInterface, new ArrayList());
                }
                if (!receiveHandlerMap.get(targetInterface).contains(receiveHandler)) {
                    receiveHandlerMap.get(targetInterface).add(receiveHandler);
                }
                if (!receiveHandlerMethodMap.containsKey(targetInterface)) {
                    Method[] methods = targetInterface.getDeclaredMethods();
                    if (methods != null && methods.length == 1) {
                        receiveHandlerMethodMap.put(targetInterface, methods[0]);
                    } else {
                        throw new IllegalArgumentException("接口" + targetInterface.getName() + "中的方法不合法");
                    }
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes"})
    public void unregistReceiveHandler(ReceiveHandler receiveHandler) {
        if (receiveHandler != null) {
            Class[] interfaces = receiveHandler.getClass().getInterfaces();
            Class targetInterface = null;
            if (interfaces != null && interfaces.length > 0) {
                for (Class inf : interfaces) {
                    if (inf != ReceiveHandler.class && ReceiveHandler.class.isAssignableFrom(inf)) {
                        targetInterface = inf;
                    }
                }
            }
            if (targetInterface != null) {
                if (receiveHandlerMap.containsKey(targetInterface)) {
                    receiveHandlerMap.get(targetInterface).remove(receiveHandler);
                }
            }
        }
    }

    public void notifyReceiveHandler(Class<? extends ReceiveHandler> receiveHandlerClass, Object... objects) {
        if (logger.isInfoEnabled()) {
            logger.info("触发了事件：" + receiveHandlerClass.getName() + "，参数是：" + Arrays.toString(objects));
        }
        if (receiveHandlerMethodMap.containsKey(receiveHandlerClass) && receiveHandlerMap.containsKey(receiveHandlerClass)) {
            for (ReceiveHandler receiveHandler : receiveHandlerMap.get(receiveHandlerClass)) {
                try {
                    if (logger.isInfoEnabled()) {
                        logger.info("接收器：" + receiveHandler + "接收了事件");
                    }
                    receiveHandlerMethodMap.get(receiveHandlerClass).invoke(receiveHandler, objects);
                } catch (Exception e) {
                    logger.warn("事件：" + receiveHandlerClass.getName() + "的处理出现异常", e);
                }
            }
        }
    }
}


