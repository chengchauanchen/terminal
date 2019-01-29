package org.easydarwin.push;

import org.easydarwin.muxer.EasyMuxer;

import java.io.IOException;

/**
 * Created by apple on 2017/5/13.
 */

public interface VideoConsumer {
     void onVideoStart(int width, int height) throws IOException;

     int onVideo(byte[] data, int format);

     void onVideoStop();

     void setMuxer(EasyMuxer muxer);
}
