package com.zectec.imageandfileselector.bean;

/**
 * 录音实体类
 */
public class Record {
    private String id;
    private String path;
    private long size;
    private long startTime;
    private long endTime;
    private boolean isPlayed;//是否已经播放过
    private boolean isPlaying;//是否正在播放


    public boolean isPlaying() {
        return isPlaying;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isPlayed() {
        return isPlayed;
    }

    public void setPlayed(boolean played) {
        isPlayed = played;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", isPlayed=" + isPlayed +
                ", isPlaying=" + isPlaying +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
