package cn.com.mobnote.golukmobile.newest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.base.CarRecordBaseActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;

public class VideoCategoryListActivity extends CarRecordBaseActivity implements VideoSuqareManagerFn{
	private String id;
	private boolean headLoading = false;
	private boolean dataLoading = false;
	private String curOperation = "0";
	private String historyDate;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");
	private RTPullListView mRTPullListView = null;
	public List<VideoSquareInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private NewestAdapter mNewestAdapter = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addContentView(LayoutInflater.from(this).inflate(R.layout.video_type_list, null)); 
		mRTPullListView = (RTPullListView)findViewById(R.id.mRTPullListView);
		String title = getIntent().getStringExtra("title");
		id = getIntent().getStringExtra("id");
		setTitle(title);
		
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if("".equals(historyDate)){
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
		
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if(null != mVideoSquareManager){
			mVideoSquareManager.addVideoSquareManagerListener("NewestListView", this);
		}
		
		loadHistoryData();
		httpPost(true, "0", "");
	}
	
	private void loadHistoryData() {
		String data = GolukApplication.getInstance().getVideoSquareManager().getTypeVideoList("0");
		if (!TextUtils.isEmpty(data)) {
			mDataList = JsonParserUtils.parserNewestItemData(data);
			initLayout();
		}
	}
	
	private void httpPost(boolean flag, String operation, String timestamp){
		curOperation = operation;
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomLoadingDialog(this, null);
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
			mNewestAdapter = new NewestAdapter(this);
		}
	
		if ("0".equals(curOperation)) {
			mRTPullListView.setAdapter(mNewestAdapter);
		}
		mNewestAdapter.setData(null, mDataList);
		
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				
				mRTPullListView.onRefreshComplete(historyDate);
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
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == VSquare_Req_List_Video_Catlog) {
			if (RESULE_SUCESS == msg) {
				dataLoading = false;
				List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData((String)param2);
				if ("0".equals(curOperation)) {
					mDataList.clear();
				}
				
				mDataList.addAll(datalist);
				initLayout();
			}else{
				GolukUtils.showToast(VideoCategoryListActivity.this, "网络异常，请检查网络");
			}
		}
		
	}

}
