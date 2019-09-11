package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.ContactItemBean;
import com.vsxin.terminalpad.utils.Constants;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/18
 * 描述：
 * 修订历史：
 */
public class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ContactItemBean> data;
    private Context context;

    public SelectAdapter(Context context, List<ContactItemBean> data){
        this.context = context;
        this.data= data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.select_item_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        ViewHolder viewHolder = (ViewHolder) holder;
        ContactItemBean bean = data.get(position);
        if(bean.getType() == Constants.TYPE_USER){
            Member member = (Member) bean.getBean();
            viewHolder.mTvName.setText(member.getName());
        }else if(bean.getType() == Constants.TYPE_GROUP){
            Group group = (Group) bean.getBean();
            viewHolder.mTvName.setText(group.getName());
        }else if(bean.getType() == Constants.TYPE_ACCOUNT){
            Account account = (Account) bean.getBean();
            viewHolder.mTvName.setText(account.getName());
        }

    }

    @Override
    public int getItemCount(){
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mTvName;
        public ViewHolder(View itemView){
            super(itemView);
            mTvName = itemView.findViewById(R.id.tv_name);
        }
    }
}
