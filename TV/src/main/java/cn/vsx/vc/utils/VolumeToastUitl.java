package cn.vsx.vc.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.vsx.vc.R;

public class VolumeToastUitl {

    private static Toast toast;
    /**
     * 显示有image的toast 这是个view
     */
    public static void showToastWithImg(Context context, final String tvStr) {
        if (toast == null) {
            toast = new Toast(context.getApplicationContext());
        }
        View view = LayoutInflater.from(context).inflate(R.layout.layout_toast_volume, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_volume_toast);
        tv.setText(TextUtils.isEmpty(tvStr) ? "" : tvStr);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
