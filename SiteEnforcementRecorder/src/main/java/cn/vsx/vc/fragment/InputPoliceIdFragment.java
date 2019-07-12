package cn.vsx.vc.fragment;

import android.os.Bundle;
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
import cn.com.cybertech.pdk.utils.DisplayUtil;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.InputMethodUtil;
import cn.vsx.vc.utils.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class InputPoliceIdFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.et_police_id)
    EditText etPoliceId;
    @Bind(R.id.bt_sure)
    TextView btSure;

    public static InputPoliceIdFragment newInstance() {
        InputPoliceIdFragment fragment = new InputPoliceIdFragment();
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
        View view = inflater.inflate(R.layout.fragment_input_police_id, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.GONE);
        tvTitle.setText(getString(R.string.text_bind_input));
        tvTitle.setPadding(DisplayUtil.dip2px(this.getContext(), 10), 0, 0, 0);
        btSure.setEnabled(false);
        etPoliceId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = s.toString().trim();
                btSure.setEnabled(!TextUtils.isEmpty(content));

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etPoliceId.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //关闭软键盘
                InputMethodUtil.hideInputMethod(this.getContext(), etPoliceId);
                bindPoliceID();
                return true;
            }
            return false;
        });

    }

    @OnClick({R.id.iv_close, R.id.bt_sure})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.bt_sure:
                //绑定
                InputMethodUtil.hideInputMethod(this.getContext(), etPoliceId);
                bindPoliceID();
                break;
        }
    }

    /**
     * 绑定警号
     */
    private void bindPoliceID(){
        String policeId = etPoliceId.getText().toString().trim();

        if(TextUtils.isEmpty(policeId)){
            ToastUtil.showToast(getContext(),getString(R.string.text_please_input_bind_police_id));
            return;
        }
        int accountNo  = DataUtil.stringToInt(policeId);
        if(accountNo == 0){
            ToastUtil.showToast(getContext(),getString(R.string.text_please_input_bind_police_id));
            return;
        }
        long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L);
        TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getRecorderBindManager().requestBind(accountNo,uniqueNo,0,""));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
