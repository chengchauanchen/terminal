package cn.vsx.vc.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.ToastUtils;

import cn.vsx.vc.activity.MonitorGroupListActivity;

/**
 * Date:2020/5/7
 * Time:19:13
 * author: taozhenglin
 */
public class MyBroaddCoastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(MonitorGroupListActivity.ACTION)){
            String removelist = intent.getExtras().getString("removelist");
            ToastUtils.showShort(removelist);
        }

    }
}
