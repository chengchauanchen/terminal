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

import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileCheckMessageHandler;
import com.zectec.imageandfileselector.receivehandler.ReceiverSendFileHandler;
import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import org.apache.log4j.Logger;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Util;
import cn.vsx.vc.R;
import cn.vsx.vc.adapter.GridViewAdapter;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiveHandle.ReceiverSelectChatListHandler;
import cn.vsx.vc.receiveHandle.ReceiverShowTransponPopupHandler;
import cn.vsx.vc.record.AudioRecordButton;
import cn.vsx.vc.utils.Constants;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.audio.CheckMyPermission;
import ptt.terminalsdk.tools.ToastUtil;

import static android.content.Context.MODE_PRIVATE;

/**
 *  会话界面下部
 * Created by zckj on 2017/3/14.
 */

public class FunctionHidePlus extends LinearLayout implements View.OnClickListener{


    GridView gv_function_bottom;

    RelativeLayout top;

    EditText groupCallNewsEt;

    ImageView hideFunction;

    ImageView groupCallNewsKeyboard;

    Button btn_ptt;

    AudioRecordButton btn_record;

    Button send;


    LinearLayout ll_function_hide_plus_bottom;

    View v_edit_line;

    Button bt_merge_transmit;
    //合并转发


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

        gv_function_bottom = view.findViewById(R.id.gv_function_bottom);
        top = view.findViewById(R.id.rl_function_hide_plus_top);
        groupCallNewsEt = view.findViewById(R.id.group_call_news_et);
        hideFunction = view.findViewById(R.id.hide_function);
        groupCallNewsKeyboard = view.findViewById(R.id.group_call_news_keyboard);
        btn_ptt = view.findViewById(R.id.btn_ptt);
        btn_record = view.findViewById(R.id.btn_record);
        send = view.findViewById(R.id.bt_send);
        ll_function_hide_plus_bottom = view.findViewById(R.id.ll_function_hide_plus_bottom);
        v_edit_line = view.findViewById(R.id.v_edit_line);
        bt_merge_transmit = view.findViewById(R.id.bt_merge_transmit);

        view.findViewById(R.id.hide_function).setOnClickListener(this);
        view.findViewById(R.id.group_call_news_keyboard).setOnClickListener(this);
        view.findViewById(R.id.bt_send).setOnClickListener(this);
        view.findViewById(R.id.bt_merge_transmit).setOnClickListener(this);
        ll_function_hide_plus_bottom.setVisibility(GONE);
        bt_merge_transmit.setVisibility(GONE);
    }

    private void initListener () {
        groupCallNewsEt.addTextChangedListener(mTextWatcher);
        groupCallNewsEt.setOnFocusChangeListener(mOnFocusChangeListener);
        groupCallNewsEt.setOnClickListener(v -> showBottom(false));

//        setHasVideo();
        gridViewAdapter = new GridViewAdapter(titles, images, context);
        gv_function_bottom.setAdapter(gridViewAdapter);
        gv_function_bottom.setOnItemClickListener((parent, view, position, id) -> {
            String title = titles[position];
            if (isFastClick()){
                if (title.equals("相册")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.PHOTO_ALBUM, true, userId);
                }else if (title.equals("拍照")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    if(MyApplication.instance.isMiniLive){
                        ToastUtil.showToast(context,context.getString(R.string.text_small_window_mode_can_not_do_this));
                        return;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.CAMERA, true, userId);
                }else if (title.equals("文件")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.FILE, true, userId);
                }else if (title.equals("发送位置")
                        &&MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.LOCATION);
                }else if (title.equals("上报图像")||(title.equals("组内上图"))){
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                        CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
                        return;
                    }
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.CAMERA)) {//没有相机权限
                        CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.CAMERA);
                        return;
                    }
                    if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_UP.name())){
                        ToastUtil.showToast(context,context.getString(R.string.text_has_no_image_report_authority));
                        return;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.POST_BACK_VIDEO, true, userId);
                }else if (title.equals("请求图像")) {
                    if (!CheckMyPermission.selfPermissionGranted(context, Manifest.permission.RECORD_AUDIO)) {//没有录音权限
                        CheckMyPermission.permissionPrompt((Activity) context, Manifest.permission.RECORD_AUDIO);
                        return;
                    }
                    if (!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_VIDEO_ASK.name())) {
                        ToastUtil.showToast(context, context.getString(R.string.no_pull_authority));
                        return;
                    }
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.REQUEST_VIDEO, true, userId);
                }else if(title.equals("NFC")){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.NFC, true, userId);
                }else if(title.equals("二维码")){
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileCheckMessageHandler.class, ReceiverSendFileCheckMessageHandler.QR_CODE, true, userId);
                }else {
                    ToastUtil.showToast(context,context.getString(R.string.text_has_no_send_message_authority));
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.hide_function){
            showOrHideBottom();
        }else if(i == R.id.group_call_news_keyboard){
            if(isGroupFunction){
                btn_record.setVisibility(View.GONE);
                if(groupCallNewsEt.getVisibility() == View.VISIBLE){
                    showVoiceView(true);
                    showKeyBoard(false);
                    ll_function_hide_plus_bottom.setVisibility(View.GONE);
                }else{
                    showInputView();
                    showKeyBoard(true);
                    String unsendMessage = context.getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId), null);
                    if(!TextUtils.isEmpty(unsendMessage)){
                        groupCallNewsEt.setText(unsendMessage);
                        groupCallNewsEt.setSelection(unsendMessage.length());
                    }
                }
            }else{
                if(groupCallNewsEt.getVisibility() == View.VISIBLE){
                    showKeyBoard(false);
                    ll_function_hide_plus_bottom.setVisibility(View.GONE);
                    showVoiceView(false);
                }else{
                    showInputView();
                    showKeyBoard(true);
                }
            }
        }else if(i == R.id.bt_send){
            if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_MESSAGE_SEND.name())){
                ToastUtil.showToast(context, context.getString(R.string.text_has_no_send_message_authority));
                return;
            }
            //点击发送并不真的发送数据，而是触发另外一个地方的回调去发送
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSendFileHandler.class, ReceiverSendFileHandler.TEXT);
            if(!TextUtils.isEmpty(context.getSharedPreferences("unsendMessage", MODE_PRIVATE).getString(String.valueOf(userId), ""))){
                context.getSharedPreferences("unsendMessage", MODE_PRIVATE).edit().remove(String.valueOf(userId)).apply();
            }
        }else if(i == R.id.bt_merge_transmit){//合并转发
            OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverShowTransponPopupHandler.class, Constants.TRANSPON_TYPE_MORE);
        }else{
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
            //判读之前是否有输入文字
            if(TextUtils.isEmpty(groupCallNewsEt.getText().toString().trim())){
                //没有输入文字，不显示发送按钮，显示+号
                send.setVisibility(GONE);
                hideFunction.setVisibility(VISIBLE);
            }else{
                //输入有文字，显示发送按钮，不显示+号
                send.setVisibility(VISIBLE);
                hideFunction.setVisibility(GONE);
            }
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
//            HashMap<String,String> hashMap = TerminalFactory.getSDK().getHashMap(Params.GROUP_WARNING_MAP,new HashMap<String,String>());
//            if(hashMap.containsKey( userId+"")&&!TextUtils.isEmpty(hashMap.get( userId+""))){
                setHasNFC();
//            }else{
//                setNoVideo();
//            }
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


            canPull=true;
            canPush=true;
            //是否在同一个组
//            for(Member member :MyTerminalFactory.getSDK().getConfigManager().getCurrentGroupMembers()){
//                if(member.id==userId){
//                    canPull=true;
//                    canPush=true;
//                }
//            }
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

            //PDT终端不支持录音消息类型
            TerminalFactory.getSDK().getThreadPool().execute(() -> {
                if(DataUtil.isPDTMember(userId)){
                    handler.post(() -> {
                        btn_record.setBackgroundResource(R.drawable.shape_news_ptt_wait);
                        btn_record.setText(R.string.text_can_not_sound_recorder);
                        btn_record.setEnabled(false);
                        setPDTFunction();
                        gridViewAdapter.refresh(titles, images);
                    });
                }else {
                    handler.post(()->{
                        btn_record.setBackgroundResource(R.drawable.shape_news_ptt_listen);
                        btn_record.setText(R.string.text_long_press_to_sound_recorder);
                        btn_record.setEnabled(true);
                        gridViewAdapter.refresh(titles, images);
                    });
                }
            });
        }

        gridViewAdapter.refresh(titles, images);
    }
    private void setNoVideo() {
        titles = null;
        titles=new String[]{
                "相册","拍照","文件","发送位置","组内上图"
        };
        images = null;
        images=new Integer[]{
                R.drawable.album,R.drawable.take_phones,
                R.drawable.file_selector,R.drawable.position,
                R.drawable.push_video
        };
    }

    /**
     * 警情组（包含NFC）
     */
    private void setHasNFC() {
        titles = null;
        titles=new String[]{
                "相册","拍照","文件","发送位置","组内上图","NFC","二维码"
        };
        images = null;
        images=new Integer[]{
                R.drawable.album,R.drawable.take_phones,
                R.drawable.file_selector,R.drawable.position,
                R.drawable.push_video, R.drawable.nfc, R.drawable.qr_code
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

    private void setPCFunction(){
        titles = null;
        titles=new String[]{
                "相册","拍照","文件","发送位置","请求图像"
        };
        images = null;
        images=new Integer[]{
                R.drawable.album,R.drawable.take_phones,
                R.drawable.file_selector,R.drawable.position,
                R.drawable.pull_video
        };
    }

    private void setRecoderFunction(){
        titles = null;
        titles=new String[]{
                "相册","拍照","文件","发送位置","请求图像"
        };
        images = null;
        images=new Integer[]{
                R.drawable.album,R.drawable.take_phones,
                R.drawable.file_selector,R.drawable.position,
                R.drawable.pull_video
        };
    }

    public void setPDTFunction(){
        titles = null;
        titles=new String[]{
        };
        images = null;
        images=new Integer[]{
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

    public void setCannotSendMessage(){
        gv_function_bottom.setEnabled(false);
        top.setEnabled(false);
        groupCallNewsEt.setEnabled(false);
        groupCallNewsKeyboard.setEnabled(false);
        btn_ptt.setEnabled(false);
        btn_record.setEnabled(false);
        send.setEnabled(false);
        ll_function_hide_plus_bottom.setEnabled(false);
        bt_merge_transmit.setEnabled(false);
        hideFunction.setEnabled(false);
        btn_ptt.setBackgroundResource(R.drawable.shape_news_ptt_wait);
    }

    public void setMemberFunction(int type){
        if(type == TerminalMemberType.TERMINAL_PDT.getCode()){
            btn_record.setBackgroundResource(R.drawable.shape_news_ptt_wait);
            btn_record.setText(R.string.text_can_not_sound_recorder);
            btn_record.setEnabled(false);
            setPDTFunction();
            gridViewAdapter.refresh(titles, images);
        }else if(type == TerminalMemberType.TERMINAL_PC.getCode()){
            setPCFunction();
            gridViewAdapter.refresh(titles, images);
        }else if(type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()){
            setRecoderFunction();
            gridViewAdapter.refresh(titles, images);
        }else {
            setHasVideo();
            gridViewAdapter.refresh(titles, images);
        }
    }

    public interface PttOnTouchLitener{
        void up();
        void down();
    }

    /**
     * 设置合并转发按钮是否显示
     * @param visibility
     */
    public void setMergeTransmitVisibility(int visibility){
        bt_merge_transmit.setVisibility(visibility);
    }

    /**
     * 获取合并转发按钮的显示和隐藏的状态
     * @return
     */
    public int getMergeTransmitVisibility(){
        return bt_merge_transmit.getVisibility();
    }

}
