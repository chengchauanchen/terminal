package cn.vsx.vsxsdk;

import android.util.Log;

import cn.vsx.vsxsdk.constant.CommandEnum;

/**
 * 命令缓存
 *
 * 场景描述：绿之云接到警情后，调用SDK接口,但此时融合通信app没有启动，导致接口调用不起来
 *
 * 解决办法：只要调用接口连接失败，就重新启动融合通信，
 *          并且将之前的操作缓存起来，
 *          待连接成功后，通过次接口，重新执行
 */
public class CommandCache {
    //单例，值缓存一个指令（最新指令）,其它指令没必要缓存，不然，一次执行多个操作，页面刷刷的跳
    private static CommandCache commandCache;

    private String paramJson;//指令参数
    private int commandType=-1;//指令类型

    private CommandCache() {

    }

    private CommandCache(String paramJson, int commandType) {
        this.paramJson = paramJson;
        this.commandType = commandType;
    }

    public static CommandCache getInstance() {
        if(commandCache==null){
            commandCache = new CommandCache();
        }
        return commandCache;
    }

    public void setCommandCacheData(String paramJson,int commandType){
        this.paramJson = paramJson;
        this.commandType = commandType;
    }

    /**
     * 是否有缓存指令
     * @return
     */
    private boolean hasCache(){
        return commandType!=-1;
    }

    /**
     * 执行缓存指令
     */
    public void doCommandCache() {
        if(!hasCache()){
            return;
        }
        try {
            VsxSDK.getInstance().getIJump().jumpPage(paramJson, commandType);
        } catch (Exception e) {
            Log.e("CommandCache", "执行缓存指令失败");
        }
        clear();
    }

    private void clear(){
        setCommandCacheData("",-1);
    }
}
