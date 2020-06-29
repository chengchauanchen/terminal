package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.NfcUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.receiveHandler.ReceiveNFCWriteResultHandler;

public class NFCBindingDialog extends Dialog implements DialogInterface.OnDismissListener {

    private RelativeLayout rlContent;
    private ImageView ivBinding;
    private TextView tvTempt;
    private ImageView ivClose;

    private int type = 1;
    public static final int TYPE_WAIT = 1;//等待刷
    public static final int TYPE_COMPLETE = 2;//刷入完成

    protected Handler handler = new Handler(Looper.getMainLooper()) {
    };

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
                Glide.with(getContext()).load(R.drawable.icon_nfc_binding_gif)
                        .asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(ivBinding);
                ivClose.setVisibility(View.VISIBLE);
                tvTempt.setVisibility(View.VISIBLE);
                tvTempt.setText(getContext().getString(R.string.binding_nfc_tempt));
                break;
            case TYPE_COMPLETE:
                rlContent.setVisibility(View.VISIBLE);
                ivBinding.setVisibility(View.GONE);
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
        setOnDismissListener(this);
    }

    /**
     * 显示NFC弹窗
     */
    public void showDialog(int groupId,String warningId,String voiceDes){
        if(type == TYPE_WAIT){
            //设置刷NFC需要传的数据
            int memberId = MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
            long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L);
            String action = "";
            if(TextUtils.isEmpty(warningId)){
                action = MyTerminalFactory.getSDK().getNfcManager().getBindString(memberId,uniqueNo+"",groupId);
            }else{
                action = MyTerminalFactory.getSDK().getNfcManager().getBindWarningString(memberId,uniqueNo+"",groupId,warningId);
            }
            MyTerminalFactory.getSDK().getNfcManager().setTransmitData(action);
            MyTerminalFactory.getSDK().registReceiveHandler(receiveNFCWriteResultHandler);
        }
        show();
    }

    /**
     * 隐藏NFC弹窗
     * @param dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        NfcUtil.writeData();
        type = TYPE_WAIT;
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNFCWriteResultHandler);
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 收到刷入NFC数据的回调的通知
     **/
    private ReceiveNFCWriteResultHandler receiveNFCWriteResultHandler = (resultCode, resultDec) -> {
        if (resultCode == 0 && isShowing()) {
            type = TYPE_COMPLETE;
            handler.post(this::setView);
            handler.postDelayed(this::dismiss,2*1000);
        }
    };
}
