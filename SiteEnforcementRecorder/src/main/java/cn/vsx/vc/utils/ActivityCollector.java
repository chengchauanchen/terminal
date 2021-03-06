package cn.vsx.vc.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zckj on 2017/7/28.
 */

public class ActivityCollector {
    static Logger logger = Logger.getLogger(ActivityCollector.class);
    /**
     * 存放activity的列表
     */
    public static HashMap<Class<?>, Activity> activities = new LinkedHashMap<>();

    /**
     * 添加Activity
     *
     * @param activity
     */
    public static void addActivity(Activity activity, Class<?> clz) {
        logger.info("addActivity----->activity = "+activity);
        activities.put(clz, activity);
        logger.info("addActivity----->activities = "+activities);
    }

    public static Map<Class<?>, Activity> getAllActivity(){
        logger.info("getAllActivity()------->"+activities);
        return activities;
    }
    /**
     * 判断一个Activity 是否存在
     *
     * @param clz
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static <T extends Activity> boolean isActivityExist(Class<T> clz) {
        boolean res;
        Activity activity = getActivity(clz);
        logger.info("getActivity----->activity = "+activity);
        if (activity == null) {
            res = false;
        } else {
            if (activity.isFinishing() || activity.isDestroyed()) {
                res = false;
            } else {
                res = true;
            }
        }
        logger.info("getActivity----->res = "+res);
        return res;
    }

    /**
     * 获得指定activity实例
     *
     * @param clazz Activity 的类对象
     * @return
     */
    public static <T extends Activity> T getActivity(Class<T> clazz) {
        return (T) activities.get(clazz);
    }

    /**
     * 移除activity,代替finish
     *
     * @param activity
     */
    public static void removeActivity(Activity activity) {
        logger.info("removeActivity----->activity = "+activity);
        if (activities.containsKey(activity.getClass())) {
            activities.remove(activity.getClass());
        }
        logger.info("removeActivity----->activities = "+activities);
    }

    /**
     * 移除所有的Activity
     */
    public static void removeAllActivity() {
        if (activities != null && activities.size() > 0) {
            Set<Map.Entry<Class<?>, Activity>> sets = activities.entrySet();
            for (Map.Entry<Class<?>, Activity> s : sets) {
                if (!s.getValue().isFinishing()) {
                    s.getValue().finish();
                }
            }
        }
        activities.clear();
    }
}
