package cn.vsx.vc.key;

import android.util.Log;

import org.apache.log4j.Logger;

import cn.vsx.vc.utils.DeviceUtil;

/**
 * 实体按键的Util
 */
public class KeyUtil {


    public static Logger logger = Logger.getLogger(KeyUtil.class);
    public static final String TAG = "KeyUtil---";

    private static BaseKey key;

    /**
     * 根据设备类型获取对应的实体按键的监听
     * @param type
     * @return
     */
    public static BaseKey getKeyByType(String type){
        logger.info(TAG+"getKeyByType:"+type);
        switch (type){
            case DeviceUtil.TYPE_BITSTART_I7:
                if(key == null){
                    key = new BitStartI7Key();
                }
                break;
            case DeviceUtil.TYPE_CL310A:
                if(key == null){
                    key = new CL310AKey();
                }
                break;
            case DeviceUtil.TYPE_H40:
                if(key == null){
                    key = new H40Key();
                }
                break;
            case DeviceUtil.TYPE_TB8766P1_BSP_1G:
                if(key == null){
                    key = new TB8766P1_BSP_1Gkey();
                }
                break;
            default:
                if(key == null){
                    key = new DefaultKey();
                }
                break;
        }
        return key;
    }
}
