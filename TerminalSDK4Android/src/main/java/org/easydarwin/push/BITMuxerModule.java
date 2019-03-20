package org.easydarwin.push;

import org.easydarwin.audio.BITAudioStream;

import dagger.Component;

/**
 * Created by apple on 2017/5/13.
 */
@Component(modules = BITMediaStream.class)
public interface BITMuxerModule {
    void inject(BITHWConsumer consumer);
    void inject(BITAudioStream MedisStream);
}

