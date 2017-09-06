package com.rd.veuisdk.model;

public class DoodleEntity {

    private static int doodleId = -1;
    private int startPosition = 0;
    private int endPosition = 0;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int startTime = 0;
    private int endTime = 0;
    private String imagePaht = null;

    public DoodleEntity() {
	doodleId++;

	startTime = 2000 * doodleId;

	endTime = startTime + 1000;
    }

    public int getStartPosition() {
	return startPosition;
    }

    public void setStartPosition(int startPosition) {
	this.startPosition = startPosition;
    }

    public int getEndPosition() {
	return endPosition;
    }

    public void setEndPosition(int endPosition) {
	this.endPosition = endPosition;
    }

    public int getImageWidth() {
	return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
	this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
	return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
	this.imageHeight = imageHeight;
    }

    public String getImagePaht() {
	return imagePaht;
    }

    public void setImagePaht(String imagePaht) {
	this.imagePaht = imagePaht;
    }

    public int getStartTime() {
	return startTime;
    }

    public void setStartTime(int startTime) {
	this.startTime = startTime;
    }

    public int getEndTime() {
	return endTime;
    }

    public void setEndTime(int endTime) {
	this.endTime = endTime;
    }

    public int getDoodleId() {
	return doodleId;
    }
}
