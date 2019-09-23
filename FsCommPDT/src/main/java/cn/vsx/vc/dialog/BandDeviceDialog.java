package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.vsx.hamster.common.MountType;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.dialog.UnbindDialog.UnbindListener;
import cn.vsx.vc.model.DongHuTerminalType;
import cn.vsx.vc.model.Relationship;
import cn.vsx.vc.utils.ToastUtil;
import cn.vsx.vc.view.XCDropDownListWhiteBgView;
import cn.vsx.vc.view.XCDropDownListWhiteBgView.XCDropDownListViewClickListeren;
import ptt.terminalsdk.context.MyTerminalFactory;

public class BandDeviceDialog extends Dialog {

    private TextView tvCancel;
    private TextView tvSure;
    private EditText et_device_no;
    private TextView tv_title_hint;

    private SureBindListener sureBindListener;

    private String title;

    public BandDeviceDialog(Context context, String title,SureBindListener sureBindListener) {
        super(context,R.style.inputDialog);
        this.title = title;
        this.sureBindListener = sureBindListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_band_device);
        initView();
        init();
    }

    private void initView() {
        tv_title_hint = findViewById(R.id.tv_title_hint);//设备编号
        et_device_no = findViewById(R.id.et_device_no);//设备编号

        if(!TextUtils.isEmpty(title)){
            tv_title_hint.setText(title);
        }

        tvCancel = findViewById(R.id.tv_cancel);
        tvSure = findViewById(R.id.tv_sure);
        tvCancel.setOnClickListener(v -> dismiss());
        tvSure.setOnClickListener(v -> {
            if (sureBindListener != null) {
                sureBindListener.bind(et_device_no.getText().toString());
                //bandDevice();
            }
        });
    }

    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = (int) (width * 0.9);
//        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(layoutParams);
        window.setAttributes(layoutParams);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    public interface SureBindListener {
        void bind(String deviceNo);
    }





}
