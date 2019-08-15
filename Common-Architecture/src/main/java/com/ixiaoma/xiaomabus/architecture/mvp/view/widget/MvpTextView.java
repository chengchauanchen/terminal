package com.ixiaoma.xiaomabus.architecture.mvp.view.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.IBasePresenter;
import com.ixiaoma.xiaomabus.architecture.mvp.IBaseView;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.viewgroup.ViewGroupMvpDelegateCallback;
import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.viewgroup.ViewGroupMvpDelegateImpl;

@SuppressLint("AppCompatCustomView")
public abstract class MvpTextView<V extends IBaseView, P extends IBasePresenter<V>>
		extends TextView implements ViewGroupMvpDelegateCallback<V, P>,
		IBaseView {

	private ViewGroupMvpDelegateImpl<V, P> mvpDelegateImpl;

	private P presenter;

	private boolean isRetainInstance;

	public MvpTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MvpTextView(Context context) {
		super(context);
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
	public Parcelable onSaveInstanceState() {
		super.onSaveInstanceState();
		return getMvpDelegate().onSaveInstanceState();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
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

}
