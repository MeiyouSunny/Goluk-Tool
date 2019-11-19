package com.mobnote.golukmain.startshare;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.RSRuntimeException;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventGetShareSignTokenInvalid;
import com.mobnote.eventbus.EventShareCompleted;
import com.mobnote.eventbus.EventSharetypeSelected;
import com.mobnote.eventbus.SharePlatformSelectedEvent;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.helper.QCloudHelper;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.GetBaiduAddress;
import com.mobnote.golukmain.newest.IDialogDealFn;
import com.mobnote.golukmain.photoalbum.PhotoAlbumPlayer;
import com.mobnote.golukmain.player.MovieActivity;
import com.mobnote.golukmain.promotion.PromotionActivity;
import com.mobnote.golukmain.promotion.PromotionData;
import com.mobnote.golukmain.promotion.PromotionListRequest;
import com.mobnote.golukmain.promotion.PromotionModel;
import com.mobnote.golukmain.promotion.PromotionSelectItem;
import com.mobnote.golukmain.startshare.bean.ShareTypeBean;
import com.mobnote.golukmain.startshare.bean.VideoSaveDataBean;
import com.mobnote.golukmain.startshare.bean.VideoSaveRetBean;
import com.mobnote.golukmain.thirdshare.SharePlatformAdapter;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.thirdshare.ThirdShareTool;
import com.mobnote.golukmain.thirdshare.bean.SharePlatform;
import com.mobnote.log.app.LogConst;
import com.mobnote.map.LngLat;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukFileUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.util.glideblur.FastBlur;
import com.mobnote.util.glideblur.RSBlur;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import cn.com.mobnote.eventbus.EventShortLocationFinish;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.baidu.BaiduLocation;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * Created by wangli on 2016/5/10.
 */
public class VideoShareActivity extends BaseActivity implements View.OnClickListener, IDialogDealFn, IUploadVideoFn, IRequestResultListener {

    private final int SHARE_PLATFORM_COLUMN_NUMBERS = 3;
    private int mCurrSelectedSharePlatform;
    private RecyclerView mRcShareList;
    private SharePlatformAdapter mSharePlatformAdapter;
    private LinearLayout mLocationLayout;
    private TextView mLocationTv;
    private ImageView mLocationIv;
    private String mLocationAddress;

    /**
     * 定位状态 0 表示定位中, 1 表示定位成功, 2表示点击定位, 3 表示用户删除了位置
     */
    private int mLocationState = 0;
    /**
     * 定位中
     */
    public static final int LOCATION_STATE_ING = 0;
    /**
     * 定位成功
     */
    public static final int LOCATION_STATE_SUCCESS = 1;
    /**
     * 定位失败
     */
    public static final int LOCATION_STATE_FAILED = 2;
    /**
     * 用户禁止使用位置
     */
    public static final int LOCATION_STATE_FORBID = 3;
    private StartShareFunctionDialog mStartShareDialog = null;

    RelativeLayout mRootLayout;
    private TextView mShareTypeTv;
    private TextView mJoinActivityTV;

    private EditText mShareDiscribleEt;
    private String mShareDiscrible;
    private ImageView mBackIv;
    private ImageView mVideoThumbIv;
    private ImageView mVideoThumbBlurIv;
    private PromotionSelectItem mSelectedPromotionItem;
    private int mSelectedShareType;
    private String mSelectedShareString;
    private boolean shouldDelete;//分享完成后是否应该删除
    private int mVideoDuration;
    private String mVideoQuality;

    PopupWindow mPopupWindow;
    boolean isPopup;

    private String videoFrom = "";
    /**
     * 分享的视频创建时间
     **/
    private String videoCreateTime = "";
    /**
     * 视频路径
     */
    private String mVideoPath;
    /**
     * 视频类型
     */
    private int mVideoType;
    /**
     * 分享的视频名称
     */
    private String videoName = "";

    private UploadVideo mUploadVideo = null;
    private boolean isExiting = false;
    private ShareLoading mShareLoading = null;

    LinearLayout mShareLL;
    TextView mShareTv;
    boolean isSharing;
    private Thread mProgressThread = null;

    private ThirdShareTool mThirdShareTool;
    private List<PromotionData> mPromotionList;
    private boolean isShowNew;
    private TextView mNewActivityTv;

    Bitmap mVidThumbnail;
    Bitmap mVidBlurBitmap;

    private String mActivityname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_share);
        EventBus.getDefault().register(this);
        // 开始定位
        BaiduLocation.getInstance().startLocation();

        initData(savedInstanceState);

        initView();

        setupView();

        loadPromotionData();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNewActivityTv != null) {
            if (isShowNew) {
                mNewActivityTv.setVisibility(View.VISIBLE);
            } else {
                mNewActivityTv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        EventBus.getDefault().unregister(this);
        super.onDestroy();
        // 停止定位
        BaiduLocation.getInstance().stopLocation();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("vidPath", mVideoPath);
        outState.putInt("vidType", mVideoType);
        outState.putString("filename", videoName);
        outState.putBoolean("shouldDelete", shouldDelete);
        outState.putInt("video_duration", mVideoDuration);
        outState.putString("video_quality", mVideoQuality);
        if (mSelectedPromotionItem != null) {
            outState.putSerializable(PhotoAlbumPlayer.ACTIVITY_INFO, mSelectedPromotionItem);
        }
        super.onSaveInstanceState(outState);
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mVideoPath = getIntent().getStringExtra("vidPath");
            videoName = getIntent().getStringExtra("filename");
            mVideoType = getIntent().getIntExtra("vidType", 1);
            shouldDelete = getIntent().getBooleanExtra("shouldDelete", false);
            mVideoDuration = getIntent().getIntExtra("video_duration", 0);
            mVideoQuality = getIntent().getStringExtra("video_quality");
            mSelectedPromotionItem = (PromotionSelectItem) getIntent().getSerializableExtra(PhotoAlbumPlayer.ACTIVITY_INFO);
        } else {
            mVideoPath = savedInstanceState.getString("vidPath");
            mVideoType = savedInstanceState.getInt("vidType", 2);
            videoName = savedInstanceState.getString("filename");
            shouldDelete = savedInstanceState.getBoolean("shouldDelete");
            mVideoDuration = savedInstanceState.getInt("video_duration", 0);
            mVideoQuality = savedInstanceState.getString("video_quality");
            mSelectedPromotionItem = (PromotionSelectItem) savedInstanceState.getSerializable(PhotoAlbumPlayer.ACTIVITY_INFO);
        }

        mCurrSelectedSharePlatform = SharePlatform.SHARE_PLATFORM_NULL;

        mSelectedShareType = ShareTypeBean.SHARE_TYPE_SSP;
        mSelectedShareString = "# " + getResources().getString(R.string.share_str_type_ssp);

        getVideoCreateTime();
    }

    public void onEventMainThread(EventShortLocationFinish event) {
        if (null == event) {
            return;
        }

        if (event.getShortAddress() != null && mLocationState != LOCATION_STATE_FORBID) {
            mLocationAddress = event.getShortAddress();
            mLocationState = LOCATION_STATE_SUCCESS;
            refreshLocationUI();
        }

        // 停止定位
        BaiduLocation.getInstance().stopLocation();
    }

    public void onEventMainThread(EventSharetypeSelected event) {
        if (event != null) {
            this.mSelectedShareType = event.getShareType();
            this.mSelectedShareString = "# " + event.getShareName();
            mShareTypeTv.setText(mSelectedShareString);
        }
    }

    public void onEventMainThread(EventGetShareSignTokenInvalid event) {
        if (event != null) {
            toInitState();
            GolukUtils.startLoginActivity(this);
        }
    }

    public void onEventMainThread(EventShareCompleted event) {
        if (event != null) {
            if (mCurrSelectedSharePlatform == SharePlatform.SHARE_PLATFORM_COPYLINK) {
                Toast.makeText(this, getString(R.string.str_copy_link_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.str_share_success), Toast.LENGTH_SHORT).show();
            }
            toInitState();
            exit();
        }
    }

    public void onEventMainThread(PromotionSelectItem event) {

        if (event == null) {
            return;
        }
        if (mSelectedPromotionItem != null && mSelectedPromotionItem.activityid.equals(event.activityid)) {
            //点击相同活动则取消
            mSelectedPromotionItem = null;
            mJoinActivityTV.setText(getResources().getString(R.string.join_activity));
            mJoinActivityTV.setTextColor(Color.parseColor("#818385"));
            return;
        } else {
            mSelectedPromotionItem = event;
        }

        String activityTitle = mSelectedPromotionItem.activitytitle;
        if (!TextUtils.isEmpty(activityTitle) && activityTitle.length() > 10) {
            mJoinActivityTV.setText(activityTitle.substring(0, 10) + "...");
        } else {
            mJoinActivityTV.setText(mSelectedPromotionItem.activitytitle);
        }
        mJoinActivityTV.setTextColor(Color.parseColor("#0080ff"));
    }

    public void onEventMainThread(SharePlatformSelectedEvent event) {
        if (event != null) {
            this.mCurrSelectedSharePlatform = event.getSharePlatform();
            if (mCurrSelectedSharePlatform == SharePlatform.SHARE_PLATFORM_NULL ||
                    mCurrSelectedSharePlatform == SharePlatform.SHARE_PLATFORM_COPYLINK) {
                mShareTv.setText(getString(R.string.share_to_jishe));
            } else {
                mShareTv.setText(getString(R.string.share_btn_text));
            }
        }
    }

    private void loadPromotionData() {
        PromotionListRequest request = new PromotionListRequest(IPageNotifyFn.PageType_GetPromotion, this);
        request.get();
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp;
        private int editStart;
        private int editEnd;

        @Override
        public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            temp = s;
        }

        @Override
        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            if (s == null) {
                mShareDiscrible = null;
            } else {
                mShareDiscrible = s.toString().trim();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            editStart = mShareDiscribleEt.getSelectionStart();
            editEnd = mShareDiscribleEt.getSelectionEnd();
            if (temp.length() > 50) {
                Toast.makeText(VideoShareActivity.this,
                        VideoShareActivity.this.getString(R.string.str_content_out), Toast.LENGTH_SHORT).show();
                s.delete(editStart - 1, editEnd);
                int tempSelection = editStart;
                mShareDiscribleEt.setText(s);
                mShareDiscribleEt.setSelection(tempSelection);
            }
        }
    };

    /**
     * 获取视频创建时间
     */
    private void getVideoCreateTime() {
        File f = new File(mVideoPath);
        if (f != null) {
            Calendar cal = Calendar.getInstance();
            long time = f.lastModified();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            cal.setTimeInMillis(time);
            videoCreateTime = formatter.format(cal.getTime());
        }
    }

    private class GetThumbAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {

            if (mVidThumbnail == null) {
                mVidThumbnail = GolukUtils.createVideoThumbnail(mVideoPath);
            }

            if (mVidThumbnail != null) {
                mVidBlurBitmap = mVidThumbnail.copy(mVidThumbnail.getConfig(), true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    try {
                        mVidBlurBitmap = RSBlur.blur(VideoShareActivity.this, mVidBlurBitmap, 50);
                    } catch (RSRuntimeException e) {
                        mVidBlurBitmap = FastBlur.blur(mVidBlurBitmap, 50, true);
                    }
                } else {
                    mVidBlurBitmap = FastBlur.blur(mVidBlurBitmap, 50, true);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            if (mVidThumbnail != null && mVideoThumbIv != null) {
                mVideoThumbIv.setImageBitmap(mVidThumbnail);
            }

            if (mVidBlurBitmap != null && mVideoThumbBlurIv != null) {
                mVideoThumbBlurIv.setImageBitmap(mVidBlurBitmap);
            }
        }
    }

    private void setupView() {

        new GetThumbAsyncTask().execute();

        mUploadVideo = new UploadVideo(this, GolukApplication.getInstance(), videoName);
        mUploadVideo.setListener(this);

        mShareLoading = new ShareLoading(this, mRootLayout);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, SHARE_PLATFORM_COLUMN_NUMBERS);
        mRcShareList.setLayoutManager(gridLayoutManager);
        mSharePlatformAdapter = new SharePlatformAdapter(this);
        mRcShareList.setAdapter(mSharePlatformAdapter);
        mRcShareList.addItemDecoration(new SpacesItemDecoration());

        mLocationLayout.setOnClickListener(this);
        if (null != mSelectedPromotionItem && mSelectedPromotionItem.type == 0) {
            // only activity can re-select promotion
            mJoinActivityTV.setOnClickListener(null);
        } else {
            mJoinActivityTV.setOnClickListener(this);
        }

        mShareLL.setOnClickListener(this);
        mShareTypeTv.setOnClickListener(this);
        mBackIv.setOnClickListener(this);
        mVideoThumbIv.setOnClickListener(this);
        mShareDiscribleEt.addTextChangedListener(mTextWatcher);

        mShareTypeTv.setText(mSelectedShareString);
        mShareTypeTv.setText(mSelectedShareString);
        if (mSelectedPromotionItem != null) {
            String activityTitle = mSelectedPromotionItem.activitytitle;
            if (!TextUtils.isEmpty(activityTitle) && activityTitle.length() > 10) {
                mJoinActivityTV.setText(activityTitle.substring(0, 10) + "...");
            } else {
                mJoinActivityTV.setText(mSelectedPromotionItem.activitytitle);
            }
            mJoinActivityTV.setTextColor(Color.parseColor("#0080ff"));
        } else {
            mJoinActivityTV.setText(getResources().getString(R.string.join_activity));
            mJoinActivityTV.setTextColor(Color.parseColor("#818385"));
        }
    }

    private void initView() {
        mBackIv = (ImageView) findViewById(R.id.iv_videoshare_back);
        mShareDiscribleEt = (EditText) findViewById(R.id.et_share_discrible);
        mRootLayout = (RelativeLayout) findViewById(R.id.rl_videoshare_root);
        mLocationIv = (ImageView) findViewById(R.id.iv_share_location);
        mLocationTv = (TextView) findViewById(R.id.tv_share_location);
        mLocationLayout = (LinearLayout) findViewById(R.id.ll_share_location);
        mRcShareList = (RecyclerView) findViewById(R.id.rv_share_list);
        mJoinActivityTV = (TextView) findViewById(R.id.tv_share_joniActivity);
        mShareTypeTv = (TextView) findViewById(R.id.tv_share_videoType);
        mShareLL = (LinearLayout) findViewById(R.id.ll_share_now);
        mVideoThumbIv = (ImageView) findViewById(R.id.iv_videoshare_videothumb);
        mVideoThumbBlurIv = (ImageView) findViewById(R.id.iv_videoshare_blur);
        mShareTv = (TextView) findViewById(R.id.tv_share);
        mNewActivityTv = (TextView) findViewById(R.id.tv_share_newActivity);

    }

    @Override
    public void onClick(View view) {
        int vId = view.getId();
        if (vId == R.id.iv_videoshare_back) {
            exit();
        } else if (vId == R.id.ll_share_location) {
            click_location();
        } else if (vId == R.id.tv_share_videoType) {
            Intent intent = new Intent(VideoShareActivity.this, ShareTypeActivity.class);
            intent.putExtra(ShareTypeActivity.SHARE_TYPE_KEY, mSelectedShareType);
            VideoShareActivity.this.startActivity(intent);
        } else if (vId == R.id.tv_share_joniActivity) {
            if (GolukApplication.getInstance().isIpcLoginSuccess) {
                showToast(getString(R.string.please_change_network_from_car_recorder_to_others));
                return;
            }
            isShowNew = false;
            if (!UserUtils.isNetDeviceAvailable(VideoShareActivity.this)) {
                GolukUtils.showToast(VideoShareActivity.this, VideoShareActivity.this.getResources().getString(R.string.user_net_unavailable));
                return;
            }
            Intent intent = new Intent(VideoShareActivity.this, PromotionActivity.class);
            if (mSelectedPromotionItem != null) {
                intent.putExtra(PromotionActivity.PROMOTION_SELECTED_ITEM, mSelectedPromotionItem.activityid);
            }
            VideoShareActivity.this.startActivity(intent);
        } else if (vId == R.id.ll_share_now) {
            startShare();
        } else if (vId == R.id.iv_videoshare_videothumb) {
            Intent intent = new Intent(VideoShareActivity.this, MovieActivity.class);
            intent.putExtra("from", "local");
            intent.putExtra("image", "");
            intent.putExtra("path", mVideoPath);
            VideoShareActivity.this.startActivity(intent);
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
                // 开始定位
                BaiduLocation.getInstance().startLocation();
                break;
        }
    }

    private void startShare() {
        if (!GolukUtils.isNetworkConnected(VideoShareActivity.this)) {
            showToast(VideoShareActivity.this.getString(R.string.network_error));
            return;
        }
        if (GolukApplication.getInstance().isIpcLoginSuccess) {
            showToast(getString(R.string.please_change_network_from_car_recorder_to_others));
            return;
        }
        if (!GolukApplication.getInstance().isUserLoginSucess) {
            GolukUtils.startLoginActivity(this);
            return;
        }
        if (isSharing) {
            return;
        }
        isSharing = true;
        if (mUploadVideo == null) {
            return;
        }
        this.mUploadVideo.setUploadInfo(mVideoPath, mVideoType, videoName);
        if (null == mShareLoading) {
            mShareLoading = new ShareLoading(this, mRootLayout);
        }
        mShareLoading.showLoadingLayout();
        mShareLoading.switchState(ShareLoading.STATE_UPLOAD);

        XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Start upload share video");
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

    @Override
    public void CallBack_Del(int event, Object data) {
        if (1 == event) {
            // 重新定位
            mLocationState = LOCATION_STATE_ING;
            refreshLocationUI();
            GetBaiduAddress.getInstance().searchAddress(LngLat.lat, LngLat.lng);
            // 开始定位
            BaiduLocation.getInstance().startLocation();
        } else if (2 == event) {
            // 删除定位
            mLocationState = LOCATION_STATE_FORBID;
            mLocationAddress = null;
            refreshLocationUI();
        }
    }

    @Override
    public void CallBack_UploadVideo(int event, Object obj) {
        if (isExiting) {
            return;
        }
        switch (event) {
            case EVENT_EXIT:

                toInitState();
                break;
            case EVENT_UPLOAD_SUCESS:
                // 文件上传成功，请求分享连接
                requestShareInfo((Map<String, String>) obj);
                break;
            case EVENT_PROCESS:
                if (null != obj && null != mShareLoading) {
                    final int process = (Integer) obj;
                    mShareLoading.setProcess(process);
                    GolukDebugUtils.i("process", "process: " + process);
                }
                break;
        }
    }

    public void shareCallBack(boolean isSuccess) {

        EventBus.getDefault().post(new EventShareCompleted(isSuccess));
    }

    // 请求分享信息
    private void requestShareInfo(Map<String, String> urls) {
        String photoUrl = "";
        String videoUrl = "";
        if (urls.containsKey(QCloudHelper.PHOTO_BUCKET)) {
            photoUrl = urls.get(QCloudHelper.PHOTO_BUCKET);
        }
        if (urls.containsKey(QCloudHelper.VIDEO_BUCKET)) {
            videoUrl = urls.get(QCloudHelper.VIDEO_BUCKET);
        }
        mShareLoading.switchState(ShareLoading.STATE_GET_SHARE);
        final String t_vid = this.mUploadVideo.getVideoId();
        final String t_signTime = this.mUploadVideo.getSignTime();
        final String t_type = String.valueOf(mVideoType);
        final String selectTypeJson = JsonUtil.createShareType(String.valueOf(mSelectedShareType));
        final String desc = (TextUtils.isEmpty(mShareDiscrible) ? this.getString(R.string.default_comment) : mShareDiscrible);
        final String isSquare = "1";
        final String t_location = (TextUtils.isEmpty(mLocationAddress) ? "" : mLocationAddress);

        String activityid = "";
        String tagId = "";

        if (mSelectedPromotionItem != null) {
            if (mSelectedPromotionItem.type == 0) {
                tagId = mSelectedPromotionItem.activityid;
            } else {
                activityid = mSelectedPromotionItem.activityid;
            }

            mActivityname = mSelectedPromotionItem.activitytitle;
        }
        GetVideoSaveAddressRequest request = new GetVideoSaveAddressRequest(IPageNotifyFn.PageType_Share, this);

        request.get(t_vid, t_type, desc, selectTypeJson, isSquare, videoCreateTime, t_signTime, activityid,
                t_location, "", tagId, "", mVideoQuality, "", photoUrl, videoUrl);

        XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Request share short url info");
    }

    private void exit() {
        if (isExiting) {
            return;
        }
        isExiting = true;
        //原视频和经过视频后处理的视频都不删除，直接加片尾的视频需要删除
        if (shouldDelete) {
            File file = new File(mVideoPath);
            if (file.exists()) {
                file.delete();
            }
        }

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

    private void checkNewActivity() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        String preMd5String = GolukFileUtils.loadString(GolukFileUtils.PROMOTION_LIST_STRING, "");
        String newMd5String = "";
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(mPromotionList);
            byte[] content = bos.toByteArray();
            newMd5String = GolukUtils.compute32(content);
            GolukFileUtils.saveString(GolukFileUtils.PROMOTION_LIST_STRING, newMd5String);
        } catch (IOException ex) {

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        if ((TextUtils.isEmpty(preMd5String) || !preMd5String.equalsIgnoreCase(newMd5String)) &&
                ((null != mSelectedPromotionItem && mSelectedPromotionItem.type == 1) || (null == mSelectedPromotionItem))) {
            isShowNew = true;
            mNewActivityTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        switch (requestType) {
            case IPageNotifyFn.PageType_GetPromotion:
                PromotionModel data = (PromotionModel) result;
                if (data != null && data.success) {
                    mPromotionList = data.data.PromotionList;
                    if (mPromotionList != null && mPromotionList.size() > 0) {
                        checkNewActivity();
                    }
                }
                break;
            case IPageNotifyFn.PageType_Share:
                VideoSaveRetBean shareDataFull = (VideoSaveRetBean) result;
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Share short url info:%s", shareDataFull);
                if (shareDataFull == null) {
                    videoShareCallBack(null);
                    return;
                }
                if (shareDataFull != null && shareDataFull.data != null) {
                    if (!GolukUtils.isTokenValid(shareDataFull.data.result)) {
                        GolukUtils.startLoginActivity(VideoShareActivity.this);
                        toInitState();
                        return;
                    }
                }
                String shareState = "";
                if (shareDataFull != null && shareDataFull.success) {
                    shareState = this.getString(R.string.str_zhuge_share_video_state_success);
                    videoShareCallBack(shareDataFull.data);
                } else {
                    shareState = shareDataFull.msg;
                    GolukUtils.showToast(this, this.getString(R.string.str_get_share_address_fail));
                    videoShareCallBack(null);
                }
//                ZhugeUtils.eventShareVideo(this, mSelectedShareType + "", mVideoQuality, mVideoDuration, mShareDiscrible, mCurrSelectedSharePlatform,
//                        mActivityname, shareState);
                zhugeShareVideo(shareState);
                break;
        }
    }

    // 当分享成功，失败或某一环节出现失败后，还原到原始状态，再进行分享
    private void toInitState() {
        isSharing = false;
        if (isExiting) {
            return;
        }
        if (null != mShareLoading) {
            mShareLoading.hide();
            mShareLoading.switchState(ShareLoading.STATE_NONE);
            mShareLoading = null;
        }
    }

    /**
     * 本地视频分享回调
     */
    public void videoShareCallBack(VideoSaveDataBean shareData) {
        if (shareData == null) {
            GolukUtils.showToast(this, this.getString(R.string.str_get_share_address_fail));
            toInitState();
            return;
        }

        if (mCurrSelectedSharePlatform == SharePlatform.SHARE_PLATFORM_NULL) {
            EventBus.getDefault().post(new EventShareCompleted(true));
            return;
        }

        if (mCurrSelectedSharePlatform == SharePlatform.SHARE_PLATFORM_COPYLINK) {
            ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(ClipData.newPlainText("goluk", shareData.shorturl));
            EventBus.getDefault().post(new EventShareCompleted(true));
            return;
        }

        if (mShareLoading == null || mUploadVideo == null) {
            toInitState();
            return;
        }
        mShareLoading.switchState(ShareLoading.STATE_SHAREING);

        toInitState();
        final String title = this.getString(R.string.str_video_edit_share_title);
        final String describe = (TextUtils.isEmpty(mShareDiscrible) ? this.getString(R.string.default_comment) : mShareDiscrible);
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
        bean.from = this.getString(R.string.str_zhuge_share_instantly);
        mThirdShareTool = new ThirdShareTool(this, new SharePlatformUtil(this), bean.surl, bean.curl, bean.db, bean.tl,
                bean.bitmap, bean.realDesc, bean.videoId, bean.mShareType, bean.filePath, bean.from);

        if (mSharePlatformAdapter != null && mSharePlatformAdapter.getCurrSelectedPlatformType() != SharePlatform.SHARE_PLATFORM_NULL) {
            mSharePlatformAdapter.getCurrSelectedPlatformzBean().startShare(this, new SharePlatformUtil(this), bean);
        }

        XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Open share way list");
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {

        /**
         * 是否是最后一行
         *
         * @param position
         * @return
         */
        private boolean isTheLastRow(int position) {
            int count = mSharePlatformAdapter.getItemCount();
            int rowNum;
            if (count % SHARE_PLATFORM_COLUMN_NUMBERS == 0) {
                rowNum = count / SHARE_PLATFORM_COLUMN_NUMBERS;
            } else {
                rowNum = count / SHARE_PLATFORM_COLUMN_NUMBERS + 1;
            }

            if ((position + 1) % SHARE_PLATFORM_COLUMN_NUMBERS == 0 && (position + 1) / SHARE_PLATFORM_COLUMN_NUMBERS == rowNum) {
                return true;
            } else if ((position + 1) % SHARE_PLATFORM_COLUMN_NUMBERS != 0 && (position + 1) / SHARE_PLATFORM_COLUMN_NUMBERS == (rowNum - 1)) {
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
            if (isTheLastRow(position)) {
                outRect.bottom = 0;
            } else {
                outRect.bottom = 2;
            }

        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            c.drawColor(Color.parseColor("#242629"));
            c.save();
            super.onDraw(c, parent, state);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != mSharePlatformAdapter && null != mSharePlatformAdapter.mSharePlatform) {
            mSharePlatformAdapter.mSharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void zhugeShareVideo(String state) {
        ZhugeUtils.eventShareVideo(this, mSelectedShareType + "", mVideoQuality, mVideoDuration, mShareDiscrible, mCurrSelectedSharePlatform,
                mActivityname, state);
    }
}
