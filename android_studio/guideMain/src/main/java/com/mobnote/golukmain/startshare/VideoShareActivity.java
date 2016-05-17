package com.mobnote.golukmain.startshare;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventSharetypeSelected;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.IPCControlManager;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.newest.IDialogDealFn;
import com.mobnote.golukmain.promotion.PromotionActivity;
import com.mobnote.golukmain.promotion.PromotionItem;
import com.mobnote.golukmain.promotion.PromotionModel;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.startshare.bean.ShareDataBean;
import com.mobnote.golukmain.startshare.bean.ShareDataFullBean;
import com.mobnote.golukmain.startshare.bean.ShareTypeBean;
import com.mobnote.golukmain.thirdshare.SharePlatformAdapter;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.map.LngLat;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.rd.car.editor.FilterPlaybackView;

import java.util.ArrayList;

import cn.com.mobnote.eventbus.EventShortLocationFinish;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wangli on 2016/5/10.
 */
public class VideoShareActivity extends BaseActivity implements View.OnClickListener , IDialogDealFn , IUploadVideoFn ,IRequestResultListener {

    private final int SHARE_PLATFORM_COLUMN_NUMBERS = 3;
    private RecyclerView mRcShareList;
    private SharePlatformAdapter mSharePlatformAdapter;
    private LinearLayout mLocationLayout;
    private TextView mLocationTv;
    private ImageView mLocationIv;
    private String mLocationAddress;

    /** 定位状态 0 表示定位中, 1 表示定位成功, 2表示点击定位, 3 表示用户删除了位置 */
    private int mLocationState = 0;
    /** 定位中 */
    public static final int LOCATION_STATE_ING = 0;
    /** 定位成功 */
    public static final int LOCATION_STATE_SUCCESS = 1;
    /** 定位失败 */
    public static final int LOCATION_STATE_FAILED = 2;
    /** 用户禁止使用位置 */
    public static final int LOCATION_STATE_FORBID = 3;
    private StartShareFunctionDialog mStartShareDialog = null;

    RelativeLayout mRootLayout;
    private TextView mShareTypeTv;
    private TextView mJoinActivityTV;

    private PromotionSelectItem mSelectedPromotionItem;
    private int mSelectedShareType;
    private String mSelectedShareString;

    PopupWindow mPopupWindow;
    boolean isPopup;

    private String videoFrom = "";
    /** 分享的视频创建时间 **/
    private String videoCreateTime = "";
    /** 视频路径 */
    private String mVideoPath;
    /** 视频类型 */
    private int mVideoType;
    /** 分享的视频名称 */
    private String videoName = "";
    private UploadVideo mUploadVideo = null;
    private boolean mIsT1Video = false;
    private boolean isExit = false;
    private ShareLoading mShareLoading = null;

    LinearLayout mShareLL;
    boolean isSharing;
    private Thread mProgressThread = null;

    GolukVideoInfoDbManager mGolukVideoInfoDbManager = GolukVideoInfoDbManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_share);
        EventBus.getDefault().register(this);

        initData();
        interceptVideoName();// 拿到视频名称
        mUploadVideo = new UploadVideo(this, GolukApplication.getInstance(), videoName);
        mUploadVideo.setListener(this);
        initView();

        setupView();
    }

    private void initData(){

        mVideoPath = getIntent().getStringExtra("vidPath");
        mVideoType = getIntent().getIntExtra("vidType",1);

        mSelectedShareType = ShareTypeBean.SHARE_TYPE_SSP;
        mSelectedShareString = "# " + getResources().getString(R.string.share_str_type_ssp);
    }

    public void onEventMainThread(EventShortLocationFinish event) {
        if (null == event) {
            return;
        }

        if(event.getShortAddress() != null && mLocationState != LOCATION_STATE_FORBID){
            mLocationAddress = event.getShortAddress();
            mLocationState = LOCATION_STATE_SUCCESS;
            refreshLocationUI();
        }
    }

    public void onEventMainThread(EventSharetypeSelected event){
        if(event != null){
            this.mSelectedShareType = event.getShareType();
            this.mSelectedShareString = "# " + event.getShareName();
            mShareTypeTv.setText(mSelectedShareString);
        }
    }

    public void onEventMainThread(PromotionSelectItem event){

        if(event != null){
            mSelectedPromotionItem = event;
            mJoinActivityTV.setText(mSelectedPromotionItem.activitytitle);
            mJoinActivityTV.setTextColor(Color.parseColor("#0080ff"));
        }
    }

    /**
     * 得到视频名称
     */
    private void interceptVideoName() {
        if (mVideoPath != null && !"".equals(mVideoPath)) {
            String[] strs = mVideoPath.split("/");
            videoName = strs[strs.length - 1];
            if (mGolukVideoInfoDbManager != null) {
                VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(videoName);
                if (videoFileInfoBean != null) {
                    videoCreateTime = videoFileInfoBean.timestamp + "000";
                    videoFrom = videoFileInfoBean.devicename;
                    if (IPCControlManager.T1_SIGN.equalsIgnoreCase(videoFrom)) {
                        mIsT1Video = true;
                    }
                }
            }
            videoName = videoName.replace("mp4", "jpg");
            // 分享时间
            GolukDebugUtils.e("", "----------------------------VideoEditActivity-----videoName：" + videoName);
            if (TextUtils.isEmpty(videoCreateTime)) {
                if (videoName.contains("_")) {
                    String[] videoTimeArray = videoName.split("_");
                    if (videoTimeArray.length == 3) {
                        videoCreateTime = "20" + videoTimeArray[1] + "000";
                    } else if (videoTimeArray.length == 7) {
                        videoCreateTime = videoTimeArray[2] + "000";
                        mIsT1Video = true;
                    } else if (videoTimeArray.length == 8) {
                        videoCreateTime = videoTimeArray[1] + "000";
                        mIsT1Video = true;
                    }
                } else {
                    videoCreateTime = "";
                }
            }
        }
    }

    private void setupView() {
        mShareLoading = new ShareLoading(this, mRootLayout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,SHARE_PLATFORM_COLUMN_NUMBERS);
        mRcShareList.setLayoutManager(gridLayoutManager);
        mSharePlatformAdapter = new SharePlatformAdapter(this);
        mRcShareList.setAdapter(mSharePlatformAdapter);
        mRcShareList.addItemDecoration(new SpacesItemDecoration());

        mLocationLayout.setOnClickListener(this);
        mJoinActivityTV.setOnClickListener(this);
        mShareLL.setOnClickListener(this);
        mShareTypeTv.setOnClickListener(this);

        mShareTypeTv.setText(mSelectedShareString);
        mShareTypeTv.setText(mSelectedShareString);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView(){
        mRootLayout = (RelativeLayout) findViewById(R.id.rl_videoshare_root);
        mLocationIv = (ImageView) findViewById(R.id.iv_share_location);
        mLocationTv = (TextView) findViewById(R.id.tv_share_location);
        mLocationLayout = (LinearLayout) findViewById(R.id.ll_share_location);
        mRcShareList = (RecyclerView) findViewById(R.id.rv_share_list);
        mJoinActivityTV = (TextView) findViewById(R.id.tv_share_joniActivity);
        mShareTypeTv = (TextView) findViewById(R.id.tv_share_videoType);
        mShareLL = (LinearLayout) findViewById(R.id.ll_share_now);
    }

    @Override
    public void onClick(View view) {
        int vId = view.getId();
        if(vId == R.id.ll_share_location){
            click_location();
        }else if(vId == R.id.tv_share_videoType){
            Intent intent = new Intent (VideoShareActivity.this, ShareTypeActivity.class);
            intent.putExtra(ShareTypeActivity.SHARE_TYPE_KEY,mSelectedShareType);
            VideoShareActivity.this.startActivity(intent);
        }else if(vId == R.id.tv_share_joniActivity){
            if (!UserUtils.isNetDeviceAvailable(VideoShareActivity.this)) {
                GolukUtils.showToast(VideoShareActivity.this, VideoShareActivity.this.getResources().getString(R.string.user_net_unavailable));
                return;
            }

            Intent intent = new Intent(VideoShareActivity.this, PromotionActivity.class);
            if (mSelectedPromotionItem != null) {
                intent.putExtra(PromotionActivity.PROMOTION_SELECTED_ITEM, mSelectedPromotionItem.activityid);
            }
            VideoShareActivity.this.startActivity(intent);
        }else if(vId == R.id.ll_share_now){
            startShare();
        }
    }

    private void click_location() {
        switch (mLocationState) {
            case LOCATION_STATE_ING:
                // 当前状态是定位中，用户点击，直接再次发起定位
                GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
                break;
            case LOCATION_STATE_SUCCESS:
                // 定位成功
                // 需要弹出框让用户确认
                showDealDialog();
                break;
            case LOCATION_STATE_FAILED:
            case LOCATION_STATE_FORBID:
                // 未定位
                mLocationState = LOCATION_STATE_ING;
                refreshLocationUI();
                GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
                break;
        }
    }

    private void startShare(){
        if (isSharing) {
            return;
        }
        isSharing = true;
        this.mUploadVideo.setUploadInfo(mVideoPath, mVideoType, videoName);
        mShareLoading.switchState(ShareLoading.STATE_UPLOAD);
    }

    private void refreshLocationUI() {
        switch (mLocationState) {
            case LOCATION_STATE_ING:
                // 改图标
                mLocationIv.setImageResource(R.drawable.share_weizhi_failed);
                mLocationTv.setText(R.string.share_str_no_location);
                mLocationTv.setTextColor(Color.parseColor("#909090"));
                break;
            case LOCATION_STATE_SUCCESS:
                // 改变图标
                mLocationIv.setImageResource(R.drawable.share_weizhi_success);
                mLocationTv.setText(mLocationAddress);
                mLocationTv.setTextColor(Color.parseColor("#0080ff"));
                break;
            case LOCATION_STATE_FAILED:
            case LOCATION_STATE_FORBID:
                mLocationIv.setImageResource(R.drawable.share_weizhi_failed);
                mLocationTv.setText(R.string.get_current_location);
                mLocationTv.setTextColor(Color.parseColor("#909090"));
                break;
            default:
                break;
        }
    }

    private void showDealDialog() {
        if (null != mStartShareDialog) {
            mStartShareDialog.dismiss();
            mStartShareDialog = null;
        }
        mStartShareDialog = new StartShareFunctionDialog(VideoShareActivity.this, this);
        mStartShareDialog.show();
    }

    public void showPopup() {
        View contentView = this.getLayoutInflater().inflate(R.layout.promotion_popup_hint, null);

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popWidth = contentView.getMeasuredWidth();
        int popHeight = contentView.getMeasuredHeight();
        mPopupWindow = new PopupWindow(contentView, popWidth, popHeight);
        contentView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                mPopupWindow.dismiss();
                return false;
            }
        });

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        int[] location = new int[2];
        mLocationTv.getLocationOnScreen(location);

        mPopupWindow.showAtLocation(mLocationTv, Gravity.NO_GRAVITY, location[0], location[1] - popHeight);
        isPopup = false;
        GolukFileUtils.saveBoolean(GolukFileUtils.SHOW_PROMOTION_POPUP_FLAG, false);
    }

    @Override
    public void CallBack_Del(int event, Object data) {
        if (1 == event) {
            // 重新定位
            mLocationState = LOCATION_STATE_ING;
            refreshLocationUI();
            GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
        } else if (2 == event) {
            // 删除定位
            mLocationState = LOCATION_STATE_FORBID;
            refreshLocationUI();
        }
    }

    @Override
    public void CallBack_UploadVideo(int event, Object obj) {
        if (isExit) {
            return;
        }
        switch (event) {
            case EVENT_EXIT:
                exit();
                break;
            case EVENT_UPLOAD_SUCESS:
                // 文件上传成功，请求分享连接
                requestShareInfo();
                break;
            case EVENT_PROCESS:
                if (null != obj && null != mShareLoading) {
                    final int process = (Integer) obj;
                    mShareLoading.setProcess(process);
                }
                break;
        }
    }

    // 请求分享信息
    private void requestShareInfo() {
        mShareLoading.switchState(ShareLoading.STATE_GET_SHARE);
        final String t_vid = this.mUploadVideo.getVideoId();
        final String t_signTime = this.mUploadVideo.getSignTime();
        final String t_type = String.valueOf(mVideoType);
        final String selectTypeJson = JsonUtil.createShareType(String.valueOf(mSelectedShareType));
        final String desc = "这是测试用的分享内容";
        final String isSeque = "1";
        final String t_location = mLocationAddress;
        PromotionSelectItem item = mSelectedPromotionItem;
        String channelid = "";
        String activityid = "";
        String activityname = "";

        if (item != null) {
            channelid = item.channelid;
            activityid = item.activityid;
            activityname = item.activitytitle;
        }
        GetShareAddressRequest request = new GetShareAddressRequest(IPageNotifyFn.PageType_Share, this);
        request.get(t_vid, t_type, desc, selectTypeJson, isSeque, videoCreateTime, t_signTime, channelid, activityid,
                activityname, t_location, videoFrom);
    }
    private void exit() {
        if (isExit) {
            return;
        }
        isExit = true;
        //isBack = true;
        mBaseHandler.removeCallbacksAndMessages(null);
        stopProgressThread();

        mUploadVideo.setExit();
        mUploadVideo = null;

        this.toInitState();
        mShareLoading = null;
        finish();
    }

    /**
     * 停止进度条线程
     */
    private void stopProgressThread() {
        Thread tmpThread = mProgressThread;
        mProgressThread = null;
        if (tmpThread != null) {
            tmpThread.interrupt();
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        switch (requestType) {
            case IPageNotifyFn.PageType_GetPromotion:
                PromotionModel data = (PromotionModel) result;
                if (data != null && data.success) {

                    ArrayList<PromotionSelectItem> list = new ArrayList<PromotionSelectItem>(2);
                    if (data.data.priorityacts != null) {
                        for (PromotionItem item : data.data.priorityacts) {
                            PromotionSelectItem promotionSelectItem = new PromotionSelectItem();
                            promotionSelectItem.activityid = item.id;
                            promotionSelectItem.activitytitle = item.name;
                            list.add(promotionSelectItem);
                        }
                    }

                }
                break;
            case IPageNotifyFn.PageType_Share:
                ShareDataFullBean shareDataFull = (ShareDataFullBean) result;
                if (shareDataFull != null && shareDataFull.success) {
                    videoShareCallBack(shareDataFull.data);
                } else {
                    getShareFailed();
                }
                break;
        }
    }

    private void getShareFailed() {
        GolukUtils.showToast(this, this.getString(R.string.str_get_share_address_fail));
        toInitState();
    }
    // 当分享成功，失败　或某一环节出现失败后，还原到原始状态，再进行分享
    private void toInitState() {
        isSharing = false;
        if (isExit) {
            return;
        }
        if (null != mShareLoading) {
            mShareLoading.hide();
            mShareLoading.switchState(ShareLoading.STATE_NONE);
        }
    }
    /**
     * 本地视频分享回调
     */
    public void videoShareCallBack(ShareDataBean shareData) {
        if (mShareLoading == null || mUploadVideo == null) {
            return;
        }
        mShareLoading.switchState(ShareLoading.STATE_SHAREING);
        if (shareData == null) {
            getShareFailed();
            return;
        }

        final String title = this.getString(R.string.str_video_edit_share_title);
        final String describe = "这是测试分享内容";
        final String sinaTxt = this.getString(R.string.str_share_board_real_desc);

        ThirdShareBean bean = new ThirdShareBean();
        bean.surl = shareData.shorturl;
        bean.curl = shareData.coverurl;
        bean.db = describe;
        bean.tl = title;
        bean.bitmap = mUploadVideo.getThumbBitmap();
        bean.realDesc = sinaTxt;
        bean.videoId = this.mUploadVideo.getVideoId();
        bean.mShareType = "1";
        bean.filePath = mVideoPath;
        //mShareDealTool.toShare(bean);
    }
    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        /**
         * 是否是最后一行
         * @param position
         * @return
         */
        private boolean isTheLastRow(int position){
            int count = mSharePlatformAdapter.getItemCount();
            int rowNum ;
            if(count % SHARE_PLATFORM_COLUMN_NUMBERS == 0){
                rowNum = count / SHARE_PLATFORM_COLUMN_NUMBERS;
            }else{
                rowNum = count / SHARE_PLATFORM_COLUMN_NUMBERS + 1;
            }

            if((position + 1) % SHARE_PLATFORM_COLUMN_NUMBERS == 0 && (position + 1 ) / SHARE_PLATFORM_COLUMN_NUMBERS == rowNum){
                return true;
            }else if((position + 1) % SHARE_PLATFORM_COLUMN_NUMBERS != 0 && (position + 1 ) / SHARE_PLATFORM_COLUMN_NUMBERS == (rowNum -1)){
                return true;
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            int spanIndex = ((GridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
            int position = parent.getChildAdapterPosition(view);

            if (spanIndex == 0) {
                outRect.left = 0;
                outRect.right = 0;

            } else {//if you just have 2 span . Or you can use (staggeredGridLayoutManager.getSpanCount()-1) as last span
                outRect.left = 2;
                outRect.right = 0;
            }
            if(isTheLastRow(position)){
                outRect.bottom = 0;
            }else{
                outRect.bottom = 2;
            }

        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            c.drawColor(Color.parseColor("#404246"));
            c.save();
            super.onDraw(c, parent, state);
        }
    }
}
