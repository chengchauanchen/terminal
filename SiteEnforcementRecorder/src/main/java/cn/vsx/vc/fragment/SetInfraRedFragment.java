package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.model.InfraRedState;
import cn.vsx.vc.receiveHandle.ReceiverChangeInfraRedHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverUpdateInfraRedHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class SetInfraRedFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.rg_Orientation)
    RadioGroup rgOrientation;
    @Bind(R.id.rb_close)
    RadioButton rbClose;
    @Bind(R.id.rb_open)
    RadioButton rbOpen;
    @Bind(R.id.rb_automatic)
    RadioButton rbAutomatic;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static SetInfraRedFragment newInstance() {
        SetInfraRedFragment fragment = new SetInfraRedFragment();
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
        View view = inflater.inflate(R.layout.fragment_set_infra_red, container, false);
        ButterKnife.bind(this, view);
        initView();
        updateUI();
        initListener();
        return view;
    }
    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.tv_set_infra_red));

    }

    /**
     * 更新UI
     */
    private void updateUI() {
        int state = TerminalFactory.getSDK().getParam(Params.INFRA_RED_STATE, InfraRedState.CLOSE.getCode());
        if(state == InfraRedState.CLOSE.getCode()){
            rbClose.setChecked(true);
        }else if(state == InfraRedState.OPEN.getCode()){
            rbOpen.setChecked(true);
        }else if(state == InfraRedState.AUTO.getCode()){
            rbAutomatic.setChecked(true);
        }
    }

    /**
     * 初始化监听
     */
    private void initListener() {
        rgOrientation.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_close:
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverChangeInfraRedHandler.class, InfraRedState.CLOSE.getCode());
                    break;
                case R.id.rb_open:
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverChangeInfraRedHandler.class,InfraRedState.OPEN.getCode());
                    break;
                case R.id.rb_automatic:
                    MyTerminalFactory.getSDK().notifyReceiveHandler(ReceiverChangeInfraRedHandler.class,InfraRedState.AUTO.getCode());
                    break;
            }
        });
        MyTerminalFactory.getSDK().registReceiveHandler(receiverUpdateInfraRedHandler);
    }

    @OnClick({R.id.iv_return, R.id.iv_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
        }
    }

    /**
     * 更新红外状态的UI
     */
    private ReceiverUpdateInfraRedHandler receiverUpdateInfraRedHandler = new ReceiverUpdateInfraRedHandler() {
        @Override
        public void handler(boolean open) {
            mHandler.post(() -> updateUI());
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        mHandler.removeCallbacksAndMessages(null);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverUpdateInfraRedHandler);
    }
}
