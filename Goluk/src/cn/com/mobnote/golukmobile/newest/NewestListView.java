package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.UserEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class NewestListView {
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	private NewestListHeadDataInfo mHeadDataInfo = null;
	public List<VideoSquareInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	public  static Handler mHandler = null;
	private NewestAdapter mNewestAdapter = null;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	
	public NewestListView(Context context) {
		mContext = context;
		mHeadDataInfo = new NewestListHeadDataInfo();
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRootLayout = new RelativeLayout(mContext);
		mRootLayout.addView(mRTPullListView);
		
		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					mNewestAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					mNewestAdapter.unlock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mNewestAdapter.lock();
					break;
					
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int arg3) {
				firstVisible = firstVisibleItem;
				visibleCount = visibleItemCount;
			}
			
		});
		
		
//		httpPost(true);
		
		
		loadData();
		
		initLayout();
	}
	
	
	private void loadData() {
		
		for(int i=0; i<4; i++) {
			CategoryDataInfo info = new CategoryDataInfo();
			info.name = "#曝光台=="+i;
			if(i == 0 || i == 3) {
				info.coverurl = "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png";
			}else{
				info.coverurl = "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png";
			}
			info.time = i+"分钟前更新";
			mHeadDataInfo.categoryList.add(info);
		}
		LiveInfo live = new LiveInfo();
		live.pic = "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png";
		live.number = "34";
		mHeadDataInfo.mLiveDataInfo = live;
		
		for(int i=0; i<10; i++) {
			VideoSquareInfo info = new VideoSquareInfo();
			UserEntity user = new UserEntity();
			user.nickname = "极路客=="+i;
			if(i < 7) {
				int index = i+1;
				user.headportrait = ""+index;
			}else {
				user.headportrait = "1";
			}
			
			info.mUserEntity = user;
			
			VideoEntity video = new VideoEntity();
			video.sharingtime = "20150706084849566";
			video.picture = "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png";
			video.describe = "#极路客精彩视频#==士大夫哈哈哈哈大黄蜂和合法";
			video.ondemandsdkaddress = "http://cdn.goluk.cn/files/cdcvideo/20150706/20150706_F6E67520197311E5B520A2283A0E25C4.mp4";
			if(0 == i) {
				video.ispraise = "1";
				video.praisenumber = "120000244";
				video.clicknumber = "120000244";
			}else {
				video.ispraise = "0";
				video.praisenumber = "16534";
				video.clicknumber = "15423";
			}
			
			
			int size = 3;
			if(1 == i) {
				size = 0;
			}else if(2 == i) {
				size = 1;
			}else if(3 == i) {
				size = 2;
			}else {
				size = 3;
			}
			for(int v=0; v<size; v++) {
				CommentDataInfo comm = new CommentDataInfo();
				comm.name = "chsdedff";
				comm.text = "时代光华个大概上搜索萨拉开始看开始看的方法和海三个大概多个省市";
				
				video.commentList.add(comm);
			}
			
			video.comcount = "1565244";
			info.mVideoEntity = video;
			
			mDataList.add(info);
		}
		
	}
	
	private void httpPost(boolean flag){
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomLoadingDialog(mContext,null);
				mCustomProgressDialog.show();
			}
		}
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getHotList("1","0");
			if(!result){
				closeProgressDialog();
			}
		}else{
			closeProgressDialog();
		}
	}
	
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
	
	private void initLayout(){
		if(null == mNewestAdapter){
			mNewestAdapter = new NewestAdapter(mContext);
		}
		
		mNewestAdapter.setData(mHeadDataInfo, mDataList);
		mRTPullListView.setAdapter(mNewestAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				
				mRTPullListView.onRefreshComplete("2015-08-04");//historyDate);
			}
		});
	}

	public View getView(){
		return mRootLayout;
	}
	
	public void onDestroy(){
		
	}
	
}

