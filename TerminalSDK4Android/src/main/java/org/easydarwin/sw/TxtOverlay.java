package org.easydarwin.sw;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by John on 2017/2/23.
 */

public class TxtOverlay {

    static {
        System.loadLibrary("TxtOverlay");
    }

    private final Context context;

    public TxtOverlay(Context context){
        this.context = context;
    }
    private long ctx;

    public void init(int width, int height,String fonts, int textSize) {
        if (TextUtils.isEmpty(fonts)){
            throw new IllegalArgumentException("the font file must be valid!");
        }
        if (!new File(fonts).exists()){
            throw new IllegalArgumentException("the font file must be exists!");
        }
        ctx = txtOverlayInit(width, height,fonts , textSize);
    }

    public void overlay(byte[] data,
                        String txt , int marginLeft, int marginTop) {
        //        txt = "drawtext=fontfile="+context.getFileStreamPath("SIMYOU.ttf")+": text='EasyPusher 2017':x=(w-text_w)/2:y=H-60 :fontcolor=white :box=1:boxcolor=0x00000000@0.3";
        //        txt = "movie=/sdcard/qrcode.png [logo];[in][logo] "
        //                + "overlay=" + 0 + ":" + 0
        //                + " [out]";
        //        if (ctx == 0) throw new RuntimeException("initClient should be called at first!");
        if (ctx == 0) return;
        //        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //        int width = wm.getDefaultDisplay().getWidth();
        //        int height = wm.getDefaultDisplay().getHeight();
        txtOverlay(ctx, data, txt , marginLeft , marginTop , false);
    }

    public void release() {
        if (ctx == 0) return;
        txtOverlayRelease(ctx);
        ctx = 0;
    }

    /*
            初始化函数
            width 屏幕的宽
            height 屏幕的高
            fonts  字体库等信息
            foutSize 字体大小
         */
    private static native long txtOverlayInit(int width, int height, String fonts, int foutSize );

    /*
        显示水印函数
        ctx  初始化的字体上下文信息
        data ？？
        txt  将要显示的字体
        startX 字体的横向起点
        startY 字体的竖向起点
        lean  是否倾斜，false 则水平显示; true 则竖向显示
     */
    private static native void txtOverlay(long ctx, byte[] data, String txt, long startX, long startY, boolean lean);

    private static native void txtOverlayRelease(long ctx);

}
