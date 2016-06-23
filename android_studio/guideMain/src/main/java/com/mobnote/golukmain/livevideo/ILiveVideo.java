package com.mobnote.golukmain.livevideo;

import android.os.Bundle;

public interface ILiveVideo {
	
	public void initMap(Bundle bundle);
	
	public void drawPersonsHead();

	public void drawMyLocation();
	
	public void drawMyPosition(double lon, double lat, double radius);
	
	public void toMyLocation();

}
