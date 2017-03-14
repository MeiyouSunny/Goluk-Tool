package com.mobnote.golukmain.newest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.newest.ClickPraiseListener.IClickPraiseView;
import com.mobnote.golukmain.newest.ClickShareListener.IClickShareView;
import com.mobnote.golukmain.praise.PraiseCancelRequest;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultBean;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultDataBean;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.golukmain.videosuqare.VideoSquareManager;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.golukmain.videosuqare.ZhugeParameterFn;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class NewestListView implements VideoSuqareManagerFn, IClickShareView, IClickPraiseView, IRequestResultListener, ZhugeParameterFn {
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
	private SimpleDateFormat sdf = null;
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
	private RelativeLayout shareBg = null;
	private long zXRequestId = 0;
	private long typeVideoRequestId = 0;
	/** 上拉加载的次数 **/
	private int mPushCount = 0;

	public NewestListView(Context context) {
		mContext = context;
		sdf = new SimpleDateFormat(mContext.getString(R.string.str_date_formatter));
		mHeadDataInfo = new NewestListHeadDataInfo();
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		// mRTPullListView.setDividerHeight(78);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		mRTPullListView.setLayoutParams(lp);
		mRootLayout = new RelativeLayout(mContext);
		shareBg = (RelativeLayout) View.inflate(context, R.layout.video_square_bj, null);

		if (context instanceof MainActivity) {
			sharePlatform = ((MainActivity) context).getSharePlatform();
		}

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
			if (GolukApplication.getInstance().getVideoSquareManager() == null) {
				return;
			}
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
			mNewestAdapter = new NewestAdapter(mContext, ZHUGE_PLAY_VIDEO_PAGE_NEWEST);
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
							ZhugeUtils.eventNewestlPush(mContext, (++mPushCount));
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

		GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
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

						String realDesc = mContext.getString(R.string.str_share_board_real_desc);

						if (TextUtils.isEmpty(describe)) {
							describe = mContext.getString(R.string.str_share_describe);
						}
						String ttl = mContext.getString(R.string.str_share_ttl);
						if (mContext instanceof MainActivity) {
							MainActivity vspa = (MainActivity) mContext;
							if (vspa != null && !vspa.isFinishing()) {
								String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
										: "";
								String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
										: "";
								describe = username + mContext.getString(R.string.str_colon) + describe;

								ThirdShareBean bean = new ThirdShareBean();
								bean.surl = shareurl;
								bean.curl = coverurl;
								bean.db = describe;
								bean.tl = ttl;
								bean.bitmap = null;
								bean.realDesc = realDesc;
								bean.videoId = videoId;
								bean.from = mContext.getString(R.string.str_zhuge_newest_event);
								ProxyThirdShare share = new ProxyThirdShare(vspa, sharePlatform, bean);
								share.showAtLocation(vspa.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
							}
						}
					} else {
						GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
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
				GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
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

	// 删除视频
	public void deleteVideo(String vid) {
		mNewestAdapter.deleteVideo(vid);
	}

	public void changePraiseStatus(boolean status, String videoId) {
		GolukUtils.changePraiseStatus(mDataList, status, videoId);
		mNewestAdapter.notifyDataSetChanged();
	}

	// 点赞请求
	public boolean sendPraiseRequest(String id) {
		PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
		return request.get("1", id, "1");
	}

	// 取消点赞请求
	public boolean sendCancelPraiseRequest(String id) {
		PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
		return request.get("1", id);
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		switch (requestType) {
		case IPageNotifyFn.PageType_Praise:
			PraiseResultBean prBean = (PraiseResultBean) result;
			if (null == result || !prBean.success) {
				GolukUtils.showToast(mContext, mContext.getString(R.string.user_net_unavailable));
				return;
			}

			PraiseResultDataBean ret = prBean.data;
			if (null != ret && !TextUtils.isEmpty(ret.result)) {
				if ("0".equals(ret.result)) {
					//最新页面--视频点赞
					ZhugeUtils.eventPraiseVideo(mContext, mContext.getString(R.string.str_zhuge_newest_event));
					if (null != mVideoSquareInfo) {
						if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
							mVideoSquareInfo.mVideoEntity.ispraise = "1";
							updateClickPraiseNumber(true, mVideoSquareInfo);
						}
					}
				} else if ("7".equals(ret.result)) {
					GolukUtils.showToast(mContext, mContext.getString(R.string.str_no_duplicated_praise));
				} else {
					GolukUtils.showToast(mContext, mContext.getString(R.string.str_praise_failed));
				}
			}
			break;
		case IPageNotifyFn.PageType_PraiseCancel:
			PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
			if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
				GolukUtils.showToast(mContext, mContext.getString(R.string.user_net_unavailable));
				return;
			}

			PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
			if (null != cancelRet && !TextUtils.isEmpty(cancelRet.result)) {
				if ("0".equals(cancelRet.result)) {
					if (null != mVideoSquareInfo) {
						if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
							mVideoSquareInfo.mVideoEntity.ispraise = "0";
							updateClickPraiseNumber(true, mVideoSquareInfo);
						}
					}
				} else {
					GolukUtils.showToast(mContext, mContext.getString(R.string.str_cancel_praise_failed));
				}
			}
			break;
		default:
			break;
		}
	}
}
