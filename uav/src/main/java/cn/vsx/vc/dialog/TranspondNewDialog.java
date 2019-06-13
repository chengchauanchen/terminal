package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.vc.R;

/**
 *  转发
 * Created by gt358 on 2017/9/14.
 */

public class TranspondNewDialog extends Dialog {


    private LinearLayout tv_copy;
    private LinearLayout tv_forward;
    private LinearLayout tv_withdraw;
    private LinearLayout tv_forward_more;


    //撤回时间限制
    private static final int WITH_DRAW_TIME = 2;


    public TranspondNewDialog(Context context, final TerminalMessage terminalMessage,OnItemClickListener onItemClickListener) {
        super(context, R.style.dialog_style);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_transpond, null);
        tv_copy =  view.findViewById(R.id.tv_copy);
        tv_forward =  view.findViewById(R.id.tv_forward);
        tv_withdraw =  view.findViewById(R.id.tv_withdraw);
        tv_forward_more =  view.findViewById(R.id.tv_forward_more);
        //只有文本消息才复制
        if(terminalMessage.messageType != MessageType.SHORT_TEXT.getCode() &&terminalMessage.messageType != MessageType.LONG_TEXT.getCode()){
            tv_copy.setVisibility(View.GONE);
        }
        //转发 (个呼不可以转发)
        tv_forward.setVisibility(View.GONE);
//        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode()){
//            tv_forward.setVisibility(View.GONE);
//        }
        //只有自己才撤回，并且是五分钟之内的消息
        tv_withdraw.setVisibility(View.GONE);
//        if(terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)&&
//                isShowWithDrawBySendTime(terminalMessage.sendTime)){
//            tv_withdraw.setVisibility(View.VISIBLE);
//        }else{
//            tv_withdraw.setVisibility(View.GONE);
//        }
        //合并转发 (个呼和图像不可以合并转发)
        tv_forward_more.setVisibility(View.GONE);
//        if(terminalMessage.messageType == MessageType.PRIVATE_CALL.getCode() ||
//                terminalMessage.messageType == MessageType.WARNING_INSTANCE.getCode()||
//                terminalMessage.messageType == MessageType.VIDEO_LIVE.getCode()||
//                terminalMessage.messageType == MessageType.GB28181_RECORD.getCode()){
//            tv_forward_more.setVisibility(View.GONE);
//        }
        //复制
        tv_copy.setOnClickListener(v -> {
            if(onItemClickListener!=null){
                onItemClickListener.onCopy(terminalMessage);
            }
            dismiss();
        });
        //转发
        tv_forward.setOnClickListener(view1 -> {
            dismiss();
        });
        //撤回
        tv_withdraw.setOnClickListener(v -> {
            dismiss();
        });
        //合并转发
        tv_forward_more.setOnClickListener(v -> {
            dismiss();
        });

        setContentView(view);
    }

    /**
     * 显示菜单
     */
    public void showView(){

        if(tv_forward==null||tv_copy == null||tv_withdraw == null||tv_forward_more == null){
            return;
        }

        if(tv_forward.getVisibility() == View.VISIBLE ||
                tv_copy.getVisibility() == View.VISIBLE ||
                tv_withdraw.getVisibility() == View.VISIBLE ||
                tv_forward_more.getVisibility() == View.VISIBLE){
            show();
        }

    }

    /**
     * 根据 sendTime判断是否显示撤回（是否在五分钟之内）
     * @param sendTime
     * @return
     */
    private boolean isShowWithDrawBySendTime(long sendTime){
        long currentTime = System.currentTimeMillis();
        long s = (currentTime - sendTime) / (1000 * 60);
        return (s>=0 && s<WITH_DRAW_TIME);
    }


    public interface  OnItemClickListener{
        void onCopy(TerminalMessage terminalMessage);
    }
}
