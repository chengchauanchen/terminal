package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.util.Log;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMessageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetAllMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeNameHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyRecallRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePersonMessageNotifyDateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRecallRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class MessagePresenter extends RefreshPresenter<TerminalMessage, IMessageView> {
    private static final String TAG = "MessagePresenter";
    /**
     * 会话列表list,线程安全的
     */
    private List<TerminalMessage> conversations = Collections.synchronizedList(new ArrayList<>());

    public void setConversations(List<TerminalMessage> terminalMessages) {
        this.conversations.clear();
        this.conversations.addAll(terminalMessages);
    }

    public List<TerminalMessage> getConversations() {
        return conversations;
    }

    public MessagePresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 获取本地缓存消息
     */
    public void loadMessages() {
        List<TerminalMessage> messageList = TerminalFactory.getSDK().getTerminalMessageManager().getMessageList();
        getView().getLogger().info("从数据库取出消息列表：" + messageList);
        setConversations(messageList);
        getView().notifyDataSetChanged(getConversations());

        //当本地没有消息记录时，添加主组消息到messageList中，通过主组的uniqueNo查询主组最新一条消息记录
        if (messageList.isEmpty()) {
            getConversations().add(addMainGroupMessage());
        }
        getAllMessageToServer();
    }

    /**
     * 当本地没有消息记录时，添加主组消息到messageList中，通过主组的uniqueNo查询主组最新一条消息记录
     * <p>
     * 如果没有就new一个新消息，如果有就取最后一条
     */
    private TerminalMessage addMainGroupMessage() {
        //查看当前组的全部消息
        List<TerminalMessage> groupMessageRecord = TerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
                MessageCategory.MESSAGE_TO_GROUP.getCode(), TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0),
                0, 0);
        TerminalMessage terminalMessage;
        if (groupMessageRecord.size() == 0) {
            terminalMessage = newMainGroupMessage();
        } else {
            //最后一条消息
            terminalMessage = groupMessageRecord.get(groupMessageRecord.size() - 1);
        }
        return terminalMessage;
    }

    /**
     * 创建一个空的主组的消息
     */
    private TerminalMessage newMainGroupMessage() {
        TerminalMessage terminalMessage = new TerminalMessage();
        terminalMessage.messageToId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);
        terminalMessage.messageToName = DataUtil.getGroupName(terminalMessage.messageToId);
        terminalMessage.messageCategory = MessageCategory.MESSAGE_TO_GROUP.getCode();
        return terminalMessage;
    }


    /*****************************************getAllMessageRecordNewMethod**************************************************/
    /**
     * 同步 未读 聊天记录到本地
     * 1.保存到本地数据库
     * 2.将数据，绑定到参数 terminalMessages
     * <p>
     * 会回调
     * ReceivePersonMessageNotifyDateHandler
     * GetAllMessageRecordHandler
     */
    private void getAllMessageToServer() {
        //同步服务器消息
        MyTerminalFactory.getSDK().getThreadPool().execute(() ->
                MyTerminalFactory.getSDK()
                        .getTerminalMessageManager()
                        .getAllMessageRecordNewMethod(getConversations()));
    }


    /**
     * 同步消息 结束后（不管有无数据）都会回调
     * <p>
     * 通知一个状态而已
     */
    private ReceivePersonMessageNotifyDateHandler receivePersonMessageNotifyDateHandler = (int resultCode, String resultDes) -> {
        getView().getLogger().info("同步消息 结束后（不管有无数据）都会回调 resultCode=" + resultCode + ",resultDes=" + resultDes + ",size=" + getConversations().size());
        getView().notifyDataSetChanged(getConversations());
    };

    /**
     * 返回所有 同步的消息
     * 有空的情况
     */
    private GetAllMessageRecordHandler getAllMessageRecordHandler = messageRecord -> {
        getView().getLogger().info(TAG + ":返回所有 同步的消息:" + messageRecord.size());

        if (messageRecord == null || messageRecord.isEmpty()) {//消息为空
            //Todo 1消息排序，2更新未读消息数

        } else {//消息不为空
            for (TerminalMessage message:messageRecord){
                updateConversation(message);
            }
        }

        //保存到本地
        sortAndSaveConversationList();
    };

    /*************************************************getAllMessageRecordNewMethod END*****************************************/

    /**
     * 未读消息状态改变
     * <p>
     * 看代码逻辑，只用于点击通知栏消息后，将此消息的未读数置为0
     */
    private ReceiveUnreadMessageChangedHandler receiveUnreadMessageChangedHandler = terminalMessage -> {
        getView().getLogger().info("未读消息状态改变");
    };

    /**
     * 网络状态变化
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        getView().getLogger().info("网络状态变化");
    };


    /**
     * 接收到 组呼 停止的指示
     * <p>
     * 跟新UI
     */
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = reasonCode -> {
        getView().getLogger().info("接收到 组呼 停止的指示");
    };

    /**
     * 接收到一条消息
     */
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        getView().getLogger().info("接收到一条消息");
        //将此消息更新到会话列表中
        updateConversation(terminalMessage);
        sortAndSaveConversationList();
        getView().notifyDataSetChanged(getConversations());
    };

    /**
     * 更新会话列表
     *
     * @param terminalMessage
     */
    private void updateConversation(TerminalMessage terminalMessage) {
        //是否为别人发的消息
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);

        if (getConversations().isEmpty()) {
            if (isReceiver) {
                //如果是别人发的消息，未读直接+1
                terminalMessage.unReadCount = 1;
            }
            getConversations().add(terminalMessage);
        } else {
            int unReadCount = 0;
            long tempGroupMessageVersion = 0;

            //将次消息与会话列表中的消息比较，判断是否有相同的人发送过来的消息,并删除之前的，将新消息添加进去
            Iterator<TerminalMessage> iterator = getConversations().iterator();
            while (iterator.hasNext()){
                TerminalMessage next = iterator.next();
                boolean isRemove = false;
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个人消息
                    //个人消息分四种情况
                    if(next.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个人消息
                        //1、列表中是自己给对方发送的，新消息也是自己给对方发送的
                        //2、列表中是对方给我发的，新消息也是对方给我发的
                        if(next.messageFromId == terminalMessage.messageFromId && next.messageToId == terminalMessage.messageToId){
                            isRemove = true;
                        }
                        //3、列表中是自己给对方发送的，新消息是对方给我发的
                        //4、列表中是对方给我发的，新消息也是我给对方发的
                        if(next.messageFromId == terminalMessage.messageToId && next.messageToId == terminalMessage.messageFromId){
                            isRemove = true;
                        }
                    }
                }
                //组消息, messageToId相同就是同一个组
                else if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() &&
                        next.messageToId == terminalMessage.messageToId){
                    isRemove = true;
                    tempGroupMessageVersion = next.messageVersion;
                }
                if(isRemove){
                    unReadCount = next.unReadCount;
                    iterator.remove();
                    break;//找到一个就退出
                }
            }
            getConversations().add(terminalMessage);
        }


    }

    /**
     * 切组后的消息回调
     */
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            getView().getLogger().info("切组后的消息回调");
        }
    };

    /**
     * 强制切组
     */
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId, boolean forceSwitchGroup, String tempGroupType) {
            getView().getLogger().info("强制切组");
        }
    };


    /**
     * 终端被删除
     */
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            getView().getLogger().info("终端被删除");
        }
    };

    /**
     * 收到修改名字成功的消息
     */
    private ReceiveChangeNameHandler receiveChangeNameHandler = (resultCode, memberId, newMemberName) -> {
        getView().getLogger().info("收到修改名字成功的消息");
    };

    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler() {
        @Override
        public void handler() {
            getView().getLogger().info("更新文件夹和组列表数据");
        }
    };


    /**
     * 文件等下载完成的监听
     */
    private ReceiveDownloadFinishHandler receiveDownloadFinishHandler = (terminalMessage, success) -> {
        getView().getLogger().info("文件等下载完成的监听");
    };

    /**
     * 临时组 相关
     */
    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler() {
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType) {
            getView().getLogger().info("临时组 相关");
        }
    };

    /**
     * 撤回消息
     **/
    private ReceiveResponseRecallRecordHandler mReceiveResponseRecallRecordHandler = (resultCode, resultDesc, messageId, messageBodyId) -> {
        getView().getLogger().info("撤回消息");
    };

    /**
     * 收到别人撤回消息的通知
     **/
    private ReceiveNotifyRecallRecordHandler mNotifyRecallRecordMessageHandler = (version, messageId, messageBodyId) -> {
        getView().getLogger().info("收到别人撤回消息的通知");
    };

    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUnreadMessageChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseRecallRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mNotifyRecallRecordMessageHandler);
    }

    public void unRegistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUnreadMessageChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseRecallRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mNotifyRecallRecordMessageHandler);
    }

    /**
     * 保存会话列表到本地数据库
     */
    private void saveConversationListToDB() {
        getView().getLogger().info("---------保存消息列表---------" + getConversations());
        //该方法本身为同步方法,线程安全
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateMessageList(getConversations());
    }

    /**
     * 排序并保存会话列表
     */
    private synchronized void sortAndSaveConversationList() {
        if (!getConversations().isEmpty()) {
            //去掉不存在的组消息
            removeNotExistGroup();
            //再按照时间来排序
            Collections.sort(getConversations(), (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
            //置顶当前组,暂无需这个功能
            //setFirstMessage();
            //再保存到数据库
            saveConversationListToDB();
        } else {
            addMainGroupMessage();
        }
    }

    /**
     * 去掉在通讯录里不存在的组
     */
    private void removeNotExistGroup() {
        Iterator<TerminalMessage> iterator = getConversations().iterator();
        while (iterator.hasNext()) {
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {//组消息
                if (!DataUtil.isExistGroup(next.messageToId) && !TerminalMessageUtil.isCombatGroup(next)) {
                    //说明组列表中没有这个组了
                    iterator.remove();//消息列表中移除
                } else {
                    next.messageToName = DataUtil.getGroupName(next.messageToId);
                }
            }
        }
    }
}
