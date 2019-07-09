package ptt.terminalsdk.manager.http;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/5
 * 描述：
 * 修订历史：
 */
public class AppUrlConfig{

    public static String FILE_SERVER_KEY = "fileServerKey";
    public static Map<String, String> allUrl = new HashMap<>();

    public static Map<String, String> getAllUrl() {
        return allUrl;
    }

    public static void setFileServerUrl(String fileServerUrl){
        allUrl.put(FILE_SERVER_KEY,fileServerUrl);
    }
}
