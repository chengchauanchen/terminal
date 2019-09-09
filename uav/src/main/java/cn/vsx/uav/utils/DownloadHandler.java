package cn.vsx.uav.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;

import org.apache.log4j.Logger;

import java.io.File;

import cn.vsx.uav.R;
import dji.common.error.DJIError;
import dji.sdk.media.DownloadListener;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;

public class DownloadHandler<B> implements DownloadListener<B> {

    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    private String fileName;
    public DownloadHandler(String fileName){
        this.fileName = fileName;
    }
    @Override
    public void onStart() {
        logger.info("开始下载onStart");
    }

    @Override
    public void onRateUpdate(long total, long current, long arg2) {
//        logger.info("onRateUpdate"+"---total:"+total+"---current:"+current);
    }

    @Override
    public void onProgress(long total, long current) {
//        logger.info("onProgress"+"---total:"+total+"---current:"+current);
    }

    @Override
    public void onSuccess(B obj) {
        if (obj instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) obj;
            logger.info("Success! The bitmap's byte count is: " + bitmap.getByteCount());

        } else if (obj instanceof String) {
            ToastUtils.showShort(R.string.uav_picture_download_finish);
            String path = obj.toString();
            logger.info("The file has been stored, its path is " + path);
            File file = new File(path+File.separator+fileName+".jpg");
            Log.e("DownloadHandler", file.getPath());
            if(file.exists() && file.isFile()){
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                operation.generateFileComplete(file.getParent(),file.getPath());
            }
        }
    }

    @Override
    public void onFailure(DJIError djiError) {
        ToastUtils.showShort("下载照片失败："+djiError.getDescription());
        logger.error("下载照片失败："+djiError.getDescription());
    }
}
