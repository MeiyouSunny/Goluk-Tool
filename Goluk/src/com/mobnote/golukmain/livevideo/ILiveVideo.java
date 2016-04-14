package com.mobnote.golukmain.livevideo;

public interface ILiveVideo {
	
	public void initMap();
	
	public void drawPersonsHead();

	public void drawMyLocation();
	
	public void drawMyPosition(double lon, double lat, double radius);
	
	public void toMyLocation();

}
