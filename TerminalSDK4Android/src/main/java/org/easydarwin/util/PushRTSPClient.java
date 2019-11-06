package org.easydarwin.util;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;

import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.Pusher;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.FileTransgerUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/11/26
 * 描述：
 * 修订历史：
 */
public class PushRTSPClient{

    private EasyRTSPPlayer easyRTSPPlayer;
    private Pusher pusher;

    private String tempFile;
    private String dataStr;
    private long millis;
    private String fileIndex;
    private static final int VIDEO_RECODE_PER_TIME = 5 * 60 * 1000;

    public PushRTSPClient(Context context, String PLAYKEY,
                          SurfaceTexture surfaceTexture, ResultReceiver mResultReceiver){
        pusher = new EasyPusher();
        easyRTSPPlayer = new EasyRTSPPlayer(context,PLAYKEY,surfaceTexture,mResultReceiver);
    }

    public void start(final String url, int type, int mediaType, String user, String pwd){
//        if (TextUtils.isEmpty(tempFile)) {
            FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
            operation.checkExternalUsableSize();
            dataStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            millis = PreferenceManager.getDefaultSharedPreferences(MyTerminalFactory.getSDK().application).getInt("record_interval", VIDEO_RECODE_PER_TIME);
            fileIndex = FileTransgerUtil.getRecodeFileIndex(1);
            String fileName = FileTransgerUtil.getVideoRecodeFileName(dataStr, fileIndex);
            File videoRecord = new File(MyTerminalFactory.getSDK().getBITVideoRecordesDirectoty(operation.getExternalUsableStorageDirectory()), fileName);
            if (!videoRecord.exists()) {
                videoRecord.getParentFile().mkdirs();
            }
            tempFile = videoRecord.toString();
//        }
        easyRTSPPlayer.start(url,type,mediaType,user,pwd,tempFile);
    }

    public void stop(){
        tempFile = null;
        easyRTSPPlayer.stop();
    }

    public void stopRecord(){
        easyRTSPPlayer.stopRecord();
    }


    public void setRTSPInfo(String ip,String port,String id, InitCallback callBack){
        easyRTSPPlayer.setRTSPInfo(pusher,ip,port,id,callBack);
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        easyRTSPPlayer.setSurfaceTexture(surfaceTexture);
    }
}
