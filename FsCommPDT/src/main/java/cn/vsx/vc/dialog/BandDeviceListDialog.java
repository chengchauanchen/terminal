package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.BandDevicesAdapter;
import cn.vsx.vc.adapter.ExPandableListViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.BandDeviceDialog.SureBindListener;
import cn.vsx.vc.model.Car;
import cn.vsx.vc.model.HongHuBean;
import cn.vsx.vc.model.Relationship;
import cn.vsx.vc.utils.HongHuUtils;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class BandDeviceListDialog extends Dialog {


    String dataJson = "[{\"car\":[{\"carrierOccupant\":\"吹笛派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":98,\"uniqueNo\":\"鄂A7616警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"吹笛派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":99,\"uniqueNo\":\"鄂A7621警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"吹笛派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":100,\"uniqueNo\":\"鄂A7609警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"吹笛派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":101,\"uniqueNo\":\"鄂A0790警\",\"updateTime\":1567699200000}],\"deptName\":\"吹笛派出所\"},{\"car\":[{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":93,\"uniqueNo\":\"鄂A7620警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":94,\"uniqueNo\":\"鄂A0792警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":95,\"uniqueNo\":\"鄂A0779警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":96,\"uniqueNo\":\"鄂A7606警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":97,\"uniqueNo\":\"鄂A8595警\",\"updateTime\":1567699200000}],\"deptName\":\"落雁派出所\"},{\"car\":[{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":93,\"uniqueNo\":\"鄂A7620警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":94,\"uniqueNo\":\"鄂A0792警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":95,\"uniqueNo\":\"鄂A0779警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":96,\"uniqueNo\":\"鄂A7606警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":97,\"uniqueNo\":\"鄂A8595警\",\"updateTime\":1567699200000}],\"deptName\":\"落雁派出所\"},{\"car\":[{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":93,\"uniqueNo\":\"鄂A7620警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":94,\"uniqueNo\":\"鄂A0792警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":95,\"uniqueNo\":\"鄂A0779警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":96,\"uniqueNo\":\"鄂A7606警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":97,\"uniqueNo\":\"鄂A8595警\",\"updateTime\":1567699200000}],\"deptName\":\"落雁派出所\"},{\"car\":[{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":93,\"uniqueNo\":\"鄂A7620警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":94,\"uniqueNo\":\"鄂A0792警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":95,\"uniqueNo\":\"鄂A0779警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":96,\"uniqueNo\":\"鄂A7606警\",\"updateTime\":1567699200000},{\"carrierOccupant\":\"落雁派出所\",\"carrierType\":14,\"createdTime\":1567699200000,\"id\":97,\"uniqueNo\":\"鄂A8595警\",\"updateTime\":1567699200000}],\"deptName\":\"落雁派出所\"}]";

    private ExpandableListView myExpandableListView;
    private ExPandableListViewAdapter adapter;

    private BandDeviceListDialog.SureBindListener sureBindListener;

    protected Handler handler = new Handler(Looper.getMainLooper());


    public BandDeviceListDialog(Context context,BandDeviceListDialog.SureBindListener sureBindListener) {
        super(context,R.style.dialog);
        this.sureBindListener = sureBindListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_band_device_list);
        initView();
        init();
        initData();
    }

    private void initView() {
        myExpandableListView = findViewById(R.id.elv_car_boat);
        myExpandableListView.setGroupIndicator(null);

        myExpandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            List<HongHuBean> datas = adapter.getDatas();
            Car car = datas.get(groupPosition).getCar().get(childPosition);
            boolean isCheck = car.isCheck();
            List<HongHuBean> hongHuBeans = setNoChecked(datas);
            hongHuBeans.get(groupPosition).getCar().get(childPosition).setCheck(!isCheck);
            setAdapter(hongHuBeans);
            return false;
        });

        TextView tvCancel = findViewById(R.id.tv_cancel);
        TextView tvSure = findViewById(R.id.tv_sure);
        tvCancel.setOnClickListener(v -> dismiss());
        tvSure.setOnClickListener(v -> {
            if (sureBindListener != null) {
                Car checkCar = getCheckCar(adapter.getDatas());
                sureBindListener.bind(checkCar);
            }
        });
        //32010000001320000114
    }

    private void initData() {
        getCarAndBoatData();
    }

    /**
     * 自定义setAdapter
     */
    private void setAdapter(List<HongHuBean> datas) {
        if (adapter == null) {
            adapter = new ExPandableListViewAdapter(getContext(), datas);
            myExpandableListView.setAdapter(adapter);
        } else {
            adapter.notifyData(datas);
        }
    }


    private Car getCheckCar(List<HongHuBean> datas){
        for (HongHuBean hongHuBean : datas){
            List<Car> cars = hongHuBean.getCar();
            for (Car car:cars){
                if(car.isCheck()){
                    return car;
                }
            }
        }
        return null;
    }

    private List<HongHuBean> setNoChecked(List<HongHuBean> hongHuBeans){
        List<HongHuBean> huBeans = new ArrayList<>();
        huBeans.addAll(hongHuBeans);
        for (HongHuBean hongHuBean : huBeans){
            List<Car> cars = hongHuBean.getCar();
            if(cars!=null){
                for (Car car:cars){
                    car.setCheck(false);
                }
            }
        }
        return huBeans;
    }


    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) (width * 0.9);
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }


    /**
     * 获取全部数据
     */
    private void getCarAndBoatData() {
        Map<String, String> paramsMap = new HashMap<>();
        //String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
        //int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT, 0);
//        final String url = "http://"+ip+":"+port+"/save";

        final String url = HongHuUtils.IP+ "/management/getCarrierMap";
        Gson gson = new Gson();
        final String json = gson.toJson(paramsMap);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String result = MyTerminalFactory.getSDK().getHttpClient().get(url, paramsMap);
            Log.i("BandDeviceDialog", result);
            try{
                List<HongHuBean> hongHuBeans = gson.fromJson(result, new TypeToken<List<HongHuBean>>() {}.getType());
                handler.post(() -> {
                    setAdapter(hongHuBeans);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }



    public interface SureBindListener {
        void bind(Car car);
    }
}
