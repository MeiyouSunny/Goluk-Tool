package com.rd.veuisdk;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;

import com.rd.recorder.api.RecorderCore;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.ui.HorizontalListViewCamera;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 摄像头特效滤镜handler
 * 
 * @author abreal
 * 
 */
class CameraEffectHandler {
	private ArrayList<String> m_listInternalColorEffects;
	private ArrayList<String> m_listEffectCaptain;
	private Context context;

	public CameraEffectHandler(Context c) {
		m_listInternalColorEffects = new ArrayList<String>();
		m_listEffectCaptain = new ArrayList<String>();
		this.context = c;
	}

	void recycle() {
		if (null != m_listEffectCaptain) {
			m_listEffectCaptain.clear();
			m_listEffectCaptain = null;
		}
		if (null != m_listInternalColorEffects) {
			m_listInternalColorEffects.clear();
			m_listInternalColorEffects = null;
		}

	}

	private class FilterItem {
		// public FilterItem(int drawId, int strId, String effct) {
		// this.drawId = drawId;
		// // this.strId = strId;
		// this.effect = effct;
		// }
		public FilterItem(int drawId, String str, String effct) {
			this.drawId = drawId;
			this.str = str;
			this.effect = effct;
		}

		int drawId;
		String str;
		String effect;
	}

	class Str2IntComparator implements Comparator<String> {
		private boolean reverseOrder; // 是否倒序

		public Str2IntComparator(boolean reverseOrder) {
			this.reverseOrder = reverseOrder;
		}

		public int compare(String arg0, String arg1) {
			if (reverseOrder)
				return Integer.parseInt(arg1) - Integer.parseInt(arg0);
			else
				return Integer.parseInt(arg0) - Integer.parseInt(arg1);
		}
	}

	private void initEffects(HorizontalListViewCamera lvCameraFilter,
			List<String> supportedColorEffects) {
		ArrayList<FilterItem> list = new ArrayList<CameraEffectHandler.FilterItem>();
		Collections.sort(supportedColorEffects, new Str2IntComparator(false));

		Resources res = lvCameraFilter.getContext().getResources();
		// res.getString(R.string.camare_effect_0)
		list.add(new FilterItem(R.drawable.camera_effect_0, res
				.getString(R.string.camera_effect_0), supportedColorEffects
				.get(0)));
		// list.add(new FilterItem(R.drawable.camera_effect_5,
		// "黑白", supportedColorEffects.get(1)));
		//
		//
		// list.add(new FilterItem(R.drawable.camera_effect_6,
		// "怀旧", supportedColorEffects.get(2)));
		// list.add(new FilterItem(R.drawable.camera_effect_7,
		// "冷色", supportedColorEffects.get(3)));
		// list.add(new FilterItem(R.drawable.camera_effect_8,
		// "暖色", supportedColorEffects.get(4)));

		list.add(new FilterItem(R.drawable.camera_effect_5, res
				.getString(R.string.camera_effect_5), supportedColorEffects
				.get(5)));
		list.add(new FilterItem(R.drawable.camera_effect_6, res
				.getString(R.string.camera_effect_6), supportedColorEffects
				.get(6)));
		list.add(new FilterItem(R.drawable.camera_effect_7, res
				.getString(R.string.camera_effect_7), supportedColorEffects
				.get(7)));
		list.add(new FilterItem(R.drawable.camera_effect_8, res
				.getString(R.string.camera_effect_8), supportedColorEffects
				.get(8)));
		list.add(new FilterItem(R.drawable.camera_effect_9, res
				.getString(R.string.camera_effect_9), supportedColorEffects
				.get(9)));
		list.add(new FilterItem(R.drawable.camera_effect_10, res
				.getString(R.string.camera_effect_10), supportedColorEffects
				.get(10)));
		list.add(new FilterItem(R.drawable.camera_effect_11, res
				.getString(R.string.camera_effect_11), supportedColorEffects
				.get(11)));
		list.add(new FilterItem(R.drawable.camera_effect_12, res
				.getString(R.string.camera_effect_12), supportedColorEffects
				.get(12)));
		list.add(new FilterItem(R.drawable.camera_effect_13, res
				.getString(R.string.camera_effect_13), supportedColorEffects
				.get(13)));
		list.add(new FilterItem(R.drawable.camera_effect_14, res
				.getString(R.string.camera_effect_14), supportedColorEffects
				.get(14)));

		list.add(new FilterItem(R.drawable.camera_effect_15, res
				.getString(R.string.camera_effect_15), supportedColorEffects
				.get(15)));
		list.add(new FilterItem(R.drawable.camera_effect_16, res
				.getString(R.string.camera_effect_16), supportedColorEffects
				.get(16)));
		list.add(new FilterItem(R.drawable.camera_effect_17, res
				.getString(R.string.camera_effect_17), supportedColorEffects
				.get(17)));
		list.add(new FilterItem(R.drawable.camera_effect_18, res
				.getString(R.string.camera_effect_18), supportedColorEffects
				.get(18)));
		list.add(new FilterItem(R.drawable.camera_effect_19, res
				.getString(R.string.camera_effect_19), supportedColorEffects
				.get(19)));
		list.add(new FilterItem(R.drawable.camera_effect_20, res
				.getString(R.string.camera_effect_20), supportedColorEffects
				.get(20)));
		list.add(new FilterItem(R.drawable.camera_effect_21, res
				.getString(R.string.camera_effect_21), supportedColorEffects
				.get(21)));

		list.add(new FilterItem(R.drawable.camera_effect_22, res
				.getString(R.string.camera_effect_22), supportedColorEffects
				.get(22)));
		list.add(new FilterItem(R.drawable.camera_effect_23, res
				.getString(R.string.camera_effect_23), supportedColorEffects
				.get(23)));
		list.add(new FilterItem(R.drawable.camera_effect_24, res
				.getString(R.string.camera_effect_24), supportedColorEffects
				.get(24)));
		list.add(new FilterItem(R.drawable.camera_effect_25, res
				.getString(R.string.camera_effect_25), supportedColorEffects
				.get(25)));
		list.add(new FilterItem(R.drawable.camera_effect_26, res
				.getString(R.string.camera_effect_26), supportedColorEffects
				.get(26)));

		list.add(new FilterItem(R.drawable.camera_effect_27, res
				.getString(R.string.camera_effect_27), supportedColorEffects
				.get(27)));
		list.add(new FilterItem(R.drawable.camera_effect_28, res
				.getString(R.string.camera_effect_28), supportedColorEffects
				.get(28)));
		list.add(new FilterItem(R.drawable.camera_effect_29, res
				.getString(R.string.camera_effect_29), supportedColorEffects
				.get(29)));
		list.add(new FilterItem(R.drawable.camera_effect_30, res
				.getString(R.string.camera_effect_30), supportedColorEffects
				.get(30)));
		list.add(new FilterItem(R.drawable.camera_effect_31, res
				.getString(R.string.camera_effect_31), supportedColorEffects
				.get(31)));
		list.add(new FilterItem(R.drawable.camera_effect_32, res
				.getString(R.string.camera_effect_32), supportedColorEffects
				.get(32)));

		list.add(new FilterItem(R.drawable.camera_effect_33, res
				.getString(R.string.camera_effect_33), supportedColorEffects
				.get(33)));
		list.add(new FilterItem(R.drawable.camera_effect_34, res
				.getString(R.string.camera_effect_34), supportedColorEffects
				.get(34)));
		list.add(new FilterItem(R.drawable.camera_effect_35, res
				.getString(R.string.camera_effect_35), supportedColorEffects
				.get(35)));
		list.add(new FilterItem(R.drawable.camera_effect_36, res
				.getString(R.string.camera_effect_36), supportedColorEffects
				.get(36)));
		list.add(new FilterItem(R.drawable.camera_effect_37, res
				.getString(R.string.camera_effect_37), supportedColorEffects
				.get(37)));
		list.add(new FilterItem(R.drawable.camera_effect_38, res
				.getString(R.string.camera_effect_38), supportedColorEffects
				.get(38)));

		int len = list.size();
		FilterItem item;
		for (int i = 0; i < len; i++) {
			item = list.get(i);
			m_listInternalColorEffects.add(item.effect);
			lvCameraFilter.addListItem(i, item.drawId, item.str);
			m_listEffectCaptain.add(item.str);

		}
	}

	/**
	 * 初始并刷新所有特效滤镜
	 * 
	 * @param lvCameraFilter
	 * @param supportedColorEffects
	 */
	public void initAllEffects(HorizontalListViewCamera lvCameraFilter,
			List<String> supportedColorEffects) {
		lvCameraFilter.removeAllListItem();
		int nItemId = 0;

		m_listInternalColorEffects.clear();
		m_listEffectCaptain.clear();

		if (RecorderCore.isSupportBeautify()) {
			initEffects(lvCameraFilter, supportedColorEffects);
		} else {
			m_listInternalColorEffects.add(Camera.Parameters.EFFECT_NONE);
			lvCameraFilter.addListItem(nItemId++, R.drawable.camera_filter_,
					context.getString(R.string.camera_filter_));
			m_listEffectCaptain.add(context.getString(R.string.camera_filter_));
			Log.i("supported filter", "---------begin  test----------");
			if (Utils.isSupported(Camera.Parameters.EFFECT_MONO,
					supportedColorEffects)) {
				m_listInternalColorEffects.add(Camera.Parameters.EFFECT_MONO);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_mono,
						context.getString(R.string.camera_filter_mono));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_mono));
				Log.i("supported filter", "mono");
			}
			if (Utils.isSupported(Camera.Parameters.EFFECT_POSTERIZE,
					supportedColorEffects)) {
				m_listInternalColorEffects
						.add(Camera.Parameters.EFFECT_POSTERIZE);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_posterize,
						context.getString(R.string.camera_filter_posterize));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_posterize));
				Log.i("supported filter", "posterize");
			}
			if (Utils.isSupported(Camera.Parameters.EFFECT_SEPIA,
					supportedColorEffects)) {
				m_listInternalColorEffects.add(Camera.Parameters.EFFECT_SEPIA);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_sepia,
						context.getString(R.string.camera_filter_sepia));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_sepia));
				Log.i("supported filter", "sepia");
			}
			if (Utils.isSupported(Camera.Parameters.EFFECT_NEGATIVE,
					supportedColorEffects)) {
				m_listInternalColorEffects
						.add(Camera.Parameters.EFFECT_NEGATIVE);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_negative,
						context.getString(R.string.camera_filter_negative));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_negative));
				Log.i("supported filter", "negative");
			}

			if (Utils.isSupported(Camera.Parameters.EFFECT_SOLARIZE,
					supportedColorEffects)) {
				m_listInternalColorEffects
						.add(Camera.Parameters.EFFECT_SOLARIZE);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_solarize,
						context.getString(R.string.camera_filter_solarize));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_solarize));
				Log.i("supported filter", "solarize");
			}
			if (Utils.isSupported(Camera.Parameters.EFFECT_WHITEBOARD,
					supportedColorEffects)) {
				m_listInternalColorEffects
						.add(Camera.Parameters.EFFECT_WHITEBOARD);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_whiteboard,
						context.getString(R.string.camera_filter_whiteboard));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_whiteboard));
				Log.i("supported filter", "whiteboard");
			}
			if (Utils.isSupported(Camera.Parameters.EFFECT_AQUA,
					supportedColorEffects)) {
				m_listInternalColorEffects.add(Camera.Parameters.EFFECT_AQUA);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_aqua,
						context.getString(R.string.camera_filter_aqua));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_aqua));
				Log.i("supported filter", "aqua");
			}
			if (Utils.isSupported(Camera.Parameters.EFFECT_BLACKBOARD,
					supportedColorEffects)) {
				m_listInternalColorEffects
						.add(Camera.Parameters.EFFECT_BLACKBOARD);
				lvCameraFilter.addListItem(nItemId++,
						R.drawable.camera_filter_blackboard,
						context.getString(R.string.camera_filter_blackboard));
				m_listEffectCaptain.add(context
						.getString(R.string.camera_filter_blackboard));
				Log.i("supported filter", "blackboard");
			}

		}
		Log.i("supported filter", "---------finish  test----------");
	}

	/**
	 * 获取系统内置特效滤镜
	 * 
	 * @param nItemIndex
	 * @return
	 */
	public String getInternalColorEffectByItemId(int nItemIndex) {
		if (nItemIndex >= 0 && nItemIndex < m_listInternalColorEffects.size()) {
			return m_listInternalColorEffects.get(nItemIndex);
		} else {
			return Camera.Parameters.EFFECT_NONE;
		}
	}

	/**
	 * 获取特效captain
	 * 
	 * @param i
	 * @return
	 */
	public String getEffectCaptains(int i) {
		if (m_listEffectCaptain.size() > i) {
			return m_listEffectCaptain.get(i);
		} else {
			return null;
		}
	}

	/**
	 *
	 * @return
	 */
	public int getEffectCount(){
		return  m_listEffectCaptain.size();
	}
}
