package cn.vsx.vc.fragment;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.FileUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageStatus;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.common.TempGroupType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetAllMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetWarningMessageDetailHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeNameHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberAboutTempGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyRecallRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveOnLineStatusChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePersonMessageNotifyDateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveResponseRecallRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.GroupUtils;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.TerminalMessageUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.CombatGroupActivity;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.HistoryCombatGroupActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.PhoneAssistantManageActivity;
import cn.vsx.vc.activity.PushLiveMessageManageActivity;
import cn.vsx.vc.adapter.MessageListAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.DeleteMessageDialog;
import cn.vsx.vc.receiveHandle.ReceiveUnReadCountChangedHandler;
import cn.vsx.vc.receiveHandle.ReceiverDeleteMessageHandler;
import cn.vsx.vc.receiver.NotificationClickReceiver;
import cn.vsx.vc.utils.ActivityCollector;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import ptt.terminalsdk.context.MyTerminalFactory;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

@SuppressLint("ValidFragment")
public class NewsFragment extends BaseFragment {


    ListView newsList;

    ImageView add_icon;

    TextView setting_group_name;

    ImageView icon_laba;

    TextView speaking_name;

    ImageView voice_image;

    private int deletePos = -1 ;
    private MessageListAdapter mMessageListAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    //消息列表，在子线程中使用
    private List<TerminalMessage> terminalMessageData = new ArrayList<>();
    //界面显示的消息列表
    private ArrayList<TerminalMessage> messageList = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private boolean soundOff;//是否静音
    private boolean isFirstCall;

    private void saveMessagesToSql(){
        synchronized(NewsFragment.this){
            logger.info("---------保存消息列表---------"+messageList);
            MyTerminalFactory.getSDK().getTerminalMessageManager().updateMessageList(messageList);
        }
    }

    private void loadMessages(){
        synchronized(NewsFragment.this){
            terminalMessageData.clear();
            clearData();
            List<TerminalMessage> messageList = TerminalFactory.getSDK().getTerminalMessageManager().getMessageList();
            logger.info("从数据库取出消息列表："+messageList);
            addData(messageList);
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

    /**
     * 将当前响应组消息置顶
     * @param groupId
     */
    private void setFirstResponseMessage(int groupId){
        if(haveCurrentResponseGroupMessage(groupId)){
            stickCurrentResponseGroupMessage(groupId);
        }else {
            addCurrentResponseGroupMessage(groupId);
        }
    }

    private void addCurrentResponseGroupMessage(int groupId){
        //查看当前响应组的全部消息
        List<TerminalMessage> groupMessageRecord = TerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
                MessageCategory.MESSAGE_TO_GROUP.getCode(), groupId,
                0, 0);
        TerminalMessage terminalMessage;
        if (groupMessageRecord.size() == 0) {
            terminalMessage = new TerminalMessage();
            terminalMessage.messageToId = groupId;
            terminalMessage.messageToName = DataUtil.getGroupName(terminalMessage.messageToId);
            terminalMessage.messageCategory = MessageCategory.MESSAGE_TO_GROUP.getCode();
        } else {
            //最后一条消息
            terminalMessage = groupMessageRecord.get(groupMessageRecord.size()-1);
        }
        addData(0,terminalMessage);
        if(mMessageListAdapter != null){
            mMessageListAdapter.notifyDataSetChanged();
        }
    }

    private void stickCurrentResponseGroupMessage(int groupId){
        synchronized(NewsFragment.this){

            List<TerminalMessage> temporaryList = new ArrayList<>();
            TerminalMessage currentResponseGroupMessage = null;
            for (TerminalMessage terminalMessage : messageList) {
                //当前组的消息
                if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() && terminalMessage.messageToId == groupId) {
                    currentResponseGroupMessage = terminalMessage;
                }else {
                    temporaryList.add(terminalMessage);
                }
            }

            if(currentResponseGroupMessage != null) {
                clearData();
                addData(currentResponseGroupMessage);
                addData(temporaryList);
            }
        }

        if(mMessageListAdapter != null){
            mMessageListAdapter.notifyDataSetChanged();
        }
    }

    private boolean haveCurrentResponseGroupMessage(int groupId){
        for(TerminalMessage terminalMessage : messageList) {
            if(terminalMessage.messageToId == groupId){
                return true;
            }
        }
        return false;
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
            terminalMessage = new TerminalMessage();
            terminalMessage.messageToId = MyTerminalFactory.getSDK().getParam(Params.MAIN_GROUP_ID, 0);
            terminalMessage.messageToName = DataUtil.getGroupName(terminalMessage.messageToId);
            terminalMessage.messageCategory = MessageCategory.MESSAGE_TO_GROUP.getCode();
        } else {
            //最后一条消息
            terminalMessage = groupMessageRecord.get(groupMessageRecord.size()-1);
        }
        addData(0,terminalMessage);
        if(mMessageListAdapter != null){
            mMessageListAdapter.notifyDataSetChanged();
        }
    }

    /**  当前组的消息是否存在 **/
    private boolean haveCurrentGroupMessage () {
        for(TerminalMessage terminalMessage : messageList) {
            if(terminalMessage.messageToId == MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)){
                return true;
            }
        }
        return false;
    }

    /**  置顶当前组消息  **/
    public void stickCurrentGroupMessage () {
        List<TerminalMessage> temporaryList = new ArrayList<>();
        TerminalMessage currentGroupMessage = null;
        for (TerminalMessage terminalMessage : messageList) {
            //当前组的消息
            if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() && terminalMessage.messageToId == TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)) {
                currentGroupMessage = terminalMessage;
            }else {
                temporaryList.add(terminalMessage);
            }
        }

        if(currentGroupMessage != null) {
            clearData();
            addData(currentGroupMessage);
            addData(temporaryList);
        }

        if(mMessageListAdapter != null){
            mMessageListAdapter.notifyDataSetChanged();
        }
    }

    public NewsFragment() {

    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_news;
    }

    @Override
    public void initView() {
        voice_image = (ImageView) mRootView.findViewById(R.id.voice_image);
        speaking_name = (TextView) mRootView.findViewById(R.id.speaking_name);
        icon_laba = (ImageView) mRootView.findViewById(R.id.icon_laba);
        setting_group_name = (TextView) mRootView.findViewById(R.id.setting_group_name);
        add_icon = (ImageView) mRootView.findViewById(R.id.add_icon);
        newsList = (ListView) mRootView.findViewById(R.id.news_list);
        setVideoIcon();
        setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)));
        voice_image.setImageResource(BitmapUtil.getVolumeImageResourceByValue(false));
        voice_image.setOnClickListener(view -> {
            if(!soundOff){
                voice_image.setImageResource(R.drawable.volume_off_call);
                TerminalFactory.getSDK().getAudioProxy().volumeQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, true,1);
                soundOff =true;
            }else {
                voice_image.setImageResource(R.drawable.horn);
                TerminalFactory.getSDK().getAudioProxy().volumeCancelQuiet();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveVolumeOffCallHandler.class, false,1);
                soundOff =false;
            }
        });
    }

    @Override
    public void initListener() {
        newsList.setOnItemClickListener(new OnItemClickListenerImp());
//        newsList.setOnItemLongClickListener(new OnItemLongClickListenerImp());
        MyTerminalFactory.getSDK().registReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUnreadMessageChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(getWarningMessageDetailHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberAboutTempGroupHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mReceiveResponseRecallRecordHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(mNotifyRecallRecordMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverDeleteMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);

    }

    @Override
    public void initData() {
        loadMessages();
        mMessageListAdapter = new MessageListAdapter(getContext(), messageList, true, false);
        newsList.setAdapter(mMessageListAdapter);
        MyTerminalFactory.getSDK().getThreadPool().execute(() -> MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecordNewMethod(messageList));
    }

    @Override
    public void onResume(){
        super.onResume();
        if(null !=mMessageListAdapter){
            mMessageListAdapter.notifyDataSetChanged();
        }
    }


    private void setVideoIcon() {
        MyTopRightMenu.offerObject().initview(add_icon,getActivity() );
        add_icon.setVisibility(View.VISIBLE);
    }
    @Override
    public void onDestroyView() {

        mHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUnreadMessageChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveOnLineStatusChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getWarningMessageDetailHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveResponseRecallRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mNotifyRecallRecordMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberAboutTempGroupHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(mReceiverDeleteMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().unregistReceiveHandler(receiveVolumeOffCallHandler);
        clearData();
        terminalMessageData.clear();
        super.onDestroyView();
    }

    private void setViewEnable (boolean isEnable) {
        newsList.setEnabled(isEnable);
        add_icon.setEnabled(isEnable);
    }

    /***  组呼的时候显示View **/
    private void showViewWhenGroupCall (final String speakerName) {
        speaking_name.setVisibility(View.VISIBLE);
        icon_laba.setVisibility(View.VISIBLE);
        speaking_name.setText(speakerName);
    }

    /***  停止组呼的时候隐藏View **/
    private void hideViewWhenStopGroupCall () {
        if(speaking_name != null && icon_laba != null){
            speaking_name.setVisibility(View.GONE);
            icon_laba.setVisibility(View.GONE);
        }
    }
    /**音量改变*/
    private ReceiveVolumeOffCallHandler receiveVolumeOffCallHandler = new ReceiveVolumeOffCallHandler() {

        @Override
        public void handler(boolean isVolumeOff,int status) {

            if(isVolumeOff){
                voice_image.setImageResource(R.drawable.volume_off_call);
                soundOff=true;
            }else {
                voice_image.setImageResource(R.drawable.horn);
                soundOff=false;
            }
        }
    };
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = () -> mHandler.post(() -> {
        sortFirstMessageList();
        setVideoIcon();
    });

    private class OnItemLongClickListenerImp implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if(position > 0) {
                new DeleteMessageDialog(context).show();
                deletePos = position;
            }
            return true;
        }
    }

    private final class OnItemClickListenerImp implements OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TerminalMessage terminalMessage = messageList.get(position);
            terminalMessage.unReadCount = 0;
            unReadCountChanged();//未读消息数变了，通知tab
            saveMessagesToSql();
            // 进入图像助手
            if(terminalMessage.messageFromId == terminalMessage.messageToId &&
                    terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0) &&
                    terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()) {
                Intent intent = new Intent(context, PushLiveMessageManageActivity.class);
                context.startActivity(intent);
            }
            //进入电话助手
            else if(terminalMessage.messageType ==MessageType.CALL_RECORD.getCode()){
                Intent intent = new Intent(context, PhoneAssistantManageActivity.class);
                context.startActivity(intent);
            }
            else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {
                Intent intent = new Intent(context, IndividualNewsActivity.class);
                intent.putExtra("isGroup", TerminalMessageUtil.isGroupMeaage(terminalMessage));
                intent.putExtra("userId", TerminalMessageUtil.getNo(terminalMessage));
                intent.putExtra("userName", TerminalMessageUtil.getTitleName(terminalMessage));
                context.startActivity(intent);
            }
            else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//进入组会话页
                if(TerminalMessageUtil.isCombatGroup(terminalMessage)){
                    context.startActivity(new Intent(context, CombatGroupActivity.class));
                }else {
                    Intent intent = new Intent(context, GroupCallNewsActivity.class);
                    intent.putExtra("isGroup", TerminalMessageUtil.isGroupMeaage(terminalMessage));
                    intent.putExtra("userId", TerminalMessageUtil.getNo(terminalMessage));
                    intent.putExtra("userName", TerminalMessageUtil.getTitleName(terminalMessage));
                    intent.putExtra("speakingId",speakingId);
                    intent.putExtra("speakingName",speakingName);
                    context.startActivity(intent);
                }
            }
        }
    }


    private void unReadCountChanged() {
        int allUnReadCount = 0;
        for (TerminalMessage terminalMessage0 : messageList){
            allUnReadCount += terminalMessage0.unReadCount;
        }
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveUnReadCountChangedHandler.class, allUnReadCount);
    }

    /*** 自己组呼返回的消息 **/
    private ReceiveRequestGroupCallConformationHandler mReceiveRequestGroupCallConformationHandler = (methodResult, resultDesc) -> mHandler.post(() -> {
        if (methodResult == 0) {
            showViewWhenGroupCall(getString(R.string.text_I_am_talking));
            setViewEnable(false);
        }
    });

    /***  自己组呼结束 **/
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = (resultCode, resultDesc) -> mHandler.post(() -> {
        if (MyApplication.instance.getGroupListenenState() == LISTENING) {
            return;
        }
        hideViewWhenStopGroupCall();
        setViewEnable(true);
    });

    private int speakingId;
    private String speakingName;
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, final String memberName, final int groupId, String groupName, CallMode currentCallMode) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                mHandler.post(() -> {
                    speakingId = groupId;
                    speakingName = memberName;
                    logger.info("sjl_消息页面的组呼到来");
                    logger.info("sjl_消息页面的组呼到来"+speaking_name.getVisibility()+",正在说话人的名字："+MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
                    String speakingName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
                    showViewWhenGroupCall(speakingName);
                    setting_group_name.setText(DataUtil.getGroupName(groupId));
                });
            }

        }
    };
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler(){

        @Override
        public void handler(int reasonCode) {
            mHandler.post(() -> {
                hideViewWhenStopGroupCall();
                setViewEnable(true);
                setting_group_name.setText(DataUtil.getGroupName(MyTerminalFactory.getSDK()
                        .getParam(Params.CURRENT_GROUP_ID, 0)));
            });
        }
    };

    private GetWarningMessageDetailHandler getWarningMessageDetailHandler = new GetWarningMessageDetailHandler(){
        @Override
        public void handle(TerminalMessage terminalMessage){
            //组的警情消息都是合成作战组消息
            for(TerminalMessage next : terminalMessageData){
                if(TerminalMessageUtil.isSameGroupMessage(next, terminalMessage) || TerminalMessageUtil.isSameMemberMessage(next, terminalMessage)){
                    if(next.messageVersion >= terminalMessage.messageVersion){
                        return;
                    }
                }
            }
            if(TerminalMessageUtil.isGroupMeaage(terminalMessage)){

                //添加到合成作战组
                if(GroupUtils.isCombatGroup(terminalMessage.messageToId)){
                    //如果是合成作战组存一下标记
                    terminalMessage.messageBody.put(JsonParam.COMBAT_GROUP,true);
                    saveHelpCombatMessage(terminalMessage, false);
                    saveHelpCombatMessageToSql(terminalMessage);
                }else {
                    saveMessageToList(terminalMessage,false);
                }
            }else {
                //个人的警情消息放到警情列表里面
                saveWarningMessage(terminalMessage,false);
            }
            mHandler.post(() -> {
                clearData();
                addData(terminalMessageData);
                sortMessageList();
                unReadCountChanged();
            });
        }
    };

    /**  接收消息 **/
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = terminalMessage -> {
        synchronized(NewsFragment.this){
            terminalMessageData.clear();
            terminalMessageData.addAll(messageList);
            for(TerminalMessage next : terminalMessageData){
                if(TerminalMessageUtil.isSameGroupMessage(next, terminalMessage) || TerminalMessageUtil.isSameMemberMessage(next, terminalMessage)){
                    if(next.messageVersion >= terminalMessage.messageVersion){
                        return;
                    }
                }
            }
            if (terminalMessage.messageFromId == terminalMessage.messageToId
                    && terminalMessage.messageToId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){//主动播的消息，存入视频助手
                if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() &&
                        (terminalMessage.messageBody.getIntValue("remark") == Remark.ACTIVE_VIDEO_LIVE || terminalMessage.messageBody.getIntValue("remark") == Remark.ASK_VIDEO_LIVE)
                        && terminalMessage.resultCode==0) {
                    saveVideoMessage(terminalMessage,false);
//                }else{
//                    saveMessageToList(terminalMessage,false);
                }
            }else if (TerminalMessageUtil.isGroupMeaage(terminalMessage)) {
                if(terminalMessage.messageType != MessageType.WARNING_INSTANCE.getCode()){
                    //合成作战组消息，只存一个条目
                    if(GroupUtils.isCombatGroup(terminalMessage.messageToId)){
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
                    saveMessageToList(terminalMessage,false);
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
            terminalMessage.unReadCount+=1;
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

    private void saveHistoryHelpCombatMessageToSql(int combatGroupId) {
        TerminalMessage handleMessage = null;
        //从处理中变为处理完成；从处理列表移除
        List<TerminalMessage> combatMessageList = TerminalFactory.getSDK().getTerminalMessageManager().getCombatMessageList();
        Iterator<TerminalMessage> iterator = combatMessageList.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode() && //合成作战组消息，只存一个条目
                    TerminalMessageUtil.isCombatGroup(next)&&
                    next.messageToId == combatGroupId){
                handleMessage = next;
                iterator.remove();
                break;
            }
        }
        MyTerminalFactory.getSDK().getTerminalMessageManager().updateCombatMessageList(combatMessageList);
        //添加到历史列表
        if (handleMessage != null){
            List<TerminalMessage> historyCombatMessageList = TerminalFactory.getSDK().getTerminalMessageManager().getHistoryCombatMessageList();
            historyCombatMessageList.add(handleMessage);
            MyTerminalFactory.getSDK().getTerminalMessageManager().updateHistoryCombatMessageList(historyCombatMessageList);
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
        whetherUnReadAdd(unReadCount, terminalMessage1,clearUnread,0);
        terminalMessageData.add(terminalMessage1);
    }

    /**  下载原图  **/
    @SuppressWarnings("unused")
    private void downloadDistinctPhoto (TerminalMessage terminalMessage) {
        MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
        MyTerminalFactory.getSDK().download(terminalMessage, true);
    }

    /**
     * 是否往消息未读里加1
     * @param unReadCount 原来未读条数
     * @param terminalMessage 新消息
     */
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
                    if(ActivityCollector.isActivityExist(CombatGroupActivity.class) ||
                            ActivityCollector.isActivityExist(HistoryCombatGroupActivity.class)){
                        terminalMessage.unReadCount = 0;
                    }else {
                        setGroupMessageUnReadCount(unReadCount, terminalMessage,tempGroupMessageVersion);
                    }
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
                    int liveNo = Util.stringToInt(terminalMessage.messageBody.getString(JsonParam.LIVERNO));
                    if (TextUtils.isEmpty(liver)){
                        terminalMessage.unReadCount = unReadCount;
                    }else{
                        if (liveNo == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
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
        List<Integer> monitorGroupNo = TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo();
        if (terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){//组呼消息
            if (terminalMessage.isOffLineMessage) {//离线的组呼也是未读
//                terminalMessage.unReadCount = unReadCount + 1;
                terminalMessage.messageBody.put(JsonParam.UNREAD, true);
            } else {
                if(terminalMessage.messageToId == currentGroupId){//是当前值
//                    terminalMessage.unReadCount = unReadCount;
                    terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                }else{//不是当前值
                    boolean isScanGroup = false;
                    for (Integer integer : monitorGroupNo){
                        if (integer == terminalMessage.messageToId){//是扫描的组
                            isScanGroup = true;
                            break;
                        }
                    }
                    if (isScanGroup){//在组扫描列表中
//                        terminalMessage.unReadCount = unReadCount;
                        terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                    }else {
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

    /**
     * 获取组消息的未读数量
     * @param unReadCount
     * @param terminalMessage
     * @param tempGroupMessageVersion
     * @return
     */
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

    private void removeCurrentResponseMessage(int responseGroupId){
        synchronized(NewsFragment.this){
            Iterator<TerminalMessage> iterator = messageList.iterator();
            while(iterator.hasNext()){
                TerminalMessage next = iterator.next();
                if(next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                    if(responseGroupId == next.messageToId){
                        iterator.remove();
                    }
                }
            }
            mMessageListAdapter.notifyDataSetChanged();
        }
    }

    private ReceivePersonMessageNotifyDateHandler receivePersonMessageNotifyDateHandler = new ReceivePersonMessageNotifyDateHandler(){
        @Override
        public void handler(int resultCode, String resultDes){
            if(resultCode ==0){
                mHandler.post(() -> mMessageListAdapter.notifyDataSetChanged());
            }
        }
    };

    private GetAllMessageRecordHandler getAllMessageRecordHandler = messageRecord -> {
        //加上同步，防止更新消息时又来新的消息，导致错乱
        synchronized(NewsFragment.this){
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
                    if (terminalMessage.messageFromId == terminalMessage.messageToId
                            && terminalMessage.messageToId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){//主动播的消息，存入视频助手
                        if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() &&
                                (terminalMessage.messageBody.getIntValue("remark") == Remark.ACTIVE_VIDEO_LIVE
                                        || terminalMessage.messageBody.getIntValue("remark") == Remark.ASK_VIDEO_LIVE)
                                && terminalMessage.resultCode==0) {
                            saveVideoMessage(terminalMessage,true);
                        }
                    }else {
                        saveMessageToList(terminalMessage,true);
                    }
                }
                mHandler.post(() -> {
                    clearData();
                    addData(terminalMessageData);
//                    sortMessageList();
                    sortFirstMessageList();
                    updateFrequentMembers();
                    unReadCountChanged();
                    //通知notification
                    for(int i = messageList.size()-1; 0 <=i; i--){
                        TerminalMessage terminalMessage = messageList.get(i);
                        //如果当前组是new出来的新消息messageFromId默认为0
                        if(terminalMessage.messageFromId != 0 && terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                            generateNotification(terminalMessage,i);
                        }
                    }
                });
            }
        }
    };

    private void updateFrequentMembers(){
        if(messageList.size()>0){
            List<Integer> frequentMemberNos = new ArrayList<>(5);
            for(int i = 0; i < messageList.size(); i++){
                TerminalMessage terminalMessage = messageList.get(i);
                if(terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){
                    int no = TerminalMessageUtil.getNo(terminalMessage);
                    if(no !=0){
                        frequentMemberNos.add(no);
                    }
                }
            }
            //只取最后五个不重复的
            if(cn.vsx.hamster.terminalsdk.tools.DataUtil.removeDuplicate(frequentMemberNos).size() > 5){
                frequentMemberNos.subList(frequentMemberNos.size()-5,frequentMemberNos.size());
            }
            if(!frequentMemberNos.isEmpty()){
                TerminalFactory.getSDK().getConfigManager().updateFrequentMember(frequentMemberNos);
            }
        }
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
            whetherUnReadAdd(unReadCount, terminalMessage,clearUnread,tempGroupMessageVersion);
            terminalMessageData.add(terminalMessage);
        }
    }

    private void saveWarningMessage(TerminalMessage terminalMessage,boolean clearUnread){
        int unReadCount = 0;
        final Iterator<TerminalMessage> iterator = terminalMessageData.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if(next.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode() && next.messageType == MessageType.WARNING_INSTANCE.getCode()){
                unReadCount = next.unReadCount;
                iterator.remove();
                break;
            }
        }
        whetherUnReadAdd(unReadCount, terminalMessage,clearUnread,0);
        terminalMessageData.add(terminalMessage);
    }

    private void saveVideoMessage(TerminalMessage terminalMessage,boolean clearUnread){
        int unReadCount = 0;
        final Iterator<TerminalMessage> iterator = terminalMessageData.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageFromId == next.messageToId && next.messageFromId == terminalMessage.messageToId &&
                    next.messageToId == terminalMessage.messageFromId){
                unReadCount = next.unReadCount;
                iterator.remove();
                break;
            }
        }
        terminalMessageData.add(terminalMessage);
        whetherUnReadAdd(unReadCount, terminalMessage,clearUnread,0);
    }

    private void generateNotification(TerminalMessage terminalMessage,int position){
        //通知栏标题
        String noticeTitle;
        //通知栏内容
        String noticeContent=null;
        //通知Id
        int noticeId ;

        int unReadCount = terminalMessage.unReadCount;
        String unReadCountText;


        if (terminalMessage.messageCategory== MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个呼
            noticeTitle=terminalMessage.messageFromName;
            noticeId=terminalMessage.messageFromId;

        }else {//组呼
            noticeTitle=terminalMessage.messageToName;
            noticeId=terminalMessage.messageToId;

            if (GroupCallNewsActivity.mGroupId==terminalMessage.messageToId){
                return;
            }

        }

        if (unReadCount>99){
            unReadCountText="[99+条] ";
        }else if (unReadCount<=0){
            unReadCountText=" ";
        }else {
            unReadCountText="["+unReadCount+"条] ";
        }


        if(terminalMessage.messageType ==  MessageType.SHORT_TEXT.getCode()) {
            String content = terminalMessage.messageBody.getString(JsonParam.CONTENT);
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+content;
            }else {
                noticeContent=content;
            }
        }

        if(terminalMessage.messageType ==  MessageType.LONG_TEXT.getCode()) {
            String path = terminalMessage.messagePath;
            File file = new File(path);
            if (!file.exists()) {
                MyTerminalFactory.getSDK().getTerminalMessageManager().setMessagePath(terminalMessage, false);
                MyTerminalFactory.getSDK().download(terminalMessage, true);
            }
            String content = FileUtil.getStringFromFile(file);
            logger.info("长文本： path:"+path+"    content:"+content);
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_text_),terminalMessage.messageFromName,content);
            } else {
                noticeContent=content;
            }
        }

        if(terminalMessage.messageType ==  MessageType.PICTURE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_picture_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_picture);
            }
        }
        if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_voice_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_voice);
            }
        }
        if(terminalMessage.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_video_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_video);
            }
        }
        if(terminalMessage.messageType ==  MessageType.FILE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_file_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_file);
            }
        }
        if(terminalMessage.messageType ==  MessageType.POSITION.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_location_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_location);
            }
        }
        if(terminalMessage.messageType ==  MessageType.AFFICHE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_notice_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_notice);
            }
        }
        if(terminalMessage.messageType ==  MessageType.WARNING_INSTANCE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_warning_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_warning);
            }
        }
        if(terminalMessage.messageType ==  MessageType.PRIVATE_CALL.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_personal_call_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_personal_call);
            }
        }
        if(terminalMessage.messageType ==  MessageType.VIDEO_LIVE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_image_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_image);
            }
        }
        if(terminalMessage.messageType ==  MessageType.GROUP_CALL.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_group_call_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_group_call);
            }
        }
        if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_sound_recording_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_sound_recording);
            }
        }
        if(terminalMessage.messageType ==  MessageType.HYPERLINK.getCode()) {//人脸识别
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_face_recognition_),terminalMessage.messageFromName);
            } else {
                noticeContent=getString(R.string.text_message_list_face_recognition);
            }
        }
        //合并转发
        if(terminalMessage.messageType ==  MessageType.MERGE_TRANSMIT.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=String.format(getString(R.string.text_message_list_merge_transmit_),terminalMessage.messageFromName);
            }else{
                noticeContent=getString(R.string.text_message_list_merge_transmit);
            }

        }

        Intent intent=new Intent(getActivity(), NotificationClickReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("TerminalMessage",terminalMessage);
        intent.putExtra("bundle",bundle);
        PendingIntent pIntent=PendingIntent.getBroadcast(getActivity(),noticeId,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager=(NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder myBuilder = new Notification.Builder(MyApplication.instance.getApplicationContext());
        myBuilder.setContentTitle(noticeTitle)//设置通知标题
                .setContentText(unReadCountText+noticeContent)//设置通知内容
                .setTicker(getString(R.string.text_you_has_a_new_message))//设置状态栏提示消息
                .setSmallIcon(R.drawable.pttpdt)//设置通知图标
                .setAutoCancel(true)//点击后取消
                .setWhen(System.currentTimeMillis())//设置通知时间
                .setPriority(Notification.PRIORITY_HIGH)//高优先级
                .setContentIntent(pIntent); //设置通知点击事件

        if(position ==messageList.size()-1){
            myBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //设置任何情况都会显示通知
            myBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        Notification notification = myBuilder.build();
        //通过通知管理器来发起通知，ID区分通知
        if(notificationManager != null){
            notificationManager.notify(noticeId, notification);
        }
    }

    private ReceiveUnreadMessageChangedHandler receiveUnreadMessageChangedHandler = terminalMessage -> {
        for(TerminalMessage message : messageList){
            if(message.messageFromId == terminalMessage.messageFromId  && message.messageToId == terminalMessage.messageToId){
                message.unReadCount = 0;
                break;
            }
        }
        saveMessagesToSql();
        unReadCountChanged();
    };
    /**
     * 网络状态变化
     */
    private ReceiveOnLineStatusChangedHandler receiveOnLineStatusChangedHandler = connected -> {
        if(connected){
            MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecordNewMethod(messageList);
        }
    };

    /***  删除消息   **/
    private ReceiverDeleteMessageHandler mReceiverDeleteMessageHandler = () -> {
        TerminalMessage terminalMessage = messageList.get(deletePos);
        int myId = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()) {
            if (isReceiver) {//接收的消息
                MyTerminalFactory.getSDK().getTerminalMessageManager().deleteMessageFromSQLite(MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), terminalMessage.messageFromId, myId);
            } else {//自己发的
                MyTerminalFactory.getSDK().getTerminalMessageManager().deleteMessageFromSQLite(MessageCategory.MESSAGE_TO_PERSONAGE.getCode(), terminalMessage.messageToId, myId);
            }
        }else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
            MyTerminalFactory.getSDK().getTerminalMessageManager().deleteMessageFromSQLite(MessageCategory.MESSAGE_TO_GROUP.getCode(), terminalMessage.messageToId, myId);
        }
        removeData(deletePos);
//        sortMessageList();
        sortFirstMessageList();
        unReadCountChanged();
        deletePos = -1;
    };

    /***  切组后的消息 **/
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            mHandler.post(() -> {
                if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                    int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    Group targetGroup = DataUtil.getGroupByGroupNo(currentGroupId);
                    setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
//                    sortMessageList();
                    sortFirstMessageList();
                }
            });
        }
    };


    private ReceiveMemberAboutTempGroupHandler receiveMemberAboutTempGroupHandler = new ReceiveMemberAboutTempGroupHandler(){
        @Override
        public void handler(boolean isAdd, boolean isLocked, boolean isScan, boolean isSwitch, int tempGroupNo, String tempGroupName, String tempGroupType){
            if(isAdd){
                if(isLocked || isSwitch){
                    mHandler.post(()-> setting_group_name.setText(tempGroupName));
                }

            }else {
                int currentGroupId = TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                mHandler.post(()-> setting_group_name.setText(DataUtil.getGroupName(currentGroupId)));
                //合成作战组处理完成后，刷新完成列表
                if (TempGroupType.TO_HELP_COMBAT.toString().equals(tempGroupType)) {
                    saveHistoryHelpCombatMessageToSql(tempGroupNo);
                }
            }
        }
    };

    /**强制切组*/
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId,boolean forceSwitchGroup,String tempGroupType) {
            if(!forceSwitchGroup){
                return;
            }
            mHandler.post(() -> {
                int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
//                sortMessageList();
                sortFirstMessageList();
            });
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

    /**收到修改名字成功的消息*/
    private ReceiveChangeNameHandler receiveChangeNameHandler = (resultCode, memberId, newMemberName) -> mHandler.post(() -> {
        //遍历消息列表，把名字改过来
        for (TerminalMessage terminalMessage : messageList){
            final boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);//是否为别人发的消息
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){
                if (isReceiver){//接收的消息
                    if (terminalMessage.messageFromId == memberId){
                        terminalMessage.messageFromName = newMemberName;
                    }
                }else {
                    if (terminalMessage.messageToId == memberId){
                        terminalMessage.messageToName = newMemberName;
                    }
                }
            }else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                if (terminalMessage.messageFromId == memberId){
                    terminalMessage.messageFromName = newMemberName;
                }
            }
        }
//        sortMessageList();
        sortFirstMessageList();
    });

    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler() {
        @Override
        public void handler() {
            mHandler.post(() -> {
                int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                setting_group_name.setText(DataUtil.getGroupName(currentGroupId));
                for (TerminalMessage terminalMessage : messageList) {
                    if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组消息
                        terminalMessage.messageToName = DataUtil.getGroupName(terminalMessage.messageToId);
                    }
                }
                loadMessages();
                //判断是否插入一条电话助手消息
                SharedPreferences preferences = context.getSharedPreferences("CallRecord", MODE_PRIVATE);
                isFirstCall = preferences.getBoolean("isFirstCall", false);
                logger.info("loadMessages()====="+"isFirstCall "+isFirstCall);
                if(isFirstCall){
                    TerminalMessage terminalMessage = new TerminalMessage();
                    terminalMessage.messageType=MessageType.CALL_RECORD.getCode();
                    terminalMessage.messageCategory=MessageCategory.MESSAGE_TO_PERSONAGE.getCode();
                    addData(terminalMessage);
                    SharedPreferences.Editor editor = context.getSharedPreferences("CallRecord",
                            MODE_PRIVATE).edit();
                    editor.putBoolean("isFirstCall", false);
                    editor.commit();
                }

//                sortMessageList();
                sortFirstMessageList();
            });
        }
    };

    /** 文件等下载完成的监听handler */
    private ReceiveDownloadFinishHandler receiveDownloadFinishHandler = (terminalMessage, success) -> mHandler.post(() -> {
        if(terminalMessage.messageType ==  MessageType.LONG_TEXT.getCode()) {
//            sortMessageList();
            sortFirstMessageList();
        }
    });

    /**
     * 撤回消息
     **/
    private ReceiveResponseRecallRecordHandler mReceiveResponseRecallRecordHandler = (resultCode, resultDesc, messageId,messageBodyId) -> {
        if(resultCode == 0){
            updataMessageWithDrawState(messageId,messageBodyId);
        }
    };

    /**
     * 收到别人撤回消息的通知
     **/
    private ReceiveNotifyRecallRecordHandler mNotifyRecallRecordMessageHandler = (version, messageId,messageBodyId) -> {
         updataMessageWithDrawState(messageId,messageBodyId);
    };

    /**
     * 更新消息的撤回状态
     * @param messageId
     */
    private void updataMessageWithDrawState(long messageId,String messageBodyId){
        if(TextUtils.isEmpty(messageBodyId)){
            return;
        }
        for (TerminalMessage message1: messageList) {
            if(TextUtils.equals(messageBodyId,message1.messageBodyId)){
                message1.messageStatus = MessageStatus.MESSAGE_RECALL.toString();
            }
        }
        //更新UI
        mHandler.post(() ->{
            if (mMessageListAdapter != null) {
                mMessageListAdapter.notifyDataSetChanged();
            }
        });
        //更新消息的撤回状态
        TerminalFactory.getSDK().getSQLiteDBManager().updateTerminalMessageWithDraw(messageBodyId,MessageStatus.MESSAGE_RECALL.getCode());
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
                if(!DataUtil.isExistGroup(next.messageToId)){
                    //说明组列表中没有这个组了
                    iterator.remove();//消息列表中移除
                }else {
                    next.messageToName = DataUtil.getGroupName(next.messageToId);
                }
            }
        }
    }

    /**
     * 去掉在通讯录里不存在的人，暂时没用到此方法
     */
    @SuppressWarnings("unused")
    private void setNewMemberList() {
        Iterator<TerminalMessage> iterator = messageList.iterator();
        while (iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if (next.messageCategory == MessageCategory.MESSAGE_TO_PERSONAGE.getCode()){//个人消息
                Member memberInfo = DataUtil.getMemberInfoByMemberNo(next.messageFromId);
                if (memberInfo == null){//说明成员列表中没有这个人了
                    iterator.remove();//消息列表中移除
                }else {
                    next.messageFromName = memberInfo.getName();
                }
            }
        }
    }

    private void clearData(){
        synchronized(NewsFragment.this){
            messageList.clear();
        }
    }

    private void removeData(int position){
        synchronized(NewsFragment.this){
            messageList.remove(position);
        }
    }

    private void addData(List<TerminalMessage> terminalMessages){
        synchronized(NewsFragment.this){
            messageList.addAll(terminalMessages);
        }
    }

    private void addData(TerminalMessage terminalMessage){
        synchronized(NewsFragment.this){
            messageList.add(terminalMessage);
        }
    }
    private void addData(int position ,TerminalMessage terminalMessage){
        synchronized(NewsFragment.this){
            messageList.add(position,terminalMessage);
        }
    }

    /**
     * 给响应组排序
     */
    private void sortResponseGroup(){
        List<TerminalMessage> tempList = new ArrayList<>();
        Iterator<TerminalMessage> iterator = messageList.iterator();
        while(iterator.hasNext()){
            TerminalMessage next = iterator.next();
            if(!DataUtil.isExistGroup(next.messageToId)){
                //说明组列表中没有这个组了
                iterator.remove();//消息列表中移除
                continue;
            }
            if(next.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){
                Group groupInfo = DataUtil.getGroupByGroupNo(next.messageToId);
                if(groupInfo.getResponseGroupType()!=null && groupInfo.getResponseGroupType().equals(ResponseGroupType.RESPONSE_TRUE.toString())){
                    tempList.add(next);
                }else {
                    iterator.remove();
                }
            }
        }
        Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
        Collections.sort(tempList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
        addData(tempList);
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
            if(mMessageListAdapter !=null){
                mMessageListAdapter.notifyDataSetChanged();
            }
        }else {
            addMainGroupMessage();
        }
    }

    /**
     * 对聊天列表排序
     */
    private void sortMessageList(){
        synchronized(NewsFragment.this){
            if(!messageList.isEmpty()){
//                setNewGroupList();
                //            setNewMemberList();
                //再按照时间来排序
                Collections.sort(messageList, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
                //再设置第一条消息，一般是当前组
                setFirstMessage();
                //再保存到数据库
                saveMessagesToSql();
                if(mMessageListAdapter !=null){
                    Log.e("NewsFragment", "messageList:" + messageList);
                    mMessageListAdapter.notifyDataSetChanged();
                }
            }else {
                addMainGroupMessage();
            }
        }
    }
}
