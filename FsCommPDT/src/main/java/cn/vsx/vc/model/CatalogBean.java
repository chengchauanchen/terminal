package cn.vsx.vc.model;


import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

/**
 * Created by XX on 2018/4/11.
 */

public class CatalogBean implements Parcelable {
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

    public CatalogBean(){
    }

    public CatalogBean(String name, int id){
        this.name = name;
        this.id = id;
    }

    protected CatalogBean(Parcel in){
        this.name = in.readString();
        this.id = in.readInt();
    }

    public static final Creator<CatalogBean> CREATOR = new Creator<CatalogBean>(){
        @Override
        public CatalogBean createFromParcel(Parcel source){
            return new CatalogBean(source);
        }

        @Override
        public CatalogBean[] newArray(int size){
            return new CatalogBean[size];
        }
    };

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogBean that = (CatalogBean) o;
        return id == that.id;
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }
}
