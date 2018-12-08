package ptt.terminalsdk.tools;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean on 2017/5/15.
 */
public class CoordTransformUtils {

    private static Double PI = 3.14159265358979324;
    private static Double x_pi = 3.14159265358979324 * 3000.0 / 180.0;

    public static List<Double> delta(Double lat, Double lon) {
        List<Double> result = new ArrayList();
        // Krasovsky 1940
        //
        // a = 6378245.0, 1/f = 298.3
        // b = a * (1 - f)
        // ee = (a^2 - b^2) / a^2;
        Double a = 6378245.0; //  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
        Double ee = 0.00669342162296594323; //  ee: 椭球的偏心率。
        Double dLat = transformLat(lon - 105.0, lat - 35.0);
        Double dLon = transformLon(lon - 105.0, lat - 35.0);
        Double radLat = lat / 180.0 * PI;
        Double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        Double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        result.add(dLat);
        result.add(dLon);
        return result;
    }

    //WGS-84 to GCJ-02
    /**
     * WGS-84 to GCJ-02
     * @param wgsLat
     * @param wgsLon
     * @return
     */
    public static List<Double> gcj_encrypt(Double wgsLat, Double wgsLon) {
        List<Double> result = new ArrayList();
        if (outOfChina(wgsLat, wgsLon)) {
            result.add(wgsLat);
            result.add(wgsLon);
            return result;
        }

        List<Double> d = delta(wgsLat, wgsLon);
        result.add(wgsLat+d.get(0));
        result.add(wgsLon+d.get(1));
        return result;
    }


    //GCJ-02 to WGS-84
    /**
     * GCJ-02 to WGS-84
     * @param gcjLat
     * @param gcjLon
     * @return
     */
    public static List<Double>  gcj_decrypt(Double gcjLat, Double gcjLon) {
        List<Double> result = new ArrayList();
        if (outOfChina(gcjLat, gcjLon)) {
            result.add(gcjLat);
            result.add(gcjLon);
            return result;
        }

        List<Double> d = delta(gcjLat, gcjLon);
        result.add(gcjLat - d.get(0));
        result.add(gcjLon - d.get(1));
        return result;
    }



    //GCJ-02 to WGS-84 exactly

    /**
     * GCJ-02 to WGS-84 exactly
     * @param gcjLat
     * @param gcjLon
     * @return
     */
    public static List<Double> gcj_decrypt_exact(Double gcjLat, Double gcjLon) {
        List<Double> result = new ArrayList();
        Double initDelta = 0.01;
        Double threshold = 0.000000001;
        Double dLat = initDelta, dLon = initDelta;
        Double mLat = gcjLat - dLat, mLon = gcjLon - dLon;
        Double pLat = gcjLat + dLat, pLon = gcjLon + dLon;
        Double wgsLat, wgsLon, i = 0d;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLon = (mLon + pLon) / 2;
            List<Double> tmp = gcj_encrypt(wgsLat, wgsLon);
            dLat = tmp.get(0) - gcjLat;
            dLon = tmp.get(1) - gcjLon;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold))
                break;

            if (dLat > 0) pLat = wgsLat; else mLat = wgsLat;
            if (dLon > 0) pLon = wgsLon; else mLon = wgsLon;

            if (++i > 10000) break;
        }
        //console.log(i);
        result.add(wgsLat);
        result.add(wgsLon);
        return result;
    }


    //GCJ-02 to BD-09

    /**
     * GCJ-02 to BD-09
     * @param gcjLat
     * @param gcjLon
     * @return
     */
    public static List<Double> bd_encrypt(Double gcjLat, Double gcjLon) {
        List<Double> result = new ArrayList();
        Double x = gcjLon, y = gcjLat;
        Double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        Double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        Double bdLon = z * Math.cos(theta) + 0.0065;
        Double bdLat = z * Math.sin(theta) + 0.006;
        result.add(bdLat);
        result.add(bdLon);
        return result;
    }
    

    //BD-09 to GCJ-02

    /**
     * BD-09 to GCJ-02
     * @param bdLat
     * @param bdLon
     * @return
     */
    public static List<Double> bd_decrypt(Double bdLat, Double bdLon) {
        List<Double> result = new ArrayList();
        Double x = bdLon - 0.0065, y = bdLat - 0.006;
        Double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        Double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        Double gcjLon = z * Math.cos(theta);
        Double gcjLat = z * Math.sin(theta);
        result.add(gcjLat);
        result.add(gcjLon);
        return result;
    }


    //WGS-84 to Web mercator
    //mercatorLat -> y mercatorLon -> x

    /**
     * WGS-84 to Web mercator
     * mercatorLat -> y mercatorLon -> x
     * @param wgsLat
     * @param wgsLon
     * @return
     */
    public static List<Double> mercator_encrypt(Double wgsLat, Double wgsLon) {
        List<Double> result = new ArrayList();
        Double x = wgsLon * 20037508.34 / 180.;
        Double y = Math.log(Math.tan((90. + wgsLat) * PI / 360.)) / (PI / 180.);
        y = y * 20037508.34 / 180.;
        result.add(y);
        result.add(x);
        return result;
        /*
         if ((Math.abs(wgsLon) > 180 || Math.abs(wgsLat) > 90))
         return null;
         Double x = 6378137.0 * wgsLon * 0.017453292519943295;
         Double a = wgsLat * 0.017453292519943295;
         Double y = 3189068.5 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
         return {'lat' : y, 'lon' : x};
         //*/
    }


    // Web mercator to WGS-84
    // mercatorLat -> y mercatorLon -> x

    /**
     * Web mercator to WGS-84
     * mercatorLat -> y mercatorLon -> x
     * @param mercatorLat
     * @param mercatorLon
     * @return
     */
    public static List<Double> mercator_decrypt(Double mercatorLat, Double mercatorLon) {
        List<Double> result = new ArrayList();
        Double x = mercatorLon / 20037508.34 * 180.;
        Double y = mercatorLat / 20037508.34 * 180.;
        y = 180 / PI * (2 * Math.atan(Math.exp(y * PI / 180.)) - PI / 2);
        result.add(y);
        result.add(x);
        return result;
        /*
         if (Math.abs(mercatorLon) < 180 && Math.abs(mercatorLat) < 90)
         return null;
         if ((Math.abs(mercatorLon) > 20037508.3427892) || (Math.abs(mercatorLat) > 20037508.3427892))
         return null;
         Double a = mercatorLon / 6378137.0 * 57.295779513082323;
         Double x = a - (Math.floor(((a + 180.0) / 360.0)) * 360.0);
         Double y = (1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * mercatorLat) / 6378137.0)))) * 57.295779513082323;
         return {'lat' : y, 'lon' : x};
         //*/
    }

    //BD-09 to WGS-84

    /**
     * BD-09 to WGS-84
     * @param bdLat
     * @param bdLon
     * @return
     */
    public static List<Double> bd2wgs(Double bdLat,Double bdLon){
        List<Double> result = bd_decrypt(bdLat,bdLon);
        return gcj_decrypt(result.get(0),result.get(1));
    }

    //WGS-84 to BD-09

    /**
     * WGS-84 to BD-09
     * @param wgsLat
     * @param wgsLon
     * @return
     */
    public static List<Double> wgs2bd(Double wgsLat,Double wgsLon){
        List<Double> result = gcj_encrypt(wgsLat,wgsLon);
        return bd_encrypt(result.get(0),result.get(1));
    }

    //BD-09 to Web mercator

    /**
     * BD-09 to Web mercator
     * @param bdLat
     * @param bdLon
     * @return
     */
    public static List<Double> bd2mercator(Double bdLat,Double bdLon){
        List<Double> result = bd2wgs(bdLat,bdLon);
        return mercator_encrypt(result.get(0),result.get(1));
    }

    //Web mercator to BD-09

    /**
     * Web mercator to BD-09
     * @param mercatorLat
     * @param mercatorLon
     * @return
     */
    public static List<Double> mercator2bd(Double mercatorLat,Double mercatorLon){
        List<Double> result = mercator_decrypt(mercatorLat,mercatorLon);
        return wgs2bd(result.get(0),result.get(1));
    }


    // two point's distance

    /**
     * two point's distance
     *                  两点之间距离
     * @param latA
     * @param lonA
     * @param latB
     * @param lonB
     * @return
     */
    public static Double distance(Double latA, Double lonA, Double latB, Double lonB) {
        Double earthR = 6371000.;
        Double x = Math.cos(latA * PI / 180.) * Math.cos(latB * PI / 180.) * Math.cos((lonA - lonB) * PI / 180);
        Double y = Math.sin(latA * PI / 180.) * Math.sin(latB * PI / 180.);
        Double s = x + y;
        if (s > 1) s = 1d;
        if (s < -1) s = -1d;
        Double alpha = Math.acos(s);
        Double distance = alpha * earthR;
        return distance;
    }

    /**
     *
     * @param lat
     * @param lon
     * @return
     */
    public static boolean outOfChina(Double lat,Double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }


    /**
     *
     * @param x
     * @param y
     * @return
     */
    public static Double transformLat (Double x, Double y) {
        Double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public static Double transformLon (Double x, Double y) {
        Double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }
    
    public static void main(String[] args){
        List<Double> doubles = gcj_decrypt_exact(40.03277371, 116.30710534);

        System.out.println(doubles);
    }
}
