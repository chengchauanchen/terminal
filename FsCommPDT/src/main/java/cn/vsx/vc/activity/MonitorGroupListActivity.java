package cn.vsx.vc.activity;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupListHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.MonitorGroupListAdapter;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/12
 * 描述：
 * 修订历史：
 */
public class MonitorGroupListActivity extends BaseActivity implements View.OnClickListener{

    private RecyclerView recyclerview;
    private ImageView barBack;
    private List<Group> data = new ArrayList<>();
    private MonitorGroupListAdapter monitorGroupListAdapter;
    private Handler mHandler = new Handler();

    @Override
    public int getLayoutResId(){
        return R.layout.activity_monitor_group_list;
    }

    @Override
    public void initView(){
        recyclerview = findViewById(R.id.recyclerview);
        barBack = findViewById(R.id.news_bar_back);
    }

    @Override
    public void initListener(){
        barBack.setOnClickListener(this);
        TerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupListHandler);
    }

    @Override
    public void initData(){
        setData();
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        monitorGroupListAdapter = new MonitorGroupListAdapter(data,this);
        recyclerview.setAdapter(monitorGroupListAdapter);
    }

    @Override
    public void doOtherDestroy(){
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);
    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.news_bar_back){
            finish();
        }
    }

    private ReceiveSetMonitorGroupListHandler receiveSetMonitorGroupListHandler = new ReceiveSetMonitorGroupListHandler(){
        @Override
        public void handler(int errorCode, String errorDesc){
            if(errorCode == BaseCommonCode.SUCCESS_CODE){
                mHandler.post(()-> {
                    setData();
                    if(!isFinishing()){
                        monitorGroupListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    private void setData(){
        data.clear();
        List<Group> groups = TerminalFactory.getSDK().getConfigManager().getAllListenerGroup();
        data.addAll(groups);
    }
}
