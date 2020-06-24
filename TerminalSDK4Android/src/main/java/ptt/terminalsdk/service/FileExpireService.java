package ptt.terminalsdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptt.terminalsdk.context.MyTerminalFactory;

public class FileExpireService extends Service {
    private Logger logger = LoggerFactory.getLogger(FileExpireService.class);
    public static final String TAG = "FileTransferOperation---";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info(TAG + "FileExpireService：处理48小时未上传的文件");
        KeepLiveManager.getInstance().setServiceForeground(this);
        MyTerminalFactory.getSDK().getFileTransferOperation().uploadFileByExpire(false);
        return super.onStartCommand(intent, flags, startId);
    }
}
