package com.mobnote.golukmain.videodetail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.eventbus.EventPraiseStatusChanged;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserLoginActivity;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.comment.CommentAddRequest;
import com.mobnote.golukmain.comment.CommentBean;
import com.mobnote.golukmain.comment.CommentDeleteRequest;
import com.mobnote.golukmain.comment.CommentListRequest;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.comment.bean.CommentAddBean;
import com.mobnote.golukmain.comment.bean.CommentAddResultBean;
import com.mobnote.golukmain.comment.bean.CommentDataBean;
import com.mobnote.golukmain.comment.bean.CommentDelResultBean;
import com.mobnote.golukmain.comment.bean.CommentItemBean;
import com.mobnote.golukmain.comment.bean.CommentResultBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.internation.login.InternationUserLoginActivity;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.LiveDialogManager.ILiveDialogManagerFn;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.praise.PraiseCancelRequest;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultBean;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultDataBean;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videoclick.NewestVideoClickRequest;
import com.mobnote.golukmain.videoshare.ShareVideoShortUrlRequest;
import com.mobnote.golukmain.videoshare.bean.VideoShareDataBean;
import com.mobnote.golukmain.videoshare.bean.VideoShareRetBean;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.golukmain.videosuqare.ZhugeParameterFn;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.umeng.socialize.utils.Log;
import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 精选
 *
 * @author mobnote
 */
public class VideoDetailActivity extends BaseActivity implements OnClickListener, OnRefreshListener,
        OnRTScrollListener, ICommentFn, TextWatcher, ILiveDialogManagerFn, OnItemClickListener, IRequestResultListener,
        EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener, OnLayoutChangeListener, ZhugeParameterFn {

    private static final String TAG = "VideoDetailActivity";
    private final static int DIALOG_TYPE_VIDEO_DELETED = 24;
    /**
     * 是否允许评论
     **/
    public static final String VIDEO_ISCAN_COMMENT = "iscan_input";
    public static final String TYPE = "type";
    /**
     * 视频id
     **/
    public static final String VIDEO_ID = "videoid";
    /**
     * 评论超时为10 秒
     */
    private final int COMMENT_CIMMIT_TIMEOUT = 10 * 1000;
    /**
     * 布局
     **/
    private ImageButton mImageBack = null;
    private TextView mTextTitle = null;
    private ImageView mImageRight = null;
    private TextView mTextSend = null;
    private EditText mEditInput = null;
    private RTPullListView mRTPullListView = null;
    private RelativeLayout mImageRefresh = null;
    public LinearLayout mCommentLayout = null;
    private ImageView mEmojiImg = null;
    private boolean isCanInput = true;
    /**
     * 评论
     **/
    private ArrayList<CommentBean> commentDataList = null;
    /**
     * 详情
     **/
    private VideoDetailRetBean mVideoDetailRetBean = null;
    private VideoDetailAdapter mAdapter = null;
    /**
     * 保存列表一个显示项索引
     */
    private int detailFirstVisible;
    /**
     * 保存列表显示item个数
     */
    private int detailVisibleCount;
    /**
     * 操作 (0:首次进入；1:下拉；2:上拉)
     */
    private int mCurrentOperator = 0;
    /**
     * 上拉刷新时，在ListView底部显示的布局
     */
    private RelativeLayout loading = null;
    /**
     * 最近更新时间
     */
    private String historyDate = "";
    /**
     * 是否还有分页
     */
    private boolean mIsHaveData = true;
    private SharePlatformUtil sharePlatform;
    /**
     * 保存将要删除的数据
     */
    private CommentBean mWillDelBean = null;
    /**
     * 专题id
     **/
    private String ztId = "";
    /**
     * videoid
     **/
    private String mVideoId = "";
    /**
     * 状态栏的高度
     */
    static int stateBraHeight = 0;
    private CustomLoadingDialog mLoadingDialog = null;
    private boolean clickRefresh = false;
    /**
     * 回调数据没有回来
     **/
    private boolean isClick = false;
    /**
     * false评论／false删除／true回复
     **/
    private boolean mIsReply = false;
    /**
     * 底部无评论的footer
     **/
    private View mNoDataView = null;
    /**
     * 禁止评论的footer
     **/
    private View mForbidCommentView = null;
    /**
     * 回复评论的dialog
     **/
    private ReplyDialog mReplyDialog = null;
    /**
     * 右侧操作按钮的dialog
     **/
    private DetailDialog mDetailDialog = null;

    private long mCommentTime = 0;
    /**
     * 判断是精选(0)还是最新(1)
     **/
    private int mType = 0;
    private VideoDetailHeader mHeader;
    private View mHeaderView;
    private String mTitleStr;

    private View activityRootView = null;
    private int screenHeight = 0;
    private int keyHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment);
        activityRootView = findViewById(R.id.all_layout);
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        keyHeight = screenHeight / 3;

        Intent intent = getIntent();
        if (savedInstanceState == null) {
            String type = intent.getStringExtra(TYPE);
            if ("Wonderful".equals(type)) {
                mType = 0;
                ztId = intent.getStringExtra("ztid");
            } else {
                mType = 1;
                mVideoId = intent.getStringExtra(VIDEO_ID);
                isCanInput = intent.getBooleanExtra(VIDEO_ISCAN_COMMENT, true);
            }
            mTitleStr = intent.getStringExtra("title");
            if (TextUtils.isEmpty(mTitleStr)) {
                mTitleStr = this.getString(R.string.str_videodetail_text);
            } else {
                if (mTitleStr.length() > 12) {
                    mTitleStr = mTitleStr.substring(0, 12) + this.getString(R.string.str_omit);
                }
            }
        } else {
            mType = savedInstanceState.getInt("Wonderful");
            mVideoId = savedInstanceState.getString(VIDEO_ID);
            ztId = savedInstanceState.getString("ztid");
            isCanInput = savedInstanceState.getBoolean(VIDEO_ISCAN_COMMENT, true);
        }
        commentDataList = new ArrayList<CommentBean>();
        initView();
        sharePlatform = new SharePlatformUtil(this);
        historyDate = GolukUtils.getCurrentFormatTime(this);
        initListener();
        getDetailData();
        init();
        observeSoftKeyboard();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("Wonderful", mType);
        if (mType == 0) {
            outState.putString("ztid", ztId);
        } else {
            outState.putString(VIDEO_ID, mVideoId);
            outState.putBoolean(VIDEO_ISCAN_COMMENT, isCanInput);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getStateHeight();
    }

    private void getStateHeight() {
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        stateBraHeight = rectangle.top;
        GolukDebugUtils.e("", "videoDetailActivity-----------------statusBarHeight:" + stateBraHeight);
    }

    @Override
    protected void onResume() {
        activityRootView.addOnLayoutChangeListener(this);
        mHeader.startPlayer();
        super.onResume();
        mBaseApp.setContext(this, "detailcomment");
    }

    private void initView() {
        mImageBack = (ImageButton) findViewById(R.id.comment_back);
        mTextTitle = (TextView) findViewById(R.id.comment_title);
        mImageRight = (ImageView) findViewById(R.id.comment_title_right);
        mTextSend = (TextView) findViewById(R.id.comment_send);
        mEditInput = (EditText) findViewById(R.id.comment_input);
        mRTPullListView = (RTPullListView) findViewById(R.id.commentRTPullListView);
        mImageRefresh = (RelativeLayout) findViewById(R.id.video_detail_click_refresh);
        mCommentLayout = (LinearLayout) findViewById(R.id.comment_layout);
        mEmojiImg = (ImageView) findViewById(R.id.emojicon);
        emoLayout = (FrameLayout) findViewById(R.id.emojiconsLayout);

        mImageRight.setImageResource(R.drawable.mine_icon_more_click);
        mTextTitle.setText(mTitleStr);
        mAdapter = new VideoDetailAdapter(this, 0);
        mHeader = new VideoDetailHeader(this, mType);
        mHeaderView = mHeader.createHeadView();
        mRTPullListView.addHeaderView(mHeaderView);
        mHeaderView.setVisibility(View.GONE);
        mRTPullListView.setAdapter(mAdapter);
    }

    private void initListener() {
        mImageBack.setOnClickListener(this);
        mImageRight.setOnClickListener(this);
        mTextSend.setOnClickListener(this);
        mImageRight.setOnClickListener(this);
        mEditInput.addTextChangedListener(this);
        mEmojiImg.setOnClickListener(this);
        mImageRefresh.setOnClickListener(this);

        mRTPullListView.setonRefreshListener(this);
        mRTPullListView.setOnRTScrollListener(this);
        mRTPullListView.setOnItemClickListener(this);
    }

    /**
     * 获取网络视频详情数据
     */
    private void getDetailData() {
        LiveDialogManager.getManagerInstance().setDialogManageFn(this);
        // 设置这个参数第一次进来会由下拉状态变为松开刷新的状态
        mRTPullListView.firstFreshState();
        boolean b = false;

        if (!UserUtils.isNetDeviceAvailable(this)) {
            mRTPullListView.setVisibility(View.GONE);
            mCommentLayout.setVisibility(View.GONE);
            mImageRefresh.setVisibility(View.VISIBLE);
            GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
            return;
        }

        if (mType == 0) {
            SingleVideoRequest request = new SingleVideoRequest(IPageNotifyFn.PageType_VideoDetail, this);
            b = request.get(ztId);
            GolukDebugUtils.e("", "----WonderfulActivity-----b====: " + b);
        } else {
            SingleDetailRequest request = new SingleDetailRequest(IPageNotifyFn.PageType_VideoDetail, this);
            b = request.get(mVideoId);
            GolukDebugUtils.e("", "----VideoDetailActivity-----b====: " + b);
        }
        if (!b) {
            mImageRefresh.setVisibility(View.VISIBLE);
        } else {
            if (clickRefresh)
                showLoadingDialog();
        }
    }

    // 是否允许评论
    private void permitInput() {
        if (!isCanInput) {
            mCommentLayout.setVisibility(View.GONE);
        } else {
            mCommentLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取评论列表数据
     */
    private void getCommentList(int operation, String timestamp) {
        String type = ICommentFn.COMMENT_TYPE_VIDEO;
        if (mType == 0) {
            type = ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO;
        }
        CommentListRequest request = new CommentListRequest(IPageNotifyFn.PageType_CommentList, this);
        request.get(mVideoDetailRetBean.data.avideo.video.videoid, type, operation, timestamp);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.comment_back) {
            if (GolukUtils.isFastDoubleClick()) {
                return;
            }
            removeAllMessage();
            finish();
        } else if (id == R.id.comment_title_right) {
            if (!isClick) {
                return;
            }
            if (null == mVideoDetailRetBean) {
                if (!UserUtils.isNetDeviceAvailable(this)) {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
                    return;
                }
            }
            mDetailDialog = new DetailDialog(this, mVideoDetailRetBean, testUser());
            mDetailDialog.show();
        } else if (id == R.id.comment_send) {
            if (!UserUtils.isNetDeviceAvailable(this)) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.user_net_unavailable));
                return;
            }
            if (!isClick) {
                return;
            }
            // 隐藏键盘
            UserUtils.hideSoftMethod(this);
            this.hideEmojocon();
            setSwitchState(true);
            click_send();
        } else if (id == R.id.video_detail_click_refresh) {
            clickRefresh = true;
            getDetailData();
        } else if (id == R.id.emojicon) {
            click_switchInput();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            int count = mRTPullListView.getAdapter().getCount();
            int visibleCount = detailFirstVisible + detailVisibleCount;

            GolukDebugUtils.e("", "----VideoDetailActivity-----onScrollStateChanged-----222: " + count + "  vicount:"
                    + visibleCount + "  mIsHaveData===" + mIsHaveData);
            if (null != mAdapter) {
                mHeader.scrollDealPlayer();
            }

            if (count == visibleCount && mIsHaveData) {
                if (null != mVideoDetailRetBean && null != mVideoDetailRetBean.data && null != mVideoDetailRetBean.data.avideo
                        && null != mVideoDetailRetBean.data.avideo.video && null != mVideoDetailRetBean.data.avideo.video.videoid) {
                    startPush();
                }
            }
            if ((count == visibleCount) && (count > 20) && !mIsHaveData) {
                GolukUtils.showToast(this,
                        this.getResources().getString(R.string.str_pull_refresh_listview_bottom_reach));
            }
        } else if (OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState) {
            if (!mInputState) {
                this.hideEmojocon();
                this.setSwitchState(true);
            }
        }
    }

    @Override
    public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
        detailFirstVisible = firstVisibleItem;
        detailVisibleCount = visibleItemCount;
    }

    /*
     *
     * 首次进入，数据回调处理 之调用视频详情的借口
     */
    private void firstEnterCallBack(int count, VideoDetailRetBean videoDetailRetBeanData, ArrayList<CommentBean> dataList) {
        // 首次进入
        this.mAdapter.setData(dataList);
        mRTPullListView.onRefreshComplete(getLastRefreshTime());
        if (count >= PAGE_SIZE) {
            mIsHaveData = true;
            addFoot();
        } else {
            mIsHaveData = false;
            this.removeFoot();
        }
    }

    // 下拉刷新，数据回调处理
    private void pullCallBack(int count, VideoDetailRetBean videoDetailRetBeanData, ArrayList<CommentBean> dataList) {
        this.mAdapter.setData(dataList);
        mRTPullListView.onRefreshComplete(getLastRefreshTime());
        if (count >= PAGE_SIZE) {
            mIsHaveData = true;
            addFoot();
        } else {
            mIsHaveData = false;
            this.removeFoot();
        }

    }

    // 上拉刷新，数据回调处理
    private void pushCallBack(int count, VideoDetailRetBean videoDetailRetBeanData, ArrayList<CommentBean> dataList) {
        if (count >= PAGE_SIZE) {
            mIsHaveData = true;
        } else {
            mIsHaveData = false;
            this.removeFoot();
        }
        this.mAdapter.appendData(dataList);
    }

    @Override
    public void onRefresh() {
        startPull();
    }

    // 开始下拉刷新
    private void startPull() {
        if (mCurrentOperator == OPERATOR_UP) {
            mRTPullListView.onRefreshComplete(getLastRefreshTime());
            return;
        }
        mCurrentOperator = OPERATOR_DOWN;
        getDetailData();
    }

    // 开始上拉刷新
    private void startPush() {
        if (mCurrentOperator != OPERATOR_NONE) {
            return;
        }
        mCurrentOperator = OPERATOR_UP;
        getCommentList(OPERATOR_DOWN, mAdapter.getLastDataTime());
    }

    // 发表评论
    private void click_send() {
        // 发评论／回复 前需要先判断用户是否登录
        if (!mBaseApp.isUserLoginSucess) {
            Intent intent = null;
            if (GolukApplication.getInstance().isMainland() == false) {
                intent = new Intent(this, InternationUserLoginActivity.class);
            } else {
                intent = new Intent(this, UserLoginActivity.class);
            }
            intent.putExtra("isInfo", "back");
            startActivity(intent);
            return;
        }
        UserInfo loginUser = mBaseApp.getMyInfo();
        if (null != mWillDelBean && loginUser.uid.equals(mWillDelBean.mUserId) && mIsReply) {
            mIsReply = false;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - mCommentTime < COMMENT_CIMMIT_TIMEOUT) {
            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
                    LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
                    this.getResources().getString(R.string.comment_sofast_text));
            return;
        }

        final String content = mEditInput.getText().toString().trim();
        if (null == content || "".equals(content)) {
            GolukUtils.showToast(this, this.getString(R.string.str_input_comment_content));
            return;
        }

        GolukDebugUtils.e("", "client  send---comment: " + content);

        httpPost_requestAdd(content);
    }

    // 删除评论
    void httpPost_requestDel(String id) {
        CommentDeleteRequest request = new CommentDeleteRequest(IPageNotifyFn.PageType_DelComment, this);
        boolean isSucess = request.get(id);
        if (!isSucess) {
            // 失败
            GolukUtils.showToast(this, this.getString(R.string.str_delete_fail));
            return;
        }
        LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
                LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", this.getString(R.string.str_delete_ongoing),
                true);
    }

    // 添加评论
    private void httpPost_requestAdd(String txt) {
        if (null == mVideoDetailRetBean.data.avideo.video.videoid) {
            GolukUtils.showToast(this, this.getString(R.string.str_load_data_ongoing));
            return;
        }
        String type = ICommentFn.COMMENT_TYPE_VIDEO;
        if (mType == 0) {
            type = ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO;
        }
        CommentAddRequest request = new CommentAddRequest(IPageNotifyFn.PageType_AddComment, this);
        boolean isSucess = false;
        if (mIsReply) {
            isSucess = request.get(mVideoDetailRetBean.data.avideo.video.videoid, type, txt, mWillDelBean.mUserId,
                    mWillDelBean.mUserName, ztId);
        } else {
            isSucess = request.get(mVideoDetailRetBean.data.avideo.video.videoid, type, txt, "", "", ztId);
        }
        if (!isSucess) {
            // 失败
            GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
            return;
        }
        LiveDialogManager.getManagerInstance().showCommProgressDialog(this,
                LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", this.getString(R.string.str_comment_ongoing), true);
    }

    // 点赞请求
    public boolean sendPraiseRequest() {
        PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
        return request.get("1", mVideoDetailRetBean.data.avideo.video.videoid, "1");
    }

    // 取消点赞请求
    public boolean sendCancelPraiseRequest() {
        PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
        return request.get("1", mVideoDetailRetBean.data.avideo.video.videoid);
    }

    // 异常情况处理
    private void dealCondition() {
        isClick = false;
        mEditInput.clearFocus();
        mEditInput.setFocusable(false);
        mRTPullListView.setVisibility(View.GONE);
        mImageRefresh.setVisibility(View.VISIBLE);
        GolukUtils.showToast(this, this.getString(R.string.str_network_connect_outtime));
    }

    // 数据异常判断
    private boolean isHasData() {
        if (null == mVideoDetailRetBean || null == mVideoDetailRetBean.data || null == mVideoDetailRetBean.data.avideo) {
            return false;
        }
        return true;
    }

    // 分享描述信息
    private String shareDescribe(String describe) {
        String allDescribe = "";
        if (!isHasData()) {
            return allDescribe;
        }
        if (null != mVideoDetailRetBean.data.avideo.user && null != mVideoDetailRetBean.data.avideo.video) {
            if (TextUtils.isEmpty(describe)) {
                allDescribe = mVideoDetailRetBean.data.avideo.user.nickname + "：" + mVideoDetailRetBean.data.avideo.video.describe;
            } else {
                allDescribe = mVideoDetailRetBean.data.avideo.user.nickname + "：" + describe;
            }
        }
        return allDescribe;
    }

    private void switchSendState(boolean isSend) {
        if (isSend) {
            mTextSend.setTextColor(this.getResources().getColor(R.color.color_comment_can_send));
        } else {
            mTextSend.setTextColor(this.getResources().getColor(R.color.color_comment_not_send));
        }
    }

    private void updateRefreshTime() {
        historyDate = GolukUtils.getCurrentFormatTime(this);
    }

    private String getLastRefreshTime() {
        return historyDate;
    }

    /**
     * 添加上拉底部的loading View
     *
     * @author jyf
     * @date 2015年8月12日
     */
    private void addFoot() {
        if (mRTPullListView.getFooterViewsCount() > 0) {
            return;
        }
        loading = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_square_below_loading, null);
        mRTPullListView.addFooterView(loading);
    }

    private void removeFoot() {
        if (loading != null) {
            if (mRTPullListView != null) {
                mRTPullListView.removeFooterView(loading);
            }
        }
    }

    private void exit() {
        removeAllMessage();
        LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
        if (null != mReplyDialog) {
            mReplyDialog.dismiss();
        }
        if (null != mDetailDialog) {
            mDetailDialog.dismiss();
        }
        mIsReply = false;
        mHeader.exit();
    }

    private void removeAllMessage() {
        if (null != mBaseHandler) {
            mBaseHandler.removeMessages(100);
            mBaseHandler.removeMessages(200);
            mBaseHandler.removeMessages(300);
        }
    }

    private void callBackFailed() {
        if (mCurrentOperator == OPERATOR_FIRST || mCurrentOperator == OPERATOR_DOWN) {
            mRTPullListView.onRefreshComplete(getLastRefreshTime());
        } else if (mCurrentOperator == OPERATOR_UP) {
            // 上拉刷新
            removeFoot();
            GolukUtils.showToast(this, this.getString(R.string.str_network_unavailable));
        }

    }

    private void noDataDeal() {
        mIsHaveData = false;
        if (mCurrentOperator == OPERATOR_UP) {// 上拉刷新
            removeFoot();
        } else if (mCurrentOperator == OPERATOR_FIRST || OPERATOR_DOWN == mCurrentOperator) {// 下拉刷新
            mRTPullListView.onRefreshComplete(getLastRefreshTime());
        }
    }

    @Override
    protected void onPause() {
        if (mHeader != null) {
            mHeader.pausePlayer();
        }
        super.onPause();
    }

    @Override
    public void afterTextChanged(Editable arg0) {
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        final String txt = mEditInput.getText().toString().trim();
        if (null != txt && txt.length() > 0) {
            switchSendState(true);
        } else {
            switchSendState(false);
        }
    }

    /**
     * 点击删除或者回复评论
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        GolukDebugUtils.e("", "----commentActivity--------position:" + position + "   arg3:" + arg3);
        try {
            mWillDelBean = (CommentBean) parent.getAdapter().getItem(position);
            if (null != mWillDelBean) {
                if (this.mBaseApp.isUserLoginSucess) {
                    UserInfo loginUser = mBaseApp.getMyInfo();
                    GolukDebugUtils.e("", "-----commentActivity--------mUserId:" + mWillDelBean.mUserId);
                    GolukDebugUtils.e("", "-----commentActivity--------uid:" + loginUser.uid);
                    if (loginUser.uid.equals(mWillDelBean.mUserId)) {
                        mIsReply = false;
                    } else {
                        mIsReply = true;
                    }
                } else {
                    mIsReply = true;
                }
                mReplyDialog = new ReplyDialog(this, mWillDelBean, mEditInput, mIsReply);
                mReplyDialog.show();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // 获得当前得到焦点的View
            View v = mCommentLayout;
            if (UserUtils.isShouldHideInput(v, ev)) {
                UserUtils.hideSoftMethod(this);
                if ("".equals(mEditInput.getText().toString().trim()) && mIsReply) {
                    mEditInput.setHint(this.getString(R.string.video_play_comment_text));
                    mIsReply = false;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != sharePlatform) {
            sharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void dialogManagerCallBack(int dialogType, int function, String data) {
        if (LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT == dialogType) {
            if (LiveDialogManager.FUNCTION_DIALOG_CANCEL == function) {

            }
        } else if (LiveDialogManager.DIALOG_TYPE_COMMENT_DELETE == dialogType) {
            if (LiveDialogManager.FUNCTION_DIALOG_OK == function) {
                httpPost_requestDel(mWillDelBean.mCommentId);
            } else {
                mWillDelBean = null;
            }
        } else if (LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE == dialogType) {
            // 取消删除
        } else if (dialogType == DIALOG_TYPE_VIDEO_DELETED) {
            finish();
        } else if (LiveDialogManager.DIALOG_TYPE_DEL_VIDEO == dialogType) {
            // 取消删除
            LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
        }
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new CustomLoadingDialog(this, null);
            mLoadingDialog.show();
        }
    }

    private void closeLoadingDialog() {
        if (null != mLoadingDialog) {
            mLoadingDialog.close();
            mLoadingDialog = null;
        }
    }

    private boolean isCanShowSoft() {
        if (null == mVideoDetailRetBean || null == mVideoDetailRetBean.data || null == mVideoDetailRetBean.data.avideo) {
            return false;
        }
        if ((mVideoDetailRetBean.data.avideo.video != null) && (mVideoDetailRetBean.data.avideo.video.comment != null)
                && "1".equals(mVideoDetailRetBean.data.avideo.video.comment.iscomment) && isSwitchStateFinish) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (View.GONE != emoLayout.getVisibility()) {
                this.hideEmojocon();
                setSwitchState(true);
                return true;
            }
            if (GolukUtils.isFastDoubleClick()) {
                return true;
            }
            removeAllMessage();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        exit();
        super.onDestroy();
    }

    private void addFooterView() {
        if (null == mNoDataView) {
            mNoDataView = LayoutInflater.from(this).inflate(R.layout.video_detail_footer, null);
            mRTPullListView.addFooterView(mNoDataView);
            mNoDataView.setVisibility(View.VISIBLE);
        }
    }

    private void removeFooterView() {
        if (null != mRTPullListView && null != mNoDataView) {
            mRTPullListView.removeFooterView(mNoDataView);
            mNoDataView.setVisibility(View.GONE);
            mNoDataView = null;
        }
    }

    private void addForbitCommentFooterView() {
        if (null == mForbidCommentView) {
            mForbidCommentView = LayoutInflater.from(this).inflate(R.layout.video_detail_forbit_comment_footer, null);
            mRTPullListView.addFooterView(mForbidCommentView);
        }
    }

    private void removeForbitCommentFooterView() {
        if (null != mRTPullListView && null != mForbidCommentView) {
            mRTPullListView.removeFooterView(mForbidCommentView);
            mForbidCommentView = null;
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

    private boolean testUser() {
        if (!isLoginSucess()) {
            return false;
        }
        UserInfo info = mBaseApp.getMyInfo();
        if (null == info) {
            return false;
        }
        if (mVideoDetailRetBean.data.avideo.user.uid.equals(info.uid)) {
            return true;
        } else {
            return false;
        }

    }

    void delVideo() {
        showDelDialog();
    }

    /**
     * 删除用户自己发布的视频
     *
     * @author jyf
     */
    private void deleteVideo() {
        if (!UserUtils.isNetDeviceAvailable(this)) {
            GolukUtils.showToast(this, getResources().getString(R.string.user_net_unavailable));
            return;
        }
        if (null == mVideoDetailRetBean || mVideoDetailRetBean.success == false || null == mVideoDetailRetBean.data
                || null == mVideoDetailRetBean.data.avideo || null == mVideoDetailRetBean.data.avideo.video
                || null == mVideoDetailRetBean.data.avideo.video.videoid || null == mVideoDetailRetBean.data.avideo.user
                || null == mVideoDetailRetBean.data.avideo.user.uid) {
            return;
        }
        DeleteVideoRequest request = new DeleteVideoRequest(IPageNotifyFn.PageType_DeleteVideo, this);
        boolean isSuccess = request.get(mVideoDetailRetBean.data.avideo.video.videoid, mVideoDetailRetBean.data.avideo.user.uid);
        if (isSuccess) {
            LiveDialogManager.getManagerInstance()
                    .showCommProgressDialog(this, LiveDialogManager.DIALOG_TYPE_DEL_VIDEO, "",
                            getString(R.string.str_delete_ongoing_with_omit), true);
        } else {
            GolukUtils.showToast(this, this.getString(R.string.str_delete_video_fail));
        }
    }

    void getShare() {
        if (GolukUtils.isFastDoubleClick()) {
            return;
        }
        if (mVideoDetailRetBean == null || mVideoDetailRetBean.data == null
                || mVideoDetailRetBean.data.avideo == null
                || mVideoDetailRetBean.data.avideo.video == null) {
            return;
        }
        if (!SharePlatformUtil.checkShareableWhenNotHotspot(VideoDetailActivity.this)) return;
        showLoadingDialog();
        ShareVideoShortUrlRequest request = new ShareVideoShortUrlRequest(IPageNotifyFn.PageType_GetShareURL, this);
        boolean result = request.get(mVideoDetailRetBean.data.avideo.video.videoid, mVideoDetailRetBean.data.avideo.video.type);
        GolukDebugUtils.i("detail", "--------result-----Onclick------" + result);
        if (!result) {
            GolukUtils.showToast(this, this.getString(R.string.network_error));
        }
    }

    /**
     * 视频围观数上报
     */
    private void clickVideoNumber() {
        try {
            if (null != mVideoDetailRetBean && null != mVideoDetailRetBean.data && null != mVideoDetailRetBean.data.avideo
                    && null != mVideoDetailRetBean.data.avideo.video && null != mVideoDetailRetBean.data.avideo.video.videoid) {
                NewestVideoClickRequest videoClickRequest = new NewestVideoClickRequest(
                        IPageNotifyFn.PageType_VideoClick, this);
                JSONArray array = new JSONArray();
                JSONObject jsonVideo = new JSONObject();
                jsonVideo.put("videoid", mVideoDetailRetBean.data.avideo.video.videoid);
                jsonVideo.put("number", "1");
                array.put(jsonVideo);
                if (null != array) {
                    videoClickRequest.get("100", "1", array.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        closeLoadingDialog();
        switch (requestType) {
            case IPageNotifyFn.PageType_VideoDetail:
                mVideoDetailRetBean = (VideoDetailRetBean) result;
                if (mVideoDetailRetBean != null && mVideoDetailRetBean.success) {
                    mRTPullListView.setVisibility(View.VISIBLE);
                    mCommentLayout.setVisibility(View.VISIBLE);
                    mImageRefresh.setVisibility(View.GONE);
                    if (null != mVideoDetailRetBean.data) {
                        if ("4".equals(mVideoDetailRetBean.data.result)) {
                            mRTPullListView.setVisibility(View.GONE);
                            mCommentLayout.setVisibility(View.GONE);
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this, DIALOG_TYPE_VIDEO_DELETED, "",
                                    this.getResources().getString(R.string.str_video_removed));
                            return;
                        } else if ("3".equals(mVideoDetailRetBean.data.result)) {
                            mRTPullListView.setVisibility(View.GONE);
                            mCommentLayout.setVisibility(View.GONE);
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this, DIALOG_TYPE_VIDEO_DELETED, "",
                                    this.getResources().getString(R.string.str_video_not_exist));
                            return;
                        }
                    }
                    permitInput();
                    isClick = true;
                    mEditInput.setFocusable(true);
                    updateRefreshTime();

                    mHeader.setData(mVideoDetailRetBean);
                    //观看视频统计
                    playVideoStatistics(mVideoDetailRetBean);

                    VideoDetailDataBean videoDetailDataBean = mVideoDetailRetBean.data;
                    if (mVideoDetailRetBean.data == null) {
                        mCurrentOperator = OPERATOR_NONE;
                        return;
                    }

                    ZTHead ztHead = videoDetailDataBean.head;
                    VideoDetailAvideoBean avideo = videoDetailDataBean.avideo;

                    if (avideo == null) {
                        mCurrentOperator = OPERATOR_NONE;
                        return;
                    }

                    VideoInfo videoInfo = avideo.video;
                    if (videoInfo == null) {
                        mCurrentOperator = OPERATOR_NONE;
                        return;
                    }

                    if (OPERATOR_FIRST == mCurrentOperator) {
                        // 首次进入
                        mHeader.getHeadData(true);
                        if (null != ztHead) {
                            this.mTextTitle.setText(ztHead.ztitle);
                        }

                    } else if (OPERATOR_DOWN == mCurrentOperator) {
                        // 下拉刷新
                        mHeader.getHeadData(false);
                    }
                    if (VideoInfo.VIDEO_TYPE_LIVE.equals(videoDetailDataBean.avideo.video.type)){
                        this.mTextTitle.setText(R.string.str_live_play_back);
                    }
                    mHeaderView.setVisibility(View.VISIBLE);
                    VideoCommentInfo commentInfo = videoInfo.comment;

                    if (commentInfo != null) {
                        if ("0".equals(mVideoDetailRetBean.data.avideo.video.comment.iscomment)) {
                            removeFooterView();
                            mCommentLayout.setVisibility(View.GONE);
                            addForbitCommentFooterView();
                            mRTPullListView.setEnabled(false);
                            mRTPullListView.onRefreshComplete(getLastRefreshTime());
                            mCurrentOperator = OPERATOR_NONE;
                            return;
                        } else {
                            removeForbitCommentFooterView();
                            mRTPullListView.setEnabled(true);
                            mCommentLayout.setVisibility(View.VISIBLE);
                        }
                        int count = 0;
                        if (!TextUtils.isEmpty(commentInfo.comcount) && TextUtils.isDigitsOnly(commentInfo.comcount)) {
                            count = Integer.parseInt(commentInfo.comcount);
                        }
                        if (count == 0) {
                            mIsHaveData = false;
                            mRTPullListView.onRefreshComplete(getLastRefreshTime());
                            addFooterView();
                            mCurrentOperator = OPERATOR_NONE;
                        } else {
                            removeFooterView();
                            getCommentList(OPERATOR_FIRST, "");
                        }
                        this.setOnTouchListener();

                    }
                    clickVideoNumber();
                } else if (mVideoDetailRetBean != null && !mVideoDetailRetBean.success) {
                    mRTPullListView.setVisibility(View.GONE);
                    mCommentLayout.setVisibility(View.GONE);
                    if (null != mVideoDetailRetBean.data) {
                        if ("4".equals(mVideoDetailRetBean.data.result)) {
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this, DIALOG_TYPE_VIDEO_DELETED, "",
                                    this.getResources().getString(R.string.str_video_removed));
                        } else if ("3".equals(mVideoDetailRetBean.data.result)) {
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this, DIALOG_TYPE_VIDEO_DELETED, "",
                                    this.getResources().getString(R.string.str_video_not_exist));
                        } else {
                            dealCondition();
                            mCurrentOperator = OPERATOR_NONE;
                        }
                    }
                } else {
                    dealCondition();
                    mCurrentOperator = OPERATOR_NONE;
                }
                break;
            case IPageNotifyFn.PageType_CommentList:

                CommentResultBean resultBean = (CommentResultBean) result;
                if (resultBean != null && resultBean.success && resultBean.data != null) {
                    CommentDataBean dataBean = resultBean.data;
                    int count = 0;
                    if (!TextUtils.isEmpty(dataBean.count) && TextUtils.isDigitsOnly(dataBean.count)) {
                        count = Integer.parseInt(dataBean.count);
                    }

                    if (null == dataBean.comments || dataBean.comments.size() <= 0) {
                        // 无数据
                        noDataDeal();
                        mCurrentOperator = OPERATOR_NONE;
                        return;
                    }

                    // 有数据
                    commentDataList.clear();
                    for (CommentItemBean item : dataBean.comments) {
                        CommentBean bean = new CommentBean();
                        bean.mSeq = item.seq;
                        bean.mCommentId = item.commentId;
                        bean.mCommentTime = item.time;
                        bean.mCommentTxt = item.text;
                        if (item.reply != null) {
                            bean.mReplyId = item.reply.id;
                            bean.mReplyName = item.reply.name;
                        }
                        if (item.author != null) {
                            bean.customavatar = item.author.customavatar;
                            bean.mUserHead = item.author.avatar;
                            bean.mUserId = item.author.authorid;
                            bean.mUserName = item.author.name;
                            if (item.author.label != null) {
                                bean.mApprove = item.author.label.approve;
                                bean.mApprovelabel = item.author.label.approvelabel;
                                bean.mHeadplusv = item.author.label.headplusv;
                                bean.mHeadplusvdes = item.author.label.headplusvdes;
                                bean.mTarento = item.author.label.tarento;
                            }
                        }
                        commentDataList.add(bean);
                    }

                    updateRefreshTime();

                    if (OPERATOR_FIRST == mCurrentOperator) {
                        // 首次进入
                        firstEnterCallBack(count, mVideoDetailRetBean, commentDataList);
                    } else if (OPERATOR_UP == mCurrentOperator) {
                        // 上拉刷新
                        GolukDebugUtils.e("newadapter", "================VideoDetailActivity：commentDataList=="
                                + commentDataList.size());
                        pushCallBack(count, mVideoDetailRetBean, commentDataList);
                    } else {
                        // 下拉刷新
                        pullCallBack(count, mVideoDetailRetBean, commentDataList);
                    }
                } else {
                    callBackFailed();
                }
                mCurrentOperator = OPERATOR_NONE;
                break;
            case IPageNotifyFn.PageType_DelComment:
                LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
                CommentDelResultBean DelResultBean = (CommentDelResultBean) result;

                if (DelResultBean != null && DelResultBean.data != null) {
                    if (!GolukUtils.isTokenValid(DelResultBean.data.result)) {
                        startUserLogin();
                        return;
                    }
                }
                if (null != DelResultBean && DelResultBean.success) {

                    mAdapter.deleteData(mWillDelBean);
                    mVideoDetailRetBean.data.avideo.video.comment.comcount = String.valueOf(Integer
                            .parseInt(mVideoDetailRetBean.data.avideo.video.comment.comcount) - 1);
                    mHeader.setCommentCount(mVideoDetailRetBean.data.avideo.video.comment.comcount);
                    GolukUtils.showToast(this, this.getString(R.string.str_delete_success));

                    if (mAdapter.getCount() < 1) {
                        addFooterView();
                    } else {
                        removeFooterView();
                    }
                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_delete_fail));
                }
                mWillDelBean = null;
                break;
            case IPageNotifyFn.PageType_AddComment:
                LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
                CommentAddResultBean addResultBean = (CommentAddResultBean) result;

                if (addResultBean != null && addResultBean.data != null) {
                    if (!GolukUtils.isTokenValid(addResultBean.data.result)) {
                        startUserLogin();
                        return;
                    }
                }

                if (null != addResultBean && addResultBean.success) {
                    CommentAddBean addBean = addResultBean.data;
                    if (addBean == null) {
                        GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
                        return;
                    }
                    CommentBean bean = new CommentBean();
                    bean.customavatar = addBean.customavatar;
                    bean.mReplyId = addBean.replyid;
                    bean.mReplyName = addBean.replyname;
                    bean.mSeq = addBean.seq;
                    bean.result = addBean.result;
                    bean.mCommentTime = addBean.time;
                    bean.mCommentTxt = String.valueOf(addBean.text);
                    bean.mCommentId = addBean.commentid;
                    bean.mUserHead = addBean.authoravatar;
                    bean.mUserId = addBean.authorid;
                    bean.mUserName = addBean.authorname;
                    if (addBean.label != null) {
                        bean.mApprove = addBean.label.approve;
                        bean.mApprovelabel = addBean.label.approvelabel;
                        bean.mHeadplusv = addBean.label.headplusv;
                        bean.mHeadplusvdes = addBean.label.headplusvdes;
                        bean.mTarento = addBean.label.tarento;
                    }

                    bean.mCommentTime = GolukUtils.getCurrentCommentTime();
                    if (!"".equals(bean.result)) {
                        if ("0".equals(bean.result)) {// 成功
                            //评论视频
                            ZhugeUtils.eventCommentVideo(this);
                            removeFooterView();
                            this.mAdapter.addFirstData(bean);
                            mVideoDetailRetBean.data.avideo.video.comment.comcount = String.valueOf(Integer
                                    .parseInt(mVideoDetailRetBean.data.avideo.video.comment.comcount) + 1);
                            mHeader.setCommentCount(mVideoDetailRetBean.data.avideo.video.comment.comcount);
                            mEditInput.setText("");
                            switchSendState(false);
                            // 回复完评论之后需要还原状态以判断下次是评论还是回复
                            mIsReply = false;
                            mEditInput.setHint(this.getString(R.string.video_play_comment_text));
                            mCommentTime = System.currentTimeMillis();
                        } else if ("1".equals(bean.result)) {
                            GolukDebugUtils.e("", "参数错误");
                        } else if ("2".equals(bean.result)) {// 重复评论
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
                                    LiveDialogManager.FUNCTION_DIALOG_OK, "",
                                    this.getResources().getString(R.string.comment_repeat_text));
                        } else if ("3".equals(bean.result)) {// 频繁评论
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
                                    LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
                                    this.getResources().getString(R.string.comment_sofast_text));
                        } else {
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(this,
                                    LiveDialogManager.FUNCTION_DIALOG_OK, "",
                                    this.getString(R.string.str_save_comment_fail));
                        }
                    }

                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_comment_fail));
                }
                break;
            case IPageNotifyFn.PageType_GetShareURL:
                closeLoadingDialog();
                if (!isHasData()) {
                    return;
                }
                VideoShareRetBean shareVideoResultBean = (VideoShareRetBean) result;
                if (shareVideoResultBean != null && shareVideoResultBean.success && shareVideoResultBean.data != null) {
                    VideoShareDataBean shareVideoBean = shareVideoResultBean.data;
                    String shareurl = shareVideoBean.shorturl;
                    String coverurl = shareVideoBean.coverurl;
                    String describe = shareVideoBean.describe;
                    String realDesc = this.getString(R.string.str_share_board_real_desc);
                    String allDescribe = shareDescribe(describe);
                    String ttl = this.getString(R.string.str_video_edit_share_title);
                    Bitmap bitmap = null;
                    if (!this.isFinishing()) {
                        if (null != mVideoDetailRetBean.data.avideo.video) {
                            ThirdShareBean shareBean = new ThirdShareBean();
                            shareBean.surl = shareurl;
                            shareBean.curl = coverurl;
                            if(TextUtils.isEmpty(shareBean.curl)){
                                shareBean.curl = mHeader.getVideoThumbnailURL();
                            }
                            shareBean.db = allDescribe;
                            shareBean.tl = ttl;
                            shareBean.bitmap = bitmap;
                            shareBean.realDesc = realDesc;
                            shareBean.videoId = mVideoDetailRetBean.data.avideo.video.videoid;
                            shareBean.from = this.getString(R.string.str_zhuge_video_detail);
                            ProxyThirdShare shareBoard = new ProxyThirdShare(this, sharePlatform, shareBean);
                            shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                        }
                    }

                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_network_connect_outtime));
                }
                break;
            case IPageNotifyFn.PageType_Praise:
                PraiseResultBean praiseResultBean = (PraiseResultBean) result;
                if (praiseResultBean == null || !praiseResultBean.success) {
                    GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
                    return;
                }

                PraiseResultDataBean ret = praiseResultBean.data;
                if (null != ret && !TextUtils.isEmpty(ret.result)) {
                    if ("0".equals(ret.result)) {
                        //详情页--视频点赞
                        ZhugeUtils.eventPraiseVideo(this, this.getString(R.string.str_zhuge_video_detail));
                        EventBus.getDefault().post(
                                new EventPraiseStatusChanged(EventConfig.PRAISE_STATUS_CHANGE, mVideoId, true));
                    } else if ("7".equals(ret.result)) {
                        GolukUtils.showToast(this, this.getString(R.string.str_no_duplicated_praise));
                    } else {
                        GolukUtils.showToast(this, this.getString(R.string.str_praise_failed));
                    }
                }
                break;
            case IPageNotifyFn.PageType_PraiseCancel:
                PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
                if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
                    GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
                    return;
                }

                PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
                if (null != cancelRet && !TextUtils.isEmpty(cancelRet.result)) {
                    if ("0".equals(cancelRet.result)) {
                        EventBus.getDefault().post(
                                new EventPraiseStatusChanged(EventConfig.PRAISE_STATUS_CHANGE, mVideoId, false));
                    } else {
                        GolukUtils.showToast(this, this.getString(R.string.str_cancel_praise_failed));
                    }
                }
                break;
            case IPageNotifyFn.PageType_VideoClick:
                break;

            case IPageNotifyFn.PageType_DeleteVideo:
                LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
                DeleteJson deleteVideo = (DeleteJson) result;
                if (null != deleteVideo && null != deleteVideo.data) {
                    String resultCode = deleteVideo.data.result;
                    if (!GolukUtils.isTokenValid(resultCode)) {
                        startUserLogin();
                        return;
                    }
                    if ("0".equals(resultCode) && deleteVideo.success) {
                        GolukUtils.showToast(this, this.getString(R.string.str_delete_success));
                        // 通知其它界面，数据更新
                        String vid = "";
                        if (null == mVideoDetailRetBean || mVideoDetailRetBean.success == false || null == mVideoDetailRetBean.data
                                || null == mVideoDetailRetBean.data.avideo || null == mVideoDetailRetBean.data.avideo.video
                                || null == mVideoDetailRetBean.data.avideo.video.videoid) {

                        } else {
                            vid = mVideoDetailRetBean.data.avideo.video.videoid;
                        }
                        EventBus.getDefault().post(new EventDeleteVideo(EventConfig.VIDEO_DELETE, vid));
                        exit();
                        finish();
                    } else {
                        GolukUtils.showToast(this, this.getString(R.string.str_delete_video_fail));
                    }

                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_delete_video_fail));
                }
                break;
            default:
                break;
        }
    }

    private void setOnTouchListener() {
        mEditInput.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (MotionEvent.ACTION_DOWN == arg1.getAction()) {
                    click_soft();
                }
                return false;
            }
        });
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

    private void showDelDialog() {
        new AlertDialog.Builder(this).setTitle(this.getString(R.string.str_delete_video_title))
                .setMessage(this.getString(R.string.str_delete_video_message))
                .setPositiveButton(this.getString(R.string.delete_text), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        deleteVideo();
                    }
                }).setNegativeButton(this.getString(R.string.user_cancle), null).create().show();
    }

    @Override
    protected void hMessage(Message msg) {
        if (100 == msg.what) {
            showEmojocon();
            setSwitchState(false);
            isSwitchStateFinish = true;
        } else if (200 == msg.what) {
            setSwitchState(true);
            GolukUtils.showSoftNotThread(mEditInput);
            mBaseHandler.sendEmptyMessageDelayed(300, 800);
        } else if (300 == msg.what) {
            hideEmojocon();
            this.setResize();
            isSwitchStateFinish = true;
        }
    }

    /**
     * 标志一个状态是否切换完成
     */
    private boolean isSwitchStateFinish = true;

    private void click_soft() {
        if (!this.isCanShowSoft()) {
            return;
        }
        isSwitchStateFinish = false;
        mEditInput.setFocusable(true);
        mEditInput.requestFocus();

        if (!GolukUtils.isSettingBoardHeight()) {
            this.hideEmojocon();
        }

        if (emoLayout.getVisibility() == View.GONE) {
            this.setResize();
        } else {
            setInputAdJust();
        }
        mBaseHandler.sendEmptyMessageDelayed(200, 80);
    }

    // 点击“显示 表情”
    private void click_Emojocon() {
        if (!isCanShowSoft()) {
            return;
        }
        isSwitchStateFinish = false;
        GolukUtils.hideSoft(this, mEditInput);
        mBaseHandler.sendEmptyMessageDelayed(100, 80);
    }

    /**
     * 表情布局
     */
    private FrameLayout emoLayout = null;
    /**
     * true:键盘输入状态, false: 表情输入状态
     */
    private boolean mInputState = true;

    private void setInputAdJust() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private void setResize() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void observeSoftKeyboard() {
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                GolukUtils.setKeyBoardHeight(height);
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });
    }

    private void setLayoutHeight() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) emoLayout.getLayoutParams();
        lp.height = GolukUtils.getKeyBoardHeight();
        emoLayout.setLayoutParams(lp);
    }

    private void showEmojocon() {
        setLayoutHeight();
        emoLayout.setVisibility(View.VISIBLE);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void hideEmojocon() {
        emoLayout.setVisibility(View.GONE);
    }

    private void init() {
        EmojiconsFragment fg = EmojiconsFragment.newInstance(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.emojiconsLayout, fg).commit();
    }

    private void click_switchInput() {
        if (!this.isCanShowSoft()) {
            return;
        }
        if (mInputState) {
            click_Emojocon();
        } else {
            click_soft();
        }
    }

    private void setSwitchState(boolean isTextInput) {
        mInputState = isTextInput;
        if (isTextInput) {
            // 显示表情
            mEmojiImg.setImageDrawable(this.getResources().getDrawable(R.drawable.input_state_emojo));
        } else {
            // 显示键盘
            mEmojiImg.setImageDrawable(this.getResources().getDrawable(R.drawable.input_state_txt));
        }
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mEditInput);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mEditInput, emojicon);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            setSwitchState(true);
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
            if (this.emoLayout.getVisibility() == View.GONE) {
                setSwitchState(true);
            } else {
                setSwitchState(false);
            }
        }
    }

    private void playVideoStatistics(VideoDetailRetBean videoDetailRetBean) {
        if (null != videoDetailRetBean && null != videoDetailRetBean.data && null != videoDetailRetBean.data.avideo &&
                null != videoDetailRetBean.data.avideo.video) {
            String actionName = "";
            if (null != videoDetailRetBean.data.avideo.video.recom && null != videoDetailRetBean.data.avideo.video.recom.topicname) {
                actionName = videoDetailRetBean.data.avideo.video.recom.topicname;
            }
            JSONObject json = ZhugeUtils.eventPlayVideo(this, videoDetailRetBean.data.avideo.video.videoid,
                    videoDetailRetBean.data.avideo.video.describe, actionName, videoDetailRetBean.data.avideo.video.category, ZHUGE_PLAY_VIDEO_PAGE_VIDEODETAIL);
            ZhugeSDK.getInstance().track(this, this.getString(R.string.str_zhuge_play_video_event), json);
        }
    }

}
