package cn.vsx.vc.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
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
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.terminalsdk.model.Member;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveCurrentGroupIndividualCallHandler;
import cn.vsx.vc.R;
import cn.vsx.vc.utils.DataUtil;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Created by weishixin on 2017/12/14.
 */

public class DialPopupwindow extends PopupWindow implements View.OnClickListener {

    private View mPopView;
    private EditText phone;
    private Button delete;
    private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    private SoundPool spool;
    private AudioManager am = null;
    private ImageView btCall;
    private ImageView btDiss;
    private String str="";

    public DialPopupwindow(Context context) {
        super(context);
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
        mPopView = inflater.inflate(R.layout.custom_popup_window, null,false);

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

        delete = (Button) mPopView.findViewById(R.id.delete);
        delete.setOnClickListener(this);
        delete.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                phone.setText("");
                return false;
            }
        });

        btCall = (ImageView) mPopView.findViewById(R.id.image_call);
        btDiss = (ImageView) mPopView.findViewById(R.id.image_diss);
        btCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String inputString = phone.getText().toString().trim();
                    if(TextUtils.isEmpty(inputString) || inputString.length()>8){
                        return;
                    }
                    if(inputString.length() == 6){
                        inputString = "88" + inputString;
                    }
                    int callId = Integer.parseInt(inputString);
                    int resultCode = -1;
                    if(MyTerminalFactory.getSDK().getConfigManager().getExtendAuthorityList().contains(Authority.AUTHORITY_CALL_PRIVATE.name())){
                        resultCode = MyTerminalFactory.getSDK().getIndividualCallManager().requestIndividualCall(callId,"");
                    }else {
                        ToastUtil.showToast(context,"没有个呼权限");
                        dismiss();
                        return;
                    }

                    if (resultCode == BaseCommonCode.SUCCESS_CODE){
                        Member member = DataUtil.getMemberByMemberNo(callId);
                        OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiveCurrentGroupIndividualCallHandler.class, member);
                    }else {
                        ToastUtil.individualCallFailToast(context, resultCode);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
//                text.setText("");
                dismiss();
            }
        });
        btDiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

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
        switch (v.getId()) {
            case R.id.dialNum10:
                if (phone.getText().length() < 8) {
                    play(11);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum1:
                if (phone.getText().length() < 8) {
                    play(1);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum2:
                if (phone.getText().length() < 8) {
                    play(2);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum3:
                if (phone.getText().length() < 8) {
                    play(3);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum4:
                if (phone.getText().length() < 8) {
                    play(4);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum5:
                if (phone.getText().length() < 8) {
                    play(5);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum6:
                if (phone.getText().length() < 8) {
                    play(6);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum7:
                if (phone.getText().length() < 8) {
                    play(7);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum8:
                if (phone.getText().length() < 8) {
                    play(8);
                    input(v.getTag().toString());
                }
                break;
            case R.id.dialNum9:
                if (phone.getText().length() < 8) {
                    play(9);
                    input(v.getTag().toString());
                }
                break;
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
            case R.id.delete:
                play(12);
                delete();
                break;

            default:
                break;
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
}
