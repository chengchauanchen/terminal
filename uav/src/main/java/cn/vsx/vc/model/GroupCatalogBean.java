package cn.vsx.vc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by XX on 2018/4/11.
 */

public class GroupCatalogBean implements Parcelable{
    private String name;
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(this.name);
        dest.writeInt(this.id);
    }

    public GroupCatalogBean(){
    }

    public GroupCatalogBean(String name, int id){
        this.name = name;
        this.id = id;
    }

    protected GroupCatalogBean(Parcel in){
        this.name = in.readString();
        this.id = in.readInt();
    }

    public static final Parcelable.Creator<GroupCatalogBean> CREATOR = new Parcelable.Creator<GroupCatalogBean>(){
        @Override
        public GroupCatalogBean createFromParcel(Parcel source){
            return new GroupCatalogBean(source);
        }

        @Override
        public GroupCatalogBean[] newArray(int size){
            return new GroupCatalogBean[size];
        }
    };
}
