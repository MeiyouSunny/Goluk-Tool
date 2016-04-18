package com.goluk.videoedit.utils;

import java.util.List;

import android.view.View;

import com.goluk.videoedit.bean.ChunkBean;
import com.goluk.videoedit.bean.ProjectItemBean;
import com.goluk.videoedit.constant.VideoEditConstant;

public class VideoEditUtils {

	public static int generateIndexTag(List<ProjectItemBean> list) {
		if(null == list || list.size() == 0) {
			return 0;
		} else {
			return list.size();
		}
	}

	public static float ChunkWidth2Time(int width, int thumbWidth) {
		int thumbCount = width / thumbWidth;
		float last = (width % thumbWidth) / width;

		return (thumbCount + last) * VideoEditConstant.BITMAP_TIME_INTERVAL;
	}

	public static int ChunkTime2Width(float time, int bitmapDefaultWidth) {
//		int thumbCount = (int)(time / VideoEditConstant.BITMAP_TIME_INTERVAL);
//		float last = (int)(time % VideoEditConstant.BITMAP_TIME_INTERVAL);
//		return thumbCount * bitmapDefaultWidth
//				+ (int)((last / VideoEditConstant.BITMAP_TIME_INTERVAL) * bitmapDefaultWidth);
		return (int)(bitmapDefaultWidth / VideoEditConstant.BITMAP_TIME_INTERVAL * time);
	}

	private static final float TIME_ADJUST = 0.1f;

	public static boolean judgeChunkEnd(float totalTime, float curTime, List<ProjectItemBean> list) {
		if(null == list || list.size() == 0 || curTime >= totalTime || curTime == 0f) {
			return false;
		}
		int count = list.size();

		for(int i = 0; i < count; i++) {
			ProjectItemBean itemBean = list.get(i);
			if(itemBean instanceof ChunkBean) {
				ChunkBean chunkBean = (ChunkBean)itemBean;
				float delta = curTime - chunkBean.chunk.getDuration();
				if(Math.abs(delta) <= TIME_ADJUST) {
					return true;
				}
			}
		}

		return false;
	}

	public static int getViewXLocation(View view) {
		int[] location = new int[2];
		view.getLocationInWindow(location);
		int x = location[0];
		int y = location[1];
		return x;
	}

	public static boolean judgeGateOverlap(int gateX, int itemX, int itemWidth) {
		if(gateX >= itemX && gateX <= itemX + itemWidth) {
			return true;
		}

		return false;
	}
}
