package cn.vsx.vc.model;

import android.bluetooth.BluetoothDevice;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/9/28
 * 描述：
 * 修订历史：
 */

public class MyBleDevice{
    private BluetoothDevice bluetoothDevice;
    private int status; //连接状态   1：未连接   2：连接中  3：已连接

    public BluetoothDevice getBluetoothDevice(){
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice){
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }
}
