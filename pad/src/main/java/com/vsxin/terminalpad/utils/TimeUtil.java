package com.vsxin.terminalpad.utils;

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
}
