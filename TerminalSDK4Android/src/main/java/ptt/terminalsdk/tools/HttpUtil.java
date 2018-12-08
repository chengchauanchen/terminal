package ptt.terminalsdk.tools;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ysl on 2017/6/9.
 */

public class HttpUtil{
    private static final OkHttpClient client = new OkHttpClient();
    public static FutureTask<JsonObject> syncGet(final String url) {
        return new FutureTask<>(new Callable<JsonObject>() {
            @Override
            public JsonObject call() throws Exception {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                return parseBody(client.newCall(request).execute());
            }
        });
    }

    public static JsonObject parseBody(Response response) throws IOException {
        if (response.isSuccessful()) {
            String resp = response.body().string();
            Log.i("", String.format("%s %s result:\n%s", response.request().method(),response.request().url(),resp));
            JsonObject o = new JsonParser().parse(resp).getAsJsonObject();
            JsonObject EasyDarwin = o.getAsJsonObject("EasyDarwin");
            JsonObject header = EasyDarwin.getAsJsonObject("Header");
            final int code = header.getAsJsonPrimitive("ErrorNum").getAsInt();
            final String error = header.getAsJsonPrimitive("ErrorString").getAsString();
            if (code == 200) {
                return EasyDarwin.getAsJsonObject("Body");
            }
        }
        return null;
    }

    //将图片写入文件
    public static String saveFileByBitmap(String path,String fileName, Bitmap content){
        try {
            //方法一：
            File file = new File(path+fileName);
            if(!file.exists()){
                File file1 = new File(file.getParent());
                file1.mkdirs();

                file.createNewFile();
            }

            FileOutputStream out = new FileOutputStream(file);
            content.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return path+fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
