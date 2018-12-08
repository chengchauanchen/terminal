package ptt.terminalsdk.manager.gps;

import android.app.Application;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.apache.log4j.Logger;

/**
 * Created by zckj on 2017/8/29.
 */

public class BDGPSManager {
    public Application application;
    public BDGPSManager(Application application){
        this.application = application;
    }

    public LocationClient mLocationClient = null;
    public Logger logger = Logger.getLogger(getClass());

    public MyLocationListener myListener = new MyLocationListener();
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口，原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    public void start(){
        mLocationClient = new LocationClient(application);
        //声明LocationClient类
        initLocation();
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
    }

    private void initLocation(){

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        int span = 0;
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

        mLocationClient.setLocOption(option);
    }

    public void stop(){
        mLocationClient.stop();
    }

}
