package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
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

import java.util.ArrayList;
import java.util.List;

import cn.com.cybertech.pdk.utils.DisplayUtil;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Department;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.model.CatalogBean;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiverRequestVideoHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowPersonFragmentHandler;
import cn.vsx.vc.utils.BitmapUtil;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

import static cn.vsx.vc.utils.Constants.TYPE_DEPARTMENT;

/**
 * Created by XX on 2018/4/11.
 */

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<ContactItemBean> mDatas;
    private long lastSearchTime;
    private List<CatalogBean> catalogNames;
    private ItemClickListener listener;
    private CatalogItemClickListener catalogItemClickListener;
    private int contractType;
    private int recorderLiveRightMargin = 0;
    private ArrayList<String> terminalMemberTypes;
    public ContactAdapter(Context context, List<ContactItemBean> datas, List<CatalogBean> catalogNames, int contractType, ArrayList<String> terminalMemberTypes){
        this.mContext = context;
        this.mDatas = datas;
        this.catalogNames = catalogNames;
        this.contractType = contractType;
        this.terminalMemberTypes = terminalMemberTypes;
        recorderLiveRightMargin = DisplayUtil.dip2px(context,12);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        if(viewType == Constants.TYPE_FREQUENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.frequent_layout, parent, false);
            return new FrequentViewHolder(view);
        }
        if(viewType == Constants.TYPE_TITLE){
            View view = LayoutInflater.from(mContext).inflate(R.layout.group_adapter_parent_layout, parent, false);
            return new TitleViewHolder(view);
        }
        if(viewType == Constants.TYPE_DEPARTMENT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_department, parent, false);
            return new DepartmentViewHolder(view);
        }else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        if(getItemViewType(position) == Constants.TYPE_TITLE){
            TitleViewHolder titleViewHolder = (TitleViewHolder)holder;
            titleViewHolder.iv_search.setVisibility(View.VISIBLE);
            titleViewHolder.parent_recyclerview.setLayoutManager(new LinearLayoutManager(mContext, OrientationHelper.HORIZONTAL,false));

            GroupCatalogAdapter mCatalogAdapter=new GroupCatalogAdapter(mContext,catalogNames);
            titleViewHolder.parent_recyclerview.setAdapter(mCatalogAdapter);
            mCatalogAdapter.setOnItemClick((view, position12) -> {
                if(catalogItemClickListener !=null){
                    catalogItemClickListener.onCatalogItemClick(view, position12);
                }
            });

            titleViewHolder.iv_search.setOnClickListener(v -> {
                if(System.currentTimeMillis() - lastSearchTime<1000){
                    return;
                }
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowPersonFragmentHandler.class, contractType,terminalMemberTypes);
                lastSearchTime = System.currentTimeMillis();
            });
        }
        else if(getItemViewType(position) == Constants.TYPE_DEPARTMENT){
            DepartmentViewHolder holder1 = (DepartmentViewHolder) holder;
            Department department = (Department) mDatas.get(position).getBean();
            holder1.tvDepartment.setText(department.getName());
            holder1.itemView.setOnClickListener(view -> {
                if(listener != null){
                    listener.onItemClick(view,department.getId(),department.getName(),TYPE_DEPARTMENT);
                }
            });
        }else if(getItemViewType(position) == Constants.TYPE_ACCOUNT){
            // TODO: 2019/4/15
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            final Account account = (Account) mDatas.get(position).getBean();
            if(account==null){
                return;
            }
            if(!TextUtils.isEmpty(account.getName())){
                userViewHolder.tvName.setText(account.getName() + "");
            }
            userViewHolder.tvId.setText(account.getNo() + "");
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
                userViewHolder.llDialTo.setVisibility(View.VISIBLE);
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
        }else if(getItemViewType(position) == Constants.TYPE_USER){
            setUserData((UserViewHolder) holder, position);
        }else if(getItemViewType(position) == Constants.TYPE_LTE){
            //LTE
            setLTEData((UserViewHolder) holder, position);
        }else if(getItemViewType(position) == Constants.TYPE_RECORDER){
            //执法记录仪
            setRecoderData((UserViewHolder) holder, position);
        }else if(getItemViewType(position) == Constants.TYPE_TERMINAL){
            try{
                UserViewHolder userViewHolder = (UserViewHolder) holder;
                final Member member = (Member) mDatas.get(position).getBean();
                String terminalMemberType = member.getTerminalMemberType();
                TerminalMemberType memberType = TerminalMemberType.valueOf(terminalMemberType);
                switch(memberType){
                    case TERMINAL_BODY_WORN_CAMERA:
                    case TERMINAL_HDMI:
                        setRecoderData((UserViewHolder) holder, position);
                        break;
                    case TERMINAL_LTE:
                    case TERMINAL_LTE_HYTERA:
                    case TERMINAL_UAV:
                        setLTEData((UserViewHolder) holder, position);
                        break;
                    case TERMINAL_PDT:
                    case TERMINAL_PDT_WANGE:
                    case TERMINAL_PDT_HAIGE:
                        setUserData((UserViewHolder) holder, position);
                        break;
                    default:
                        setUserData((UserViewHolder) holder, position);
                        break;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void setRecoderData(UserViewHolder holder, int position){
        UserViewHolder userViewHolder = holder;
        final Member member = (Member) mDatas.get(position).getBean();
        if (member.isBind()) {
            if(!TextUtils.isEmpty(member.getName())){
                userViewHolder.tvName.setText(member.getName() + "");
            }
            userViewHolder.tvId.setText(member.getNo() + "");
            userViewHolder.tvId.setVisibility(View.VISIBLE);
        }else{
            userViewHolder.tvName.setText(member.getNo() + "");
            userViewHolder.tvId.setVisibility(View.GONE);
        }
        userViewHolder.ivLogo.setImageResource(R.drawable.icon_recorder);
        userViewHolder.rBindedLogo.setVisibility(member.isBind()?View.VISIBLE:View.GONE);
        userViewHolder.llDialTo.setVisibility(View.GONE);
        userViewHolder.llMessageTo.setVisibility(View.GONE);
        userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
        userViewHolder.llCallTo.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) userViewHolder.llLiveTo.getLayoutParams();
        lp.rightMargin = recorderLiveRightMargin;
        userViewHolder.llLiveTo.setLayoutParams(lp);
        //请求图像
        userViewHolder.llLiveTo.setOnClickListener(view -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
            }else{
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
            }
        });
        //个人信息页面
        userViewHolder.ivLogo.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userId", member.getNo());
            intent.putExtra("userName", member.getName());
            mContext.startActivity(intent);
        });
    }

    private void setLTEData(UserViewHolder holder, int position){
        UserViewHolder userViewHolder = holder;
        final Member member = (Member) mDatas.get(position).getBean();
        if(!TextUtils.isEmpty(member.getName())){
            userViewHolder.tvName.setText(member.getName() + "");
        }
        userViewHolder.tvId.setText(member.getNo() + "");
        userViewHolder.llDialTo.setVisibility(View.GONE);
        userViewHolder.llMessageTo.setOnClickListener(view -> IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName(),member.getType()));
        userViewHolder.llCallTo.setOnClickListener(view -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
            }else{
                activeIndividualCall(member);
            }
        });
        //请求图像
        userViewHolder.llLiveTo.setOnClickListener(view -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
            }else{
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
            }
        });
        //个人信息页面
        userViewHolder.ivLogo.setImageResource(BitmapUtil.getUserPhoto());
        userViewHolder.ivLogo.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userId", member.getNo());
            intent.putExtra("userName", member.getName());
            mContext.startActivity(intent);
        });
        //如果是自己的不显示业务按钮
        if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
            userViewHolder.llMessageTo.setVisibility(View.GONE);
            userViewHolder.llCallTo.setVisibility(View.GONE);
            userViewHolder.llLiveTo.setVisibility(View.GONE);
        }else{
            userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
            userViewHolder.llCallTo.setVisibility(View.VISIBLE);
            userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
        }
    }

    private void setUserData(UserViewHolder holder, int position){
        UserViewHolder userViewHolder = holder;
        final Member member = (Member) mDatas.get(position).getBean();
        if(!TextUtils.isEmpty(member.getName())){
            userViewHolder.tvName.setText(member.getName() + "");
        }
        userViewHolder.tvId.setText(member.getNo() + "");
        userViewHolder.llDialTo.setOnClickListener(view -> {
            Account account = DataUtil.getAccountByMember(member);
            callPhone(account);
        });
        userViewHolder.llMessageTo.setOnClickListener(view -> IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName(),member.getType()));
        userViewHolder.llCallTo.setOnClickListener(view -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                ToastUtil.showToast(mContext, mContext.getString(R.string.text_no_call_permission));
            }else{
                activeIndividualCall(member);
            }
        });
        //请求图像
        userViewHolder.llLiveTo.setOnClickListener(view -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                ToastUtil.showToast(mContext, mContext.getString(R.string.text_has_no_image_request_authority));
            }else{
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
            }
        });
        //个人信息页面
        userViewHolder.ivLogo.setImageResource(BitmapUtil.getUserPhoto());
        userViewHolder.ivLogo.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userId", member.getNo());
            intent.putExtra("userName", member.getName());
            mContext.startActivity(intent);
        });
        //如果是自己的不显示业务按钮
        if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
            userViewHolder.llDialTo.setVisibility(View.GONE);
            userViewHolder.llMessageTo.setVisibility(View.GONE);
            userViewHolder.llCallTo.setVisibility(View.GONE);
        }else{
            userViewHolder.llDialTo.setVisibility(View.VISIBLE);
            userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
            userViewHolder.llCallTo.setVisibility(View.VISIBLE);
        }
        //电话按钮，是否显示
        if(contractType == Constants.TYPE_CONTRACT_MEMBER && member.getNo() != MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
            userViewHolder.llDialTo.setVisibility(View.VISIBLE);
            userViewHolder.llLiveTo.setVisibility(View.VISIBLE);
        }else{
            //电台不显示电话
            userViewHolder.llDialTo.setVisibility(View.GONE);
            userViewHolder.llLiveTo.setVisibility(View.GONE);
        }
    }

    private void pullStream(Account account){
        new ChooseDevicesDialog(mContext,ChooseDevicesDialog.TYPE_PULL_LIVE, account, (dialog, member) -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverRequestVideoHandler.class, member);
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

    @Override
    public int getItemCount(){
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position){
        ContactItemBean bean = mDatas.get(position);
        if(bean.getType() == Constants.TYPE_FREQUENT){
            return Constants.TYPE_FREQUENT;
        }else if(bean.getType() == Constants.TYPE_TITLE){
            return Constants.TYPE_TITLE;
        }else if(bean.getType() == Constants.TYPE_ACCOUNT){
            return Constants.TYPE_ACCOUNT;
        }else if(bean.getType() == Constants.TYPE_USER){
            return Constants.TYPE_USER;
        }else if(bean.getType() == Constants.TYPE_DEPARTMENT){
            return Constants.TYPE_DEPARTMENT;
        }else if(bean.getType() == Constants.TYPE_LTE){
            return Constants.TYPE_LTE;
        }else if(bean.getType() == Constants.TYPE_RECORDER){
            return Constants.TYPE_RECORDER;
        }else if(bean.getType() == Constants.TYPE_TERMINAL){
            return Constants.TYPE_TERMINAL;
        }
        else{
            return -1;
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

    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView){
            super(itemView);
            tvDepartment = itemView.findViewById(R.id.tv_department);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ImageView ivLogo;
        ImageView rBindedLogo;

        TextView tvName;

        TextView tvId;

        LinearLayout llDialTo;

        LinearLayout llMessageTo;

        LinearLayout llCallTo;

        LinearLayout llLiveTo;

        public UserViewHolder(View itemView){
            super(itemView);
            ivLogo = itemView.findViewById(R.id.shoutai_user_logo);
            rBindedLogo = itemView.findViewById(R.id.recorder_binded_logo);
            tvName = itemView.findViewById(R.id.shoutai_tv_member_name);
            tvId = itemView.findViewById(R.id.shoutai_tv_member_id);
            llDialTo = itemView.findViewById(R.id.shoutai_dial_to);
            llMessageTo = itemView.findViewById(R.id.shoutai_message_to);
            llCallTo = itemView.findViewById(R.id.shoutai_call_to);
            llLiveTo = itemView.findViewById(R.id.shoutai_live_to);

        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder{

        RecyclerView parent_recyclerview;

        ImageView iv_search;

        TitleViewHolder(View rootView){
            super(rootView);
            parent_recyclerview = itemView.findViewById(R.id.parent_recyclerview);
            iv_search = itemView.findViewById(R.id.iv_search);
        }
    }

    class FrequentViewHolder extends RecyclerView.ViewHolder{

        FrequentViewHolder(View rootView){
            super(rootView);
        }
    }


    public void setOnItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }

    public interface ItemClickListener{
        void onItemClick(View view, int depId, String depName, int type);
    }

    public void setCatalogItemClickListener(CatalogItemClickListener catalogItemClickListener){
        this.catalogItemClickListener = catalogItemClickListener;
    }

    public interface CatalogItemClickListener{
        void onCatalogItemClick(View view, int position);
    }
}
