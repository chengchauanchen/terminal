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

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static ptt.terminalsdk.tools.PhoneAdapter.logger;

/**
 * Created by zckj on 2017/6/13.
 */

public class LiveContactsAdapter extends BaseAdapter {

    private List<Member> list;
    private Context context;
    //上报图像时被选中的人，推送给他们看
//    private Map<Integer, Boolean> pushMap;
    // 用来控制CheckBox的选中状况
//    private HashMap<Integer, Boolean> isSelected = new HashMap<>();

    private List<Member> pushMembers = new ArrayList<>();
    private Member memberExceptMe;// 上一个人
    private String keyWords = "";
    private boolean isPush;
    private OnItemClickListener onItemClickListener;

    public LiveContactsAdapter(Context context, List<Member> list, boolean isPush) {
        this.context = context;
        this.list = new ArrayList<>();
        this.list.addAll(list);
        this.isPush = isPush;
    }

    public void bind(List<Member> stringList, List<Member> item, String keyWords) {
        this.keyWords = keyWords;
        this.list = stringList;

        notifyDataSetChanged();
    }

    public boolean isChecked(List<Member> item, int id) {
        Log.e("sjl_", "isChecked:" + item.size());
        for (int i = 0; i < item.size(); i++) {
            Log.e("sjl_", "item.get(i).id:" + item.get(i).id);
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
    public Member getItem(int position) {
        return list.get(position);
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


        final Member member = list.get(position);
        // 获取上一个人
        if (position == 0) {
            memberExceptMe = null;
        } else {
            memberExceptMe = list.get(position - 1);
        }
        // 当前人  拼音的首字母  和 上一个人的拼音的首字母  如果相同  隐藏
        boolean isShow = true; // 是否显示拼音
        if (memberExceptMe == null) {
            isShow = true;
        } else {
            logger.info(member.pinyin);

            if (member.pinyin.charAt(0) == memberExceptMe.pinyin.charAt(0)) {
                isShow = false;
            }
        }

        final String name = member.getName();
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
        viewHolder.tv_pinyin.setText(member.pinyin.charAt(0) + "");

        if (member.terminalMemberType.equals("TERMINAL_PAD")) {
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.pad);
        } else if (member.terminalMemberType.equals("TERMINAL_PHONE")) {
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.imgphone);
        } else  {
            viewHolder.img_terminalMemberType.setBackgroundResource(R.drawable.pc);
        }

        viewHolder.rbSelectmember.setVisibility(View.GONE);
        viewHolder.cbSelectmember.setVisibility(View.VISIBLE);

       if(isPush){
           //上报图像
           viewHolder.cbSelectmember.setChecked(list.get(position).isChecked);
        }else{
          //请求图像   由于请求图像每次只能选择一个人，这里需要规避从搜索列表转到全列表显示时，之前已经选择的不能取消的问题
           if(liveMember!=null){
               //之前已经有选择的
               viewHolder.cbSelectmember.setChecked(member.id == liveMember.id);
           }else{
               //之前没有选择
               viewHolder.cbSelectmember.setChecked(list.get(position).isChecked);
           }
       }


        convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                //如果是上报逻辑
                if(isPush){
                    //判断有没有推送的权限
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_PUSH.name())){
                        ToastUtil.showToast(context,"您没有推送权限，点“开始”按钮直接上报");
                        return;
                    }else {
                        if(viewHolder.cbSelectmember.isChecked()){
                            viewHolder.cbSelectmember.setChecked(false);
                            member.isChecked = false;
                            pushMembers.remove(member);
                        }else {
                            viewHolder.cbSelectmember.setChecked(true);
                            member.isChecked = true;
                            pushMembers.add(member);
                        }
                    }
                }
                //如果是请求逻辑
                else{
                    if(viewHolder.cbSelectmember.isChecked()){
                        viewHolder.cbSelectmember.setChecked(false);
                        member.isChecked = false;
                        liveMember = null;
                    }else {
                        //取消上一个选中的item
                        if(liveMember!=null){
                            for (Member member:list) {
                                if(member.id == liveMember.id){
                                    member.isChecked = false;
                                }
                            }
                        }
                        viewHolder.cbSelectmember.setChecked(true);
                        member.isChecked = true;
                        liveMember = member;
                    }
                }
                //回调到listview界面
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position,member.isChecked,isPush);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    public List<Integer> getPushMemberList() {
        List<Integer> pushNos = new ArrayList<>();
        for(Member pushMember : pushMembers){
            pushNos.add(pushMember.getId());
        }
        return pushNos;
    }

    private Member liveMember;

    public Member getLiveMember() {
        if (liveMember!=null&&liveMember.isChecked ) {
            return liveMember;
        } else {
            return null;
        }
    }

    public void notifyLiveMember() {
        if (liveMember != null) {
            liveMember.isChecked = false;
        }
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
        @Bind(R.id.cb_selectmember)
        CheckBox cbSelectmember;
        @Bind(R.id.rb_selectmember)
        RadioButton rbSelectmember;
        @Bind(R.id.img_terminalMemberType)
        ImageView img_terminalMemberType;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void refreshLiveContactsAdapter(int mPosition, List<Member> list) {
        this.list = new ArrayList<>();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public Member getData(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                return list.get(i);
            }
        }
        return new Member();
    }

//    public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
//        this.isSelected.clear();
//        this.isSelected.putAll(isSelected);
//        Log.e("LiveContactsAdapter", "setIsSelected---isSelected:" + isSelected);
//    }

     public int getKey(HashMap<Integer,Boolean> map,boolean value){
         Integer key = null;

         for(Integer getKey: map.keySet()){
             if(map.get(getKey).equals(value)){
                 key = getKey;
             }
         }
         return key;

     }

     public void setOnItemClickListener(OnItemClickListener onItemClickListener){
         this.onItemClickListener = onItemClickListener;
     }
     public interface OnItemClickListener{
         void onItemClick(int position ,boolean checked,boolean isPush);
     }
}
