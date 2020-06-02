package cn.vsx.vc.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.search.GroupSearchBean;
import cn.vsx.hamster.terminalsdk.manager.search.MemberSearchBean;
import cn.vsx.hamster.terminalsdk.model.VideoMeetingDataBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCloseVideoMeetingMinimizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyAddVideoMeetingMessageHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.notification.VideoMeetingNotification;
import cn.vsx.vc.prompt.PromptManager;
import cn.vsx.vc.receiveHandle.ReceiverCloseKeyBoardHandler;
import cn.vsx.vc.receiveHandle.ReceiverEntityKeyEventInServiceHandler;
import cn.vsx.vc.utils.Constants;
import cn.vsx.vc.view.IndividualCallTimerView;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.search.SearchUtil;

/**
 * 视频会商
 */
public class VideoMeetingService extends BaseService {

    private RelativeLayout mRlPhonePushLive;
    private WebView wvVideoMeeting;

    private RelativeLayout mPopMinimize;
    private IndividualCallTimerView mPopupSpeakingTime;
    //1-为加号发起，2-为组内发起，3-为进入视频会商页面
    private int videoMeetingType;
    private long roomId;
    //private int groupNo;
    private String url;


    private float downX = 0;
    private float downY = 0;
    private int oddOffsetX = 0;
    private int oddOffsetY = 0;

    private JsInterface jsInterface = new JsInterface();
    private Map<String,ArrayList<MemberSearchBean>>  memberSearchMap = new HashMap<>();
    private Map<String,ArrayList<GroupSearchBean>>  groupSearchMap = new HashMap<>();
    private boolean onPageFinished;

    public VideoMeetingService(){}

    @SuppressLint("InflateParams")
    @Override
    protected void setRootView(){
        rootView = LayoutInflater.from(MyTerminalFactory.getSDK().application).inflate(R.layout.layout_video_meeting, null);
    }

    @Override
    protected void initWindow() {
        super.initWindow();
    }
    @Override
    protected void findView(){
        //大窗口
        mRlPhonePushLive = rootView.findViewById(R.id.rl_phone_push_live);
        wvVideoMeeting = rootView.findViewById(R.id.wv_video_meeting);
        mLlNoNetwork = rootView.findViewById(R.id.ll_no_network);

        mPopMinimize = rootView.findViewById(R.id.pop_minimize);
        mPopupSpeakingTime = rootView.findViewById(R.id.popup_speaking_time);
        mPopupSpeakingTime.setTextSize(14);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener(){
        mPopMinimize.setOnTouchListener(miniPopOnTouchListener);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiverEntityKeyEventInServiceHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveCloseVideoMeetingMinimizeHandler);
    }

    @Override
    protected void initView(Intent intent){
        videoMeetingType = intent.getIntExtra(Constants.VIDEO_MEETING_TYPE, 0);
        if(videoMeetingType == 1){
          //groupNo = intent.getIntExtra(Constants.VIDEO_MEETING_GROUP_NO, 0);
            url = TerminalFactory.getSDK().getVideoMeetingManager().getVideoMeetingUrlFromType(videoMeetingType);
            TerminalFactory.getSDK().getVideoMeetingManager().setMeetingStatus(false);
        }else if(videoMeetingType == 2){
          //groupNo = intent.getIntExtra(Constants.VIDEO_MEETING_GROUP_NO, 0);
            url = TerminalFactory.getSDK().getVideoMeetingManager().getVideoMeetingUrlFromType(videoMeetingType);

            TerminalFactory.getSDK().getVideoMeetingManager().setMeetingStatus(false);
        }else if(videoMeetingType == 3){
            //进入视频会商页面
            roomId = intent.getLongExtra(Constants.ROOM_ID, 0L);
            if (roomId <= 0) {
                cn.vsx.vc.utils.ToastUtil.showToast(MyApplication.instance,
                    getString(R.string.text_video_meeting_data_error));
                finishVideoLive();
                return;
            }
            url = TerminalFactory.getSDK().getVideoMeetingManager().getVideoMeetingUrlFromRoomId(roomId);
        }else{
            TerminalFactory.getSDK().getVideoMeetingManager().setMeetingStatus(false);
            finishVideoLive();
            return;
        }
        initWeb();
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
        wvVideoMeeting.addJavascriptInterface(jsInterface,"android");
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(wvVideoMeeting,true);
        }

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
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
                    if (handler != null) {
                        handler.proceed();//忽略证书的错误继续加载页面内容，不会变成空白页面
                    }
                }
            });
            wvVideoMeeting.setWebChromeClient(new WebChromeClient(){
              @Override public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                logger.info(TAG+"--onProgressChanged--newProgress:"+newProgress);
                if(newProgress == 100){
                  onPageFinished = true;
                }
              }

              //@Override public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                //    //return super.onConsoleMessage(consoleMessage);
                //    return true;
                //}

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
                    mHandler.post(() -> {
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
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                goBackView();
                return true;
            }
            return false;
        });
    }

    public class JsInterface{
        //跳转到视频会商主页面
        @JavascriptInterface
        public void goToVideoMeeting(String roomId){
            try{
                logger.info(TAG+"--goToVideoMeeting----roomId:" + roomId);
                mHandler.post(() -> {
                    if(!TextUtils.isEmpty(roomId) && DataUtil.stringToLong(roomId)>0){
                        TerminalFactory.getSDK().getVideoMeetingManager().setMeetingStatus(true);
                        videoMeetingType = 3;
                        VideoMeetingService.this.roomId = DataUtil.stringToLong(roomId);
                    }else{
                        cn.vsx.vc.utils.ToastUtil.showToast(MyApplication.instance,
                            getString(R.string.text_video_meeting_data_error));
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

      /**
       *  搜索人
       */
      @JavascriptInterface
      public void searchAccountByKeyword(String key,int index,int pageSize){
        try{
          logger.info(TAG+"--searchAllByKeyword----key:" + key+"-index-"+index+"-pageSize-"+pageSize);
          TerminalFactory.getSDK().getThreadPool().execute(() -> {
            List<MemberSearchBean> result = new ArrayList<>();
            if (!TextUtils.isEmpty(key)) {
              if(memberSearchMap.containsKey(key)){
                if(memberSearchMap.get(key) == null||memberSearchMap.get(key).isEmpty()){
                  //重新查数据
                  getSearchAccountByKey(key);
                }
              }else{
                getSearchAccountByKey(key);
              }
              List<MemberSearchBean> list = getSearchByPaging(memberSearchMap.get(key),index,pageSize);
              if(list!=null){
                result.clear();
                result.addAll(list);
              }
            }
            logger.info(TAG+"--searchAccountByKeyword----onSuccess:"+ JSONArray.toJSONString(result));
            mHandler.post(() -> {
              if(wvVideoMeeting!=null){
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                  wvVideoMeeting.loadUrl("javascript:searchAccountByKeywordResult(" + JSONArray.toJSONString(result) + ")");
                }else{
                  wvVideoMeeting.evaluateJavascript("javascript:searchAccountByKeywordResult(" + JSONArray.toJSONString(result) + ")",null);
                }
              }
            });
          });
        }catch (Exception e){
          e.printStackTrace();
        }
      }

      /**
       *  搜索组
       */
      @JavascriptInterface
      public void searchGroupByKeyword(String key,int index,int pageSize){
        try{
          logger.info(TAG+"--searchGroupByKeyword----key:" + key+"-index-"+index+"-pageSize-"+pageSize);
          TerminalFactory.getSDK().getThreadPool().execute(() -> {
            List<GroupSearchBean> result = new ArrayList<>();
            if (!TextUtils.isEmpty(key)) {
              if(groupSearchMap.containsKey(key)){
                if(groupSearchMap.get(key) == null||groupSearchMap.get(key).isEmpty()){
                  //重新查数据
                  getSearchGroupByKey(key);
                }
              }else{
                getSearchGroupByKey(key);
              }
              List<GroupSearchBean> list = getSearchByPaging(groupSearchMap.get(key),index,pageSize);
              if(list!=null){
                result.clear();
                result.addAll(list);
              }
            }
            logger.info(TAG+"--searchGroupByKeyword----onSuccess:"+result);
            mHandler.post(() -> {
              if(wvVideoMeeting!=null){
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                  wvVideoMeeting.loadUrl("javascript:searchGroupByKeywordResult(" + JSONArray.toJSONString(result) + "," + JSONArray.toJSONString(result) + ")");
                }else{
                  wvVideoMeeting.evaluateJavascript("javascript:searchGroupByKeywordResult(" + JSONArray.toJSONString(result) + ")",null);
                }
              }
            });
          });
        }catch (Exception e){
          e.printStackTrace();
        }
      }

      /**
       *开始显示时间
       */
      @JavascriptInterface
      public void startVideoMeetingTime(){
        try{
          logger.info(TAG+"--startVideoMeetingTime----");
          videoMeetingTime(0);
        }catch (Exception e){
          e.printStackTrace();
        }
      }
      /**
       *开始显示时间
       * type 0为开始，1为暂停，2为继续
       */
      @JavascriptInterface
      public void videoMeetingTime(int type){
        try{
          logger.info(TAG+"--startVideoMeetingTime----");
          mHandler.post(() -> {
            if(mPopupSpeakingTime!=null) {
              switch (type){
                case 0:
                  mPopupSpeakingTime.onStop();
                  mPopupSpeakingTime.onStart();
                  break;
                case 1:
                  mPopupSpeakingTime.onPause();
                  break;
                case 2:
                  mPopupSpeakingTime.onContinue();
                  break;
              }
            }
          });
        }catch (Exception e){
          e.printStackTrace();
        }
      }
        /**
         * 设置是否免提
         * @param result
         */
        @JavascriptInterface
        public void setSpeakPhoneOn(boolean result){
            try{
                logger.info(TAG+"--setSpeakPhoneOn----result:" + result);
                if(result){
                    if (!MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                        MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(result);
                    }
                }else{
                    if (MyTerminalFactory.getSDK().getAudioProxy().isSpeakerphoneOn()) {
                        MyTerminalFactory.getSDK().getAudioProxy().setSpeakerphoneOn(result);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        /**
         * 设置横竖屏
         * @param landscape
         */
        @JavascriptInterface
        public void screenOrientation(boolean landscape){
            try{
                logger.info(TAG+"--screenOrientation----landscape:" + landscape);
                mHandler.post(() -> {
                    layoutParams1.screenOrientation = landscape?ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    if(windowManager!=null){
                        windowManager.updateViewLayout(rootView,layoutParams1);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

      //返回界面
      @JavascriptInterface
      public void goBack(){
        try{
          logger.info(TAG+"--goBack--");
          mHandler.post(() -> goBackView());
        }catch (Exception e){
          e.printStackTrace();
        }
      }
        //关闭界面
        @JavascriptInterface
        public void hangUp(){
            try{
                logger.info(TAG+"--hangUp--");
                mHandler.post(() -> finishVideoLive());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据关键字查询全部数据
     * @param key
     */
    private synchronized void getSearchAccountByKey(String key){
        try{
            List<MemberSearchBean> search = SearchUtil.searchMemberByKey(key);
            if(search!=null){
                memberSearchMap.clear();
                memberSearchMap.put(key, (ArrayList<MemberSearchBean>) search);
            }
            logger.info(TAG+"--getSearchAccountByKey----search:"+search);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

  /**
   * 根据关键字查询组数据
   * @param key
   */
  private synchronized void getSearchGroupByKey(String key){
    try{
      List<GroupSearchBean> search = SearchUtil.searchGroupByKey(key);
      if(search!=null){
        groupSearchMap.clear();
        groupSearchMap.put(key, (ArrayList<GroupSearchBean>) search);
      }
      logger.info(TAG+"--getSearchGroupByKey----search:"+search);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 根据分页索引和每页的数据条数获取关键字的分页数据
   * @param resouce
   * @param index
   * @param pageSize
   * @return
   */
  private List getSearchByPaging(ArrayList resouce, int index, int pageSize) {
    try{
      List list = (List) resouce.clone();
      if(list != null&&!list.isEmpty()) {
        if (index >= 0 && index < list.size()) {
          return list.subList(index,
              ((index + pageSize - 1) < list.size()) ? (index + pageSize)
                  : list.size());
        }else{
          return null;
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }
    return null;
  }

    @Override
    protected void showPopMiniView(){
        try{
            if(videoMeetingType == 3){
                windowManager.removeView(rootView);
                windowManager.addView(rootView, layoutParams);
                hideAllView();
                MyApplication.instance.isMiniLive = true;
               if(mPopupSpeakingTime.getTime()<=0){
                 mPopupSpeakingTime.onStop();
                 mPopupSpeakingTime.setVisibility(View.INVISIBLE);
                }else{
                 mPopupSpeakingTime.onContinue();
                 mPopupSpeakingTime.setVisibility(View.VISIBLE);
               }
                mPopMinimize.setVisibility(View.VISIBLE);
                VideoMeetingNotification.createNotification();
            }else{
                finishVideoLive();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void handleMesage(Message msg){
        switch(msg.what){
            default:break;
        }
    }

    @Override
    protected void onNetworkChanged(boolean connected){
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

    @Override
    protected void initBroadCastReceiver(){}

    @Override
    protected void initData(){

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
                            cn.vsx.vc.utils.ToastUtil.showToast(MyApplication.instance, getString(R.string.text_video_meeting_end));
                        }else{
                            cn.vsx.vc.utils.ToastUtil.showToast(MyApplication.instance, getString(R.string.text_you_were_kicked_outvideo_meeting));
                        }
                        mHandler.post(() -> finishVideoLive());
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    };

    /**
     * 实体按键点击事件的回调
     */
    private ReceiverEntityKeyEventInServiceHandler receiverEntityKeyEventInServiceHandler = new ReceiverEntityKeyEventInServiceHandler(){

        @Override
        public void handler(KeyEvent event) {
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    mHandler.post(() -> {
                        if (event.getAction() == KeyEvent.ACTION_UP) {
                          goBackView();
                        }
                    });
                    break;
                default:break;
            }
        }
    };

  /**
   * 退出判断
   */
  private void goBackView(){
    try{
      if(wvVideoMeeting!=null&&wvVideoMeeting.canGoBack()){
        wvVideoMeeting.goBack();
      }else {
        if(videoMeetingType == 3&&onPageFinished){
          showPopMiniView();
        }else{
          PromptManager.getInstance().stopRing();
          stopBusiness();
        }
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }

    /**
     * 实体按键点击事件的回调
     */
    private ReceiveCloseVideoMeetingMinimizeHandler receiveCloseVideoMeetingMinimizeHandler = new ReceiveCloseVideoMeetingMinimizeHandler(){
        @Override
        public void handle() {
            //判断下是否是在小窗口模式
            if((mRlPhonePushLive!=null&&mRlPhonePushLive.getVisibility() == View.GONE)&&
                (mPopMinimize!=null&&mPopMinimize.getVisibility() == View.VISIBLE)){
                mHandler.post(() -> closeVideoMeetingMinimizeView());
            }
        }
    };

    @Override
    public void onDestroy(){
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
        finishVideoLive();
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyAddVideoMeetingMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiverEntityKeyEventInServiceHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveCloseVideoMeetingMinimizeHandler);
        TerminalFactory.getSDK().getVideoMeetingManager().setMeetingStatus(false);
        VideoMeetingNotification.cancelNotification();
        super.onDestroy();
    }

    private void hideAllView(){
        if(mRlPhonePushLive !=null){
            mRlPhonePushLive.setVisibility(View.GONE);
        }
        if(mPopMinimize!=null){
            mPopMinimize.setVisibility(View.GONE);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener miniPopOnTouchListener = (v,event)->{
        //触摸点到边界屏幕的距离
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //触摸点到自身边界的距离
                downX = event.getX();
                downY = event.getY();
                oddOffsetX = layoutParams.x;
                oddOffsetY = layoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                //不除以3，拖动的view抖动的有点厉害
                if(Math.abs(downX - moveX) > 5 || Math.abs(downY - moveY) > 5){
                    // 更新浮动窗口位置参数
                    layoutParams.x = (int) (screenWidth - (x + downX));
                    layoutParams.y = (int) (y - downY);
                    windowManager.updateViewLayout(rootView, layoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                int newOffsetX = layoutParams.x;
                int newOffsetY = layoutParams.y;
                if(Math.abs(newOffsetX - oddOffsetX) <= 30 && Math.abs(newOffsetY - oddOffsetY) <= 30){
                    closeVideoMeetingMinimizeView();
                }
                break;
        }
        return true;
    };

    /**
     * 关闭小窗口
     */
    private void closeVideoMeetingMinimizeView(){
        try{
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverCloseKeyBoardHandler.class);
            if(windowManager!=null){
                windowManager.removeView(rootView);
                windowManager.addView(rootView,layoutParams1);
            }
            hideAllView();
            MyApplication.instance.isMiniLive = false;
            if(mRlPhonePushLive!=null){
                mRlPhonePushLive.setVisibility(View.VISIBLE);
            }
            VideoMeetingNotification.cancelNotification();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void finishVideoLive(){
        mHandler.removeCallbacksAndMessages(null);
        PromptManager.getInstance().stopRing();//停止响铃
        hideAllView();
        stopBusiness();
    }
}
