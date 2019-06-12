package cn.vsx.vc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.vsx.vc.utils.ToastUtil;

public class TempGroupExpireReceiver extends BroadcastReceiver {
    public static final String ACTION_TEMP_GROUP_EXPIRE = "action_temp_group_expire";
    public static final String EXTRA_GROUP_NAME = "extra_group_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_TEMP_GROUP_EXPIRE.equals(action)) {
            String name = intent.getStringExtra(EXTRA_GROUP_NAME);
            System.out.println("收到广播了！！！！！");
            ToastUtil.showToast(context, name + "到期！");
        }
    }
}
