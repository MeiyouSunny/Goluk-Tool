package com.mobnote.map;

public interface IMapTools {

	public void updatePosition(String aid, double lon, double lat, boolean isNeedMapCenter);

	public void addSinglePoint(String pointStr, boolean isNeedMapCenter);
}
