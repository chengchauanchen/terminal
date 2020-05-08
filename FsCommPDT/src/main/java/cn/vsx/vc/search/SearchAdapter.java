package cn.vsx.vc.search;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.ResponseGroupType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.GroupCallNewsActivity;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.receiveHandle.ReceiverMonitorViewClickHandler;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.bean.SearchTitleBean;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by XX on 2018/4/11.
 */

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Object> mDatas;

    //分类 title
    private static final int TITLE = 1000;
    //监听组
    private static final int MONITOR_GROUP = 1001;
    //组
    private static final int GROUP = 1002;
    //成员
    private static final int MEMBER = 1003;


    public SearchAdapter(Context context, List<Object> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        Object bean = mDatas.get(position);

        if (bean instanceof SearchTitleBean) {
            return TITLE;
        } else if (bean instanceof GroupSearchBean) {
            GroupSearchBean group = (GroupSearchBean) bean;
            if (checkIsMonitorGroup(group)) {
                return MONITOR_GROUP;
            } else {
                return GROUP;
            }
        } else if (bean instanceof MemberSearchBean) {
            return MEMBER;
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TITLE:
                View view0 = LayoutInflater.from(mContext).inflate(R.layout.search_title_layout, parent, false);
                return new TitleViewHolder(view0);
            case MONITOR_GROUP:
                View view1 = LayoutInflater.from(mContext).inflate(R.layout.item_search_group, parent, false);
                return new GroupViewHolder(view1);
            case GROUP:
                View view2 = LayoutInflater.from(mContext).inflate(R.layout.item_search_group, parent, false);
                return new GroupViewHolder(view2);
            case MEMBER:
                View view3 = LayoutInflater.from(mContext).inflate(R.layout.item_search_contact, parent, false);
                return new UserViewHolder(view3);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        try{
            int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case TITLE:
                    SearchTitleBean data = (SearchTitleBean) mDatas.get(position);
                    TitleViewHolder holder = (TitleViewHolder) viewHolder;
                    bindTitle(data, holder);
                    break;
                case MONITOR_GROUP:
                    GroupSearchBean data2 = (GroupSearchBean) mDatas.get(position);
                    GroupViewHolder holder2 = (GroupViewHolder) viewHolder;
                    bindGroup(data2, holder2);
                    break;
                case GROUP:
                    GroupSearchBean data3 = (GroupSearchBean) mDatas.get(position);
                    GroupViewHolder holder3 = (GroupViewHolder) viewHolder;
                    bindGroup(data3, holder3);
                    break;
                case MEMBER:
                    MemberSearchBean data4 = (MemberSearchBean) mDatas.get(position);
                    UserViewHolder holder4 = (UserViewHolder) viewHolder;
                    bindMember(data4, holder4);
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    /*----分类绑定数据-----*/

    /**
     * 分类title
     *
     * @param data
     * @param holder
     */
    private void bindTitle(SearchTitleBean data, TitleViewHolder holder) {
        holder.tv_title.setText(data.getTitle());
    }

    /**
     * 组
     *
     * @param data
     * @param holder
     */
    private void bindGroup(GroupSearchBean data, GroupViewHolder holder) {

        switch (data.getSearchByType()) {
            case SearchByLabel:
                ViewUtil.showTextHighlight(holder.tvName, data.getName(), data.getMatchKeywords().toString());
                ViewUtil.showTextHighlight(holder.tv_group_no, data.getNo()+"", data.getMatchKeywords().toString());
                break;
            case SearchByNull:
                ViewUtil.showTextNormal(holder.tvName, data.getName());
                ViewUtil.showTextNormal(holder.tv_group_no, data.getNo()+"");
                break;
            default:
                break;
        }
        holder.tv_department_name.setText(data.getDepartmentName());
//        holder.tv_group_no.setText(data.getNo()+"");
//        if(TextUtils.equals(data.getResponseGroupType(),ResponseGroupType.RESPONSE_TRUE.name())){
//            holder.iv_response_group_icon.setVisibility(View.VISIBLE);
//        }else {
//            holder.iv_response_group_icon.setVisibility(View.GONE);
//        }
        //是否是响应组通过组图标来区分
        holder.iv_group_logo.setImageResource(TextUtils.equals(data.getResponseGroupType(),ResponseGroupType.RESPONSE_TRUE.name())
                ?R.drawable.response_group_photo:R.drawable.group_photo);


        if(checkIsMonitorGroup(data)){
            holder.ivMonitor.setImageResource(R.drawable.monitor_open);
        }else {
            holder.ivMonitor.setImageResource(R.drawable.monitor_close_blue);
        }

        if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == data.getNo()){
            holder.tv_change_group.setVisibility(View.GONE);
            holder.iv_current_group.setVisibility(View.VISIBLE);
        }else {
            holder.tv_change_group.setVisibility(View.VISIBLE);
            holder.iv_current_group.setVisibility(View.INVISIBLE);
        }

        holder.tv_change_group.setOnClickListener(v -> {
            if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) != data.getNo()){
                TerminalFactory.getSDK().getGroupManager().changeGroup(data.getNo());
            }
        });
        holder.ivMessage.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, GroupCallNewsActivity.class);
            intent.putExtra("isGroup", true);
            intent.putExtra("uniqueNo",data.getUniqueNo());
            intent.putExtra("userId", data.getNo());//组id
            intent.putExtra("userName", data.getName());
            intent.putExtra("speakingId",data.getId());
            intent.putExtra("speakingName",data.getName());
            mContext.startActivity(intent);
        });

        holder.iv_group_logo.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, GroupCallNewsActivity.class);
            intent.putExtra("isGroup", true);
            intent.putExtra("uniqueNo",data.getUniqueNo());
            intent.putExtra("userId", data.getNo());//组id
            intent.putExtra("userName", data.getName());
            intent.putExtra("speakingId",data.getId());
            intent.putExtra("speakingName",data.getName());
            mContext.startActivity(intent);
        });

        holder.ll_name.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, GroupCallNewsActivity.class);
            intent.putExtra("isGroup", true);
            intent.putExtra("uniqueNo",data.getUniqueNo());
            intent.putExtra("userId", data.getNo());//组id
            intent.putExtra("userName", data.getName());
            intent.putExtra("speakingId",data.getId());
            intent.putExtra("speakingName",data.getName());
            mContext.startActivity(intent);
        });




        holder.ivMonitor.setOnClickListener(v -> {
            if(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID,0) == data.getNo()){
                ToastUtils.showShort(R.string.current_group_cannot_cancel_monitor);
            }else {
                TerminalFactory.getSDK().notifyReceiveHandler(ReceiverMonitorViewClickHandler.class,data.getNo());
            }
        });
    }

    /**
     * 警员
     *
     * @param account
     * @param userViewHolder
     */
    private void bindMember(MemberSearchBean account, UserViewHolder userViewHolder) {
        if(account==null){
            return;
        }
        if(!TextUtils.isEmpty(account.getName())){
            ViewUtil.showTextHighlight(userViewHolder.tvName, account.getName(), account.getMatchKeywords().toString());
            //userViewHolder.tvName.setText(account.getName() + "");
        }
        //userViewHolder.tvId.setText(account.getNo() + "");

        String no = HandleIdUtil.handleId(account.getNo());//去掉88 86
        ViewUtil.showTextHighlight(userViewHolder.tvId, no + "", account.getMatchKeywords().toString());

        if(!TextUtils.isEmpty(account.getPhone())){
            userViewHolder.tv_phone.setVisibility(View.VISIBLE);
            ViewUtil.showTextHighlight(userViewHolder.tv_phone, account.getPhone() + "", account.getMatchKeywords().toString());
        }else{
            userViewHolder.tv_phone.setVisibility(View.GONE);
        }

        userViewHolder.shoutai_tv_department.setText(account.getDepartmentName());

        userViewHolder.llDialTo.setOnClickListener(view -> {
            callPhone(account);
        });
        userViewHolder.llMessageTo.setOnClickListener(view -> IndividualNewsActivity.startCurrentActivity(mContext, account.getNo(), account.getName(),0));
        userViewHolder.llCallTo.setOnClickListener(view -> {
            indivudualCall(account);
        });
        //请求图像
        userViewHolder.llLiveTo.setOnClickListener(view -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
            }else{
                pullStream(account);
            }
        });
        //个人信息页面
        userViewHolder.ivLogo.setImageResource(BitmapUtil.getUserPhoto());
        userViewHolder.ivLogo.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userId", account.getNo());
            intent.putExtra("userName", account.getName());
            mContext.startActivity(intent);
        });
        //如果是自己的不显示业务按钮
        if(account.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
            userViewHolder.llDialTo.setVisibility(View.GONE);
//                userViewHolder.llMessageTo.setVisibility(View.GONE);
//                userViewHolder.llCallTo.setVisibility(View.GONE);
        }else{
            userViewHolder.llDialTo.setVisibility(View.GONE);
//            userViewHolder.llDialTo.setVisibility(View.VISIBLE);
//                userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
//                userViewHolder.llCallTo.setVisibility(View.VISIBLE);
        }
        //电话按钮，是否显示
//            if(contractType == Constants.TYPE_CONTRACT_MEMBER && account.getNo() != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
//                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
//                userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
//            }else{
//                //电台不显示电话
//                userViewHolder.llDialTo.setVisibility(View.GONE);
//                userViewHolder.llLiveTo.setVisibility(View.GONE);
//            }
    }

    private String getPhone(MemberSearchBean account){
        String phone = account.getPhone();
        String phoneNumber = account.getPhoneNumber();

        if(!TextUtils.isEmpty(phone)){
            return phone;
        }
        if(!TextUtils.isEmpty(phoneNumber)){
            return phoneNumber;
        }

        for (Member member : account.getMembers()){
            String phone1 = member.getPhone();
            if(TextUtils.isEmpty(phone1)){
                return phone1;
            }
        }

        return "";
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    /*------------------------------------*/
    private void callPhone(Account account){
        if(TextUtils.isEmpty(account.getPhone())){
            ToastUtils.showShort(R.string.text_has_no_member_phone_number);
            return;
        }
        new ChooseDevicesDialog(mContext, ChooseDevicesDialog.TYPE_CALL_PHONE, account, (dialog, member) -> {
            if(member.getUniqueNo() == 0){
                //普通电话
                CallPhoneUtil.callPhone((Activity) mContext, account.getPhone());
            }else{
                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS, false)){
                    Intent intent = new Intent(mContext, VoipPhoneActivity.class);
                    intent.putExtra("member", member);
                    mContext.startActivity(intent);
                }else{
                    ToastUtil.showToast(mContext, mContext.getString(R.string.text_voip_regist_fail_please_check_server_configure));
                }
            }
            dialog.dismiss();
        }).showDialog();
    }

    private void indivudualCall(Account account){
        if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
        }else{
            new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_CALL_PRIVATE, account, (dialog, member) -> {
                activeIndividualCall(member);
                dialog.dismiss();
            }).showDialog();
        }
    }
    private void activeIndividualCall(Member member){
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if(network){
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
        }else{
            ToastUtil.showToast(mContext, mContext.getString(R.string.text_network_connection_abnormal_please_check_the_network));
        }
    }

    private void pullStream(Account account){
        new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_PULL_LIVE, account, (dialog, member) -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
            dialog.dismiss();
        }).showDialog();
    }


    /*--------ViewHolder----------*/

    //分类 标题
    class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;

        public TitleViewHolder(View itemView) {
            super(itemView);
            tv_title = itemView.findViewById(R.id.tv_title);
        }
    }


    class UserViewHolder extends RecyclerView.ViewHolder {

        ImageView ivLogo;
        ImageView rBindedLogo;

        TextView tvName;

        TextView tvId;

        TextView shoutai_tv_department;
        TextView tv_phone;

        LinearLayout llDialTo;

        LinearLayout llMessageTo;

        LinearLayout llCallTo;

        LinearLayout llLiveTo;

        public UserViewHolder(View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.shoutai_user_logo);
            rBindedLogo = itemView.findViewById(R.id.recorder_binded_logo);
            tvName = itemView.findViewById(R.id.shoutai_tv_member_name);
            tvId = itemView.findViewById(R.id.shoutai_tv_member_id);
            shoutai_tv_department = itemView.findViewById(R.id.shoutai_tv_department);
            tv_phone = itemView.findViewById(R.id.tv_phone);
            llDialTo = itemView.findViewById(R.id.shoutai_dial_to);
            llMessageTo = itemView.findViewById(R.id.shoutai_message_to);
            llCallTo = itemView.findViewById(R.id.shoutai_call_to);
            llLiveTo = itemView.findViewById(R.id.shoutai_live_to);

        }
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tv_department_name;
        TextView tv_group_no;

        ImageView ivMonitor;

        ImageView ivMessage;

        View placeholder;

        ImageView iv_group_logo;

        ImageView iv_response_group_icon;

        TextView tv_change_group;

        ImageView iv_current_group;

        LinearLayout ll_name;

        GroupViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tv_department_name = itemView.findViewById(R.id.tv_department_name);
            tv_group_no = itemView.findViewById(R.id.tv_group_no);
            ivMonitor = itemView.findViewById(R.id.iv_monitor);
            ivMessage = itemView.findViewById(R.id.iv_message);
            placeholder = itemView.findViewById(R.id.placeholder);
            iv_group_logo = itemView.findViewById(R.id.iv_group_logo);
            iv_response_group_icon = itemView.findViewById(R.id.iv_response_group_icon);
            tv_change_group = itemView.findViewById(R.id.tv_change_group);
            iv_current_group = itemView.findViewById(R.id.iv_current_group);

            ll_name = itemView.findViewById(R.id.ll_name);
        }
    }

    /**
     * 判断是否监听
     *
     * @param group
     * @return
     */
    private boolean checkIsMonitorGroup(Group group) {
        if (ResponseGroupType.RESPONSE_TRUE.toString().equals(group.getResponseGroupType())) {
            return true;
        }
        if (TerminalFactory.getSDK().getConfigManager().getMonitorGroupNo().contains(group.getNo())) {
            return true;
        }
        if (TerminalFactory.getSDK().getConfigManager().getTempMonitorGroupNos().contains(group.getNo())) {
            return true;
        }
        if (TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0) == group.getNo()) {
            return true;
        }
        return false;
    }
}
