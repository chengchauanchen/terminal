package com.vsxin.terminalpad.mvp.ui.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import me.jessyan.autosize.internal.CustomAdapt;

public class ArcgisWebView extends WebView implements CustomAdapt {

    private static final String TAG = "ArcgisWebView";

    public ArcgisWebView(Context context) {
        super(context);
    }

    public ArcgisWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        Log.i(TAG, "initView");
        this.requestFocus();
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setSupportZoom(false);
//        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setSaveFormData(false);
        this.getSettings().setSavePassword(false);
//        this.getSettings().setSupportZoom(false);
        this.getSettings().setUseWideViewPort(true);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setDomStorageEnabled(true); // 开启 DOM storage API 功能
        this.getSettings().setDatabaseEnabled(true);   //开启 database storage API 功能
        this.setVerticalScrollBarEnabled(false); //垂直不显示滚动条

        this.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);  //设置 缓存模式

        this.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i(TAG, "shouldOverrideUrlLoading=====request.getUrl()=" + url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.i(TAG, "shouldInterceptRequest=====request.getUrl()=" + url);
//                if (url.contains("init.js")) {
//                    return editResponse();
//                }
                return super.shouldInterceptRequest(view, url);

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.i(TAG, "shouldInterceptRequest=====request.getUrl()=" + request.getUrl().toString());
//                String url = request.getUrl().toString();
//                if (!TextUtils.isEmpty(url) && url.contains("init.js")) {
//                    return editResponse();
//                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //web_map.loadUrl(url);
            }
        });
    }

    /**
     * @return 本地jquery
     */
    private WebResourceResponse editResponse() {
        try {
            Log.i(TAG, "取本地jquery");
            return new WebResourceResponse("application/x-javascript", "utf-8", getContext().getAssets().open("init.js"));
        } catch (IOException e) {
            Log.i(TAG, "取本地jquery异常");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isBaseOnWidth() {
        return true;
    }

    @Override
    public float getSizeInDp() {
        return 540;
    }
}
