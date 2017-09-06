package com.rd.veuisdk.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import com.rd.vecore.models.Scene;

import java.util.ArrayList;

public abstract class VideoSelectorAdapter extends BaseAdapter {
	protected ArrayList<Scene> mArrItems = new ArrayList<Scene>();
	protected LayoutInflater mInflater;
	private Handler mMainHandler = new Handler(Looper.getMainLooper());

	public VideoSelectorAdapter(LayoutInflater inflater) {
		mInflater = inflater;
	}

	public ArrayList<Scene> getMediaList() {
		return mArrItems;
	}

	public boolean addItem(Scene scene) {
		return mArrItems.add(scene);

	}

	public void addItem(int index, Scene scene) {
		mArrItems.add(index, scene);
	}

	/**
	 * 根据索引值删除指定item
	 * 
	 * @param nItemIndex
	 * @return
	 */
	public Scene removeItem(int nItemIndex) {
		if (nItemIndex >= 0 && nItemIndex < this.getCount()) {
			Scene scene = this.getItem(nItemIndex);
			if (removeItem(scene)) {
				return scene;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean removeItem(Scene removeItem) {
		return mArrItems.remove(removeItem);

	}

	public void updateDisplay() {
		mMainHandler.post(new Runnable() {

			@Override
			public void run() {
				notifyDataSetChanged();
			}
		});
	}

	/**
	 * 清理操作
	 */
	public void clear() {

		mArrItems.clear();
		System.gc();
	}

	@Override
	public int getCount() {
		return mArrItems.size();
	}
	

	@Override
	public Scene getItem(int position) {
		if (position >= 0 && position < getCount()) {
			return mArrItems.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
