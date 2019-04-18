package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/4/18
 * 描述：
 * 修订历史：
 */
public class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Member> data;
    private Context context;

    public SelectAdapter(Context context,List<Member> data){
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
        viewHolder.mTvName.setText(data.get(position).getName());
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
