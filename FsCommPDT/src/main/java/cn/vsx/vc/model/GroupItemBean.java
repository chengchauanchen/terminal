package cn.vsx.vc.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import cn.vsx.hamster.terminalsdk.tools.PinYinUtils;

/**
 * Created by XX on 2018/4/13.
 */

public class GroupItemBean<T> implements Comparable<GroupItemBean> {
    private String name;
    private T bean;
    private int type;

    public GroupItemBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int compareTo(@NonNull GroupItemBean group) {
        String pinyin="";
        String pinyin2="";
        if (!TextUtils.isEmpty(name)){
            pinyin=PinYinUtils.getPinYin(name);
        }

        if (group!=null&&!TextUtils.isEmpty(group.getName())){
            pinyin2=PinYinUtils.getPinYin(group.getName());
        }

        return pinyin.compareTo(pinyin2);
    }
}
