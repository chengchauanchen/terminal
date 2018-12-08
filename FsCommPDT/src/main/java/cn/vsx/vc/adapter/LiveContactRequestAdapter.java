package cn.vsx.vc.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.HandleIdUtil;

import static ptt.terminalsdk.tools.PhoneAdapter.logger;

/**
 * Created by zckj on 2017/6/13.
 */

public class LiveContactRequestAdapter extends BaseAdapter {
    private List<Member> list;
    private Context context;
    //    private List<Integer> memberIDList = new ArrayList<>();
    private MyApplication.TYPE type;
    private int mPosition;
    private Map<Integer, Boolean> mHashMap;
    private Member memberExceptMe;// 上一个人
    private String keyWords = "";
    public LiveContactRequestAdapter(Context context, List<Member> list, MyApplication.TYPE type, int mPosition) {
        this.context = context;
        this.list = new ArrayList<>();
        this.list.addAll(list);
        this.type = type;
        this.mPosition = mPosition;
        // 初始化所有checked
        mHashMap = new HashMap<>();
    }

    public void bind(List<Member> stringList, List<Member> item, String keyWords) {
        this.keyWords = keyWords;
        this.list = stringList;
        Log.e("sjl_","bind:"+item.toString());
        if (item != null && item.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Log.e("sjl_","list.get(i).id:"+list.get(i).id);
                if (isChecked(item, list.get(i).id)) {
                    list.get(i).isChecked = true;
                } else {
                    list.get(i).isChecked = false;
                }
            }
        }
        notifyDataSetChanged();
    }

    public boolean isChecked(List<Member> item, int id) {
        Log.e("sjl_","isChecked:"+item.size());
        for (int i = 0; i < item.size(); i++) {
            Log.e("sjl_","item.get(i).id:"+item.get(i).id);
            if (item.get(i).id == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getCount() {
        if (list.size() > 0) {
            return list.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.live_request_select_member_listview_item, null);
            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        final Member member = list.get(position);
        // 获取上一个人
        if (position== 0){
            memberExceptMe = null;
        }else{
            memberExceptMe = list.get(position - 1);
        }
        // 当前人  拼音的首字母  和 上一个人的拼音的首字母  如果相同  隐藏
        boolean isShow = true; // 是否显示拼音
        if ( memberExceptMe == null){
            isShow = true;
        }else{
            logger.info(member.pinyin);

            if (member.pinyin.charAt(0)  == memberExceptMe.pinyin.charAt(0)){
                isShow = false;
            }
        }

        String name = member.getName();
        String id = HandleIdUtil.handleId(member.id);

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
        viewHolder.tv_pinyin.setText(member.pinyin.charAt(0)+"");

        if (member.terminalMemberType.equals("TERMINAL_PAD")){
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.pad);
        }else if (member.terminalMemberType.equals("TERMINAL_COMMON")){
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.imgphone);
        }else if (member.terminalMemberType.equals("TERMINAL_HDMI")){
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.pc);
        }

        viewHolder.rbSelectmember.setVisibility(View.VISIBLE);
        viewHolder.cbSelectmember.setVisibility(View.GONE);

        final RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.rb_request_selectmember);
        viewHolder.rbSelectmember = radioButton;

        if (mPosition == position) {
            viewHolder.rbSelectmember.setChecked(true);
            liveMember = member;
        } else {
            viewHolder.rbSelectmember.setChecked(false);
        }
        return convertView;
    }

    public void removeKey(int id) {
        mHashMap.remove(id);
    }

    public void addKey(int id) {
        mHashMap.put(id, true);
    }

    public List<Integer> getMemberList() {
        List<Integer> ids = new ArrayList<>();
        ArrayList<Integer> integers = new ArrayList<>(mHashMap.keySet());
        for (int i : integers) {
            ids.add(list.get(i).id);
        }
        return ids;
    }

    private Member liveMember;

    public Member getLiveMember() {
        return liveMember;
    }

    static class ViewHolder {
        @Bind(R.id.catagory_request)
        LinearLayout ll_item_person_contacts;
        @Bind(R.id.tv_request_catagory)
        TextView tv_pinyin;
        @Bind(R.id.iv_request_selectmember_headsculpture)
        ImageView ivSelectmemberHeadsculpture;
        @Bind(R.id.tv_request_selectmember_id)
        TextView tv_selectmember_id;
        @Bind(R.id.tv_request_selectmember_name)
        TextView tvSelectmemberName;
        @Bind(R.id.cb_request_selectmember)
        CheckBox cbSelectmember;
        @Bind(R.id.rb_request_selectmember)
        RadioButton rbSelectmember;
        @Bind(R.id.img_request_terminalMemberType)
        ImageView img_terminalMemberType;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void refreshLiveContactsAdapter(int mPosition, List<Member> list) {
        this.mPosition = mPosition;
        this.list = new ArrayList<>();
        this.list.addAll(list);

        notifyDataSetChanged();
    }

    public void upDataId(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                if (this.type == MyApplication.TYPE.PUSH) {
                    list.get(i).isChecked = true;
                    addKey(id);
                } else {
                    mPosition = i;
                    this.liveMember = list.get(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Member getData(int id){
        for (int i = 0; i <list.size() ; i++) {
            if (list.get(i).id ==id){
                return list.get(i);
            }
        }
        return  new Member();
    }
}
