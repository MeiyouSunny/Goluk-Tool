package com.mobnote.golukmain.msg;

import java.util.List;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.msg.bean.MessageBean;
import com.mobnote.golukmain.msg.bean.MessageMsgsBean;
import com.mobnote.golukmain.msg.bean.SystemMsgBenRequest;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.manager.MessageManager;
import com.mobnote.util.GolukUtils;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import cn.com.mobnote.module.page.IPageNotifyFn;

/**
 * 消息中心——评论／回复
 * 
 * @author xuhw
 *
 */
public class MsgCenterCommentActivity extends BaseActivity implements OnClickListener, OnRefreshListener,
		OnRTScrollListener, IRequestResultListener {

	/** 返回 **/
	private ImageButton mBtnBack = null;
	/** 标题 **/
	private TextView mTvTitle = null;
	private RTPullListView mRTPullListView = null;
	private RelativeLayout mRefreshLayout = null;
	private TextView mNoCommentText = null;
	private MsgCenterCommentAdapter mAdapter = null;
	/** 操作 (0:首次进入；1:下拉；2:上拉) */
	private String mCurrentOperator = "0";
	/** 是否还有分页 */
	private boolean mIsHaveData = true;
	/** 首次进入 */
	private static final String OPERATOR_FIRST = "0";
	/** 下拉 */
	private static final String OPERATOR_DOWN = "1";
	/** 上拉 */
	private static final String OPERATOR_UP = "2";
	/** 保存列表一个显示项索引 */
	private int mVisibleItem;
	/** 保存列表显示item个数 */
	private int mVisibleCount;
	/** 最近更新时间 */
	private String mHistoryDate = "";
	private MessageBean mMessageBean = null;

	private boolean mIsFirst = true;
	private final static String TYPES_COMMENT = "[101]";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msgcenter_comment);

		mHistoryDate = GolukUtils.getCurrentFormatTime(this);
		initView();

		mIsFirst = true;
		mRTPullListView.firstFreshState();
		httpData(OPERATOR_FIRST, "");

	}

	// 初始化view
	private void initView() {
		mBtnBack = (ImageButton) findViewById(R.id.imagebtn_comment_title_back);
		mTvTitle = (TextView) findViewById(R.id.tv_comment_title_text);
		mRTPullListView = (RTPullListView) findViewById(R.id.listview_comment);
		mRefreshLayout = (RelativeLayout) findViewById(R.id.ry_msgcenter_refresh);
		mNoCommentText = (TextView) findViewById(R.id.tv_msgcenter_nodata);
		mTvTitle.setText(this.getResources().getString(R.string.str_msgcenter_comment_title));

		mBtnBack.setOnClickListener(this);
		mRefreshLayout.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(this);
		mRTPullListView.setOnRTScrollListener(this);

		mAdapter = new MsgCenterCommentAdapter(this);
		mRTPullListView.setAdapter(mAdapter);
	}

	private void httpData(String operation, String timestamp) {
		SystemMsgBenRequest commentRequest = new SystemMsgBenRequest(IPageNotifyFn.PageType_MsgComment, this);
		commentRequest.get(GolukApplication.getInstance().mCurrentUId, TYPES_COMMENT, operation, timestamp);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.imagebtn_comment_title_back) {
			exit();
		} else if (id == R.id.ry_msgcenter_refresh) {
			mIsFirst = false;
			if (!mIsFirst) {
				showLoadingDialog();
				mRefreshLayout.setVisibility(View.GONE);
			}
			httpData(OPERATOR_FIRST, "");
		} else {
		}
	}

	/**
	 * 开始下拉刷新
	 */
	private void startPull() {
		mCurrentOperator = OPERATOR_DOWN;
		httpData(OPERATOR_FIRST, "");
	}

	/**
	 * 开始上拉加载
	 */
	private void startPush() {
		mCurrentOperator = OPERATOR_UP;
		httpData(OPERATOR_UP, mAdapter.getLastDataTime());
	}

	/**
	 * 首次进入／下拉刷新，数据回调处理
	 * 
	 * @param count
	 * @param mIncomeList
	 */
	private void pullCallBack(int count, List<MessageMsgsBean> beanList) {
		this.mAdapter.setData(beanList);
		mRTPullListView.onRefreshComplete(mHistoryDate);
		if (count >= 20) {
			mIsHaveData = true;
			this.removeFoot(2);
			addFoot(1);
		} else {
			mIsHaveData = false;
			this.removeFoot(1);
		}

	}

	/**
	 * 上拉加载，数据回调处理
	 * 
	 * @param count
	 * @param mIncomeList
	 */
	private void pushCallBack(int count, List<MessageMsgsBean> beanList) {
		if (count >= 20) {
			mIsHaveData = true;
		} else {
			mIsHaveData = false;
			this.removeFoot(1);
			this.addFoot(2);
		}
		this.mAdapter.appendData(beanList);
	}

	@Override
	public void onRefresh() {
		mIsFirst = false;
		startPull();
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mIsFirst = false;
			int count = mRTPullListView.getAdapter().getCount();
			int visibleCount = mVisibleItem + mVisibleCount;
			if (count == visibleCount && mIsHaveData) {
				startPush();
			}
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
		mVisibleItem = firstVisibleItem;
		mVisibleCount = visibleItemCount;
	}

	private void addFoot(int type) {
		if (mRTPullListView.getFooterViewsCount() > 0) {
			return;
		}
		if (1 == type) {
			mRTPullListView.addFooterView(1);
		} else {
			mRTPullListView.addFooterView(2);
		}
	}

	private void removeFoot(int type) {
		if (mRTPullListView != null) {
			if (1 == type) {
				mRTPullListView.removeFooterView(1);
			} else {
				mRTPullListView.removeFooterView(2);
			}
		}
	}

	private void exit() {
		mIsFirst = true;
		finish();
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if (requestType == IPageNotifyFn.PageType_MsgComment) {
			mMessageBean = (MessageBean) result;
			closeLoadingDialog();
			if(mMessageBean != null && mMessageBean.data != null){
				if ("10001".equals(mMessageBean.data.result) || "10002".equals(mMessageBean.data.result)){
					GolukUtils.startLoginActivity(MsgCenterCommentActivity.this);
					return;
				}
			}
			if (null != mMessageBean && mMessageBean.success && null != mMessageBean.data
					&& null != mMessageBean.data.messages) {
				MessageManager.getMessageManager().setCommentCount(0);
				mRTPullListView.setVisibility(View.VISIBLE);
				mRefreshLayout.setVisibility(View.GONE);
				closeLoadingDialog();
				List<MessageMsgsBean> commentList = mMessageBean.data.messages;
				if (null != commentList) {
					if (commentList.size() <= 0 && !mCurrentOperator.equals(OPERATOR_UP)) {
						mRTPullListView.setVisibility(View.GONE);
						mRefreshLayout.setVisibility(View.GONE);
						mNoCommentText.setVisibility(View.VISIBLE);
						mNoCommentText.setText(this.getResources().getString(R.string.str_msg_center_no_comment));
						return;
					}
					mNoCommentText.setVisibility(View.GONE);
					if (mCurrentOperator.equals(OPERATOR_FIRST) || mCurrentOperator.equals(OPERATOR_DOWN)) {
						pullCallBack(commentList.size(), commentList);
					} else {
						pushCallBack(commentList.size(), commentList);
					}
				}
			} else {
				unusual();
			}
		}
	}

	private void unusual() {
		if (mCurrentOperator == OPERATOR_UP) {
			this.removeFoot(1);
		} else {
			mRTPullListView.onRefreshComplete(GolukUtils.getCurrentCommentTime());
		}
		closeLoadingDialog();
		mRTPullListView.setVisibility(View.GONE);
		mNoCommentText.setVisibility(View.GONE);
		mRefreshLayout.setVisibility(View.VISIBLE);
		GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
	}

	CustomLoadingDialog loadingDialog = null;

	private void showLoadingDialog() {
		if (null == loadingDialog) {
			loadingDialog = new CustomLoadingDialog(this, null);
		}
		loadingDialog.show();
	}

	private void closeLoadingDialog() {
		if (null != loadingDialog) {
			loadingDialog.close();
			loadingDialog = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeLoadingDialog();
	}

}
