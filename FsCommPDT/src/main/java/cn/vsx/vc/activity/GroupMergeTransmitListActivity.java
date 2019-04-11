package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingHistoryListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingListHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupMergeTransmitListAdapter;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

public class GroupMergeTransmitListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {


    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.layout_srl)
    SwipeRefreshLayout layoutSrl;
    @Bind(R.id.contentView)
    RecyclerView contentView;
    private GroupMergeTransmitListAdapter adapter;

    private TerminalMessage terminalMessage;
    protected List<TerminalMessage> chatMessageList = new ArrayList<>();

    //当前页数
    private int mPage = 1;
    //每页显示条数
    private static final int mPageSize = 10;

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_group_video_live_list;
    }

    @Override
    public void initView() {
        Intent intent = getIntent();
        terminalMessage = (TerminalMessage) intent.getSerializableExtra(Constants.TERMINALMESSAGE);
        if(terminalMessage == null || (terminalMessage != null && terminalMessage.messageBody == null)){
            finish();
            return;
        }

        JSONObject jsonObject = terminalMessage.messageBody;
        barTitle.setText(jsonObject.containsKey(JsonParam.CONTENT)?jsonObject.getString(JsonParam.CONTENT):"消息记录");

        adapter = new GroupMergeTransmitListAdapter(chatMessageList,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contentView.setLayoutManager(linearLayoutManager);
        contentView.setAdapter(adapter);
        layoutSrl.setColorSchemeResources(R.color.colorPrimary);
        layoutSrl.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        layoutSrl.setOnRefreshListener(this);

    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingListHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGetGroupLivingHistoryListHandler);
    }

    @Override
    public void initData() {
        loadData(true);

    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingListHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGetGroupLivingHistoryListHandler);
        handler.removeCallbacksAndMessages(null);
    }

    @OnClick({R.id.news_bar_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.news_bar_back:
                finish();
                break;
        }
    }


    @Override
    public void onRefresh() {
        mPage = 1;
        contentView.scrollToPosition(0);
        loadData(true);
    }

    @Override
    public void onLoadMoreRequested() {
        mPage++;
        loadData(false);
    }

    /**
     * 加载数据
     *
     * @param isRefresh
     */
    private void loadData(boolean isRefresh) {
//        if (isGroupVideoLiving) {
//            //正在上报
//            getGroupLivingList();
//        } else {
//            //上报历史
//            getGroupLivingHistoryList();
//        }
    }

    /**
     * 获取组内正在上报列表
     */
    private void getGroupLivingList() {
//        long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
//        MyTerminalFactory.getSDK().getGroupManager().getGroupLivingList(String.valueOf(groupUniqueNo), false);
    }

    /**
     * 获取组内上报历史列表
     */
    private void getGroupLivingHistoryList() {
//        long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
//        MyTerminalFactory.getSDK().getGroupManager().getGroupHistoryLiveList(String.valueOf(groupUniqueNo), mPage, mPageSize);
    }

    /**
     * 获取组内正在直播列表
     */
    private ReceiveGetGroupLivingListHandler receiveGetGroupLivingListHandler = (memberList, resultCode, resultDesc, forNumber) -> {
        handler.post(() -> {
//            if (!forNumber) {
//                adapter.setEnableLoadMore(false);
//                layoutSrl.setRefreshing(false);
//                adapter.getData().clear();
//                adapter.getData().addAll(memberList);
//                adapter.notifyDataSetChanged();
//                if (resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()) {
//                    //正在上报的人
//                } else {
//                    //没有正在上报的人
//                    ToastUtil.showToast(GroupMergeTransmitListActivity.this, resultDesc);
//                }
//            }
        });
    };

    /**
     * 获取组内直播历史列表
     */
    private ReceiveGetGroupLivingHistoryListHandler receiveGetGroupLivingHistoryListHandler = (memberList, resultCode, resultDesc) -> {
        handler.post(() -> {
//            layoutSrl.setRefreshing(false);
//            if (resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()) {
//                if (mPage == 1) {
//                    adapter.getData().clear();
//                    adapter.loadMoreEnd(true);
//                    adapter.setEnableLoadMore(!(memberList.size() < mPageSize));
//                    ToastUtil.showToast(GroupMergeTransmitListActivity.this, resultDesc);
//                } else {
//                    adapter.loadMoreComplete();
//                    if (memberList.size() < mPageSize) {
//                        adapter.loadMoreEnd();
//                    } else {
//                        adapter.setEnableLoadMore(true);
//                    }
//                }
//                adapter.getData().addAll(memberList);
//                adapter.notifyDataSetChanged();
//            } else {
//                if(mPage == 1){
//                    adapter.getData().clear();
//                    adapter.loadMoreEnd(true);
//                    adapter.setEnableLoadMore(false);
//                    adapter.notifyDataSetChanged();
//                }else{
//                    adapter.loadMoreComplete();
//                    if(resultCode != BaseCommonCode.SUCCESS_CODE){
//                        adapter.loadMoreFail();
//                    }
//                    adapter.setEnableLoadMore(true);
//                }
//                if (mPage > 1) {
//                    mPage = mPage - 1;
//                }
//                ToastUtil.showToast(GroupMergeTransmitListActivity.this, resultDesc);
//            }
        });
    };
}
