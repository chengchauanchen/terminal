package cn.vsx.vc.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.cybertech.pdk.utils.DisplayUtil;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.RecorderBindBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopAllBusniessHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.NfcUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class BindFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;

    @Bind(R.id.ll_unbind)
    LinearLayout llUnbind;
    @Bind(R.id.tv_bind_info)
    TextView tvBindInfo;


    @Bind(R.id.ll_bind_nfc)
    LinearLayout llBindNfc;
    @Bind(R.id.bt_open_nfc)
    TextView btOpenNfc;
    @Bind(R.id.ll_opened_nfc)
    LinearLayout llOpenedNfc;
    @Bind(R.id.iv_nfc_state)
    ImageView ivNfcState;
    @Bind(R.id.tv_nfc_state)
    TextView tvNfcState;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static BindFragment newInstance(){
        BindFragment fragment = new BindFragment();
        Bundle args = new Bundle();
//        args.putInt(TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
//            jumpType = getArguments().getInt(TYPE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bind, container, false);
        ButterKnife.bind(this, view);
        initListener();
        return view;
    }

    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);//更新数据
        MyTerminalFactory.getSDK().registReceiveHandler(receiverStopAllBusniessHandler);//退出
    }

    /**
     * 初始化布局
     */
    private void initView() {
        RecorderBindBean bean = DataUtil.getRecorderBindBean();
        if(bean == null){
            ivReturn.setVisibility(View.GONE);
            tvTitle.setText(getString(R.string.text_bind_way));
            tvTitle.setPadding(DisplayUtil.dip2px(this.getContext(),10),0,0,0);
            llUnbind.setVisibility(View.GONE);
            tvBindInfo.setText("");
        }else{
            ivReturn.setVisibility(View.VISIBLE);
            tvTitle.setText(getString(R.string.text_unbind_switch));
            tvTitle.setPadding(0,0,0,0);
            llUnbind.setVisibility(View.VISIBLE);
            tvBindInfo.setText(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)+"");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
        setNFCStateView();
    }

    /**
     * 设置NFC开关状态
     */
    private void setNFCStateView() {
        llBindNfc.setVisibility(View.VISIBLE);
        llBindNfc.setEnabled(true);
        btOpenNfc.setVisibility(View.GONE);
        llOpenedNfc.setVisibility(View.GONE);
        int nfcState = NfcUtil.nfcCheck(this.getContext());
        switch (nfcState){
            case NfcUtil.NFC_ENABLE_NONE:
                llBindNfc.setVisibility(View.GONE);
                llOpenedNfc.setVisibility(View.VISIBLE);
                ivNfcState.setImageResource(R.drawable.icon_load_result_error);
                tvNfcState.setText(R.string.text_nfc_state_error);
                break;
            case NfcUtil.NFC_ENABLE_FALSE_NONE:
                llBindNfc.setVisibility(View.GONE);
                llBindNfc.setEnabled(false);
                llOpenedNfc.setVisibility(View.VISIBLE);
                ivNfcState.setImageResource(R.drawable.icon_load_result_error);
                tvNfcState.setText(R.string.text_has_no_nfc);
                break;
            case NfcUtil.NFC_ENABLE_FALSE_JUMP:
                btOpenNfc.setVisibility(View.VISIBLE);
                break;
            case NfcUtil.NFC_ENABLE_FALSE_SHOW:
                llOpenedNfc.setVisibility(View.VISIBLE);
                ivNfcState.setImageResource(R.drawable.ico_openbinding_nfc);
                tvNfcState.setText(R.string.text_nfc_opened);
                break;
        }
    }

    @OnClick({R.id.iv_return,R.id.iv_close,R.id.bt_unbind,R.id.ll_bind_nfc,R.id.ll_bind_qr_scan,R.id.ll_bind_input})
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_return:
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.bt_unbind:
                //解绑
                TerminalFactory.getSDK().getThreadPool().execute(() -> TerminalFactory.getSDK().getRecorderBindManager().requestUnBind());
                break;
            case R.id.ll_bind_nfc:
                //判断是否打开了NFC
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_NFC);
                break;
            case R.id.ll_bind_qr_scan:
                //显示打开生成二维码页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_QR);
                break;
            case R.id.ll_bind_input:
                //显示打开输入警号页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_INPUT);
                break;
        }
    }

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = (errorCode, errorDesc) -> mHandler.post(() -> {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {
            initView();
        }
    });
    /**
     * 收到上报一切业务的通知
     */
    private ReceiverStopAllBusniessHandler receiverStopAllBusniessHandler = (showMessage) -> mHandler.post(this::initView);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);//更新数据
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverStopAllBusniessHandler);//退出
    }

}
