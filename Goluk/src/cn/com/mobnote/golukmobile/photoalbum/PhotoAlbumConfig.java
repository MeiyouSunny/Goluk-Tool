package cn.com.mobnote.golukmobile.photoalbum;

/**
 * 相册配置类，所有相册相关的固定配置请写在这里
 * @author leege100
 *
 */
public class PhotoAlbumConfig {
	
	/***相册本地所有类型视频*/
	public static final int PHOTO_BUM_LOCAL = 0;
	/***相册远程精彩视频*/
	public static final int PHOTO_BUM_IPC_WND = 1;
	/***相册远程紧急视频*/
	public static final int PHOTO_BUM_IPC_URG = 2;
	/***相册远程循环视频*/
	public static final int PHOTO_BUM_IPC_LOOP = 3;
	
	/**
	 * 本地文件存储目录
	 */
	public static final String LOCAL_LOOP_VIDEO_PATH = "fs1:/video/loop/";
	public static final String LOCAL_WND_VIDEO_PATH = "fs1:/video/wonderful/";
	public static final String LOCAL_URG_VIDEO_PATH = "fs1:/video/urgent/";
	
}
