package cn.com.mobnote.golukmobile.newest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.MainActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener.IClickPraiseView;
import cn.com.mobnote.golukmobile.newest.ClickShareListener.IClickShareView;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class NewestListView implements VideoSuqareManagerFn, IClickShareView, IClickPraiseView {
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	private NewestListHeadDataInfo mHeadDataInfo = null;
	public List<VideoSquareInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private NewestAdapter mNewestAdapter = null;
	private boolean headLoading = false;
	private boolean dataLoading = false;
	private String curOperation = "0";
	private String historyDate;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");
	private int pageCount = 0;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	/** 列表添加页脚标识 */
	private boolean addFooter = false;
	/** 添加列表底部加载中布局 */
	private RelativeLayout mBottomLoadingView = null;
	private int curpageCount = 0;
	private SharePlatformUtil sharePlatform;
	private ImageView shareBg = null;
	private static final String TAG = "NewestListView";

	public NewestListView(Context context) {
		mContext = context;
		mHeadDataInfo = new NewestListHeadDataInfo();
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
//		mRTPullListView.setDividerHeight(78);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mRTPullListView.setLayoutParams(lp);
		mRootLayout = new RelativeLayout(mContext);
		shareBg = (ImageView) View.inflate(context, R.layout.video_square_bj, null);

		sharePlatform = new SharePlatformUtil(mContext);
		sharePlatform.configPlatforms();// 设置分享平台的参数

		initListener();
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if ("".equals(historyDate)) {
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));

		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("NewestListView", this);
		}

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mRootLayout.addView(shareBg, rlp);
		mRootLayout.addView(mRTPullListView);

		setListAdapter();
		setViewListBg(false);
		loadHistoryData();
		httpPost(true, "0", "");

		shareBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setViewListBg(false);
				httpPost(true, "0", "");
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (null != sharePlatform) {
			sharePlatform.onActivityResult(requestCode, resultCode, data);
		}
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

	private long zXRequestId = 0;
	private long typeVideoRequestId = 0;

	private void httpPost(boolean flag, String operation, String timestamp) {
		curOperation = operation;
		if (flag) {
			mRTPullListView.firstFreshState();
		}

		if (null != GolukApplication.getInstance().getVideoSquareManager()) {
			if ("0".equals(operation)) {
				if (!headLoading) {
					headLoading = true;
					zXRequestId = GolukApplication.getInstance().getVideoSquareManager().getZXListData();
				}
			}

			GolukDebugUtils.e("", "GGGGGG=====111111=======dataLoading=" + dataLoading);
			if (dataLoading) {
				return;
			}

			List<String> attribute = new ArrayList<String>();
			attribute.add("0");
			dataLoading = true;
			typeVideoRequestId = GolukApplication.getInstance().getVideoSquareManager()
					.getTypeVideoList("1", "2", attribute, operation, timestamp);
			GolukDebugUtils.e("", "GGGGGG=====222222=======tv=" + typeVideoRequestId);
			if (typeVideoRequestId <= 0) {
				closeProgressDialog();
			}
		} else {
			closeProgressDialog();
		}
	}

	@Override
	public void showProgressDialog() {
		if (null == mCustomProgressDialog) {
			mCustomProgressDialog = new CustomLoadingDialog(mContext, null);
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

	private void setListAdapter() {
		if (null == mNewestAdapter) {
			mNewestAdapter = new NewestAdapter(mContext);
			mNewestAdapter.setNewestLiseView(this);
		}

		mRTPullListView.setAdapter(mNewestAdapter);

	}

	private void initLayout() {
		if (headLoading || dataLoading) {
			return;
		}

		if (!addFooter) {
			addFooter = true;
			mBottomLoadingView = (RelativeLayout) LayoutInflater.from(mContext).inflate(
					R.layout.video_square_below_loading, null);
			mRTPullListView.addFooterView(mBottomLoadingView);
		}

		if (curpageCount < pageCount) {
			if (addFooter) {
				addFooter = false;
				mRTPullListView.removeFooterView(mBottomLoadingView);
			}
		}

		setViewListBg(false);
		closeProgressDialog();
		historyDate = sdf.format(new Date());
		SettingUtils.getInstance().putString("hotHistoryDate", historyDate);
		mRTPullListView.onRefreshComplete(historyDate);
		if ("0".equals(curOperation)) {
			mNewestAdapter.setData(mHeadDataInfo, mDataList);
		} else {
			mNewestAdapter.loadData(mDataList);
		}
	}

	private void initListener() {
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				httpPost(false, "0", "");
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					if (mRTPullListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
						if (mDataList.size() > 0) {
							if (!addFooter) {
								addFooter = true;
								mBottomLoadingView = (RelativeLayout) LayoutInflater.from(mContext).inflate(
										R.layout.video_square_below_loading, null);
								mRTPullListView.addFooterView(mBottomLoadingView);
							}
							httpPost(false, "2", mDataList.get(mDataList.size() - 1).mVideoEntity.sharingtime);
						}
					}

					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					break;
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				firstVisible = firstVisibleItem;
				visibleCount = visibleItemCount;
			}

		});
	}

	public View getView() {
		return mRootLayout;
	}

	private VideoSquareInfo mWillShareVideoSquareInfo;

	@Override
	public void setWillShareInfo(VideoSquareInfo info) {
		mWillShareVideoSquareInfo = info;
	}

	private void showErrorTips() {
		if (!headLoading && !dataLoading) {
			closeProgressDialog();
			mRTPullListView.onRefreshComplete(historyDate);
		}

		GolukUtils.showToast(mContext, "网络异常，请检查网络");
	}

	private void checkData() {
		if (!headLoading && !dataLoading) {
			if (mHeadDataInfo.categoryList.size() > 0 || mDataList.size() > 0) {
				setViewListBg(false);
			} else {
				setViewListBg(true);
			}
		}
	}

	private void callBack_List_Catlog(int msg, int param1, Object param2) {
		if (param1 != zXRequestId) {
			return;
		}
		headLoading = false;
		if (RESULE_SUCESS == msg) {
			mHeadDataInfo = JsonParserUtils.parserNewestHeadData((String) param2);
			initLayout();
		} else {
			showErrorTips();
		}
		checkData();
	}

	private void callBack_List_Video(int msg, int param1, Object param2) {
		if (param1 != typeVideoRequestId) {
			return;
		}
		dataLoading = false;
		if (RESULE_SUCESS == msg) {
			List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData((String) param2);
			if ("0".equals(curOperation)) {
				mDataList.clear();
				pageCount = datalist.size();
			}

			mDataList.addAll(datalist);
			curpageCount = datalist.size();
			initLayout();
		} else {
			showErrorTips();

			if ("2".equals(curOperation)) {
				if (addFooter) {
					addFooter = false;
					mRTPullListView.removeFooterView(mBottomLoadingView);
				}
			}

		}
		checkData();
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {

		GolukDebugUtils.e("", "NewList----------------------------param2: " + (String) param2);

		if (event == VSquare_Req_List_Catlog) {
			// 最新分类
			callBack_List_Catlog(msg, param1, param2);
		} else if (event == VSquare_Req_List_Video_Catlog) {
			// 最新列表
			callBack_List_Video(msg, param1, param2);

		} else if (event == VSquare_Req_VOP_GetShareURL_Video) {
			if (mContext instanceof MainActivity) {
				Context topContext = ((MainActivity) mContext).mApp.getContext();
				if (!(topContext instanceof MainActivity)) {
					return;
				}
			}
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {
				try {
					JSONObject result = new JSONObject((String) param2);
					if (result.getBoolean("success")) {
						JSONObject data = result.getJSONObject("data");
						String shareurl = data.getString("shorturl");
						String coverurl = data.getString("coverurl");
						String describe = data.optString("describe");

						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";

						if (TextUtils.isEmpty(describe)) {
							// if
							// ("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type))
							// {
							// describe = "#极路客直播#";
							// } else {
							describe = "#极路客精彩视频#";
							// }
						}
						String ttl = "极路客精彩视频";

						// if
						// ("1".equals(mVideoSquareOnClickListener.mVideoSquareInfo.mVideoEntity.type))
						// {// 直播
						// ttl =
						// mVideoSquareOnClickListener.mVideoSquareInfo.mUserEntity.nickname
						// + "的直播视频分享";
						// realDesc = ttl + "(使用#极路客Goluk#拍摄)";
						// }

						if (mContext instanceof MainActivity) {
							MainActivity vspa = (MainActivity) mContext;
							if (vspa != null && !vspa.isFinishing()) {
								String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
										: "";
								String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
										: "";
								describe = username + "：" + describe;
								CustomShareBoard shareBoard = new CustomShareBoard(vspa, sharePlatform, shareurl,
										coverurl, describe, ttl, null, realDesc, videoId);
								shareBoard.showAtLocation(vspa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
							}

						}

					} else {
						GolukUtils.showToast(mContext, "网络异常，请检查网络");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
		} else if (event == VSquare_Req_VOP_Praise) {
			GolukDebugUtils.e("", "GGGG===@@@===1111======");
			if (RESULE_SUCESS == msg) {
				GolukDebugUtils.e("", "GGGG===@@@====2222=====");
				if (null != mVideoSquareInfo) {
					if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
						mVideoSquareInfo.mVideoEntity.ispraise = "1";
						updateClickPraiseNumber(true, mVideoSquareInfo);
					}
				}

			} else {
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
		}

	}

	public void setViewListBg(boolean flog) {
		if (flog) {
			shareBg.setVisibility(View.VISIBLE);
			mRTPullListView.setVisibility(View.GONE);
		} else {
			shareBg.setVisibility(View.GONE);
			mRTPullListView.setVisibility(View.VISIBLE);
		}
	}

	public void onResume() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("NewestListView", this);
		}
	}

	public void onPause() {
		GolukDebugUtils.e("", "NewList----------------------------onPause: ");
		// VideoSquareManager mVideoSquareManager =
		// GolukApplication.getInstance().getVideoSquareManager();
		// if (null != mVideoSquareManager) {
		// mVideoSquareManager.removeVideoSquareManagerListener("NewestListView");
		// }
	}

	public void onDestroy() {

	}

	VideoSquareInfo mVideoSquareInfo;

	public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
		mVideoSquareInfo = info;
		if (!flag) {
			return;
		}

		for (int i = 0; i < mDataList.size(); i++) {
			VideoSquareInfo vs = mDataList.get(i);
			if (vs.id.equals(mVideoSquareInfo.id)) {
				int number = Integer.parseInt(mVideoSquareInfo.mVideoEntity.praisenumber);
				if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
					number++;
				} else {
					number--;
				}

				mDataList.get(i).mVideoEntity.praisenumber = "" + number;
				mDataList.get(i).mVideoEntity.ispraise = mVideoSquareInfo.mVideoEntity.ispraise;
				mVideoSquareInfo.mVideoEntity.praisenumber = "" + number;
				break;
			}
		}

		mNewestAdapter.updateClickPraiseNumber(info);
	}
}
