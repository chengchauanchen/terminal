package cn.vsx.vc.view.custompopupwindow;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveAnswerIndividualCallTimeoutHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyEmergencyIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.utils.DataUtil;
import cn.vsx.vc.view.IndividualCallTimerView;
import ptt.terminalsdk.tools.PhoneAdapter;

/**
 * 个呼的popupwindow
 * Created by gt358 on 2017/8/19.
 */

public class IndividualNewsPopupwindow extends PopupWindow{

    //popupWindow页面
    @Bind(R.id.individual_call_request)
    RelativeLayout individual_call_request;
    @Bind(R.id.individual_call_chooice)
    RelativeLayout individual_call_chooice;
    @Bind(R.id.individual_call_speaking)
    LinearLayout individual_call_speaking;
    @Bind(R.id.emergency_group_call)
    LinearLayout emergency_group_call;

    //请求个呼页面
    @Bind(R.id.tv_request_prompt)
    TextView tv_request_prompt;
    @Bind(R.id.iv_member_portrait_request)
    ImageView iv_member_portrait_request;
    @Bind(R.id.tv_member_name_request)
    TextView tv_member_name_request;
    @Bind(R.id.tv_member_id_request)
    TextView tv_member_id_request;
    @Bind(R.id.ll_individual_call_hangup_request)
    LinearLayout ll_individual_call_hangup_request;
    @Bind(R.id.ll_individual_call_retract_request)
    ImageView ll_individual_call_retract_request;
    @Bind(R.id.individual_call_request_view)
    View individual_call_request_view;

    //个呼选择页面
    @Bind(R.id.tv_chooice_prompt)
    TextView tv_chooice_prompt;
    @Bind(R.id.iv_member_portrait_chooice)
    ImageView iv_member_portrait_chooice;
    @Bind(R.id.tv_member_name_chooice)
    TextView tv_member_name_chooice;
    @Bind(R.id.tv_member_id_chooice)
    TextView tv_member_id_chooice;
    @Bind(R.id.ll_individual_call_accept)
    LinearLayout ll_individual_call_accept;
    @Bind(R.id.ll_individual_call_refuse)
    LinearLayout ll_individual_call_refuse;
    @Bind(R.id.individual_call_chooice_view)
    View individual_call_chooice_view;

    //个呼说话页面
    @Bind(R.id.tv_speaking_prompt)
    TextView tv_speaking_prompt;
    @Bind(R.id.iv_member_portrait_speaking)
    ImageView iv_member_portrait_speaking;
    @Bind(R.id.tv_member_name_speaking)
    TextView tv_member_name_speaking;
    @Bind(R.id.tv_member_id_speaking)
    TextView tv_member_id_speaking;
    @Bind(R.id.ll_individual_call_hangup_speaking)
    ImageView ll_individual_call_hangup_speaking;
    @Bind(R.id.ll_individual_call_retract_speaking)
    ImageView ll_individual_call_retract_speaking;
    @Bind(R.id.ICTV_speaking_time_speaking)
    IndividualCallTimerView ICTV_speaking_time_speaking;
    @Bind(R.id.individual_call_speak_view)
    View individual_call_speak_view;

    //紧急组呼页面
//        @Bind(R.id.tv_current_folder)
//        TextView tv_current_folder;
    @Bind(R.id.tv_current_group)
    TextView tv_current_group;
    @Bind(R.id.ll_individual_call_hangup_emergency)
    ImageView ll_individual_call_hangup_emergency;
    @Bind(R.id.ll_individual_call_retract_emergency)
    ImageView ll_individual_call_retract_emergency;
    @Bind(R.id.ICTV_speaking_time_emergency)
    IndividualCallTimerView ICTV_speaking_time_emergency;

    private Context context;
    public IndividualNewsPopupwindow (Context context) {
        this.context = context;
        initView();
        initListener();
    }

    private void initView () {
        View popupWindowView = View.inflate(context, R.layout.individual_call, null);

        setContentView(popupWindowView);
        ButterKnife.bind(this, popupWindowView);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        //是否响应touch事件
        setTouchable(true);
        //是否具有获取焦点的能力
        setFocusable(true);
        //外部是否可以点击
        setOutsideTouchable(false);//android6.0按返回键还是能关闭popuwindow

        ButterKnife.bind(this, popupWindowView);
        individual_call_request.setVisibility(View.GONE);
        individual_call_chooice.setVisibility(View.GONE);
        individual_call_speaking.setVisibility(View.GONE);
        emergency_group_call.setVisibility(View.GONE);
    }

    private void initListener () {

    }

    public void doOtherDestory () {

    }

    private void recoverSpeakingPop() {
        if (PhoneAdapter.isF25()) {
            individual_call_speak_view.setVisibility(View.GONE);
        } else {
            individual_call_speak_view.setVisibility(View.VISIBLE);
        }
        ICTV_speaking_time_speaking.stop();
        tv_speaking_prompt.setText("您正在进行个呼");
//        viewHolder.ll_individual_call_hangup_speaking.setBackgroundResource(R.drawable.individual_call_button_shape);
        individual_call_speaking.setVisibility(View.GONE);
    }

    private void recoverChooicePop() {
        if (PhoneAdapter.isF25()) {
            individual_call_chooice_view.setVisibility(View.GONE);
        } else {
            individual_call_chooice_view.setVisibility(View.VISIBLE);
        }
        tv_chooice_prompt.setText("");
        ll_individual_call_accept.setEnabled(true);
        ll_individual_call_refuse.setEnabled(true);
        individual_call_chooice.setVisibility(View.GONE);
    }

    private void recoverRequestPop() {
        if (PhoneAdapter.isF25()) {
            individual_call_request_view.setVisibility(View.GONE);
        } else {
            individual_call_request_view.setVisibility(View.VISIBLE);
        }

        tv_request_prompt.setText("等待对方接听");
//        viewHolder.ll_individual_call_hangup_request.setBackgroundResource(R.drawable.individual_call_button_shape);
        individual_call_request.setVisibility(View.GONE);
    }


    //停止个呼结束提示音
    Handler stopPromptHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PromptManager.getInstance().stopRing();
        }
    };

    private ReceiveServerConnectionEstablishedHandler mReceiveServerConnectionEstablishedHandler = new ReceiveServerConnectionEstablishedHandler() {
        @Override
        public void handler(boolean connected) {
            if (!connected) {
                if (individual_call_chooice.getVisibility() == View.VISIBLE) {
                    PromptManager.getInstance().IndividualHangUpRing();
                    stopPromptHandler.sendEmptyMessageDelayed(1, 2700);
                    recoverChooicePop();
                }
                if (individual_call_request.getVisibility() == View.VISIBLE) {
                    PromptManager.getInstance().stopRing();
                    recoverRequestPop();
                }
                if (individual_call_speaking.getVisibility() == View.VISIBLE) {
                    PromptManager.getInstance().IndividualHangUpRing();
                    stopPromptHandler.sendEmptyMessageDelayed(1, 2700);
                    recoverSpeakingPop();
                }
            }
        }
    };

    /**
     * 紧急个呼时，被动方强制接听
     */
    private ReceiveNotifyEmergencyIndividualCallHandler receiveNotifyEmergencyIndividualCallHandler = new ReceiveNotifyEmergencyIndividualCallHandler() {
        @Override
        public void handler(int mainMemberId) {
            individual_call_speaking.setVisibility(View.VISIBLE);
            ICTV_speaking_time_speaking.start();
            tv_member_id_speaking.setText("ID:" + mainMemberId);
            tv_member_name_speaking.setText(DataUtil.getMemberByMemberNo(mainMemberId).getName());
            MyApplication.instance.isPopupWindowShow = true;
        }
    };

    /**
     * 被动方个呼答复超时
     */
    private ReceiveAnswerIndividualCallTimeoutHandler receiveAnswerIndividualCallTimeoutHandler = new ReceiveAnswerIndividualCallTimeoutHandler() {
        @Override
        public void handler() {

        }
    };
}
