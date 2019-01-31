package cn.vsx.vc.adapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.HandleIdUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/5/3
 * 描述：
 * 修订历史：
 */

public class StackViewAdapter extends BaseAdapter{

    private List<TerminalMessage> data;
    private LayoutInflater inflater;
    private CloseDialogListener closeDialogListener;
    private GoWatchListener goWatchListener;
    private Context context;

    public StackViewAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
        data = new ArrayList<>();
    }

    public void addAll(Collection<TerminalMessage> collection) {
        if (isEmpty()){
            data.addAll(collection);
            notifyDataSetChanged();
        } else {
            data.addAll(collection);
        }
    }

    public void setData(List<TerminalMessage> terminalMessages){
        //先按照时间来排序，再根据类型来排序
        Collections.sort(terminalMessages, (o1, o2) -> (o1.sendTime) > (o2.sendTime) ? -1 : 1);
        data.clear();
        data.addAll(terminalMessages);
        for(int i = terminalMessages.size()-1; 0 <=i ; i--){
            //如果是警情，添加到最前面
            if(terminalMessages.get(i).messageType == MessageType.WARNING_INSTANCE.getCode()){
                data.remove(terminalMessages.get(i));
                data.add(0,terminalMessages.get(i));
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 添加到第一个位置
     */
    public void addData(TerminalMessage terminalMessage){
        data.add(0,terminalMessage);
        notifyDataSetChanged();
    }
    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void remove(int index) {
        if (index > -1 && index < data.size()) {
            data.remove(index);
            notifyDataSetChanged();
        }
    }

    /**
     * 把当前移到最后
     */
    public void setLast(int index){
        if (index > -1 && index < data.size()){
            TerminalMessage terminalMessage = data.get(index);
            data.add(data.size(),terminalMessage);
            data.remove(index);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public TerminalMessage getItem(int position){
        return data.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.video_dialog_item, parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //判断消息类型
        if(data.get(position).messageType == MessageType.WARNING_INSTANCE.getCode()){
            // TODO: 2018/5/4 根据警情级别显示不同的图片,以及设置标题
            viewHolder.dialog_root.setBackgroundResource(R.drawable.warning_background);
//            viewHolder.iv_warning_level.setImageResource();
        }else if(data.get(position).messageType == MessageType.VIDEO_LIVE.getCode()){
            viewHolder.iv_warning_level.setVisibility(View.GONE);
            viewHolder.dialog_root.setBackgroundResource(R.drawable.video_background);
            String liver = (String) data.get(position).messageBody.get("liver");
            if(!TextUtils.isEmpty(liver)){
                if(liver.contains("_")){
                    String[] split = liver.split("_");
                    String memberNo = split[0];
                    Member member = DataUtil.getMemberByMemberNo(Integer.valueOf(memberNo));
                    if(split.length>1){
                        String memberName = split[1];

                        if(TextUtils.isEmpty(member.getDepartmentName())){
                            viewHolder.video_member.setText(String.format(context.getString(R.string.text_unknown_sector_name),memberName,HandleIdUtil.handleId(Integer.valueOf(memberNo))));
                        }else{
                            viewHolder.video_member.setText(String.format(context.getString(R.string.text_known_sector_name),memberName,HandleIdUtil.handleId(Integer.valueOf(memberNo)),member.getDepartmentName()));
                        }

                        viewHolder.tv_live_theme.setText(String.format(context.getString(R.string.text_living_theme_member_name),memberName));
                    }else {
                        if(TextUtils.isEmpty(member.getDepartmentName())){
                            viewHolder.video_member.setText(String.format(context.getString(R.string.text_unknown_sector_name),member.getName(),HandleIdUtil.handleId(Integer.valueOf(memberNo))));
                        }else{
                            viewHolder.video_member.setText(String.format(context.getString(R.string.text_known_sector_name),member.getName(),HandleIdUtil.handleId(Integer.valueOf(memberNo)),member.getDepartmentName()));
                        }
                        viewHolder.tv_live_theme.setText(String.format(context.getString(R.string.text_living_theme_member_name),member.getName()));
                    }

                }else {
                    Log.e("StackViewAdapter","有上报者，但是liver里没有_，无法获取编号和名字");
                }
            }else {
                Member member = DataUtil.getMemberByMemberNo(data.get(position).messageFromId);
                if(TextUtils.isEmpty(member.getDepartmentName())){
                    viewHolder.video_member.setText(String.format(context.getString(R.string.text_unknown_sector_name),data.get(position).messageFromName,HandleIdUtil.handleId(member.getNo())));
                }else {
                    viewHolder.video_member.setText(String.format(context.getString(R.string.text_known_sector_name),data.get(position).messageFromName,HandleIdUtil.handleId(member.getNo()),member.getDepartmentName()));
                }

                viewHolder.tv_live_theme.setText(String.format(context.getString(R.string.text_living_theme_member_name),member.getName()));
            }

            //如果有标题就设置标题，这个放在最后设置
            if(!TextUtils.isEmpty(data.get(position).messageBody.getString(JsonParam.TITLE))){
                viewHolder.tv_live_theme.setText(data.get(position).messageBody.getString(JsonParam.TITLE));
            }

        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date(data.get(position).sendTime);
        viewHolder.tv_time.setText(simpleDateFormat.format(date));
        viewHolder.lv_live_return.setOnClickListener(v -> {
            if(closeDialogListener!=null){
                closeDialogListener.onCloseDialogClick(position);
            }
        });
        viewHolder.btn_live_gowatch.setTag(data.get(position));
        viewHolder.btn_live_gowatch.setOnClickListener(v -> {
            if(goWatchListener!=null){
                goWatchListener.onGoWatchClick(position);
            }
        });
        return convertView;
    }

    class ViewHolder{
        ConstraintLayout dialog_root;
        TextView tv_live_theme;
        TextView video_member;
        TextView tv_time;
        ImageView lv_live_return;
        ImageView iv_warning_level;
        Button btn_live_gowatch;

        public ViewHolder(View view){
            dialog_root =  view.findViewById(R.id.dialog_root);
            tv_live_theme =  view.findViewById(R.id.tv_live_theme);
            iv_warning_level =  view.findViewById(R.id.iv_warning_level);
            video_member =  view.findViewById(R.id.video_member);
            tv_time =  view.findViewById(R.id.tv_time);
            lv_live_return = view.findViewById(R.id.lv_live_return);
            btn_live_gowatch = view.findViewById(R.id.btn_live_gowatch);
        }
    }

    public void setCloseDialogListener(CloseDialogListener closeDialogListener){
        this.closeDialogListener = closeDialogListener;
    }

    public void setGoWatchListener(GoWatchListener goWatchListener){
        this.goWatchListener = goWatchListener;
    }

    public interface CloseDialogListener{
        void onCloseDialogClick(int position);
    }

    public interface GoWatchListener{
        void onGoWatchClick(int position);
    }
}
