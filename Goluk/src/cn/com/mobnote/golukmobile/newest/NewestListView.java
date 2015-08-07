package cn.com.mobnote.golukmobile.newest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class NewestListView implements VideoSuqareManagerFn{
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	private NewestListHeadDataInfo mHeadDataInfo = null;
	public List<VideoSquareInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	public  static Handler mHandler = null;
	private NewestAdapter mNewestAdapter = null;
	private boolean headLoading = false;
	private boolean dataLoading = false;
	private String curOperation = "0";
	private String historyDate;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");
	
	public NewestListView(Context context) {
		mContext = context;
		mHeadDataInfo = new NewestListHeadDataInfo();
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRootLayout = new RelativeLayout(mContext);
		mRootLayout.addView(mRTPullListView);
	
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if("".equals(historyDate)){
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
		
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("NewestListView", this);
		}
		
		loadHistoryData();
		httpPost(true, "0", "");
	}
	
	private void loadHistoryData() {
		boolean headFlag = false;
		boolean dataFlag = false;
		String head = GolukApplication.getInstance().getVideoSquareManager().getZXList();
		String data = GolukApplication.getInstance().getVideoSquareManager().getTypeVideoList("0");
		if (!TextUtils.isEmpty(head)) {
			headFlag = true;
			mHeadDataInfo = JsonParserUtils.parserNewestHeadData(head);
		}
		if (!TextUtils.isEmpty(data)) {
			dataFlag = true;
			mDataList = JsonParserUtils.parserNewestItemData(data);
		}
		
		if (headFlag && dataFlag) {
			initLayout();
		}
		
	}
	
	private void httpPost(boolean flag, String operation, String timestamp){
		curOperation = operation;
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomLoadingDialog(mContext,null);
				mCustomProgressDialog.show();
			}
		}
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			if ("0".equals(operation)) {
				if(!headLoading) {
					headLoading = true;
					GolukApplication.getInstance().getVideoSquareManager().getZXListData();
				}
			}
			
			if (dataLoading) {
				return;
			}
			
			List<String> attribute = new ArrayList<String>();
			attribute.add("0");
			dataLoading = true;
			boolean tv = GolukApplication.getInstance().getVideoSquareManager().getTypeVideoList("1", "0", attribute, operation, timestamp);
			if(!tv){
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
		if(!headLoading && !dataLoading) {
			return;
		}
		
		closeProgressDialog();
		mRTPullListView.onRefreshComplete(historyDate);
		if(null == mNewestAdapter){
			mNewestAdapter = new NewestAdapter(mContext);
		}
	
		if ("0".equals(curOperation)) {
			mRTPullListView.setAdapter(mNewestAdapter);
		}
		mNewestAdapter.setData(mHeadDataInfo, mDataList);
		
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				httpPost(true, "0", "");
			}
		});
		
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
//				firstVisible = firstVisibleItem;
//				visibleCount = visibleItemCount;
			}
			
		});
		
	}

	public View getView(){
		return mRootLayout;
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == VSquare_Req_List_Catlog){
			if (RESULE_SUCESS == msg) {
				headLoading = false;
				mHeadDataInfo = JsonParserUtils.parserNewestHeadData((String)param2);
				initLayout();
			}else{
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
			
//			if(mDataList.size()>0){
//				setViewListBg(false);
//			}else{
//				setViewListBg(true);
//			}
		}else if(event == VSquare_Req_List_Video_Catlog) {
			if (RESULE_SUCESS == msg) {
				dataLoading = false;
				List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData((String)param2);
				if ("0".equals(curOperation)) {
					mDataList.clear();
				}
				
				mDataList.addAll(datalist);
				initLayout();
			}else{
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
		}
		
	}
	
	public void onResume() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if(null != mVideoSquareManager){
			mVideoSquareManager.addVideoSquareManagerListener("NewestListView", this);
		}
	}
	
	public void onPause() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if(null != mVideoSquareManager){
			mVideoSquareManager.removeVideoSquareManagerListener("NewestListView");
		}
	}
	
	public void onDestroy(){
		
	}
	
}

