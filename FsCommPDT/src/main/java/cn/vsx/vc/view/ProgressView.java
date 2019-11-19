package cn.vsx.vc.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.StyleRes;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.vsx.vc.R;

/**
 *
 */
public class ProgressView extends RelativeLayout{

    private ImageView img;
    private TextView txt;
    private ImageView iv_loading_bg;

    public ProgressView(Context context){
        super(context);
        initView();
    }

    public ProgressView(Context context, AttributeSet attrs){
        super(context, attrs);
        initView();
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        String infServie = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(infServie);
        if(layoutInflater !=null){
            View view = layoutInflater.inflate(R.layout.progress_view, this, true);

            iv_loading_bg = view.findViewById(R.id.iv_loading_bg);
            img =  view.findViewById(R.id.progress_dialog_img);
            txt =  view.findViewById(R.id.progress_dialog_txt);

            Animation anim = AnimationUtils.loadAnimation(getContext(),
                    R.anim.loading_dialog_progressbar_anim);
            img.setAnimation(anim);
        }
    }
}
