package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.JudgeWhetherConnect;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverChangeServerHandler;
import cn.vsx.vc.receiveHandle.ReceiverClearAccountHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.InputMethodUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class SetServerFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_sure)
    TextView tvSure;
    @Bind(R.id.et_server_ip)
    EditText etServerIp;
    @Bind(R.id.et_server_port)
    EditText etServerPort;

    private String contentIp;
    private String contentPort;

    private boolean isCanCheck;//联通校验是否完成

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static SetServerFragment newInstance() {
        SetServerFragment fragment = new SetServerFragment();
        Bundle args = new Bundle();
//        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            jumpType = getArguments().getInt(TYPE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_server, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_set_server));
        tvSure.setEnabled(false);
        etServerIp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                 contentIp = s.toString().trim();
                checkButtonEnable();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etServerPort.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                 contentPort = s.toString().trim();
                checkButtonEnable();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etServerPort.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //关闭软键盘
                InputMethodUtil.hideInputMethod(this.getContext(), etServerPort);
                setServerAddress();
                return true;
            }
            return false;
        });

        etServerIp.setText(TerminalFactory.getSDK().getAuthManagerTwo().getTempIp());
        etServerIp.setSelection(etServerIp.getText().toString().length());

        etServerPort.setText(TerminalFactory.getSDK().getAuthManagerTwo().getTempPort());
        etServerPort.setSelection(etServerPort.getText().toString().length());

    }

    @OnClick({R.id.iv_return, R.id.tv_sure})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.tv_sure:
                //确定
                InputMethodUtil.hideInputMethod(this.getContext(), etServerPort);
                setServerAddress();
                break;
        }
    }

    /**
     * 修改服务器地址
     */
    private void setServerAddress(){
        String ip = etServerIp.getText().toString().trim();
        String port = etServerPort.getText().toString().trim();
        if(TextUtils.isEmpty(ip)||TextUtils.isEmpty(port)){
            ToastUtil.showToast(getContext(),getString(R.string.text_please_input_ip_port));
            return;
        }
        //验证IP和端口是否可用
        checkServerAddress(ip,port);
    }

    /**
     * 验证ip和端口
     * @param ip
     * @param port
     */
    private void checkServerAddress(String ip,String port) {
        if (!isCanCheck) {
            MyTerminalFactory.getSDK().getThreadPool().execute(() -> {
                isCanCheck = true;
                if (JudgeWhetherConnect.isHostConnectable(ip,port)) {
                    isCanCheck = false;
                    TerminalFactory.getSDK().notifyReceiveHandler(ReceiverChangeServerHandler.class,ip,port,true);
                } else {
                    isCanCheck = false;
                    ToastUtil.showToast(getContext(), getString(R.string.text_connect_fail_please_input_corrent_ip_and_port));
                }
            });
        }
    }

    /**
     * 检查是否可以确定
     */
    private void checkButtonEnable(){
        tvSure.setEnabled((!TextUtils.isEmpty(contentIp)&&!TextUtils.isEmpty(contentPort)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
