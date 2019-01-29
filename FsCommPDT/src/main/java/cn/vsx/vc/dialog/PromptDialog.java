package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import cn.vsx.vc.R;

/**
 * Created by Administrator on 2017/3/18 0018.
 */

public class PromptDialog extends Dialog implements View.OnClickListener {
    private String contentStr = "";
    private String titleStr = "";
    private String okStr = "";
    private String cancelStr = "";
    private DialogOnclickListener dialogOnclickListener;

    public PromptDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_change_group_cancel);

        TextView quit =  findViewById(R.id.dialog_no);
        TextView ok =  findViewById(R.id.dialog_yes);
        TextView content =  findViewById(R.id.dialog_title);
        if (!"".equals(contentStr)) {
            content.setText(contentStr);
        }
        if (!"".equals(okStr)) {
            ok.setText(okStr);
        }
        if (!"".equals(cancelStr)) {
            quit.setText(cancelStr);
        }
        quit.setOnClickListener(this);
        ok.setOnClickListener(this);
    }

    public void setContent(String contentStr) {
        this.contentStr = contentStr;
    }

    public void setTitle(String titleStr) {
        this.titleStr = titleStr;
    }

    public void setOkText(String okStr) {
        this.okStr = okStr;
    }

    public void setCancelText(String cancelStr) {
        this.cancelStr = cancelStr;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_no:
                dialogOnclickListener.onQuitClick();
                break;
            case R.id.dialog_yes:
                dialogOnclickListener.onOkClick();
                break;
        }
    }

    public void setDialogOnclickListener(DialogOnclickListener dialogOnclickListener) {
        this.dialogOnclickListener = dialogOnclickListener;
    }

    public interface DialogOnclickListener {
        void onOkClick();

        void onQuitClick();
    }
}
