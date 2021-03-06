package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.vc.R;

public class ProgressDialog extends Dialog {
    private Context context;
    private ImageView img;
    private TextView txt;
    private ImageView iv_loading_bg;

    public ProgressDialog(Context context) {
		super(context, R.style.progress_dialog);
		this.context = context;
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.progress_dialog, null);
        iv_loading_bg = view.findViewById(R.id.iv_loading_bg);
        img =  view.findViewById(R.id.progress_dialog_img);
		txt =  view.findViewById(R.id.progress_dialog_txt);
		
		Animation anim = AnimationUtils.loadAnimation(context,
				R.anim.loading_dialog_progressbar_anim);
		img.setAnimation(anim);
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = ((WindowManager) windowManager).getDefaultDisplay();
//        android.view.WindowManager.LayoutParams p = getWindow().getAttributes();
//        p.height = (int) (display.getHeight() * 0.8);//
//        p.width = display.getWidth();
//        p.gravity = Gravity.TOP;
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);//解决dialog悬浮再activity上无法点击其他未被覆盖控件问题
//        this.getWindow().setAttributes(p);

        setContentView(view);
    }

    @Override
    public void show() {
    	super.show();
    	
		Animation anim = AnimationUtils.loadAnimation(context,
				R.anim.loading_dialog_progressbar_anim);
		img.setAnimation(anim);
    }

    public void setMsg(String msg){
            txt.setText(msg);
    }
    public void setMsg(int msgId){
            txt.setText(msgId);
    }
    public void setTextColor(int color){
    	txt.setTextColor(color);
    }
    public void setLoadingBgRes(int loading_bgRes){
        iv_loading_bg.setImageResource(loading_bgRes);
    }
    public void setLoadingRes(int loadingRes){
        img.setImageResource(loadingRes);
    }
}