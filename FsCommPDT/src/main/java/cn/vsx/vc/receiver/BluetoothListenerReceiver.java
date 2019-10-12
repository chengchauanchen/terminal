package cn.vsx.vc.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.vc.receiveHandle.ReceiveBluetoothListenerHandler;

public class BluetoothListenerReceiver extends BroadcastReceiver {
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
//                            logger.info("onReceive---------蓝牙正在打开中");
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveBluetoothListenerHandler.class, BluetoothAdapter.STATE_TURNING_ON);
                            break;
                        case BluetoothAdapter.STATE_ON:
//                            logger.info("onReceive---------蓝牙已经打开");
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveBluetoothListenerHandler.class, BluetoothAdapter.STATE_ON);
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
//                            logger.info("onReceive---------蓝牙正在关闭中");
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveBluetoothListenerHandler.class, BluetoothAdapter.STATE_TURNING_OFF);
                            break;
                        case BluetoothAdapter.STATE_OFF:
//                            logger.info("onReceive---------蓝牙已经关闭");
                            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveBluetoothListenerHandler.class, BluetoothAdapter.STATE_OFF);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
