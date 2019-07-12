package cn.vsx.vc.fragment;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.cybertech.pdk.utils.DisplayUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.utils.NfcUtil;

/**
 * Created by gt358 on 2017/10/20.
 */

public class NFCFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.rl_nfc_no_open)
    RelativeLayout rlNfcNoOpen;
    @Bind(R.id.tv_nfc_state)
    TextView tvNfcState;
    @Bind(R.id.bt_open_nfc)
    TextView btOpenNfc;
    @Bind(R.id.iv_nfc)
    ImageView ivNfc;
    @Bind(R.id.rl_nfc_opened)
    LinearLayout rlNfcOpened;

    private AnimationDrawable aDrawable ;

    public static NFCFragment newInstance() {
        NFCFragment fragment = new NFCFragment();
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
        View view = inflater.inflate(R.layout.fragment_nfc, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.GONE);
        tvTitle.setText(getString(R.string.text_bind_nfc));
        tvTitle.setPadding(DisplayUtil.dip2px(this.getContext(), 10), 0, 0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        setNFCStateView();
    }

    /**
     * 设置NFC开关状态
     */
    private void setNFCStateView() {
        rlNfcNoOpen.setVisibility(View.GONE);
        rlNfcOpened.setVisibility(View.GONE);
        if(aDrawable!=null){
            aDrawable.stop();
        }
        int nfcState = NfcUtil.nfcCheck(this.getContext());
        switch (nfcState){
            case NfcUtil.NFC_ENABLE_NONE:
                rlNfcNoOpen.setVisibility(View.VISIBLE);
                tvNfcState.setText(getString(R.string.text_nfc_state_error));
                btOpenNfc.setVisibility(View.INVISIBLE);
                btOpenNfc.setEnabled(false);
                break;
            case NfcUtil.NFC_ENABLE_FALSE_NONE:
                rlNfcNoOpen.setVisibility(View.VISIBLE);
                tvNfcState.setText(getString(R.string.text_has_no_nfc));
                btOpenNfc.setVisibility(View.INVISIBLE);
                btOpenNfc.setEnabled(false);
                break;
            case NfcUtil.NFC_ENABLE_FALSE_JUMP:
                rlNfcNoOpen.setVisibility(View.VISIBLE);
                tvNfcState.setText(getString(R.string.text_nfc_is_not_opened));
                btOpenNfc.setVisibility(View.VISIBLE);
                btOpenNfc.setEnabled(true);
                break;
            case NfcUtil.NFC_ENABLE_FALSE_SHOW:
                rlNfcOpened.setVisibility(View.VISIBLE);
                aDrawable = (AnimationDrawable)ivNfc.getDrawable();
                aDrawable.start();
                break;
        }
    }

    @OnClick({R.id.iv_close, R.id.bt_open_nfc})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.bt_open_nfc:
                //打开NFC
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        if(aDrawable!=null){
            aDrawable.stop();
        }
    }
}
