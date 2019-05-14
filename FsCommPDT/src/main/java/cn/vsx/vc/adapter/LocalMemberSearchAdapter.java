package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualCallForAddressBookHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverIndividualMsgForAddressBookHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import java.util.List;



import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by gt358 on 2017/10/25.
 */

public class LocalMemberSearchAdapter extends BaseAdapter {
    private OnItemBtnClickListener mItemBtnClickListener;
    private Context context;
    private List<Member> allMembersExceptMe;
    private Logger logger = Logger.getLogger(PersonContactsAdapter.class);
    private Member memberExceptMe;// 上一个人
    Handler handler = new Handler();
    private int longClickPos = -1;
    private int VOIP=0;
    private int TELEPHONE=1;
    private String keyWords;
    public LocalMemberSearchAdapter(Context context, List<Member> allMembersExceptMe) {
        this.context = context;
        this.allMembersExceptMe = allMembersExceptMe;
    }

    public void setLongClickPos (int longClickPos) {
        this.longClickPos = longClickPos;
        handler.post(() -> notifyDataSetChanged());
    }

    public void refreshPersonContactsAdapter() {
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return allMembersExceptMe.size();
    }

    @Override
    public Object getItem(int position) {
        return allMembersExceptMe.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.fragment_person_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final Member searchContactsBean = allMembersExceptMe.get(position);
        String name = searchContactsBean.getName();
        String id = HandleIdUtil.handleId(searchContactsBean.id);

        logger.info("类型："+"\n"+searchContactsBean.getType());

        if (TerminalMemberType.TERMINAL_PDT.getCode() == searchContactsBean.getType()){
            holder.llDialTo.setVisibility(View.GONE);
        }else {
            holder.llDialTo.setVisibility(View.VISIBLE);
        }

        if (!Util.isEmpty(name) && !Util.isEmpty(keyWords) && name.toLowerCase().contains(keyWords.toLowerCase())) {

            int index = name.toLowerCase().indexOf(keyWords.toLowerCase());

            int len = keyWords.length();

            Spanned temp = Html.fromHtml(name.substring(0, index)
                    + "<u><font color=#eb403a>"
                    + name.substring(index, index + len) + "</font></u>"
                    + name.substring(index + len, name.length()));

            holder.tv_member_name.setText(temp);
        } else {
            holder.tv_member_name.setText(name);
        }
        if (!Util.isEmpty(id) && !Util.isEmpty(keyWords) && id.contains(keyWords)) {

            int index = id.indexOf(keyWords);

            int len = keyWords.length();

            Spanned temp = Html.fromHtml(id.substring(0, index)
                    + "<u><font color=#eb403a>"
                    + id.substring(index, index + len) + "</font></u>"
                    + id.substring(index + len, id.length()));

            holder.tv_member_id.setText(temp);
        } else {
            holder.tv_member_id.setText(id);
        }

        Member personContactsBean = allMembersExceptMe.get(position);//当前位置的人
        // 获取上一个人
        if (position== 0){
            memberExceptMe = null;
        }else{
            memberExceptMe = allMembersExceptMe.get(position - 1);
        }
        // 当前人  拼音的首字母  和 上一个人的拼音的首字母  如果相同  隐藏
//        boolean isShow = true; // 是否显示拼音
//        if ( memberExceptMe == null){
//            isShow = true;
//        }else{
//            logger.info(personContactsBean.pinyin);
//
//            if (personContactsBean.pinyin.charAt(0)  == memberExceptMe.pinyin.charAt(0)){
//                isShow = false;
//            }
//        }
        holder.tv_pinyin.setVisibility(View.GONE);
        holder.tv_pinyin.setText(personContactsBean.pinyin.charAt(0)+"");
        holder.iv_individual_call.setOnClickListener(view -> {

            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverIndividualCallForAddressBookHandler.class, 4, position);
            if (mItemBtnClickListener!=null){
                mItemBtnClickListener.onItemBtnClick();
            }
        });
        holder.iv_individual_msg.setOnClickListener(v -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverIndividualMsgForAddressBookHandler.class, 4, position);
            if (mItemBtnClickListener!=null){
                mItemBtnClickListener.onItemBtnClick();
            }
        });
        holder.llDialTo.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(searchContactsBean.phone)) {

                ItemAdapter adapter = new ItemAdapter(context,ItemAdapter.iniDatas());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                //设置标题
                builder.setTitle(context.getString(R.string.text_call_up));
                builder.setAdapter(adapter, (dialogInterface, position1) -> {
                    if(position1 ==VOIP){//voip电话
                        if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                            Intent intent = new Intent(context, VoipPhoneActivity.class);
                            intent.putExtra("member",searchContactsBean);
                            context.startActivity(intent);
                        }else {
                            ToastUtil.showToast(context,context.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                        }
                    }
                    else if(position1 ==TELEPHONE){//普通电话

                        CallPhoneUtil.callPhone((Activity) context, searchContactsBean.phone);

                    }

                });
                builder.create();
                builder.show();
            }else {
                ToastUtil.showToast(context,context.getString(R.string.text_has_no_member_phone_number));
            }
        });
        holder.ivUserLogo.setOnClickListener(view -> {
            Intent intent = new Intent(context, UserInfoActivity.class);
            intent.putExtra("userId", searchContactsBean.getNo());
            intent.putExtra("userName", searchContactsBean.getName());
            context.startActivity(intent);
        });
        return convertView;
    }

    public void setFilterKeyWords(String filterKeyWords) {
        this.keyWords = filterKeyWords;
    }


    public void setOnItemBtnClick(OnItemBtnClickListener listener){
        this.mItemBtnClickListener=listener;
    }

    public interface OnItemBtnClickListener{
        void onItemBtnClick();
    }

    public static class ViewHolder {

        LinearLayout ll_person_search_item;

        LinearLayout ll_item_person_contacts;

        TextView tv_pinyin;

        TextView tv_member_name;

        TextView tv_member_id;

        LinearLayout iv_individual_msg;

        LinearLayout iv_individual_call;

        LinearLayout llDialTo;

        ImageView ivUserLogo;
        public ViewHolder(View rootView) {
            ll_person_search_item = rootView.findViewById(R.id.ll_person_search_item);

            ll_item_person_contacts = rootView.findViewById(R.id.catagory);

            tv_pinyin = rootView.findViewById(R.id.tv_catagory);

            tv_member_name = rootView.findViewById(R.id.tv_member_name);

            tv_member_id = rootView.findViewById(R.id.tv_member_id);

            iv_individual_msg = rootView.findViewById(R.id.message_to);

            iv_individual_call = rootView.findViewById(R.id.call_to);

            llDialTo = rootView.findViewById(R.id.shoutai_dial_to);

            ivUserLogo = rootView.findViewById(R.id.user_logo);

        }
    }
}
