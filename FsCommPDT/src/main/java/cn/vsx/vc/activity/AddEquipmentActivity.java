package cn.vsx.vc.activity;

import android.text.TextUtils;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.dialog.BandDeviceDialog;
import cn.vsx.vc.dialog.BandDeviceListDialog;
import cn.vsx.vc.dialog.BandDeviceListDialog.SureBindListener;
import cn.vsx.vc.dialog.BandShipDialog;
import cn.vsx.vc.model.Car;
import cn.vsx.vc.model.Relationship;
import cn.vsx.vc.model.TerminalType;
import cn.vsx.vc.utils.HongHuUtils;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * @author qzw
 * <p>
 * 东湖赛事安保指挥系统，绑定设备
 */
public class AddEquipmentActivity extends BaseActivity {

    private ImageView iv_close;
    private ImageView iv_hand350m;
    private ImageView iv_lte;
    private ImageView iv_car;
    private ImageView iv_boat;
    private BandDeviceDialog deviceDialog;
    private BandShipDialog shipDialog;
    private BandDeviceListDialog deviceListDialog;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_add_equipment_layout;
    }

    @Override
    public void initView() {
        iv_close = findViewById(R.id.iv_close);//关闭
        iv_hand350m = findViewById(R.id.iv_hand350m);//手台
        iv_lte = findViewById(R.id.iv_lte);//lte
        iv_car = findViewById(R.id.iv_car);//车
        iv_boat = findViewById(R.id.iv_boat);//船

        iv_close.setOnClickListener(v -> {
            finish();
        });

        //手台
        iv_hand350m.setOnClickListener(v -> {
            ToastUtil.showToast(AddEquipmentActivity.this, "手台");
            showBandDialog(TerminalType.TERMINAL_PDT, "绑定电台");
        });

        //LTE
        iv_lte.setOnClickListener(v -> {
            ToastUtil.showToast(AddEquipmentActivity.this, "LTE");
            showBandDialog(TerminalType.TERMINAL_LTE, "绑定LTE");
        });

        //车
        iv_car.setOnClickListener(v -> {
            deviceListDialog = new BandDeviceListDialog(AddEquipmentActivity.this, new SureBindListener() {
                @Override
                public void bind(Car car) {
                    bandDevice(car.getVehicleType(), car.getUniqueNo(), new BandDeviceListener() {
                        @Override
                        public void result(String msg, int code) {
                            ToastUtil.showToast(AddEquipmentActivity.this, TextUtils.isEmpty(msg) ? "绑定成功" : msg);
                            if (code == 200) {
                                deviceListDialog.dismiss();
                            }
                        }
                    });
                }
            });
            deviceListDialog.show();
        });

        //船
        iv_boat.setOnClickListener(v -> {
            shipDialog = new BandShipDialog(AddEquipmentActivity.this, boat -> {
                bandDevice(boat.getVehicleType(), boat.getUniqueNo(), new BandDeviceListener() {
                    @Override
                    public void result(String msg, int code) {
                        ToastUtil.showToast(AddEquipmentActivity.this, TextUtils.isEmpty(msg) ? "绑定成功" : msg);
                        if (code == 200) {
                            shipDialog.dismiss();
                        }
                    }
                });
            });
            shipDialog.show();
        });
    }

    /**
     * 显示 绑定设备 手台、LTE
     *
     * @param title
     */
    private void showBandDialog(String equipmentType, String title) {
        deviceDialog = new BandDeviceDialog(this, title, (deviceNo) -> {
            if (TextUtils.isEmpty(deviceNo)) {
                ToastUtil.showToast(AddEquipmentActivity.this, "请输入设备编号");
                return;
            }
            bandDevice(equipmentType, deviceNo, new BandDeviceListener() {
                @Override
                public void result(String msg, int code) {
                    ToastUtil.showToast(AddEquipmentActivity.this, TextUtils.isEmpty(msg) ? "绑定成功" : msg);
                    if (code == 200) {
                        deviceDialog.dismiss();
                    }
                }
            });
        });
        deviceDialog.show();
    }


    @Override
    public void initListener() {

    }

    @Override
    public void initData() {
    }

    @Override
    public void doOtherDestroy() {

    }


    private void bandDevice(String equipmentType, String equipmentNo, BandDeviceListener bandDeviceListener) {
        Relationship relationship = new Relationship();
        relationship.setEquipmentType(equipmentType);
        relationship.setEquipmentNo(equipmentNo);
        relationship.setUniqueNo(MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L) + "");
        String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
        int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT, 0);
//			final String url = "http://192.168.1.174:6666/save";
//        final String url = "http://"+ip+":"+port+"/save";
        //final String url = "http://192.168.1.20:9011/management/bindingPhoneEquipment";
        final String url = HongHuUtils.IP + "/management/bindingPhoneEquipment";
        Gson gson = new Gson();
        final String json = gson.toJson(relationship);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String result = MyTerminalFactory.getSDK().getHttpClient().postJson(url, "bind=" + json);
            //{"STATUS_MSG":"绑定插入数据库成功","eWebCommonErrorCode":200}
            try {
                JSONObject jsonObject = parseObject(result);
                String status_msg = jsonObject.getString("STATUS_MSG");
                int code = jsonObject.getInteger("eWebCommonErrorCode");
                if (bandDeviceListener != null) {
                    bandDeviceListener.result(status_msg, code);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 绑定设备监听
     */
    interface BandDeviceListener {
        void result(String msg, int code);
    }

}
