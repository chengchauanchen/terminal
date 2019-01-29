package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverShowCopyPopupHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowTransponPopupHandler;

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
        if(terminalMessage.messageType != MessageType.SHORT_TEXT.getCode() &&terminalMessage.messageType != MessageType.LONG_TEXT.getCode()){
            tv_copy.setVisibility(View.GONE);
        }
        tv_linkman_delete.setOnClickListener(view1 -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowTransponPopupHandler.class);
            dismiss();
        });
        tv_copy.setOnClickListener(v -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowCopyPopupHandler.class,terminalMessage);
            dismiss();
        });
        setContentView(view);
    }
}
