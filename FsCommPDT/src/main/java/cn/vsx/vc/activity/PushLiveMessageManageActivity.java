package cn.vsx.vc.activity;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.PushLiveListAdapter;
import cn.vsx.vc.view.PullToRefreshLayout;
import cn.vsx.vc.view.PullableListView;
import cn.vsx.vc.view.VolumeViewLayout;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 *  图像管理助手界面
 * Created by gt358 on 2017/9/28.
 */

public class PushLiveMessageManageActivity extends BaseActivity{

    @Bind(R.id.pull_refresh_layout)
    PullToRefreshLayout pull_refresh_layout;
    @Bind(R.id.pl_video_send)
    PullableListView pl_video_send;
    @Bind(R.id.volume_layout)
    VolumeViewLayout volumeViewLayout;
    private PushLiveListAdapter mPushLiveListAdapter;

    private Handler mHandler = new Handler();
    private List<TerminalMessage> mLiveMessageList = new ArrayList<>();
    @Override
    public int getLayoutResId() {
        return R.layout.activity_live_message_manage;
    }

    @Override
    public void initView() {
        mPushLiveListAdapter = new PushLiveListAdapter(this, mLiveMessageList);
        pl_video_send.setAdapter(mPushLiveListAdapter);
        pl_video_send.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent intent = new Intent(PushLiveMessageManageActivity.this,LiveHistoryActivity.class);
                Log.e("PushLiveMessageManageAc", "position:" + position+"----id:"+id);
                intent.putExtra("terminalMessage",mLiveMessageList.get(position));
                PushLiveMessageManageActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
    }

    @Override
    public void initData() {
        mLiveMessageList.addAll(MyTerminalFactory.getSDK().getTerminalMessageManager().
                getVideoLiveMessageRecord(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0), 0));
        Collections.sort(mLiveMessageList);
        filtrateTermessage();
        mPushLiveListAdapter.notifyDataSetChanged();
        if(mLiveMessageList.size()>0)
            pl_video_send.setSelection(mLiveMessageList.size()-1);
        pull_refresh_layout.setOnRefreshListener(mOnRefreshListener);
    }

    /***  筛选消息 ***/
    private void filtrateTermessage () {
        List<TerminalMessage> list = new ArrayList<>();
        list.addAll(mLiveMessageList);
        mLiveMessageList.clear();
        for(TerminalMessage terminalMessage : list) {
            if(terminalMessage.messageBody.containsKey(JsonParam.REMARK) &&
                    terminalMessage.messageBody.getInteger(JsonParam.REMARK) == Remark.STOP_ASK_VIDEO_LIVE
                    ||terminalMessage.resultCode !=0) {
            }else {
                mLiveMessageList.add(terminalMessage);
            }
        }
    }

    @Override
    public void doOtherDestroy() {
        if (volumeViewLayout!= null){
            volumeViewLayout.unRegistLintener();
        }
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
    }

    /**  接收实时消息  **/
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = new ReceiveNotifyDataMessageHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            if(terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
                String liverId_Name = terminalMessage.messageBody.getString(JsonParam.LIVER);
                int liverId = Integer.parseInt(liverId_Name.substring(0, liverId_Name.indexOf("_")));
                if(liverId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLiveMessageList.add(terminalMessage);
                            mPushLiveListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }
    };

    /**  下拉刷新 和 上拉加载更多**/
    private PullToRefreshLayout.OnRefreshListener mOnRefreshListener = new PullToRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh(PullToRefreshLayout pullToRefreshLayout) {
            // 下拉刷新操作
            if(mLiveMessageList.size() <= 0) {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
                return;
            }
            List<TerminalMessage> groupMessageRecord1 = MyTerminalFactory.getSDK().getTerminalMessageManager().
                    getVideoLiveMessageRecord(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0), mLiveMessageList.get(0).sendTime - 1);
            if(groupMessageRecord1 != null && groupMessageRecord1.size() >= 0) {
                logger.info("会话列表刷新成功");
                Collections.sort(groupMessageRecord1);
                List<TerminalMessage> groupMessageRecord2 = new ArrayList<>();
                groupMessageRecord2.addAll(mLiveMessageList);
                mLiveMessageList.clear();
                mLiveMessageList.addAll(groupMessageRecord1);
                mLiveMessageList.addAll(groupMessageRecord2);
                filtrateTermessage();
                mPushLiveListAdapter.notifyDataSetChanged();
                pl_video_send.setSelection(groupMessageRecord1.size());
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
            }
            else {
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
            }
        }

        @Override
        public void onLoadMore(PullToRefreshLayout pullToRefreshLayout) {

        }
    };

    @OnClick({R.id.iv_back})
    public void onClick (View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
