package cn.vsx.vc.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.ShouTaiBean;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by jamie on 2017/10/27.
 */

public class PoliceAffairsAdapter extends BaseExpandableListAdapter {
    private Activity activity;
    private List<ShouTaiBean.BuMenBean> list;

    public PoliceAffairsAdapter(List<ShouTaiBean.BuMenBean> list, Activity activity){
        this.list = list;
        this.activity = activity;
    }
    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return list.get(groupPosition).memberList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return list.get(groupPosition).memberList;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = View.inflate(activity,R.layout.item_jingwutong,null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.jingwutong_bumenName.setText(list.get(groupPosition).bumenName); //文件夹图标变化
        viewHolder.jingwutong_group_size.setText("("+list.get(groupPosition).memberList.size() + ")"); //部门下的成员个数
        if (isExpanded) {
            viewHolder.is_jingwutong_img.setBackgroundResource(R.drawable.new_folder_open);
        } else {
            viewHolder.is_jingwutong_img.setBackgroundResource(R.drawable.new_folder_close);
        }

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderPerson viewHolderPerson = null;
        if (convertView == null){
            convertView = View.inflate(activity,R.layout.jingwutong_item_childview,null);
            viewHolderPerson = new ViewHolderPerson(convertView);
            convertView.setTag(viewHolderPerson);
        }else {
            viewHolderPerson = (ViewHolderPerson) convertView.getTag();
        }
        viewHolderPerson.jignwutong_user_logo.setImageResource(BitmapUtil.getUserPhoto());
        Member member = list.get(groupPosition).memberList.get(childPosition);
        String phoneNo = member.phone;
        String id = HandleIdUtil.handleId(member.id);
        if (!TextUtils.isEmpty(id)){
            viewHolderPerson.jingwutong_tv_member_id.setText(id);
        }else {
            viewHolderPerson.jingwutong_tv_member_id.setText(phoneNo);
        }
        if (MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0) == member.id){
            viewHolderPerson.jingwutong_call_to.setVisibility(View.GONE);
            viewHolderPerson.jingwutong_message_to.setVisibility(View.GONE);
        }else {
            viewHolderPerson.jingwutong_call_to.setVisibility(View.VISIBLE);
            viewHolderPerson.jingwutong_message_to.setVisibility(View.VISIBLE);
        }

        viewHolderPerson.jingwutong_tv_member_name.setText(list.get(groupPosition).memberList.get(childPosition).getName());
        viewHolderPerson.jingwutong_call_to.setOnClickListener(v -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                ToastUtil.showToast(activity,activity.getString(R.string.text_no_call_permission));
            }else {
                activeIndividualCall(groupPosition,childPosition);
            }


        });
        viewHolderPerson.jingwutong_message_to.setOnClickListener(v -> {
            Member member1 = list.get(groupPosition).memberList.get(childPosition);
            IndividualNewsActivity.startCurrentActivity(activity, member1.no, member1.getName(),member1.getType());
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
    public static class ViewHolder {

        TextView jingwutong_bumenName;

        TextView jingwutong_group_size;

        ImageView is_jingwutong_img;

        public ViewHolder(View rootView) {
            jingwutong_bumenName = rootView.findViewById(R.id.jingwutong_bumen_name);
            jingwutong_group_size = rootView.findViewById(R.id.jingwutong_group_size);
            is_jingwutong_img = rootView.findViewById(R.id.is_jingwutong_img);
        }
    }
    public static class ViewHolderPerson {

        LinearLayout jingwutong_call_to;

        LinearLayout jingwutong_message_to;

        ImageView jignwutong_user_logo;
        TextView jingwutong_tv_member_name;

        TextView jingwutong_tv_member_id;

        public ViewHolderPerson(View rootView) {
            jignwutong_user_logo = rootView.findViewById(R.id.jignwutong_user_logo);
            jingwutong_call_to = rootView.findViewById(R.id.jingwutong_call_to);
            jingwutong_message_to = rootView.findViewById(R.id.jingwutong_message_to);
            jingwutong_tv_member_name = rootView.findViewById(R.id.jingwutong_tv_member_name);
            jingwutong_tv_member_id = rootView.findViewById(R.id.jingwutong_tv_member_id);
        }
    }
    /**
     * 请求个呼
     * @param
     */
    private void activeIndividualCall(int groupPosition,int childPosition) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();

        if (network){
            if ( list.size() > 0) {
//                Member member = DataUtil.getMemberByMemberNo(currentGroupMembers.get(position).no);
//                List<Member> list = new ArrayList<>();
//                new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, list, (view1, position12) -> {
//                    long uniqueNo = 0l;
//                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member,uniqueNo);
//                }).show();
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, list.get(groupPosition).memberList.get(childPosition));

            }
        } else {
            ToastUtil.showToast(activity, activity.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }
}
