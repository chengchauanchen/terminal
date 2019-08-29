package cn.vsx.uav.utils;

import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.uav.receiveHandler.ReceiveAircraftFilesHandler;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/25
 * 描述：
 * 修订历史：
 */
public class AirCraftUtil{

    private static Logger logger = Logger.getLogger(AirCraftUtil.class);
    private static boolean storageSpaceEnough = true;

    private static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static synchronized Camera getAircraftCamera(){
        if(null != getAircraftInstance()){
            return getAircraftInstance().getCamera();
        }else {
            return null;
        }
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

    public static MediaManager getMediaManager(){
        if(getAircraftCamera() != null){
            return getAircraftCamera().getMediaManager();
        }else {
            return null;
        }
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
            logger.info("指南针是否需要校准:"+hasError);
            if(hasError){
                //校准回调
                compass.setCalibrationStateCallback(compassCalibrationState -> {
//                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveCalibrationStateCallbackHandler.class,compassCalibrationState);
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
            if(calibrationProgress == 100){
                boolean calibrationSuccessful = gimbalState.isCalibrationSuccessful();
                if(calibrationSuccessful){
                    ToastUtils.showShort("云台校准成功");
                }else {
                    ToastUtils.showShort("云台校准失败");
                }
            }
        });
        gimbal.startCalibration(djiError -> {
            if(djiError == null){
                ToastUtils.showShort("开始云台校准");
            }else {
                ToastUtils.showShort("云台校准失败");
                logger.error("云台校准失败:"+djiError);
            }
        });
    }

    public static void startShootPhoto(){
        Camera camera = getAircraftCamera();
        if(null != camera){
            //判断存储空间有没有满
            if(storageSpaceEnough){
                //相机必须处于拍摄照片模式

                camera.getMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.CameraMode>(){
                    @Override
                    public void onSuccess(SettingsDefinitions.CameraMode cameraMode){
                        if(cameraMode != SettingsDefinitions.CameraMode.SHOOT_PHOTO){
                            camera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, djiError -> {
                                if(djiError !=null){
                                    logger.error("设置相机模式失败:"+djiError);
                                }else {
                                    camera.getShootPhotoMode(new CommonCallbacks.CompletionCallbackWith<SettingsDefinitions.ShootPhotoMode>(){
                                        @Override
                                        public void onSuccess(SettingsDefinitions.ShootPhotoMode shootPhotoMode){
                                            if(shootPhotoMode != SettingsDefinitions.ShootPhotoMode.SINGLE){
                                                camera.setShootPhotoMode(SettingsDefinitions.ShootPhotoMode.SINGLE, djiError1 -> {
                                                    if(djiError1 == null){
                                                        startShootPhoto(camera);
                                                    }else {
                                                        logger.error("设置单张拍照模式失败:"+ djiError1);
                                                    }
                                                });
                                            }else {
                                                startShootPhoto(camera);
                                            }
                                        }

                                        @Override
                                        public void onFailure(DJIError djiError){
                                            logger.info("获取单张拍照模式失败");
                                        }
                                    });
                                }
                            });
                        }else {
                            startShootPhoto(camera);
                        }
                    }
                    @Override
                    public void onFailure(DJIError djiError){
                        logger.info("获取相机模式失败！！--"+djiError);
                    }
                });
            }else {
                ToastUtils.showShort("存储空间已满，无法拍照");
            }
        }
    }

    public static void setFileListener(){
        Camera camera = getAircraftCamera();
        if(camera != null){
            setStorageStateCallBack(camera);
            setMediaFileCallback(camera);
        }
    }

    private static void startShootPhoto(Camera camera){
        camera.startShootPhoto(djiError -> {
            if(djiError != null){
                ToastUtils.showShort(djiError.getDescription());
                logger.error("拍摄照片失败:"+djiError.getDescription());
            }
        });
    }

    private static void setMediaFileCallback(Camera camera){
        camera.setMediaFileCallback(AirCraftUtil::fetchFileData);
    }


    private static void setStorageStateCallBack(Camera camera){
        camera.setStorageStateCallBack(storageState -> {
            //剩余空间
            int remainingSpaceInMB = storageState.getRemainingSpaceInMB();
            logger.info("剩余存储空间："+remainingSpaceInMB);
            if(remainingSpaceInMB < 20){
                storageSpaceEnough = false;
            }else {
                storageSpaceEnough = true;
            }
        });
    }



    public static void getFileList(){
        if(getMediaManager() != null){
            getMediaManager().refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null){
                        List<MediaFile> medias = getMediaManager().getSDCardFileListSnapshot();
                        for(MediaFile media : medias){
                            fetchFileData(media);
                        }
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveAircraftFilesHandler.class,medias);
                    }else {
                        ToastUtils.showShort("获取文件失败："+djiError.getDescription());
                    }
                }
            });
        }
    }

    public static void fetchFileData(MediaFile mediaFile){
        File dir = new File(MyTerminalFactory.getSDK().getUavPictureDirectory());
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName = mediaFile.getFileName();
        mediaFile.fetchFileData(dir, fileName, new DownloadHandler<String>());
    }
}
