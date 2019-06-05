package ptt.terminalsdk.manager.live;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/5
 * 描述：
 * 修订历史：
 */
public class LiveManager{

    public static final String DEFAULT_SERVER_URL = "rtmp://www.easydss.com:10085/live/stream_"+String.valueOf((int) (Math.random() * 1000000 + 100000));


    private String playKey = "79393674363536526D3432414D7435517279476D63505A6A6269353263336775646D4E584446616732504467523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D";
    private String rtmpPlayKey = "59617A414C5A36526D3432415A657462704253614A654676636D63755A57467A65575268636E64706269356C59584E3563477868655756794C6E4A306258416A567778576F502B6C3430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";
    /*这是永久的推流rtsp的key*/
    private String pushKey = "6A36334A743536526D343041676C394C744B425738505A6A6269353263336775646D4E584446616732504467523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D";

    public String getPlayKey(){
        return playKey;
    }

    public void setPlayKey(String playKey){
        this.playKey = playKey;
    }

    public String getRtmpPlayKey(){
        return rtmpPlayKey;
    }

    public void setRtmpPlayKey(String rtmpPlayKey){
        this.rtmpPlayKey = rtmpPlayKey;
    }

    public String getPushKey(){
        return pushKey;
    }

    public void setPushKey(String pushKey){
        this.pushKey = pushKey;
    }
}
