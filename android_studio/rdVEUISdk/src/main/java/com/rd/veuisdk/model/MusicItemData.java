package com.rd.veuisdk.model;

public class MusicItemData {

	/** 音乐标题 */
	private String title;

	/** 音乐路径路径 */
	private String path;

	/** 音频时长 */
	private long duration;

	/** 配音比例 */
	private int musicTrackFactor = 50;

	/** 开始位置 */
	private long startTrackPosition = 0;

	/** 结束位置 */
	private long endTrackPosition = 0;

	/** 是否被选中 */
	private boolean isSelected = false;

	public MusicItemData() {
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTitle() {
		return title;
	}

	public String getPath() {
		return path;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public int getMusicTrackFactor() {
		return musicTrackFactor;
	}

	public void setMusicTrackFactor(int musicTrackFactor) {
		this.musicTrackFactor = musicTrackFactor;
	}

	public long getStartTrackPosition() {
		return startTrackPosition;
	}

	public void setStartTrackPosition(long startTrackPosition) {
		this.startTrackPosition = startTrackPosition;
	}

	public long getEndTrackPosition() {
		return endTrackPosition;
	}

	public void setEndTrackPosition(long endTrackPosition) {
		this.endTrackPosition = endTrackPosition;
	}
}
