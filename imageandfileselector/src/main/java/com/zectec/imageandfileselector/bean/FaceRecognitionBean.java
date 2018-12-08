package com.zectec.imageandfileselector.bean;

/**
 * Created by gt358 on 2017/10/11.
 */

public class FaceRecognitionBean {
    private String title;
    private String matcheDegree;
    private String content;
    private String pictureUrl;
    private String detailedHtml;

    public FaceRecognitionBean () {}

    public FaceRecognitionBean(String title, String matcheDegree, String content, String pictureUrl, String detailedHtml) {
        this.title = title;
        this.matcheDegree = matcheDegree;
        this.content = content;
        this.pictureUrl = pictureUrl;
        this.detailedHtml = detailedHtml;
    }

    public String getDetailedHtml() {
        return detailedHtml;
    }

    public void setDetailedHtml(String detailedHtml) {
        this.detailedHtml = detailedHtml;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMatcheDegree() {
        return matcheDegree;
    }

    public void setMatcheDegree(String matcheDegree) {
        this.matcheDegree = matcheDegree;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
