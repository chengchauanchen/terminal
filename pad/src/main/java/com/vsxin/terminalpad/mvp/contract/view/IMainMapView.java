package com.vsxin.terminalpad.mvp.contract.view;


import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * @author qzw
 *
 * 主页地图
 */
public interface IMainMapView extends IBaseView {

    void drawMapLayer(int type, boolean isShow);
}
