package com.mobnote.golukmain.startshare;

import android.content.Context;

import java.util.ArrayList;

import com.mobnote.golukmain.R;

public class MVManage {

	Context mContext ;
	
	public MVManage(Context context) {
		this.mContext = context;
	}

	/**
	 * 获取本地滤镜主题列表
	 * 
	 * @return
	 */
	public ArrayList<MVEditData> getLocalVideoList() {
		ArrayList<MVEditData> list = new ArrayList<MVEditData>();
		// 保存数据
		MVEditData data1 = new MVEditData();
		data1.src = R.drawable.filter_nothing;
		data1.name = mContext.getString(R.string.str_no_text);
		data1.display = true;
		data1.filterId = 0;
		list.add(data1);

		MVEditData data8 = new MVEditData();
		data8.src = R.drawable.filter_gudian;
		data8.name = mContext.getString(R.string.str_classical_film);
		data8.filterId = 7;
		list.add(data8);

		MVEditData data3 = new MVEditData();
		data3.src = R.drawable.filter_heibai;
		data3.name = mContext.getString(R.string.str_black_and_white);
		data3.filterId = 1;
		list.add(data3);

		MVEditData data7 = new MVEditData();
		data7.src = R.drawable.filter_rouhe;
		data7.name = mContext.getString(R.string.str_soft_quiet);
		data7.filterId = 5;
		list.add(data7);

		MVEditData data5 = new MVEditData();
		data5.src = R.drawable.filter_fugu;
		data5.name = mContext.getString(R.string.str_retro_nostalgia);
		data5.filterId = 2;
		list.add(data5);

		MVEditData data4 = new MVEditData();
		data4.src = R.drawable.filter_duocai;
		data4.name = mContext.getString(R.string.str_colorful_summer);
		data4.filterId = 4;
		list.add(data4);

		MVEditData data6 = new MVEditData();
		data6.src = R.drawable.filter_binfen;
		data6.name = mContext.getString(R.string.str_colorful_dream);
		data6.filterId = 6;
		list.add(data6);

		MVEditData data2 = new MVEditData();
		data2.src = R.drawable.filter_qingxin;
		data2.name = mContext.getString(R.string.str_fresh_and_elegant);
		data2.filterId = 3;
		list.add(data2);

		return list;
	}

	public class MVEditData {
		// 图片路径
		public int src;
		// 名称
		public String name;
		// 选中标识
		public boolean display = false;
		// 滤镜值
		public int filterId;
	}
}
