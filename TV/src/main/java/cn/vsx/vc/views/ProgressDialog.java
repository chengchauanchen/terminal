package cn.vsx.vc.views;

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
        
    public ProgressDialog(Context context) {
		super(context, R.style.progress_dialog);
		this.context = context;
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.progress_dialog, null);
		img = (ImageView) view.findViewById(R.id.progress_dialog_img);
		txt = (TextView) view.findViewById(R.id.progress_dialog_txt);
		
		Animation anim = AnimationUtils.loadAnimation(context,
				R.anim.loading_dialog_progressbar_anim);
		img.setAnimation(anim);
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

}