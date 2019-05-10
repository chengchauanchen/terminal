package cn.vsx.vc.utils;

import android.text.TextUtils;

import org.apache.log4j.Logger;

import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/25
 * 描述：
 * 修订历史：
 */
public class AirCraftUtil{

    private static Logger logger = Logger.getLogger(AirCraftUtil.class);

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    /**
     * Gets instance of the specific product connected after the
     * API KEY is successfully validated. Please make sure the
     * API_KEY has been added in the Manifest
     */
    public static synchronized BaseProduct getProductInstance() {
        return DJISDKManager.getInstance().getProduct();
    }

    public static synchronized String getAircraftLocation(){
        StringBuilder sb = new StringBuilder();
        if(getAircraftInstance() !=null){
            Aircraft aircraft = getAircraftInstance();
            LocationCoordinate3D location = aircraft.getFlightController().getState().getAircraftLocation();
            logger.info("location.getLatitude():"+location.getLatitude());
            logger.info("location.getLongitude():"+location.getLongitude());
            logger.info("location.getAltitude():"+location.getAltitude());
            if(checkLatitude(location.getLatitude()) && checkLongitude(location.getLongitude())){
                sb.append(location.getLatitude()).append(",").append(location.getLongitude()).append(",").append(location.getAltitude());
            }
        }
        logger.info("无人机位置："+sb.toString());
        return sb.toString();
    }

    public static synchronized double getLatitude(String aircraftLocation){
        if(TextUtils.isEmpty(aircraftLocation) || !aircraftLocation.contains(",")){
            return 0.0;
        }else {
            String[] split = aircraftLocation.split(",");
            return Double.parseDouble(split[0]);
        }
    }

    public static synchronized double getLongitude(String aircraftLocation){
        if(TextUtils.isEmpty(aircraftLocation) || !aircraftLocation.contains(",")){
            return 0.0;
        }else {
            String[] split = aircraftLocation.split(",");
            return Double.parseDouble(split[1]);
        }
    }

    public static synchronized float getAltitude(String aircraftLocation){
        if(TextUtils.isEmpty(aircraftLocation) || !aircraftLocation.contains(",")){
            return 0.0f;
        }else {
            String[] split = aircraftLocation.split(",");
            return Float.parseFloat(split[2]);
        }
    }

    public static boolean checkLatitude(double latitude){
        return latitude !=0.0 && !Double.isNaN(latitude) && latitude >=-90 && latitude<=90;
    }

    public static boolean checkLongitude(double longitude){
        return longitude !=0.0 && !Double.isNaN(longitude) && longitude >=- 180 && longitude <= 180;
    }
}
