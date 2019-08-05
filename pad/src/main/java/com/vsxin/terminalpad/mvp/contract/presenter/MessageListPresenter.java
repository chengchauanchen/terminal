package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMessageListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
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
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * @author qzw
 * <p>
 * app模块-消息模块
 */
public class MessageListPresenter extends RefreshPresenter<TerminalMessage, IMessageListView> {
    private static final String TAG = "MessagePresenter";
    /**
     * 会话列表list,线程安全的
     */
    private List<TerminalMessage> messageList = Collections.synchronizedList(new ArrayList<>());
    //消息列表，在子线程中使用
    private List<TerminalMessage> terminalMessageData = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public MessageListPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 获取本地缓存消息
     */
    public void loadMessages() {
        synchronized(MessageListPresenter.this){
            terminalMessageData.clear();
            clearData();
            List<TerminalMessage> messageList = TerminalFactory.getSDK().getTerminalMessageManager().getMessageList();
            getView().getLogger().info("从数据库取出消息列表：" + messageList);
            addData(messageList);
        }
    }

    /**
     * 创建一个主组的消息
     */
    private TerminalMessage newMainGroupMessageToList() {
        TerminalMessage  terminalMessage = new TerminalMessage();
        terminalMessage.messageToId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);
        terminalMessage.messageToName = DataUtil.getGroupName(terminalMessage.messageToId);
        terminalMessage.messageCategory = MessageCategory.MESSAGE_TO_GROUP.getCode();
        return terminalMessage;
    }

    /**
     * 添加主组消息，如果没有就new一个新消息，如果有就取最后一条
     */
    private void addMainGroupMessage() {
        //查看当前组的全部消息
        List<TerminalMessage> groupMessageRecord = TerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
                MessageCategory.MESSAGE_TO_GROUP.getCode(), TerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0),
                0, 0);
        TerminalMessage terminalMessage;
        if (groupMessageRecord.size() == 0) {
            terminalMessage = newMainGroupMessageToList();
        } else {
            //最后一条消息
            terminalMessage = groupMessageRecord.get(groupMessageRecord.size()-1);
        }
        addData(0,terminalMessage);
        getView().notifyDataSetChanged(messageList);
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
    public void getAllMessageFromServer() {
        //同步服务器消息
        MyTerminalFactory.getSDK().getThreadPool().execute(() ->
                MyTerminalFactory.getSDK()
                        .getTerminalMessageManager()
                        .getAllMessageRecordNewMethod(messageList));
    }


    /**
     * 同步消息 结束后（不管有无数据）都会回调
     * <p>
     * 通知一个状态而已
     */
    private ReceivePersonMessageNotifyDateHandler receivePersonMessageNotifyDateHandler = (int resultCode, String resultDes) -> {
        getView().getLogger().info("同步消息 结束后（不管有无数据）都会回调 resultCode=" + resultCode + ",resultDes=" + resultDes + ",size=" + messageList.size());
        getView().notifyDataSetChanged(messageList);
    };

    /**
     * 返回所有 同步的消息
     * 有空的情况
     */
    private GetAllMessageRecordHandler getAllMessageRecordHandler = messageRecord -> {
        getView().getLogger().info(TAG + ":返回所有 同步的消息:" + messageRecord.size());

        //加上同步，防止更新消息时又来新的消息，导致错乱
        synchronized(MessageListPresenter.this){
            //更新未读消息和聊天界面
            if(messageRecord.isEmpty()){
                mHandler.post(() -> {
                    sortFirstMessageList();
                    //                    sortMessageList();
                    unReadCountChanged();
                });
            }else {
                terminalMessageData.clear();
                terminalMessageData.addAll(messageList);
                for(TerminalMessage terminalMessage : messageRecord){
                    if(terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()){
                        //警情消息会在警情详情处理
                        continue;
                    }
                    if(TerminalMessageUtil.isLiveMessage(terminalMessage)){
                        saveVideoMessage(terminalMessage,true);
                    }else {
                        saveMessageToList(terminalMessage,true);
                    }
                }
                mHandler.post(() -> {
                    clearData();
                    addData(terminalMessageData);
                    //                    sortMessageList();
                    sortFirstMessageList();
//                    updateFrequentMembers();
                    unReadCountChanged();
                    //通知notification
                    for(int i = messageList.size()-1; 0 <=i; i--){
                        TerminalMessage terminalMessage = messageList.get(i);
                        //如果当前组是new出来的新消息messageFromId默认为0
                        if(terminalMessage.messageFromId != 0 && terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
//                            generateNotification(terminalMessage,i);
                        }
                    }
                });
            }
        }
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
        getView().getLogger().info("MessagePresenter---收到新消息"+terminalMessage);
        synchronized(MessageListPresenter.this){
            terminalMessageData.clear();
            terminalMessageData.addAll(messageList);
            for(TerminalMessage next : terminalMessageData){
                if(TerminalMessageUtil.isSameGroupMessage(next, terminalMessage) || TerminalMessageUtil.isSameMemberMessage(next, terminalMessage)){
                    if(next.messageVersion >= terminalMessage.messageVersion){
                        return;
                    }
                }
            }
            if (TerminalMessageUtil.isGroupMessage(terminalMessage)) {
                if(terminalMessage.messageType != MessageType.WARNING_INSTANCE.getCode()){
                    //合成作战组消息，只存一个条目
                    if(GroupUtils.isCombatGroup(terminalMessage.messageToId)){
                        getView().getLogger().info("合成作战组消息:"+terminalMessage.messageToId);
                        //如果是合成作战组存一下标记
                        terminalMessage.messageBody.put(JsonParam.COMBAT_GROUP,true);
                        saveHelpCombatMessage(terminalMessage, false);
                        saveHelpCombatMessageToSql(terminalMessage);
                    }else {
                        saveMessageToList(terminalMessage,false);
                    }
                }else {
                    //组的警情消息会在警情详情那里处理
                    return;
                }
            }else {
                if(terminalMessage.messageType != MessageType.WARNING_INSTANCE.getCode()){
                    if(TerminalMessageUtil.isLiveMessage(terminalMessage)){
                        saveVideoMessage(terminalMessage,false);
                    }else {
                        saveMessageToList(terminalMessage,false);
                    }
                }else {
                    //警情详情会处理
                    return;
                }
            }
            mHandler.post(() -> {
                clearData();
                addData(terminalMessageData);
                //来一条消息的时候不用删除响应组
                sortMessageList();
                unReadCountChanged();
            });
        }
    };


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
        getView().getLogger().info("---------保存消息列表---------" + messageList);
        //该方法本身为同步方法,线程安全
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateMessageList(messageList);
    }

    /**
     * 排序并保存会话列表
     */
    private synchronized void sortAndSaveConversationList() {
        if (!messageList.isEmpty()) {
            //去掉不存在的组消息
            removeNotExistGroup();
            //再按照时间来排序
            Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
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
        Iterator<TerminalMessage> iterator = messageList.iterator();
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

    private synchronized void clearData(){
        messageList.clear();
    }

    private synchronized void removeData(int position){
        messageList.remove(position);
    }

    private synchronized void addData(List<TerminalMessage> terminalMessages){
        messageList.addAll(terminalMessages);
    }

    private synchronized void addData(TerminalMessage terminalMessage){
        messageList.add(terminalMessage);
    }
    private synchronized void addData(int position ,TerminalMessage terminalMessage){
        messageList.add(position,terminalMessage);
    }

    /**
     * 去掉在通讯录里不存在的组
     */
    @SuppressWarnings("unused")
    private void setNewGroupList() {
        Iterator<TerminalMessage> iterator = messageList.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组消息
                if(!DataUtil.isExistGroup(next.messageToId) && !TerminalMessageUtil.isCombatGroup(next)){
                    //说明组列表中没有这个组了
                    iterator.remove();//消息列表中移除
                }else {
                    next.messageToName = DataUtil.getGroupName(next.messageToId);
                }
            }
        }
    }

    /**
     * 保证第一条消息是当前组消息
     */
    private void setFirstMessage() {
        if(messageList.size() == 0){//无列表，添加当前组
            addMainGroupMessage();
        }
//        else{//有列表
//            列表中有当前组，置顶当前组
//            if(haveCurrentGroupMessage()) {
//                stickCurrentGroupMessage();
//            }else {//列表中无当前组，添加
//                addMainGroupMessage();
//            }
//        }
    }

    private synchronized void saveMessagesToSql(){
        getView().getLogger().info("---------保存消息列表---------"+messageList);
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateMessageList(messageList);
    }

    /**
     * 刚进入应用时低级用户不显示响应组，高级用户将响应组放在当前组前面
     */
    private void sortFirstMessageList(){
        if(!messageList.isEmpty()){
            setNewGroupList();
            Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
            setFirstMessage();
            //再保存到数据库
            saveMessagesToSql();
            getView().notifyDataSetChanged(messageList);
        }else {
            addMainGroupMessage();
        }
    }

    private void unReadCountChanged() {
        int allUnReadCount = 0;
        for (TerminalMessage terminalMessage0 : messageList){
            allUnReadCount += terminalMessage0.unReadCount;
        }
//        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveUnReadCountChangedHandler.class, allUnReadCount);
    }

    private void saveVideoMessage(TerminalMessage terminalMessage,boolean clearUnread){
        int unReadCount = 0;
        final Iterator<TerminalMessage> iterator = terminalMessageData.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (TerminalMessageUtil.isLiveMessage(next)){
                unReadCount = next.unReadCount;
                iterator.remove();
                break;
            }
        }
        terminalMessageData.add(terminalMessage);
//        whetherUnReadAdd(unReadCount, terminalMessage,clearUnread,0);
    }

    /**
     * 保存消息到列表，如果为空就直接添加
     * 如果不为空，需查找到相同的人发送的消息，删除之后再添加新的
     * @param terminalMessage 消息
     * @param clearUnread 是否清空未读消息
     */
    private void saveMessageToList(TerminalMessage terminalMessage,boolean clearUnread){
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);//是否为别人发的消息
        if (terminalMessageData.isEmpty()){
            if(isReceiver){
                //如果是别人发的消息，未读直接+1
                terminalMessage.unReadCount = 1;
            }
            terminalMessageData.add(terminalMessage);
        }else {
            int unReadCount = 0;
            long tempGroupMessageVersion = 0;
            Iterator<TerminalMessage> iterator = terminalMessageData.iterator();
            while (iterator.hasNext()){
                TerminalMessage next = iterator.next();
                boolean isRemove = false;
                if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个人消息
                    //个人消息分四种情况
                    if(next.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){
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
                }
            }
            //未读消息条目是否+1
//            whetherUnReadAdd(unReadCount, terminalMessage,clearUnread,tempGroupMessageVersion);
            terminalMessageData.add(terminalMessage);
        }
    }

    private void saveHelpCombatMessageToSql(TerminalMessage terminalMessage) {
        List<TerminalMessage> combatMessageList = TerminalFactory.getSDK().getTerminalMessageManager().getCombatMessageList();
        Iterator<TerminalMessage> iterator = combatMessageList.iterator();
        boolean remove = false;
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() &&
                    terminalMessage.messageBody.containsKey(JsonParam.COMBAT_GROUP) &&
                    terminalMessage.messageBody.getBooleanValue(JsonParam.COMBAT_GROUP)&&
                    next.messageToId == terminalMessage.messageToId){
                setCombatMessageUnreadCount(next,terminalMessage);
                iterator.remove();
                remove = true;
                break;
            }
        }
        if(!remove){
            //自己发的消息未读不加
            if(terminalMessage.messageFromId == TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                terminalMessage.unReadCount = 0;
            }else {
                terminalMessage.unReadCount+=1;
            }
        }
        combatMessageList.add(terminalMessage);
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateCombatMessageList(combatMessageList);
    }

    /**
     * 设置合成作战组某一条消息未读数量
     * @param lastMessage 以前的消息
     * @param terminalMessage 新消息
     */
    private void setCombatMessageUnreadCount(TerminalMessage lastMessage,TerminalMessage terminalMessage){
        if(terminalMessage.messageFromId == TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
            terminalMessage.unReadCount = 0;
        }else {
            if(terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){
                //组呼消息，判断组是否被监听
                if(GroupUtils.getAllMonitorGroups().contains(terminalMessage.messageToId)){
                    terminalMessage.unReadCount = lastMessage.unReadCount;
                }else {
                    terminalMessage.unReadCount = lastMessage.unReadCount+1;
                }
            }else {
                terminalMessage.unReadCount = lastMessage.unReadCount+1;
            }
        }
    }

    /**找到合成作战组的条目替换成新的合成作战组的最新一条消息*/
    private void saveHelpCombatMessage(TerminalMessage terminalMessage, boolean clearUnread) {
        int unReadCount = 0;
        final Iterator<TerminalMessage> iterator = terminalMessageData.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() && //合成作战组消息，只存一个条目
                    next.messageBody !=null && next.messageBody.containsKey(JsonParam.COMBAT_GROUP) && next.messageBody.getBooleanValue(JsonParam.COMBAT_GROUP)){
                unReadCount = next.unReadCount;
                iterator.remove();
                break;
            }
        }
        TerminalMessage terminalMessage1 = (TerminalMessage) terminalMessage.clone();
        //是否往消息列表里未读消息+1
//        whetherUnReadAdd(unReadCount, terminalMessage1,clearUnread,0);
        terminalMessageData.add(terminalMessage1);
    }

    /**
     * 对聊天列表排序
     */
    private void sortMessageList(){
        synchronized(MessageListPresenter.this){
            if(!messageList.isEmpty()){
                setNewGroupList();
                //            setNewMemberList();
                //再按照时间来排序
                Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
                //再设置第一条消息，一般是当前组
                setFirstMessage();
                //再保存到数据库
                saveMessagesToSql();
                getView().notifyDataSetChanged(messageList);
            }else {
                addMainGroupMessage();
            }
        }
    }

    public List<TerminalMessage> getData(){
        return messageList;
    }
}
