package cn.com.mobnote.entity;

import java.util.Date;

import android.graphics.Bitmap;
import com.rd.car.modal.IVideoInfo;

public class VideoItem implements IVideoInfo {
	
	private static final long serialVersionUID = 1L;
	/** 视频id */
	private String id;
	/** 文件id */
	private String fileId;
	/** 视频标题 */
	private String title;
	
	/** 视频描述 */
	private String description;
	
	/** 视频缩略图地址 */
	private String thumbnailUrl;
	
	/** 视频链接地址 */
	private String videoUrl;
	
	/** 上传时间 */
	private String uploadTime;
	
	/** 视频时长 */
	private String videoTime;
	
	/** 视频大小 （字节） */
	private long videoSize;
	
	/** 视频路径 */
	private String videoPath;
	
	/** 视频缩略图 */
	private Bitmap videoThumbnail;
	
	public VideoItem() {
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	
	public String getVideoUrl() {
		return videoUrl;
	}
	
	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
	
	public String getUploadTime() {
		return uploadTime;
	}
	
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	
	public String getVideoTime() {
		return videoTime;
	}
	
	public void setVideoTime(String videoTime) {
		this.videoTime = videoTime;
	}
	
	public long getVideoSize() {
		return videoSize;
	}
	
	public void setVideoSize(long videoSize) {
		this.videoSize = videoSize;
	}
	
	public void setVideoPath(String path) {
		this.videoPath = path;
	}
	
	public Bitmap getVideoThumbnail() {
		return videoThumbnail;
	}
	
	public void setVideoThumbnail(Bitmap videoThumbnail) {
		this.videoThumbnail = videoThumbnail;
	}

	public String toString() {
		return "[VideoItem: " + "id=" + id + ",   fileId= " + fileId
			+ ",   title=" + title + ",   description=" + description
			+ ",   thumbnailUrl=" + thumbnailUrl + ",   videoUrl="
			+ videoUrl + ",   uptime=" + uploadTime + ",   videoTime"
			+ videoTime + ",   videoSize=" + videoSize + ",videoPath="
			+ videoPath + ",duration=" + duration + ",type=" + type
			+ ",width=" + width + "+" + "]";
	}

	@Override
	public String getVideoPath() {
		// TODO Auto-generated method stub
		return videoPath;
	}

	/** 视频的宽高 */
	private int width, height;
	/** 持续时间 */
	private int duration;
	/** 视频开始开始录制时间 */
	private Date videoStart;
	/** 视频开始结束时间 */
	private Date videoEnd;
	/** 需要截取的部分在该视频段的开始时间 */
	private int trimStart;
	/** 需要截取的部分在该视频段的结束时间 */
	private int trimEnd;
	/** 帧率 */
	private int frameRate;
	/** 码率 */
	private int encodingBitRate;
	/** 视频样式 */
	private int type;

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return width;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return height;
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return duration;
	}

	@Override
	public Date getVideoStart() {
		// TODO Auto-generated method stub
		return videoStart;
	}

	@Override
	public Date getVideoEnd() {
		// TODO Auto-generated method stub
		return videoEnd;
	}

	@Override
	public int getTrimStart() {
		// TODO Auto-generated method stub
		return trimStart;
	}
	
	@Override
	public int getTrimEnd() {
		// TODO Auto-generated method stub
		return trimEnd;
	}
	
	@Override
	public int getFrameRate() {
		// TODO Auto-generated method stub
		return frameRate;
	}
	
	@Override
	public int getEncodingBitRate() {
		// TODO Auto-generated method stub
		return encodingBitRate;
	}
	
	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return type;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public void setVideoStart(Date videoStart) {
		this.videoStart = videoStart;
	}
	
	public void setVideoEnd(Date videoEnd) {
		this.videoEnd = videoEnd;
	}
	
	public void setTrimStart(int trimStart) {
		this.trimStart = trimStart;
	}
	
	public void setTrimEnd(int trimEnd) {
		this.trimEnd = trimEnd;
	}
	
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	
	public void setEncodingBitRate(int encodingBitRate) {
		this.encodingBitRate = encodingBitRate;
	}
	
	public void setType(int type) {
		this.type = type;
	}
}
