package cn.vsx.vc.playercontrol;//package com.zectec.a4gptt_tv.playercontrol;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//
//import org.videolan.libvlc.IVideoPlayer;
//import org.videolan.libvlc.LibVLC;
//import org.videolan.libvlc.LibVlcException;
//import org.videolan.vlc.Util;
//import org.videolan.vlc.VLCApplication;
//
//
///**
// *
// * @author Administrator VLC 视频播放
// */
//@SuppressLint("HandlerLeak")
//public class VLCControl implements IVideoPlayer, SurfaceHolder.Callback {
//	private static final String TAG = "PlayerControl";
//	/**
//	 * 播放器大小改变
//	 */
//	private static final int CHANGE_SIZE = 0x0001;
//	/**
//	 * 播放地址改变
//	 */
//	private static final int CHANGE_ADDR = 0x0002;
//
////	private static Context context;
//
//	private static String playAddr = "";
//
//	private int mWidth;
//	private int mHeight;
//	private SurfaceView surfaceView;
//	private static LibVLC mLibVLC;
//
////	private LayoutInflater inflater;
////	private View layout;
////	private TextView msg_text;
////	private Toast toast;
//	@SuppressLint("InflateParams")
//	public VLCControl(Context context, SurfaceView surfaceView) {
//		this.surfaceView = surfaceView;
////		this.context = context;
////		inflater = LayoutInflater.from(context);
////		layout = inflater.inflate(R.layout.warning, null);
////		msg_text = (TextView) layout.findViewById(R.id.warning_text);
//		try {
//			if(LibVLC.getExistingInstance() != null){
//				LibVLC.restart(VLCApplication.getAppContext());
//			}
//			mLibVLC = Util.getLibVlcInstance();
//			mLibVLC.setHardwareAcceleration(0);
//			Log.d(TAG, "LibVLS 初始化成功！");
//		} catch (LibVlcException e) {
//			Log.d(TAG, "LibVLC 初始化失败。出现了不可预知的错误");
//		}
//	}
//
//	@Override
//	public void setSurfaceSize(int width, int height, int visible_width,
//			int visible_height, int sar_num, int sar_den) {
//		if (width * height == 0)
//			return;
//		mWidth = width;
//		mHeight = height;
//		playerHandler.sendEmptyMessage(CHANGE_SIZE);
//	}
//
//	public static LibVLC getmLibVLC() {
//		return mLibVLC;
//	}
//
//	/**
//	 * 更新播放地址,并且播放当前地址。这个方法和PAD_STB是否登陆无关。 不管是否登陆，都会刷新播放器
//	 */
//	public void updatePlayAddr() {
//		if(playAddr != null && playAddr.length()>0){
//			if(mLibVLC.isPlaying()){
//				mLibVLC.stop();
//			}
//			Log.e(TAG, "RTSP 开始播放了" + playAddr);
//			if (playAddr != null) {
//				mLibVLC.playMRL(playAddr);
//			}
//			if (!mLibVLC.isPlaying()) {
//				mLibVLC.play();
//			}
//		}
//	}
//	/**
//	 * 停止VLC 的播放。当Activity 的onPause 方法调用的时候
//	 */
//	public static void stopVLC() {
//		if (mLibVLC != null) {
//			Log.e(TAG, "stopVLC");
//			if (mLibVLC.isPlaying()) {
//				Log.e(TAG, "pause1");
//				mLibVLC.pause();
//				Log.e(TAG, "pause2");
//			}
//			Log.e(TAG, "stop1");
//			mLibVLC.stop();
//			Log.e(TAG, "stop2");
//			playAddr = "";
//		}
//	}
//
//	/**
//	 * 开始VLC 的播放。当Activity 的onPause 方法调用的时候
//	 */
//	public static void playVlc() {
//		if (mLibVLC != null) {
//			if (!mLibVLC.isPlaying())
//				mLibVLC.play();
//			mLibVLC.play();
//		}
//	}
//
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//
//	}
//
//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
//		synchronized (this) {
//			mLibVLC.attachSurface(surfaceView.getHolder().getSurface(), this);
//		}
//	}
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		 synchronized (this) {
//			mLibVLC.detachSurface();
//		}
//	}
//	public void switchVlc(String streamAddress){
//		Message msg = new Message();
//		msg.what = CHANGE_ADDR;
//		playAddr = streamAddress;
//		playerHandler.sendMessage(msg);
//	}
//	/**
//	 * VLC 播放器统一控制的Handler
//	 */
//	public Handler playerHandler = new Handler() {
//		public void dispatchMessage(Message msg) {
//			switch (msg.what) {
//			case CHANGE_SIZE:
//				surfaceView.getHolder().setFixedSize(mWidth, mHeight);
//				break;
//			case CHANGE_ADDR:
//				Log.i("MyInfo", "开始执行！");
////				DVRControl.stopDVR();
//				/*PlayerActivity.surfaceViewVLC.setVisibility(View.VISIBLE);
//				PlayerActivity.surfaceViewDVR.setVisibility(View.GONE);*/
////				CameraVedioFragment.surfaceViewVLC.setVisibility(View.VISIBLE);
////				CameraVedioFragment.surfaceViewDVR.setVisibility(View.GONE);
//				updatePlayAddr();
//				break;
//			default:break;
//			}
//		};
//	};
//	/**
//	 * 暂停时停止所有的数据
//	 */
//	public void stop(){
//
//	}
//
//	public void setSurfaceView(SurfaceView surfaceView){
//		boolean temp = false;
//		if(mLibVLC.isPlaying()){
//			stopVLC();
//			temp = true;
//		}
//		this.surfaceView = surfaceView;
//		surfaceChanged(null, 0, 0, 0);
//		if(temp)updatePlayAddr();
//	}
//	/**
//	 * 重启当前VLC 播放器
//	 */
//	public void repeat(){
//		updatePlayAddr();
//	}
//	/**
//	 * 移除所有资源
//	 */
//	public void destroy(){
//		mLibVLC.destroy();
//	}
//}
