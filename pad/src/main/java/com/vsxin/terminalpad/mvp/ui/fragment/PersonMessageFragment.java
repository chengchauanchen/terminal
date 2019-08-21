package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.PersonMessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IPersonMessageView;
import com.vsxin.terminalpad.mvp.ui.widget.ChooseDevicesDialog;

import cn.vsx.hamster.common.Authority;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/3
 * 描述：
 * 修订历史：
 */
public class PersonMessageFragment extends MessageBaseFragment<IPersonMessageView, PersonMessagePresenter> implements IPersonMessageView{

    @Override
    protected int getLayoutResID(){
        return R.layout.activity_individual_news;
    }

    public static PersonMessageFragment newInstance(int userId, String userName){
        Bundle args = new Bundle();
        args.putInt("userId", userId);
        args.putString("userName", userName);
        args.putBoolean("isGroup", false);
        PersonMessageFragment fragment = new PersonMessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public PersonMessagePresenter createPresenter(){
        return new PersonMessagePresenter(getContext());
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        view.findViewById(R.id.iv_call).setOnClickListener(v -> {
            //
            getPresenter().goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PHONE);
        });
        view.findViewById(R.id.individual_news_phone).setOnClickListener(v -> {
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                ToastUtil.showToast(this.getContext(), getString(R.string.text_no_call_permission));
            }else{
                getPresenter().goToChooseDevices(ChooseDevicesDialog.TYPE_CALL_PRIVATE);
            }
        });
        view.findViewById(R.id.individual_news_info).setOnClickListener(v -> {

        });

    }
}
