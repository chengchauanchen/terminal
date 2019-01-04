package cn.vsx.vc.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.activity.IndividualNewsActivity;
import cn.vsx.vc.activity.UserInfoActivity;
import cn.vsx.vc.activity.VoipPhoneActivity;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.CallPhoneUtil;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.utils.HandleIdUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class GroupMemberAdapter extends BaseAdapter {
    private List<Member> currentGroupMembers = new ArrayList<>();
    private List<Member> deleteMembers = new ArrayList<>();
    private Context mContext;
    private boolean isDelete;
    private String phoneNum;
    private String no;
    private int VOIP=0;
    private int TELEPHONE=1;
    private OnItemClickListener onItemClickListener;

    public GroupMemberAdapter(Context mContext, List<Member> currentGroupMembers, boolean isDelete) {
        this.mContext = mContext;
        this.currentGroupMembers = currentGroupMembers;
        this.isDelete = isDelete;
    }

    public int getCount() {
      return currentGroupMembers.size();

    }

    public Object getItem(int position) {
        return currentGroupMembers.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public List<Member> getDeleteMemberList() {

        return deleteMembers;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        final ViewHolder viewHolder ;
        final Member member = currentGroupMembers.get(position);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_person_item, null);
            viewHolder.ll_person_search_item=(LinearLayout)view.findViewById(R.id.ll_person_search_item);
            viewHolder.userName = (TextView) view.findViewById(R.id.tv_member_name);
            viewHolder.me = (TextView) view.findViewById(R.id.me);
            viewHolder.userId = (TextView) view.findViewById(R.id.tv_member_id);
            viewHolder.userLogo = (ImageView) view.findViewById(R.id.user_logo);
            viewHolder.messageTo = (LinearLayout) view.findViewById(R.id.message_to);
            viewHolder.callTo = (LinearLayout) view.findViewById(R.id.call_to);
            viewHolder.tvLetter = (TextView) view.findViewById(R.id.tv_catagory);
            viewHolder.catagory = (LinearLayout) view.findViewById(R.id.catagory);
            viewHolder.line =  view.findViewById(R.id.lay_line);
            viewHolder.dialTo= (LinearLayout) view.findViewById(R.id.shoutai_dial_to);
            viewHolder.cbSelectmember=(CheckBox)view.findViewById(R.id.cb_selectmember);
            viewHolder.select_delete=(LinearLayout)view.findViewById(R.id.select_delete);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.catagory.setVisibility(View.GONE);

        no = HandleIdUtil.handleId(member.no);
        viewHolder.userName.setText(member.getName());
        phoneNum = DataUtil.getMemberByMemberNo(currentGroupMembers.get(position).no).phone;
        Log.i("sjl_", "getView: "+phoneNum);
//        viewHolder.userId.setText(member.id+"");
        if (TextUtils.isEmpty(no)){
            viewHolder.userId.setText(phoneNum);
        }else {
            viewHolder.userId.setText(no);
        }

        if(isDelete){
            viewHolder.messageTo.setVisibility(View.GONE);
            viewHolder.callTo.setVisibility(View.GONE);
            viewHolder.dialTo.setVisibility(View.GONE);
            if (member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                viewHolder.select_delete.setVisibility(View.GONE);
            }else {
                viewHolder.select_delete.setVisibility(View.VISIBLE);
            }

        }else {
            viewHolder.select_delete.setVisibility(View.GONE);
            if (member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                viewHolder.messageTo.setVisibility(View.GONE);
                viewHolder.callTo.setVisibility(View.GONE);
                viewHolder.dialTo.setVisibility(View.GONE);
            }else {
                viewHolder.messageTo.setVisibility(View.VISIBLE);
                viewHolder.callTo.setVisibility(View.VISIBLE);
                viewHolder.dialTo.setVisibility(View.VISIBLE);
            }

        }

        if(position == getCount()-1){
            viewHolder.line.setVisibility(View.GONE);
        }else {
            viewHolder.line.setVisibility(View.VISIBLE);
        }

        if(member.isChecked()){
            viewHolder.cbSelectmember.setChecked(true);
        }else {
            viewHolder.cbSelectmember.setChecked(false);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDelete){
                    if (member.getNo() == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0)){
                        return;
                    }
                    if(member.isChecked()){
                        member.isChecked = false;
                        deleteMembers.remove(member);
                    }else {
                        member.isChecked = true;
                        deleteMembers.add(member);
                    }
                    if(onItemClickListener!=null){
                        onItemClickListener.onItemClick(v,position,member.isChecked,member);
                    }
                    notifyDataSetChanged();
                }
            }
        });

        //跳转到消息界面
        viewHolder.messageTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IndividualNewsActivity.startCurrentActivity(mContext, member.id, member.getName() );
            }
        });
        //跳转到个呼
        viewHolder.callTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    activeIndividualCall(position);
            }else {
                    ToastUtil.showToast(mContext,"没有个呼功能权限");
                }

            }
        });
        viewHolder.userLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("userId", member.getNo());
                intent.putExtra("userName", member.getName());
                mContext.startActivity(intent);
            }
        });
        viewHolder.dialTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(member.phone)) {

                    ItemAdapter adapter = new ItemAdapter(mContext,ItemAdapter.iniDatas());
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    //设置标题
                    builder.setTitle("拨打电话");
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            if(position==VOIP){//voip电话
                                if(MyTerminalFactory.getSDK().getParam(Params.VOIP_SUCCESS,false)){
                                    Intent intent = new Intent(mContext, VoipPhoneActivity.class);
                                    intent.putExtra("member",member);
                                    mContext.startActivity(intent);
                                }else {
                                    ToastUtil.showToast(mContext,"voip注册失败，请检查服务器配置");
                                }
                            }
                            else if(position==TELEPHONE){//普通电话

                                CallPhoneUtil.callPhone((Activity) mContext, member.phone);

                            }

                        }
                    });
                    builder.create();
                    builder.show();
                }else {
                    ToastUtil.showToast(mContext,"暂无该用户电话号码");
                }
            }
        });
        return view;

    }


    final static class ViewHolder {
        LinearLayout ll_person_search_item;
        TextView tvLetter;
        TextView userName;
        TextView me;
        TextView userId;
        ImageView userLogo;
        LinearLayout messageTo;
        LinearLayout callTo;
        View line;
        LinearLayout catagory;
        LinearLayout dialTo;
        CheckBox cbSelectmember;
        LinearLayout select_delete;
    }

    /**
     * 请求个呼
     * @param position
     */
    private void activeIndividualCall(int position) {
        MyApplication.instance.isCallState = true;
        boolean network = MyTerminalFactory.getSDK().hasNetwork();
        if (network){
            if ( currentGroupMembers.size() > 0) {
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, currentGroupMembers.get(position));
            }
        } else {
            ToastUtil.showToast(mContext, "网络连接异常，请检查网络！");
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener{
        void onItemClick(View view,int position ,boolean checked,Member member);
    }
}