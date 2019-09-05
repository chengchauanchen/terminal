package org.easydarwin.push;

import org.easydarwin.muxer.UAVEasyMuxer;

import java.io.IOException;

/**
 * 作者：ly-xuxiaolong
 * 版本：1.0
 * 创建日期：2019/3/22
 * 描述：
 * 修订历史：
 */
public interface UAVVideoConSumer{
    public void onVideoStart(int width, int height) throws IOException;

    public int onVideo(byte[] data, int format);

    public void onVideoStop();

    public void setMuxer(UAVEasyMuxer muxer);
}
