package cn.com.mobnote.golukmobile.videodetail;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sina.weibo.sdk.constant.WBConstants.Msg;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.UserLoginActivity;
import cn.com.mobnote.golukmobile.carrecorder.util.ImageManager;
import cn.com.mobnote.golukmobile.carrecorder.util.MD5Utils;
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
import cn.com.mobnote.golukmobile.player.FullScreenVideoView;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.UCUserInfo;
import cn.com.mobnote.golukmobile.usercenter.UserCenterActivity;
import cn.com.mobnote.golukmobile.videoclick.NewestVideoClickRequest;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailAdapter.ViewHolder;
import cn.com.mobnote.golukmobile.videosuqare.PraiseRequest;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.ShareVideoShortUrlRequest;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.bean.PraiseResultBean;
import cn.com.mobnote.golukmobile.videosuqare.bean.ShareVideoBean;
import cn.com.mobnote.golukmobile.videosuqare.bean.ShareVideoResultBean;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GlideUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 精选
 * 
 * @author mobnote
 *
 */
public class VideoDetailActivity extends BaseActivity implements OnClickListener, OnRefreshListener,
		OnRTScrollListener, VideoSuqareManagerFn, ICommentFn, TextWatcher, ILiveDialogManagerFn, OnItemClickListener,
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

	/** 监听管理类 **/
	private VideoSquareManager mVideoSquareManager = null;
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
				mTitleStr = "视频详情";
			} else {
				if (mTitleStr.length() > 12) {
					mTitleStr = mTitleStr.substring(0, 12) + "...";
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
		historyDate = GolukUtils.getCurrentFormatTime();

		setListener();
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
		super.onResume();
		mApp.setContext(this, "detailcomment");
		mHeader.startPlayer();
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
			GolukUtils.showToast(this, "当前网络不可用，请检查网络");
			return;
		}

		if (mType == 0) {
			SingleVideoRequest request = new SingleVideoRequest(VSquare_Req_Get_VideoDetail, this);
			b = request.get(ztId);
			GolukDebugUtils.e("", "----WonderfulActivity-----b====: " + b);
		} else {
			SingleDetailRequest request = new SingleDetailRequest(VSquare_Req_Get_VideoDetail, this);
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
		CommentListRequest request = new CommentListRequest(VSquare_Req_List_Comment, this);
		request.get(mVideoJson.data.avideo.video.videoid, type, operation, timestamp);
		// final String requestStr =
		// JsonUtil.getCommentRequestStr(mVideoJson.data.avideo.video.videoid,
		// ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO, operation, timestamp,
		// PAGE_SIZE, ztId);
		// GolukDebugUtils.e("",
		// "================VideoDetailActivity：requestStr==" + requestStr);
		// boolean isSucess =
		// mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
		// VideoSuqareManagerFn.VSquare_Req_List_Comment, requestStr);
		// GolukDebugUtils.e("",
		// "================VideoDetailActivity：isSucess==" + isSucess);
		// if (!isSucess) {
		// // TODO 失败
		// }
	}

	// 注册监听
	private void setListener() {
		// 注册监听
		mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager.checkVideoSquareManagerListener(LISTENER_TAG)) {
				mVideoSquareManager.removeVideoSquareManagerListener(LISTENER_TAG);
			}
			mVideoSquareManager.addVideoSquareManagerListener(LISTENER_TAG, this);
		}
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
		mCurrentOperator = OPERATOR_DOWN;
		getDetailData();
	}

	// 开始上拉刷新
	private void startPush() {
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
			GolukUtils.showToast(this, "请输入评论内容");
			return;
		}
		httpPost_requestAdd(content);
	}

	// 删除评论
	public void httpPost_requestDel(String id) {
		CommentDeleteRequest request = new CommentDeleteRequest(VSquare_Req_Del_Comment, this);
		boolean isSucess = request.get(id);
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, "删除失败");
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", "正在删除", true);
	}

	// 添加评论
	private void httpPost_requestAdd(String txt) {
		if (null == mVideoJson.data.avideo.video.videoid) {
			GolukUtils.showToast(this, "数据加载中，请稍候再试");
			return;
		}
		String type = ICommentFn.COMMENT_TYPE_VIDEO;
		if (mType == 0) {
			type = ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO;
		}
		CommentAddRequest request = new CommentAddRequest(VSquare_Req_Add_Comment, this);
		boolean isSucess = false;
		if (mIsReply) {
			isSucess = request.get(mVideoJson.data.avideo.video.videoid, type, txt, mWillDelBean.mUserId,
					mWillDelBean.mUserName, ztId);
		} else {
			isSucess = request.get(mVideoJson.data.avideo.video.videoid, type, txt, "", "", ztId);
		}
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, "评论失败!");
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", "正在提交评论", true);
	}

	//点赞请求
	public boolean sendPraiseRequest() {
		PraiseRequest request = new PraiseRequest(VSquare_Req_VOP_Praise, this);
		return request.get("1", mVideoJson.data.avideo.video.videoid, "1");
	}

//	@Override
//	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
//		GolukDebugUtils.e("", "=====VideoSuqare_CallBack===========VideoDetailActivity：event==" + event);
//		if (event == VSquare_Req_Get_VideoDetail) {
//			// callBack_videoDetail(msg, param1, param2);
//		} else if (event == VSquare_Req_List_Comment) {
//			// callBack_commentList(msg, param1, param2);
//		} else if (event == VSquare_Req_VOP_GetShareURL_Video) {
////			callBack_share(msg, param1, param2);
//		} else if (event == VSquare_Req_VOP_Praise) {
////			callBack_praise(msg, param1, param2);
//		} else if (VSquare_Req_Add_Comment == event) {
//			// callBack_commentAdd(msg, param1, param2);
//		} else if (VSquare_Req_Del_Comment == event) {
//			// callBack_commentDel(msg, param1, param2);
//		}
//	}

	// 异常情况处理
	private void dealCondition() {
		isClick = false;
		mEditInput.clearFocus();
		mEditInput.setFocusable(false);
		mRTPullListView.setVisibility(View.GONE);
		mImageRefresh.setVisibility(View.VISIBLE);
		GolukUtils.showToast(this, "网络连接超时，请检查网络");
	}

//	// 分享回调
//	private void callBack_share(int msg, int param1, Object param2) {
//		closeLoadingDialog();
//		if (!isHasData()) {
//			return;
//		}
//		if (RESULE_SUCESS == msg) {
//			try {
//				JSONObject result = new JSONObject((String) param2);
//				if (result.getBoolean("success")) {
//					JSONObject data = result.getJSONObject("data");
//					String shareurl = data.getString("shorturl");
//					String coverurl = data.getString("coverurl");
//					String describe = data.optString("describe");
//					String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";
//					String allDescribe = shareDescribe(describe);
//					String ttl = "极路客精彩视频";
//					Bitmap bitmap = null;
////					if (null != mVideoJson.data.avideo.video.picture) {
////						// 缩略图
////						bitmap = getThumbBitmap(mVideoJson.data.avideo.video.picture);
////					}
//					if (!this.isFinishing()) {
//						if (null != mVideoJson.data.avideo.video) {
//							CustomShareBoard shareBoard = new CustomShareBoard(this, sharePlatform, shareurl, coverurl,
//									allDescribe, ttl, bitmap, realDesc, mVideoJson.data.avideo.video.videoid);
//							shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
//						}
//					}
//
//				} else {
//					GolukUtils.showToast(this, "当前网络不可用，请检查网络");
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		} else {
//			GolukUtils.showToast(this, "网络连接超时，请检查网络");
//		}
//	}

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


//	// 点赞回调
//	private void callBack_praise(int msg, int param1, Object param2) {
//		GolukDebugUtils.e("lily", "222VideoSuqare_CallBack=@@@@Get_VideoDetail==" + "=msg=" + msg + "=param1=" + param1
//				+ "=param2=" + param2);
//		if (msg == RESULE_SUCESS) {
//			// {"data":{"result":"3"},"msg":"视频不存在","success":false}
//			try {
//				String jsonStr = (String) param2;
//				JSONObject jsonObject = new JSONObject(jsonStr);
//				JSONObject dataObject = jsonObject.optJSONObject("data");
//				String result = dataObject.optString("result");
//				if ("0".equals(result)) {
//					// 成功
//				} else {
//					// 错误
//					GolukUtils.showToast(this, "当前网络不可用，请检查网络");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			GolukUtils.showToast(this, "网络连接超时，请检查网络");
//		}
//	}

	// 添加评论回调
	// private void callBack_commentAdd(int msg, int param1, Object param2) {
	// // LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
	// // if (1 != msg) {
	// // GolukUtils.showToast(this, "评论失败");
	// // return;
	// // }
	// // try {
	// // JSONObject obj = new JSONObject((String) param2);
	// // Log.e("", "================comment：obj==" + obj.toString());
	// // boolean isSucess = obj.getBoolean("success");
	// // if (!isSucess) {
	// // GolukUtils.showToast(this, "评论失败");
	// // return;
	// // }
	// //
	// // CommentBean bean =
	// JsonUtil.parseAddCommentData(obj.getJSONObject("data"));
	// // if (null != bean) {
	// // bean.mCommentTime = GolukUtils.getCurrentCommentTime();
	// // if (!"".equals(bean.result)) {
	// // if ("0".equals(bean.result)) {// 成功
	// // removeFooterView();
	// // commentDataList.add(0, bean);
	// // this.mAdapter.addFirstData(bean);
	// // mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
	// // .parseInt(mVideoJson.data.avideo.video.comment.comcount) + 1);
	// //
	// mHeader.setCommentCount(mVideoJson.data.avideo.video.comment.comcount);
	// // mEditInput.setText("");
	// // switchSendState(false);
	// // // 回复完评论之后需要还原状态以判断下次是评论还是回复
	// // mIsReply = false;
	// // mEditInput.setHint("写评论");
	// // mCommentTime = System.currentTimeMillis();
	// // } else if ("1".equals(bean.result)) {
	// // GolukDebugUtils.e("", "参数错误");
	// // } else if ("2".equals(bean.result)) {// 重复评论
	// // LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
	// // LiveDialogManager.FUNCTION_DIALOG_OK, "",
	// // this.getResources().getString(R.string.comment_repeat_text));
	// // } else if ("3".equals(bean.result)) {// 频繁评论
	// // LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
	// // LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
	// // this.getResources().getString(R.string.comment_sofast_text));
	// // } else {
	// // LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
	// // LiveDialogManager.FUNCTION_DIALOG_OK, "", "评论保存失败。");
	// // }
	// // }
	// // } else {
	// // GolukUtils.showToast(this, "评论失败");
	// // }
	// // } catch (Exception e) {
	// // GolukUtils.showToast(this, "评论失败");
	// // }
	//
	// }

	// // 删除评论回调
	// private void callBack_commentDel(int msg, int param1, Object param2) {
	// LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
	// if (1 != msg) {
	// // 失败
	// GolukUtils.showToast(this, "网络异常，请稍后重试");
	// mWillDelBean = null;
	// return;
	// }
	// try {
	// JSONObject obj = new JSONObject((String) param2);
	// boolean isSucess = obj.getBoolean("success");
	// if (isSucess) {
	// int size = commentDataList.size();
	// for (int i = 0; i < size; i++) {
	// if (commentDataList.get(i).mCommentId.equals(mWillDelBean.mCommentId)) {
	// commentDataList.remove(i);
	// break;
	// }
	// }
	// mAdapter.deleteData(mWillDelBean);
	// mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
	// .parseInt(mVideoJson.data.avideo.video.comment.comcount) - 1);
	// mHeader.setCommentCount(mVideoJson.data.avideo.video.comment.comcount);
	// GolukUtils.showToast(this, "删除成功");
	//
	// if (mAdapter.getCount() <= 1) {
	// addFooterView();
	// } else {
	// removeFooterView();
	// }
	// } else {
	// GolukUtils.showToast(this, "删除失败");
	// }
	// } catch (Exception e) {
	// }
	// mWillDelBean = null;
	// }

	private void switchSendState(boolean isSend) {
		if (isSend) {
			mTextSend.setTextColor(this.getResources().getColor(R.color.color_comment_can_send));
		} else {
			mTextSend.setTextColor(this.getResources().getColor(R.color.color_comment_not_send));
		}
	}

//	public Bitmap getThumbBitmap(String netUrl) {
//		String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
//		String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
//		File file = new File(path + File.separator + name);
//		Bitmap t_bitmap = null;
//		if (file.exists()) {
//			t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
//		}
//		return t_bitmap;
//	}

	private void updateRefreshTime() {
		historyDate = GolukUtils.getCurrentFormatTime();
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
		if (null != mVideoSquareManager) {
			mVideoSquareManager.removeVideoSquareManagerListener(LISTENER_TAG);
		}

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
		super.onPause();
		if (null == mVideoJson) {
			if (!UserUtils.isNetDeviceAvailable(this)) {
				return;
			}
		}
		mHeader.pausePlayer();
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
					mEditInput.setHint("写评论");
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
		if ("1".equals(mVideoJson.data.avideo.video.comment.iscomment)) {
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
		ShareVideoShortUrlRequest request = new ShareVideoShortUrlRequest(VSquare_Req_VOP_GetShareURL_Video, this);
		boolean result = request.get(mVideoJson.data.avideo.video.videoid, mVideoJson.data.avideo.video.type);
		GolukDebugUtils.i("detail", "--------result-----Onclick------" + result);
		if (!result) {
			GolukUtils.showToast(this, "网络异常，请检查网络");
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

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		closeLoadingDialog();
		switch (requestType) {
		case VSquare_Req_Get_VideoDetail:
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
					return;
				}
				ZTHead ztHead = videoAllData.head;
				VideoSquareDetailInfo avideo = videoAllData.avideo;

				if (avideo == null) {
					return;
				}

				VideoInfo videoInfo = avideo.video;
				if (videoInfo == null) {
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
			} else {
				dealCondition();
			}
			break;
		case VSquare_Req_List_Comment:

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
			break;
		case VSquare_Req_Del_Comment:
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			CommentDelResultBean DelResultBean = (CommentDelResultBean) result;
			if (null != DelResultBean && DelResultBean.success) {
				int size = commentDataList.size();
				for (int i = 0; i < size; i++) {
					if (commentDataList.get(i).mCommentId.equals(DelResultBean.data.comment_id)) {
						commentDataList.remove(i);
						break;
					}
				}
				mAdapter.setData(commentDataList);
				mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
						.parseInt(mVideoJson.data.avideo.video.comment.comcount) - 1);
				mHeader.setCommentCount(mVideoJson.data.avideo.video.comment.comcount);
				GolukUtils.showToast(this, "删除成功");

				if (mAdapter.getCount() < 1) {
					addFooterView();
				} else {
					removeFooterView();
				}
			} else {
				GolukUtils.showToast(this, "删除失败");
				mWillDelBean = null;
			}
			mWillDelBean = null;
			break;
		case VSquare_Req_Add_Comment:
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			CommentAddResultBean addResultBean = (CommentAddResultBean) result;
			if (null != addResultBean && addResultBean.success) {
				CommentAddBean addBean = addResultBean.data;
				if (addBean == null) {
					GolukUtils.showToast(this, "评论失败");
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
						commentDataList.add(0, bean);
						this.mAdapter.addFirstData(bean);
						mVideoJson.data.avideo.video.comment.comcount = String.valueOf(Integer
								.parseInt(mVideoJson.data.avideo.video.comment.comcount) + 1);
						mHeader.setCommentCount(mVideoJson.data.avideo.video.comment.comcount);
						mEditInput.setText("");
						switchSendState(false);
						// 回复完评论之后需要还原状态以判断下次是评论还是回复
						mIsReply = false;
						mEditInput.setHint("写评论");
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
								LiveDialogManager.FUNCTION_DIALOG_OK, "", "评论保存失败。");
					}
				}

			} else {
				GolukUtils.showToast(this, "评论失败");
			}
			break;
		case VSquare_Req_VOP_GetShareURL_Video:
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
				String realDesc = "极路客精彩视频(使用#极路客Goluk#拍摄)";
				String allDescribe = shareDescribe(describe);
				String ttl = "极路客精彩视频";
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
				GolukUtils.showToast(this, "网络连接超时，请检查网络");
			}
			break;
		case VSquare_Req_VOP_Praise:
			PraiseResultBean praiseResultBean = (PraiseResultBean) result;
			if (praiseResultBean == null || !praiseResultBean.success) {
				GolukUtils.showToast(this, "当前网络不可用，请检查网络");
			}
			break;
		case IPageNotifyFn.PageType_VideoClick:
			
			break;
		default:
			Log.e("", "======onLoadComplete result==" + result.toString());
			break;
		}
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		// TODO Auto-generated method stub
		
	}

	/** 头部视频详情holder **/

}
