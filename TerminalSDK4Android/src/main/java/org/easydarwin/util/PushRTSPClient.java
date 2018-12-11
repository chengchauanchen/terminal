package org.easydarwin.util;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.ResultReceiver;

import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.Pusher;

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

    public PushRTSPClient(Context context, String PLAYKEY,
                     SurfaceTexture surfaceTexture, ResultReceiver mResultReceiver){
        pusher = new EasyPusher();
        easyRTSPPlayer = new EasyRTSPPlayer(context,PLAYKEY,surfaceTexture,mResultReceiver);
    }

    public int start(final String url, int type, int mediaType, String user, String pwd, String recordPath){
        return easyRTSPPlayer.start(url,type,mediaType,user,pwd,recordPath);
    }

    public void stop(){
        easyRTSPPlayer.stop();
    }

    public void setRTSPInfo(String ip,String port,String id, InitCallback callBack){
        easyRTSPPlayer.setRTSPInfo(pusher,ip,port,id,callBack);
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture){
        easyRTSPPlayer.setSurfaceTexture(surfaceTexture);
    }
}
