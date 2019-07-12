package cn.vsx.vc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.utils.Constants;

/**
 * Created by gt358 on 2017/10/20.
 */

public class SetFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;

    public static SetFragment newInstance() {
        SetFragment fragment = new SetFragment();
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
        View view = inflater.inflate(R.layout.fragment_set, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_set));
        tvTitle.setPadding(0, 0, 0, 0);
    }

    @OnClick({R.id.iv_return, R.id.iv_close, R.id.tv_set_server, R.id.tv_set_vpn})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.tv_set_server:
                //ip、端口号设置
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_SET_SERVER);
                break;
            case R.id.tv_set_vpn:
                //vpn设置
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
