package com.zectec.imageandfileselector.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by XX on 2018/1/18.
 */

public class ImageBean implements Parcelable {
    private String path;
    private boolean isReceive;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isReceive() {
        return isReceive;
    }

    public void setReceive(boolean receive) {
        isReceive = receive;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.path);
        dest.writeByte(this.isReceive ? (byte) 1 : (byte) 0);
    }

    public ImageBean() {
    }

    protected ImageBean(Parcel in) {
        this.path = in.readString();
        this.isReceive = in.readByte() != 0;
    }

    public static final Parcelable.Creator<ImageBean> CREATOR = new Parcelable.Creator<ImageBean>() {
        @Override
        public ImageBean createFromParcel(Parcel source) {
            return new ImageBean(source);
        }

        @Override
        public ImageBean[] newArray(int size) {
            return new ImageBean[size];
        }
    };
}
