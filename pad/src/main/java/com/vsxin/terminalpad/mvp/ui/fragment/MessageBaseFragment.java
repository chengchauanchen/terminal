package com.vsxin.terminalpad.mvp.ui.fragment;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.BaseMessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IBaseMessageView;
import com.vsxin.terminalpad.mvp.ui.adapter.MessageAdapter;
import com.vsxin.terminalpad.utils.FragmentManage;

import java.util.List;
import java.util.Objects;

import cn.vsx.hamster.terminalsdk.model.TerminalMessage;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：
 * 修订历史：
 */
public abstract class MessageBaseFragment<V extends IBaseMessageView,P extends BaseMessagePresenter<V>> extends RefreshRecycleViewFragment<TerminalMessage, V, P> implements IBaseMessageView, View.OnClickListener{

    protected int userId;
    protected String userName;
    protected boolean isGroup;
    protected long uniqueNo;
    protected TextView tv_title;
    private ImageView iv_back;
    protected Button ptt;

    @Override
    protected void refresh(){
    }

    @Override
    protected void loadMore(){
    }

    @Override
    protected void initViews(View view){
        super.initViews(view);
        tv_title = view.findViewById(R.id.tv_chat_name);
        iv_back = view.findViewById(R.id.news_bar_return);
        ptt = view.findViewById(R.id.btn_ptt);
        iv_back.setOnClickListener(this);
        getPresenter().registReceiveHandler();
    }

    @Override
    protected void initData(){
        this.userId = getArguments().getInt("userId", userId);
        this.userName = getArguments().getString("userName", userName);
        this.uniqueNo = getArguments().getLong("uniqueNo", 0L);
        this.isGroup = getArguments().getBoolean("isGroup", true);

        tv_title.setText(userName);
        getPresenter().setAdapter(recyclerView, mSuperAdapter);
        getPresenter().getMessageFromServer(isGroup,uniqueNo,userId,10);
    }

    @Override
    protected BaseRecycleViewAdapter createAdapter(){
        mSuperAdapter = new MessageAdapter(getActivity());

        return mSuperAdapter;
    }

    @Override
    public void notifyDataSetChanged(List<TerminalMessage> terminalMessages){
        getActivity().runOnUiThread(() -> refreshOrLoadMore(terminalMessages));
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        getPresenter().unregistReceiveHandler();
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.news_bar_return:
                FragmentManage.finishFragment(Objects.requireNonNull(getActivity()));
            break;
        }
    }

    @Override
    public void setListSelection(int position){
        recyclerView.scrollToPosition(position);
    }

    @Override
    public void stopReFreshing(){
        refreshLayout.finishRefresh();
    }

    @Override
    public boolean isGroup(){
        return isGroup;
    }

    @Override
    public void notifyItemRangeInserted(int startPosition, int endPosition){
        mSuperAdapter.notifyItemRangeInserted(startPosition,endPosition);
    }

    @Override
    public void smoothScrollBy(int start, int end){
        recyclerView.smoothScrollBy(start,end);
    }

    @Override
    public void scrollMyListViewToBottom(){
        recyclerView.postDelayed(() -> recyclerView.scrollToPosition(mSuperAdapter.getItemCount() - 1), 10);
    }
}
