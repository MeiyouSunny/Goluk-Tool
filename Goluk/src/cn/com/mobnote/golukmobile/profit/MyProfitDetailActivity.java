package cn.com.mobnote.golukmobile.profit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

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
	private ImageView mImageRefresh = null;
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
	/**加载更多**/
	private RelativeLayout mBottomLoadingView = null;
	/**进入页面的loading**/
	private CustomLoadingDialog mLoadingDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_profit_detail);
		
		historyDate = GolukUtils.getCurrentFormatTime();
		initView();
		
		Intent it = getIntent();
		uid = it.getStringExtra("uid").toString();
		
		mRTPullListView.firstFreshState();
		firstEnter();
		
	}
	
	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.profit_detail_back);
		mRTPullListView = (RTPullListView) findViewById(R.id.profit_detail_RTPullListView);
		mImageRefresh = (ImageView) findViewById(R.id.video_detail_click_refresh);
		mBottomLoadingView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading,null);
		mTextNoData = (TextView) findViewById(R.id.my_profit_detail_nodata);
		
		mBtnBack.setOnClickListener(this);
		mImageRefresh.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(this);
		mRTPullListView.setOnRTScrollListener(this);
		mRTPullListView.setOnItemClickListener(this);
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
			if (count == visibleCount) {
				if(null != detailInfo.data.incomelist && detailInfo.data.incomelist.size() >0) {
					if(detailInfo.data.incomelist.size() >20) {
						mRTPullListView.addFooterView(mBottomLoadingView);
						startPush();
					} else {
						mRTPullListView.removeFooterView(mBottomLoadingView);
					}
				}
			}
			
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		wonderfulFirstVisible = firstVisibleItem;
		wonderfulVisibleCount = visibleItemCount;
	}
	
	/**
	 * 首次进入
	 */
	private void firstEnter() {
		httpRequestData(OPERATOR_FIRST, "");
	}

	/**
	 * 开始下拉刷新
	 */
	private void startPull() {
		httpRequestData(OPERATOR_FIRST, "");
	}
	
	/**
	 * 开始上拉加载
	 */
	private void startPush() {
		httpRequestData(OPERATOR_UP, mAdapter.getLastDataTime());
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.profit_detail_back:
			exit();
			break;
		//点击刷新
		case R.id.video_detail_click_refresh:
			showLoadingDialog();
			Intent it = getIntent();
			uid = it.getStringExtra("uid").toString();
			firstEnter();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onLoadComplete(int requestType, Object result) {
		closeLoadingDialog();
		if (requestType == IPageNotifyFn.PageType_ProfitDetail) {
			detailInfo = (ProfitDetailInfo) result;
			if (null != detailInfo && detailInfo.success && null != detailInfo.data && null != detailInfo.data.incomelist) {
				int size = detailInfo.data.incomelist.size();
				if (size > 0) {
					mImageRefresh.setVisibility(View.GONE);
					mTextNoData.setVisibility(View.GONE);
					mRTPullListView.setVisibility(View.VISIBLE);
					mAdapter = new MyProfitDetailAdapter(this, detailInfo.data.incomelist);
					mRTPullListView.setAdapter(mAdapter);
					mRTPullListView.onRefreshComplete(historyDate);
				} else {
					mImageRefresh.setVisibility(View.GONE);
					mRTPullListView.setVisibility(View.GONE);
					mTextNoData.setVisibility(View.VISIBLE);
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
			if(null != income && !"".equals(income.vid)) {
				Intent itVideoDetail = new Intent(MyProfitDetailActivity.this,VideoDetailActivity.class);
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
		GolukUtils.showToast(this, "网络数据异常");
	}
	
}
