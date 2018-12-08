package ptt.terminalsdk.manager.gps;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import org.apache.log4j.Logger;

import java.util.TimerTask;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGetBaiDuLocationHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.context.MyTerminalFactory;

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
        return lastLocation.getLongitude();
    }

    public double getBDLatitude(){
        if(lastLocation == null
                || (lastLocation.getLocType() != 61 && lastLocation.getLocType() != 161 && lastLocation.getLocType() != 66)
                || lastLocation.getLatitude() == 0.0
                || lastLocation.getLatitude() == 4.9E-324){
            return 0;
        }
        return lastLocation.getLatitude();
    }

    public String getAddress(){
        if(lastLocation == null || lastLocation.getAddrStr() == null){
            return "";
        }
        return lastLocation.getCity() + lastLocation.getDistrict() + lastLocation.getStreet() + lastLocation.getStreetNumber();
    }

    //    /**
//     * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
//     * 自动回调，相同的diagnosticType只会回调一次
//     *
//     * @param locType           当前定位类型
//     * @param diagnosticType    诊断类型（1~9）
//     * @param diagnosticMessage 具体的诊断信息释义
//     */
//    public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
//
//        if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_GPS) {
//
//            //建议打开GPS
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_BETTER_OPEN_WIFI) {
//
//            //建议打开wifi，不必连接，这样有助于提高网络定位精度！
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_LOC_PERMISSION) {
//
//            //定位权限受限，建议提示用户授予APP定位权限！
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CHECK_NET) {
//
//            //网络异常造成定位失败，建议用户确认网络状态是否异常！
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_CLOSE_FLYMODE) {
//
//            //手机飞行模式造成定位失败，建议用户关闭飞行模式后再重试定位！
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_INSERT_SIMCARD_OR_OPEN_WIFI) {
//
//            //无法获取任何定位依据，建议用户打开wifi或者插入sim卡重试！
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_NEED_OPEN_PHONE_LOC_SWITCH) {
//
//            //无法获取有效定位依据，建议用户打开手机设置里的定位开关后重试！
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_SERVER_FAIL) {
//
//            //百度定位服务端定位失败
//            //建议反馈location.getLocationID()和大体定位时间到loc-bugs@baidu.com
//
//        } else if (diagnosticType == LocationClient.LOC_DIAGNOSTIC_TYPE_FAIL_UNKNOWN) {
//
//            //无法获取有效定位依据，但无法确定具体原因
//            //建议检查是否有安全软件屏蔽相关定位权限
//            //或调用LocationClient.restart()重新启动后重试！
//
//        }
//    }
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

