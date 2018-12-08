package cn.vsx.vc.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.vsx.vc.R;

/**
 * Created by Administrator on 2017/3/21 0021.
 */

public class CallStatePopuwindow extends PopupWindow implements View.OnClickListener {
    RelativeLayout individual_call_request, individual_call_chooice;
    LinearLayout individual_call_speaking, emergency_group_call, llIndividualCallHangupRequest, llIndividualCallRefuse,
            llIndividualCallAccept;
    ImageView llIndividualCallRetractRequest, ivMemberPortraitRequest, individualCallRetractEmergency, ivMemberPortraitChooice,
            llIndividualCallRetractSpeaking, ivMemberPortraitSpeaking, llIndividualCallHangupSpeaking, llIndividualCallRetractEmergency,
            groupLogo, llIndividualCallHangupEmergency;

    TextView tvRequestPrompt, tvMemberNameRequest, tvMemberIdRequest, tvChooicePrompt, tvMemberNameChooice, tvMemberIdChooice,
            tvSpeakingPrompt, tvMemberNameSpeaking, tvMemberIdSpeaking, tvCurrentGroup;

    IndividualCallTimerView ICTVSpeakingTimeSpeaking, ICTVSpeakingTimeEmergency;
    CheckBox individualSetGroupCallNovoice, individualetGroupCallMaxvoice, setGroupCallNovoice, setGroupCallMaxvoice;

    private Context context;
    private View view;

    public CallStatePopuwindow(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        setAnimationStyle(R.style.AnimationWindow);
        view = LayoutInflater.from(context).inflate(R.layout.individual_call, null);
        findIds(view);
        setContentView(view);
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.MATCH_PARENT);// 控制popupwindow屏幕
        ColorDrawable dw = new ColorDrawable(0x00000000);
        this.setBackgroundDrawable(dw);
        setFocusable(true); // 设置PopupWindow可获得焦点
        setTouchable(true); // 设置PopupWindow可触摸
        setClippingEnabled(false);
        setOutsideTouchable(false); // 设置非PopupWindow区域可触摸
        initListener();
    }

    private void findIds(View view) {
        individual_call_request = (RelativeLayout) view.findViewById(R.id.individual_call_request);
        individual_call_chooice = (RelativeLayout) view.findViewById(R.id.individual_call_chooice);
        individual_call_speaking = (LinearLayout) view.findViewById(R.id.individual_call_speaking);
        emergency_group_call = (LinearLayout) view.findViewById(R.id.emergency_group_call);
        //请求个呼页面
        llIndividualCallRetractRequest = (ImageView) view.findViewById(R.id.ll_individual_call_retract_request);
        tvRequestPrompt = (TextView) view.findViewById(R.id.tv_request_prompt);
        ivMemberPortraitRequest = (ImageView) view.findViewById(R.id.iv_member_portrait_request);
        tvMemberNameRequest = (TextView) view.findViewById(R.id.tv_member_name_request);
        tvMemberIdRequest = (TextView) view.findViewById(R.id.tv_member_id_request);
        llIndividualCallHangupRequest = (LinearLayout) view.findViewById(R.id.ll_individual_call_hangup_request);
        //选择接听/挂断页面
        individualCallRetractEmergency = (ImageView) view.findViewById(R.id.individual_call_retract_emergency);
        tvChooicePrompt = (TextView) view.findViewById(R.id.tv_chooice_prompt);
        ivMemberPortraitChooice = (ImageView) view.findViewById(R.id.iv_member_portrait_chooice);
        tvMemberNameChooice = (TextView) view.findViewById(R.id.tv_member_name_chooice);
        tvMemberIdChooice = (TextView) view.findViewById(R.id.tv_member_id_chooice);
        llIndividualCallRefuse = (LinearLayout) view.findViewById(R.id.ll_individual_call_refuse);
        llIndividualCallAccept = (LinearLayout) view.findViewById(R.id.ll_individual_call_accept);
        //个呼通话中界面
        llIndividualCallRetractSpeaking = (ImageView) view.findViewById(R.id.ll_individual_call_retract_speaking);
        tvSpeakingPrompt = (TextView) view.findViewById(R.id.tv_speaking_prompt);
        ivMemberPortraitSpeaking = (ImageView) view.findViewById(R.id.iv_member_portrait_speaking);
        tvMemberNameSpeaking = (TextView) view.findViewById(R.id.tv_member_name_speaking);
        tvMemberIdSpeaking = (TextView) view.findViewById(R.id.tv_member_id_speaking);
        ICTVSpeakingTimeSpeaking = (IndividualCallTimerView) view.findViewById(R.id.ICTV_speaking_time_speaking);
        individualSetGroupCallNovoice = (CheckBox) view.findViewById(R.id.individual_set_group_call_novoice);
        llIndividualCallHangupSpeaking = (ImageView) view.findViewById(R.id.ll_individual_call_hangup_speaking);
        individualetGroupCallMaxvoice = (CheckBox) view.findViewById(R.id.individual_set_group_call_maxvoice);
        //紧急组呼通话界面
        llIndividualCallRetractEmergency = (ImageView) view.findViewById(R.id.ll_individual_call_retract_emergency);
        groupLogo = (ImageView) view.findViewById(R.id.group_logo);
        tvCurrentGroup = (TextView) view.findViewById(R.id.tv_current_group);
        ICTVSpeakingTimeEmergency = (IndividualCallTimerView) view.findViewById(R.id.ICTV_speaking_time_emergency);
        setGroupCallNovoice = (CheckBox) view.findViewById(R.id.set_group_call_novoice);
        llIndividualCallHangupEmergency = (ImageView) view.findViewById(R.id.ll_individual_call_hangup_emergency);
        setGroupCallMaxvoice = (CheckBox) view.findViewById(R.id.set_group_call_maxvoice);
    }

    private void initListener() {
        llIndividualCallRetractRequest.setOnClickListener(this);
        individualCallRetractEmergency.setOnClickListener(this);
        llIndividualCallRetractSpeaking.setOnClickListener(this);
        llIndividualCallRetractEmergency.setOnClickListener(this);

        llIndividualCallHangupRequest.setOnClickListener(this);
        llIndividualCallRefuse.setOnClickListener(this);
        llIndividualCallAccept.setOnClickListener(this);
        llIndividualCallHangupSpeaking.setOnClickListener(this);
        llIndividualCallHangupEmergency.setOnClickListener(this);
    }

    public void showGroupCall(View view, Activity activity) {
        individual_call_request.setVisibility(View.GONE);
        individual_call_chooice.setVisibility(View.GONE);
        individual_call_speaking.setVisibility(View.GONE);
        emergency_group_call.setVisibility(View.VISIBLE);
        showAtLocation(view, Gravity.NO_GRAVITY, 0, getStatusBarHeight(activity));
    }

    public void showPersonCall(View view, Activity activity) {
        individual_call_request.setVisibility(View.VISIBLE);
        individual_call_chooice.setVisibility(View.GONE);
        individual_call_speaking.setVisibility(View.GONE);
        emergency_group_call.setVisibility(View.GONE);
        showAtLocation(view, Gravity.NO_GRAVITY, 0, getStatusBarHeight(activity));
    }

    public static int getStatusBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_individual_call_retract_request://个呼等待页面缩小
//                break;
            case R.id.ll_individual_call_hangup_request://个呼等待页面挂断
//                break;
            case R.id.individual_call_retract_emergency://个呼等待页面缩小
//                break;
            case R.id.ll_individual_call_retract_speaking://个呼接通页面挂断
//                break;
            case R.id.ll_individual_call_retract_emergency://紧急组呼接通页面缩小
                dismiss();
                break;
        }
    }
}
