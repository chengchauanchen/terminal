package cn.vsx.vc.adapter;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.BitmapUtil;
import ptt.terminalsdk.tools.StringUtil;

public class GroupVideoLiveListAdapter extends BaseQuickAdapter<TerminalMessage, BaseViewHolder> {

    //是否是组内正在上报的列表
    private boolean isGroupVideoLiving;
    private OnItemClickListerner onItemClickListerner;

    public GroupVideoLiveListAdapter(boolean isGroupVideoLiving) {
        super(R.layout.item_group_video_live, new ArrayList<TerminalMessage>());
        this.isGroupVideoLiving = isGroupVideoLiving;
    }

    @Override
    protected void convert(BaseViewHolder helper, TerminalMessage item) {
        if (item != null){
            item.messageUrl = "";
            //图标
            try{
                helper.setImageResource(R.id.iv_user_photo,
                    BitmapUtil.getImageResourceByType(
                        TerminalMemberType.valueOf(
                            TextUtils.isEmpty(item.terminalMemberType)?TerminalMemberType.TERMINAL_PHONE.toString():item.terminalMemberType)
                            .getCode()
                    ));
            }catch (Exception e){
                e.printStackTrace();
            }
            JSONObject messageBody = item.messageBody;
            if (messageBody!=null&&!TextUtils.isEmpty(messageBody.toJSONString())) {
                //姓名
                if(messageBody.containsKey(JsonParam.BACKUP)){
                    String backUp = messageBody.getString(JsonParam.BACKUP);
                    if(!TextUtils.isEmpty(backUp)&&backUp.contains("_")){
                        String[] split = backUp.split("_");
                        if(split.length>1){
                            helper.setText(R.id.tv_user_name, split[1]);
                        }
                        helper.setText(R.id.tv_user_name, "");
                    }else{
                        helper.setText(R.id.tv_user_name, "");
                    }
                }
                //警号
                if(messageBody.containsKey(JsonParam.LIVERNO)){
                    helper.setText(R.id.tv_user_number, String.valueOf(messageBody.getIntValue(JsonParam.LIVERNO)));
                }else{
                    helper.setText(R.id.tv_user_number, "");
                }
            }

            //时间
            helper.setGone(R.id.tv_time,!isGroupVideoLiving);
            helper.setText(R.id.tv_time, StringUtil.stringToDate(item.sendTime));
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
        void goToWatch(TerminalMessage item);
        void goToForward(TerminalMessage item);
    }
}
