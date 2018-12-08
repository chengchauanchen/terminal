package org.easydarwin.push;

import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.IStatusCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUVCCameraConnectChangeHandler;
import ptt.terminalsdk.R;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/9/11
 * 描述：
 * 修订历史：
 */

public class UVCCameraService extends Service{

    private Logger logger = Logger.getLogger(getClass());
    public static class UVCCameraLivaData extends LiveData<UVCCamera>{
        @Override
        protected void postValue(UVCCamera value) {
            super.postValue(value);
        }
    }

    public static final UVCCameraLivaData liveData = new UVCCameraLivaData();
    public static class MyUVCCamera extends UVCCamera{

        boolean prev = false;
        @Override
        public synchronized void startPreview() {
            if (prev ) return;
            super.startPreview();
            prev = true;
        }

        @Override
        public synchronized void stopPreview() {
            if (!prev )return;
            super.stopPreview();
            prev = false;

        }

        @Override
        public synchronized void destroy() {
            prev = false;
            super.destroy();
        }
    }
    private static final String TAG = "UVCCameraService";
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;

    private SparseArray<UVCCamera> cameras = new SparseArray<>();

    public class MyBinder extends Binder{

        public UVCCameraService getService() {
            return UVCCameraService.this;
        }

    }

    MyBinder binder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public UVCCamera getCamera() {
        return mUVCCamera;
    }

    private void releaseCamera() {
        if (mUVCCamera != null) {
            try {
                mUVCCamera.close();
                mUVCCamera.destroy();
                mUVCCamera = null;
            } catch (final Exception e) {
                //
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mUSBMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(final UsbDevice device) {
                Log.v(TAG, "onAttach:" + device);
                mUSBMonitor.requestPermission(device);
            }

            @Override
            public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
                releaseCamera();
                logger.info("DevicesOnConnect:");
                try {
                    final UVCCamera camera = new MyUVCCamera();
                    camera.open(ctrlBlock);
                    camera.setStatusCallback(new IStatusCallback() {
                        @Override
                        public void onStatus(final int statusClass, final int event, final int selector,
                                             final int statusAttribute, final ByteBuffer data) {
                            logger.info("onStatus(statusClass=" + statusClass
                                    + "; " +
                                    "event=" + event + "; " +
                                    "selector=" + selector + "; " +
                                    "statusAttribute=" + statusAttribute + "; " +
                                    "data=...)");
                        }
                    });
                    camera.setButtonCallback(new IButtonCallback() {
                        @Override
                        public void onButton(final int button, final int state) {
                            logger.info("onButton(button=" + button + "; " + "state=" + state + ")");
                        }
                    });
                    //					camera.setPreviewTexture(camera.getSurfaceTexture());
                    mUVCCamera = camera;
                    liveData.postValue(camera);
                    logger.info("支持的分辨率:" + mUVCCamera.getSupportedSize());
                    ToastUtil.showToast(UVCCameraService.this,"外置摄像头已连接");
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUVCCameraConnectChangeHandler.class,true);
                    if (device != null)
                        cameras.append(device.getDeviceId(), camera);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
                logger.info("onDisconnect:");
                //                Toast.makeText(MainActivity.this, R.string.usb_camera_disconnected, Toast.LENGTH_SHORT).show();

                //                releaseCamera();

                if (device != null) {
                    UVCCamera camera = cameras.get(device.getDeviceId());
                    if (mUVCCamera == camera) {
                        mUVCCamera = null;
                        TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUVCCameraConnectChangeHandler.class,false);
                        ToastUtil.showToast(UVCCameraService.this,"外置摄像头已断开");
                        liveData.postValue(null);
                    }
                    cameras.remove(device.getDeviceId());
                }else {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUVCCameraConnectChangeHandler.class,false);
                    ToastUtil.showToast(UVCCameraService.this,"外置摄像头已断开");
                    mUVCCamera = null;
                    liveData.postValue(null);
                }

                //                if (mUSBMonitor != null) {
                //                    mUSBMonitor.destroy();
                //                }
                //
                //                mUSBMonitor = new USBMonitor(OutterCameraService.this, this);
                //                mUSBMonitor.setDeviceFilter(DeviceFilter.getDeviceFilters(OutterCameraService.this, R.xml.device_filter));
                //                mUSBMonitor.register();
            }

            @Override
            public void onCancel(UsbDevice usbDevice) {
                releaseCamera();
            }

            @Override
            public void onDettach(final UsbDevice device) {
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUVCCameraConnectChangeHandler.class,false);
                logger.info("onDettach:");
                releaseCamera();
                //                AppContext.getInstance().bus.post(new UVCCameraDisconnect());
            }
        });

        mUSBMonitor.setDeviceFilter(DeviceFilter.getDeviceFilters(this, R.xml.device_filter));
        mUSBMonitor.register();

    }

    @Override
    public void onDestroy() {
        releaseCamera();
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
        super.onDestroy();
    }
}
