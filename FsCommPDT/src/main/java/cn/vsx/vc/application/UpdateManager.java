package cn.vsx.vc.application;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import cn.vsx.vc.R;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.tools.PhoneAdapter;

public class UpdateManager
{
	private Logger logger = Logger.getLogger(getClass());
	
	private String updateUrl = null;
	private String url_apk = null;
	/* 下载中 */
	private static final int DOWNLOAD = 1;
	/* 下载结束 */
	private static final int DOWNLOAD_FINISH = 2;
	/* 保存解析的XML信息*/
	HashMap<String, String> mHashMap;
	/* 下载保存路径*/
	private String mSavePath;
	/* 记录进度条数量 */
	private int progress;
	/*是否取消更新*/
	private boolean cancelUpdate = false;

	private Context mContext;
	/* 更新进度条 */
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;

	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			// 正在下载
			case DOWNLOAD:
				//设置进度条位置
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				// 安装文件
				installApk();
				break;
			default:
				break;
			}
		};
	};

	public UpdateManager(Context context)
	{
		this.mContext = context;
	}

	/**
	 * 检测软件更新
	 */
	public void checkUpdate(String updateUrl,boolean showMsg)
	{
		logger.info("检查更新");
		this.updateUrl = updateUrl;
		if (isUpdate()){
			// 显示提示对话框
			showNoticeDialog();
		} else {
			if(showMsg){
				ToastUtil.showToast(mContext, "已经是最新版本");
			}
		}
	}

	/**
	 * 检查软件是否有更新版本
	 * 
	 * @return
	 */
	private boolean isUpdate()
	{
		try {
			// 获取当前软件版本
			int versionCode = getVersionCode(mContext);
			System.out.println("UpdateManager-----------"+updateUrl);
			URL url = new URL(updateUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.connect();
			InputStream inStream = conn.getInputStream();
			mHashMap = ParseXmlService.parseXml(inStream);
			logger.info("检查版本更新时，解析xml文件的结果："+mHashMap);
			if (null != mHashMap) {
				int serviceCode = Integer.valueOf(mHashMap.get("version"));
				if (serviceCode > versionCode) {
					logger.info("PhoneAdapter.isF32() ----> "+ PhoneAdapter.isF32());
					if (PhoneAdapter.isF32()){
						url_apk = mHashMap.get("url_f32");
					}else {
						url_apk = mHashMap.get("url");
					}
					return true;
				}
			}
		} catch (Exception e) {
			logger.warn("服务器版本文件读取出错", e);
		}
		return false;
	}

	/**
	 * 获取软件版本号
	 * 
	 * @param context
	 * @return
	 */
	private int getVersionCode(Context context)
	{
		int versionCode = 0;
		try
		{
			//获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo(MyApplication.instance.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 显示软件更新对话框
	 */
	public void showNoticeDialog(){
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				try{
					// 构造对话框
					Builder builder = new Builder(mContext);
					builder.setTitle(R.string.soft_update_title);
					builder.setMessage(R.string.soft_update_info);
					// 更新
					builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            // 显示下载对话框
                            showDownloadDialog();
                        }
                    });
					// 稍后更新
					builder.setNegativeButton(R.string.soft_update_later, new OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                            MyApplication.instance.isUpdatingAPP = false;
                        }
                    });
					builder.setCancelable(false);
					builder.show();
					MyApplication.instance.isUpdatingAPP = true;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 *显示软件下载对话框
	 */
	private void showDownloadDialog()
	{
		// 构造软件下载对话框
		Builder builder = new Builder(mContext);
		builder.setTitle(R.string.soft_updating);
		// 给下载对话框增加进度条
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.softupdate_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
		builder.setView(v);
		// 取消更新
		builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				MyApplication.instance.isUpdatingAPP = false;
				// 设置取消状态
				cancelUpdate = true;
			}
		});
		mDownloadDialog = builder.create();
		mDownloadDialog.setCancelable(false);
		mDownloadDialog.show();

		// 下载文件
		downloadApk();
	}

	/**
	 * 下载apk文件
	 */
	private void downloadApk()
	{
		// 启动新线程下载软件
		new downloadApkThread().start();
	}

	/**
	 * 下载文件线程
	 */
	private class downloadApkThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					logger.info("开始下载APK更新包");
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory() + "/";
					mSavePath = sdpath + "download";
					logger.info("下载APK的路径url_apk："+url_apk);
					URL url = new URL(url_apk);
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);
					// 判断文件目录是否存在
					if (!file.exists())
					{
						file.mkdir();
					}
					File apkFile = new File(mSavePath, "4gptt.apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do
					{
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0)
						{
							// 下载完成
							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			// 取消下载对话框显示
			mDownloadDialog.dismiss();
			MyApplication.instance.isUpdatingAPP = false;
		}
	};

	/**
	 * 安装APK文件
	 */
	private void installApk()
	{
		File apkfile = new File(mSavePath, "4gptt.apk");
		if (!apkfile.exists())
		{
			return;
		}
		// 通过Intent安装APK文件
//		Intent i = new Intent(Intent.ACTION_VIEW);
//		i.setDataAndType(getUriForFile(mContext, apkfile), "application/vnd.android.package-archive");
//		mContext.startActivity(i);

		if(Build.VERSION.SDK_INT>=24) {//判读版本是否在7.0以上
 			Uri apkUri = FileProvider.getUriForFile(mContext, cn.vsx.vc.BuildConfig.APPLICATION_ID+".myprovider", apkfile);//在AndroidManifest中的android:authorities值
			Intent install = new Intent(Intent.ACTION_VIEW);
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			install.setDataAndType(apkUri, "application/vnd.android.package-archive");
			mContext.startActivity(install);
		} else{
			Intent install = new Intent(Intent.ACTION_VIEW);
			install.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(install);
		}


		logger.info("下载完成，通过Intent开始安装APK文件");
	}
}
