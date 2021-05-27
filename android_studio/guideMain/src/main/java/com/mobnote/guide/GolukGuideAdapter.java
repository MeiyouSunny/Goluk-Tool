package com.mobnote.guide;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import com.mobnote.golukmain.carrecorder.util.SoundUtils;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:引导页数据适配器
 * 
 * @author 陈宣宇
 * 
 */

public class GolukGuideAdapter extends PagerAdapter {
	public List<View> views;
	private int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
	private int height = SoundUtils.getInstance().getDisplayMetrics().heightPixels;
	
	public GolukGuideAdapter(List<View> mListViews) {
		this.views = mListViews;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager)arg0).removeView((View)arg2);
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		return this.views == null ? 0 : this.views.size();
	}
	
	@Override
	public Object instantiateItem(View arg0, int arg1){
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
		((ViewPager)arg0).addView(this.views.get(arg1),0,params);
		return this.views.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
}

