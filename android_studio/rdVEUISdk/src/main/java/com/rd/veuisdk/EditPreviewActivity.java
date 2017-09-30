package com.rd.veuisdk;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidStateException;
import com.rd.vecore.listener.ExportListener;
import com.rd.vecore.models.AspectRatioFitMode;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.vecore.models.Transition;
import com.rd.vecore.models.TransitionType;
import com.rd.vecore.models.VideoConfig;
import com.rd.vecore.utils.ExportUtils;
import com.rd.veuisdk.adapter.DragMediaAdapter;
import com.rd.veuisdk.adapter.DragMediaAdapter.DragItemListener;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.model.VideoObjectPack;
import com.rd.veuisdk.ui.DraggableAddGridView;
import com.rd.veuisdk.ui.DraggableGridView;
import com.rd.veuisdk.ui.DraggedTrashLayout;
import com.rd.veuisdk.ui.DraggedView;
import com.rd.veuisdk.ui.ExtListItemView;
import com.rd.veuisdk.ui.HorizontalProgressDialog;
import com.rd.veuisdk.ui.PriviewLayout;
import com.rd.veuisdk.ui.PriviewLinearLayout;
import com.rd.veuisdk.ui.ProportionDialog;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.ui.SubFunctionUtils;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 片段编辑页
 */
public class EditPreviewActivity extends BaseActivity {
    private static final String TAG = "EditPreviewActivity";

    public static final String ACTION_APPEND = "action_append";
    static final String APPEND_IMAGE = "edit.addmenu.addimage";
    public static final String TEMP_FILE = "temp_file";
    public static float mCurAspect;

    private SplitHandler mSplitHandler;
    private DragMediaAdapter mAdapterScene;
    private String mTempRecfile = null;
    private ProportionDialog mProportionDialog;
    private float mCurProportion = 0;
    private float mLastPlayPostion;
    private boolean mIsLongClick;
    private boolean mBackDiaglog = false;
    private boolean mHasChanged = false;
    private boolean mIsUseCustomUI = false;

    private ArrayList<Scene> mSceneList = new ArrayList<>();
    private int mProportionStatus;
    private VirtualVideo mVirtualVideo;
    private Scene mCurrentScene;

    VirtualVideoView mMediaPlayer;
    PreviewFrameLayout mVideoPreview;
    ImageView mIvVideoPlayState;
    TextView mTvVideoDuration;
    RdSeekBar mSbPreview;
    RelativeLayout mRlSplitView;
    DraggableAddGridView mGridVideosArray;
    View mMainView;
    View mSplitLayout;
    PreviewFrameLayout mPreviewPlayer;
    private int mPlaybackDurationMs = 0; //实际播放时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStrActivityPageName = getString(R.string.editvideopriview);
        setContentView(R.layout.activity_edit_preview);
        if (!Utils.checkDeviceHasNavigationBar(this)) {
            AppConfiguration.setAspectRatio(1);
        }
        Intent in = getIntent();
        mSceneList = in.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
        mCurProportion = in.getFloatExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, 0);
        mProportionStatus = in.getIntExtra(IntentConstants.EDIT_PROPORTION_STATUS, 0);

        UIConfiguration uiConfig = SdkEntry.getSdkService().getUIConfig();

        mIsUseCustomUI = uiConfig.useCustomAlbum;

        if (SubFunctionUtils.isEnableWizard()) {
            if (uiConfig.videoProportion == 0) {
                mCurProportion = 0;
                mProportionStatus = 0;
            } else if (uiConfig.videoProportion == 1) {
                mCurProportion = 1;
                mProportionStatus = 1;
            } else {
                mCurProportion = (float) 16 / 9;
                mProportionStatus = 2;
            }
        }

        mLastPlayPostion = 0;
        mAdapterScene = new DragMediaAdapter(this, getLayoutInflater());
        mAdapterScene.setDragItemListener(mDragItemListener);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SdkEntry.ALBUM_CUSTOMIZE);
        registerReceiver(mReceiver, intentFilter);

        for (Scene scene : mSceneList) {
            addVideoObToMedia(scene.getAllMedia().get(0), 0, null);
            mAdapterScene.addItem(scene);
        }

        mTempRecfile = in.getStringExtra(TEMP_FILE);

        initView();

        mProportionDialog = new ProportionDialog(this);
        mProportionDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                setProportion();
            }
        });

        mGridVideosArray.setAddItemInfo(mSceneList);

        if (SubFunctionUtils.isHideSort()) {
            mGridVideosArray.hideSort(true);
        }

        initListView(mIndex);
        reload();
        playVideo();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Intent broadcast = new Intent();
        broadcast.setAction(intent.getStringExtra("action"));
        broadcast.putExtra(SdkEntry.INTENT_KEY_VIDEO_PATH,
                intent.getStringExtra(SdkEntry.INTENT_KEY_VIDEO_PATH));
        sendBroadcast(broadcast);
        SelectMediaActivity.mIsAppend = false;
        finish();
    }

    private OnItemClickListener mItemListener = new OnItemClickListener() {
        private long lastClickTime;

        @Override
        public void onItemClick(AdapterView<?> parent, final View view,
                                int position, long id) {
            if (SystemClock.uptimeMillis() - lastClickTime < 1000) {
                // 防止频繁调用
                return;
            }
            lastClickTime = SystemClock.uptimeMillis();

            if (!mIsLongClick) {
                onDragItemClick(position);
            }
            mIsLongClick = false;
            seekToPosition(position, false);
            pauseVideo();
        }
    };

    private void seekToPosition(int position, boolean isAddItem) {
        float progress = 0;
        if (isAddItem) {
            position += 1;
        }
        for (int n = 0; n < position; n++) {
            Scene scene = mSceneList.get(n);
            Transition transition = scene.getTransition();
            progress += scene.getDuration();
            if (transition != null) {
                if (checkMediaDuration(n + 1)) {
                    if (transition.getType() != TransitionType.TRANSITION_NULL &&
                            transition.getType() != TransitionType.TRANSITION_BLINK_BLACK &&
                            transition.getType() != TransitionType.TRANSITION_BLINK_WHITE) {
                        progress -= transition.getDuration();
                        if (!isAddItem) {
                            if (n == position - 1) {
                                progress += transition.getDuration();
                            }
                        }
                    } else {
                        if (n == position - 1) {
                            if (isAddItem) {
                                progress -= transition.getDuration();
                            } else {
                                progress += transition.getDuration();
                            }
                        }
                    }
                }
            }
        }
        if (isAddItem) {
            progress -= 0.1f;
        } else {
            progress += 0.1f;
        }
        if (progress < 0) {
            progress = 0;
        }
        playBackSeekTo(progress);
    }

    private void playBackSeekTo(float progress) {
        if (null != mMediaPlayer) {
            mMediaPlayer.seekTo(progress);
            mSbPreview.setProgress(Utils.s2ms(progress));
            mTvVideoDuration.setText(gettime(Utils.s2ms(mMediaPlayer.getCurrentPosition())) + "/" + gettime(mPlaybackDurationMs));
        }
    }

    private void onDragItemClick(int position) {

        mIndex = position;
        mAddItemIndex = -1;
        mGridVideosArray.resetAddItem();
        mAdapterScene.setCheckId(mIndex);
        onListViewItemSelected();
        updateView();
    }


    private int mLongIndex = -1;
    /**
     * 处理长按删除的逻辑
     */
    private DraggableGridView.onLonglistener onDragLongListener = new DraggableGridView.onLonglistener() {

        @Override
        public void onLong(int index, final View chid) {
            mDraggedView.setTrashListener(null);
            mDraggedView.setScollListener(null);
            mIsLongClick = true;
            onDragItemClick(index);
            mBackDiaglog = true;

            if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }

            ExtListItemView item = (ExtListItemView) chid
                    .findViewById(R.id.ivItemExt);

            if (null != item) {
                mLongIndex = index;
                final Bitmap bmp = BitmapUtils.copyBmp(item.getBmpCache(),
                        item.getWidth() + 16, item.getHeight() + 9);

                mPriviewLinearLayout.setEnableTouch(false);
                mParentFrame.setForceToTarget(true);

                if (null != bmp) {
                    mDraggedView.setTrashListener(mTashListener);
                    mDraggedLayout.setVisibility(View.VISIBLE);
                    final int[] location = new int[2];
                    chid.getLocationOnScreen(location);
                    int[] top = new int[2];

                    View playview_rel = mParentFrame.findViewById(R.id.rlPreview);
                    playview_rel.getLocationOnScreen(top);
                    final int mtop = location[1] - top[1];

                    final int imageCenterY = playview_rel.getHeight() / 2;

                    mDraggedView.postDelayed(new Runnable() {

                        @Override
                        public void run() { // 计算中心点

                            mDraggedView.initTrashRect(imageCenterY);
                            // mleftt=x
                            mDraggedView.setData(bmp, location[0], mtop,
                                    location[0] + bmp.getWidth(),
                                    mtop + bmp.getHeight());

                        }
                    }, 50);

                    mDraggedView.setScollListener(new DraggedView.ITashScroll() {

                        @Override
                        public void onTouchMove(int x, int y) {
                            if (y - imageCenterY > 0
                                    && y < mtop + chid.getHeight()) {
                                mGridVideosArray.doActionMove(x, y - mtop);
                            }
                        }
                    });

                }
            }

        }

        @Override
        public void onCancel() {

        }

    };


    private DraggedView.onTrashListener mTashListener = new DraggedView.onTrashListener() {

        @Override
        public void onDelete() {
            if (mLongIndex != -1) {
                if (mDraggedLayout.getVisibility() == View.VISIBLE) {
                    mDraggedLayout.setVisibility(View.GONE);
                }
                mParentFrame.setForceToTarget(false);
                mPriviewLinearLayout.setEnableTouch(true);
                mIndex = mLongIndex;
                mGridVideosArray.resetData();
                mParentFrame.post(new Runnable() {
                    @Override
                    public void run() {
                        mLongIndex = -1;
                        deleteVideo();
                    }
                });
            }
        }

        @Override
        public void onCancel() {
            if (mDraggedLayout.getVisibility() == View.VISIBLE) {
                mDraggedLayout.setVisibility(View.GONE);
            }
            mParentFrame.setForceToTarget(false);
            mPriviewLinearLayout.setEnableTouch(true);
            mParentFrame.post(new Runnable() {

                @Override
                public void run() {
                    mGridVideosArray.reset();
                    reload();
                }
            });
        }
    };

    private void setAllMediaAspectRatio(AspectRatioFitMode mode) {
        for (Scene scene : mSceneList) {
            for (MediaObject mediaObject : scene.getAllMedia()) {
                mediaObject.setAspectRatioFitMode(mode);
            }
        }
    }

    private void updateView() {
        mTvTitle.setText(R.string.partedit);
        if (SubFunctionUtils.isHideProportion()) {
            mIvProportion.setVisibility(View.GONE);
        } else {
            mIvProportion.setVisibility(View.VISIBLE);
        }
    }


    private void onListViewItemSelected() {
        mCurrentScene = mAdapterScene.getItem(mIndex);
        MediaObject mediaObject = mCurrentScene.getAllMedia().get(0);
        mMenuLayout.setVisibility(View.VISIBLE);
        onUI(false);
        if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            int buttonCount = 6;
            mSplit.setVisibility(View.VISIBLE);
            mSpeed.setVisibility(View.VISIBLE);
            mTrim.setVisibility(View.VISIBLE);
            mEdit.setVisibility(View.VISIBLE);
            mReverse.setVisibility(View.VISIBLE);
            mText.setVisibility(View.GONE);
            mDuration.setVisibility(View.GONE);
            if (SubFunctionUtils.isHideSpeed()) {
                buttonCount -= 1;
                mSpeed.setVisibility(View.GONE);
            }
            if (SubFunctionUtils.isHideEdit()) {
                buttonCount -= 1;
                mEdit.setVisibility(View.GONE);
            }
            if (SubFunctionUtils.isHideTrim()) {
                buttonCount -= 1;
                mTrim.setVisibility(View.GONE);
            }
            if (SubFunctionUtils.isHideSplit()) {
                buttonCount -= 1;
                mSplit.setVisibility(View.GONE);
            }
            if (SubFunctionUtils.isHideReverse()) {
                buttonCount -= 1;
                mReverse.setVisibility(View.GONE);
            }
            if (SubFunctionUtils.isHideCopy()) {
                buttonCount -= 1;
                if (buttonCount == 0) {
                    findViewById(R.id.preview_copy).setVisibility(
                            View.INVISIBLE);
                } else {
                    findViewById(R.id.preview_copy).setVisibility(View.GONE);
                }
            }
            LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (buttonCount <= 3) {
                int dwidth = getWindowManager().getDefaultDisplay().getWidth();
                int width = (int) (4 * 60 * CoreUtils.getPixelDensity());
                int margin = (dwidth - width) / 2;
                lp.setMargins(margin, 0, margin, 0);

            } else {
                lp.gravity = Gravity.LEFT;
            }
            mMenuLayout.setLayoutParams(lp);

        } else {
            mSplit.setVisibility(View.GONE);
            mSpeed.setVisibility(View.GONE);
            mReverse.setVisibility(View.GONE);
            VideoOb vo = (VideoOb) mediaObject.getTag();

            if (vo.isExtPic == 1) {
                mText.setVisibility(View.VISIBLE);
                mTrim.setVisibility(View.GONE);
                mEdit.setVisibility(View.GONE);
                mDuration.setVisibility(View.GONE);

                int width = (int) (4 * 60 * CoreUtils.getPixelDensity());
                int dwidth = getWindowManager().getDefaultDisplay().getWidth();
                int margin = (dwidth - width) / 2;

                LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(margin, 0, margin, 0);
                mMenuLayout.setLayoutParams(lp);

            } else {
                mText.setVisibility(View.GONE);
                mTrim.setVisibility(View.GONE);
                mEdit.setVisibility(View.VISIBLE);

                mDuration.setVisibility(View.VISIBLE);

                int width = (int) (4 * 60 * CoreUtils.getPixelDensity());
                int dwidth = getWindowManager().getDefaultDisplay().getWidth();
                int margin = (dwidth - width) / 2;

                LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(margin, 0, margin, 0);
                mMenuLayout.setLayoutParams(lp);
            }
            int count = 3;
            if (SubFunctionUtils.isHideDuration()) {
                mDuration.setVisibility(View.GONE);
                count -= 1;
            }
            if (SubFunctionUtils.isHideEdit()) {
                mEdit.setVisibility(View.GONE);
                count -= 1;
            }
            if (SubFunctionUtils.isHideCopy()) {
                if (count == 1 && vo.isExtPic != 1) {
                    findViewById(R.id.preview_copy).setVisibility(
                            View.INVISIBLE);
                } else {
                    findViewById(R.id.preview_copy).setVisibility(View.GONE);
                }
            }
        }
    }

    ExtButton mTrim, mSplit, mSpeed, mDuration, mText, mEdit, mReverse;
    TextView mTvTitle;
    ExtButton mBtnTitleBarLeft;
    ExtButton mBtnTitleBarRight;

    ImageView mIvProportion;
    PriviewLayout mParentFrame;
    DraggedTrashLayout mDraggedLayout;
    DraggedView mDraggedView;
    PriviewLinearLayout mPriviewLinearLayout;

    LinearLayout mMenuLayout;
    LinearLayout mAddMenuLayout;

    private void initView() {
        mMediaPlayer = (VirtualVideoView) findViewById(R.id.vvMediaPlayer);
        mVideoPreview = (PreviewFrameLayout) findViewById(R.id.rlPreview);
        mIvVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        mTvVideoDuration = (TextView) findViewById(R.id.tvEditorDuration);
        mSbPreview = (RdSeekBar) findViewById(R.id.sbPreview);
        mRlSplitView = (RelativeLayout) findViewById(R.id.rlSplitView);
        mGridVideosArray = (DraggableAddGridView) findViewById(R.id.gridVideosDstArray);
        mMainView = findViewById(R.id.preview_edit_layout);
        mSplitLayout = findViewById(R.id.split_layout);
        mPreviewPlayer = (PreviewFrameLayout) findViewById(R.id.rlPreview_player);

        mTrim = (ExtButton) findViewById(R.id.preview_trim);
        mSplit = (ExtButton) findViewById(R.id.preview_spilt);
        mSpeed = (ExtButton) findViewById(R.id.preview_speed);
        mDuration = (ExtButton) findViewById(R.id.preview_duration);
        mText = (ExtButton) findViewById(R.id.preview_text);
        mEdit = (ExtButton) findViewById(R.id.preview_edit);
        mReverse = (ExtButton) findViewById(R.id.preview_reverse);
        mTvTitle = (TextView) findViewById(R.id.tvTitle);
        mBtnTitleBarLeft = (ExtButton) findViewById(R.id.btnLeft);
        mBtnTitleBarRight = (ExtButton) findViewById(R.id.btnRight);

        mIvProportion = (ImageView) findViewById(R.id.ivProportion);
        mParentFrame = (PriviewLayout) findViewById(R.id.mroot_priview_layout);
        mDraggedLayout = (DraggedTrashLayout) findViewById(R.id.thelinearDraggedLayout);
        mDraggedView = (DraggedView) findViewById(R.id.dragged_info_trash_View);
        mPriviewLinearLayout = (PriviewLinearLayout) findViewById(R.id.the_priview_layout_content);

        mMenuLayout = (LinearLayout) findViewById(R.id.menus);
        mAddMenuLayout = (LinearLayout) findViewById(R.id.addmenus);

        mVirtualVideo = new VirtualVideo();

        mVideoPreview.setClickable(true);
        mVideoPreview.setAspectRatio(AppConfiguration.ASPECTRATIO);

        mBtnTitleBarLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SubFunctionUtils.isEnableWizard()) {
                    onCreateDialog(DIALOG_EXIT_ID).show();
                    return;
                }
                if (mBackDiaglog) {
                    onCreateDialog(DIALOG_RETURN_ID).show();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
        mBtnTitleBarLeft.setPadding(25, 0, 0, 0);
        mTvTitle.setText(R.string.partedit);
        mBtnTitleBarLeft.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.public_menu_cancel, 0, 0, 0);
        mBtnTitleBarRight.setVisibility(View.VISIBLE);
        mBtnTitleBarRight.setText("");
        if (SubFunctionUtils.isEnableWizard()) {
            mBtnTitleBarRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mBtnTitleBarRight.setText(R.string.next_step);
            mBtnTitleBarRight.setTextColor(getResources().getColor(R.color.main_orange));
        } else {
            mBtnTitleBarRight.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.public_menu_sure, 0, 0, 0);
        }


        mBtnTitleBarRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onContinue();
            }
        });

        mRlSplitView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                clickView(null);
            }
        });

        mSbPreview.setOnSeekBarChangeListener(onSeekBarListener);
        mSbPreview.setMax(100);

        mIvProportion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProportionDialog.show();
                mProportionDialog.mIndex = mProportionStatus;
                mProportionDialog.resetStatus();
            }
        });

        mSplitHandler = new SplitHandler(this, mParentFrame, iSplitHandler);

        mTvVideoDuration.setVisibility(View.VISIBLE);
        mLastPlayPostion = -1;

        mMediaPlayer.setOnPlaybackListener(mPlayViewListener);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.setPreviewAspectRatio(mCurProportion);
        mMediaPlayer.setOnClickListener(mPlayerUIListener);
        mIvVideoPlayState.setOnClickListener(mPlayerUIListener);
        mGridVideosArray.setLongLisenter(onDragLongListener);
        mGridVideosArray.setOnItemClickListener(mItemListener);
        mGridVideosArray.setAddItemListener(mAddItemListener);
        // 设置项目大小
        mGridVideosArray.setItemSize(R.dimen.priview_item_width_plus, R.dimen.priview_item_height_plus);
    }


    /**
     * 播放、暂停
     */
    private OnClickListener mPlayerUIListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    };

    /**
     * 拆分
     */
    private SplitHandler.ISplitHandler iSplitHandler = new SplitHandler.ISplitHandler() {

        @Override
        public void onScrollBegin(int progress) {
            pauseVideo();
            if (progress >= 0) {
                mMediaPlayer.seekTo(Utils.ms2s(progress));
            }
        }

        @Override
        public void onScrollEnd(int progress) {
            if (progress >= 0) {
                mMediaPlayer.seekTo(Utils.ms2s(progress));
            }
        }

        @Override
        public void onSure(ArrayList<MediaObject> list) {
            mBackDiaglog = true;
            stopVideo();
            mRlSplitView.setVisibility(View.GONE);
            mSbPreview.setVisibility(View.VISIBLE);
            mIvVideoPlayState.setVisibility(View.VISIBLE);
            mMainView.setVisibility(View.VISIBLE);
            mSplitLayout.setVisibility(View.GONE);

            mBtnTitleBarLeft.setVisibility(View.VISIBLE);
            mBtnTitleBarRight.setVisibility(View.VISIBLE);

            Scene scene = mSceneList.remove(mIndex);
            mAdapterScene.removeItem(scene);
            MediaObject mediaObject = scene.getAllMedia().get(0);
            if (mediaObject != null && mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                VideoOb vob = (VideoOb) mediaObject.getTag();
                if (vob != null) {
                    int isextpic = vob.isExtPic;
                    for (int i = 0; i < list.size(); i++) {
                        MediaObject mo = list.get(i);
                        Scene newScene = VirtualVideo.createScene();
                        newScene.addMedia(mo);
                        mAdapterScene.addItem(i + mIndex, newScene);
                        mSceneList.add(i + mIndex, newScene);
                        mo.setTag(new VideoOb(mo.getTrimStart(), mo.getTrimEnd(), 0, mo.getDuration(), 0, mo
                                .getDuration(), isextpic, vob.getExtpic(), vob.getCropMode()));
                    }
                }
            }
            if (null != scene) {
                scene.getAllMedia().clear();
                scene = null;
            }
            if (null != list) {
                list.clear();
                list = null;
            }

            mHasChanged = false;
            initListView(mIndex);
            mDraggedView.setTrash(false);
            reload();

            setSeekTo(true);
            seekToPosition(mIndex, false);
        }

        @Override
        public void onCancel() {
            onBackPressed();
            cancelSplit();
        }

        @Override
        public void onTemp(ArrayList<MediaObject> list, int progress) {
            mHasChanged = true;
            boolean isPlaying = mMediaPlayer.isPlaying();
            mSplitHandler.setPrepared(false);
            mMediaPlayer.reset();
            mVirtualVideo.reset();
            for (int i = 0; i < list.size(); i++) {
                Scene scene = VirtualVideo.createScene();
                scene.addMedia(list.get(i));
                mVirtualVideo.addScene(scene);
            }
            setAllMediaAspectRatio(AspectRatioFitMode.KEEP_ASPECTRATIO);
            try {
                mVirtualVideo.build(mMediaPlayer);
            } catch (InvalidStateException e) {
                e.printStackTrace();
            }
            mMediaPlayer.seekTo(Utils.ms2s(progress));
            if (isPlaying) {
                playVideo();
            } else {
                pauseVideo();
            }
            mOnTemp = true;
            if (null != list) {
                list.clear();
                list = null;
            }

        }

        @Override
        public void onSeekTo(int progress) {

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            if (progress >= 0) {
                mMediaPlayer.seekTo(Utils.ms2s(progress));
            }
        }

        @Override
        public void onTouchPause() {
            pauseVideo();
        }

        @Override
        public void onPlayOrPause() {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    };

    private boolean mOnTemp = false;

    private void cancelSplit() {
        mDraggedView.setTrash(false);
        mSbPreview.setVisibility(View.VISIBLE);
        mRlSplitView.setVisibility(View.GONE);
        mHasChanged = false;
        reload();

        setSeekTo(true);
        seekToPosition(mIndex, false);
    }

    private VirtualVideo.OnInfoListener mInfoListener = new VirtualVideo.OnInfoListener() {

        @Override
        public boolean onInfo(int what, int extra, Object obj) {
            if (what == VirtualVideo.INFO_WHAT_PLAYBACK_PREPARING) {
                SysAlertDialog.showLoadingDialog(EditPreviewActivity.this,
                        R.string.isloading, false, null);
            } else if (what == VirtualVideo.INFO_WHAT_GET_VIDEO_HIGHTLIGHTS) {
                int[] ls = (int[]) obj;
                mSbPreview.setHighLights(ls);
            }
            return false;
        }
    };


    /**
     * 播放器
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {


        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            updatePreviewFrameAspect(mMediaPlayer.getWidth(), mMediaPlayer.getHeight());
            mAdapterScene.notifyDataSetChanged();
            mMediaPlayer.setFilterType(VideoEditActivity.mCurrentFilterType);

            mPlaybackDurationMs = Utils.s2ms(mMediaPlayer.getDuration());
            mTvVideoDuration.setText(gettime(Utils.s2ms(mMediaPlayer.getCurrentPosition())) + "/" + gettime(mPlaybackDurationMs));

            if (mSplitHandler.isSpliting()) {
                mSplitHandler.setPrepared(true);
            } else {
                mSbPreview.setMax(mPlaybackDurationMs);
            }
            if (mIsSeekTo && mMediaPlayer != null) {
                setSeekTo(false);
            }
        }

        @Override
        public boolean onPlayerError(int what, int extra) {
            SysAlertDialog.cancelLoadingDialog();
            SysAlertDialog.showAlertDialog(EditPreviewActivity.this,
                    getString(R.string.edit_priview),
                    getString(R.string.error_preview_crop),
                    getString(R.string.sure), null, null, null);
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            mIvVideoPlayState.setImageResource(R.drawable.btn_play);
            if (mSplitHandler.isSpliting()) {

            } else {
                mIvVideoPlayState.setVisibility(View.VISIBLE);
            }
            mMediaPlayer.seekTo(0);
            mSbPreview.setProgress(0);
            mTvVideoDuration.setText(gettime(0) + "/"
                    + gettime(mPlaybackDurationMs));
            if (mSplitHandler.isSpliting()) {
                mSplitHandler.onScrollCompleted();
            }
        }

        @Override
        public void onGetCurrentPosition(float nPosition) {
            if (nPosition == 0) {
                return;
            }
            int pms = Utils.s2ms(nPosition);
            mTvVideoDuration.setText(gettime(pms) + "/"
                    + gettime(mPlaybackDurationMs));
            mSbPreview.setProgress(pms);
            if (mHasChanged) {
                if (mSplitHandler.isSpliting()) {
                    mSplitHandler.onScrollProgress(pms);
                } else {
                }
            } else {
                if (mSplitHandler.isSpliting()) {
                    mSplitHandler.onScrollProgress(pms);
                } else {
                }
            }
        }
    };

    private void onContinue() {
        Intent i = new Intent();
        i.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mSceneList);
        i.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, mCurProportion);
        i.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, mProportionStatus);

        if (SubFunctionUtils.isEnableWizard()) {
            i.setClass(this, VideoEditActivity.class);
            startActivityForResult(i, REQUESTCODE_FOR_ADVANCED_EDIT);
        } else {
            setResult(RESULT_OK, i);
            finish();
        }
        overridePendingTransition(0, 0);
    }


    private int mIndex;// 记录正在编辑的视频的index
    private int mAddItemIndex = -1; // 记录当前加号按钮的index
    private final int REQUESTCODE_FOR_SPEED = 7;
    private final int REQUESTCODE_FOR_APPEND = 10;
    private final int REQUESTCODE_FOR_DURATION = 11;
    private final int REQUESTCODE_FOR_EDIT_PIC = 12;
    private final int REQUESTCODE_FOR_SORT = 13;
    private final int REQUESTCODE_FOR_TRIM = 14;
    private final int REQUESTCODE_FOR_TRANSITION = 15;
    private final int REQUESTCODE_FOR_EDIT = 16;
    private final int REQUESTCODE_FOR_CAMERA = 17;
    private final int REQUESTCODE_FOR_ADVANCED_EDIT = 18;
    private final int REQUESTCODE_FOR_POSTER_PROCESS = 19;

    /**
     * 响应剪辑功能按钮点击
     *
     * @param v
     */

    public void onPreviewOptionClick(View v) {
        int id = v.getId();
        if (mIndex < 0) {
            return;
        }
        stopVideo();
        Scene scene = mAdapterScene.getItem(mIndex);
        MediaObject mediaObject = scene.getAllMedia().get(0);
        if (id == R.id.preview_spilt) {
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                mCurrentScene = mAdapterScene.getItem(mIndex);
                if (mCurrentScene.getDuration() <= 0.5f) {
                    Utils.autoToastNomal(
                            this, getString(R.string.video_duration_too_short_to_split));
                } else {
                    mDraggedView.setTrash(true);

                    mSbPreview.setVisibility(View.INVISIBLE);

                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                    mVirtualVideo.reset();
                    mVirtualVideo.addScene(mCurrentScene);
                    try {
                        mVirtualVideo.build(mMediaPlayer);
                    } catch (InvalidStateException e) {
                        e.printStackTrace();
                    }
                    mSplitHandler.init(mCurrentScene);
                    mIvVideoPlayState.setVisibility(View.INVISIBLE);
                    mRlSplitView.setVisibility(View.VISIBLE);
                    mMainView.setVisibility(View.INVISIBLE);
                    mSplitLayout.setVisibility(View.VISIBLE);

                    mTvTitle.setText(R.string.preview_spilt);
                    mTvTitle.setVisibility(View.VISIBLE);
                    mBtnTitleBarLeft.setVisibility(View.INVISIBLE);
                    mBtnTitleBarRight.setVisibility(View.INVISIBLE);

                    mIvProportion.setVisibility(View.GONE);

                    mMediaPlayer.start();
                }
            }
        } else if (id == R.id.preview_edit) {  //编辑
            Intent intent = new Intent();
            intent.setClass(EditPreviewActivity.this, CropRotateMirrorActivity.class);
            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            startActivityForResult(intent, REQUESTCODE_FOR_EDIT);
            overridePendingTransition(0, 0);
        } else if (id == R.id.preview_speed) {
            if (null != mediaObject && mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                Intent intent = new Intent(EditPreviewActivity.this, SpeedPreviewActivity.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
                intent.putExtra(IntentConstants.ALL_MEDIA_MUTE, getIntent()
                        .getBooleanExtra(IntentConstants.ALL_MEDIA_MUTE, false));

                startActivityForResult(intent, REQUESTCODE_FOR_SPEED);
            } else {
                SysAlertDialog.showAutoHideDialog(this, "",
                        getString(R.string.fix_video), Toast.LENGTH_SHORT);
            }

        } else if (id == R.id.preview_copy) {
            mBackDiaglog = true;
            Scene newScene = VirtualVideo.createScene();
            for (MediaObject mo : scene.getAllMedia()) {
                newScene.addMedia(mo);
            }

            ArrayList<Scene> list = mAdapterScene.getMediaList();
            ArrayList<Scene> newList = new ArrayList<Scene>();

            for (int i = 0; i < mIndex; i++) {
                newList.add(list.get(i));

            }

            newList.add(newScene);

            for (int i = mIndex; i < mSceneList.size(); i++) {
                newList.add(list.get(i));
            }

            list.clear();
            list.addAll(newList);

            mSceneList.clear();
            mSceneList.addAll(newList);
            newList.clear();


            mAdapterScene.updateDisplay();

            initListView(mIndex);
            reload();

            setSeekTo(true);
            seekToPosition(mIndex, false);

            playVideo();
            updateView();
        } else if (id == R.id.preview_reverse) {
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                VideoOb ob = (VideoOb) mediaObject.getTag();
                if (ob.getVideoObjectPack() == null) {  //第一次倒序
                    reverseVideo(mediaObject);
                } else {   // 不为空就不是第一次倒序了
                    VideoObjectPack vop = (VideoObjectPack) ob.getVideoObjectPack();
                    VideoOb reverseOb = (VideoOb) vop.mediaObject.getTag();
                    if (vop.isReverse) { // 从倒序还原
                        reverseOb.rStart = vop.originReverseEndTime - ob.rEnd;
                        reverseOb.rEnd = vop.originReverseEndTime - ob.rStart;
                    } else { // 倒序
                        if (ob.rStart < vop.originReverseStartTime
                                || ob.rEnd > vop.originReverseEndTime) {
                            reverseVideo(mediaObject);
                            return;
                        } else {
                            reverseOb.rStart = vop.originReverseEndTime - ob.rEnd;
                            reverseOb.rEnd = vop.originReverseEndTime - ob.rStart;
                        }
                    }
                    MediaObject moClone = mediaObject.clone();
                    ((VideoOb) moClone.getTag()).setVideoObjectPack(null);
                    vop.mediaObject.setSpeed(mediaObject.getSpeed());
                    reverseOb.nStart = (int) (reverseOb.rStart / vop.mediaObject.getSpeed());
                    reverseOb.nEnd = (int) (reverseOb.rEnd / vop.mediaObject.getSpeed());
                    reverseOb.setCropMode(ob.getCropMode());

                    reverseOb.setVideoObjectPack(new VideoObjectPack(moClone,
                            !vop.isReverse, vop.originReverseStartTime,
                            vop.originReverseEndTime));
                    vop.mediaObject.setTimeRange(reverseOb.rStart
                            + reverseOb.TStart, reverseOb.rEnd
                            + reverseOb.TStart);
                    computeShowRect(vop.mediaObject, moClone);
                    Scene reverseScene = new Scene();
                    reverseScene.addMedia(vop.mediaObject);
                    mAdapterScene.getMediaList().set(mIndex, reverseScene);
                    mAdapterScene.notifyDataSetChanged();// m_adaDstVideos.getItem(mIndex).setMediaFilePath(strTempOutPath);
                    mSceneList.set(mIndex, reverseScene);
                    reload();

                    setSeekTo(true);
                    seekToPosition(mIndex, false);
                }

            } else {
                SysAlertDialog.showAutoHideDialog(this, "",
                        getString(R.string.fix_video), Toast.LENGTH_SHORT);
            }
        } else if (id == R.id.preview_duration) {
            Intent intent = new Intent(EditPreviewActivity.this, ImageDurationActivity.class);
            intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
            startActivityForResult(intent, REQUESTCODE_FOR_DURATION);
        } else if (id == R.id.preview_text) {
            VideoOb vob = (VideoOb) mediaObject.getTag();
            Intent intent = new Intent(EditPreviewActivity.this, ExtPhotoActivity.class);
            intent.putExtra(IntentConstants.EXTRA_EXT_PIC_INFO, vob.getExtpic());
            intent.putExtra(IntentConstants.EXTRA_EXT_ISEDIT, true);

            startActivityForResult(intent, REQUESTCODE_FOR_EDIT_PIC);
            updateView();
        } else if (id == R.id.preview_trim) {
            if (mediaObject.getDuration() < 1) {
                Utils.autoToastNomal(this, getString(R.string.video_duration_too_short_to_trim));
            } else {
                Intent intent = new Intent(EditPreviewActivity.this, TrimMediaActivity.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
                intent.putExtra(IntentConstants.TRIM_FROM_EDIT, true);
                startActivityForResult(intent, REQUESTCODE_FOR_TRIM);
                overridePendingTransition(0, 0);
            }
        } else if (id == R.id.preview_addimage) {

            if (mIsUseCustomUI) {
                SdkEntryHandler.getInstance().onSelectImage(
                        EditPreviewActivity.this);
            } else {
                Intent intent = new Intent(EditPreviewActivity.this,
                        com.rd.veuisdk.SelectMediaActivity.class);
                intent.putExtra(EditPreviewActivity.ACTION_APPEND, true);
                intent.putExtra(APPEND_IMAGE, true);
                startActivityForResult(intent, REQUESTCODE_FOR_APPEND);
            }

        } else if (id == R.id.preview_addvideo) {
            if (mIsUseCustomUI) {
                SdkEntryHandler.getInstance().onSelectVideo(
                        EditPreviewActivity.this);
            } else {
                Intent intent = new Intent(EditPreviewActivity.this,
                        com.rd.veuisdk.SelectMediaActivity.class);
                intent.putExtra(EditPreviewActivity.ACTION_APPEND, true);
                startActivityForResult(intent, REQUESTCODE_FOR_APPEND);
            }
        } else if (id == R.id.preview_addtext) {
            Intent intent = new Intent(EditPreviewActivity.this,
                    ExtPhotoActivity.class);
            startActivityForResult(intent, REQUESTCODE_FOR_APPEND);
        } else if (id == R.id.preview_transition) {
            if (checkMediaDuration(mAddItemIndex)) {
                Intent intent = new Intent(EditPreviewActivity.this,
                        com.rd.veuisdk.TransitionActivity.class);

                intent.putExtra(IntentConstants.INTENT_EXTRA_TRANSITION, scene.getTransition());
                intent.putExtra(IntentConstants.INTENT_TRANSITION_COUNT, mSceneList.size());
                startActivityForResult(intent, REQUESTCODE_FOR_TRANSITION);
            } else {
                Utils.autoToastNomal(this, getString(R.string.video_duration_too_short_to_transition));
            }
        }
    }

    private boolean mIsReversing = false;

    private void reverseVideo(final MediaObject mediaObject) {
        mIsReversing = true;
        VideoConfig videoConfig = new VideoConfig();
        videoConfig.setVideoFrameRate(24);
        // videoConfig.setVideoSize(848,480);可指定的输出分辨率,一般不用设置，
        // 需根据系统版本动态变化,好的机型可以设置更高分辨率(最高1280*720)
        final String strTempOutPath = PathUtils.getTempFileNameForSdcard(
                "reverse", "mp4"); // 生成临时文件路径，这个路径，用于替换倒放前媒体信息

        ExportUtils.reverseSave(this, mediaObject, strTempOutPath, videoConfig, new ExportListener() {
            private HorizontalProgressDialog horiProgressSave = null;
            private Dialog dialog = null;
            private boolean cancelSave = false;

            @Override
            public void onExportStart() {
                if (horiProgressSave == null) {
                    horiProgressSave = SysAlertDialog.showHoriProgressDialog(
                            EditPreviewActivity.this,
                            getString(R.string.reversing), false, true,
                            new DialogInterface.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    horiProgressSave = null;
                                }
                            });
                    horiProgressSave.setCanceledOnTouchOutside(false);
                    horiProgressSave.setOnCancelClickListener(new HorizontalProgressDialog.onCancelClickListener() {

                        @Override
                        public void onCancel() {
                            dialog = SysAlertDialog.showAlertDialog(
                                    EditPreviewActivity.this, "",
                                    getString(R.string.reverse_cancel),
                                    getString(R.string.no),
                                    null,
                                    getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (horiProgressSave != null) {
                                                horiProgressSave.dismiss();
                                            }
                                            cancelSave = true;
                                        }
                                    });
                        }
                    });
                }
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            @Override
            public boolean onExporting(int progress, int max) {
                if (null != horiProgressSave) {
                    horiProgressSave.setProgress(progress);
                    horiProgressSave.setMax(max);
                }
                if (cancelSave) {
                    return false;
                }
                return true;
            }

            @Override
            public void onExportEnd(int result) {
                if (null != horiProgressSave) {
                    horiProgressSave.dismiss();
                    horiProgressSave = null;
                    mIsReversing = false;
                }
                if (result >= VirtualVideo.RESULT_SUCCESS) {
                    Scene scene = VirtualVideo.createScene();
                    MediaObject outputmo = scene.addMedia(strTempOutPath);

                    computeShowRect(outputmo, mediaObject);

                    VideoOb oldvo = (VideoOb) mediaObject.getTag();

                    oldvo.setVideoObjectPack(null);

                    VideoOb newvo = new VideoOb(0, outputmo.getTrimEnd(), 0, outputmo.getTrimEnd(), 0,
                            outputmo.getTrimEnd(), 0, null, oldvo.getCropMode());
                    mediaObject.setTag(oldvo);
                    newvo.setVideoObjectPack(new VideoObjectPack(mediaObject, true,
                            oldvo.rStart, oldvo.rStart + outputmo.getTrimEnd()));
                    outputmo.setTag(newvo);

                    mAdapterScene.getMediaList().set(mIndex, scene);
                    mAdapterScene.notifyDataSetChanged();
                    mSceneList.set(mIndex, scene);
                    initListView(mIndex);
                    reload();
                }
                setSeekTo(true);
                seekToPosition(mIndex, false);
                playVideo();
            }
        });

    }

    private void computeShowRect(MediaObject outputmo, MediaObject oldmo) {
        int newVideoWidth = outputmo.getWidth();
        int newVideoHeight = outputmo.getHeight();
        int oldVideoWidth = oldmo.getWidth();
        int oldVideoHeight = oldmo.getHeight();

        int nAngle = oldmo.getAngle();

        RectF rectClip, rectShow, rectNewClip;

        rectClip = oldmo.getClipRectF();
        rectShow = oldmo.getShowRectF();

        if (rectClip.left == 0 & rectClip.right == 0
                & rectClip.top == 0 & rectClip.bottom == 0) {
            rectClip.right = oldVideoWidth;
            rectClip.bottom = oldVideoHeight;
        }

        rectNewClip = new RectF();
        rectNewClip.left = (int) Math
                .ceil((rectClip.left * newVideoWidth) / oldVideoWidth);
        rectNewClip.right = (int) Math
                .ceil((rectClip.right * newVideoWidth) / oldVideoWidth);
        rectNewClip.top = (int) Math
                .ceil((rectClip.top * newVideoHeight) / oldVideoHeight);
        rectNewClip.bottom = (int) Math
                .ceil((rectClip.bottom * newVideoHeight) / oldVideoHeight);


        outputmo.setAngle(nAngle);
        outputmo.setAudioMute(oldmo.isAudioMute());
        outputmo.setMixFactor(oldmo.getMixFactor());
        outputmo.setSpeed(oldmo.getSpeed());
        outputmo.setFlipType(oldmo.getFlipType());
        outputmo.setShowRectF(rectShow);
        outputmo.setClipRectF(rectNewClip);
    }

    private boolean checkMediaDuration(int addIndex) {
        if (addIndex < 1 || addIndex > (mSceneList.size() - 1)) {
            return false;
        }
        Scene sceneFront = mSceneList.get(addIndex - 1);
        Scene sceneBelow = mSceneList.get(addIndex);
        if (sceneFront.getDuration() < 0.5f || sceneBelow.getDuration() < 0.5f) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除视频
     */
    private void deleteVideo() {
        mBackDiaglog = true;
        Scene scene = mAdapterScene.getItem(mIndex);
        stopVideo();
        mAdapterScene.removeItem(scene);
        mSceneList.remove(mIndex);

        int len = mSceneList.size();
        if (mIndex >= len) {
            mIndex = len - 1;
        }
        initListView(mIndex);
        updateView();
        reload();
        playVideo();
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;
        String strMessage = null;
        if (id == DIALOG_REMOVE_ID) {
            strMessage = getString(R.string.remove_item);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.yes),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteVideo();
                        }
                    });
        } else if (id == DIALOG_EXIT_ID) {
            strMessage = getString(R.string.quit_edit);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }, getString(R.string.sure),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            overridePendingTransition(0, 0);
                        }
                    });
        } else if (id == DIALOG_RETURN_ID) {

            strMessage = getString(R.string.quit_partedit);
            dialog = SysAlertDialog.showAlertDialog(this, "", strMessage,
                    getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }, getString(R.string.sure),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(RESULT_CANCELED);
                            finish();
                            overridePendingTransition(0, 0);
                        }
                    });
        }

        return dialog;
    }

    /**
     * 删除媒体项的提示信息
     */

    private static final int DIALOG_REMOVE_ID = 3;
    /**
     * 返回的提示信息
     */
    private static final int DIALOG_RETURN_ID = 1;
    /**
     * 退出编辑
     */
    private static final int DIALOG_EXIT_ID = 5;

    private void setSeekTo(boolean seekTo) {
        mIsSeekTo = seekTo;
    }

    private boolean mIsSeekTo = false;
    private boolean mAddNewTran = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSbPreview.setHighLights(null);
        if (resultCode == RESULT_OK) {
            mBackDiaglog = true;
            setSeekTo(true);

            if (requestCode == REQUESTCODE_FOR_SPEED) {
                Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                mAdapterScene.getMediaList().set(mIndex, scene);
                mAdapterScene.notifyDataSetChanged();
                mSceneList.set(mIndex, scene);
                onListViewItemSelected();
                reload();
            } else if (requestCode == REQUESTCODE_FOR_EDIT) {
                Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                mSceneList.set(mIndex, scene);
                mAdapterScene.getMediaList().set(mIndex, scene);

                mGridVideosArray.post(new Runnable() {

                    @Override
                    public void run() {
                        initListView(mIndex);
                    }
                });
                reload();
            } else if (requestCode == REQUESTCODE_FOR_APPEND) {
                ArrayList<MediaObject> tempMedias = data
                        .getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
                int isextPic = data.getIntExtra(
                        IntentConstants.EXTRA_EXT_ISEXTPIC, 0);
                int len = tempMedias.size();
                for (int i = 0; i < len; i++) {
                    MediaObject mo = tempMedias.get(i);
                    int addPosition;
                    if (mAddItemIndex != -1) {
                        addPosition = mAddItemIndex + i;
                    } else {
                        addPosition = mSceneList.size();
                    }
                    Scene scene = VirtualVideo.createScene();
                    scene.addMedia(mo);
                    mAdapterScene.addItem(addPosition, scene);
                    mSceneList.add(addPosition, scene);

                    if (isextPic == 1) {
                        addVideoObToMedia(mo, isextPic,
                                (ExtPicInfo) data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO));
                    } else {
                        addVideoObToMedia(mo, isextPic, null);
                    }
                }
                reload();
                mGridVideosArray.post(new Runnable() {

                    @Override
                    public void run() {
                        initListView(mIndex);
                    }
                });
            } else if (requestCode == REQUESTCODE_FOR_DURATION) {
                int duration = data.getIntExtra(IntentConstants.EXTRA_EXT_APPLYTOALL_DURATION, 0);

                if (duration != 0) {
                    for (int n = 0; n < mSceneList.size(); n++) {
                        MediaObject media = mSceneList.get(n).getAllMedia().get(0);
                        if (media.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                            VideoOb temp = (VideoOb) media.getTag();
                            if (temp.isExtPic == 0) {
                                media.setTimeRange(0, duration);
                                media.setIntrinsicDuration(duration);
                                temp.nStart = media.getTrimStart();
                                temp.nEnd = media.getTrimEnd();
                                temp.rStart = temp.nStart;
                                temp.rEnd = temp.nEnd;
                                temp.TStart = temp.nStart;
                                temp.TEnd = temp.nEnd;
                                mAdapterScene.getMediaList().set(n, mSceneList.get(n));
                                mSceneList.set(n, mSceneList.get(n));
                            }
                        }
                    }
                } else {
                    Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    mAdapterScene.getMediaList().set(mIndex, scene);
                    mSceneList.set(mIndex, scene);
                }
                reload();
                mAdapterScene.notifyDataSetChanged();
                onListViewItemSelected();
                playVideo();
            } else if (requestCode == REQUESTCODE_FOR_EDIT_PIC) {
                MediaObject media = data
                        .getParcelableExtra(IntentConstants.EXTRA_MEDIA_OBJECTS);
                VideoOb nvb = new VideoOb(media.getTrimStart(), media.getTrimEnd(),
                        media.getTrimStart(), media.getTrimEnd(),
                        media.getTrimStart(), media.getTrimEnd(),
                        1, (ExtPicInfo) data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO), 0);
                Scene scene = VirtualVideo.createScene();
                media.setTag(nvb);
                scene.addMedia(media);
                mAdapterScene.getMediaList().set(mIndex, scene);
                mSceneList.set(mIndex, scene);
                mCurrentScene = mAdapterScene.getItem(mIndex);
                mGridVideosArray.post(new Runnable() {

                    @Override
                    public void run() {
                        initListView(mIndex);
                    }
                });
                reload();
            } else if (requestCode == REQUESTCODE_FOR_SORT) {
                mAdapterScene.clear();
                mSceneList = data.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
                for (Scene scene : mSceneList) {
                    mAdapterScene.addItem(scene);
                }
                mGridVideosArray.setAddItemInfo(mSceneList);
                mIndex = 0;
                mAddItemIndex = -1;
                reload();
                mGridVideosArray.post(new Runnable() {

                    @Override
                    public void run() {
                        initListView(mIndex);
                    }
                });

            } else if (requestCode == REQUESTCODE_FOR_TRIM) {
                Scene newScene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                mSceneList.set(mIndex, newScene);
                mAdapterScene.getMediaList().set(mIndex, newScene);
                reload();
                mAdapterScene.notifyDataSetChanged();
                onListViewItemSelected();

            } else if (requestCode == REQUESTCODE_FOR_TRANSITION) {
                ArrayList<Transition> arrTransitions = data.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_TRANSITION);
                if (data.getBooleanExtra(IntentConstants.TRANSITION_APPLY_TO_ALL, false)) {
                    for (int nTemp = 0; nTemp < arrTransitions.size(); nTemp++) {
                        mSceneList.get(nTemp).setTransition(arrTransitions.get(nTemp));
                    }
                } else {
                    mSceneList.get(mAddItemIndex - 1).setTransition(arrTransitions.get(0));
                }
                mAddNewTran = true;
                reload();

            } else if (requestCode == REQUESTCODE_FOR_CAMERA) {
                MediaObject mediaObject = data.getParcelableExtra(IntentConstants.EDIT_CAMERA_MEDIA);
                Scene scene = VirtualVideo.createScene();
                scene.addMedia(mediaObject);
                mSceneList.add(mIndex + 1, scene);
                reload();
                mGridVideosArray.post(new Runnable() {

                    @Override
                    public void run() {
                        initListView(mIndex);
                    }
                });
            } else if (requestCode == REQUESTCODE_FOR_ADVANCED_EDIT) {
//                mAdapterScene.clear();
//                mSceneList.clear();
//                ArrayList<MediaObject> allMedia = data.getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
//                for (MediaObject mo : allMedia) {
//                    Scene scene = VirtualVideo.createScene();
//                    scene.addMedia(mo);
//                    mAdapterScene.addItem(scene);
//                    mSceneList.add(scene);
//                }
//                mGridVideosArray.setAddItemInfo(mSceneList);
                reload();
            }
            if (!mAddNewTran) {
                if (mAddItemIndex == -1) {
                    seekToPosition(mIndex, false);
                } else {
                    seekToPosition(mAddItemIndex, true);
                }
            }
        } else {
            if (requestCode == REQUESTCODE_FOR_ADVANCED_EDIT) {
                setResult(RESULT_OK, data);
                finish();
                return;
            }
            setSeekTo(false);
        }
        updateView();
    }

    /**
     * 添加到扩展类
     */
    private void addVideoObToMedia(MediaObject media, int isextpic, ExtPicInfo info) {
        media.setTag(new VideoOb(media.getTrimStart(), media.getTrimEnd(), media
                .getTrimStart(), media.getTrimEnd(), media.getTrimStart(),
                media.getTrimEnd(), isextpic, info, 0));
    }


    private void initListView(final int index) {

        mGridVideosArray.setAdapter(mAdapterScene);
        mCurrentScene = mAdapterScene.getItem(mIndex);
        mGridVideosArray.post(new Runnable() {

            @Override
            public void run() {
                if (mAddItemIndex >= mSceneList.size()) {
                    mAddItemIndex = mSceneList.size() - 1;
                }
                if (mAddItemIndex == -1 || mAddItemIndex == 0) {
                    onDragItemClick(index);
                } else {
                    mGridVideosArray.setAddItemSelect(mAddItemIndex - 1);
                    mMenuLayout.setVisibility(View.GONE);
                    onUI(true);
                    mAdapterScene.setCheckId(-1);
                }
            }
        });
    }

    /**
     * 单位毫秒
     *
     * @param progress
     * @return
     */
    private String gettime(int progress) {
        return DateTimeUtils.stringForMillisecondTime(progress, true, true);
    }

    /**
     * 更新预览视频播放器比例
     */
    protected void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {
        if (mVideoPreview != null) {
            mCurAspect = (float) (nVideoWidth / (nVideoHeight + 0.0));
            mPreviewPlayer.setAspectRatio(mCurAspect);
        }
    }


    @Override
    public void onBackPressed() {
        SelectMediaActivity.mIsAppend = false;
        stopVideo();
        if (mSplitHandler.isSpliting()) {
            if (mSplitHandler.onBackPressed()) {
                cancelSplit();
                mIvVideoPlayState.setVisibility(View.VISIBLE);
                mMainView.setVisibility(View.VISIBLE);
                updateView();

                mBtnTitleBarLeft.setVisibility(View.VISIBLE);
                mBtnTitleBarRight.setVisibility(View.VISIBLE);

                mHasChanged = false;
                onListViewItemSelected();
            }
        } else {
            if (SubFunctionUtils.isEnableWizard()) {
                onCreateDialog(DIALOG_EXIT_ID).show();
                return;
            }
            if (mBackDiaglog) {
                onCreateDialog(DIALOG_RETURN_ID).show();
            } else {
                setResult(RESULT_CANCELED);
                finish();
                overridePendingTransition(0, 0);
            }
        }

    }

    @Override
    public void clickView(View v) {
        if (v == null) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        }
    }

    @Override
    protected void onPause() {
        // listView.setResume(false);
        super.onPause();
        if (null != mMediaPlayer) {
            mLastPlayPostion = mMediaPlayer.getCurrentPosition();
            pauseVideo();
        }

    }

    @Override
    protected void onDestroy() {
        SysAlertDialog.cancelLoadingDialog();

        if (null != mMediaPlayer) {
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        if (mAdapterScene != null) {
            mAdapterScene.onDestroy();
            mAdapterScene = null;
        }

        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (null != TempVideoParams.getInstance() && SubFunctionUtils.isEnableWizard()) {
            // 删除倒序临时文件
            PathUtils.cleanTempFilesByPrefix("reverse");
            TempVideoParams.getInstance().recycle();
        }

        if (!TextUtils.isEmpty(mTempRecfile)) {
            try {
                new File(mTempRecfile).delete(); // 删除临时录制的文件
            } catch (Exception e) {
                e.printStackTrace();
            }
            mTempRecfile = null;
        }
        System.gc();
        System.runFinalization();
    }

    @Override
    public void finish() {
        SysAlertDialog.cancelLoadingDialog();
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSplitHandler.isSpliting()) {
            mMediaPlayer.seekTo(mLastPlayPostion);
        } else if (!mIsSeekTo) {
            if (mLastPlayPostion == 0) {
                mMediaPlayer.seekTo(0);
            }
            if (mLastPlayPostion > 0) {
                playBackSeekTo(mLastPlayPostion);
                mLastPlayPostion = -1;
            }
        }
    }

    private void playVideo() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mIsReversing) {
            return;
        }
        mMediaPlayer.start();
        mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        if (mSplitHandler.isSpliting()) {
            mIvVideoPlayState.setImageResource(R.drawable.btn_pause);
        }
        ViewUtils.fadeOut(this, mIvVideoPlayState);
    }

    private void pauseVideo() {
        if (mOnTemp) {
            mOnTemp = false;
            return;
        }
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.pause();
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);
        if (mSplitHandler.isSpliting()) {
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        } else {
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        }
    }

    private void stopVideo() {
        if (mMediaPlayer == null) {
            return;
        }
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mIvVideoPlayState.setImageResource(R.drawable.btn_play);

        if (mSplitHandler.isSpliting()) {
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        } else {
            mIvVideoPlayState.setVisibility(View.VISIBLE);
        }

    }

    private OnSeekBarChangeListener onSeekBarListener = new OnSeekBarChangeListener() {
        private boolean m_bIsPlayingOnSeek;

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (m_bIsPlayingOnSeek) {
                playVideo();
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (mMediaPlayer.isPlaying()) {
                pauseVideo();
                m_bIsPlayingOnSeek = true;
            } else {
                m_bIsPlayingOnSeek = false;
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mTvVideoDuration.setText(gettime(progress) + "/"
                        + gettime(Utils.s2ms(mMediaPlayer.getDuration())));
                mMediaPlayer.seekTo(Utils.ms2s(progress));
            }
        }
    };

    private DragItemListener mDragItemListener = new DragItemListener() {

        @Override
        public void onRemove(int position) {
            if (mAdapterScene.getCount() == 1) {
                int msgResId = R.string.just_only_one_scene;
                SysAlertDialog.showAutoHideDialog(EditPreviewActivity.this,
                        null, getString(msgResId), Toast.LENGTH_SHORT);
            } else {
                onCreateDialog(DIALOG_REMOVE_ID).show();
            }

        }

        @Override
        public boolean isExt(int position) {
            if (position >= mSceneList.size()) {
                return false;
            }
            MediaObject mo = mSceneList.get(position).getAllMedia().get(0);
            VideoOb vo = (VideoOb) mo.getTag();
            if (vo == null) {
                return false;
            }
            if (vo.isExtPic == 1) {
                return true;
            } else {
                return false;
            }

        }
    };

    private void reload() {
        if (getIntent().getBooleanExtra(IntentConstants.ALL_MEDIA_MUTE, false)) {
            for (Scene scene : mAdapterScene.getMediaList()) {
                for (MediaObject mediaObject : scene.getAllMedia()) {
                    mediaObject.setAudioMute(true);
                }
            }
        }
        mMediaPlayer.reset();
        mVirtualVideo.reset();
        mSbPreview.setHighLights(null);
        setAllMediaAspectRatio(AspectRatioFitMode.KEEP_ASPECTRATIO);
        for (Scene scene : mAdapterScene.getMediaList()) {
            mVirtualVideo.addScene(scene);
        }
        try {
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
        }

        if (!SubFunctionUtils.isHideSoundTrack()) {
//            mVirtualVideo.addMusic(TempVideoParams.getInstance()
//                    .getMusic().getMediaFilePath(),true);
        }
        playVideo();
    }

    /**
     * 设置比例
     */
    private void setProportion() {
        if (mProportionDialog.mIndex == -1) {
            return;
        }
        if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_AUTO) {
            mCurProportion = 0;
            mMediaPlayer.setPreviewAspectRatio(mCurProportion);
        } else if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_SQUARE) {
            mCurProportion = 1;
            mMediaPlayer.setPreviewAspectRatio(mCurProportion);
        } else if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_LANDSCAPE) {
            // 横屏
            mCurProportion = (float) 16 / 9;
            mMediaPlayer.setPreviewAspectRatio(mCurProportion);
        } else if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_PORTRAIT) {
            // 竖屏
            mCurProportion = 0;
            mMediaPlayer.setPreviewAspectRatio(mCurProportion);
            for (Scene mo : mSceneList) {
                List<MediaObject> list = mo.getAllMedia();
                if (null != list && list.size() > 0) {
                    MediaObject tmp = list.get(0);
                    int height, width;
                    if (tmp.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                        height = tmp.getHeight();
                        width = tmp.getWidth();
                    } else {
                        height = tmp.getHeight();
                        width = tmp.getWidth();
                    }
                    if (width > height) {
                        SysAlertDialog.showAutoHideDialog(this, null,
                                getString(R.string.proportion_change_to_auto),
                                Toast.LENGTH_LONG);
                        mProportionDialog.mIndex = ProportionDialog.ORIENTATION_AUTO;
                        break;
                    }
                }

            }

        } else {
        }
        reload();
        playVideo();
        mProportionStatus = mProportionDialog.mIndex;
        mProportionDialog.resetStatus();
    }

    /**
     * 是否显示UI
     *
     * @param show
     */
    private void onUI(boolean show) {
        if (null != mAddMenuLayout) {
            if (show) {
                mAddMenuLayout.setVisibility(View.VISIBLE);
                if (SubFunctionUtils.isHideText()) {
                    findViewById(R.id.preview_addtext).setVisibility(View.GONE);
                }
            } else {
                mAddMenuLayout.setVisibility(View.GONE);
            }
        }

    }

    private DraggableAddGridView.AddItemOnClickListener mAddItemListener = new DraggableAddGridView.AddItemOnClickListener() {

        @Override
        public void onClick(int index) {
            mAddItemIndex = index + 1;
            mMenuLayout.setVisibility(View.GONE);
            onUI(true);
            mAdapterScene.setCheckId(-1);
            seekToPosition(index, true);
            pauseVideo();
        }

        @Override
        public void addItemClick(int type) {
            if (type == 1) {
                mAddItemIndex = -1;
                if (mIsUseCustomUI) {
                    SdkEntryHandler.getInstance().onSelectVideo(
                            EditPreviewActivity.this);
                } else {
                    Intent intent = new Intent(EditPreviewActivity.this,
                            com.rd.veuisdk.SelectMediaActivity.class);
                    intent.putExtra(EditPreviewActivity.ACTION_APPEND, true);
                    startActivityForResult(intent, REQUESTCODE_FOR_APPEND);
                }
            } else if (type == 2) {
                Intent intent = new Intent();

                intent.setClass(EditPreviewActivity.this, com.rd.veuisdk.SortMediaActivity.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mSceneList);

                startActivityForResult(intent, REQUESTCODE_FOR_SORT);
                overridePendingTransition(0, 0);
            }
        }

        @Override
        public void reorderAddItem(ArrayList<Scene> arr, int drag) {
            mSceneList = arr;
            onDragItemClick(drag);
        }
    };


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, SdkEntry.ALBUM_CUSTOMIZE)) {
                boardcastResult(intent);
            }
        }

    };

    private void boardcastResult(Intent intent) {
        final ArrayList<String> arrPath = intent
                .getStringArrayListExtra(SdkEntry.MEDIA_PATH_LIST);
        final ArrayList<Scene> alMedias = new ArrayList<Scene>();
        mGridVideosArray.post(new Runnable() {

            @Override
            public void run() {
                mBackDiaglog = true;
                for (String nMediaKey : arrPath) {
                    if (null != nMediaKey) {
                        Scene scene = VirtualVideo.createScene();
                        MediaObject mo = scene.addMedia(nMediaKey);
                        if (scene != null && mo != null) {
                            alMedias.add(scene);
                        }
                    }
                }
                if (alMedias.size() == 0) {
                    Log.e(TAG, getString(R.string.select_medias));
                    return;
                }

                int len = alMedias.size();
                for (int i = 0; i < len; i++) {
                    Scene scene = alMedias.get(i);
                    MediaObject mo = scene.getAllMedia().get(0);
                    int addPosition;
                    if (mAddItemIndex != -1) {
                        addPosition = mAddItemIndex + i;
                    } else {
                        addPosition = mSceneList.size();
                    }
                    mAdapterScene.addItem(addPosition, scene);
                    mSceneList.add(addPosition, scene);
                }
                reload();
                playVideo();

                initListView(mIndex);
            }
        });

    }
}
