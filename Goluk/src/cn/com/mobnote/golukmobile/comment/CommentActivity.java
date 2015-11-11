package cn.com.mobnote.golukmobile.comment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.golukmobile.live.LiveDialogManager.ILiveDialogManagerFn;
import cn.com.mobnote.golukmobile.live.UserInfo;
import cn.com.mobnote.golukmobile.videodetail.ReplyDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class CommentActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnRTScrollListener,
		VideoSuqareManagerFn, ILiveDialogManagerFn, ICommentFn, TextWatcher,OnItemClickListener {

	public static final String TAG = "Comment";

	/** application */
	public GolukApplication mApp = null;
	/** 是否退出本界面,　防止用户按了退出键再更新数据 */
	private boolean isExit = true;
	/** 返回键 */
	private ImageButton mBackBtn = null;
	/** 发送评论按钮 */
	private TextView mSendBtn = null;
	/** 评论输入框 */
	private EditText mEditText = null;
	/** 评论列表无数据时显示 */
	private ImageView mNoData = null;
	/** 上拉刷新时，在ListView底部显示的布局 */
	private RelativeLayout loading = null;
	/** 评论关闭显示布局 */
	private TextView mNoInputTv = null;
	/** 输入评论底部的整体布局 */
	private RelativeLayout mCommentInputLayout = null;
	/** 数据显示适配器 */
	private CommentListViewAdapter mAdapter = null;
	/** 可上下拉的ListView */
	private RTPullListView mRTPullListView = null;

	/** 视频、专题或直播的id */
	private String mId = null;
	/** 评论主题类型 (1:单视频；2:专题；3:直播；4:其它) */
	private String mTopicType = null;
	/** 视频发布者的uid, 用于显示判断是否是“车主”发布的信息 */
	private String mVideoUserId = null;
	/** 操作 (0:首次进入；1:下拉；2:上拉) */
	private int mCurrentOperator = 0;
	/** 是否还有分页 */
	private boolean mIsHaveData = true;
	/** 是否弹出软键盘 */
	private boolean mIsShowSoft = true;
	/** 是否允许评论 */
	private boolean isCanInput = true;

	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 最近更新时间 */
	private String historyDate = "";

	private VideoSquareManager mVideoSquareManager = null;

	/** 保存将要删除的数据 */
	private CommentBean mWillDelBean = null;
	/**false评论／false删除／true回复**/
	private boolean mIsReply = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();
		getWindow().setContentView(R.layout.comment_layout);
		getIntentData();
		historyDate = GolukUtils.getCurrentFormatTime();
		initView();
		isExit = false;
		initListener();
		firstDeal();

		CommentTimerManager.getInstance();
	}

	/**
	 * 获取外部传递的数据
	 * 
	 * @author jyf
	 * @date 2015年8月12日
	 */
	private void getIntentData() {
		Intent intent = getIntent();
		if (null != intent) {
			mId = intent.getStringExtra(COMMENT_KEY_MID);
			mTopicType = intent.getStringExtra(COMMENT_KEY_TYPE);
			mIsShowSoft = intent.getBooleanExtra(COMMENT_KEY_SHOWSOFT, true);
			isCanInput = intent.getBooleanExtra(COMMENT_KEY_ISCAN_INPUT, true);
			mVideoUserId = intent.getStringExtra(COMMENT_KEY_USERID);
		}
		GolukDebugUtils.e("", "jyf----CommentActivity-----mId:" + mId + "   type:" + mTopicType + "  mIsShowSoft:"
				+ mIsShowSoft + "  isCanInput:" + isCanInput);
	}

	private void initView() {
		mBackBtn = (ImageButton) findViewById(R.id.comment_back);
		mSendBtn = (TextView) findViewById(R.id.comment_send);
		mEditText = (EditText) findViewById(R.id.comment_input);
		mRTPullListView = (RTPullListView) findViewById(R.id.commentRTPullListView);
		mNoData = (ImageView) findViewById(R.id.comment_nodata);
		mCommentInputLayout = (RelativeLayout) findViewById(R.id.comment_layout);
		mNoInputTv = (TextView) findViewById(R.id.comment_noinput);

		mBackBtn.setOnClickListener(this);
		mSendBtn.setOnClickListener(this);
		mEditText.addTextChangedListener(this);

		mAdapter = new CommentListViewAdapter(this);
		mAdapter.setVideoUserId(mVideoUserId);
		mRTPullListView.setAdapter(mAdapter);
		if (isCanInput) {
			mRTPullListView.setonRefreshListener(this);
			mRTPullListView.setOnRTScrollListener(this);
		}
		mRTPullListView.setOnItemClickListener(this);
		
	}

	/**
	 * 注册数据回调
	 * 
	 * @author jyf
	 * @date 2015年8月12日
	 */
	private void initListener() {
		mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager.checkVideoSquareManagerListener("videosharehotlist")) {
				mVideoSquareManager.removeVideoSquareManagerListener("videosharehotlist");
			}
			mVideoSquareManager.addVideoSquareManagerListener(TAG, this);
		}
	}

	/**
	 * 初次进入界面，数据初始化操作
	 * 
	 * @author jyf
	 * @date 2015年8月12日
	 */
	private void firstDeal() {
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		if (isCanInput) {
			mRTPullListView.setEnabled(true);
			if (mIsShowSoft) {
				mEditText.requestFocus();
				GolukUtils.showSoft(mEditText);
			}
			mCommentInputLayout.setVisibility(View.VISIBLE);
			mNoInputTv.setVisibility(View.GONE);

			mRTPullListView.firstFreshState();
			firstEnter();
		} else {
			mRTPullListView.setEnabled(false);
			mCommentInputLayout.setVisibility(View.GONE);
			mNoInputTv.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, TAG);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.comment_back:
			back();
			break;
		case R.id.comment_send:
			click_send();
			break;
		}
	}

	private void back() {
		isExit = true;
		mVideoSquareManager.removeVideoSquareManagerListener(TAG);
		mIsReply = false;
		CommentTimerManager.getInstance().cancelTimer();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void click_send() {
		// 发评论前需要先判断用户是否登录
		if (!mApp.isUserLoginSucess) {
			Intent intent = new Intent(this, UserLoginActivity.class);
			intent.putExtra("isInfo", "back");
			startActivity(intent);
			return;
		}

		if (CommentTimerManager.getInstance().getIsStarting()) {
			LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
					LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "", "您评论的速度太快了，请休息一下再评论。");
			return;
		}

		final String content = mEditText.getText().toString().trim();
		if (null == content || "".equals(content)) {
			GolukUtils.showToast(this, "请输入评论内容");
			return;
		}
		httpPost_requestAdd(content);
	}

	// 首次进入，数据回调处理
	private void firstEnterCallBack(int count, ArrayList<CommentBean> dataList) {
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
	private void pullCallBack(int count, ArrayList<CommentBean> dataList) {
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

	private void updateRefreshTime() {
		historyDate = GolukUtils.getCurrentFormatTime();
	}

	private String getLastRefreshTime() {
		return historyDate;
	}

	// 上拉刷新，数据回调处理
	private void pushCallBack(int count, ArrayList<CommentBean> dataList) {
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
			this.removeFoot();
		}
		this.mAdapter.appendData(dataList);
	}

	private void callBackFailed() {
		if (mCurrentOperator == OPERATOR_FIRST || mCurrentOperator == OPERATOR_DOWN) {
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		} else if (mCurrentOperator == OPERATOR_UP) {
			// 上拉刷新
			removeFoot();
		}

		noData(mAdapter.getCount() <= 0);
	}

	private void noDataDeal() {
		mIsHaveData = false;
		if (mCurrentOperator == OPERATOR_UP) {// 上拉刷新
			removeFoot();
		} else if (mCurrentOperator == OPERATOR_FIRST || OPERATOR_DOWN == mCurrentOperator) {// 下拉刷新
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		}

		noData(mAdapter.getCount() <= 0);
	}

	// 是否显示无数据提示
	private void noData(boolean isno) {
		if (isno) {
			mRTPullListView.setVisibility(View.GONE);
			mNoData.setVisibility(View.VISIBLE);
		} else {
			mRTPullListView.setVisibility(View.VISIBLE);
			mNoData.setVisibility(View.GONE);
		}
	}

	private void callBack_commentList(int msg, int param1, Object param2) {
		if (1 != msg) {
			// 请求失败
			callBackFailed();
			GolukUtils.showToast(this, "当前网络不可用，请检查网络");
			return;
		}
		try {
			JSONObject rootObj = new JSONObject((String) param2);
			boolean isSucess = rootObj.getBoolean("success");
			if (!isSucess) {
				// 请求失败
				callBackFailed();
				return;
			}
			JSONObject dataObj = rootObj.getJSONObject("data");
			String result = dataObj.getString("result");
			int count = Integer.parseInt(dataObj.getString("count"));
			if (count <= 0) {
				// 　没数据
				noDataDeal();
				return;
			}
			// 有数据
			ArrayList<CommentBean> dataList = JsonUtil.parseCommentData(dataObj.getJSONArray("comments"));
			if (null == dataList || dataList.size() <= 0) {
				// 无数据
				noDataDeal();
				return;
			}
			updateRefreshTime();
			noData(false);

			GolukDebugUtils.e("", "jyf----CommentActivity----msg:" + msg + "  param1:" + param1 + "  param2:" + param2);

			if (OPERATOR_FIRST == mCurrentOperator) {
				// 首次进入
				firstEnterCallBack(count, dataList);
			} else if (OPERATOR_DOWN == mCurrentOperator) {
				// 下拉刷新
				pullCallBack(count, dataList);
			} else if (OPERATOR_UP == mCurrentOperator) {
				// 上拉刷新
				pushCallBack(count, dataList);
			}

		} catch (Exception e) {
			callBackFailed();
		}
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

	private void callBack_commentAdd(int msg, int param1, Object param2) {
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (1 != msg) {
			GolukUtils.showToast(this, "评论失败");
			return;
		}
		try {
			JSONObject obj = new JSONObject((String) param2);
			boolean isSucess = obj.getBoolean("success");
			if (!isSucess) {
				GolukUtils.showToast(this, "评论失败");
				return;
			}

			CommentBean bean = JsonUtil.parseAddCommentData(obj.getJSONObject("data"));
			if (null != bean) {
				noData(false);
				bean.mCommentTime = GolukUtils.getCurrentCommentTime();
				if(!"".equals(bean.result)) {
					if("0".equals(bean.result)) {//成功
						this.mAdapter.addFirstData(bean);
						mEditText.setText("");
						switchSendState(false);
						mIsReply = false;
						mEditText.setHint("写评论");
						CommentTimerManager.getInstance().start(COMMENT_CIMMIT_TIMEOUT);
					} else if("1".equals(bean.result)) {
						GolukDebugUtils.e("", "参数错误");
					} else if("2".equals(bean.result)) {//重复评论
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.FUNCTION_DIALOG_OK, "", "请勿重复发送相同的评论内容。");
					} else if("3".equals(bean.result)) {//频繁评论
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "", "您评论的速度太快了，请休息一下再评论。");
					} else {
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.FUNCTION_DIALOG_OK, "", "评论保存失败。");
					}
				}
				
			} else {
				GolukUtils.showToast(this, "评论失败");
			}
		} catch (Exception e) {
			GolukUtils.showToast(this, "评论失败");
		}

	}

	private void callBack_commentDel(int msg, int param1, Object param2) {
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (1 != msg) {
			// 失败
			GolukUtils.showToast(this, "网络异常，请稍后重试");
			mWillDelBean = null;
			return;
		}
		try {
			JSONObject obj = new JSONObject((String) param2);
			boolean isSucess = obj.getBoolean("success");
			if (isSucess) {
				mAdapter.deleteData(mWillDelBean);
				GolukUtils.showToast(this, "删除成功");

				noData(mAdapter.getCount() <= 0);
			} else {
				GolukUtils.showToast(this, "删除失败");
			}
		} catch (Exception e) {
		}
		mWillDelBean = null;
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (isExit) {
			return;
		}
		GolukDebugUtils.e("", "jyf----CommentActivity----msg:" + msg + "  param1:" + param1 + "  param2:" + param2);
		if (VSquare_Req_List_Comment == event) {
			callBack_commentList(msg, param1, param2);
		} else if (VSquare_Req_Add_Comment == event) {
			callBack_commentAdd(msg, param1, param2);
		} else if (VSquare_Req_Del_Comment == event) {
			callBack_commentDel(msg, param1, param2);
		}
	}

	@Override
	public void onRefresh() {
		// 下拉刷新
		startPull();
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		GolukDebugUtils.e("", "jyf----CommentActivity-----onScrollStateChanged-----scrollState: " + scrollState + "  "
				+ mIsHaveData);
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			int count = mRTPullListView.getAdapter().getCount();
			int visibleCount = wonderfulFirstVisible + wonderfulVisibleCount;

			GolukDebugUtils.e("", "jyf----CommentActivity-----onScrollStateChanged-----222: " + count + "  " + count
					+ "  vicount:" + visibleCount + "  mIsHaveData:" + mIsHaveData);

			if (count == visibleCount && mIsHaveData) {
				startPush();
			}
		}
	}

	// 首次进入
	private void firstEnter() {
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// 开始下拉刷新
	private void startPull() {
		mCurrentOperator = OPERATOR_DOWN;
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// 开始上拉刷新
	private void startPush() {
		GolukDebugUtils.e("", "jyf----CommentActivity-----startPush-----1111" + this.mIsHaveData);
		mCurrentOperator = OPERATOR_UP;
		httpPost_requestList(OPERATOR_DOWN, mAdapter.getLastDataTime());
	}

	private void httpPost_requestList(int operation, String timestamp) {
		final String requestStr = JsonUtil.getCommentRequestStr(mId, mTopicType, operation, timestamp, PAGE_SIZE);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_List_Comment, requestStr);
		if (!isSucess) {
			// TODO 失败
		}
	}

	// 添加评论
	private void httpPost_requestAdd(String txt) {
		String requestStr = "";
		if (mIsReply) {
			requestStr = JsonUtil.getAddCommentJson(mId, mTopicType, txt,
					mWillDelBean.mUserId, mWillDelBean.mUserName,null);
		} else {
			requestStr = JsonUtil.getAddCommentJson(mId, mTopicType, txt,"","",null);
		}
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_Add_Comment, requestStr);
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, "评论失败!");
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", "正在提交评论", true);
	}

	// 删除评论
	private void httpPost_requestDel(String id) {
		final String requestStr = JsonUtil.getDelCommentJson(id);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_Del_Comment, requestStr);
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, "删除失败");
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", "正在删除", true);
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		wonderfulFirstVisible = firstVisibleItem;
		wonderfulVisibleCount = visibleItemCount;
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		GolukDebugUtils.e("", "-----commentActivity--------position:" + position + "   arg3:" + arg3);
		if (null != mAdapter) {
			if ( this.mApp.isUserLoginSucess) {
				mWillDelBean = (CommentBean) mAdapter.getItem(position - 1);
				if(null != mWillDelBean) {
					final UserInfo loginUser = mApp.getMyInfo();
					GolukDebugUtils.e("", "-----commentActivity--------bean userId:" + mWillDelBean.mUserId
							+ "  login user:" + loginUser.uid);
					if (loginUser.uid.equals(mWillDelBean.mUserId)) {
						mIsReply = false;
					} else {
						mIsReply = true;
					}
				} else {
					mIsReply = true;
				}
				new ReplyDialog(this, mWillDelBean, mEditText,mIsReply).show();
				}
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// 获得当前得到焦点的View
			View v = mCommentInputLayout;
			if (UserUtils.isShouldHideInput(v, ev)) {
				UserUtils.hideSoftMethod(this);
				if("".equals(mEditText.getText().toString().trim()) && mIsReply) {
					mEditText.setHint("写评论");
					mIsReply = false;
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}
	
	private void switchSendState(boolean isSend) {
		if (isSend) {
			mSendBtn.setTextColor(this.getResources().getColor(R.color.color_comment_can_send));
		} else {
			mSendBtn.setTextColor(this.getResources().getColor(R.color.color_comment_not_send));
		}
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		final String txt = mEditText.getText().toString().trim();
		if (null != txt && txt.length() > 0) {
			switchSendState(true);
		} else {
			switchSendState(false);
		}

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		back();
	}

}
