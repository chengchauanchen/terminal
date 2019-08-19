package cn.vsx.uav.service;

import android.content.Intent;

import cn.vsx.uav.activity.UavPushActivity;
import cn.vsx.vc.service.ReceiveLiveCommingService;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/15
 * 描述：
 * 修订历史：
 */
public class UavReceiveLiveCommingService extends ReceiveLiveCommingService{

    @Override
    protected void onAcceptLive(){
        Intent intent = new Intent(getApplicationContext(), UavPushActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
