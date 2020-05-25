package ptt.terminalsdk.manager.gps.recoder;

import android.location.Location;

import com.sfmap.api.location.SfMapLocation;
import com.sfmap.api.location.SfMapLocationListener;

import org.apache.log4j.Logger;

import ptt.terminalsdk.bean.LocationType;
import ptt.terminalsdk.context.MyTerminalFactory;

/**
 * 顺丰定位监听
 */
public class MySfMapLocationListener implements SfMapLocationListener {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onLocationChanged(SfMapLocation sfMapLocation) {
        if(sfMapLocation==null){
            logger.info(LocationManager.TAG + "顺丰GPSManager定位失败=null");
            MyTerminalFactory.getSDK().getRecorderSfGPSManager().removelocationListener();
            MyTerminalFactory.getSDK().getLocationManager().locationFail(LocationType.SF);
            return;
        }

        double longitude = sfMapLocation.getLongitude();
        double latitude = sfMapLocation.getLatitude();

        logger.info(LocationManager.TAG + "顺丰GPSManager中onReceiveLocation-原-Longitude;" + longitude + "--Latitude:" + latitude+ "-- AddrStr=" + sfMapLocation.getAddress());

        //ToastUtil.showToast("顺丰定位：Longitude--"+longitude+"--Latitude:"+ latitude);

        //判断百度定位是否可用  可用就停止GPS的监听
        if (longitude != 0 && latitude != 0) {
            //停止GPS定位的监听
            MyTerminalFactory.getSDK().getRecorderGPSManager().removelocationListener();
            //停止百度定位的监听
            MyTerminalFactory.getSDK().getRecorderBDGPSManager().removelocationListener();
            //到LocationManager中分发位置信息
            MyTerminalFactory.getSDK().getLocationManager().dispatchCommitLocation(bdTransformLocation(sfMapLocation,longitude,latitude));
        }else{
            MyTerminalFactory.getSDK().getRecorderSfGPSManager().removelocationListener();
            MyTerminalFactory.getSDK().getLocationManager().locationFail(LocationType.SF);
        }
    }

    /**
     * 顺丰坐标系转换
     * @param sfLocation
     * @return
     */
    private Location bdTransformLocation(SfMapLocation sfLocation, double longitude, double latitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(sfLocation.getSpeed());
        location.setAltitude(sfLocation.getAltitude());
        location.setExtras(MyTerminalFactory.getSDK().getLocationManager().getAddressBundle(sfLocation.getAddress()));
        return location;
    }


}
