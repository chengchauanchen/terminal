package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.CallPhoneUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.vc.utils.Constants.TYPE_ACCOUNT;
import static cn.vsx.vc.utils.Constants.TYPE_DEPARTMENT;
import static cn.vsx.vc.utils.Constants.TYPE_USER;

/**
 * Created by XX on 2018/4/11.
 */

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<ContactItemBean> mDatas;
    private static int PC_VOIP = 0;
    private static int PHONE_VOIP = 1;
    private static int TELEPHONE = 2;

    private ItemClickListener listener;
    private boolean isPoliceAffairs;//是否为警务通界面

    public ContactAdapter(Context context, List<ContactItemBean> datas, boolean isPoliceAffairs){
        this.mContext = context;
        this.mDatas = datas;
        this.isPoliceAffairs = isPoliceAffairs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == TYPE_DEPARTMENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_department, parent, false);
            return new DepartmentViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        if(getItemViewType(position) == TYPE_DEPARTMENT){
            DepartmentViewHolder holder1 = (DepartmentViewHolder) holder;
            Department department = (Department) mDatas.get(position).getBean();
            holder1.tvDepartment.setText(department.getName());
            holder1.itemView.setOnClickListener(view -> {
                if(listener != null){
                    listener.onItemClick(view,department.getId(),department.getName(),TYPE_DEPARTMENT);
                }
            });
        }else if(getItemViewType(position) == TYPE_ACCOUNT){
            // TODO: 2019/4/15
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            final Account account = (Account) mDatas.get(position).getBean();
            if(!TextUtils.isEmpty(account.getName())){
                userViewHolder.tvName.setText(account.getName() + "");
            }
            userViewHolder.tvId.setText(account.getNo() + "");
            userViewHolder.llDialTo.setOnClickListener(view -> {
                if(!TextUtils.isEmpty(account.getPhone())){
                    new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PHONE, account.getMembers(), (dialog,member) -> {
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
                }else{
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_member_phone_number));
                }
            });
            userViewHolder.llMessageTo.setOnClickListener(view -> IndividualNewsActivity.startCurrentActivity(mContext, account.getNo(), account.getName()));
            userViewHolder.llCallTo.setOnClickListener(view -> {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
                }else{
                    // TODO: 2019/4/15弹窗拨打个呼
                    new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, account.getMembers(), (dialog,member) -> {
                        activeIndividualCall(member);
                        dialog.dismiss();
                    }).showDialog();
                }
            });
            //请求图像
            userViewHolder.llLiveTo.setOnClickListener(view -> {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
                }else{
                    // TODO: 2019/4/15弹窗请求图像
                    new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_PULL_LIVE, account.getMembers(), (dialog,member) -> {
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
                        dialog.dismiss();
                    }).showDialog();
                }
            });
            //个人信息页面
            userViewHolder.ivLogo.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("userId", account.getNo());
                intent.putExtra("userName", account.getName());
                mContext.startActivity(intent);
            });
            //如果是自己的不显示业务按钮
            if(account.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llMessageTo.setVisibility(View.GONE);
                userViewHolder.llCallTo.setVisibility(View.GONE);
            }else{
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
                userViewHolder.llCallTo.setVisibility(View.VISIBLE);
            }
            //电话按钮，是否显示
            if(isPoliceAffairs && account.getNo() != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
            }else{
                //电台不显示电话
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llLiveTo.setVisibility(View.GONE);
            }
        }else if(getItemViewType(position) == TYPE_USER){
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            final Member member = (Member) mDatas.get(position).getBean();
            if(!TextUtils.isEmpty(member.getName())){
                userViewHolder.tvName.setText(member.getName() + "");
            }
            userViewHolder.tvId.setText(member.getNo() + "");
            userViewHolder.llMessageTo.setOnClickListener(view -> IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName()));
            userViewHolder.llCallTo.setOnClickListener(view -> {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
                }else{
                    activeIndividualCall(member);
                }
            });
            //请求图像
            userViewHolder.llLiveTo.setOnClickListener(view -> {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
                }else{
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
                }
            });
            //个人信息页面
            userViewHolder.ivLogo.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("userId", member.getNo());
                intent.putExtra("userName", member.getName());
                mContext.startActivity(intent);
            });
            //如果是自己的不显示业务按钮
            if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llMessageTo.setVisibility(View.GONE);
                userViewHolder.llCallTo.setVisibility(View.GONE);
            }else{
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
                userViewHolder.llCallTo.setVisibility(View.VISIBLE);
            }
            //电话按钮，是否显示
            if(isPoliceAffairs && member.getNo() != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
            }else{
                //电台不显示电话
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llLiveTo.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount(){
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position){
        ContactItemBean bean = mDatas.get(position);
        if(bean.getType() == TYPE_ACCOUNT){
            return TYPE_ACCOUNT;
        }else if(bean.getType() == TYPE_USER){
            return TYPE_USER;
        }else if(bean.getType() == TYPE_DEPARTMENT){
            return TYPE_DEPARTMENT;
        }else{
            return -1;
        }
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

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView){
            super(itemView);
            tvDepartment = itemView.findViewById(R.id.tv_department);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.shoutai_user_logo)
        ImageView ivLogo;
        @Bind(R.id.shoutai_tv_member_name)
        TextView tvName;
        @Bind(R.id.shoutai_tv_member_id)
        TextView tvId;
        @Bind(R.id.shoutai_dial_to)
        LinearLayout llDialTo;
        @Bind(R.id.shoutai_message_to)
        LinearLayout llMessageTo;
        @Bind(R.id.shoutai_call_to)
        LinearLayout llCallTo;
        @Bind(R.id.shoutai_live_to)
        LinearLayout llLiveTo;

        public UserViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }

    public interface ItemClickListener{
        void onItemClick(View view, int depId,String depName, int type);
    }

}
