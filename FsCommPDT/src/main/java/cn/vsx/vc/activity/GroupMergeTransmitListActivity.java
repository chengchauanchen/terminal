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

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingHistoryListHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGroupLivingListHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GroupVideoLiveListAdapter;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class GroupMergeTransmitListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener, GroupVideoLiveListAdapter.OnItemClickListerner {


    @Bind(R.id.bar_title)
    TextView barTitle;
    @Bind(R.id.layout_srl)
    SwipeRefreshLayout layoutSrl;
    @Bind(R.id.contentView)
    RecyclerView contentView;
    private GroupVideoLiveListAdapter adapter;

    //是否是组内正在上报的列表
    private boolean isGroupVideoLiving;
    //组ID
    protected int groupId;
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
        isGroupVideoLiving = intent.getBooleanExtra(Constants.IS_GROUP_VIDEO_LIVING, false);
        groupId = getIntent().getIntExtra(Constants.GROUP_ID, 0);
        barTitle.setText(getResources().getString((isGroupVideoLiving) ? R.string.living_list : R.string.living_history));

        adapter = new GroupVideoLiveListAdapter(isGroupVideoLiving);
        adapter.setOnItemClickListerner(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contentView.setLayoutManager(linearLayoutManager);
        adapter.setOnLoadMoreListener(this, contentView);
        adapter.setEnableLoadMore(false);
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
        if (isGroupVideoLiving) {
            //正在上报
            getGroupLivingList();
        } else {
            //上报历史
            getGroupLivingHistoryList();
        }
    }

    /**
     * 获取组内正在上报列表
     */
    private void getGroupLivingList() {
        long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
        MyTerminalFactory.getSDK().getGroupManager().getGroupLivingList(String.valueOf(groupUniqueNo), false);
    }

    /**
     * 获取组内上报历史列表
     */
    private void getGroupLivingHistoryList() {
        long groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(groupId);
        MyTerminalFactory.getSDK().getGroupManager().getGroupHistoryLiveList(String.valueOf(groupUniqueNo), mPage, mPageSize);
    }

    /**
     * 获取组内正在直播列表
     */
    private ReceiveGetGroupLivingListHandler receiveGetGroupLivingListHandler = (memberList, resultCode, resultDesc, forNumber) -> {
        handler.post(() -> {
            if (!forNumber) {
                adapter.setEnableLoadMore(false);
                layoutSrl.setRefreshing(false);
                adapter.getData().clear();
                adapter.getData().addAll(memberList);
                adapter.notifyDataSetChanged();
                if (resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()) {
                    //正在上报的人
                } else {
                    //没有正在上报的人
                    ToastUtil.showToast(GroupMergeTransmitListActivity.this, resultDesc);
                }
            }
        });
    };

    /**
     * 获取组内直播历史列表
     */
    private ReceiveGetGroupLivingHistoryListHandler receiveGetGroupLivingHistoryListHandler = (memberList, resultCode, resultDesc) -> {
        handler.post(() -> {
            layoutSrl.setRefreshing(false);
            if (resultCode == BaseCommonCode.SUCCESS_CODE && !memberList.isEmpty()) {
                if (mPage == 1) {
                    adapter.getData().clear();
                    adapter.loadMoreEnd(true);
                    adapter.setEnableLoadMore(!(memberList.size() < mPageSize));
                    ToastUtil.showToast(GroupMergeTransmitListActivity.this, resultDesc);
                } else {
                    adapter.loadMoreComplete();
                    if (memberList.size() < mPageSize) {
                        adapter.loadMoreEnd();
                    } else {
                        adapter.setEnableLoadMore(true);
                    }
                }
                adapter.getData().addAll(memberList);
                adapter.notifyDataSetChanged();
            } else {
                if(mPage == 1){
                    adapter.getData().clear();
                    adapter.loadMoreEnd(true);
                    adapter.setEnableLoadMore(false);
                    adapter.notifyDataSetChanged();
                }else{
                    adapter.loadMoreComplete();
                    if(resultCode != BaseCommonCode.SUCCESS_CODE){
                        adapter.loadMoreFail();
                    }
                    adapter.setEnableLoadMore(true);
                }
                if (mPage > 1) {
                    mPage = mPage - 1;
                }
                ToastUtil.showToast(GroupMergeTransmitListActivity.this, resultDesc);
            }
        });
    };

    @Override
    public void goToWatch(Member item) {
        if(isGroupVideoLiving){
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("callId", "");
                paramsMap.put("sign", SignatureUtil.sign(paramsMap));
//             logger.info("查看视频播放是否结束url：" + url);
                String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
//             logger.info("查看视频播放是否结束结果：" + result);
                if (!Util.isEmpty(result)) {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    boolean living = jsonObject.getBoolean("living");
                    Long endChatTime = jsonObject.getLong("endChatTime");
                    if (living) {
                        handler.post(() -> {
//                            Intent intent = new Intent(GroupVideoLiveListActivity.this, PullLivingService.class);
//                            intent.putExtra(Constants.WATCH_TYPE, Constants.ACTIVE_WATCH);
//                            intent.putExtra(Constants.TERMINALMESSAGE, terminalMessage);
//                            startService(intent);
                        });

                    } else {
                        // TODO: 2018/8/7
//                        Intent intent = new Intent(GroupVideoLiveListActivity.this, LiveHistoryActivity.class);
//                        intent.putExtra("terminalMessage", terminalMessage);
////                      intent.putExtra("endChatTime",endChatTime);
//                        GroupVideoLiveListActivity.this.startActivity(intent);
                    }
                }
            });
        }else{
//            Intent intent = new Intent(GroupVideoLiveListActivity.this, LiveHistoryActivity.class);
//            intent.putExtra("terminalMessage", terminalMessage);
////          intent.putExtra("endChatTime",endChatTime);
//            GroupVideoLiveListActivity.this.startActivity(intent);
        }
    }

    @Override
    public void goToForward(Member item) {

    }
}
