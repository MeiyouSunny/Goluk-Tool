package com.mobnote.golukmain.cluster;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.cluster.ClusterAdapter.IClusterInterface;
import com.mobnote.golukmain.cluster.bean.ActivityJsonData;
import com.mobnote.golukmain.cluster.bean.ClusterHeadBean;
import com.mobnote.golukmain.cluster.bean.GetClusterShareUrlData;
import com.mobnote.golukmain.cluster.bean.JsonData;
import com.mobnote.golukmain.cluster.bean.ShareUrlDataBean;
import com.mobnote.golukmain.cluster.bean.VideoListBean;
import com.mobnote.golukmain.cluster.bean.VolleyDataFormat;
import com.mobnote.golukmain.comment.CommentActivity;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.newest.ClickPraiseListener.IClickPraiseView;
import com.mobnote.golukmain.newest.ClickShareListener.IClickShareView;
import com.mobnote.golukmain.newest.IDialogDealFn;
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
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

public class ClusterActivity extends BaseActivity implements OnClickListener, IRequestResultListener, IClickShareView,
		IClickPraiseView, IDialogDealFn, IClusterInterface, VideoSuqareManagerFn {

	public static final String TAG = "ClusterActivity";

	public static final String CLUSTER_KEY_ACTIVITYID = "activityid";
	public static final String CLUSTER_KEY_TITLE = "cluster_key_title";
	private RTPullListView mRTPullListView = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private VolleyDataFormat vdf = new VolleyDataFormat();
	private static final int ClOSE_ACTIVITY = 1000;
	/** ????????????????????????????????? */
	private int mWonderfulFirstVisible;
	/** ??????????????????item?????? */
	private int mWonderfulVisibleCount;
	/** ???????????? */
	private ImageButton mBackbtn;
	/** ?????? **/
	private TextView mTitle;

	/** ???????????? **/
	private Button mShareBtn;
	private EditText mEditText = null;
	private TextView mCommenCountTv = null;
	public ClusterHeadBean mHeadData = null;
	public List<VideoSquareInfo> mRecommendlist = null;
	public List<VideoSquareInfo> mNewslist = null;
	public ClusterAdapter mClusterAdapter;
	private SharePlatformUtil mSharePlatform = null;
	/** ??????id **/
	private String mActivityid = null;
	private String mClusterTitle = null;

	private ClusterBeanRequest mRequest = null;
	private RecommendBeanRequest mRecommendRequest = null;
	private NewsBeanRequest mNewsRequest = null;
	private GetShareUrlRequest mShareRequest = null;
	private boolean mIsfrist = false;
	/** ?????????????????? */
	private boolean mIsCanInput = true;
	/** ???????????????????????????????????????????????????????????????????????? */
	private boolean mIsRequestSucess = false;

	private boolean mIsRecommendLoad = false;
	private boolean mIsNewsLoad = false;

	/** ????????????????????? **/
	private boolean mIsLoadDataRecommend = false;
	private boolean mIsLoadDataNews = false;

	private String mTjtime = "00000000000000000";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cluster_main);

		Intent intent = this.getIntent();
		mActivityid = intent.getStringExtra(CLUSTER_KEY_ACTIVITYID);
		mClusterTitle = intent.getStringExtra(CLUSTER_KEY_TITLE);

		this.initData();// ?????????view
		this.initListener();// ?????????view?????????

		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener(TAG, this);
		mIsfrist = true;
		httpPost(mActivityid);
		mRTPullListView.firstFreshState();
	}

	public static class NoVideoDataViewHolder {
		TextView tips;
		TextView emptyImage;
		TextView tipsText;
		boolean bMeasureHeight;
	}

	/**
	 * ??????????????????
	 * 
	 * @param flag
	 *            ??????????????????????????????
	 * @author xuhw
	 * @date 2015???4???15???
	 */
	private void httpPost(String activityid) {
		mRequest = new ClusterBeanRequest(IPageNotifyFn.PageType_ClusterMain, this);
		mRequest.get(activityid);
	}

	private void initData() {
		mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
		mBackbtn = (ImageButton) findViewById(R.id.back_btn);
		mTitle = (TextView) findViewById(R.id.title);

		if (mClusterTitle.length() > 12) {
			mClusterTitle = mClusterTitle.substring(0, 12) + this.getString(R.string.str_omit);
		}
		mTitle.setText(mClusterTitle);
		mShareBtn = (Button) findViewById(R.id.title_share);
		mEditText = (EditText) findViewById(R.id.custer_comment_input);
		mCommenCountTv = (TextView) findViewById(R.id.custer_comment_send);
		mSharePlatform = new SharePlatformUtil(this);
		mClusterAdapter = new ClusterAdapter(this, mSharePlatform, 1, this, mActivityid);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRTPullListView.setAdapter(mClusterAdapter);
	}

	private void setTitle(String titles) {
		if (null != mClusterTitle && !"".equals(mClusterTitle)) {
			return;
		}
		mClusterTitle = titles;
		mTitle.setText(mClusterTitle);
	}

	private void initListener() {
		mBackbtn.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);
		mEditText.setOnClickListener(this);
		mCommenCountTv.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// ????????????????????????????????????
				httpPost(mActivityid);// ????????????
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					if (mRTPullListView.getAdapter().getCount() == (mWonderfulFirstVisible + mWonderfulVisibleCount)) {// ??????
						if (mClusterAdapter.getCurrentViewType() == ClusterAdapter.sViewType_RecommendVideoList) {// ????????????
							if (mRecommendlist != null && mRecommendlist.size() > 0) {// ????????????????????????
								if (mIsRecommendLoad) {
									mRecommendRequest = new RecommendBeanRequest(
											IPageNotifyFn.PageType_ClusterRecommend, ClusterActivity.this);
									mRecommendRequest.get(mActivityid, "2", mTjtime, "20");
									mIsRecommendLoad = false;
									mIsLoadDataRecommend = true;
								}
							}
						} else {// ????????????
							if (mNewslist != null && mNewslist.size() > 0) {// ????????????????????????
								if (mIsNewsLoad) {
									mNewsRequest = new NewsBeanRequest(IPageNotifyFn.PageType_ClusterNews,
											ClusterActivity.this);
									mNewsRequest.get(mActivityid, "2",
											mNewslist.get(mNewslist.size() - 1).mVideoEntity.sharingtime, "20");
									mIsLoadDataNews = true;
									mIsNewsLoad = false;
								}
							}
						}
					}
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
				mWonderfulFirstVisible = firstVisibleItem;
				mWonderfulVisibleCount = visibleItemCount;
			}
		});
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.back_btn) {
			this.finish();
		} else if (id == R.id.title_share) {
			showProgressDialog();
			mShareRequest = new GetShareUrlRequest(IPageNotifyFn.PageType_ClusterShareUrl, this);
			mShareRequest.get(mActivityid);
		} else if (id == R.id.custer_comment_send) {
			toCommentActivity(false);
		} else if (id == R.id.custer_comment_input) {
			toCommentActivity(true);
		} else {
		}
	}

	private void toCommentActivity(boolean isShowSoft) {
		if (!mIsRequestSucess) {
			return;
		}
		Intent intent = new Intent(this, CommentActivity.class);
		intent.putExtra(CommentActivity.COMMENT_KEY_MID, mActivityid);
		intent.putExtra(CommentActivity.COMMENT_KEY_TYPE, ICommentFn.COMMENT_TYPE_CLUSTER);
		intent.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, isShowSoft);
		intent.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, mIsCanInput);
		intent.putExtra(CommentActivity.COMMENT_KEY_USERID, "");
		startActivity(intent);
	}

	private void setCommentCount(String count) {
		if (null == count) {
			return;
		}
		String show = GolukUtils.getFormatNumber(count) + this.getString(R.string.str_comment_unit);
		mCommenCountTv.setText(show);
	}

	@Override
	protected void onResume() {
		mBaseApp.setContext(this, TAG);
		super.onResume();
	}

	public void updateViewData(boolean succ, int count) {
		mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(this));
		if (succ) {
			mClusterAdapter.notifyDataSetChanged();
			if (count > 0) {
				this.mRTPullListView.setSelection(count);
			}
		}
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param list
	 */
	private void setTjTime(List<VideoListBean> list) {
		if (list != null && list.size() > 0) {
			VideoListBean vlb = list.get(list.size() - 1);
			if (vlb != null && vlb.video != null && vlb.video.gen != null) {
				mTjtime = vlb.video.gen.tjtime;
			}
		}
	}

	private void setCommentData(ClusterHeadBean bean) {
		if (bean == null) {
			return;
		}

		mIsCanInput = false;
		mActivityid = bean.activity.activityid;
		if (null != bean.activity.iscomment && !"".equals(bean.activity.iscomment)) {
			if ("1".equals(bean.activity.iscomment)) {
				mIsCanInput = true;
			}
		}
		setCommentCount(bean.activity.commentcount);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != mSharePlatform) {
			mSharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	// ????????????
	public boolean sendPraiseRequest(String id) {
		PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
		return request.get("1", id, "1");
	}

	// ??????????????????
	public boolean sendCancelPraiseRequest(String id) {
		PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
		return request.get("1", id);
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if (requestType == IPageNotifyFn.PageType_ClusterMain) {
			JsonData data = (JsonData) result;
			mRTPullListView.removeFooterView(1);
			mRTPullListView.removeFooterView(2);
			if (data != null && data.success) {
				if (data.data != null) {
					mIsRequestSucess = true;
					ClusterHeadBean chb = data.data;
					setTjTime(chb.recommendvideo);
					setCommentData(chb);
					mRecommendlist = vdf.getClusterList(chb.recommendvideo);
					if (mRecommendlist != null && mRecommendlist.size() == 20) {
						mIsRecommendLoad = true;
					} else {
						mIsRecommendLoad = false;
					}

					mNewslist = vdf.getClusterList(chb.latestvideo);
					if (mNewslist != null && mNewslist.size() == 20) {
						mIsNewsLoad = true;
					} else {
						mIsNewsLoad = false;
					}

					mClusterAdapter.setDataInfo(chb.activity, mRecommendlist, mNewslist);

					String activityname = chb.activity.activityname;
					if (null != activityname && !"".equals(activityname)) {
						setTitle(activityname);
					}

					updateViewData(true, 0);
				} else {
					updateViewData(false, 0);
				}
			} else {

				GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
				updateViewData(false, 0);
				if (mIsfrist) {
					mBaseHandler.sendEmptyMessageDelayed(ClOSE_ACTIVITY, 2000);
				}

			}
			mIsfrist = false;
		} else if (requestType == IPageNotifyFn.PageType_ClusterRecommend) {
			mIsLoadDataRecommend = false;
			ActivityJsonData data = (ActivityJsonData) result;
			if (data != null && data.success) {
				if (data.data != null) {
					if ("0".equals(data.data.result)) {
						setTjTime(data.data.videolist);
						List<VideoSquareInfo> list = vdf.getClusterList(data.data.videolist);
						int count = mRecommendlist.size();
						if (list != null && list.size() > 0) {
							if (list.size() == 20) {
								mIsRecommendLoad = true;
							} else {
								mIsRecommendLoad = false;
							}
							mRecommendlist.addAll(list);
							updateViewData(true, count);
						} else {
							mIsRecommendLoad = false;
							GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
						}
					} else {
						mIsRecommendLoad = true;
						GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
					}
				} else {
					mIsRecommendLoad = true;
					GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
				}
			} else {
				mIsRecommendLoad = true;
				GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
			}

		} else if (requestType == IPageNotifyFn.PageType_ClusterNews) {
			mIsLoadDataNews = false;
			ActivityJsonData data = (ActivityJsonData) result;
			if (data != null && data.success) {
				if (data.data != null) {
					if ("0".equals(data.data.result)) {
						List<VideoSquareInfo> list = vdf.getClusterList(data.data.videolist);
						int count = mNewslist.size();
						if (list != null && list.size() > 0) {
							if (list.size() == 20) {
								mIsNewsLoad = true;
							} else {
								mIsNewsLoad = false;
							}
							mNewslist.addAll(list);
							updateViewData(true, count);
						} else {
							mIsNewsLoad = false;
							GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
						}
					} else {
						mIsNewsLoad = true;
						GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
					}
				} else {
					mIsNewsLoad = true;
					GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
				}
			} else {
				mIsNewsLoad = true;
				GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
			}
		} else if (requestType == IPageNotifyFn.PageType_ClusterShareUrl) {
			closeProgressDialog();
			GetClusterShareUrlData data = (GetClusterShareUrlData) result;
			if (data != null && data.success) {
				if (data.data != null) {

					ShareUrlDataBean sdb = data.data;
					if ("0".equals(sdb.result)) {
						String shareurl = sdb.shorturl;
						String coverurl = sdb.coverurl;
						String describe = sdb.description;
						String realDesc = this.getResources().getString(R.string.cluster_jxzt_share_txt);

						if (TextUtils.isEmpty(describe)) {
							describe = "";
						}
						String ttl = mClusterTitle;
						if (TextUtils.isEmpty(mClusterTitle)) {
							ttl = this.getResources().getString(R.string.cluster_jx_zt_share);
						}
						// ?????????
						Bitmap bitmap = null;
						if (mHeadData != null) {
							bitmap = getThumbBitmap(mHeadData.activity.picture);
						}

						if (this != null && !this.isFinishing()) {
							ThirdShareBean bean = new ThirdShareBean();
							bean.surl = shareurl;
							bean.curl = coverurl;
							bean.db = describe;
							bean.tl = ttl;
							bean.bitmap = bitmap;
							bean.realDesc = realDesc;
							bean.videoId = mActivityid;
							ProxyThirdShare shareBoard = new ProxyThirdShare(ClusterActivity.this, mSharePlatform, bean);
							shareBoard.showAtLocation(ClusterActivity.this.getWindow().getDecorView(), Gravity.BOTTOM,
									0, 0);
						} else {
							GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
						}
					} else {
						GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
					}
				} else {
					GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
				}
			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
			}
		} else if (requestType == IPageNotifyFn.PageType_Praise) {
			PraiseResultBean prBean = (PraiseResultBean) result;
			if (null == result || !prBean.success) {
				GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
				return;
			}

			PraiseResultDataBean ret = prBean.data;
			if (null != ret && !TextUtils.isEmpty(ret.result)) {
				if ("0".equals(ret.result)) {
					if (null != mVideoSquareInfo) {
						if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
							mVideoSquareInfo.mVideoEntity.ispraise = "1";
							updateClickPraiseNumber(true, mVideoSquareInfo);
						}
					}
				} else if ("7".equals(ret.result)) {
					GolukUtils.showToast(this, this.getString(R.string.str_no_duplicated_praise));
				} else {
					GolukUtils.showToast(this, this.getString(R.string.str_praise_failed));
				}
			}
		} else if (requestType == IPageNotifyFn.PageType_PraiseCancel) {
			PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
			if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
				GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
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
					GolukUtils.showToast(this, this.getString(R.string.str_cancel_praise_failed));
				}
			}
		}
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param type
	 */
	public void updateListViewBottom(int type) {
		mRTPullListView.removeFooterView(1);
		mRTPullListView.removeFooterView(2);
		if (mClusterAdapter.sViewType_RecommendVideoList == type) {// ??????
			if (mIsRecommendLoad) {
				mRTPullListView.addFooterView(1);
			} else {
				if (mRecommendlist != null && mRecommendlist.size() > 0) {
					mRTPullListView.addFooterView(2);
				}
			}
		} else {// ??????
			if (mIsNewsLoad) {
				mRTPullListView.addFooterView(1);
			} else {
				if (mNewslist != null && mNewslist.size() > 0) {
					mRTPullListView.addFooterView(2);
				}
			}
		}
	}

	public Bitmap getThumbBitmap(String netUrl) {
		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
		File file = new File(path + File.separator + name);
		Bitmap t_bitmap = null;
		if (file.exists()) {
			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
		}
		return t_bitmap;
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
		if (null == mClusterAdapter) {
			return;
		}
		if (mClusterAdapter.getCurrentViewType() == ClusterAdapter.sViewType_RecommendVideoList) {
			if (null != mRecommendlist) {
				for (int i = 0; i < mRecommendlist.size(); i++) {
					VideoSquareInfo vs = this.mRecommendlist.get(i);
					if (this.updatePraise(vs, mRecommendlist, i)) {
						break;
					}
				}
			}
		} else {
			if (null != mNewslist) {
				for (int i = 0; i < this.mNewslist.size(); i++) {
					VideoSquareInfo vs = this.mNewslist.get(i);
					if (this.updatePraise(vs, mNewslist, i)) {
						break;
					}
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
			mClusterAdapter.notifyDataSetChanged();
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
	}

	@Override
	public void OnRefrushMainPageData() {

	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == VSquare_Req_VOP_GetShareURL_Video) {
			Context topContext = mBaseApp.getContext();
			if (topContext != this) {
				return;
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

						String realDesc = this.getResources().getString(R.string.str_share_board_real_desc);

						if (TextUtils.isEmpty(describe)) {
							describe = this.getResources().getString(R.string.str_share_describe);
						}
						String ttl = this.getResources().getString(R.string.str_goluk_wonderful_video);

						if (!this.isFinishing()) {
							String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
									: "";
							String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
									: "";
							describe = username + this.getString(R.string.str_colon) + describe;

							ThirdShareBean bean = new ThirdShareBean();
							bean.surl = shareurl;
							bean.curl = coverurl;
							bean.db = describe;
							bean.tl = ttl;
							bean.bitmap = null;
							bean.realDesc = realDesc;
							bean.videoId = videoId;

							ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, bean);
							shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
						}

					} else {
						GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
			}
		} else if (event == VSquare_Req_VOP_Praise) {
			if (RESULE_SUCESS == msg) {
				if (null != mVideoSquareInfo) {
					if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
						mVideoSquareInfo.mVideoEntity.ispraise = "1";
						updateClickPraiseNumber(true, mVideoSquareInfo);
					}
				}

			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
			}

		}
	}

	@Override
	protected void onDestroy() {
		if (null != mCustomProgressDialog) {
			if (mCustomProgressDialog.isShowing()) {
				mCustomProgressDialog.close();
			}
		}
		mBaseHandler.removeMessages(ClOSE_ACTIVITY);
		GlideUtils.clearMemory(this);
		GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener(TAG);
		super.onDestroy();
	}

	@Override
	protected void hMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case ClOSE_ACTIVITY:
			this.finish();
			break;
		default:
			break;
		}
		super.hMessage(msg);
	}
}
