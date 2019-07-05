package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.RecorderBindBean;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.CodeCreatorUtil;
import cn.vsx.vc.utils.DensityUtil;

public class QRBindingDialog extends Dialog implements DialogInterface.OnDismissListener {

    private ImageView ivBinding;
    private TextView tvTempt;
    private ImageView ivClose;
    //生成二维码的图片大小
    private int bitmapSize = 0;
    private String  account;


    protected Handler handler = new Handler(Looper.getMainLooper()) {
    };

    public QRBindingDialog(Context context) {
        super(context);
        bitmapSize = DensityUtil.dip2px(context,300);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_qr_binding);
        initView();
        init();
    }

    private void initView() {
        ivBinding = findViewById(R.id.iv_binding);
        tvTempt = findViewById(R.id.tv_tempt);
        ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(v -> dismiss());
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
        setOnDismissListener(this);

       if(!TextUtils.isEmpty(account)){
           try{
               Bitmap bitmap = CodeCreatorUtil.createQRCode(account, bitmapSize, bitmapSize, null);
               ivBinding.setImageBitmap(bitmap);
           }catch (Exception e){
               e.printStackTrace();
           }
        }

    }

    /**
     * 显示NFC弹窗
     */
    public void showDialog(int groupId,int isTempGroup,String warningId){
         account = new Gson().toJson(new RecorderBindBean(TerminalFactory.getSDK().getUuid(),groupId,isTempGroup,warningId));
        if(!TextUtils.isEmpty(account)){
            show();
        }
    }

    /**
     * 隐藏NFC弹窗
     * @param dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        account = null;
    }

}
