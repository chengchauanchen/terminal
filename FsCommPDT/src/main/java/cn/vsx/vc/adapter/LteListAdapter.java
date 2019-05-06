package cn.vsx.vc.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.model.ContactItemBean;
import cn.vsx.vc.receiveHandle.ReceiveGoWatchRTSPHandler;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

import static cn.vsx.vc.utils.Constants.TYPE_DEPARTMENT;
import static cn.vsx.vc.utils.Constants.TYPE_USER;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/3/5
 * 描述：
 * 修订历史：
 */
public class LteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context mContext;
    private List<ContactItemBean> mDatas;
    private static int VOIP=0;
    private static int TELEPHONE=1;

    private ItemClickListener listener;
    public LteListAdapter(Context context, List<ContactItemBean> datas) {
        this.mContext=context;
        this.mDatas=datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==TYPE_DEPARTMENT){
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_department,parent,false);
            return new DepartmentViewHolder(view);
        }else {
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_lte,parent,false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (getItemViewType(position)==TYPE_DEPARTMENT){
            DepartmentViewHolder holder1= (DepartmentViewHolder) holder;
            holder1.tvDepartment.setText(mDatas.get(position).getName());
            holder1.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null) {
                        listener.onItemClick(view, position, TYPE_DEPARTMENT);
                    }
                }
            });
        }else {
            UserViewHolder userViewHolder= (UserViewHolder) holder;
            final Member member= (Member) mDatas.get(position).getBean();
            userViewHolder.tvName.setText(member.getName()+"");
            userViewHolder.tvId.setText(member.getNo()+"");

            userViewHolder.ll_lte_live.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: 2019/3/5 拉流

                    String gb28181No = member.getGb28181No();
                    String gateWayUrl = TerminalFactory.getSDK().getParam(Params.GATE_WAY_URL);
                    String gb28181RtspUrl = gateWayUrl+"DevAor="+gb28181No;
                    TerminalMessage terminalMessage = new TerminalMessage();
                    terminalMessage.messageBody = new JSONObject();
                    terminalMessage.messageBody.put(JsonParam.GB28181_RTSP_URL,gb28181RtspUrl);
                    terminalMessage.messageBody.put(JsonParam.DEVICE_NAME,member.getName());
                    terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_NAME,member.getDepartmentName());
                    terminalMessage.messageBody.put(JsonParam.DEVICE_DEPT_ID,member.getDeptId());
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGoWatchRTSPHandler.class,terminalMessage);
                }
            });
            userViewHolder.llMessageTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IndividualNewsActivity.startCurrentActivity(mContext, member.no, member.getName());

                }
            });

            userViewHolder.llCallTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        ToastUtil.showToast(mContext,"没有个呼功能权限");
                    }else {
                        activeIndividualCall(member);
                    }


                }
            });

            userViewHolder.ivLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra("userId", member.getNo());
                    intent.putExtra("userName", member.getName());
                    mContext.startActivity(intent);
                }
            });

            if(member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                userViewHolder.ll_lte_live.setVisibility(View.GONE);
                userViewHolder.llMessageTo.setVisibility(View.GONE);
                userViewHolder.llCallTo.setVisibility(View.GONE);
            }else {
                userViewHolder.ll_lte_live.setVisibility(View.VISIBLE);
                userViewHolder.llMessageTo.setVisibility(View.VISIBLE);
                userViewHolder.llCallTo.setVisibility(View.VISIBLE);

            }
        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        ContactItemBean bean=mDatas.get(position);
        if (bean.getType()==TYPE_USER){
            return TYPE_USER;
        }else {
            return TYPE_DEPARTMENT;
        }
    }

    private void activeIndividualCall(Member member){
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network) {
            int resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(member.getNo(),member.getUniqueNo(),"");
            if (resultCode == BaseCommonCode.SUCCESS_CODE) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
            } else {
                ToastUtil.individualCallFailToast(mContext, resultCode);
            }

        } else {
            ToastUtil.showToast(mContext, "网络连接异常，请检查网络！");
        }
    }



    class DepartmentViewHolder extends RecyclerView.ViewHolder{
        TextView tvDepartment;

        public DepartmentViewHolder(View itemView) {
            super(itemView);
            tvDepartment= (TextView) itemView.findViewById(R.id.tv_department);
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.shoutai_user_logo)
        ImageView ivLogo;
        @Bind(R.id.shoutai_tv_member_name)
        TextView tvName;
        @Bind(R.id.shoutai_tv_member_id)
        TextView tvId;
        @Bind(R.id.ll_lte_live)
        LinearLayout ll_lte_live;
        @Bind(R.id.shoutai_message_to)
        LinearLayout llMessageTo;
        @Bind(R.id.shoutai_call_to)
        LinearLayout llCallTo;

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

    public void setOnItemClickListener(ItemClickListener listener){
        this.listener=listener;
    }
    public interface ItemClickListener{
        void onItemClick(View view, int postion, int type);
    }

}
