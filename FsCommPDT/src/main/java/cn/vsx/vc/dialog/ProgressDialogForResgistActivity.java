package cn.vsx.vc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;

import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * Date:2020/4/17
 * Time:17:32
 * author: taozhenglin
 */
public class ProgressDialogForResgistActivity extends Dialog {
    private final TextView tv_uoloadLog;
    private Context context;
    private ImageView img;
    private TextView txt;
    private ImageView iv_loading_bg;//上传日志
    private TextView tv_apk_version;//版本号
    private Timer timer = new Timer();
    static ProgressDialogForResgistActivity progressDialogForResgistActivity;

    public static ProgressDialogForResgistActivity getInstance(Context context) {
        if (progressDialogForResgistActivity==null){
            synchronized (ProgressDialogForResgistActivity.class){
                if (progressDialogForResgistActivity==null){
                    progressDialogForResgistActivity=new ProgressDialogForResgistActivity(context);
                }
            }

        }
    return progressDialogForResgistActivity;
    }

    public ProgressDialogForResgistActivity(@NonNull Context context) {
        super(context, R.style.progress_dialog);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.progress_dialog_for_resgit, null);
        iv_loading_bg = view.findViewById(R.id.iv_loading_bg);
        img = view.findViewById(R.id.progress_dialog_img);
        txt = view.findViewById(R.id.progress_dialog_txt);

        tv_apk_version = (TextView) view.findViewById(R.id.tv_apk_version);
        tv_apk_version.setText(MyTerminalFactory.getSDK().getVersionName());

        tv_uoloadLog = (TextView) view.findViewById(R.id.tv_uoloadLog);
        tv_uoloadLog.setOnClickListener(new MyOnClickListener());
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Window window = this.getWindow();
        window.setWindowAnimations(R.style.progress_dialog_for_regist);
//        window.setEnterTransition();
//        Animation anim = AnimationUtils.loadAnimation(context,
//                R.anim.loading_dialog_progressbar_anim);
//        img.setAnimation(anim);
        setContentView(view);
    }

    @Override
    public void show() {
        super.show();

        Animation anim = AnimationUtils.loadAnimation(context,
                R.anim.loading_dialog_progressbar_anim);
        img.setAnimation(anim);
    }


    public void onDismiss(){
        try{
            dismiss();
            progressDialogForResgistActivity = null;
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

    public void setLoadingBgRes(int loading_bgRes) {
        iv_loading_bg.setImageResource(loading_bgRes);
    }

    public void setLoadingRes(int loadingRes) {
        img.setImageResource(loadingRes);
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //点击上传日志
                case R.id.tv_uoloadLog:
                    if (loadLogClickListeren != null) {
                        loadLogClickListeren.onUploadLogClickListeren();
                    }
                    break;
            }
        }
    }

    private long currentTime;

    public void uploadLog() {
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Log.d("result:","开始上传");
//                ToastUtil.showToast(context.getString(R.string.log_uploading));
//                if (System.currentTimeMillis() - currentTime > 5000) {
//                    MyTerminalFactory.getSDK().getLogFileManager().uploadAllLogFile();
//                    currentTime = System.currentTimeMillis();
//                } else {
//                    ToastUtil.showToast(context.getString(R.string.text_uploaded_log_try_again_later));
//                }
//            }
//        }, 0);

    }

    public interface UpLoadLogClickListeren {
        void onUploadLogClickListeren();
    }

    private UpLoadLogClickListeren loadLogClickListeren;

    public void setUpLoadLogClickListeren(UpLoadLogClickListeren loadLogClickListerenr) {
        this.loadLogClickListeren = loadLogClickListerenr;
    }
}
