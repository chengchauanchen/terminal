package com.vsxin.terminalpad.mvp.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.manager.PullLiveManager;
import com.vsxin.terminalpad.manager.StartCallManager;
import com.vsxin.terminalpad.mvp.contract.constant.TerminalEnum;
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;
import com.vsxin.terminalpad.utils.BitmapUtil;
import com.vsxin.terminalpad.utils.CallPhoneUtil;
import com.vsxin.terminalpad.utils.HandleIdUtil;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberStatusEnum;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 组内在线成员
 */
public class GroupMemberAdapter extends BaseRecycleViewAdapter<Member, GroupMemberAdapter.ViewHolder> {

    public GroupMemberAdapter(Context mContext) {
        super(mContext);
    }

    @NonNull
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_group_member_item, parent, false);
        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberAdapter.ViewHolder holder, int position) {
        Member member = getDatas().get(position);

        if(member.getStatus() == TerminalMemberStatusEnum.ONLINE.getCode()){
            holder.user_logo.setImageResource(BitmapUtil.getImageResourceByType(member.type));
        }else{
            holder.user_logo.setImageResource(BitmapUtil.getOffineImageResourceByType(member.type));
        }
        String no = HandleIdUtil.handleId(member.no);
        holder.tv_member_name.setText(member.getName());
        holder.tv_member_id.setText(no);

        //执法记录仪是否绑定
        if(member.type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
            holder.recorder_binded_logo.setVisibility(member.isBind()?View.VISIBLE:View.GONE);
            if (!member.isBind()) {
                holder.tv_member_name.setText(no);
                holder.tv_member_id.setVisibility(View.GONE);
            }
        }else{
            holder.recorder_binded_logo.setVisibility(View.GONE);
        }

        if (member.getUniqueNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L)){
            holder.shoutai_dial_to.setVisibility(View.GONE);
            holder.message_to.setVisibility(View.GONE);
            holder.call_to.setVisibility(View.GONE);
        }else {
            holder.shoutai_dial_to.setVisibility(View.VISIBLE);
            holder.message_to.setVisibility(View.VISIBLE);
            holder.call_to.setVisibility(View.VISIBLE);

            // TODO: 2019/7/11 处理执法记录仪默认账号只显示拉取图像
            if(member.type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
                holder.shoutai_dial_to.setVisibility(View.GONE);
                holder.message_to.setVisibility(View.GONE);
                holder.iv_call.setBackgroundResource(R.drawable.new_live_icon);
            }else{
                holder.iv_call.setBackgroundResource(R.drawable.new_call_icon);
            }
        }

        //跳转到消息界面
        holder.message_to.setOnClickListener(v -> {
            //IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName(),member.getType());
        });
        //跳转到个呼
        holder.call_to.setOnClickListener(v -> {
            //执法记录仪 公用一个按钮，该按钮时请求图像
            if(member.type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
                }else{
                    //OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
                    //OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
                    PullLiveManager liveManager = new PullLiveManager(getContext());
                    liveManager.pullVideo(member.no+"", TerminalEnum.TERMINAL_BODY_WORN_CAMERA, member.getUniqueNo()+"");
                }
            }else{
                //打个呼
                if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    StartCallManager startCallManager = new StartCallManager(getContext());
                    startCallManager.activeIndividualCall(member);
                }else {
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_no_call_permission));
                }
            }
        });
        holder.user_logo.setOnClickListener(view1 -> {
//            Intent intent = new Intent(mContext, UserInfoActivity.class);
//            intent.putExtra("userId", member.getNo());
//            intent.putExtra("userName", member.getName());
//            mContext.startActivity(intent);
        });
        holder.shoutai_dial_to.setOnClickListener(view12 -> {
            Account account = DataUtil.getAccountByMember(member);
            if(TextUtils.isEmpty(account.getPhone())){
                ToastUtils.showShort(R.string.text_has_no_member_phone_number);
                return;
            }
            new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PHONE, account, (dialog, member1) -> {
                if(member1.getUniqueNo() == 0){
                    //普通电话
                    CallPhoneUtil.callPhone((Activity) mContext, account.getPhone());
                }else{
                    if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
//                        Intent intent = new Intent(mContext, VoipPhoneActivity.class);
//                        intent.putExtra("member",member1);
//                        mContext.startActivity(intent);
                    }else {
                        ToastUtil.showToast(mContext,mContext.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                    }
                }
                dialog.dismiss();
            }).showDialog();
        });


    }

    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView user_logo;
        private final TextView tv_member_name;
        private final TextView tv_member_id;
        private final LinearLayout shoutai_dial_to;
        private final LinearLayout message_to;
        private final LinearLayout call_to;
        private final ImageView iv_call;
        private final ImageView recorder_binded_logo;

        public ViewHolder(View itemView) {
            super(itemView);
            //设备图表
            user_logo = itemView.findViewById(R.id.user_logo);
            //名称
            tv_member_name = itemView.findViewById(R.id.tv_member_name);
            //设备号
            tv_member_id = itemView.findViewById(R.id.tv_member_id);
            //打电话
            shoutai_dial_to = itemView.findViewById(R.id.shoutai_dial_to);
            //消息
            message_to = itemView.findViewById(R.id.message_to);
            //个呼
            iv_call = itemView.findViewById(R.id.iv_call);
            call_to = itemView.findViewById(R.id.call_to);
            recorder_binded_logo = itemView.findViewById(R.id.recorder_binded_logo);
        }
    }
}
