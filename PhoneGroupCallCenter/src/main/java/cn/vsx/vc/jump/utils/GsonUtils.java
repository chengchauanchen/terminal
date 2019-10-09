package cn.vsx.vc.jump.utils;

import android.util.Log;

import com.google.gson.Gson;

import cn.vsx.vc.jump.bean.SendBean;

public class GsonUtils {

    /**
     * 将第三方发送过来的json 转化为 SendBean对象
     * @param sendJson
     * @return
     */
    public static SendBean sendJsonToBean(String sendJson){
        SendBean sendBean ;
        try{
            sendBean = new Gson().fromJson(sendJson, SendBean.class);
        }catch (Exception e){
            Log.d("JumpService", e.toString());
            sendBean = new SendBean();
        }
        return sendBean;
    }



}
