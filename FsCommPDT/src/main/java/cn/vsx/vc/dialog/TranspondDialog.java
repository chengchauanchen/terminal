package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;
import com.zectec.imageandfileselector.utils.SystemUtil;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverShowCopyPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowTransponPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowWithDrawPopupHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 *  转发
 * Created by gt358 on 2017/9/14.
 */

public class TranspondDialog  extends Dialog {

    public TranspondDialog (Context context, final TerminalMessage terminalMessage) {
        super(context, R.style.dialog_style);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_transpond, null);
        LinearLayout tv_linkman_delete =  view.findViewById(R.id.tv_linkman_delete);
        LinearLayout tv_copy =  view.findViewById(R.id.tv_copy);
        LinearLayout tv_withdraw =  view.findViewById(R.id.tv_withdraw);
        //只有文本消息才复制
        if(terminalMessage.messageType != MessageType.SHORT_TEXT.getCode() &&terminalMessage.messageType != MessageType.LONG_TEXT.getCode()){
            tv_copy.setVisibility(View.GONE);
        }
        //只有自己才撤回，并且是五分钟之内的消息
        if(terminalMessage.messageFromId == MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)&&
                isShowWithDrawBySendTime(terminalMessage.sendTime)){
            tv_withdraw.setVisibility(View.VISIBLE);
        }else{
            tv_withdraw.setVisibility(View.GONE);
        }
        //转发
        tv_linkman_delete.setVisibility(View.GONE);
        tv_linkman_delete.setOnClickListener(view1 -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowTransponPopupHandler.class);
            dismiss();
        });
        tv_copy.setOnClickListener(v -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowCopyPopupHandler.class,terminalMessage);
            dismiss();
        });
        tv_withdraw.setOnClickListener(v -> {
            //撤回
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowWithDrawPopupHandler.class,terminalMessage);
            dismiss();
        });
        setContentView(view);
    }

    /**
     * 根据 sendTime判断是否显示撤回（是否在五分钟之内）
     * @param sendTime
     * @return
     */
    private boolean isShowWithDrawBySendTime(long sendTime){
        long currentTime = System.currentTimeMillis();
        long s = (currentTime - sendTime) / (1000 * 60);
        return (s>=0 && s<5);
    }
}
