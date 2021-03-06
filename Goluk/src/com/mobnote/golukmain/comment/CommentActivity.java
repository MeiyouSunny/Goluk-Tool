package com.mobnote.golukmain.comment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.comment.bean.CommentAddBean;
import com.mobnote.golukmain.comment.bean.CommentAddResultBean;
import com.mobnote.golukmain.comment.bean.CommentDataBean;
import com.mobnote.golukmain.comment.bean.CommentDelResultBean;
import com.mobnote.golukmain.comment.bean.CommentItemBean;
import com.mobnote.golukmain.comment.bean.CommentResultBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.videodetail.ReplyDialog;
import com.mobnote.golukmain.videodetail.SoftKeyBoardListener;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class CommentActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnRTScrollListener,
		ILiveDialogManagerFn, ICommentFn, TextWatcher, OnItemClickListener, IRequestResultListener,
		OnLayoutChangeListener, EmojiconGridFragment.OnEmojiconClickedListener,
		EmojiconsFragment.OnEmojiconBackspaceClickedListener {

	public static final String TAG = "Comment";

	/** application */
	public GolukApplication mApp = null;
	/** ?????????????????????,????????????????????????????????????????????? */
	private boolean isExit = true;
	/** ????????? */
	private ImageButton mBackBtn = null;
	/** ?????????????????? */
	private TextView mSendBtn = null;
	/** ??????????????? */
	private EditText mEditInput = null;
	/** ?????????????????????????????? */
	private TextView mNoData = null;
	/** ?????????????????????ListView????????????????????? */
	private RelativeLayout loading = null;
	/** ???????????????????????? */
	private TextView mNoInputTv = null;
	/** ????????????????????????????????? */
	private LinearLayout mCommentInputLayout = null;
	/** ????????????????????? */
	private CommentListViewAdapter mAdapter = null;
	/** ???????????????ListView */
	private RTPullListView mRTPullListView = null;

	/** ???????????????????????????id */
	private String mId = null;
	/** ?????????????????? (1:????????????2:?????????3:?????????4:??????) */
	private String mTopicType = null;
	/** ??????????????????uid, ?????????????????????????????????????????????????????? */
	private String mVideoUserId = null;
	/** ?????? (0:???????????????1:?????????2:??????) */
	private int mCurrentOperator = 0;
	/** ?????????????????? */
	private boolean mIsHaveData = true;
	/** ????????????????????? */
	private boolean mIsShowSoft = true;
	/** ?????????????????? */
	private boolean isCanInput = true;

	/** ????????????????????????????????? */
	private int wonderfulFirstVisible;
	/** ??????????????????item?????? */
	private int wonderfulVisibleCount;
	/** ?????????????????? */
	private String historyDate = "";

	/** ??????????????????????????? */
	private CommentBean mWillDelBean = null;
	/** false?????????false?????????true?????? **/
	private boolean mIsReply = false;
	/** ???????????????dialog **/
	private ReplyDialog mReplyDialog = null;

	private long mCommentTime = 0;

	private View mRootLayout = null;
	private int screenHeight = 0;
	private int keyHeight = 0;
	private ImageView mEmojiImg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();

		mRootLayout = LayoutInflater.from(this).inflate(R.layout.comment_layout, null);
		getWindow().setContentView(mRootLayout);
		getIntentData();
		historyDate = GolukUtils.getCurrentFormatTime(this);
		initView();
		isExit = false;
		firstDeal();

		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		keyHeight = screenHeight / 3;
		init();
		observeSoftKeyboard();
	}

	/**
	 * ???????????????????????????
	 * 
	 * @author jyf
	 * @date 2015???8???12???
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
		mEditInput = (EditText) findViewById(R.id.comment_input);
		mRTPullListView = (RTPullListView) findViewById(R.id.commentRTPullListView);
		mNoData = (TextView) findViewById(R.id.comment_nodata);
		mCommentInputLayout = (LinearLayout) findViewById(R.id.comment_layout);
		mNoInputTv = (TextView) findViewById(R.id.comment_noinput);
		mEmojiImg = (ImageView) findViewById(R.id.emojicon);
		emoLayout = (FrameLayout) findViewById(R.id.emojiconsLayout);

		mBackBtn.setOnClickListener(this);
		mSendBtn.setOnClickListener(this);
		mEditInput.addTextChangedListener(this);
		mEmojiImg.setOnClickListener(this);

		mAdapter = new CommentListViewAdapter(this);
		mAdapter.setVideoUserId(mVideoUserId);
		mRTPullListView.setAdapter(mAdapter);
		if (isCanInput) {
			mRTPullListView.setonRefreshListener(this);
			mRTPullListView.setOnRTScrollListener(this);
		}
		mRTPullListView.setOnItemClickListener(this);

		setOnTouchListener();
	}

	public void observeSoftKeyboard() {
		SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
			@Override
			public void keyBoardShow(int height) {
				GolukUtils.setKeyBoardHeight(height);
			}

			@Override
			public void keyBoardHide(int height) {
			}
		});
	}

	/**
	 * ??????????????????????????????????????????
	 * 
	 * @author jyf
	 * @date 2015???8???12???
	 */
	private void firstDeal() {
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		if (isCanInput) {
			mRTPullListView.setEnabled(true);
			if (mIsShowSoft) {
				mEditInput.requestFocus();
				GolukUtils.showSoft(mEditInput);
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
		mRootLayout.addOnLayoutChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.comment_back) {
			exit();
		} else if (id == R.id.comment_send) {
			click_send();
		} else if (id == R.id.emojicon) {
			click_switchInput();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (emoLayout.getVisibility() != View.GONE) {
			this.hideEmojocon();
			setSwitchState(true);
		}
	}

	private void back() {
		isExit = true;
		mIsReply = false;
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (null != mReplyDialog) {
			mReplyDialog.dismiss();
		}
	}

	private void exit() {
		removeAllMessage();
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (View.GONE != emoLayout.getVisibility()) {
				this.hideEmojocon();
				setSwitchState(true);
				return true;
			}
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void click_send() {
		// ?????????????????????????????????????????????
		if (!mApp.isUserLoginSucess) {
			Intent intent = null;
			if (GolukApplication.getInstance().isInteral() == false) {
				intent = new Intent(this, InternationUserLoginActivity.class);
			} else {
				intent = new Intent(this, UserLoginActivity.class);
			}
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
		UserUtils.hideSoftMethod(this);
		this.hideEmojocon();
		setSwitchState(true);
		httpPost_requestAdd(content);
	}

	// ?????????????????????????????????
	private void firstEnterCallBack(int count, ArrayList<CommentBean> dataList) {
		// ????????????
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

	// ?????????????????????????????????
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
		historyDate = GolukUtils.getCurrentFormatTime(this);
	}

	private String getLastRefreshTime() {
		return historyDate;
	}

	// ?????????????????????????????????
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
			// ????????????
			removeFoot();
		}

		GolukUtils.showToast(this, this.getString(R.string.str_network_unavailable));
		noData(mAdapter.getCount() <= 0);
	}

	private void noDataDeal() {
		mIsHaveData = false;
		if (mCurrentOperator == OPERATOR_UP) {// ????????????
			removeFoot();
		} else if (mCurrentOperator == OPERATOR_FIRST || OPERATOR_DOWN == mCurrentOperator) {// ????????????
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
		}

		noData(mAdapter.getCount() <= 0);
	}

	// ???????????????????????????
	private void noData(boolean isno) {
		if (isno) {
			mRTPullListView.setVisibility(View.GONE);
			mNoData.setVisibility(View.VISIBLE);
		} else {
			mRTPullListView.setVisibility(View.VISIBLE);
			mNoData.setVisibility(View.GONE);
		}
	}

	/**
	 * ?????????????????????loading View
	 * 
	 * @author jyf
	 * @date 2015???8???12???
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

	@Override
	public void onRefresh() {
		// ????????????
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
			if ((count == visibleCount) && (count > 20) && !mIsHaveData) {
				GolukUtils.showToast(this,
						this.getResources().getString(R.string.str_pull_refresh_listview_bottom_reach));
			}
		} else if (OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState) {
			if (!mInputState) {
				this.hideEmojocon();
				this.setSwitchState(true);
			}
		}
	}

	// ????????????
	private void firstEnter() {
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// ??????????????????
	private void startPull() {
		if (mCurrentOperator == OPERATOR_UP) {
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
			return;
		}
		mCurrentOperator = OPERATOR_DOWN;
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// ??????????????????
	private void startPush() {
		GolukDebugUtils.e("", "jyf----CommentActivity-----startPush-----1111" + this.mIsHaveData);
		if (mCurrentOperator != OPERATOR_NONE) {
			return;
		}
		mCurrentOperator = OPERATOR_UP;
		httpPost_requestList(OPERATOR_DOWN, mAdapter.getLastDataTime());
	}

	private void httpPost_requestList(int operation, String timestamp) {
		final String requestStr = JsonUtil.getCommentRequestStr(mId, mTopicType, operation, timestamp, PAGE_SIZE);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_List_Comment, requestStr);
		if (!isSucess) {
			// TODO ??????
		}

		CommentListRequest request = new CommentListRequest(IPageNotifyFn.PageType_CommentList, this);
		request.get(mId, mTopicType, operation, timestamp);
	}

	// ????????????
	private void httpPost_requestAdd(String txt) {

		CommentAddRequest request = new CommentAddRequest(IPageNotifyFn.PageType_AddComment, this);
		boolean isSucess = false;
		if (mIsReply) {
			isSucess = request.get(mId, mTopicType, txt, mWillDelBean.mUserId, mWillDelBean.mUserName, null);
		} else {
			isSucess = request.get(mId, mTopicType, txt, "", "", null);
		}
		if (!isSucess) {
			// ??????
			GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", this.getString(R.string.str_comment_ongoing), true);
	}

	// ????????????
	public void httpPost_requestDel(String id) {

		CommentDeleteRequest request = new CommentDeleteRequest(IPageNotifyFn.PageType_DelComment, this);
		boolean isSucess = request.get(id);
		if (!isSucess) {
			// ??????
			GolukUtils.showToast(this, this.getString(R.string.str_delete_fail));
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", this.getString(R.string.str_delete_ongoing),
				true);
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
			// ????????????
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		GolukDebugUtils.e("", "----commentActivity--------position:" + position + "   arg3:" + arg3);
		try {
			if (null != mAdapter) {
				mWillDelBean = (CommentBean) mAdapter.getItem(position - 1);
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
					if (!mInputState) {
						this.hideEmojocon();
						this.setSwitchState(true);
					}
				}
			}
		} catch (Exception e) {

		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// ???????????????????????????View
			View v = mCommentInputLayout;
			if (UserUtils.isShouldHideInput(v, ev)) {
				UserUtils.hideSoftMethod(this);
				if ("".equals(mEditInput.getText().toString().trim()) && mIsReply) {
					mEditInput.setHint(this.getString(R.string.str_comment_input_hit));
					mIsReply = false;
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void afterTextChanged(Editable arg0) {

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

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
		final String txt = mEditInput.getText().toString().trim();
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

	@Override
	public void onLoadComplete(int requestType, Object result) {
		switch (requestType) {
		case IPageNotifyFn.PageType_CommentList:

			CommentResultBean resultBean = (CommentResultBean) result;
			if (resultBean != null && resultBean.success && resultBean.data != null) {
				// ?????????
				CommentDataBean dataBean = resultBean.data;
				int count = 0;
				if (!TextUtils.isEmpty(dataBean.count) && TextUtils.isDigitsOnly(dataBean.count)) {
					count = Integer.parseInt(dataBean.count);
				}

				if (null == dataBean.comments || dataBean.comments.size() <= 0) {
					// ?????????
					noDataDeal();
					mCurrentOperator = OPERATOR_NONE;
					return;
				}

				// ?????????
				ArrayList<CommentBean> dataList = new ArrayList<CommentBean>();
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
					dataList.add(bean);
				}
				updateRefreshTime();
				noData(false);

				if (OPERATOR_FIRST == mCurrentOperator) {
					// ????????????
					firstEnterCallBack(count, dataList);
				} else if (OPERATOR_DOWN == mCurrentOperator) {
					// ????????????
					pullCallBack(count, dataList);
				} else if (OPERATOR_UP == mCurrentOperator) {
					// ????????????
					pushCallBack(count, dataList);
				}
			} else {
				callBackFailed();
			}
			mCurrentOperator = OPERATOR_NONE;
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
					if ("0".equals(bean.result)) {// ??????
						this.mAdapter.addFirstData(bean);
						noData(false);
						mEditInput.setText("");
						switchSendState(false);
						mIsReply = false;
						mEditInput.setHint(this.getString(R.string.str_comment_input_hit));
						mCommentTime = System.currentTimeMillis();
					} else if ("1".equals(bean.result)) {
						GolukDebugUtils.e("", this.getString(R.string.str_parameter_error));
					} else if ("2".equals(bean.result)) {// ????????????
						LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
								LiveDialogManager.FUNCTION_DIALOG_OK, "",
								this.getResources().getString(R.string.comment_repeat_text));
					} else if ("3".equals(bean.result)) {// ????????????
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
		case IPageNotifyFn.PageType_DelComment:
			LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
			CommentDelResultBean DelResultBean = (CommentDelResultBean) result;
			if (null != DelResultBean && DelResultBean.success) {

				mAdapter.deleteData(mWillDelBean);
				GolukUtils.showToast(this, this.getString(R.string.str_delete_success));
				noData(mAdapter.getCount() <= 0);
			} else {
				GolukUtils.showToast(this, this.getString(R.string.str_delete_fail));
			}
			mWillDelBean = null;
			break;
		default:
			Log.e("CommentActivity", "onLoadComplete ,requestType = " + requestType);
			break;
		}
	}

	private void setOnTouchListener() {
		mEditInput.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
					click_soft();
				}
				return false;
			}
		});
	}

	private boolean isCanShowSoft() {
		if (isSwitchStateFinish) {
			return true;
		}
		return false;
	}

	@Override
	protected void hMessage(Message msg) {
		if (100 == msg.what) {
			showEmojocon();
			setSwitchState(false);
			isSwitchStateFinish = true;
		} else if (200 == msg.what) {
			setSwitchState(true);
			GolukUtils.showSoftNotThread(mEditInput);
			mBaseHandler.sendEmptyMessageDelayed(300, 800);
		} else if (300 == msg.what) {
			hideEmojocon();
			this.setResize();
			isSwitchStateFinish = true;
		}
	}

	/** ???????????????????????????????????? */
	private boolean isSwitchStateFinish = true;

	public void click_soft() {
		if (!this.isCanShowSoft()) {
			return;
		}
		isSwitchStateFinish = false;
		mEditInput.setFocusable(true);
		mEditInput.requestFocus();
		if (!GolukUtils.isSettingBoardHeight()) {
			this.hideEmojocon();
		}
		if (emoLayout.getVisibility() == View.GONE) {
			this.setResize();
		} else {
			setInputAdJust();
		}
		mBaseHandler.sendEmptyMessageDelayed(200, 80);
	}

	// ??????????????? ?????????
	private void click_Emojocon() {
		if (!isCanShowSoft()) {
			return;
		}
		this.isSwitchStateFinish = false;
		GolukUtils.hideSoft(this, mEditInput);
		mBaseHandler.sendEmptyMessageDelayed(100, 80);
	}

	/** ???????????? */
	private FrameLayout emoLayout = null;
	/** true:??????????????????, false: ?????????????????? */
	private boolean mInputState = true;

	private void setInputAdJust() {
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	private void setResize() {
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	private void setLayoutHeight() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) emoLayout.getLayoutParams();
		lp.height = GolukUtils.getKeyBoardHeight();
		emoLayout.setLayoutParams(lp);
	}

	private void showEmojocon() {
		setLayoutHeight();
		emoLayout.setVisibility(View.VISIBLE);
	}

	private void hideEmojocon() {
		emoLayout.setVisibility(View.GONE);
	}

	private void init() {
		EmojiconsFragment fg = EmojiconsFragment.newInstance(false);
		getSupportFragmentManager().beginTransaction().replace(R.id.emojiconsLayout, fg).commit();
	}

	private void click_switchInput() {
		if (!this.isCanShowSoft()) {
			return;
		}
		if (mInputState) {
			click_Emojocon();
		} else {
			click_soft();
		}
	}

	private void setSwitchState(boolean isTextInput) {
		mInputState = isTextInput;
		if (isTextInput) {
			// ????????????
			mEmojiImg.setImageDrawable(this.getResources().getDrawable(R.drawable.input_state_emojo));
		} else {
			// ????????????
			mEmojiImg.setImageDrawable(this.getResources().getDrawable(R.drawable.input_state_txt));
		}
	}

	@Override
	public void onEmojiconBackspaceClicked(View v) {
		EmojiconsFragment.backspace(mEditInput);
	}

	@Override
	public void onEmojiconClicked(Emojicon emojicon) {
		EmojiconsFragment.input(mEditInput, emojicon);
	}

	private void removeAllMessage() {
		if (null != mBaseHandler) {
			mBaseHandler.removeMessages(100);
			mBaseHandler.removeMessages(200);
			mBaseHandler.removeMessages(300);
		}
	}

	public void reply() {
		if (!mInputState) {
			this.hideEmojocon();
			this.setSwitchState(true);
		}
	}

	@Override
	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
			int oldBottom) {
		if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
			setSwitchState(true);
		} else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
			if (this.emoLayout.getVisibility() == View.GONE) {
				setSwitchState(true);
			} else {
				setSwitchState(false);
			}
		}
	}

}
