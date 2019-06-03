package cn.vsx.vc.model;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/5/31
 * 描述：
 * 修订历史：
 */
public class MediaBean{
    private String url;
    private String startTime;
    private boolean selected;

    public String getUrl(){
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public String getStartTime(){
        return startTime;
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
}
