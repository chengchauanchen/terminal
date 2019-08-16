package com.vsxin.terminalpad.mvp.ui.fragment;

import android.util.Log;
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
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;
import com.vsxin.terminalpad.mvp.ui.widget.ProgressDialog;
import com.vsxin.terminalpad.utils.CallPhoneUtil;
import com.vsxin.terminalpad.utils.FragmentManage;

import java.io.File;
import java.util.List;
import java.util.Objects;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

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
    private ProgressDialog myProgressDialog;//加载数据的弹窗
    private MessageAdapter messageAdapter;

    private boolean isActivity;//是否是显示
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
        createProgressDialog();
        getPresenter().registReceiveHandler();
    }

    @Override
    protected void initData(){
        Log.e("MessageBaseFragment", "initData");
        this.userId = getArguments().getInt("userId", userId);
        this.userName = getArguments().getString("userName", userName);
        this.uniqueNo = getArguments().getLong("uniqueNo", 0L);
        this.isGroup = getArguments().getBoolean("isGroup", true);
        refreshLayout.setEnableLoadMore(false);
        tv_title.setText(userName);
        getPresenter().setAdapter(recyclerView, messageAdapter);
        getPresenter().getMessageFromServer(isGroup,uniqueNo,userId,10);
    }

    @Override
    protected BaseRecycleViewAdapter createAdapter(){
        messageAdapter = new MessageAdapter(getActivity());

        return messageAdapter;
    }

    @Override
    public void notifyDataSetChanged(List<TerminalMessage> terminalMessages){
        getActivity().runOnUiThread(() -> refreshOrLoadMore(terminalMessages));
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivity = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivity = false;
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
    public void setSmoothScrollToPosition(int position) {
        recyclerView.smoothScrollToPosition(position);
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
    public int getUserId(){
        return userId;
    }

    @Override
    public void notifyItemChanged(int position) {
        messageAdapter.notifyItemChanged(position);
    }

    @Override
    public void notifyDataSetChanged() {
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyItemRangeInserted(int startPosition, int endPosition){
        messageAdapter.notifyItemRangeInserted(startPosition,endPosition);
    }

    @Override
    public void smoothScrollBy(int start, int end){
        recyclerView.smoothScrollBy(start,end);
    }

    @Override
    public void scrollMyListViewToBottom(){
        recyclerView.postDelayed(() -> recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1), 10);
    }

    @Override
    public void refreshPersonContactsAdapter(int mposition,List<TerminalMessage> terminalMessageList, boolean isPlaying, boolean isSameItem) {
        if (messageAdapter!=null) {
            messageAdapter.refreshPersonContactsAdapter(mposition,terminalMessageList, isPlaying, isSameItem);
        }
    }

    @Override
    public void downloadProgress(float percent, TerminalMessage terminalMessage) {
        if (isActivity) {
            if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
                if (null != messageAdapter.loadingView) {
                    int percentInt = (int) (percent * 100);
                    if (percentInt >= 100) {
                        setViewVisibility(messageAdapter.loadingView, View.GONE);
                        messageAdapter.loadingView = null;
                    } else {
                        setViewVisibility(messageAdapter.loadingView, View.VISIBLE);
                        messageAdapter.loadingView.setProgerss(percentInt);
                    }
                }
            } else {
                if (messageAdapter.downloadProgressBar != null
                        && messageAdapter.download_tv_progressBars != null) {
                    int percentInt = (int) (percent * 100);
                    messageAdapter.downloadProgressBar.setProgress(percentInt);
                    setText(messageAdapter.download_tv_progressBars, percentInt + "%");

                    if (percentInt >= 100) {
                        setViewVisibility(messageAdapter.downloadProgressBar, View.GONE);
                        setViewVisibility(messageAdapter.download_tv_progressBars, View.GONE);
                        messageAdapter.downloadProgressBar = null;
                        messageAdapter.download_tv_progressBars = null;
                    }
                }
            }
        }
    }

    @Override
    public void downloadFinish(TerminalMessage terminalMessage, boolean success) {
        if (!isActivity) {
            return;
        }
        if (!success) {
            return;
        }
        if (isGroup) {//组消息
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode())//个人消息屏蔽
            {
                return;
            }
            if (terminalMessage.messageToId != userId)//其它组的屏蔽
            {
                return;
            }
        } else {//个人消息
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode())//个人消息屏蔽
            {
                return;
            }
            if (terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)) {//自己发的
                if (terminalMessage.messageToId != userId) { return; }
            } else {//接收的
                if (terminalMessage.messageFromId != userId)//其它人的屏蔽
                {
                    return;
                }
            }
        }

        if (terminalMessage.messageType == MessageType.LONG_TEXT.getCode()
                || terminalMessage.messageType == MessageType.HYPERLINK.getCode()) {
            getPresenter().replaceMessage(terminalMessage);
        }
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
            messageAdapter.isDownloadingPicture = false;
            getPresenter().replaceMessage(terminalMessage);
            //如果是原图下载完了就打开
            if (terminalMessage.messageBody.containsKey(JsonParam.ISMICROPICTURE) &&
                    !terminalMessage.messageBody.getBooleanValue(JsonParam.ISMICROPICTURE)) {
                messageAdapter.openPhotoAfterDownload(terminalMessage);
            }
        }

        if (terminalMessage.messageType == MessageType.FILE.getCode()) {
            messageAdapter.openFileAfterDownload(terminalMessage);
            messageAdapter.isDownloading = false;
            terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
            getPresenter().replaceMessage(terminalMessage);
        }
        if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
            messageAdapter.isDownloading = false;
            terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
            getPresenter().replaceMessage(terminalMessage);
        }
        if (terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()) {
            File file = new File(terminalMessage.messagePath);
            messageAdapter.openVideo(terminalMessage, file);
            messageAdapter.isDownloading = false;
            terminalMessage.messageBody.put(JsonParam.IS_DOWNLOADINF, false);
            getPresenter().replaceMessage(terminalMessage);
        }
    }
    protected void setText(TextView textView, String content) {
        if (textView != null) {
            textView.setText(content);
        }
    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 创建加载数据的ProgressDialog
     */
    private void createProgressDialog() {
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(this.getContext());
            myProgressDialog.setCancelable(true);
        }
    }

    /**
     * 显示加载数据的ProgressDialog
     */
    @Override
    public void showProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.setMsg(this.getString(R.string.get_data_now));
            myProgressDialog.show();
        }
    }

    /**
     * 隐藏加载数据的ProgressDialog
     */
    @Override
    public void dismissProgressDialog() {
        if (myProgressDialog != null) {
            myProgressDialog.dismiss();
        }
    }

    @Override
    public void showMsg(String msg) {
        ToastUtil.showToast(this.getContext(),msg);
    }

    @Override
    public void showMsg(int resouce) {
        ToastUtil.showToast(this.getContext(),getString(resouce));
    }

    @Override
    public void chooseDevicesDialog(int type, Account account) {
        new ChooseDevicesDialog(MessageBaseFragment.this.getContext(),type, account, (dialog, member) -> {
            switch (type){
                case ChooseDevicesDialog.TYPE_CALL_PRIVATE:
                    getPresenter().activeIndividualCall(member);
                    break;
                case ChooseDevicesDialog.TYPE_CALL_PHONE:
                    getPresenter().goToCall(member);
                    break;
                case ChooseDevicesDialog.TYPE_PULL_LIVE:
                    getPresenter().goToPullLive(member);
                    break;
                case ChooseDevicesDialog.TYPE_PUSH_LIVE:
                    getPresenter().goToPushLive(member);
                    break;
                    default: break;
            }
            dialog.dismiss();
        }).showDialog();
    }

    @Override
    public void callPhone(String phone) {
        getActivity().runOnUiThread(() -> CallPhoneUtil.callPhone(MessageBaseFragment.this.getActivity(), phone));
    }

    @Override
    public void goToVoIpActivity(Member member) {
        getActivity().runOnUiThread(() -> {
//            Intent intent = new Intent(MessageBaseFragment.this.getActivity(), VoipPhoneActivity.class);
//            intent.putExtra("member",member);
//            startActivity(intent);
        });
    }
}
