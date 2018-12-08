package cn.vsx.vc.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.vsx.vc.R;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * 定位的界面
 * Created by gt358 on 2017/9/14.
 */

@SuppressLint("ValidFragment")
public class LocationFragment extends Fragment{
    @Bind(R.id.wv_help)
    WebView wv_help;
    @Bind(R.id.ll_pb)
    LinearLayout ll_pb;

    @Bind(R.id.ll_top_bar)
    LinearLayout ll_top_bar;
    @Bind(R.id.iv_back_face)
    ImageView iv_back_face;
    @Bind(R.id.tv_name_face)
    TextView tv_name_face;

    private FrameLayout frameLayout;
    String url;
    private boolean isLocation;
    private String name;

    private Logger logger = Logger.getLogger(getClass());
    private Handler myHandler = new Handler();

    public static LocationFragment getInstance(String url, String name, boolean isLocation){
        LocationFragment lLocationFragment = new LocationFragment();
        Bundle bundle = new Bundle();
        bundle.putCharSequence("url", url);
        bundle.putBoolean("isLocation", isLocation);
        bundle.putCharSequence("name", name);
        lLocationFragment.setArguments(bundle);
        return lLocationFragment;
    }

    public void setFrameLayout(FrameLayout frameLayout){
        this.frameLayout = frameLayout;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        final int uid = android.os.Process.myUid();
        logger.error("本应用是否为系统级应用" + (uid == android.os.Process.SYSTEM_UID));
        if(uid == android.os.Process.SYSTEM_UID){
            hookWebView();
        }
        View mRootView = inflater.inflate(getContentViewId(), container, false);
        ButterKnife.bind(this, mRootView);//绑定framgent
        initView();
        return mRootView;
    }

    public int getContentViewId(){
        return R.layout.help_word;
    }

    public void initView(){
        url = getArguments().getString("url");
        isLocation = getArguments().getBoolean("isLocation");
        name = getArguments().getString("name");
        if(!TextUtils.isEmpty(name)){
            ll_top_bar.setVisibility(View.VISIBLE);
            tv_name_face.setText(name);
            iv_back_face.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    frameLayout.setVisibility(View.GONE);
                    getFragmentManager().popBackStack();
                }
            });
        }
        WebSettings settings = wv_help.getSettings();
        // 设置是否可以交互Javascript
        settings.setJavaScriptEnabled(true);
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
        //settings.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");     // enable Web Storage: localStorage, sessionStorage
        //开启 DOM storage API 功能
        settings.setDomStorageEnabled(false);
        //设置是否获得焦点
        wv_help.requestFocus();
        logger.info("加载定位的地址" + url);
        if(!TextUtils.isEmpty(url)){
            wv_help.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageFinished(WebView view, String url){
                    myHandler.post(new Runnable(){
                        @Override
                        public void run(){
                            try{
                                ll_pb.setVisibility(View.GONE);
                                wv_help.setEnabled(true);
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            wv_help.setEnabled(false);
            ll_pb.setVisibility(View.VISIBLE);
            wv_help.loadUrl(url);
        }else{
            if(isLocation){
                ToastUtil.showToast(getActivity(), "定位失败！");
            }else{
                ToastUtil.showToast(getActivity(), "获取人脸识别信息失败！");
            }
        }
    }

    private void hookWebView(){
        Class<?> factoryClass = null;
        try{
            factoryClass = Class.forName("android.webkit.WebViewFactory");
            Method getProviderClassMethod = null;
            Object sProviderInstance = null;
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            if(field.get(null) == null){
                if(Build.VERSION.SDK_INT == 23){
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
                    getProviderClassMethod.setAccessible(true);
                    Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                    Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                    if(constructor != null){
                        constructor.setAccessible(true);
                        Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                        constructor2.setAccessible(true);
                        sProviderInstance = constructor.newInstance(constructor2.newInstance());
                    }
                }else if(Build.VERSION.SDK_INT == 22){
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                    getProviderClassMethod.setAccessible(true);
                    Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                    Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                    if(constructor != null){
                        constructor.setAccessible(true);
                        Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                        constructor2.setAccessible(true);
                        sProviderInstance = constructor.newInstance(constructor2.newInstance());
                    }
                }else if(Build.VERSION.SDK_INT == 21){//Android 21无WebView安全限制
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                    getProviderClassMethod.setAccessible(true);
                    Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    sProviderInstance = providerClass.newInstance();
                }
                if(sProviderInstance != null){
                    field.set("sProviderInstance", sProviderInstance);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        if(wv_help != null){
            wv_help.stopLoading();
            wv_help.setVisibility(View.GONE);
            wv_help.destroy();
        }
        super.onDestroy();
    }
}
