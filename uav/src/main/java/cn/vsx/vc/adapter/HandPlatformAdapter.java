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
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.ShouTaiBean;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by jamie on 2017/10/27.
 */

public class HandPlatformAdapter extends BaseExpandableListAdapter {
    private List<ShouTaiBean.BuMenBean> list;
    private Activity activity;

    public HandPlatformAdapter(List<ShouTaiBean.BuMenBean> list, Activity activity) {
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
        if (convertView == null) {
            convertView = View.inflate(activity, R.layout.item_shoutai, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.shoutai_bumen_name.setText(list.get(groupPosition).bumenName);
        viewHolder.shoutai_group_size.setText("(" + list.get(groupPosition).memberList.size() + ")"); //部门下的成员个数
        if (isExpanded) {
            viewHolder.is_shoutai.setBackgroundResource(R.drawable.new_folder_open);

        } else {
            viewHolder.is_shoutai.setBackgroundResource(R.drawable.new_folder_close);
        }
        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderPerson viewHolderPerson = null;
        if (convertView == null) {
            convertView = View.inflate(activity, R.layout.shoutai_item_childview, null);
            viewHolderPerson = new ViewHolderPerson(convertView);
            convertView.setTag(viewHolderPerson);
        } else {
            viewHolderPerson = (ViewHolderPerson) convertView.getTag();
        }
        viewHolderPerson.shoutai_tv_member_name.setText(list.get(groupPosition).memberList.get(childPosition).getName());
        String phoneNo = list.get(groupPosition).memberList.get(childPosition).phone;
        String id = HandleIdUtil.handleId(list.get(groupPosition).memberList.get(childPosition).id);
        if (TextUtils.isEmpty(phoneNo)) {
            viewHolderPerson.shoutai_tv_member_id.setText(id);
        } else {
            viewHolderPerson.shoutai_tv_member_id.setText(phoneNo);
        }

        viewHolderPerson.shoutai_tv_member_name.setText(list.get(groupPosition).memberList.get(childPosition).getName());
        viewHolderPerson.shoutai_call_to.setOnClickListener(v -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                ToastUtil.showToast(activity,activity.getString(R.string.text_no_call_permission));
            }else {
                activeIndividualCall(groupPosition,childPosition);
            }


        });
        viewHolderPerson.shoutai_message_to.setOnClickListener(v -> {
            Member member = list.get(groupPosition).memberList.get(childPosition);
            IndividualNewsActivity.startCurrentActivity(activity, member.no, member.getName());
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public static class ViewHolder {

        TextView shoutai_bumen_name;

        TextView shoutai_group_size;

        ImageView is_shoutai;

        public ViewHolder(View rootView) {
            shoutai_bumen_name = rootView.findViewById(R.id.shoutai_bumen_name);
            shoutai_group_size = rootView.findViewById(R.id.shoutai_group_size);
            is_shoutai = rootView.findViewById(R.id.is_shoutai);

        }
    }

    public static class ViewHolderPerson {

        LinearLayout shoutai_call_to;

        LinearLayout shoutai_message_to;

        TextView shoutai_tv_member_name;

        TextView shoutai_tv_member_id;

        public ViewHolderPerson(View rootView) {
            shoutai_call_to = rootView.findViewById(R.id.shoutai_call_to);
            shoutai_message_to = rootView.findViewById(R.id.shoutai_message_to);
            shoutai_tv_member_name = rootView.findViewById(R.id.shoutai_tv_member_name);
            shoutai_tv_member_id = rootView.findViewById(R.id.shoutai_tv_member_id);

        }
    }

    /**
     * 请求个呼
     *
     * @param
     */
    private void activeIndividualCall(int groupPosition, int childPosition) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class,
                    list.get(groupPosition).memberList.get(childPosition),0l);
        } else {
            ToastUtil.showToast(activity, activity.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

}
