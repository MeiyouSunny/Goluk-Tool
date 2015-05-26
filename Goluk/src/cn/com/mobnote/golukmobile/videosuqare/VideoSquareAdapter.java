package cn.com.mobnote.golukmobile.videosuqare;

import cn.com.mobnote.golukmobile.SharePlatformUtil;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class VideoSquareAdapter extends PagerAdapter{
	private Context mContext=null;
	public VideoSquareListView mVideoSquareListView=null;
	private VideoCategoryView mVideoCategoryView=null;
	SharePlatformUtil sharePlatform;
	
	public VideoSquareAdapter(Context c,SharePlatformUtil spf) {
		this.mContext=c;
		sharePlatform = spf;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if(0 == position){
			mVideoSquareListView = new VideoSquareListView(mContext,sharePlatform);
			container.addView(mVideoSquareListView.getView());
			return mVideoSquareListView.getView();
		}else{
			mVideoCategoryView = new VideoCategoryView(mContext);
			container.addView(mVideoCategoryView.getView());
			return mVideoCategoryView.getView();
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
		if(null != mVideoSquareListView){
			mVideoSquareListView.onDestroy();
		}
		if(null != mVideoCategoryView){
			mVideoCategoryView.onDestroy();
		}
	}
	
	public void onBackPressed(){
		if(null != mVideoSquareListView){
			mVideoSquareListView.onBackPressed();
		}
	}
	
	public void onResume(){
		if(null != mVideoSquareListView){
			mVideoSquareListView.onResume();
			mVideoCategoryView.onResume();
		}
	}
	
	public void onStop(){
		if(null != mVideoSquareListView){
			mVideoSquareListView.onStop();
		}
	}

}
