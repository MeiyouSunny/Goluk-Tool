package cn.com.mobnote.golukmobile.usercenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.ClusterListActivity;
import cn.com.mobnote.golukmobile.special.SpecialDataManage;
import cn.com.mobnote.golukmobile.special.SpecialInfo;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;

/**
 * 
 * @author 曾浩
 * 
 */
public class UserCenterActivity extends BaseActivity implements
		VideoSuqareManagerFn {

	private RTPullListView mRTPullListView = null;
	private UserCenterAdapter uca = null;
	private SharePlatformUtil sharePlatform = null;

	private String uchistoryDate;

	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;

	private RelativeLayout loading = null;

	/** 是否还有分页 */
	private boolean isHaveData = true;

	private ClusterInfo endtime = null;

	private UserInfo userdata = null;
	private List<ClusterInfo> videodata = null;
	private List<PraiseInfo> praisedata;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");

	private UserCenterDataFormat ucdf = new UserCenterDataFormat();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("UserCenterActivity", this);

		videodata = new ArrayList<ClusterInfo>();
		praisedata = new ArrayList<PraiseInfo>();
	}

	/**
	 * 初始化数据
	 */
	private void init() {
		sharePlatform = new SharePlatformUtil(this);
		uca = new UserCenterAdapter(this, sharePlatform);
		mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
		// mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRTPullListView.setAdapter(uca);

		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				uchistoryDate = SettingUtils.getInstance().getString(
						"gcHistoryDate", sdf.format(new Date()));
				SettingUtils.getInstance().putString("ucHistoryDate",
						sdf.format(new Date()));
				httpPost("");// 请求数据
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
						if (isHaveData) {
							httpPost("");// 请求数据
						}
					}
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int arg3) {
				wonderfulFirstVisible = firstVisibleItem;
				wonderfulVisibleCount = visibleItemCount;
			}
		});

		// 有下一页刷新
		if (isHaveData) {
			loading = (RelativeLayout) LayoutInflater.from(this).inflate(
					R.layout.video_square_below_loading, null);
			mRTPullListView.addFooterView(loading);
		}

		SpecialInfo si = new SpecialInfo();
		si.videoid = "zh";

		uca.setUserData(userdata);
		uca.setVideoData(videodata);
		uca.setPraisData(praisedata);
		uca.notifyDataSetChanged();
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
		boolean result = GolukApplication.getInstance().getVideoSquareManager().getUserCenter(otheruid);
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		if (event == VSquare_Req_MainPage_Infor) {
			if (RESULE_SUCESS == msg) {
				List<ClusterInfo> videos = ucdf.getClusterList((String) param2);
				List<PraiseInfo> praise = ucdf.getPraises((String) param2);
				UserInfo user = ucdf.getUserInfo((String) param2);
				
				// 说明有数据
				if (videos != null && videos.size() > 0) {
					if (videos.size() >= 20) {
						isHaveData = true;
					} else {
						isHaveData = false;
					}
					videodata = videos;
				}
				// 说明有数据
				if ( praise != null && praise.size() > 0 ) {
					praisedata = praise;
				}
				// 说明有数据
				if (user != null) {
					userdata = user;
				}
				
				init();

			} else {
				isHaveData = false;
				GolukUtils.showToast(UserCenterActivity.this, "网络异常，请检查网络");
			}

		} else if (event == VSquare_Req_VOP_GetShareURL_Video
				|| event == VSquare_Req_VOP_GetShareURL_Topic_Tag) {
//			if (RESULE_SUCESS == msg) {
//				try {
//					JSONObject result = new JSONObject((String) param2);
//					if (result.getBoolean("success")) {
//						JSONObject data = result.getJSONObject("data");
//						GolukDebugUtils.i("detail",
//								"------VideoSuqare_CallBack--------data-----"
//										+ data);
//						String shareurl = data.getString("shorturl");
//						String coverurl = data.getString("coverurl");
//						String describe = data.optString("describe");
//						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";
//
//						if (TextUtils.isEmpty(describe)) {
//							describe = "#极路客精彩视频#";
//						}
//						String ttl = "极路客精彩视频分享";
//
//						// 缩略图
//						Bitmap bitmap = getThumbBitmap(coverurl);
//
//						if (this != null && !this.isFinishing()) {
//							mCustomProgressDialog.close();
//							CustomShareBoard shareBoard = new CustomShareBoard(
//									this, sharePlatform, shareurl, coverurl,
//									describe, ttl, bitmap, realDesc,
//									getShareVideoId());
//							System.out.println("我日我日我日====bitmap=" + bitmap);
//							shareBoard.showAtLocation(this.getWindow()
//									.getDecorView(), Gravity.BOTTOM, 0, 0);
//							System.out.println("我擦我擦我擦");
//						}
//					} else {
//						GolukUtils.showToast(this, "网络异常，请检查网络");
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			} else {
//				mCustomProgressDialog.close();
//				GolukUtils.showToast(this, "网络异常，请检查网络");
//			}
		}

	}

	public void sj() {
		// if (uptype == 1) {// 上拉刷新
		// if (list.size() >= 20) {// 数据超过20条
		// isHaveData = true;
		// } else {// 数据没有20条
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
		// mDataList.clear();
		//
		// if (list.size() >= 20) {// 数据超过20条
		// isHaveData = true;
		// } else {// 数据没有20条
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
	}

	private List<ClusterInfo> getData() {
		List<ClusterInfo> list = new ArrayList<ClusterInfo>();

		ClusterInfo ci = null;
		for (int i = 0; i < 15; i++) {
			ci = new ClusterInfo();
			ci.videoid = i + "";
			list.add(ci);
		}
		return list;
	}

}
