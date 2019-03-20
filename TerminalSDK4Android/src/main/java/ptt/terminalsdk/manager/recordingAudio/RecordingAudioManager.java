package ptt.terminalsdk.manager.recordingAudio;


import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.FileTransgerUtil;
import ptt.terminalsdk.tools.SDCardUtil;

public class RecordingAudioManager {

    private Logger logger = Logger.getLogger(RecordingAudioManager.class);
    public static final String TAG = "RecordingAudioManager---";
    private Context context;
    private MediaRecorder mediaRecorder;
    private static AudioRecordStatus status = AudioRecordStatus.STATUS_STOPED;
    private File dir;
    private String date;
    private int index;
//    private boolean isImportant = false;
    private MediaRecorder.OnErrorListener onErrorListener;
    private File lastFile;
    //录制每个录音片段的时间
    private static final int AUDIO_RECODE_PER_TIME = 10*60*1000;
//    private static final int AUDIO_RECODE_PER_TIME = 10*1000;
    private final int MESSAGE_WHAT_RECORD_NEXT_FILE = 1000;
    public RecordingAudioManager(Context context) {
        this.context = context;
    }
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_WHAT_RECORD_NEXT_FILE:
                    logger.info(TAG+"准备录制下一个音频文件");
                    //检测内存卡的size
                    MyTerminalFactory.getSDK().getFileTransferOperation().checkExternalUsableSize();
                    checkIndexOutOfBounds();
                    String fileName = FileTransgerUtil.getAudioFileName(date,FileTransgerUtil.getRecodeFileIndex(++index))+ FileTransgerUtil._TYPE_AUDIO_SUFFIX;
                    File file = new File(dir,fileName);
                    mediaRecorder.stop();
                    //文件生成完成
                    if(lastFile!=null){
                        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                        operation.generateFileComplete(
                                MyTerminalFactory.getSDK().getBITAudioRecordedDirectoty(operation.getExternalUsableStorageDirectory())
                                ,lastFile.getPath());
                    }
                    //开始录制下个音频
                    recordFile(file);
                    break;
            }
        }
    };

    /**
     * 开始录制音频文件
     * @param onErrorListener 录制出错的回调
     */
    public synchronized void start(MediaRecorder.OnErrorListener onErrorListener) {
        logger.info(TAG+"start");
        this.onErrorListener = onErrorListener;
        index = 1;
        //检测内存卡的size
        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
        operation.checkExternalUsableSize();
        date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        dir = new File(MyTerminalFactory.getSDK().getBITAudioRecordedDirectoty(operation.getExternalUsableStorageDirectory()));
        if (!dir.exists()){
            dir.mkdirs();
        }
        String fileName = FileTransgerUtil.getAudioFileName(date,FileTransgerUtil.getRecodeFileIndex(index))+ FileTransgerUtil._TYPE_AUDIO_SUFFIX;

        File audioFile = new File(dir,fileName);
        status = AudioRecordStatus.STATUS_READY;
        mediaRecorder = new MediaRecorder();
        recordFile(audioFile);
    }

    private void recordFile(File file){
        logger.info(TAG+"开始录制音频文件");
        try {
            logger.info(TAG+"初始化MediaRecorder");
            // 设置音频录入源
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置录制音频的输出格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            // 设置音频的编码格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            if (onErrorListener != null){
                mediaRecorder.setOnErrorListener(onErrorListener);
            }
            // 设置录制音频文件输出文件路径
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            // 准备、开始
            status = AudioRecordStatus.STATUS_RECORDING;
            mediaRecorder.prepare();
            mediaRecorder.start();
            lastFile = file;
            logger.info(TAG+"lastfile = " + lastFile.getAbsolutePath());
            handler.sendEmptyMessageDelayed(MESSAGE_WHAT_RECORD_NEXT_FILE,AUDIO_RECODE_PER_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录制
     */
    public synchronized void stop() {
        logger.info(TAG+"stop");
        if (status == AudioRecordStatus.STATUS_RECORDING && mediaRecorder != null) {
            try{
                mediaRecorder.stop();
            }catch (Exception e){
                e.printStackTrace();
                logger.info(TAG+"停止录音失败"+e);
            }
            mediaRecorder.release();
            mediaRecorder = null;
            status = AudioRecordStatus.STATUS_STOPED;
            handler.removeMessages(MESSAGE_WHAT_RECORD_NEXT_FILE);
            //文件生成完成
            if(lastFile!=null){
                FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                operation.generateFileComplete(
                        MyTerminalFactory.getSDK().getBITAudioRecordedDirectoty(operation.getExternalUsableStorageDirectory())
                        ,lastFile.getPath());
            }
        }
    }

    private void renameToImportamt(){
        if (lastFile != null) {
            String oldFileName = lastFile.getName();
            if (oldFileName.contains("OON")) {
                String newFileName = oldFileName.replace("OON","IMP");
                File newFile = new File(lastFile.getParent(),newFileName);
                lastFile.renameTo(newFile);
                SDCardUtil.fileScan(context, newFile.getAbsolutePath());
            }else{
                SDCardUtil.fileScan(context, lastFile.getAbsolutePath());
            }
        }
    }

    public AudioRecordStatus getStatus() {
        return status;
    }

    /**
     * 判断文件序号是否超出4位边界
     */
    private void checkIndexOutOfBounds(){
        if(index>=9999){
            index = 0;
            date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
        }
    }
}
