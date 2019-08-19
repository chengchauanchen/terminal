package cn.vsx.uav.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.log4j.Logger;

import dji.common.error.DJIError;
import dji.sdk.media.DownloadListener;

public class DownloadHandler<B> implements DownloadListener<B> {

    private Logger logger = Logger.getLogger(getClass().getSimpleName());
    @Override
    public void onStart() {

    }

    @Override
    public void onRateUpdate(long total, long current, long arg2) {

    }

    @Override
    public void onProgress(long total, long current) {

    }

    @Override
    public void onSuccess(B obj) {
        if (obj instanceof Bitmap) {
            Bitmap bitmap = (Bitmap) obj;
            logger.info("Success! The bitmap's byte count is: " + bitmap.getByteCount());

        } else if (obj instanceof String) {
            logger.info("The file has been stored, its path is " + obj.toString());
        }
    }

    @Override
    public void onFailure(DJIError djiError) {

    }
}
