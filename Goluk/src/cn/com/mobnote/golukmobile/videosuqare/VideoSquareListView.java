package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import com.bokecc.sdk.mobile.play.DWMediaPlayer;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomProgressDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class VideoSquareListView implements VideoSuqareManagerFn{
	private Context mContext=null;
	private ListView mRTPullListView=null;
//	private RTPullListView mRTPullListView=null;
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
	public  static Handler mHandler=null;
	private RelativeLayout mRootLayout=null;
	private SurfaceView mSurfaceView=null;
	private SurfaceHolder mSurfaceHolder=null;
	private final String USERID = "77D36B9636FF19CF";
	private final String API_KEY = "O8g0bf8kqiWroHuJaRmihZfEmj7VWImF";
	private DWMediaPlayer mDWMediaPlayer=null;
	
	public VideoSquareListView(Context context){
		mContext=context;
		
		mSurfaceView = new SurfaceView(mContext);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				System.out.println("SSSYYY===111======surfaceDestroyed===");
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder arg0) {
				mSurfaceHolder=arg0;
				System.out.println("SSSYYY===111======surfaceCreated===");
				
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				System.out.println("SSSYYY===111======surfaceChanged===");
			}
		});
		
		mDWMediaPlayer = new DWMediaPlayer();
		mDWMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
				arg0.start();
				System.out.println("SSSYYY========arg0.start();=======");
			}
		});
		mDWMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
				System.out.println("SSSYYY========onBufferingUpdate======arg1="+arg1);
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		mRTPullListView = new ListView(mContext);
		mRTPullListView.setDivider(mContext.getResources().getDrawable(R.color.video_square_list_frame));
		mRTPullListView.setDividerHeight((int)(2*jj));
		//getResources().getColor(R.color.textcolor_select)
		mDataList = new ArrayList<VideoSquareInfo>();
		LogUtils.d("SSS=================111111111===================");
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if(null != mVideoSquareManager){
			mVideoSquareManager.addVideoSquareManagerListener("hotlist", this);
		}
		httpPost(true);
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					refeshData((VideoSquareInfo)msg.obj);
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		mRootLayout = new RelativeLayout(mContext);
		mRootLayout.addView(mRTPullListView);
		
		RelativeLayout.LayoutParams surfaceParams = new RelativeLayout.LayoutParams(200, 200);
		mRootLayout.addView(mSurfaceView, surfaceParams);
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
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getHotList("1","0");
			if(!result){
				closeProgressDialog();
			}
			System.out.println("YYYY==22222==getHotList======result="+result);
		}else{
			System.out.println("YYYY==22222==getHotList======error=");
		}
	}
	
	private void initLayout(){
		
		if(null == mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mContext);
		}
		
		mVideoSquareListViewAdapter.setData(mDataList);
		mRTPullListView.setAdapter(mVideoSquareListViewAdapter);
//		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
//			@Override
//			public void onRefresh() {
////				mRTPullListView.onRefreshComplete();
//				Toast.makeText(mContext, "下拉刷新", Toast.LENGTH_SHORT).show();
//				mRTPullListView.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						mRTPullListView.onRefreshComplete();
//					}
//				}, 1500);
//				
//			}
//		});
		
		mRootLayout.removeView(mSurfaceView);
		updateSurfaceViewIndex(0);
		
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
//				System.out.println("SSSYYY==11111===firstVisibleItem="+firstVisibleItem+"===visibleItemCount="+visibleItemCount+"==arg3="+arg3);
//				if(firstVisibleItem == 0){
//					mRootLayout.removeView(mSurfaceView);
//					View view = mRTPullListView.getChildAt(firstVisibleItem);
//					if(null != view){
//						System.out.println("SSSYYY==222222222===firstVisibleItem="+firstVisibleItem);
//						RelativeLayout mPlayerLayout = (RelativeLayout)view.findViewById(R.id.mPlayerLayout);
//						if(null != mPlayerLayout){
//							String tag = (String)mPlayerLayout.getTag();
//							if(TextUtils.isEmpty(tag)){
//								mPlayerLayout.addView(mSurfaceView);
//								mPlayerLayout.setTag("add");
//								System.out.println("SSSYYY==333333333333===firstVisibleItem="+firstVisibleItem);
//							}
//						}
//					}
//					
//					
//				}
				if(wonderfulFirstVisible != firstVisibleItem){
					System.out.println("SSSYYY==22222===firstVisibleItem="+firstVisibleItem+"===visibleItemCount="+visibleItemCount+"==arg3="+arg3);
				}
				wonderfulFirstVisible=firstVisibleItem;
				wonderfulVisibleCount=visibleItemCount;
			}
		});
		
		RelativeLayout loading = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.video_square_below_loading, null); 
		mRTPullListView.addFooterView(loading);
		
	}
	
	private void updateSurfaceViewIndex(int index){
		System.out.println("SSSYYY==aaa111122===index="+index);
		mDWMediaPlayer.reset();
		View view = mRTPullListView.getChildAt(index);
		if(null != view){
			System.out.println("SSSYYY==222222222===index="+index);
			SurfaceView surface = (SurfaceView)view.findViewById(R.id.mSurfaceView);
			if(null != surface){
				System.out.println("SSSYYY==333333===videoid="+mDataList.get(index).mVideoEntity.videoid);
				surface = mSurfaceView;
				mDWMediaPlayer.setDisplay(mSurfaceHolder);
				mDWMediaPlayer.setVideoPlayInfo(mDataList.get(index).mVideoEntity.videoid, USERID, API_KEY,mContext);
				mDWMediaPlayer.prepareAsync();
			}
			
			
//			RelativeLayout mPlayerLayout = (RelativeLayout)view.findViewById(R.id.mPlayerLayout);
//			if(null != mPlayerLayout){
//				String tag = (String)mPlayerLayout.getTag();
//				if(TextUtils.isEmpty(tag)){
//					mPlayerLayout.addView(mSurfaceView);
//					mPlayerLayout.setTag("add");
//					System.out.println("SSSYYY==33333333===index="+index+"==videoid="+mDataList.get(index).mVideoEntity.videoid);
//					mDWMediaPlayer.setDisplay(mSurfaceHolder);
//					mDWMediaPlayer.setVideoPlayInfo(mDataList.get(index).mVideoEntity.videoid, USERID, API_KEY,mContext);
//					mDWMediaPlayer.prepareAsync();
//				}
//			}
		}
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
		return mRootLayout;
	}
	
	public void onBackPressed(){
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onBackPressed();
		}
	}
	
	public void onStop() {
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onStop();
		}
	}
	
	public void onDestroy(){
		GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener("hotlist");
		if(null != mVideoSquareListViewAdapter){
//			mVideoSquareListViewAdapter.onDestroy();
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		System.out.println("YYY======event="+event+"======msg="+msg+"===param2="+param2);
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
	
	public void refeshData(VideoSquareInfo videoinfo){
		for(int i = 0;i<mDataList.size();i++){
			VideoSquareInfo vsi =  mDataList.get(i);
			if(vsi.mVideoEntity.videoid.equals(videoinfo.mVideoEntity.videoid)){
				mDataList.get(i).mVideoEntity = videoinfo.mVideoEntity;
			}
		}
		
		
	}
	
}
