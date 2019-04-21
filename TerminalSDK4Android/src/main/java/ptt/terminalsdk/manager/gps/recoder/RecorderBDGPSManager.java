package ptt.terminalsdk.manager.gps.recoder;

import android.app.Application;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.apache.log4j.Logger;

import ptt.terminalsdk.manager.gps.MyLocationListener;

/**
 * Created by zckj on 2017/8/29.
 */

public class RecorderBDGPSManager {
    public Application application;
    public RecorderBDGPSManager(Application application){
        this.application = application;
    }

    public LocationClient mLocationClient = null;
    public Logger logger = Logger.getLogger(getClass());

    public MyRecorderLocationListener myListener = new MyRecorderLocationListener();
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    public void init(int span){
        logger.info(LocationManager.TAG+"BDGPSManager初始化了");
        mLocationClient = new LocationClient(application);
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        initLocation(span);
    }

    private void initLocation(int span){

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(false);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        option.setWifiCacheTimeOut(5 * 60 * 1000);
        //可选，V7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位

        mLocationClient.setLocOption(option);
    }



    /**
     * 获取位置信息
     * @param uploadInterval
     */
    public void requestLocationInfo(long uploadInterval){
        int interval = (int) uploadInterval;
        if(mLocationClient == null){
            init((interval>=0)?interval:0);
        }else{
            initLocation((interval>=0)?interval:0);
        }
        mLocationClient.start();
    }

    /**
     * 去掉监听事件
     */
    public void removelocationListener(){
        if (mLocationClient!=null) {
            mLocationClient.unRegisterLocationListener(myListener);
        }
    }

    /**
     * 停止定位
     */
    public void stop(){
        logger.info(LocationManager.TAG+"BDGPSManager销毁了");
//        removelocationListener();
        if(mLocationClient!=null){
            mLocationClient.stop();
        }

    }
}
