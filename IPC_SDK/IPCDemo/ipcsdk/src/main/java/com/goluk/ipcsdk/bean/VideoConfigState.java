package com.goluk.ipcsdk.bean;

/**
 * T3 VideoConfig
 */
public class VideoConfigState {
    /**
     * 0:主码流，1:子码流
     */
    public int bitstreams;
    /**
     * 分辨率
     */
    public String resolution;
    /**
     * 帧数：fps
     */
    public int frameRate;
    /**
     * 码率:kbits/s
     */
    public int bitrate;
    /**
     * 1:有音频，0:没有音频
     */
    public int AudioEnabled;
}
