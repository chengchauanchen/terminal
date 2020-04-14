package cn.vsx.vc.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import cn.vsx.vc.R;
import cn.vsx.vc.application.MyApplication;
import cn.vsx.vc.receiver.VideoMeetingNotificationClickReceiver;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.ApkUtil;
import ptt.terminalsdk.tools.ToastUtil;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * @author martian on 2020/4/2.
 */
public class VideoMeetingNotification {

  //通知栏标题
  private static String noticeTitle = "会商进行中";
  //通知栏内容
  private static String noticeContent = "点击返回会商界面";
  //通知Id
  private static int noticeId = 20200402;

  public static String CHANNEL_ID = "cn.vsx.vc.videomeeting";
  public static void createNotification(){
    try {
      Intent intent = new Intent(MyTerminalFactory.getSDK().application, VideoMeetingNotificationClickReceiver.class);
      PendingIntent
          pIntent = PendingIntent.getBroadcast(MyTerminalFactory.getSDK().application, noticeId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      NotificationManager notificationManager = (NotificationManager) MyTerminalFactory.getSDK().application.getSystemService(NOTIFICATION_SERVICE);
      Notification notification = null;
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        String channelId = createNotificationChannel();
        NotificationCompat.Builder myBuilder = new NotificationCompat.Builder(MyTerminalFactory.getSDK().application, channelId);
        myBuilder.setContentTitle(noticeTitle)//设置通知标题
            .setContentText(noticeContent)//设置通知内容
            .setTicker(noticeTitle)//设置状态栏提示消息
            .setSmallIcon(ApkUtil.isWuTie()? R.mipmap.ic_launcher_wutie: R.mipmap.ic_launcher)//设置通知图标
            .setAutoCancel(true)//点击后取消
            .setWhen(System.currentTimeMillis())//设置通知时间
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .setContentIntent(pIntent)//设置通知点击事件
            .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);
         notification = myBuilder.build();
      }else {
        Notification.Builder myBuilder = new Notification.Builder(MyTerminalFactory.getSDK().application);
        myBuilder.setContentTitle(noticeTitle)//设置通知标题
            .setContentText(noticeContent)//设置通知内容
            .setTicker(noticeTitle)//设置状态栏提示消息
            .setSmallIcon(ApkUtil.isWuTie()? R.mipmap.ic_launcher_wutie: R.mipmap.ic_launcher)//设置通知图标
            .setAutoCancel(true)//点击后取消
            .setOngoing(false)
            .setWhen(System.currentTimeMillis())//设置通知时间
            .setPriority(Notification.PRIORITY_HIGH)//高优先级
            .setContentIntent(pIntent)//设置通知点击事件
            .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
          //设置任何情况都会显示通知
          myBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        notification = myBuilder.build();
        //通过通知管理器来发起通知，ID区分通知
      }
      if(null != notificationManager){
        //notification.flags = Notification.FLAG_ONGOING_EVENT|Notification.FLAG_AUTO_CANCEL; // 设置常驻 Flag
        notificationManager.notify(noticeId,notification);
      }
      checkNotificatonPermission();
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  private static String createNotificationChannel(){
    NotificationChannel chan = new NotificationChannel(CHANNEL_ID,
        "视频会商", NotificationManager.IMPORTANCE_HIGH);
    chan.setLightColor(Color.BLUE);
    chan.setSound(null,null);
    chan.enableVibration(false);
    chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    NotificationManager manager = (NotificationManager) MyTerminalFactory.getSDK().application.getSystemService(Context.NOTIFICATION_SERVICE);
    if(null !=manager){
      manager.createNotificationChannel(chan);
    }
    return CHANNEL_ID;
  }


  // 取消通知
  public static void cancelNotification() {
    try{
      NotificationManager notificationManager = (NotificationManager) MyTerminalFactory.getSDK().application.getSystemService(NOTIFICATION_SERVICE);
      notificationManager.cancel(noticeId);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 检查通知的权限
   */
  private static void checkNotificatonPermission() {
    if(!VideoMeetingNotification.checkNotificationIsOpen()){
      VideoMeetingNotification.openNotificationSetting();
      ToastUtil.showToast(String.format(
          MyApplication.instance.getApplicationContext().getString(R.string.text_next_count),
          MyApplication.instance.getApplicationContext().getString(R.string.app_name)));
    }else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if(!VideoMeetingNotification.checkNotificatonChannelIsOpen(VideoMeetingNotification.CHANNEL_ID)){
          VideoMeetingNotification.openNotificationChannelSetting(VideoMeetingNotification.CHANNEL_ID);
          ToastUtil.showToast(MyApplication.instance.getApplicationContext().getString(R.string.please_open_video_meeting_notification));
        }
      }
    }
  }

  /**
   * 判断是否开启通知权限
   * @return
   */
  public static boolean checkNotificationIsOpen(){
    try{
      NotificationManagerCompat notification = NotificationManagerCompat.from(MyTerminalFactory.getSDK().application);
      return notification.areNotificationsEnabled();
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  public static  boolean checkNotificatonChannelIsOpen(String channelId){
    try{
      NotificationManager notificationManager = (NotificationManager) MyTerminalFactory.getSDK().application.getSystemService(NOTIFICATION_SERVICE);
      NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
      return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }catch (Exception e){
      e.printStackTrace();
    }
    return false;
  }

  /**
   * 打开通知的设置页面
   */
  public static void openNotificationSetting(){
    try{
      Intent intent = new Intent();
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, MyApplication.instance.getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, MyApplication.instance.getApplicationInfo().uid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", MyApplication.instance.getPackageName());
        intent.putExtra("app_uid", MyApplication.instance.getApplicationInfo().uid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      }else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT ){
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.fromParts("package", MyApplication.instance.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      }else{
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_SETTINGS);
      }
      MyApplication.instance.startActivity(intent);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  /**
   * 打开通知的设置页面
   */
  public static void openNotificationChannelSetting(String channelId){
    try{
      Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
      intent.putExtra(Settings.EXTRA_APP_PACKAGE, MyApplication.instance.getPackageName());
      intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
      MyApplication.instance.startActivity(intent);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

}
