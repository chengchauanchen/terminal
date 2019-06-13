package cn.vsx.vc.model;

/**
 * Created by XX on 2018/3/23.
 */

public class Picture {
    private String title;
    private int imageId;

    public Picture(String title, Integer imageId) {
        this.imageId = imageId;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public int getImageId() {
        return imageId;
    }
}
