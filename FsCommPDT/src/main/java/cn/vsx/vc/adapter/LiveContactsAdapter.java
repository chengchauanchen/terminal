package cn.vsx.vc.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by zckj on 2017/6/13.
 */

public class LiveContactsAdapter extends BaseAdapter {

    private List<Member> data = new ArrayList<>();
    private Context context;
    private String keyWords = "";
    private boolean isPush;
    private int lastCheckedItem = -1;
    private OnItemClickListener onItemClickListener;
    private List<Member> pushMembers = new ArrayList<>();
    private Member liveMember;//请求上报者

    public LiveContactsAdapter(Context context, List<Member> list, boolean isPush) {
        this.context = context;
        for(Member member : list){
            if(member.isChecked()){
                member.setChecked(false);
            }
        }
        Collections.sort(list);
        data.addAll(list);
        this.isPush = isPush;
    }

    public void addData(List<Member> list){
        for(Member member : list){
            if(member.isChecked()){
                member.setChecked(false);
            }
        }
        Collections.sort(list);
        data.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (data.isEmpty()) {
            return 0;
        } else {
            return data.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder ;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.live_select_member_listview_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Member member = data.get(position);
        // 获取上一个人
        // 当前位置的上一个人，判断首位拼英
        Member aboveCurrentMember;
        if (position == 0) {
            aboveCurrentMember = null;
        } else {
            aboveCurrentMember = data.get(position - 1);
        }
        // 当前人  拼音的首字母  和 上一个人的拼音的首字母  如果相同  隐藏
        boolean isShow = true; // 是否显示拼音
        if(null != aboveCurrentMember){
            if (member.pinyin.charAt(0) == aboveCurrentMember.pinyin.charAt(0)) {
                isShow = false;
            }
        }

        final String name = member.getName();
        String id = HandleIdUtil.handleId(member.getNo());
        if (!Util.isEmpty(name) && !Util.isEmpty(keyWords) && name.toLowerCase().contains(keyWords.toLowerCase())) {
            int index = name.toLowerCase().indexOf(keyWords.toLowerCase());
            int len = keyWords.length();
            Spanned temp = Html.fromHtml(name.substring(0, index)
                    + "<u><font color=#FF0000>"
                    + name.substring(index, index + len) + "</font></u>"
                    + name.substring(index + len, name.length()));

            viewHolder.tvSelectmemberName.setText(temp);
        } else {
            viewHolder.tvSelectmemberName.setText(name);
        }

        if (!Util.isEmpty(id) && !Util.isEmpty(keyWords) && id.contains(keyWords)) {
            int index = id.indexOf(keyWords);
            int len = keyWords.length();
            Spanned temp = Html.fromHtml(id.substring(0, index)
                    + "<u><font color=#FF0000>"
                    + id.substring(index, index + len) + "</font></u>"
                    + id.substring(index + len, id.length()));

            viewHolder.tv_selectmember_id.setText(temp);
        } else {
            viewHolder.tv_selectmember_id.setText(id);
        }
        viewHolder.tv_pinyin.setVisibility(isShow ? View.VISIBLE : View.GONE);
        viewHolder.tv_pinyin.setText(member.pinyin.substring(0,1));

        if (member.terminalMemberType.equals(TerminalMemberType.TERMINAL_PAD.getValue())){
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.pad);
        } else if (member.terminalMemberType.equals(TerminalMemberType.TERMINAL_PHONE.getValue())) {
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.imgphone);
        } else  {
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.pc);
        }

        if(data.get(position).isChecked){
            viewHolder.iv_select.setSelected(true);
        }else {
            viewHolder.iv_select.setSelected(false);
        }

        convertView.setOnClickListener(v -> {

            //如果是上报逻辑
            if(isPush){
                //判断有没有推送的权限
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
                    ToastUtil.showToast(context,"您没有推送权限，点“开始”按钮直接上报");
                    return;
                }else {
                    if(member.isChecked){
                        member.setChecked(false);
                        pushMembers.remove(member);
                    }else {
                        member.setChecked(true);
                        pushMembers.add(member);
                    }
                }
            }
            //如果是请求逻辑
            else{
                if(member.isChecked){
                    member.setChecked(false);
                    liveMember = null;
                    lastCheckedItem = -1;
                }else {
                    //取消上一个选中的item
                    if(lastCheckedItem!=-1){
                        data.get(lastCheckedItem).setChecked(false);
                    }
                    member.setChecked(true);
                    liveMember = member;
                    lastCheckedItem = position;
                }

            }
            //回调到listview界面
            if(onItemClickListener!=null){
                onItemClickListener.onItemClick(pushMembers,liveMember,isPush);
            }
            notifyDataSetChanged();
        });
        return convertView;
    }

    public ArrayList<Integer> getPushMemberList() {
        ArrayList<Integer> pushNos = new ArrayList<>();
        for(Member pushMember : pushMembers){
            pushNos.add(pushMember.getId());
        }
        return pushNos;
    }



    public Member getLiveMember() {
        if (liveMember!=null&&liveMember.isChecked() ) {
            return liveMember;
        } else {
            return null;
        }
    }

    public void setKeyWords(String keyWords){
        this.keyWords = keyWords;
    }

    static class ViewHolder {
        @Bind(R.id.catagory)
        LinearLayout ll_item_person_contacts;
        @Bind(R.id.tv_catagory)
        TextView tv_pinyin;
        @Bind(R.id.iv_selectmember_headsculpture)
        ImageView ivSelectmemberHeadsculpture;
        @Bind(R.id.tv_selectmember_id)
        TextView tv_selectmember_id;
        @Bind(R.id.tv_selectmember_name)
        TextView tvSelectmemberName;
        @Bind(R.id.iv_select)
        ImageView iv_select;
        @Bind(R.id.img_terminalMemberType)
        ImageView img_terminalMemberType;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick(List<Member>pushMembers ,Member liveMember,boolean isPush);
    }
}
