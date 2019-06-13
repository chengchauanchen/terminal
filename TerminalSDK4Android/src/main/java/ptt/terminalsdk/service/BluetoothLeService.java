package ptt.terminalsdk.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/9/4
 * 描述：
 * 修订历史：
 */

@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    private final static String TAG = "BluetoothLeService";
    //后面的0000-1000-8000-00805f9b34fb一般是固定的，前面的做区分
    public static final UUID INTELLIGENT_LAMP_SERVER = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");//服务
    public static final UUID INTELLIGENT_LAMP_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");//特征值

    public BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothGatt mBluetoothGatt;
    private BluetoothGattService bluetoothGattService;

    public String getmBluetoothDeviceAddress() {
        return mBluetoothDeviceAddress != null ? mBluetoothDeviceAddress : "无效设备";
    }

    public int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    /**
     * 连接成功
     */
    public final static String ACTION_GATT_CONNECTED = "com.higigantic.cloudinglighting.ACTION_GATT_CONNECTED";
    /**
     * 断开连接
     */
    public final static String ACTION_GATT_DISCONNECTED = "com.higigantic.cloudinglighting.ACTION_GATT_DISCONNECTED";
    /**
     * 发现服务
     */
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.higigantic.cloudinglighting.ACTION_GATT_SERVICES_DISCOVERED";
    /**
     * 数据返回
     */
    public final static String PTT_DOWN = "com.zello.ptt.down";
    /**
     * 数据标志
     */
    public final static String PTT_UP = "com.zello.ptt.up";

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    private BluetoothGattCharacteristic nCharacteristic;
    private boolean isConnect = false;//是否发现服务
    private String deviceName;
    //设备名称
    private String mBluetoothDeviceAddress;
    //    当前准备去连接的设备(对象存在不一定连接成功)
    private BluetoothDevice currentDevice;
    private int status = 1;//参见MyBleDevice中的状态
    public static final String EXTRA_DEVICE = "extra_device";
    public static final String EXTRA_STATUS = "extra_status";

    private int connectCount;

    /***
     * 判断是否连接设备
     *
     * @return true已连接  false为未连接
     */
    public boolean isConnectDevice() {
        return isConnect;
    }

    //实现回调方法,关贸总协定app关心的事件。例如,
    //连接变化和服务发现。
    @SuppressLint("NewApi")
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e(TAG, " status:" + status + ";newState" + newState);
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectCount++;
                isConnect = false;
                intentAction = ACTION_GATT_DISCONNECTED;
                Intent intent = new Intent();
                intent.setAction(intentAction);
                intent.putExtra("deviceAddress", mBluetoothDeviceAddress);
                sendBroadcast(intent);
                mConnectionState = STATE_DISCONNECTED;
                ToastUtil.showToast(getApplicationContext(), "设备连接断开！");
                closeGatt();
                closeBluetoothAdapter();
                //重连3次
                if (mBluetoothAdapter.isEnabled() && connectCount < 3) {
                    connect(mBluetoothDeviceAddress);
                } else {
                    connectCount = 0;
                    closeDevice();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String intentAction;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mBluetoothGatt.getServices() != null) {
//                    List<BluetoothGattService> services = mBluetoothGatt.getServices();
                    bluetoothGattService = mBluetoothGatt.getService(INTELLIGENT_LAMP_SERVER);//蓝牙设备中需要使用的服务的UUID
                    if (bluetoothGattService == null) {
                        Log.e(TAG, "没有所需要的服务");
                        mConnectionState = STATE_DISCONNECTED;
                        intentAction = ACTION_GATT_DISCONNECTED;
                        isConnect = false;
                        Intent intent = new Intent();
                        intent.setAction(intentAction);
                        intent.putExtra("deviceAddress", mBluetoothDeviceAddress);
                        sendBroadcast(intent);
                        close();
                        ToastUtil.showToast(getApplicationContext(), "设备连接断开！");
                    } else {
                        List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristics) {

                            Log.e(TAG, "nCharacteristic:" + "特征值：" + characteristic.getUuid());
                        }

                        nCharacteristic = bluetoothGattService.getCharacteristic(INTELLIGENT_LAMP_UUID);//蓝牙模块向手机端回传特性UUID

                        setCharacteristicNotification(nCharacteristic, true);//开启通知
                        mConnectionState = STATE_CONNECTED;
                        intentAction = ACTION_GATT_CONNECTED;
                        Intent intent = new Intent();
                        intent.setAction(intentAction);
                        intent.putExtra("deviceName", deviceName);
                        intent.putExtra("deviceAddress", mBluetoothDeviceAddress);
                        sendBroadcast(intent);
                        BluetoothLeService.this.status = 3;
                        ToastUtil.showToast(getApplicationContext(), "设备连接成功！");
                        Log.e(TAG, "发现服务成功: " + bluetoothGattService);
                        Log.e(TAG, "service成功:" + mBluetoothGatt.getServices().size());
                        isConnect = true;
                        connectCount = 0;
                    }
                }
            } else {
                Log.e(TAG, "发现服务失败: " + status);
                Log.e(TAG, "service失败:" + mBluetoothGatt.getServices().size());
                isConnect = false;
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            Pttbroadcast(characteristic);
        }
    };

    @SuppressLint("NewApi")
    private void Pttbroadcast(BluetoothGattCharacteristic characteristic) {
        if (GroupUtils.currentIsForbid()) {
            //当前是禁止呼叫的组，直接忽略手雷的按下事件
            return;
        }
        final Intent intent = new Intent();
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data) {
                stringBuilder.append(String.format("%02X", byteChar));
            }
            String value = stringBuilder.toString();
            Log.e(TAG, value);
            if (value.equals("01")) {
                Log.e(TAG, "按下");
                intent.setAction(PTT_DOWN);
            } else if (value.equals("00")) {
                Log.e(TAG, "抬起");
                intent.setAction(PTT_UP);
            }
            sendBroadcast(intent);
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();


    public void writeDataToDevice(byte[] data) {
        if (mConnectionState == STATE_CONNECTING) {
            Log.e(TAG, "正在重连中，不能写数据");
            return;
        }
        if (mConnectionState == STATE_DISCONNECTED) {
            Log.e(TAG, "连接断开，不能写数据");
            return;
        }
        if (!isConnect) {
            Log.e(TAG, "未发现服务，不能写数据");
            return;
        }
        if (bluetoothGattService == null) {
            Log.e(TAG, "未获取到服务: 不能写数据");
            return;
        }
        if (nCharacteristic == null) {
            Log.e(TAG, "未获取到特征值: 不能写数据");
            return;
        }
        nCharacteristic.setValue(data); //设置需要发送的数据
        wirteCharacteristic(nCharacteristic);
    }

    public void wirteCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 初始化本地蓝牙适配器的引用。
     *
     * @return 如果初始化成功返回真值.
     */
    @SuppressLint("NewApi")
    public boolean initialize() {
        //在API级别的18以上,得到一个参考BluetoothAdapter通过
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * 连接到关贸总协定服务器托管在蓝牙设备。
     *
     * @param地址目的设备的设备地址。
     * @return返回true,如果连接成功启动。连接的结果 通过异步报道
     * { @code BluetoothGattCallback # onConnectionStateChange(android.bluetooth。BluetoothGatt,int,int)}
     * 回调。
     */
    @SuppressLint("NewApi")
    public boolean connect(final String bleAddress) {
        if (mBluetoothAdapter == null || bleAddress == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        //如果连接的是别的设备，先断开当前设备，再去连接
        if (isConnect && mBluetoothDeviceAddress != null && !bleAddress.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            close();
            connect(bleAddress);
            return true;
        }

        if (mBluetoothDeviceAddress != null && bleAddress.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                Log.w(TAG, "重连  未释放资源");
                return true;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bleAddress);
        currentDevice = device;
        status = 2;
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        deviceName = device.getName();
        mBluetoothGatt = device.connectGatt(this, false, mBluetoothGattCallback);
        mBluetoothDeviceAddress = bleAddress;
        Log.w(TAG, mBluetoothDeviceAddress);
        mConnectionState = STATE_CONNECTING;
        return true;
    }


    public Bundle getCurrentDevice() {
        Bundle bundle = new Bundle();
        if (currentDevice != null) {
            bundle.putParcelable(EXTRA_DEVICE, currentDevice);
            bundle.putInt(EXTRA_STATUS, status);
        }
        return bundle;
    }


    /**
     * 启用或禁用通知给特征。
     *
     * @param特点特点采取行动。
     * @param启用如果这是真的,启用通知。否则错误。
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
            mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public void close() {
        closeGatt();
        closeBluetoothAdapter();
        closeDevice();
    }

    public void closeGatt() {
        if (mBluetoothGatt != null) {
            //     mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            isConnect = false;
            Log.e(TAG, "gatt:close");
        }
    }

    public void closeBluetoothAdapter() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            //  mBluetoothAdapter = null;
        }
    }

    public void closeDevice() {
        currentDevice = null;
        status = 1;
        deviceName = null;
        mBluetoothDeviceAddress = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.close();
    }
}

