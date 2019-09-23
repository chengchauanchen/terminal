package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.BindEquipmentListAdapter;
import cn.vsx.vc.model.BindBean;
import cn.vsx.vc.receiveHandle.ReceiverBindDeviceHandler;
import cn.vsx.vc.receiveHandle.ReceiverUnBindDeviceHandler;
import cn.vsx.vc.utils.HongHuUtils;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * 东湖赛事安保指挥系统，已绑定设备列表
 */
public class BindEquipmentListActivity extends BaseActivity {

    public static String BIND_DATA = "bindData";

    private RecyclerView mRecyclerview;
    private List<BindBean> bindBeans = new ArrayList<>();
    private BindEquipmentListAdapter bindEquipmentListAdapter;
    protected Handler handler = new Handler(Looper.getMainLooper());
    private ImageView iv_close;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_bind_equipment_list_layout;
    }

    @Override
    public void initView() {
        mRecyclerview = findViewById(R.id.recyclerview);
        iv_close = findViewById(R.id.iv_close);
        iv_close.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void initListener() {
        //东湖 绑定设备
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverBindDeviceHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiverUnBindDeviceHandler);
    }

    @Override
    public void initData() {
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        bindEquipmentListAdapter = new BindEquipmentListAdapter(this, bindBeans);
        mRecyclerview.setAdapter(bindEquipmentListAdapter);

        List<BindBean> bindBeans = (List<BindBean>) getIntent().getSerializableExtra(BIND_DATA);
        if (bindBeans != null) {
            this.bindBeans.addAll(bindBeans);
            bindEquipmentListAdapter.notifyDataSetChanged();
        }else{
            HongHuUtils.getBindDevices();
        }
    }

    @Override
    public void doOtherDestroy() {
        //东湖 绑定设备
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverBindDeviceHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiverUnBindDeviceHandler);
    }

    private ReceiverBindDeviceHandler receiverBindDeviceHandler = deviceJson -> myHandler.post(() -> {
        logger.info(deviceJson);
        Gson gson = new Gson();
        List<BindBean> bindBeans = gson.fromJson(deviceJson, new TypeToken<List<BindBean>>() {
        }.getType());
        if (bindBeans != null) {
            this.bindBeans.clear();
            this.bindBeans.addAll(bindBeans);
            bindEquipmentListAdapter.notifyDataSetChanged();
            //getDevices();
        }
    });

    private ReceiverUnBindDeviceHandler receiverUnBindDeviceHandler = (id, position) -> myHandler.post(() -> {
        logger.info("解绑成功 id = " + id+",position:"+position);
        if (bindBeans != null) {
            this.bindBeans.remove(position);
            bindEquipmentListAdapter.notifyDataSetChanged();
        }
    });
}
