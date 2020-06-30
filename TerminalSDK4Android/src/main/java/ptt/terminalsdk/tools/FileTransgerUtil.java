package ptt.terminalsdk.tools;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.model.FileBean;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.Util;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.nfc.NfcManager;

public class FileTransgerUtil {

    //文件命名规范时的类型区别
    private static final String TYPE_AUDIO_CODE = "01OON";
    private static final String TYPE_VEDIO_CODE = "02OON";
    private static final String TYPE_IMAGE_CODE = "03OON";

    //文件的后缀名
    private static final String TYPE_AUDIO_SUFFIX = "mp3";
    private static final String TYPE_VIDEO_SUFFIX = "mp4";
    private static final String TYPE_IMAGE_SUFFIX = "jpg";

    //上传到服务器时的类型
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_IMAGE = "img";

    //文件的后缀名
    public static final String _TYPE_AUDIO_SUFFIX = ".mp3";
    public static final String _TYPE_VIDEO_SUFFIX = ".mp4";
    public static final String _TYPE_IMAGE_SUFFIX = ".jpg";

    //上传文件失败的code
    public static final int UPLOAD_FILE_FAIL_RESULT_CODE =-1;
    //上传文件失败的描述-文件不存在
    public static final String UPLOAD_FILE_FAIL_RESULT_DESC_NOT_EXISTS ="文件不存在";
    public static final String UPLOAD_FILE_FAIL_RESULT_DESC_SERVER_NO_RESPONSE ="服务器无响应";
    public static final String UPLOAD_FILE_FAIL_RESULT_DESC_SERVER_ERROR ="服务器出错";
    public static final String UPLOAD_FILE_FAIL_RESULT_DESC_UPLOAD_FAEL ="上传文件失败";
    public static final String UPLOAD_FILE_FAIL_RESULT_DESC_UPLOAD_INFO_FAEL ="更新文件信息失败";
    public static final String UPLOAD_FILE_FAIL_RESULT_DESC_POWER_SAVE_STATUS ="设备省电模式中，稍后再试";

    //未上传的文件限定时间，超过就自动上传
    private static final int FILE_EXPIRE_TIME = 48;
    /**
     * 获取警号
     *
     * @return
     */
    private static String getPoliceId() {
        int memberNo = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        return (memberNo == 0)?"00000000":handleId(memberNo);
    }
    /**
     * 获取警号
     *
     * @return
     */
    public static int getPoliceIdInt() {
        return TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
    }

    /**
     * 获取UniqueNo
     *
     * @return
     */
    public static long getPoliceUniqueNo() {
        return TerminalFactory.getSDK().getParam(Params.MEMBER_UNIQUENO, 0L);
    }

    /**
     * 获取WarningId
     *
     * @return
     */
    public static String getWarningId() {
        String result = "";
        String tag = getTag();
        if(!TextUtils.isEmpty(tag)){
            try{
                JSONObject jsonObject = JSONObject.parseObject(tag);
                if(jsonObject.containsKey(NfcManager.WID)){
                    result = jsonObject.getString(NfcManager.WID);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取tag
     *
     * @return
     */
    public static String getTag() {
        return MyTerminalFactory.getSDK().getNfcManager().getFileTag();
    }

    /**
     * 获取录像视频文件的名字
     *
     * @param dateString
     * @param fileIndex
     * @return
     */
    public static String getVideoRecodeFileName(String dateString, String fileIndex) {
        return dateString + TYPE_VEDIO_CODE + fileIndex + getPoliceId();
    }

    /**
     * 获取图片文件的名字
     *
     * @return
     */
    public static String getPhotoFileName() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return time + TYPE_IMAGE_CODE + "1234" + getPoliceId();
    }

    /**
     * 获取录音文件的名字
     *
     * @return
     */
    public static String getAudioFileName(String dateString, String fileIndex) {
        return dateString + TYPE_AUDIO_CODE + fileIndex + getPoliceId();
    }

    /**
     * 获取录制视频和音频的文件索引（4位数）
     *
     * @param index
     * @return
     */
    public static String getRecodeFileIndex(int index) {
        if (index < 0 || index > 9999) {
            index = 0;
        }
        DecimalFormat df = new DecimalFormat("0000");
        return df.format(index);
    }

    /**
     * 对警号截取
     *
     * @param memberId
     * @return
     */
    public static String handleId(int memberId) {
        String account = "";
        String s = memberId + "";

        if (!Util.isEmpty(s) && s.length() > 2 && "88".equals(s.substring(0, 2))) {
            account = s.substring(2);
        } else {
            account = s;
        }
        return account;
    }

    /**
     * String转int
     *
     * @param data
     * @return
     */
    public static int stringToInt(String data) {
        int result = 0;
        try {
            result = Integer.parseInt(data);
        } catch (Exception e) {
            e.printStackTrace();
            result = 0;
        } finally {
            return result;
        }
    }

    /**
     * 获取文件的类型
     *
     * @param name
     * @return
     */
    public static String getBITFileType(String name) {
        if (!TextUtils.isEmpty(name)) {
            if (name.contains(TYPE_VEDIO_CODE) && TextUtils.equals(getFileSuffix(name), TYPE_VIDEO_SUFFIX)) {
                //视频文件
                return TYPE_VIDEO;
            } else if (name.contains(TYPE_IMAGE_CODE) && TextUtils.equals(getFileSuffix(name), TYPE_IMAGE_SUFFIX)) {
                //图片文件
                return TYPE_IMAGE;
            } else if (name.contains(TYPE_AUDIO_CODE) && TextUtils.equals(getFileSuffix(name), TYPE_AUDIO_SUFFIX)) {
                //音频
                return TYPE_AUDIO;
            }
        }
        return "";
    }

    /**
     * 获取后缀名
     *
     * @param name
     * @return
     */
    private static String getFileSuffix(String name) {
        if (!TextUtils.isEmpty(name)) {
            String result = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
            return result;
        }
        return "";
    }

    /**
     * 获取文件名称
     *
     * @param name
     * @return
     */
    public static String getFileName(String name) {
        if (!TextUtils.isEmpty(name)) {
            String result = name.substring(name.lastIndexOf("/") + 1);
            return result;
        }
        return "";
    }

    /**
     * 根据文件路径获取目录信息
     *
     * @param path
     * @return
     */
    public static FileBean getFileInfo(String path,File file) {
        FileBean bean = new FileBean();
        bean.setTerminalMemberNo(getPoliceIdInt());
        bean.setTerminalUniqueNo(getPoliceUniqueNo());
        bean.setWarningId(getWarningId());
        bean.setTag(getTag());
        bean.setName(getFileName(path));
        String type =  getBITFileType(path);
        bean.setType(type);
        if(TextUtils.equals(type,TYPE_VIDEO)||TextUtils.equals(type,TYPE_AUDIO)){
            //音频和视频 获取时长
            MediaPlayer mediaPlayer =  getMediaPlayer(MyTerminalFactory.getSDK().application,file);
            if(mediaPlayer!=null&&mediaPlayer.getDuration()>=0){
                bean.setDuration(mediaPlayer.getDuration()+"");
            }else{
                bean = null;
            }
        }
        return bean;
    }

    private static MediaPlayer getMediaPlayer(Context context, File file) {
        MediaPlayer mediaPlayer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            return mediaPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            mediaPlayer = null;
        }
        return mediaPlayer;
    }

    /**
     * 获取目录树
     *
     * @param file
     * @return
     */
    public static List<FileBean> getFileTree(File file, List<FileBean> list, int memberId) {
        if (file.isDirectory()) {
            File subFiles[] = file.listFiles();
            if (subFiles.length > 0) {
                for (File sub : subFiles) {
                    getFileTree(sub, list, memberId);
                }
            }
            return list;
        } else {
            FileBean bean = new FileBean();
            bean.setTerminalMemberNo(memberId);
            String name = file.getName();
            bean.setName(name);
            bean.setType(FileTransgerUtil.getBITFileType(name));
            list.add(bean);
            return list;
        }
    }

    /**
     *
     * @param creatTime
     * @return
     */
    public static boolean isOutOf48Hours(long creatTime) {
        double result = (System.currentTimeMillis()-creatTime) * 1.0 / (1000 * 60 * 60);
        return  (result>FILE_EXPIRE_TIME);
    }

    /**
     * 获取CallId
     * @return
     */
    public static String getCallId(){
//        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return System.currentTimeMillis()+"";
    }

}
