package cn.vsx.vc.utils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import cn.vsx.hamster.terminalsdk.manager.auth.LoginModel;

/**
 * Created by zckj on 2017/7/3.
 */

public class SetToListUtil {
    private static ArrayList<String> arrayList = new ArrayList<>();
    public static ArrayList<String> setToArrayList(Map<String, LoginModel> map ){
        arrayList.clear();
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            String ip = iterator.next();
            arrayList.add(ip);
        }
        return arrayList;
    }
}
