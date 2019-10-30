package ptt.terminalsdk.tools;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cloud on 2016/10/11.
 */

public class StringUtil{

    public static final String NUMBER_REG_EXPRESSION = "^[0-9]*$";
    /**
     * 检查字符串是否有效
     *
     * @param str   true有效 false无效
     * @return
     */
    public static boolean checkStringIsValid(String str) {
        return str != null && str.length() > 0 && !str.equalsIgnoreCase("null");
    }

    /**
     * 判断字符串是否为数字
     * @param str
     * @return
     */
    public static boolean isNumber(String str){
        Pattern p = Pattern.compile(NUMBER_REG_EXPRESSION);
        Matcher m = p.matcher(str);
        return !TextUtils.isEmpty(str) && m.find();
    }

    /**
     * 时间格式化
     * @param time
     * @return
     */
    public static String formatTime(String time) {    //判断时间  如果时间时和分钟都不大于9 那么在前面补上0站位.
        if(!time.contains(":")){
            return "";
        }
        String[]start_hous_min = time.split(":");
        if(!(isNumber(start_hous_min[0])&&isNumber(start_hous_min[0]))){
            return "";
        }
        int wv_hours = Integer.valueOf(start_hous_min[0]);
        int wv_mins = Integer.valueOf(start_hous_min[1]);
        StringBuilder sb = new StringBuilder();
        if (wv_hours <=9) {
            sb.append("0")
                    .append(wv_hours)
                    .append(":");
            if (wv_mins<=9){
                sb.append("0").append(wv_mins);
            }else {
                sb.append(wv_mins);
            }
        }else {
            sb.append(wv_hours)
                    .append(":");
            if (wv_mins<=9){
                sb.append("0").append(wv_mins);
            }else {
                sb.append(wv_mins);
            }
        }
        return sb.toString();
    }



    /**
     * 判断是否是Emoji
     *
     * @param source 比较的单个字符
     * @return
     */
    public static boolean isEmoji(CharSequence source) {
        Pattern p = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(source);
        return m.find();
    }

    public static boolean isValidTempGroupName(CharSequence source){
        Pattern p = Pattern.compile("^[a-zA-Z0-9_\\-\u4e00-\u9fa5]+$",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(source);
        return m.find();
    }

    /**
     * 将paramValue中的汉字提取出来
     * @param paramValue
     * @return
     */
    public static String getChinese(String paramValue) {
        String str = "";
        String regex = "([\u4e00-\u9fa5]+)";
        Matcher matcher = Pattern.compile(regex).matcher(paramValue);
        while (matcher.find()) {
            str += matcher.group(0);
        }
        return str;
    }

    /**
     * String转float
     * @param data
     * @return
     */
    public static float toFloat(String data){
        if(!TextUtils.isEmpty(data)){
            float result = 0f;
            try{
                result = Float.valueOf(data);
            }catch (Exception e){
            }finally {
                return result;
            }
        }
        return 0f;
    }

    /**
     * String转Long
     * @param data
     * @return
     */
    public static long toLong(String data){
        long result = 0L;
        if(!TextUtils.isEmpty(data)){
            try{
                result = Long.valueOf(data);
            }catch (Exception e){
                e.printStackTrace();
                result = 0L;
            }
        }
        return result;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stringToDate(Long time){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date(time);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * String 转int
     * @return
     */
    public static  int  stringToInt(String string){
        int data = 0;
        if(!TextUtils.isEmpty(string)){
            try{
                data = Integer.valueOf(string);
            }catch (Exception e){
                data =  0 ;
            }
        }
        return data;
    }

    /**
     * 小时转秒
     * @return
     */
    public static  long  hourToSeconds(int time){
        return time*3600;
    }
    /**
     * 秒转小时
     * @return
     */
    public static  long  secondsToHour(long time){
        return time/3600;
    }

    /**
     * 秒转分钟
     * @return
     */
    public static  long  secondsToMinute(long time){
        return time/60;
    }

}
