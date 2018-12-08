package cn.vsx.vc.model;


import android.os.Parcel;
import android.os.Parcelable;

import cn.vsx.hamster.terminalsdk.model.MemberResponse;

/**
 * Created by XX on 2018/4/11.
 */

public class CatalogBean implements Parcelable {
    private String name;
    private MemberResponse bean;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MemberResponse getBean() {
        return bean;
    }

    public void setBean(MemberResponse bean) {
        this.bean = bean;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeSerializable(this.bean);
    }

    public CatalogBean() {
    }

    protected CatalogBean(Parcel in) {
        this.name = in.readString();
        this.bean = (MemberResponse) in.readSerializable();
    }

    public static final Parcelable.Creator<CatalogBean> CREATOR = new Parcelable.Creator<CatalogBean>() {
        @Override
        public CatalogBean createFromParcel(Parcel source) {
            return new CatalogBean(source);
        }

        @Override
        public CatalogBean[] newArray(int size) {
            return new CatalogBean[size];
        }
    };
}
