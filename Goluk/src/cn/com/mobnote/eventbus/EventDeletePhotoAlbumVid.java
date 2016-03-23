package cn.com.mobnote.eventbus;

/**
 * 删除相册视频event
 * @author uestc
 *
 */
public class EventDeletePhotoAlbumVid {
	
	private String mVidPath;
	private int mType;
	
	public EventDeletePhotoAlbumVid(String path,int type) {
		mVidPath = path;
		mType = type;
	}

	public String getVidPath() {
		return mVidPath;
	}

	public int getType() {
		return mType;
	}
	
}
