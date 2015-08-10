package cn.com.mobnote.golukmobile.videosuqare;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class VideoSquarePlayActivity extends BaseActivity implements OnClickListener, VideoSuqareManagerFn {
	private RTPullListView mRTPullListView = null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter = null;
	private List<VideoSquareInfo> mDataList = null;
	public CustomLoadingDialog mCustomProgressDialog = null;
	private VideoSquareInfo begantime = null;
	private VideoSquareInfo endtime = null;
	private ImageButton mBackBtn = null;
	private RelativeLayout loading = null;

	public String shareVideoId;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 是否还有分页 */
	private boolean isHaveData = true;
	/** 视频广场类型 */
	private String type;
	/**
	 * 1：上拉 2：下拉 0:第一次
	 */
	private int uptype = 0;

	/** 广场视频列表默认背景图片 */
	private ImageView squareTypeDefault;

	private TextView title;
	// 点播分类
	private String attribute;

	SharePlatformUtil sharePlatform;

	private String historyDate;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_play);
		// Intent intent = getIntent();
		// title = (TextView) findViewById(R.id.title);
		// type = intent.getStringExtra("type");// 视频广场类型
		// attribute = intent.getStringExtra("attribute");// 点播类型
		// historyDate = SettingUtils.getInstance().getString("gcHistoryDate",
		// sdf.format(new Date()));
		//
		// SettingUtils.getInstance().putString("gcHistoryDate", sdf.format(new
		// Date()));
		// if ("1".equals(attribute)) {
		// title.setText("曝光台");
		// } else if ("2".equals(attribute)) {
		// title.setText("事故大爆料");
		// } else if ("3".equals(attribute)) {
		// title.setText("美丽风景");
		// } else if ("4".equals(attribute)) {
		// title.setText("随手拍");
		// } else {
		// title.setText("直播列表");
		// }
		//
		// GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videocategory",
		// this);
		// mDataList = new ArrayList<VideoSquareInfo>();
		// mRTPullListView = (RTPullListView)
		// findViewById(R.id.mRTPullListView);
		// squareTypeDefault = (ImageView)
		// findViewById(R.id.square_type_default);
		// squareTypeDefault.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// mCustomProgressDialog = null;
		// httpPost(true, type, "0", "");
		// }
		// });
		//
		// /** 返回按钮 */
		// mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		// mBackBtn.setOnClickListener(this);
		//
		// sharePlatform = new SharePlatformUtil(this);
		// sharePlatform.configPlatforms();// 设置分享平台的参数
		// // 如果是直播就不拿历史数据
		// if (!"1".equals(type)) {
		// loadHistorydata();// 显示历史请求数据
		// }
		// httpPost(true, type, "0", "");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if (null != sharePlatform) {
		// sharePlatform.mSinaWBUtils.onActivityResult(requestCode, resultCode,
		// data);
		// }
	}

	/**
	 * 获取网络数据
	 * 
	 * @param flag
	 *            是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void httpPost(boolean flag, String type, String operation, String timestamp) {
		// if (flag) {
		// if (null == mCustomProgressDialog) {
		// mCustomProgressDialog = new CustomLoadingDialog(this, null);
		// mCustomProgressDialog.show();
		// }
		// }
		//
		// boolean result =
		// GolukApplication.getInstance().getVideoSquareManager()
		// .getSquareList("1", type, attribute, operation, timestamp);
		// if (!result) {
		// closeProgressDialog();
		// }
	}

	// private void init(boolean isloading) {
	//
	// if (null == mVideoSquareListViewAdapter) {
	// mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(this, 2,
	// sharePlatform);
	// }
	//
	// mVideoSquareListViewAdapter.setData(mDataList);
	// mRTPullListView.setAdapter(mVideoSquareListViewAdapter, historyDate);
	// mRTPullListView.setonRefreshListener(new OnRefreshListener() {
	// @Override
	// public void onRefresh() {
	// historyDate = SettingUtils.getInstance().getString("gcHistoryDate",
	// sdf.format(new Date()));
	// SettingUtils.getInstance().putString("gcHistoryDate", sdf.format(new
	// Date()));
	// if (begantime != null) {
	// uptype = 2;
	// if ("1".equals(type)) {// 直播
	// httpPost(true, type, "0", "");
	// } else {
	// httpPost(true, type, "1", begantime.mVideoEntity.sharingtime);
	// }
	//
	// } else {
	// mRTPullListView.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// mRTPullListView.onRefreshComplete(historyDate);
	// }
	// }, 1500);
	// }
	//
	// }
	// });
	//
	// mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
	// @Override
	// public void onScrollStateChanged(AbsListView arg0, int scrollState) {
	// if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
	//
	// if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible +
	// wonderfulVisibleCount)) {
	// if (isHaveData) {
	// uptype = 1;
	// httpPost(true, type, "2", endtime.mVideoEntity.sharingtime);
	// }
	// }
	// }
	// }
	//
	// @Override
	// public void onScroll(AbsListView arg0, int firstVisibleItem, int
	// visibleItemCount, int arg3) {
	// wonderfulFirstVisible = firstVisibleItem;
	// wonderfulVisibleCount = visibleItemCount;
	// }
	// });
	//
	// if (isloading == false) {
	// // 有下一页刷新
	// if (isHaveData) {
	// loading = (RelativeLayout)
	// LayoutInflater.from(this).inflate(R.layout.video_square_below_loading,
	// null);
	// mRTPullListView.addFooterView(loading);
	// }
	// }
	//
	// }

	public void flush() {
		// mVideoSquareListViewAdapter.setData(mDataList);
	}

	@Override
	public void onClick(View view) {
		// switch (view.getId()) {
		// case R.id.back_btn:
		// this.finish();
		// break;
		//
		// default:
		// break;
		// }
	}

	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
		// if (!isSucess) {
		// GolukUtils.showToast(this, "第三方分享失败");
		// return;
		// }
		// GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel,
		// shareVideoId);
	}

	/**
	 * 关闭加载中对话框
	 * 
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog() {
		// if (null != mCustomProgressDialog) {
		// mCustomProgressDialog.close();
		// }
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// if (null != mVideoSquareListViewAdapter) {
		// mVideoSquareListViewAdapter.onBackPressed();
		// }
	}

	@Override
	protected void onStop() {
		super.onStop();
		// if (null != mVideoSquareListViewAdapter) {
		// mVideoSquareListViewAdapter.onStop();
		// }

		// GolukApplication.getInstance().getVideoSquareManager()
		// .removeVideoSquareManagerListener("videosharehotlist");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (null != mVideoSquareListViewAdapter) {
		// mVideoSquareListViewAdapter.onResume();
		// }

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// if (null != mVideoSquareListViewAdapter) {
		// // mVideoSquareListViewAdapter.onDestroy();
		// }
		//
		// for (VideoSquareInfo info : mDataList) {
		// String url = info.mVideoEntity.picture;
		// BitmapManager.getInstance().mBitmapUtils.clearMemoryCache(url);
		// }

	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		// if (event == SquareCmd_Req_SquareList) {
		// closeProgressDialog();
		// if (RESULE_SUCESS == msg) {
		//
		// List<VideoSquareInfo> list =
		// DataParserUtils.parserVideoSquareListData((String) param2);
		//
		// // 说明有数据
		// if (list.size() > 0) {
		// begantime = list.get(0);
		// endtime = list.get(list.size() - 1);
		//
		// if (uptype == 0) {// 说明是第一次
		// if (list.size() >= 30) {
		// isHaveData = true;
		// } else {
		// isHaveData = false;
		// }
		// mDataList = list;
		// init(false);
		// } else if (uptype == 1) {// 上拉刷新
		// if (list.size() >= 30) {// 数据超过30条
		// isHaveData = true;
		// } else {// 数据没有30条
		// isHaveData = false;
		// if (loading != null) {
		// if (mRTPullListView != null) {
		// mRTPullListView.removeFooterView(loading);
		// loading = null;
		// }
		// }
		// }
		// mDataList.addAll(list);
		// flush();
		// } else if (uptype == 2) {// 下拉刷新
		// if (list.size() >= 30) {// 数据超过30条
		// isHaveData = true;
		// } else {// 数据没有30条
		// isHaveData = false;
		// }
		//
		// if ("1".equals(type)) {// 直播
		// mDataList = list;
		// } else {
		// list.addAll(mDataList);
		// mDataList = list;
		// }
		//
		// mRTPullListView.onRefreshComplete(historyDate);
		// flush();
		// }
		//
		// } else {// 没有数据
		//
		// if (uptype == 1) {// 上拉刷新
		// if (loading != null) {
		// if (mRTPullListView != null) {
		// mRTPullListView.removeFooterView(loading);
		// loading = null;
		// }
		// }
		// } else if (uptype == 2) {// 下拉刷新
		// if ("1".equals(type)) {// 直播
		// mDataList.clear();
		// }
		// mRTPullListView.onRefreshComplete(historyDate);
		// }
		// }
		//
		// } else {
		// isHaveData = false;
		//
		// if (0 == uptype) {
		// closeProgressDialog();
		// } else if (1 == uptype) {
		// if (mRTPullListView != null) {
		// mRTPullListView.removeFooterView(loading);
		// loading = null;
		// }
		// } else if (2 == uptype) {
		// mRTPullListView.onRefreshComplete(historyDate);
		// }
		// GolukUtils.showToast(this, "网络异常，请检查网络");
		// }
		//
		// if (mDataList.size() > 0) {
		// squareTypeDefault.setVisibility(View.GONE);
		// mRTPullListView.setVisibility(View.VISIBLE);
		// } else {
		// squareTypeDefault.setVisibility(View.VISIBLE);
		// mRTPullListView.setVisibility(View.GONE);
		// }
		// }

	}

	/**
	 * 初始化历史请求数据
	 * 
	 * @Title: loadHistorydata
	 * @Description: TODO void
	 * @author 曾浩
	 * @throws
	 */
	public void loadHistorydata() {
		// String param =
		// GolukApplication.getInstance().getVideoSquareManager().getSquareList(attribute);
		// if(param != null && !"".equals(param)){
		// List<VideoSquareInfo> list =
		// DataParserUtils.parserVideoSquareListData((String)param);
		// if(list!=null && list.size()>0){
		// mDataList = list;
		// begantime = list.get(0);
		// endtime = list.get(list.size()-1);
		// init(true);
		// }
		// }

	}

}
