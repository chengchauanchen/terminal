package cn.vsx.vc.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.vc.R;
import ptt.terminalsdk.context.MyTerminalFactory;

public class BitmapUtil {
	
	/**
	 *根据图片路径压缩图片
	 */
	public static Bitmap decodeSampledBitmapFromFd(String pathName,
			int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		Bitmap src = BitmapFactory.decodeFile(pathName, options);
		return createScaleBitmap(src, reqWidth, reqHeight);
	}

	/**
	 * Bitmap对象保存味图片文件
	 */

	public static void saveBitmapFile(Bitmap bitmap,String filePath,String filename){
		String s=filePath+filename;
		File file=new File(s);//将要保存图片的路径
		if(!file.getParentFile().exists()){
			file.getParentFile().mkdirs();
		}
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 从Resources中加载图片并压缩
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options); // 读取图片长款
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight); // 计算inSampleSize
		options.inJustDecodeBounds = false;
		Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
		return createScaleBitmap(src, reqWidth, reqHeight); // 进一步得到目标大小的缩略图
	}
	
	/**
	 * bitmap图片进行质量压缩
	 */
	public static Bitmap compressImage(Bitmap image) {  
		  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        image.compress(Bitmap.CompressFormat.PNG, 10, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中  
        int options = 100;  
        while ( baos.toByteArray().length / 1024>30 && options > 0) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos  
            image.compress(Bitmap.CompressFormat.PNG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
            options -= 10;//每次都减少10  
        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片  
        return bitmap;  
    } 

	// 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
	private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
			int dstHeight) {
		Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
		if (src != dst) { // 如果没有缩放，那么不回收
			src.recycle(); // 释放Bitmap的native像素数组
		}
		return dst;
	}

	//获取压缩比例
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(), 
                drawable.getIntrinsicHeight(), 
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        drawable.draw(canvas);

        return bitmap;
    }
	
	@SuppressWarnings("deprecation")
	public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }

	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static Bitmap createVideoThumbnail(String videoPath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(videoPath);
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}
		return bitmap;
	}

	/**
	 * 根据设备类型获取设备对应的图标
	 *
	 * @param type
	 * @return
	 */
	public static int getImageResourceByType(int type) {
		if (type == TerminalMemberType.TERMINAL_PC.getCode()) {
			return R.drawable.icon_pc;
		} else if (type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
			return R.drawable.icon_recorder;
		} else if (type == TerminalMemberType.TERMINAL_UAV.getCode()) {
			return R.drawable.icon_uav;
		} else if (type == TerminalMemberType.TERMINAL_HDMI.getCode()) {
			return R.drawable.icon_hdmi;
		} else if (type == TerminalMemberType.TERMINAL_LTE.getCode() ||
				   type == TerminalMemberType.TERMINAL_LTE_HYTERA.getCode()) {
			return R.drawable.icon_lte;
		} else {
			return R.drawable.icon_phone;
		}
	}

	/**
	 * 根据设备类型获取不在线设备对应的图标
	 *
	 * @param type
	 * @return
	 */
	public static int getOffineImageResourceByType(int type) {
		if (type == TerminalMemberType.TERMINAL_PC.getCode()) {
			return R.drawable.icon_pc_offline;
		} else if (type == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode()) {
			return R.drawable.icon_recorder_offline;
		} else if (type == TerminalMemberType.TERMINAL_UAV.getCode()) {
			return R.drawable.icon_uav_offline;
		} else if (type == TerminalMemberType.TERMINAL_HDMI.getCode()) {
			return R.drawable.icon_hdmi_offline;
		} else if (type == TerminalMemberType.TERMINAL_LTE.getCode() ||
				   type == TerminalMemberType.TERMINAL_LTE_HYTERA.getCode()) {
			return R.drawable.icon_lte_offline;
		} else {
			return R.drawable.icon_phone_offline;
		}
	}

	/**
	 * 根据Volume 显示是否静音的图标
	 * @return
	 */
	public static int getVolumeImageResourceByValue(boolean isBlue){
		int value = MyTerminalFactory.getSDK().getAudioProxy().getVolume();
		return (value <=0)?R.drawable.volume_off_call:(isBlue)?R.drawable.volume_silence:R.drawable.horn;
	}

	/**
	 * 获取用户的头像
	 * @return
	 */
	public static int getUserPhoto(){
		if(ApkUtil.isAnjian()){
			return R.drawable.user_photo_anjian;
		}
		return R.drawable.user_photo;
	}

	/**
	 * 获取用户的头像圆
	 * @return
	 */
	public static int getUserPhotoRound(){
		if(ApkUtil.isAnjian()){
			return R.drawable.user_photo_anjian_round;
		}
		return R.drawable.member_icon_new;
	}

	public static Bitmap getBitmapFormUrl(String url) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			if (Build.VERSION.SDK_INT >= 14) {
				retriever.setDataSource(url, new HashMap<String, String>());
			} else {
				retriever.setDataSource(url);
			}
        /*getFrameAtTime()--->在setDataSource()之后调用此方法。 如果可能，该方法在任何时间位置找到代表性的帧，
         并将其作为位图返回。这对于生成输入数据源的缩略图很有用。**/
			bitmap = retriever.getFrameAtTime();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			try {
				retriever.release();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return bitmap;
	}
}
