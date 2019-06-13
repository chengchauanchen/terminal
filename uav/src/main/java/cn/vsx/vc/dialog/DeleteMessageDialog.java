package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverDeleteMessageHandler;

/**
 * Created by gt358 on 2017/9/27.
 */

public class DeleteMessageDialog extends Dialog{
    public DeleteMessageDialog (Context context) {
        super(context, R.style.dialog_style);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.diaslog_detele_messsage, null);
        view.findViewById(R.id.tv_message_delete).setOnClickListener(view1 -> {
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverDeleteMessageHandler.class);
            dismiss();
        });
        setContentView(view);
    }
}
