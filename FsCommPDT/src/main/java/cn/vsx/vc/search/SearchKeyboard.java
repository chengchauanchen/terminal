package cn.vsx.vc.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.zectec.imageandfileselector.utils.OperateReceiveHandlerUtilSync;

import java.util.HashMap;
import java.util.Map;

import cn.vsx.hamster.common.Authority;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.Account;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.vc.R;
import cn.vsx.vc.dialog.ChooseDevicesDialog;
import cn.vsx.vc.dialog.ProgressDialog;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.StringUtil;
import ptt.terminalsdk.tools.ToastUtil;

/**
 * Created by weishixin on 2017/12/14.
 */

public class SearchKeyboard extends PopupWindow implements View.OnClickListener, PopupWindow.OnDismissListener {

    private Context context;
    private View mPopView;
    private EditText phone;
    private Map<Integer, Integer> map = new HashMap<>();
    private SoundPool spool;
    private AudioManager am = null;
    private String str="";
    private ProgressDialog myProgressDialog;
    private Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch(msg.what){
                default:
                    break;
            }
        }
    };

    public SearchKeyboard(Context context) {
        super(context);
        this.context = context;
        // TODO Auto-generated constructor stub
        init(context);
        setPopupWindow();


    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(final Context context) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        mPopView = inflater.inflate(R.layout.search_keyboard_popup_window_layout, null,false);

        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        spool = new SoundPool(11, AudioManager.STREAM_SYSTEM, 5);
        map.put(0, spool.load(context, R.raw.dtmf0, 0));
        map.put(1, spool.load(context, R.raw.dtmf1, 0));
        map.put(2, spool.load(context, R.raw.dtmf2, 0));
        map.put(3, spool.load(context, R.raw.dtmf3, 0));
        map.put(4, spool.load(context, R.raw.dtmf4, 0));
        map.put(5, spool.load(context, R.raw.dtmf5, 0));
        map.put(6, spool.load(context, R.raw.dtmf6, 0));
        map.put(7, spool.load(context, R.raw.dtmf7, 0));
        map.put(8, spool.load(context, R.raw.dtmf8, 0));
        map.put(9, spool.load(context, R.raw.dtmf9, 0));
        map.put(11, spool.load(context, R.raw.dtmf11, 0));
        map.put(12, spool.load(context, R.raw.dtmf12, 0));

        phone = (EditText) mPopView.findViewById(R.id.phone);
        phone.setInputType(InputType.TYPE_NULL);
        phone.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //EditText动态改变

            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void afterTextChanged(Editable s) {
            }
        });
        int[] numberID=new int[]{R.id.dialNum1,R.id.dialNum2,R.id.dialNum3,R.id.dialNum4,R.id.dialNum5,R.id.dialNum6,R.id.dialNum7,R.id.dialNum8,R.id.dialNum9,R.id.dialNum10};
        for (int i = 0; i < numberID.length; i++) {
            Button v = (Button) mPopView.findViewById(numberID[i]);
            v.setOnClickListener(this);
        }

        Button delete = (Button) mPopView.findViewById(R.id.delete);
        delete.setOnClickListener(this);
        delete.setOnLongClickListener(v -> {
            phone.setText("");
            return false;
        });

        ImageView btCall = (ImageView) mPopView.findViewById(R.id.image_call);
        ImageView btDiss = (ImageView) mPopView.findViewById(R.id.image_diss);
        btCall.setOnClickListener(view -> {
            try {
                String inputString = phone.getText().toString().trim();
                if(TextUtils.isEmpty(inputString) || inputString.length()>8){
                    return;
                }
                //五位数代表手台，直接播，信令会处理逻辑
                if(inputString.length() == 5){
                    int callId = StringUtil.stringToInt(inputString);
                    Member member = new Member(callId,inputString,callId);
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
                    return;
                }
                if(inputString.length() == 6){
                    inputString = "88" + inputString;
                }
                int callId = StringUtil.stringToInt(inputString);
                if(!MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                    ToastUtil.showToast(context,context.getString(R.string.text_has_no_personal_call_authority));
                    dismiss();
                    return;
                }
                if(callId == TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0)){
                    ToastUtil.showToast(context,context.getString(R.string.please_input_other_no));
                    return;
                }
                showProgressDialog();
                TerminalFactory.getSDK().getThreadPool().execute(() -> {
                    Account account = DataUtil.getAccountByMemberNo(callId,true);
                    myHandler.post(() -> {
                        SearchKeyboard.this.dismiss();
                        dismissProgressDialog();
                        if(account == null){
                         ToastUtil.showToast(context,context.getString(R.string.text_has_no_found_this_user));
                         return;
                         }
                        new ChooseDevicesDialog(context,ChooseDevicesDialog.TYPE_CALL_PRIVATE, account, (dialog,member) -> {
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
                        dialog.dismiss();
                         }).showDialog();
                    });
                });
            } catch (NumberFormatException e) {
                e.printStackTrace();
                dismiss();
            }
//                text.setText("");

        });
        btDiss.setOnClickListener(view -> dismiss());
        createProgressDialog();
    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口可
        this.setAnimationStyle(R.style.mypopwindow_anim_style);// 设置动画
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));// 设置背景透明
        this.setOnDismissListener(this);
//        mPopView.setOnTouchListener(new View.OnTouchListener() {// 如果触摸位置在窗口外面则销毁
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // TODO Auto-generated method stub
//                int height = mPopView.findViewById(R.id.id_pop_layout).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });
    }

    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.dialNum10){
            if(phone.getText().length() < 8){
                play(11);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum1){
            if(phone.getText().length() < 8){
                play(1);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum2){
            if(phone.getText().length() < 8){
                play(2);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum3){
            if(phone.getText().length() < 8){
                play(3);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum4){
            if(phone.getText().length() < 8){
                play(4);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum5){
            if(phone.getText().length() < 8){
                play(5);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum6){
            if(phone.getText().length() < 8){
                play(6);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum7){
            if(phone.getText().length() < 8){
                play(7);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum8){
            if(phone.getText().length() < 8){
                play(8);
                input(v.getTag().toString());
            }
        }else if(i == R.id.dialNum9){
            if(phone.getText().length() < 8){
                play(9);
                input(v.getTag().toString());
            }
            //            case R.id.dialx:
            //                if (phone.getText().length() < 8) {
            //                    play(11);
            //                    input(v.getTag().toString());
            //                }
            //                break;
            //		case R.id.dialj:
            //			if (phone.getText().length() < 8) {
            //				play(12);
            //				input(v.getTag().toString());
            //			}
            //			break;
        }else if(i == R.id.delete){
            play(12);
            delete();
        }else{
        }
    }

    private void play(int id) {
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);

        float value = (float)0.7 / max * current;
        spool.setVolume(spool.play(id, value, value, 0, 0, 1f), value, value);
    }
    private void input(String str) {
        int c = phone.getSelectionStart();
        String p = phone.getText().toString();
        phone.setText(p.substring(0, c) + str + p.substring(phone.getSelectionStart(), p.length()));
        phone.setSelection(c + 1, c + 1);
    }
    private void delete() {
        int c = phone.getSelectionStart();
        if (c > 0) {
            String p = phone.getText().toString();
            phone.setText(p.substring(0, c - 1) + p.substring(phone.getSelectionStart(), p.length()));
            phone.setSelection(c - 1, c - 1);
        }
    }
    private void call(String phone) {
//		Uri uri = Uri.parse("tel:" + phone);
//		Intent it = new Intent(Intent.ACTION_CALL, uri);
//		startActivity(it);
    }

    /**
     * 创建加载数据的ProgressDialog
     */
    private void createProgressDialog(){
        if (myProgressDialog == null) {
            myProgressDialog = new ProgressDialog(context);
            myProgressDialog.setCancelable(true);
        }
    }

    /**
     * 显示加载数据的ProgressDialog
     */
    private void  showProgressDialog(){
        if(myProgressDialog!=null){
            myProgressDialog.setMsg(context.getString(R.string.get_data_now));
            myProgressDialog.show();
        }
    }

    /**
     * 隐藏加载数据的ProgressDialog
     */
    private void dismissProgressDialog(){
        if(myProgressDialog!=null){
            myProgressDialog.dismiss();
        }
    }

    @Override
    public void onDismiss() {
        myHandler.removeCallbacksAndMessages(null);
    }
}
