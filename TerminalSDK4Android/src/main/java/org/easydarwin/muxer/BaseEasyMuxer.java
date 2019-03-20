package org.easydarwin.muxer;

import android.media.MediaFormat;

public interface BaseEasyMuxer {
    public  void addTrack(MediaFormat format, boolean isVideo);
}
