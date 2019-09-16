package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.view.layout.MvpLinearLayout;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.LiveSmallCoverPresenter;
import com.vsxin.terminalpad.mvp.contract.view.ILiveSmallCoverView;
import com.vsxin.terminalpad.utils.ResUtil;

/**
 * @author qzw
 * <p>
 * 直播小框 浮层view
 */
public class LiveSmallCoverView extends MvpLinearLayout<ILiveSmallCoverView, LiveSmallCoverPresenter> implements ILiveSmallCoverView {

    private TextView tv_member_name;
    private TextView tv_member_no;
    private TextView tv_time;
    private TextView tv_group_call_member;
    private LinearLayout ll_group_call_member;
    private PttButton ptt_group_call;
    private ImageView iv_share_live;
    private ImageView iv_full_screen;
    private ImageView iv_break_live;

    private OnClickListener quitLiveClickListener;//退出
    private OnClickListener fullScreenClickListener;//全屏
    private OnClickListener shareLiveClickListener;//分享

    public LiveSmallCoverView(Context context) {
        super(context);
        initView();
    }

    public LiveSmallCoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        getLogger().info("initView");
        LayoutInflater.from(getSuperContext()).inflate(R.layout.view_live_small_cover, this, true);

        //上报人名称
        tv_member_name = findViewById(R.id.tv_member_name);
        //警号
        tv_member_no = findViewById(R.id.tv_member_no);
        //上报计时
        tv_time = findViewById(R.id.tv_time);

        //当前组呼人名称
        ll_group_call_member = findViewById(R.id.ll_group_call_member);
        tv_group_call_member = findViewById(R.id.tv_group_call_member);

        //组呼按钮
        ptt_group_call = findViewById(R.id.ptt_group_call);

        //分享
        iv_share_live = findViewById(R.id.iv_share_live);
        //全屏
        iv_full_screen = findViewById(R.id.iv_full_screen);
        //退出
        iv_break_live = findViewById(R.id.iv_break_live);

        //退出
        iv_break_live.setOnClickListener(v -> {
            if (quitLiveClickListener != null) {
                quitLiveClickListener.onClick(v);
            }
        });
        //全屏
        iv_full_screen.setOnClickListener(v -> {
            if (fullScreenClickListener != null) {
                fullScreenClickListener.onClick(v);
            }
        });
        //分享
        iv_share_live.setOnClickListener(v -> {
            if (shareLiveClickListener != null) {
                shareLiveClickListener.onClick(v);
            }
        });
        initPTT();
    }

    private void initPTT() {
        ptt_group_call.setPttListener(new SendGroupCallListener() {
            @Override
            public void speaking() {
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_speaking));
            }

            @Override
            public void readySpeak() {
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_pre_speak));
            }

            @Override
            public void forbid() {
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_other));
            }

            @Override
            public void waite() {
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_pre_speak));
            }

            @Override
            public void silence() {//结束
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_normal));
                showCurrentGroupCall(false);
            }

            @Override
            public void listening(String memberName) {//听
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_other));
                if(!TextUtils.isEmpty(memberName)){
                    showCurrentGroupCall(true);
                    setCurrentGroupCallMember(memberName);
                }
            }

            @Override
            public void fail() {
                ptt_group_call.setBackground(ResUtil.getDrawable(getContext(),R.mipmap.ic_ptt_normal));
            }
        });
    }

    /**
     * 设置上报人信息
     * @param name
     * @param no
     */
    public void setMemberInfo(String name,String no){
        tv_member_name.setText(name);
        tv_member_no.setText(no);
    }

    /**
     * 设置当前组呼人员名称
     * @param name
     */
    public void setCurrentGroupCallMember(String name){
        tv_group_call_member.setText(name);
    }

    /**
     * 是否显示当前组呼人
     * @param isShow
     */
    private void showCurrentGroupCall(Boolean isShow){
        ll_group_call_member.setVisibility(isShow?VISIBLE:GONE);
    }

    /**
     * 退出
     *
     * @param quitLiveClickListener
     */
    public void setQuitLiveClickListener(OnClickListener quitLiveClickListener) {
        this.quitLiveClickListener = quitLiveClickListener;
    }

    /**
     * 全屏
     *
     * @param fullScreenClickListener
     */
    public void setFullScreenClickListener(OnClickListener fullScreenClickListener) {
        this.fullScreenClickListener = fullScreenClickListener;
    }

    /**
     * 分享
     *
     * @param shareLiveClickListener
     */
    public void setShareLiveClickListener(OnClickListener shareLiveClickListener) {
        this.shareLiveClickListener = shareLiveClickListener;
    }

    @Override
    public LiveSmallCoverPresenter createPresenter() {
        return new LiveSmallCoverPresenter(getSuperContext());
    }
}
