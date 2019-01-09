package ptt.terminalsdk.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

/**
 * 作者：xuxiaolong
 * 创建日期：2018/8/16
 * 描述：后台进程保活管理类
 */

public class KeepLiveManager{
    /**
     * 前台进程的NotificationId
     */
    private final static int GRAY_SERVICE_ID = 456;

    /**
     * 单例模式
     */
    private static KeepLiveManager instance = new KeepLiveManager();



    public static KeepLiveManager getInstance(){
        return instance;
    }


    /**
     * 设置服务为前台服务
     * @param service
     */
    public void setServiceForeground(Service service){


        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT < 18) {
            //Android4.3以下 ，此方法能有效隐藏Notification上的图标
            service.startForeground(GRAY_SERVICE_ID, new Notification());
        } else if(Build.VERSION.SDK_INT <Build.VERSION_CODES.O){
            //Android4.3 - Android7.0，此方法能有效隐藏Notification上的图标
            Intent innerIntent = new Intent(service, GrayInnerService.class);
            service.startService(innerIntent);
            service.startForeground(GRAY_SERVICE_ID, new Notification());
        }
//        else if(Build.VERSION.SDK_INT <Build.VERSION_CODES.O){
//            //Android7.1 google修复了此漏洞，暂无解决方法（现状：Android7.1以上app启动后通知栏会出现一条"正在运行"的通知消息）
//            service.startForeground(GRAY_SERVICE_ID, new Notification());
//        }
        else{
            startMyOwnForeground(service);
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(Service service, String channelId, String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        if(null !=manager){
            manager.createNotificationChannel(chan);
        }
        return channelId;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground(Service service){
        String channelId = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            channelId = createNotificationChannel(service,"cn.vsx.vc", "Background Service");
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service.getApplicationContext(), channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        service.startForeground(2, notification);
    }

    /**
     * 辅助Service
     */
    public static class GrayInnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
