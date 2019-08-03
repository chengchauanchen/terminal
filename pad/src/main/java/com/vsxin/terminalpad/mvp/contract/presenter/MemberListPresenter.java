package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMemberInfoView;
import com.vsxin.terminalpad.mvp.contract.view.IMemberListView;

/**
 * @author qzw
 *
 * 地图气泡点击-成员详情页
 */


public class MemberListPresenter extends BasePresenter<IMemberListView> {

    public MemberListPresenter(Context mContext) {
        super(mContext);
    }


}
