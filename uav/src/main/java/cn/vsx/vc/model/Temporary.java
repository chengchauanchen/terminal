package cn.vsx.vc.model;

/**
 * Created by zckj on 2017/3/22.
 */

public class Temporary {
    public int personal;
    public int isVoice;
    public String textContent;
    public long time;
    public Temporary(int personal, String textContent, int isVoice, long time){
        this.personal = personal;
        this.textContent = textContent;
        this.isVoice = isVoice;
        this.time = time;
    }
}
