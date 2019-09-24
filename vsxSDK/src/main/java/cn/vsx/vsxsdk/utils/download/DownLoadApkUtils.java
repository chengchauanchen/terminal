package cn.vsx.vsxsdk.utils.download;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.UnifiedListenerManager;

import java.util.ArrayList;
import java.util.List;

import cn.vsx.vsxsdk.R;
import cn.vsx.vsxsdk.utils.SystemUtil;

public class DownLoadApkUtils {
    private Context activity;
    private DownloadTask task;
    private NotificationSampleListener listener;
    private CancelReceiver cancelReceiver;
    private final InstallUtils mInstallUtil;

    private int redownloadCount = 0;

    private static DownLoadApkUtils downLoadApkUtils;

//    private static String url_temp = "http://192.168.1.100:6063/u/phone_common_08-28_1.0.40_40.apk";
    private static String url_temp = "http://192.168.20.189:6063/u/phone.apk";

    public static DownLoadApkUtils getInstance(Context activity) {
        if(downLoadApkUtils==null){
            downLoadApkUtils = new DownLoadApkUtils(activity);
        }
        return downLoadApkUtils;
    }

    public DownLoadApkUtils(Context activity) {
        this.activity = activity;
        //String fileName = FileUtils.getRootPath().getPath() + File.separator + activity.getString(R.string.vsx_app_name) + ".apk";
        mInstallUtil = new InstallUtils(activity);
    }



    /**
     *
     */
    public void startDownLoadApk(Context context){
        String packageName = "cn.vsx.vc";//要打开应用的包名
        if(!SystemUtil.isAvilible(context,packageName)){
            Log.e("--vsx--","未安装融合通信app:"+packageName);
            startDownLoadApk(url_temp);
        }
    }

    public void startDownLoadApk(String url) {
        Log.e("--vsx--","正在下载");
        initTask(url);
        initListener();
        // for cancel action on notification.
        IntentFilter filter = new IntentFilter(CancelReceiver.ACTION);
        cancelReceiver = new CancelReceiver(task);
        activity.registerReceiver(cancelReceiver, filter);

        GlobalTaskManager.getImpl().attachListener(task, listener);
        GlobalTaskManager.getImpl().addAutoRemoveListenersWhenTaskEnd(task.getId());
        if (StatusUtil.isSameTaskPendingOrRunning(task)) {
//           actionTv.setText("取消");
//           actionView.setTag(new Object());
        }
        // need to start
        GlobalTaskManager.getImpl().enqueueTask(task, listener);
    }


    private void initTask(String url) {
        task = new DownloadTask
                .Builder(url, FileUtils.getRootPath())
                .setFilename(activity.getString(R.string.vsx_app_name) + ".apk")
                // if there is the same task has been completed download, just delete it and
                // re-download automatically.
                .setPassIfAlreadyCompleted(false)
                .setMinIntervalMillisCallbackProcess(80)
                // because for the notification we don't need make sure invoke on the ui thread, so
                // just let callback no need callback to the ui thread.
                .setAutoCallbackToUIThread(false)
                .build();
    }


    private void initListener() {
        listener = new NotificationSampleListener(activity);
        listener.attachTaskEndRunnable(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                if (listener.getLoadStatus() == 5) {//下载完成
                    redownloadCount = 0;
                    installApk();
                    Log.e("DownLoadApk", "下载完成后,开始自动安装");
                } else if (listener.getLoadStatus() == 6) {//下载失败
                    Log.e("DownLoadApk","下载异常，重新下载"+redownloadCount+"次");
                    //重新下载 3次
                    if(redownloadCount<10){
                        GlobalTaskManager.getImpl().enqueueTask(task, listener);
                        redownloadCount++;
                    }else{
                        redownloadCount = 0;
                    }
                }
            }
        });

        final Intent intent = new Intent(CancelReceiver.ACTION);
        final PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(activity, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        listener.setAction(new NotificationCompat.Action(0, "", cancelPendingIntent));
        listener.initNotification();
    }


    static class CancelReceiver extends BroadcastReceiver {
        static final String ACTION = "cancelOkdownload";

        private DownloadTask task;

        CancelReceiver(@NonNull DownloadTask task) {
            this.task = task;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //this.task.cancel();
        }
    }

    static class GlobalTaskManager {
        private UnifiedListenerManager manager;

        private GlobalTaskManager() {
            manager = new UnifiedListenerManager();
        }

        private static class ClassHolder {
            private static final GlobalTaskManager INSTANCE = new GlobalTaskManager();
        }

        static GlobalTaskManager getImpl() {
            return GlobalTaskManager.ClassHolder.INSTANCE;
        }

        void addAutoRemoveListenersWhenTaskEnd(int id) {
            manager.addAutoRemoveListenersWhenTaskEnd(id);
        }

        void attachListener(@NonNull DownloadTask task, @NonNull DownloadListener listener) {
            manager.attachListener(task, listener);
        }

        void enqueueTask(@NonNull DownloadTask task,
                         @NonNull DownloadListener listener) {
            manager.enqueueTaskWithUnifiedListener(task, listener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void installApk() {
        mInstallUtil.install(task.getFile().getAbsolutePath());

//        //如果为8.0以上系统，则判断是否有 未知应用安装权限
//        if (!LoaderPermissionUtils.isHasInstallPermissionWithO(activity)) {
//            LoaderPermissionUtils.startInstallPermissionSettingActivity(activity);
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        String filePath = FileUtils.getRootPath().getPath() + File.separator + activity.getString(R.string.xiaoma_app_name) + ".apk";
//        File file = new File(filePath);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (Build.VERSION.SDK_INT >= 24) {//大于7.0使用此方法
//            Uri apkUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileProvider", file);///-----ide文件提供者名
////            //添加这一句表示对目标应用临时授权该Uri所代表的文件
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//        } else {//小于7.0就简单了
//            // 由于没有在Activity环境下启动Activity,设置下面的标签
//            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        }
//        activity.startActivity(intent);
    }

//    public void checkVersion(CheckVersionBean checkVersionBean) {
//        Integer netVersionCod = checkVersionBean.getVersion();
//        int currentVersionCode = ComConstant.VERSION_CODE;
//        if (currentVersionCode < netVersionCod) {//有新版本
//            if(checkVersionBean.getState()==1 && checkVersionBean.getPromptUpdate()==1){//上架
//                if(checkVersionBean.getForceUpdate()==1){//强制升级
//                    checkVersionDialog(checkVersionBean,true);
//                }else{//非强制升级
//                    checkVersionDialog(checkVersionBean,false);
//                }
//            }
//        }
//    }

//    private void downLoadApk(CheckVersionBean updateInfo){
//        //判断网络情况 非wifi下提示用户
//        NetWorkUtils netWorkUtils = new NetWorkUtils(activity);
//        int type = netWorkUtils.getNetType();
//        if (type != 1) {//0: 无网络， 1:WIFI， 2:其他（流量）
//            showNetDialog(updateInfo);
//        } else {
//            startDownLoadApk(updateInfo.getUrl());
//        }
//    }

//    /**
//     * 2014-10-27新增流量提示框，当网络为数据流量方式时，下载就会弹出此对话框提示
//     *
//     * @param updateInfo
//     */
//    private void showNetDialog(final CheckVersionBean updateInfo) {
//        AlertDialog.Builder netBuilder = new AlertDialog.Builder(activity);
//        netBuilder.setTitle("下载提示");
//        netBuilder.setMessage("您在目前的网络环境下继续下载将可能会消耗手机流量，请确认是否继续下载？");
//        netBuilder.setNegativeButton("取消下载",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//        netBuilder.setPositiveButton("继续下载",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        startDownLoadApk(updateInfo.getUrl());
//                    }
//                });
//        AlertDialog netDialog = netBuilder.create();
//        netDialog.show();
//        netDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
//        netDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
//        netDialog.setCanceledOnTouchOutside(updateInfo.getForceUpdate() != 1);//是否强制升级
//    }

    //非强制升级 提示
//    private void checkVersionDialog(final CheckVersionBean updateInfo, final boolean isForce) {
//        final AlertDialog updateDialog = new AlertDialog.Builder(activity).create();
//        updateDialog.show();
//        Window window = updateDialog.getWindow();
//        window.setContentView(R.layout.dialog_load_apk_layout);
//        TextView contentTextView = window.findViewById(R.id.content);
//        TextView titleTextView = window.findViewById(R.id.title);
//        View view_line = window.findViewById(R.id.view_line);
//        final TextView updateButton = window.findViewById(R.id.updateBtn);
//        TextView cancelBtn = window.findViewById(R.id.cancelBtn);
//
//        titleTextView.setText("发现新版本("+updateInfo.getVersion()+")");
//        contentTextView.setText(updateInfo.getMessage());
//        contentTextView.setGravity(Gravity.LEFT);
//        updateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //如果为8.0以上系统，则判断是否有 未知应用安装权限
//                if (!LoaderPermissionUtils.isHasInstallPermissionWithO(activity)) {
//                    authorDialog();
//                    return;
//                }
//                updateDialog.dismiss();
//                downLoadApk(updateInfo);
//            }
//        });
//        view_line.setVisibility(isForce? View.GONE: View.VISIBLE);
//        cancelBtn.setVisibility(isForce? View.GONE: View.VISIBLE);
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                updateDialog.dismiss();
//            }
//        });
//        Display d = activity.getWindow().getWindowManager().getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams layoutParams = updateDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
//        layoutParams.width = (int) (d.getWidth() * 0.9); // 宽度设置为屏幕的0.85
//        updateDialog.getWindow().setAttributes(layoutParams);
//        updateDialog.setCancelable(!isForce);
//    }

    //授权提示
    private void authorDialog() {
//        final AlertDialog updateDialog = new AlertDialog.Builder(activity).create();
//        updateDialog.show();
//        Window window = updateDialog.getWindow();
//        window.setContentView(R.layout.dialog_load_apk_layout);
//        TextView contentTextView = window.findViewById(R.id.content);
//        TextView titleTextView = window.findViewById(R.id.title);
//        View view_line = window.findViewById(R.id.view_line);
//        final TextView updateButton = window.findViewById(R.id.updateBtn);
//        TextView cancelBtn = window.findViewById(R.id.cancelBtn);
//        titleTextView.setText("开启权限");
//        contentTextView.setText("安装应用需要打开未知来源权限，请去设置中开启权限");
//        contentTextView.setGravity(Gravity.LEFT);
//        updateButton.setText("去设置");
//        cancelBtn.setText("取消");
//        updateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateDialog.dismiss();
//                LoaderPermissionUtils.startInstallPermissionSettingActivity(activity);
//            }
//        });
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                updateDialog.dismiss();
//            }
//        });
//        Display d = activity.getWindow().getWindowManager().getDefaultDisplay(); // 获取屏幕宽、高用
//        WindowManager.LayoutParams layoutParams = updateDialog.getWindow().getAttributes(); // 获取对话框当前的参数值
//        layoutParams.width = (int) (d.getWidth() * 0.9); // 宽度设置为屏幕的0.85
//        updateDialog.getWindow().setAttributes(layoutParams);
//        updateDialog.setCancelable(true);
    }


    public void onDestroy() {
        if (cancelReceiver != null) {
            activity.unregisterReceiver(cancelReceiver);
        }
        if (listener != null) {
            listener.releaseTaskEndRunnable();
        }
    }

}
