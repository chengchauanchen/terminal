package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Intent;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupSelectedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveMemberSelectedHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 通讯录组搜索adapter
 * Created by gt358 on 2017/10/21.
 */

public class SearchAdapter extends BaseMultiItemQuickAdapter<ContactItemBean, BaseViewHolder>{

    private OnItemClickListener onItemClickListener;
    private String keyWords;

    public SearchAdapter(List<ContactItemBean> mData){
        super(mData);
        addItemType(Constants.TYPE_CONTRACT_GROUP, R.layout.item_group_search);
        addItemType(Constants.TYPE_CONTRACT_MEMBER, R.layout.item_search_contacts);
        addItemType(Constants.TYPE_CONTRACT_PDT, R.layout.item_search_pdt);
        addItemType(Constants.TYPE_CHECK_SEARCH_GROUP, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_PC, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_POLICE, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_HDMI, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_UAV, R.layout.layout_item_user);
        addItemType(Constants.TYPE_CHECK_SEARCH_RECODER, R.layout.layout_item_user);
    }

    public void setFilterKeyWords(String keyWords){
        this.keyWords = keyWords;
    }


    @Override
    protected void convert(BaseViewHolder holder, ContactItemBean item){
        switch(item.getType()){
            case Constants.TYPE_CONTRACT_GROUP:
                Group group = (Group) item.getBean();
                holder.setText(R.id.group_child_name,group.getName());
                holder.addOnClickListener(R.id.to_group);
                holder.addOnClickListener(R.id.is_current_group_tv);
                break;
            case Constants.TYPE_CONTRACT_MEMBER:
                Account account = (Account) item.getBean();
                holder.setText(R.id.tv_member_name,HandleIdUtil.handleName(account.getName()));
                holder.setText(R.id.tv_member_id, HandleIdUtil.handleId(account.getNo()));
                //拨打电话
                holder.setOnClickListener(R.id.shoutai_dial_to, v -> callPhone(account));
                //发送消息
                holder.setOnClickListener(R.id.iv_search_msg, v -> IndividualNewsActivity.startCurrentActivity(mContext, account.getNo(), account.getName()));
                //拉流
                holder.setOnClickListener(R.id.shoutai_live_to, v -> pullStream(account));
                //拨打个呼
                holder.setOnClickListener(R.id.iv_search_call, v -> indivudualCall(account));
                break;
            case Constants.TYPE_CONTRACT_PDT:

                Member contractpdt = (Member) item.getBean();
                holder.setText(R.id.tv_member_name,HandleIdUtil.handleName(contractpdt.getName()));
                holder.setText(R.id.tv_member_id, HandleIdUtil.handleId(contractpdt.getNo()));

                //发送消息
                holder.setOnClickListener(R.id.iv_search_msg, v -> IndividualNewsActivity.startCurrentActivity(mContext,contractpdt.getNo(), contractpdt.getName()));

                //拨打个呼
                holder.setOnClickListener(R.id.iv_search_call, v -> {
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
                    }else{
                        activeIndividualCall(contractpdt);
                    }
                });
                break;
            case Constants.TYPE_CHECK_SEARCH_GROUP:
                Group group1 = (Group) item.getBean();
                holder.setText(R.id.shoutai_tv_member_name, group1.getName());
                holder.setGone(R.id.shoutai_tv_member_id,false);
                holder.setChecked(R.id.checkbox,group1.isChecked());
                holder.setOnClickListener(R.id.checkbox, v -> {
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
                Member member = (Member) item.getBean();
                holder.setText(R.id.shoutai_tv_member_name, member.getName());
                holder.setText(R.id.shoutai_tv_member_id, String.valueOf(member.getNo()));
                holder.setChecked(R.id.checkbox,member.isChecked());
                holder.setOnClickListener(R.id.checkbox, v -> {
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveMemberSelectedHandler.class,member,!member.isChecked());
                    member.setChecked(!member.isChecked());
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
        new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PHONE, account, (dialog, member) -> {
            if(member.getUniqueNo() == 0){
                //普通电话
                CallPhoneUtil.callPhone((Activity) mContext, account.getPhone());
            }else{
                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                    Intent intent = new Intent(mContext, VoipPhoneActivity.class);
                    intent.putExtra("member",member);
                    mContext.startActivity(intent);
                }else {
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                }
            }
            dialog.dismiss();
        }).showDialog();
    }

    private void activeIndividualCall(Member member){
        MyApplication.instance.isCallState = true;
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
}
