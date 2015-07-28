package cn.com.mobnote.golukmobile.videosuqare;

import io.vov.vitamio.utils.Log;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class VideoSquareAdapter extends PagerAdapter{
	private Context mContext=null;
	public VideoSquareListView mVideoSquareListView=null;
	private VideoCategoryView mVideoCategoryView=null;
	public BaiduMapView baidumap = null;
	SharePlatformUtil sharePlatform;
	
	public VideoSquareAdapter(Context c,SharePlatformUtil spf) {
		this.mContext=c;
		sharePlatform = spf;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Log.e("BBBBBBB", "BBBBBBB======="+position);
		if(0 == position){
			mVideoSquareListView = new VideoSquareListView(mContext,sharePlatform);
			container.addView(mVideoSquareListView.getView());
			return mVideoSquareListView.getView();
		}else if(1 == position){
			mVideoCategoryView = new VideoCategoryView(mContext);
			container.addView(mVideoCategoryView.getView());
			return mVideoCategoryView.getView();
		}else {		
			baidumap = new BaiduMapView(mContext);
			container.addView(baidumap.getView());
			return baidumap.getView();
		}
	}

	
	@Override
	public int getCount() {
		return 3;
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
		
		/*if(null != baidumap){
			baidumap.onDestroy();
		}*/
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
			baidumap.onResume();
		}
	}
	
	public void onStop(){
		if(null != mVideoSquareListView){
			mVideoSquareListView.onStop();
		}
	}

}
