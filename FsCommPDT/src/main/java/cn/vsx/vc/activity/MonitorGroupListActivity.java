package cn.vsx.vc.activity;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetMonitorGroupViewHandler;
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
        TerminalFactory.getSDK().registReceiveHandler(receiveSetMonitorGroupViewHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);

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
        TerminalFactory.getSDK().unregistReceiveHandler(receiveSetMonitorGroupViewHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        if(id == R.id.news_bar_back){
            finish();
        }
    }

    /**
     * 设置监听组之后更新UI
     */
    private ReceiveSetMonitorGroupViewHandler receiveSetMonitorGroupViewHandler = new ReceiveSetMonitorGroupViewHandler(){
        @Override
        public void handler(){
            setData();
            mHandler.post(()-> {
                if(monitorGroupListAdapter!=null&&!isFinishing()){
                    monitorGroupListAdapter.notifyDataSetChanged();
                }
            });
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
        //失效列表可能包含当前监听列表的id
        for (Group group:monitorGroup){
            if(group!=null){
                removelists.remove(group);
            }
        }
        TerminalFactory.getSDK().putList(Params.TOTAL_REMOVE_GROUP_LIST,removelists);
        data.addAll(removelists);
    }

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
            setData();
            myHandler.post(() -> {
                if(monitorGroupListAdapter!=null&&!isFinishing()){
                    monitorGroupListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}
