package ptt.terminalsdk.manager.live;

import android.text.TextUtils;

import org.apache.log4j.Logger;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.tools.Params;
import ptt.terminalsdk.tools.ApkUtil;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/6/5
 * 描述：
 * 修订历史：
 */
public class LiveManager{
    private Logger logger = Logger.getLogger(getClass());
    public static final String DEFAULT_SERVER_URL = "rtmp://www.easydss.com:10085/live/stream_"+String.valueOf((int) (Math.random() * 1000000 + 100000));

    //cn.vsx.vc 临时key
    private String playKey = "79393674363536526D3432414D7435517279476D63505A6A6269353263336775646D4E584446616732504467523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D";
    /*这是永久的推流rtsp的key*/
    private String pushKey = "6A36334A743536526D343041676C394C744B425738505A6A6269353263336775646D4E584446616732504467523246326157346D516D466962334E68514449774D545A4659584E355247467964326C75564756686257566863336B3D";
    private String rtmpPlayKey = "59617A414C5A36526D3432415A657462704253614A654676636D63755A57467A65575268636E64706269356C59584E3563477868655756794C6E4A306258416A567778576F502B6C3430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";

    //com.vsxin.pstation 临时key
    private String playKeyWuTie = "79393674363536526D34304179684E656F657731772B316A62323075646E4E346157347563484E305958527062323573567778576F502F443430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";
    private String pushKeyWuTie = "6A36334A743536526D34304179684E656F657731772B316A62323075646E4E346157347563484E305958527062323470567778576F502F443430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";
//    //com.vsxin.langfang 临时key
//    private String playKeyLangFang = "79393674363536526D34304179684E656F657731772B316A62323075646E4E346157347563484E305958527062323573567778576F502F443430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";
//    private String pushKeyLangFang = "6A36334A743536526D34304179684E656F657731772B316A62323075646E4E346157347563484E305958527062323470567778576F502F443430566863336C4559584A33615735555A57467453584E55614756435A584E30514449774D54686C59584E35";
    public String getPlayKey(){
        String playKeyFormServer = TerminalFactory.getSDK().getParam(Params.EASYDARWIN_PLAYER_ANDROID_KEY);
        logger.info("LiveManager---playKeyFormServer:"+playKeyFormServer);
        if(!TextUtils.isEmpty(playKeyFormServer)){
            return playKeyFormServer;
        }else{
            if(ApkUtil.isWuTie()){
                return playKeyWuTie;
            }else if(ApkUtil.isLangFang()){
                return playKeyWuTie;
            }else{
                return playKey;
            }
        }
    }

    public void setPlayKey(String playKey){
        this.playKey = playKey;
    }

    public String getRTMPPlayKey(){
        return rtmpPlayKey;
    }

    public void setRTMPPlayKey(String rtmpPlayKey){
        this.rtmpPlayKey = rtmpPlayKey;
    }

    public String getPushKey(){
        String pushKeyFormServer = TerminalFactory.getSDK().getParam(Params.EASYDARWIN_PUSHER_ANDROID_KEY);
        logger.info("LiveManager---pushKeyFormServer:"+pushKeyFormServer);
        if(!TextUtils.isEmpty(pushKeyFormServer)){
            return pushKeyFormServer;
        }else{
            if(ApkUtil.isWuTie()){
                return pushKeyWuTie;
            }else if(ApkUtil.isLangFang()){
                return pushKeyWuTie;
            }else{
                return pushKey;
            }
        }
    }

    public void setPushKey(String pushKey){
        this.pushKey = pushKey;
    }
}
