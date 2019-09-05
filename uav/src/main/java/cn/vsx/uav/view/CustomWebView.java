package cn.vsx.uav.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveChangePersonLocationHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.uav.utils.AirCraftUtil;
import cn.vsx.vc.utils.ToastUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/3/28
 * 描述：
 * 修订历史：
 */
public class CustomWebView extends WebView{

    private static final int UPDATE_AIRCRAFT_LOCATION = 0;
    private static final long UPDATE_DALAY = 15*1000L;
    private Logger logger = Logger.getLogger(CustomWebView.class);
    private double uvLng = 114.41588661389632;
    private double uvLat = 30.55199833333333;
    private double personLng = 114.41584253964803;
    private double personLat = 30.55562209282912;

    private String mapUrl = "http://192.168.1.225:9011/offlineMap/indexApp.html";
    public static String MAP_MIN_TYPE = "min";
    public static String MAP_MAX_TYPE = "max";
    private Handler mhandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                case UPDATE_AIRCRAFT_LOCATION:
                    String aircraftLocation = AirCraftUtil.getAircraftLocation();
                    double latitude = AirCraftUtil.getLatitude(aircraftLocation);
                    double longitude = AirCraftUtil.getLongitude(aircraftLocation);
                    if(latitude !=0.0 && longitude !=0.0){

                        uvLat = latitude;
                        uvLng = longitude;
                        updateUavLocation(uvLng,uvLat);
                    }
                    if(getVisibility() == VISIBLE){
                        mhandler.sendEmptyMessageDelayed(UPDATE_AIRCRAFT_LOCATION,UPDATE_DALAY);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public CustomWebView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        initWeb();
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(getVisibility() == VISIBLE){
            mhandler.sendEmptyMessage(UPDATE_AIRCRAFT_LOCATION);
        }
        TerminalFactory.getSDK().registReceiveHandler(receiveChangePersonLocationHandler);
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        TerminalFactory.getSDK().unregistReceiveHandler(receiveChangePersonLocationHandler);
        mhandler.removeCallbacksAndMessages(null);
    }

    public void initWeb(){
        WebSettings settings = getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false);
        }
        // 设置是否可以交互Javascript
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        //        // 设置允许JS弹窗
        //        settings.setJavaScriptCanOpenWindowsAutomatically(true);
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

        //保存表单数据
        settings.setSaveFormData(false);
        //启用地理定位
        settings.setGeolocationEnabled(true);
        //不显示webview缩放按钮
        settings.setDisplayZoomControls(false);
        requestFocus();
        //settings.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");     // enable Web Storage: localStorage, sessionStorage
        //开启 DOM storage API 功能
        //        settings.setDomStorageEnabled(false);
        //        settings.setAllowContentAccess(true);
        setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                } else {
                    view.loadUrl(request.toString());
                }
                return true;
            }
        });
        loadMap(MAP_MIN_TYPE,uvLng,uvLat,personLng,personLat);
    }

    private void loadMap(String type,double uv_lng,double uv_lat,double person_lng,double person_lat){
        String url = TerminalFactory.getSDK().getParam(Params.LOCATION_URL, "")+"?type="+type+"&u_lng="+uv_lng+"&u_lat="+uv_lat+"&lng="+person_lng+"&lat="+person_lat;
        logger.info("加载无人机地址" + url);
        if(!TextUtils.isEmpty(url)){
            clearCache(true);
            loadUrl(url);
        }else{
            ToastUtil.showToast(getContext(), "获取无人机地图失败，无法查看!");
        }
    }

    /**
     * 刷新地图（地图放大和缩小时）
     * @param type
     */
    public void refreshMap(String type){
        loadMap(type,uvLng,uvLat,personLng,personLat);
    }

    /**
     * 更新飞手位置
     * @param lng
     * @param lat
     */
    public void updatePersonLocation(double lng,double lat){
        //调用js代码
        evaluateJavascript("javascript:uav_service.personnel("+lng+","+lat+")", new ValueCallback<String>(){
            @Override
            public void onReceiveValue(String value){
                logger.info("调用js方法更新飞手位置--"+"lng:"+lng+",lat:"+lat);
                //此处为 js 返回的结果
            }
        });
    }

    /**
     * 更新无人机位置
     * @param lng
     * @param lat
     */
    public void updateUavLocation(final double lng,final double lat){
        //调用js代码
        evaluateJavascript("javascript:uav_service.UAV("+lng+","+lat+")", new ValueCallback<String>(){
            @Override
            public void onReceiveValue(String value){
                logger.info("调用js方法更新无人机位置--"+"lng:"+lng+",lat:"+lat);
                //此处为 js 返回的结果
            }
        });
    }

    private ReceiveChangePersonLocationHandler receiveChangePersonLocationHandler = (latitude, Longitude) -> {
        personLng = Longitude;
        personLat = latitude;
        if(personLng !=0.0 && personLat!= 0.0){
            if(getVisibility() == VISIBLE){
                mhandler.post(() -> updatePersonLocation(Longitude,latitude));
            }
        }
    };
}
