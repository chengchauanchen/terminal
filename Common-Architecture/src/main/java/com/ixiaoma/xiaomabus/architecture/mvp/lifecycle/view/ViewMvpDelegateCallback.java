package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.view;

import android.content.Context;
import android.os.Parcelable;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.IMvpView;

/**
 * 针对ViewGroup集成MVP代理 目标接口
 *
 * @param <V>
 * @param <P>
 * @author Dream
 */
public interface ViewMvpDelegateCallback<V extends IBaseView, P extends IBasePresenter<V>>
        extends IMvpView<V, P> {
    /**
     * 保存布局实例状态（这里是指布局相关数据）
     *
     * @return
     */
    Parcelable superOnSaveInstanceState();

    /**
     * 恢复布局实例状态
     *
     * @param state
     */
    void superOnRestoreInstanceState(Parcelable state);

    Context getSuperContext();

    /**
     * 是否保存数据
     * @param retaionInstance
     */
    void setRetainInstance(boolean retaionInstance);

    boolean isRetainInstance();

    /**
     * 判断是否保存数据(该方法还会处理横竖屏切换)
     * @return
     */
    boolean shouldInstanceBeRetained();
}
