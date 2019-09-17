package cn.vsx.uav.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.Objects;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/21
 * 描述：
 * 修订历史：
 */
public class FileBean implements Parcelable,Comparable<FileBean>, MultiItemEntity{
    private String path;
    private String name;
    private boolean isVideo;
    private long duration;
    private int width;
    private int height;
    private boolean selected;
    private String date;
    private int type;
    private long fileSize;

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getPath(){
        return path;
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public boolean isVideo(){
        return isVideo;
    }

    public void setIsVideo(boolean isVideo){
        this.isVideo = isVideo;
    }

    public long getDuration(){
        return duration;
    }

    public void setDuration(long duration){
        this.duration = duration;
    }

    public int getWidth(){
        return width;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public int getHeight(){
        return height;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public long getFileSize(){
        return fileSize;
    }

    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileBean bean = (FileBean) o;
        return Objects.equals(name, bean.name) &&
                Objects.equals(path, bean.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    @Override
    public String toString(){
        return "FileBean{" + "path='" + path + '\'' + ", name='" + name + '\'' + ", isVideo=" + isVideo + ", duration=" + duration + ", width=" + width + ", height=" + height  + ", selected=" + selected + ", date='" + date + '\'' + ", type=" + type + '}';
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(this.path);
        dest.writeString(this.name);
        dest.writeByte(this.isVideo ? (byte) 1 : (byte) 0);
        dest.writeLong(this.duration);
        dest.writeLong(this.fileSize);
        dest.writeInt(this.width);
        dest.writeInt(this.type);
        dest.writeInt(this.height);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
        dest.writeString(this.date);
    }

    public FileBean(){
    }

    protected FileBean(Parcel in){
        this.path = in.readString();
        this.name = in.readString();
        this.isVideo = in.readByte() != 0;
        this.duration = in.readLong();
        this.fileSize = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.type = in.readInt();
        this.selected = in.readByte() != 0;
        this.date = in.readString();
    }

    public static final Creator<FileBean> CREATOR = new Creator<FileBean>(){
        @Override
        public FileBean createFromParcel(Parcel source){
            return new FileBean(source);
        }

        @Override
        public FileBean[] newArray(int size){
            return new FileBean[size];
        }
    };

    @Override
    public int getItemType(){
        return 0;
    }

    @Override
    public int compareTo(@NonNull FileBean o){
        return 0;
    }
}
