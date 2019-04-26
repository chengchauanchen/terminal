package cn.vsx.vc.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;

public class GroupVideoLiveListAdapter extends BaseQuickAdapter<Member, BaseViewHolder> {

    //是否是组内正在上报的列表
    private boolean isGroupVideoLiving;
    private OnItemClickListerner onItemClickListerner;

    public GroupVideoLiveListAdapter(boolean isGroupVideoLiving) {
        super(R.layout.item_group_video_live, new ArrayList<Member>());
        this.isGroupVideoLiving = isGroupVideoLiving;
    }

    @Override
    protected void convert(BaseViewHolder helper, Member item) {
        if (item != null){
            //图标
            helper.setImageResource(R.id.iv_user_photo,R.drawable.icon_phone);
//            helper.setImageResource(R.id.iv_user_photo,R.drawable.icon_lte);
//            helper.setImageResource(R.id.iv_user_photo,R.drawable.icon_record);
//            helper.setImageResource(R.id.iv_user_photo,R.drawable.icon_car);
//            helper.setImageResource(R.id.iv_user_photo,R.drawable.icon_uav);
            //姓名
//            helper.setText(R.id.tv_user_name, listBean.getcName());
            //警号
//            helper.setText(R.id.tv_user_number, listBean.getcName());
            //时间
            helper.setGone(R.id.tv_time,!isGroupVideoLiving);
//            helper.setText(R.id.tv_time, listBean.getcName());
            //点击：观看
            helper.setOnClickListener(R.id.iv_watch, v -> {
                if(onItemClickListerner!=null){
                    onItemClickListerner.goToWatch(item);
                }
            });
            //点击：转发
            helper.setOnClickListener(R.id.iv_forward, v -> {
                if(onItemClickListerner!=null){
                    onItemClickListerner.goToForward(item);
                }
            });
        }
    }

    public void setOnItemClickListerner(OnItemClickListerner onItemClickListerner){
        this.onItemClickListerner = onItemClickListerner;
    }


    public interface  OnItemClickListerner{
        void goToWatch(Member item);
        void goToForward(Member item);
    }
}
