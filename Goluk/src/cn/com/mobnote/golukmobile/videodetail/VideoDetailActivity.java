package cn.com.mobnote.golukmobile.videodetail;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.comment.CommentAddRequest;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.comment.CommentDeleteRequest;
import cn.com.mobnote.golukmobile.comment.CommentListRequest;
import cn.com.mobnote.golukmobile.comment.ICommentFn;
import cn.com.mobnote.golukmobile.comment.bean.CommentAddBean;
import cn.com.mobnote.golukmobile.comment.bean.CommentAddResultBean;
import cn.com.mobnote.golukmobile.comment.bean.CommentDataBean;
import cn.com.mobnote.golukmobile.comment.bean.CommentDelResultBean;
import cn.com.mobnote.golukmobile.comment.bean.CommentItemBean;
import cn.com.mobnote.golukmobile.comment.bean.CommentResultBean;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.praise.PraiseCancelRequest;
import cn.com.mobnote.golukmobile.praise.PraiseRequest;
import cn.com.mobnote.golukmobile.praise.bean.PraiseCancelResultBean;
import cn.com.mobnote.golukmobile.praise.bean.PraiseCancelResultDataBean;
import cn.com.mobnote.golukmobile.praise.bean.PraiseResultBean;
import cn.com.mobnote.golukmobile.praise.bean.PraiseResultDataBean;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videoclick.NewestVideoClickRequest;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.ShareVideoShortUrlRequest;
import cn.com.mobnote.golukmobile.videosuqare.bean.ShareVideoBean;
import cn.com.mobnote.golukmobile.videosuqare.bean.ShareVideoResultBean;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 精选
 * 
 * @author mobnote
 *
 */
public class VideoDetailActivity extends BaseActivity implements OnClickListener, OnRefreshListener,
		OnRTScrollListener, ICommentFn, TextWatcher, ILiveDialogManagerFn, OnItemClickListener,
		IRequestResultListener {

	/** application */
	public GolukApplication mApp = null;
	public static final String LISTENER_TAG = "VideoDetailActivity";
	/** 布局 **/
	private ImageButton mImageBack = null;
	private TextView mTextTitle = null;
	private ImageView mImageRight = null;
	private TextView mTextSend = null;
	private EditText mEditInput = null;
	private RTPullListView mRTPullListView = null;
	private ImageView mImageRefresh = null;
	public RelativeLayout mCommentLayout = null;
	private boolean isCanInput = true;
	/** 评论 **/
	private ArrayList<CommentBean> commentDataList = null;
	/** 详情 **/
	private VideoJson mVideoJson = null;

	/** 视频id **/
	public static final String VIDEO_ID = "videoid";
	/** 是否允许评论 **/
	public static final String VIDEO_ISCAN_COMMENT = "iscan_input";

	public static final String TYPE = "type";

	public VideoDetailAdapter mAdapter = null;
	/** 保存列表一个显示项索引 */
	private int detailFirstVisible;
	/** 保存列表显示item个数 */
	private int detailVisibleCount;

	/** 操作 (0:首次进入；1:下拉；2:上拉) */
	private int mCurrentOperator = 0;
	/** 上拉刷新时，在ListView底部显示的布局 */
	private RelativeLayout loading = null;
	/** 最近更新时间 */
	private String historyDate = "";
	/** 是否还有分页 */
	private boolean mIsHaveData = true;
	private SharePlatformUtil sharePlatform;
	/** 保存将要删除的数据 */
	private CommentBean mWillDelBean = null;
	/** 专题id **/
	private String ztId = "";
	/** videoid **/
	private String mVideoId = "";
	/** 状态栏的高度 */
	public static int stateBraHeight = 0;

	private CustomLoadingDialog mLoadingDialog = null;
	private boolean clickRefresh = false;
	/** 回调数据没有回来 **/
	private boolean isClick = false;
	/** false评论／false删除／true回复 **/
	private boolean mIsReply = false;
	/** 底部无评论的footer **/
	private View mNoDataView = null;

	/** 禁止评论的footer **/
	private View mForbidCommentView = null;
	/** 回复评论的dialog **/
	private ReplyDialog mReplyDialog = null;
	/** 右侧操作按钮的dialog **/
	private DetailDialog mDetailDialog = null;
	/** 评论超时为10 秒 */
	public static final int COMMENT_CIMMIT_TIMEOUT = 10 * 1000;
	private long mCommentTime = 0;
	/** 判断是精选(0)还是最新(1) **/
	private int mType = 0;
	public VideoDetailHeader mHeader;
	private View mHeaderView;
	private String mTitleStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();
		setContentView(R.layout.comment);
		Intent intent = getIntent();
		if (savedInstanceState == null) {
			String type = intent.getStringExtra(TYPE);
			if ("Wonderful".equals(type)) {
				mType = 0;
				ztId = intent.getStringExtra("ztid");
			} else {
				mType = 1;
				mVideoId = intent.getStringExtra(VIDEO_ID);
				isCanInput = intent.getBooleanExtra(VIDEO_ISCAN_COMMENT, true);
			}

			mTitleStr = intent.getStringExtra("title");

			if (TextUtils.isEmpty(mTitleStr)) {
				mTitleStr = this.getString(R.string.str_videodetail_text);
			} else {
				if (mTitleStr.length() > 12) {
					mTitleStr = mTitleStr.substring(0, 12) + this.getString(R.string.str_omit);
				}
			}
		} else {
			mType = savedInstanceState.getInt("Wonderful");
			mVideoId = savedInstanceState.getString(VIDEO_ID);
			ztId = savedInstanceState.getString("ztid");
			isCanInput = savedInstanceState.getBoolean(VIDEO_ISCAN_COMMENT, true);
		}
		commentDataList = new ArrayList<CommentBean>();
		initView();

		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数
		historyDate = GolukUtils.getCurrentFormatTime(this);

		initListener();

		getDetailData();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		outState.putInt("Wonderful", mType);
		if (mType == 0) {
			outState.putString("ztid", ztId);
		} else {
			outState.putString(VIDEO_ID, mVideoId);
			outState.putBoolean(VIDEO_ISCAN_COMMENT, isCanInput);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		getStateHeight();
	}

	private void getStateHeight() {
		Rect rectangle = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
		stateBraHeight = rectangle.top;
		GolukDebugUtils.e("", "videoDetailActivity-----------------statusBarHeight:" + stateBraHeight);
	}

	@Override
	protected void onResume() {
		mHeader.startPlayer();
		super.onResume();
		mApp.setContext(this, "detailcomment");
	}

	private void initView() {
		mImageBack = (ImageButton) findViewById(R.id.comment_back);
		mTextTitle = (TextView) findViewById(R.id.comment_title);
		mImageRight = (ImageView) findViewById(R.id.comment_title_right);
		mTextSend = (TextView) findViewById(R.id.comment_send);
		mEditInput = (EditText) findViewById(R.id.comment_input);
		mRTPullListView = (RTPullListView) findViewById(R.id.commentRTPullListView);
		mImageRefresh = (ImageView) findViewById(R.id.video_detail_click_refresh);
		mCommentLayout = (RelativeLayout) findViewById(R.id.comment_layout);

		mImageRight.setImageResource(R.drawable.mine_icon_more);
		mTextTitle.setText(mTitleStr);
		mAdapter = new VideoDetailAdapter(this, 0);
		mHeader = new VideoDetailHeader(this, mType);
		mHeaderView = mHeader.createHeadView();
		mRTPullListView.addHeaderView(mHeaderView);
		mHeaderView.setVisibility(View.GONE);
		mRTPullListView.setAdapter(mAdapter);
	}

	private void initListener() {
		mImageBack.setOnClickListener(this);
		mImageRight.setOnClickListener(this);
		mTextSend.setOnClickListener(this);
		mImageRight.setOnClickListener(this);
		mEditInput.addTextChangedListener(this);
		mImageRefresh.setOnClickListener(this);

		mRTPullListView.setonRefreshListener(this);
		mRTPullListView.setOnRTScrollListener(this);
		mRTPullListView.setOnItemClickListener(this);

	}

	/**
	 * 获取网络视频详情数据
	 */
	public void getDetailData() {
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);
		// 设置这个参数第一次进来会由下拉状态变为松开刷新的状态
		mRTPullListView.firstFreshState();
		boolean b = false;

		if (!UserUtils.isNetDeviceAvailable(this)) {
			mRTPullListView.setVisibility(View.GONE);
			mCommentLayout.setVisibility(View.GONE);
			mImageRefresh.setVisibility(View.VISIBLE);
			GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
			return;
		}

		if (mType == 0) {
			SingleVideoRequest request = new SingleVideoRequest(IPageNotifyFn.PageType_VideoDetail, this);
			b = request.get(ztId);
			GolukDebugUtils.e("", "----WonderfulActivity-----b====: " + b);
		} else {
			SingleDetailRequest request = new SingleDetailRequest(IPageNotifyFn.PageType_VideoDetail, this);
			b = request.get(mVideoId);
			GolukDebugUtils.e("", "----VideoDetailActivity-----b====: " + b);
		}
		if (!b) {
			mImageRefresh.setVisibility(View.VISIBLE);
		} else {
			if (clickRefresh)
				showLoadingDialog();
		}
	}

	// 是否允许评论
	private void permitInput() {
		if (!isCanInput) {
			mCommentLayout.setVisibility(View.GONE);
		} else {
			mCommentLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 获取评论列表数据
	 */
	public void getCommentList(int operation, String timestamp) {
		String type = ICommentFn.COMMENT_TYPE_VIDEO;
		if (mType == 0) {
			type = ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO;
		}
		CommentListRequest request = new CommentListRequest(IPageNotifyFn.PageType_CommentList, this);
		request.get(mVideoJson.data.avideo.video.videoid, type, operation, timestamp);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.comment_back:
			if (GolukUtils.isFastDoubleClick()) {
				return;
			}
			finish();
			break;
		case R.id.comment_title_right:
			if (!isClick) {
				return;
			}
			if (null == mVideoJson) {
				if (!UserUtils.isNetDeviceAvailable(this)) {
					GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
					return;
				}
			}
			mDetailDialog = new DetailDialog(this, mVideoJson);
			mDetailDialog.show();
			break;
		case R.id.comment_send:
			if (!UserUtils.isNetDeviceAvailable(this)) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
				return;
			}
			if (!isClick) {
				return;
			}
			UserUtils.hideSoftMethod(this);
			click_send();
			break;
		case R.id.video_detail_click_refresh:
			clickRefresh = true;
			getDetailData();
			break;
		default:
			break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			int count = mRTPullListView.getAdapter().getCount();
			int visibleCount = detailFirstVisible + detailVisibleCount;

			GolukDebugUtils.e("", "----VideoDetailActivity-----onScrollStateChanged-----222: " + count + "  vicount:"
					+ visibleCount + "  mIsHaveData===" + mIsHaveData);
			if (null != mAdapter) {
				mHeader.scrollDealPlayer();
			}

			if (count == visibleCount && mIsHaveData) {
				if (null != mVideoJson && null != mVideoJson.data && null != mVideoJson.data.avideo
						&& null != mVideoJson.data.avideo.video && null != mVideoJson.data.avideo.video.videoid) {
					startPush();
				}
			}
			if ((count == visibleCount) && (count > 20) && !mIsHaveData) {
				GolukUtils.showToast(this,
						this.getResources().getString(R.string.str_pull_refresh_listview_bottom_reach));
			}
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		detailFirstVisible = firstVisibleItem;
		detailVisibleCount = visibleItemCount;
	}

	/*
	 * 
	 * 首次进入，数据回调处理 之调用视频详情的借口
	 */
	private void firstEnterCallBack(int count, VideoJson videoJsonData, ArrayList<CommentBean> dataList) {
		// 首次进入
		this.mAdapter.setData(dataList);
		mRTPullListView.onRefreshComplete(getLastRefreshTime());
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
			addFoot();
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}
	}

	// 下拉刷新，数据回调处理
	private void pullCallBack(int count, VideoJson videoJsonData, ArrayList<CommentBean> dataList) {
		this.mAdapter.setData(dataList);
		mRTPullListView.onRefreshComplete(getLastRefreshTime());
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
			addFoot();
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}

	}

	// 上拉刷新，数据回调处理
	private void pushCallBack(int count, VideoJson videoJsonData, ArrayList<CommentBean> dataList) {
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}
		this.mAdapter.appendData(dataList);
	}

	@Override
	public void onRefresh() {
		startPull();
	}

	// 开始下拉刷新
	private void startPull() {
		if (mCurrentOperator == OPERATOR_UP) {
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
			return;
		}
		mCurrentOperator = OPERATOR_DOWN;
		getDetailData();
	}

	// 开始上拉刷新
	private void startPush() {
		if (mCurrentOperator != OPERATOR_NONE) {
			return;
		}
		mCurrentOperator = OPERATOR_UP;
		getCommentList(OPERATOR_DOWN, mAdapter.getLastDataTime());
	}

	// 发表评论
	private void click_send() {
		// 发评论／回复 前需要先判断用户是否登录
		if (!mApp.isUserLoginSucess) {
			Intent intent = new Intent(this, UserLoginActivity.class);
			intent.putExtra("isInfo", "back");
			startActivity(intent);
			return;
		}
		UserInfo loginUser = mApp.getMyInfo();
		if (null != mWillDelBean && loginUser.uid.equals(mWillDelBean.mUserId) && mIsReply) {
			mIsReply = false;
		}
		long currentTime = System.currentTimeMillis();
		if (currentTime - mCommentTime < COMMENT_CIMMIT_TIMEOUT) {
			LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
					this.getResources().getString(R.string.comment_sofast_text));
			return;
		}

		final String content = mEditInput.getText().toString().trim();
		if (null == content || "".equals(content)) {
			GolukUtils.showToast(this, this.getString(R.string.str_input_comment_content));
			return;
		}
		httpPost_requestAdd(content);
	}

	// 删除评论
	public void httpPost_requestDel(String id) {
		CommentDeleteRequest request = new CommentDeleteRequest(IPageNotifyFn.PageType_DelComment, this);
		boolean isSucess = request.get(id);
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, this.getString(R.string.str_delete_fail));
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", this.getString(R.string.str_delete_ongoing),
				true);
	}

	// 添加评论
	private void httpPost_requestAdd(String txt) {
		if (null == mVideoJson.data.avideo.video.videoid) {
			GolukUtils.showToast(this, this.getString(R.string.str_load_data_ongoing));
			return;
		}
		String type = ICommentFn.COMMENT_TYPE_VIDEO;
		if (mType == 0) {
			type = ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO;
		}
		CommentAddRequest request = new CommentAddRequest(IPageNotifyFn.PageType_AddComment, this);
		boolean isSucess = false;
		if (mIsReply) {
			isSucess = request.get(mVideoJson.data.avideo.video.videoid, type, txt, mWillDelBean.mUserId,
					mWillDelBean.mUserName, ztId);
		} else {
			isSucess = request.get(mVideoJson.data.avideo.video.videoid, type, txt, "", "", ztId);
		}
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", this.getString(R.string.str_comment_ongoing), true);
	}

	//点赞请求
	public boolean sendPraiseRequest() {
		PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
		return request.get("1", mVideoJson.data.avideo.video.videoid, "1");
	}

	//取消点赞请求
	public boolean sendCancelPraiseRequest() {
		PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
		return request.get("1", mVideoJson.data.avideo.video.videoid);
	}

	// 异常情况处理
	private void dealCondition() {
		isClick = false;
		mEditInput.clearFocus();
		mEditInput.setFocusable(false);
		mRTPullListView.setVisibility(View.GONE);
		mImageRefresh.setVisibility(View.VISIBLE);
		GolukUtils.showToast(this, this.getString(R.string.str_network_connect_outtime));
	}

	// 数据异常判断
	private boolean isHasData() {
		if (null == mVideoJson || null == mVideoJson.data || null == mVideoJson.data.avideo) {
			return false;
		}
		return true;
	}

	// 分享描述信息
	private String shareDescribe(String describe) {
		String allDescribe = "";
		if (!isHasData()) {
			return allDescribe;
		}
		if (null != mVideoJson.data.avideo.user && null != mVideoJson.data.avideo.video) {
			if (TextUtils.isEmpty(describe)) {
				allDescribe = mVideoJson.data.avideo.user.nickname + "：" + mVideoJson.data.avideo.video.describe;
			} else {
				allDescribe = mVideoJson.data.avideo.user.nickname + "：" + describe;
			}
		}
		return allDescribe;
	}

	private void switchSendState(boolean isSend) {
		if (isSend) {
			mTextSend.setTextColor(this.getResources().getColor(R.color.color_comment_can_send));
		} else {
			mTextSend.setTextColor(this.getResources().getColor(R.color.color_comment_not_send));
		}
	}

	private void updateRefreshTime() {
		historyDate = GolukUtils.getCurrentFormatTime(this);
	}

	private String getLastRefreshTime() {
		return historyDate;
	}

	/**
	 * 添加上拉底部的loading View
	 * 
	 * @author jyf
	 * @date 2015年8月12日
	 */
	private void addFoot() {
		if (mRTPullListView.getFooterViewsCount() > 0) {
			return;
		}
		loading = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading, null);
		mRTPullListView.addFooterView(loading);
	}

	private void removeFoot() {
		if (loading != null) {
			if (mRTPullListView != null) {
				mRTPullListView.removeFooterView(loading);
			}
		}
	}

	private void exit() {
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (null != mReplyDialog) {
			mReplyDialog.dismiss();
		}
		if (null != mDetailDialog) {
			mDetailDialog.dismiss();
		}
		mIsReply = false;
		mHeader.exit();
	}

	private void callBackFailed() {
		if (mCurrentOperator == OPERATOR_FIRST || mCurrentOperator == OPERATOR_DOWN) {
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		} else if (mCurrentOperator == OPERATOR_UP) {
			// 上拉刷新
			removeFoot();
		}

	}

	private void noDataDeal() {
		mIsHaveData = false;
		if (mCurrentOperator == OPERATOR_UP) {// 上拉刷新
			removeFoot();
		} else if (mCurrentOperator == OPERATOR_FIRST || OPERATOR_DOWN == mCurrentOperator) {// 下拉刷新
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		}

	}

	@Override
	protected void onPause() {
		if (mHeader != null) {
			mHeader.pausePlayer();
		}
		super.onPause();
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		final String txt = mEditInput.getText().toString().trim();
		if (null != txt && txt.length() > 0) {
			switchSendState(true);
		} else {
			switchSendState(false);
		}

	}

	/**
	 * 点击删除或者回复评论
	 * 
	 * @param parent
	 * @param view
	 * @param position
	 * @param arg3
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		GolukDebugUtils.e("", "----commentActivity--------position:" + position + "   arg3:" + arg3);
		try {
			mWillDelBean = (CommentBean) parent.getAdapter().getItem(position);
			if (null != mWillDelBean) {
				if (this.mApp.isUserLoginSucess) {
					UserInfo loginUser = mApp.getMyInfo();
					GolukDebugUtils.e("", "-----commentActivity--------mUserId:" + mWillDelBean.mUserId);
					GolukDebugUtils.e("", "-----commentActivity--------uid:" + loginUser.uid);
					if (loginUser.uid.equals(mWillDelBean.mUserId)) {
						mIsReply = false;
					} else {
						mIsReply = true;
					}
				} else {
					mIsReply = true;
				}
				mReplyDialog = new ReplyDialog(this, mWillDelBean, mEditInput, mIsReply);
				mReplyDialog.show();
			}
		} catch (Exception e) {

		}

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// 获得当前得到焦点的View
			View v = mCommentLayout;
			if (UserUtils.isShouldHideInput(v, ev)) {
				UserUtils.hideSoftMethod(this);
				if ("".equals(mEditInput.getText().toString().trim()) && mIsReply) {
					mEditInput.setHint(this.getString(R.string.video_play_comment_text));
					mIsReply = false;
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != sharePlatform) {
			sharePlatform.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void dialogManagerCallBack(int dialogType, int function, String data) {
		if (LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT == dialogType) {
			if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {

			}
		} else if (LiveDialogManager.DIALOG_TYPE_COMMENT_DELETE == dialogType) {
			if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
				httpPost_requestDel(mWillDelBean.mCommentId);
			} else {
				mWillDelBean = null;
			}
		} else if (LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE == dialogType) {
			// 取消删除
		} else if(dialogType == DIALOG_TYPE_VIDEO_DELETED) {
			finish();
		}
	}

	private void showLoadingDialog() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new CustomLoadingDialog(this, null);
			mLoadingDialog.show();
		}
	}

	private void closeLoadingDialog() {
		if (null != mLoadingDialog) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
	}

	/**
	 * 点击评论弹出键盘
	 */
	public void showSoft() {
		if ((mVideoJson.data.avideo.video != null) && (mVideoJson.data.avideo.video.comment != null)
				&& "1".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
			mEditInput.requestFocus();
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(mEditInput, 0);
		} else {
			mEditInput.clearFocus();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (GolukUtils.isFastDoubleClick()) {
				return true;
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		exit();
		super.onDestroy();
	}

	private void addFooterView() {
		if (null == mNoDataView) {
			mNoDataView = LayoutInflater.from(this).inflate(R.layout.video_detail_footer, null);
			mRTPullListView.addFooterView(mNoDataView);
			mNoDataView.setVisibility(View.VISIBLE);
		}
	}

	private void removeFooterView() {
		if (null != mRTPullListView && null != mNoDataView) {
			mRTPullListView.removeFooterView(mNoDataView);
			mNoDataView.setVisibility(View.GONE);
			mNoDataView = null;
		}
	}

	private void addForbitCommentFooterView() {
		if (null == mForbidCommentView) {
			mForbidCommentView = LayoutInflater.from(this).inflate(R.layout.video_detail_forbit_comment_footer, null);
			mRTPullListView.addFooterView(mForbidCommentView);
		}
	}

	private void removeForbitCommentFooterView() {
		if (null != mRTPullListView && null != mForbidCommentView) {
			mRTPullListView.removeFooterView(mForbidCommentView);
			mForbidCommentView = null;
		}
	}

	public void getShare() {
		if (GolukUtils.isFastDoubleClick()) {
			return;
		}
		showLoadingDialog();
		ShareVideoShortUrlRequest request = new ShareVideoShortUrlRequest(IPageNotifyFn.PageType_GetShareURL, this);
		boolean result = request.get(mVideoJson.data.avideo.video.videoid, mVideoJson.data.avideo.video.type);
		GolukDebugUtils.i("detail", "--------result-----Onclick------" + result);
		if (!result) {
			GolukUtils.showToast(this, this.getString(R.string.network_error));
		}
	}

	/**
	 * 视频围观数上报
	 */
	private void clickVideoNumber() {
		try {
			if (null != mVideoJson && null != mVideoJson.data && null != mVideoJson.data.avideo
					&& null != mVideoJson.data.avideo.video && null != mVideoJson.data.avideo.video.videoid) {
				NewestVideoClickRequest videoClickRequest = new NewestVideoClickRequest(
						IPageNotifyFn.PageType_VideoClick, this);
				JSONArray array = new JSONArray();
				JSONObject jsonVideo = new JSONObject();
				jsonVideo.put("videoid", mVideoJson.data.avideo.video.videoid);
				jsonVideo.put("number", "1");
				array.put(jsonVideo);
				if (null != array) {
					videoClickRequest.get("100", "1", array.toString());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private final static int DIALOG_TYPE_VIDEO_DELETED = 24;

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		closeLoadingDialog();
		switch (requestType) {
		case IPageNotifyFn.PageType_VideoDetail:
			mVideoJson = (VideoJson) result;
			if (mVideoJson != null && mVideoJson.success) {
				mRTPullListView.setVisibility(View.VISIBLE);
				mCommentLayout.setVisibility(View.VISIBLE);
				mImageRefresh.setVisibility(View.GONE);
				permitInput();
				isClick = true;
				mEditInput.setFocusable(true);
				updateRefreshTime();

				mHeader.setData(mVideoJson);
				VideoAllData videoAllData = mVideoJson.data;
				if (mVideoJson.data == null) {
					mCurrentOperator = OPERATOR_NONE;
					return;
				}

				ZTHead ztHead = videoAllData.head;
				VideoSquareDetailInfo avideo = videoAllData.avideo;

				if (avideo == null) {
					mCurrentOperator = OPERATOR_NONE;
					return;
				}

				VideoInfo videoInfo = avideo.video;
				if (videoInfo == null) {
					mCurrentOperator = OPERATOR_NONE;
					return;
				}

				if (OPERATOR_FIRST == mCurrentOperator) {
					// 首次进入
					mHeader.getHeadData(true);
					if (null != ztHead) {
						this.mTextTitle.setText(ztHead.ztitle);
					}

				} else if (OPERATOR_DOWN == mCurrentOperator) {
					// 下拉刷新
					mHeader.getHeadData(false);
				}
				mHeaderView.setVisibility(View.VISIBLE);
				VideoCommentInfo commentInfo = videoInfo.comment;

				if (commentInfo != null) {
					if ("0".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
						removeFooterView();
						mCommentLayout.setVisibility(View.GONE);
						addForbitCommentFooterView();
						mRTPullListView.setEnabled(false);
						mRTPullListView.onRefreshComplete(getLastRefreshTime());
						mCurrentOperator = OPERATOR_NONE;
						return;
					} else {
						removeForbitCommentFooterView();
						mRTPullListView.setEnabled(true);
						mCommentLayout.setVisibility(View.VISIBLE);
					}
					int count = 0;
					if (!TextUtils.isEmpty(commentInfo.comcount) && TextUtils.isDigitsOnly(commentInfo.comcount)) {
						count = Integer.parseInt(commentInfo.comcount);
					}
					if (count == 0) {
						mIsHaveData = false;
						mRTPullListView.onRefreshComplete(getLastRefreshTime());
						addFooterView();
						mCurrentOperator = OPERATOR_NONE;
					} else {
						removeFooterView();
						getCommentList(OPERATOR_FIRST, "");
					}
					mCommentLayout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {

							showSoft();
						}
					});
				}
				clickVideoNumber();
			} else if(mVideoJson != null && !mVideoJson.success) {
				mRTPullListView.setVisibility(View.GONE);
				mCommentLayout.setVisibility(View.GONE);
				if(null != mVideoJson.data) {
					if("4".equals(mVideoJson.data.result)) {
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								DIALOG_TYPE_VIDEO_DELETED, "",
								this.getResources().getString(R.string.str_video_removed));
					} else if("3".equals(mVideoJson.data.result)) {
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								DIALOG_TYPE_VIDEO_DELETED, "",
								this.getResources().getString(R.string.str_video_not_exist));
					} else {
						dealCondition();
						mCurrentOperator = OPERATOR_NONE;
					}
				}
			} else {
				dealCondition();
				mCurrentOperator = OPERATOR_NONE;
			}
			break;
		case IPageNotifyFn.PageType_CommentList:

			CommentResultBean resultBean = (CommentResultBean) result;
			if (resultBean != null && resultBean.success && resultBean.data != null) {
				CommentDataBean dataBean = resultBean.data;
				int count = 0;
				if (!TextUtils.isEmpty(dataBean.count) && TextUtils.isDigitsOnly(dataBean.count)) {
					count = Integer.parseInt(dataBean.count);
				}

				if (null == dataBean.comments || dataBean.comments.size() <= 0) {
					// 无数据
					noDataDeal();
					mCurrentOperator = OPERATOR_NONE;
					return;
				}

				// 有数据
				commentDataList.clear();
				for (CommentItemBean item : dataBean.comments) {
					CommentBean bean = new CommentBean();
					bean.mSeq = item.seq;
					bean.mCommentId = item.commentId;
					bean.mCommentTime = item.time;
					bean.mCommentTxt = item.text;
					if (item.reply != null) {
						bean.mReplyId = item.reply.id;
						bean.mReplyName = item.reply.name;
					}
					if (item.author != null) {
						bean.customavatar = item.author.customavatar;
						bean.mUserHead = item.author.avatar;
						bean.mUserId = item.author.authorid;
						bean.mUserName = item.author.name;
						if (item.author.label != null) {
							bean.mApprove = item.author.label.approve;
							bean.mApprovelabel = item.author.label.approvelabel;
							bean.mHeadplusv = item.author.label.headplusv;
							bean.mHeadplusvdes = item.author.label.headplusvdes;
							bean.mTarento = item.author.label.tarento;
						}
					}
					commentDataList.add(bean);
				}

				updateRefreshTime();

				if (OPERATOR_FIRST == mCurrentOperator) {
					// 首次进入
					firstEnterCallBack(count, mVideoJson, commentDataList);
				} else if (OPERATOR_UP == mCurrentOperator) {
					// 上拉刷新
					GolukDebugUtils.e("newadapter", "================VideoDetailActivity：commentDataList=="
							+ commentDataList.size());
					pushCallBack(count, mVideoJson, commentDataList);
				} else {
					// 下拉刷新
					pullCallBack(count, mVideoJson, commentDataList);
				}
			} else {
				callBackFailed();
			}
			mCurrentOperator = OPERATOR_NONE;
			break;
		case IPageNotifyFn.PageType_DelComment:
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			CommentDelResultBean DelResultBean = (CommentDelResultBean) result;
			if (null != DelResultBean && DelResultBean.success) {

				mAdapter.deleteData(mWillDelBean);
				mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
						.parseInt(mVideoJson.data.avideo.video.comment.comcount) - 1);
				mHeader.setCommentCount(mVideoJson.data.avideo.video.comment.comcount);
				GolukUtils.showToast(this, this.getString(R.string.str_delete_success));

				if (mAdapter.getCount() < 1) {
					addFooterView();
				} else {
					removeFooterView();
				}
			} else {
				GolukUtils.showToast(this, this.getString(R.string.str_delete_fail));
			}
			mWillDelBean = null;
			break;
		case IPageNotifyFn.PageType_AddComment:
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			CommentAddResultBean addResultBean = (CommentAddResultBean) result;
			if (null != addResultBean && addResultBean.success) {
				CommentAddBean addBean = addResultBean.data;
				if (addBean == null) {
					GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
					return;
				}
				CommentBean bean = new CommentBean();
				bean.customavatar = addBean.customavatar;
				bean.mReplyId = addBean.replyid;
				bean.mReplyName = addBean.replyname;
				bean.mSeq = addBean.seq;
				bean.result = addBean.result;
				bean.mCommentTime = addBean.time;
				bean.mCommentTxt = addBean.text;
				bean.mCommentId = addBean.commentid;
				bean.mUserHead = addBean.authoravatar;
				bean.mUserId = addBean.authorid;
				bean.mUserName = addBean.authorname;
				if (addBean.label != null) {
					bean.mApprove = addBean.label.approve;
					bean.mApprovelabel = addBean.label.approvelabel;
					bean.mHeadplusv = addBean.label.headplusv;
					bean.mHeadplusvdes = addBean.label.headplusvdes;
					bean.mTarento = addBean.label.tarento;
				}

				bean.mCommentTime = GolukUtils.getCurrentCommentTime();
				if (!"".equals(bean.result)) {
					if ("0".equals(bean.result)) {// 成功
						removeFooterView();
						this.mAdapter.addFirstData(bean);
						mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
								.parseInt(mVideoJson.data.avideo.video.comment.comcount) + 1);
						mHeader.setCommentCount(mVideoJson.data.avideo.video.comment.comcount);
						mEditInput.setText("");
						switchSendState(false);
						// 回复完评论之后需要还原状态以判断下次是评论还是回复
						mIsReply = false;
						mEditInput.setHint(this.getString(R.string.video_play_comment_text));
						mCommentTime = System.currentTimeMillis();
					} else if ("1".equals(bean.result)) {
						GolukDebugUtils.e("", "参数错误");
					} else if ("2".equals(bean.result)) {// 重复评论
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.FUNCTION_DIALOG_OK, "",
								this.getResources().getString(R.string.comment_repeat_text));
					} else if ("3".equals(bean.result)) {// 频繁评论
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
								this.getResources().getString(R.string.comment_sofast_text));
					} else {
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.FUNCTION_DIALOG_OK, "",
								this.getString(R.string.str_save_comment_fail));
					}
				}

			} else {
				GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
			}
			break;
		case IPageNotifyFn.PageType_GetShareURL:
			closeLoadingDialog();
			if (!isHasData()) {
				return;
			}
			ShareVideoResultBean shareVideoResultBean = (ShareVideoResultBean) result;
			if (shareVideoResultBean != null && shareVideoResultBean.success && shareVideoResultBean.data != null) {
				ShareVideoBean shareVideoBean = shareVideoResultBean.data;
				String shareurl = shareVideoBean.shorturl;
				String coverurl = shareVideoBean.coverurl;
				String describe = shareVideoBean.describe;
				String realDesc = this.getString(R.string.str_share_board_real_desc);
				String allDescribe = shareDescribe(describe);
				String ttl = this.getString(R.string.str_video_edit_share_title);
				Bitmap bitmap = null;
//				if (null != mVideoJson.data.avideo.video.picture) {
//					// 缩略图
//					bitmap = getThumbBitmap(mVideoJson.data.avideo.video.picture);
//				}
				if (!this.isFinishing()) {
					if (null != mVideoJson.data.avideo.video) {
						CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shareurl, coverurl,
								allDescribe, ttl, bitmap, realDesc, mVideoJson.data.avideo.video.videoid);
						shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
					}
				}

			} else {
				GolukUtils.showToast(this, this.getString(R.string.str_network_connect_outtime));
			}
			break;
		case IPageNotifyFn.PageType_Praise:
			PraiseResultBean praiseResultBean = (PraiseResultBean) result;
			if (praiseResultBean == null || !praiseResultBean.success) {
				GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
				return;
			}

			PraiseResultDataBean ret = praiseResultBean.data;
			if(null != ret && !TextUtils.isEmpty(ret.result)) {
				if("0".equals(ret.result)) {
					// do nothing
				} else if("7".equals(ret.result)) {
					GolukUtils.showToast(this, this.getString(R.string.str_no_duplicated_praise));
				} else {
					GolukUtils.showToast(this, this.getString(R.string.str_praise_failed));
				}
			}
			break;
		case IPageNotifyFn.PageType_PraiseCancel:
			PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
			if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
				GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
				return;
			}

			PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
			if(null != cancelRet && !TextUtils.isEmpty(cancelRet.result)) {
				if("0".equals(cancelRet.result)) {
					// do nothing
				} else {
					GolukUtils.showToast(this, this.getString(R.string.str_cancel_praise_failed));
				}
			}
			break;
		case IPageNotifyFn.PageType_VideoClick:
			break;
		default:
//			Log.e("", "======onLoadComplete result==" + result.toString());
			break;
		}
	}
}
