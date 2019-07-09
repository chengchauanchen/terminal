package ptt.terminalsdk.manager.http.api;

import com.allen.library.RxHttpUtils;

import ptt.terminalsdk.manager.http.AppUrlConfig;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/8
 * 描述：
 * 修订历史：
 */
public class ApiManager{
    public static FileServerApi getFileServerApi(){
        return RxHttpUtils.createApi(AppUrlConfig.FILE_SERVER_KEY, AppUrlConfig.getAllUrl().get(AppUrlConfig.FILE_SERVER_KEY), FileServerApi.class);
    }
}
