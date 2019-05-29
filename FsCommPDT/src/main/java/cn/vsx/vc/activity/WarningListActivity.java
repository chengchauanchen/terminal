package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.WarningRecord;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetWarningMessageDetailHandler;
import cn.vsx.hamster.terminalsdk.tools.DateUtils;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.WarningListAdapter;
import cn.vsx.vc.receiveHandle.ReceiveWarningReadCountChangedHandler;
import cn.vsx.vc.view.MyLinearLayoutManager;
import ptt.terminalsdk.context.MyTerminalFactory;

public class WarningListActivity extends BaseActivity{

    private ImageView mIvReturn;
    private RecyclerView mRecyclerview;
    private WarningListAdapter warningListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MyLinearLayoutManager layoutManager;
    private List<WarningRecord>warningRecords = new ArrayList<>();

    private final static int PULL_TO_REFRESH = 1;
    private final static int LOAD_MORE = 2;
    private final static int IDLE = 0;
    private int currentState = IDLE;
    private int page = 1;
    private static final int PAGE_SIZE = 100;
//    private boolean firstLaodData = true;
    private boolean loadMoreEnable = false;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public int getLayoutResId(){
        return R.layout.activity_warning_list;
    }

    @Override
    public void initView(){
        mIvReturn = findViewById(R.id.iv_return);
        mRecyclerview = findViewById(R.id.recyclerview);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    public void initListener(){
        mIvReturn.setOnClickListener(returnOnClickListener);
        MyTerminalFactory.getSDK().registReceiveHandler(getWarningMessageDetailHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveWarningReadCountChangedHandler);

    }

    private View.OnClickListener returnOnClickListener = v -> finish();

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener(){
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState){
            super.onScrollStateChanged(recyclerView, newState);
        }
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(currentState == IDLE && loadMoreEnable&& checkIfLoadMore()){
                currentState = LOAD_MORE;
                mSwipeRefreshLayout.setEnabled(false);
                loadMoreData();
            }
        }
    };

    private boolean checkIfLoadMore(){
        int position = layoutManager.findLastVisibleItemPosition();
        int totalCount = layoutManager.getItemCount();
        return totalCount- position < 2;
    }

    private void loadMoreData(){
        showFootView();
        page ++;
        getData();
    }

    private void showFootView(){
        warningListAdapter.setLoading(true);
        mHandler.post(()->{
            warningListAdapter.notifyItemChanged(warningListAdapter.getRealLastPosition());
        });
//        warningListAdapter.notifyDataSetChanged();
    }

    private void hideFootView(boolean hasData){
        warningListAdapter.setLoading(false);
        warningListAdapter.setHasMore(hasData);
        Collections.sort(warningRecords);
        warningListAdapter.notifyDataSetChanged();
    }

    private SwipeRefreshLayout.OnRefreshListener onRefreshListener = () -> {
        if(currentState != IDLE){
            return;
        }
        currentState = PULL_TO_REFRESH;
        warningRecords.clear();
        mSwipeRefreshLayout.setRefreshing(true);
        warningListAdapter.setHasMore(false);
        page = 1;
        getData();
    };

    @Override
    public void initData(){
        page = 1;
        warningListAdapter = new WarningListAdapter(this,warningRecords);
        layoutManager = new MyLinearLayoutManager(this);
        mRecyclerview.setLayoutManager(layoutManager);
        mRecyclerview.setAdapter(warningListAdapter);
        mRecyclerview.setItemAnimator(new DefaultItemAnimator());
        mRecyclerview.addOnScrollListener(onScrollListener);
        mSwipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        warningListAdapter.setItemClickListener(warningRecord -> {
            if(warningRecord!=null){
                //修改未读的状态
                if(warningRecord.getUnRead() == 0){
                    //改变页面
                    warningRecord.setUnRead(1);
                    warningListAdapter.notifyDataSetChanged();
                    //修改数据库中的未读状态
                    MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                        MyTerminalFactory.getSDK().getSQLiteDBManager().updateWarningRecord(warningRecord);
                    });
                }
                Intent intent = new Intent(WarningListActivity.this,WarningMessageDetailActivity.class);
                intent.putExtra("warningRecord",warningRecord);
                startActivity(intent);
            }
        });
        getData();
        warningListAdapter.notifyDataSetChanged();
    }

    private void getData(){
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            Map<String,Integer> warningRecordList = MyTerminalFactory.getSDK().getSQLiteDBManager().getWarningRecordsNo(page,PAGE_SIZE);
            List<WarningRecord> warningRecordss = MyTerminalFactory.getSDK().getTerminalMessageManager().getWarningRecords(warningRecordList);
            mHandler.post(() -> {
                loadMoreEnable = warningRecordList.size() >= PAGE_SIZE;
                if(warningRecordss.isEmpty()){
                    refreshComplete(false);
                }else {
                    this.warningRecords.addAll(warningRecordss);
                    refreshComplete(true);
                }
            });
        });
    }

    private void refreshComplete(boolean hasData){
        if(currentState == PULL_TO_REFRESH){
            mSwipeRefreshLayout.setRefreshing(false);
        }else if(currentState == LOAD_MORE){
            mSwipeRefreshLayout.setEnabled(true);
        }
        currentState = IDLE;
        hideFootView(hasData);
    }

    @Override
    public void doOtherDestroy(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(getWarningMessageDetailHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveWarningReadCountChangedHandler);
        warningListAdapter.removeMessage();
    }

    private GetWarningMessageDetailHandler getWarningMessageDetailHandler = (terminalMessage,newMessage) -> {
        WarningRecord warningRecord = new WarningRecord();
        warningRecord.setLevels(terminalMessage.messageBody.getIntValue(JsonParam.LEVELS));
        warningRecord.setStatus(terminalMessage.messageBody.getIntValue(JsonParam.STATUS));
        warningRecord.setAlarmNo(terminalMessage.messageBody.getString(JsonParam.ALARM_NO));
        warningRecord.setAperson(terminalMessage.messageBody.getString(JsonParam.APERSON));
        warningRecord.setApersonPhone(terminalMessage.messageBody.getString(JsonParam.APERSON_PHONE));
        warningRecord.setRecvperson(terminalMessage.messageBody.getString(JsonParam.RECVPERSON));
        warningRecord.setRecvphone(terminalMessage.messageBody.getString(JsonParam.RECVPHONE));
        warningRecord.setAlarmTime(terminalMessage.messageBody.getString(JsonParam.ALARM_TIME));
        warningRecord.setAddress(terminalMessage.messageBody.getString(JsonParam.ADDRESS));
        warningRecord.setSummary(terminalMessage.messageBody.getString(JsonParam.SUMMARY));
        warningRecord.setDate(DateUtils.getDate(warningRecord.getAlarmTime()));
        warningRecord.setUnRead(terminalMessage.messageBody.getIntValue(JsonParam.ALARM_UNREAD));
        if(!warningRecords.contains(warningRecord)){
            warningRecords.add(warningRecord);
            Collections.sort(warningRecords);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    warningListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    /**
     * 收到点击警情通知时，刷新数据
     */
    private ReceiveWarningReadCountChangedHandler receiveWarningReadCountChangedHandler = () -> {
        if(currentState != IDLE){
            return;
        }
        currentState = PULL_TO_REFRESH;
        warningRecords.clear();
        page = 1;
        getData();
    };

}
