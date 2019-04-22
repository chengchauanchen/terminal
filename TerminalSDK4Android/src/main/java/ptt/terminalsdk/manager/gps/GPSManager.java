package ptt.terminalsdk.manager.gps;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.protolbuf.PTTProtolbuf.NotifyForceUploadGpsMessage;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.channel.ServerMessageReceivedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetGPSLocationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLoginResponseHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateConfigHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;

public class GPSManager {
	private Context context;
	private Logger logger = Logger.getLogger(getClass());
	private boolean isChatSendLocation;
	public GPSManager(Context context) {
		this.context = context;
		init();
	}

	private long lastLocationTime;
	private LocationManager locationManager;
	private String locationProvider;
	private boolean isCommonUpload = true;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case 0:
					setLocationUpdate(false);
					break;
				default:
					break;
			}
		}


	};

	private ReceiveUpdateConfigHandler receiveUpdateConfigHandler = new ReceiveUpdateConfigHandler() {
		@Override
		public void handler() {
			logger.info("GPSManager收到成员更新配置的通知，GPS开关状态为：" + MyTerminalFactory.getSDK().getParam(Params.GPS_STATE, false));
			if (MyTerminalFactory.getSDK().getParam(Params.GPS_STATE, false)) {//GPS开关打开了，普通上传
				isCommonUpload = true;
				TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(0);
					}
				});
//				new Thread().start();
			} else {//GPS开关关掉
				if (!Util.isEmpty(locationProvider)) {
					if (locationManager != null && locationListener != null) {
						locationManager.removeUpdates(locationListener);
//						locationListener = null;
					}
				}
			}
		}
	};
	/**通知强制上传GPS信息的消息*/
	private ServerMessageReceivedHandler<NotifyForceUploadGpsMessage> notifyForceUploadGpsMessageReceivedHandler = new ServerMessageReceivedHandler<NotifyForceUploadGpsMessage>() {
		@Override
		public void handle(NotifyForceUploadGpsMessage message) {
			logger.info("收到GPS强制上传通知命令" + message);
			//保存强制上传GPS信息的一些参数
			MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_INTERVAL, message.getUploadRate());
			MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, message.getHoldTime());
			MyTerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_START_TIME, System.currentTimeMillis());
			//提高GPS信息上传间隔
			forceUpload();
			//定时回复GPS信息上传间隔
			MyTerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
				@Override
				public void run() {//指定时长过后，开始普通上传
					commonUpload();
				}
			}, MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, 0));
		}
	};
	private ServerMessageReceivedHandler<PTTProtolbuf.ResponseMemberConfigMessage> responseMemberConfigMessageHandler = new ServerMessageReceivedHandler<PTTProtolbuf.ResponseMemberConfigMessage>() {
		@Override
		public void handle(PTTProtolbuf.ResponseMemberConfigMessage message) {
			TerminalFactory.getSDK().putParam(Params.GPS_STATE, message.getGpsEnable());//gps开关状态,打开还是关闭，默认为关闭
			TerminalFactory.getSDK().putParam(Params.GPS_UPLOAD_INTERVAL, message.getDefaultGpsFrequency());
			TerminalFactory.getSDK().putParam(Params.GPS_FORCE_UPLOAD_INTERVAL, message.getActiveGpsFrequency());
			setLocationUpdate(false);
		}
	};
	/**收到登录的消息*/
	private ReceiveLoginResponseHandler receiveLoginResponseHandler = new ReceiveLoginResponseHandler() {
		@Override
		public void handler(int resultCode, String resultDesc) {
			if (resultCode == 0) {
				TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(0);
					}
				});
//				new Thread().start();
			}
		}
	};

	private LocationListener locationListener;
	public void createLocationListener(){
		locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				logger.info("onStatusChanged    provider = "+provider+"    status = "+status);
			}

			@Override
			public void onProviderEnabled(String provider) {
				logger.info("onProviderEnabled    provider = "+provider);
			}

			@Override
			public void onProviderDisabled(String provider) {
				logger.info("onProviderDisabled    provider = "+provider);
			}

			@Override
			public void onLocationChanged(Location location) {
				logger.info("GPSManager中onLocationChanged()=" + location +"   isChatSendLocation = "+isChatSendLocation);
				if (isChatSendLocation){
//					List<Double> doubles = CoordTransformUtils.wgs2bd(location.getLatitude(), location.getLongitude());
//					logger.info("GPSManager换算后的坐标："+doubles);
					//国产手机获取到的定位就是WGS坐标系，地图地图也是，所以不用转
					TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, location.getLongitude(),location.getLatitude());
					isChatSendLocation = false;
				}else {
					upLoadLocation(location);
				}
			}

		};
	}

	/**初始化LocationManager*/
	private void init() {
		locationManager = (LocationManager) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.LOCATION_SERVICE);
		//获取所有可用的位置提供器
		List<String> providersFalse = locationManager.getProviders(false);
		logger.info("所有的locationProvider："+providersFalse);
		List<String> providers = locationManager.getProviders(true);
		logger.info("可用的locationProvider："+providers);
		if (providers.contains(LocationManager.GPS_PROVIDER)) {
			//如果是GPS
			locationProvider = LocationManager.GPS_PROVIDER;
		} else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			//如果是Network
			locationProvider = LocationManager.NETWORK_PROVIDER;
		}

		if (Util.isEmpty(locationProvider)){
			locationProvider = LocationManager.GPS_PROVIDER;
		}
		logger.info("获取到的位置提供者是：locationProvider = "+locationProvider);
	}

	/** 普通上传 */
	private void commonUpload() {
		logger.info("调用了commondUpload");
		isCommonUpload = true;
		setLocationUpdate(false);
	}

	/** 强制上传 */
	private void forceUpload() {
		logger.info("调用了forceUpload");
		isCommonUpload = false;
		setLocationUpdate(false);
	}

	/** 启动 */
	public void start() {
		//启动定位服务
		MyTerminalFactory.getSDK().registReceiveHandler(receiveLoginResponseHandler);
		MyTerminalFactory.getSDK().registReceiveHandler(receiveUpdateConfigHandler);
		MyTerminalFactory.getSDK().getClientChannel().registMessageReceivedHandler(notifyForceUploadGpsMessageReceivedHandler);
		MyTerminalFactory.getSDK().getClientChannel().registMessageReceivedHandler(responseMemberConfigMessageHandler);
		//检查强制上传GPS信息的状态是否依然存在，若存在，则进行强制上传
		if (System.currentTimeMillis() - MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_START_TIME, 0L) < MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, 0)) {
			forceUpload();//持续时间之内强制上传
			logger.error("强制上传!");
			MyTerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
				@Override
				public void run() {
					commonUpload();
				}
			}, MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_HOLD_TIME, 0) - (System.currentTimeMillis() - MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_START_TIME, 0L)));
		}else {
			mHandler.post(new Runnable(){
				@Override
				public void run(){
					commonUpload();
					logger.error("普通上传!");
				}
			});

		}
		if (!Util.isEmpty(locationProvider)) {
		}else {
			logger.error("locationProvider是空的!");
		}
	}

	/** 停止 */
	public void stop() {
		logger.error("GPSManager销毁了");
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLoginResponseHandler);
		MyTerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateConfigHandler);
		MyTerminalFactory.getSDK().getClientChannel().unregistMessageReceivedHandler(responseMemberConfigMessageHandler);
		MyTerminalFactory.getSDK().getClientChannel().unregistMessageReceivedHandler(notifyForceUploadGpsMessageReceivedHandler);
		if (!Util.isEmpty(locationProvider)) {
			if (locationManager != null && locationListener != null) {
				locationManager.removeUpdates(locationListener);
			}
		}
	}

	private void upLoadLocation(Location location) {

		if (TerminalFactory.getSDK().getParam(Params.GPS_STATE, false)
				&& !TerminalFactory.getSDK().isExit()
				&& System.currentTimeMillis() - lastLocationTime > 5000) {//配置要求GPS打开，时间间隔大于5秒
			lastLocationTime = System.currentTimeMillis();
			if (isCommonUpload) {
				commonUpload();
			} else {
				forceUpload();
			}
			//上传GPS位置信息
			String ip = MyTerminalFactory.getSDK().getParam(Params.GPS_IP);
			int port = MyTerminalFactory.getSDK().getParam(Params.GPS_PORT,0);
//			final String url = "http://192.168.1.174:6666/save";
			final String url = "http://"+ip+":"+port+"/save";
			Map<String,Object> params = new HashMap<>();
			params.put("terminalno",MyTerminalFactory.getSDK().getParam(Params.MEMBER_ID,0));
			params.put("memberuniqueno",MyTerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO,0l));
			params.put("longitude",location.getLongitude());
			params.put("latitude",location.getLatitude());
			params.put("speed",location.getSpeed());
			params.put("bearing",location.getBearing());
			params.put("altitude",location.getAltitude());

			Gson gson = new Gson();
			final String json = gson.toJson(params);
			MyTerminalFactory.getSDK().getThreadPool().execute(new Runnable(){
				@Override
				public void run(){
					MyTerminalFactory.getSDK().getHttpClient().postJson(url,"gps="+json);
				}
			});
		}
	}

	private long getUploadTime() {
		if (isCommonUpload) {
//			return Math.max(1 * 60 * 1000, MyTerminalFactory.getSDK().getParam(Params.GPS_UPLOAD_INTERVAL, 5 * 60 * 1000));//普通上传最小间隔是一分钟
			return MyTerminalFactory.getSDK().getParam(Params.GPS_UPLOAD_INTERVAL, 1 * 60 * 1000);
		} else {
			return Math.max(5 * 1000, MyTerminalFactory.getSDK().getParam(Params.GPS_FORCE_UPLOAD_INTERVAL, 5 * 1000));//强制上传最小间隔是5秒钟
		}
	}

	/**
	 * 设置获取位置的方式
	 * @param isChat   区分会话页面的发送位置
	 */
	public void setLocationUpdate(final boolean isChat) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (ActivityCompat.checkSelfPermission(context, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(context, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(context, "请打开位置权限，以方便获取您的位置信息", Toast.LENGTH_SHORT).show();
					logger.error("请打开位置权限，以方便获取您的位置信息");
					TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class,0,0);
					return;
				}
				if (Util.isEmpty(locationProvider)){
					init();
				}
				if (!Util.isEmpty(locationProvider) && locationManager.isProviderEnabled(locationProvider)) {
					long uploadTime;
					if (isChat) {
						uploadTime = 0;
					} else {
						uploadTime = getUploadTime();
					}
					isChatSendLocation = isChat;
					logger.error("locationProvider不为空------" + locationProvider + "     uploadTime-----" + uploadTime+"   isChat-----"+isChat);
					logger.error(locationListener+"");
					if(locationListener == null){
						createLocationListener();
					}
					locationManager.requestLocationUpdates(locationProvider, uploadTime, 0, locationListener);
					TerminalFactory.getSDK().getTimer().schedule(new TimerTask() {
						@Override
						public void run() {
							TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class,0,0);
							if (isChat && locationManager != null){

								locationManager.removeUpdates(locationListener);
							}
						}
					}, 5000);
				} else {
					Toast.makeText(context, "GPS不可用", Toast.LENGTH_SHORT).show();
					logger.error("GPS不可用");
					TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class,0,0);
				}
			}
		});

	}
}
