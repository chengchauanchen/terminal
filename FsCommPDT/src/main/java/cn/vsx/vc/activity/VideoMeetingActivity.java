package cn.vsx.vc.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingDataBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNetworkChangeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyAddVideoMeetingMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.utils.ToastUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import ptt.terminalsdk.context.MyTerminalFactory;

public class VideoMeetingActivity extends BaseActivity {
  protected Logger logger = Logger.getLogger(this.getClass());
  private final String TAG = this.getClass().getName();

  private WebView wvVideoMeeting;
  private LinearLayout mLlNoNetwork;

  private long roomId;

  @SuppressWarnings("HandlerLeak")
  private Handler mHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    roomId = getIntent().getLongExtra(Constants.ROOM_ID, 0L);
    if (roomId <= 0) {
      ToastUtil.showToast(MyApplication.instance,
          getString(R.string.text_video_meeting_data_error));
      finish();
      return;
    }
    super.onCreate(savedInstanceState);
    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
  }

  @Override
  public int getLayoutResId() {
    return R.layout.activity_video_meeting;
  }

  @Override
  protected void setOrientation() {
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
  }

  @Override
  public void initView() {
    wvVideoMeeting = (WebView) findViewById(R.id.wv_video_meeting);
    mLlNoNetwork = findViewById(R.id.ll_no_network);
    mLlNoNetwork.setVisibility(View.GONE);
    initWeb();
  }

  @Override
  public void initListener() {
    MyTerminalFactory.getSDK().registReceiveHandler(receiveNetworkChangeHandler);
    MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void initData() {
  }

  /**
   * 初始化webview
   */
  private void initWeb() {
    wvVideoMeeting .setBackgroundColor((Color.parseColor("#000000")));
    WebSettings settings = wvVideoMeeting.getSettings();
    // 设置是否可以交互Javascript
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    // 设置允许JS弹窗
    settings.setJavaScriptCanOpenWindowsAutomatically(true);
    //设置WebView加载页面文本内容的编码，默认“UTF-8”。
    settings.setDefaultTextEncodingName("utf-8");
    //当webview调用requestFocus时为webview设置节点
    settings.setNeedInitialFocus(true);
    //支持屏幕缩放
    settings.setSupportZoom(false);
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
    settings.setSaveFormData(true);
    //启用地理定位
    settings.setGeolocationEnabled(true);
    //不显示webview缩放按钮
    settings.setDisplayZoomControls(false);

    settings.setPluginState(WebSettings.PluginState.ON);
    settings.setDatabaseEnabled(true);// 数据库缓存
    settings.setAppCacheEnabled(true);// 打开缓存
    settings.setAllowFileAccess(true);  //设置可以访问文件
    settings.setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
    settings.setRenderPriority(WebSettings.RenderPriority.HIGH);// 提高渲染级别
    settings.setLoadsImagesAutomatically(true);// 自动加载网络图片
    // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
    settings.setAllowFileAccessFromFileURLs(true);
    // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
    settings.setAllowUniversalAccessFromFileURLs(true);
    // 支持
    settings.setCacheMode(WebSettings.LOAD_DEFAULT);
    //多窗口
    settings.supportMultipleWindows();
    settings.setBlockNetworkImage(false); // 解决图片不显示
    settings.setMediaPlaybackRequiresUserGesture(false); // 自动播放
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ){
      settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }
    //设置是否获得焦点
    wvVideoMeeting.requestFocus();
    wvVideoMeeting.addJavascriptInterface(new JsInterface(),"android");
    CookieManager cookieManager = CookieManager.getInstance();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cookieManager.setAcceptThirdPartyCookies(wvVideoMeeting,true);
    }
    String httpsIp = TerminalFactory.getSDK().getParam(Params.HTTPS_IP, "");
    int httpsPort = TerminalFactory.getSDK().getParam(Params.HTTPS_PORT, 0);
    long uniqueNo = MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0L);
    //roomId = 1228641337463099393L;
    String url = "https://" + httpsIp + ":" + httpsPort + "/m/#/app?roomId="+roomId+"&uniqueNo="+uniqueNo;
    //String url = "https://106.12.14.136:18086/m/#/app?roomId="+roomId+"&uniqueNo="+uniqueNo;
    //String url = "https://106.12.14.136:18086/m/#/app?uniqueNo=305552138073800704&roomId=1228641337463099393";
    //String url = "https://www.baidu.com";

    logger.info("加载视频会议的地址" + url);
    if (!TextUtils.isEmpty(url)) {
      wvVideoMeeting.setWebViewClient(new WebViewClient() {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          logger.debug("转到:"+url);
          view.loadUrl(url);
          return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
          if (handler != null) {
            handler.proceed();//忽略证书的错误继续加载页面内容，不会变成空白页面
          }
        }
      });
      wvVideoMeeting.setWebChromeClient(new WebChromeClient(){

        @Override public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
          //return super.onConsoleMessage(consoleMessage);
          //logger.info(TAG+"--onConsoleMessage-message:"+consoleMessage.message());
          //logger.info(TAG+"--onConsoleMessage-From-line::"+consoleMessage.lineNumber());
          //logger.info(TAG+"--onConsoleMessage-of:"+consoleMessage.sourceId());
          return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
          logger.info(TAG+"--onJsAlert--message:"+message);
          result.confirm();
          return true;
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
            FileChooserParams fileChooserParams) {
          return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }

        @Override public void onPermissionRequest(PermissionRequest request) {
          logger.info(TAG+"--onPermissionRequest--");
          myHandler.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              request.grant(request.getResources());
            }
          });
        }
      });
      wvVideoMeeting.loadUrl(url);
    } else {
      //ptt.terminalsdk.tools.ToastUtil.showToast(this, getString(R.string.text_get_help_word_fail_please_restart_app));
    }

    /*
     * 监听手机返回按键，点击返回H5就返回上一级
     */
    wvVideoMeeting.setOnKeyListener((v, keyCode, event) -> {
      if ((keyCode == KeyEvent.KEYCODE_BACK) && wvVideoMeeting.canGoBack()) {
        wvVideoMeeting.goBack();
        return true;
      }
      return false;
    });
  }


  /**
   * 通知终端加入视频会商会议室
   */
  private ReceiveNotifyAddVideoMeetingMessageHandler receiveNotifyAddVideoMeetingMessageHandler = (notifyMessage) -> {
    //1.保存到数据库中，2.从数据库中查到所有正在会议的和时间最近的一条消息，3.刷新UI
    try{
      TerminalFactory.getSDK().getThreadPool().execute(() -> {
        if(notifyMessage!=null){
          if(notifyMessage.getRoomId() == roomId&&!notifyMessage.getAddOrOutMeeting()){
            VideoMeetingDataBean bean =  JSONObject.parseObject(notifyMessage.getMeetingDescribe(), VideoMeetingDataBean.class);
            if(bean!=null&&bean.getStatus() == 2){
              ToastUtil.showToast(MyApplication.instance, getString(R.string.text_video_meeting_end));
            }else{
              ToastUtil.showToast(MyApplication.instance, getString(R.string.text_you_were_kicked_outvideo_meeting));
            }
            mHandler.post(() -> VideoMeetingActivity.this.finish());
          }
        }
      });
    }catch (Exception e){
      e.printStackTrace();
    }
  };

  private ReceiveNetworkChangeHandler receiveNetworkChangeHandler = new ReceiveNetworkChangeHandler() {
    @Override
    public void handler(boolean connected) {
      mHandler.post(() -> {
        if(mLlNoNetwork!=null){
          mLlNoNetwork.setVisibility((!connected)?View.VISIBLE:View.GONE);
          if(connected){
            if(wvVideoMeeting!=null){
              wvVideoMeeting.reload();
            }
          }
        }
      });
    }
  };

  public class JsInterface{
    //关闭界面
    @JavascriptInterface
    public void hangUp(){
      myHandler.post(() -> VideoMeetingActivity.this.finish());
    }
  }

  @Override
  public void doOtherDestroy() {
    logger.info(TAG + "--doOtherDestroy");
    try{
      if(wvVideoMeeting!=null){
        wvVideoMeeting.stopLoading();
        wvVideoMeeting.clearCache(true);
        wvVideoMeeting.clearFormData();
        wvVideoMeeting.clearHistory();
        wvVideoMeeting.setVisibility(View.GONE);
        wvVideoMeeting.destroy();
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNetworkChangeHandler);
    MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
    mHandler.removeCallbacksAndMessages(null);
  }
}
