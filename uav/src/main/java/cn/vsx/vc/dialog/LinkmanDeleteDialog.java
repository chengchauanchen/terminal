package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.zectec.imageandfileselector.receivehandler.ReceiverLinkManDeleteHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverDialogDimissHandler;

/**
 * Created by gt358 on 2017/8/14.
 */

public class LinkmanDeleteDialog extends Dialog {

    public LinkmanDeleteDialog(@NonNull Context context) {
        super(context, R.style.dialog_style);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_linkmandelete, null);
        view.findViewById(R.id.tv_linkman_delete).setOnClickListener(view1 -> OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverLinkManDeleteHandler.class));
        setContentView(view);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverDialogDimissHandler.class, LinkmanDeleteDialog.class.getName());
    }
}
