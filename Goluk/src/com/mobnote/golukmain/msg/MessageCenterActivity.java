package com.mobnote.golukmain.msg;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import cn.com.mobnote.module.page.IPageNotifyFn;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.tiros.debug.GolukDebugUtils;

import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMessageUpdate;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.PushSettingActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.msg.bean.MessageCounterBean;
import com.mobnote.manager.MessageManager;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;

import de.greenrobot.event.EventBus;

public class MessageCenterActivity extends BaseActivity implements OnClickListener, IRequestResultListener {
	private final static String TAG = "MessageCenterActivity";

	private ImageButton mBackButton;
	private TextView mMessageSetting;
	private RelativeLayout mPraiseRL;
	private RelativeLayout mCommentRL;
	private RelativeLayout mSystemRL;
	private TextView mOfficialTV;
	private TextView mPraiseTV;
	private TextView mCommentTV;
	private TextView mSystemTV;
	private PullToRefreshScrollView mScrollView;
	private final static String PROTOCOL = "100";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_center);

		mBackButton = (ImageButton)findViewById(R.id.ib_back_btn);
		mMessageSetting = (TextView)findViewById(R.id.tv_msg_setting);
		mPraiseRL = (RelativeLayout)findViewById(R.id.rl_msg_center_praise);
		mCommentRL = (RelativeLayout)findViewById(R.id.rl_msg_center_comment);
		mSystemRL = (RelativeLayout)findViewById(R.id.rl_msg_center_system);
		mOfficialTV = (TextView)findViewById(R.id.tv_msg_center_official);
		mPraiseTV = (TextView)findViewById(R.id.tv_msg_center_praise_tip);
		mCommentTV = (TextView)findViewById(R.id.tv_msg_center_comment_tip);
		mSystemTV = (TextView)findViewById(R.id.tv_msg_center_system_tip);
		mScrollView = (PullToRefreshScrollView)findViewById(R.id.sv_msg_center);

		mBackButton.setOnClickListener(this);
		mMessageSetting.setOnClickListener(this);
		mPraiseRL.setOnClickListener(this);
		mCommentRL.setOnClickListener(this);
		mSystemRL.setOnClickListener(this);
		mOfficialTV.setOnClickListener(this);

		mScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
		if(GolukApplication.getInstance().isUserLoginSucess) {
			MsgCenterCounterRequest msgCounterReq = new MsgCenterCounterRequest(IPageNotifyFn.PageType_MsgCounter, this);
			msgCounterReq.get(PROTOCOL, GolukApplication.getInstance().mCurrentUId, "", "", "");
		}

		mScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ScrollView> pullToRefreshBase) {
				// show latest refresh time
				pullToRefreshBase.getLoadingLayoutProxy().setLastUpdatedLabel(
						MessageCenterActivity.this.getResources().getString(R.string.updating) +
						GolukUtils.getCurrentFormatTime(MessageCenterActivity.this));

				if(GolukApplication.getInstance().isUserLoginSucess) {
					MsgCenterCounterRequest newReq = new MsgCenterCounterRequest(
						IPageNotifyFn.PageType_MsgCounter, MessageCenterActivity.this);
					newReq.get(PROTOCOL, GolukApplication.getInstance().mCurrentUId, "", "", "");
				} else {
					mScrollView.onRefreshComplete();
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ScrollView> pullToRefreshBase) {
			}
		});
		EventBus.getDefault().register(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ib_back_btn) {
			finish();
		} else if (id == R.id.tv_msg_setting) {
			startMsgSettingActivity();
		} else if (id == R.id.rl_msg_center_praise) {
			if(!GolukUtils.isNetworkConnected(this)) {
				Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				return;
			}
			GolukApplication app = (GolukApplication)getApplication();
			if (!app.isUserLoginSucess) {
//					GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_login));
				Intent intent = null;
				if(GolukApplication.getInstance().isInteral() == false){
					intent = new Intent(this, InternationUserLoginActivity.class);
				}else{
					intent = new Intent(this, UserLoginActivity.class);
				}
				
				startActivityForResult(intent, GolukConfig.REQUEST_CODE_MSG_LOGIN_PRAISE);
				return;
			}
			Intent praise = new Intent(this, MsgCenterPraiseActivity.class);
			startActivity(praise);
		} else if (id == R.id.rl_msg_center_comment) {
			if(!GolukUtils.isNetworkConnected(this)) {
				Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				return;
			}
			GolukApplication app = (GolukApplication)getApplication();
			if (!app.isUserLoginSucess) {
//					GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_login));
				Intent intent = null;
				if(GolukApplication.getInstance().isInteral() == false){
					intent = new Intent(this, InternationUserLoginActivity.class);
				}else{
					intent = new Intent(this, UserLoginActivity.class);
				}
				startActivityForResult(intent, GolukConfig.REQUEST_CODE_MSG_LOGIN_COMMENT);
				return;
			}
			Intent comment = new Intent(this, MsgCenterCommentActivity.class);
			startActivity(comment);
		} else if (id == R.id.rl_msg_center_system) {
			if(!GolukUtils.isNetworkConnected(this)) {
				Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				return;
			}
			GolukApplication app = (GolukApplication)getApplication();
			if (!app.isUserLoginSucess) {
//					GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_login));
				Intent intent = null;
				if(GolukApplication.getInstance().isInteral() == false){
					intent = new Intent(this, InternationUserLoginActivity.class);
				}else{
					intent = new Intent(this, UserLoginActivity.class);
				}
				startActivityForResult(intent, GolukConfig.REQUEST_CODE_MSG_LOGIN_SYSTEM);
				return;
			}
			Intent system = new Intent(this, SystemMsgActivity.class);
			startActivity(system);
		} else if (id == R.id.tv_msg_center_official) {
			// Goto system message activity
			if(!GolukUtils.isNetworkConnected(this)) {
				Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
				return;
			}
			Intent official = new Intent(this, OfficialMessageActivity.class);
			startActivity(official);
		} else {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case GolukConfig.REQUEST_CODE_MSG_LOGIN_PRAISE:
				//start praise list
				Intent praise = new Intent(this, MsgCenterPraiseActivity.class);
				startActivity(praise);
				break;
			case GolukConfig.REQUEST_CODE_MSG_LOGIN_COMMENT:
				//start comment list
				Intent comment = new Intent(this, MsgCenterCommentActivity.class);
				startActivity(comment);
				break;
			case GolukConfig.REQUEST_CODE_MSG_LOGIN_SYSTEM:
				Intent system = new Intent(this, SystemMsgActivity.class);
				startActivity(system);
				break;
			case GolukConfig.REQUEST_CODE_MSG_LOGIN_SETTING:
				Intent setting = new Intent(this, PushSettingActivity.class);
				startActivity(setting);
				break;
			default:
				GolukDebugUtils.d(TAG, "unknown activity returned");
				break;
			}
		}
	};

	private void getHttpData(int type) {
		MsgCenterCounterRequest newReq = new MsgCenterCounterRequest(
				IPageNotifyFn.PageType_MsgCounter, MessageCenterActivity.this);
		newReq.get(PROTOCOL, GolukApplication.getInstance().mCurrentUId, "", "", "");
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	private void setEachCounter() {
		int praise = MessageManager.getMessageManager().getPraiseCount();
		int comment = MessageManager.getMessageManager().getCommentCount();
		int system = MessageManager.getMessageManager().getSystemMessageCount();

		setMessageTip(mPraiseTV, praise);
		setMessageTip(mCommentTV, comment);
		setMessageTip(mSystemTV, system);
	}

	private void setMessageTip(TextView tv, int number) {
		String sNumber = null;
		if(number <= 0) {
			tv.setVisibility(View.GONE);
			return;
		}

		tv.setVisibility(View.VISIBLE);
		if(number > 99) {
			sNumber = "99+";
		} else {
			sNumber = String.valueOf(number);
		}
		tv.setText(sNumber);
	}

	private void startMsgSettingActivity() {
		GolukApplication app = (GolukApplication)getApplication();
		if(!GolukUtils.isNetworkConnected(this)) {
			Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			return;
		}
//		if (!app.isUserLoginSucess) {
//			GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_login));
//			return;
//		}
		if (!app.isUserLoginSucess) {
//			GolukUtils.showToast(this, this.getResources().getString(R.string.str_please_login));
			Intent intent = null;
			if(GolukApplication.getInstance().isInteral() == false){
				intent = new Intent(this, InternationUserLoginActivity.class);
			}else{
				intent = new Intent(this, UserLoginActivity.class);
			}
			startActivityForResult(intent, GolukConfig.REQUEST_CODE_MSG_LOGIN_SETTING);
			return;
		}
		Intent intent = new Intent(this, PushSettingActivity.class);
		startActivity(intent);
	}

	public void onEventMainThread(EventMessageUpdate event) {
		if (null == event) {
			return;
		}

		switch (event.getOpCode()) {
		case EventConfig.MESSAGE_UPDATE:
			setEachCounter();
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		mScrollView.onRefreshComplete();
		if(null == result) {
			Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
			return;
		}

		if(requestType == IPageNotifyFn.PageType_MsgCounter) {
			MessageCounterBean bean = (MessageCounterBean)result;
			if(null == bean.data) {
				Toast.makeText(this, getString(
						R.string.str_server_request_unknown_error), Toast.LENGTH_SHORT).show();
				return;
			}

			if(!bean.success) {
				Toast.makeText(this, getString(
						R.string.str_server_request_arg_error), Toast.LENGTH_SHORT).show();
				return;
			}

			if(!"0".equals(bean.data.result)) {
				return;
			}

			if(null != bean.data.messagecount){
				int praiseCount = 0;
				int commentCount = 0;
				int systemCount = 0;
				int followCount =0;
				if(null != bean.data.messagecount.user) {
					praiseCount = bean.data.messagecount.user.like;
					commentCount = bean.data.messagecount.user.comment;
				}
				if(null != bean.data.messagecount.system) {
					systemCount = bean.data.messagecount.system.total;
				}

				MessageManager.getMessageManager().setMessageEveryCount(
						praiseCount,
						commentCount,
						followCount,
						systemCount);
			}
		}
	}
}
