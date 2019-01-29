package ptt.terminalsdk.manager.gps;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.TimerTask;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetBaiDuLocationHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.CoordTransformUtils;

/**
 * Created by zckj on 2017/8/28.
 */

public class MyLocationListener extends BDAbstractLocationListener {

    private Logger logger = Logger.getLogger(getClass());
    private BDLocation bdLocation;

    @Override
    public void onReceiveLocation(BDLocation location) {
        bdLocation = location;
        lastLocation = location;
        logger.info("百度地图onReceiveLocation---->"+getBDLatitude());
        logger.info("百度地图中获取到的位置信息:LocType=" + location.getLocType()
                +"\n AddrStr="+lastLocation.getAddrStr()
                +"\n LocationDescribe="+location.getLocationDescribe()
                +"\n LocTypeDescription="+location.getLocTypeDescription());


        if (getBDLatitude() != 0) {
            if(wait4ResoltTask != null){
                wait4ResoltTask.cancel();
                wait4ResoltTask = null;
            }
            recordLocation();
            //发送到界面
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveGetBaiDuLocationHandler.class, getBDLongitude(), location.getLatitude());
        }
    }

    public double getBDLongitude(){
        if(lastLocation == null
                || (lastLocation.getLocType() != 61 && lastLocation.getLocType() != 161 && lastLocation.getLocType() != 66)
                || lastLocation.getLongitude() == 0.0
                || lastLocation.getLongitude() == 4.9E-324){
            return 0;
        }
        List<Double> doubles = CoordTransformUtils.bd2wgs(lastLocation.getLatitude(), lastLocation.getLongitude());
        return doubles.get(1);
    }

    public double getBDLatitude(){
        if(lastLocation == null
                || (lastLocation.getLocType() != 61 && lastLocation.getLocType() != 161 && lastLocation.getLocType() != 66)
                || lastLocation.getLatitude() == 0.0
                || lastLocation.getLatitude() == 4.9E-324){
            return 0;
        }
        List<Double> doubles = CoordTransformUtils.bd2wgs(lastLocation.getLatitude(), lastLocation.getLongitude());
        return doubles.get(0);
    }

    public String getAddress(){
        if(lastLocation == null || lastLocation.getAddrStr() == null){
            return "";
        }
        return lastLocation.getCity() + lastLocation.getDistrict() + lastLocation.getStreet() + lastLocation.getStreetNumber();
    }

    private TimerTask wait4ResoltTask;
    private BDLocation lastLocation;
    public void baiduUpdate(){
        int i = MyTerminalFactory.getSDK().getBDGPSManager().mLocationClient.requestLocation();
        logger.info("百度地图请求位置返回值----->"+i);

        lastLocation = bdLocation;
        if(lastLocation != null){
            logger.info("使用百度定位，缓存上个坐标：纬度" + lastLocation.getLatitude() + "经度" + lastLocation.getLongitude());
            bdLocation = null;
        }

        //3秒后，调用GPSManager定位
        if(wait4ResoltTask != null){
            wait4ResoltTask.cancel();
            wait4ResoltTask = null;
        }
        wait4ResoltTask = new TimerTask() {
            @Override
            public void run() {
                MyTerminalFactory.getSDK().getGpsManager().setLocationUpdate(true);
            }
        };
        TerminalFactory.getSDK().getTimer().schedule(wait4ResoltTask, 3000);
    }

    public void recordLocation(){
        if(getBDLatitude() != 0 && getBDLongitude() != 0 ){
            TerminalFactory.getSDK().putParam(Params.CLOCK_LAT, getBDLatitude());
            TerminalFactory.getSDK().putParam(Params.CLOCK_LONG , getBDLongitude());
        }
    }

}

