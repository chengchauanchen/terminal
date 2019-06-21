package cn.vsx.vc.utils;

import android.app.AlertDialog;
import android.util.TypedValue;
import android.widget.Button;

import ptt.terminalsdk.tools.DialogUtil;

public abstract class BITDialogUtil extends DialogUtil {

    private AlertDialog dialog;
    public AlertDialog showDialog() {
        if(dialog == null){
            dialog = super.showDialog();
        }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        dialog.show();
        return dialog;
    }

    public AlertDialog showDialog(String positive,String negative) {
        if(dialog == null){
            dialog = super.showDialog();
        }
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        positiveButton.setText(positive);

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        negativeButton.setText(negative);
        if(dialog.isShowing()){
            dialog.dismiss();
        }
        dialog.show();
        return dialog;
    }


    public AlertDialog  getAlertDialog(){
        return dialog;
    }
}
