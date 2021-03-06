package com.vsxin.terminalpad.mvp.ui.adapter;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.app.PadApplication;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;
import com.vsxin.terminalpad.mvp.ui.fragment.PersonMessageFragment;
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;
import com.vsxin.terminalpad.receiveHandler.ReceiverRequestVideoHandler;
import com.vsxin.terminalpad.utils.BitmapUtil;
import com.vsxin.terminalpad.utils.CallPhoneUtil;
import com.vsxin.terminalpad.utils.Constants;
import com.vsxin.terminalpad.utils.FragmentManage;
import com.vsxin.terminalpad.utils.HandleIdUtil;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAccountSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 通讯录组搜索adapter
 * Created by gt358 on 2017/10/21.
 */

public class SearchAdapter extends BaseMultiItemQuickAdapter<ContactItemBean, BaseViewHolder> {

    private OnItemClickListener onItemClickListener;
    private String keyWords;
    private FragmentActivity activity;
    public SearchAdapter(List<ContactItemBean> mData, FragmentActivity activity){
        super(mData);
        this.activity = activity;
        addItemType(Constants.TYPE_CONTRACT_GROUP, R.layout.item_group_search);
        addItemType(Constants.TYPE_CONTRACT_MEMBER, R.layout.item_search_contacts);
        addItemType(Constants.TYPE_CONTRACT_PDT, R.layout.item_search_pdt);
        addItemType(Constants.TYPE_CONTRACT_LTE, R.layout.item_search_lte);
        addItemType(Constants.TYPE_CONTRACT_RECORDER, R.layout.item_search_recorder);
        addItemType(Constants.TYPE_CHECK_SEARCH_GROUP, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_PC, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_POLICE, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_HDMI, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_UAV, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_RECODER, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_ACCOUNT, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_LTE, R.layout.layout_item_user);
    }

    public void setFilterKeyWords(String keyWords){
        this.keyWords = keyWords;
    }


    @Override
    protected void convert(BaseViewHolder holder, ContactItemBean item){
        switch(item.getType()){
            case Constants.TYPE_CONTRACT_GROUP:
                Group group = (Group) item.getBean();
                TextView groupName = holder.getView(R.id.group_child_name);
                setKeyWordsView(groupName,group.getName());
                holder.addOnClickListener(R.id.to_group);
                holder.addOnClickListener(R.id.is_current_group_tv);
                break;
            case Constants.TYPE_CONTRACT_MEMBER:
                Account account = (Account) item.getBean();
                ImageView ivLogoMember = holder.getView(R.id.iv_member_portrait);
                ivLogoMember.setImageResource(BitmapUtil.getUserPhoto());
                TextView memberName = holder.getView(R.id.tv_member_name);
                TextView memberId = holder.getView(R.id.tv_member_id);
                setKeyWordsView(memberName, HandleIdUtil.handleName(account.getName()));
                setKeyWordsView(memberId,HandleIdUtil.handleId(account.getNo()));
                //如果是自己，把功能入口禁掉
                if(account.getNo() == TerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                    holder.setGone(R.id.shoutai_dial_to, false);
                }
                //拨打电话
                holder.setOnClickListener(R.id.shoutai_dial_to, v -> callPhone(account));
                //发送消息
                holder.setOnClickListener(R.id.iv_search_msg, v -> {
                    //打开个人会话页面
                    FragmentManage.startFragment(activity, PersonMessageFragment.newInstance(account.getNo(),account.getName()));
                });
                //拉流
                holder.setOnClickListener(R.id.shoutai_live_to, v -> pullStream(account));
                //拨打个呼
                holder.setOnClickListener(R.id.iv_search_call, v -> indivudualCall(account));
                break;
            case Constants.TYPE_CONTRACT_PDT:
                Member contractpdt = (Member) item.getBean();
                ImageView ivLogoPdt = holder.getView(R.id.iv_member_portrait);
                ivLogoPdt.setImageResource(BitmapUtil.getUserPhoto());
                TextView memberNamePdt = holder.getView(R.id.tv_member_name);
                TextView memberIdPdt = holder.getView(R.id.tv_member_id);
                setKeyWordsView(memberNamePdt,HandleIdUtil.handleName(contractpdt.getName()));
                setKeyWordsView(memberIdPdt,HandleIdUtil.handleId(contractpdt.getNo()));

                //发送消息
                holder.setOnClickListener(R.id.iv_search_msg, v -> {
                    //打开个人会话页面
                    FragmentManage.startFragment(activity, PersonMessageFragment.newInstance(contractpdt.getNo(),contractpdt.getName()));
                });
                //拨打个呼
                holder.setOnClickListener(R.id.iv_search_call, v -> {
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
                    }else{
                        activeIndividualCall(contractpdt);
                    }
                });
                break;

            case Constants.TYPE_CONTRACT_LTE:
                Member contractLte = (Member) item.getBean();
                ImageView ivLogoLte = holder.getView(R.id.iv_member_portrait);
                ivLogoLte.setImageResource(BitmapUtil.getUserPhoto());
                setKeyWordsView(holder.getView(R.id.tv_member_name),HandleIdUtil.handleName(contractLte.getName()));
                setKeyWordsView(holder.getView(R.id.tv_member_id),HandleIdUtil.handleId(contractLte.getNo()));

                //发送消息
                holder.setOnClickListener(R.id.iv_search_msg, v -> {
                    //打开个人会话页面
                    FragmentManage.startFragment(activity, PersonMessageFragment.newInstance(contractLte.getNo(),contractLte.getName()));
                });

                //拉流
                holder.setOnClickListener(R.id.shoutai_live_to, v ->  OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, contractLte));

                //拨打个呼
                holder.setOnClickListener(R.id.iv_search_call, v -> {
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
                    }else{
                        activeIndividualCall(contractLte);
                    }
                });

                break;
            case Constants.TYPE_CONTRACT_RECORDER:
                Member contractRecorder = (Member) item.getBean();
                holder.setGone(R.id.recorder_binded_logo,contractRecorder.isBind());
                if(contractRecorder.isBind()){
                    setKeyWordsView(holder.getView(R.id.tv_member_name),HandleIdUtil.handleName(contractRecorder.getName()));
                    setKeyWordsView(holder.getView(R.id.tv_member_id),HandleIdUtil.handleId(contractRecorder.getNo()));
                    holder.setGone(R.id.tv_member_id,true);
                }else{
                    setKeyWordsView(holder.getView(R.id.tv_member_name),HandleIdUtil.handleId(contractRecorder.getNo()));
                    holder.setGone(R.id.tv_member_id,false);
                }
                //拉流
                holder.setOnClickListener(R.id.shoutai_live_to, v ->  OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, contractRecorder));
                break;
            case Constants.TYPE_CHECK_SEARCH_GROUP:
                Group group1 = (Group) item.getBean();
                holder.setImageResource(R.id.shoutai_user_logo,R.drawable.group_photo);
                holder.setGone(R.id.shoutai_tv_member_id,false);
                holder.setChecked(R.id.checkbox,group1.isChecked());
                setKeyWordsView(holder.getView(R.id.shoutai_tv_member_name),group1.getName());
                holder.itemView.setOnClickListener(v -> {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGroupSelectedHandler.class,group1,!group1.isChecked());
                    group1.setChecked(!group1.isChecked());
                    if(onItemClickListener !=null){
                        onItemClickListener.onItemClick();
                    }
                });
                break;
            case Constants.TYPE_CHECK_SEARCH_PC:
            case Constants.TYPE_CHECK_SEARCH_POLICE:
            case Constants.TYPE_CHECK_SEARCH_RECODER:
            case Constants.TYPE_CHECK_SEARCH_HDMI:
            case Constants.TYPE_CHECK_SEARCH_UAV:
            case Constants.TYPE_CHECK_SEARCH_LTE:
                Member member = (Member) item.getBean();
                ImageView image = holder.getView(R.id.shoutai_user_logo);
                image.setImageResource(BitmapUtil.getPadDeviceImageResourceByType(member.getType()));
                setKeyWordsView(holder.getView(R.id.shoutai_tv_member_name),HandleIdUtil.handleName(member.getName()));
                setKeyWordsView(holder.getView(R.id.shoutai_tv_member_id),HandleIdUtil.handleId(member.getNo()));
                holder.setGone(R.id.shoutai_tv_member_id,true);
                if(item.getType() == Constants.TYPE_CHECK_SEARCH_RECODER){
                    holder.setGone(R.id.recorder_binded_logo,member.isBind());
                    if(!member.isBind()){
                        setKeyWordsView(holder.getView(R.id.shoutai_tv_member_name),HandleIdUtil.handleId(member.getNo()));
                        holder.setGone(R.id.shoutai_tv_member_id,false);
                    }
                }else{
                    holder.setGone(R.id.recorder_binded_logo,false);
                }
                holder.setChecked(R.id.checkbox,member.isChecked());

                holder.itemView.setOnClickListener(v -> {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMemberSelectedHandler.class,member,!member.isChecked(), TerminalMemberType.getInstanceByCode(member.getType()).toString());
                    member.setChecked(!member.isChecked());
                    if(onItemClickListener !=null){
                        onItemClickListener.onItemClick();
                    }
                });
                break;
            case Constants.TYPE_CHECK_SEARCH_ACCOUNT:
                Account account1 = (Account) item.getBean();
                holder.setImageResource(R.id.shoutai_user_logo,BitmapUtil.getUserPhoto());
                holder.setText(R.id.shoutai_tv_member_name, account1.getName());
                holder.setText(R.id.shoutai_tv_member_id, String.valueOf(account1.getNo()));
                holder.setChecked(R.id.checkbox,account1.isChecked());
                setKeyWordsView(holder.getView(R.id.shoutai_tv_member_name),HandleIdUtil.handleName(account1.getName()));
                setKeyWordsView(holder.getView(R.id.shoutai_tv_member_id),HandleIdUtil.handleId(account1.getNo()));
                holder.itemView.setOnClickListener(v -> {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveAccountSelectedHandler.class,account1,!account1.isChecked());
                    account1.setChecked(!account1.isChecked());
                    if(onItemClickListener !=null){
                        onItemClickListener.onItemClick();
                    }
                });
                break;
            default:
        }
    }

    /**
     * 拉流
     */
    private void pullStream(Account account){
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
        }else{
            new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_PULL_LIVE, account, (dialog, member) -> {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
                dialog.dismiss();
            }).showDialog();
        }
    }

    /**
     * 拨打个呼
     */
    private void indivudualCall(Account account){
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
        }else{
            new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, account, (dialog, member) -> {
                activeIndividualCall(member);
                dialog.dismiss();
            }).showDialog();
        }
    }

    /**
     * 拨打电话
     */
    private void callPhone(Account account){
        if(TextUtils.isEmpty(account.getPhone())){
            ToastUtil.showToast(R.string.text_has_no_member_phone_number);
            return;
        }
        new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PHONE, account, (dialog, member) -> {
            if(member.getUniqueNo() == 0){
                //普通电话
                CallPhoneUtil.callPhone((Activity) mContext, account.getPhone());
            }else{
                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                    // TODO: 2019/8/23 VOIP电话
//                    Intent intent = new Intent(mContext, VoipPhoneActivity.class);
//                    intent.putExtra("member",member);
//                    mContext.startActivity(intent);
                }else {
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                }
            }
            dialog.dismiss();
        }).showDialog();
    }

    private void activeIndividualCall(Member member){
        PadApplication.getPadApplication().isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
        }else{
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick();
    }

    /**
     * 设置关键字的颜色
     * @param textView
     * @param name
     */
    private void setKeyWordsView(TextView textView, String name){
        if (!Util.isEmpty(name) && !Util.isEmpty(keyWords) && name.toLowerCase().contains(keyWords.toLowerCase())) {
            int index = name.toLowerCase().indexOf(keyWords.toLowerCase());
            int len = keyWords.length();
            Spanned temp = Html.fromHtml(name.substring(0, index)
                    + "<u><font color=#eb403a>"
                    + name.substring(index, index + len) + "</font></u>"
                    + name.substring(index + len, name.length()));
            textView.setText(temp);
        }else if(!Util.isEmpty(name) && !Util.isEmpty(keyWords) && keyWords.toLowerCase().contains(name.toLowerCase())){
            int len = name.length();
            Spanned temp = Html.fromHtml("<u><font color=#eb403a>"
                    + name.substring(0, len)+"</font></u>"
                    );
            textView.setText(temp);
        }
        else {
            textView.setText(name);
        }
    }
}
