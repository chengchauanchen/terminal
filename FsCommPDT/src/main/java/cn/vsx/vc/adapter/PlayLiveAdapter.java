package cn.vsx.vc.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.vsx.vc.R;
import cn.vsx.vc.model.MediaBean;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/5/31
 * 描述：
 * 修订历史：
 */
public class PlayLiveAdapter extends BaseQuickAdapter<MediaBean, BaseViewHolder>{

    public PlayLiveAdapter(int layoutResId, @Nullable List<MediaBean> data){
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MediaBean item){
        String startTime = item.getStartTime();
        if(startTime.length()>12){
            String date = startTime.substring(0, 8);
            String hour = startTime.substring(8, 10);
            String min = startTime.substring(10, 12);
            String second = startTime.substring(12);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(date).append(" ").append(hour).append(min).append(second);
            helper.setText(R.id.tv_start_time, stringBuffer.toString());
        }
        if(item.isSelected()){
            helper.setTextColor(R.id.tv_start_time, mContext.getResources().getColor(R.color.blue_21bfe2));
        }else{
            helper.setTextColor(R.id.tv_start_time, mContext.getResources().getColor(R.color.white));
        }
    }
}
