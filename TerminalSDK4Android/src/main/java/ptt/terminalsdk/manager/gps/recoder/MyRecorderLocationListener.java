package ptt.terminalsdk.manager.gps.recoder;

import android.location.Location;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import org.apache.log4j.Logger;

import java.util.List;

import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.CoordTransformUtils;

/**
 * Created by zckj on 2017/8/28.
 */

public class MyRecorderLocationListener extends BDAbstractLocationListener {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onReceiveLocation(BDLocation location) {
        double longitude = getBDLongitude(location);
        double latitude = getBDLatitude(location);
        String floor = location.getFloor();
        logger.info(LocationManager.TAG + "BDGPSManager中onReceiveLocation-原-Longitude;" + location.getLongitude() + "--Latitude:" + location.getLatitude());
        logger.info(LocationManager.TAG + "BDGPSManager中onReceiveLocation-转-Longitude;" + longitude + "--Latitude:" + latitude);
        logger.info(LocationManager.TAG + "BDGPSManager中LocType=" + location.getLocType()
                + "-- AddrStr=" + location.getAddrStr()
                + "-- LocationDescribe=" + location.getLocationDescribe()
                + "-- LocTypeDescription=" + location.getLocTypeDescription()
                +"-- floor="+floor);


        //判断百度定位是否可用  可用就停止GPS的监听
        if (longitude != 0 && latitude != 0) {
            //停止GPS定位的监听
            MyTerminalFactory.getSDK().getRecorderGPSManager().removelocationListener();

            //到LocationManager中分发位置信息
            MyTerminalFactory.getSDK().getLocationManager().dispatchCommitLocation(bdTransformLocation(location,longitude,latitude));
        }
    }

    public double getBDLongitude(BDLocation location) {
        if (location == null
                || (location.getLocType() != 61 && location.getLocType() != 161 && location.getLocType() != 66)
                || location.getLongitude() == 0.0
                || location.getLongitude() == 4.9E-324) {
            return 0;
        }
        List<Double> doubles = CoordTransformUtils.bd2wgs(location.getLatitude(), location.getLongitude());
        return doubles.get(1);

    }

    public double getBDLatitude(BDLocation location) {
        if (location == null
                || (location.getLocType() != 61 && location.getLocType() != 161 && location.getLocType() != 66)
                || location.getLatitude() == 0.0
                || location.getLatitude() == 4.9E-324) {
            return 0;
        }
        List<Double> doubles = CoordTransformUtils.bd2wgs(location.getLatitude(), location.getLongitude());
        return doubles.get(0);

    }

    /**
     * 百度坐标系转换
     * @param bdLocation
     * @return
     */
    private Location bdTransformLocation(BDLocation bdLocation,double longitude,double latitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(bdLocation.getSpeed());
        location.setAltitude(bdLocation.getAltitude());
        location.setExtras(MyTerminalFactory.getSDK().getLocationManager().getAddressBundle(bdLocation.getAddrStr()));
        return location;
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
}

