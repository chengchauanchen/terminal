package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.refresh.RefreshPresenter;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.view.IPoliceInfoView;
import com.vsxin.terminalpad.mvp.entity.DeviceBean;
import com.vsxin.terminalpad.mvp.entity.MemberInfoBean;
import com.vsxin.terminalpad.mvp.entity.TerminalBean;
import com.vsxin.terminalpad.mvp.ui.fragment.PoliceInfoFragment;
import com.vsxin.terminalpad.mvp.ui.fragment.TerminalInfoFragment;

import java.util.List;

/**
 * @author qzw
 * <p>
 * 地图气泡点击-民警详情页
 * 显示民警身上绑定的终端设备
 */
public class PoliceInfoPresenter extends RefreshPresenter<TerminalBean,IPoliceInfoView> {



    public PoliceInfoPresenter(Context mContext) {
        super(mContext);
    }

}
