package cn.vsx.vc.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.MessageCategory;
import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.Remark;
import cn.vsx.hamster.common.util.JsonParam;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyDataMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceivePcScanLoginResultHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.HandleIdUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

public class PcLoginActivity extends BaseActivity {
    @Bind(R.id.tv_tempt)
    TextView tvTempt;
    @Bind(R.id.tv_confirm)
    TextView tvConfirm;
    @Bind(R.id.tv_cancel)
    TextView tvCancel;
    private String result;
    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_pc_login;
    }

    @Override
    public void initView() {}

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

    @OnClick({R.id.iv_back, R.id.tv_confirm, R.id.tv_cancel})
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
