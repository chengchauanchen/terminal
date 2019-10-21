package com.zectec.imageandfileselector.utils;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *  时间Util
 * Created by gt358 on 2017/9/11.
 */

public class DateUtils {

//    private static boolean isSameDay(long var0) {
//        TimeInfo var2 = getTodayStartAndEndTime();
//        return var0 > var2.getStartTime() && var0 < var2.getEndTime();
//    }
//
//    private static boolean isYesterday(long var0) {
//        TimeInfo var2 = getYesterdayStartAndEndTime();
//        return var0 > var2.getStartTime() && var0 < var2.getEndTime();
//    }
//
//    public static String getTimestampString(Date var0) {
//        String var1 = null;
//        String var2 = Locale.getDefault().getLanguage();
//        boolean var3 = var2.startsWith("zh");
//        long var4 = var0.getTime();
//        if(isSameDay(var4)) {
//            if(var3) {
//                var1 = "aa hh:mm";
//            } else {
//                var1 = "hh:mm aa";
//            }
//        } else if(isYesterday(var4)) {
//            if(!var3) {
//                return "Yesterday " + (new SimpleDateFormat("hh:mm aa", Locale.ENGLISH)).format(var0);
//            }
//
//            var1 = "昨天aa hh:mm";
//        } else if(var3) {
//            var1 = "M月d日aa hh:mm";
//        } else {
//            var1 = "MMM dd hh:mm aa";
//        }
//
//        return var3?(new SimpleDateFormat(var1, Locale.CHINESE)).format(var0):(new SimpleDateFormat(var1, Locale.ENGLISH)).format(var0);
//    }
//
//    public static TimeInfo getYesterdayStartAndEndTime() {
//        Calendar var0 = Calendar.getInstance();
//        var0.add(Calendar.DAY_OF_MONTH, -1);//5
//        var0.set(Calendar.HOUR_OF_DAY, 0);//11
//        var0.set(Calendar.MINUTE, 0);//12
//        var0.set(Calendar.SECOND, 0);//13
//        var0.set(Calendar.MILLISECOND, 0);//Calendar.MILLISECOND
//        Date var1 = var0.getTime();
//        long var2 = var1.getTime();
//        Calendar var4 = Calendar.getInstance();
//        var4.add(Calendar.DAY_OF_MONTH, -1);//5
//        var4.set(Calendar.HOUR_OF_DAY, 23);//11
//        var4.set(Calendar.MINUTE, 59);//12
//        var4.set(Calendar.SECOND, 59);//13
//        var4.set(Calendar.MILLISECOND, 999);//Calendar.MILLISECOND
//        Date var5 = var4.getTime();
//        long var6 = var5.getTime();
//        TimeInfo var8 = new TimeInfo();
//        var8.setStartTime(var2);
//        var8.setEndTime(var6);
//        return var8;
//    }
//
//    public static TimeInfo getTodayStartAndEndTime() {
//        Calendar var0 = Calendar.getInstance();
//        var0.set(Calendar.HOUR_OF_DAY, 0);
//        var0.set(Calendar.MINUTE, 0);
//        var0.set(Calendar.SECOND, 0);
//        var0.set(Calendar.MILLISECOND, 0);
//        Date var1 = var0.getTime();
//        long var2 = var1.getTime();
//        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");
//        Calendar var5 = Calendar.getInstance();
//        var5.set(Calendar.HOUR_OF_DAY, 23);
//        var5.set(Calendar.MINUTE, 59);
//        var5.set(Calendar.SECOND, 59);
//        var5.set(Calendar.MILLISECOND, 999);
//        Date var6 = var5.getTime();
//        long var7 = var6.getTime();
//        TimeInfo var9 = new TimeInfo();
//        var9.setStartTime(var2);
//        var9.setEndTime(var7);
//        return var9;
//    }

    public static boolean isCloseEnough(long var0, long var2) {
        long var4 = var0 - var2;
        if(var4 < 0L) {
            var4 = -var4;
        }

        return var4 < 30000L;
    }

    public static class TimeInfo {
        private long startTime;
        private long endTime;

        public TimeInfo() {
        }

        public long getStartTime() {
            return this.startTime;
        }

        public void setStartTime(long var1) {
            this.startTime = var1;
        }

        public long getEndTime() {
            return this.endTime;
        }

        public void setEndTime(long var1) {
            this.endTime = var1;
        }
    }

    /**
     *  时间戳格式转换
     */
    static String dayNames[] = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    public static String getNewChatTime(long timesamp) {
        String result = "";
        Calendar todayCalendar = Calendar.getInstance();
        Calendar otherCalendar = Calendar.getInstance();
        otherCalendar.setTimeInMillis(timesamp);

        String timeFormat="M月d日 HH:mm";
        String yearTimeFormat="yyyy年M月d日 ";
        String am_pm="";
        int hour=otherCalendar.get(Calendar.HOUR_OF_DAY);
//        if(hour>=0&&hour<6){
//            am_pm="凌晨";
//        }else if(hour>=6&&hour<12){
//            am_pm="早上";
//        }else if(hour==12){
//            am_pm="中午";
//        }else if(hour>12&&hour<18){
//            am_pm="下午";
//        }else if(hour>=18){
//            am_pm="晚上";
//        }
        timeFormat="M月d日 "+ am_pm +"HH:mm";
        yearTimeFormat="yyyy年M月d日 "/*+ am_pm +"HH:mm"*/;

        boolean yearTemp = todayCalendar.get(Calendar.YEAR)==otherCalendar.get(Calendar.YEAR);
        if(yearTemp){
            int todayMonth=todayCalendar.get(Calendar.MONTH);
            int otherMonth=otherCalendar.get(Calendar.MONTH);
            if(todayMonth==otherMonth){//表示是同一个月
                int temp=todayCalendar.get(Calendar.DATE)-otherCalendar.get(Calendar.DATE);
                switch (temp) {
                    case 0:
                        result = getHourAndMin(timesamp);
                        break;
                    case 1:
                        result = "昨天 " + getHourAndMin(timesamp);
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        int dayOfMonth = otherCalendar.get(Calendar.WEEK_OF_MONTH);
                        int todayOfMonth=todayCalendar.get(Calendar.WEEK_OF_MONTH);
                        if(dayOfMonth==todayOfMonth){//表示是同一周
                            int dayOfWeek=otherCalendar.get(Calendar.DAY_OF_WEEK);
                            if(dayOfWeek!=1){//判断当前是不是星期日     如想显示为：周日 12:09 可去掉此判断
//                                result = dayNames[otherCalendar.get(Calendar.DAY_OF_WEEK)-1] + getHourAndMin(timesamp);
                                result=getYearTime(timesamp,yearTimeFormat);
                            }else{
//                                result = getTime(timesamp,timeFormat);
                                result=getYearTime(timesamp,yearTimeFormat);
                            }
                        }else{
//                            result = getTime(timesamp,timeFormat);
                            result=getYearTime(timesamp,yearTimeFormat);
                        }
                        break;
                    default:
//                        result = getTime(timesamp,timeFormat);
                        result=getYearTime(timesamp,yearTimeFormat);
                        break;
                }
            }else{
//                result = getTime(timesamp,timeFormat);
                result=getYearTime(timesamp,yearTimeFormat);
            }
        }else{
            result=getYearTime(timesamp,yearTimeFormat);
        }
        return result;
    }

    /**
     * 当天的显示时间格式
     * @param time
     * @return
     */
    public static String getHourAndMin(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    /**
     * 不同一周的显示时间格式
     * @param time
     * @param timeFormat
     * @return
     */
    public static String getTime(long time,String timeFormat) {
        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
        return format.format(new Date(time));
    }

    /**
     * 不同年的显示时间格式
     * @param time
     * @param yearTimeFormat
     * @return
     */
    public static String getYearTime(long time,String yearTimeFormat) {
        SimpleDateFormat format = new SimpleDateFormat(yearTimeFormat);
        return format.format(new Date(time));
    }

    public static String formatDateTime(long milliseconds) {
        StringBuilder sb = new StringBuilder();
        long mss = milliseconds / 1000;
        long days = mss / (60 * 60 * 24);
        long hours = (mss % (60 * 60 * 24)) / (60 * 60);
        long minutes = (mss % (60 * 60)) / 60;
        long seconds = mss % 60;
        DecimalFormat format = new DecimalFormat("00");
        Log.d("Time", "--days:"+days+"--hours:"+hours+"--minutes:"+minutes+"--seconds:"+seconds);
        if (days > 0 || hours > 0) {
            sb.append(format.format(hours)).append(":").append(format.format(minutes)).append(":").append(format.format(seconds));
        }else {
            sb.append(format.format(minutes)).append(":").append(format.format(seconds));
        }

        Log.d("Time", "--data:"+sb.toString());
        return sb.toString();
    }
}
