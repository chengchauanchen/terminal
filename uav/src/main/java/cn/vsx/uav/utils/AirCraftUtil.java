package cn.vsx.uav.utils;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;

import org.apache.log4j.Logger;

import java.util.List;

import dji.common.flightcontroller.CompassCalibrationState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.gimbal.Gimbal;
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

    private static boolean isAircraftConnected() {
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

    /**
     * @return 指南针是否需要校准
     */
    @SuppressWarnings("unused")
    public static synchronized boolean checkCompass(){
        boolean hasError = false;
        Aircraft aircraft = getAircraftInstance();
        if(aircraft !=null){
            Compass compass = aircraft.getFlightController().getCompass();
            hasError = compass.hasError();
        }
        return hasError;
    }

    /**
     * 校准指南针
     */
    @SuppressWarnings("unused")
    public static synchronized void calibratCompass(){
        Aircraft aircraft = getAircraftInstance();
        if(aircraft !=null){
            Compass compass = aircraft.getFlightController().getCompass();
            boolean hasError = compass.hasError();
            if(hasError){
                //校准回调
                compass.setCalibrationStateCallback(compassCalibrationState -> {
                    if(compassCalibrationState == CompassCalibrationState.HORIZONTAL){
                        //指南针水平校准。用户应水平握住飞机并将其旋转360度。
                        ToastUtils.showShort("水平握住飞机并将其旋转360度");
                    }else if(compassCalibrationState == CompassCalibrationState.VERTICAL){
                        //指南针垂直校准。使用者应垂直握住飞机，使机头指向地面，并将飞机旋转360度。
                        ToastUtils.showShort("垂直握住飞机，使机头指向地面，并将飞机旋转360度");
                    }else if(compassCalibrationState == CompassCalibrationState.SUCCESSFUL){
                        //指南针校准成功
                        ToastUtils.showShort("指南针校准成功");
                    }else if(compassCalibrationState == CompassCalibrationState.FAILED){
                        //指南针校准失败。确保指南针附近没有磁铁或金属物体，然后重试。
                        ToastUtils.showShort("指南针校准失败。确保指南针附近没有磁铁或金属物体，然后重试");
                    }else if(compassCalibrationState == CompassCalibrationState.NOT_CALIBRATING){
                        //正常状态。指南针不在校准中。
                        ToastUtils.showShort("正常状态。指南针不在校准中");
                    }else if(compassCalibrationState == CompassCalibrationState.UNKNOWN){
                        //指南针校准状态未知。
                        ToastUtils.showShort("指南针校准状态未知");
                    }
                });
                compass.startCalibration(djiError -> {
                    if(djiError == null){
                        //开始校准
                        ToastUtils.showShort("开始校准");
                    }else {
                        logger.error("校准指南针出错："+djiError);
                    }
                });
            }
        }
    }

    /**
     * 校准云台
     */
    @SuppressWarnings("unused")
    public static synchronized void calibratGimbals(){
        Aircraft aircraft = getAircraftInstance();
        if(aircraft !=null){
            List<Gimbal> gimbals = aircraft.getGimbals();
            logger.info("云台个数："+gimbals.size());
            for(Gimbal gimbal : gimbals){
                calibratGimbal(gimbal);
            }
        }
    }

    private static synchronized void calibratGimbal(Gimbal gimbal){
        gimbal.setStateCallback(gimbalState -> {
            int calibrationProgress = gimbalState.getCalibrationProgress();
            logger.info("calibrationProgress:"+calibrationProgress);
            boolean calibrationSuccessful = gimbalState.isCalibrationSuccessful();
            if(calibrationSuccessful){
                ToastUtils.showShort("云台校准成功");
            }else {
                ToastUtils.showShort("云台校准失败");
            }
        });
        gimbal.startCalibration(djiError -> {
            if(djiError == null){
                ToastUtils.showShort("云台开始校准");
            }else {
                ToastUtils.showShort("云台校准失败");
                logger.error("云台校准失败:"+djiError);
            }
        });
    }
}
