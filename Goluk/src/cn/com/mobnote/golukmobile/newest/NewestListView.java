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
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class NewestListView implements VideoSuqareManagerFn {
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	private NewestListHeadDataInfo mHeadDataInfo = null;
	public List<VideoSquareInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	public static Handler mHandler = null;
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

	public NewestListView(Context context) {
		mContext = context;
		mHeadDataInfo = new NewestListHeadDataInfo();
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRootLayout = new RelativeLayout(mContext);
		mRootLayout.addView(mRTPullListView);

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

	private void httpPost(boolean flag, String operation, String timestamp) {
		curOperation = operation;
		if (flag) {
			if (null == mCustomProgressDialog) {
				mCustomProgressDialog = new CustomLoadingDialog(mContext, null);
				mCustomProgressDialog.show();
			}
		}

		if (null != GolukApplication.getInstance().getVideoSquareManager()) {
			if ("0".equals(operation)) {
				if (!headLoading) {
					headLoading = true;
					GolukApplication.getInstance().getVideoSquareManager().getZXListData();
				}
			}

			GolukDebugUtils.e("", "GGGGGG=====111111=======dataLoading=" + dataLoading);
			if (dataLoading) {
				return;
			}

			List<String> attribute = new ArrayList<String>();
			attribute.add("0");
			dataLoading = true;
			boolean tv = GolukApplication.getInstance().getVideoSquareManager()
					.getTypeVideoList("1", "0", attribute, operation, timestamp);
			GolukDebugUtils.e("", "GGGGGG=====222222=======tv=" + tv);
			if (!tv) {
				closeProgressDialog();
			}
		} else {
			closeProgressDialog();
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

	public void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
			mCustomProgressDialog.close();
		}
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

		closeProgressDialog();
		mRTPullListView.onRefreshComplete(historyDate);
		if (null == mNewestAdapter) {
			mNewestAdapter = new NewestAdapter(mContext);
			mNewestAdapter.setNewestLiseView(this);
		}

		if ("0".equals(curOperation)) {
			mRTPullListView.setAdapter(mNewestAdapter);
			mNewestAdapter.setData(mHeadDataInfo, mDataList);
		}else {
			mNewestAdapter.loadData(mDataList);
		}

	}
	
	private void initListener() {
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
					if (mRTPullListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
						httpPost(false, "2", mDataList.get(mDataList.size() - 1).mVideoEntity.sharingtime);
					}

					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mNewestAdapter.lock();
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

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == VSquare_Req_List_Catlog) {
			if (RESULE_SUCESS == msg) {
				headLoading = false;
				mHeadDataInfo = JsonParserUtils.parserNewestHeadData((String) param2);
				initLayout();
			} else {
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}

		} else if (event == VSquare_Req_List_Video_Catlog) {
			if (RESULE_SUCESS == msg) {
				dataLoading = false;
				List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData((String) param2);
				if ("0".equals(curOperation)) {
					mDataList.clear();
					pageCount = datalist.size();
				}

				mDataList.addAll(datalist);
				curpageCount = datalist.size();
				initLayout();
			} else {
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
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
						String ttl = "极路客精彩视频分享";
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
								CustomShareBoard shareBoard = new CustomShareBoard(vspa, sharePlatform, shareurl,
										coverurl, describe, ttl, null, realDesc);
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
					mVideoSquareInfo.mVideoEntity.ispraise = "1";
					updateClickPraiseNumber(true, mVideoSquareInfo);
				}

			}
		}

	}

	public void onResume() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.addVideoSquareManagerListener("NewestListView", this);
		}
	}

	public void onPause() {
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			mVideoSquareManager.removeVideoSquareManagerListener("NewestListView");
		}
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

		mNewestAdapter.updateClickPraiseNumber(info);
	}

}
