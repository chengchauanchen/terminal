package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.view.ISelectMemberView;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;
import com.vsxin.terminalpad.mvp.entity.InviteMemberLiverMember;
import com.vsxin.terminalpad.receiveHandler.ReceiveRemoveSelectedMemberHandler;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.MyDataUtil;
import com.vsxin.terminalpad.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.MessageSendStateEnum;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.ReceiveObjectMode;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.common.util.NoCodec;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by PC on 2018/11/1.
 */

public class SelectMemberPresenter extends BasePresenter<ISelectMemberView> {


    public SelectMemberPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 注册监听
     */
    public void registReceiveHandler() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupSelectedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveMemberSelectedHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveRemoveSelectedMemberHandler);
    }

    /**
     * 取消监听
     */
    public void unregistReceiveHandler() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupSelectedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveMemberSelectedHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveRemoveSelectedMemberHandler);
    }

    /**
     * 组列表的选择变化通知
     */
    private ReceiveGroupSelectedHandler receiveGroupSelectedHandler = (group, selected) -> {
        List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
        //不是同一个Group对象
        group.setChecked(selected);
        int index = -1;
        for (int i = 0; i < selectedMembers.size(); i++) {
            ContactItemBean bean = selectedMembers.get(i);
            if(bean.getType() == Constants.TYPE_GROUP){
                Group group1 = (Group) bean.getBean();
                if(group1!=null&&group1.getNo() == group.getNo()){
                    index = i;
                    break;
                }
            }
        }

        if(index>=0&&index < selectedMembers.size()){
            if(!group.isChecked()){
                selectedMembers.remove(index);
            }
        }else{
            if(group.isChecked()){
                ContactItemBean bean = new ContactItemBean();
                bean.setBean(group);
                bean.setType(Constants.TYPE_GROUP);
                selectedMembers.add(bean);
            }
        }
        getView().selectedView(selectedMembers);
    };

    /**
     * 成员列表的选择变化通知
     */
    private ReceiveMemberSelectedHandler receiveMemberSelectedHandler = (member, selected, type) -> {
        List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
        member.setChecked(selected);
        int index = -1;
        for (int i = 0; i < selectedMembers.size(); i++) {
            ContactItemBean bean = selectedMembers.get(i);
            if(bean.getType() == Constants.TYPE_USER){
                Member member1 = (Member) bean.getBean();
                if(member1!=null&&member1.getNo() == member.getNo()){
                    index = i;
                    break;
                }
            }
        }

        if(index>=0&&index < selectedMembers.size()){
            if(!member.isChecked()){
                selectedMembers.remove(index);
            }
        }else{
            if(member.isChecked()){
                ContactItemBean bean = new ContactItemBean();
                bean.setBean(member);
                bean.setType(Constants.TYPE_USER);
                selectedMembers.add(bean);
            }
        }
        getView().selectedView(selectedMembers);
    };

    /**
     * 删除已选择的成员通知
     */
    private ReceiveRemoveSelectedMemberHandler receiveRemoveSelectedMemberHandler = new ReceiveRemoveSelectedMemberHandler(){
        @Override
        public void handle(ContactItemBean contactItemBean){
            if(contactItemBean!=null){
                int type = contactItemBean.getType();
                List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
                for (int i = 0; i < selectedMembers.size(); i++) {
                    ContactItemBean bean = selectedMembers.get(i);
                    if(type == bean.getType()){
                        if(bean.getType() == Constants.TYPE_USER){
                            Member deleteMember = (Member) contactItemBean.getBean();
                            Member member = (Member) bean.getBean();
                            if(deleteMember!=null&&member!=null&&deleteMember.equals(member)){
                                selectedMembers.remove(i);
                                break;
                            }
                        }else if(bean.getType() == Constants.TYPE_GROUP){
                            Group deleteGroup = (Group) contactItemBean.getBean();
                            Group group = (Group) bean.getBean();
                            if(deleteGroup!=null&&group!=null&&deleteGroup.equals(group)){
                                selectedMembers.remove(i);
                                break;
                            }
                        }
                    }
                }
                getView().selectedView(selectedMembers);
            }
        }
    };

    /**
     * 邀请别人来观看
     */
    public void inviteToWatchLive(boolean pulling, InviteMemberLiverMember liverMember) {
        getView().getLogger().info("通知别人来观看的列表：" + getSelectMembersUniqueNo());
        if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())) {
            getView().showMsg(R.string.no_push_authority);
            return;
        }
        if (!getSelectMembersUniqueNo().isEmpty()) {
            int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            long uniqueNo = TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
            if(pulling&&liverMember!=null){
                memberId = liverMember.memberNo;
                uniqueNo = liverMember.getUniqueNo();
            }
            MyTerminalFactory.getSDK().getLiveManager().requestNotifyWatch(getNotifyWatchMemberUniqueNoAndType(),
                    memberId, uniqueNo);
        }
        getView().removeView();
    }

    /**
     * 邀请别人去观看
     * @param oldTerminalMessage
     */
    public void inviteOtherMemberToWatch(TerminalMessage oldTerminalMessage) {
        if(oldTerminalMessage==null){
            getView().showMsg(R.string.text_live_data_error);
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonParam.SEND_STATE, MessageSendStateEnum.SENDING);
        jsonObject.put(JsonParam.DEVICE_ID, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_ID));
        if(oldTerminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
            jsonObject.put(JsonParam.GB28181_RTSP_URL, oldTerminalMessage.messageBody.getString(JsonParam.GB28181_RTSP_URL));
            jsonObject.put(JsonParam.DEVICE_NAME, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_NAME));
            jsonObject.put(JsonParam.DEVICE_DEPT_ID, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_DEPT_ID));
            jsonObject.put(JsonParam.DEVICE_DEPT_NAME, oldTerminalMessage.messageBody.getString(JsonParam.DEVICE_DEPT_NAME));
            jsonObject.put(JsonParam.ACCOUNT_ID, oldTerminalMessage.messageBody.getString(JsonParam.ACCOUNT_ID));
        }
        jsonObject.put(JsonParam.TOKEN_ID, MyTerminalFactory.getSDK().getMessageSeq());
        TerminalMessage mTerminalMessage = new TerminalMessage();
        mTerminalMessage.messageFromId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        mTerminalMessage.messageFromName = MyTerminalFactory.getSDK().getParam(Params.MEMBER_NAME, "");
        mTerminalMessage.messageToId = NoCodec.encodeMemberNo(0);
        mTerminalMessage.messageToName = "";
        mTerminalMessage.sendTime = System.currentTimeMillis();
        mTerminalMessage.messageType = MessageType.GB28181_RECORD.getCode();
        mTerminalMessage.messageBody = jsonObject;
        MyTerminalFactory.getSDK().getTerminalMessageManager().uploadDataByDDPUSH("", mTerminalMessage, getSelectMembersNo(), getSelectMembersUniqueNo());
        getView().removeView();
    }


    /**
     * 请求别人上报
     */
    public void requestOtherStartLive() {
        Member member = getLiveMember();
        if (null != member) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
            getView().removeView();
        } else {
            getView().showMsg(R.string.please_select_live_member);
        }
    }


    /**
     * 请求自己开始上报
     */
    public void requestStartLive(String type) {
//        String theme = mTvLiveSelectmemberTheme.getText().toString().trim();
//        Intent intent = new Intent();
//        intent.putExtra(Constants.THEME, theme);
//        intent.putExtra(Constants.TYPE, Constants.ACTIVE_PUSH);
//        intent.putExtra(Constants.IS_GROUP_PUSH_LIVING, isGroupPushLive);
//        intent.putExtra(Constants.PUSH_MEMBERS,new PushLiveMemberList(getNotifyWatchMemberUniqueNoAndType()));
//        switch (type) {
//            case Constants.PHONE_PUSH:
//                intent.setClass(this, PhonePushService.class);
//                break;
//            case Constants.UVC_PUSH:
//                intent.setClass(this, SwitchCameraService.class);
//                intent.putExtra(Constants.CAMERA_TYPE, Constants.UVC_CAMERA);
//                break;
//            case Constants.RECODER_PUSH:
//                intent.setClass(this, SwitchCameraService.class);
//                intent.putExtra(Constants.CAMERA_TYPE, Constants.RECODER_CAMERA);
//                break;
//        }
//        startService(intent);
//        removeView();
    }

    /**
     * 获取选择成员
     * @return
     */
    private Member getLiveMember(){
        List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
        Member member  = null;
        if(!selectedMembers.isEmpty()){
            ContactItemBean bean  = selectedMembers.get(0);
            if(bean.getType() == Constants.TYPE_USER){
                member = (Member) bean.getBean();
            }
        }
        return member;
    }

    /**
     * 获取选择成员的No
     * @return
     */
    private ArrayList<Integer> getSelectMembersNo(){
        List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
        ArrayList<Integer> result = new ArrayList<>();
        for (ContactItemBean bean:selectedMembers) {
            if(bean.getType() == Constants.TYPE_USER){
                Member member = (Member) bean.getBean();
                result.add(member.getNo());
            }else if(bean.getType() == Constants.TYPE_GROUP){
                Group group = (Group) bean.getBean();
                result.add(group.getNo());
            }
        }
        return result;
    }

    /**
     * 获取选择成员的uniqueNo
     * @return
     */
    private ArrayList<Long> getSelectMembersUniqueNo(){
        List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
        ArrayList<Long> result = new ArrayList<>();
        for (ContactItemBean bean:selectedMembers) {
            if(bean.getType() == Constants.TYPE_USER){
                Member member = (Member) bean.getBean();
                result.add(member.getUniqueNo());
            }else if(bean.getType() == Constants.TYPE_GROUP){
                Group group = (Group) bean.getBean();
                result.add(group.getUniqueNo());
            }
        }
        return result;
    }

    /**
     * 获取通知观看成员的uniqueNo和类型
     * @return
     */
    private ArrayList<String> getNotifyWatchMemberUniqueNoAndType(){
        List<ContactItemBean> selectedMembers = getView().getSelectedMembers();
        ArrayList<String> result = new ArrayList<>();
        for (ContactItemBean bean:selectedMembers) {
            if(bean.getType() == Constants.TYPE_USER){
                Member member = (Member) bean.getBean();
                result.add(MyDataUtil.getPushInviteMemberData(member.getUniqueNo(), ReceiveObjectMode.MEMBER.toString()));
            }else if(bean.getType() == Constants.TYPE_GROUP){
                Group group = (Group) bean.getBean();
                //推送到组使用组的编号，不用UniqueNoa
                result.add(MyDataUtil.getPushInviteMemberData(group.getNo(), ReceiveObjectMode.GROUP.toString()));
            }
        }
        return result;
    }
}
