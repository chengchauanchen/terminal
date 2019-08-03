package com.vsxin.terminalpad.mvp.contract.presenter;

import android.content.Context;

import com.ixiaoma.xiaomabus.architecture.mvp.BasePresenter;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeConstans;
import com.vsxin.terminalpad.mvp.contract.constant.MemberTypeEnum;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;

import java.util.List;

/**
 * @author qzw
 * <p>
 * 主页地图
 */
public class MainMapPresenter extends BasePresenter<IMainMapView> {

    public MainMapPresenter(Context mContext) {
        super(mContext);
    }

    /**
     * 默认加载全部 图层
     */
    public void defaultLoadAllLayer(){
        List<MemberTypeEnum> memberTypeList = MemberTypeConstans.getMemberTypeList();
        for (MemberTypeEnum typeEnum : memberTypeList){
            getView().drawMapLayer(typeEnum.getType(),typeEnum.isCheck());
        }
    }
}
