package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.CallPhoneUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.vc.utils.Constants.TYPE_DEPARTMENT;
import static cn.vsx.vc.utils.Constants.TYPE_USER;


/**
 * Created by XX on 2018/4/11.
 */

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ContactItemBean> mDatas;
    private static int VOIP=0;
    private static int TELEPHONE=1;

    private ItemClickListener listener;
    private boolean isPoliceAffairs;//是否为警务通界面
    public ContactAdapter(Context context, List<ContactItemBean> datas,boolean isPoliceAffairs) {
        this.mContext=context;
        this.mDatas=datas;
        this.isPoliceAffairs = isPoliceAffairs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==TYPE_DEPARTMENT){
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_department,parent,false);
            return new DepartmentViewHolder(view);
        }else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_contact,parent,false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (getItemViewType(position)==TYPE_DEPARTMENT){
            DepartmentViewHolder holder1= (DepartmentViewHolder) holder;
            holder1.tvDepartment.setText(mDatas.get(position).getName());
            holder1.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null) {
                        listener.onItemClick(view, position, TYPE_DEPARTMENT);
                    }
                }
            });
        }else {
            UserViewHolder userViewHolder= (UserViewHolder) holder;
            final Member member= (Member) mDatas.get(position).getBean();
            userViewHolder.tvName.setText(member.getName()+"");
            userViewHolder.tvId.setText(member.getNo()+"");

            userViewHolder.llDialTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(member.phone)) {

                        ItemAdapter adapter = new ItemAdapter(mContext,ItemAdapter.iniDatas());
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        //设置标题
                        builder.setTitle("拨打电话");
                        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                if(position==VOIP){//voip电话
                                    if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                                        Intent intent = new Intent(mContext, VoipPhoneActivity.class);
                                        intent.putExtra("member",member);
                                        mContext.startActivity(intent);
                                    }else {
                                        ToastUtil.showToast(mContext,"voip注册失败，请检查服务器配置");
                                    }
                                }
                                else if(position==TELEPHONE){//普通电话

                                  CallPhoneUtil.callPhone((Activity) mContext, member.phone);

                                }

                            }
                        });
                        builder.create();
                        builder.show();
                    }else {
                        ToastUtil.showToast(mContext,"暂无该用户电话号码");
                    }
                }
            });
            userViewHolder.llMessageTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName());

                }
            });

            userViewHolder.llCallTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        ToastUtil.showToast(mContext,"没有个呼功能权限");
                    }else {
                        activeIndividualCall(member);
                    }


                }
            });

            userViewHolder.ivLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra("userId", member.getNo());
                    intent.putExtra("userName", member.getName());
                    mContext.startActivity(intent);
                }
            });
            if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llMessageTo.setVisibility(View.GONE);
                userViewHolder.llCallTo.setVisibility(View.GONE);
            }else {
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
                userViewHolder.llCallTo.setVisibility(View.VISIBLE);

            }

            if(isPoliceAffairs&&member.getNo() != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
            }else {
                //警务通不显示电话
                userViewHolder.llDialTo.setVisibility(View.GONE);
            }

        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        ContactItemBean bean=mDatas.get(position);
        if (bean.getType()==TYPE_USER){
            return TYPE_USER;
        }else {
            return TYPE_DEPARTMENT;
        }
    }

    private void activeIndividualCall(Member member){
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
        } else {
            ToastUtil.showToast(mContext, "网络连接异常，请检查网络！");
        }
    }



    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView) {
            super(itemView);
            tvDepartment= (TextView) itemView.findViewById(R.id.tv_department);
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

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener=listener;
    }
    public interface ItemClickListener{
        void onItemClick(View view, int postion, int type);
    }

}
