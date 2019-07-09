package cn.vsx.vc.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLogFileUploadCompleteHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberKilledHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.utils.FileUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by zckj on 2017/7/25.
 */

public class HelpWordActivity extends FragmentActivity implements View.OnTouchListener, AdapterView.OnItemClickListener{

    RelativeLayout content_view;

    WebView wv_help;

    LinearLayout ll_pb;

    private Logger logger = Logger.getLogger(getClass());
    private Handler myHandler = new Handler();
    private String txtFileName= System.currentTimeMillis()+"_意见反馈.txt";
    private String logFileName= "log.txt";
    private List<String> fileNames=new ArrayList<>();
    private Map<String,String> files = new HashMap<>();
    private List<String>problem = new ArrayList<>();
    private WindowManager windowManager;
    private View dialog;
    private WindowManager.LayoutParams layoutParams;
    private boolean isDialogShow;

    /** 请求存储读取权限 */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int uid = android.os.Process.myUid();
        logger.error("本应用是否为系统级应用" + (uid == android.os.Process.SYSTEM_UID));
        if (uid == android.os.Process.SYSTEM_UID) {
            hookWebView();
        }
        setContentView(R.layout.help_word);

        content_view = findViewById(R.id.content_view);
        wv_help = findViewById(R.id.wv_help);
        ll_pb = findViewById(R.id.ll_pb);

        initWeb();
        initDialog();
        MyTerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberKilledHandler );
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLogFileUploadCompleteHandler);
    }

    private void initDialog(){
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        dialog = LayoutInflater.from(HelpWordActivity.this).inflate(R.layout.layout_problem_dialog, null);
        //华为8.0的手机上将WindowManager的type设置为TYPE_PHONE程序会崩溃
        layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_PHONE ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        RelativeLayout root =  dialog.findViewById(R.id.dialog_root);
        ListView list_dialog =  dialog.findViewById(R.id.list_dialog);
        problem.add(getString(R.string.text_group_call_fail));
        problem.add(getString(R.string.text_no_message_history));
        problem.add(getString(R.string.text_send_message_fail));
        problem.add(getString(R.string.text_no_call_can_be_connected));

        list_dialog.setAdapter(new DialogAdapter(problem,this));
        list_dialog.setOnItemClickListener(this);
        root.setOnTouchListener(this);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event){
        if(MotionEvent.ACTION_DOWN == event.getAction() && isDialogShow){
            v.performClick();
            windowManager.removeView(dialog);
            isDialogShow = false;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        wv_help.loadUrl("javascript: dialogResult('"+ problem.get(position) + "')");
        windowManager.removeView(dialog);
        isDialogShow = false;
    }

    public class JsInterface{
        @JavascriptInterface
        //打开相机
        public void takePhoto() {
            openPhoto();
        }
        //发送后返回结果

        /**
         * 提交
         * @param reqTel 电话号码、QQ号、或者邮箱
         * @param options 选择的问题
         * @param opinion 建议
         */
        @JavascriptInterface
        public void reqSub(String reqTel,String options,String opinion){
            logger.debug("reqSub-------------"+reqTel+"/"+options+"/"+opinion);
            String strcontent=opinion+"\r\n"+reqTel+"\r\n"+options;
            FileUtil.writeTxtToFile(strcontent, MyTerminalFactory.getSDK().getLogDirectory(), txtFileName);
            fileNames.add(logFileName);
            fileNames.add(txtFileName);
            MyTerminalFactory.getSDK().getLogFileManager().uploadLogFile(fileNames,"question");
        }
        @JavascriptInterface
        public void showMessage(String message){
            ToastUtil.showToast(HelpWordActivity.this,message);
        }
        //关闭界面
        @JavascriptInterface
        public void closeWebView(){
            myHandler.post(() -> finish());
        }
        @JavascriptInterface
        public void deleteFile(String filePath){
            if(filePath.startsWith("http://androidimg")){
                filePath = filePath.substring(17);
            }
            Log.e("JsInterface", "deleteFile:" + filePath);
            if(files.keySet().contains(filePath)){
                fileNames.remove(files.get(filePath));
                files.remove(filePath);
            }
        }
        @JavascriptInterface
        public void showDialog(){
            myHandler.post(() -> {
                if(!isDialogShow){
                    windowManager.addView(dialog,layoutParams);
                    isDialogShow = true;
                }
            });
        }
    }

    private void initWeb() {
        WebSettings settings = wv_help.getSettings();
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
        //settings.setGeolocationDatabasePath("/data/data/org.itri.html5webview/databases/");     // enable Web Storage: localStorage, sessionStorage
        //开启 DOM storage API 功能
//        settings.setDomStorageEnabled(false);
//        settings.setAllowContentAccess(true);
        //设置是否获得焦点
        wv_help.requestFocus();
        wv_help.addJavascriptInterface(new JsInterface(),"jsObj");
        String helpUrl = MyTerminalFactory.getSDK().getParam(Params.PHONE_HELP_URL, "");
        String nightHelpUrl = MyTerminalFactory.getSDK().getParam(Params.PHONE_NIGHT_HELP_URL, "");
        String help_word_url;
        if(MyTerminalFactory.getSDK().getParam(Params.DAYTIME_MODE,false)){
            help_word_url = helpUrl;
        }else {
            help_word_url = nightHelpUrl;
        }


        logger.info("加载帮助文档的地址" + help_word_url);
        if (!TextUtils.isEmpty(help_word_url)) {
            wv_help.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    logger.debug("转到:"+url);
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    myHandler.post(() -> {
                        try {
                            ll_pb.setVisibility(View.GONE);
                            wv_help.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest){
                    FileInputStream input;
                    String url = webResourceRequest.getUrl().toString();
                    String key = "http://androidimg";
                    /*如果请求包含约定的字段 说明是要拿本地的图片*/
                    if (url.contains(key)){
                        String imgPath = url.replace(key, "");
                        String path = decode(imgPath);
                        Log.e("HelpWordActivity", "本地图片路径:" + path.trim());
                        try {
                        /*重新构造WebResourceResponse  将数据已流的方式传入*/
                        input = new FileInputStream(new File(path.trim()));

                        /*返回WebResourceResponse*/
                            return new WebResourceResponse("image/jpg", "UTF-8", input);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                    return super.shouldInterceptRequest(webView, webResourceRequest);
                }
            });
            wv_help.setEnabled(false);
            ll_pb.setVisibility(View.VISIBLE);
            wv_help.loadUrl(help_word_url);
//            wv_help.loadUrl("file:///android_asset/help/index.html");
        } else {
            ToastUtil.showToast(this, getString(R.string.text_get_help_word_fail_please_restart_app));
        }
//        /*
//         * 监听手机返回按键，点击返回H5就返回上一级
//         */
        wv_help.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_BACK) && wv_help.canGoBack()) {
                if(isDialogShow){
                    windowManager.removeView(dialog);
                    isDialogShow = false;
                }else {
                    wv_help.goBack();
                }
                return true;
            }
            return false;
        });
    }

    public static String decode(String url)
    {
        try {
            String prevURL="";
            String decodeURL=url;
            while(!prevURL.equals(decodeURL))
            {
                prevURL=decodeURL;
                decodeURL=URLDecoder.decode( decodeURL, "UTF-8" );
            }
            return decodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue while decoding" +e.getMessage();
        }
    }

    private void hookWebView() {
        Class<?> factoryClass = null;
        try {
            factoryClass = Class.forName("android.webkit.WebViewFactory");
            Method getProviderClassMethod = null;
            Object sProviderInstance = null;
            Field field = factoryClass.getDeclaredField("sProviderInstance");
            field.setAccessible(true);
            if(field.get(null) == null){
                if (Build.VERSION.SDK_INT == 23) {
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
                    getProviderClassMethod.setAccessible(true);
                    Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                    Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                    if (constructor != null) {
                        constructor.setAccessible(true);
                        Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                        constructor2.setAccessible(true);
                        sProviderInstance = constructor.newInstance(constructor2.newInstance());
                    }
                } else if (Build.VERSION.SDK_INT == 22) {
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                    getProviderClassMethod.setAccessible(true);
                    Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
                    Constructor<?> constructor = providerClass.getConstructor(delegateClass);
                    if (constructor != null) {
                        constructor.setAccessible(true);
                        Constructor<?> constructor2 = delegateClass.getDeclaredConstructor();
                        constructor2.setAccessible(true);
                        sProviderInstance = constructor.newInstance(constructor2.newInstance());
                    }
                } else if (Build.VERSION.SDK_INT == 21) {//Android 21无WebView安全限制
                    getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
                    getProviderClassMethod.setAccessible(true);
                    Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
                    sProviderInstance = providerClass.newInstance();
                }
                if (sProviderInstance != null) {
                    field.set("sProviderInstance", sProviderInstance);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        if(wv_help!=null){
            wv_help.stopLoading();
            ViewGroup parent = (ViewGroup) wv_help.getParent();
            if(parent !=null){
                parent.removeView(wv_help);
            }
            wv_help.setVisibility(View.GONE);
            wv_help.destroy();
        }

        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberKilledHandler );
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLogFileUploadCompleteHandler);

        super.onDestroy();

    }


    /**
     * 打开相册
     */
    private void openPhoto(){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            startActivityForResult(intent,1);

    }

    public void setPlatformType(final String result) {
        myHandler.postDelayed(() -> {
            //android调用H5代码
            logger.debug("上传的图片路径："+result);
            wv_help.loadUrl("javascript: cameraResult('"+ result.toString() + "')");
        },1000);
    }

    /**组成员遥毙消息*/
    private ReceiveNotifyMemberKilledHandler receiveNotifyMemberKilledHandler = forbid -> {
        logger.info("收到遥毙，此时forbid" + forbid);
        if(forbid){
            myHandler.post(() -> {
                HelpWordActivity.this.finish();
                MyApplication.instance.stopHandlerService();
            });
        }
    };
    /**日志上传是否成功的消息*/
    private ReceiveLogFileUploadCompleteHandler receiveLogFileUploadCompleteHandler = new ReceiveLogFileUploadCompleteHandler() {
        @Override
        public void handler(final int resultCode,String type) {
            myHandler.post(() -> {
              String result = String.valueOf(resultCode);
              if (resultCode == BaseCommonCode.SUCCESS_CODE && "question".equals(type)) {
                  ToastUtil.toast(HelpWordActivity.this, getString(R.string.text_log_upload_success_thanks));
                  wv_help.loadUrl("javascript: reqSuccess('"+result+ "')");
                  finish();
              } else {
                  ToastUtil.showToast( getString(R.string.text_log_upload_fail_please_try_later), HelpWordActivity.this);
                  wv_help.loadUrl("javascript: reqSuccess('"+result+ "')");
              }
            });
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
                logger.debug("onActivityResult");
                if (requestCode == 1) {
                    //判断手机系统版本号
                    if(intent==null){
                        return;
                    }
                    if (Build.VERSION.SDK_INT > 19) {
                        //4.4及以上系统使用这个方法处理图片
                        handleImgeOnKitKat(intent);
                    }else {
//                        handleImageBeforeKitKat(data);
                    }

                }

    }

    /**
     * 4.4及以上系统处理图片的方法
     * */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImgeOnKitKat(Intent data){
        logger.debug("handleImgeOnKitKat");
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //解析出数字格式的id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }else if ("content".equalsIgnoreCase(uri.getScheme())) {
                //如果是content类型的uri，则使用普通方式处理
                imagePath = getImagePath(uri,null);
            }else if ("file".equalsIgnoreCase(uri.getScheme())) {
                //如果是file类型的uri，直接获取图片路径即可
                imagePath = uri.getPath();
            }
            //根据图片路径显示图片
            String imgFileName= System.currentTimeMillis()+"_img.jpg";
            FileUtil.copyFile(imagePath,MyTerminalFactory.getSDK().getLogDirectory()+imgFileName);
            fileNames.add(imgFileName);
            files.put(imagePath,imgFileName);
            setPlatformType(imagePath);
        }
    }
    /**
     * 通过uri和selection来获取真实的图片路径
     * */
    private String getImagePath(Uri uri,String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private class DialogAdapter extends BaseAdapter{

        private List<String> data;
        private LayoutInflater inflater;
        private DialogAdapter(List<String> data, Context context){
            this.data = data;
            inflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount(){
            return data.size();
        }

        @Override
        public String getItem(int position){
            return data.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = inflater.inflate(R.layout.help_dialog_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.itemTv.setText(data.get(position));
            return convertView;
        }
    }

    private class ViewHolder{
        private TextView itemTv;
        private ViewHolder(View view){
            itemTv =  view.findViewById(R.id.tv_item);
        }
    }
}
