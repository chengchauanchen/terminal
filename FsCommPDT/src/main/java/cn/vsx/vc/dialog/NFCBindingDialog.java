package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.ChooseDevicesAdapter;

public class NFCBindingDialog extends Dialog {

    private RelativeLayout rlContent;
    private ImageView ivBinding;
    private TextView tvTempt;
    private ImageView ivClose;

    private int type = 1;
    public static final int TYPE_WAIT = 1;//等待刷
    public static final int TYPE_COMPLETE = 2;//刷入完成

    public NFCBindingDialog(Context context, int type) {
        super(context);
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_nfc_binding);
        initView();
        setView();
        init();
    }

    private void initView() {
        rlContent = findViewById(R.id.rl_content);
        ivBinding = findViewById(R.id.iv_binding);
        tvTempt = findViewById(R.id.tv_tempt);
        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(v -> dismiss());
    }

    /**
     * 设置控件
     */
    private void setView() {
        switch (type){
            case TYPE_WAIT:
                rlContent.setVisibility(View.INVISIBLE);
                ivBinding.setVisibility(View.VISIBLE);
                ivClose.setVisibility(View.VISIBLE);

                tvTempt.setVisibility(View.VISIBLE);
                tvTempt.setText(getContext().getString(R.string.binding_nfc_tempt));
                break;
            case TYPE_COMPLETE:
                rlContent.setVisibility(View.VISIBLE);
                ivBinding.setVisibility(View.INVISIBLE);
                ivClose.setVisibility(View.GONE);

                tvTempt.setVisibility(View.VISIBLE);
                tvTempt.setText(getContext().getString(R.string.binding_success_tempt));
                break;
        }
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
    }
}
