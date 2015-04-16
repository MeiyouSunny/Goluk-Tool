package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomProgressDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class VideoSquareListView implements VideoSuqareManagerFn{
	private Context mContext=null;
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;
	private List<VideoSquareInfo> mDataList=null;
	private CustomProgressDialog mCustomProgressDialog=null;
	private Float jj= SoundUtils.getInstance().getDisplayMetrics().density;
	
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 是否还有分页*/
	private boolean isHaveData = true;
	
	
	public VideoSquareListView(Context context){
		mContext=context;
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setDivider(mContext.getResources().getDrawable(R.color.video_square_list_frame));
		mRTPullListView.setDividerHeight((int)(2*jj));
		//getResources().getColor(R.color.textcolor_select)
		mDataList = new ArrayList<VideoSquareInfo>();
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("hotlist", this);
		httpPost(true);
	}
	
	/**
	 * 获取网络数据
	 * @param flag 是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void httpPost(boolean flag){
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomProgressDialog(mContext);
				mCustomProgressDialog.setCancelable(false);
				mCustomProgressDialog.show();
			}
		}
		boolean result = GolukApplication.getInstance().getVideoSquareManager().getHotList();
		if(!result){
			closeProgressDialog();
		}
		System.out.println("YYYY==22222==getHotList======result="+result);
	}
	
	private void initLayout(){
		
		if(null == mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mContext);
		}
		
		mVideoSquareListViewAdapter.setData(mDataList);
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
		
		
		mRTPullListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					if(mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible+wonderfulVisibleCount)){
						if(isHaveData){
							Toast.makeText(mContext, "上拉刷新", Toast.LENGTH_SHORT).show();
							httpPost(false);
						}
						
					}
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				wonderfulFirstVisible=firstVisibleItem;
				wonderfulVisibleCount=visibleItemCount;
			}
		});
		
		RelativeLayout loading = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.video_square_below_loading, null); 
		mRTPullListView.addFooterView(loading);
		
	}
	
	public void flush(){
		mVideoSquareListViewAdapter.setData(mDataList);
	}
	
	/**
	 * 关闭加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			if(mCustomProgressDialog.isShowing()){
				mCustomProgressDialog.dismiss();
			}
		}
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
//		System.out.println("SSS=============msg="+msg+"===param2="+param2);
		if(event == SquareCmd_Req_HotList){
			closeProgressDialog();
			if(RESULE_SUCESS == msg){
				List<VideoSquareInfo> list = DataParserUtils.parserVideoSquareListData((String)param2);
				if(list.size()>=2){
					isHaveData = true;
				}else{
					isHaveData = false;
				}
				
				if(mDataList.size()<=0){
					mDataList = list;
					initLayout();
				}else{
					mDataList.addAll(list);
					flush();
				}
				
			}
		}
	}
	
}
