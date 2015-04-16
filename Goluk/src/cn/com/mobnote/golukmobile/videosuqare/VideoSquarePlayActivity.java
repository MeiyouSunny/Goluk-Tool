package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomProgressDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class VideoSquarePlayActivity extends Activity implements OnClickListener, VideoSuqareManagerFn {
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;
	private List<VideoSquareInfo> mDataList=null;
	private CustomProgressDialog mCustomProgressDialog=null;
	
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	
	/** 是否还有分页*/
	private boolean isHaveData = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_play);
		Intent intent=getIntent();
		
		String category=intent.getStringExtra("category");//视频广场类型
		
		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videocategory", this);
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = (RTPullListView)findViewById(R.id.mRTPullListView);
		
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
				mCustomProgressDialog = new CustomProgressDialog(this);
				mCustomProgressDialog.setCancelable(false);
				mCustomProgressDialog.show();
			}
		}
		boolean result = GolukApplication.getInstance().getVideoSquareManager().getSquareList();
		if(!result){
			closeProgressDialog();
		}
		System.out.println("YYYY==22222==getSquareList======result="+result);
	}
	
	private void init (){
		/** 返回按钮 */
		 Button mBackBtn = (Button) findViewById(R.id.back_btn);;
		 mBackBtn.setOnClickListener(this);
		 
		 if(null == mVideoSquareListViewAdapter){
			 mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(this);
		 }
		 
//		 List<VideoSquareInfo> data = new ArrayList<VideoSquareInfo>();
//			for(int i=0;i<100;i++){
//				VideoSquareInfo info = new VideoSquareInfo();
//				UserEntity user = new UserEntity();
//				user.nickname = "三十三岁";
//				info.mUserEntity = user;
//				data.add(info);
//			}
			mVideoSquareListViewAdapter.setData(mDataList);
			mRTPullListView.setAdapter(mVideoSquareListViewAdapter);
			mRTPullListView.setonRefreshListener(new OnRefreshListener() {
				@Override
				public void onRefresh() {
//					mRTPullListView.onRefreshComplete();
					//Toast.makeText(VideoSquarePlayActivity.this, "下拉刷新", Toast.LENGTH_SHORT).show();
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
								httpPost(true);
							}
							//Toast.makeText(VideoSquarePlayActivity.this, "滑动到最后了222", 1000).show();
							//System.out.println("TTTTT=====滑动到最后了222");
						}
					}
				}
				@Override
				public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
					wonderfulFirstVisible=firstVisibleItem;
					wonderfulVisibleCount=visibleItemCount;
				}
			});
			
			RelativeLayout loading = (RelativeLayout)LayoutInflater.from(this).inflate(R.layout.video_square_below_loading, null); 
			mRTPullListView.addFooterView(loading);
		 
	}
	
	public void flush(){
		mVideoSquareListViewAdapter.setData(mDataList);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;

		default:
			break;
		}
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener("videocategory");
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		System.out.println("YYYY==333==getSquareList====event="+event+"===msg="+msg+"==param2="+param2);
		if(event == SquareCmd_Req_SquareList){
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
					init();
				}else{
					mDataList.addAll(list);
					flush();
				}
			}
		}
	}
	
}
