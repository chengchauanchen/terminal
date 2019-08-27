package cn.vsx.uav.bean;

import java.util.List;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/8/23
 * 描述：
 * 修订历史：
 */
public class DateBean{
    private String date;
    private List<FileBean> fileBeans;

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public List<FileBean> getFileBeans(){
        return fileBeans;
    }

    public void setFileBeans(List<FileBean> fileBeans){
        this.fileBeans = fileBeans;
    }
}
