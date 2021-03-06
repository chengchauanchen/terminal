package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberStatusEnum;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class GroupMemberAdapter extends BaseAdapter {
    private List<Member> currentGroupMembers = new ArrayList<>();
//    private List<Member> deleteMembers = new ArrayList<>();
    private Context mContext;
    private boolean isDelete;
    private boolean isTempGroup;
    private OnItemClickListener onItemClickListener;

    public GroupMemberAdapter(Context mContext, List<Member> currentGroupMembers, boolean isDelete,boolean isTempGroup) {
        this.mContext = mContext;
        this.currentGroupMembers = currentGroupMembers;
        this.isDelete = isDelete;
        this.isTempGroup = isTempGroup;
    }

    @Override
    public int getCount() {
      return currentGroupMembers.size();

    }

    @Override
    public Object getItem(int position) {
        return currentGroupMembers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取选择的设备UniqueNo集合
     * @return
     */
    public List<Long> getDeleteMemberList() {
        List<Long> uniqueNoList = new ArrayList<>();
        uniqueNoList.clear();
        if(currentGroupMembers!=null){
            for (Member member:currentGroupMembers) {
                if (member!=null&&member.isChecked) {
                    uniqueNoList.add(member.getUniqueNo());
                }
            }
        }
        return uniqueNoList;
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        final ViewHolder viewHolder ;
        final Member member = currentGroupMembers.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_person_item, null);
            viewHolder.ll_person_search_item=view.findViewById(R.id.ll_person_search_item);
            viewHolder.userName =  view.findViewById(R.id.tv_member_name);
            viewHolder.me =  view.findViewById(R.id.me);
            viewHolder.userId =  view.findViewById(R.id.tv_member_id);
            viewHolder.userLogo =  view.findViewById(R.id.user_logo);
            viewHolder.rBindedLogo =  view.findViewById(R.id.recorder_binded_logo);
            viewHolder.messageTo =  view.findViewById(R.id.message_to);
            viewHolder.callTo =  view.findViewById(R.id.call_to);
            viewHolder.ivCall =  view.findViewById(R.id.iv_call);
            viewHolder.tvLetter =  view.findViewById(R.id.tv_catagory);
            viewHolder.catagory =  view.findViewById(R.id.catagory);
            viewHolder.line =  view.findViewById(R.id.lay_line);
            viewHolder.dialTo =  view.findViewById(R.id.shoutai_dial_to);
            viewHolder.cbSelectmember = view.findViewById(R.id.cb_selectmember);
            viewHolder.select_delete = view.findViewById(R.id.select_delete);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.catagory.setVisibility(View.GONE);
        if(!isTempGroup|| member.getStatus() == TerminalMemberStatusEnum.ONLINE.getCode()){
            viewHolder.userLogo.setImageResource(BitmapUtil.getImageResourceByType(member.type));
        }else{
            viewHolder.userLogo.setImageResource(BitmapUtil.getOffineImageResourceByType(member.type));
        }

        String no = HandleIdUtil.handleId(member.no);
        viewHolder.userName.setText(member.getName());
        viewHolder.userId.setText(no);

        //是否绑定
        if(member.type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
            viewHolder.rBindedLogo.setVisibility(member.isBind()?View.VISIBLE:View.GONE);
            if (!member.isBind()) {
                viewHolder.userName.setText(no);
                viewHolder.userId.setVisibility(View.GONE);
            }
        }else{
            viewHolder.rBindedLogo.setVisibility(View.GONE);
        }



        if(isDelete){
            viewHolder.messageTo.setVisibility(View.GONE);
            viewHolder.callTo.setVisibility(View.GONE);
            viewHolder.dialTo.setVisibility(View.GONE);
            if (member.getUniqueNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L)){
                viewHolder.select_delete.setVisibility(View.GONE);
            }else {
                viewHolder.select_delete.setVisibility(View.VISIBLE);
            }

        }else {
            viewHolder.select_delete.setVisibility(View.GONE);
            if (member.getUniqueNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L)){
                viewHolder.messageTo.setVisibility(View.GONE);
                viewHolder.callTo.setVisibility(View.GONE);
                viewHolder.dialTo.setVisibility(View.GONE);
            }else {
                viewHolder.messageTo.setVisibility(View.VISIBLE);
                viewHolder.callTo.setVisibility(View.VISIBLE);
                viewHolder.dialTo.setVisibility(View.VISIBLE);

                // TODO: 2019/7/11 处理执法记录仪默认账号只显示拉取图像
                if(member.type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
                    viewHolder.messageTo.setVisibility(View.GONE);
                    viewHolder.dialTo.setVisibility(View.GONE);
                    viewHolder.ivCall.setBackgroundResource(R.drawable.new_live_icon);
                }else{
                    viewHolder.ivCall.setBackgroundResource(R.drawable.new_call_icon);
                }
            }
        }



        if(position == getCount()-1){
            viewHolder.line.setVisibility(View.GONE);
        }else {
            viewHolder.line.setVisibility(View.VISIBLE);
        }

        if(member.isChecked()){
            viewHolder.cbSelectmember.setChecked(true);
        }else {
            viewHolder.cbSelectmember.setChecked(false);
        }
        view.setOnClickListener(v -> {if(isDelete){
            if (member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                return;
            }
            if(member.isChecked()){
                member.isChecked = false;
            }else {
                member.isChecked = true;
            }
            if(onItemClickListener!=null){
                onItemClickListener.onItemClick(v,position,member);
            }
            notifyDataSetChanged();
        }

        });

        //跳转到消息界面
        viewHolder.messageTo.setOnClickListener(v -> {
            IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName(),member.getType());
        });
        //跳转到个呼
        viewHolder.callTo.setOnClickListener(v -> {
            //执法记录仪 公用一个按钮，该按钮时请求图像
            if(member.type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
                }else{
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
                }
            }else{
                //打个呼
                if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    activeIndividualCall(position);
                }else {
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_no_call_permission));
                }
            }
        });
        viewHolder.userLogo.setOnClickListener(view1 -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userId", member.getNo());
            intent.putExtra("userName", member.getName());
            mContext.startActivity(intent);
        });
        viewHolder.dialTo.setOnClickListener(view12 -> {
            Account account = cn.vsx.hamster.terminalsdk.tools.DataUtil.getAccountByMember(member);
            if(TextUtils.isEmpty(account.getPhone())){
                ToastUtils.showShort(R.string.text_has_no_member_phone_number);
                return;
            }
            new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PHONE, account, (dialog,member1) -> {
                if(member1.getUniqueNo() == 0){
                    //普通电话
                    CallPhoneUtil.callPhone((Activity) mContext, account.getPhone());
                }else{
                    if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                        Intent intent = new Intent(mContext, VoipPhoneActivity.class);
                        intent.putExtra("member",member1);
                        mContext.startActivity(intent);
                    }else {
                        ToastUtil.showToast(mContext,mContext.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                    }
                }
                dialog.dismiss();
            }).showDialog();
        });
        return view;

    }

    public void setDelete(boolean isDelete){
        this.isDelete = isDelete;
    }

    final static class ViewHolder {
        LinearLayout ll_person_search_item;
        TextView tvLetter;
        TextView userName;
        TextView me;
        TextView userId;
        ImageView userLogo;
        ImageView rBindedLogo;
        LinearLayout messageTo;
        LinearLayout callTo;
        ImageView ivCall;
        View line;
        LinearLayout catagory;
        LinearLayout dialTo;
        CheckBox cbSelectmember;
        LinearLayout select_delete;
    }

    /**
     * 请求个呼
     * @param position
     */
    private void activeIndividualCall(int position) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network){
            if ( currentGroupMembers.size() > 0) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, currentGroupMembers.get(position));
            }
        } else {
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick(View view, int position, Member member);
    }
}