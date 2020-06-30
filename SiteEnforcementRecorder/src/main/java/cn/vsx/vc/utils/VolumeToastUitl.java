package cn.vsx.vc.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;

public class VolumeToastUitl {

    private static Toast toast;
    private static Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 显示有image的toast 这是个view
     */
    public static void showToastWithImg(final String tvStr) {
        handler.post(() -> {
            if (toast == null) {
                toast = new Toast(MyApplication.instance);
            }
            View view = LayoutInflater.from(MyApplication.instance).inflate(R.layout.layout_toast_volume, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_volume_toast);
            tv.setText(TextUtils.isEmpty(tvStr) ? "" : tvStr);
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });
    }
}
