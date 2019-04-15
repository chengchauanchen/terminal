package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
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

import java.util.ArrayList;
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
import cn.vsx.vc.dialog.ChooseDevicesDialog;
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
            holder1.itemView.setOnClickListener(view -> {
                if (listener!=null) {
                    listener.onItemClick(view, position, TYPE_DEPARTMENT);
                }
            });
        }else {
            UserViewHolder userViewHolder= (UserViewHolder) holder;
            final Member member= (Member) mDatas.get(position).getBean();
            userViewHolder.tvName.setText(member.getName()+"");
            userViewHolder.tvId.setText(member.getNo()+"");
            //拨打电话
            userViewHolder.llDialTo.setOnClickListener(view -> {
                if (!TextUtils.isEmpty(member.phone)) {
//                    List<Member> list = new ArrayList<>();
//                    new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PHONE, list, (view1, position12) -> {
//                        long uniqueNo = 0l;
////                    activeIndividualCall(member,uniqueNo);
//                    }).show();

                    ItemAdapter adapter = new ItemAdapter(mContext,ItemAdapter.iniDatas());
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    //设置标题
                    builder.setTitle("拨打电话");
                    builder.setAdapter(adapter, (dialogInterface, position1) -> {
                        if(position1 ==VOIP){//voip电话
                            if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                                Intent intent = new Intent(mContext, VoipPhoneActivity.class);
                                intent.putExtra("member",member);
                                mContext.startActivity(intent);
                            }else {
                                ToastUtil.showToast(mContext,mContext.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                            }
                        } else if(position1 ==TELEPHONE){//普通电话
                          CallPhoneUtil.callPhone((Activity) mContext, member.phone);
                        }
                    });
                    builder.create();
                    builder.show();
                }else {
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_has_no_member_phone_number));
                }
            });
            //发送消息
            userViewHolder.llMessageTo.setOnClickListener(view -> IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName()));
            //个呼
            userViewHolder.llCallTo.setOnClickListener(view -> {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(mContext,mContext.getString(R.string.text_no_call_permission));
                }else {
                    //弹窗选择设备
                    List<Member> list = new ArrayList<>();
                    new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, list, (view1, position12) -> {
                        long uniqueNo = 0l;
                        activeIndividualCall(member,uniqueNo);
                    }).show();
                }
            });
            //请求图像
            userViewHolder.llLiveTo.setOnClickListener(view -> {
                List<Member> list = new ArrayList<>();
                new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_LIVE, list, (view1, position12) -> {
                    long uniqueNo = 0l;

//                    activeIndividualCall(member,uniqueNo);
                }).show();
            });
            //个人信息页面
            userViewHolder.ivLogo.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("userId", member.getNo());
                intent.putExtra("userName", member.getName());
                mContext.startActivity(intent);
            });
            //如果是自己的不显示业务按钮
            if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llMessageTo.setVisibility(View.GONE);
                userViewHolder.llCallTo.setVisibility(View.GONE);
            }else {
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
                userViewHolder.llCallTo.setVisibility(View.VISIBLE);

            }
            //电话按钮，是否显示
            if(isPoliceAffairs&&member.getNo() != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
                userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
            }else {
                //电台不显示电话
                userViewHolder.llDialTo.setVisibility(View.GONE);
                userViewHolder.llLiveTo.setVisibility(View.GONE);
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

    private void activeIndividualCall(Member member,long uniqueNo){
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member,uniqueNo);
        } else {
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }



    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView) {
            super(itemView);
            tvDepartment=  itemView.findViewById(R.id.tv_department);
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
