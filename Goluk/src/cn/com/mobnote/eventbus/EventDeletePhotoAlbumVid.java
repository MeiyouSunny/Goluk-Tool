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

	public String getmVidPath() {
		return mVidPath;
	}

	public void setmVidPath(String mVidPath) {
		this.mVidPath = mVidPath;
	}

	public int getmType() {
		return mType;
	}

	public void setmType(int mType) {
		this.mType = mType;
	}
	
	

}
