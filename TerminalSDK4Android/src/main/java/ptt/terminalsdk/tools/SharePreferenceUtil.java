package ptt.terminalsdk.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类
 * @author cloud
 * 2016.10.23
 */
public class SharePreferenceUtil{
	/**统一名称*/
	public static String SP_NAME;
	public static final String SP_NAME_ONE = "bleData_one";
	public static final String SP_NAME_TWO = "bleData_two";
	public static final String SP_NAME_THREE = "bleData_three";
	public static final String SP_NAME_FOUR = "bleData_four";
	public static final String SP_NAME_FIVE = "bleData_five";


	/**
	 * 获取SharedPreferences的String值
	 * @param context 上下文环境
	 * @param spName 键
	 * @param key 值
	 * @return
	 */
	public static String getSpStringValue(Context context, String spName, String key){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getString(key, null);
	}

	/**
	 * 获取SharedPreferences的String值
	 * @param context 上下文环境
	 * @param spName 键
	 * @param defalut 默认值
	 * @return
	 */
	public static String getSpStringValue(Context context, String spName, String key, String defalut){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getString(key, defalut);
	}
	
	/**
	 * SharedPreferences保存String值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @param value 值
	 */
	public static void putSpStringValue(Context context, String spName, String key, String value){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		sp.edit().putString(key, value).apply();
	}
	
	/**
	 * 获取SharedPreferences的Boolean值
	 * @param context 上下文环境
	 * @param spName  文件名
	 * @param key	键
	 * @param flag  默认值
	 * @return
	 */
	public static Boolean getSpBooleanValue(Context context, String spName, String key, boolean flag){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getBoolean(key, flag);
	}
	
	/**
	 * SharedPreferences保存Boolean值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @param value 值
	 */
	public static void putSpBooleanValue(Context context, String spName, String key, Boolean value){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, value).apply();
	}

	
	/**
	 * 获取SharedPreferences的int值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @return
	 */
	public static int getSpIntValue(Context context, String spName, String key){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getInt(key, 0);
	}
	/**
	 * 获取SharedPreferences的int值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @param def  默认值
	 * @return
	 */
	public static int getSpIntValue(Context context, String spName, String key, int def){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getInt(key, def);
	}
	
	/**
	 * SharedPreferences保存int值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @param value 值
	 */
	public static void putSpIntValue(Context context, String spName, String key, int value){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		sp.edit().putInt(key, value).apply();
	}
	
	/**
	 * 获取SharedPreferences的long值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @return
	 */
	public static long getSpLongValue(Context context, String spName, String key,long defaultValue){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getLong(key, defaultValue);
	}
	
	/**
	 * SharedPreferences保存Long值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @param value 值
	 */
	public static void putSpLongValue(Context context, String spName, String key, Long value){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		sp.edit().putLong(key, value).apply();
	}
	
	/**
	 * 获取SharedPreferences的float值
	 * @param context  上下文环境
	 * @param spName 文件名
	 * @param key 键
	 * @return
	 */
	public static float getSpFloatValue(Context context, String spName, String key,float defaultValue){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.getFloat(key, defaultValue);
	}
	
	/**
	 * SharedPreferences保存float值
	 * @param context 上下文环境
	 * @param spName 文件名
	 * @param key   键
	 * @param value 值
	 */
	public static void putSpFloatValue(Context context, String spName, String key, Float value){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		sp.edit().putFloat(key, value).apply();
	}


	/**
	 * 清除某一个SharedPreferences
	 * @param context 上下文环境
	 * @param spName 文件名
	 */
	public static void clearSp(Context context, String spName){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		sp.edit().clear().apply();
	}

	public static boolean containParam(Context context,String spName,String param){
		SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
		return sp.contains(param);
	}
}
