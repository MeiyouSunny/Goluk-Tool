package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.golukmobile.newest.NewestListView;
import cn.com.mobnote.golukmobile.newest.WonderfulSelectedListView;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class VideoSquareAdapter extends PagerAdapter{
	private Context mContext=null;
	private SharePlatformUtil sharePlatform;
	private WonderfulSelectedListView mWonderfulSelectedListView = null;
	private NewestListView mNewestListView = null;
	
	
	public VideoSquareAdapter(Context c,SharePlatformUtil spf) {
		this.mContext=c;
		sharePlatform = spf;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(0 == position){
			mWonderfulSelectedListView = new WonderfulSelectedListView(mContext);
			container.addView(mWonderfulSelectedListView.getView());
			return mWonderfulSelectedListView.getView();
		}else{
			mNewestListView = new NewestListView(mContext);
			container.addView(mNewestListView.getView());
			return mNewestListView.getView();
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
	
	public void onDestroy(){
		if(null != mWonderfulSelectedListView){
			mWonderfulSelectedListView.onDestroy();
		}
		if(null != mNewestListView){
			mNewestListView.onDestroy();
		}
		
	}
	
	public void onResume() {
		if(null != mNewestListView){
			mNewestListView.onResume();
		}
		
	}
	
	public void onPause() {
		if(null != mNewestListView){
			mNewestListView.onPause();
		}
		
	}
	
	public void onStop(){
		
	}

}
