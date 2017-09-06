package com.rd.veuisdk.model;

public class FilterItemInfo {

    public CustomData1 getData() {
	return data;
    }

    public void setData(CustomData1 data) {
	this.data = data;
    }

    public int getnItemId() {
	return nItemId;
    }

    public void setnItemId(int nItemId) {
	this.nItemId = nItemId;
    }

    public FilterItemInfo(CustomData1 data, int nItemId) {
	this.data = data;
	this.nItemId = nItemId;
    }

    private CustomData1 data;
    private int nItemId;

}
