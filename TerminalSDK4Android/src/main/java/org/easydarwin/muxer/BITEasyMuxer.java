package org.easydarwin.muxer;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.vsx.hamster.terminalsdk.TerminalFactory;
import ptt.terminalsdk.context.MyTerminalFactory;
import ptt.terminalsdk.manager.filetransfer.FileTransferOperation;
import ptt.terminalsdk.tools.FileTransgerUtil;

/**
 * Created by John on 2017/1/10.
 */

public class BITEasyMuxer implements BaseEasyMuxer{
    Logger logger = Logger.getLogger(getClass().getName());
    //    private static final boolean VERBOSE = BuildConfig.DEBUG;
    private static final String TAG = BITEasyMuxer.class.getSimpleName()+"---";
    private String mFilePath;
    private MediaMuxer mMuxer;
    private final long durationMillis;
//    private int index = 1;
    private String dateStr = "";
    private String fileIndex = "1";
    private int mVideoTrackIndex = -1;
    private int mAudioTrackIndex = -1;
    private long mBeginMillis;
    private MediaFormat mVideoFormat;
    private MediaFormat mAudioFormat;
    private Context mContext;
    private boolean toastMemoryNotSufficient;

    public BITEasyMuxer(String path, long durationMillis) {
        mFilePath = path;
        this.durationMillis = durationMillis;
        Object mux = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mux = new MediaMuxer(path + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mMuxer = (MediaMuxer) mux;
        }
    }
    @Override
    public synchronized void addTrack(MediaFormat format, boolean isVideo) {
        // now that we have the Magic Goodies, start the muxer
        if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1)
            throw new RuntimeException("already add all tracks");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            int track = mMuxer.addTrack(format);
//            if (VERBOSE)
            logger.info(TAG+String.format("addTrack %s result %d", isVideo ? "video" : "audio", track));
            if (isVideo) {
                mVideoFormat = format;
                mVideoTrackIndex = track;
                if (mAudioTrackIndex != -1) {
//                    if (VERBOSE)
                    logger.info(TAG+"both audio and video added,and muxer is started");
                    mMuxer.start();
                    mBeginMillis = System.currentTimeMillis();
                }
            } else {
                mAudioFormat = format;
                mAudioTrackIndex = track;
                if (mVideoTrackIndex != -1) {
                    mMuxer.start();
                    mBeginMillis = System.currentTimeMillis();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void pumpStream(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean isVideo) {
        if (mAudioTrackIndex == -1 || mVideoTrackIndex == -1) {
            logger.info(TAG+String.format("pumpStream [%s] but muxer is not start.ignore..", isVideo ? "video" : "audio"));
            return;
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
        } else if (bufferInfo.size != 0) {
            if (isVideo && mVideoTrackIndex == -1) {
                throw new RuntimeException("muxer hasn't started");
            }

            // adjust the ByteBuffer values to match BufferInfo (not needed?)
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mMuxer.writeSampleData(isVideo ? mVideoTrackIndex : mAudioTrackIndex, outputBuffer, bufferInfo);
            }
//            if (VERBOSE)
//            logger.info(TAG+String.format("sent %s [" + bufferInfo.size + "] with timestamp:[%d] to muxer", isVideo ? "video" : "audio", bufferInfo.presentationTimeUs / 1000));
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//            if (VERBOSE)
            logger.info(TAG+"BUFFER_FLAG_END_OF_STREAM received");
        }

        if (System.currentTimeMillis() - mBeginMillis >= durationMillis) {
            logger.info(TAG+"超过每段视频的时间，重新创建视频片段的文件");
                //录制过程中检测内存卡的size
            FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
            boolean onlyUserSdCard = operation.checkOnlyUseSdCardStorage();
            if(!onlyUserSdCard){
                operation.checkExternalUsableSize();
            }
            if (TerminalFactory.getSDK().checkeExternalStorageIsAvailable(operation.getExternalUsableStorageDirectory())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                if (VERBOSE)
                    logger.info(TAG+String.format("record file reach expiration.create new file:" + fileIndex));
                    mMuxer.stop();
                    mMuxer.release();
                    mMuxer = null;
                    mVideoTrackIndex = mAudioTrackIndex = -1;
                    String directoty = MyTerminalFactory.getSDK().getBITVideoRecordesDirectoty(operation.getExternalUsableStorageDirectory());
                    //生成文件
                    operation.generateFileComplete(directoty,mFilePath+ FileTransgerUtil._TYPE_VIDEO_SUFFIX);
                    try {
                        checkIndexOutOfBounds();
                        logger.info(TAG+"5分钟后生成新的退图像文件，之前的 mFilePath====" + mFilePath);
                        String fileName = FileTransgerUtil.getVideoRecodeFileName(dateStr , fileIndex );
                        File videoRecord =  new File(directoty, fileName);
                        mFilePath =videoRecord.toString();
                        logger.info(TAG+"5分钟后生成新的退图像文件， mFilePath====" + mFilePath);
                        mMuxer = new MediaMuxer(mFilePath + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        addTrack(mVideoFormat, true);
                        addTrack(mAudioFormat, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }


    public void setDateStr (String dateStr, String fileIndex) {
        this.dateStr = dateStr;
        this.fileIndex = fileIndex;
    }

    public synchronized void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (mMuxer != null) {
                if (mAudioTrackIndex != -1 && mVideoTrackIndex != -1) {
//                    if (VERBOSE)
                    logger.info(TAG+String.format("muxer is started. now it will be stoped."));
                    try {
                        mMuxer.stop();
                        mMuxer.release();
                    } catch (IllegalStateException ex) {
                        ex.printStackTrace();
                    }
                    File file = new File(mFilePath + ".mp4");
                    if (System.currentTimeMillis() - mBeginMillis <= 1000 || file.length() < 10){
                        file.delete();
                    }else{
                        //生成文件
                        FileTransferOperation operation = MyTerminalFactory.getSDK().getFileTransferOperation();
                        operation.generateFileComplete(
                                MyTerminalFactory.getSDK().getBITVideoRecordesDirectoty(operation.getExternalUsableStorageDirectory()),
                                mFilePath+ FileTransgerUtil._TYPE_VIDEO_SUFFIX);
                    }
                    mAudioTrackIndex = mVideoTrackIndex = -1;
                }
            }
        }
    }

    /**
     * 判断文件序号是否超出4位边界
     */
    private void checkIndexOutOfBounds(){
        int index = FileTransgerUtil.stringToInt(fileIndex);
        if(index>=9999){
            index = 0;
            dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        }
        fileIndex = FileTransgerUtil.getRecodeFileIndex(++index);
    }
}
