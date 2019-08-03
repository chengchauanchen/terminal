package com.vsxin.terminalpad.mvp.ui.fragment;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.ixiaoma.xiaomabus.architecture.mvp.lifecycle.MvpFragment;
import com.vsxin.terminalpad.R;
import com.vsxin.terminalpad.js.TerminalPadJs;
import com.vsxin.terminalpad.mvp.contract.presenter.MainMapPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.MainPresenter;
import com.vsxin.terminalpad.mvp.contract.presenter.NoticePresenter;
import com.vsxin.terminalpad.mvp.contract.view.IMainMapView;
import com.vsxin.terminalpad.mvp.contract.view.IMainView;
import com.vsxin.terminalpad.utils.HandleIdUtil;

import butterknife.BindView;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * @author qzw
 *
 * 小地图模块
 */
public class SmallMapFragment extends MvpFragment<IMainView, MainPresenter> implements IMainView {

    @BindView(R.id.web_small_map)
    WebView web_small_map;

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_small_map;
    }

    @Override
    protected void initViews(View view) {

//        "http://127.0.0.1:9011/offlineMap/indexPadMin.html?no=88752369"
        initWeb();

    }

    private void initWeb() {
        web_small_map.requestFocus();
        web_small_map.getSettings().setJavaScriptEnabled(true);
        web_small_map.getSettings().setSaveFormData(false);
        web_small_map.getSettings().setSavePassword(false);
        web_small_map.getSettings().setSupportZoom(false);
        web_small_map.getSettings().setUseWideViewPort(true);
        web_small_map.getSettings().setLoadWithOverviewMode(true);
        web_small_map.getSettings().setDomStorageEnabled(true); // 开启 DOM storage API 功能
        web_small_map.getSettings().setDatabaseEnabled(true);   //开启 database storage API 功能
        web_small_map.setVerticalScrollBarEnabled(false); //垂直不显示滚动条
        web_small_map.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress >= 100) {
                }
            }
        });


        web_small_map.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        String memberId = HandleIdUtil.handleId(MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0));
        Long memberUniqueno = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
        int depId =MyTerminalFactory.getSDK().getParam(Params.DEP_ID, 0);
        String format = String.format("no=%s", memberId);
        getLogger().info("http://192.168.1.187:9011/offlineMap/indexPadMin.html?"+format);
        web_small_map.loadUrl("http://192.168.1.187:9011/offlineMap/indexPadMin.html?"+format);
    }

    @Override
    protected void initData() {

    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter(getContext());
    }
}
