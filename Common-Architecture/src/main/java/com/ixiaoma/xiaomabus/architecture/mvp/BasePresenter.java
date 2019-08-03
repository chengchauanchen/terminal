package com.ixiaoma.xiaomabus.architecture.mvp;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by win on 2018/4/28.
 */

public class BasePresenter<V extends IBaseView> implements IBasePresenter<V> {
    private V proxyView;
    private WeakReference<Context> weakContext;
    private WeakReference<V> weakView;//弱引用

    public BasePresenter(Context mContext) {
        this.weakContext = new WeakReference<Context>(mContext);
    }

    //绑定View
    @Override
    public void attachView(V view) {
        this.weakView = new WeakReference<V>(view);
        //动态代理 给View判空
        ClassLoader classLoader = view.getClass().getClassLoader();
        Class<?>[] interfaces = view.getClass().getInterfaces();
        MvpViewInvocationHandler mvpViewInvocationHandler = new MvpViewInvocationHandler(this.weakView.get());
        this.proxyView = (V) Proxy.newProxyInstance(classLoader, interfaces, mvpViewInvocationHandler);
    }

    //解绑View
    @Override
    public void detachView() {
        if (this.weakView != null) {
            this.weakView.clear();
            this.weakView = null;
        }
    }

    @Override
    public V getView() {
        return proxyView;
    }

    @Override
    public Context getContext() {
        return this.weakContext.get();
    }

    /**
     * 动态代理 hook 在调用View的方法前判空。
     */
    private class MvpViewInvocationHandler implements InvocationHandler {

        private IBaseView view;

        public MvpViewInvocationHandler(IBaseView view) {
            this.view = view;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (isAttachView())
                return method.invoke(view,args);
            return null;
        }
    }

    private boolean isAttachView() {
        return this.weakView != null && weakView.get() != null;
    }
}
