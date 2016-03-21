package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeJson;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeUser;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeVideoList;
import cn.com.mobnote.headergridview.HeaderGridView;
import cn.com.mobnote.headergridview.PullToRefreshHeaderGridView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class TestGridViewActivity extends BaseActivity implements IRequestResultListener, OnClickListener {

	private static final String OPERATOR_FIRST = "0";
	private static final String OPERATOR_DOWN = "1";
	private static final String OPERATOR_UP = "2";

	private ImageButton mBackBtn, mMoreBtn;
	private TextView mTitleText = null;
	private PullToRefreshHeaderGridView mGridView = null;
	private TestGridViewAdapter mAdapter = null;
	private View mHeaderView = null;
	/** 当前状态 **/
	private String mCurrentOperator = "";
	/** header **/
	private UserCenterHeader mHeader = null;
	/** 用户 **/
	private UCUserInfo mUserInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_testgridview_layout);

		Intent it = getIntent();
		mUserInfo = (UCUserInfo) it.getSerializableExtra("userinfo");

		initView();

		if (testUser()) {
			mTitleText.setText("我的主页");
		} else {
			mTitleText.setText("TA的主页");
		}

	}

	private void initView() {
		mGridView = (PullToRefreshHeaderGridView) findViewById(R.id.gv_usercenter);
		mBackBtn = (ImageButton) findViewById(R.id.ib_usercenter_back);
		mTitleText = (TextView) findViewById(R.id.tv_usercenter_title);
		mMoreBtn = (ImageButton) findViewById(R.id.ib_usercenter_more);

		mBackBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);

		mGridView.getRefreshableView().setNumColumns(2);
		mGridView.getRefreshableView().setVerticalSpacing(20);
		mGridView.getRefreshableView().setHorizontalSpacing(20);

		mAdapter = new TestGridViewAdapter(this);
		mHeader = new UserCenterHeader(this);
		mHeaderView = mHeader.createHeader();
		mGridView.getRefreshableView().addHeaderView(mHeaderView);
		mGridView.setAdapter(mAdapter);

		mGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<HeaderGridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<HeaderGridView> pullToRefreshBase) {
				GolukDebugUtils.e("", "-----------onPullDownToRefresh-------");
				// 下拉刷新
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						getResources().getString(R.string.updating)
								+ GolukUtils.getCurrentFormatTime(TestGridViewActivity.this));
				httpRequestData(mUserInfo.uid, OPERATOR_FIRST);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<HeaderGridView> pullToRefreshBase) {
				GolukDebugUtils.e("", "-----------onPullUpToRefresh-------");
				// 上拉加载
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(
						getResources().getString(R.string.goluk_pull_to_refresh_footer_pull_label));
				httpRequestData(mUserInfo.uid, OPERATOR_DOWN);
			}
		});

		httpRequestData(mUserInfo.uid, OPERATOR_FIRST);

	}

	private void httpRequestData(String uid, String operation) {
		UserInfoRequest request = new UserInfoRequest(IPageNotifyFn.PageType_HomeUserInfo, this);
		request.get("200", uid, "0", uid, "");
		mCurrentOperator = operation;
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		mGridView.onRefreshComplete();
		if (requestType == IPageNotifyFn.PageType_HomeUserInfo) {
			HomeJson homeJson = (HomeJson) result;
			if (null != homeJson && null != homeJson.data && null != homeJson.data.user
					&& null != homeJson.data.videolist) {
				List<HomeVideoList> videoList = homeJson.data.videolist;
				mHeader.setHeaderData(homeJson.data);
				mHeader.getHeaderData();
				if (mCurrentOperator.equals(OPERATOR_FIRST) || mCurrentOperator.equals(OPERATOR_DOWN)) {
					this.mAdapter.setData(videoList);
				} else {

				}
			}
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ib_usercenter_back:
			exit();
			break;
		case R.id.ib_usercenter_more:

			break;

		default:
			break;
		}
	}

	private void exit() {
		finish();
	}

	/**
	 * 验证当前看的是自己的个人中心 还是别人的个人中心
	 * 
	 * @return
	 */
	public boolean testUser() {
		if (!isLoginSucess()) {
			return false;
		}
		String info = mBaseApp.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_HttpPage, 0, "");
		try {
			JSONObject json = new JSONObject(info);
			String id = json.getString("uid");

			if (id.equals(mUserInfo.uid)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean isLoginSucess() {
		if (mBaseApp.isUserLoginSucess) {
			// 登录成功
			return true;
		}
		if (mBaseApp.loginoutStatus) {
			// 用户注销, 表示登录失败
			return false;
		}
		if (mBaseApp.loginStatus == 1 || (mBaseApp.autoLoginStatus == 1 || mBaseApp.autoLoginStatus == 2)) {
			return true;
		}

		return false;
	}
}
