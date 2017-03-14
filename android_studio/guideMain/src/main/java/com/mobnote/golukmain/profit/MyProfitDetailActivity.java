package com.mobnote.golukmain.profit;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import cn.com.mobnote.module.page.IPageNotifyFn;

/**
 * 收益明细
 * @author lily
 *
 */
public class MyProfitDetailActivity extends BaseActivity implements OnClickListener, IRequestResultListener, OnItemClickListener,
		OnRefreshListener, OnRTScrollListener {

	private ImageButton mBtnBack;
	private RTPullListView mRTPullListView;
	private ProfitDetailRequest profitDetailRequest = null;
	private MyProfitDetailAdapter mAdapter = null;
	private ProfitDetailInfo detailInfo = null;
	private RelativeLayout mImageRefresh = null;
	private TextView mTextNoData = null;
	/** 首次进入 */
	private static final String OPERATOR_FIRST = "0";
	/** 下拉 */
	private static final String OPERATOR_DOWN = "1";
	/** 上拉 */
	private static final String OPERATOR_UP = "2";
	/** 一页请求多少条数据 */
	private static final String PAGE_SIZE = "20";
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 最近更新时间 */
	private String historyDate = "";
	/**用户id**/
	private String uid;
	/**进入页面的loading**/
	private CustomLoadingDialog mLoadingDialog = null;
	/** 操作 (0:首次进入；1:下拉；2:上拉) */
	private String mCurrentOperator = "0";
	/** 是否还有分页 */
	private boolean mIsHaveData = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit_detail);
		
		historyDate = GolukUtils.getCurrentFormatTime(this);
		initView();
		if (savedInstanceState == null) {
			Intent it = getIntent();
			uid = it.getStringExtra("uid");
		} else {
			uid = savedInstanceState.getString("uid");
		}
		
		firstEnter();
		mRTPullListView.firstFreshState();
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		if (!TextUtils.isEmpty(uid)) {
			outState.putString("uid", uid);
		}
		super.onSaveInstanceState(outState);
	}

	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.profit_detail_back);
		mRTPullListView = (RTPullListView) findViewById(R.id.profit_detail_RTPullListView);
		mImageRefresh = (RelativeLayout) findViewById(R.id.video_detail_click_refresh);
		mTextNoData = (TextView) findViewById(R.id.my_profit_detail_nodata);
		
		mBtnBack.setOnClickListener(this);
		mImageRefresh.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(this);
		mRTPullListView.setOnRTScrollListener(this);
		mRTPullListView.setOnItemClickListener(this);
		mAdapter = new MyProfitDetailAdapter(this);
		mRTPullListView.setAdapter(mAdapter);
	}
	
	/**
	 * 请求数据
	 * @param uid
	 * @param operation
	 * @param timestamp
	 * @param pagesize
	 */
	private void httpRequestData (String operation, String timestamp) {
		profitDetailRequest = new ProfitDetailRequest(IPageNotifyFn.PageType_ProfitDetail, this);
		profitDetailRequest.get(uid, operation, timestamp, PAGE_SIZE, "");
	}
	
	@Override
	public void onRefresh() {
		startPull();
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			int count = mRTPullListView.getAdapter().getCount();
			int visibleCount = wonderfulFirstVisible + wonderfulVisibleCount;
			if (count == visibleCount && mIsHaveData) {
				startPush();
			}
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		wonderfulFirstVisible = firstVisibleItem;
		wonderfulVisibleCount = visibleItemCount;
	}
	
	/**
	 * 首次进入／下拉刷新，数据回调处理
	 * @param count
	 * @param mIncomeList
	 */
	private void pullCallBack(int count, List<ProfitDetailResult> mIncomeList) {
		this.mAdapter.setData(mIncomeList);
		mRTPullListView.onRefreshComplete(historyDate);
		if (count >= 20) {
			mIsHaveData = true;
			removeFoot(2);
			addFoot(1);
		} else {
			mIsHaveData = false;
			this.removeFoot(1);
		}

	}

	/**
	 * 上拉加载，数据回调处理
	 * @param count
	 * @param mIncomeList
	 */
	private void pushCallBack(int count, List<ProfitDetailResult> incomeList) {
		if (count >= 20) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
			this.removeFoot(1);
			this.addFoot(2);
		}
		this.mAdapter.appendData(incomeList);
	}
	
	/**
	 * 首次进入
	 */
	private void firstEnter() {
		mCurrentOperator = OPERATOR_FIRST;
		httpRequestData(OPERATOR_FIRST, "");
	}

	/**
	 * 开始下拉刷新
	 */
	private void startPull() {
		mCurrentOperator = OPERATOR_DOWN;
		httpRequestData(OPERATOR_FIRST, "");
	}
	
	/**
	 * 开始上拉加载
	 */
	private void startPush() {
		mCurrentOperator = OPERATOR_UP;
		httpRequestData(OPERATOR_UP, mAdapter.getLastDataTime());
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.profit_detail_back) {
			exit();
		} else if (id == R.id.video_detail_click_refresh) {
			showLoadingDialog();
			firstEnter();
		} else {
		}
	}

	TimerTask task = new TimerTask(){

		public void run(){
			MyProfitDetailActivity.this.finish();
		}

	};
	
	@Override
	public void onLoadComplete(int requestType, Object result) {
		closeLoadingDialog();
		if (requestType == IPageNotifyFn.PageType_ProfitDetail) {
			detailInfo = (ProfitDetailInfo) result;
			if (detailInfo == null) {
				return;
			}
			if(detailInfo.data != null){
				if(!GolukUtils.isTokenValid(detailInfo.data.result)){
					mImageRefresh.setVisibility(View.GONE);
					mRTPullListView.setVisibility(View.GONE);
					mTextNoData.setVisibility(View.VISIBLE);
					GolukUtils.startUserLogin(this);
					Timer timer = new Timer();
					timer.schedule(task, GolukConfig.CLOSE_ACTIVITY_TIMER);
					return;
				}
			}
			if (null != detailInfo && detailInfo.success && null != detailInfo.data && null != detailInfo.data.incomelist) {
				int size = detailInfo.data.incomelist.size();
				if (size <= 0 && !mCurrentOperator.equals(OPERATOR_UP)) {
					mImageRefresh.setVisibility(View.GONE);
					mRTPullListView.setVisibility(View.GONE);
					mTextNoData.setVisibility(View.VISIBLE);
					return;
				}
				mImageRefresh.setVisibility(View.GONE);
				mTextNoData.setVisibility(View.GONE);
				mRTPullListView.setVisibility(View.VISIBLE);
				if (mCurrentOperator.equals(OPERATOR_FIRST) || mCurrentOperator.equals(OPERATOR_DOWN)) {
					pullCallBack(detailInfo.data.incomelist.size(), detailInfo.data.incomelist);
				} else {
					pushCallBack(detailInfo.data.incomelist.size(), detailInfo.data.incomelist);
				}
			} else {
				unusual();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		if(null != mAdapter) {
			ProfitDetailResult income = (ProfitDetailResult) mAdapter.getItem(position-1);
			if(null != income && !"".equals(income.vid) && 100 != income.type) {
				//视频详情页访问
				ZhugeUtils.eventVideoDetail(this, this.getString(R.string.str_zhuge_share_video_network_other));

				Intent itVideoDetail = new Intent(MyProfitDetailActivity.this, VideoDetailActivity.class);
				itVideoDetail.putExtra(VideoDetailActivity.VIDEO_ID, income.vid);
				startActivity(itVideoDetail);
			}
		}
	}
	
	private void exit() {
		this.finish();
	}
	
	// 显示loading
	private void showLoadingDialog() {
		if (null == mLoadingDialog) {
			mLoadingDialog = new CustomLoadingDialog(this, null);
			mLoadingDialog.show();
		}
	}

	// 关闭loading
	private void closeLoadingDialog() {
		if (null != mLoadingDialog) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
	}
	
	/**
	 * 处理异常信息
	 */
	private void unusual() {
		mTextNoData.setVisibility(View.GONE);
		mRTPullListView.setVisibility(View.GONE);
		mImageRefresh.setVisibility(View.VISIBLE);
		GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
	}
	
	private void addFoot(int type) {
		if (mRTPullListView.getFooterViewsCount() > 0) {
			return;
		}
		if (1 == type) {
			mRTPullListView.addFooterView(1);
		} else {
			mRTPullListView.addFooterView(2);
		}
	}
	
	private void removeFoot(int type) {
		if (mRTPullListView != null) {
			if (1 == type) {
				mRTPullListView.removeFooterView(1);
			} else {
				mRTPullListView.removeFooterView(2);
			}
		}
	}
	
}
