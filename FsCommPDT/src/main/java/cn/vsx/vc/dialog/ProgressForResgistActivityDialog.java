package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Date:2020/4/17
 * Time:17:32
 * author: taozhenglin
 */
public class ProgressForResgistActivityDialog extends Dialog {
    private Context context;
    private ImageView img;
    private TextView txt;
    //加载图标
    private ImageView ivLoadingBg;
    static ProgressForResgistActivityDialog progressForResgistActivityDialog;

    public static ProgressForResgistActivityDialog getInstance(Context context) {
        if (progressForResgistActivityDialog==null){
            synchronized (ProgressForResgistActivityDialog.class){
                if (progressForResgistActivityDialog==null){
                    progressForResgistActivityDialog=new ProgressForResgistActivityDialog(context);
                }
            }
        }
    return progressForResgistActivityDialog;
    }

    public ProgressForResgistActivityDialog(@NonNull Context context) {
        super(context, R.style.progress_dialog);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.progress_dialog_for_resgit, null);
        ivLoadingBg = view.findViewById(R.id.iv_loading_bg);
        img = view.findViewById(R.id.progress_dialog_img);
        txt = view.findViewById(R.id.progress_dialog_txt);

        TextView tvApkVersion = (TextView) view.findViewById(R.id.tv_apk_version);
        tvApkVersion.setText(MyTerminalFactory.getSDK().getVersionName());
        view.findViewById(R.id.tv_uoloadLog).setOnClickListener(new MyOnClickListener());
        Window window = this.getWindow();
        window.setWindowAnimations(R.style.progress_dialog_for_regist);
        setContentView(view);
    }

    @Override
    public void show() {
        super.show();
        if(img!=null){
            Animation anim = AnimationUtils.loadAnimation(context,
                    R.anim.loading_dialog_progressbar_anim);
            img.setAnimation(anim);
        }
    }

    /**
     * 停止加载动画
     */
    public void onStopAnimation(){
        try{
            if(img!=null){
                img.clearAnimation();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void onDismiss(){
        try{
            dismiss();
            progressForResgistActivityDialog = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void setMsg(String msg) {
        txt.setText(msg);
    }

    public void setMsg(int msgId) {
        txt.setText(msgId);
    }

    public void setTextColor(int color) {
        txt.setTextColor(color);
    }

    public void setLoadingBgRes(int loadingBgRes) {
        ivLoadingBg.setImageResource(loadingBgRes);
    }

    public void setLoadingRes(int loadingRes) {
        img.setImageResource(loadingRes);
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.tv_uoloadLog){
                if (loadLogClickListener != null) {
                    loadLogClickListener.onUploadLogClickListener();
                }
            }
        }
    }
    public interface UpLoadLogClickListener {
        void onUploadLogClickListener();
    }
    private UpLoadLogClickListener loadLogClickListener;
    public void setUpLoadLogClickListeren(UpLoadLogClickListener loadLogClickListener) {
        this.loadLogClickListener = loadLogClickListener;
    }
}
