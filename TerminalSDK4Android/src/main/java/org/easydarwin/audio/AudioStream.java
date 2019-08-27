package org.easydarwin.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import org.apache.log4j.Logger;
import org.easydarwin.muxer.EasyMuxer;
import org.easydarwin.push.Pusher;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import cn.vsx.hamster.common.CallMode;
import cn.vsx.hamster.common.TerminalMemberType;
import cn.vsx.hamster.common.UrlParams;
import cn.vsx.hamster.terminalsdk.TerminalFactory;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallCeasedIndicationHandler;
import cn.vsx.hamster.terminalsdk.receiveHandler.ReceiveGroupCallIncommingHandler;
import ptt.terminalsdk.context.MyTerminalFactory;

public class AudioStream{
    private EasyMuxer muxer;
    private int samplingRate = 8000;
    private int bitRate = 16000;
    private int BUFFER_SIZE = 1920;
    private int mSamplingRateIndex = 0;
    private AudioRecord mAudioRecord;
    private MediaCodec mMediaCodec;
    private Thread mThread = null;
    private String TAG = "AudioStream";
    private Logger logger = Logger.getLogger(getClass());

    protected MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    protected ByteBuffer[] mBuffers = null;
    Set<Pusher> sets = new HashSet<>();
    private boolean groupCalling = false;//是否正在组呼
    /**
     * There are 13 supported frequencies by ADTS.
     **/
    public static final int[] AUDIO_SAMPLING_RATES = {96000, // 0
            88200, // 1
            64000, // 2
            48000, // 3
            44100, // 4
            32000, // 5
            24000, // 6
            22050, // 7
            16000, // 8
            12000, // 9
            11025, // 10
            8000, // 11
            7350, // 12
            -1, // 13
            -1, // 14
            -1, // 15
    };
    private Thread mWriter;
    private MediaFormat newFormat;
    private final String type;

    public AudioStream() {
        type = TerminalFactory.getSDK().getParam(UrlParams.TERMINALMEMBERTYPE);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().registReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        int i = 0;
        for (; i < AUDIO_SAMPLING_RATES.length; i++) {
            if (AUDIO_SAMPLING_RATES[i] == samplingRate) {
                mSamplingRateIndex = i;
                break;
            }
        }
    }

    /**
     * 被动方组呼来了
     */
    private ReceiveGroupCallIncommingHandler receiveGroupCallIncommingHandler = new ReceiveGroupCallIncommingHandler() {
        @Override
        public void handler(final int memberId, final String memberName, final int groupId,
                            String version, CallMode currentCallMode) {
            groupCalling = true;
        }
    };

    //组呼停止
    private ReceiveGroupCallCeasedIndicationHandler receiveGroupCallCeasedIndicationHandler = new ReceiveGroupCallCeasedIndicationHandler() {
        @Override
        public void handler(int reasonCode) {
            groupCalling = false;
        }
    };

    public void addPusher(Pusher pusher){
        boolean shouldStart =false;
        synchronized (this){
            if (sets.isEmpty())
                shouldStart = true;
            sets.add(pusher);
        }
        if (shouldStart) startRecord();
    }

    public void removePusher(Pusher pusher){
        boolean shouldStop = false;
        synchronized (this){
            sets.remove(pusher);
            if (sets.isEmpty())
                shouldStop = true;
        }
        if (shouldStop) stop();
    }

    /**
     * 编码
     */
    private void startRecord() {
        logger.info("startRecord---"+mThread);
        if (mThread != null) return;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                int len = 0, bufferIndex = 0;
                try {
                    int bufferSize = AudioRecord.getMinBufferSize(samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                    mMediaCodec = MediaCodec.createEncoderByType("audio/mp4a-latm");
                    MediaFormat format = new MediaFormat();
                    format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
                    format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
                    format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                    format.setInteger(MediaFormat.KEY_SAMPLE_RATE, samplingRate);
                    format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                            MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                    format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE);
                    mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                    mMediaCodec.start();


                    mWriter = new WriterThread();
                    mWriter.start();
                    mAudioRecord.startRecording();
                    final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();

                    long presentationTimeUs = 0;
                    while (mThread != null) {
                        bufferIndex = mMediaCodec.dequeueInputBuffer(1000);
                        if (bufferIndex >= 0) {
                            inputBuffers[bufferIndex].clear();
                            len = mAudioRecord.read(inputBuffers[bufferIndex], BUFFER_SIZE);
                            long timeUs = System.nanoTime() / 1000;
                            Log.i(TAG, String.format("audio: %d [%d] ", timeUs, timeUs - presentationTimeUs));
                            presentationTimeUs = timeUs;
                            if (len == AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
                                mMediaCodec.queueInputBuffer(bufferIndex, 0, 0, presentationTimeUs, 0);
                            } else {
                                mMediaCodec.queueInputBuffer(bufferIndex, 0, len, presentationTimeUs, 0);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Record___Error!!!!!");
                    e.printStackTrace();
                } finally {
                    Thread t = mWriter;
                    mWriter = null;
                    while (t != null && t.isAlive()) {
                        try {
                            t.interrupt();
                            t.join();
                        } catch (InterruptedException e) {
                            logger.error("mWriter被中断了");
                            e.printStackTrace();
                        }
                    }

                    try {
                        if (mAudioRecord != null) {
                            mAudioRecord.stop();
                            mAudioRecord.release();
                            mAudioRecord = null;
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }

                    try {
                        if (mMediaCodec != null) {
                            mMediaCodec.stop();
                            mMediaCodec.release();
                            mMediaCodec = null;
                        }
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }, "AACRecoder");
        mThread.start();

    }


    public synchronized void setMuxer(EasyMuxer muxer) {
        if (muxer != null) {
            if (newFormat != null)
                muxer.addTrack(newFormat, false);
        }
        this.muxer = muxer;
    }


    private class WriterThread extends Thread{

        public WriterThread() {
            super("WriteAudio");
        }

        @Override
        public void run() {
            int index = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            } else {
                mBuffers = mMediaCodec.getOutputBuffers();
            }
            ByteBuffer mBuffer = ByteBuffer.allocate(10240);
            do {
                index = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 10000);
                if (index >= 0) {
                    if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        continue;
                    }
                    mBuffer.clear();
                    ByteBuffer outputBuffer = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        outputBuffer = mMediaCodec.getOutputBuffer(index);
                    } else {
                        outputBuffer = mBuffers[index];
                    }

                    if (muxer != null)
                        muxer.pumpStream(outputBuffer, mBufferInfo, false);
                    outputBuffer.get(mBuffer.array(), 7, mBufferInfo.size);
                    outputBuffer.clear();
                    mBuffer.position(7 + mBufferInfo.size);
                    addADTStoPacket(mBuffer.array(), mBufferInfo.size + 7);
                    mBuffer.flip();
                    Collection<Pusher> p;
                    synchronized (AudioStream.this){
                        p = sets;
                    }
                    Iterator<Pusher> it = p.iterator();
                    while (it.hasNext()){
                        Pusher ps = it.next();
                        if(checkIfPush()){
                            ps.push(mBuffer.array(), 0, mBufferInfo.size + 7, mBufferInfo.presentationTimeUs / 1000, 0);
                        }
                    }

                    mMediaCodec.releaseOutputBuffer(index, false);
                } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    mBuffers = mMediaCodec.getOutputBuffers();
                } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    synchronized (AudioStream.this) {
                        Log.v(TAG, "output format changed...");
                        newFormat = mMediaCodec.getOutputFormat();
                        if (muxer != null)
                            muxer.addTrack(newFormat, false);
                    }
                } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                    Log.v(TAG, "No buffer available...");
                } else {
                    Log.e(TAG, "Message: " + index);
                }
            } while (mWriter != null);
        }
    }

    private boolean checkIfPush(){

        if(TextUtils.equals(type, TerminalMemberType.TERMINAL_UAV.name())){
            logger.info("终端类型为无人机---"+"是否开启声音："+TerminalFactory.getSDK().getDataManager().isUavVoiceOpen());
            return TerminalFactory.getSDK().getDataManager().isUavVoiceOpen();
        }else {
            logger.info("是否开启声音:"+true);
            return true;
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((2 - 1) << 6) + (mSamplingRateIndex << 2) + (1 >> 2));
        packet[3] = (byte) (((1 & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private void stop() {
        logger.info("audioStream---stop");
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallIncommingHandler);
        MyTerminalFactory.getSDK().unregistReceiveHandler(receiveGroupCallCeasedIndicationHandler);
        try {
            Thread t = mThread;
            mThread = null;
            if (t != null) {
                t.interrupt();
                t.join();
            }
        } catch (InterruptedException e) {
            logger.error("mThread被中断了");
            e.printStackTrace();
        }
    }

}
