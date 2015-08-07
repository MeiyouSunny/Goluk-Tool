package cn.com.mobnote.golukmobile.comment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import cn.com.tiros.debug.GolukDebugUtils;

public class CommentActivity extends BaseActivity implements OnClickListener, OnRefreshListener, OnRTScrollListener,
		VideoSuqareManagerFn, ILiveDialogManagerFn {

	/** 如果为true, 則为测试数据，false則使用真实数据 (上线前要删除掉) */
	private final boolean isTest = true;

	public static final String TAG = "Comment";

	/** 视频、专题或直播的id */
	public static final String COMMENT_KEY_MID = "comment_key_mid";
	/** 评论主题类型 (1:单视频；2:专题；3:直播；4:其它) */
	public static final String COMMENT_KEY_TYPE = "comment_key_mid";
	/** 是否弹出键盘, true/false 弹出/不弹出 */
	public static final String COMMENT_KEY_SHOWSOFT = "comment_key_showsoft";
	/** 是否允许评论 */
	public static final String COMMENT_KEY_ISCAN_INPUT = "comment_key_iscan_input";

	/** 一页请求多少条数据 */
	private static final int PAGE_SIZE = 20;
	/** 首次进入 */
	private static final int OPERATOR_FIRST = 0;
	/** 上拉 */
	private static final int OPERATOR_PUSH = 1;
	/** 下拉 */
	private static final int OPERATOR_PULL = 2;

	/** application */
	public GolukApplication mApp = null;
	private boolean isExit = true;

	private ImageButton mBackBtn = null;
	private TextView mSendBtn = null;
	private EditText mEditText = null;
	private ImageView mNoData = null;
	private RelativeLayout loading = null;

	private CommentListViewAdapter mAdapter = null;
	private RTPullListView mRTPullListView = null;

	/** 视频、专题或直播的id */
	private String mId = null;
	/** 评论主题类型 (1:单视频；2:专题；3:直播；4:其它) */
	private String mTopicType = null;
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

	private String historyDate = "2015-8-6 18:10";

	private VideoSquareManager mVideoSquareManager = null;
	private RelativeLayout mCommentInputLayout = null;
	private TextView mNoInputTv = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mApp = (GolukApplication) getApplication();
		getWindow().setContentView(R.layout.comment);
		getIntentData();
		initView();
		isExit = false;
		initListener();
		firstDeal();

		CommentTimerManager.getInstance();
	}

	private void firstDeal() {
		LiveDialogManager.getManagerInstance().setDialogManageFn(this);

		if (isCanInput) {
			if (mIsShowSoft) {
				mEditText.requestFocus();
				GolukUtils.showSoft(mEditText);
			}
			mCommentInputLayout.setVisibility(View.VISIBLE);
			mNoInputTv.setVisibility(View.GONE);

			mRTPullListView.firstFreshState();
			firstEnter();
		} else {
			mCommentInputLayout.setVisibility(View.GONE);
			mNoInputTv.setVisibility(View.VISIBLE);
		}
	}

	private void initListener() {
		mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if (null != mVideoSquareManager) {
			if (mVideoSquareManager.checkVideoSquareManagerListener("videosharehotlist")) {
				mVideoSquareManager.removeVideoSquareManagerListener("videosharehotlist");
			}
			mVideoSquareManager.addVideoSquareManagerListener(TAG, this);
		}
	}

	private void getIntentData() {
		if (isTest) {
			mIsShowSoft = false;
			mId = "04DB0612A41EBB909C33DC5901307461";
			mTopicType = "1";
			return;
		}

		Intent intent = getIntent();
		if (null != intent) {
			mId = intent.getStringExtra(COMMENT_KEY_MID);
			mTopicType = intent.getStringExtra(COMMENT_KEY_TYPE);
			mIsShowSoft = intent.getBooleanExtra(COMMENT_KEY_SHOWSOFT, true);
			isCanInput = intent.getBooleanExtra(COMMENT_KEY_ISCAN_INPUT, true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setContext(this, TAG);
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

		mAdapter = new CommentListViewAdapter(this);
		mRTPullListView.setAdapter(mAdapter);
		if (isCanInput) {
			mRTPullListView.setonRefreshListener(this);
			mRTPullListView.setOnRTScrollListener(this);
		}

		loading = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading, null);
	}

	private ArrayList<CommentBean> getTestData() {
		ArrayList<CommentBean> data = new ArrayList<CommentBean>();

		for (int i = 0; i < 10; i++) {
			CommentBean bean = new CommentBean();
			bean.mUserHead = "2";
			bean.mUserName = "name: " + i;
			bean.mCommentTxt = "This is comment :" + i;
			bean.mCommentTime = "today 13:19";

			data.add(bean);
		}

		return data;
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

		final String content = mEditText.getText().toString();
		if (null == content || "".equals(content)) {
			GolukUtils.showToast(this, "请输入评论内容");
			return;
		}
		httpPost_requestAdd(content);
	}

	// 首次进入，数据回调处理
	private void firstEnterCallBack(int count, ArrayList<CommentBean> dataList) {
		// 首次进入
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
		}
		this.mAdapter.setData(dataList);

		mRTPullListView.onRefreshComplete(historyDate);
		this.removeFoot();
	}

	// 下拉刷新，数据回调处理
	private void pullCallBack(int count, ArrayList<CommentBean> dataList) {
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
		}
		this.mAdapter.appendData(dataList);

		mRTPullListView.onRefreshComplete(historyDate);
		this.removeFoot();
	}

	// 上拉刷新，数据回调处理
	private void pushCallBack(int count, ArrayList<CommentBean> dataList) {
		if (count >= PAGE_SIZE) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
		}
		removeFoot();
		this.mAdapter.appendData(dataList);
	}

	// 是否显示无数据提示
	private void noData(boolean isno) {
		if (isno) {
			mNoData.setVisibility(View.VISIBLE);
		} else {
			mNoData.setVisibility(View.GONE);
		}
	}

	private void callBack_commentList(int msg, int param1, Object param2) {
		GolukDebugUtils.e("", "jyf----CommentActivity----msg:" + msg + "  param1:" + param1 + "  param2:" + param2);
		if (1 == msg) {
			try {
				JSONObject rootObj = new JSONObject((String) param2);
				boolean isSucess = rootObj.getBoolean("success");
				JSONObject dataObj = rootObj.getJSONObject("data");
				String result = dataObj.getString("result");
				int count = Integer.parseInt(dataObj.getString("count"));
				if (count > 0) {
					// 有数据
					ArrayList<CommentBean> dataList = JsonUtil.parseCommentData(dataObj.getJSONArray("comments"));
					if (null != dataList && dataList.size() > 0) {

						noData(false);

						if (OPERATOR_FIRST == mCurrentOperator) {
							// 首次进入
							firstEnterCallBack(count, dataList);
						} else if (OPERATOR_PULL == mCurrentOperator) {
							// 上拉刷新
							pushCallBack(count, dataList);
						} else if (OPERATOR_PUSH == mCurrentOperator) {
							// 下拉刷新
							pullCallBack(count, dataList);
						}

					} else {
						// 无数据
						noDataDeal();
					}
				} else {
					// 无数据
					noDataDeal();
				}

			} catch (Exception e) {

			}
		} else {

		}
	}

	private void removeFoot() {
		if (loading != null) {
			if (mRTPullListView != null) {
				mRTPullListView.removeFooterView(loading);
			}
		}
	}

	private void noDataDeal() {
		if (mCurrentOperator == OPERATOR_PUSH) {// 上拉刷新
			removeFoot();
		} else if (mCurrentOperator == OPERATOR_FIRST) {// 下拉刷新
			mRTPullListView.onRefreshComplete(historyDate);
		} else if (OPERATOR_FIRST == mCurrentOperator) {
			if (this.mAdapter.getCount() <= 0) {
				noData(true);
			}
		}
	}

	private void callBack_commentAdd(int msg, int param1, Object param2) {
		LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
		if (1 == msg) {
			try {
				JSONObject obj = new JSONObject((String) param2);
				boolean isSucess = obj.getBoolean("success");
				if (isSucess) {
					CommentBean bean = JsonUtil.parseAddCommentData(obj.getJSONObject("data"));
					if (null != bean) {
						noData(false);
						bean.mCommentTime = GolukUtils.getCurrentFormatTime();
						this.mAdapter.addFirstData(bean);
						CommentTimerManager.getInstance().start(10);
					} else {
						GolukUtils.showToast(this, "评论失败");
					}
				} else {
					// 失败
					GolukUtils.showToast(this, "评论失败");
				}
			} catch (Exception e) {
				GolukUtils.showToast(this, "评论失败");
			}
		} else {
			GolukUtils.showToast(this, "评论失败");
			// 失败
		}
	}

	private void callBack_commentDel(int msg, int param1, Object param2) {

	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (isExit) {
			return;
		}
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
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
				if (mIsHaveData) {
					startPush();
				}
			}
		}
	}

	// 首次进入
	private void firstEnter() {
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// 开始下拉刷新
	private void startPull() {
		mCurrentOperator = OPERATOR_FIRST;
		httpPost_requestList(OPERATOR_FIRST, "");
	}

	// 开始上拉刷新
	private void startPush() {

		GolukDebugUtils.e("", "jyf----CommentActivity-----startPush-----1111" + this.mIsHaveData);

		mCurrentOperator = OPERATOR_PUSH;
		httpPost_requestList(OPERATOR_PULL, mAdapter.getLastDataTime());

		mRTPullListView.addFooterView(loading);
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
		final String requestStr = JsonUtil.getAddCommentJson(mId, mTopicType, txt);
		boolean isSucess = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
				VideoSuqareManagerFn.VSquare_Req_Add_Comment, requestStr);
		if (!isSucess) {
			// TODO 失败
			GolukUtils.showToast(this, "评论失败!");
			return;
		}
		LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
				LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", "正在提交评论", true);
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
		}

	}

}
