package cn.zectec.speex;

import cn.vsx.hamster.terminalsdk.manager.audio.ISpeex;

public class Speex implements ISpeex{
     /** quality 
     * 1 : 4kbps (very noticeable artifacts, usually intelligible) 
     * 2 : 6kbps (very noticeable artifacts, good intelligibility) 
     * 4 : 8kbps (noticeable artifacts sometimes) 
     * 6 : 11kpbs (artifacts usually only noticeable with headphones) 
     * 8 : 15kbps (artifacts not usually noticeable)
     * default with 8 
     */  
    public static final int DEFAULT_COMPRESSION = 8;
    /** sample rate
     * only support 8000, 16000, 32000
     */
    public static final int DEFAULT_SAMPLING_RATE = 8000; 
    /**
     * encode buffer only 8 * frameSize see getFrameSize
     */
    public static final int ERROR_ENCODE_BUFFER_OVERFLOW = -1;

    public Speex() {  
    }  

    /**
     * initClient with compression and samplingRate
     * @param compression see DEFAULT_COMPRESSION
     * @param samplingRate see DEFAULT_SAMPLING_RATE
     */
    public void init(int compression, int samplingRate) {  
        load();   
        open(compression, samplingRate);   
    }  

    /**
     * initClient with DEFAULT_COMPRESSION and DEFAULT_SAMPLING_RATE
     */
    public void init() { 
    	init(DEFAULT_COMPRESSION, DEFAULT_SAMPLING_RATE);   
    }  

    private void load() {  
        try {  
            System.loadLibrary("speex");  
        } catch (Throwable e) {  
            e.printStackTrace();  
        }  

    }  

    /**
     * open the codec
     * @param compression bit rate, see DEFAULT_COMPRESSION
     * @param samplingRate sample rate, see DEFAULT_SAMPLING_RATE
     * @return
     */
    public native int open(int compression, int samplingRate);  
    /**
     * return frame size, means encode shorts pre times
     * @return the frame size
     */
    public native int getFrameSize();  
    /**
     * decode speex bytes to pcm shorts with variable length, return pcm shorts's length
     * @param encoded speex bytes
     * @param encodedOffset speex bytes valid data offset
     * @param sout a shortArray to load decoded pcm shorts
     * @param size speex bytes valid data length
     * @return pcm shorts's length
     */
    public native int decode4Stream(byte encoded[], int encodedOffset, short sout[], int size);  
    /**
     * decode speex bytes to pcm shorts with fixed length, return pcm shorts's length
     * @param encoded speex bytes
     * @param encodedOffset speex bytes valid data offset
     * @param sout a shortArray to load decoded pcm shorts
     * @param size speex bytes valid data length
     * @return pcm shorts's length
     */
    public native int decode4File(byte encoded[], int encodedOffset, short sout[], int size);   
    /**
     * encode pcm shorts to speex bytes, return speex bytes's length 
     * @param sin pcm shorts
     * @param sinOffset pcm shorts valid data offset
     * @param encoded a byteArray to load speex bytes
     * @param encodedOffset speex bytes load offset
     * @param size pcm shorts valid data length
     * @return speex bytes's length, 0 means no data, -1 means @field ERROR_ENCODE_BUFFER_OVERFLOW
     */
    public native int encode(short sin[], int sinOffset, byte encoded[], int encodedOffset, int size);  
    /**
     * close the codec
     */
    public native void close();  
}