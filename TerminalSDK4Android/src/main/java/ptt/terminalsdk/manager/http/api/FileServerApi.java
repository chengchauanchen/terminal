package ptt.terminalsdk.manager.http.api;

import java.util.Map;

import io.reactivex.Observable;
import ptt.terminalsdk.bean.DepData;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/8
 * 描述：文件服务
 * 修订历史：
 */
public interface FileServerApi{

    @GET("/file/getDeptData")
    Observable<DepData> getDeptData(@QueryMap() Map<String,String> map);

    @Headers("Content-type: application/json;charset=UTF-8")
    @POST("/file/getDeptList")
    Observable<String> getDeptList(@Body String params);

    @Headers("Content-type: application/json;charset=UTF-8")
    @POST("/file/terminal/findByDeptAndKeyList")
    Observable<String> findByDeptAndKeyList(@Body String params);

}
