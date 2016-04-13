package com.mobnote.golukmain.comment;

import java.util.ArrayList;

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
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.videodetail.ReplyDialog;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class CommentActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnRTScrollListener, ILiveDialogManagerFn, ICommentFn, TextWatcher, OnItemClickListener,
		IRequestResultListener {

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
	private TextView mNoData = null;
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

	/** 保存将要删除的数据 */
	private CommentBean mWillDelBean = null;
	/** false评论／false删除／true回复 **/
	private boolean mIsReply = false;
	/** 回复评论的dialog **/
	private ReplyDialog mReplyDialog = null;

	private long mCommentTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();
		getWindow().setContentView(R.layout.comment_layout);
		getIntentData();
		historyDate = GolukUtils.getCurrentFormatTime(this);
		initView();
		isExit = false;
		firstDeal();
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
		mNoData = (TextView) findViewById(R.id.comment_nodata);
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
		int id = v.getId();
		if (id == R.id.comment_back) {
			finish();
		} else if (id == R.id.comment_send) {
			click_send();
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void click_send() {
		// 发评论前需要先判断用户是否登录
		if (!mApp.isUserLoginSucess) {
			Intent intent =  null;
			if(GolukApplication.getInstance().isInternation){
				intent = new Intent(this, InternationUserLoginActivity.class);
			}else{
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

		final String content = mEditText.getText().toString().trim();
		if (null == content || "".equals(content)) {
			GolukUtils.showToast(this, this.getString(R.string.str_input_comment_content));
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
		historyDate = GolukUtils.getCurrentFormatTime(this);
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
			if ((count == visibleCount) && (count > 20) && !mIsHaveData) {
				GolukUtils.showToast(this,
						this.getResources().getString(R.string.str_pull_refresh_listview_bottom_reach));
			}
		}
	}

	// 首次进入
	private void firstEnter() {
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// 开始下拉刷新
	private void startPull() {
		if (mCurrentOperator == OPERATOR_UP) {
			mRTPullListView.onRefreshComplete(getLastRefreshTime());
			return;
		}
		mCurrentOperator = OPERATOR_DOWN;
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// 开始上拉刷新
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
			// TODO 失败
		}

		CommentListRequest request = new CommentListRequest(IPageNotifyFn.PageType_CommentList, this);
		request.get(mId, mTopicType, operation, timestamp);
	}

	// 添加评论
	private void httpPost_requestAdd(String txt) {

		CommentAddRequest request = new CommentAddRequest(IPageNotifyFn.PageType_AddComment, this);
		boolean isSucess = false;
		if (mIsReply) {
			isSucess = request.get(mId, mTopicType, txt, mWillDelBean.mUserId, mWillDelBean.mUserName, null);
		} else {
			isSucess = request.get(mId, mTopicType, txt, "", "", null);
		}
		if (!isSucess) {
			// 失败
			GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", this.getString(R.string.str_comment_ongoing), true);
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
					mReplyDialog = new ReplyDialog(this, mWillDelBean, mEditText, mIsReply);
					mReplyDialog.show();
				}
			}
		} catch (Exception e) {

		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			// 获得当前得到焦点的View
			View v = mCommentInputLayout;
			if (UserUtils.isShouldHideInput(v, ev)) {
				UserUtils.hideSoftMethod(this);
				if ("".equals(mEditText.getText().toString().trim()) && mIsReply) {
					mEditText.setHint(this.getString(R.string.str_comment_input_hit));
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

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		switch (requestType) {
		case IPageNotifyFn.PageType_CommentList:

			CommentResultBean resultBean = (CommentResultBean) result;
			if (resultBean != null && resultBean.success && resultBean.data != null) {
				// 有数据
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
					// 首次进入
					firstEnterCallBack(count, dataList);
				} else if (OPERATOR_DOWN == mCurrentOperator) {
					// 下拉刷新
					pullCallBack(count, dataList);
				} else if (OPERATOR_UP == mCurrentOperator) {
					// 上拉刷新
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
					if ("0".equals(bean.result)) {// 成功
						this.mAdapter.addFirstData(bean);
						noData(false);
						mEditText.setText("");
						switchSendState(false);
						mIsReply = false;
						mEditText.setHint(this.getString(R.string.str_comment_input_hit));
						mCommentTime = System.currentTimeMillis();
					} else if ("1".equals(bean.result)) {
						GolukDebugUtils.e("", this.getString(R.string.str_parameter_error));
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

}
