package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import cn.vsx.vc.R;

public class UnbindDialog extends Dialog {

    private TextView textTitle;
    private TextView tvCancel;
    private TextView tvSure;
    private String  content;
   private UnbindListener unbindListener;

    public UnbindDialog(Context context,String content,UnbindListener unbindListener) {
        super(context);
        this.content = content;
        this.unbindListener = unbindListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_unbind);
        initView();
        init();
    }

    private void initView() {
        textTitle = findViewById(R.id.text_title);
        tvCancel = findViewById(R.id.tv_cancel);
        tvSure = findViewById(R.id.tv_sure);
        tvCancel.setOnClickListener(v -> dismiss());
        tvSure.setOnClickListener(v ->{
            if(unbindListener!=null){
                unbindListener.unbind();
            }
            dismiss();
        });

    }

    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.width= (int) (width*0.9);
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        textTitle.setText(getContext().getString(R.string.text_temp_unbind));
    }

    public interface UnbindListener{
        public void unbind();
    }

}
