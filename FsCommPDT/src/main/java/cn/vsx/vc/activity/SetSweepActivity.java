package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveSetScanGroupListResultHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupSweepAdapter;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;


/**
 * Created by Administrator on 2017/3/16 0016.
 */
public class SetSweepActivity extends BaseActivity {
    @Bind(R.id.group_num)
    TextView groupNum;
    @Bind(R.id.group_list)
    ListView groupList;
    private Handler myHandler = new Handler();
    private GroupSweepAdapter adapter;
    private List<Integer> groupSweeps = new ArrayList<>();

    @Override
    public int getLayoutResId() {
        return R.layout.activity_set_sweep;
    }

    @Override
    public void initView() {


    }

    @Override
    public void initListener() {
        TerminalFactory.getSDK().registReceiveHandler(mReceiveSetScanGroupListResultHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
    }

    @Override
    public void initData() {
        groupSweeps.addAll(MyTerminalFactory.getSDK().getConfigManager().loadScanGroup());
        logger.info("扫描组列表："+groupSweeps);
        adapter = new GroupSweepAdapter(this, groupSweeps);
        groupList.setAdapter(adapter);
    }

    @Override
    public void doOtherDestroy() {
        TerminalFactory.getSDK().unregistReceiveHandler(mReceiveSetScanGroupListResultHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
    }

    @OnClick({R.id.news_bar_back, R.id.right_add})
    public void onClick(View view) {
            switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
            case R.id.right_add:
                if(groupSweeps.size()>=10){
                    ToastUtil.showToast(SetSweepActivity.this,"扫描组不能超过10个");
                    return;
                }
                Intent intent = new Intent(this, ChangeGroupActivity.class);
//                intent.putExtra("INTENTFROM", 2);
                startActivity(intent);

                break;
        }
    }

    //响应扫描组设置
    private ReceiveSetScanGroupListResultHandler mReceiveSetScanGroupListResultHandler=new ReceiveSetScanGroupListResultHandler(){

        @Override
        public void handler(final List<Integer> scanGroups, final int errorCode, final String errorDesc) {
            myHandler.post(() -> {
                logger.info("ReceiveSetScanGroupListResultHandler："+errorDesc+"======="+scanGroups);
                if(errorCode== BaseCommonCode.SUCCESS_CODE){
                    groupSweeps.clear();
                    groupSweeps.addAll(scanGroups);
                    adapter.notifyDataSetChanged();
                    ToastUtil.toast(SetSweepActivity.this,"扫描组删除成功");
                }else {
                    ToastUtil.toast(SetSweepActivity.this,"删除失败,"+errorDesc);
                }
            });

        }
    };

    /**更新配置信息*/
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {//更新组扫描的开关，主组名字
            myHandler.post(() -> {
                groupSweeps.clear();
                groupSweeps.addAll(TerminalFactory.getSDK().getConfigManager().getScanGroups());
                adapter.notifyDataSetChanged();
            });
        }
    };


}
