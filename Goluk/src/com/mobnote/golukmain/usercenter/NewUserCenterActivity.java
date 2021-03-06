package com.mobnote.golukmain.usercenter;

import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.handmark.pulltorefresh.library.GridViewWithHeaderAndFooter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderGridView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.eventbus.EventRefreshUserInfo;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.usercenter.bean.AttentionJson;
import com.mobnote.golukmain.usercenter.bean.HomeJson;
import com.mobnote.golukmain.usercenter.bean.HomeVideoList;
import com.mobnote.golukmain.usercenter.bean.ShareJson;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;

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
	private TextView mFooterText = null;
	/** ???????????? **/
	private String mCurrentOperator = "";
	/** header **/
	private UserCenterHeader mHeader = null;
	/** ?????? **/
	private UCUserInfo mUserInfo = null;
	private CustomLoadingDialog mLoadinDialog = null;
	/****/
	private HomeJson mHomeJson = null;
	/** ???????????? **/
	private UserMoreDialog mMoreDialog = null;
	/** ???????????? **/
	private SharePlatformUtil mSharePlatform = null;
	/** ???????????????uid **/
	private String mCurrentUid = "";
	private String mFirstIndex = "";
	private String mLastIndex = "";
	/** ?????????????????????????????? **/
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
		mFooterText = (TextView) mFooterView.findViewById(R.id.tv_usercenter_footer);
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
				// ????????????
				mIsFirst = false;
				pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
						getResources().getString(R.string.updating)
								+ GolukUtils.getCurrentFormatTime(NewUserCenterActivity.this));
				httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_DOWN, "");
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridViewWithHeaderAndFooter> pullToRefreshBase) {
				// ????????????
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

	public void onEventMainThread(EventDeleteVideo event) {
		if (EventConfig.VIDEO_DELETE == event.getOpCode()) {
			final String delVid = event.getVid(); // ???????????????id
			this.mAdapter.deleteVideo(delVid);
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

				ThirdShareBean shareBean = new ThirdShareBean();
				shareBean.surl = shorturl;
				shareBean.curl = customavatar;
				shareBean.db = describe;
				shareBean.tl = title;
				shareBean.bitmap = null;
				shareBean.realDesc = realDesc;
				shareBean.videoId = "";

				ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, shareBean);
				shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
			} else {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
			}
		} else if (requestType == IPageNotifyFn.PageType_HomeAttention) {
			AttentionJson attention = (AttentionJson) result;
			if (null != attention && 0 == attention.code && null != attention.data && null != mHeader
					&& null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user) {
				// 0???????????????1????????????2???????????????
				if (0 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_cancle_ok));
					mHomeJson.data.user.fans -= 1;
				} else if (1 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
					mHomeJson.data.user.fans += 1;
				} else if (2 == attention.data.link) {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
					mHomeJson.data.user.fans += 1;
				} else {
					GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_cancle_ok));
					mHomeJson.data.user.fans -= 1;
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
		Drawable drawable = getResources().getDrawable(R.drawable.mine_novideo);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		mFooterText.setCompoundDrawables(null, drawable, null, null);
		if (null != mFooterView && null != mGridView && mFooterView.getVisibility() == View.GONE) {
			if (testUser()) {
				mFooterText.setText(this.getString(R.string.str_mine_no_video_text));
			} else {
				mFooterText.setText(this.getString(R.string.str_mine_ta_video_text));
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
		int id = view.getId();
		if (id == R.id.ib_usercenter_back) {
			exit();
		} else if (id == R.id.ib_usercenter_more) {
			if (null == mHomeJson || !UserUtils.isNetDeviceAvailable(this)) {
				GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
				return;
			}
			mMoreDialog = new UserMoreDialog(this);
			mMoreDialog.show();
		} else if (id == R.id.ry_usercenter_refresh) {
			httpRequestData(mUserInfo.uid, mCurrentUid, OPERATOR_FIRST, "");
		} else {
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
	 * ?????????????????????????????????????????? ???????????????????????????
	 * 
	 * @return
	 */
	public boolean testUser() {
		if (!isLoginSucess()) {
			return false;
		}
		UserInfo info = mBaseApp.getMyInfo();
		try {

			if (info != null && info.uid.equals(mUserInfo.uid)) {
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
			// ????????????
			return true;
		}
		if (mBaseApp.loginoutStatus) {
			// ????????????, ??????????????????
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
	 * ??????
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
	 * ??????
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
