package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.fragment.RefreshRecycleViewFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.contract.presenter.BaseMessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IBaseMessageView;
import com.vsxin.terminalpad.mvp.ui.adapter.MessageAdapter;
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;
import com.vsxin.terminalpad.mvp.ui.widget.ProgressDialog;
import com.vsxin.terminalpad.utils.CallPhoneUtil;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.FragmentManage;
import com.vsxin.terminalpad.utils.LiveUtil;
import com.zectec.imageandfileselector.bean.FileInfo;
import com.zectec.imageandfileselector.fragment.ImagePreviewFragment;

import org.apache.http.util.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.individualcall.IndividualCallState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePlayingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
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
    private boolean isFrist = true;
//    FrameLayout fl_fragment_container;

    private boolean isActivity;//是否是显示
    @Override
    protected void refresh(){
        isRefrash = false;
        getPresenter().getMessageFromServer(isGroup,uniqueNo,userId,10,true);
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
        isFrist = true;
        refreshLayout.setEnableLoadMore(false);
        tv_title.setText(userName);
        messageAdapter.setIsGroup(isGroup);
//        messageAdapter.setFragment_contener(fl_fragment_container);
        getPresenter().setAdapter(recyclerView, messageAdapter);
        getPresenter().getMessageFromServer(isGroup,uniqueNo,userId,10,false);
    }

    @Override
    protected BaseRecycleViewAdapter createAdapter(){
        messageAdapter = new MessageAdapter(getActivity());

        return messageAdapter;
    }

    @Override
    public void notifyDataSetChanged(List<TerminalMessage> terminalMessages){
        getActivity().runOnUiThread(() -> {
            isRefrash = true;
            refreshOrLoadMore(terminalMessages);
//            if(isFrist){
//                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
//            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivity = true;
        getPresenter().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivity = false;
        getPresenter().onPause();
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
                //FragmentManage.finishFragment(Objects.requireNonNull(getActivity()));
                FragmentManage.startVsxFragment(getActivity());
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

    @Override
    public void chatListItemClick(TerminalMessage terminalMessage, boolean isReceiver) {
        /**  进入定位界面 **/
        if (terminalMessage.messageType == MessageType.POSITION.getCode()) {
            if (terminalMessage.messageBody.containsKey(JsonParam.LONGITUDE) &&
                    terminalMessage.messageBody.containsKey(JsonParam.LATITUDE)) {
//                setViewVisibility(fl_fragment_container, View.VISIBLE);
                double longitude = terminalMessage.messageBody.getDouble(JsonParam.LONGITUDE);
                double altitude = terminalMessage.messageBody.getDouble(JsonParam.LATITUDE);
                //http://192.168.1.96:7007/mapLocationl.html?lng=117.68&lat=39.456
                String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "") + "?lng=" + longitude + "&lat=" + altitude;
                if (TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL, ""))) {
                    showMsg(R.string.text_please_go_to_the_management_background_configuration_location_url);
                } else {
                    LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
//                    locationFragment.setFragment_contener(fl_fragment_container);
//                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commit();

                    FragmentManage.startFragment(getActivity(), locationFragment);
                }
            } else {
//                setViewVisibility(fl_fragment_container, View.VISIBLE);
                String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "");
                if (TextUtils.isEmpty(TerminalFactory.getSDK().getParam(Params.LOCATION_URL, ""))) {
                    showMsg(R.string.text_please_go_to_the_management_background_configuration_location_url);
                } else {
                    LocationFragment locationFragment = LocationFragment.getInstance(url, "", true);
//                    locationFragment.setFragment_contener(fl_fragment_container);
//                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, locationFragment).commit();

                    FragmentManage.startFragment(getActivity(), locationFragment);
                }
            }
        }

        /**  进入图片预览界面  **/
        if (terminalMessage.messageType == MessageType.PICTURE.getCode()) {
//            setViewVisibility(fl_fragment_container, View.VISIBLE);
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFilePath(terminalMessage.messagePath);
            List<FileInfo> images = new ArrayList<>();
            images.add(fileInfo);
//            getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, new ImagePreviewFragment(images)).commit();

            FragmentManage.startFragment(getActivity(), new ImagePreviewFragment(images));
        }

        /**  上报图像  **/
        if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
            //如果在视频上报、观看、个呼中不允许观看
            if (PadApplication.getPadApplication().getVideoLivePushingState() != VideoLivePushingState.IDLE) {
                ToastUtil.showToast(R.string.text_pushing_cannot_pull);
                return;
            } else if (PadApplication.getPadApplication().getVideoLivePlayingState() != VideoLivePlayingState.IDLE) {
                ToastUtil.showToast(R.string.text_pulling_cannot_pull);
                return;
            } else if (PadApplication.getPadApplication().getIndividualState() != IndividualCallState.IDLE) {
                ToastUtil.showToast(R.string.text_calling_cannot_pull);
                return;
            }
            //先请求看视频上报是否已经结束
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
                int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
                String url = "http://" + serverIp + ":" + serverPort + "/file/download/isLiving";
                Map<String, String> paramsMap = new HashMap<>();
                paramsMap.put("callId", terminalMessage.messageBody.getString(JsonParam.CALLID));
                paramsMap.put("sign", SignatureUtil.sign(paramsMap));
                getLogger().info("查看视频播放是否结束url：" + url);
                String result = MyTerminalFactory.getSDK().getHttpClient().sendGet(url, paramsMap);
                getLogger().info("查看视频播放是否结束结果：" + result);
                if (!Util.isEmpty(result)) {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    boolean living = jsonObject.getBoolean("living");
                    if (living) {
                        int resultCode = LiveUtil.requestToWatchLiving(terminalMessage);
                        if(resultCode !=0){
                            ToastUtil.livingFailToast(MessageBaseFragment.this.getContext(), resultCode, TerminalErrorCode.LIVING_PLAYING.getErrorCode());
                        }
                    } else {
                        LiveUtil.getHistoryLiveUrls(terminalMessage);
                    }
                }
            });
        }

        if (terminalMessage.messageType == MessageType.AUDIO.getCode()) {
            getLogger().debug("点击了录音消息！");
        }
        /**  跳转到合并转发  **/
        if (terminalMessage.messageType == MessageType.MERGE_TRANSMIT.getCode()) {
            MergeTransmitListFragment fragment = new MergeTransmitListFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.TERMINALMESSAGE,terminalMessage);
            bundle.putBoolean(Constants.IS_GROUP,isGroup);
            bundle.putInt(Constants.USER_ID,userId);
            fragment.setArguments(bundle);
//            fragment.setFragment_contener(fl_fragment_container);
//            getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fl_fragment_container, fragment).commit();

            FragmentManage.startFragment(getActivity(), fragment);
        }
    }

}
