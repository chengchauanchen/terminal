package cn.vsx.vc.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetAllMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.adapter.MessageListAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiveGoToHelpCombatHandler;
import cn.vsx.vc.receiveHandle.ReceiveUnReadCountChangedHandler;
import cn.vsx.vc.receiveHandle.ReceiverDeleteMessageHandler;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public abstract class CombatFragment extends BaseFragment {

    @Bind(R.id.help_combat_list)
    ListView help_combat_list;
    @Bind(R.id.iv_return)
    ImageView iv_return;
    @Bind(R.id.tv_title)
    TextView tv_title;
    @Bind(R.id.tv_close_combat)
    TextView tv_close_combat;

    protected boolean isGoToHistory;
    private MessageListAdapter mMessageListAdapter;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, String> idNameMap = TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<>());
    private ArrayList<TerminalMessage> messageList = new ArrayList<>();
    private List<TerminalMessage> terminalMessageData = new ArrayList<>();
    private Handler mHandler = new Handler();

    public CombatFragment(boolean isGoToHistory) {
        this.isGoToHistory = isGoToHistory;
    }

    public void isGoToHistory(boolean isGoToHistory){
        this.isGoToHistory = isGoToHistory;
    }

    public boolean isGoToHistory() {
        return isGoToHistory;
    }

    public void setGoToHistory(boolean goToHistory){
        this.isGoToHistory = isGoToHistory;
    };

    @Override
    public int getContentViewId() {
        return R.layout.fragment_help_combat;
    }

    @Override
    public void initView() {
        logger.debug("initView----------》isGoToHistory："+isGoToHistory);
        if (isGoToHistory){
            tv_title.setText(context.getString(R.string.tv_history_combat_group));
            tv_close_combat.setVisibility(View.GONE);
        }else {
            tv_title.setText(context.getString(R.string.tv_combat_group));
            tv_close_combat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initListener() {
        tv_close_combat.setOnClickListener(new GoToHistoryHelpCombatFragmentClickListener());
        iv_return.setOnClickListener(new ReturnGoToHelpCombatFragmentClickListener());
        help_combat_list.setOnItemClickListener(new CombatListOnItemClickListener());
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
    }

    @Override
    public void initData() {
        loadMessages();
        mMessageListAdapter = new MessageListAdapter(getContext(), messageList, idNameMap, false, isGoToHistory);
        help_combat_list.setAdapter(mMessageListAdapter);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecordNewMethod(messageList));
    }
    private void loadMessages(){
        synchronized(this){
            clearData();
            List<TerminalMessage> messageList;
//            if (isGoToHistory) {
//                messageList = TerminalFactory.getSDK().getTerminalMessageManager().getHistoryCombatMessageList();
//            } else {
//                messageList = TerminalFactory.getSDK().getTerminalMessageManager().getCombatMessageList();
//            }
            messageList = TerminalFactory.getSDK().getTerminalMessageManager().getMessageList();
            logger.info("isGoToHistory："+isGoToHistory+"，从数据库取出合成作战组的消息列表："+messageList);
            addData(messageList);
        }
    }
    private void clearData(){
        synchronized(this){
            messageList.clear();
        }
    }
    private void addData(List<TerminalMessage> terminalMessages){
        synchronized(this){
            messageList.addAll(terminalMessages);
        }
    }

    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler(){
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType){
            if (TempGroupType.TO_HELP_COMBAT.equals(tempGroupType)) {
                if(isAdd){
                    //有新的合成作战组,会在发送消息时弹出来
                    TerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecordNewMethod("", "",
                            DataUtil.getGroupByGroupNo(tempGroupNo).getUniqueNo()+"", 0, 1);
                }else {
                    //合成作战组完成,从处理列表移到历史列表
                    saveHistoryHelpCombatMessageToSql(tempGroupNo);
                }
            }
        }
    };
    //终端被删除
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                clearData();
                saveMessagesToSql();
                if(mMessageListAdapter != null){
                    mMessageListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    private GetAllMessageRecordHandler getAllMessageRecordHandler = messageRecord -> {
        //加上同步，防止更新消息时又来新的消息，导致错乱
        synchronized(this){
            //更新未读消息和聊天界面
            if(messageRecord.isEmpty()){
                return;
            }else {
                terminalMessageData.clear();
                terminalMessageData.addAll(messageList);
                TerminalMessage terminalMessage = messageRecord.get(0);
                saveMessageToList(terminalMessage,false);
                saveHelpCombatMessageToSql(terminalMessage);//来合成作战组消息了，刷新并保存列表
                mHandler.post(() -> {
                    clearData();
                    addData(terminalMessageData);
                    sortMessageList();
                    unReadCountChanged();
                });
            }
        }
    };

    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        if (isGoToHistory)//在历史界面，不监听消息
            return;
        synchronized(this){
            idNameMap.put(terminalMessage.messageFromId, terminalMessage.messageFromName);
            idNameMap.put(terminalMessage.messageToId, terminalMessage.messageToName);
            terminalMessageData.clear();
            terminalMessageData.addAll(messageList);
            //合成作战组消息，只存一个条目
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() &&
                    TempGroupType.TO_HELP_COMBAT.equals((DataUtil.getGroupByGroupNo(terminalMessage.messageToId)).getTempGroupType())){
                saveMessageToList(terminalMessage,false);
                saveHelpCombatMessageToSql(terminalMessage);//来合成作战组消息了，刷新并保存列表
            }
            mHandler.post(() -> {
                clearData();
                addData(terminalMessageData);
                sortMessageList();
                unReadCountChanged();
            });
        }
    };
    private void saveHelpCombatMessageToSql(TerminalMessage terminalMessage) {
        List<TerminalMessage> combatMessageList = TerminalFactory.getSDK().getTerminalMessageManager().getCombatMessageList();
        Iterator<TerminalMessage> iterator = combatMessageList.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() && //合成作战组消息，只存一个条目
                    TempGroupType.TO_HELP_COMBAT.equals((DataUtil.getGroupByGroupNo(next.messageToId)).getTempGroupType()) &&
                    next.messageToId == terminalMessage.messageToId){
                iterator.remove();
                break;
            }
        }
        combatMessageList.add(terminalMessage);
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateCombatMessageList(combatMessageList);
    }
    private void saveHistoryHelpCombatMessageToSql(int combatGroupId) {
        TerminalMessage handleMessage = null;
        //从处理中变为处理完成；从处理列表移除
        List<TerminalMessage> combatMessageList = TerminalFactory.getSDK().getTerminalMessageManager().getCombatMessageList();
        Iterator<TerminalMessage> iterator = combatMessageList.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() && //合成作战组消息，只存一个条目
                    TempGroupType.TO_HELP_COMBAT.equals((DataUtil.getGroupByGroupNo(next.messageToId)).getTempGroupType()) &&
                    next.messageToId == combatGroupId){
                handleMessage = next;
                iterator.remove();
                break;
            }
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateCombatMessageList(combatMessageList);
        //添加到历史列表
        if (handleMessage != null){
            //从处理中变为处理完成；从处理列表移除
            List<TerminalMessage> historyCombatMessageList = TerminalFactory.getSDK().getTerminalMessageManager().getHistoryCombatMessageList();
            historyCombatMessageList.add(handleMessage);
            MyTerminalFactory.getSDK().getTerminalMessageManager().updateHistoryCombatMessageList(historyCombatMessageList);
        }
    }
    private void sortMessageList(){
        synchronized(this){
            if(!messageList.isEmpty()){
                setNewGroupList();
                //再按照时间来排序
                Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
                //再保存到数据库
                saveMessagesToSql();
                if(mMessageListAdapter !=null){
                    mMessageListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void setNewGroupList() {
        Iterator<TerminalMessage> iterator = messageList.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组消息
                if(!DataUtil.isExistGroup(next.messageToId)){
                    //说明组列表中没有这个组了,合成作战组任务完成了；
                    iterator.remove();//消息列表中移除
                    removeMemberMap(next.messageToId);
                }else {
                    Group groupInfo = DataUtil.getGroupByGroupNo(next.messageToId);
                    next.messageToName = groupInfo.name;
                    saveMemberMap(next);
                }
            }
        }
    }
    @SuppressLint("UseSparseArrays")
    private void saveMemberMap(TerminalMessage terminalMessage) {
        idNameMap.putAll(TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<>()));
        if(terminalMessage.messageFromId !=0){
            idNameMap.put(terminalMessage.messageFromId, terminalMessage.messageFromName);
        }
        if(terminalMessage.messageToId !=0){
            idNameMap.put(terminalMessage.messageToId, terminalMessage.messageToName);
        }
        TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
    }
    @SuppressLint("UseSparseArrays")
    private void removeMemberMap(int id) {
        idNameMap.putAll(TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<>()));
        idNameMap.remove(id);
        TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
    }
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
            whetherUnReadAdd(unReadCount, terminalMessage,clearUnread,tempGroupMessageVersion);
            terminalMessageData.add(terminalMessage);
        }
    }
    private void whetherUnReadAdd(int unReadCount, TerminalMessage terminalMessage,boolean clearUnread,long tempGroupMessageVersion) {
        //如果是自己发的消息，如果是请求过来的消息就清空未读，如果是个呼的消息，就用原来的未读
        if(terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
            if(clearUnread){
                terminalMessage.unReadCount = 0;
            }else{
                terminalMessage.unReadCount = unReadCount;
            }
        }else {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个人消息
                if (ActivityCollector.isActivityExist(IndividualNewsActivity.class)){//个人会话页打开了
                    IndividualNewsActivity activity = ActivityCollector.getActivity(IndividualNewsActivity.class);
                    if (terminalMessage.messageFromId == activity.getChatTargetId()){// 会话的人是同一个人，不通知
                        terminalMessage.unReadCount = 0;
                        if(terminalMessage.messageType == MessageType.AUDIO.getCode()){//录音消息，未读
                            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                        }
                    }else {//不是同一个人，通知
                        setPersonMessageUnReadCount(unReadCount, terminalMessage);
                    }
                }else {//个人会话页关闭， 通知
                    setPersonMessageUnReadCount(unReadCount, terminalMessage);
                }
            }else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组消息
                if (ActivityCollector.isActivityExist(GroupCallNewsActivity.class)){//组会话页打开了
                    GroupCallNewsActivity activity = ActivityCollector.getActivity(GroupCallNewsActivity.class);
                    if (terminalMessage.messageToId == activity.getChatTargetId()){// 会话的组是打开的组，不通知
                        terminalMessage.unReadCount = 0;

                        //如果组扫描开启了
                        if(MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN,false)){
                            //如果不是当前组或者扫描组的消息，添加未读红点
                            ArrayList<Integer> scanGroups = MyTerminalFactory.getSDK().getConfigManager().getScanGroups();
                            if(null != scanGroups && scanGroups.contains(terminalMessage.messageToId)){
                                terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                            }else {
                                terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                            }
                        }else{
                            //如果组扫描没开启判断是否为当前组
                            if(terminalMessage.messageToId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0)){
                                terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                            }else {
                                terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                            }
                        }
                    }else {//不是当前打开的会话组，通知
                        setGroupMessageUnReadCount(unReadCount, terminalMessage,tempGroupMessageVersion);
                    }
                }else {//组会话页关闭， 通知
                    setGroupMessageUnReadCount(unReadCount, terminalMessage,tempGroupMessageVersion);
                }
            }
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateTerminalMessage(terminalMessage);
    }

    private void setPersonMessageUnReadCount(int unReadCount, TerminalMessage terminalMessage) {
        if (terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()){//个呼消息
            if (MyApplication.instance.isPrivateCallOrVideoLiveHand){//做过操作，已读
                terminalMessage.unReadCount = unReadCount;
                MyApplication.instance.isPrivateCallOrVideoLiveHand = false;
            }else {//未做操作，未读
                terminalMessage.unReadCount = unReadCount + 1;
            }
        }else if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()){//直播消息
            if (terminalMessage.messageBody.getIntValue("remark") == Remark.ACTIVE_VIDEO_LIVE){//自己主动直播
                terminalMessage.unReadCount = unReadCount;
            }else if (terminalMessage.messageBody.getIntValue("remark") == Remark.ASK_VIDEO_LIVE){//请求图像
                if (terminalMessage.resultCode == SignalServerErrorCode.MEMBER_REFUSE.getErrorCode()) {//对方拒绝
                    terminalMessage.unReadCount = unReadCount;
                } else if (terminalMessage.resultCode == SignalServerErrorCode.VIDEO_LIVE_WAITE_TIMEOUT.getErrorCode()){//请求图像超时
                    String liver = terminalMessage.messageBody.getString("liver");
                    if (TextUtils.isEmpty(liver)){
                        terminalMessage.unReadCount = unReadCount;
                    }else{
                        if (Integer.valueOf(liver.split("_")[0]) == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                            terminalMessage.unReadCount = unReadCount + 1;//直播方，超时未读
                        }else {
                            terminalMessage.unReadCount = unReadCount;//主叫方已读
                        }
                    }
                }else {
                    if (MyApplication.instance.isPrivateCallOrVideoLiveHand){//做过操作，已读
                        terminalMessage.unReadCount = unReadCount;
                        MyApplication.instance.isPrivateCallOrVideoLiveHand = false;
                    }else {//未做操作，未读
                        terminalMessage.unReadCount = unReadCount + 1;
                    }
                }
            }else if (terminalMessage.messageBody.getIntValue("remark") == Remark.LIVE_WATCHING_END){//观看结束
                terminalMessage.unReadCount = unReadCount;
            }else {
                terminalMessage.unReadCount = unReadCount + 1;
            }
        }else if(terminalMessage.messageType == MessageType.AUDIO.getCode()){//录音消息，未读
            terminalMessage.unReadCount = unReadCount + 1;
            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
        }
        else {//其它消息
            terminalMessage.unReadCount = unReadCount + 1;
        }
    }

    private void setGroupMessageUnReadCount(int unReadCount, TerminalMessage terminalMessage,long tempGroupMessageVersion) {
        int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
        List<Integer> scanGroups = MyTerminalFactory.getSDK().getConfigManager().loadScanGroup();//组扫描列表
        boolean groupScanTog = MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false);//组扫描开关
        if (terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){//组呼消息
            if (terminalMessage.isOffLineMessage) {//离线的组呼也是未读
//                terminalMessage.unReadCount = unReadCount + 1;
                terminalMessage.messageBody.put(JsonParam.UNREAD, true);
            } else {
                if(terminalMessage.messageToId == currentGroupId){//是当前值
//                    terminalMessage.unReadCount = unReadCount;
                    terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                }else{//不是当前值
                    if (groupScanTog){//组扫描开着
                        boolean isScanGroup = false;
                        for (Integer integer : scanGroups){
                            if (integer == terminalMessage.messageToId){//是扫描的组
                                isScanGroup = true;
                                break;
                            }
                        }
                        if (isScanGroup){//在组扫描列表中
//                            terminalMessage.unReadCount = unReadCount;
                            terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                        }else {
//                            terminalMessage.unReadCount = unReadCount + 1;
                            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                        }
                    }else {//组扫描关着，判断主组状态
//                        terminalMessage.unReadCount = unReadCount + 1;
                        terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                    }
                }
            }

        }else {//其它类型的消息
//            terminalMessage.unReadCount = unReadCount + 1;
        }
        terminalMessage.unReadCount = getGroupMessageUnreadCount(unReadCount,terminalMessage,tempGroupMessageVersion);
    }
    private int getGroupMessageUnreadCount(int unReadCount, TerminalMessage terminalMessage,long tempGroupMessageVersion){
        if(tempGroupMessageVersion == 0){
            return (unReadCount + 1);
        }else{
            if(terminalMessage.messageVersion>tempGroupMessageVersion){
                return (int) (terminalMessage.messageVersion-tempGroupMessageVersion);
            }else{
                return 0;
            }
        }
    }
    private class GoToHistoryHelpCombatFragmentClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveGoToHelpCombatHandler.class, true, false);
        }
    }

    private class ReturnGoToHelpCombatFragmentClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (isGoToHistory) {//历史组界面return；去合成作战组
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveGoToHelpCombatHandler.class, false, false);
            } else {//合成作战组界面return，去消息界面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveGoToHelpCombatHandler.class, false, true);
            }
        }
    }

    private class CombatListOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TerminalMessage terminalMessage = messageList.get(position);
            if(terminalMessage.unReadCount!=0){
                terminalMessage.unReadCount = 0;
                unReadCountChanged();//未读消息数变了，通知tab
                saveMessagesToSql();
            }

            Intent intent = new Intent(context, GroupCallNewsActivity.class);
            intent.putExtra("isGroup", true);
            intent.putExtra("userId", terminalMessage.messageToId);//组id
            intent.putExtra("userName", DataUtil.getGroupByGroupNo(terminalMessage.messageToId).getName());
            context.startActivity(intent);
        }
    }

    private void unReadCountChanged() {
        int allUnReadCount = 0;
        for (TerminalMessage terminalMessage0 : messageList){
            allUnReadCount += terminalMessage0.unReadCount;
        }
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveUnReadCountChangedHandler.class, allUnReadCount);
    }
    private void saveMessagesToSql(){
        synchronized(this){
            logger.info("isGoToHistory："+isGoToHistory+"，---------保存消息列表---------"+messageList);
            if (isGoToHistory) {
                MyTerminalFactory.getSDK().getTerminalMessageManager().updateHistoryCombatMessageList(messageList);
            } else {
                MyTerminalFactory.getSDK().getTerminalMessageManager().updateCombatMessageList(messageList);
            }
        }
    }

    @Override
    public void onDestroyView() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        clearData();
        super.onDestroyView();
    }
}
