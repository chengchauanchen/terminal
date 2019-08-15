package com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.view;

import android.content.Context;
import android.os.Parcelable;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;

/**
 * 以下代理是针对布局的代理 目标对象：ViewGroupMvpDelegateImpl
 * 
 * @author Dream
 *
 * @param <V>
 * @param <P>
 */
public class ViewMvpDelegateImpl<V extends IBaseView, P extends IBasePresenter<V>>
		implements ViewMvpDelegate<V, P> {

	private ViewMvpDelegateCallback<V, P> delegateCallback;
	private OrientationChangeManager<V, P> orientationChangeManager;
	
	// 当前布局对应的id
	private int viewId;

	public ViewMvpDelegateImpl(
			ViewMvpDelegateCallback<V, P> delegateCallback) {
		if (this.delegateCallback == null) {
			throw new NullPointerException("delegateCallback is not null!");
		}
		this.delegateCallback = delegateCallback;
	}
	

	private OrientationChangeManager<V, P> getOrientationChangeManager() {
		if (this.orientationChangeManager == null) {
			this.orientationChangeManager = new OrientationChangeManager<V, P>();
		}
		return this.orientationChangeManager;
	}

	@Override
	public void onAttachedToWindow() {
		if (delegateCallback.isRetainInstance()) {
			// 当我们的布局创建时候，需不需要缓存数据
			P presenter = getOrientationChangeManager().getPresenter(viewId,
					delegateCallback.getSuperContext());
			if (presenter != null) {
				delegateCallback.setPresenter(presenter);
				presenter.attachView(delegateCallback.getUI());
				return;
			}
		}

		// 关联MVP
		P presenter = this.delegateCallback.getPresenter();
		if (presenter == null) {
			presenter = this.delegateCallback.createPresenter();
		}
		if (presenter == null) {
			// 布局子类没有指定相应的Presenter
			throw new NullPointerException("presenter is not null!");
		}
		//之前没有给presenter赋值
		delegateCallback.setPresenter(presenter);
		// 关联V和P
		presenter.attachView(delegateCallback.getUI());
	}

	@Override
	public void onDetachedFromWindow() {
		if (this.delegateCallback.isRetainInstance()) {
			Context context = delegateCallback.getSuperContext();
			// 判断一下缓存数据的Framgent有没有被暂停，或者被意外销毁释放
			boolean isDestroyed = getOrientationChangeManager()
					.willViewBeDestroyedPermanently(context);
			if (isDestroyed) {
				// 解除关联
				viewId = 0;
				//delegateCallback.getPresenter().dettachView();
				delegateCallback.getPresenter().detachView();

			} else {
				// 你的当前布局的状态有没有发现改变（例如：横竖屏切换，就会导致视图被干掉）
				boolean orientationChange = getOrientationChangeManager()
						.willViewBeDetachedBecauseOrientationChange(context);
				if (orientationChange) {
					// 发送了异常请求，意外退出，所以不需要删除之前的缓存数据
					// 缓存数据的Fragment调用了onStop方法，停止了
					// 发生改变就要解除关联
					delegateCallback.getPresenter().detachView();
				} else {
					// 我们要把之前的缓存数据删除掉
					// 没有发送异常，正常情况退出，清理数据缓存（避免垃圾数据，占用内存）
					getOrientationChangeManager().removePresenterAndViewState(
							viewId, context);
					viewId = 0;
					delegateCallback.getPresenter().detachView();
				}
			}
		} else {
			// 解除关联MVP
			// 解除V和P关联
			delegateCallback.getPresenter().detachView();
		}
		// 释放资源
		getOrientationChangeManager().cleanUp();
	}

	private Parcelable createInstance(Parcelable parcelable) {
		MvpSavedState mvpSavedState = new MvpSavedState(parcelable);
		mvpSavedState.setMosbyViewId(viewId);
		return mvpSavedState;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable instanceState = this.delegateCallback
				.superOnSaveInstanceState();
		if (delegateCallback.isRetainInstance()) {
			// 需要保存数据(所谓的保存是指：自己的数据)
			return createInstance(instanceState);
		}
		return instanceState;
	}

	/**
	 * 恢复MVP状态
	 * 
	 * @param savedState
	 */
	private void restoreMvpSavedState(MvpSavedState savedState) {
		this.viewId = savedState.getMosbyViewId();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		// 恢复状态（恢复数据）
		if (!(state instanceof MvpSavedState)) {
			// 调用父类方法(记得一定要调用)
			delegateCallback.superOnRestoreInstanceState(state);
			return;
		}
		MvpSavedState mvpSavedState = (MvpSavedState) state;
		restoreMvpSavedState(mvpSavedState);
		delegateCallback.superOnRestoreInstanceState(mvpSavedState);
	}

}
