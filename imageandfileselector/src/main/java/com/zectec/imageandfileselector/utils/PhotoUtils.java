package com.zectec.imageandfileselector.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.zectec.imageandfileselector.R;
import com.zectec.imageandfileselector.receivehandler.ReceiverSaveImgHandler;

import java.io.File;
import java.io.FileOutputStream;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;

/**
 * Created by gt358 on 2017/9/13.
 */

public class PhotoUtils {

    private static PhotoUtils instance;

    public static PhotoUtils getInstance() {
        if (instance == null) {
            synchronized (PhotoUtils.class) {
                if (instance == null) {
                    instance = new PhotoUtils();
                }
            }
        }
        return instance;
    }

    /**
     * @param
     * @param path
     * @param imageView
     */
    public void loadLocalBitmap(Context context, String path, ImageView imageView) {
        try {
            if (imageView != null) {
                if (!Util.isEmpty(path) && path.endsWith("f")) {
//                    Glide.with(context)
//                            .load(new File(path))
//                            //                        .asGif()
////                            .fitCenter()
////                            .override(480, 800)
//                            .dontAnimate()
//                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                            .into(new GlideDrawableImageViewTarget(imageView, 0));

                    Glide.with(context).load(new File(path)).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(imageView);
                } else {
                    Glide.with(context)
//                            .load(Uri.fromFile(new File(path)))
                            .load(path)
//                            .override(480, 800)
//                            .fitCenter()
                            .into(imageView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadLocalResource(Context context, ImageView imageView) {
        Glide.with(context)
                .load(R.drawable.placeholder)
                .into(imageView);
    }

    public void loadNetBitmap(Context context, String path, ImageView imageView, TextView progress, ProgressBar progressBar) {
        try {
            //遇到一种情况 path = http://192.168.1.100:4866/null
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.error_image);
            Glide.with(context)
                    .load(path)
                    .apply(options)
                    .into(imageView);

//            Glide.with(context)
//                    .load(path)
//                    .placeholder(R.drawable.default_image)//加载中显示的图片
//                    .error(R.drawable.error_image)//加载失败时显示的图片
////                .override(200,200)//设置最终显示的图片像素为80*80,注意:这个是像素,而不是控件的宽高
//                    .into(imageView);
            progress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadNetBitmap2(Context context, String path, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.default_image)
                .error(R.drawable.error_image);

        Glide.with(context)
                .load(path)
                .apply(options)
                .into(imageView);
//        Glide.with(context)
//                .load(path)
//                .asBitmap()
//                .placeholder(R.drawable.default_image)//加载中显示的图片
//                .error(R.drawable.error_image)//加载失败时显示的图片
//                .into(imageView);
    }

    public static void loadNetBitmap(Context context, String path, ImageView imageView, int placeHolder) {
//        Glide.with(context)
//                .load(path)
//                .asBitmap()
//                .placeholder(placeHolder)//加载中显示的图片
//                .error(placeHolder)//加载失败时显示的图片
//                .into(imageView);

        RequestOptions options = new RequestOptions()
                .placeholder(placeHolder)
                .error(placeHolder);

        Glide.with(context)
                .load(path)
                .apply(options)
                .into(imageView);

    }

    public static void openCamera(Activity activity, int requestCode) {
        //调用系统相机, 并将拍摄的图片保存
        Intent intentCamera = new Intent();
        int photoNum = TerminalFactory.getSDK().getParam(Params.PHOTO_NUM, 0);
        photoNum++;
        TerminalFactory.getSDK().putParam(Params.PHOTO_NUM, photoNum);
        File file = new File(TerminalFactory.getSDK().getPhotoRecordDirectory(), "image" + photoNum + ".jpg");
        file.getParentFile().mkdirs();
        Uri imageUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //兼容Android7.0
            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imageUri = FileProvider.getUriForFile(activity, getFileProviderName(activity), file);
        } else {
            imageUri = Uri.fromFile(file);
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intentCamera.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intentCamera, requestCode);

        //getSDK().getPhotoRecordDirectory()
    }

    public static String getFileProviderName(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    /**
     * 将图片保存到本地，并在相册中显示
     ***/
    public static void savePhotoTo(final Context context, final File file) {

        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                String storrPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "4GPTT";
                File appDir = new File(storrPath);
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String photoName = file.getName();
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                File file1 = new File(appDir, photoName);
                try {
                    FileOutputStream fos = new FileOutputStream(file1);
                    if (photoName.endsWith("png")) {
                        boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    } else {
                        boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    }
                    fos.flush();
                    fos.close();

                    //把文件插入到系统图库
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), file1.getAbsolutePath(), file1.getName(), "4GPTT");
                    Uri uri = Uri.fromFile(file1);
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSaveImgHandler.class, true, "4GPTT");
                } catch (Exception e) {
                    OperateReceiveHandlerUtilSync.getInstance().notifyReceiveHandler(ReceiverSaveImgHandler.class, false, "4GPTT");
                    e.printStackTrace();
                }
            }
        });

    }


}
