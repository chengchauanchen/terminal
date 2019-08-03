package com.ixiaoma.xiaomabus.architecture.eventbus;

import org.simple.eventbus.EventBus;

public class AndroidEventBus {

    /**
     * 注册
     * @param subscriber
     */
    public static void register(Object subscriber){
        EventBus.getDefault().register(subscriber);
    }

    /**
     * 取消注册
     * @param subscriber
     */
    public static void unregister(Object subscriber){
        EventBus.getDefault().unregister(subscriber);
    }


}
