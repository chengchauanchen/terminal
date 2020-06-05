package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.vsx.vc.R;

public class DestroyTemporaryGroupsDialog extends Dialog {

    private LinearLayout llSubmitStatus;
    private ImageView icon;
    private TextView content;
    private TextView toJump;
    private LinearLayout llSelect;
    private Button btnConfirm;
    private Button btnCancel;

    //销毁临时组-默认状态（按钮）
    public static final int STATE_INIT = 0;
    //销毁临时组-销毁中
    public static final int STATE_DESTROYING = 1;
    //销毁临时组-销毁成功
    public static final int STATE_SUCCESS = 2;
    //销毁临时组-销毁失败
    public static final int STATE_FAIL = 3;

    private OnClickListener onClickListener;

    public DestroyTemporaryGroupsDialog(Context context, OnClickListener onClickListener) {
        super(context);
        this.onClickListener = onClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_temporary_group);
        initView();
        init();
    }

    private void initView() {
        llSubmitStatus = findViewById(R.id.ll_submit_status);
        icon = findViewById(R.id.iv_status);
        content = findViewById(R.id.text_context);
        toJump = findViewById(R.id.text_toJump);

        llSelect = findViewById(R.id.ll_select);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onConfirm();
            }
        });
        btnCancel.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onCancel();
            }
        });
    }

    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int heigth = display.getWidth();
        int width = display.getHeight();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = width / 2;
        layoutParams.height = heigth / 2;
        window.setAttributes(layoutParams);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        if(llSubmitStatus!=null){
            llSubmitStatus.setVisibility(View.GONE);
        }
        if(llSelect!=null){
            llSelect.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新销毁临时组的弹窗
     *
     * @param type
     * @param failMessage
     */
    public void updateDialog(int type, String failMessage) {
        try{
            show();
            switch (type) {
                case STATE_INIT:
                    //销毁临时组-确认是否销毁
                    llSubmitStatus.setVisibility(View.GONE);
                    llSelect.setVisibility(View.VISIBLE);
                    break;
                case STATE_DESTROYING:
                    //销毁临时组-销毁中
                    llSubmitStatus.setVisibility(View.VISIBLE);
                    llSelect.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.dialog_icon_creatting);
                    content.setText(R.string.text_temporary_group_destroying);
                    toJump.setVisibility(View.INVISIBLE);
                    break;
                case STATE_SUCCESS:
                    //销毁临时组-销毁成功
                    llSubmitStatus.setVisibility(View.VISIBLE);
                    llSelect.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.dialog_icon);
                    content.setText(R.string.text_disbanded_successfully);
                    toJump.setVisibility(View.VISIBLE);
                    toJump.setText(R.string.text_temporary_group_jumping);
                    break;
                case STATE_FAIL:
                    //销毁临时组-销毁失败
                    llSubmitStatus.setVisibility(View.VISIBLE);
                    llSelect.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.dialog_icon_fail);
                    content.setText(R.string.text_disbanded_fail);
                    failMessage = TextUtils.isEmpty(failMessage) ? "" : failMessage;
                    toJump.setText(failMessage);
                    toJump.setVisibility(View.VISIBLE);
                    break;
                default:break;
            }
        }catch (Exception e){
         e.printStackTrace();
        }
    }

    public interface OnClickListener {
        void onConfirm();
        void onCancel();
    }

}
