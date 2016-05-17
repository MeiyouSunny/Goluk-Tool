package com.mobnote.videoedit.constant;

public class VideoEditConstant {
	public static final int BITMAP_TIME_INTERVAL = 3;
	// common bitmap width of dp
	public static final int BITMAP_COMMON_WIDTH = 45;
	public static final int BITMAP_COMMON_HEIGHT = 45;
	public static final int DUMMY_HEADER_WIDTH = 64;
	// width for transition of dp
	public static final int TRANSITION_COMMON_WIDTH = 20;

	// below for test only
	public static final String VIDEO_PATH_1 = "/storage/emulated/0/goluk/video/wonderful/WND_event_20160406121432_1_TX_3_0012.mp4";
	public static final String VIDEO_PATH = "/storage/emulated/0/goluk/video/wonderful/WND_event_20160406204409_1_TX_3_0012.mp4";
	public static final String MUSIC_PATH = "/storage/emulated/0/qqmusic/song/500miles.mp3";

	public static final int DEFAULT_EXPORT_WIDTH = 865;
	public static final int DEFAULT_EXPORT_HEIGHT = 480;
	/**
	 * 1mbps
	 */
	public static final int DEFAULT_EXPORT_BITRATE = 1000000;
	public static final float DEFAULT_FPS = 25;

	public static final String TAG_EXPORT_MANAGER = "AEEXPORT_MANAGER";
	public static final String TAG_EXPORT_ENCODER = "AEEXPORT_ENCODER";
	public static final String EXPORT_FOLDER_NAME = "/goluk_export_video";

	public static final float MIN_VIDEO_DURATION = 10f;
	public static final float MAX_VIDEO_DURATION = 90f;

	public static final int EXPORT_1080P_WIDTH = 1920;
	public static final int EXPORT_1080P_HEIGHT = 1080;

	public static final int EXPORT_HD720P_WIDTH = 1280;
	public static final int EXPORT_HD720P_HEIGHT = 720;

	public static final int EXPORT_480P_WIDTH = 848;
	public static final int EXPORT_480P_HEIGHT = 480;

    public static final float VIDEO_TAIL_TIME_DURATION = 2.6f;

    // dp
    public static final int VIDEO_TAIL_WIDTH = 80;

    public static final int VIDEO_VOLUME_MAX = 300;
}
