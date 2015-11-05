package cn.com.mobnote.golukmobile.videosuqare;

/**
 *
 * 视频广场数据信息
 *
 * 2015年4月14日
 *
 * @author xuhw
 */
public class VideoSquareInfo {
	/** 视频广场视频属性信息 */
	public VideoEntity mVideoEntity = null;
	/** 视频广场用户属性信息 */
	public UserEntity mUserEntity = null;
	/** 播放器播放状态 */
	public PlayerState mPlayerState = PlayerState.noallow;
	/** 唯一标识 */
	public String id;
}
