package ptt.terminalsdk.manager.http;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/7/31
 * 描述：
 * 修订历史：
 */
public class LoggingInterceptor implements Interceptor{
    private final Charset UTF8 = Charset.forName("UTF-8");
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public Response intercept(Chain chain) throws IOException{

        Request request = chain.request();
        RequestBody requestBody = request.body();

        String body = null;

        if(requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);

            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            body = buffer.readString(charset);
        }

        logger.info(String.format("发送请求\nmethod：%s\nurl：%s\nheaders: %sbody：%s",
                request.method(), request.url(), request.headers(), body));

        long startNs = System.nanoTime();
        Response response = chain.proceed(request);
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        String rBody = null;


         BufferedSource source = responseBody.source();
         source.request(Long.MAX_VALUE); // Buffer the entire body.
         Buffer buffer = source.buffer();

         Charset charset = UTF8;
         MediaType contentType = responseBody.contentType();
         if (contentType != null) {
             try {
                 charset = contentType.charset(UTF8);
             } catch (UnsupportedCharsetException e) {
                 e.printStackTrace();
             }
         }
         rBody = buffer.clone().readString(charset);


        logger.info(String.format("收到响应 %s%s %ss\n请求url：%s\n请求body：%s\n响应body：%s",
                response.code(), response.message(), tookMs, response.request().url(), body, rBody));

        return response;
    }
}
