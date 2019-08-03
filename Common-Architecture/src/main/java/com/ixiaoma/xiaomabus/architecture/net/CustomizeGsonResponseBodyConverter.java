package com.ixiaoma.xiaomabus.architecture.net;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.ixiaoma.xiaomabus.architecture.bean.RequestResult;
import com.ixiaoma.xiaomabus.architecture.constant.ArchConstant;
import com.ixiaoma.xiaomabus.architecture.constant.ArchEventBusConstants;
import com.ixiaoma.xiaomabus.architecture.eventbus.MessageEvent;
import okhttp3.ResponseBody;
import org.simple.eventbus.EventBus;
import retrofit2.Converter;

import java.io.IOException;

public class CustomizeGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    CustomizeGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override public T convert(ResponseBody value) throws IOException {

//        try {
//            JsonReader jsonReader = gson.newJsonReader(value.charStream());
//            RequestResult result = gson.fromJson(jsonReader, RequestResult.class);
//            //登录过期
//            if (result != null && result.getMsg() != null && TextUtils.isEmpty(result.getMsg().getCode())) {
//                if (TextUtils.equals(result.getMsg().getCode(), ArchConstant.LOGIN_INVALID)) {//用户未登录或已过期
//                    EventBus.getDefault().post(new MessageEvent(ArchEventBusConstants.LOGIN_INVALID));
//                } else if (TextUtils.equals(result.getMsg().getCode(), ArchConstant.LOGIN_OTHER_DEVICE)) {//账号在其他设备登录
//                    EventBus.getDefault().post(new MessageEvent(ArchEventBusConstants.LOGIN_OTHER_DEVICE));
//                }
//            }
//        } catch (Exception e) {
//        }

        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        try {
            T read = adapter.read(jsonReader);
            try{
                RequestResult result = (RequestResult)read;
                //登录过期
                if (result != null && result.getMsg() != null && !TextUtils.isEmpty(result.getMsg().getCode())) {
                    if (TextUtils.equals(result.getMsg().getCode(), ArchConstant.LOGIN_INVALID)) {//用户未登录或已过期
                        EventBus.getDefault().post(new MessageEvent(ArchEventBusConstants.LOGIN_INVALID));
                    } else if (TextUtils.equals(result.getMsg().getCode(), ArchConstant.LOGIN_OTHER_DEVICE)) {//账号在其他设备登录
                        EventBus.getDefault().post(new MessageEvent(ArchEventBusConstants.LOGIN_OTHER_DEVICE));
                    }
                }
            }catch (Exception e){

            }

            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }
            return read;
        } finally {
            value.close();
        }
    }

}