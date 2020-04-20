package ptt.terminalsdk.manager.filetransfer;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

import org.apache.log4j.Logger;
import org.easydarwin.push.EasyPusher;
import org.easydarwin.push.InitCallback;
import org.easydarwin.push.LocalVideoPushStream;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.errcode.BaseCommonCode;
import cn.vsx.hamster.protolbuf.PTTProtolbuf;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.manager.okhttp.RateLimitingRequestBody;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingState;
import cn.vsx.hamster.terminalsdk.manager.videolive.VideoLivePushingStateMachine;
import cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory;
import cn.vsx.hamster.terminalsdk.model.BitStarFileRecord;
import cn.vsx.hamster.terminalsdk.model.FileBean;
import cn.vsx.hamster.terminalsdk.model.FileTreeBean;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveExternStorageSizeHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveLocalVideoPushFinishHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveNotifyMemberUploadFileMessageHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveUpdateUploadFileRateLimitHandler;
import cn.vsx.hamster.terminalsdk.tools.Params;
import cn.vsx.hamster.terminalsdk.tools.SignatureUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import ptt.terminalsdk.broadcastreceiver.FileExpireReceiver;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.tools.FileTransgerUtil;
import ptt.terminalsdk.tools.SDCardUtil;
import ptt.terminalsdk.tools.ToastUtil;
import ptt.terminalsdk.tools.VideoFileUtil;

import static android.content.Context.ALARM_SERVICE;
import static cn.vsx.hamster.terminalsdk.model.BitStarFileDirectory.USB;

public class FileTransferOperation {
    private Context context;
    private Logger logger = Logger.getLogger(getClass());
    public static final String TAG = "FileTransferOperation---";
    //未上传
    public static final int UPLOAD_STATE_NO = 0;
    //上传中
    public static final int UPLOAD_STATE_ING = 1;
    //已上传
    public static final int UPLOAD_STATE_YES = 2;

    private static final String UPLOAD_FILE_SERVER_HEAD = "http://";
    private static final String UPLOAD_FILE_SERVER_PATH = "/file/btx/uploadFile";
    private static final String UPLOAD_FILE_INFO_SERVER_PATH = "/file/btx/uploadInfo";
    private static final String UPLOAD_FILE_NEW_SERVER_PATH = "/interface/btx/uploadFileInfo";
    private static final String COLON = ":";
    private static final String RESULT_SUCCESS = "success";
    private static final String RESULT_SUCCESS_TRUE = "true";
    private static final String RESULT_MSG = "msg";
    private String uploadFileServerUrl;
    private String uploadFileNewServerUrl;
    //通知未上传文件上传的间隔时间
    private static final long EXPIRE_TIME = 48 * 60 * 60 * 1000;
    //        private static final long EXPIRE_TIME = 15 * 1000;
    //第一次保存48小时对比的文件信息
    private static final int HANDLER_WHAT_SAVE_EXPIRE_INFO_FOR_FRIST_TIMES = 1;
    //更新48小时对比的文件信息
    private static final int HANDLER_WHAT_UPDATE_EXPIRE_INFO = 2;

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    //推送视频文件到流媒体服务器
    private LocalVideoPushStream localVideoPushStream;
    private PushCallback pushCallback;
    //需要推送的视频文件的list
    private CopyOnWriteArrayList<BitStarFileRecord> pushVideoFiles = new CopyOnWriteArrayList<>();
    //当前推流文件的个数
    private int pushIndex = 0;
    //是否来自上传推送本地视频文件
    private boolean isPushStreamFromLocalFile;
    //限速的RequestBody
    private RateLimitingRequestBody rateLimitingRequestBody;
    //上传文件限制的速率
    private static final int UPLOAD_FILE_RATE_LIMIT = 2 * 1000 * 1000;
    //上传文件不限制的速率
    private static final int UPLOAD_FILE_RATE_LIMIT_NO = 0;


    public FileTransferOperation(Context context) {
        this.context = context;
    }

    public void start() {
        TerminalFactory.getSDK().registReceiveHandler(receiveNotifyMemberUploadFileMessageHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveLocalVideoPushFinishHandler);
        TerminalFactory.getSDK().registReceiveHandler(receiveUpdateUploadFileRateLimitHandler);//是否限制上传文件的速度
    }

    public void stop() {
        TerminalFactory.getSDK().unregistReceiveHandler(receiveNotifyMemberUploadFileMessageHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveLocalVideoPushFinishHandler);
        TerminalFactory.getSDK().unregistReceiveHandler(receiveUpdateUploadFileRateLimitHandler);//是否限制上传文件的速度
    }

    /**
     * 收到上传本地文件的通知
     */
    private ReceiveNotifyMemberUploadFileMessageHandler receiveNotifyMemberUploadFileMessageHandler = new ReceiveNotifyMemberUploadFileMessageHandler() {
        @Override
        public void handler(PTTProtolbuf.NotifyMemberUploadFileMessage message) {
            logger.info(TAG + "ReceiveNotifyMemberUploadFileMessageHandler:" + message);
            if (message != null) {
                List<String> list = message.getFileNameListList();

                if (list != null && list.size() > 0) {
                    logger.info(TAG + "ReceiveNotifyMemberUploadFileMessageHandler:list:" + list);
                    //从数据库中获取文件的信息,并上传
                    uploadFileByPaths(getRecordsByNames(list), message.getRequestMemberId(),message.getRequestUniqueNo(), false);
                    //推送视频文件到流媒体服务器
                    //                    pushStreamOfVideoFile(list);
                }
            }
        }
    };

    /**
     * 开始上报（限制上传文件的速度）
     **/
    private ReceiveUpdateUploadFileRateLimitHandler receiveUpdateUploadFileRateLimitHandler = new ReceiveUpdateUploadFileRateLimitHandler() {
        @Override
        public void handler(boolean needLimit) {
            if(rateLimitingRequestBody!=null){
                rateLimitingRequestBody.setMaxRate(needLimit?UPLOAD_FILE_RATE_LIMIT:UPLOAD_FILE_RATE_LIMIT_NO);
            }
        }
    };

    /**
     * 推送本地视频文件到流媒体服务器完成的通知
     */
    private ReceiveLocalVideoPushFinishHandler receiveLocalVideoPushFinishHandler = new ReceiveLocalVideoPushFinishHandler() {
        @Override
        public void handler() {
            logger.info(TAG + "ReceiveLocalVideoPushFinishHandler");
            if (pushIndex >= (pushVideoFiles.size() - 1)) {
                //已经推送完成
                pushVideoFiles.clear();
                pushIndex = 0;
                if (localVideoPushStream != null) {
                    localVideoPushStream.stopStream();
                    pushCallback = null;
                }
            } else {
                //推送下个视频文件
                pushIndex++;
                //                requestPushStream();
                pushVideoFile();
            }
        }
    };

    /**
     * 推送视频文件到流媒体服务器
     *
     * @param list
     */
    public void pushStreamOfVideoFile(List<String> list) {
        logger.info(TAG + "pushStreamOfVideoFile：list：" + list);
        List<String> videoNames = new ArrayList<>();
        for (String name : list) {
            if (TextUtils.equals(FileTransgerUtil.TYPE_VIDEO, FileTransgerUtil.getBITFileType(name))) {
                videoNames.add(name);
            }
        }
        if (!videoNames.isEmpty()) {
            //添加推送视频文件信息到暂存list中
            pushVideoFiles.addAll(getRecordsByNames(videoNames));
            pushVideoFile();
        }
    }

    /**
     * 通过上报获取推流的地址
     */
    private void requestPushStream() {
        int requestCode = MyTerminalFactory.getSDK().getLiveManager().requestMyselfLive("", "");
        logger.error(TAG + "requestPushStream：requestCode=" + requestCode);
        if (requestCode == BaseCommonCode.SUCCESS_CODE) {
            isPushStreamFromLocalFile = true;
        } else {
            isPushStreamFromLocalFile = false;
        }
    }

    /**
     * 推送单个视频文件
     */
    private void pushVideoFile() {
        if (pushVideoFiles != null && pushVideoFiles.size() > 0) {
            //发起主动上报
            String ip = TerminalFactory.getSDK().getParam(Params.MEDIA_SERVER_IP, "");
            String port = TerminalFactory.getSDK().getParam(Params.MEDIA_SERVER_PORT, 0) + "";
            String id = FileTransgerUtil.getPoliceIdInt() + "_" + FileTransgerUtil.getCallId();
            if (pushCallback == null) {
                pushCallback = new PushCallback();
            }
            logger.info(TAG + "pushVideoFile：pushCount：" + pushIndex);
            getLocalVideoPushStream().startStream(pushVideoFiles.get(pushIndex).getFilePath(), ip, port, id, pushCallback);
        }
    }

    /**
     * 生成文件完成
     *
     * @param scanPath
     * @param path
     */
    public void generateFileComplete(String scanPath, String path) {
        logger.info(TAG + "generateFileComplete scanPath" + scanPath+"path:"+path);
        //扫描卡中的文件
        SDCardUtil.scanMtpAsync(context, scanPath);
        //上传文件目录和保存文件信息到本地
        uploadFileTreeAndSaveFileToSqlite(path);
    }

    /**
     * 上传文件目录和保存文件的状态到数据库
     */
    public void uploadFileTreeAndSaveFileToSqlite(String path) {
        uploadFileTreeBean(path);
        saveFileToSQlite(path);
    }

    /**
     * 上传文件目录树
     */
    public void uploadFileTreeBean(final String path) {
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                List<FileBean> list = saveAndGetBITFileTreeBean(path);
                if (!isConnected(context)) {
                    logger.info(TAG + "uploadFileTreeBean:isConnected:" + isConnected(context));
                    return;
                }
                if (list != null && list.size() > 0) {
                    logger.info(TAG + "uploadFileTreeBean:url:" + getUploadFileServerUrl() + UPLOAD_FILE_INFO_SERVER_PATH + "-list-" + list);
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("fileTree", new Gson().toJson(list));
                    String result = TerminalFactory.getSDK().getHttpClient().post(getUploadFileServerUrl() + UPLOAD_FILE_INFO_SERVER_PATH, paramsMap);
                    logger.info(TAG + "uploadFileTreeBean:result:" + result);
                    //{"msg":"上传的文件信息已经存在记录","success":false} {"success":true}
                    if (!TextUtils.isEmpty(result)) {
                        try {
                            JSONObject object = JSONObject.parseObject(result);
                            if (object != null) {
                                String success = object.getString(RESULT_SUCCESS);
                                if (RESULT_SUCCESS_TRUE.equals(success)) {
                                    //判断是否是执法记录仪，是的话就自动上传文件
                                    // TODO: 2019/7/24  判断是否是执法记录仪,是的话就自动上传文件,随后要改成服务配置参数
                                    if(isRecorderDevice()){
                                        uploadFileByPath(path, 0, 0L,false);
                                    }
                                    deleteBITFileTreeBean(list);
                                } else {
                                    logger.error(TAG + "uploadFileTreeBean:result;" + object.getString(RESULT_MSG));
                                }
                            } else {
                                logger.info(TAG + "uploadFileTreeBean:result;解析错误");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.info(TAG + "uploadFileTreeBean:result:Exception" + e);
                        }
                    }
                }
            }
        });
    }
    /**
     * 根据文件路径上传文件
     *
     * @param path
     * @param requestMemberId
     * @param isDelete        是否在上传成功之后删除文件
     */
    public void uploadFileByPath(final String path, final int requestMemberId,final long requestUniqueNo, final boolean isDelete) {
        if (!isConnected(context)) {
            logger.info(TAG + "uploadFileByPath:isConnected:" + isConnected(context));
            return;
        }
        TerminalFactory.getSDK().getBITStarFileUploadThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(path);
                logger.info(TAG + "uploadFileByPath:path:" + path + "-file-exists-" + (file.exists()));
                String fileName = FileTransgerUtil.getFileName(path);
                try {
                    if (file.exists()) {
                        String type = FileTransgerUtil.getBITFileType(path);
                        String url = MyTerminalFactory.getSDK().getParam(Params.FILE_UPLOAD_URL, "");
                        if(TextUtils.equals(type,FileTransgerUtil.TYPE_IMAGE)){
                            url = MyTerminalFactory.getSDK().getParam(Params.IMAGE_UPLOAD_URL, "");
                        }
                        String fileUrl = uploadFileByOkHttp(url,file);
                        if(!TextUtils.isEmpty(fileUrl)){
                            //上传成功，调用接口更新文件信息到服务端
                            String result = updateFileByOkHttp(fileName,fileUrl,requestMemberId,requestUniqueNo);
                            if (!TextUtils.isEmpty(result)) {
                                JSONObject object = JSONObject.parseObject(result);
                                if (object != null) {
                                    String success = object.getString(RESULT_SUCCESS);
                                    if (TextUtils.equals(RESULT_SUCCESS_TRUE,success)) {
                                        //是否删除
                                        if (isDelete) {
                                            deleteRecordByName(fileName);
                                            file.delete();
                                        } else {
                                            //更新数据库中文件的上传状态
                                            updateRecordState(fileName, UPLOAD_STATE_YES);
                                        }
                                        //更新48小时未上传的对比的文件信息
                                        checkUpdateExpireFileInfo();
                                    } else {
                                        logger.error(TAG + "uploadFileInfo:result;" + object.getString(RESULT_MSG));
                                        String resultDesc = TextUtils.isEmpty(object.getString(RESULT_MSG))?FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_DESC_UPLOAD_INFO_FAEL:object.getString(RESULT_MSG);
                                        notifyMemberUploadFileFail(fileName, requestUniqueNo, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_CODE, resultDesc);
                                    }
                                } else {
                                    logger.info(TAG + "uploadFileInfo:result;解析错误");
                                    notifyMemberUploadFileFail(fileName, requestUniqueNo, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_CODE, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_DESC_UPLOAD_INFO_FAEL);
                                }
                            } else {
                                notifyMemberUploadFileFail(fileName, requestUniqueNo, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_CODE, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_DESC_UPLOAD_INFO_FAEL);
                            }
                        } else {
                            //上传失败
                            notifyMemberUploadFileFail(fileName, requestUniqueNo, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_CODE, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_DESC_UPLOAD_FAEL);
                        }
                    } else {
                        notifyMemberUploadFileFail(fileName, requestUniqueNo, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_CODE, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_DESC_NOT_EXISTS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //                    TerminalFactory.getSDK().getServiceBusManager().addErrorCount();
                    logger.info(TAG + "uploadFileByPath:result:Exception" + e);
                    notifyMemberUploadFileFail(fileName, requestUniqueNo, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_CODE, FileTransgerUtil.UPLOAD_FILE_FAIL_RESULT_DESC_SERVER_ERROR);
                }
            }
        });
    }

    /**
     * 上传文件
     */
    public String uploadFileByOkHttp(String url, final File file) {
        try{
            long startTime = System.currentTimeMillis();
            rateLimitingRequestBody = RateLimitingRequestBody.createRequestBody(MediaType.parse("multipart/form-data"), file, getUpLoadFileNeedRateLimit()?UPLOAD_FILE_RATE_LIMIT:UPLOAD_FILE_RATE_LIMIT_NO);
            RequestBody requestBody;
            requestBody = new MultipartBody.Builder()
                .addFormDataPart("fileStream", file.getName(), rateLimitingRequestBody)
                .addFormDataPart("sign", SignatureUtil.sign(""))
                .build();
            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.post(requestBody);
            Request request = builder.build();

            String result = mOkHttpClient.newCall(request).execute().body().string();
            long endTime = System.currentTimeMillis() - startTime;
            logger.info(TAG + "uploadFileByOkHttp:url:" + url + "-fileName-" + file.getName() +  "-time-" + endTime+"-result-"+result);
            JSONObject jsonObject = JSONObject.parseObject(result);
            boolean success = jsonObject.getBoolean("success");
            if (success) {
                return jsonObject.getString("path");
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info(TAG + "uploadFileByOkHttp:result:Exception" + e);
            return "";
        }
        return "";
    }

    /**
     * 更新文件信息
     */
    public String updateFileByOkHttp(String fileName, String path, int requestMemberId, long requestUniqueNo) {
        try{
            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("name", fileName);
            paramsMap.put("memberId", FileTransgerUtil.getPoliceIdInt() + "");
            paramsMap.put("uniqueNo", FileTransgerUtil.getPoliceUniqueNo() + "");
            paramsMap.put("path", path);
            if (requestMemberId != 0 && requestUniqueNo != 0) {
                paramsMap.put("requestMemberId", requestMemberId + "");
                paramsMap.put("requestUniqueNo", requestUniqueNo + "");
            }
            return TerminalFactory.getSDK().getHttpClient().post(getUploadFileNewServerUrl() + UPLOAD_FILE_NEW_SERVER_PATH, paramsMap);
        }catch (Exception e){
            e.printStackTrace();
            logger.info(TAG + "updateFileByOkHttp:result:Exception" + e);
        }
        return "";
    }

    /**
     * 上传多个文件
     *
     * @param records
     * @param isDelete 是否在上传成功之后删除文件
     */
    public synchronized void uploadFileByPaths(CopyOnWriteArrayList<BitStarFileRecord> records, int requestMemberId,final long requestUniqueNo, boolean isDelete) {
        if (records != null && records.size() > 0) {
            for (BitStarFileRecord record : records) {
                //上传文件
                //                isDelete?FileTransgerUtil.isOutOf48Hours(record.getFileTime()):如果确认删除 也只是删除48小时之前的文件
                uploadFileByPath(record.getFilePath(), requestMemberId,requestUniqueNo, isDelete);
            }
        }
    }

    /**
     * 上传48小时未上传的文件
     */
    public void uploadFileByExpire() {
        if (!isConnected(context)) {
            logger.info(TAG + "uploadFileByExpire:isConnected:" + isConnected(context));
            return;
        }
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                CopyOnWriteArrayList<BitStarFileRecord> list = getRecordByState(FileTransferOperation.UPLOAD_STATE_NO);
                logger.info(TAG + "uploadFileByExpire:list:" + list);
                if (list != null && list.size() > 0) {
                    //有没有上传的文件
                    CopyOnWriteArrayList<BitStarFileRecord> uploadList = new CopyOnWriteArrayList<>();
                    for (BitStarFileRecord record : list) {
                        if (record != null && FileTransgerUtil.isOutOf48Hours(record.getFileTime())) {
                            //如果超过48小时就加入上传集合中
                            uploadList.add(record);
                        }
                    }
                    if (uploadList.size() > 0) {
                        //有超过48小时未上传的文件
                        uploadFileByPaths(uploadList, 0, 0L,false);
                        showToast("有48小时未上传的文件，开始上传");
                    } else {
                        //没有超过48小时未上传的文件，就把未上传的最早的一条文件信息记录并打开定时任务
                        //                        sendMessgeToUpdateExpireInfo(list.get(0));
                        updateExpireFileInfo(list.get(0));
                    }
                }else{
                    updateExpireFileInfo(null);
                }
            }
        });
    }

    /**
     * 保存文件信息到数据库
     */
    public void saveFileToSQlite(final String path) {
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(path)) {
                    BitStarFileRecord record = new BitStarFileRecord();
                    record.setFileName(FileTransgerUtil.getFileName(path));
                    record.setFilePath(path);
                    record.setFileType(FileTransgerUtil.getBITFileType(path));
                    record.setFileTime(System.currentTimeMillis());
                    record.setFileState(UPLOAD_STATE_NO);
                    if(isUavDevice()){
                        record.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                        if(TextUtils.equals(FileTransgerUtil.TYPE_VIDEO,FileTransgerUtil.getBITFileType(record.getFileName()))){
                            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
                            //视频文件
                            if(bitmap != null){
                                record.setWidth(bitmap.getWidth());
                                record.setHeight(bitmap.getHeight());
                                bitmap.recycle();
                            }
                            int videoDuration = VideoFileUtil.getVideoDuration(path);
                            if(videoDuration>0){
                                record.setDuration(videoDuration);
                                TerminalFactory.getSDK().getSQLiteDBManager().addBitStarFileRecord(record);
                                //发送保存超过48小时对比文件信息
                                saveExpireFileInfoForFirstTimes(record);
                                //                    sendMessgeToSaveExpireInfoForFirstTimes(record);
                            }else {
                                //该视频文件有问题，视频文件删除
                                File file = new File(path);
                                if(file.exists()){
                                    file.delete();
                                }
                            }
                        }else {
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            if(bitmap != null){
                                record.setWidth(bitmap.getWidth());
                                record.setHeight(bitmap.getHeight());
                                bitmap.recycle();
                            }
                            TerminalFactory.getSDK().getSQLiteDBManager().addBitStarFileRecord(record);
                            //发送保存超过48小时对比文件信息
                            saveExpireFileInfoForFirstTimes(record);
                            //                    sendMessgeToSaveExpireInfoForFirstTimes(record);
                        }

                    }else {
                        TerminalFactory.getSDK().getSQLiteDBManager().addBitStarFileRecord(record);
                        //发送保存超过48小时对比文件信息
                        saveExpireFileInfoForFirstTimes(record);
                        //                    sendMessgeToSaveExpireInfoForFirstTimes(record);
                    }
                }
            }
        });
    }

    /**
     * 获取全部的文件
     */
    public CopyOnWriteArrayList<BitStarFileRecord> getRecordByAll() {
        CopyOnWriteArrayList<BitStarFileRecord> list = TerminalFactory.getSDK().getSQLiteDBManager().getBitStarFileRecordByAll();
        logger.info(TAG + "getRecordByAll:--list--" + list);
        return list;
    }

    /**
     * 根据文件的状态获取文件信息
     *
     * @param fileState
     */
    public CopyOnWriteArrayList<BitStarFileRecord> getRecordByState(final int fileState) {
        CopyOnWriteArrayList<BitStarFileRecord> list = TerminalFactory.getSDK().getSQLiteDBManager().getBitStarFileRecordByState(fileState);
        logger.info(TAG + "getRecordByState:fileState：" + fileState + "--size--" + list.size() + "--list--" + list);
        return list;
    }

    /**
     * 根据文件的状态获取文件信息第一条数据
     *
     * @param fileState
     */
    public BitStarFileRecord getRecordByStateAndFirst(final int fileState) {
        BitStarFileRecord record = TerminalFactory.getSDK().getSQLiteDBManager().getBitStarFileRecordByStateAndFirst(fileState);
        logger.info(TAG + "getRecordByStateAndFirst:fileState：" + fileState + "-record--" + record);
        return record;
    }

    /**
     * 根据文件的名字获取文件信息
     *
     * @param fileName
     */
    public BitStarFileRecord getRecordByName(final String fileName) {
        BitStarFileRecord record = null;
        if (!TextUtils.isEmpty(fileName)) {
            record = TerminalFactory.getSDK().getSQLiteDBManager().getBitStarFileRecord(fileName);
            logger.info(TAG + "getRecordByName:fileName：" + fileName + "--record--" + record);
        }
        return record;
    }

    /**
     * 根据多个文件的名字获取多个文件信息
     *
     * @param fileNames
     */
    public CopyOnWriteArrayList<BitStarFileRecord> getRecordsByNames(List<String> fileNames) {
        CopyOnWriteArrayList<BitStarFileRecord> list = new CopyOnWriteArrayList<>();
        String[] array = fileNames.toArray(new String[fileNames.size()]);
        if (fileNames != null && fileNames.size() > 0 && array != null && array.length > 0) {
            list.addAll(TerminalFactory.getSDK().getSQLiteDBManager().getBitStarFileRecords(array));
            logger.info(TAG + "getRecordByName:fileNames：" + fileNames + "--list--" + list);
        }
        return list;
    }

    /**
     * 更新文件的状态
     *
     * @param fileName
     * @param fileState
     */
    public void updateRecordState(final String fileName, final int fileState) {
        logger.info(TAG + "updateRecordState:fileName:" + fileName + "--fileState--" + +fileState);
        if (!TextUtils.isEmpty(fileName)) {
            TerminalFactory.getSDK().getSQLiteDBManager().updateBitStarFileRecordState(fileName, fileState);
        }
    }

    /**
     * 删除数据库中的文件信息
     *
     * @param fileName
     */
    public void deleteRecordByName(final String fileName) {
        logger.info(TAG + "deleteRecordByName:fileName:" + fileName);
        if (!TextUtils.isEmpty(fileName)) {
            TerminalFactory.getSDK().getSQLiteDBManager().deleteBitStarFileRecord(fileName);
        }
    }

    /**
     * 根据文件名称获取目录树
     *
     * @return
     */
    private List<FileBean> saveAndGetBITFileTreeBean(String path) {
        //        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(getExternalUsableStorageDirectory())) {
        List<FileBean> list = new ArrayList<>();
        //获取保存的没有上传的文件目录信息
        FileTreeBean fileTreeBean = TerminalFactory.getSDK().getBean(Params.UNUPLOAD_FILE_TREE_LIST,new FileTreeBean(),FileTreeBean.class);
        if(fileTreeBean.getFileTree()!=null){
            list.addAll(fileTreeBean.getFileTree());
        }
        FileBean bean = getSingleFileTreeBean(path);
        if(bean!=null){
            list.add(bean);
        }
        //保存没有上传的文件目录信息
        fileTreeBean.setFileTree(list);
        TerminalFactory.getSDK().putBean(Params.UNUPLOAD_FILE_TREE_LIST,fileTreeBean);
        logger.info(TAG + "saveAndGetBITFileTreeBean:list:" + list);
        return list;
    }

    /**
     * 文件的目录信息上传成功之后删除保存的记录
     */
    private void deleteBITFileTreeBean(List<FileBean> list){
        FileTreeBean fileTreeBean = TerminalFactory.getSDK().getBean(Params.UNUPLOAD_FILE_TREE_LIST,new FileTreeBean(),FileTreeBean.class);
        List<FileBean> allList =  fileTreeBean.getFileTree();
        if(allList != null && allList.size()>0){
            if(list!=null&&list.size()>0){
                for (FileBean bean :list) {
                    if(allList.contains(bean)){
                        allList.remove(bean);
                    }
                }
                fileTreeBean.setFileTree(allList);
                TerminalFactory.getSDK().putBean(Params.UNUPLOAD_FILE_TREE_LIST,fileTreeBean);
            }
        }
    }

    /**
     * 获取单个文件的目录信息
     * @param path
     * @return
     */
    private FileBean getSingleFileTreeBean(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        File file = new File(path);
        if (file != null && file.exists()) {
            return FileTransgerUtil.getFileInfo(path, file);
        }else{
            return null;
        }
    }

    /**
     * 获取整个目录树
     *
     * @return
     */
    private List<FileBean> getBITFileTreeBeanByAll() {
        int code = MyTerminalFactory.getSDK().getFileTransferOperation().getExternalUsableStorageDirectory();
        //        if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(code)) {
        int memberId = TerminalFactory.getSDK().getParam(Params.MEMBER_ID, 0);
        File file = new File(MyTerminalFactory.getSDK().getBITRecordesDirectoty(code));
        if (!file.exists()) {
            file.mkdir();
        }
        return FileTransgerUtil.getFileTree(file, new ArrayList<FileBean>(), memberId);
        //        } else {
        //            return null;
        //        }
    }

    /**
     * 保存初始化存储空间的目录信息
     */
    public void initExternalUsableStorage(){
        int recordFileStorageType = TerminalFactory.getSDK().getParam(Params.RECORD_FILE_STORAGE,-1);
        if(recordFileStorageType == -1){
            TerminalFactory.getSDK().putParam(Params.RECORD_FILE_STORAGE ,getExternalUsableStorage());
        }
    }

    /**
     * 获取最大的存储空间的目录
     * @return
     */
    public int getExternalUsableStorage(){
        long usb = MyTerminalFactory.getSDK().getExternalUsableSize(USB.getCode());
        long sdCard = MyTerminalFactory.getSDK().getExternalUsableSize(BitStarFileDirectory.SDCARD.getCode());
        return (usb>sdCard)?USB.getCode():BitStarFileDirectory.SDCARD.getCode();
    }

    /**
     * 获取存储空间的目录
     * @return
     */
    public int getExternalUsableStorageDirectory(){
        int recordFileStorageType = TerminalFactory.getSDK().getParam(Params.RECORD_FILE_STORAGE,-1);
        if(recordFileStorageType!=-1){
            return recordFileStorageType;
        }else{
            final int type = getExternalUsableStorage();
            TerminalFactory.getSDK().putParam(Params.RECORD_FILE_STORAGE ,type);
            return type;
        }
    }

    /**
     * 检测存储空间是否够用
     */
    public synchronized void checkExternalUsableSize() {
        long usb = MyTerminalFactory.getSDK().getExternalUsableSize(USB.getCode())/ 1024 / 1024;
        long sdCard = MyTerminalFactory.getSDK().getExternalUsableSize(BitStarFileDirectory.SDCARD.getCode())/ 1024 / 1024;
        int code = getExternalUsableStorageDirectory();
        long memorySize = (code == USB.getCode())?usb:sdCard;
        boolean isNeedNotify = (usb<200&&sdCard<200);
        //检查是否切换存储空间目录
        if(memorySize<100){
            changeExternalStorage(code,usb,sdCard,200);
        }
        //通知页面停止相关操作
        if(isNeedNotify){
            TerminalFactory.getSDK().notifyReceiveHandler(ReceiveExternStorageSizeHandler.class,memorySize);
        }
    }

    /**
     * 当空间不足时切换
     * @param code
     */
    public boolean changeExternalStorage(int code,long usbSize,long sdCardSize,int checkSize){
        final boolean[] flag = {false};
        if(code == USB.getCode()&&sdCardSize>checkSize){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(TerminalFactory.getSDK().checkeExternalStorageIsAvailable(BitStarFileDirectory.SDCARD.getCode())){
                        TerminalFactory.getSDK().putParam(Params.RECORD_FILE_STORAGE ,BitStarFileDirectory.SDCARD.getCode());
                        flag[0] = true;
                    }
                }
            });
        }else if(code == BitStarFileDirectory.SDCARD.getCode()&&usbSize>checkSize){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(TerminalFactory.getSDK().checkeExternalStorageIsAvailable(BitStarFileDirectory.USB.getCode())){
                        TerminalFactory.getSDK().putParam(Params.RECORD_FILE_STORAGE ,USB.getCode());
                        flag[0] = true;
                    }
                }
            });
        }
        return flag[0];
    }

    /**
     * 当内存不足的时候  上传没有上传的文件，删除这些文件
     */
    public void externNoStorageOperation() {
        TerminalFactory.getSDK().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //删除已经上传的文件
                deleteUploadedFile();

                CopyOnWriteArrayList<BitStarFileRecord> noList = getRecordByState(FileTransferOperation.UPLOAD_STATE_NO);
                logger.info(TAG + "externNoStorageOperation:no_upload:" + noList);
                //上传文件,删除
                uploadFileByPaths(noList, 0, 0L,true);
            }
        });
    }

    /**
     * 删除已经上传的文件
     */
    private void deleteUploadedFile() {
        //删除已经上传的文件
        CopyOnWriteArrayList<BitStarFileRecord> yesList = getRecordByState(FileTransferOperation.UPLOAD_STATE_YES);
        String[] array = new String[yesList.size()];
        if (yesList != null && yesList.size() > 0) {
            int size = yesList.size();
            for (int i = 0; i < size; i++) {
                File file = new File(yesList.get(i).getFilePath());
                if (file != null && file.exists()) {
                    file.delete();
                }
                array[i] = yesList.get(i).getFileName();
            }
            if (array.length > 0) {
                TerminalFactory.getSDK().getSQLiteDBManager().deleteBitStarFileRecords(array);
            }
        }
    }

    /**
     * 发送Message 第一次保存48小时过期对比文件信息
     *
     * @param record
     */
    private void sendMessgeToSaveExpireInfoForFirstTimes(BitStarFileRecord record) {
        Message message = mHandler.obtainMessage();
        message.what = HANDLER_WHAT_SAVE_EXPIRE_INFO_FOR_FRIST_TIMES;
        message.obj = record;
        mHandler.sendMessage(message);
    }

    /**
     * 发送Message 更新48小时过期对比文件信息
     *
     * @param record
     */
    private void sendMessgeToUpdateExpireInfo(BitStarFileRecord record) {
        Message message = mHandler.obtainMessage();
        message.what = HANDLER_WHAT_UPDATE_EXPIRE_INFO;
        message.obj = record;
        mHandler.sendMessage(message);
    }

    /**
     * 启动应用时保证48小时定时任务开启
     */
    public void checkStartExpireFileAlarm() {
        BitStarFileRecord expireRecord = TerminalFactory.getSDK().getBean(Params.FILE_EXPIRE_RECORD, null,BitStarFileRecord.class);
        if (expireRecord != null) {
            uploadFileByExpire();
        }
    }

    /**
     * 检查更新48小时未上传的对比的文件信息
     */
    private void checkUpdateExpireFileInfo() {
        BitStarFileRecord record = getRecordByStateAndFirst(UPLOAD_STATE_NO);
        updateExpireFileInfo(record);
    }

    /**
     * 保存超过48小时对比文件信息
     *
     * @param record
     */
    private void saveExpireFileInfoForFirstTimes(BitStarFileRecord record) {
        BitStarFileRecord expireRecord = TerminalFactory.getSDK().getBean(Params.FILE_EXPIRE_RECORD, null,BitStarFileRecord.class);
        if (expireRecord == null) {
            logger.info(TAG + "saveExpireFileInfoForFirstTimes:record" + record);
            //保存超过48小时对比文件信息
            TerminalFactory.getSDK().putBean(Params.FILE_EXPIRE_RECORD, record);
            //开启倒计时
            startFileExpireAlarmManager(record.getFileTime() + EXPIRE_TIME);
        }
    }

    /**
     * 更新48小时未上传的对比的文件信息
     */
    private void updateExpireFileInfo(BitStarFileRecord record) {
        logger.info(TAG + "updateExpireFileInfo-record:" + record);
        if (record != null) {
            //保存超过48小时对比文件信息
            TerminalFactory.getSDK().putBean(Params.FILE_EXPIRE_RECORD, record);
            //关闭倒计时
            cancelFileExpireAlarmManager();
            //开启倒计时
            startFileExpireAlarmManager(record.getFileTime() + EXPIRE_TIME);
        }else{
            //清空超过48小时对比文件信息
            TerminalFactory.getSDK().putBean(Params.FILE_EXPIRE_RECORD, null);
            //关闭倒计时
            cancelFileExpireAlarmManager();
        }
    }

    /**
     * 开启文件48小时未上传的倒计时
     *
     * @param endTime 到期时间
     */
    public void startFileExpireAlarmManager(long endTime) {
        if(isRecorderDevice()){
            logger.info(TAG + "startFileExpireAlarmManager:endTime-" + endTime);
            getAlarmManager().set(AlarmManager.RTC_WAKEUP, endTime, getPendingIntent());
        }
    }

    /**
     * 关闭文件48小时未上传的倒计时
     */
    public void cancelFileExpireAlarmManager() {
        logger.info(TAG + "cancelFileExpireAlarmManager");
        getAlarmManager().cancel(getPendingIntent());
    }

    /**
     * 获取PendingIntent
     */
    private PendingIntent getPendingIntent() {
        if (pendingIntent == null) {
            Intent intent = new Intent(context, FileExpireReceiver.class);
            intent.setAction("vsxin.action.fileexpiretime");
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        return pendingIntent;
    }

    /**
     * 获取AlarmManager
     *
     * @return
     */
    public AlarmManager getAlarmManager() {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        }
        return alarmManager;
    }

    /**
     * 获取LocalVideoPushStream
     *
     * @return
     */
    private LocalVideoPushStream getLocalVideoPushStream() {
        if (localVideoPushStream == null) {
            localVideoPushStream = new LocalVideoPushStream(context);
        }
        return localVideoPushStream;
    }

    /**
     * 获取文件服务器的地址
     *
     * @return
     */
    private String getUploadFileServerUrl() {
        if (TextUtils.isEmpty(uploadFileServerUrl)) {
            String serverIp = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_IP, "");
            int serverPort = MyTerminalFactory.getSDK().getParam(Params.FILE_SERVER_PORT, 0);
            uploadFileServerUrl = UPLOAD_FILE_SERVER_HEAD + serverIp + COLON + serverPort;
            return uploadFileServerUrl;
        } else {
            return uploadFileServerUrl;
        }
    }

    /**
     * 获取更新文件地址到服务的地址
     *
     * @return
     */
    private String getUploadFileNewServerUrl() {
        if (TextUtils.isEmpty(uploadFileNewServerUrl)) {
            String serverIp = MyTerminalFactory.getSDK().getParam(Params.HTTP_IP, "");
            int serverPort = MyTerminalFactory.getSDK().getParam(Params.HTTP_PORT, 0);
            uploadFileNewServerUrl = UPLOAD_FILE_SERVER_HEAD + serverIp + COLON + serverPort;
            return uploadFileNewServerUrl;
        } else {
            return uploadFileNewServerUrl;
        }
    }


    /**
     * 获取是否来自上传推送本地视频文件
     *
     * @return
     */
    public boolean isPushStreamFromLocalFile() {
        return isPushStreamFromLocalFile;
    }

    /**
     * 判断网络连接状态
     *
     * @param context
     * @return
     */
    private boolean isConnected(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                return info.isAvailable();
            }
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_WHAT_SAVE_EXPIRE_INFO_FOR_FRIST_TIMES:
                    //保存48小时对比的文件信息
                    BitStarFileRecord saveRecord = (BitStarFileRecord) msg.obj;
                    saveExpireFileInfoForFirstTimes(saveRecord);
                    break;
                case HANDLER_WHAT_UPDATE_EXPIRE_INFO:
                    //保存48小时对比的文件信息
                    BitStarFileRecord updateRecord = (BitStarFileRecord) msg.obj;
                    updateExpireFileInfo(updateRecord);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * toast
     * @param content
     */
    private void showToast(final String content) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(MyTerminalFactory.getSDK().getApplication(),content);
            }
        });
    }

    /**
     * 通知PC文件上传失败
     * @param fileName
     * @param requestUniqueNo
     * @param resultCode
     * @param resultDesc
     */
    private void notifyMemberUploadFileFail(String fileName,long requestUniqueNo,int resultCode,String resultDesc){
        //被动上传文件（PC拉取文件）
        if(requestUniqueNo !=0){

            //文件不存在，通知PC上传失败
            TerminalFactory.getSDK().getFileTransferManager().notifyMemberUploadFileFail(fileName,requestUniqueNo,
                resultCode,resultDesc);
        }
    }

    private class PushCallback implements InitCallback {

        @Override
        public void onCallback(int code) {
            switch (code) {
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_INVALID_KEY:
                    logger.info(TAG + "PushCallback：EasyRTSP 无效Key");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_SUCCESS:
                    logger.info(TAG + "PushCallback：EasyRTSP 激活成功");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTING:
                    logger.info(TAG + "PushCallback：EasyRTSP 连接中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECTED:
                    logger.info(TAG + "PushCallback：EasyRTSP 连接成功");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_FAILED:
                    logger.info(TAG + "PushCallback：EasyRTSP 连接失败");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_CONNECT_ABORT:
                    logger.info(TAG + "PushCallback：EasyRTSP 连接异常中断");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_PUSHING:
                    logger.info(TAG + "PushCallback：EasyRTSP 推流中");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_PUSH_STATE_DISCONNECTED:
                    logger.info(TAG + "PushCallback：EasyRTSP 断开连接");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PLATFORM_ERR:
                    logger.info(TAG + "PushCallback：EasyRTSP 平台不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_COMPANY_ID_LEN_ERR:
                    logger.info(TAG + "PushCallback：EasyRTSP 断授权使用商不匹配");
                    break;
                case EasyPusher.OnInitPusherCallback.CODE.EASY_ACTIVATE_PROCESS_NAME_LEN_ERR:
                    logger.info(TAG + "PushCallback：EasyRTSP 进程名称长度不匹配");
                    break;
            }
        }
    }

    /**
     * 上传文件的okhttp客户端
     */
    private OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();

    /**
     * 获取限速大小
     * @return
     */
    private boolean getUpLoadFileNeedRateLimit() {
        VideoLivePushingStateMachine liveStateMachine = TerminalFactory.getSDK().getLiveManager().getVideoLivePushingStateMachine();
        return (liveStateMachine != null && liveStateMachine.getCurrentState()!= VideoLivePushingState.IDLE);
    }

    /**
     * 是否是执法记录仪
     * @return
     */
    private boolean isRecorderDevice(){
        String type = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        return TerminalMemberType.valueOf(type).getCode() == TerminalMemberType.TERMINAL_BODY_WORN_CAMERA.getCode();
    }

    private boolean isUavDevice(){
        String type = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        return TerminalMemberType.valueOf(type).getCode() == TerminalMemberType.TERMINAL_UAV.getCode();
    }
}
