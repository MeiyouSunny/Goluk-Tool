package com.mobnote.golukmain.videosuqare;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.newest.ClickPraiseListener.IClickPraiseView;
import com.mobnote.golukmain.newest.ClickShareListener.IClickShareView;
import com.mobnote.golukmain.newest.JsonParserUtils;
import com.mobnote.golukmain.newest.NewestAdapter;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

public class CategoryListView implements VideoSuqareManagerFn, OnRefreshListener, OnRTScrollListener, OnClickListener,
        IClickShareView, IClickPraiseView, IRequestResultListener, ZhugeParameterFn {

    public static final String TAG = "CategoryListView";

    private Context mContext = null;
    private LayoutInflater layoutInflater = null;
    private RelativeLayout mRootLayout = null;

    public List<VideoSquareInfo> mDataList = null;

    private RTPullListView mRTPullListView = null;
    private NewestAdapter mCategoryAdapter = null;
    private String historyDate;
    private RelativeLayout noDataView = null;
    private TextView mHintTv;

    private SimpleDateFormat sdf;

    /**
     * ?????????????????? 0.?????? 1.?????? 2.??????
     */
    private String mType;
    /**
     * ???????????? 0.?????? 1.???????????? 2.???????????? 3.???????????? 4.????????? 5.??????????????? 6.???????????? 7.???????????? 8.???????????? 9.????????????
     * 10.???????????????
     */
    private String mAttribute;
    /**
     * ?????????????????????????????????
     */
    private int wonderfulFirstVisible;
    /**
     * ??????????????????item??????
     */
    private int wonderfulVisibleCount;
    /**
     * ??????????????????
     */
    private boolean isHaveData = true;
    private RelativeLayout loading = null;
    /**
     * * 0:?????????
     * <p/>
     * 1?????????
     * <p/>
     * 2?????????
     */
    private int uptype = 0;
    private VideoSquareInfo endtime = null;
    private VideoSquareInfo mPraiseVideoSquareInfo;
    private final int COUNT = 30;
    /**
     * ??????????????????????????????????????????????????????????????????????????????
     */
    private VideoSquareInfo mWillShareSquareInfo = null;
    private CustomLoadingDialog mCustomProgressDialog = null;

    @SuppressLint("SimpleDateFormat")
    public CategoryListView(Context context, final String type, final String attr) {
        mContext = context;
        mType = type;
        mAttribute = attr;

        sdf = new SimpleDateFormat(mContext.getString(R.string.str_date_formatter));

        layoutInflater = LayoutInflater.from(mContext);

        mDataList = new ArrayList<VideoSquareInfo>();
        initView();
        updateRefreshTime();

        addCallBackListener();
        loadHistoryData();

        initYMShare();
        firstRequest(false);
    }

    public void deleteVideo(String vid) {
        if (null != mCategoryAdapter) {
            mCategoryAdapter.deleteVideo(vid);
        }
    }

    private void initYMShare() {
    }

    public void closeProgressDialog() {
        if (null != mCustomProgressDialog) {
            mCustomProgressDialog.close();
            mCustomProgressDialog = null;
        }
    }

    public void showProgressDialog() {
        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(mContext, null);
        }

        if (!mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.show();
        }
    }

    private void addCallBackListener() {
        VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
        if (null != mVideoSquareManager) {
            mVideoSquareManager.addVideoSquareManagerListener(TAG, this);
        }
    }

    public void removeListener() {
        VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
        if (null != mVideoSquareManager) {
            mVideoSquareManager.removeVideoSquareManagerListener(TAG);
        }
    }

    // ???????????????????????????
    public void firstRequest(boolean isclick) {
        boolean isSucess = httpPost(mType, mAttribute, "0", "");
        if (isSucess) {
            if (isclick) {
                this.showProgressDialog();
            } else {
                GolukDebugUtils.e("", "jyf----category-------firstRequest--------" + isFirstShowDialog);
                if (this.isFirstShowDialog) {
                    mHandler.sendEmptyMessageDelayed(100, 150);
                }
            }

        } else {
            GolukUtils.showToast(mContext, mContext.getString(R.string.str_request_fail));
        }
    }

    private boolean isLive() {
        return "1".equals(mType);
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
            if (msg.what == 100) {
                if (isFirstShowDialog) {
                    showProgressDialog();
                }
            }
        }
    };

    private void initView() {
        mRootLayout = (RelativeLayout) layoutInflater.inflate(R.layout.video_type_list, null);
        mHintTv = (TextView) mRootLayout.findViewById(R.id.tv_category_hint);
        mRTPullListView = (RTPullListView) mRootLayout.findViewById(R.id.mRTPullListView);
        mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        noDataView = (RelativeLayout) mRootLayout.findViewById(R.id.category_list_nodata);
        noDataView.setOnClickListener(this);

        if (null == mCategoryAdapter) {
            mCategoryAdapter = new NewestAdapter(mContext, ZHUGE_PLAY_VIDEO_PAGE_CATEGORY);
            mCategoryAdapter.setCategoryListView(this);
        }
        mRTPullListView.setAdapter(mCategoryAdapter);
    }

    private void updateRefreshTime() {
        historyDate = GolukUtils.getCurrentFormatTime(mContext);
    }

    private String getLastRefreshTime() {
        return historyDate;
    }

    public View getView() {
        return mRootLayout;
    }

    private void loadHistoryData() {
        initLayout();
        if (!isLive()) {
            String result = getLocalCacheData();
            if (null != result && !"".equals(result)) {
                List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData(result);
                if (null != datalist && datalist.size() > 0) {
                    mDataList.addAll(datalist);
                    mCategoryAdapter.setData(null, mDataList);
                }
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @return
     * @author jyf
     * @date 2015???8???12???
     */
    private String getLocalCacheData() {
        String json = JsonUtil.getCategoryLocalCacheJson(mAttribute);
        String result = GolukApplication.getInstance().getVideoSquareManager().getCategoryLocalCacheData(json);
        return result;
    }

    /**
     * ??????????????????(??????)
     *
     * @param flag ??????????????????????????????
     * @author xuhw
     * @date 2015???4???15???
     */
    private boolean httpPost(String type, String attribute, String operation, String timestamp) {
        boolean result = GolukApplication.getInstance().getVideoSquareManager()
                .getSquareList("1", type, attribute, operation, timestamp);
        if (!result) {

        }
        return result;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    private void initLayout() {
        mRTPullListView.onRefreshComplete(getLastRefreshTime());
        mRTPullListView.setonRefreshListener(this);
        mRTPullListView.setOnRTScrollListener(this);
    }

    boolean isFirstShowDialog = true;

    private void callBack_CatLog(int msg, int param1, Object param2) {
        closeProgressDialog();
        if (1 != msg) {
            // ??????
            isFirstShowDialog = false;
            mHintTv.setText(mContext.getResources().getString(R.string.msg_system_connect_error));
            callBackFailed();
            dataCallBackRefresh();
            return;
        }

        // ????????????
        List<VideoSquareInfo> datalist = JsonParserUtils.parserNewestItemData((String) param2);
        if (null != datalist && datalist.size() > 0) {
            final int listSize = datalist.size();

            GolukDebugUtils.e("", "jyf----CategoryListView------------------VideoSuqare_CallBack  listSize: "
                    + listSize + "   uptype:" + uptype);

            // ?????????
            updateRefreshTime();
            endtime = datalist.get(listSize - 1);

            if (uptype == 0) {// ??????????????????

                mDataList = datalist;
                mCategoryAdapter.setData(null, mDataList);
                if (listSize >= COUNT) {
                    isHaveData = true;
                    addFoot();
                } else {
                    isHaveData = false;
                    this.removeFoot();
                }
            } else if (uptype == 1) {// ????????????
                if (listSize >= COUNT) {// ????????????30???
                    isHaveData = true;
                } else {// ????????????30???
                    isHaveData = false;
                    removeFoot();
                }
                mDataList.addAll(datalist);
                mCategoryAdapter.setData(null, mDataList);
            } else if (uptype == 2) {// ????????????
                if (listSize >= COUNT) {// ????????????30???
                    isHaveData = true;
                } else {// ????????????30???
                    isHaveData = false;
                    this.removeFoot();
                }
                mDataList = datalist;
                mCategoryAdapter.setData(null, mDataList);
                mRTPullListView.onRefreshComplete(getLastRefreshTime());
            }

        } else {
            // TODO ?????????
            noDataCallBack();
        }

        dataCallBackRefresh();
    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param info
     * @author jyf
     * @date 2015???8???9???
     */
    public void setWillShareInfo(VideoSquareInfo info) {
        mWillShareSquareInfo = info;
    }

    private void callBack_getShareUrl(int msg, int param1, Object param2) {
        closeProgressDialog();
        if (RESULE_SUCESS != msg) {
            GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
            return;
        }
        ShareDataBean shareBean = JsonUtil.parseShareCallBackData((String) param2);
        if (!shareBean.isSucess) {
            GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
        }
        // ????????????
        String describe = getShareDescribe(shareBean.describe);
        final String ttl = getTTL();
        final String realDesc = getRealDesc();
        if (mContext instanceof VideoCategoryActivity) {
            VideoCategoryActivity activity = (VideoCategoryActivity) mContext;
            if (activity != null && !activity.isFinishing()) {
                String videoId = null != mWillShareSquareInfo ? mWillShareSquareInfo.mVideoEntity.videoid : "";
                String nickname = null != mWillShareSquareInfo ? mWillShareSquareInfo.mUserEntity.nickname : "";
                describe = nickname + mContext.getString(R.string.str_colon) + describe;

            }
        }
    }

    private String getRealDesc() {
        if (isShareLive()) {
            return getTTL() + mContext.getString(R.string.str_user_goluk);
        } else {
            return mContext.getString(R.string.str_share_board_real_desc);
        }
    }

    private String getTTL() {
        if (isShareLive()) {
            return mContext.getString(R.string.str_wonderful_live);
        } else {
            return mContext.getString(R.string.str_video_edit_share_title);
        }
    }

    private String getShareDescribe(String describe) {
        if (isShareLive()) {
            if (TextUtils.isEmpty(describe)) {
                return mContext.getString(R.string.str_live_default_describe);
            }
        } else {
            if (TextUtils.isEmpty(describe)) {
                return mContext.getString(R.string.str_share_describe);
            }
        }
        return describe;
    }

    private boolean isShareLive() {
        if (null == mWillShareSquareInfo) {
            return false;
        }
        return mWillShareSquareInfo.mVideoEntity.type.equals("1");
    }

    private void callBack_praise(int msg, int param1, Object param2) {
        if (RESULE_SUCESS == msg) {
            GolukDebugUtils.e("", "GGGG===@@@====2222=====");
            if (null != mPraiseVideoSquareInfo) {
                mPraiseVideoSquareInfo.mVideoEntity.ispraise = "1";
                updateClickPraiseNumber(true, mPraiseVideoSquareInfo);
            }
        } else {
            GolukUtils.showToast(mContext, mContext.getString(R.string.str_network_unusual));
        }
    }

    @Override
    public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
        GolukDebugUtils.e("", "jyf----CategoryListView------------------VideoSuqare_CallBack: " + event + " msg:" + msg
                + "  param2:" + param2);
        if (VSquare_Req_List_Video_Catlog == event) {
            callBack_CatLog(msg, param1, param2);
        } else if (VSquare_Req_VOP_GetShareURL_Video == event) {
            callBack_getShareUrl(msg, param1, param2);
        } else if (VSquare_Req_VOP_Praise == event) {
            callBack_praise(msg, param1, param2);
        }

    }

    // ???????????????????????????
    private void callBackFailed() {
        isHaveData = false;

        if (0 == uptype) {
            // closeProgressDialog();
        } else if (1 == uptype) {
            this.removeFoot();
        } else if (2 == uptype) {
            mRTPullListView.onRefreshComplete(getLastRefreshTime());
        }
        GolukUtils.showToast(mContext, mContext.getString(R.string.network_error));
    }

    private void noDataCallBack() {
        if (uptype == 1) {// ????????????
            Toast.makeText(mContext, R.string.str_pull_refresh_listview_bottom_reach, Toast.LENGTH_SHORT).show();
            this.removeFoot();
        } else if (uptype == 2) {// ????????????
            // ????????? ??????????????????????????????????????????????????????????????????????????????????????????
            if ("1".equals(mType)) {
                mDataList.clear();
                mHintTv.setText(mContext.getResources().getString(R.string.str_live_no_live));
            }
            mRTPullListView.onRefreshComplete(getLastRefreshTime());
        } else {
            mHintTv.setText(mContext.getResources().getString(R.string.str_live_no_live));
        }
    }

    private void dataCallBackRefresh() {
        if (mDataList.size() > 0) {
            noDataView.setVisibility(View.GONE);
            mRTPullListView.setVisibility(View.VISIBLE);
        } else {
            noDataView.setVisibility(View.VISIBLE);
            mRTPullListView.setVisibility(View.GONE);
        }
    }

    private void addFoot() {
        loading = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.video_square_below_loading, null);
        mRTPullListView.addFooterView(loading);
    }

    private void removeFoot() {
        if (loading != null) {
            if (mRTPullListView != null) {
                mRTPullListView.removeFooterView(loading);
                loading = null;
            }
        }
    }

    public void onResume() {
        addCallBackListener();
    }

    public void onPause() {
        removeListener();
    }

    public void onStop() {
    }

    public void onDestroy() {
        this.removeListener();
        closeProgressDialog();
        if (null != mDataList) {
            mDataList.clear();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void onBackPressed() {

    }

    @Override
    public void onRefresh() {
        GolukDebugUtils.e("", "jyf----CategoryListView------------------onRefresh  ????????????: ");
        // ????????????
        uptype = 2;
        httpPost(mType, mAttribute, "0", "");
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                if (null != mRTPullListView && null != mRTPullListView.getAdapter()) {
                    if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
                        if (isHaveData) {
                            // ????????????
                            uptype = 1;
                            if (null != endtime && null != endtime.mVideoEntity) {
                                String timeSign = endtime.mVideoEntity.sharingtime;
                                GolukDebugUtils.e("", "jyf----CategoryListView------------------onRefresh  ????????????: "
                                        + timeSign);
                                httpPost(mType, mAttribute, "2", timeSign);
                            }
                        }
                    }
                }
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                break;
            default:
                break;
        }

    }

    @Override
    public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
        wonderfulFirstVisible = firstVisibleItem;
        wonderfulVisibleCount = visibleItemCount;

        if (null == mDataList && mDataList.size() <= 0) {
            return;
        }

    }

    public void updateClickPraiseNumber(boolean flag, VideoSquareInfo info) {
        mPraiseVideoSquareInfo = info;
        if (!flag) {
            return;
        }

        for (int i = 0; i < mDataList.size(); i++) {
            VideoSquareInfo vs = mDataList.get(i);
            if (vs.id.equals(info.id)) {
                int number = Integer.parseInt(info.mVideoEntity.praisenumber);
                if ("1".equals(info.mVideoEntity.ispraise)) {
                    number++;
                } else {
                    number--;
                }
                mDataList.get(i).mVideoEntity.praisenumber = "" + number;
                mDataList.get(i).mVideoEntity.ispraise = info.mVideoEntity.ispraise;
                info.mVideoEntity.praisenumber = "" + number;
                break;
            }
        }
        mCategoryAdapter.updateClickPraiseNumber(info);
    }

    public void changePraiseStatus(boolean status, String videoId) {
        GolukUtils.changePraiseStatus(mDataList, status, videoId);
        mCategoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.category_list_nodata) {
            this.firstRequest(true);
        }
    }

    // ????????????
    public boolean sendPraiseRequest(String id) {
        return true;
    }

    // ??????????????????
    public boolean sendCancelPraiseRequest(String id) {
        return true;
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
    }
}
