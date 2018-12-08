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

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.ShouTaiBean;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by jamie on 2017/10/27.
 */

public class HandPlatformAdapter extends BaseExpandableListAdapter {
    private List<ShouTaiBean.BuMenBean> list;
    private Activity activity;
    private ViewHolder viewHolder;
    private ViewHolderPerson viewHolderPerson;
    private String phoneNo;
    private String id;

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
        viewHolder = null;
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
        viewHolderPerson = null;
        if (convertView == null) {
            convertView = View.inflate(activity, R.layout.shoutai_item_childview, null);
            viewHolderPerson = new ViewHolderPerson(convertView);
            convertView.setTag(viewHolderPerson);
        } else {
            viewHolderPerson = (ViewHolderPerson) convertView.getTag();
        }
        viewHolderPerson.shoutai_tv_member_name.setText(list.get(groupPosition).memberList.get(childPosition).getName());
        phoneNo = list.get(groupPosition).memberList.get(childPosition).phone;
        id = HandleIdUtil.handleId(list.get(groupPosition).memberList.get(childPosition).id);
        if (TextUtils.isEmpty(phoneNo)) {
            viewHolderPerson.shoutai_tv_member_id.setText(id);
        } else {
            viewHolderPerson.shoutai_tv_member_id.setText(phoneNo);
        }

        viewHolderPerson.shoutai_tv_member_name.setText(list.get(groupPosition).memberList.get(childPosition).getName());
        viewHolderPerson.shoutai_call_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(activity,"没有个呼功能权限");
                }else {
                    activeIndividualCall(groupPosition,childPosition);
                }


            }
        });
        viewHolderPerson.shoutai_message_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Member member = list.get(groupPosition).memberList.get(childPosition);
                IndividualNewsActivity.startCurrentActivity(activity, member.id, member.getName());
            }
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public static class ViewHolder {
        @Bind(R.id.shoutai_bumen_name)
        TextView shoutai_bumen_name;
        @Bind(R.id.shoutai_group_size)
        TextView shoutai_group_size;
        @Bind(R.id.is_shoutai)
        ImageView is_shoutai;

        public ViewHolder(View rootView) {
            ButterKnife.bind(this, rootView);
        }
    }

    public static class ViewHolderPerson {
        @Bind(R.id.shoutai_call_to)
        LinearLayout shoutai_call_to;
        @Bind(R.id.shoutai_message_to)
        LinearLayout shoutai_message_to;
        @Bind(R.id.shoutai_tv_member_name)
        TextView shoutai_tv_member_name;
        @Bind(R.id.shoutai_tv_member_id)
        TextView shoutai_tv_member_id;

        public ViewHolderPerson(View rootView) {
            ButterKnife.bind(this, rootView);
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
            int callID = list.get(groupPosition).memberList.get(childPosition).id;
            int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(callID,"");
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, list.get(groupPosition).memberList.get(childPosition));
            } else {
                ToastUtil.individualCallFailToast(activity, resultCode);
            }

        } else {
            ToastUtil.showToast(activity, "网络连接异常，请检查网络！");
        }
    }

}
