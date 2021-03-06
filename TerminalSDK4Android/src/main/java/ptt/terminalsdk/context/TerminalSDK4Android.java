package ptt.terminalsdk.context;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.allen.library.RxHttpUtils;
import com.allen.library.config.OkHttpConfig;
import com.allen.library.cookie.store.SPCookieStore;
import com.allen.library.interfaces.BuildHeadersListener;
import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.apache.log4j.Level;
import org.ddpush.im.client.v1.ServerConnectionChangedHandler;
import org.ddpush.im.client.v1.ServerConnectionEstablishedHandler;
import org.ddpush.im.util.StringUtil;
import org.easydarwin.push.UVCCameraService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.TerminalSDKBaseImpl;
import cn.vsx.hamster.terminalsdk.manager.audio.IAudioProxy;
import cn.vsx.hamster.terminalsdk.manager.channel.AbsClientChannel;
import cn.vsx.hamster.terminalsdk.manager.data.DataManager;
import cn.vsx.hamster.terminalsdk.manager.http.IHttpClient;
import cn.vsx.hamster.terminalsdk.manager.okhttp.LoggingInterceptor;
import cn.vsx.hamster.terminalsdk.manager.okhttp.MyCacheInterceptor;
import cn.vsx.hamster.terminalsdk.manager.servicebus.ServiceBusManager;
import cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory;
import cn.vsx.hamster.terminalsdk.model.Group;
import cn.vsx.hamster.terminalsdk.model.TerminalMessage;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveDownloadProgressHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveServerConnectionEstablishedHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUploadProgressHandler;
import cn.vsx.hamster.terminalsdk.tools.DataUtil;
import cn.vsx.hamster.terminalsdk.tools.OperateReceiveHandlerUtil;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import ptt.terminalsdk.IMessageService;
import ptt.terminalsdk.IMessageService.Stub;
import ptt.terminalsdk.broadcastreceiver.NetWorkConnectionChangeReceiver;
import ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver;
import ptt.terminalsdk.listener.InitUuidCallBack;
import ptt.terminalsdk.manager.MyDataManager;
import ptt.terminalsdk.manager.Prompt.PromptManager;
import ptt.terminalsdk.manager.audio.AudioProxy;
import ptt.terminalsdk.manager.channel.ClientChannel;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.manager.gps.BDGPSManager;
import ptt.terminalsdk.manager.gps.GPSManager;
import ptt.terminalsdk.manager.gps.recoder.LocationManager;
import ptt.terminalsdk.manager.gps.recoder.RecorderBDGPSManager;
import ptt.terminalsdk.manager.gps.recoder.RecorderGPSManager;
import ptt.terminalsdk.manager.gps.recoder.RecorderSfGPSManager;
import ptt.terminalsdk.manager.http.MyHttpClient;
import ptt.terminalsdk.manager.http.ProgressHelper;
import ptt.terminalsdk.manager.http.ProgressUIListener;
import ptt.terminalsdk.manager.live.LiveManager;
import ptt.terminalsdk.manager.message.SQLiteDBManager;
import ptt.terminalsdk.manager.nfc.INfcManager;
import ptt.terminalsdk.manager.nfc.NfcManager;
import ptt.terminalsdk.manager.powersave.PowerSaveManager;
import ptt.terminalsdk.manager.recordingAudio.RecordingAudioManager;
import ptt.terminalsdk.manager.search.SearchDataManager;
import ptt.terminalsdk.manager.video.VideoProxy;
import ptt.terminalsdk.manager.voip.VoipManager;
import ptt.terminalsdk.service.BluetoothLeService;
import ptt.terminalsdk.service.MessageService;
import ptt.terminalsdk.tools.ApkUtil;
import ptt.terminalsdk.tools.AppUtil;
import ptt.terminalsdk.tools.DeleteData;
import ptt.terminalsdk.tools.HttpUtil;
import ptt.terminalsdk.tools.SDCardUtil;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_CHECKCARRIERS_FAILURE;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_CHECKCARRIERS_SUCCESS;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_DOWNLOADCFG_SUCCESS;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_NETWORK_CONNECTED;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_NETWORK_DISCONNECTED;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_STARTSERVER_FAILURE;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_STARTSERVER_INPROC;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_STARTSERVER_SUCCESS;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_STOPSERVER_SUCCESS;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_TUNNEL_CONNECTED;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_TUNNEL_FAILURE;
import static ptt.terminalsdk.broadcastreceiver.VPNConnectionChangeReceiver.ACTION_INTENT_TUNNEL_FAILURE_AUTH;

public class TerminalSDK4Android extends TerminalSDKBaseImpl {


	private SharedPreferences account;
	public Application application;
	private boolean calling = false;
	private byte[] uuidByte;
	private boolean bindService;
	private String accessServerIp;
	private int accessServerPort;
	private String protocolType;
    private VoipManager voipManager;
    private LiveManager liveManager;
    private ServiceBusManager serviceBusManager;
	//DDpush连接
	private boolean Established = false;
	//在线状态，手机网络连上和UDP连上
	private NetWorkConnectionChangeReceiver netWorkConnectionChangeReceiver;

	private boolean isBindOnlineService;
	private boolean isBindBleService;
	private boolean isBindedUVCCameraService;
	private VPNConnectionChangeReceiver vpnConnectionChangeReceiver;
	private TimerTask timerTask;

	public TerminalSDK4Android(Application mApplication, String terminalMemberType){
		application = mApplication;
		account = application.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_MULTI_PROCESS);
		putParam(UrlParams.TERMINALMEMBERTYPE, terminalMemberType);
	}

	@Override
	protected void onStart() {
		//启动内部事件处理类
		OperateReceiveHandlerUtil.getInstance().start();
		putParam(Params.NETWORK_JITTER_CHECK_INTERVAL, 60 * 1000);//网络抖动检测间隔，60秒
		putParam(Params.GPS_UPLOAD_INTERVAL, 30 * 1000);//GPS上传时间间隔，5分钟
		locationStart();
		getVideoProxy().start();
		PromptManager.getInstance().start(application);
		getFileTransferOperation().start();
		searchDataStart();
        powerSaveManagerStart();
		nfcManagerStart();
		// 广播接收器，用来监听SSL服务发出的广播
		vpnConnectionChangeReceiver = new VPNConnectionChangeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_INTENT_STARTSERVER_INPROC);
		filter.addAction(ACTION_INTENT_STARTSERVER_SUCCESS);
		filter.addAction(ACTION_INTENT_STARTSERVER_FAILURE);
		filter.addAction(ACTION_INTENT_DOWNLOADCFG_SUCCESS);
		filter.addAction(ACTION_INTENT_STOPSERVER_SUCCESS);
		filter.addAction(ACTION_INTENT_NETWORK_CONNECTED);
		filter.addAction(ACTION_INTENT_NETWORK_DISCONNECTED);
		filter.addAction(ACTION_INTENT_TUNNEL_CONNECTED);
		filter.addAction(ACTION_INTENT_TUNNEL_FAILURE);
		filter.addAction(ACTION_INTENT_TUNNEL_FAILURE_AUTH);
		filter.addAction(ACTION_INTENT_CHECKCARRIERS_SUCCESS);
		filter.addAction(ACTION_INTENT_CHECKCARRIERS_FAILURE);
		application.registerReceiver(vpnConnectionChangeReceiver, filter);

		//个呼通讯录，请求的是自己的列表，还是所有成员列表
		putParam(Params.REQUEST_ALL, false);
		startUVCCameraService(application);
		initRxHttpUtils();
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

    public void registNetworkChangeHandler(){
		netWorkConnectionChangeReceiver = new NetWorkConnectionChangeReceiver();
		IntentFilter netFilter = new IntentFilter();
		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		application.registerReceiver(netWorkConnectionChangeReceiver,netFilter);
	}

	public void unregistNetworkChangeHandler(){
		if(netWorkConnectionChangeReceiver!=null){
			application.unregisterReceiver(netWorkConnectionChangeReceiver);
		}
	}

	@Override
	protected void onStop() {
		logger.error("TerminalSDK4Android----stop！！");
		nfcManagerStop();
        powerSaveManagerStop();
		searchDataStop();
		getFileTransferOperation().stop();
		locationStop();
		getVideoProxy().stop();
		PromptManager.getInstance().stop();
		application.unregisterReceiver(vpnConnectionChangeReceiver);
		stopUVCCameraService();
		getVoipCallManager().destroy(application);//VOIP服务注销
		putParam(Params.CURRENT_SPEAKER,"");

		logger.info("TerminalSDK4Android关闭了OnlineService");
		Intent onlineService = new Intent(application, OnlineService.class);
		application.stopService(onlineService);
		if(isBindOnlineService){
			application.unbindService(onlineServiceConn);
			isBindOnlineService = false;
		}
		Intent bleService = new Intent(application, BluetoothLeService.class);
		application.stopService(bleService);
		if(isBindBleService){
			application.unbindService(bleServiceConn);
			isBindBleService = false;
		}
		disConnectToServer();
		OperateReceiveHandlerUtil.getInstance().stop();
	}


	@SuppressLint("NewApi")/**日志生成文件保存*/
	public void configLogger() {

		File dir = new File(getLogDirectory());
		if (!dir.exists()) {
			try {
				//按照指定的路径创建文件夹
				dir.mkdirs();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File file = new File(getLogDirectory() + "log.txt");
		if (!file.exists()) {
			try {
				//在指定的文件夹中创建文件
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	@Override
	public <V>void  putList(String param, List<V> value){
		String result = list2String(value);
		account.edit().putString(param, result).apply();
	}

	@Override
	public <V> List<V> getList(String param,List<V> defaultValue,Class<V> clazz){
		List<V> list = new ArrayList<>();
		String json = account.getString(param, "");
		if (!TextUtils.isEmpty(json)) {
			Gson gson = new Gson();
			JsonArray array = new JsonParser().parse(json).getAsJsonArray();
			for (JsonElement elem : array) {
				list.add(gson.fromJson(elem, clazz));
			}
			return list;
		}else {
			return defaultValue;
		}
	}

	@Override
	public void putHashMap(String param, HashMap<String,String> value){
		Gson gson = new Gson();
		//转换成json数据，再保存
		String strJson = gson.toJson(value);
		logger.info("param:"+param+"---putHashMap:"+strJson);
		account.edit().putString(param,strJson).apply();
	}

	@Override
	public  HashMap<String,String> getHashMap(String param,HashMap<String,String> defaultValue){
		String strJson = account.getString(param, "");
		logger.info("getHashMap:"+strJson);
		if(TextUtils.isEmpty(strJson)){
			return defaultValue;
		}else {
			Gson gson = new Gson();
			Type type = new TypeToken<HashMap<String, String>>(){}.getType();
			return gson.fromJson(strJson, type);
		}
	}

    public void putTerminalMessageListMap(String param, Map<String,List<TerminalMessage>> value){
        //转换成json数据，再保存
        Gson gson = new Gson();
        String result = gson.toJson(value);
        Log.d("TerminalSDK4Android","保存hashMap结果"+result);
        account.edit().putString(param,result).apply();
    }

	public LinkedTreeMap<String,List<TerminalMessage>> getTerminalMessageListMap(String param, LinkedTreeMap<String, List<TerminalMessage>> defaultValue){
		String strJson = account.getString(param, "");
		logger.info("getListHashMap:"+strJson);
		if(TextUtils.isEmpty(strJson)){
			return defaultValue;
		}else {
            Type type = new TypeToken<Map<String, List<TerminalMessage>>>(){}.getType();
            LinkedTreeMap<String, List<TerminalMessage>> resultMap = GsonUtils.fromJson(strJson, type);
			Log.d("TerminalSDK4Android","获取hashMap结果"+resultMap);
			return resultMap;
		}
	}

	public <K> String list2String(List<K> list){
		String result = "";
		if(list !=null && !list.isEmpty()){
			String type = list.get(0).getClass().getSimpleName();
			JsonArray array = new JsonArray();
			try{
				switch(type){
					case "Boolean":
						for(int i = 0; i < list.size(); i++){
							array.add((Boolean) list.get(i));
						}
						break;
					case "Long":
						for(int i = 0; i < list.size(); i++){
							array.add((Long) list.get(i));
						}
						break;
					case "Float":
						for(int i = 0; i < list.size(); i++){
							array.add((Float) list.get(i));
						}
						break;
					case "String":
						for(int i = 0; i < list.size(); i++){
							array.add((String) list.get(i));
						}
						break;
					case "Integer":
						for(int i = 0; i < list.size(); i++){
							array.add((Integer) list.get(i));
						}
						break;
					default:
						Gson gson = new Gson();
						for(int i = 0; i < list.size(); i++){
							JsonElement obj = gson.toJsonTree(list.get(i));
							array.add(obj);
						}
						break;
				}
				result =  array.toString();
			}catch(Exception e){
				e.printStackTrace();
			}

		}
		return result;
	}

	@Override
    public <V> void putBean(String param, V value){
        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(value);
        account.edit().putString(param,strJson).apply();
    }

    @Override
    public <V> V getBean(String param, V defaultValue,Class<V> clazz){
        String strJson = account.getString(param, "");
        if(TextUtils.isEmpty(strJson)){
            return defaultValue;
        }else {
            Gson gson = new Gson();
            return gson.fromJson(strJson, clazz);
        }
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
    public <T extends Serializable> T getSerializable(String param, T defaultValue) {
        // 拿出持久化数据
        T obj = null;
        FileInputStream in = null;
        ObjectInputStream oin = null;
        try {
            File file = new File(getSerializableDataDirectory(), param);
            Util.createFileOrDirectoryIfNotExists(file);
            in = new FileInputStream(file);
            oin = new ObjectInputStream(in);
            obj = (T) oin.readObject();
            in.close();
            oin.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(oin != null){
                try {
                    oin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return defaultValue;
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
        } catch (Exception e) {
			e.printStackTrace();
		}
		finally {
        	if(bout != null){
        		try {
					bout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if(oout != null){
        		try {
        			oout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	if(out != null){
        		try {
        			out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
	}

	private void startService(){
		logger.info("TerminalSDK4Android启动了OnlineService");
		Intent onlineService = new Intent(application, OnlineService.class);
		Intent bleService = new Intent(application, BluetoothLeService.class);
		if (application != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				application.startForegroundService(onlineService);
			} else {
				application.startService(onlineService);
			}
//			application.startService(onlineService);
			isBindOnlineService = application.bindService(onlineService,onlineServiceConn,BIND_AUTO_CREATE);
			application.startService(bleService);
			isBindBleService = application.bindService(bleService,bleServiceConn,BIND_AUTO_CREATE);
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
			isBindOnlineService = false;
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
			isBindBleService = false;
			startService();
		}
	};

	/**得到文字记录的存储位置*/
	@Override
    public String getWordRecordDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "wordRecord"
				+ File.separator;
	}
	/**得到图片记录的存储位置*/
	@Override
    public String getPhotoRecordDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "photoRecord"
				+ File.separator;
	}
	/**得到录音记录的存储位置*/
	@Override
    public String getAudioRecordDirectory(){
		return Environment.getExternalStorageDirectory()
                + File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "audioRecord"
                + File.separator;
	}
	/**得到视频记录的存储位置*/
	@Override
    public String getVideoRecordDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager()) + File.separator + "videoRecord"
				+ File.separator;
	}
	/**得到文件的存储位置*/
	@Override
    public String getFileRecordDirectory() {
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "file"
				+ File.separator;
	}
	/**得到缓存的存储位置*/
	@Override
	public String getCacheDirectory(){
		return application.getCacheDir()
				+ File.separator + "okhttpCache"
				+ File.separator;
	}
	/**得到日志的存储位置*/
	@Override
    public String getLogDirectory() {
		return Environment.getExternalStorageDirectory()
					+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "logs"
					+ File.separator;
	}

	/**得到保存视频的位置*/
	public String getVideoDirectoty(){
		if(TextUtils.isEmpty(getParam(Params.VIDOE_STORE_PATH))){
			putParam(Params.VIDOE_STORE_PATH , Environment.getExternalStorageDirectory()
					+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "VideoRecord"
					+ File.separator);
		}
		return getParam(Params.VIDOE_STORE_PATH)
				+ File.separator;
	}

	public String getUavDirectory(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + application.getApplicationInfo().loadLabel(application.getPackageManager())+ File.separator + "uavFile"+File.separator;
	}

	/**得到存放录制视频和照片的文件目录*/
	@Override
    public String getBITRecordesDirectoty(int code){

		return getBITDirectoryByCode(code) + File.separator  +  (needAndroidDataPath()?"Android/data/":"") + application.getPackageName()+ File.separator;
	}

	/**得到存放录制视频文件的目录*/
	@Override
    public String getBITVideoRecordesDirectoty(int code){
		return getBITDirectoryByCode(code) + File.separator +   (needAndroidDataPath()?"Android/data/":"") + application.getPackageName() + "/VideoRecord"+ File.separator;
	}

	/**得到存放照片文件的目录*/
	@Override
    public String getBITPhotoRecordedDirectoty(int code){
		return getBITDirectoryByCode(code) + File.separator +   (needAndroidDataPath()?"Android/data/":"") + application.getPackageName() + "/PhotoRecord"+ File.separator;
	}

	/**得到存放录音文件的目录*/
	@Override
    public String getBITAudioRecordedDirectoty(int code){
		return getBITDirectoryByCode(code) + File.separator +   (needAndroidDataPath()?"Android/data/":"") + application.getPackageName() + "/AudioRecord"+ File.separator;
	}
	/**判断外部存储可用*/
	@Override
    public boolean checkeExternalStorageIsAvailable(int code) {
		if(code == BitStarFileDirectory.USB.getCode()){
			return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		}else if(code == BitStarFileDirectory.SDCARD.getCode()){
			String sdcardDir = getBITSDCardDirectory();
			return !TextUtils.isEmpty(sdcardDir);
		}
		return false;
	}
	/**获取外部存储可用空间的大小*/
	@Override
    public long getExternalUsableSize(int code) {
		String sdcardDir = null;
		if(code == BitStarFileDirectory.USB.getCode()){
			if(checkeExternalStorageIsAvailable(code)){
				sdcardDir = Environment.getExternalStorageDirectory().getPath();
			}else{
				return 0;
			}
		}else if(code == BitStarFileDirectory.SDCARD.getCode()){
			sdcardDir = getBITSDCardDirectory();
		}
		if(!TextUtils.isEmpty(sdcardDir)){
			StatFs sf = new StatFs(sdcardDir);
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();
			return availCount * blockSize;
		}else{
			return 0;
		}
	}
	/**
	 * 根据code获取存储空间的目录
	 * @return
	 */
	public  String getBITDirectoryByCode(int code){
		String sdcardDir = "";
		if(code == BitStarFileDirectory.USB.getCode()){
			sdcardDir = Environment.getExternalStorageDirectory().getPath();
		}else if(code == BitStarFileDirectory.SDCARD.getCode()){
			sdcardDir = getBITSDCardDirectory();
		}
		return sdcardDir;
	}
	/**获取比特星sdcard的根目录*/
	@Override
	public String getBITSDCardDirectory() {
		application.getExternalFilesDir(null);
		StorageManager mStorageManager = (StorageManager) application.getSystemService(Context.STORAGE_SERVICE);
		Class<?> storageVolumeClazz = null;
		try {
			storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
			Method stateMethod = storageVolumeClazz.getMethod("getState");
			Object result = getVolumeList.invoke(mStorageManager);
			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++) {
				Object storageVolumeElement = Array.get(result, i);
				String path = (String) getPath.invoke(storageVolumeElement);
				boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
				String state = (String) stateMethod.invoke(storageVolumeElement);
				if (removable && state.equals(Environment.MEDIA_MOUNTED)) {
					logger.info("外置内存卡路径=====" + path );
					return path;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public VoipManager getVoipCallManager() {
		if(voipManager==null){
			voipManager = new VoipManager();
        }
		return voipManager;
	}

	public LiveManager getLiveConfigManager(){
		if(liveManager == null){
			liveManager = new LiveManager();
		}
		return liveManager;
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

	//顺丰定位
	private RecorderSfGPSManager recorderSfGPSManager;
	public RecorderSfGPSManager getRecorderSfGPSManager(){
		if(recorderSfGPSManager == null){
			recorderSfGPSManager = new RecorderSfGPSManager(application);
		}
		return recorderSfGPSManager;
	}

	private RecorderGPSManager recorderGPSManager;
	public RecorderGPSManager getRecorderGPSManager(){
		if(recorderGPSManager == null){
			recorderGPSManager = new RecorderGPSManager(application);
		}
		return recorderGPSManager;
	}
	private RecorderBDGPSManager recorderBDGPSManager;
	public RecorderBDGPSManager getRecorderBDGPSManager(){
		if(recorderBDGPSManager == null){
			recorderBDGPSManager = new RecorderBDGPSManager(application);
		}
		return recorderBDGPSManager;
	}
	private LocationManager locationManager;
	public LocationManager getLocationManager(){
		if(locationManager == null){
			locationManager = new LocationManager(application);
		}
		return locationManager;
	}
	private SearchDataManager searchDataManager;
	public SearchDataManager getSearchDataManager(){
		if(searchDataManager == null){
			searchDataManager = new SearchDataManager();
		}
		return searchDataManager;
	}
	public Application getApplication(){
		return application;
	}
	private WakeLock wakeLock;
	public WakeLock getWakeLock(){
		if(wakeLock == null){
			PowerManager pm = (PowerManager) application.getSystemService(Context.POWER_SERVICE);
			if(pm != null){
				wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vsx:tag");
			}
		}
		return wakeLock;
	}

	@Override
	public boolean hasNetwork() {
	    ConnectivityManager cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
	    if(cm == null){
			return false;
		}
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}
	@Override
	public boolean isServerConnected(){
		return Established;
	}
	private FileTransferOperation fileTransferOperation;
	public FileTransferOperation getFileTransferOperation(){
		if(fileTransferOperation == null){
			fileTransferOperation = new FileTransferOperation(application);
		}
		return fileTransferOperation;
	}
	private RecordingAudioManager recordingAudioManager;
	public RecordingAudioManager getRecordingAudioManager(){
		if(recordingAudioManager == null){
			recordingAudioManager = new RecordingAudioManager(application);
		}
		return recordingAudioManager;
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
		TerminalFactory.getSDK().putParam(Params.MESSAGE_VERSION, 0L);
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
				if(percent<0){
					percent = 0;
				}
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

	/**
	 * 向指定人或者组上传文件
	 * @param list
	 * @param toUniqueNos
	 * @param file
	 * @param terminalMessage
	 * @param isNeedUi
	 */
	public void upload(List<Integer> list , List<Long> toUniqueNos, File file, TerminalMessage terminalMessage, boolean isNeedUi){
		TerminalFactory.getSDK().getTerminalMessageManager().uploadDataByOkHttp(list,toUniqueNos, file, terminalMessage, isNeedUi);
	}

	/**界面需要上传进度展示*/
	public void upload(String url, File file, TerminalMessage terminalMessage, boolean isNeedUi){
		TerminalFactory.getSDK().getTerminalMessageManager().uploadDataByOkHttp(url, file, terminalMessage, isNeedUi);
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
                if (progressBar != null) {
					progressBar.setVisibility(View.GONE);
				}
                if (textView != null) {
					textView.setVisibility(View.GONE);
				}
            }
        }, 2000);
	}


	private SQLiteDBManager sqliteDBManager;
	@Override
	public SQLiteDBManager getSQLiteDBManager() {
		if (sqliteDBManager == null){
			sqliteDBManager = SQLiteDBManager.getSQLiteDBManager(application);
		}
		return sqliteDBManager;
	}

	public void setLoginFlag(){
		logger.info("setLoginFlag");
		messageService = null;
		if (bindService && messageServiceConn != null) {
			application.unbindService(messageServiceConn);
			application.stopService(new Intent(application, MessageService.class));
		}
	}
	@Override
	public void connectToServer() {
		logger.error("--vsx--connectToServer");
//		disConnectToServer();
		uuidByte = StringUtil.hexStringToByteArray(getUuid());
		accessServerIp = getParam(Params.ACCESS_SERVER_IP, "");
		protocolType = getParam(Params.PROTOCOL_TYPE, Params.UDP);
		if(Params.TCP.equals(protocolType)){
			accessServerPort = getParam(Params.ACCESS_SERVER_TCP_PORT,0);
		}else if(Params.UDP.equals(protocolType)){
			accessServerPort = getParam(Params.ACCESS_SERVER_PORT, 0);
		}
		logger.info("uuidByte.length"+ uuidByte.length +"  accessServerIp = "+ accessServerIp +"  accessServerPort = "+ accessServerPort);


		Intent messageServiceIntent = new Intent(application, MessageService.class);

		logger.error("确定另一个进程messageService的哈希值------------->messageService.hashCode = "+this.messageService);

		//首先要绑定服务，获取service实例，才能注册handler。
		//如果messageService不为空把Service先unbind,再bindService，保证onServiceConnected可以正常被调用
		if(this.messageService !=null){
			unBindMessageService();
		}
		if (uuidByte.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0) {
			messageServiceIntent.putExtra("uuid", uuidByte);
			messageServiceIntent.putExtra("accessServerIp", accessServerIp);
			messageServiceIntent.putExtra("accessServerPort", accessServerPort);
			messageServiceIntent.putExtra("protocolType",protocolType);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				application.startForegroundService(messageServiceIntent);
			} else {
				application.startService(messageServiceIntent);
			}
			logger.info("开始启动服务MessageService, 连接到信令服务");


//			if (this.messageService == null) {
				application.bindService(messageServiceIntent, messageServiceConn, BIND_AUTO_CREATE);
				bindService = true;
				logger.info("开始绑定服务MessageService"+bindService);
//			}
		}else {
			logger.error("接入服务地址不对！！不能出现这种情况");
		}
	}

	@Override
	public void disConnectToServer() {
		logger.info("调用了disConnectToServer");
		//停止与服务器的连接
		try {
			if(getClientChannel()!=null){
				getClientChannel().stop();
			}
			clientChannel = null;
			application.stopService(new Intent(application, MessageService.class));
			if (bindService && messageServiceConn != null) {
				application.unbindService(messageServiceConn);
				logger.error("停止与服务器的连接");
			}
			bindService = false;
			messageService = null;
		} catch (Exception e) {
			logger.error("连接停止时出现异常", e);
		}
	}

	private IMessageService messageService;
	private ServiceConnection messageServiceConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			logger.error("MessageService --- onServiceConnected");
			messageService = Stub.asInterface(service);
			if(getClientChannel()!=null){
				getClientChannel().stop();
			}
			clientChannel = null;
			getClientChannel().registServerConnectionChangedHandler(serverConnectionChangedHandler);
			getClientChannel().registServerConnectionEstablishedHandler(serverConnectionEstablishedHandler);
			getClientChannel().start();
			startService();//bind方式启动onlineservice，普通方式启动bluetoothservice
			//当绑定成功之后再开始连接Client
			notifyConnetClientToServer();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			logger.error("MessageServiceon----onServiceDisconnected");
//			disConnectToServer();
//			reTryConnectToServer();
		}
	};

	/**
	 * 重新尝试再连接MessageServer
	 */
	private void reTryConnectToServer() {
		try{
			if(timerTask != null){
				timerTask.cancel();
				timerTask = null;
			}
		}catch (Exception ef){
			logger.error("onServiceDisconnected  Exception f:"+ef);
			timerTask = null;
		}finally {
			try{
				timerTask = new TimerTask() {
					@Override
					public void run() {

						connectToServer();
					}
				};
				TerminalFactory.getSDK().getTimer().schedule(timerTask,5*1000);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * 如果messageService不为空把Service先unbind,再bindService，保证onServiceConnected可以正常被调用
	 */
	private void unBindMessageService() {
		try{
			if(getClientChannel()!=null){
				getClientChannel().stop();
			}
			clientChannel = null;
			if (bindService && messageServiceConn != null) {
				application.unbindService(messageServiceConn);
				logger.error("停止与服务器的连接");
			}
			application.stopService(new Intent(application, MessageService.class));
		}catch (Exception e){
			logger.error(e.toString());
		}finally {
			bindService = false;
			messageService = null;
		}
	}

	/**
	 * 当绑定成功之后再开始连接Client
	 */
	private void notifyConnetClientToServer(){
		Intent intent = new Intent(Params.BR_START_CONNECT_CLIENT);
		if (uuidByte.length != 0 && accessServerIp.length() != 0 && accessServerPort != 0) {
			intent.putExtra("uuid", uuidByte);
			intent.putExtra("accessServerIp", accessServerIp);
			intent.putExtra("accessServerPort", accessServerPort);
			intent.putExtra("protocolType", protocolType);
		}
		application.sendBroadcast(intent);
	}

	public ServiceConnection getMessageServiceConn(){
		return messageServiceConn;
	}

	/**
	 * MessageService 执行了unbind的时候
	 */
	private ServerConnectionChangedHandler serverConnectionChangedHandler = new ServerConnectionChangedHandler(){
		@Override
		public void handler(boolean connected){
			logger.info("ServerConnectionChangedHandler:"+connected);
			if(!connected && bindService){
				connectToServer();
			}
		}
	};

	private ServerConnectionEstablishedHandler serverConnectionEstablishedHandler = new ServerConnectionEstablishedHandler() {
		@Override
		public void handler(boolean connected) {
			logger.info("***********ClientBase**************connected = "+connected);
			Established = connected;
			notifyReceiveHandler(ReceiveServerConnectionEstablishedHandler.class, connected);
		}
	};

	@Override
	public void setAPKUpdateAddress(String path) {
		TerminalFactory.getSDK().putParam(Params.UPDATE_URL, ApkUtil.getAPKUpdateAddress(path));
	}
	@Override
	public void setLogUpLoadAddress(String path) {
		TerminalFactory.getSDK().putParam(Params.LOG_UPLOAD_URL, ApkUtil.getLogUpdateAddress(path));
	}

    private PowerSaveManager powerSaveManager;
    @Override
    public PowerSaveManager getPowerSaveManager() {
        if (powerSaveManager == null){
            powerSaveManager =new PowerSaveManager(application);
        }
        return powerSaveManager;
    }

	private INfcManager nfcManager;
	public INfcManager getNfcManager() {
		if (nfcManager == null){
			nfcManager =new NfcManager(application);
		}
		return nfcManager;
	}

	@Override
	public void setDataUpdateAddress(String path) {
		StringBuffer address = new StringBuffer();
		address.append(path);
		address.append(Params.FILE_CHECK_DATA_UPDATE);
		TerminalFactory.getSDK().putParam(Params.FILE_CHECK_DATA_UPDATE_URL,address.toString());
	}

	public SharedPreferences getAccount(){
		return account;
	}

    /**
     * 初始化uuid，主要是从文件中获取uuid
     */
	public void initUuid(InitUuidCallBack callBack){
        getThreadPool().execute(() -> {
        	try{
                //从sharepreference获取
				String uuid = getParam(Params.UUID,"");
				String uuidFileData = SDCardUtil.readUuidFromSdCard(getUuidFilePath()+getUuidFileName());
				logger.info("initUuid--uuid："+uuid+"--uuidFileData:"+uuidFileData);
				if(TextUtils.isEmpty(uuid)){
					//从文件中获取uuid，如果有数据，就存到sharepreference中
					if(!TextUtils.isEmpty(uuidFileData)){
						putParam(Params.UUID,uuidFileData);
					}
				}else{
					//判断文件中是否有uuid数据，没有的话，需要再写入
					if(TextUtils.isEmpty(uuidFileData)){
						SDCardUtil.saveUuidToSdCard(getUuidFilePath(),getUuidFileName(), uuid);
					}
				}
			}catch (Exception e){
        		e.printStackTrace();
			}finally {
				if(callBack!=null){
					callBack.onComplete();
				}
			}
        });
	}


	@Override
	public String getUuid(){
		//从sharepreference获取
		String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		String uuid = getParam(Params.UUID,"");
		logger.info("getUuid-uuid："+uuid);
		if(TextUtils.isEmpty(uuid)){
			uuid = UUID.randomUUID().toString();

			//保存在sharepreference
			putParam(Params.UUID,uuid);
			//写入sd卡中
			String finalUuid = uuid;
			getThreadPool().execute(() -> {
				SDCardUtil.saveUuidToSdCard(getUuidFilePath(),getUuidFileName(), finalUuid);
			});
		}
		String result = Util.md5(uuid+deviceType);
		logger.info("getUuid-uuid-result："+result);
		return result;
	}

    /**
     * 获取uuid文件的目录
     * @return
     */
	public String getUuidFilePath(){
		return Environment.getExternalStorageDirectory()
				+ File.separator + "."+ application.getPackageName()+File.separator;
	}
	public String getUuidFileName(){
	    return "uuid.txt";
    }

	@SuppressLint("MissingPermission")
	@Override
	protected String newUuid() {
		String account;
		String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		if(!TextUtils.isEmpty(deviceType)&&(TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString()))){
			account = newIMEI();
		}else{
			TelephonyManager telephonyManager = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
			WifiManager wm = (WifiManager)application.getSystemService(Context.WIFI_SERVICE);
			account = telephonyManager.getDeviceId() == null ?
					wm.getConnectionInfo().getMacAddress().hashCode()+"" : telephonyManager.getDeviceId().hashCode()+"";
		}
		logger.info(" TerminalSDK4Android--------> account = "+account+"---terminalType = "+deviceType);
//		return account+deviceType;
		return account;
//		return account+"1234567890";
	}
	@SuppressLint("MissingPermission")
	@Override
	protected String newOldUuid() {
		String deviceType = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		String account = "";
		TelephonyManager telephonyManager = (TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
		WifiManager wm = (WifiManager)application.getSystemService(Context.WIFI_SERVICE);
		if(telephonyManager.getDeviceId() != null ){
			account = telephonyManager.getDeviceId();
			logger.info(" TerminalSDK4Android--------> newOldUuid = "+account);
		}else if(!TextUtils.isEmpty(wm.getConnectionInfo().getMacAddress())
				&&!TextUtils.equals(wm.getConnectionInfo().getMacAddress(),"02:00:00:00:00:00")){
			account = wm.getConnectionInfo().getMacAddress();
			logger.info(" TerminalSDK4Android--------> newOldUuid getMacAddress= "+account);
		}
		logger.info(" TerminalSDK4Android--------> newOldUuid = "+account+"---terminalType = "+deviceType);
		return (TextUtils.isEmpty(account))?"":(account.hashCode()+""+deviceType);
	}

	@Override
	protected String newIMSIUuid() {
		String account = TerminalFactory.getSDK().getParam(UrlParams.ACCOUNT);
		if (Util.isEmpty(account)){
			account = newIMSI();
		}
		String terminalType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE, "");
		logger.info(" TerminalSDK4Android--------> account = "+account+"---terminalType = "+terminalType);
		return account+terminalType;
	}

	@SuppressLint("MissingPermission")
	@Override
	public String newIMSI() {
		try {
			TelephonyManager telephonyManager=(TelephonyManager)application.getSystemService(Context.TELEPHONY_SERVICE);
			//获取IMSI号
			String imsi = telephonyManager.getSubscriberId();
			if(null == imsi){
				imsi="";
			}
			return imsi;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	@SuppressLint("MissingPermission")
	@Override
	protected String newIMEI() {
		try {
			//实例化TelephonyManager对象
//			TelephonyManager telephonyManager = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
			//获取IMEI号
			String imei = AppUtil.getIMEI(application);
			//在次做个验证，也不是什么时候都能获取到的啊
			if (TextUtils.isEmpty(imei)) {
				imei = "";
			}
			return imei;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public void renovateVideoRecord(String videRecordPath){

		try{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				Intent mediaScanIntent = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri contentUri = Uri.fromFile(new File(videRecordPath+ "-0.mp4"));
				mediaScanIntent.setData(contentUri);
				application.sendBroadcast(mediaScanIntent);
			} else {
				Intent scanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
				scanIntent.setData(Uri.fromFile(new File(getVideoDirectoty())));
				application.sendBroadcast(scanIntent);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 根据设备的类型开启对应的定位
	 */
	private void locationStart() {
		String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		if(!TextUtils.isEmpty(deviceType)){
			if(!TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_HDMI.toString())){
//				if(TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString())){
					getLocationManager().start();
//				}else{
//					getBDGPSManager().start();
//					getGpsManager().start();
//				}
			}
		}
	}
	/**
	 * 根据设备的类型关闭对应的定位
	 */
	private void locationStop(){
		String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		if(!TextUtils.isEmpty(deviceType)){
			if(!TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_HDMI.toString())){
//				if(TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString())){
					getLocationManager().stop();
//				}else{
//					getGpsManager().stop();
//					getBDGPSManager().stop();
//				}
			}
		}
	}

	/**
	 * 根据设备类型开启搜索
	 */
	private void searchDataStart() {
		if(checkSearchDataDevice()){
			getSearchDataManager().start();
		}
	}

	/**
	 * 根据设备类型开启搜索
	 */
	private void searchDataStop() {
		if(checkSearchDataDevice()){
			getSearchDataManager().stop();
		}
	}

	/**
	 * 判断哪些设备可以加载搜索数据
	 * @return
	 */
	private boolean checkSearchDataDevice(){
		String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		if(!TextUtils.isEmpty(deviceType)){
			if(TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_PHONE.toString())
					||TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_UAV.toString())){
				return true;
			}
		}
		return false;
	}

    /**
     * 开启省电管理
     */
    private void powerSaveManagerStart() {
        if(DataUtil.checkPowerSaveManagerDevice()){
            getPowerSaveManager().start();
        }
    }

    /**
     * 关闭省电管理
     */
    private void powerSaveManagerStop() {
        if(DataUtil.checkPowerSaveManagerDevice()){
            getPowerSaveManager().stop();
        }
    }

	/**
	 * 开启NFC数据交换管理
	 */
	private void nfcManagerStart() {
		if(DataUtil.checkNfcManagerDevice()){
			getNfcManager().start();
		}
	}

	/**
	 * 关闭NFC数据交换管理
	 */
	private void nfcManagerStop() {
		if(DataUtil.checkNfcManagerDevice()){
			getNfcManager().stop();
		}
	}

    @Override
	public Group getGroupByGroupNo(int no){
		return DataUtil.getGroupByGroupNo(no);
	}

	public void startUVCCameraService(Application application){
		Intent intent = new Intent(application, UVCCameraService.class);
		isBindedUVCCameraService = application.bindService(intent,cameraconn,BIND_AUTO_CREATE);
	}

	public void stopUVCCameraService(){
		if(isBindedUVCCameraService){
			application.unbindService(cameraconn);
		}
	}

//	private UVCCameraService.MyBinder uvcBinder;
	private ServiceConnection cameraconn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
//			uvcBinder = (UVCCameraService.MyBinder) service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e("MyApplication", "UVCCameraService服务断开了");
			isBindedUVCCameraService = false;
		}
	};

	public boolean isStart(){
		return started;
	}

	@Override
	public DataManager getDataManager(){
		if(dataManager == null){
			dataManager = new MyDataManager();
		}
		return dataManager;
	}

	@Override
	public void audioProxyStart(){
		getAudioProxy().start(TerminalFactory.getSDK().getParam(Params.PROTOCOL_TYPE, Params.UDP));
	}

	@Override
	public void initVoip() {

	}

	@Override
	public Object getEventManager() {
		return null;
	}

	@Override
	public String getPackageName() {
		return application.getPackageName();
	}

    @Override
    public ServiceBusManager getServiceBusManager() {
	    if(serviceBusManager == null){
            serviceBusManager = new ServiceBusManager();
        }
        return serviceBusManager;
    }

	@Override public boolean inMeeting() {
		return AppUtil.isForeground(application,"cn.vsx.vc.activity.VideoMeetingActivity")||
				AppUtil.isForeground(application,"cn.vsx.vc.activity.VideoMeetingInvitationActivity");
	}

	/**
	 * 全局请求的统一配置（以下配置根据自身情况选择性的配置即可）
	 */
	private void initRxHttpUtils() {
		RxHttpUtils
				.getInstance()
				.init(application)
				.config()
				//自定义factory的用法
				//.setCallAdapterFactory(RxJava2CallAdapterFactory.create())
				//.setConverterFactory(ScalarsConverterFactory.create(),GsonConverterFactory.create(GsonAdapter.buildGson()))
				//配置全局baseUrl
				//开启全局配置
				.setOkClient(createOkHttp(10));
	}
      @Override
	  public OkHttpClient createOkHttp(int cacheTime) {
		//        获取证书
		//        InputStream cerInputStream = null;
		//        InputStream bksInputStream = null;
		//        try {
		//            cerInputStream = getAssets().open("YourSSL.cer");
		//            bksInputStream = getAssets().open("your.bks");
		//        } catch (IOException e) {
		//            e.printStackTrace();
		//        }

		OkHttpClient okHttpClient = new OkHttpConfig
				.Builder(application)
				//添加公共请求头
				.setHeaders(new BuildHeadersListener() {
					@Override
					public Map<String, String> buildHeaders() {
						HashMap<String, String> hashMap = new HashMap<>();
						hashMap.put("appVersion", getVersionName());
						hashMap.put("Accept", "application/json");
						hashMap.put("Content-type",
								"application/json;charset=UTF-8");
						hashMap.put("client", "android");
						return hashMap;
					}
				})

				//添加自定义拦截器
				.setAddInterceptor(new LoggingInterceptor())
				.setCacheMaxSize(10*1024*1024)
				.setAddInterceptor(new MyCacheInterceptor())
//				.setAddInterceptor(new MyCacheInterceptor())
//				.cache(new Cache(new File(TerminalFactory.getSDK().getCacheDirectory()), 100*1024*1024))
				//开启缓存策略(默认false)
				//1、在有网络的时候，先去读缓存，缓存时间到了，再去访问网络获取数据；
				//2、在没有网络的时候，去读缓存中的数据。
				.setCache(true)
				.setCachePath(TerminalFactory.getSDK().getCacheDirectory())
				.setHasNetCacheTime(cacheTime)//默认有网络时候缓存60秒
				//全局持久话cookie,保存到内存（new MemoryCookieStore()）或者保存到本地（new SPCookieStore(this)）
				//不设置的话，默认不对cookie做处理
				.setCookieType(new SPCookieStore(application))
				//可以添加自己的拦截器(比如使用自己熟悉三方的缓存库等等)
				//.setAddInterceptor(null)
				//全局ssl证书认证
				//1、信任所有证书,不安全有风险（默认信任所有证书）
				//.setSslSocketFactory()
				//2、使用预埋证书，校验服务端证书（自签名证书）
				//.setSslSocketFactory(cerInputStream)
				//3、使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
				//.setSslSocketFactory(bksInputStream,"123456",cerInputStream)
				//设置Hostname校验规则，默认实现返回true，需要时候传入相应校验规则即可
				//.setHostnameVerifier(null)
				//全局超时配置
				.setReadTimeout(10)
				//全局超时配置
				.setWriteTimeout(10)
				//全局超时配置
				.setConnectTimeout(10)
				//全局是否打开请求log日志
				.setDebug(false)
				.build();

		return okHttpClient;
	}

	public String getVersionName(){
		String localVersion = "";
		try{
			PackageInfo packageInfo = application.getApplicationContext().getPackageManager().getPackageInfo(application.getPackageName(), 0);
			localVersion = packageInfo.versionName;
		}catch(PackageManager.NameNotFoundException e){
			e.printStackTrace();
		}
		return localVersion;
	}

	private boolean needAndroidDataPath(){
		String deviceType = MyTerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
		if(!TextUtils.isEmpty(deviceType)){
			if(TextUtils.equals(deviceType, TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.toString())
					&& (TextUtils.equals("CL310A",Build.MODEL)||TextUtils.equals("H40",Build.MODEL))){
				return true;
			}
		}
		return false;
	}

}
