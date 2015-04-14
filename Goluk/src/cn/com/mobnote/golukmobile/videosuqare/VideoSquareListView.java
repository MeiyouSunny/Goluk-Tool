package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class VideoSquareListView implements VideoSuqareManagerFn{
	private Context mContext=null;
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;
	private List<VideoSquareInfo> mDataList=null;

	public VideoSquareListView(Context context){
		mContext=context;
		mRTPullListView = new RTPullListView(mContext);
		mDataList = new ArrayList<VideoSquareInfo>();
		httpPost(true);
	}
	
	private void httpPost(boolean flag){
		if(flag){
			
		}
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("hotlist", this);
		boolean a = GolukApplication.getInstance().getVideoSquareManager().getHotList();
		System.out.println("YYYY==22222==getHotList======a="+a);
	}
	
	private void initLayout(){
		
		if(null == mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mContext);
		}
		
		
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
	
	public void onDestroy(){
		GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener("hotlist");
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onDestroy();
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == SquareCmd_Req_HotList){
			if(RESULE_SUCESS == msg){
				System.out.println("YYY=====getHotList==33333=======msg="+msg+"===param2="+param2);
				mDataList = DataParserUtils.parserVideoSquareListData((String)param2);
				initLayout();
				
				
				for(int i=0;i<mDataList.size();i++){
					System.out.println("YYY=====getHotList==4444====describe="+mDataList.get(i).mVideoEntity.describe+"===nickname="+mDataList.get(i).mUserEntity.nickname);
				}
				
			}
		}
	}
	
}
