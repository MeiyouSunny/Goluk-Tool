package com.mobnote.golukmain.praised;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventPraiseStatusChanged;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.praised.bean.MyPraisedListBean;
import com.mobnote.golukmain.praised.bean.MyPraisedVideoBean;
import com.mobnote.util.GolukUtils;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.page.IPageNotifyFn;
import de.greenrobot.event.EventBus;

public class MyPraisedActivity extends BaseActivity implements IRequestResultListener {
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";

	private PullToRefreshListView mListView;
	private MyPraisedListAdapter mAdapter;
	private List<MyPraisedVideoBean> mPraisedList;
	private ImageView mBackButton;
	private RelativeLayout mEmptyRL;
	private RelativeLayout mNodataRL;
	private final static String PAGESIZE = "10";
	private String mTimeStamp = "";
	private String mCurMotion = REFRESH_NORMAL;
	private TextView mRetryClickIV;
	private CustomLoadingDialog mLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_praised_list);
		mBackButton = (ImageView)findViewById(R.id.iv_praised_list_back_btn);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyPraisedActivity.this.finish();
			}
		});

		mEmptyRL = (RelativeLayout)findViewById(R.id.rl_praised_list_exception_refresh);
		mNodataRL = (RelativeLayout)findViewById(R.id.rl_praised_list_no_data_refresh);
		mRetryClickIV = (TextView)findViewById(R.id.tv_praised_list_exception_refresh);
		mListView = (PullToRefreshListView)findViewById(R.id.plv_praised_list);
		mRetryClickIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendPraisedListRequest(REFRESH_NORMAL, null);
			}
		});
		mAdapter = new MyPraisedListAdapter(this);
		mListView.setAdapter(mAdapter);
		mLoadingDialog = new CustomLoadingDialog(this, null);
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						MyPraisedActivity.this.getResources().getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(MyPraisedActivity.this));
				sendPraisedListRequest(REFRESH_PULL_DOWN, null);
				mCurMotion = REFRESH_PULL_DOWN;
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						MyPraisedActivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendPraisedListRequest(REFRESH_PULL_UP, mTimeStamp);
				mCurMotion = REFRESH_PULL_UP;
			}
		});

		sendPraisedListRequest(REFRESH_NORMAL, null);
		mPraisedList = new ArrayList<MyPraisedVideoBean>();
		EventBus.getDefault().register(this);
	}

	private void sendPraisedListRequest(String op, String timeStamp) {
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}
		MyPraisedListRequest request =
				new MyPraisedListRequest(IPageNotifyFn.PageType_PraisedList, this);
		GolukApplication app = GolukApplication.getInstance();
		if(null != app && app.isUserLoginSucess) {
			if(!TextUtils.isEmpty(app.mCurrentUId)) {
				request.get(app.mCurrentUId, tmpOp, PAGESIZE, timeStamp);
			}
		}
		if(!mLoadingDialog.isShowing() && REFRESH_NORMAL.equals(op)) {
			mLoadingDialog.show();
		}
	}

	private void setEmptyView() {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mListView.setEmptyView(mEmptyRL);
			mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}

	private void setNodataView() {
		if(REFRESH_NORMAL.equals(mCurMotion)) {
			mListView.setEmptyView(mNodataRL);
			mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		}
	}

	public void onEventMainThread(EventPraiseStatusChanged event) {
		if(null == event) {
			return;
		}

		switch(event.getOpCode()) {
		case EventConfig.PRAISE_STATUS_CHANGE:
			sendPraisedListRequest(REFRESH_NORMAL, null);
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	};

	@Override
	protected void onDestroy() {
		if(mLoadingDialog != null) {
			mLoadingDialog.close();
			mLoadingDialog = null;
		}
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	};

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		mListView.onRefreshComplete();
		if(null != mLoadingDialog) {
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_PraisedList) {
			MyPraisedListBean bean = (MyPraisedListBean)result;
			if(null == bean) {
				Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					setEmptyView();
				}
				return;
			}

			if(bean != null && bean.data != null){
				if (!GolukUtils.isTokenValid(bean.data.result)){
					setEmptyView();
					GolukApplication.getInstance().isUserLoginSucess = false;
					GolukApplication.getInstance().loginStatus = 2;
					GolukApplication.getInstance().autoLoginStatus = 3;
					GolukUtils.startUserLogin(this);
					this.finish();
					return;
				}
			}

			if(!bean.success) {
				if(!TextUtils.isEmpty(bean.msg)) {
					Toast.makeText(this, bean.msg, Toast.LENGTH_SHORT).show();
				}
				setEmptyView();
				return;
			}

			if(null == bean.data) {
				setEmptyView();
				return;
			}

			if("1".equals(bean.data.result)) {
				Toast.makeText(this, MyPraisedActivity.this.getString(
						R.string.str_server_request_arg_error), Toast.LENGTH_SHORT).show();
				setEmptyView();
				return;
			}
			if("2".equals(bean.data.result)) {
				Toast.makeText(this, MyPraisedActivity.this.getString(
						R.string.str_server_request_unknown_error), Toast.LENGTH_SHORT).show();
				setEmptyView();
				return;
			}

			mListView.setMode(PullToRefreshBase.Mode.BOTH);
			List<MyPraisedVideoBean> videoList = bean.data.videolist;
			if(null == videoList || videoList.size() == 0) {
				if("0".equals(mCurMotion)) {
					mPraisedList.clear();
					mAdapter.setData(mPraisedList);
					setNodataView();
				} else if(REFRESH_PULL_UP.equals(mCurMotion)) {
					Toast.makeText(this,
							getString(R.string.str_pull_refresh_listview_bottom_reach),
							Toast.LENGTH_SHORT).show();
				}
				return;
			}

			MyPraisedVideoBean last = videoList.get(videoList.size() - 1);
			if(null != last) {
				mTimeStamp = GolukUtils.parseMillesToTimeStr(last.ts);
			} else {
				return;
			}

			if(REFRESH_PULL_UP.equals(mCurMotion)) {
				mPraisedList.addAll(videoList);
			} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
				mPraisedList.clear();
				mPraisedList.addAll(videoList);
			} else {
			}

			mAdapter.setData(mPraisedList);
//			if(mOfficialMsgList.size() < PAGESIZE) {
//				mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//			}
			mCurMotion = REFRESH_NORMAL;
		}
	}
}
