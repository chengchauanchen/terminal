package com.ixiaoma.xiaomabus.architecture.net;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public  abstract class BaseAppClient {
    private Retrofit retrofit;

    protected abstract String getBaseUrl();


    public BaseAppClient() {
        create();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    private Retrofit create() {
        if (null == retrofit) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .addConverterFactory(CustomizeGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(genericClient())
                    .build();
        }
        return retrofit;
    }

    /**
     * 设置头
     *
     * @return
     */
    private static OkHttpClient genericClient() {
//        HttpLoggingInterceptor.Level body = DeBug.debug? HttpLoggingInterceptor.Level.BODY:HttpLoggingInterceptor.Level.NONE;
        //动态切换BaseUrl
        OkHttpClient httpClient = new OkHttpClient.Builder()
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        Request request = chain.request();
//                        Request.Builder builder = request.newBuilder();
//                        builder.addHeader("token", getToken());
//                        builder.addHeader("Content-Type", "application/json;charset=UTF-8");
//                        Request build = builder.build();
//                        return chain.proceed(build);
//                    }
//                })
                //谷歌浏览器调试网络请求
//                .addNetworkInterceptor(new StethoInterceptor())
                //日志打印
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        return httpClient;
    }

}
