package com.mobnote.golukmain.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import cn.com.mobnote.module.page.IPageNotifyFn;
import de.greenrobot.event.EventBus;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.cluster.bean.GetClusterShareUrlData;
import com.mobnote.golukmain.cluster.bean.TagActivityBean;
import com.mobnote.golukmain.cluster.bean.TagDataBean;
import com.mobnote.golukmain.cluster.bean.TagGeneralRetBean;
import com.mobnote.golukmain.cluster.bean.TagGeneralVideoListBean;
import com.mobnote.golukmain.cluster.bean.TagRetBean;
import com.mobnote.golukmain.cluster.bean.ShareUrlDataBean;
import com.mobnote.golukmain.cluster.bean.VolleyDataFormat;
import com.mobnote.golukmain.comment.CommentActivity;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.praise.PraiseCancelRequest;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultBean;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultDataBean;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videoshare.ShareVideoShortUrlRequest;
import com.mobnote.golukmain.videoshare.bean.VideoShareRetBean;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;

public class ClusterActivity extends BaseActivity implements OnClickListener, IRequestResultListener {

    public static final String TAG = "ClusterActivity";

    public static final String CLUSTER_KEY_ACTIVITYID = "activityid";
    public static final String CLUSTER_KEY_TITLE = "cluster_key_title";
    public static final String CLUSTER_KEY_TYPE = "cluster_key_type";


    private RTPullListView mRTPullListView = null;
    private CustomLoadingDialog mCustomProgressDialog = null;
    private VolleyDataFormat vdf = new VolleyDataFormat();
    private static final int ClOSE_ACTIVITY = 1000;
    private static final String LIST_PAGE_SIZE = "20";
    /**
     * 保存列表一个显示项索引
     */
    private int mListFirstVisible;
    /**
     * 保存列表显示item个数
     */
    private int mListVisibleCount;
    /**
     * 返回按钮
     */
    private ImageButton mBackBtn;
    /**
     * 标题
     **/
    private TextView mTitleTV;

    /**
     * 分享按钮
     **/
    private Button mShareBtn;
    private EditText mEditText = null;
    private TextView mCommenCountTV = null;
    private TagDataBean mHeadData = null;
    public List<VideoSquareInfo> mRecommendList = null;
    public List<VideoSquareInfo> mNewestList = null;
    public ClusterAdapter mClusterAdapter;
    private SharePlatformUtil mSharePlatform = null;
    /**
     * 活动id
     **/
    private String mTagId = null;
    private String mActivityId = null;
    private String mClusterTitle = null;

    private TagGetRequest mTagGetRequest = null;
    private TagRecommendListRequest mRecommendRequest = null;
    private TagNewestListRequest mNewsRequest = null;
    private GetShareUrlRequest mShareRequest = null;
    private boolean mIsfrist = false;
    /**
     * 是否允许评论
     */
    private boolean mIsCanInput = true;
    /**
     * 是否允许点击评论，只有当数据回来时，才可以去评论
     */
    private boolean mIsRequestSucess = false;

    private boolean mIsRecommendLoad = false;
    private boolean mIsNewestLoad = false;

    private String mTimeStamp = "";
    private int mTagType;
    private View mClusterCommentRL;
    private String mCurMotion = GolukConfig.LIST_REFRESH_NORMAL;
    private GolukApplication mApp;
    private int mCurrentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cluster_main);

        Intent intent = this.getIntent();
        mTagId = intent.getStringExtra(CLUSTER_KEY_ACTIVITYID);
        mClusterTitle = intent.getStringExtra(CLUSTER_KEY_TITLE);
        mTagType = intent.getIntExtra(CLUSTER_KEY_TYPE, 0);
        mRecommendList = new ArrayList<VideoSquareInfo>();
        mNewestList = new ArrayList<VideoSquareInfo>();
        mApp = GolukApplication.getInstance();
        this.initData();// 初始化view
        this.initListener();// 初始化view的监听

        mIsfrist = true;
        mCurMotion = GolukConfig.LIST_REFRESH_NORMAL;
        httpPost(mTagId);
        mRTPullListView.firstFreshState();

        EventBus.getDefault().register(this);
    }

    public static class NoVideoDataViewHolder {
        TextView emptyImage;
        TextView tipsText;
        boolean bMeasureHeight;
    }

    public void onEventMainThread(EventDeleteVideo event) {
        if(EventConfig.VIDEO_DELETE != event.getOpCode()) {
            return;
        }

        final String delVid = event.getVid();
        mClusterAdapter.deleteVideo(delVid);
    }

    private void httpPost(String tagId) {
        sendTagGetRequest(tagId);
        sendTagRecommendListRequest(tagId, GolukConfig.LIST_REFRESH_NORMAL, mTimeStamp, LIST_PAGE_SIZE);
        sendTagNewestListRequest(tagId, GolukConfig.LIST_REFRESH_NORMAL, mTimeStamp, LIST_PAGE_SIZE);
    }

    private void sendTagGetRequest(String tagId) {
        mTagGetRequest = new TagGetRequest(IPageNotifyFn.PageType_TagGet, this);
        mTagGetRequest.get(GolukConfig.SERVER_PROTOCOL_V2, tagId);
    }

    private void initData() {
        mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        mTitleTV = (TextView) findViewById(R.id.title);

        if (mClusterTitle.length() > 12) {
            mClusterTitle = mClusterTitle.substring(0, 12) + this.getString(R.string.str_omit);
        }
        mTitleTV.setText(mClusterTitle);
        mShareBtn = (Button) findViewById(R.id.title_share);
        mEditText = (EditText) findViewById(R.id.custer_comment_input);
        mCommenCountTV = (TextView) findViewById(R.id.custer_comment_send);
        mClusterCommentRL = findViewById(R.id.custer_comment_layout);
        if(mTagType == 1) {
            mClusterCommentRL.setVisibility(View.VISIBLE);
        } else {
            mClusterCommentRL.setVisibility(View.GONE);
        }
        mSharePlatform = new SharePlatformUtil(this);
        mClusterAdapter = new ClusterAdapter(this, 1, mTagId);
        mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mRTPullListView.setAdapter(mClusterAdapter);
    }

    private void initListener() {
        mBackBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);
        mEditText.setOnClickListener(this);
        mCommenCountTV.setOnClickListener(this);

        mRTPullListView.setonRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurMotion = GolukConfig.LIST_REFRESH_NORMAL;
                httpPost(mTagId);
            }
        });

        mRTPullListView.setOnRTScrollListener(mOnRTScrollListener);
    }

    OnRTScrollListener mOnRTScrollListener = new OnRTScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView arg0, int scrollState) {
            if(scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                return;
            }

            mCurMotion = GolukConfig.LIST_REFRESH_PULL_UP;
            if(mRTPullListView.getAdapter().getCount() !=
                    (mListFirstVisible + mListVisibleCount)) {
                return;
            }

            if (mClusterAdapter.getCurrentViewType() == ClusterAdapter.VIEW_TYPE_RECOMMEND) {
                if(mRecommendList == null || mRecommendList.size() == 0) {
                    return;
                }

                if(!mIsRecommendLoad) {
                    return;
                }

                mCurMotion = GolukConfig.LIST_REFRESH_PULL_UP;
                mRecommendRequest = new TagRecommendListRequest(
                        IPageNotifyFn.PageType_ClusterRecommend, ClusterActivity.this);
                mRecommendRequest.get(mTagId, mCurMotion, mTimeStamp, LIST_PAGE_SIZE);
                mIsRecommendLoad = false;
            } else {
                if(mNewestList == null || mNewestList.size() == 0) {
                    return;
                }
                if(!mIsNewestLoad) {
                    return;
                }

                mCurMotion = GolukConfig.LIST_REFRESH_PULL_UP;
                mNewsRequest = new TagNewestListRequest(IPageNotifyFn.PageType_ClusterNews,
                        ClusterActivity.this);
                mNewsRequest.get(mTagId, mCurMotion,
                        mNewestList.get(mNewestList.size() - 1).mVideoEntity.sharingtime, LIST_PAGE_SIZE);
                mIsNewestLoad = false;
            }
        }

        @Override
        public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
            mListFirstVisible = firstVisibleItem;
            mListVisibleCount = visibleItemCount;
        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.back_btn) {
            finish();
        } else if (id == R.id.title_share) {
            showProgressDialog();
            mShareRequest = new GetShareUrlRequest(IPageNotifyFn.PageType_ClusterShareUrl, this);
            mShareRequest.get(GolukConfig.SERVER_PROTOCOL_V2, mTagId);
        } else if (id == R.id.custer_comment_send) {
            toCommentActivity(false);
        } else if (id == R.id.custer_comment_input) {
            toCommentActivity(true);
        } else {
        }
    }

    private void toCommentActivity(boolean isShowSoft) {
        if (!mIsRequestSucess) {
            return;
        }
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(CommentActivity.COMMENT_KEY_MID, mActivityId);
        intent.putExtra(CommentActivity.COMMENT_KEY_TYPE, ICommentFn.COMMENT_TYPE_CLUSTER);
        intent.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, isShowSoft);
        intent.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, mIsCanInput);
        intent.putExtra(CommentActivity.COMMENT_KEY_USERID, "");
        startActivity(intent);
    }

    private void setCommentCount(String count) {
        if (null == count) {
            return;
        }
        String show = GolukUtils.getFormatNumber(count) + this.getString(R.string.str_comment_unit);
        mCommenCountTV.setText(show);
    }

    @Override
    protected void onResume() {
        mBaseApp.setContext(this, TAG);
        super.onResume();
    }

    public void updateViewData(boolean success, int count) {
        mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(this));
        if(!success) {
            return;
        }

        mClusterAdapter.notifyDataSetChanged();
        if (count > 0 && mCurMotion.equals(GolukConfig.LIST_REFRESH_PULL_UP)) {
            mRTPullListView.setSelection(count);
        }
    }

    private void setTimeStamp(List<TagGeneralVideoListBean> list) {
        if(null == list || list.size() == 0) {
            return;
        }

        TagGeneralVideoListBean videoListBean = list.get(list.size() - 1);
        if (videoListBean != null && videoListBean.video != null) {
            mTimeStamp = videoListBean.video.sharingtime;
        }
    }

    private void setCommentData(TagActivityBean bean) {
        if (bean == null) {
            return;
        }

        mIsCanInput = false;
        mActivityId = bean.activityid;

        if(null != bean.iscomment && "1".equals(bean.iscomment)) {
            mIsCanInput = true;
        }

        setCommentCount(bean.commentcount);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(null == mSharePlatform) {
            return;
        }

        mSharePlatform.onActivityResult(requestCode, resultCode, data);
    }

    public boolean sendPraiseRequest(String id) {
        PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
        return request.get("1", id, "1");
    }

    public boolean sendCancelPraiseRequest(String id) {
        PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
        return request.get("1", id);
    }

    private void sendTagRecommendListRequest(String tagId, String operation, String timeStamp, String pageSize) {
        TagRecommendListRequest request = new TagRecommendListRequest(IPageNotifyFn.PageType_ClusterRecommend, this);
        request.get(tagId, operation, timeStamp, pageSize);
    }

    private void sendTagNewestListRequest(String tagId, String operation, String timeStamp, String pageSize) {
        TagNewestListRequest request = new TagNewestListRequest(IPageNotifyFn.PageType_ClusterNews, this);
        request.get(tagId, operation, timeStamp, pageSize);
    }

    protected void sendTagNewestListRequest() {
        sendTagNewestListRequest(mTagId, GolukConfig.LIST_REFRESH_NORMAL, "", LIST_PAGE_SIZE);
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        if (requestType == IPageNotifyFn.PageType_TagGet) {
            TagRetBean ret = (TagRetBean) result;
            mRTPullListView.removeFooterView(1);
            mRTPullListView.removeFooterView(2);
            if(null == ret || ret.code != 0) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                updateViewData(false, 0);
                if (mIsfrist) {
                    mBaseHandler.sendEmptyMessageDelayed(ClOSE_ACTIVITY, 2000);
                }
                return;
            }

            if (ret.data != null) {
                mIsRequestSucess = true;
                mClusterAdapter.setDataInfo(ret.data);
                if(1 == ret.data.type) {
                    setCommentData(ret.data.activity);
                }
                updateViewData(true, 0);
            } else {
                updateViewData(false, 0);
            }

            mIsfrist = false;
            if(ret.data.type == 1) {
                mClusterCommentRL.setVisibility(View.VISIBLE);
            } else {
                mClusterCommentRL.setVisibility(View.GONE);
            }
        } else if (requestType == IPageNotifyFn.PageType_ClusterRecommend) {
            TagGeneralRetBean ret = (TagGeneralRetBean) result;
            if(ret == null || ret.code != 0) {
                mIsRecommendLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                return;
            }

            if(null == ret.data) {
                mIsRecommendLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            setTimeStamp(ret.data.videolist);
            List<VideoSquareInfo> list = vdf.getClusterList(ret.data.videolist);
            if(null == list || list.size() == 0) {
                mIsRecommendLoad = false;
//                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            int count = mRecommendList.size();
            if (list.size() == 20) {
                mIsRecommendLoad = true;
            } else {
                mIsRecommendLoad = false;
            }

            if(GolukConfig.LIST_REFRESH_PULL_DOWN.equals(ret.data.operation) ||
                    GolukConfig.LIST_REFRESH_NORMAL.equals(ret.data.operation)) {
                mRecommendList.clear();
            }
            mRecommendList.addAll(list);
            mClusterAdapter.setDataInfo(mRecommendList, null);
            updateViewData(true, count);
        } else if (requestType == IPageNotifyFn.PageType_ClusterNews) {
            TagGeneralRetBean ret = (TagGeneralRetBean) result;
            if(null == ret || ret.code != 0) {
                mIsNewestLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                return;
            }

            if(null == ret.data) {
                mIsNewestLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            List<VideoSquareInfo> list = vdf.getClusterList(ret.data.videolist);
            int count = mNewestList.size();
            if (list != null && list.size() > 0) {
                if (list.size() == 20) {
                    mIsNewestLoad = true;
                } else {
                    mIsNewestLoad = false;
                }

                if(GolukConfig.LIST_REFRESH_PULL_DOWN.equals(ret.data.operation) ||
                        GolukConfig.LIST_REFRESH_NORMAL.equals(ret.data.operation)) {
                    mNewestList.clear();
                }
                mNewestList.addAll(list);
                mClusterAdapter.setDataInfo(null, mNewestList);
                updateViewData(true, count);
            } else {
                mIsNewestLoad = false;
//                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
            }
        } else if (requestType == IPageNotifyFn.PageType_ClusterShareUrl) {
            closeProgressDialog();

            GetClusterShareUrlData ret = (GetClusterShareUrlData) result;
            if(null == ret || 0 != ret.code) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                return;
            }

            if(null == ret.data) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                return;
            }

            ShareUrlDataBean sdb = ret.data;
            String shareurl = sdb.shorturl;
            String coverurl = sdb.coverurl;
            String describe = sdb.description;
            String realDesc = this.getResources().getString(R.string.cluster_jxzt_share_txt);

            if (TextUtils.isEmpty(describe)) {
                describe = "";
            }
            String ttl = mClusterTitle;
            if (TextUtils.isEmpty(mClusterTitle)) {
                ttl = this.getResources().getString(R.string.cluster_jx_zt_share);
            }

            Bitmap bitmap = null;

            if(mHeadData != null) {
                if (mHeadData.type == 0 && null != mHeadData.tag) {
                    bitmap = getThumbBitmap(mHeadData.tag.picture);
                } else if(mHeadData.type == 1 && null != mHeadData.activity) {
                    bitmap = getThumbBitmap(mHeadData.activity.picture);
                } else {
                    // which should not happen
                }
            }

            if(null == this || this.isFinishing()) {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                return;
            }

            ThirdShareBean bean = new ThirdShareBean();
            bean.surl = shareurl;
            bean.curl = coverurl;
            bean.db = describe;
            bean.tl = ttl;
            bean.bitmap = bitmap;
            bean.realDesc = realDesc;
            bean.videoId = mTagId;
            bean.from = this.getString(R.string.str_zhuge_action_tag);
            ProxyThirdShare shareBoard = new ProxyThirdShare(ClusterActivity.this, mSharePlatform, bean);
            shareBoard.showAtLocation(ClusterActivity.this.getWindow().getDecorView(), Gravity.BOTTOM,
                        0, 0);
        } else if (requestType == IPageNotifyFn.PageType_Praise) {
            PraiseResultBean prBean = (PraiseResultBean) result;
            if (null == result || !prBean.success) {
                GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
                return;
            }

            PraiseResultDataBean ret = prBean.data;
            if(null == ret || TextUtils.isEmpty(ret.result)) {
                return;
            }

            if ("0".equals(ret.result)) {
                if(null == mVideoSquareInfo) {
                    return;
                }

                if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
                    mVideoSquareInfo.mVideoEntity.ispraise = "1";
                    updateClickPraiseNumber(true, mVideoSquareInfo);
                }
            } else if ("7".equals(ret.result)) {
                GolukUtils.showToast(this, this.getString(R.string.str_no_duplicated_praise));
            } else {
                GolukUtils.showToast(this, this.getString(R.string.str_praise_failed));
            }
        } else if (requestType == IPageNotifyFn.PageType_PraiseCancel) {
            PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
            if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
                GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
                return;
            }

            PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
            if(null == cancelRet || TextUtils.isEmpty(cancelRet.result)) {
                return;
            }

            if ("0".equals(cancelRet.result)) {
                if(null == mVideoSquareInfo) {
                    return;
                }

                if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
                    mVideoSquareInfo.mVideoEntity.ispraise = "0";
                    updateClickPraiseNumber(true, mVideoSquareInfo);
                }
            } else {
                GolukUtils.showToast(this, this.getString(R.string.str_cancel_praise_failed));
            }
        } else if(requestType == IPageNotifyFn.PageType_GetShareURL) {
            VideoShareRetBean bean = (VideoShareRetBean) result;
            if (null == bean) {
                Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!bean.success) {
                Toast.makeText(this, bean.msg, Toast.LENGTH_SHORT).show();
                return;
            }

            if (null == mSharePlatform) {
                return;
            }
            String shareurl = bean.data.shorturl;
            String coverurl = bean.data.coverurl;
            String describe = bean.data.describe;

            String realDesc = getResources().getString(R.string.str_share_board_real_desc);
            if (TextUtils.isEmpty(describe)) {
                describe = getResources().getString(R.string.str_share_describe);
            }
            String ttl = getResources().getString(R.string.str_goluk_wonderful_video);
            if(mCurrentIndex < 1) {
                return;
            }
            VideoSquareInfo info = null;
            if(mClusterAdapter.getCurrentViewType() == ClusterAdapter.VIEW_TYPE_RECOMMEND) {
                info = mRecommendList.get(mCurrentIndex - 1);
            }

            if(mClusterAdapter.getCurrentViewType() == ClusterAdapter.VIEW_TYPE_NEWEST) {
                info = mNewestList.get(mCurrentIndex - 1);
            }

            if(null == info) {
                return;
            }

            String videoId = info.mVideoEntity.videoid;
            String username = info.mUserEntity.nickname;
            describe = username + this.getString(R.string.str_colon) + describe;


            ThirdShareBean shareBean = new ThirdShareBean();
            shareBean.surl = shareurl;
            shareBean.curl = coverurl;
            shareBean.db = describe;
            shareBean.tl = ttl;
            shareBean.bitmap = null;
            shareBean.realDesc = realDesc;
            shareBean.videoId = videoId;
            shareBean.from = getString(R.string.str_zhuge_follow);

            ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, shareBean);
            shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        }
    }

    /**
     * 更改聚合页面底部显示的布局
     *
     * @param type
     */
    public void updateListViewBottom(int type) {
        mRTPullListView.removeFooterView(1);
        mRTPullListView.removeFooterView(2);

        if (ClusterAdapter.VIEW_TYPE_RECOMMEND == type) {
            if (mIsRecommendLoad) {
                mRTPullListView.addFooterView(1);
            } else {
                if (mRecommendList != null && mRecommendList.size() > 0) {
                    mRTPullListView.addFooterView(2);
                }
            }
        }

        if(ClusterAdapter.VIEW_TYPE_NEWEST == type) {
            if (mIsNewestLoad) {
                mRTPullListView.addFooterView(1);
            } else {
                if (mNewestList != null && mNewestList.size() > 0) {
                    mRTPullListView.addFooterView(2);
                }
            }
        }
    }

    public Bitmap getThumbBitmap(String netUrl) {
        if(TextUtils.isEmpty(netUrl)) {
            return null;
        }

        String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
        String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
        File file = new File(path + File.separator + name);
        Bitmap t_bitmap = null;

        if (file.exists()) {
            t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
        }
        return t_bitmap;
    }

    public void showProgressDialog() {
        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(this, null);
        }
        if (!mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.show();
        }

    }

    public void closeProgressDialog() {
        if (null != mCustomProgressDialog) {
            mCustomProgressDialog.close();
        }
    }

    VideoSquareInfo mVideoSquareInfo;

    public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
        mVideoSquareInfo = info;
        if (!flag) {
            return;
        }

        if (null == mClusterAdapter) {
            return;
        }

        if (mClusterAdapter.getCurrentViewType() == ClusterAdapter.VIEW_TYPE_RECOMMEND) {
            if(null == mRecommendList) {
                return;
            }

            for (int i = 0; i < mRecommendList.size(); i++) {
                VideoSquareInfo vs = this.mRecommendList.get(i);
                if (this.updatePraise(vs, mRecommendList, i)) {
                    break;
                }
            }
        } else {
            if(null == mNewestList) {
                return;
            }

            for (int i = 0; i < this.mNewestList.size(); i++) {
                VideoSquareInfo vs = this.mNewestList.get(i);
                if (this.updatePraise(vs, mNewestList, i)) {
                    break;
                }
            }
        }
    }

    public boolean updatePraise(VideoSquareInfo vs, List<VideoSquareInfo> videos, int index) {
        if (vs.id.equals(mVideoSquareInfo.id)) {
            int number = Integer.parseInt(mVideoSquareInfo.mVideoEntity.praisenumber);
            if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
                number++;
            } else {
                number--;
            }

            videos.get(index).mVideoEntity.praisenumber = "" + number;
            videos.get(index).mVideoEntity.ispraise = mVideoSquareInfo.mVideoEntity.ispraise;
            mVideoSquareInfo.mVideoEntity.praisenumber = "" + number;
            mClusterAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        if (null != mCustomProgressDialog) {
            if (mCustomProgressDialog.isShowing()) {
                mCustomProgressDialog.close();
            }
        }
        mBaseHandler.removeMessages(ClOSE_ACTIVITY);
        GlideUtils.clearMemory(this);
        GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener(TAG);

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void hMessage(Message msg) {
        switch (msg.what) {
            case ClOSE_ACTIVITY:
                this.finish();
                break;
            default:
                break;
        }
        super.hMessage(msg);
    }

    protected void sendGetShareVideoUrlRequest(String videoId, String type) {
        ShareVideoShortUrlRequest request = new ShareVideoShortUrlRequest(
                IPageNotifyFn.PageType_GetShareURL, this);
        if (null != mApp && mApp.isUserLoginSucess) {
            if (!TextUtils.isEmpty(mApp.mCurrentUId)) {
                request.get(videoId, type);
            }
        }
    }

    protected void storeCurrentIndex(int index) {
        mCurrentIndex = index;
    }

    public int getListViewHeight() {
        return mRTPullListView.getHeight();
    }
}
