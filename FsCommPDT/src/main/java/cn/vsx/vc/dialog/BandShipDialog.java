package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.BindShipListAdapter;
import cn.vsx.vc.model.Boat;
import cn.vsx.vc.model.Car;
import cn.vsx.vc.model.HongHuBean;
import cn.vsx.vc.utils.HongHuUtils;
import ptt.terminalsdk.context.MyTerminalFactory;

public class BandShipDialog extends Dialog {


    private BandShipDialog.SureBindListener sureBindListener;

    protected Handler handler = new Handler(Looper.getMainLooper());
    private RecyclerView recyclerview;
    private BindShipListAdapter adapter;


    public BandShipDialog(Context context, BandShipDialog.SureBindListener sureBindListener) {
        super(context, R.style.dialog);
        this.sureBindListener = sureBindListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_band_ship_list);
        initView();
        init();
        initData();
    }

    private void initView() {
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        TextView tvCancel = findViewById(R.id.tv_cancel);
        TextView tvSure = findViewById(R.id.tv_sure);
        tvCancel.setOnClickListener(v -> dismiss());
        tvSure.setOnClickListener(v -> {
            if (sureBindListener != null) {
                Boat boat = getCheckCar(adapter.getDatas());
                sureBindListener.bind(boat);
            }
        });

    }

    private void initData() {
        getCarAndBoatData();
    }

    /**
     * 自定义setAdapter
     */
    private void setAdapter(List<Boat> datas) {
        if (adapter == null) {
            adapter = new BindShipListAdapter(getContext(), datas);
            recyclerview.setAdapter(adapter);
        } else {
            adapter.notifyData(datas);
        }
    }


    private Boat getCheckCar(List<Boat> datas){
        for (Boat boat : datas){
            if(boat.isCheck()){
                return boat;
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
        String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
        int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT, 0);
//        final String url = "http://"+ip+":"+port+"/save";
//        final String url = "http://192.168.1.20:9011/management/getCarrierMap";
        final String url = HongHuUtils.IP + "/management/getCarrierMap";
        Gson gson = new Gson();
        final String json = gson.toJson(paramsMap);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            String result = MyTerminalFactory.getSDK().getHttpClient().get(url, paramsMap);
            Log.i("BandDeviceDialog", result);
            try{
                List<HongHuBean> hongHuBeans = gson.fromJson(result, new TypeToken<List<HongHuBean>>() {}.getType());
                List<Boat> boatList = getBoatList(hongHuBeans);
                handler.post(() -> {
                    setAdapter(boatList);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    /**
     * 获取所有船
     * @param hongHuBeans
     * @return
     */
    private List<Boat>  getBoatList(List<HongHuBean> hongHuBeans){
        List<Boat> boats = new ArrayList<>();
        for (HongHuBean hongHuBean : hongHuBeans){
            List<Boat> ship = hongHuBean.getShip();
            if(ship!=null && ship.size()>0){
                boats.addAll(ship);
            }
        }
        return boats;
    }


    public interface SureBindListener {
        void bind(Boat boat);
    }
}
