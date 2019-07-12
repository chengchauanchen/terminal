package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.vsx.vc.R;

public class MyProgressDialog extends Dialog {
    private Context context;

    private RelativeLayout rl_loading;
    private ImageView iv_loading;
    private TextView tv_loading;

    private LinearLayout home_loaded_layout;
    private ImageView iv_result;
    private TextView tv_result;

    private int type = 1;
    public static final int TYPE_WAIT = 1;//加载
    public static final int TYPE_COMPLETE_SUCCESS = 2;//成功
    public static final int TYPE_COMPLETE_ERROR = 3;//失败
    private String resultContent = "";
    private static final int PROGRESS_TIME = 2*1000;

    protected Handler handler = new Handler(Looper.getMainLooper());

    public MyProgressDialog(Context context, int type,String resultContent) {
		super(context, R.style.my_progress_dialog);
		this.context = context;
        this.type = type;
        this.resultContent = resultContent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_progress_dialog);
        initView();
        setView();
        init();
    }


    private void initView() {
        rl_loading = (RelativeLayout)findViewById(R.id.rl_loading);
        iv_loading = (ImageView) findViewById(R.id.iv_loading);
        tv_loading = (TextView) findViewById(R.id.tv_loading);

        home_loaded_layout = (LinearLayout)findViewById(R.id.home_loaded_layout);
        iv_result = (ImageView) findViewById(R.id.iv_result);
        tv_result = (TextView) findViewById(R.id.tv_result);
    }

    private void init() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
//        layoutParams.width= (int) (width*0.9);
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    /**
     * 设置控件
     */
    private void setView() {
        switch (type){
            case TYPE_WAIT:
                rl_loading.setVisibility(View.VISIBLE);
                Animation anim1 = AnimationUtils.loadAnimation(context,
                        R.anim.loading_dialog_progressbar_anim);
                iv_loading.setAnimation(anim1);
                tv_loading.setText(resultContent);
                home_loaded_layout.setVisibility(View.GONE);
                break;
            case TYPE_COMPLETE_SUCCESS:
                rl_loading.setVisibility(View.GONE);
                Animation anim2 = iv_loading.getAnimation();
                if(anim2!=null){
                    anim2.cancel();
                }
                home_loaded_layout.setVisibility(View.VISIBLE);
                iv_result.setImageResource(R.drawable.icon_load_result_success);
                tv_result.setTextColor(context.getResources().getColor(R.color.load_result_success));
                tv_result.setText(resultContent);
                break;
            case TYPE_COMPLETE_ERROR:
                rl_loading.setVisibility(View.GONE);
                Animation anim3 = iv_loading.getAnimation();
                if(anim3!=null){
                    anim3.cancel();
                }
                home_loaded_layout.setVisibility(View.VISIBLE);
                iv_result.setImageResource(R.drawable.icon_load_result_error);
                tv_result.setTextColor(context.getResources().getColor(R.color.load_result_error));
                tv_result.setText(resultContent);
                break;
        }
    }

    @Override
    public void show() {
        super.show();
        if(type != TYPE_WAIT){
            handler.postDelayed(this::dismiss,PROGRESS_TIME);
        }
    }

    /**
     * 设置不同状态
     * @param type
     * @param resultContent
     */
    public void setResult(int type,String resultContent){
        this.type = type;
        this.resultContent = resultContent;
        handler.post(this::setView);
        if(type != TYPE_WAIT){
            handler.postDelayed(this::dismiss,PROGRESS_TIME);
        }
    }

}