package com.rd.veuisdk.utils;

import java.util.ArrayList;

import android.content.Context;

import com.rd.xpk.editor.modal.ImageObject;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.HorizontalListAdapter;
import com.rd.veuisdk.model.CustomData1;
import com.rd.veuisdk.model.FilterGroupHorizontalistListItem;
import com.rd.veuisdk.model.FilterItemInfo;
import com.rd.veuisdk.model.HorizontalListItem.OnFilterClickListener;
import com.rd.veuisdk.model.MyFilterHorizontalListItem;

public class FilterUtils {

    public static void initFilter(HorizontalListAdapter mAdapter1,
	    Context mContext, OnFilterClickListener listener) {
	
	
	mAdapter1.add(new MyFilterHorizontalListItem(mContext, listener,
		new CustomData1(R.drawable.filter_normal,
			mContext.getString(R.string.filter_no_filter),
			R.drawable.item_selected_02),
		ImageObject.FILTER_TYPE_NORMAL,false));
	
	
	
	
	int nitemId = 5;
	int nDirId = 5;
	ArrayList<FilterItemInfo> list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.camera_effect_5,
		"暖风飘", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.camera_effect_6,
		"阿宝", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.camera_effect_7,
		"梦里", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.camera_effect_8,
		"拿铁", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.camera_effect_9,
		"活力", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.camera_effect_10,
		"优雅", R.drawable.item_selected_02), nitemId));
	nitemId++;
	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.wucha_icon, nDirId, listener, list));
	nDirId++;
	/**
	 * 哥特
	 */
	list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.gete_0,
		"暴走", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.gete_1,
		"街拍", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.gete_2,
		"欧洲站", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.gete_3,
		"里约", R.drawable.item_selected_02), nitemId));
	nitemId++;
	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.gete_icon, nDirId, listener, list));
	nDirId++;
	/**
	 * lomo
	 */

	list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_0,
		"流年", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_1,
		"聚光", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_2,
		"候鸟", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_3,
		"云端", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_4,
		"彩虹瀑", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_5,
		"淡雅", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lomo_6,
		"优格", R.drawable.item_selected_02), nitemId));
	nitemId++;
	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.lomo_icon, nDirId, listener, list));
	nDirId++;

	/**
	 * 冷调
	 */
	list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lengdiao_0,
		"青春", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lengdiao_1,
		"胭脂", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lengdiao_2,
		"精灵", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lengdiao_3,
		"遐想", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.lengdiao_4,
		"冰冰凉", R.drawable.item_selected_02), nitemId));
	nitemId++;

	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.lengdiao_icon, nDirId, listener, list));
	nDirId++;
	/**
	 * 薄暮
	 */
	list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.bomu_0,
		"牛奶", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.bomu_1,
		"塞纳河畔", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.bomu_2,
		"清晨", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.bomu_3,
		"伊豆", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.bomu_4,
		"随想", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.bomu_5,
		"窗扉", R.drawable.item_selected_02), nitemId));
	nitemId++;

	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.bomu_icon, nDirId, listener, list));
	nDirId++;
	/**
	 * 夜色
	 */
	list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.yese_0,
		"近黄昏", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.yese_1,
		"阴天", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.yese_2,
		"檀岛", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.yese_3,
		"节气", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.yese_4,
		"晚霞", R.drawable.item_selected_02), nitemId));
	nitemId++;
	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.yese_icon, nDirId, listener, list));
	nDirId++;
	/**
	 * 怀旧
	 */
	list = new ArrayList<FilterItemInfo>();
	list.add(new FilterItemInfo(new CustomData1(R.drawable.huaijiu_1,
		"朦胧", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.huaijiu_2,
		"记忆", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.huaijiu_3,
		"黑白", R.drawable.item_selected_02), nitemId));
	nitemId++;
	list.add(new FilterItemInfo(new CustomData1(R.drawable.huaijiu_4,
		"岁月", R.drawable.item_selected_02), nitemId));
	nitemId++;
	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
		R.drawable.huaijiu_icon, nDirId, listener, list));
	nDirId++;
//	/**
//	 * 艺术
//	 */
//	list = new ArrayList<FilterItemInfo>();
//	list.add(new FilterItemInfo(new CustomData1(R.drawable.yishu_0,
//		"反转", R.drawable.item_selected_02), nitemId));
//	nitemId++;
//	list.add(new FilterItemInfo(new CustomData1(R.drawable.yishu_1,
//		"照亮边缘", R.drawable.item_selected_02), nitemId));
//	nitemId++;
//	list.add(new FilterItemInfo(new CustomData1(R.drawable.yishu_2,
//		"哈哈镜", R.drawable.item_selected_02), nitemId));
//	nitemId++;
//	list.add(new FilterItemInfo(new CustomData1(R.drawable.yishu_3,
//		"素描", R.drawable.item_selected_02), nitemId));
//	nitemId++;
//	mAdapter1.add(new FilterGroupHorizontalistListItem(mContext,
//		R.drawable.yishu_icon, nDirId, listener, list));
//	nDirId++;
    }
}
