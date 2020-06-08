package cn.vsx.vc.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopAllBusniessHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class SetFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_set_living_stop_time)
    TextView tvSetLivingStopTime;
    private Handler mHandler = new Handler(Looper.getMainLooper());

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
        initListener();
        return view;
    }

    /**
     * 初始化布局
     */
    private void initView() {
        ivReturn.setVisibility(View.VISIBLE);
        tvTitle.setText(getString(R.string.text_set));
        tvTitle.setPadding(0, 0, 0, 0);
        checkLoginState();
    }

    private void initListener() {
        MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateAllDataCompleteHandler);//更新数据
        MyTerminalFactory.getSDK().registReceiveHandler(receiverStopAllBusniessHandler);//退出
    }

    @OnClick({R.id.iv_return, R.id.iv_close,R.id.tv_set_infra_red,R.id.tv_set_living_stop_time, R.id.tv_set_server, R.id.tv_set_vpn,R.id.tv_app_list})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_return:
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.tv_set_infra_red:
                //红外设置
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_INFRA_RED,new Bundle());
                break;
            case R.id.tv_set_living_stop_time:
                //实时视频上报时长设置
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_SET_LIVING_STOP_TIME,new Bundle());
                break;
            case R.id.tv_set_server:
                //ip、端口号设置
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_SET_SERVER,new Bundle());
                break;
            case R.id.tv_set_vpn:
                //vpn设置
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            case R.id.tv_app_list:
                //应用列表
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_APP_LIST,new Bundle());
                break;

        }
    }

    /**
     * 根据登录状态显示不同的布局
     */
    private void checkLoginState() {
        if (TerminalFactory.getSDK().getAuthManagerTwo().isOnLine()) {
            //已经登录
            tvSetLivingStopTime.setVisibility(View.VISIBLE);
        }else{
            tvSetLivingStopTime.setVisibility(View.GONE);
        }
    }

    /**
     * 更新所有数据信息的消息
     */
    private ReceiveUpdateAllDataCompleteHandler receiveUpdateAllDataCompleteHandler = (errorCode, errorDesc) -> mHandler.post(() -> {
        if (errorCode == BaseCommonCode.SUCCESS_CODE) {
            checkLoginState();
        }
    });
    /**
     * 收到上报一切业务的通知
     */
    private ReceiverStopAllBusniessHandler receiverStopAllBusniessHandler = (showMessage) -> mHandler.post(() -> {
        checkLoginState();
    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateAllDataCompleteHandler);//更新数据
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverStopAllBusniessHandler);//退出
        mHandler.removeCallbacksAndMessages(null);
    }
}
