package cn.com.mobnote.golukmobile.photoalbum;

/**
 * 相册配置类，所有相册相关的固定配置请写在这里
 * @author uestc
 *
 */
public class PhotoAlbumConfig {
	
	public static final int PHOTO_BUM_LOCAL = 0;
	public static final int PHOTO_BUM_IPC_WND = 1;
	public static final int PHOTO_BUM_IPC_URG = 2;
	public static final int PHOTO_BUM_IPC_LOOP = 3;
	
	public static final String LOCAL_LOOP_VIDEO_PATH = "fs1:/video/loop/";
	public static final String LOCAL_WND_VIDEO_PATH = "fs1:/video/wonderful/";
	public static final String LOCAL_URG_VIDEO_PATH = "fs1:/video/urgent/";
	
}
