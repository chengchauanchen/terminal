package com.ixiaoma.xiaomabus.architecture.mvp.view.layout;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.viewgroup.ViewGroupMvpDelegateCallback;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.viewgroup.ViewGroupMvpDelegateImpl;

import org.apache.log4j.Logger;

/**
 * 以下代理是针对布局的代理 代理对象
 * 
 * 代理对象持有目标对象的引用
 * 
 * @author Dream
 *
 */
public abstract class MvpFrameLayout<V extends IBaseView, P extends IBasePresenter<V>>
		extends FrameLayout implements ViewGroupMvpDelegateCallback<V, P>,
		IBaseView {

	private ViewGroupMvpDelegateImpl<V, P> mvpDelegateImpl;

	private P presenter;
	
	private boolean isRetainInstance;

	public MvpFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MvpFrameLayout(Context context) {
		super(context);
	}

	public MvpFrameLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr){
		super(context, attrs, defStyleAttr);
	}

	private ViewGroupMvpDelegateImpl<V, P> getMvpDelegate() {
		if (mvpDelegateImpl == null) {
			mvpDelegateImpl = new ViewGroupMvpDelegateImpl<V, P>(this);
		}
		return mvpDelegateImpl;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getMvpDelegate().onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getMvpDelegate().onDetachedFromWindow();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		super.onSaveInstanceState();
		return getMvpDelegate().onSaveInstanceState();
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		getMvpDelegate().onRestoreInstanceState(state);
	}

	@Override
	public P getPresenter() {
		return this.presenter;
	}

	@Override
	public void setPresenter(P presenter) {
		this.presenter = presenter;
	}

	@Override
	public V getUI() {
		return (V) this;
	}

	@Override
	public void setRetainInstance(boolean retaionInstance) {
		this.isRetainInstance = retaionInstance;
	}

	@Override
	public boolean isRetainInstance() {
		return this.isRetainInstance;
	}

	@Override
	public boolean shouldInstanceBeRetained() {
		return this.isRetainInstance;
	}

	@Override
	public Parcelable superOnSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	public void superOnRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}

	@Override
	public Context getSuperContext() {
		return getContext();
	}

	@Override
	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}
}
