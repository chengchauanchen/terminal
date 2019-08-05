package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.vsxin.terminalpad.mvp.contract.view.IPersonMessageView;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/5
 * 描述：
 * 修订历史：
 */
public class PersonMessagePresenter extends BaseMessagePresenter<IPersonMessageView>{
    public PersonMessagePresenter(Context mContext){
        super(mContext);
    }
}
