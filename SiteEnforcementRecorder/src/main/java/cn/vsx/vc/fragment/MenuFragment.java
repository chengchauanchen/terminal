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
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateAllDataCompleteHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.receiveHandle.ReceiverFragmentPopBackStackHandler;
import cn.vsx.vc.receiveHandle.ReceiverFragmentShowHandler;
import cn.vsx.vc.receiveHandle.ReceiverStopAllBusniessHandler;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by gt358 on 2017/10/20.
 */

public class MenuFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.iv_return)
    ImageView ivReturn;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_group_info)
    TextView tvGroupInfo;
    @Bind(R.id.ll_change_group)
    LinearLayout llChangeGroup;
    @Bind(R.id.ll_bind)
    LinearLayout llBind;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
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
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        ButterKnife.bind(this, view);
        initListener();
        initView();
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
        ivReturn.setVisibility(View.GONE);
        tvTitle.setText(getString(R.string.text_menu));
        tvTitle.setPadding(DisplayUtil.dip2px(this.getContext(), 10), 0, 0, 0);
        checkLoginState();
    }

    /**
     * 根据登录状态显示不同的布局
     */
    private void checkLoginState() {
        if (TerminalFactory.getSDK().getAuthManagerTwo().isOnLine()) {
            //已经登录
            llChangeGroup.setVisibility(View.VISIBLE);
            llBind.setVisibility(View.VISIBLE);
            //组信息
            Group group = DataUtil.getGroupByGroupNo(TerminalFactory.getSDK().getParam(Params.CURRENT_GROUP_ID, 0));
            if (group != null) {
                tvGroupInfo.setText(group.getName());
            }
        }else{
            llChangeGroup.setVisibility(View.GONE);
            llBind.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.iv_close, R.id.ll_change_group, R.id.ll_bind, R.id.ll_set})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                //关闭页面
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentPopBackStackHandler.class);
                break;
            case R.id.ll_change_group:
                //转组
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_GROUP_CHANGE);
                break;
            case R.id.ll_bind:
                //绑定、解绑
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_BIND);
                break;
            case R.id.ll_set:
                //设置
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverFragmentShowHandler.class, Constants.FRAGMENT_TAG_SET);
                break;
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
