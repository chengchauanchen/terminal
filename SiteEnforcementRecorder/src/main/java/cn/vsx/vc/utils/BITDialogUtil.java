package cn.vsx.vc.utils;

import android.app.AlertDialog;
import android.util.TypedValue;

import ptt.terminalsdk.tools.DialogUtil;

public abstract class BITDialogUtil extends DialogUtil {
    public AlertDialog showDialog() {
        AlertDialog dialog = super.showDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        return dialog;
    }
}
