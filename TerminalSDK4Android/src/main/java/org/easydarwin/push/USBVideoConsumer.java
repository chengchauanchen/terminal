package org.easydarwin.push;

import org.easydarwin.muxer.EasyMuxer;

import java.io.IOException;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2018/9/11
 * 描述：
 * 修订历史：
 */

public interface USBVideoConsumer {
     void onVideoStart(int width, int height) throws IOException;

     int onVideo(byte[] data, int format);

     void onVideoStop();

     void setMuxer(EasyMuxer muxer);
}
