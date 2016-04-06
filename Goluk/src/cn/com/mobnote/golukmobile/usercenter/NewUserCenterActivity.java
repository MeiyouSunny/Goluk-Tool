package cn.com.mobnote.golukmobile.usercenter;

import java.util.List;

import org.json.JSONObject;

import com.handmark.pulltorefresh.library.GridViewWithHeaderAndFooter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderGridView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.eventbus.EventConfig;
import cn.com.mobnote.eventbus.EventRefreshUserInfo;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog.ForbidBack;
import cn.com.mobnote.golukmobile.http.IRequestResultListener;
import cn.com.mobnote.golukmobile.thirdshare.CustomShareBoard;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.usercenter.bean.AttentionJson;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeJson;
import cn.com.mobnote.golukmobile.usercenter.bean.HomeVideoList;
import cn.com.mobnote.golukmobile.usercenter.bean.ShareJson;
import cn.com.mobnote.golukmobile.videodetail.VideoDetailActivity;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.user.UserUtils;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class NewUserCenterActivity extends BaseActivity implements IRequestResultListener, OnClickListener,
		OnItemClickListener, ForbidBack {

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
	/**是否是第一次请求数据**/
	private boolean mIsFirst = false;

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
		
		EventBus.getDefault().register(this);

	}

	private void initView() {
		mGridView = (PullToRefreshHeaderGridView) findViewById(R.id.gv_usercenter);
		mBackBtn = (ImageButton) findViewById(R.id.ib_usercenter_back);
		mTitleText = (TextView) findViewById(R.id.tv_usercenter_title);
		mMoreBtn = (ImageButton) findViewById(R.id.ib_usercenter_more);
		mRefreshLayout = (RelativeLayout) findViewById(R.id.ry_usercenter_refresh);
		mFooterView = LayoutInflater.from(this).inflate(R.layout.activity_usercenter_footer, null);
		mFooterImage = (ImageView) mFooterView.findViewById(R.id.iv_usercenter_footer);
		mFooterView.setVisibility(View.GONE);

		mBackBtn.setOnClickListener(this);
		mMoreBtn.setOnClickListener(this);
		mRefreshLayout.setOnClickListener(this);
		mGridView.setOnItemClickListener(this);

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
				mIsFirst = false;
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						getResources().getString(R.string.updating)
								+ GolukUtils.getCurrentFormatTime(NewUserCenterActivity.this));
				httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_DOWN, "");
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridViewWithHeaderAndFooter> pullToRefreshBase) {
				// 上拉加载
				mIsFirst = false;
				pullToRefreshBase.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(
						getResources().getString(R.string.goluk_pull_to_refresh_footer_pull_label));
				httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_UP, mLastIndex);
			}

		});
		
		httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_FIRST, "");
		mIsFirst = true;

	}

	public void onEventMainThread(EventRefreshUserInfo event) {
		if (null == event) {
			return;
		}
		switch (event.getOpCode()) {
		case EventConfig.REFRESH_USER_INFO:
			if (mUserInfo != null) {
				if (testUser()) {
					mIsFirst = false;
					httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_FIRST, "");
				}
			}
			break;
		default:
			break;
		}
	}

	private void httpRequestData(String otheruid, String currentuid, String operation, String index) {
		if (OPERATOR_FIRST.equals(operation)) {
			showLoadingDialog();
		}
		if (operation.equals(OPERATOR_DOWN)) {
			operation = OPERATOR_FIRST;
		}
		UserInfoRequest request = new UserInfoRequest(IPageNotifyFn.PageType_HomeUserInfo, this);
		request.get(otheruid, operation, currentuid, index);
		mCurrentOperator = operation;
	}

	@Override
	public void onLoadComplete(int requestType, Object result) {
		mGridView.onRefreshComplete();
		closeLoadingDialog();
		if (requestType == IPageNotifyFn.PageType_HomeUserInfo) {
			if (!UserUtils.isNetDeviceAvailable(this)) {
				unusual();
				return;
			}
			mHomeJson = (HomeJson) result;
			if (null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user
					&& null != mHomeJson.data.videolist) {
				mGridView.setVisibility(View.VISIBLE);
				mRefreshLayout.setVisibility(View.GONE);
				List<HomeVideoList> videoList = mHomeJson.data.videolist;

				mHeader.setHeaderData(mHomeJson.data);
				mHeader.getHeaderData();

				if (null != videoList && videoList.size() <= 0 && !mCurrentOperator.equals(OPERATOR_UP)) {
					mGridView.setMode(PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
					addFooterView();
					return;
				}
				removeFooterView();
				if (null != videoList && videoList.size() > 0) {
					mFirstIndex = videoList.get(0).index;
					mLastIndex = videoList.get(mHomeJson.data.videocount - 1).index;
					GolukDebugUtils.e("", "-----newusercenteractivity-----firstindex: " + mFirstIndex
							+ "----lastindex: " + mLastIndex);
				}
				if (mCurrentOperator.equals(OPERATOR_FIRST)) {
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
				CustomShareBoard shareBoard = new CustomShareBoard(this, mSharePlatform, shorturl, customavatar,
						describe, title, null, realDesc, "");
				shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
			}
		} else if (requestType == IPageNotifyFn.PageType_HomeAttention) {
			AttentionJson attention = (AttentionJson) result;
			if (null != attention && 0 == attention.code && null != attention.data && null != mHeader
					&& null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user) {
				// 0：未关注；1：关注；2：互相关注
				if (0 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_cancle_ok));
					mHomeJson.data.user.fans -= 1;
				} else if (1 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
					mHomeJson.data.user.fans += 1;
				} else {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
					mHomeJson.data.user.fans += 1;
				}
				mHeader.changeAttentionState(attention.data.link);
				mHomeJson.data.user.link = attention.data.link;
				mHeader.setHeaderData(mHomeJson.data);
				mHeader.getHeaderData();
			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
			}
		}
	}

	private void unusual() {
		GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
		if (mIsFirst) {
			mRefreshLayout.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);
		}
	}

	private void addFooterView() {
		if (null != mFooterView && null != mGridView && mFooterView.getVisibility() == View.GONE) {
			if (testUser()) {
				mFooterImage.setImageResource(R.drawable.mine_novideo);
			} else {
				mFooterImage.setImageResource(R.drawable.mine_tavideo);
			}
			mGridView.getRefreshableView().addFooterView(mFooterView);
			mFooterView.setVisibility(View.VISIBLE);
		}
	}

	private void removeFooterView() {
		if (null != mGridView && null != mFooterView && mFooterView.getVisibility() == View.VISIBLE) {
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
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
				return;
			}
			mMoreDialog = new UserMoreDialog(this);
			mMoreDialog.show();
			break;
		case R.id.ry_usercenter_refresh:
			httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_FIRST, "");
			break;

		default:
			break;
		}
	}

	private void exit() {
		if (null != mMoreDialog) {
			mMoreDialog.dismiss();
		}
		closeLoadingDialog();
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

	private void showLoadingDialog() {
		if (null == mLoadinDialog) {
			mLoadinDialog = new CustomLoadingDialog(this, null);
			mLoadinDialog.show();
			mLoadinDialog.setListener(this);
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
		if (null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user
				&& null != mHomeJson.data.user.uid) {
			showLoadingDialog();
			ShareHomePageRequest request = new ShareHomePageRequest(IPageNotifyFn.PageType_HomeShare, this);
			request.get(mCurrentUid, mHomeJson.data.user.uid);
		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
		}
	}

	/**
	 * 关注
	 * 
	 * @param otheruid
	 * @param type
	 * @param currentuid
	 */
	public void attentionRequest(String type) {
		if (null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user
				&& null != mHomeJson.data.user.uid) {
			showLoadingDialog();
			UserAttentionRequest request = new UserAttentionRequest(IPageNotifyFn.PageType_HomeAttention, this);
			request.get(mHomeJson.data.user.uid, type, mCurrentUid);
		} else {
			GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeLoadingDialog();
		EventBus.getDefault().unregister(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		if (!UserUtils.isNetDeviceAvailable(this)) {
			GolukUtils.showToast(this, this.getString(R.string.str_network_unavailable));
			return;
		}
		if (null != mAdapter && position >= 2) {
			HomeVideoList video = (HomeVideoList) mAdapter.getItem(position - 2);
			if (null != video) {
				Intent itVideoDetail = new Intent(this, VideoDetailActivity.class);
				itVideoDetail.putExtra(VideoDetailActivity.VIDEO_ID, video.videoid);
				startActivity(itVideoDetail);
			}
		}
	}

	@Override
	public void forbidBackKey(int backKey) {
		if (1 == backKey) {
			exit();
		}
	}

}
