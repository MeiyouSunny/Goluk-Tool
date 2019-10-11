package com.mobnote.eventbus;

/**
 * 删除相册视频event
 *
 * @author uestc
 */
public class EventDeletePhotoAlbumVid {

    private String mVidPath;
    private String mRelativePath;
    private int mType;

    public EventDeletePhotoAlbumVid(String path, int type) {
        mVidPath = path;
        mType = type;
    }

    public EventDeletePhotoAlbumVid(String path, String relativePath, int type) {
        mVidPath = path;
        mRelativePath = relativePath;
        mType = type;
    }

    public String getVidPath() {
        return mVidPath;
    }

    public String getRelativePath() {
        return mRelativePath;
    }

    public int getType() {
        return mType;
    }

}
