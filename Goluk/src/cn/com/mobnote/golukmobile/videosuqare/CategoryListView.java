package cn.com.mobnote.golukmobile.videosuqare;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.newest.NewestAdapter;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class CategoryListView implements VideoSuqareManagerFn, OnRefreshListener, OnRTScrollListener, OnClickListener {

	public static final String TAG = "CategoryListView";

	private Context mContext = null;
	private RelativeLayout mRootLayout = null;

	public List<VideoSquareInfo> mDataList = null;

	private RTPullListView mRTPullListView = null;
	private NewestAdapter mCategoryAdapter = null;
	private String historyDate;
	private ImageView noDataView = null;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");

	/** 视频广场类型 0.全部 1.直播 2.点播 */
	private String mType;
	/**
	 * 点播分类 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料 6.堵车预警 7.惊险十分 8.疯狂超车 9.感人瞬间
	 * 10.传递正能量
	 */
	private String mAttribute;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 是否还有分页 */
	private boolean isHaveData = true;
	private RelativeLayout loading = null;
	/**
	 * * 0:第一次
	 * 
	 * 1：上拉
	 * 
	 * 2：下拉
	 * 
	 */
	private int uptype = 0;

	LayoutInflater layoutInflater = null;
	private SharePlatformUtil sharePlatform;

	public CategoryListView(Context context, final String type, final String attr) {
		mContext = context;
		mType = type;
		mAttribute = attr;

		layoutInflater = LayoutInflater.from(mContext);

		mDataList = new ArrayList<VideoSquareInfo>();
		initView();
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if ("".equals(historyDate)) {
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
		addCallBackListener();
		loadHistoryData();

		initYMShare();

		firstRequest();
	}

	private void initYMShare() {
		sharePlatform = new SharePlatformUtil(mContext);
		sharePlatform.configPlatforms();// 设置分享平台的参数
	}

	private void showLoadingDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, null);
			mCustomProgressDialog.show();
		}
	}

	private void closeLoadingDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
			mCustomProgressDialog = null;
		}
	}

	private void addCallBackListener() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener(TAG, this);
		}
	}

	public void removeListener() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.removeVideoSquareManagerListener(TAG);
		}
	}

	// 第一次进入列表请求
	public void firstRequest() {
		boolean isSucess = httpPost(mType, mAttribute, "0", "");
		if (isSucess) {
			mHandler.sendEmptyMessageDelayed(100, 150);
		} else {
			GolukDebugUtils.e("", "加载失败");
		}
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			if (msg.what == 100) {
				showLoadingDialog();
			}
		}

	};

	private void initView() {
		mRootLayout = (RelativeLayout) layoutInflater.inflate(R.layout.video_type_list, null);
		mRTPullListView = (RTPullListView) mRootLayout.findViewById(R.id.mRTPullListView);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

		noDataView = (ImageView) mRootLayout.findViewById(R.id.category_list_nodata);
		noDataView.setOnClickListener(this);

		if (null == mCategoryAdapter) {
			mCategoryAdapter = new NewestAdapter(mContext);
			mCategoryAdapter.setCategoryListView(this);
		}
		mRTPullListView.setAdapter(mCategoryAdapter);
	}

	public View getView() {
		return mRootLayout;
	}

	private void loadHistoryData() {
		boolean headFlag = false;
		boolean dataFlag = false;
		String head = GolukApplication.getInstance().getVideoSquareManager().getZXList();
		String data = GolukApplication.getInstance().getVideoSquareManager().getTypeVideoList("0");

		// if (headFlag && dataFlag) {
		initLayout();
		// }

	}

	/**
	 * 获取网络数据(请求)
	 * 
	 * @param flag
	 *            是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private boolean httpPost(String type, String attribute, String operation, String timestamp) {
		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getSquareList("1", type, attribute, operation, timestamp);
		if (!result) {

		}

		return result;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (null != sharePlatform) {
			sharePlatform.mSinaWBUtils.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void initLayout() {
		// if (!headLoading && !dataLoading) {
		// return;
		// }

		mRTPullListView.onRefreshComplete(historyDate);

		// mCategoryAdapter.setData(mDataList);
		// if ("0".equals(curOperation)) {

		// }

		mRTPullListView.setonRefreshListener(this);

		mRTPullListView.setOnRTScrollListener(this);

	}

	private VideoSquareInfo begantime = null;
	private VideoSquareInfo endtime = null;

	private final int COUNT = 30;

	private void callBack_CatLog(int msg, int param1, Object param2) {
		closeLoadingDialog();
		if (1 != msg) {
			// 失败
			callBackFailed();
			dataCallBackRefresh();
			return;
		}

		// 解析数据
		List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData((String) param2);
		if (null != datalist && datalist.size() > 0) {
			final int listSize = datalist.size();

			GolukDebugUtils.e("", "jyf----CategoryListView------------------VideoSuqare_CallBack  listSize: "
					+ listSize + "   uptype:" + uptype);

			// 有数据
			begantime = datalist.get(0);
			endtime = datalist.get(listSize - 1);

			if (uptype == 0) {// 说明是第一次

				mDataList = datalist;
				mCategoryAdapter.setData(null, mDataList);
				if (listSize >= COUNT) {
					isHaveData = true;
					addFoot();
				} else {
					isHaveData = false;
					this.removeFoot();
				}
			} else if (uptype == 1) {// 上拉刷新
				if (listSize >= COUNT) {// 数据超过30条
					isHaveData = true;
				} else {// 数据没有30条
					isHaveData = false;
					removeFoot();
				}
				mDataList.addAll(datalist);
				mCategoryAdapter.setData(null, mDataList);
			} else if (uptype == 2) {// 下拉刷新
				if (listSize >= COUNT) {// 数据超过30条
					isHaveData = true;
				} else {// 数据没有30条
					isHaveData = false;
					this.removeFoot();
				}
				mDataList = datalist;
				mCategoryAdapter.setData(null, mDataList);
				mRTPullListView.onRefreshComplete(historyDate);
			}

		} else {
			// TODO 无数据
			noDataCallBack();
		}

		dataCallBackRefresh();
	}

	private CustomLoadingDialog mCustomProgressDialog = null;

	public void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
		}
	}

	public void showProgressDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, null);
		}

		if (!mCustomProgressDialog.isShowing()) {
			mCustomProgressDialog.show();
		}
	}

	/** 保存即将分享的数据实体，用于判断是否是直播和获取信息 */
	private VideoSquareInfo mWillShareSquareInfo = null;

	/**
	 * 在用户点击列表分享前，要保存将被分享的实体
	 * 
	 * @param info
	 * @author jyf
	 * @date 2015年8月9日
	 */
	public void setWillShareInfo(VideoSquareInfo info) {
		mWillShareSquareInfo = info;
	}

	private void callBack_getShareUrl(int msg, int param1, Object param2) {
		closeProgressDialog();
		if (RESULE_SUCESS != msg) {
			GolukUtils.showToast(mContext, "网络异常，请检查网络");
			return;
		}
		ShareDataBean shareBean = JsonUtil.parseShareCallBackData((String) param2);
		if (!shareBean.isSucess) {
			GolukUtils.showToast(mContext, "网络异常，请检查网络");
		}
		// 获取描述
		final String describe = getShareDescribe(shareBean.describe);
		final String ttl = getTTL();
		final String realDesc = getRealDesc();
		if (mContext instanceof VideoCategoryActivity) {
			VideoCategoryActivity activity = (VideoCategoryActivity) mContext;
			if (activity != null && !activity.isFinishing()) {
				CustomShareBoard shareBoard = new CustomShareBoard(activity, sharePlatform, shareBean.shareurl,
						shareBean.coverurl, describe, ttl, null, realDesc);
				shareBoard.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			}
		}
	}

	private String getRealDesc() {
		if (isShareLive()) {
			return getTTL() + "(使用#极路客Goluk#拍摄)";
		} else {
			return "极路客精彩视频(使用#极路客Goluk#拍摄)";
		}
	}

	private String getTTL() {
		if (isShareLive()) {
			return mWillShareSquareInfo.mUserEntity.nickname + "的直播视频分享";
		} else {
			return "极路客精彩视频分享";
		}
	}

	private String getShareDescribe(String describe) {
		if (isShareLive()) {
			if (TextUtils.isEmpty(describe)) {
				return "#极路客直播#";
			}
		} else {
			if (TextUtils.isEmpty(describe)) {
				return "#极路客精彩视频#";
			}
		}
		return describe;
	}

	private boolean isShareLive() {
		if (null == mWillShareSquareInfo) {
			return false;
		}
		return mWillShareSquareInfo.mVideoEntity.type.equals("1");
	}

	private void callBack_praise(int msg, int param1, Object param2) {
		if (RESULE_SUCESS == msg) {
			GolukDebugUtils.e("", "GGGG===@@@====2222=====");
			if (null != mVideoSquareInfo) {
				mVideoSquareInfo.mVideoEntity.ispraise = "1";
				updateClickPraiseNumber(true, mVideoSquareInfo);
			}

		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "jyf----CategoryListView------------------VideoSuqare_CallBack: " + event + " msg:" + msg
				+ "  param2:" + param2);
		if (VSquare_Req_List_Video_Catlog == event) {
			callBack_CatLog(msg, param1, param2);
		} else if (VSquare_Req_VOP_GetShareURL_Video == event) {
			callBack_getShareUrl(msg, param1, param2);
		} else if (VSquare_Req_VOP_Praise == event) {
			callBack_praise(msg, param1, param2);
		}

	}

	// 数据回调失败的问题
	private void callBackFailed() {
		isHaveData = false;

		if (0 == uptype) {
			// closeProgressDialog();
		} else if (1 == uptype) {
			this.removeFoot();
		} else if (2 == uptype) {
			mRTPullListView.onRefreshComplete(historyDate);
		}
		GolukUtils.showToast(mContext, "网络异常，请检查网络");
	}

	private void noDataCallBack() {
		if (uptype == 1) {// 上拉刷新
			this.removeFoot();
		} else if (uptype == 2) {// 下拉刷新
			// 如果是 直播，下拉刷新后，没有数据，則直接清空列表，证明当前没有直播
			if ("1".equals(mType)) {
				mDataList.clear();
			}
			mRTPullListView.onRefreshComplete(historyDate);
		}
	}

	private void dataCallBackRefresh() {
		if (mDataList.size() > 0) {
			noDataView.setVisibility(View.GONE);
			mRTPullListView.setVisibility(View.VISIBLE);
		} else {
			noDataView.setVisibility(View.VISIBLE);
			mRTPullListView.setVisibility(View.GONE);
		}
	}

	private void addFoot() {
		loading = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.video_square_below_loading, null);
		mRTPullListView.addFooterView(loading);
	}

	private void removeFoot() {
		if (loading != null) {
			if (mRTPullListView != null) {
				mRTPullListView.removeFooterView(loading);
				loading = null;
			}
		}
	}

	public void onResume() {
		addCallBackListener();
	}

	public void onPause() {
		removeListener();
	}

	public void onStop() {

	}

	public void onDestroy() {
		this.removeListener();
		this.closeLoadingDialog();

	}

	public void onBackPressed() {

	}

	@Override
	public void onRefresh() {
		GolukDebugUtils.e("", "jyf----CategoryListView------------------onRefresh  下拉刷新: ");
		// 下拉刷新
		uptype = 2;
		httpPost(mType, mAttribute, "0", "");
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		switch (scrollState) {
		case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
			mCategoryAdapter.lock();
			break;
		case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
			mCategoryAdapter.unlock();
			if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
				if (isHaveData) {
					// 上拉刷新
					uptype = 1;
					String timeSign = endtime.mVideoEntity.sharingtime;
					GolukDebugUtils.e("", "jyf----CategoryListView------------------onRefresh  上拉刷新: " + timeSign);
					httpPost(mType, mAttribute, "2", timeSign);
				}
			}
			break;
		case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mCategoryAdapter.lock();
			break;

		default:
			break;
		}

	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		wonderfulFirstVisible = firstVisibleItem;
		wonderfulVisibleCount = visibleItemCount;
	}

	VideoSquareInfo mVideoSquareInfo;

	public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
		mVideoSquareInfo = info;
		if (!flag) {
			return;
		}

		for (int i = 0; i < mDataList.size(); i++) {
			VideoSquareInfo vs = mDataList.get(i);
			if (vs.id.equals(info.id)) {
				int number = Integer.parseInt(info.mVideoEntity.praisenumber);
				if ("1".equals(info.mVideoEntity.ispraise)) {
					number++;
				} else {
					number--;
				}
				mDataList.get(i).mVideoEntity.praisenumber = "" + number;
				mDataList.get(i).mVideoEntity.ispraise = info.mVideoEntity.ispraise;
				info.mVideoEntity.praisenumber = "" + number;
				break;
			}
		}
		mCategoryAdapter.updateClickPraiseNumber(info);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.category_list_nodata) {
			this.firstRequest();
		}

	}

}
