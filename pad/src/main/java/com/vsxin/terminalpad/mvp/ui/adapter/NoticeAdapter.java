package com.vsxin.terminalpad.mvp.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.LLSInterface;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.adapter.BaseRecycleViewAdapter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInCallEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeInOrOutEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeOutCallEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeOutLiveEnum;
import com.vsxin.terminalpad.mvp.contract.constant.NoticeTypeEnum;
import com.vsxin.terminalpad.mvp.contract.presenter.NoticePresenter;
import com.vsxin.terminalpad.mvp.entity.NoticeBean;

import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 * <p>
 * 通知模块
 * <p>
 * 通用属性：开始时间，接通时长
 * <p>
 * 1.个呼(a.打进来，b.拨出去)
 * a.打进来(等待接听(接听/拒绝),正在通话中(通话时长),通话结束(自己\对方 挂断))
 * b.拨出去(等待接听(取消,对方未接听(超时、挂断)),正在通话中,通话结束(自己\对方 挂断))
 * <p>
 * <p>
 * 2.直播(a.主动直播,b.主动邀请他人直播,c.他人请求我直播,d.他人直播邀请我观看)
 * a.主动直播????
 * 我正在直播？？
 * b.主动邀请他人直播：正在邀请中，对方拒绝，对方同意，正在观看，结束观看(主动退出、对方下播)
 * c.他人请求我直播：等待直播(接收/拒绝),正在直播,结束直播
 * d.他人直播邀请我观看:收到邀请(点击观看--直播正在进行中,直播已结束),
 */
public class NoticeAdapter extends BaseRecycleViewAdapter<NoticeBean, RecyclerView.ViewHolder> {

    private static int CALL = 1;//个呼通知
    private static int LIVE = 2;//上报通知
    private static int WATCH = 3;//被邀请观看

    private NoticePresenter noticePresenter;

    public NoticeAdapter(Context mContext, NoticePresenter noticePresenter) {
        super(mContext);
        this.noticePresenter = noticePresenter;
    }

    @Override
    public int getItemViewType(int position) {
        NoticeBean noticeBean = getDatas().get(position);
        return noticeBean.getNoticeType().getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView;
        if (NoticeTypeEnum.CALL.getType() == viewType) {//个呼
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_notice_call_item, parent, false);
            return new CallViewHolder(convertView);
        } else {//直播
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_notice_live_item, parent, false);
            return new LiveViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoticeBean noticeBean = getDatas().get(position);
        if (holder instanceof CallViewHolder) {
            CallViewHolder callViewHolder = (CallViewHolder) holder;
            bindCallViewHolder(callViewHolder, noticeBean, position);
        } else if (holder instanceof LiveViewHolder) {
            LiveViewHolder liveViewHolder = (LiveViewHolder) holder;
            bindLiveViewHolder(liveViewHolder, noticeBean, position);
        }
    }


    private void bindCallViewHolder(CallViewHolder callViewHolder, NoticeBean noticeBean, int position) {

        callViewHolder.iv_phone.setImageResource(R.mipmap.call_connected);
        callViewHolder.tv_name.setText(noticeBean.getMemberName());
        callViewHolder.tv_member_no.setText(noticeBean.getMemberId() + "");
        callViewHolder.tv_time.setText("");

        OnClickListener callOnClickListener = null;

        if (noticeBean.getInOrOut() == NoticeInOrOutEnum.IN) {//被动接收 个呼
            //callViewHolder.ll_call_answer_hang
            //callViewHolder.tv_answer_hang
            if (noticeBean.getInCall() == NoticeInCallEnum.CALL_IN_WAIT) {//等待接听
                callViewHolder.tv_answer_hang.setText("接听");
                callOnClickListener = v -> {
                    noticePresenter.startIndividualCall();

                    //todo 接听他人个呼，成功与否好像没有回调哦
                    noticeBean.setInCall(NoticeInCallEnum.CALL_IN_CONNECT);
                    notifyDataSetChanged();
                };
            } else if (noticeBean.getInCall() == NoticeInCallEnum.CALL_IN_CONNECT) {//正在通话中
                callViewHolder.tv_answer_hang.setText("挂断");
                callOnClickListener = v -> {
                    noticePresenter.stopIndividualCall();
                    //todo 通话过程中挂断他人个呼，成功与否好像没有回调哦
                    noticeBean.setInCall(NoticeInCallEnum.CALL_IN_END);
                    notifyDataSetChanged();
                };
            } else if (noticeBean.getInCall() == NoticeInCallEnum.CALL_IN_END) {//通话结束
                callViewHolder.tv_answer_hang.setText("结束");
            } else if (noticeBean.getInCall() == NoticeInCallEnum.CALL_IN_TIME_OUT) {//超时未接听
                callViewHolder.tv_answer_hang.setText("超时");
                callViewHolder.iv_phone.setImageResource(NoticeInCallEnum.CALL_IN_TIME_OUT.getResId());
            } else if (noticeBean.getInCall() == NoticeInCallEnum.CALL_IN_REFUSE) {//拒接接听
                callViewHolder.tv_answer_hang.setText("拒接");
                callViewHolder.iv_phone.setImageResource(NoticeInCallEnum.CALL_IN_REFUSE.getResId());
            }
        } else {//主动发起 个呼
            if (noticeBean.getOutCall() == NoticeOutCallEnum.CALL_OUT_WAIT) {//等待接听
                callViewHolder.tv_answer_hang.setText("挂断");
                callOnClickListener = v -> {
                    noticePresenter.stopIndividualCall();

                    //todo 挂断自己发起的个呼，成功与否好像没有回调哦
                    noticeBean.setOutCall(NoticeOutCallEnum.CALL_OUT_END);//通话结束
                    notifyDataSetChanged();
                };
            } else if (noticeBean.getOutCall() == NoticeOutCallEnum.CALL_OUT_CONNECT) {//正在通话中
                callViewHolder.tv_answer_hang.setText("挂断");
                callViewHolder.tv_time.setText("通话中");
                callOnClickListener = v -> {
                    noticePresenter.stopIndividualCall();
                    //todo 挂断自己发起的个呼，成功与否好像没有回调哦
                    noticeBean.setOutCall(NoticeOutCallEnum.CALL_OUT_END);//通话结束
                    notifyDataSetChanged();
                };
            } else if (noticeBean.getOutCall() == NoticeOutCallEnum.CALL_OUT_END) {//通话结束
                callViewHolder.tv_answer_hang.setText("结束");
                callViewHolder.tv_time.setText("通话结束");
            } else if (noticeBean.getOutCall() == NoticeOutCallEnum.CALL_OUT_TIME_OUT) {//超时未接听
                callViewHolder.tv_answer_hang.setText("超时");
                callViewHolder.tv_time.setText("超时");
                callViewHolder.iv_phone.setImageResource(NoticeOutCallEnum.CALL_OUT_TIME_OUT.getResId());
            } else if (noticeBean.getOutCall() == NoticeOutCallEnum.CALL_OUT_REFUSE) {//拒接接听
                callViewHolder.tv_answer_hang.setText("拒接");
                callViewHolder.tv_time.setText("拒接");
                callViewHolder.iv_phone.setImageResource(NoticeOutCallEnum.CALL_OUT_REFUSE.getResId());
            }
        }

        callViewHolder.tv_name.setText(noticeBean.getMemberName());
        callViewHolder.ll_call_answer_hang.setOnClickListener(callOnClickListener);

        callViewHolder.ll_location.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //定位
                ToastUtil.showToast(getContext(), "定位");
            }
        });
    }

    private void bindLiveViewHolder(LiveViewHolder liveViewHolder, NoticeBean noticeBean, int position) {
        liveViewHolder.tv_name.setText(noticeBean.getMemberName());
        liveViewHolder.tv_member_no.setText(noticeBean.getMemberId() + "");

//        liveViewHolder.tv_accept_live.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {//接收直播
//                noticePresenter.acceptLive(noticeBean);
//            }
//        });

        if (noticeBean.getInOrOut() == NoticeInOrOutEnum.OUT) {//主动 请求他人上报
            if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_REPORT) {//主动 上报
            } else if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_INVITE) {//主动 邀请他人直播 邀请中
                liveViewHolder.tv_accept_live.setText("正在请求");
            } else if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_INVITE_REFUSE) {//主动 邀请他人直播 邀请被拒绝
                liveViewHolder.tv_accept_live.setText("拒绝上报");
            } else if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_INVITE_AGREE) {//主动 邀请他人直播 邀请同意
                liveViewHolder.tv_accept_live.setText("正在观看");
            } else if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_WATCH) {//主动 邀请他人直播 正在观看
                liveViewHolder.tv_accept_live.setText("正在观看");
            } else if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_TIME_OUT) {//超时
                liveViewHolder.tv_accept_live.setText("请求超时");
            }else if (noticeBean.getOutLive() == NoticeOutLiveEnum.LIVE_OUT_END) {//结束
                liveViewHolder.tv_accept_live.setText("结束上报");
            }
        }
    }

    /**
     * 观看视频
     *
     * @param watchViewHolder
     * @param noticeBean
     * @param position
     */
    private void bindWatchViewHolder(WatchViewHolder watchViewHolder, NoticeBean noticeBean, int position) {
        watchViewHolder.tv_name.setText(noticeBean.getMemberName());
        watchViewHolder.tv_accept_live.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//接收直播
//                noticePresenter.goToWatch(noticeBean.getTerminalMessage(),-1);
            }
        });
    }


    @Override
    public int getItemCount() {
        return getDatas().size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iv_phone;//电话图标
        private final TextView tv_name;
        private final TextView tv_member_no;
        private final TextView tv_time;
        private final LinearLayout ll_location;//定位
        private final LinearLayout ll_call_answer_hang;//接听或挂断
        private final TextView tv_answer_hang;//接听或挂断


        public CallViewHolder(View itemView) {
            super(itemView);
            iv_phone = itemView.findViewById(R.id.iv_phone);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_member_no = itemView.findViewById(R.id.tv_member_no);
            ll_location = itemView.findViewById(R.id.ll_location);
            ll_call_answer_hang = itemView.findViewById(R.id.ll_call_answer_hang);
            tv_answer_hang = itemView.findViewById(R.id.tv_answer_hang);
        }
    }

    static class LiveViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_name;
        private final TextView tv_member_no;
        private final TextView tv_accept_live;

        public LiveViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);//名称
            tv_member_no = itemView.findViewById(R.id.tv_member_no);//警号
            tv_accept_live = itemView.findViewById(R.id.tv_accept_live);//描述
        }
    }

    static class WatchViewHolder extends RecyclerView.ViewHolder {

        private final TextView tv_name;
        private final TextView tv_accept_live;

        public WatchViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_accept_live = itemView.findViewById(R.id.tv_accept_live);
        }
    }
}
