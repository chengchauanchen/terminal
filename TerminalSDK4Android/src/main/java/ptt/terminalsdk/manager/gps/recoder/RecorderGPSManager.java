package ptt.terminalsdk.manager.gps.recoder;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import org.apache.log4j.Logger;

import java.util.List;

import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;

public class RecorderGPSManager {
	private Context context;
	private Logger logger = Logger.getLogger(getClass());

	private LocationManager locationManager;
	private String locationProvider;
	private LocationListener locationListener;

	public RecorderGPSManager(Context context) {
		this.context = context;
	}

	/**
	 * 初始化LocationManager
	 */
	public void init() {
		logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager初始化了");
		locationManager = (LocationManager) MyTerminalFactory.getSDK().getApplication().getSystemService(Context.LOCATION_SERVICE);
		//获取所有可用的位置提供器
		List<String> providersFalse = locationManager.getProviders(false);
		logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager所有的locationProvider：" + providersFalse);
		List<String> providers = locationManager.getProviders(true);
		logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager可用的locationProvider：" + providers);
		if (providers.contains(LocationManager.GPS_PROVIDER)) {
			//如果是GPS
			locationProvider = LocationManager.GPS_PROVIDER;
		} else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			//如果是Network
			locationProvider = LocationManager.NETWORK_PROVIDER;
		}

		if (Util.isEmpty(locationProvider)) {
			locationProvider = LocationManager.GPS_PROVIDER;
		}
		logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager获取到的位置提供者是：locationProvider = " + locationProvider);
	}

	public void createLocationListener() {
		locationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
				logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"RecorderGPSManager--onProviderEnabled    provider = " + provider);
			}

			@Override
			public void onProviderDisabled(String provider) {
				logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"RecorderGPSManager--onProviderDisabled    provider = " + provider);
			}

			@Override
			public void onLocationChanged(Location location) {
				logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager中onLocationChanged--Longitude:" + location.getLongitude()+"--Latitude:" +location.getLatitude()
						+"--location:" +location);
				StringBuffer stringBuffer = new StringBuffer();
				try {
					List<Address> addresses = new Geocoder(context).getFromLocation(
							location.getLatitude(), location.getLongitude(),
							1);
					if (addresses.size() > 0) {
						Address address = addresses.get(0);
						stringBuffer.append(address.getCountryName());
                        if(address.getMaxAddressLineIndex() == 0){
                            stringBuffer.append(address.getAddressLine(0));
                        }else{
                            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                stringBuffer.append(address.getAddressLine(i));
                            }
                        }
					}
				}catch (Exception e){
					e.printStackTrace();
				}
				location.setExtras(MyTerminalFactory.getSDK().getLocationManager().getAddressBundle(stringBuffer.toString()));
				logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager中onLocationChanged--addresses:" + stringBuffer.toString());
				MyTerminalFactory.getSDK().getLocationManager().dispatchCommitLocation(location);
			}
		};
	}

	/**
	 * 停止
	 */
	public void stop() {
		logger.info(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPSManager销毁了");
		if (!Util.isEmpty(locationProvider)) {
			if (locationManager != null && locationListener != null) {
				locationManager.removeUpdates(locationListener);
			}
		}
	}

	/**
	 * 检查是否初始化
	 */
	private void checkInit(){
		if(locationManager == null||Util.isEmpty(locationProvider)){
			init();
		}
	}

	/**
	 * 获取locationManager
	 * @return
	 */
	public LocationManager getLocationManager(){
		if(locationManager == null){
			init();
		}
		return locationManager;
	}

	/**
	 * 获取locationProvider
	 * @return
	 */
	public String  getlocationProvider(){
		if(Util.isEmpty(locationProvider)){
			init();
		}
		return locationProvider;
	}

	/**
	 * 获取locationListener
	 * @return
	 */
	public LocationListener getLocationListener(){
		if(locationListener == null){
			createLocationListener();
		}
		return locationListener;
	}

	/**
	 * 检查GPS是否可用，并赋值是否是聊天页面
	 */
	public boolean checkInitCompleteAndIsChat() {
		if (checkGPSIsEnable()) {
			return true;
		}
		return false;
	}

	/**
	 * 检查GPS是否可用
	 *
	 * @return
	 */
	private boolean checkGPSIsEnable() {
		checkInit();
		if (locationManager.isProviderEnabled(locationProvider)) {
			return true;
		} else {
//			Toast.makeText(context, "GPS不可用", Toast.LENGTH_SHORT).show();
			logger.error(ptt.terminalsdk.manager.gps.recoder.LocationManager.TAG+"GPS不可用");
//			TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetGPSLocationHandler.class, 0, 0);
			return false;
		}
	}

	/**
	 * 去掉监听事件
	 */
	public void removelocationListener() {
		if (!Util.isEmpty(locationProvider)) {
			if (locationManager != null && locationListener != null) {
				locationManager.removeUpdates(locationListener);
			}
		}
	}
}
