package cn.vsx.vc.activity;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePcScanLoginResultHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class PcLoginActivity extends BaseActivity implements View.OnClickListener{

    TextView tvTempt;

    TextView tvConfirm;

    TextView tvCancel;
    private String result;
    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    @Override
    public int getLayoutResId() {
        return R.layout.activity_pc_login;
    }

    @Override
    public void initView() {
        tvCancel = (TextView) findViewById(R.id.tv_cancel);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        tvTempt = (TextView) findViewById(R.id.tv_tempt);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.tv_confirm).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    @Override
    public void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receivePcScanLoginResultHandler);
    }

    @Override
    public void initData() {
        result = getIntent().getStringExtra(Constants.SCAN_DATA);
        if (TextUtils.isEmpty(result)) {
            finish();
            return;
        }
    }

    @Override
    public void doOtherDestroy() {
        MyTerminalFactory.getSDK().unregistReceiveHandler(receivePcScanLoginResultHandler);
        handler.removeCallbacksAndMessages(null);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_confirm:
                TerminalFactory.getSDK().getAuthManagerTwo().pcLogin(result);
                break;
            case R.id.tv_cancel:
                finish();
                break;
        }
    }

    /**
     * 接收到绑定结果的消息
     **/
    private ReceivePcScanLoginResultHandler receivePcScanLoginResultHandler = (resultCode, resultDesc) -> {
        ToastUtil.showToast(PcLoginActivity.this, resultDesc);
        if (resultCode == BaseCommonCode.SUCCESS_CODE) {
            handler.postDelayed(this::finish, 500);
        }
    };
}
