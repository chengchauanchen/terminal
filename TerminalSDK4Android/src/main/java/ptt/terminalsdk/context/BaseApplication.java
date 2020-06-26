package ptt.terminalsdk.context;

import android.app.Application;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/30
 * 描述：
 * 修订历史：
 */
public class BaseApplication extends Application{
    //电量
    private int batteryLevel  = 0;
    //信号强度
    private int dbmLevel = 0;
    private static BaseApplication baseApp;
    @Override
    public void onCreate(){
        super.onCreate();
        baseApp = this;
    }

    public static BaseApplication getApplication(){
        return baseApp;
    }

    public void setApkType(){
    }

    public void setAppKey(){
    }

    public void setTerminalMemberType(){
    }

    public void startHandlerService() {
    }

    public void stopHandlerService(){
    }

    public void setAppLogined(){
    }

    public void startPromptManager(){
    }

    public void initVsxSendMessage(){

    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public int getDbmLevel() {
        return dbmLevel;
    }

    public void setDbmLevel(int dbmLevel) {
        this.dbmLevel = dbmLevel;
    }
}
