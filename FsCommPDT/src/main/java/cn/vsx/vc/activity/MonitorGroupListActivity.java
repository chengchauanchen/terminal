package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRemoveMonitorGroupListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupListHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
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

    public static final String ACTION ="receive_remove_list" ;
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
        TerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveRemoveMonitorGroupListHandler);

    }

    @Override
    public void initData(){
        logger.info("initData");
        setData();
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        monitorGroupListAdapter = new MonitorGroupListAdapter(data,this);
        recyclerview.setAdapter(monitorGroupListAdapter);
    }

    @Override
    public void doOtherDestroy(){
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupListHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveMonitorGroupListHandler);
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
        logger.info("获取所有监听组列表-----groups="+groups);
        data.addAll(groups);


        //监听组
        List<Group> monitorGroup = TerminalFactory.getSDK().getConfigManager().getMonitorGroup();
        logger.info("获取监听组列表-----monitorGroup="+monitorGroup);
        //移除总列表
        List<Group> removelists = TerminalFactory.getSDK().getList(Params.TOTAL_REMOVE_GROUP_LIST, new ArrayList<Group>(), Group.class);
        logger.info("失效的监听组列表-----removelists="+removelists);

        //去除临时组监听列表中被取消监听的
        List<Integer> temp_monitor_remove_id_list = TerminalFactory.getSDK().getList(Params.TEMP_MONITOR_REMOVE_ID_LIST, new ArrayList<Integer>(), Integer.class);
        logger.info("临时组监听列表中被取消监听的监听组列表-----groups="+temp_monitor_remove_id_list);
        for (Integer grouid:temp_monitor_remove_id_list) {
//            removelists.remove(TerminalFactory.getSDK().getGroupByGroupNo(grouid));
            data.remove(TerminalFactory.getSDK().getGroupByGroupNo(grouid));
        }
        //失效列表可能包含当前监听列表的id  因为当前监听列表之前可能失效过  存储在本地的失效列表无法判断
        removelists.remove(monitorGroup);


        data.addAll(removelists);
        //所有需要移除的监听组列表中如果包含了此时的监听组，需要将这些正被监听的组剔除出来

    }

    /**
     * 移除监听组
     */
    ReceiveRemoveMonitorGroupListHandler receiveRemoveMonitorGroupListHandler=new ReceiveRemoveMonitorGroupListHandler() {
        @Override
        public void handler(List<Integer> removeScanGroupList) {
            logger.info("在监听组界面收到回调");
            mHandler.post(()->{
                StringBuilder builder=new StringBuilder();
                for (Integer groupnNo:removeScanGroupList) {
                    Group group = TerminalFactory.getSDK().getGroupByGroupNo(groupnNo);
                    builder.append(group.name).append(",");
                }
                Intent intent=new Intent(MonitorGroupListActivity.ACTION);
                intent.putExtra("removelist","管理员远程指定了您的监听组"+builder.toString()+"监听状态设置已失效");
                sendBroadcast(intent);
//                ToastUtils.showShort("管理员远程指定了您的监听组"+builder.toString()+"监听状态设置已失效");
            });

        }
    };
    /**
     * 强制切组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {

        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            if (!forceSwitchGroup) {
                return;
            }
            logger.info("TalkbackFragment收到强制切组： toGroupId：" + toGroupId);
            myHandler.post(() -> {
                setData();
                if(!isFinishing()){
                    monitorGroupListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}
