package com.rd.veuisdk.model;

import android.annotation.SuppressLint;

import com.rd.veuisdk.adapter.HorizontalListAdapter;

public class RelativeMap {

    private static RelativeMap instance;

    /** 链表适配器 */
    private HorizontalListAdapter adapter1;

    /** 链表适配器 */
    private HorizontalListAdapter adapter2;

    @SuppressLint("UseSparseArrays")
    private RelativeMap() {

    }

    public static RelativeMap getInstance() {

	if (instance == null) {
	    instance = new RelativeMap();
	}

	return instance;
    }

    public HorizontalListAdapter getAdapter1() {
	return adapter1;
    }

    public void setAdapter1(HorizontalListAdapter adapter1) {
	this.adapter1 = adapter1;
    }

    public HorizontalListAdapter getAdapter2() {
	return adapter2;
    }

    public void setAdapter2(HorizontalListAdapter adapter2) {
	this.adapter2 = adapter2;
    }

}
