package cn.vsx.vc.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.io.Serializable;

import cn.vsx.hamster.terminalsdk.tools.PinYinUtils;

/**
 * Created by XX on 2018/4/11.
 */

public class ContactItemBean<T> implements Comparable<ContactItemBean>,Cloneable, MultiItemEntity, Serializable{
    private String name;
    private T bean;
    private int type;

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
    public int compareTo(@NonNull ContactItemBean contact) {
        String pinyin="";
        String pinyin2="";
        if (!TextUtils.isEmpty(name)){
            pinyin= PinYinUtils.getPinYin(name);
        }

        if (!TextUtils.isEmpty(contact.getName())){
            pinyin2=PinYinUtils.getPinYin(contact.getName());
        }

        return pinyin.compareTo(pinyin2);
    }

    @Override
    public Object clone() {
        ContactItemBean contactItemBean = null;
        try {
            contactItemBean = (ContactItemBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return contactItemBean;
    }

    @Override
    public int getItemType(){
        return type;
    }

}
