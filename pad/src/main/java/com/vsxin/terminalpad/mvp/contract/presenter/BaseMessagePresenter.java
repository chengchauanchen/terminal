package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IBaseMessageView;
import com.vsxin.terminalpad.utils.DensityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.TerminalErrorCode;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetHistoryMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveHistoryMessageNotifyDateHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：
 * 修订历史：
 */
public class BaseMessagePresenter<V extends IBaseMessageView> extends RefreshPresenter<TerminalMessage, V>{

    private List<TerminalMessage> data = new ArrayList<>();
    private TerminalMessage tempGetMessage;
    protected List<TerminalMessage> historyFailMessageList = new ArrayList<>();//当前会话历史发送失败消息集合
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isEnoughPageCount = false;//每次从本地取的数据的条数是否够10条

    public BaseMessagePresenter(Context mContext){
        super(mContext);
    }



    public void getMessageFromServer(boolean isGroup,long uniqueNo,int userId,int messageCount){

        long messageId = this.tempGetMessage != null ? this.tempGetMessage.messageId : 0L;
        long messageVersion = this.tempGetMessage != null ? this.tempGetMessage.messageVersion : 0L;
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
            long groupUniqueNo = 0L;
            if (isGroup) {
                if (uniqueNo != 0L) {
                    groupUniqueNo = uniqueNo;
                } else {
                    groupUniqueNo = MyTerminalFactory.getSDK().getTerminalMessageManager().getGroupUniqueNo(userId);
                }
            }

            MyTerminalFactory.getSDK().getTerminalMessageManager().getHistoryMessageRecord(isGroup, (long)userId, messageId, groupUniqueNo, messageVersion, messageCount);
        });
    }

    public List<TerminalMessage> getData(){
        return data;
    }

    public void registReceiveHandler(){
        MyTerminalFactory.getSDK().registReceiveHandler(getHistoryMessageRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receivePersonMessageNotifyDateHandler);
    }

    public void unregistReceiveHandler(){
        MyTerminalFactory.getSDK().unregistReceiveHandler(getHistoryMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePersonMessageNotifyDateHandler);
    }

    private boolean refreshing;
    private double tempPage;
    private GetHistoryMessageRecordHandler getHistoryMessageRecordHandler = messageRecord -> {
        //加上同步，防止更新消息时又来新的消息，导致错乱
        synchronized (BaseMessagePresenter.this) {
            //更新未读消息和聊天界面
            if (messageRecord.isEmpty()) {
                mHandler.post(() -> {
                    if (data.size() != 0) {
                        stopRefreshAndToast("没有更多消息了");
                    }
                    refreshing = false;
                });
            } else {
                //                messageRecord.remove(0);
                setData(messageRecord, true);
            }
            mHandler.post(() -> {
                getView().getLogger().info("GetHistoryMessageRecordHandler"+data.size());
                if (data.size() > 0) {
                    if (tempPage == 1) {
                        getView().setListSelection(data.size() - 1);
                    } else {
                        if (data.size() > messageRecord.size()) {
                            getView().setListSelection(messageRecord.size());
                        }
                    }
                }
                getView().notifyDataSetChanged(data);
            });
        }
    };
    /**
     * 从网络获取数据后刷新页面
     */
    private ReceiveHistoryMessageNotifyDateHandler receivePersonMessageNotifyDateHandler = (resultCode, resultDes) -> {
        if (resultCode != BaseCommonCode.SUCCESS_CODE) {
            if (tempPage > 1) {
                tempPage--;
            }
            if (resultCode == TerminalErrorCode.OPTION_EXECUTE_ERROR.getErrorCode() && !isEnoughPageCount) {
                getView().stopReFreshing();
            } else {
                stopRefreshAndToast(resultDes);
            }
            refreshing = false;
        } else {

        }
        getView().getLogger().info("消息数量："+data.size());
        getView().notifyDataSetChanged(data);
    };

    /**
     * 停止刷新
     */
    private void stopRefreshAndToast(final String messge) {
        mHandler.post(() -> {
            getView().stopReFreshing();
            ToastUtils.showShort(messge);
        });
    }

    /**
     * 设置数据
     *
     * @param groupMessageRecord
     */
    private void setData(List<TerminalMessage> groupMessageRecord, boolean isStopRefresh) {
        if (!getView().isGroup()){
            //个人的警情消息不显示
            Iterator<TerminalMessage> iterator = groupMessageRecord.iterator();
            while (iterator.hasNext()) {
                TerminalMessage next = iterator.next();
                if (next.messageType == MessageType.WARNING_INSTANCE.getCode()) {
                    iterator.remove();
                }
            }

        }
        Collections.sort(groupMessageRecord);
        intersetMessageToList(groupMessageRecord, historyFailMessageList);
        List<TerminalMessage> groupMessageRecord2 = new ArrayList<>();
        groupMessageRecord2.addAll(data);
        stopRefresh(groupMessageRecord, groupMessageRecord2, groupMessageRecord.size(), isStopRefresh);
        tempGetMessage = groupMessageRecord.get(0);
        if (isStopRefresh) {
            refreshing = false;
        }
    }

    /**
     * 将数据插入列表中
     **/
    protected void intersetMessageToList(List<TerminalMessage> messageList, List<TerminalMessage> intersetMessageList) {
        int count = intersetMessageList.size();
        if (!messageList.isEmpty()) {
            long firstVersion = messageList.get(0).messageVersion;
            int failStart = -1;
            List<TerminalMessage> interFailMessageList = new ArrayList<>();
            /**  获取错误列表从哪个位置开始轮询 **/
            for (int i = 0; i < count; i++) {
                TerminalMessage failTerminalMessage = intersetMessageList.get(i);
                long failVersion = failTerminalMessage.messageBody.getLong(JsonParam.DOWN_VERSION_FOR_FAIL);
                if (failStart == -1
                        && firstVersion <= failVersion) {
                    failStart = i;
                }
                if (failStart != -1) {
                    interFailMessageList.add(failTerminalMessage);
                }
            }

            if (interFailMessageList.size() > 0) {
                for (int i = 0; i < interFailMessageList.size(); i++) {
                    TerminalMessage failTerminalMessage = interFailMessageList.get(i);
                    long failVersion = failTerminalMessage.messageBody.getLong(JsonParam.DOWN_VERSION_FOR_FAIL);
                    int interposition = -1;//失败消息插入消息列表的定位
                    for (int j = 0; j < messageList.size(); j++) {
                        TerminalMessage terminalMessage = messageList.get(j);
                        if (failVersion == terminalMessage.messageVersion) {
                            interposition = j + 1;
                        }
                        if (interposition != -1
                                && terminalMessage.resultCode != 0) {//判断后面是否连着失败消息
                            interposition = j + 1;
                        }
                    }
                    if (interposition != -1) {
                        messageList.add(interposition, failTerminalMessage);
                        intersetMessageList.remove(failTerminalMessage);
                    }
                }
            }
        } else {
            messageList.addAll(intersetMessageList);
            Collections.sort(messageList);
        }
    }

    /**
     * 停止刷新
     */
    private void stopRefresh(final List<TerminalMessage> groupMessageRecord1, final List<TerminalMessage> groupMessageRecord2, final int position, boolean isStopRefresh) {
        mHandler.post(() -> {
            data.clear();
            data.addAll(groupMessageRecord1);
            data.addAll(groupMessageRecord2);

            getView().notifyItemRangeInserted(0, position);

            if (tempPage == 1) {
                getView().smoothScrollBy(0,0);
            } else {
                getView().smoothScrollBy(0, -DensityUtil.dip2px(getView().getContext(),30));
            }
            //            groupCallList.scrollBy(0, -DensityUtil.dip2px(ChatBaseActivity.this, 30));
            if (isStopRefresh) {
                getView().stopReFreshing();
            }
        });
    }


}
