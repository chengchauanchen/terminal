package cn.vsx.vc.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GridViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverSelectChatListHandler;
import cn.vsx.vc.record.AudioRecordButton;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 *  会话界面下部
 * Created by zckj on 2017/3/14.
 */

public class FunctionHidePlus extends LinearLayout {

    @Bind(R.id.gv_function_bottom)
    GridView gv_function_bottom;
    @Bind(R.id.rl_function_hide_plus_top)
    RelativeLayout top;
    @Bind(R.id.group_call_news_et)
    EditText groupCallNewsEt;
    @Bind(R.id.hide_function)
    ImageView hideFunction;
    @Bind(R.id.group_call_news_keyboard)
    ImageView groupCallNewsKeyboard;
    @Bind(R.id.btn_ptt)
    Button btn_ptt;
    @Bind(R.id.btn_record)
    AudioRecordButton btn_record;
    @Bind(R.id.bt_send)
    Button send;

    @Bind(R.id.ll_function_hide_plus_bottom)
    LinearLayout ll_function_hide_plus_bottom;
    @Bind(R.id.v_edit_line)
    View v_edit_line;
    private Context context;
    private Logger logger = Logger.getLogger(DataUtil.class);
    InputMethodManager inputMethodManager ;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private GridViewAdapter gridViewAdapter;
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;
    private int userId;
    private boolean isGroupFunction;
    //是处于发消息还是发语音状态,组会话刚进入是false，个人会话是true

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
    public FunctionHidePlus(Context context) {
        this(context, null);
    }

    public FunctionHidePlus(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public FunctionHidePlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.VERTICAL);
        this.context = context;
        inputMethodManager = ((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE));
        initView();
        initListener();
    }

    private void initView() {
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater;
        layoutInflater =  (LayoutInflater) getContext().getSystemService(infServie);
        View view = layoutInflater.inflate(R.layout.layout_functionhideplus, this, true);
        ButterKnife.bind(this, view);
        ll_function_hide_plus_bottom.setVisibility(GONE);
    }

    private void initListener () {
        groupCallNewsEt.addTextChangedListener(mTextWatcher);
        groupCallNewsEt.setOnFocusChangeListener(mOnFocusChangeListener);
        groupCallNewsEt.setOnClickListener(v -> showBottom(false));

        setHasVideo();
        gridViewAdapter = new GridViewAdapter(titles, images, context);
        gv_function_bottom.setAdapter(gridViewAdapter);
        gv_function_bottom.setOnItemClickListener((parent, view, position, id) -> {
            String title = titles[position];
            if (isFastClick()){
                if(DataUtil.getMemberByMemberNo(userId).terminalMemberType== TerminalMemberType.TERMINAL_PDT.name()){
                    ToastUtil.showToast(context,"对方不支持该消息类型");
                    return;
                }

                if (title.equals("相册")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.PHOTO_ALBUM, true, userId);
                }else if (title.equals("拍照")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    if(MyApplication.instance.isMiniLive){
                        ToastUtil.showToast(context,"小窗口模式中，不能执行该操作");
                        return;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.CAMERA, true, userId);
                }else if (title.equals("文件")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.FILE, true, userId);
                }else if (title.equals("发送位置")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.LOCATION);
                }else if (title.equals("上报图像")){
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                        CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
                        return;
                    }
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.CAMERA)) {//没有相机权限
                        CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.CAMERA);
                        return;
                    }
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                        Toast.makeText(context,"没有图像上报权限",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.POST_BACK_VIDEO, true, userId);
                }else if (title.equals("请求图像")){
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                        CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
                        return;
                    }
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())){
                        Toast.makeText(context,"没有图像请求权限",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO, true, userId);
                }else {
                    ToastUtil.showToast(context,"没有发送消息功能权限");
                }
            }
        });
    }
    private String[] titles=new String[]{
            "相册","拍照","文件","发送位置","上报图像","请求图像"
    };
    private Integer[] images=new Integer[]{
            R.drawable.album,R.drawable.take_phones,
            R.drawable.file_selector,R.drawable.position,
            R.drawable.push_video,R.drawable.pull_video
    };
    @OnClick({R.id.hide_function, R.id.group_call_news_keyboard, R.id.bt_send})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hide_function:
                showOrHideBottom();
                break;

            case R.id.group_call_news_keyboard:
                    if(isGroupFunction){
                        btn_record.setVisibility(View.GONE);
                        if(groupCallNewsEt.getVisibility() == View.VISIBLE) {
                            showVoiceView(true);
                            showKeyBoard(false);
                            ll_function_hide_plus_bottom.setVisibility(View.GONE);
                        }
                        else {
                            showInputView();
                            showKeyBoard(true);
                            String unsendMessage = context.getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId), null);
                            if(!TextUtils.isEmpty(unsendMessage)){
                                groupCallNewsEt.setText(unsendMessage);
                                groupCallNewsEt.setSelection(unsendMessage.length());
                            }
                        }
                    }else {
                        if(groupCallNewsEt.getVisibility()==View.VISIBLE){
                            showKeyBoard(false);
                            ll_function_hide_plus_bottom.setVisibility(View.GONE);
                            showVoiceView(false);
                        }
                        else {
                            showInputView();
                            showKeyBoard(true);
                        }

                    }

                break;
            case R.id.bt_send:
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    ToastUtil.showToast(context,"没有发送消息的功能权限");
                    return;
                }
                OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.TEXT);
                if(!TextUtils.isEmpty(context.getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId),""))){
                    context.getSharedPreferences("unsendMessage",MODE_PRIVATE).edit().remove(String.valueOf(userId)).apply();
                }
                break;

            default:
                break;
        }
    }

    private void showVoiceView(boolean isGroup){
        groupCallNewsEt.setVisibility(View.GONE);
        v_edit_line.setVisibility(GONE);
        send.setVisibility(GONE);
        hideFunction.setVisibility(VISIBLE);
        groupCallNewsKeyboard.setBackground(MyApplication.instance.getResources().getDrawable(R.drawable.soft_keyboard));
        if(isGroup){
            btn_ptt.setVisibility(View.VISIBLE);
            btn_record.setVisibility(View.GONE);
        }else {
            btn_ptt.setVisibility(View.GONE);
            btn_record.setVisibility(View.VISIBLE);
        }
    }

    private void showKeyBoard (boolean showKeyBoard) {
        if(showKeyBoard){
            //显示et
            groupCallNewsEt.setVisibility(VISIBLE);
            v_edit_line.setVisibility(VISIBLE);
            btn_ptt.setVisibility(GONE);
            btn_record.setVisibility(View.GONE);
            groupCallNewsEt.requestFocus();
            //切换软键盘的显示与隐藏
            hideKeyboard(false);
        }else {
            //显示bt
            groupCallNewsEt.setVisibility(GONE);
            v_edit_line.setVisibility(GONE);
            hideFunction.setVisibility(VISIBLE);
            hideKeyboard(true);
            if(isGroupFunction){
                btn_ptt.setVisibility(VISIBLE);
                btn_record.setVisibility(View.GONE);
            }else {
                btn_ptt.setVisibility(GONE);
                btn_record.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showOrHideBottom () {
        if(ll_function_hide_plus_bottom.getVisibility() == View.GONE) {

            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSelectChatListHandler.class);
            //让edittext不获取焦点
            showInputView();
            hideKeyboard(true);
            groupCallNewsEt.clearFocus();

            handler.postDelayed(() -> {
                ll_function_hide_plus_bottom.setVisibility(View.VISIBLE);
                ll_function_hide_plus_bottom.requestFocus();
            }, 50L);
        }
        else {
            ll_function_hide_plus_bottom.setVisibility(View.GONE);
            showInputView();
            groupCallNewsEt.requestFocus();
            hideKeyboard(true);
        }
    }

    private void showInputView(){
        groupCallNewsEt.setVisibility(View.VISIBLE);
        v_edit_line.setVisibility(VISIBLE);
        btn_record.setVisibility(View.GONE);
        btn_ptt.setVisibility(GONE);
        groupCallNewsKeyboard.setBackground(MyApplication.instance.getResources().getDrawable(R.drawable.recording));
    }

    public void showBottom (boolean show) {
        if(!show) {
            postDelayed(() -> ll_function_hide_plus_bottom.setVisibility(View.GONE),50);

//            ll_function_bottom.setVisibility(GONE);
        }
        else {
            ll_function_hide_plus_bottom.setVisibility(View.VISIBLE);
//            ll_function_bottom.setVisibility(GONE);
        }
    }

    public boolean isBottomShow () {
        return ll_function_hide_plus_bottom.getVisibility() == VISIBLE;
    }

    public void hideKey () {
        hideKeyboard(true);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 提供给外界是否隐藏图像回传按钮
     */
    public void setFunction(boolean isGroupFunction, int userId){
        this.userId = userId;
        this.isGroupFunction = isGroupFunction;
        String unsendMessage = context.getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId),"");
        if(isGroupFunction){//组消息界面
            setNoVideo();
            groupCallNewsKeyboard.setBackgroundResource(R.drawable.soft_keyboard);
            groupCallNewsEt.setVisibility(GONE);
            v_edit_line.setVisibility(GONE);
            btn_ptt.setVisibility(VISIBLE);
            btn_record.setVisibility(View.GONE);
            //如果有未读消息，显示未读消息，显示edittext
            if(!Util.isEmpty(unsendMessage)){
                groupCallNewsKeyboard.setBackgroundResource(R.drawable.recording);
                groupCallNewsEt.setVisibility(VISIBLE);
                v_edit_line.setVisibility(VISIBLE);
                btn_ptt.setVisibility(GONE);
                groupCallNewsEt.setText(unsendMessage);
                groupCallNewsEt.setSelection(unsendMessage.length());
            }
        }else{//个人消息界面

            groupCallNewsKeyboard.setBackgroundResource(R.drawable.soft_keyboard);
            boolean canPush = false;
            boolean canPull = false;
            //PDT终端不支持录音消息类型
            if(DataUtil.getMemberByMemberNo(userId).terminalMemberType==TerminalMemberType.TERMINAL_PDT.name()){
                btn_record.setBackgroundResource(R.drawable.shape_news_ptt_wait);
                btn_record.setText("禁止录音");
                btn_record.setEnabled(false);
            }else {
                btn_record.setBackgroundResource(R.drawable.shape_news_ptt_listen);
                btn_record.setText("长按录音");
                btn_record.setEnabled(true);
            }
            canPull=false;
            canPush=false;
            //是否在同一个组
            for(Member member :MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers()){
                if(member.id==userId){
                    canPull=true;
                    canPush=true;
                }
            }
            if(!canPull&&!canPush){
                setNoVideo();
            }else {
                setHasVideo();
            }

            if(!Util.isEmpty(unsendMessage)){
                groupCallNewsEt.setText(unsendMessage);
                groupCallNewsEt.setSelection(unsendMessage.length());
                groupCallNewsEt.setVisibility(VISIBLE);
                groupCallNewsKeyboard.setBackgroundResource(R.drawable.recording);
                groupCallNewsKeyboard.setVisibility(VISIBLE);
                btn_ptt.setVisibility(GONE);
                btn_record.setVisibility(GONE);
                v_edit_line.setVisibility(VISIBLE);
            }else {
                groupCallNewsEt.setVisibility(GONE);
                groupCallNewsKeyboard.setBackgroundResource(R.drawable.soft_keyboard);
                groupCallNewsKeyboard.setVisibility(VISIBLE);
                btn_ptt.setVisibility(GONE);
                btn_record.setVisibility(VISIBLE);
                v_edit_line.setVisibility(GONE);
            }
        }

        gridViewAdapter.refresh(titles, images);
    }
    private void setNoVideo() {
        titles = null;
        titles=new String[]{
                "相册","拍照","文件","发送位置"
        };
        images = null;
        images=new Integer[]{
                R.drawable.album,R.drawable.take_phones,
                R.drawable.file_selector,R.drawable.position
        };
    }
    private void setHasVideo() {
        titles = null;
        titles=new String[]{
                "相册","拍照","文件","发送位置","上报图像","请求图像"
        };
        images = null;
        images=new Integer[]{
                R.drawable.album,R.drawable.take_phones,
                R.drawable.file_selector,R.drawable.position,
                R.drawable.push_video,R.drawable.pull_video
        };
    }

    public void setViewEnable (boolean isEnable) {
        groupCallNewsEt.setEnabled(isEnable);
        hideFunction.setEnabled(isEnable);
        groupCallNewsKeyboard.setEnabled(isEnable);
        send.setEnabled(isEnable);
//        videoPostback.setEnabled(isEnable);
//        video_request.setEnabled(isEnable);
//        ll_open_camera.setEnabled(isEnable);
//        ll_open_photoalbum.setEnabled(isEnable);
//        ll_open_file.setEnabled(isEnable);
//        ll_send_location.setEnabled(isEnable);
    }

    public void hideKeyboard (boolean hide) {
        if(inputMethodManager != null) {
            if(hide) {
                inputMethodManager.hideSoftInputFromWindow(groupCallNewsEt.getWindowToken(), 0);
            }
            else {
                inputMethodManager.showSoftInput(groupCallNewsEt, 0);
            }
        }

    }

    public void hideKeyboardAndBottom () {
        hideKeyboard(true);
        ll_function_hide_plus_bottom.setVisibility(View.GONE);
//        ll_function_bottom.setVisibility(GONE);
    }

    /**
     * 隐藏PTT按钮，显示edittext，隐藏键盘
     */
    public void hidePtt(){
        groupCallNewsEt.setVisibility(View.VISIBLE);
        v_edit_line.setVisibility(VISIBLE);
        btn_ptt.setVisibility(View.GONE);
        btn_record.setVisibility(GONE);
        groupCallNewsKeyboard.setBackground(MyApplication.instance.getResources().getDrawable(R.drawable.recording));
        hideKeyboard(true);
    }

    /** 输入文字改变监听 */
    private TextWatcher mTextWatcher = new TextWatcher() {

        //这里的s表示改变之前的内容，通常start和count组合，可以在s中读取本次改变字段中被改变的内容。而after表示改变后新的内容的数量。
        //start开始位置，count改变前的内容数量，after改变后的内容数量
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        //这里的s表示改变时的内容，通常start和count组合，可以在s中读取本次改变字段中新的内容。而before表示被改变的内容的数量。
        //start开始位置，before改变前的内容数量，count改变的内容数量
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String msg = s.toString();
            if(s.length() == 0 || TextUtils.isEmpty(msg.trim())){
                send.setVisibility(GONE);
                hideFunction.setVisibility(VISIBLE);
                if(!TextUtils.isEmpty(context.getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId),""))){
                    context.getSharedPreferences("unsendMessage",MODE_PRIVATE).edit().remove(String.valueOf(userId)).apply();
                }
            }else {
                send.setVisibility(VISIBLE);
                hideFunction.setVisibility(GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) { //表示et输入框最终的结果

        }
    };

    private OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                ll_function_hide_plus_bottom.setVisibility(GONE);
            }
        }
    };


    private PttOnTouchLitener pttOnTouchLitener;
    public void setPttOnTouchLitener(PttOnTouchLitener pttOnTouchLitener){
        this.pttOnTouchLitener = pttOnTouchLitener;
    }
    public interface PttOnTouchLitener{
        void up();
        void down();
    }
}
