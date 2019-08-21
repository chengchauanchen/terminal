package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.GroupVideoLiveListPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IGroupVideoLiveList;
import com.vsxin.terminalpad.mvp.ui.adapter.GroupVideoLiveListAdapter;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.LiveUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：消息记录界面
 * 修订历史：
 */
public class GroupVideoLiveListFragment extends RefreshRecycleViewFragment<TerminalMessage, IGroupVideoLiveList, GroupVideoLiveListPresenter> implements IGroupVideoLiveList, View.OnClickListener, GroupVideoLiveListAdapter.OnItemClickListerner {

    private GroupVideoLiveListAdapter adapter;
    @BindView(R.id.bar_title)
    TextView bar_title;

    //是否是组内正在上报的列表
    private boolean isGroupVideoLiving;
    //组ID
    protected int groupId;
    //当前页数
    private int mPage = 0;
    //每页显示条数
    private static final int mPageSize = 10;

    //记录转发的消息
    private TerminalMessage transponMessage;
    private static final int CODE_TRANSPON_REQUEST = 0x16;//转发


    private Handler mHandler = new Handler(Looper.getMainLooper());
    @Override
    protected int getLayoutResID(){
        return R.layout.fragment_group_video_live_list;
    }

    @Override
    protected void initViews(View view){
        super.initViews(view);
        getPresenter().registReceiveHandler();
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(false);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);
        adapter.setOnItemClickListerner(this);
        view.findViewById(R.id.news_bar_return).setOnClickListener(this);

    }

    @Override
    protected void initData(){
        super.initData();
        Bundle bundle = getArguments();
        isGroupVideoLiving = bundle.getBoolean(Constants.IS_GROUP_VIDEO_LIVING, false);
        groupId = bundle.getInt(Constants.GROUP_ID, 0);
        bar_title.setText(getResources().getString((isGroupVideoLiving) ? R.string.living_list : R.string.living_history));

        getPresenter().initData(groupId,isGroupVideoLiving,mPage,mPageSize);
    }

    @Override
    protected void refresh() {
        mPage = 0;
        recyclerView.scrollToPosition(0);
        getPresenter().initData(groupId,isGroupVideoLiving,mPage,mPageSize);
    }

    @Override
    protected void loadMore() {
        mPage++;
        getPresenter().initData(groupId,isGroupVideoLiving,mPage,mPageSize);
    }

    @Override
    protected BaseRecycleViewAdapter createAdapter() {
        adapter = new GroupVideoLiveListAdapter(getContext(),isGroupVideoLiving);
        return adapter;
    }

    @Override
    public GroupVideoLiveListPresenter createPresenter(){
        return new GroupVideoLiveListPresenter(getContext());
    }

    public static GroupVideoLiveListFragment newInstance(int userId, String userName, long uniqueNo){
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        args.putString("userName", userName);
        args.putBoolean("isGroup", true);
        args.putLong("uniqueNo",uniqueNo);
         GroupVideoLiveListFragment fragment = new GroupVideoLiveListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.news_bar_return:
                //返回
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void notifyDataSetChanged(List<TerminalMessage> list, boolean toTop) {
        getActivity().runOnUiThread(() -> {
            refreshOrLoadMore(list);
            if(toTop){
                recyclerView.scrollToPosition(0);
            }
        });
    }

    @Override
    public GroupVideoLiveListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void showMsg(String msg) {
        ToastUtil.showToast(getActivity(),msg);
    }

    @Override
    public void showMsg(int resouce) {
        ToastUtil.showToast(getActivity(),getString(resouce));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().unRegistReceiveHandler();
    }

    @Override
    public void getGroupLivingList(List<TerminalMessage> beanList, int resultCode, String resultDesc, boolean forNumber) {
        getActivity().runOnUiThread(() -> {
            if (!forNumber) {
                refreshLayout.setEnableLoadMore(false);
                notifyDataSetChanged(beanList,page == 0);
                if (resultCode == BaseCommonCode.SUCCESS_CODE && !beanList.isEmpty()) {
                    //正在上报的人
                } else {
                    //没有正在上报的人
                    showMsg(resultDesc);
                }
            }
        });
    }

    @Override
    public void getGroupLivingHistoryList(List<TerminalMessage> beanList, int resultCode, String resultDesc) {
        getActivity().runOnUiThread(() -> {
            refreshLayout.setEnableLoadMore(true);
            notifyDataSetChanged(beanList,page == 0);
            if (resultCode == BaseCommonCode.SUCCESS_CODE && !beanList.isEmpty()) {
            } else {
                showMsg(resultDesc);
            }
        });
    }

    @Override
    public void goToForward(TerminalMessage item) {

    }

    @Override
    public void goToWatch(TerminalMessage item) {
        if (isGroupVideoLiving) {
            String callId = "";
            if(item!=null&&item.messageBody!=null&&!TextUtils.isEmpty(item.messageBody.toJSONString())&&item.messageBody.containsKey(JsonParam.CALLID)){
                callId = item.messageBody.getString(JsonParam.CALLID);
            }else{
                return;
            }

            String finalCallId = callId;
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("callId", finalCallId);
                paramsMap.put("sign", SignatureUtil.sign(paramsMap));
//             logger.info("查看视频播放是否结束url：" + url);
                String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
//             logger.info("查看视频播放是否结束结果：" + result);
                if (!Util.isEmpty(result)) {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    boolean living = jsonObject.getBoolean("living");
                    Long endChatTime = jsonObject.getLong("endChatTime");
                    if (living) {
                        int resultCode = LiveUtil.requestToWatchLiving(item);
                        if(resultCode !=0){
                            ToastUtil.livingFailToast(GroupVideoLiveListFragment.this.getContext(), resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
                        }
                    } else {
                        LiveUtil.getHistoryLiveUrls(item);
                    }
                }
            });
        } else {
            LiveUtil.getHistoryLiveUrls(item);
        }
    }


}
