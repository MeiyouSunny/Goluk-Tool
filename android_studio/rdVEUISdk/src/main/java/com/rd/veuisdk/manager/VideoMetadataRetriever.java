package com.rd.veuisdk.manager;


import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.VideoConfig;

/**
 * 视频元信息检索类
 */
public class VideoMetadataRetriever {

    /**
     * 检索是否防修改录制视频
     */
    public static final int METADATA_KEY_IS_SUPPORT_ANTI_CHANGE = 0;
    /**
     * 检索视频码率
     */
    public static final int METADATA_KEY_VIDEO_BIT_RATE = 1;
    /**
     * 检索视频宽度
     */
    public static final int METADATA_KEY_VIDEO_WIDHT = 2;
    /**
     * 检索视频高度
     */
    public static final int METADATA_KEY_VIDEO_HEIGHT = 3;
    /**
     * 检索视频帧率
     */
    public static final int METADATA_KEY_VIDEO_FRAME_RATE = 4;
    /**
     * 检索视频时长
     */
    public static final int METADATA_KEY_VIDEO_DURATION = 5;

    private String mPath;
    private VideoConfig mVideoConfig;
    private float mDuration;

    public void setDataSource(String path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException();
        }
        this.mPath = path;
        mVideoConfig = new VideoConfig();
        mDuration = VirtualVideo.getMediaInfo(path, mVideoConfig, true);
    }

    public String extractMetadata(int metadataKey) {
        String str = "";
        if (metadataKey == METADATA_KEY_IS_SUPPORT_ANTI_CHANGE) {
            int encypyReturnCode = RecorderCore.apiIsRDEncyptVideo(mPath);
            if (encypyReturnCode == 1) {
                str = "yes";
            } else {
                str = "no";
            }
        } else if (metadataKey == METADATA_KEY_VIDEO_BIT_RATE) {
            str = mVideoConfig.getVideoEncodingBitRate() + "";
        } else if (metadataKey == METADATA_KEY_VIDEO_WIDHT) {
            str = mVideoConfig.getVideoWidth() + "";
        } else if (metadataKey == METADATA_KEY_VIDEO_HEIGHT) {
            str = mVideoConfig.getVideoHeight() + "";
        } else if (metadataKey == METADATA_KEY_VIDEO_FRAME_RATE) {
            str = mVideoConfig.getVideoFrameRate() + "";
        } else if (metadataKey == METADATA_KEY_VIDEO_DURATION) {
            str = mDuration + "";
        }
        return str;
    }
}
