package cn.vsx.vc.model;

import android.os.Parcel;
import android.os.Parcelable;

import cn.vsx.hamster.terminalsdk.model.GroupBean;

/**
 * Created by XX on 2018/4/11.
 */

public class GroupCatalogBean implements Parcelable {
    private String name;
    private GroupBean bean;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupBean getBean() {
        return bean;
    }

    public void setBean(GroupBean bean) {
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

    public GroupCatalogBean() {
    }

    protected GroupCatalogBean(Parcel in) {
        this.name = in.readString();
        this.bean = (GroupBean) in.readSerializable();
    }

    public static final Creator<GroupCatalogBean> CREATOR = new Creator<GroupCatalogBean>() {
        @Override
        public GroupCatalogBean createFromParcel(Parcel source) {
            return new GroupCatalogBean(source);
        }

        @Override
        public GroupCatalogBean[] newArray(int size) {
            return new GroupCatalogBean[size];
        }
    };
}
