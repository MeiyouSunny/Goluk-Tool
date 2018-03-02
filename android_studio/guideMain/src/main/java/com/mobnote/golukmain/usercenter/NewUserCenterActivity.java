package com.mobnote.golukmain.usercenter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.Toast;

import com.handmark.pulltorefresh.library.GridViewWithHeaderAndFooter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshHeaderGridView;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog.ForbidBack;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
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
import com.mobnote.util.ZhugeUtils;

import java.util.List;

import cn.com.mobnote.module.page.IPageNotifyFn;
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
    /**
     * header view
     **/
    private View mHeaderView = null;
    /**
     * footer view
     **/
    private View mFooterView = null;
    private TextView mFooterText = null;
    /**
     * 当前状态
     **/
    private String mCurrentOperator = "";
    /**
     * header
     **/
    private UserCenterHeader mHeader = null;
    /**
     * 用户
     **/
    private String mUserId = null;
    private CustomLoadingDialog mLoadinDialog = null;
    /****/
    private HomeJson mHomeJson = null;
    /**
     * 更多按钮
     **/
    private UserMoreDialog mMoreDialog = null;
    /**
     * 分享平台
     **/
    private SharePlatformUtil mSharePlatform = null;
    /**
     * 当前用户的uid
     **/
    private String mCurrentUid = "";
    private String mFirstIndex = "";
    private String mLastIndex = "";
    /**
     * 是否是第一次请求数据
     **/
    private boolean mIsFirst = false;
    private boolean activityHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_usercenter_layout);

        mCurrentUid = GolukApplication.getInstance().mCurrentUId;

        Intent it = getIntent();
        mUserId = it.getStringExtra("userId");

        initView();

        if (testUser()) {
            mTitleText.setText(this.getString(R.string.user_personal_home_title));
        } else {
            mTitleText.setText(this.getString(R.string.str_his_homepage));
        }

        EventBus.getDefault().register(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityHidden = false;
        httpRequestData(mUserId, mCurrentUid, OPERATOR_FIRST, "");
        ZhugeUtils.eventUserCenter(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityHidden = true;
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
                // 下拉刷新
                mIsFirst = false;
                pullToRefreshBase.getLoadingLayoutProxy(true, false).setLastUpdatedLabel(
                        getResources().getString(R.string.updating)
                                + GolukUtils.getCurrentFormatTime(NewUserCenterActivity.this));
                httpRequestData(mUserId, mCurrentUid, OPERATOR_DOWN, "");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridViewWithHeaderAndFooter> pullToRefreshBase) {
                // 上拉加载
                mIsFirst = false;
                pullToRefreshBase.getLoadingLayoutProxy(false, true).setLastUpdatedLabel(
                        getResources().getString(R.string.goluk_pull_to_refresh_footer_pull_label));
                httpRequestData(mUserId, mCurrentUid, OPERATOR_UP, mLastIndex);
            }

        });

        mIsFirst = true;
    }

    public void onEventMainThread(EventDeleteVideo event) {
        if (EventConfig.VIDEO_DELETE == event.getOpCode()) {
            final String delVid = event.getVid(); // 已经删除的id
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

    public void startUserLogin() {
        Intent loginIntent = null;
        if (GolukApplication.getInstance().isMainland() == false) {
            loginIntent = new Intent(this, InternationUserLoginActivity.class);
        } else {
            loginIntent = new Intent(this, UserLoginActivity.class);
        }
        startActivity(loginIntent);
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
            if (mHomeJson != null) {
                //token过期
                if (!GolukUtils.isTokenValid(mHomeJson.code)) {
                    mRefreshLayout.setVisibility(View.VISIBLE);
                    mGridView.setVisibility(View.GONE);
                    startUserLogin();
                    return;
                }
            }
            if (null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user
                    && null != mHomeJson.data.videolist) {
                mGridView.setVisibility(View.VISIBLE);
                mRefreshLayout.setVisibility(View.GONE);
                List<HomeVideoList> videoList = mHomeJson.data.videolist;
                mHeader.setHeaderData(mHomeJson.data);
                if (testUser()) {
                    mHeader.saveHeadData(mHomeJson.data);
                }
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
                if (TextUtils.isEmpty(customavatar)) {
                    customavatar = "http://pic.goluk.cn/cdcavatar/defaultavatar.png";
                }
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
                shareBean.from = getString(R.string.str_zhuge_share_video_network_other);

                ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, shareBean);
                shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
            } else {
                GolukUtils.showToast(this, this.getResources().getString(R.string.str_network_unavailable));
            }
        } else if (requestType == IPageNotifyFn.PageType_HomeAttention) {
            AttentionJson attention = (AttentionJson) result;

            if (attention != null) {
                //token过期
                if (!GolukUtils.isTokenValid(attention.code)) {
                    startUserLogin();
                    return;
                } else if (attention.code == 12011) {
                    Toast.makeText(NewUserCenterActivity.this, getString(R.string.follow_operation_limit_total), Toast.LENGTH_SHORT).show();
                    return;
                } else if (attention.code == 12016) {
                    Toast.makeText(NewUserCenterActivity.this, getString(R.string.follow_operation_limit_day), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (null != attention && 0 == attention.code && null != attention.data && null != mHeader
                    && null != mHomeJson && null != mHomeJson.data && null != mHomeJson.data.user) {
                // 0：未关注；1：关注；2：互相关注
                if (0 == attention.data.link) {
                    GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_cancle_ok));
                    mHomeJson.data.user.fans -= 1;
                } else if (1 == attention.data.link || 2 == attention.data.link) {
                    GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_ok));
                    mHomeJson.data.user.fans += 1;

                    //个人主页关注统计
                    ZhugeUtils.eventFollowed(this, this.getString(R.string.str_zhuge_followed_from_usercenter));

                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_usercenter_attention_cancle_ok));
                    mHomeJson.data.user.fans -= 1;
                }
                mHeader.changeAttentionState(attention.data.link);
                mHomeJson.data.user.link = attention.data.link;
                mHeader.setHeaderData(mHomeJson.data);
                if (testUser()) {
                    mHeader.saveHeadData(mHomeJson.data);
                }
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
            httpRequestData(mUserId, mCurrentUid, OPERATOR_FIRST, "");
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
     * 验证当前看的是自己的个人中心 还是别人的个人中心
     *
     * @return
     */
    public boolean testUser() {
        if (!isLoginSucess()) {
            return false;
        }
        UserInfo info = mBaseApp.getMyInfo();
        try {

            if (info != null && info.uid.equals(mUserId)) {
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
     * @param type
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
                //视频详情页访问
                ZhugeUtils.eventVideoDetail(this, this.getString(R.string.str_zhuge_followed_from_usercenter));

                Intent itVideoDetail = new Intent(this, VideoDetailActivity.class);
                itVideoDetail.putExtra(VideoDetailActivity.VIDEO_ID, video.videoid);
                startActivity(itVideoDetail);
            }
        }
    }

    @Override
    public void forbidBackKey(int backKey) {
        if (1 == backKey) {
            if (mLoadinDialog != null && mLoadinDialog.isShowing()) {
                mLoadinDialog.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != mSharePlatform) {
            mSharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }
}
