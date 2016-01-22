package cn.com.mobnote.golukmobile.msg;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.msg.bean.MessageBean;
import cn.com.mobnote.golukmobile.msg.bean.MessageMsgsBean;
import cn.com.mobnote.golukmobile.msg.bean.SystemMsgBenRequest;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.manager.MessageManager;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

public class MsgCenterPraiseActivity extends BaseActivity implements OnClickListener, IRequestResultListener,
		OnRefreshListener, OnRTScrollListener {

	/** 返回 **/
	private ImageButton mBtnBack = null;
	/** 标题 **/
	private TextView mTvTitle = null;
	private RTPullListView mRTPullListView = null;
	private RelativeLayout mRefreshLayout = null;
	private TextView nNoPraiseText = null;
	private MsgCenterPraiseAdapter mAdapter = null;
	/** 首次进入 */
	private static final String OPERATOR_FIRST = "0";
	/** 下拉 */
	private static final String OPERATOR_DOWN = "1";
	/** 上拉 */
	private static final String OPERATOR_UP = "2";
	/** 是否还有分页 */
	private boolean mIsHaveData = true;
	private MessageBean mMessageBean = null;
	/** 最近更新时间 */
	private String mHistoryDate = "";
	/** 操作 (0:首次进入；1:下拉；2:上拉) */
	private String mCurrentOperator = "0";
	/** 保存列表一个显示项索引 */
	private int mVisibleItem;
	/** 保存列表显示item个数 */
	private int mVisibleCount;

	private boolean mIsFirst = true;
	private static final String TYPES_PRAISE = "[102]";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msgcenter_comment);

		mHistoryDate = GolukUtils.getCurrentFormatTime();
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
		nNoPraiseText = (TextView) findViewById(R.id.tv_msgcenter_nodata);
		mTvTitle.setText(this.getResources().getString(R.string.str_usercenter_praise));

		mBtnBack.setOnClickListener(this);
		mRefreshLayout.setOnClickListener(this);
		mRTPullListView.setonRefreshListener(this);
		mRTPullListView.setOnRTScrollListener(this);

		mAdapter = new MsgCenterPraiseAdapter(this);
		mRTPullListView.setAdapter(mAdapter);

	}

	private void httpData(String operation, String timestamp) {
		SystemMsgBenRequest praiseRequest = new SystemMsgBenRequest(IPageNotifyFn.PageType_MsgPraise, this);
		praiseRequest.get(GolukApplication.getInstance().mCurrentUId, TYPES_PRAISE, operation, timestamp);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.imagebtn_comment_title_back:
			exit();
			break;
		case R.id.ry_msgcenter_refresh:
			mIsFirst = false;
			if (!mIsFirst) {
				showLoadingDialog();
				mRefreshLayout.setVisibility(View.GONE);
			}
			httpData(OPERATOR_FIRST, "");
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		if (requestType == IPageNotifyFn.PageType_MsgPraise) {
			mMessageBean = (MessageBean) result;
			if (null != mMessageBean && mMessageBean.success && null != mMessageBean.data
					&& null != mMessageBean.data.messages) {
				MessageManager.getMessageManager().setPraiseCount(0);
				mRTPullListView.setVisibility(View.VISIBLE);
				mRefreshLayout.setVisibility(View.GONE);
				closeLoadingDialog();
				List<MessageMsgsBean> praiseList = mMessageBean.data.messages;
				if (null != praiseList) {
					if (praiseList.size() <= 0 && !mCurrentOperator.equals(OPERATOR_UP)) {
						mRTPullListView.setVisibility(View.GONE);
						mRefreshLayout.setVisibility(View.GONE);
						nNoPraiseText.setVisibility(View.VISIBLE);
						nNoPraiseText.setText(this.getResources().getString(R.string.str_msg_center_no_praise));
						return;
					}
					nNoPraiseText.setVisibility(View.GONE);
					if (mCurrentOperator == OPERATOR_FIRST || mCurrentOperator == OPERATOR_DOWN) {
						pullCallBack(praiseList.size(), praiseList);
					} else {
						pushCallBack(praiseList.size(), praiseList);
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
			mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime());
		}
		if (!mIsFirst) {
			closeLoadingDialog();
		} else {
			mRTPullListView.setVisibility(View.GONE);
			nNoPraiseText.setVisibility(View.GONE);
			mRefreshLayout.setVisibility(View.VISIBLE);
		}
		GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
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
		mCurrentOperator = OPERATOR_DOWN;
		httpData(OPERATOR_FIRST, "");
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			mIsFirst = false;
			int count = mRTPullListView.getAdapter().getCount();
			int visibleCount = mVisibleItem + mVisibleCount;
			if (count == visibleCount && mIsHaveData) {
				mCurrentOperator = OPERATOR_UP;
				httpData(OPERATOR_UP, mAdapter.getLastDataTime());
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
