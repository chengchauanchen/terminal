package com.vsxin.terminalpad.net;

import android.app.Activity;
import android.content.Context;
import com.ixiaoma.xiaomabus.architecture.exception.CustomException;
import com.vsxin.terminalpad.mvp.ui.widget.ProgressDialog;
import com.vsxin.terminalpad.utils.NetworkUtil;
import java.net.ConnectException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by qiuzhiwen on 2018/11/19.
 */

public abstract class RxObserver<T> implements Observer<T> {
    private final Context context;
    private boolean isLoading = true;
    private Disposable mDisposable;

    private ProgressDialog myProgressDialog;


    /**
     * @param context
     * @param isLoading 是否显示加载框
     */
    public RxObserver(Context context, boolean isLoading) {
        this.context = context;
        this.isLoading = isLoading;
    }

    private void createProgressDialog(){
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setCancelable(false);
        }
    }


    @Override
    public void onSubscribe(Disposable mDisposable) {
        this.mDisposable = mDisposable;
        if (isLoading && context instanceof Activity) {
            if (context != null && !((Activity) context).isFinishing()) {
                createProgressDialog();
            }
        }
    }


    @Override
    public void onNext(T t) {
        if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
            if (myProgressDialog != null) {
                myProgressDialog.cancel();
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof TimeoutException
                || e instanceof ConnectException) {
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                if (myProgressDialog != null) {
                    myProgressDialog.cancel();
                }
                //ToastUtils.show("连接超时，请稍后再试");
            }
        } else if (!NetworkUtil.isConnected(context)) {
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                if (myProgressDialog != null) {
                    myProgressDialog.cancel();
                }
                //ToastUtils.show("亲，断网了，请检查你的网络");
            }
        } else if (e instanceof CustomException) {
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                if (myProgressDialog != null) {
                    myProgressDialog.cancel();
                }
//                if (!TextUtils.isEmpty(e.getMessage())) {
//                    ToastUtils.show(e.getMessage());
//                }
            }
        } else {
            if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
                if (myProgressDialog != null) {
                    myProgressDialog.cancel();
                }
            }
        }
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onComplete() {
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        if (context != null && context instanceof Activity && !((Activity) context).isFinishing()) {
            if (myProgressDialog != null) {
                myProgressDialog.cancel();
            }
        }
    }
}
