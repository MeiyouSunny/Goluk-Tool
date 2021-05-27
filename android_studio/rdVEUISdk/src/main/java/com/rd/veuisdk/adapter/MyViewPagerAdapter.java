package com.rd.veuisdk.adapter;

import androidx.core.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * ViewPage的适配器
 * @author Administrator
 *
 */
public class MyViewPagerAdapter extends PagerAdapter{
	
	private List<View> mViewList;//View就二十GridView
	
	
	public MyViewPagerAdapter(List<View> viewList) {
		this.mViewList = viewList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mViewList!=null ? mViewList.size() : 0;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}
	/**
	 * 将当前的View添加到ViewGroup容器中
	 * 这个方法，return一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPage上
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		container.addView(mViewList.get(position));
		return mViewList.get(position);
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		 container.removeView((View) object);
	}
}
