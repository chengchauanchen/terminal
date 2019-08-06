package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Bundle;

import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.presenter.PersonMessagePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IPersonMessageView;

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


}
