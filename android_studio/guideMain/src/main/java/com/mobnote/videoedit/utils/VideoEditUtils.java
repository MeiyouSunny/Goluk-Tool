package com.mobnote.videoedit.utils;

import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.mobnote.videoedit.bean.ChunkBean;
import com.mobnote.videoedit.bean.ProjectItemBean;
import com.mobnote.videoedit.bean.TransitionBean;
import com.mobnote.videoedit.constant.VideoEditConstant;

import java.util.ArrayList;
import java.util.List;

import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.model.Chunk;
import cn.npnt.ae.model.ChunkThumbs;
import cn.npnt.ae.model.VideoThumb;

public class VideoEditUtils {
	private static long sUniqueTag = 0;

	public static long generateIndexTag() {
//		if(null == list || list.size() == 0) {
//			return 0;
//		} else {
//			return list.size();
//		}
//		return System.currentTimeMillis();
		return sUniqueTag++;
	}

	public static float ChunkWidth2Time(int width, int thumbWidth) {
		int thumbCount = width / thumbWidth;
		float last = (width % thumbWidth) / width;

		return (thumbCount + last) * VideoEditConstant.BITMAP_TIME_INTERVAL;
	}

	public static int ChunkTime2Width(Chunk chunk) {
//		int thumbCount = (int)(time / VideoEditConstant.BITMAP_TIME_INTERVAL);
//		float last = (int)(time % VideoEditConstant.BITMAP_TIME_INTERVAL);
//		return thumbCount * bitmapDefaultWidth
//				+ (int)((last / VideoEditConstant.BITMAP_TIME_INTERVAL) * bitmapDefaultWidth);
		if(chunk == null || null == chunk.getChunkThumbs()) {
			return 0;
		}
//		return (int)(bitmapDefaultWidth / VideoEditConstant.BITMAP_TIME_INTERVAL * time);
//		return (int)((chunk.getChunkThumbs().getLength() - chunk.getChunkThumbs().getBegin()) * bitmapDefaultWidth);
		return (int)(chunk.getChunkThumbs().getLength() * chunk.getChunkThumbs().getThumbWidth());
	}

//	public static int ChunkTime2Width(float time, int bitmapDefaultWidth) {
////		int thumbCount = (int)(time / VideoEditConstant.BITMAP_TIME_INTERVAL);
////		float last = (int)(time % VideoEditConstant.BITMAP_TIME_INTERVAL);
////		return thumbCount * bitmapDefaultWidth
////				+ (int)((last / VideoEditConstant.BITMAP_TIME_INTERVAL) * bitmapDefaultWidth);
//
////		return (int)(bitmapDefaultWidth / VideoEditConstant.BITMAP_TIME_INTERVAL * time);
//		return (int)(bitmapDefaultWidth / VideoEditConstant.BITMAP_TIME_INTERVAL * time);
////		return (int)((chunk.getChunkThumbs().getLength() - chunk.getChunkThumbs().getBegin()) * bitmapDefaultWidth);
//	}

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

	public static boolean judgeChunkOverlap(int chunkX, int itemX, int itemWidth) {
		if(chunkX >= itemX && chunkX <= itemX + itemWidth) {
			return true;
		}

		return false;
	}

    public static boolean judgeChunkOverlap(LinearLayoutManager manager, int gateX, int chunkIndex) {
        // First judge whether chunk visible
        int firstVisibleItem = manager.findFirstVisibleItemPosition();
        int lastVisibleItem = manager.findLastVisibleItemPosition();
        if(chunkIndex < firstVisibleItem || chunkIndex > lastVisibleItem) {
            return false;
        }
        View view = manager.findViewByPosition(chunkIndex);
        int viewWidth = view.getWidth();
        int viewX = getViewXLocation(view);
        if(gateX >= viewX && gateX <= viewX + viewWidth) {
            return true;
        }

        return false;
    }

	public static int getTransitionFromChunk(List<ProjectItemBean> list, int chunkIndex) {
		if(null == list || list.size() == 0) {
			return -1;
		}

		ProjectItemBean itemBean = list.get(chunkIndex);
		int count = list.size();

		if(itemBean instanceof ChunkBean) {
			ChunkBean chunkBean = (ChunkBean)itemBean;
			for(int i = 0; i < count; i++) {
				ProjectItemBean tmpBean = list.get(i);
				if(tmpBean instanceof TransitionBean) {
					TransitionBean transBean = (TransitionBean)tmpBean;
					if(transBean.ct_pair_tag.equals(chunkBean.ct_pair_tag)) {
						return i;
					}
				}
			}
		}

		return -1;
	}

	// Map item index 2 chunk index
	public static int mapI2CIndex(int itemIndex) {
		return (itemIndex - 1) / 2;
	}

	// Map chunk index 2 item index
	public static int mapC2IIndex(int itemIndex) {
		return itemIndex * 2 + 1;
	}

	// remove chunk with project item list
	public static void removeChunk(AfterEffect afterEffect, List<ProjectItemBean> list, int chunkIndex) {
		if(-1 == chunkIndex || null == list) {
			return;
		}

		// remove chunk and transition
		ProjectItemBean bean = list.get(chunkIndex + 1);
		if(bean instanceof TransitionBean) {
			list.remove(chunkIndex + 1);
		}
		list.remove(chunkIndex);
		// Fix Bugly#2455
		if (afterEffect != null)
			afterEffect.editRemoveChunk(mapI2CIndex(chunkIndex));
	}

	public List<Bitmap> getBitmapListFromChunk(Chunk chunk) {
		if(null == chunk) {
			return null;
		}

		List<Bitmap> bitmapList = new ArrayList<Bitmap>();

		ChunkThumbs chunkThumbs = chunk.getChunkThumbs();
		float begin = chunkThumbs.getBegin();
		float end = chunkThumbs.getLength();
		float duration = end - begin;

		List<VideoThumb> videoThumbList = chunkThumbs.getThumbs();
		int beginChunk = (int)begin;
		int endChunk = (int)end;
//		if() {
//			
//		}

		return bitmapList;
	}

	public static void refreshCTTag(List<ProjectItemBean> list) {
		if(null == list) {
			return;
		}

		int count = list.size();
		for(int i = 0; i < count; i++) {
			ProjectItemBean bean = list.get(i);
			if(bean instanceof ChunkBean) {
				ChunkBean chunkBean = (ChunkBean)bean;
				chunkBean.ct_pair_tag = i + "chunkIndex";
			}

			if(bean instanceof TransitionBean) {
				TransitionBean transBean = (TransitionBean)bean;
				transBean.ct_pair_tag = (i - 1) + "chunkIndex";
			}
		}
	}
}
