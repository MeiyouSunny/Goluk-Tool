package cn.com.mobnote.golukmobile.cluster;

import java.util.List;

import com.android.volley.Request;
import com.lidroid.xutils.util.LogUtils;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.cluster.ClusterAdapter.IClusterInterface;
import cn.com.mobnote.golukmobile.cluster.bean.ClusterHeadBean;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.cluster.bean.VolleyDataFormat;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener.IClickPraiseView;
import cn.com.mobnote.golukmobile.newest.ClickShareListener.IClickShareView;
import cn.com.mobnote.golukmobile.newest.IDialogDealFn;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UserCenterAdapter;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

public class ClusterActivity extends BaseActivity implements OnClickListener, IRequestResultListener, IClickShareView,
		IClickPraiseView, IDialogDealFn, IClusterInterface {

	private static final String TAG = "ClusterActivity";
	private RTPullListView mRTPullListView = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	
	private VolleyDataFormat vdf = new VolleyDataFormat();
	

	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;

	/** 返回按钮 */
	private ImageButton backbtn;
	/** 分享按钮 **/
	private Button shareBtn;

	public ClusterHeadBean headData = null;
	public List<VideoSquareInfo> recommendlist = null;
	public List<VideoSquareInfo> newslist = null;

	private int currentViewType = 1; // 当前视图类型（推荐列表，最新列表）

	public ClusterAdapter clusterAdapter;

	private SharePlatformUtil sharePlatform = null;

	private RelativeLayout mBottomLoadingView = null;

	/** 活动id **/
	private String activityid = null;

	private String uid = null;
	
	ClusterBeanRequest request = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cluster_main);
		this.initData();// 初始化view
		this.initListener();// 初始化view的监听

		Intent intent = this.getIntent();

		activityid = intent.getStringExtra("activityid");
		uid = intent.getStringExtra("uid");

		mRTPullListView.firstFreshState();

	}

	public static class NoVideoDataViewHolder {
		TextView tips;
		ImageView tipsimage;
		boolean bMeasureHeight;
	}

	/**
	 * 获取网络数据
	 * 
	 * @param flag
	 *            是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void httpPost(String otheruid) {
		request = new ClusterBeanRequest(IPageNotifyFn.PageType_ClusterMain, this);
		request.get(activityid, uid);
	}

	private void initData() {
		mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
		backbtn = (ImageButton) findViewById(R.id.back_btn);
		shareBtn = (Button) findViewById(R.id.title_share);
		mBottomLoadingView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading,null);

		if (sharePlatform == null) {
			sharePlatform = new SharePlatformUtil(this);
			clusterAdapter = new ClusterAdapter(this, sharePlatform, 1, this);
			mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
			mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			mRTPullListView.setAdapter(clusterAdapter);
		}
	}

	/**
	 * 获取更多视频列表
	 * 
	 */
	private void httpGetNextVideo(String sharingtime) {
		GolukApplication.getInstance().getVideoSquareManager().getUserCenterShareVideo("", "2", sharingtime);
	}

	private void initListener() {
		backbtn.setOnClickListener(this);
		shareBtn.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// 下拉刷新个人中心所有数据
				httpPost("");// 请求数据
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {// 推荐
						if (clusterAdapter.getCurrentViewType() == ClusterAdapter.ViewType_RecommendVideoList) {// 视频列表
							if (recommendlist != null && recommendlist.size() > 0) {// 加载更多视频数据
								if (recommendlist.size() > 0) {
									mRTPullListView.addFooterView(mBottomLoadingView);
									httpGetNextVideo(recommendlist.get(recommendlist.size() - 1).mVideoEntity.sharingtime);
								}
							}
						} else {// 最新列表
							if (newslist != null && newslist.size() > 0) {// 加载更多视频数据
								if (newslist.size() > 0) {
									mRTPullListView.addFooterView(mBottomLoadingView);
									httpGetNextVideo(newslist.get(newslist.size() - 1).mVideoEntity.sharingtime);
								}
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
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.title_share:

			break;
		default:
			break;
		}

	}
	
	public void updateViewData(boolean succ, int count) {
		if (succ) {
			clusterAdapter.notifyDataSetChanged();
			if (count > 0) {
				this.mRTPullListView.setSelection(count);
			}
		}
		mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime());
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		if(requestType == IPageNotifyFn.PageType_ClusterMain){
			JsonData data = (JsonData) result;
			if(data.success){
				if(data.data != null){
					ClusterHeadBean chb = data.data;
					recommendlist = vdf.getClusterList(chb.recommendvideo);
					newslist = vdf.getClusterList(chb.latestvideo);
					clusterAdapter.setDataInfo(chb.activity,recommendlist, newslist);
					updateViewData(true, 0);
				}else{
					updateViewData(false, 0);
				}
			}
		}
	}

	@Override
	public void showProgressDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(this, null);
		}
		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
		}

	}

	@Override
	public void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
		}
	}

	private VideoSquareInfo mWillShareVideoSquareInfo;

	@Override
	public void setWillShareInfo(VideoSquareInfo info) {
		mWillShareVideoSquareInfo = info;
	}

	VideoSquareInfo mVideoSquareInfo;

	@Override
	public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
		mVideoSquareInfo = info;
		if (!flag) {
			return;
		}
		if (currentViewType == ClusterAdapter.ViewType_RecommendVideoList) {
			for (int i = 0; i < recommendlist.size(); i++) {
				VideoSquareInfo vs = this.recommendlist.get(i);
				if (this.updatePraise(vs, recommendlist, i)) {
					break;
				}
			}
		} else {
			for (int i = 0; i < this.newslist.size(); i++) {
				VideoSquareInfo vs = this.newslist.get(i);
				if (this.updatePraise(vs, newslist, i)) {
					break;
				}
			}
		}

	}

	public boolean updatePraise(VideoSquareInfo vs, List<VideoSquareInfo> videos, int index) {
		if (vs.id.equals(mVideoSquareInfo.id)) {
			int number = Integer.parseInt(mVideoSquareInfo.mVideoEntity.praisenumber);
			if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
				number++;
			} else {
				number--;
			}

			videos.get(index).mVideoEntity.praisenumber = "" + number;
			videos.get(index).mVideoEntity.ispraise = mVideoSquareInfo.mVideoEntity.ispraise;
			mVideoSquareInfo.mVideoEntity.praisenumber = "" + number;
			clusterAdapter.notifyDataSetChanged();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int OnGetListViewWidth() {
		return mRTPullListView.getWidth();
	}

	@Override
	public int OnGetListViewHeight() {
		return mRTPullListView.getHeight();
	}

	@Override
	public void CallBack_Del(int event, Object data) {
		// TODO Auto-generated method stub
	}

	@Override
	public void OnRefrushMainPageData() {
		// TODO Auto-generated method stub

	}

}
