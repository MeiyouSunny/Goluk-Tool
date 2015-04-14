package cn.com.mobnote.golukmobile.videosuqare;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class VideoSquareAdapter extends PagerAdapter{
	private Context mContext=null;
	
	public VideoSquareAdapter(Context c) {
		this.mContext=c;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(0 == position){
			VideoSquareListView list = new VideoSquareListView(mContext);
			container.addView(list.getView());
			return list.getView();
		}else{
			VideoCategoryView category = new VideoCategoryView(mContext);
			container.addView(category.getView());
			return category.getView();
		}
	}
	
	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}
