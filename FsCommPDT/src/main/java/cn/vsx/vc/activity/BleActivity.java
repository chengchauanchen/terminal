package cn.vsx.vc.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.vc.R;
import cn.vsx.vc.model.MyBleDevice;
import cn.vsx.vc.utils.StringUtil;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.MToggleButton;
import ptt.terminalsdk.service.BluetoothLeService;

public class BleActivity extends BaseActivity{

    @Bind(R.id.switch_ble)
    MToggleButton switch_ble;
    @Bind(R.id.ble_list)
    ListView bleList;
    @Bind(R.id.tv_close_ble)
    TextView tv_close_ble;
    @Bind(R.id.iv_connecting)
    ImageView iv_connecting;
    @Bind(R.id.rl_usable_device)
    RelativeLayout rl_usable_device;
    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Handler mHandler = new Handler();
    private boolean mScanning;
    public static final long SCAN_PERIOD = 5000;//扫描时间
    private static final long SCAN_LIST = 10;
    private List<MyBleDevice> mLeDevices = new ArrayList<>();
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothLeService mBluetoothLeService;
    private String connectingDeviceAddress;//正在连接中的设备地址
    private boolean connecting;//是否正在连接中
    private int connectingPosition;//正在连接的设备在列表中的位置

    @Override
    public int getLayoutResId(){
        return R.layout.activity_ble;
    }

    @Override
    public void initView(){
    }

    @Override
    public void initListener(){
        switch_ble.setOnBtnClick(new MToggleButton.OnBtnClickListener(){
            @Override
            public void onBtnClick(boolean currState){
                if(currState){
                    //打开蓝牙
                    enableBluetooth(true);
                    tv_close_ble.setVisibility(View.GONE);
                    rl_usable_device.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(scanDevice,2000);
                }else{
                    //关闭蓝牙
                    if(mScanning){
                        scanLeDevice(false);
                    }
                    mLeDevices.clear();
                    devices.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    enableBluetooth(false);
                    rl_usable_device.setVisibility(View.GONE);
                    tv_close_ble.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void initData(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            ToastUtil.showToast(this, "该设备不支持Ble");
        }
        //Android6.0以上连接蓝牙需要获得位置权限
        if(Build.VERSION.SDK_INT >= 23){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }
        }
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 检查设备上是否支持蓝牙
        if(mBluetoothAdapter == null){
            ToastUtil.showToast(this, "没有发现本机Ble设备");
            return;
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        registerReceiver(mbtBroadcastReceiver, makeGattUpdateIntentFilter());
        if(mBluetoothAdapter.isEnabled()){
            switch_ble.initToggleState(true);
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    scanLeDevice(true);
                }
            },500);
            tv_close_ble.setVisibility(View.GONE);
            rl_usable_device.setVisibility(View.VISIBLE);
        }else{
            tv_close_ble.setVisibility(View.VISIBLE);
            rl_usable_device.setVisibility(View.GONE);
            switch_ble.initToggleState(false);
        }
        mLeDeviceListAdapter = new LeDeviceListAdapter(mLeDevices);
        bleList.setAdapter(mLeDeviceListAdapter);
        bleList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                mHandler.removeCallbacksAndMessages(null);
                //如果正在连接，不用管
                if(connecting){
                    return;
                }
                final MyBleDevice device = mLeDevices.get(position);
                if(device.getBluetoothDevice() == null){
                    return;
                }
                if(mScanning){
                    hideConnectingAnimate();
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBluetoothAdapter.cancelDiscovery();
                    mScanning = false;
                }
                connectingPosition = position;
                connecting = true;
                connectingDeviceAddress = device.getBluetoothDevice().getAddress();
                mLeDevices.get(position).setStatus(2);
                mLeDeviceListAdapter.notifyDataSetChanged();
                connectDevice(device);

            }
        });
    }

    public boolean enableBluetooth(boolean enable) {
        if (enable) {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
            return true;
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
            }
            return false;
        }
    }


    private void showConnectingAnimate(){
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.loading_dialog_progressbar_anim);
        if (anim != null){
            iv_connecting.startAnimation(anim);
        }
    }

    private void hideConnectingAnimate(){
        if(mScanning){
            iv_connecting.clearAnimation();
        }
    }

    private void connectDevice(MyBleDevice device){
        if(mBluetoothAdapter.isEnabled()){
            mBluetoothLeService.connect(device.getBluetoothDevice().getAddress());
        }else {
            ToastUtil.showToast(this,"请打开蓝牙");
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter(){
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        //        intentFilter.addAction(BluetoothLeService.ACTION_STRING_DATA);
        return intentFilter;
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service){
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e("BleActivity", "服务绑定成功");
            if(!mBluetoothLeService.initialize()){
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName){
            mBluetoothLeService = null;
            Toast.makeText(BleActivity.this, "蓝牙服务断开", Toast.LENGTH_SHORT).show();
        }
    };

    private void scanLeDevice(final boolean enable){
        //开始扫描就将设备列表显示出来，图片隐藏
        bleList.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(afterScan);
        if(enable){
            if(mScanning){
                mHandler.post(stopScan);
                mHandler.postDelayed(scanDevice,500);
                return;
            }
            // 停止扫描后一个预定义的扫描周期。
            mHandler.postDelayed(afterScan, SCAN_PERIOD);
            mLeDevices.clear();
            devices.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
            mScanning = true;
            showConnectingAnimate();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }else{
            hideConnectingAnimate();
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback(){

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord){
            Log.e("BleActivity", device.getAddress());
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if(!devices.contains(device) && StringUtil.checkStringIsValid(device.getName())){
                        MyBleDevice myBleDevice = new MyBleDevice();
                        myBleDevice.setBluetoothDevice(device);
                        myBleDevice.setStatus(1);
                        mLeDevices.add(myBleDevice);
                        devices.add(device);
                        mLeDeviceListAdapter.notifyDataSetChanged();
                    }

                }
            }, SCAN_LIST);
        }
    };

    @Override
    public void onBackPressed(){
        onBack();
    }

    @OnClick(R.id.news_bar_back)
    public void onBack(){
        if(mScanning){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.cancelDiscovery();
            mScanning = false;
        }
        mHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @OnClick(R.id.ll_connecting)
    public void refreshDevice(){
        if(mScanning){
            scanLeDevice(false);
        }else{
            scanLeDevice(true);
        }
    }

    @Override
    public void doOtherDestroy(){
        hideConnectingAnimate();
        unbindService(mServiceConnection);
        unregisterReceiver(mbtBroadcastReceiver);
    }

    private Runnable scanDevice = new Runnable(){
        @Override
        public void run(){
            scanLeDevice(true);
        }
    };

    private Runnable stopScan = new Runnable(){
        @Override
        public void run(){
            scanLeDevice(false);
        }
    };

    private Runnable afterScan = new Runnable(){
        @Override
        public void run(){
            hideConnectingAnimate();
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            //搜索设备
            if(mLeDeviceListAdapter.getCount() == 0){
                ToastUtil.showToast(BleActivity.this, "没有搜索到蓝牙设备");
            }
        }
    };

    BroadcastReceiver mbtBroadcastReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            connecting = false;
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                String deviceAddress = intent.getStringExtra("deviceAddress");
                if(!TextUtils.isEmpty(deviceAddress) && deviceAddress.equals(connectingDeviceAddress)){
                    mLeDevices.get(connectingPosition).setStatus(3);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    connectingDeviceAddress = null;
                }
            }
            if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                String deviceAddress = intent.getStringExtra("deviceAddress");
                if(!TextUtils.isEmpty(deviceAddress) && deviceAddress.equals(connectingDeviceAddress)){
                    mLeDevices.get(connectingPosition).setStatus(1);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    connectingDeviceAddress = null;
                }
            }
        }
    };

    // 适配器为持有设备通过扫描发现。
    private class LeDeviceListAdapter extends BaseAdapter{
        private LayoutInflater mInflator;
        private List<MyBleDevice> mLeDevices;

        public LeDeviceListAdapter(List<MyBleDevice> mLeDevices){
            super();
            this.mLeDevices = mLeDevices;
            mInflator = LayoutInflater.from(BleActivity.this);
        }

        @Override
        public int getCount(){
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i){
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i){
            return i;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup){
            ViewHolder viewHolder;
            // 通用视图的优化代码。
            if(view == null){
                view = mInflator.inflate(R.layout.listitem_ble_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) view.findViewById(R.id.tv_device_name);
                viewHolder.connected = (ImageView) view.findViewById(R.id.iv_connected);
                viewHolder.connecting = (TextView) view.findViewById(R.id.tv_connecting);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            MyBleDevice device = mLeDevices.get(position);
            final String deviceName = device.getBluetoothDevice().getName();
            if(deviceName != null && deviceName.length() > 0){
                viewHolder.deviceName.setText(deviceName);
            }else{
                viewHolder.deviceName.setText("Unknown service");
            }
            if(device.getStatus() == 1){
                TextViewCompat.setTextAppearance(viewHolder.deviceName,R.style.ble_unconnect_tv);
                viewHolder.connecting.setVisibility(View.GONE);
                viewHolder.connected.setVisibility(View.GONE);
            }else if(device.getStatus() == 2){
                TextViewCompat.setTextAppearance(viewHolder.deviceName,R.style.ble_connect_tv);
                viewHolder.connecting.setVisibility(View.VISIBLE);
                viewHolder.connected.setVisibility(View.GONE);
            }else{
                TextViewCompat.setTextAppearance(viewHolder.deviceName,R.style.ble_connect_tv);
                viewHolder.connecting.setVisibility(View.GONE);
                viewHolder.connected.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    static class ViewHolder{
        TextView deviceName;
        TextView connecting;
        ImageView connected;
    }
}
