package cn.com.mobnote.golukmobile.cluster;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.cluster.ClusterAdapter.IClusterInterface;
import cn.com.mobnote.golukmobile.cluster.bean.ActivityJsonData;
import cn.com.mobnote.golukmobile.cluster.bean.ClusterHeadBean;
import cn.com.mobnote.golukmobile.cluster.bean.GetClusterShareUrlData;
import cn.com.mobnote.golukmobile.cluster.bean.JsonData;
import cn.com.mobnote.golukmobile.cluster.bean.ShareUrlDataBean;
import cn.com.mobnote.golukmobile.cluster.bean.VolleyDataFormat;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
import cn.com.mobnote.golukmobile.comment.ICommentFn;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.newest.ClickPraiseListener.IClickPraiseView;
import cn.com.mobnote.golukmobile.newest.ClickShareListener.IClickShareView;
import cn.com.mobnote.golukmobile.newest.IDialogDealFn;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;

public class ClusterActivity extends BaseActivity implements OnClickListener, IRequestResultListener, IClickShareView,
		IClickPraiseView, IDialogDealFn, IClusterInterface, VideoSuqareManagerFn {

	public static final String TAG = "ClusterActivity";

	public static final String CLUSTER_KEY_ACTIVITYID = "activityid";
	public static final String CLUSTER_KEY_TITLE = "cluster_key_title";
	private RTPullListView mRTPullListView = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	private VolleyDataFormat vdf = new VolleyDataFormat();
	private  static final int ClOSE_ACTIVITY = 1000;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 返回按钮 */
	private ImageButton backbtn;
	/** 标题 **/
	private TextView title;

	/** 分享按钮 **/
	private Button shareBtn;
	private EditText mEditText = null;
	private TextView commenCountTv = null;
	public ClusterHeadBean headData = null;
	public List<VideoSquareInfo> recommendlist = null;
	public List<VideoSquareInfo> newslist = null;
	public ClusterAdapter clusterAdapter;
	private SharePlatformUtil sharePlatform = null;
	private RelativeLayout mBottomLoadingView = null;
	/** 活动id **/
	private String mActivityid = null;
	private String mClusterTitle = null;

	private ClusterBeanRequest request = null;
	private RecommendBeanRequest recommendRequest = null;
	private NewsBeanRequest newsRequest = null;
	private GetShareUrlRequest shareRequest = null;
	private boolean isfrist = false;
	/** 聚合id */
	private String custerVid = "";
	/** 是否允许评论 */
	private boolean isCanInput = true;
	/** 是否允许点击评论，只有当数据回来时，才可以去评论 */
	private boolean isRequestSucess = false;
	
	private boolean isRecommendLoad = false;
	private boolean isNewsLoad = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cluster_main);

		Intent intent = this.getIntent();
		mActivityid = intent.getStringExtra(CLUSTER_KEY_ACTIVITYID);
		mClusterTitle = intent.getStringExtra(CLUSTER_KEY_TITLE);

		this.initData();// 初始化view
		this.initListener();// 初始化view的监听

		GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener(TAG, this);
		isfrist = true;
		httpPost(mActivityid);
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
	private void httpPost(String activityid) {
		request = new ClusterBeanRequest(IPageNotifyFn.PageType_ClusterMain, this);
		request.get(activityid);
	}

	private void initData() {
		mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
		backbtn = (ImageButton) findViewById(R.id.back_btn);
		title = (TextView) findViewById(R.id.title);
		
		if (mClusterTitle.length() > 12) {
			mClusterTitle = mClusterTitle.substring(0, 12) + "...";
		}
		title.setText(mClusterTitle);
		shareBtn = (Button) findViewById(R.id.title_share);
		mEditText = (EditText) findViewById(R.id.custer_comment_input);
		commenCountTv = (TextView) findViewById(R.id.custer_comment_send);
		mBottomLoadingView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading,
				null);
		sharePlatform = new SharePlatformUtil(this);
		clusterAdapter = new ClusterAdapter(this, sharePlatform, 1, this);
		mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRTPullListView.setAdapter(clusterAdapter);
	}
	
	private void setTitle(String titles) {
		if (null != mClusterTitle && !"".equals(mClusterTitle)) {
			return;
		}
		mClusterTitle = titles;
		title.setText(mClusterTitle);
	}

	private void initListener() {
		backbtn.setOnClickListener(this);
		shareBtn.setOnClickListener(this);
		mEditText.setOnClickListener(this);
		commenCountTv.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				// 下拉刷新个人中心所有数据
				httpPost(mActivityid);// 请求数据
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

					if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {// 推荐
						if (clusterAdapter.getCurrentViewType() == ClusterAdapter.ViewType_RecommendVideoList) {// 视频列表
							if (recommendlist != null && recommendlist.size() > 0) {// 加载更多视频数据
								if (isRecommendLoad) {
									mRTPullListView.addFooterView(mBottomLoadingView);
									recommendRequest = new RecommendBeanRequest(
											IPageNotifyFn.PageType_ClusterRecommend, ClusterActivity.this);
									recommendRequest.get(mActivityid, "2",
											recommendlist.get(recommendlist.size() - 1).mVideoEntity.sharingtime, "20");
								}
							}
						} else {// 最新列表
							if (newslist != null && newslist.size() >= 20) {// 加载更多视频数据
								if (isNewsLoad) {
									mRTPullListView.addFooterView(mBottomLoadingView);
									newsRequest = new NewsBeanRequest(IPageNotifyFn.PageType_ClusterNews,
											ClusterActivity.this);
									newsRequest.get(mActivityid, "2",
											newslist.get(newslist.size() - 1).mVideoEntity.sharingtime, "20");
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
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;
		case R.id.title_share:
			showProgressDialog();
			shareRequest = new GetShareUrlRequest(IPageNotifyFn.PageType_ClusterShareUrl, this);
			shareRequest.get(mActivityid);
			break;
		case R.id.custer_comment_send:
			toCommentActivity(false);
			break;
		case R.id.custer_comment_input:
			toCommentActivity(true);
			break;
		default:
			break;
		}
	}

	private void toCommentActivity(boolean isShowSoft) {
		if (!isRequestSucess) {
			return;
		}
		Intent intent = new Intent(this, CommentActivity.class);
		intent.putExtra(CommentActivity.COMMENT_KEY_MID, custerVid);
		intent.putExtra(CommentActivity.COMMENT_KEY_TYPE, ICommentFn.COMMENT_TYPE_CLUSTER);
		intent.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, isShowSoft);
		intent.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, isCanInput);
		intent.putExtra(CommentActivity.COMMENT_KEY_USERID, "");
		startActivity(intent);
	}

	private void setCommentCount(String count) {
		if (null == count) {
			return;
		}
		String show = GolukUtils.getFormatNumber(count) + "条";
		commenCountTv.setText(show);
	}

	@Override
	protected void onResume() {
		mBaseApp.setContext(this, TAG);
		super.onResume();
	}

	public void updateViewData(boolean succ, int count) {
		mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime());
		if (succ) {
			clusterAdapter.notifyDataSetChanged();
			if (count > 0) {
				this.mRTPullListView.setSelection(count);
			}
		}
	}

	private void setCommentData(ClusterHeadBean bean) {
		if (bean == null) {
			return;
		}
		isCanInput = false;
		custerVid = bean.activity.activityid;
		if (null != bean.activity.iscomment && !"".equals(bean.activity.iscomment)) {
			if ("1".equals(bean.activity.iscomment)) {
				isCanInput = true;
			}
		}
		setCommentCount(bean.activity.commentcount);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != sharePlatform) {
			sharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if (requestType == IPageNotifyFn.PageType_ClusterMain) {
			JsonData data = (JsonData) result;
			if (data != null && data.success) {
				if (data.data != null) {
					isRequestSucess = true;
					ClusterHeadBean chb = data.data;
					setCommentData(chb);
					recommendlist = vdf.getClusterList(chb.recommendvideo);
					if(recommendlist !=null && recommendlist.size() == 20){
						isRecommendLoad = true;
					}else{
						isRecommendLoad = false;
					}
					
					newslist = vdf.getClusterList(chb.latestvideo);
					if(newslist !=null && newslist.size() == 20){
						isNewsLoad = true;
					}else{
						isNewsLoad = false;
					}
					
					clusterAdapter.setDataInfo(chb.activity, recommendlist, newslist);
					
					String activityname = chb.activity.activityname;
					if (null != activityname && !"".equals(activityname)) {
						setTitle(activityname);
					}
					
					updateViewData(true, 0);
				} else {
					updateViewData(false, 0);
				}
			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
				updateViewData(false, 0);
				if(isfrist){
					mBaseHandler.sendEmptyMessageDelayed(ClOSE_ACTIVITY, 2000);
				}
				
			}
			isfrist = false;
		} else if (requestType == IPageNotifyFn.PageType_ClusterRecommend) {
			ActivityJsonData data = (ActivityJsonData) result;
			// 移除下拉
			mRTPullListView.removeFooterView(this.mBottomLoadingView);
			if (data != null && data.success) {
				if (data.data != null) {
					if ("0".equals(data.data.result)) {
						List<VideoSquareInfo> list = vdf.getClusterList(data.data.videolist);
						int count = recommendlist.size();
						if (list != null && list.size() > 0) {
							if(list.size() == 20){
								isRecommendLoad = true;
							}else{
								isRecommendLoad = false;
							}
							recommendlist.addAll(list);
							updateViewData(true, count);
						} else {
							GolukUtils.showToast(this, "数据异常，请稍候重试");
						}
					} else {
						GolukUtils.showToast(this, "数据异常，请稍候重试");
					}
				} else {
					GolukUtils.showToast(this, "数据异常，请稍候重试");
				}
			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
			}

		} else if (requestType == IPageNotifyFn.PageType_ClusterNews) {
			ActivityJsonData data = (ActivityJsonData) result;
			if (data != null && data.success) {
				if (data.data != null) {
					if ("0".equals(data.data.result)) {
						List<VideoSquareInfo> list = vdf.getClusterList(data.data.videolist);
						// 移除下拉
						mRTPullListView.removeFooterView(this.mBottomLoadingView);
						int count = newslist.size();
						if (list != null && list.size() > 0) {
							if(list.size() == 20){
								isNewsLoad = true;
							}else{
								isNewsLoad = false;
							}
							newslist.addAll(list);
							updateViewData(true, count);
						} else {
							GolukUtils.showToast(this, "数据异常，请稍候重试");
						}
					} else {
						GolukUtils.showToast(this, "数据异常，请稍候重试");
					}
				} else {
					GolukUtils.showToast(this, "数据异常，请稍候重试");
				}
			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
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
						String realDesc = "极路客精选专题(使用#极路客Goluk#拍摄)";

						if (TextUtils.isEmpty(describe)) {
							describe = "";
						}
						String ttl = mClusterTitle;
						if (TextUtils.isEmpty(mClusterTitle)) {
							ttl = "极路客精选专题分享";
						}
						// 缩略图
						Bitmap bitmap = null;
						if (headData != null) {
							bitmap = getThumbBitmap(headData.activity.picture);
						}

						if (this != null && !this.isFinishing()) {
							CustomShareBoard shareBoard = new CustomShareBoard(ClusterActivity.this, sharePlatform,
									shareurl, coverurl, describe, ttl, bitmap, realDesc, mActivityid);
							shareBoard.showAtLocation(ClusterActivity.this.getWindow().getDecorView(), Gravity.BOTTOM,
									0, 0);
						} else {
							GolukUtils.showToast(this, "网络异常，请检查网络");
						}
					} else {
						GolukUtils.showToast(this, "网络异常，请检查网络");
					}
				} else {
					GolukUtils.showToast(this, "网络异常，请检查网络");
				}
			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
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
		if (null == clusterAdapter) {
			return;
		}
		if (clusterAdapter.getCurrentViewType() == ClusterAdapter.ViewType_RecommendVideoList) {
			if (null != recommendlist) {
				for (int i = 0; i < recommendlist.size(); i++) {
					VideoSquareInfo vs = this.recommendlist.get(i);
					if (this.updatePraise(vs, recommendlist, i)) {
						break;
					}
				}
			}
		} else {
			if (null != newslist) {
				for (int i = 0; i < this.newslist.size(); i++) {
					VideoSquareInfo vs = this.newslist.get(i);
					if (this.updatePraise(vs, newslist, i)) {
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

						String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";

						if (TextUtils.isEmpty(describe)) {
							describe = "#极路客精彩视频#";
						}
						String ttl = "极路客精彩视频";

						if (!this.isFinishing()) {
							String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
									: "";
							String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
									: "";
							describe = username + "：" + describe;
							CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shareurl, coverurl,
									describe, ttl, null, realDesc, videoId);
							shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
						}

					} else {
						GolukUtils.showToast(this, "网络异常，请检查网络");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				GolukUtils.showToast(this, "网络异常，请检查网络");
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
				GolukUtils.showToast(this, "网络异常，请检查网络");
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
