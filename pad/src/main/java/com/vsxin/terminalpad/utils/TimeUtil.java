package com.vsxin.terminalpad.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    public static Long getCurrentTime(){
        return System.currentTimeMillis();
    }

    public static String getCurrentTimeYMD(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }


    /**
     * 毫秒转成时分秒显示
     */
    public static String getTime(long t) {
        if (t < 60000) {
            long s = (t % 60000) / 1000;
            if (s < 10) {
                return "00:0" + s;
            }
            return "00:" + s;
        } else if ((t >= 60000) && (t < 3600000)) {
            return formatTime((t % 3600000) / 60000) + ":" + formatTime((t % 60000) / 1000);
        } else {
            return formatTime(t / 3600000) + ":" + formatTime((t % 3600000) / 60000) + ":" + formatTime((t % 60000) / 1000);
        }
    }

    private static String formatTime(long t) {
        String m = "";
        if (t > 0) {
            if (t < 10) {
                m = "0" + t;
            } else {
                m = t + "";
            }
        } else {
            m = "00";
        }
        return m;
    }

    /**
     * //获取日期格式器  yyyy-MM-dd HH:mm:ss
     * @param timeLong
     * @return
     */
    public static String formatLongToStr(Long timeLong){
        String format = "";
        try{
            Date date2 = new Date(timeLong);
            SimpleDateFormat dateFormat =   new SimpleDateFormat( " yyyy-MM-dd HH:mm:ss " );
            format = dateFormat.format(date2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return format;
    }
}
