package ptt.terminalsdk.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptt.terminalsdk.service.FileExpireService;

public class FileExpireReceiver extends BroadcastReceiver {
    private Logger logger = LoggerFactory.getLogger(FileExpireReceiver.class);
    public static final String TAG = "FileTransferOperation---";
    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info(TAG+"FileExpireReceiver：收到文件过期没有上传的广播");
        context.startService(new Intent(context, FileExpireService.class));
    }
}
