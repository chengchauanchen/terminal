package cn.vsx.uav.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAirCraftStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.UavApplication;
import cn.vsx.uav.receiveHandler.ReceiveAircraftFilesHandler;
import cn.vsx.uav.receiveHandler.ReceiveProductRegistHandler;
import cn.vsx.vc.utils.BitmapUtil;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.realname.AircraftBindingState;
import dji.common.realname.AppActivationState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import dji.sdk.products.Aircraft;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.FileTransgerUtil;

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
    private static final int MSG_INFORM_ACTIVATION = 1;
    private static final int ACTIVATION_DALAY_TIME = 1500;
    private static AppActivationState appActivationState;
    private static AircraftBindingState bindingState;
    private static AtomicBoolean hasAppActivationListenerStarted = new AtomicBoolean(false);
    private static Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if(msg.what == MSG_INFORM_ACTIVATION){
                loginToActivationIfNeeded();
            }
        }
    };

    private static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static synchronized FlightController getFlightController(){
        if(getAircraftInstance() != null){
            return getAircraftInstance().getFlightController();
        }else {
            logger.error("获取FlightController 为null！！！");
            return null;
        }
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
        FlightController flightController = getFlightController();
        if(flightController != null && flightController.getState() != null){
            LocationCoordinate3D location = flightController.getState().getAircraftLocation();
            if(location != null && checkLatitude(location.getLatitude()) && checkLongitude(location.getLongitude())){
                sb.append(location.getLatitude()).append(",").append(location.getLongitude()).append(",").append(location.getAltitude());
            }
            logger.info("无人机位置："+sb.toString());
        }
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
        FlightController flightController = getFlightController();
        if(flightController !=null){
            Compass compass = flightController.getCompass();
            hasError = compass.hasError();
        }
        return hasError;
    }

    /**
     * 校准指南针
     */
    @SuppressWarnings("unused")
    public static synchronized void calibratCompass(){
        FlightController flightController = getFlightController();
        if(flightController !=null){
            Compass compass = flightController.getCompass();
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
                ToastUtils.showShort("拍摄照片失败:"+djiError.getDescription());
                logger.error("拍摄照片失败:"+djiError.getDescription());

            }else {
//                ToastUtils.showShort(R.string.uav_take_photo_success);
                logger.error("拍摄照片成功");
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
        File dir = new File(MyTerminalFactory.getSDK().getPhotoRecordDirectory());
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName = FileTransgerUtil.getPhotoFileName()+".jpg";
        String filePath = MyTerminalFactory.getSDK().getPhotoRecordDirectory()+File.separator+fileName;
        mediaFile.fetchPreview(djiError -> {
            if(djiError == null){
                Bitmap preview = mediaFile.getPreview();
                BitmapUtil.saveBitmapFile(preview,MyTerminalFactory.getSDK().getPhotoRecordDirectory(),fileName);
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                operation.generateFileComplete(MyTerminalFactory.getSDK().getPhotoRecordDirectory(),filePath);
            }else {
                logger.info("获取预览图片失败："+djiError.getDescription());
            }
        });
        //下载原图时间太长了，要90秒
//        mediaFile.fetchFileData(dir, fileName, new DownloadHandler<String>(fileName));
    }

    private static AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    public static void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(() -> {
                //                    ToastUtil.showToast(getApplicationContext(),"registering, pls wait...");
                DJISDKManager.getInstance().registerApp(UavApplication.getApplication(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                            logger.info("注册大疆sdk成功");
                            DJISDKManager.getInstance().startConnectionToProduct();
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveProductRegistHandler.class,true,"注册大疆sdk成功");
                        } else {
                            logger.error("注册大疆sdk："+djiError.getDescription());
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveProductRegistHandler.class,false,djiError.getDescription());
                        }
                    }

                    @Override
                    public void onProductDisconnect() {
                        logger.info("onProductDisconnect");
                        notifyStatusChange(false);

                    }
                    @Override
                    public void onProductConnect(BaseProduct baseProduct) {
                        logger.info(String.format("onProductConnect newProduct:%s", baseProduct));
                        notifyStatusChange(true);

                    }
                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                  BaseComponent newComponent) {

                        if (newComponent != null) {
                            newComponent.setComponentListener(isConnected -> {
                                logger.info("onComponentConnectivityChanged: " + isConnected);
                                notifyStatusChange(isConnected);
                            });
                        }
                        logger.info(String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                componentKey,
                                oldComponent,
                                newComponent));

                    }
                });
            });
        }
    }

    private static void notifyStatusChange(boolean connect){
        if(connect){
            Aircraft aircraft= AirCraftUtil.getAircraftInstance();
            if (null != aircraft ) {
                addAppActivationListenerIfNeeded();
            } else {
                MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,false);
            }
        }else {
            AirCraftUtil.bindingState = null;
            AirCraftUtil.appActivationState = null;
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,false);
        }
    }

    private static void addAppActivationListenerIfNeeded() {
        AppActivationState appActivationState = AppActivationManager.getInstance().getAppActivationState();
        logger.info("addAppActivationListenerIfNeeded状态："+appActivationState);
        if (appActivationState != AppActivationState.ACTIVATED) {
            mHandler.sendEmptyMessageDelayed(MSG_INFORM_ACTIVATION, ACTIVATION_DALAY_TIME);
            if (hasAppActivationListenerStarted.compareAndSet(false, true)) {
                AppActivationState.AppActivationStateListener appActivationStateListener = appActivationState1 -> {
                    logger.info("AppActivationStateListener--onUpdate:" + appActivationState1.name());
                    AirCraftUtil.appActivationState = appActivationState1;
                    if(mHandler.hasMessages(MSG_INFORM_ACTIVATION)){
                        mHandler.removeMessages(MSG_INFORM_ACTIVATION);
                    }
                    if(appActivationState1 != AppActivationState.ACTIVATED){
                        mHandler.sendEmptyMessageDelayed(MSG_INFORM_ACTIVATION, ACTIVATION_DALAY_TIME);
                    }else{
                        if(checkIsAircraftConnected()){
                            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class, true);
                        }
                    }
                };
                AircraftBindingState.AircraftBindingStateListener bindingStateListener = bindingState -> {
                    logger.info("Binding State: " + bindingState);
                    AirCraftUtil.bindingState = bindingState;
                    if(checkIsAircraftConnected()){
                        MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class, true);
                    }
                };
                AppActivationManager.getInstance().addAppActivationStateListener(appActivationStateListener);
                AppActivationManager.getInstance().addAircraftBindingStateListener(bindingStateListener);
            }
        }else {
            MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,true);
        }
    }

    public static boolean checkIsAircraftConnected(){
        return appActivationState == AppActivationState.ACTIVATED && bindingState == AircraftBindingState.BOUND;
    }

    public static void loginToActivationIfNeeded() {
        AppActivationState appActivationState = AppActivationManager.getInstance().getAppActivationState();
        logger.info("AppActivationManager.getInstance().getAppActivationState():" + appActivationState);
        if (appActivationState == AppActivationState.LOGIN_REQUIRED) {
            UserAccountManager.getInstance()
                    .logIntoDJIUserAccount(ActivityUtils.getTopActivity(),
                            new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                                @Override
                                public void onSuccess(UserAccountState userAccountState) {
//                                    ToastUtil.showToast(getApplicationContext(),"Login Successed!");
                                    logger.info("大疆账号登陆成功");
                                    MyTerminalFactory.getSDK().putParam(Params.DJ_LOGINED,true);
                                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,true);
                                }

                                @Override
                                public void onFailure(DJIError djiError) {
                                    ToastUtils.showShort("大疆账号登陆失败"+djiError.getDescription());
                                    logger.info("大疆账号登陆失败"+djiError.getDescription());
                                    MyTerminalFactory.getSDK().putParam(Params.DJ_LOGINED,false);
                                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiveAirCraftStatusChangedHandler.class,false);
                                }
                            });
        }
    }
}
