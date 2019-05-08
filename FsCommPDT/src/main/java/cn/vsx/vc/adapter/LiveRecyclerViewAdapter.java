package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.utils.Constants;

import static cn.vsx.vc.utils.Constants.TYPE_DEPARTMENT;
import static cn.vsx.vc.utils.Constants.TYPE_USER;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2018/12/27
 * 描述：
 * 修订历史：
 */
public class LiveRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private List<ContactItemBean> mDatas;
    private String type;
    private int lastPosition = -1;
    private ItemClickListener listener;
    private ArrayList<Integer> selectMember = new ArrayList<>();//被选中的人
    private Member liveMember;

    public LiveRecyclerViewAdapter(Context context, List<ContactItemBean> datas, String type){
        this.mContext = context;
        this.mDatas = datas;
        this.type = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == TYPE_DEPARTMENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_department, parent, false);
            return new DepartmentViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_live_contract, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        if(getItemViewType(position) == TYPE_DEPARTMENT){
            DepartmentViewHolder holder1 = (DepartmentViewHolder) holder;
            holder1.tvDepartment.setText(mDatas.get(position).getName());
            holder1.itemView.setOnClickListener(view -> {
                if(listener != null){
                    listener.onItemClick(position, TYPE_DEPARTMENT);
                }
            });
        }else{
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            final Member member =(Member) mDatas.get(position).getBean();
            userViewHolder.tvName.setText(member.getName() + "");
            userViewHolder.tvId.setText(member.getNo() + "");
            userViewHolder.itemView.setOnClickListener(view -> {
                if(Constants.PUSH.equals(type)){
                    if(member.isChecked()){
                        member.setChecked(false);
                        selectMember.remove(Integer.valueOf(member.getNo()));
                    }else{
                        member.setChecked(true);
                        selectMember.add(Integer.valueOf(member.getNo()));
                    }
                }else if(Constants.PULL.equals(type)){
                    if(member.isChecked()){
                        member.setChecked(false);
                        selectMember.remove(Integer.valueOf(member.getNo()));
                        lastPosition = -1;
                    }else{
                        if(lastPosition != -1){
                            Member lastMember = (Member) mDatas.get(lastPosition).getBean();
                            lastMember.setChecked(false);
                            liveMember = null;
                            selectMember.remove(Integer.valueOf(lastMember.getNo()));
                        }
                        member.setChecked(true);
                        liveMember = member;
                        selectMember.add(Integer.valueOf(member.getNo()));
                        lastPosition = position;
                    }
                }
                if(listener != null){
                    listener.onItemClick( position, TYPE_USER);
                }
                notifyDataSetChanged();
            });
            if(member.isChecked()){
                userViewHolder.iv_select.setChecked(true);
            }else{
                userViewHolder.iv_select.setChecked(false);
            }
        }
    }

    public ArrayList<Integer> getSelectMember(){
        return selectMember;
    }

    public void setSelectMember(int memberNo){
        boolean added = false;
        for(ContactItemBean next : mDatas){
            if(next.getBean() instanceof Member){
                Member bean = (Member) next.getBean();
                if(bean.getNo() == memberNo){
                    bean.setChecked(true);
                    removeExistMember(memberNo);
                    selectMember.add(memberNo);
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
            selectMember.add(memberNo);
        }
        listener.onItemClick(-1,TYPE_USER);
        notifyDataSetChanged();
    }

    //删除掉之前的人
    private void removeExistMember(int memberNo){
        Iterator<Integer> iterator = selectMember.iterator();
        while(iterator.hasNext()){
            Integer next = iterator.next();
            if(next == memberNo){
                iterator.remove();
                break;
            }
        }
    }

    public Member getLiveMember(){
        return liveMember;
    }
    @Override
    public int getItemCount(){
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position){
        ContactItemBean bean = mDatas.get(position);
        if(bean.getType() == TYPE_USER){
            return TYPE_USER;
        }else{
            return TYPE_DEPARTMENT;
        }
    }

    public void setType(String type){
        this.type = type;
    }

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView){
            super(itemView);
            tvDepartment =  itemView.findViewById(R.id.tv_department);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.shoutai_user_logo)
        ImageView ivLogo;
        @Bind(R.id.shoutai_tv_member_name)
        TextView tvName;
        @Bind(R.id.shoutai_tv_member_id)
        TextView tvId;
        @Bind(R.id.iv_select)
        CheckBox iv_select;

        public UserViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }

    public interface ItemClickListener{
        void onItemClick(int postion, int type);
    }
}
