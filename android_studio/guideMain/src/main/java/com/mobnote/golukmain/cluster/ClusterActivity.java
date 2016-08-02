package com.mobnote.golukmain.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import de.greenrobot.event.EventBus;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventDeleteVideo;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.cluster.ClusterAdapter.IClusterInterface;
import com.mobnote.golukmain.cluster.bean.ClusterHeadBean;
import com.mobnote.golukmain.cluster.bean.GetClusterShareUrlData;
import com.mobnote.golukmain.cluster.bean.TagGeneralRetBean;
import com.mobnote.golukmain.cluster.bean.TagGeneralVideoListBean;
import com.mobnote.golukmain.cluster.bean.TagRetBean;
import com.mobnote.golukmain.cluster.bean.ShareUrlDataBean;
import com.mobnote.golukmain.cluster.bean.VolleyDataFormat;
import com.mobnote.golukmain.comment.CommentActivity;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.newest.ClickPraiseListener.IClickPraiseView;
import com.mobnote.golukmain.newest.ClickShareListener.IClickShareView;
import com.mobnote.golukmain.newest.IDialogDealFn;
import com.mobnote.golukmain.praise.PraiseCancelRequest;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultBean;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultDataBean;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.golukmain.videosuqare.VideoSquareInfo;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

public class ClusterActivity extends BaseActivity implements OnClickListener, IRequestResultListener, IClickShareView,
        IClickPraiseView, IDialogDealFn, IClusterInterface, VideoSuqareManagerFn {

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
    private int mWonderfulFirstVisible;
    /**
     * 保存列表显示item个数
     */
    private int mWonderfulVisibleCount;
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
    private TextView mCommenCountTv = null;
    public ClusterHeadBean mHeadData = null;
    public List<VideoSquareInfo> mRecommendList = null;
    public List<VideoSquareInfo> mNewestList = null;
    public ClusterAdapter mClusterAdapter;
    private SharePlatformUtil mSharePlatform = null;
    /**
     * 活动id
     **/
    private String mTagId = null;
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
    private boolean mIsNewsLoad = false;

    /**
     * 是否在上拉刷新
     **/
    private boolean mIsLoadDataRecommend = false;
    private boolean mIsLoadDataNews = false;

    private String mTjtime = "00000000000000000";
    private int mTagType;
    private View mClusterCommentRL;

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

        this.initData();// 初始化view
        this.initListener();// 初始化view的监听

        GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener(TAG, this);
        mIsfrist = true;
        httpPost(mTagId);
        mRTPullListView.firstFreshState();

        EventBus.getDefault().register(this);
    }

    public static class NoVideoDataViewHolder {
        TextView tips;
        TextView emptyImage;
        TextView tipsText;
        boolean bMeasureHeight;
    }

    public void onEventMainThread(EventDeleteVideo event) {
        if (EventConfig.VIDEO_DELETE == event.getOpCode()) {
            final String delVid = event.getVid(); // 已经删除的id
            this.mClusterAdapter.deleteVideo(delVid);
        }
    }

    /**
     * 获取网络数据
     *
     * @param tagId 是否显示加载中对话框
     * @author xuhw
     * @date 2015年4月15日
     */
    private void httpPost(String tagId) {
        mTagGetRequest = new TagGetRequest(IPageNotifyFn.PageType_TagGet, this);
        mTagGetRequest.get("200", tagId);
        sendTagRecommendListRequest(mTagId, "0", mTjtime, LIST_PAGE_SIZE);
        sendTagNewestListRequest(mTagId, "0", mTjtime, LIST_PAGE_SIZE);
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
        mCommenCountTv = (TextView) findViewById(R.id.custer_comment_send);
        mClusterCommentRL = findViewById(R.id.custer_comment_layout);
        if(mTagType == 1) {
            mClusterCommentRL.setVisibility(View.VISIBLE);
        } else {
            mClusterCommentRL.setVisibility(View.GONE);
        }
        mSharePlatform = new SharePlatformUtil(this);
        mClusterAdapter = new ClusterAdapter(this, 1, this, mTagId);
        mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mRTPullListView.setAdapter(mClusterAdapter);
    }

    private void setTitle(String titles) {
        if (null != mClusterTitle && !"".equals(mClusterTitle)) {
            return;
        }
        mClusterTitle = titles;
        mTitleTV.setText(mClusterTitle);
    }

    private void initListener() {
        mBackBtn.setOnClickListener(this);
        mShareBtn.setOnClickListener(this);
        mEditText.setOnClickListener(this);
        mCommenCountTv.setOnClickListener(this);
        mRTPullListView.setonRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新个人中心所有数据
                httpPost(mTagId);// 请求数据
            }
        });

        mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

                    if (mRTPullListView.getAdapter().getCount() == (mWonderfulFirstVisible + mWonderfulVisibleCount)) {// 推荐
                        if (mClusterAdapter.getCurrentViewType() == ClusterAdapter.VIEW_TYPE_RECOMMEND) {// 视频列表
                            if (mRecommendList != null && mRecommendList.size() > 0) {// 加载更多视频数据
                                if (mIsRecommendLoad) {
                                    mRecommendRequest = new TagRecommendListRequest(
                                            IPageNotifyFn.PageType_ClusterRecommend, ClusterActivity.this);
                                    mRecommendRequest.get(mTagId, "2", mTjtime, "20");
                                    mIsRecommendLoad = false;
                                    mIsLoadDataRecommend = true;
                                }
                            }
                        } else {// 最新列表
                            if (mNewestList != null && mNewestList.size() > 0) {// 加载更多视频数据
                                if (mIsNewsLoad) {
                                    mNewsRequest = new TagNewestListRequest(IPageNotifyFn.PageType_ClusterNews,
                                            ClusterActivity.this);
                                    mNewsRequest.get(mTagId, "2",
                                            mNewestList.get(mNewestList.size() - 1).mVideoEntity.sharingtime, "20");
                                    mIsLoadDataNews = true;
                                    mIsNewsLoad = false;
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
                mWonderfulFirstVisible = firstVisibleItem;
                mWonderfulVisibleCount = visibleItemCount;
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            this.finish();
        } else if (id == R.id.title_share) {
            showProgressDialog();
            mShareRequest = new GetShareUrlRequest(IPageNotifyFn.PageType_ClusterShareUrl, this);
            mShareRequest.get(mTagId);
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
        intent.putExtra(CommentActivity.COMMENT_KEY_MID, mTagId);
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
        mCommenCountTv.setText(show);
    }

    @Override
    protected void onResume() {
        mBaseApp.setContext(this, TAG);
        super.onResume();
    }

    public void updateViewData(boolean succ, int count) {
        mRTPullListView.onRefreshComplete(GolukUtils.getCurrentFormatTime(this));
        if (succ) {
            mClusterAdapter.notifyDataSetChanged();
            if (count > 0) {
                this.mRTPullListView.setSelection(count);
            }
        }
    }

    /**
     * 获取推荐的上拉刷新时间戳
     *
     * @param list
     */
    private void setTjTime(List<TagGeneralVideoListBean> list) {
        if (list != null && list.size() > 0) {
            TagGeneralVideoListBean vlb = list.get(list.size() - 1);
            if (vlb != null && vlb.video != null && vlb.video.gen != null) {
                mTjtime = vlb.video.gen.tjtime;
            }
        }
    }

    private void setCommentData(ClusterHeadBean bean) {
        if (bean == null) {
            return;
        }

        mIsCanInput = false;
        mTagId = bean.activity.activityid;
        if (null != bean.activity.iscomment && !"".equals(bean.activity.iscomment)) {
            if ("1".equals(bean.activity.iscomment)) {
                mIsCanInput = true;
            }
        }
        setCommentCount(bean.activity.commentcount);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != mSharePlatform) {
            mSharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 点赞请求
    public boolean sendPraiseRequest(String id) {
        PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
        return request.get("1", id, "1");
    }

    // 取消点赞请求
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

                updateViewData(true, 0);
            } else {
                updateViewData(false, 0);
            }

            mIsfrist = false;
        } else if (requestType == IPageNotifyFn.PageType_ClusterRecommend) {
            mIsLoadDataRecommend = false;
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

            if(!"0".equals(ret.data.result)) {
                mIsRecommendLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            setTjTime(ret.data.videolist);
            List<VideoSquareInfo> list = vdf.getClusterList(ret.data.videolist);
            if(null == list || list.size() == 0) {
                mIsRecommendLoad = false;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            int count = mRecommendList.size();
            if (list.size() == 20) {
                mIsRecommendLoad = true;
            } else {
                mIsRecommendLoad = false;
            }
            mRecommendList.addAll(list);
            updateViewData(true, count);
        } else if (requestType == IPageNotifyFn.PageType_ClusterNews) {
            mIsLoadDataNews = false;
            TagGeneralRetBean ret = (TagGeneralRetBean) result;
            if(null == ret || ret.code != 0) {
                mIsNewsLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                return;
            }

            if(null == ret.data) {
                mIsNewsLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            if(!"0".equals(ret.data.result)) {
                mIsNewsLoad = true;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
                return;
            }

            List<VideoSquareInfo> list = vdf.getClusterList(ret.data.videolist);
            int count = mNewestList.size();
            if (list != null && list.size() > 0) {
                if (list.size() == 20) {
                    mIsNewsLoad = true;
                } else {
                    mIsNewsLoad = false;
                }
                mNewestList.addAll(list);
                updateViewData(true, count);
            } else {
                mIsNewsLoad = false;
                GolukUtils.showToast(this, this.getResources().getString(R.string.request_data_error));
            }
        } else if (requestType == IPageNotifyFn.PageType_ClusterShareUrl) {
            closeProgressDialog();
            GetClusterShareUrlData data = (GetClusterShareUrlData) result;
            if (data != null && data.success) {
                if (data.data != null) {

                    ShareUrlDataBean sdb = data.data;
                    if ("0".equals(sdb.result)) {
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
                        // 缩略图
                        Bitmap bitmap = null;
                        if (mHeadData != null) {
                            bitmap = getThumbBitmap(mHeadData.activity.picture);
                        }

                        if (this != null && !this.isFinishing()) {
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
                        } else {
                            GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                        }
                    } else {
                        GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                    }
                } else {
                    GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                }
            } else {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
            }
        } else if (requestType == IPageNotifyFn.PageType_Praise) {
            PraiseResultBean prBean = (PraiseResultBean) result;
            if (null == result || !prBean.success) {
                GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
                return;
            }

            PraiseResultDataBean ret = prBean.data;
            if (null != ret && !TextUtils.isEmpty(ret.result)) {
                if ("0".equals(ret.result)) {
                    if (null != mVideoSquareInfo) {
                        if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
                            mVideoSquareInfo.mVideoEntity.ispraise = "1";
                            updateClickPraiseNumber(true, mVideoSquareInfo);
                        }
                    }
                } else if ("7".equals(ret.result)) {
                    GolukUtils.showToast(this, this.getString(R.string.str_no_duplicated_praise));
                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_praise_failed));
                }
            }
        } else if (requestType == IPageNotifyFn.PageType_PraiseCancel) {
            PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
            if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
                GolukUtils.showToast(this, this.getString(R.string.user_net_unavailable));
                return;
            }

            PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
            if (null != cancelRet && !TextUtils.isEmpty(cancelRet.result)) {
                if ("0".equals(cancelRet.result)) {
                    if (null != mVideoSquareInfo) {
                        if ("1".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
                            mVideoSquareInfo.mVideoEntity.ispraise = "0";
                            updateClickPraiseNumber(true, mVideoSquareInfo);
                        }
                    }
                } else {
                    GolukUtils.showToast(this, this.getString(R.string.str_cancel_praise_failed));
                }
            }
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
        if (ClusterAdapter.VIEW_TYPE_RECOMMEND == type) {// 推荐
            if (mIsRecommendLoad) {
                mRTPullListView.addFooterView(1);
            } else {
                if (mRecommendList != null && mRecommendList.size() > 0) {
                    mRTPullListView.addFooterView(2);
                }
            }
        } else {// 最新
            if (mIsNewsLoad) {
                mRTPullListView.addFooterView(1);
            } else {
                if (mNewestList != null && mNewestList.size() > 0) {
                    mRTPullListView.addFooterView(2);
                }
            }
        }
    }

    public Bitmap getThumbBitmap(String netUrl) {
        String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
        String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
        File file = new File(path + File.separator + name);
        Bitmap t_bitmap = null;
        if (file.exists()) {
            t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
        }
        return t_bitmap;
    }

    @Override
    public void showProgressDialog() {
        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(this, null);
        }
        if (!mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.show();
        }

    }

    @Override
    public void closeProgressDialog() {
        if (null != mCustomProgressDialog) {
            mCustomProgressDialog.close();
        }
    }

    private VideoSquareInfo mWillShareVideoSquareInfo;

    @Override
    public void setWillShareInfo(VideoSquareInfo info) {
        mWillShareVideoSquareInfo = info;
    }

    VideoSquareInfo mVideoSquareInfo;

    @Override
    public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
        mVideoSquareInfo = info;
        if (!flag) {
            return;
        }
        if (null == mClusterAdapter) {
            return;
        }
        if (mClusterAdapter.getCurrentViewType() == ClusterAdapter.VIEW_TYPE_RECOMMEND) {
            if (null != mRecommendList) {
                for (int i = 0; i < mRecommendList.size(); i++) {
                    VideoSquareInfo vs = this.mRecommendList.get(i);
                    if (this.updatePraise(vs, mRecommendList, i)) {
                        break;
                    }
                }
            }
        } else {
            if (null != mNewestList) {
                for (int i = 0; i < this.mNewestList.size(); i++) {
                    VideoSquareInfo vs = this.mNewestList.get(i);
                    if (this.updatePraise(vs, mNewestList, i)) {
                        break;
                    }
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
    public int OnGetListViewWidth() {
        return mRTPullListView.getWidth();
    }

    @Override
    public int OnGetListViewHeight() {
        return mRTPullListView.getHeight();
    }

    @Override
    public void CallBack_Del(int event, Object data) {
    }

    @Override
    public void OnRefrushMainPageData() {

    }

    @Override
    public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
        if (event == VSquare_Req_VOP_GetShareURL_Video) {
            Context topContext = mBaseApp.getContext();
            if (topContext != this) {
                return;
            }
            closeProgressDialog();
            if (RESULE_SUCESS == msg) {
                try {
                    JSONObject result = new JSONObject((String) param2);
                    if (result.getBoolean("success")) {
                        JSONObject data = result.getJSONObject("data");
                        String shareurl = data.getString("shorturl");
                        String coverurl = data.getString("coverurl");
                        String describe = data.optString("describe");

                        String realDesc = this.getResources().getString(R.string.str_share_board_real_desc);

                        if (TextUtils.isEmpty(describe)) {
                            describe = this.getResources().getString(R.string.str_share_describe);
                        }
                        String ttl = this.getResources().getString(R.string.str_goluk_wonderful_video);

                        if (!this.isFinishing()) {
                            String videoId = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mVideoEntity.videoid
                                    : "";
                            String username = null != mWillShareVideoSquareInfo ? mWillShareVideoSquareInfo.mUserEntity.nickname
                                    : "";
                            describe = username + this.getString(R.string.str_colon) + describe;

                            ThirdShareBean bean = new ThirdShareBean();
                            bean.surl = shareurl;
                            bean.curl = coverurl;
                            bean.db = describe;
                            bean.tl = ttl;
                            bean.bitmap = null;
                            bean.realDesc = realDesc;
                            bean.videoId = videoId;
                            bean.from = this.getString(R.string.str_zhuge_action_tag);

                            ProxyThirdShare shareBoard = new ProxyThirdShare(this, mSharePlatform, bean);
                            shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                        }

                    } else {
                        GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
            }
        } else if (event == VSquare_Req_VOP_Praise) {
            if (RESULE_SUCESS == msg) {
                if (null != mVideoSquareInfo) {
                    if ("0".equals(mVideoSquareInfo.mVideoEntity.ispraise)) {
                        mVideoSquareInfo.mVideoEntity.ispraise = "1";
                        updateClickPraiseNumber(true, mVideoSquareInfo);
                    }
                }

            } else {
                GolukUtils.showToast(this, this.getResources().getString(R.string.network_error));
            }

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
        // TODO Auto-generated method stub
        switch (msg.what) {
            case ClOSE_ACTIVITY:
                this.finish();
                break;
            default:
                break;
        }
        super.hMessage(msg);
    }
}
