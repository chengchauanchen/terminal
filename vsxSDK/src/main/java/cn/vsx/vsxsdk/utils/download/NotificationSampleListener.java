/*
 * Copyright (c) 2018 LingoChamp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.vsx.vsxsdk.utils.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;

import java.util.List;
import java.util.Map;

import cn.vsx.vsxsdk.R;

public class NotificationSampleListener extends DownloadListener4WithSpeed {
    private int totalLength;

    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private Runnable taskEndRunnable;
    private Context context;

    private NotificationCompat.Action action;

    private int loadStatus = 0;//

    public int getLoadStatus() {
        return loadStatus;
    }

    public NotificationSampleListener(Context context) {
        this.context = context.getApplicationContext();
    }

    public void attachTaskEndRunnable(Runnable taskEndRunnable) {
        this.taskEndRunnable = taskEndRunnable;
    }

    public void releaseTaskEndRunnable() {
        taskEndRunnable = null;
    }

    public void setAction(NotificationCompat.Action action) {
        this.action = action;
    }

    public void initNotification() {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String channelId = "okdownload";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationChannel channel = new NotificationChannel(
                    channelId,
                    context.getString(R.string.vsx_app_name),
                    NotificationManager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(context, channelId);


        builder.setDefaults(Notification.DEFAULT_LIGHTS)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle(context.getString(R.string.vsx_app_name))
                .setContentText("开始下载...")
                .setSmallIcon(R.mipmap.ic_launcher);

        if (action != null) {
            builder.addAction(action);
        }
    }

    @Override
    public void taskStart(@NonNull DownloadTask task) {
        Log.d("NotificationActivity", "taskStart");
        builder.setTicker("开始下载");
        builder.setOngoing(true);
        builder.setAutoCancel(false);
        builder.setContentText("开始下载");
        builder.setProgress(0, 0, true);
        manager.notify(task.getId(), builder.build());

        loadStatus = 1;//开始下载
    }

    @Override
    public void connectStart(@NonNull DownloadTask task, int blockIndex,
                             @NonNull Map<String, List<String>> requestHeaderFields) {
        builder.setTicker("connectStart");
//        builder.setContentText(
//                "The connect of " + blockIndex + " block for this task is connecting");
        builder.setContentText("正在连接");
        builder.setProgress(0, 0, true);
        manager.notify(task.getId(), builder.build());

        loadStatus = 2;//正在连接
    }

    @Override
    public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode,
                           @NonNull Map<String, List<String>> responseHeaderFields) {
        builder.setTicker("connectStart");
        //builder.setContentText("The connect of " + blockIndex + " block for this task is connected");
        builder.setContentText("连接完成");
        builder.setProgress(0, 0, true);
        manager.notify(task.getId(), builder.build());

        loadStatus = 3;//连接完成
    }

    @Override
    public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info,
                          boolean fromBreakpoint,
                          @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {
        Log.d("NotificationActivity", "infoReady " + info + " " + fromBreakpoint);

        if (fromBreakpoint) {
            builder.setTicker("fromBreakpoint");
        } else {
            builder.setTicker("fromBeginning");
        }
//        builder.setContentText(
//                "This task is download fromBreakpoint[" + fromBreakpoint + "]");

        builder.setContentText(
                "[" + fromBreakpoint + "]");

        builder.setProgress((int) info.getTotalLength(), (int) info.getTotalOffset(), true);
        manager.notify(task.getId(), builder.build());

        totalLength = (int) info.getTotalLength();
    }

    @Override
    public void progressBlock(@NonNull DownloadTask task, int blockIndex,
                              long currentBlockOffset,
                              @NonNull SpeedCalculator blockSpeed) {
    }

    @Override
    public void progress(@NonNull DownloadTask task, long currentOffset,
                         @NonNull SpeedCalculator taskSpeed) {
        Log.d("NotificationActivity", "progress " + currentOffset);

//        builder.setContentText("downloading with speed: " + taskSpeed.speed());
        builder.setContentText("正在下载" + taskSpeed.speed());
        builder.setProgress(totalLength, (int) currentOffset, false);
        manager.notify(task.getId(), builder.build());

        loadStatus = 4;//正在下载
    }

    @Override
    public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info,
                         @NonNull SpeedCalculator blockSpeed) {
    }

    @Override
    public void taskEnd(@NonNull final DownloadTask task, @NonNull EndCause cause,
                        @android.support.annotation.Nullable Exception realCause,
                        @NonNull SpeedCalculator taskSpeed) {
        Log.d("NotificationActivity", "taskEnd " + cause + " " + realCause);
        builder.setOngoing(false);
        builder.setAutoCancel(true);
        builder.setTicker("taskEnd " + cause);

        if (cause == EndCause.COMPLETED) {
            builder.setContentText("下载完成");
            builder.setProgress(1, 1, true);
            loadStatus = 5;//下载完成
        }else {
            builder.setContentText("下载异常");
            manager.notify(task.getId(), builder.build());
            loadStatus = 6;//下载异常
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (taskEndRunnable != null) taskEndRunnable.run();
                manager.notify(task.getId(), builder.build());
            }
            // because of on some android phone too frequency notify for same id would be
            // ignored.
        }, 100);
    }
}
