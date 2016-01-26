package cn.com.mobnote.golukmobile.msg;

import java.util.ArrayList;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.msg.bean.MessageBean;
import cn.com.mobnote.golukmobile.msg.bean.MessageMsgsBean;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;

public class OfficialMessageActivity extends BaseActivity implements IRequestResultListener {
	private final static String REFRESH_NORMAL = "0";
	private final static String REFRESH_PULL_DOWN = "1";
	private final static String REFRESH_PULL_UP = "2";

	private PullToRefreshListView mListView;
	private OfficialMessageListAdapter mAdapter;
	private List<MessageMsgsBean> mOfficialMsgList;
	private ImageButton mBackButton;
	private RelativeLayout mEmptyRL;
	private RelativeLayout mNodataRL;
	private final static String PROTOCOL = "100";
	private final static String TYPE_ARRAY = "[300]";
	private final static String ANYCAST = "anycast";
	private final static int PAGESIZE = 20;
	private String mTimeStamp = "";
	private String mCurMotion = REFRESH_NORMAL;
	private TextView mRetryClickIV;
	private CustomLoadingDialog mLoadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_official_message);
		mBackButton = (ImageButton)findViewById(R.id.ib_offcial_message_back_btn);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OfficialMessageActivity.this.finish();
			}
		});

		mEmptyRL = (RelativeLayout)findViewById(R.id.rl_official_message_exception_refresh);
		mNodataRL = (RelativeLayout)findViewById(R.id.rl_official_message_no_data_refresh);
		mRetryClickIV = (TextView)findViewById(R.id.iv_official_message_exception_refresh);
		mListView = (PullToRefreshListView)findViewById(R.id.plv_official_message);
		mRetryClickIV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendOfficialRequest(REFRESH_NORMAL, null);
			}
		});
		mAdapter = new OfficialMessageListAdapter(this);
		mListView.setAdapter(mAdapter);
		mLoadingDialog = new CustomLoadingDialog(this, null);
		mListView.setMode(PullToRefreshBase.Mode.DISABLED);
		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						OfficialMessageActivity.this.getResources().getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(OfficialMessageActivity.this));
				sendOfficialRequest(REFRESH_PULL_DOWN, null);
				mCurMotion = REFRESH_PULL_DOWN;
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> pullToRefreshBase) {
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setPullLabel(
						OfficialMessageActivity.this.getResources().getString(
						R.string.goluk_pull_to_refresh_footer_pull_label));
				sendOfficialRequest(REFRESH_PULL_UP, mTimeStamp);
				mCurMotion = REFRESH_PULL_UP;
			}
		});

		sendOfficialRequest(REFRESH_NORMAL, null);
		mOfficialMsgList = new ArrayList<MessageMsgsBean>();
	}

	private void sendOfficialRequest(String op, String timeStamp) {
		String tmpOp = op;
		if(REFRESH_PULL_DOWN.equals(op)) {
			tmpOp = REFRESH_NORMAL;
		}
		MessageCenterOfficialRequest request =
				new MessageCenterOfficialRequest(IPageNotifyFn.PageType_MsgOfficial, this);
		request.get(PROTOCOL, ANYCAST, TYPE_ARRAY, tmpOp, timeStamp);
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
		super.onDestroy();
	};

	@Override
	public void onLoadComplete(int requestType, Object result) {
		// TODO Auto-generated method stub
		mListView.onRefreshComplete();
		if(null != mLoadingDialog) {
			mLoadingDialog.close();
		}

		if(requestType == IPageNotifyFn.PageType_MsgOfficial) {
			MessageBean bean = (MessageBean)result;
			if(null == bean) {
				Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
					setEmptyView();
				}
				return;
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
				Toast.makeText(this, OfficialMessageActivity.this.getString(
						R.string.str_server_request_arg_error), Toast.LENGTH_SHORT).show();
				setEmptyView();
				return;
			}
			if("2".equals(bean.data.result)) {
				Toast.makeText(this, OfficialMessageActivity.this.getString(
						R.string.str_server_request_unknown_error), Toast.LENGTH_SHORT).show();
				setEmptyView();
				return;
			}

			mListView.setMode(PullToRefreshBase.Mode.BOTH);
			if(null == bean.data.messages || bean.data.messages.size() == 0) {
				if("0".equals(bean.data.operation)) {
					setNodataView();
				} else if(REFRESH_PULL_UP.equals(mCurMotion)) {
					Toast.makeText(this,
							getString(R.string.str_pull_refresh_listview_bottom_reach),
							Toast.LENGTH_SHORT).show();
				}
				return;
			}

			List<MessageMsgsBean> msgBeanList = bean.data.messages;

			if(msgBeanList.size() == 0) {
				Toast.makeText(this, getString(
						R.string.str_pull_refresh_listview_bottom_reach), Toast.LENGTH_SHORT).show();
			}

			MessageMsgsBean last = bean.data.messages.get(msgBeanList.size() - 1);
			if(null != last) {
				mTimeStamp = last.addtime;
			} else {
				return;
			}

			if(REFRESH_PULL_UP.equals(mCurMotion)) {
				mOfficialMsgList.addAll(bean.data.messages);
			} else if(REFRESH_NORMAL.equals(mCurMotion) || REFRESH_PULL_DOWN.equals(mCurMotion)) {
				mOfficialMsgList.clear();
				mOfficialMsgList.addAll(bean.data.messages);
			} else {
			}

			mAdapter.setData(mOfficialMsgList);
//			if(mOfficialMsgList.size() < PAGESIZE) {
//				mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//			}
			mCurMotion = REFRESH_NORMAL;
		}
	}
}
