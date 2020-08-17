package cn.vsx.vc.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.vsx.vc.application.MyApplication;
import ptt.terminalsdk.tools.SharePreferenceUtil;

public class ScrrenUtils {
    private static ScrrenUtils instance;
    private ExecutorService cachedThreadPool ;

    public ScrrenUtils() {
        cachedThreadPool= Executors.newCachedThreadPool();
    }

    public static ScrrenUtils getInstance(){
        if (instance==null){
            instance=new ScrrenUtils();
            return instance;
        }else {
            return instance;
        }
    }

    public void  openBacklight(String type){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (type.equals("1")){
                    writeLCD("1");
                }else {
                    String spStringValue = SharePreferenceUtil.getSpStringValue(MyApplication.instance.getApplicationContext(), "rhtx", "screen", "");
                    if (!TextUtils.isEmpty(spStringValue)&&spStringValue.equals("1")){
                        writeLCD("0");
                    }else if (!TextUtils.isEmpty(spStringValue)&&spStringValue.equals("0")){
                        writeLCD("1");
                    }else {
                        writeLCD("1");
                    }
                }
            }
        });
    }

    private void writeLCD(String value) {
        try {
            File file = new File("proc/driver/lcm_backlight_status");
            FileOutputStream out = new FileOutputStream(file);
            out.write(value.getBytes());
            out.flush();
            out.close();
            SharePreferenceUtil.putSpStringValue(MyApplication.instance.getApplicationContext(), "rhtx", "screen", value);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
