package ptt.terminalsdk.context;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.log4j.Level;
import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import cn.vsx.hamster.common.MessageType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.TerminalSDKBaseImpl;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.vsx.hamster.terminalsdk.manager.channel.AbsClientChannel;
import cn.vsx.hamster.terminalsdk.manager.http.IHttpClient;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUploadProgressHandler;
import cn.vsx.hamster.terminalsdk.tools.OperateReceiveHandlerUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ptt.terminalsdk.IMessageService;
import ptt.terminalsdk.IMessageService.Stub;
import ptt.terminalsdk.manager.Prompt.PromptManager;
import ptt.terminalsdk.manager.audio.AudioProxy;
import ptt.terminalsdk.manager.channel.ClientChannel;
import ptt.terminalsdk.manager.gps.BDGPSManager;
import ptt.terminalsdk.manager.gps.GPSManager;
import ptt.terminalsdk.manager.http.MyHttpClient;
import ptt.terminalsdk.manager.http.ProgressHelper;
import ptt.terminalsdk.manager.http.ProgressUIListener;
import ptt.terminalsdk.manager.message.SQLiteDBManager;
import ptt.terminalsdk.manager.video.VideoProxy;
import ptt.terminalsdk.manager.voip.VoipManager;
import ptt.terminalsdk.service.BluetoothLeService;
import ptt.terminalsdk.service.MessageService;
import ptt.terminalsdk.tools.DeleteData;
import ptt.terminalsdk.tools.HttpUtil;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

public class TerminalSDK4Android extends TerminalSDKBaseImpl {
	private SharedPreferences account;
	public Application application;
	private boolean calling = false;
	private byte[] uuidByte;
	private String accessServerIp;
	private int accessServerPort;
	private boolean bindService;
    private VoipManager voipManager;

	public TerminalSDK4Android (Application mApplication){
		application = mApplication;
		account = application.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_MULTI_PROCESS);
	}

	@Override
	protected void onStart() {
		//启动内部事件处理类
		OperateReceiveHandlerUtil.getInstance().start();
		putParam(Params.NETWORK_JITTER_CHECK_INTERVAL, 60 * 1000);//网络抖动检测间隔，60秒
		putParam(Params.GPS_UPLOAD_INTERVAL, 5 * 60 * 1000);//GPS上传时间间隔，5分钟
		getGpsManager().start();
		getBDGPSManager().start();
		getVideoProxy().start();
		PromptManager.getInstance().start(application);

		//个呼通讯录，请求的是自己的列表，还是所有成员列表
		putParam(Params.REQUEST_ALL, false);
		try {
			File youyuan = application.getFileStreamPath("SIMYOU.ttf");
				if (!youyuan.exists()){
				AssetManager am = application.getAssets();
					InputStream is = am.open("zk/SIMYOU.ttf");
					FileOutputStream os = application.openFileOutput("SIMYOU.ttf", MODE_PRIVATE);
					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = is.read(buffer)) != -1) {
						os.write(buffer, 0, len);
					}
					os.close();
					is.close();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onStop() {
		getGpsManager().stop();
		getBDGPSManager().stop();
		getVideoProxy().stop();
		PromptManager.getInstance().stop();
		disConnectToServer();
	}


	@SuppressLint("NewApi")/**日志生成文件保存*/
	public void configLogger() {

		File dir = new File(getLogDirectory());
		if (!dir.exists()) {
			try {
				//按照指定的路径创建文件夹
				dir.mkdirs();
			} catch (Exception e) {
			}
		}
		File file = new File(getLogDirectory() + "log.txt");
		if (!file.exists()) {
			try {
				//在指定的文件夹中创建文件
				file.createNewFile();
			} catch (Exception e) {
			}
		}

//		if (!dir.exists()){
//			try {
//				File mkr = new File(getLogDirectory());
//				mkr.mkdirs();
//				file.createNewFile();
//			} catch (IOException e) {
//				logger.warn("日志生成文件" + e.toString());
//			}
//		}
//        boolean isSuccess = CreateFileOrDirectoryUtils.createFileOrDirectory(file);
//        logger.error("创建文件夹是否成功" + isSuccess);

		LogConfigurator logConfigurator = new LogConfigurator();
		logConfigurator.setFileName(getLogDirectory() + "log.txt");
		logConfigurator.setRootLevel(Level.ALL);
		logConfigurator.setFilePattern("%d %-5p [%t][%c{2}]-[%l] %m%n");
		logConfigurator.setUseLogCatAppender(true);
		logConfigurator.setMaxFileSize(1024 * 1024 * 20);
		logConfigurator.setMaxBackupSize(0);
		logConfigurator.setImmediateFlush(true);
		logConfigurator.configure();
	}

	@Override
	public boolean contains(String param) {
		return account.contains(param);
	}

	@Override
	public String getParam(String param, String defaultValue){
		return account.getString(param, defaultValue);
	}
	@Override
	public boolean getParam(String param, boolean defaultValue){
		return account.getBoolean(param, defaultValue);
	}
	@Override
	public int getParam(String param, int defaultValue){
		return account.getInt(param, defaultValue);
	}
	@Override
	public long getParam(String param, long defaultValue){
		return account.getLong(param, defaultValue);
	}
	@Override
	public float getParam(String param, float defaultValue){
		return account.getFloat(param, defaultValue);
	}
	@Override
	public double getParam(String param, double defaultValue){
		return Double.longBitsToDouble(account.getLong(param, Double.doubleToLongBits(defaultValue)));
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getSerializable(String param, T defaultValue) {
		// 拿出持久化数据  
        T obj = null;
        FileInputStream in = null;
        ObjectInputStream oin = null;
        try {
            File file = new File(getSerializableDataDirectory(), param);
            in = new FileInputStream(file);
            oin = new ObjectInputStream(in);
            obj = (T) oin.readObject();
            in.close();
            oin.close();
            return obj;
        } catch (Exception e) {}
        finally{
        	if(in != null){
        		try {
					in.close();
				} catch (IOException e) {}
        	}
        	if(oin != null){
        		try {
        			oin.close();
				} catch (IOException e) {}
        	}
        }
    	return defaultValue;
	}
	@Override
	public void putParam(String param, String value){
		account.edit().putString(param, value).commit();
	}
	@Override
	public void putParam(String param, boolean value){
		account.edit().putBoolean(param, value).commit();
	}
	@Override
	public void putParam(String param, int value){
		account.edit().putInt(param, value).commit();
	}
	@Override
	public void putParam(String param, long value){
		account.edit().putLong(param, value).commit();
	}
	@Override
	public void putParam(String param, float value){
		account.edit().putFloat(param, value).commit();
	}
	@Override
	public void putParam(String param, double value){
		account.edit().putLong(param, Double.doubleToLongBits(value)).commit();
	}
	@Override
	public <T extends Serializable> void putSerializable(String param, T value) {
		ByteArrayOutputStream bout = null;
		ObjectOutputStream oout = null;
		FileOutputStream out = null;
		try {
            bout = new ByteArrayOutputStream();
            oout = new ObjectOutputStream(bout);
            oout.writeObject(value);
            oout.flush();
            oout.close();
            bout.close();
            byte[] b = bout.toByteArray();
            File file = new File(getSerializableDataDirectory(), param);
            Util.createFileOrDirectoryIfNotExists(file);
            out = new FileOutputStream(file);
            out.write(b);
            out.flush();
            out.close();
        } catch (Exception e) {}
		finally {
        	if(bout != null){
        		try {
					bout.close();
				} catch (IOException e) {}
        	}
        	if(oout != null){
        		try {
        			oout.close();
				} catch (IOException e) {}
        	}
        	if(out != null){
        		try {
        			out.close();
				} catch (IOException e) {}
        	}
        }
	}

	public void exit(){
		//启动内部事件处理类
		OperateReceiveHandlerUtil.getInstance().stop();
		getAuthManagerTwo().logout();
		getVoipCallManager().destroy();//VOIP服务注销
		putParam(Params.CURRENT_SPEAKER,"");
		logger.info("TerminalSDK4Android关闭了OnlineService");
		Intent onlineService = new Intent(application, OnlineService.class);
		application.stopService(onlineService);
		application.unbindService(onlineServiceConn);
		Intent bleService = new Intent(application, BluetoothLeService.class);
		application.stopService(bleService);
		application.unbindService(bleServiceConn);
	}

	private void startService(){
		logger.info("TerminalSDK4Android启动了OnlineService");
		Intent onlineService = new Intent(application, OnlineService.class);
		Intent bleService = new Intent(application, BluetoothLeService.class);
		if (application != null) {
			application.startService(onlineService);
			application.bindService(onlineService,onlineServiceConn,BIND_AUTO_CREATE);
			application.startService(bleService);
			application.bindService(bleService,bleServiceConn,BIND_AUTO_CREATE);
		}
	}

	private OnlineService.OnlineServiceBinder onlineService;
	private ServiceConnection onlineServiceConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			onlineService = (OnlineService.OnlineServiceBinder) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name){
			startService();
		}
	};

	private BluetoothLeService.LocalBinder bleService;
	private ServiceConnection bleServiceConn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service){
			bleService = ((BluetoothLeService.LocalBinder) service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name){
			startService();
		}
	};

	/**得到文字记录的存储位置*/
	public String getWordRecordDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "wordRecord"
				+ File.separator;
	}
	/**得到图片记录的存储位置*/
	public String getPhotoRecordDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "photoRecord"
				+ File.separator;
	}
	/**得到录音记录的存储位置*/
	public String getAudioRecordDirectory(){
		return Environment.getExternalStorageDirectory()
                + File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "audioRecord"
                + File.separator;
	}
	/**得到视频记录的存储位置*/
	public String getVideoRecordDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "videoRecord"
				+ File.separator;
	}
	/**得到文件的存储位置*/
	public String getFileRecordDirectory() {
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "file"
				+ File.separator;
	}
	/**得到日志的存储位置*/
	public String getLogDirectory() {
		return Environment.getExternalStorageDirectory()
					+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "logs"
					+ File.separator;
	}

	public VoipManager getVoipCallManager() {
		if(voipManager==null){
			voipManager = new VoipManager();
        }
		return voipManager;
	}

	/**得到序列化数据的存储位置*/
	public String getSerializableDataDirectory() {
		return application.getFilesDir()
					+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "data"
					+ File.separator;
	}
	private IHttpClient httpClient;
	@Override
	public IHttpClient getHttpClient() {
		if(httpClient == null){
			httpClient = new MyHttpClient();
		}
		return httpClient;
	}
	private IAudioProxy audioProxy;
	@Override
	public IAudioProxy getAudioProxy() {
		if(audioProxy == null){
			audioProxy = new AudioProxy(application);
		}
		return audioProxy;
	}


	@Override
	public void stopMultimediaMessage() {
		getTerminalMessageManager().stopMultimediaMessage();
	}

	private ClientChannel clientChannel;
	@Override
	public AbsClientChannel getClientChannel() {
		if (clientChannel == null){
			clientChannel = new ClientChannel(messageService);
		}
		return clientChannel;
	}

	private VideoProxy videoProxy;
	public VideoProxy getVideoProxy() {
		if (videoProxy == null){
			videoProxy = new VideoProxy();
		}
		return videoProxy;
	}
	private GPSManager gpsManager;
	public GPSManager getGpsManager(){
		if(gpsManager == null){
			gpsManager = new GPSManager(application);
		}
		return gpsManager;
	}
	private BDGPSManager bdgpsManager;
	public BDGPSManager getBDGPSManager(){
		if(bdgpsManager == null){
			bdgpsManager = new BDGPSManager(application);
		}
		return bdgpsManager;
	}
	public Application getApplication(){
		return application;
	}
	private WakeLock wakeLock;
	public WakeLock getWakeLock(){
		if(wakeLock == null){
			PowerManager pm = (PowerManager) application.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OnlineService");
		}
		return wakeLock;
	}

	public boolean hasNetwork() {
	    ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
	public boolean isCalling() {
		return calling;
	}
	public void setCalling(boolean calling) {
		this.calling = calling;
	}

	@Override
	public void clearSharedPreferencesData() {
		account.edit().clear().commit();
	}
	@Override
	public void clearData() {
		DeleteData.deleteAllData();
	}
	@Override
	public void clearMessage() {
		logger.info("删除所有存储数据");
		TerminalFactory.getSDK().putParam(Params.MESSAGE_VERSION, 0l);
		DeleteData.deleteSerializableData();
		DeleteData.deleteSQLiteDatabase();
		DeleteData.deleteAudioRecord();
		DeleteData.deleteVideoRecord();
		DeleteData.deletePhotoRecord();
		DeleteData.deleteWordRecord();
		DeleteData.deleteFileRecord();
	}
	@Override
	public String saveFileByBitmap(String path, String fileName, Response response) {
		Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
		return HttpUtil.saveFileByBitmap(getPhotoRecordDirectory(), fileName, bitmap);
	}

	/**界面不需要下载进度展示*/
	public void download(TerminalMessage terminalMessage, boolean isNeedUi){
		TerminalFactory.getSDK().getTerminalMessageManager().downloadDataByOkHttp(terminalMessage, isNeedUi);
	}
	@Override
	public ResponseBody downloadProgress(Response response, final TerminalMessage terminalMessage) throws IOException {

		logger.info("=============onResponse===============");
		logger.info("request headers:" + response.request().headers());
		logger.info("response headers:" + response.headers());
		return ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

			//if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
			@Override
			public void onUIProgressStart(long totalBytes) {
				super.onUIProgressStart(totalBytes);
				logger.info("onUIProgressStart:" + totalBytes);
			}

			@Override
			public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
				logger.info("=============start===============");
				logger.info("numBytes:" + numBytes);
				logger.info("totalBytes:" + totalBytes);
				logger.info("percent:" + percent);
				logger.info("speed:" + speed);
				logger.info("============= end ===============");
				TerminalFactory.getSDK().notifyReceiveHandler(ReceiveDownloadProgressHandler.class, percent, terminalMessage);
			}

			//if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
			@Override
			public void onUIProgressFinish() {
				super.onUIProgressFinish();
				logger.info("onUIProgressFinish:");
			}
		});
	}

	/**界面需要上传进度展示*/
	public void upload(String url, File file, TerminalMessage terminalMessage, boolean isNeedUi){
		if(terminalMessage.messageType == MessageType.VIDEO_CLIPS.getCode()){
			Bitmap bitmap = createVideoThumbnail(terminalMessage.messagePath);
			String picture = HttpUtil.saveFileByBitmap(getPhotoRecordDirectory(), System.currentTimeMillis()+".jpg", bitmap);
			TerminalFactory.getSDK().getTerminalMessageManager().uploadVideo(url, file, terminalMessage, isNeedUi,picture);
		}else {
			TerminalFactory.getSDK().getTerminalMessageManager().uploadDataByOkHttp(url, file, terminalMessage, isNeedUi);
		}
	}
	@Override
	public RequestBody uploadProgress(RequestBody requestBody0, final TerminalMessage terminalMessage) {
		return ProgressHelper.withProgress(requestBody0, new ProgressUIListener() {

			//if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
			@Override
			public void onUIProgressStart(long totalBytes) {
				super.onUIProgressStart(totalBytes);
				logger.info("onUIProgressStart:" + totalBytes);
			}

			@Override
			public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
				logger.info("=============start===============");
				logger.info("numBytes:" + numBytes);
				logger.info("totalBytes:" + totalBytes);
				logger.info("percent:" + percent);
				logger.info("speed:" + speed);
				logger.info("============= end ===============");
				TerminalFactory.getSDK().notifyReceiveHandler(ReceiveUploadProgressHandler.class, percent, terminalMessage);
			}

			//if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
			@Override
			public void onUIProgressFinish() {
				super.onUIProgressFinish();
				logger.info("onUIProgressFinish:");
			}
		});
	}

	Handler handler;
	private void hideUI(final ProgressBar progressBar, final TextView textView) {
		if (handler == null){
			handler = new Handler();
		}
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null)
					progressBar.setVisibility(View.GONE);
                if (textView != null)
					textView.setVisibility(View.GONE);
            }
        }, 2000);
	}


	private SQLiteDBManager sqliteDBManager;
	public SQLiteDBManager getSQLiteDBManager() {
		if (sqliteDBManager == null){
			sqliteDBManager = SQLiteDBManager.getSQLiteDBManager(application);
		}
		return sqliteDBManager;
	}

	public void setLoginFlag(){
		messageService = null;
		if (bindService && messageServiceConn != null) {
			application.unbindService(messageServiceConn);
			application.stopService(new Intent(application, MessageService.class));
		}
	}
	@Override
	public void connectToServer() {
		uuidByte = StringUtil.hexStringToByteArray(getUuid());
		accessServerIp = getParam(Params.ACCESS_SERVER_IP, "");
		accessServerPort = getParam(Params.ACCESS_SERVER_PORT, 0);
		logger.info("uuidByte = "+ uuidByte +"  accessServerIp = "+ accessServerIp +"  accessServerPort = "+ accessServerPort);

		Intent messageService = new Intent(application, MessageService.class);

		logger.error("确定另一个进程messageService的哈希值------------->messageService.hashCode = "+this.messageService);

		//首先要绑定服务，获取service实例，才能注册handler。
		if (this.messageService == null) {
			bindService = application.bindService(messageService, messageServiceConn, BIND_AUTO_CREATE);
			logger.info("开始绑定服务MessageService"+bindService);
		}
		//IP、端口存在的话，启动一下，连到信令服务
		if (uuidByte.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0) {
			messageService.putExtra("uuid", uuidByte);
			messageService.putExtra("accessServerIp", accessServerIp);
			messageService.putExtra("accessServerPort", accessServerPort);
			application.startService(messageService);
			logger.info("开始启动服务MessageService, 连接到信令服务");
		}

	}

	@Override
	public void disConnectToServer() {
		//停止与服务器的连接
		try {
			if (bindService && messageServiceConn != null) {
				application.unbindService(messageServiceConn);
				application.stopService(new Intent(application, MessageService.class));
			}
			messageService = null;
			logger.error("停止与服务器的连接");
		} catch (Exception e) {
			logger.error("连接停止时出现异常", e);
		}
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				connectToServer();
//			}
//		}, 1000);
	}
	private IMessageService messageService;
	private ServiceConnection messageServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			logger.error("messageServiceConn服务连接成功");
			messageService = Stub.asInterface(service);

			clientChannel = null;
			getClientChannel().start();
			getClientChannel().registServerConnectionEstablishedHandler(serverConnectionEstablishedHandler);
			startService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			logger.error("mssageServiceConn服务断开连接");
			connectToServer();
//			messageService = null;
//			Intent messageService = new Intent(application, MessageService.class);
//			if (uuidByte.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0) {
//				messageService.putExtra("uuid", uuidByte);
//				messageService.putExtra("accessServerIp", accessServerIp);
//				messageService.putExtra("accessServerPort", accessServerPort);
//				application.startService(messageService);
//			}
//			application.bindService(messageService, messageServiceConn, Context.BIND_IMPORTANT);
		}
	};
	public ServiceConnection getMessageServiceConn(){
		return messageServiceConn;
	}
	private ServerConnectionEstablishedHandler serverConnectionEstablishedHandler = new ServerConnectionEstablishedHandler() {
		@Override
		public void handler(boolean connected) {
			logger.info("***********UDPClientBase**************connected = "+connected);
			notifyReceiveHandler(ReceiveServerConnectionEstablishedHandler.class, connected);
		}
	};



//	@Override
//	protected String newUuid() {
//		TelephonyManager telephonyManager = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
//		WifiManager wm = (WifiManager)application.getSystemService(Context.WIFI_SERVICE);
//		return telephonyManager.getDeviceId()==null?wm.getConnectionInfo().getMacAddress().hashCode()+"":telephonyManager.getDeviceId().hashCode()+"";
//	}

	@Override
	public void setAPKUpdateAddress(String identifyPath) {
		String updatePath = identifyPath.replace("/register/private/identify", "/apk/version.xml");
		TerminalFactory.getSDK().putParam(Params.UPDATE_URL, updatePath);
	}

	public SharedPreferences getAccount(){
		return account;
	}

	@SuppressLint("MissingPermission")
	@Override
	protected String newUuid() {
		String account = TerminalFactory.getSDK().getParam(UrlParams.ACCOUNT);
		if (Util.isEmpty(account)){
			TelephonyManager telephonyManager = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
			WifiManager wm = (WifiManager)application.getSystemService(Context.WIFI_SERVICE);
			account = telephonyManager.getDeviceId() == null ?
					wm.getConnectionInfo().getMacAddress().hashCode()+"" : telephonyManager.getDeviceId().hashCode()+"";
		}
		logger.info(" TerminalSDK4Android--------> account = "+account);
		return account;
	}

	private Bitmap createVideoThumbnail(String videoPath) {
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

}
