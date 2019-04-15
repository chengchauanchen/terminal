package cn.vsx.vc.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.ContactItemBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.vc.utils.Constants.TYPE_DEPARTMENT;
import static cn.vsx.vc.utils.Constants.TYPE_USER;


/**
 * Created by XX on 2018/4/11.
 */

public class IncreaseTemporaryGroupMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<ContactItemBean> mDatas;
    private List<Member> addMembers = new ArrayList<>();

    private ItemClickListener listener;
    private List<Integer> addNos = new ArrayList<>();

    public IncreaseTemporaryGroupMemberAdapter(Context context, List<ContactItemBean> datas) {
        this.mContext=context;
        this.mDatas=datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType==TYPE_DEPARTMENT){
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_department,parent,false);
            return new DepartmentViewHolder(view);
        }else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_increase_temporary_group_member,parent,false);
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
            final UserViewHolder userViewHolder= (UserViewHolder) holder;
            final Member member= (Member) mDatas.get(position).getBean();
            userViewHolder.tvName.setText(member.getName());
            userViewHolder.tvId.setText(member.getNo()+"");
            userViewHolder.cbSelectmember.setChecked(member.isChecked());

            userViewHolder.ivLogo.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("userId", member.getNo());
                intent.putExtra("userName", member.getName());
                mContext.startActivity(intent);
            });

            userViewHolder.itemView.setOnClickListener(v -> {
                if(member.isChecked()){
                    member.setChecked(false);
                    removeExistMember(member.getNo());

                }else {
                    member.setChecked(true);
                    removeExistMember(member.getNo());
                    addMembers.add(member);

                }
                if(listener!=null){
                    listener.onItemClick(v,position,TYPE_USER);
                }
                notifyDataSetChanged();
            });


        }

    }

    //删除掉之前的人
    private void removeExistMember(int memberNo){
        Iterator<Member> iterator = addMembers.iterator();
        while(iterator.hasNext()){
            Member next = iterator.next();
            if(next.getNo() == memberNo){
                iterator.remove();
                break;
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

    public List<Integer> getSelectItem(){
        for(Member addMember : addMembers){
            addNos.add(addMember.getNo());
        }
        return addNos;
    }

    public void setSelectItem(int memberNo){
        boolean added = false;
        for(ContactItemBean next : mDatas){
            if(next.getBean() instanceof Member){
                Member bean = (Member) next.getBean();
                if(bean.getNo() == memberNo){
                    bean.setChecked(true);
                    removeExistMember(memberNo);
                    addMembers.add(bean);
                    added = true;
                    break;
                }
            }
        }
        if(!added){
            //如果当前不在当前部门，就去子部门查询
            Member member = (Member) cn.vsx.hamster.terminalsdk.tools.DataUtil.getMemberByMemberNo(memberNo).clone();
            member.setChecked(true);
            removeExistMember(memberNo);
            addMembers.add(member);
        }
        listener.onItemClick(null,-1,TYPE_USER);
        notifyDataSetChanged();
    }

    public List<Member> getSelectMember(){
        return addMembers;
    }


    private void activeIndividualCall(Member member){
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member,0l);

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
        @Bind(R.id.cb_selectmember)
        CheckBox cbSelectmember;

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
