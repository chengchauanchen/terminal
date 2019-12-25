package ptt.terminalsdk.manager.gps.recoder;

import android.app.Application;

import com.sfmap.api.location.SfMapLocationClient;
import com.sfmap.api.location.SfMapLocationClientOption;

import org.apache.log4j.Logger;

/**
 * 顺丰定位
 */

public class RecorderSfGPSManager {
    public Application application;

    public RecorderSfGPSManager(Application application) {
        this.application = application;
    }

    public SfMapLocationClient mSfMapLocationClient = null;
    public Logger logger = Logger.getLogger(getClass());

    public MySfMapLocationListener myListener = new MySfMapLocationListener();

    public void init(int span) {
        logger.info(LocationManager.TAG + "顺丰GPSManager初始化了");
        mSfMapLocationClient = new SfMapLocationClient(application);

        initLocation(span);
    }

    private void initLocation(int span) {

        logger.info(LocationManager.TAG + "顺丰GPSManager---initLocation");
        // 设置定位监听
        mSfMapLocationClient.setLocationListener(myListener);

        //初始化定位参数
        SfMapLocationClientOption locationOption = new SfMapLocationClientOption();

        //设置定位间隔 或者设置单词定位
        locationOption.setInterval(span);
        locationOption.setOnceLocation(false);
        //设置为WGS84坐标系：即地球坐标系，国际上通用的坐标系
        locationOption.setUseGjc02(false);
        locationOption.setLocationMode(SfMapLocationClientOption.SfMapLocationMode.High_Accuracy);
        locationOption.setNeedAddress(true);

        //设置参数
        mSfMapLocationClient.setLocationOption(locationOption);
    }


    /**
     * 获取位置信息
     *
     * @param uploadInterval
     */
    public void requestLocationInfo(long uploadInterval) {
        int interval = (int) uploadInterval;
        if (mSfMapLocationClient == null) {
            init((interval >= 0) ? interval : 0);
        } else {
            initLocation((interval >= 0) ? interval : 0);
        }
        logger.info(LocationManager.TAG + "顺丰GPSManager定位开始1");
        if(!mSfMapLocationClient.isStarted()){
            logger.info(LocationManager.TAG + "顺丰GPSManager定位开始2");
            mSfMapLocationClient.startLocation();
        }
    }

    /**
     * 去掉监听事件
     */
    public void removelocationListener() {
        if (mSfMapLocationClient != null) {
            mSfMapLocationClient.setLocationListener(null);
        }
    }

    /**
     * 停止定位
     */
    public void stop() {
        logger.info(LocationManager.TAG + "顺丰GPSManager销毁了");
//        removelocationListener();
        if (mSfMapLocationClient != null) {
            mSfMapLocationClient.stopLocation();
        }

    }
}
