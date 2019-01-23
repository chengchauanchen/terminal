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
import android.text.TextUtils;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.GroupType;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.errcode.module.SignalServerErrorCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.GetAllMessageRecordHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCeaseGroupCallConformationHander;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangeNameHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveForceChangeGroupHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberDeleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePersonMessageNotifyDateHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveRequestGroupCallConformationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUnreadMessageChangedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateFoldersAndGroupsHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveVolumeOffCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
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
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.view.custompopupwindow.MyTopRightMenu;
import ptt.terminalsdk.context.MyTerminalFactory;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static cn.vsx.hamster.terminalsdk.manager.groupcall.GroupCallListenState.LISTENING;

@SuppressLint("ValidFragment")
public class NewsFragment extends BaseFragment {

    @Bind(R.id.news_list)
    ListView newsList;
    @Bind(R.id.add_icon)
    ImageView add_icon;
    @Bind(R.id.setting_group_name)
    TextView setting_group_name;
    @Bind(R.id.icon_laba)
    ImageView icon_laba;
    @Bind(R.id.speaking_name)
    TextView speaking_name;
    @Bind(R.id.voice_image)
    ImageView voice_image;

    private int deletePos = -1 ;
    private MessageListAdapter mMessageListAdapter;
    private Handler mHandler = new Handler();
    //消息列表，在子线程中使用
    private List<TerminalMessage> terminalMessageData = new ArrayList<>();
    //界面显示的消息列表
    private ArrayList<TerminalMessage> messageList = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, String> idNameMap = TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>());
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
            addCurrentGroupMessage();
        }else{//有列表
            //列表中有当前组，置顶当前组
            if(haveCurrentGroupMessage()) {
                stickCurrentGroupMessage();
            }else {//列表中无当前组，添加
                addCurrentGroupMessage();
            }
        }
    }

    /**
     * 添加当前组消息，如果没有就new一个新消息，如果有就取最后一条
     */
    private void addCurrentGroupMessage() {
        //查看当前组的全部消息
        List<TerminalMessage> groupMessageRecord = TerminalFactory.getSDK().getTerminalMessageManager().getGroupMessageRecord(
                MessageCategory.MESSAGE_TO_GROUP.getCode(), TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0),
                0, 0);
        TerminalMessage terminalMessage;
        if (groupMessageRecord.size() == 0) {
            terminalMessage = new TerminalMessage();
            terminalMessage.messageToId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
            terminalMessage.messageToName = DataUtil.getGroupByGroupNo(terminalMessage.messageToId).name;
            terminalMessage.messageCategory = MessageCategory.MESSAGE_TO_GROUP.getCode();
            saveMemberMap(terminalMessage);
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
            saveMemberMap(currentGroupMessage);
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
        setVideoIcon();
        setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0)).name);
        voice_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        MyTerminalFactory.getSDK().registReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
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
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(mReceiverDeleteMessageHandler);
        OperateReceiveHandlerUtilSync.getInstance().registReceiveHandler(receiveVolumeOffCallHandler);

    }

    @Override
    public void initData() {
        loadMessages();
        mMessageListAdapter = new MessageListAdapter(getContext(), messageList, idNameMap);
        newsList.setAdapter(mMessageListAdapter);
        MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
            @Override
            public void run(){
                MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecord();
            }
        });
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
        ButterKnife.unbind(this);
        mHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePersonMessageNotifyDateHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(getAllMessageRecordHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUnreadMessageChangedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveServerConnectionEstablishedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveNotifyDataMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveForceChangeGroupHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberDeleteHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveChangeNameHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateFoldersAndGroupsHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveDownloadFinishHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(mReceiveRequestGroupCallConformationHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCeaseGroupCallConformationHander);
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

            if(isVolumeOff||MyTerminalFactory.getSDK().getAudioProxy().getVolume()==0){
                voice_image.setImageResource(R.drawable.volume_off_call);
                soundOff=true;
            }else {
                voice_image.setImageResource(R.drawable.horn);
                soundOff=false;
            }
        }
    };
    private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
        @Override
        public void handler() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setVideoIcon();
                }
            });
        }
    };

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
            if(terminalMessage.unReadCount!=0){
                terminalMessage.unReadCount = 0;
                unReadCountChanged();//未读消息数变了，通知tab
                saveMessagesToSql();
            }
            boolean isReceiver = terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);//是否为别人发的消息
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
                intent.putExtra("isGroup", false);
                if (isReceiver) {
                    intent.putExtra("userId", terminalMessage.messageFromId);
                    intent.putExtra("userName", idNameMap.get(terminalMessage.messageFromId));
                } else {
                    intent.putExtra("userId", terminalMessage.messageToId);
                    intent.putExtra("userName", idNameMap.get(terminalMessage.messageToId));
                }
                context.startActivity(intent);
            }
            else if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//进入组会话页
                Intent intent = new Intent(context, GroupCallNewsActivity.class);
                intent.putExtra("isGroup", true);
                intent.putExtra("userId", terminalMessage.messageToId);//组id
                intent.putExtra("userName", idNameMap.get(terminalMessage.messageToId));
                intent.putExtra("speakingId",speakingId);
                intent.putExtra("speakingName",speakingName);
                context.startActivity(intent);
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
    private ReceiveRequestGroupCallConformationHandler mReceiveRequestGroupCallConformationHandler = new ReceiveRequestGroupCallConformationHandler() {
        @Override
        public void handler(final int methodResult, String resultDesc) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (methodResult == 0) {
                        showViewWhenGroupCall("我正在说话");
                        setViewEnable(false);
                    }
                }
            });
        }
    };

    /***  自己组呼结束 **/
    private ReceiveCeaseGroupCallConformationHander receiveCeaseGroupCallConformationHander = new ReceiveCeaseGroupCallConformationHander() {

        @Override
        public void handler(int resultCode, String resultDesc) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (MyApplication.instance.getGroupListenenState() == LISTENING) {
                        return;
                    }
                    hideViewWhenStopGroupCall();
                    setViewEnable(true);
                }
            });

        }
    };

    private int speakingId;
    private String speakingName;
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(int memberId, final String memberName, final int groupId, String groupName, CallMode currentCallMode) {
            if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_GROUP_LISTEN.name())){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        speakingId = groupId;
                        speakingName = memberName;
                        logger.info("sjl_消息页面的组呼到来");
                        logger.info("sjl_消息页面的组呼到来"+speaking_name.getVisibility()+",正在说话人的名字："+MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, ""));
                        String speakingName = MyTerminalFactory.getSDK().getParam(Params.CURRENT_SPEAKER, "");
                        showViewWhenGroupCall(speakingName);
                        setting_group_name.setText(DataUtil.getGroupByGroupNo(groupId).name);
                    }
                });
            }

        }
    };
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler(){

        @Override
        public void handler(int reasonCode) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideViewWhenStopGroupCall();
                    setViewEnable(true);
                    setting_group_name.setText(DataUtil.getGroupByGroupNo(MyTerminalFactory.getSDK()
                            .getParam(Params.CURRENT_GROUP_ID, 0)).name);
                }
            });
        }
    };

    /**  接收消息 **/
    private ReceiveNotifyDataMessageHandler mReceiveNotifyDataMessageHandler = new ReceiveNotifyDataMessageHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage) {
            synchronized(NewsFragment.this){
                idNameMap.put(terminalMessage.messageFromId, terminalMessage.messageFromName);
                idNameMap.put(terminalMessage.messageToId, terminalMessage.messageToName);
                terminalMessageData.clear();
                terminalMessageData.addAll(messageList);
                if (terminalMessage.messageFromId == terminalMessage.messageToId
                        && terminalMessage.messageToId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){//主动播的消息，存入视频助手
                    if (terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode() &&
                            (terminalMessage.messageBody.getIntValue("remark") == Remark.ACTIVE_VIDEO_LIVE || terminalMessage.messageBody.getIntValue("remark") == Remark.ASK_VIDEO_LIVE)
                            && terminalMessage.resultCode==0) {
                        saveVideoMessage(terminalMessage,false);
                    }
                }else {
                    saveMessageToList(terminalMessage,false);
                }
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        clearData();
                        addData(terminalMessageData);
                        sortMessageList();
                        unReadCountChanged();
                    }
                });
            }
        }
    };

    /**
     * 根据id保存名字
     * @param terminalMessage 消息
     */
    @SuppressLint("UseSparseArrays")
    private void saveMemberMap(TerminalMessage terminalMessage) {
        idNameMap.putAll(TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>()));
        if(terminalMessage.messageFromId !=0){
            idNameMap.put(terminalMessage.messageFromId, terminalMessage.messageFromName);
        }
        if(terminalMessage.messageToId !=0){
            idNameMap.put(terminalMessage.messageToId, terminalMessage.messageToName);
        }
        TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
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
    private void whetherUnReadAdd(int unReadCount, TerminalMessage terminalMessage,boolean clearUnread) {
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
                        setGroupMessageUnReadCount(unReadCount, terminalMessage);
                    }
                }else {//组会话页关闭， 通知
                    setGroupMessageUnReadCount(unReadCount, terminalMessage);
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

    private void setGroupMessageUnReadCount(int unReadCount, TerminalMessage terminalMessage) {
        int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);//当前组id
        List<Integer> scanGroups = MyTerminalFactory.getSDK().getConfigManager().loadScanGroup();//组扫描列表
        boolean groupScanTog = MyTerminalFactory.getSDK().getParam(Params.GROUP_SCAN, false);//组扫描开关
        if (terminalMessage.messageType == MessageType.GROUP_CALL.getCode()){//组呼消息
            if (terminalMessage.isOffLineMessage) {//离线的组呼也是未读
                terminalMessage.unReadCount = unReadCount + 1;
                terminalMessage.messageBody.put(JsonParam.UNREAD, true);
            } else {
                if(terminalMessage.messageToId == currentGroupId){//是当前值
                    terminalMessage.unReadCount = unReadCount;
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
                            terminalMessage.unReadCount = unReadCount;
                            terminalMessage.messageBody.put(JsonParam.UNREAD, false);
                        }else {
                            terminalMessage.unReadCount = unReadCount + 1;
                            terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                        }
                    }else {//组扫描关着，判断主组状态
                        terminalMessage.unReadCount = unReadCount + 1;
                        terminalMessage.messageBody.put(JsonParam.UNREAD, true);
                    }
                }
            }

        }else {//其它类型的消息
            terminalMessage.unReadCount = unReadCount + 1;
        }
    }

    private ReceivePersonMessageNotifyDateHandler receivePersonMessageNotifyDateHandler = new ReceivePersonMessageNotifyDateHandler(){
        @Override
        public void handler(int resultCode, String resultDes){
            if(resultCode ==0){
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        mMessageListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    };

    private GetAllMessageRecordHandler getAllMessageRecordHandler = new GetAllMessageRecordHandler(){
        @Override
        public void handle(List<TerminalMessage> messageRecord){
            //加上同步，防止更新消息时又来新的消息，导致错乱
            synchronized(NewsFragment.this){
                //更新未读消息和聊天界面
                if(messageRecord.isEmpty()){
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            sortMessageList();
                            unReadCountChanged();
                        }
                    });
                }else {
                    terminalMessageData.clear();
                    terminalMessageData.addAll(messageList);
                    for(TerminalMessage terminalMessage : messageRecord){
                        saveMemberMap(terminalMessage);
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
                    mHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            clearData();
                            addData(terminalMessageData);
                            sortMessageList();
                            unReadCountChanged();
                            //通知notification
                            for(int i = messageList.size()-1; 0 <=i; i--){
                                TerminalMessage terminalMessage = messageList.get(i);
                                //如果当前组是new出来的新消息messageFromId默认为0
                                if(terminalMessage.messageFromId != 0 && terminalMessage.messageFromId != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                                    generateNotification(terminalMessage,i);
                                }
                            }
                        }
                    });
                }
            }
        }
    };

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
                }
                if(isRemove){
                    unReadCount = next.unReadCount;
                    iterator.remove();
                }
            }
            //未读消息条目是否+1
            whetherUnReadAdd(unReadCount, terminalMessage,clearUnread);
            terminalMessageData.add(terminalMessage);
        }
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
        whetherUnReadAdd(unReadCount, terminalMessage,clearUnread);
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
                noticeContent=terminalMessage.messageFromName+":"+content;
            } else {
                noticeContent=content;
            }
        }

        if(terminalMessage.messageType ==  MessageType.PICTURE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[图片]";
            } else {
                noticeContent="[图片]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[语音]";
            } else {
                noticeContent="[语音]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.VIDEO_CLIPS.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[视频]";
            } else {
                noticeContent="[视频]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.FILE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[文件]";
            } else {
                noticeContent="[文件]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.POSITION.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[位置]";
            } else {
                noticeContent="[位置]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.AFFICHE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[公告]";
            } else {
                noticeContent="[公告]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.WARNING_INSTANCE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[警情]";
            } else {
                noticeContent="[警情]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.PRIVATE_CALL.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[个呼]";
            } else {
                noticeContent="[个呼]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.VIDEO_LIVE.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[图像]";
            } else {
                noticeContent="[图像]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.GROUP_CALL.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[组呼]";
            } else {
                noticeContent="[组呼]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.AUDIO.getCode()) {
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[录音]";
            } else {
                noticeContent="[录音]";
            }
        }
        if(terminalMessage.messageType ==  MessageType.HYPERLINK.getCode()) {//人脸识别
            if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()) {
                noticeContent=terminalMessage.messageFromName+":"+"[人脸识别]";
            } else {
                noticeContent="[人脸识别]";
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
                .setTicker("您有一条新消息！")//设置状态栏提示消息
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

    private ReceiveUnreadMessageChangedHandler receiveUnreadMessageChangedHandler = new ReceiveUnreadMessageChangedHandler(){
        @Override
        public void handle(TerminalMessage terminalMessage){
            for(TerminalMessage message : messageList){
                if(message.messageFromId == terminalMessage.messageFromId  && message.messageToId == terminalMessage.messageToId){
                    message.unReadCount = 0;
                    break;
                }
            }
            saveMessagesToSql();
            unReadCountChanged();
        }
    };
    /**
     * 网络状态变化
     */
    private ReceiveServerConnectionEstablishedHandler receiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {

        @Override
        public void handler(final boolean connected) {
            if(connected){
                MyTerminalFactory.getSDK().getTerminalMessageManager().getAllMessageRecord();
            }
        }
    };

    /***  删除消息   **/
    private ReceiverDeleteMessageHandler mReceiverDeleteMessageHandler = new ReceiverDeleteMessageHandler() {
        @Override
        public void handler() {
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
            sortMessageList();
            unReadCountChanged();
            deletePos = -1;
        }
    };

    /***  切组后的消息 **/
    private ReceiveChangeGroupHandler receiveChangeGroupHandler = new ReceiveChangeGroupHandler() {
        @Override
        public void handler(final int errorCode, String errorDesc) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (errorCode == BaseCommonCode.SUCCESS_CODE) {
                        int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                        setting_group_name.setText(DataUtil.getGroupByGroupNo(currentGroupId).name);
                        sortMessageList();
                    }
                }
            });
        }
    };
    /**强制切组*/
    private ReceiveForceChangeGroupHandler receiveForceChangeGroupHandler = new ReceiveForceChangeGroupHandler() {
        @Override
        public void handler(int memberId, int toGroupId,boolean forceSwitchGroup,String tempGroupType) {
            if(!forceSwitchGroup){
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    setting_group_name.setText(DataUtil.getGroupByGroupNo(currentGroupId).name);
                    sortMessageList();
                }
            });
        }
    };
    //终端被删除
    private ReceiveMemberDeleteHandler receiveMemberDeleteHandler = new ReceiveMemberDeleteHandler() {
        @Override
        public void handler() {
            mHandler.post(new Runnable(){
                @Override
                public void run(){
                    clearData();
                    saveMessagesToSql();
                    if(mMessageListAdapter != null){
                        mMessageListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    /**收到修改名字成功的消息*/
    private ReceiveChangeNameHandler receiveChangeNameHandler = new ReceiveChangeNameHandler(){
        @Override
        public void handler(final int resultCode, final int memberId, final String newMemberName) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
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
                        saveMemberMap(terminalMessage);
                    }
                    sortMessageList();
                }
            });
        }
    };

    /**
     * 更新文件夹和组列表数据
     */
    private ReceiveUpdateFoldersAndGroupsHandler receiveUpdateFoldersAndGroupsHandler = new ReceiveUpdateFoldersAndGroupsHandler() {
        @Override
        public void handler() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int currentGroupId = MyTerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0);
                    setting_group_name.setText(DataUtil.getGroupByGroupNo(currentGroupId).name);
                    for (TerminalMessage terminalMessage : messageList) {
                        if (terminalMessage.messageCategory == MessageCategory.MESSAGE_TO_GROUP.getCode()){//组消息
                            terminalMessage.messageToName = DataUtil.getGroupByGroupNo(terminalMessage.messageToId).name;
                            saveMemberMap(terminalMessage);
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

                    sortMessageList();
                }
            });
        }
    };

    /** 文件等下载完成的监听handler */
    private ReceiveDownloadFinishHandler receiveDownloadFinishHandler = new ReceiveDownloadFinishHandler() {
        @Override
        public void handler(final TerminalMessage terminalMessage,boolean success) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(terminalMessage.messageType ==  MessageType.LONG_TEXT.getCode()) {
                        sortMessageList();
                    }
                }
            });
        }
    };

    /**
     * 去掉在通讯录里不存在的组和响应组
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
                    removeMemberMap(next.messageToId);
                    continue;
                }else {
                    Group groupInfo = DataUtil.getGroupByGroupNo(next.messageToId);
                    next.messageToName = groupInfo.name;
                    saveMemberMap(next);
                }
                //去掉响应组
                if( DataUtil.getGroupByGroupNo(next.messageToId).getGroupType() == GroupType.RESPONSE){
                    iterator.remove();//消息列表中移除
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
                    removeMemberMap(next.messageFromId);
                }else {
                    next.messageFromName = memberInfo.getName();
                    saveMemberMap(next);
                }
            }
        }
    }

    @SuppressLint("UseSparseArrays")
    private void removeMemberMap(int id) {
        idNameMap.putAll(TerminalFactory.getSDK().getSerializable(Params.ID_NAME_MAP, new HashMap<Integer, String>()));
        idNameMap.remove(id);
        TerminalFactory.getSDK().putSerializable(Params.ID_NAME_MAP, idNameMap);
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
     * 对聊天列表排序
     */
    private void sortMessageList() {
        if(!messageList.isEmpty()){
            setNewGroupList();
//            setNewMemberList();
            //再按照时间来排序
            Collections.sort(messageList, new Comparator<TerminalMessage>() {
                @Override
                public int compare(TerminalMessage o1, TerminalMessage o2) {
                    return (o1.sendTime) > (o2.sendTime) ? -1 : 1;
                }
            });
            //再设置第一条消息，一般是当前组
            setFirstMessage();
            //再保存到数据库
            saveMessagesToSql();
            if(mMessageListAdapter !=null){
                mMessageListAdapter.notifyDataSetChanged();
            }
        }else {
            addCurrentGroupMessage();
        }
    }
}
