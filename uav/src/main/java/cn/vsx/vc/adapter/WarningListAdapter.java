package cn.vsx.vc.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.WarningRecord;
import cn.vsx.vc.R;

/**
 * 作者：xuxiaolong
 * 版本：1.0
 * 创建日期：2019/1/4
 * 描述：
 * 修订历史：
 */
public class WarningListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //显示的数据
    private List<WarningRecord> datas;
    private Context context;
    private LayoutInflater inflater;
    private boolean loading = false;//是否正在加载
    private boolean hasMore = true;   // 变量，是否有更多数据
    private static int NORMAL_TYPE = 0;     // 第一种ViewType，正常的item
    private static int FOOT_TYPE = 1;       // 第二种ViewType，底部的提示View
    private ItemClickListener itemClickListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public WarningListAdapter(Context context, List<WarningRecord>warningRecords){
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.datas = warningRecords;
    }

    @Override
    public int getItemViewType(int position){
        if(position == getItemCount() - 1){
            return FOOT_TYPE;
        }else{
            return NORMAL_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == NORMAL_TYPE){
            return new WarningViewHolder(inflater.inflate(R.layout.layout_item_warning, parent, false));
        }else if(viewType == FOOT_TYPE){
            return new FootHolder(inflater.inflate(R.layout.layout_foot_warning, parent, false));
        }else{
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        if(holder instanceof WarningViewHolder){
            WarningRecord warningRecord = datas.get(position);
            ((WarningViewHolder) holder).mTvSummary.setText(warningRecord.getSummary());
            ((WarningViewHolder) holder).mTvAlarmTime.setText(warningRecord.getAlarmTime());
            ((WarningViewHolder) holder).mTvDate.setText(warningRecord.getDate());
            if(position == 0){
                ((WarningViewHolder) holder).mLlTime.setVisibility(View.VISIBLE);
            }else{
                //当前item上一个
                WarningRecord lastRecord = datas.get(position - 1);
                if(lastRecord.getDate().equals(warningRecord.getDate())){
                    ((WarningViewHolder) holder).mLlTime.setVisibility(View.GONE);
                }else{
                    ((WarningViewHolder) holder).mLlTime.setVisibility(View.VISIBLE);
                }
            }
            ((WarningViewHolder) holder).mTvSummary.setText(warningRecord.getSummary());
            ((WarningViewHolder) holder).mTvAlarmTime.setText(warningRecord.getAlarmTime());
            if(warningRecord.getStatus()==0){
                ((WarningViewHolder) holder).mIvStatus.setImageResource(R.drawable.warning_dealed);
            }else if(warningRecord.getStatus() ==1){
                ((WarningViewHolder) holder).mIvStatus.setImageResource(R.drawable.warning_dealing);
            }
            //是否未读
            ((WarningViewHolder) holder).mVStatus.setVisibility((warningRecord.getUnRead() == 1)?View.GONE:View.VISIBLE);
            if(null != itemClickListener){
                ((WarningViewHolder) holder).mRlWarningItem.setOnClickListener(v -> itemClickListener.onItemClick(warningRecord));
            }

        }else if(holder instanceof FootHolder){
            ((FootHolder) holder).rl_root.setVisibility(View.VISIBLE);
            if(loading){
                showAnimate(((FootHolder) holder).progress_dialog_img);
                ((FootHolder) holder).tv_tips.setText("正在加载...");


            }else {
                if(!hasMore){
                    ((FootHolder) holder).tv_tips.setText("没有更多数据了");
                    mHandler.postDelayed(() -> hideFooter(holder),1000);
                }else {
                    hideFooter(holder);
                }
            }
        }
    }

    private void hideFooter(RecyclerView.ViewHolder holder){
        hideAnimate(((FootHolder) holder).progress_dialog_img);
        ((FootHolder) holder).rl_root.setVisibility(View.GONE);
    }

    public void setLoading(boolean loading){
        this.loading = loading;
    }
    public void setHasMore(boolean hasMore){
        this.hasMore = hasMore;
    }

    private void showAnimate(ImageView imageView){
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.loading_dialog_progressbar_anim);
        imageView.setAnimation(anim);
    }

    private void hideAnimate(ImageView imageView){
        imageView.clearAnimation();
    }

    public void removeMessage(){
        mHandler.removeCallbacksAndMessages(null);
    }

    // 获取条目数量，之所以要加1是因为增加了一条footView
    @Override
    public int getItemCount(){
        return datas.size() + 1;
    }

    // 自定义方法，获取列表中数据源的最后一个位置，比getItemCount少1，因为不计上footView
    @SuppressWarnings("unused")
    public int getRealLastPosition(){
        return datas.size();
    }

    private class WarningViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout mLlTime;
        private TextView mTvSummary;
        private TextView mTvAlarmTime;
        private TextView mTvDate;
        private View mVStatus;
        private ImageView mIvStatus;
        private RelativeLayout mRlWarningItem;

        private WarningViewHolder(View itemView){
            super(itemView);
            mLlTime = itemView.findViewById(R.id.ll_time);
            mRlWarningItem = itemView.findViewById(R.id.rl_warning_item);
            mTvSummary = itemView.findViewById(R.id.tv_summary);
            mTvAlarmTime = itemView.findViewById(R.id.tv_alarm_time);
            mTvDate = itemView.findViewById(R.id.tv_date);
            mVStatus = itemView.findViewById(R.id.v_status);
            mIvStatus = itemView.findViewById(R.id.iv_status);

        }
    }

    // // 底部footView的ViewHolder，用以缓存findView操作
    private class FootHolder extends RecyclerView.ViewHolder{
        private ImageView progress_dialog_img;
        private RelativeLayout rl_root;
        private TextView tv_tips;


        private FootHolder(View itemView){
            super(itemView);
            progress_dialog_img = itemView.findViewById(R.id.progress_dialog_img);
            rl_root = itemView.findViewById(R.id.rl_root);
            tv_tips = itemView.findViewById(R.id.tv_tips);

        }
    }

    public void setItemClickListener (ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }
    public interface ItemClickListener{
        void onItemClick(WarningRecord warningRecord);
    }
}
