package com.vsxin.terminalpad.mvp.ui.adapter;

import android.support.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.entity.HistoryMediaBean;
import java.util.List;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/5/31
 * 描述：
 * 修订历史：
 */
public class PlayHistoryVideoAdapter extends BaseQuickAdapter<HistoryMediaBean, BaseViewHolder> {

    public PlayHistoryVideoAdapter(int layoutResId, @Nullable List<HistoryMediaBean> data){
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, HistoryMediaBean item){
        String startTime = item.getStartTime();
        if(startTime.length()>12){
            String date = startTime.substring(0, 8);
            String hour = startTime.substring(8, 10);
            String min = startTime.substring(10, 12);
            String second = startTime.substring(12);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(date).append(" ").append(hour).append(":").append(min).append(":").append(second);
            helper.setText(R.id.tv_start_time, stringBuffer.toString());
        }
        if(item.isSelected()){
            helper.setTextColor(R.id.tv_start_time, mContext.getResources().getColor(R.color.blue_21bfe2));
        }else{
            helper.setTextColor(R.id.tv_start_time, mContext.getResources().getColor(R.color.white));
        }
    }
}
