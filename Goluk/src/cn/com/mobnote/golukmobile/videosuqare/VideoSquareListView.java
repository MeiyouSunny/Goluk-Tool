package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class VideoSquareListView {
	private Context mContext=null;
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;

	public VideoSquareListView(Context context){
		mContext=context;
		mRTPullListView = new RTPullListView(mContext);
		mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mContext);
		
		
		List<VideoSquareInfo> data = new ArrayList<VideoSquareInfo>();
		for(int i=0;i<100;i++){
			VideoSquareInfo info = new VideoSquareInfo();
			data.add(info);
		}
		mVideoSquareListViewAdapter.setData(data);
		mRTPullListView.setAdapter(mVideoSquareListViewAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
//				mRTPullListView.onRefreshComplete();
				Toast.makeText(mContext, "下拉刷新", Toast.LENGTH_SHORT).show();
				mRTPullListView.postDelayed(new Runnable() {
					@Override
					public void run() {
						mRTPullListView.onRefreshComplete();
					}
				}, 1500);
			}
		});
	}
	
	public View getView(){
		return mRTPullListView;
	}
	
}
