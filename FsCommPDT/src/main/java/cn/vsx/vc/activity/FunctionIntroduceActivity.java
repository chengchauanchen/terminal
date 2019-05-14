package cn.vsx.vc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import butterknife.Bind;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

public class FunctionIntroduceActivity extends FragmentActivity{

    WebView webView;
    LinearLayout ll_pb;
    ProgressBar pb_refresh;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_introduce);
        webView = findViewById(R.id.webview);
        ll_pb = findViewById(R.id.ll_pb);
        pb_refresh = findViewById(R.id.pb_refresh);
        initData();
    }

    public void initData(){
        WebSettings settings = webView.getSettings();
        // 设置是否可以交互Javascript
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        // 设置允许JS弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //设置WebView加载页面文本内容的编码，默认“UTF-8”。
        settings.setDefaultTextEncodingName("utf-8");
        //支持屏幕缩放
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        //设置显示模式
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        //是否任意比例缩放
        settings.setUseWideViewPort(true);
        //缩放至屏幕大小
        settings.setLoadWithOverviewMode(true);
        //是否保存密码
        settings.setSavePassword(true);
        //保存表单数据
        settings.setSaveFormData(false);
        //启用地理定位
        settings.setGeolocationEnabled(true);
        //不显示webview缩放按钮
        settings.setDisplayZoomControls(false);
        webView.requestFocus();
        webView.addJavascriptInterface(new JsInterface(),"jsObj");
        String help_word_url;
        String introduceUrl = MyTerminalFactory.getSDK().getParam(Params.REQRECOMMANDURL, "");
        String nightIntroduceUrl = MyTerminalFactory.getSDK().getParam(Params.REQRECOMMANDNIGHTURL, "");
        if(MyTerminalFactory.getSDK().getParam(Params.DAYTIME_MODE,false)){
            help_word_url = introduceUrl;
        }else {
            help_word_url = nightIntroduceUrl;
        }
        if(TextUtils.isEmpty(help_word_url)){
            ToastUtil.showToast(this,getString(R.string.text_access_function_introduction_connection_failure));
        }else {
            webView.setEnabled(false);
            ll_pb.setVisibility(View.VISIBLE);
            webView.loadUrl(help_word_url);
            webView.setOnKeyListener((v, keyCode, event) -> {
                if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
                    webView.goBack();
                    return true;
                }
                return false;
            });
            webView.setWebViewClient(new WebViewClient(){

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url){
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url){
                    if(ll_pb !=null){
                        ll_pb.setVisibility(View.GONE);
                    }
                    if(webView !=null){
                        webView.setEnabled(true);
                    }
                }
            });
        }
    }

    public class JsInterface{
        //关闭界面
        @JavascriptInterface
        public void closeWebView(){
            runOnUiThread(() -> finish());
        }
    }

}
