package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.bean.AttentionJson;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeJson;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeVideoList;
import cn.com.mobnote.golukmobile.usercenter.bean.ShareJson;
import cn.com.mobnote.headergridview.GridViewWithHeaderAndFooter;
import cn.com.mobnote.headergridview.PullToRefreshHeaderGridView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class NewUserCenterActivity extends BaseActivity implements IRequestResultListener, OnClickListener {

	private static final String OPERATOR_FIRST = "0";
	private static final String OPERATOR_DOWN = "1";
	private static final String OPERATOR_UP = "2";

	private ImageButton mBackBtn, mMoreBtn;
	private TextView mTitleText = null;
	private PullToRefreshHeaderGridView mGridView = null;
	private RelativeLayout mRefreshLayout = null;
	private NewUserCenterAdapter mAdapter = null;
	/** header view **/
	private View mHeaderView = null;
	/** footer view **/
	private View mFooterView = null;
	private ImageView mFooterImage = null;
	private TextView mFooterRefresh = null;
	private RelativeLayout mFooterLayout = null;
	/** 当前状态 **/
	private String mCurrentOperator = "";
	/** header **/
	private UserCenterHeader mHeader = null;
	/** 用户 **/
	private UCUserInfo mUserInfo = null;
	private CustomLoadingDialog mLoadinDialog = null;
	/****/
	private HomeJson mHomeJson = null;
	/** 更多按钮 **/
	private UserMoreDialog mMoreDialog = null;
	/** 分享平台 **/
	private SharePlatformUtil mSharePlatform = null;
	/** 当前用户的uid **/
	private String mCurrentUid = "";
	private String mFirstIndex = "";
	private String mLastIndex = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_usercenter_layout);

		mCurrentUid = GolukApplication.getInstance().mCurrentUId;

		Intent it = getIntent();
		mUserInfo = (UCUserInfo) it.getSerializableExtra("userinfo");

		initView();

		if (testUser()) {
			mTitleText.setText(this.getString(R.string.user_personal_home_title));
		} else {
			mTitleText.setText(this.getString(R.string.str_his_homepage));
		}

	}

	private void initView() {
		mGridView = (PullToRefreshHeaderGridView) findViewById(R.id.gv_usercenter);
		mBackBtn = (ImageButton) findViewById(R.id.ib_usercenter_back);
		mTitleText = (TextView) findViewById(R.id.tv_usercenter_title);
		mMoreBtn = (ImageButton) findViewById(R.id.ib_usercenter_more);
		mRefreshLayout = (RelativeLayout) findViewById(R.id.ry_usercenter_refresh);
		mFooterView = LayoutInflater.from(this).inflate(R.layout.activity_usercenter_footer, null);
		mFooterImage = (ImageView) mFooterView.findViewById(R.id.iv_usercenter_footer);
		mFooterRefresh = (TextView) mFooterView.findViewById(R.id.tv_usercenter_empty);
		mFooterLayout = (RelativeLayout) mFooterView.findViewById(R.id.ry_usercenter_footer_layout);
		mFooterView.setVisibility(View.GONE);

		mBackBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		mRefreshLayout.setOnClickListener(this);
		mFooterLayout.setOnClickListener(this);

		mSharePlatform = new SharePlatformUtil(this);

		mGridView.getRefreshableView().setNumColumns(2);
		mGridView.getRefreshableView().setVerticalSpacing(12);
		mGridView.getRefreshableView().setHorizontalSpacing(12);

		mAdapter = new NewUserCenterAdapter(this);
		mHeader = new UserCenterHeader(this);
		mHeaderView = mHeader.createHeader();
		mGridView.getRefreshableView().addHeaderView(mHeaderView);
		mGridView.setAdapter(mAdapter);
		mGridView.setMode(PullToRefreshBase.Mode.BOTH);

		mGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridViewWithHeaderAndFooter>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridViewWithHeaderAndFooter> pullToRefreshBase) {
				// 下拉刷新
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						getResources().getString(R.string.updating)
								+ GolukUtils.getCurrentFormatTime(NewUserCenterActivity.this));
				httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_DOWN, mFirstIndex);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridViewWithHeaderAndFooter> pullToRefreshBase) {
				// 上拉加载
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(
						getResources().getString(R.string.goluk_pull_to_refresh_footer_pull_label));
				httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_UP, mLastIndex);
			}

		});

		httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_FIRST, "");

	}

	private void httpRequestData(String otheruid, String currentuid, String operation, String index) {
		if (OPERATOR_FIRST.equals(operation)) {
			showLoadingDialog();
		}
		if (operation.equals(OPERATOR_DOWN)) {
			operation = OPERATOR_FIRST;
		}
		if (!UserUtils.isNetDeviceAvailable(this)) {
			closeLoadingDialog();
			unusual();
			return;
		}
		UserInfoRequest request = new UserInfoRequest(IPageNotifyFn.PageType_HomeUserInfo, this);
		request.get("200", otheruid, operation, currentuid, index);
		mCurrentOperator = operation;
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		mGridView.onRefreshComplete();
		closeLoadingDialog();
		if (requestType == IPageNotifyFn.PageType_HomeUserInfo) {
			mHomeJson = (HomeJson) result;
			if (null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user
					&& null != mHomeJson.data.videolist) {
				mHeader.usercenterNoData(true);
				List<HomeVideoList> videoList = mHomeJson.data.videolist;
				if (!mCurrentOperator.equals(OPERATOR_UP)) {
					mHeader.setHeaderData(mHomeJson.data);
					mHeader.getHeaderData();
				}
				if (null != videoList && videoList.size() <= 0 && !mCurrentOperator.equals(OPERATOR_UP)) {
					mGridView.setMode(PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
					addFooterView(1);
					return;
				}
				removeFooterView();
				GolukDebugUtils.e("", "----------usercenterheader-------PageType_HomeUserInfo----link: " + mHomeJson.data.user.link);
				if (mCurrentOperator.equals(OPERATOR_FIRST)) {
					mFirstIndex = videoList.get(0).index;
					mLastIndex = videoList.get(mHomeJson.data.videocount - 1).index;
					GolukDebugUtils.e("", "-----newusercenteractivity-----firstindex: " + mFirstIndex
							+ "----lastindex: " + mLastIndex);
					this.mAdapter.setData(videoList);
				} else {
					if (0 == videoList.size()) {
						GolukUtils.showToast(this, this.getString(R.string.str_pull_refresh_listview_bottom_reach));
						return;
					}
					this.mAdapter.appendData(videoList);
				}
			} else {
				unusual();
			}
		} else if (requestType == IPageNotifyFn.PageType_HomeShare) {
			ShareJson shareJson = (ShareJson) result;
			if (null != shareJson && shareJson.success && null != shareJson.data) {
				String shorturl = shareJson.data.shorturl;
				String describe = shareJson.data.describe;
				String title = shareJson.data.title;
				String customavatar = shareJson.data.customavatar;
				String headportrait = shareJson.data.headportrait;
				String realDesc = this.getString(R.string.str_usercenter_share_realdesc);
				if (null == customavatar || "".equals(customavatar)) {
					customavatar = headportrait;
				}
				CustomShareBoard shareBoard = new CustomShareBoard(this, mSharePlatform, shorturl, customavatar,
						describe, title, null, realDesc, "");
				shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			}
		} else if (requestType == IPageNotifyFn.PageType_HomeAttention) {
			AttentionJson attention = (AttentionJson) result;
			GolukDebugUtils.e("", "----------usercenterheader-------PageType_HomeAttention----link: " + attention.data.link);
			if (null != attention && 0 == attention.code && null != attention.data) {
				// 0：未关注；1：关注；2：互相关注
				if (0 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_cancle_ok));
				} else if (1 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
				} else {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
				}
				mHeader.changeAttentionState(attention.data.link);
				mHomeJson.data.user.link = attention.data.link;
				mAdapter.notifyDataSetChanged();
			}
		}
	}

	private void unusual() {
		if (!mCurrentOperator.equals(OPERATOR_DOWN)) {
			mHeader.usercenterNoData(false);
			addFooterView(2);
		}
		mGridView.setMode(PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
		GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
	}

	private void addFooterView(int type) {
		int a = mFooterView.getVisibility();
		if (null != mFooterView && mFooterView.getVisibility() == View.GONE) {
			if (1 == type) {
				mFooterView.setEnabled(false);
				mFooterImage.setVisibility(View.VISIBLE);
				mFooterRefresh.setVisibility(View.GONE);
				if (testUser()) {
					mFooterImage.setImageResource(R.drawable.mine_novideo);
				} else {
					mFooterImage.setImageResource(R.drawable.mine_tavideo);
				}
			} else {
				mFooterView.setEnabled(true);
				mFooterImage.setVisibility(View.GONE);
				mFooterRefresh.setVisibility(View.VISIBLE);

			}
			mGridView.getRefreshableView().addFooterView(mFooterView);
			mFooterView.setVisibility(View.VISIBLE);
		}
	}

	private void removeFooterView() {
		if (null != mGridView && null != mFooterView) {
			mGridView.getRefreshableView().removeFooterView(mFooterView);
			mFooterView.setVisibility(View.GONE);
			// mFooterView = null;
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ib_usercenter_back:
			exit();
			break;
		case R.id.ib_usercenter_more:
			if (null == mHomeJson || !UserUtils.isNetDeviceAvailable(this)) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
				return;
			}
			mMoreDialog = new UserMoreDialog(this);
			mMoreDialog.show();
			break;
		case R.id.ry_usercenter_footer_layout:
			httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_FIRST, "");
			break;

		default:
			break;
		}
	}

	private void exit() {
		finish();
		if (null != mMoreDialog) {
			mMoreDialog.dismiss();
		}
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

	private void showLoadingDialog() {
		if (null == mLoadinDialog) {
			mLoadinDialog = new CustomLoadingDialog(this, null);
			mLoadinDialog.show();
		}
	}

	private void closeLoadingDialog() {
		if (null != mLoadinDialog) {
			mLoadinDialog.close();
			mLoadinDialog = null;
		}
	}

	/**
	 * 分享
	 * 
	 */
	public void shareHomePage() {
		showLoadingDialog();
		ShareHomePageRequest request = new ShareHomePageRequest(IPageNotifyFn.PageType_HomeShare, this);
		request.get(mCurrentUid, mHomeJson.data.user.uid);
	}

	/**
	 * 关注
	 * @param otheruid
	 * @param type
	 * @param currentuid
	 */
	public void attentionRequest(String type) {
		UserAttentionRequest request = new UserAttentionRequest(IPageNotifyFn.PageType_HomeAttention, this);
		request.get(mHomeJson.data.user.uid, type, mCurrentUid);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeLoadingDialog();
	}
}
