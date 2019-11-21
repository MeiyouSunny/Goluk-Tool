package com.rd.veuisdk;

import android.app.Activity;
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
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.vecore.exception.InvalidArgumentException;
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
import com.rd.veuisdk.database.DraftData;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.fragment.TransitionFragment;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.model.ExtPicInfo;
import com.rd.veuisdk.model.RCInfo;
import com.rd.veuisdk.model.ShortVideoInfoImp;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.model.VideoObjectPack;
import com.rd.veuisdk.mvp.model.EditPreviewModel;
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
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.IMediaParamImp;
import com.rd.veuisdk.utils.IntentConstants;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 片段编辑页
 */
public class EditPreviewActivity extends BaseActivity implements IEditPreviewHandler {


    /**
     * 片段编辑
     */
    static void gotoEditPreview(Context context, ArrayList<Scene> list, float curProportion, int proportionStatus, boolean enableBg, boolean mediaMute, int requestCode) {
        Intent i = new Intent(context,
                EditPreviewActivity.class);
        i.putExtra(IntentConstants.INTENT_EXTRA_SCENE, list);
        i.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, curProportion);
        i.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, proportionStatus);
        i.putExtra(IntentConstants.ALL_MEDIA_MUTE, mediaMute);
        i.putExtra(IntentConstants.EXTRA_EXT_BACKGROUND_MODE, enableBg);
        ((Activity) context).startActivityForResult(i, requestCode);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    private final String TAG = "EditPreviewActivity";


    public static final String TEMP_FILE = "temp_file";
    public static float mCurAspect;

    private SplitHandler mSplitHandler;
    private DragMediaAdapter mMediaAdapter;
    private String mTempRecfile = null;
    private ProportionDialog mProportionDialog;
    private float mLastPlayPostion;
    private boolean mLastPlaying;
    private boolean mIsLongClick;
    private boolean mBackDiaglog = false;
    private boolean mHasChanged = false;
    private boolean mIsUseCustomUI = false;

    private ArrayList<Scene> mSceneList = new ArrayList<>();
    private int mProportionStatus;
    private float mCurProportion;
    private VirtualVideo mVirtualVideo;
    private Scene mCurrentScene;

    private VirtualVideoView mMediaPlayer;
    private PreviewFrameLayout mVideoPreview;
    private ImageView mIvVideoPlayState;
    private TextView mTvCurTime;
    private TextView mTvTotalTime;
    private RdSeekBar mSbPreview;
    private RelativeLayout mRlSplitView;
    private DraggableAddGridView mGridVideosArray;
    private View mMainView;
    private View mSplitLayout;
    private int mPlaybackDurationMs = 0; //实际播放时间
    private UIConfiguration mUIConfig;
    private ShortVideoInfoImp shortVideoInfoImp;
    private ViewGroup mHorizontalScrollView;
    private FrameLayout mFlZoom;
    private LinearLayout llDurationSeekBar;
    private boolean mIsEnableBackground = false;

    private TransitionFragment mTransitionFragment;

    private int lastProportionStatus = ProportionDialog.ORIENTATION_AUTO;

    /**
     * 根据4种比例模式， 返回比例
     *
     * @param status
     * @return
     */
    private float getAsp(int status) {
        if (status == ProportionDialog.ORIENTATION_LANDSCAPE) {
            return (float) 16 / 9;
        } else if (status == ProportionDialog.ORIENTATION_SQUARE) {
            return 1f;
        } else if (status == ProportionDialog.ORIENTATION_PORTRAIT) {
            return 9 / 16.0f;
        } else {
            //自动
            return 0;
        }
    }

    private EditPreviewModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_preview);
        int draftId = getIntent().getIntExtra(IntentConstants.INTENT_EXTRA_DRAFT, -1);
        Intent in = getIntent();
        mIsEnableBackground = in.getBooleanExtra(IntentConstants.EXTRA_EXT_BACKGROUND_MODE, false);
        mUIConfig = SdkEntry.getSdkService().getUIConfig();
        if (draftId != -1) {
            //草稿箱视频有效
            DraftData.getInstance().initilize(this);
            shortVideoInfoImp = DraftData.getInstance().queryOne(draftId);
            if (null == shortVideoInfoImp) {
                finish(); //强制退出
                return;
            }
            mSceneList = shortVideoInfoImp.getSceneList();
            mProportionStatus = shortVideoInfoImp.getProportionStatus();

        } else {
            mSceneList = in.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
            mProportionStatus = in.getIntExtra(IntentConstants.EDIT_PROPORTION_STATUS, 0);
            mCurProportion = in.getFloatExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, 0);
            if (mUIConfig.isEnableWizard()) {
                if (mUIConfig.videoProportion == 0) {
                    mProportionStatus = ProportionDialog.ORIENTATION_AUTO;
                } else if (mUIConfig.videoProportion == 1) {
                    mProportionStatus = ProportionDialog.ORIENTATION_SQUARE;
                } else {
                    mProportionStatus = ProportionDialog.ORIENTATION_LANDSCAPE;
                }
            }
        }

        mModel = new EditPreviewModel();
        lastProportionStatus = mProportionStatus;
        //修正播放器容器显示比例
        AppConfiguration.fixAspectRatio(this);

        mIsUseCustomUI = mUIConfig.useCustomAlbum;

        mLastPlayPostion = 0;
        mMediaAdapter = new DragMediaAdapter(this, getLayoutInflater());
        mMediaAdapter.setDragItemListener(mDragItemListener);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SdkEntry.ALBUM_CUSTOMIZE);
        registerReceiver(mReceiver, intentFilter);
        for (Scene scene : mSceneList) {
            addVideoObToMedia(scene.getAllMedia().get(0), 0, null);
            mMediaAdapter.addItem(scene);
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

//        if (mUIConfig.isHideSort()) {
//            mGridVideosArray.hideSort(true);
//        }
        mGridVideosArray.hideSort(true);

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

    /**
     * 选中当前位置的开始时刻
     *
     * @param position
     * @param isAddItem
     */
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
        if (position == 0) {
            //特别处理，第0个media，开始位置强制为0
            progress = 0;
        }
        playBackSeekTo(progress);
    }

    private void playBackSeekTo(float progress) {
        if (null != mMediaPlayer) {
            mMediaPlayer.seekTo(progress);
            mSbPreview.setProgress(Utils.s2ms(progress));
            mTvCurTime.setText(getTime(Utils.s2ms(mMediaPlayer.getCurrentPosition())));
            mTvTotalTime.setText(getTime(mPlaybackDurationMs));
        }
    }

    /***
     * 响应当前选中项
     * @param position
     */
    private void onDragItemClick(int position) {
        mIndex = position;
        mAddItemIndex = -1;
        mGridVideosArray.resetAddItem();
        mMediaAdapter.setCheckId(mIndex);
        onListViewItemSelected();
        updateView();
    }


    private int mLongIndex = -1;
    /**
     * 处理长按删除的逻辑
     */
    private DraggableGridView.onLonglistener onDragLongListener = new DraggableGridView.onLonglistener() {

        @Override
        public void onLong(int index, final View child) {
            mDraggedView.setTrashListener(null);
            mDraggedView.setScollListener(null);
            mIsLongClick = true;
            onDragItemClick(index);
            mBackDiaglog = true;

            if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
            ExtListItemView item = Utils.$(child, R.id.ivItemExt);
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
                    child.getLocationOnScreen(location);
                    int[] top = new int[2];

                    View view = Utils.$(mParentFrame, R.id.rlPreview);
                    view.getLocationOnScreen(top);
                    final int mtop = location[1] - top[1];

                    final int imageCenterY = view.getHeight() / 2;

                    mDraggedView.postDelayed(new Runnable() {

                        @Override
                        public void run() { // 计算中心点

                            mDraggedView.initTrashRect(imageCenterY);
                            mDraggedView.setData(bmp, location[0], mtop,
                                    location[0] + bmp.getWidth(),
                                    mtop + bmp.getHeight());

                        }
                    }, 50);

                    mDraggedView.setScollListener(new DraggedView.ITashScroll() {

                        @Override
                        public void onTouchMove(int x, int y) {
                            if (y - imageCenterY > 0
                                    && y < mtop + child.getHeight()) {
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
        setIVProportionState();
    }

    /***
     * 响应被选中项的UI
     */
    private void onListViewItemSelected() {
        mCurrentScene = mMediaAdapter.getItem(mIndex);
        if (null == mCurrentScene) {
            Log.e(TAG, "onListViewItemSelected:  mCurrentScene is null");
            return;
        }
        MediaObject mediaObject = mCurrentScene.getAllMedia().get(0);

        mHorizontalScrollView.setVisibility(View.VISIBLE);
        mTransitionMenu.setVisibility((!mUIConfig.isHideTransition() && mIndex < mMediaAdapter.getItemCount() - 1) ? View.VISIBLE : View.GONE);

        if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
            onVideoUI();
        } else {
            VideoOb vob = (VideoOb) mediaObject.getTag();
            onPhotoUI(vob.isExtPic == 1, mediaObject.isMotionImage());
        }
    }

    /**
     * 选中视频 UI
     */
    private void onVideoUI() {
        mDuration.setVisibility(View.GONE);
        mFilter.setVisibility(mUIConfig.isHideFilter() ? View.GONE : View.VISIBLE);
        mEffect.setVisibility(mUIConfig.isHideEffects() ? View.GONE : View.VISIBLE);
        mToning.setVisibility(mUIConfig.isHideFilter() ? View.GONE : View.VISIBLE);
        mSpeed.setVisibility(mUIConfig.isHideSpeed() ? View.GONE : View.VISIBLE);
        mEdit.setVisibility(mUIConfig.isHideEdit() ? View.GONE : View.VISIBLE);
        mTrim.setVisibility(mUIConfig.isHideTrim() ? View.GONE : View.VISIBLE);
        mSplit.setVisibility(mUIConfig.isHideSplit() ? View.GONE : View.VISIBLE);
        mReverse.setVisibility(mUIConfig.isHideReverse() ? View.GONE : View.VISIBLE);
        mCopy.setVisibility(mUIConfig.isHideCopy() ? View.GONE : View.VISIBLE);
        mSort.setVisibility(mUIConfig.isHideSort() ? View.GONE : View.VISIBLE);

    }

    /**
     * 图片UI
     *
     * @param isExtPic 是否是文字版图片
     */

    private void onPhotoUI(boolean isExtPic, boolean isMotionImage) {

        //动图支持分割、裁剪 、截取 （属性与视频一样）
        mSplit.setVisibility(isMotionImage ? View.VISIBLE : View.GONE);
        mSpeed.setVisibility(isMotionImage ? View.VISIBLE : View.GONE);

        mEffect.setVisibility(mUIConfig.isHideEffects() ? View.GONE : View.VISIBLE);
        mReverse.setVisibility(View.GONE);
        mText.setVisibility(isExtPic ? View.VISIBLE : View.GONE);
        mTrim.setVisibility(isMotionImage ? View.VISIBLE : View.GONE);
        if (isExtPic) {
            mEdit.setVisibility(View.GONE);
            mDuration.setVisibility(View.GONE);
            mFilter.setVisibility(View.GONE);
            mToning.setVisibility(View.GONE);
        } else {
            mEdit.setVisibility(mUIConfig.isHideEdit() ? View.GONE : View.VISIBLE);
            //动图不支持动态调整duration
            mDuration.setVisibility(mUIConfig.isHideDuration() ? View.GONE : isMotionImage ? View.GONE : View.VISIBLE);
            mFilter.setVisibility(mUIConfig.isHideFilter() ? View.GONE : View.VISIBLE);
            mToning.setVisibility(mUIConfig.isHideFilter() ? View.GONE : View.VISIBLE);
        }
        mCopy.setVisibility(mUIConfig.isHideCopy() ? View.GONE : View.VISIBLE);
        mSort.setVisibility(mUIConfig.isHideSort() ? View.GONE : View.VISIBLE);
    }

    private ExtButton mTrim, mSplit, mSpeed, mDuration, mText, mEdit, mReverse,
            mTransitionMenu, mFilter, mCopy, mToning, mEffect, mSort;
    private TextView mTvTitle;
    private ExtButton mBtnTitleBarLeft;
    private ExtButton mBtnTitleBarRight;

    private ImageView mIvProportion;
    private PriviewLayout mParentFrame;
    private DraggedTrashLayout mDraggedLayout;
    private DraggedView mDraggedView;
    private PriviewLinearLayout mPriviewLinearLayout;


    private void initView() {
        mMediaPlayer = $(R.id.vvMediaPlayer);
        mVideoPreview = $(R.id.rlPreview);
        mIvVideoPlayState = $(R.id.ivPlayerState);
        mTvTotalTime = $(R.id.tvTotalTime);
        mTvCurTime = $(R.id.tvCurTime);
        mSbPreview = $(R.id.sbPreview);
        mRlSplitView = $(R.id.rlSplitView);
        mGridVideosArray = $(R.id.gridVideosDstArray);
        mMainView = $(R.id.preview_edit_layout);
        mSplitLayout = $(R.id.split_layout);

        mTrim = $(R.id.preview_trim);
        mFilter = $(R.id.preview_filter);
        mEffect = $(R.id.preview_effect);
        mSplit = $(R.id.preview_spilt);
        mSpeed = $(R.id.preview_speed);
        mDuration = $(R.id.preview_duration);
        mText = $(R.id.preview_text);
        mEdit = $(R.id.preview_edit);
        mReverse = $(R.id.preview_reverse);
        mTransitionMenu = $(R.id.preview_transition_menu);
        mCopy = $(R.id.preview_copy);
        mSort = $(R.id.preview_sort);
        mToning = $(R.id.preview_toning);
        mTvTitle = $(R.id.tvTitle);
        mBtnTitleBarLeft = $(R.id.btnLeft);
        mBtnTitleBarRight = $(R.id.btnRight);

        mIvProportion = $(R.id.ivProportion);
        mParentFrame = $(R.id.mroot_priview_layout);
        mDraggedLayout = $(R.id.thelinearDraggedLayout);
        mDraggedView = $(R.id.dragged_info_trash_View);
        mPriviewLinearLayout = $(R.id.the_priview_layout_content);
        mHorizontalScrollView = $(R.id.clip_part_menu_layout);

        View addPart = $(R.id.ivParteditAdd);
        if (mUIConfig.mediaCountLimit == 1) {
            //15秒录制短视频
            addPart.setVisibility(View.GONE);
        } else {
            addPart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMainView.setVisibility(View.INVISIBLE);
                    setViewVisibility(R.id.llParteditAdd, true);
                }
            });
            initAddSceneListener();
        }


        llDurationSeekBar = $(R.id.llDurationSeekbar);
        mFlZoom = $(R.id.flUpperZone);
        mFlZoom.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean isFirst = true;

            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    isFirst = false;
                    mVideoPreview.setAspectRatio((float) mFlZoom.getWidth() / mFlZoom.getHeight());
                    $(R.id.llFragmentContainer).getLayoutParams().height = mMainView.getHeight();
                    mSplitLayout.getLayoutParams().height = mMainView.getHeight();
                }
            }
        });

        mVirtualVideo = new VirtualVideo();
        mVideoPreview.setClickable(true);
        mBtnTitleBarLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUIConfig.isEnableWizard()) {
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
        if (mUIConfig.isEnableWizard()) {
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
        setIVProportionState();
        if (!mUIConfig.enableMV) {
            mIvProportion.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mProportionDialog.show();
                    mProportionDialog.mIndex = mProportionStatus;
                    mProportionDialog.resetStatus();
                }
            });
        }
        mSplitHandler = new SplitHandler(this, mParentFrame, iSplitHandler);
        mLastPlayPostion = -1;
        mMediaPlayer.setOnPlaybackListener(mPlayViewListener);
        mMediaPlayer.setOnInfoListener(mInfoListener);
        mMediaPlayer.setOnClickListener(mPlayerUIListener);
        mIvVideoPlayState.setOnClickListener(mPlayerUIListener);
        mGridVideosArray.setLongLisenter(onDragLongListener);
        mGridVideosArray.setOnItemClickListener(mItemListener);
        mGridVideosArray.setAddItemListener(mAddItemListener);
        // 设置项目大小
        mGridVideosArray.setItemSize(R.dimen.priview_item_width_plus, R.dimen.priview_item_height_plus);
        mGridVideosArray.setHideAddItemWithoutSort(mUIConfig.isHideTransition());
    }

    /**
     * 添加镜头
     */
    private void initAddSceneListener() {

        $(R.id.ivPardeditAddCancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainView.setVisibility(View.VISIBLE);
                setViewVisibility(R.id.llParteditAdd, false);
            }
        });
        $(R.id.tvAddImage).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedia(false);
            }
        });
        $(R.id.tvAddVideo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedia(true);
            }
        });
        $(R.id.tvAddText).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onText();
            }
        });
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
            returnToMenu();


            Scene scene = mSceneList.remove(mIndex);
            mMediaAdapter.removeItem(scene);
            MediaObject mediaObject = scene.getAllMedia().get(0);
            VideoOb videoOb = (VideoOb) mediaObject.getTag();
            float speed = mediaObject.getSpeed();
            if (mediaObject != null) {
                VideoOb vob = (VideoOb) mediaObject.getTag();
                if (vob != null) {
                    int len = list.size();
                    for (int i = 0; i < len; i++) {
                        MediaObject mo = list.get(i);
                        VideoOb item = new VideoOb(videoOb);
                        item.TStart = mo.getTrimStart();
                        item.TEnd = mo.getTrimEnd();
                        item.nStart = item.TStart / speed;
                        item.nEnd = item.TEnd / speed;
                        item.rStart = item.TStart;
                        item.rEnd = item.TEnd;
                        mo.setTag(item);
                        Scene newScene = VirtualVideo.createScene();
                        newScene.addMedia(mo);
                        mMediaAdapter.addItem(i + mIndex, newScene);
                        mSceneList.add(i + mIndex, newScene);
                    }
                }
            }
            if (null != scene) {
                scene.getAllMedia().clear();
            }
            if (null != list) {
                list.clear();
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
        }

        @Override
        public void onTemp(ArrayList<MediaObject> list, int progress) {

            mHasChanged = true;
            boolean isPlaying = mMediaPlayer.isPlaying();
            mSplitHandler.setPrepared(false);
            mMediaPlayer.reset();
            mVirtualVideo.reset();
            int len = list.size();
            for (int i = 0; i < len; i++) {
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

    };

    private boolean mOnTemp = false;

    private void cancelSplit() {
        mDraggedView.setTrash(false);
        mSbPreview.setVisibility(View.VISIBLE);
        mRlSplitView.setVisibility(View.GONE);
        mMainView.setVisibility(View.VISIBLE);
        mHasChanged = false;
        returnToMenu();
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

    private BaseFragment mFragCurrent;

    /**
     * 切换fragment
     */
    private void changeToFragment(final BaseFragment fragment, boolean isMenuVisiable) {
        if (mFragCurrent == fragment) {
            // 未实际切换fragment时，直接返回
            return;
        }
        mFragCurrent = fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.llFragmentContainer, fragment);
        ft.commit();
        setViewVisibility(R.id.rlEditMenu, isMenuVisiable);
    }

    /**
     * 播放器
     */
    private VirtualVideoView.VideoViewListener mPlayViewListener = new VirtualVideoView.VideoViewListener() {


        @Override
        public void onPlayerPrepared() {
            SysAlertDialog.cancelLoadingDialog();
            updatePreviewFrameAspect(mMediaPlayer.getWidth(), mMediaPlayer.getHeight());
            mMediaAdapter.notifyDataSetChanged();
            mMediaPlayer.setFilterType(0);
            mPlaybackDurationMs = Utils.s2ms(mMediaPlayer.getDuration());
            mTvCurTime.setText(getTime(Utils.s2ms(mMediaPlayer.getCurrentPosition())));
            mTvTotalTime.setText(getTime(mPlaybackDurationMs));
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
                    "",
                    getString(R.string.error_preview_crop),
                    getString(R.string.sure), null, null, null);
            return false;
        }

        @Override
        public void onPlayerCompletion() {
            mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_play);
            if (mSplitHandler.isSpliting()) {

            } else {
                mIvVideoPlayState.setVisibility(View.VISIBLE);
            }
            mMediaPlayer.seekTo(0);
            mSbPreview.setProgress(0);
            mTvCurTime.setText(getTime(0));
            mTvTotalTime.setText(getTime(mPlaybackDurationMs));
            if (mSplitHandler.isSpliting()) {
                mSplitHandler.onScrollCompleted();
            }
        }

        @Override
        public void onGetCurrentPosition(float nPosition) {
            if (nPosition == 0) {
                return;
            }
            int ms = Utils.s2ms(nPosition);
            mTvCurTime.setText(getTime(ms));
            mSbPreview.setProgress(ms);
            if (mSplitHandler.isSpliting()) {
                mSplitHandler.onScrollProgress(ms);
            }
        }
    };

    private boolean mIsContinue = false;

    private void onContinue() {
        mIsContinue = true;
        int importDurationLimit = (int) SdkEntry.getSdkService().getExportConfig().importVideoDuration;
        if (importDurationLimit != 0 && mMediaPlayer.getDuration() >= importDurationLimit) {
            SysAlertDialog.showAutoHideDialog(this, "", getString(R.string.import_duration_limit, importDurationLimit), 2500);
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mSceneList);
        if (null != shortVideoInfoImp) {
            intent.putExtra(IntentConstants.INTENT_EXTRA_DRAFT, shortVideoInfoImp.getId());
        }
        intent.putExtra(IntentConstants.EXTRA_MEDIA_PROPORTION, getAsp(mProportionStatus));
        intent.putExtra(IntentConstants.EDIT_PROPORTION_STATUS, mProportionStatus);
        intent.putExtra(IntentConstants.EXTRA_PLAYER_WIDTH, mMediaPlayer.getWordLayout().getWidth());
        intent.putExtra(IntentConstants.EXTRA_PLAYER_HEIGHT, mMediaPlayer.getWordLayout().getHeight());
        intent.putExtra(IntentConstants.EXTRA_LAST_DURATION, mPlaybackDurationMs);
        intent.putExtra(IntentConstants.EXTRA_IS_EDIT_PART, mBackDiaglog);
        if (mUIConfig.isEnableWizard()) {
            intent.setClass(this, VideoEditActivity.class);
            startActivityForResult(intent, REQUESTCODE_FOR_ADVANCED_EDIT);
        } else {
            setResult(RESULT_OK, intent);
            finish();
        }
        overridePendingTransition(0, 0);
    }


    private int mIndex = 0;// 记录正在编辑的视频的index
    private int mAddItemIndex = -1; // 记录当前加号按钮的index
    private final int REQUESTCODE_FOR_SPEED = 7;
    private final int REQUESTCODE_FOR_APPEND = 10;
    private final int REQUESTCODE_FOR_DURATION = 11;
    private final int REQUESTCODE_FOR_EDIT_PIC = 12;
    private final int REQUESTCODE_FOR_SORT = 13;
    public static final int REQUESTCODE_FOR_TRIM = 14;
    private final int REQUESTCODE_FOR_TRANSITION = 15;
    private final int REQUESTCODE_FOR_EDIT = 16;
    private final int REQUESTCODE_FOR_CAMERA = 17;
    private final int REQUESTCODE_FOR_ADVANCED_EDIT = 18;
    private final int REQUESTCODE_FOR_MEDIA_FILTER = 19;
    private final int REQUESTCODE_FOR_MEDIA_FILTER_CONFIG = 20;
    private final int REQUESTCODE_FOR_MEDIA_EFFECT = 21;

    /**
     * 是否支持视频属性 （视频 |动图）
     */
    private boolean isSupportVideoAttributes(MediaObject mediaObject) {
        return mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE || (mediaObject.getMediaType() == MediaType.MEDIA_IMAGE_TYPE && mediaObject.isMotionImage());
    }


    /**
     * 响应剪辑功能按钮点击
     */
    public void onPreviewOptionClick(View v) {
        int id = v.getId();
        if (mIndex < 0) {
            return;
        }
        stopVideo();
        Scene scene = mMediaAdapter.getItem(mIndex);
        if (id == R.id.preview_toning) {
            MediaFilterConfigActivity.onFilterConfig(this, scene, REQUESTCODE_FOR_MEDIA_FILTER_CONFIG);
        } else if (id == R.id.preview_filter) {
            MediaFilterActivity.onMediaFilter(this, scene, REQUESTCODE_FOR_MEDIA_FILTER);
        } else if (id == R.id.preview_effect) {
            MediaObject mediaObject = scene.getAllMedia().get(0);
            //特效
            MediaEffectActivity.onMediaEffect(this, mediaObject, REQUESTCODE_FOR_MEDIA_EFFECT);
        } else if (id == R.id.preview_spilt) {
            MediaObject mediaObject = scene.getAllMedia().get(0);
            if (isSupportVideoAttributes(mediaObject)) {
                final float MIN_SPLIT_LIMIT = 0.5f;
                mCurrentScene = mMediaAdapter.getItem(mIndex);
                if (mCurrentScene.getDuration() <= MIN_SPLIT_LIMIT) {
                    onToast(getString(R.string.video_duration_too_short_to_split, MIN_SPLIT_LIMIT));
                } else {
                    mDraggedView.setTrash(true);

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
                    mRlSplitView.setVisibility(View.VISIBLE);
                    mMainView.setVisibility(View.INVISIBLE);
                    mSplitLayout.setVisibility(View.VISIBLE);
                    llDurationSeekBar.setVisibility(View.INVISIBLE);
                    hideTitlebar();
                    mIvProportion.setVisibility(View.GONE);
                    mMediaPlayer.start();
                }
            }
        } else if (id == R.id.preview_edit) {  //编辑
            CropRotateMirrorActivity.onCropRotate(this, scene, mMediaPlayer.getVideoWidth() / (float) mMediaPlayer.getVideoHeight(), REQUESTCODE_FOR_EDIT);
        } else if (id == R.id.preview_speed) {
            //调速
            MediaObject mediaObject = scene.getAllMedia().get(0);
            if (isSupportVideoAttributes(mediaObject)) {
                Intent intent = new Intent(this, SpeedPreviewActivity.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
                intent.putExtra(IntentConstants.ALL_MEDIA_MUTE, getIntent()
                        .getBooleanExtra(IntentConstants.ALL_MEDIA_MUTE, false));
                startActivityForResult(intent, REQUESTCODE_FOR_SPEED);
            } else {
                onToast(R.string.fix_video);
            }

        } else if (id == R.id.preview_copy) {
            mBackDiaglog = true;
            Scene newScene = VirtualVideo.createScene();
            for (MediaObject mo : scene.getAllMedia()) {
                newScene.addMedia(mo);
            }

            ArrayList<Scene> list = mMediaAdapter.getMediaList();
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


            mMediaAdapter.updateDisplay();

            initListView(mIndex);
            reload();

            setSeekTo(true);
            seekToPosition(mIndex, false);

            playVideo();
            updateView();
        } else if (id == R.id.preview_reverse) {
            MediaObject mediaObject = scene.getAllMedia().get(0);
            if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                VideoOb ob = (VideoOb) mediaObject.getTag();
                if (ob.getVideoObjectPack() == null) {  //第一次倒序
                    reverseVideo(mediaObject);
                } else {   // 不为空就不是第一次倒序了
                    VideoObjectPack vop = ob.getVideoObjectPack();
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
                    reverseScene.setTransition(scene.getTransition());
                    mMediaAdapter.getMediaList().set(mIndex, reverseScene);
                    mMediaAdapter.notifyDataSetChanged();// m_adaDstVideos.getItem(mIndex).setMediaFilePath(strTempOutPath);
                    mSceneList.set(mIndex, reverseScene);
                    reload();

                    setSeekTo(true);
                    seekToPosition(mIndex, false);
                }

            } else {
                onToast(R.string.fix_video);
            }
        } else if (id == R.id.preview_duration) {
            ImageDurationActivity.onImageDuration(this, scene, false, REQUESTCODE_FOR_DURATION);
        } else if (id == R.id.preview_text) {
            MediaObject mediaObject = scene.getAllMedia().get(0);
            VideoOb vob = (VideoOb) mediaObject.getTag();
            ExtPhotoActivity.editTextPic(this, vob.getExtpic(), REQUESTCODE_FOR_EDIT_PIC);
            updateView();
        } else if (id == R.id.preview_trim) {
            final float MIN_TRIM_LIMIT = 1f;
            MediaObject mediaObject = scene.getAllMedia().get(0);
            if (mediaObject.getDuration() < MIN_TRIM_LIMIT) {
                Utils.autoToastNomal(this, getString(R.string.video_duration_too_short_to_trim, MIN_TRIM_LIMIT));
            } else {
                Intent intent = new Intent(EditPreviewActivity.this, TrimMediaActivity.class);
                intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, scene);
                intent.putExtra(IntentConstants.TRIM_FROM_EDIT, true);
                startActivityForResult(intent, REQUESTCODE_FOR_TRIM);
                overridePendingTransition(0, 0);
            }
        } else if (id == R.id.preview_sort) {
            onSortMedia();
        } else if (id == R.id.preview_transition_menu) {
            mAddItemIndex = mIndex + 1;
            if (checkMediaDuration(mAddItemIndex)) {
                onTransition(mIndex);
            } else {
                Utils.autoToastNomal(this, getString(R.string.video_duration_too_short_to_transition, MIN_TRANSITION_LIMIT));
            }
        }
    }

    /**
     * 添加媒体
     *
     * @param isVideo
     */
    private void addMedia(boolean isVideo) {
        Log.e(TAG, "addMedia: " + isVideo);
        if (mIsUseCustomUI) {
            SdkEntryHandler.getInstance().onSelectVideo(
                    EditPreviewActivity.this);
        } else {
            int maxCount = getMaxAppend();
            if (maxCount == 0) {
                onToast(getString(R.string.media_un_exceed_num, mUIConfig.mediaCountLimit));
            } else {
                if (isVideo) {
                    SelectMediaActivity.appendMedia(this, false, maxCount, REQUESTCODE_FOR_APPEND);
                } else {
                    SelectMediaActivity.appendMedia(this, true, getIntent().getBooleanExtra(SelectMediaActivity.LOTTIE_IMAGE, false),
                            maxCount, REQUESTCODE_FOR_APPEND);
                }
            }
        }
    }


    /**
     * 进入文字界面
     */
    private void onText() {
        Intent intent = new Intent(EditPreviewActivity.this, ExtPhotoActivity.class);
        startActivityForResult(intent, REQUESTCODE_FOR_APPEND);
    }

    private ArrayList<Transition> mDefaultTransitionList;

    /**
     * 切换转场界面
     */
    private void onTransition(int index) {
        if (mTransitionFragment == null) {
            mTransitionFragment = new TransitionFragment();
            mTransitionFragment.setUrl(mUIConfig.mResTypeUrl,mUIConfig.transitionUrl);
        }
        if (mDefaultTransitionList == null) {
            mDefaultTransitionList = new ArrayList<>();
        }
        mDefaultTransitionList.clear();
        for (Scene scene : mSceneList) {
            mDefaultTransitionList.add(scene.getTransition());
        }
        mTransitionFragment.setSceneCount(mSceneList.size());
        mTransitionFragment.setCurTransition(mSceneList.get(index).getTransition());
        hideTitlebar();
        changeToFragment(mTransitionFragment, false);
    }

    private void hideTitlebar() {
        setViewVisibility(R.id.titlebar_layout, false);
    }


    private void returnToMenu() {
        if (null != mFragCurrent) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mFragCurrent);
            ft.commit();
            mFragCurrent = null;
        }
        setViewVisibility(R.id.rlEditMenu, true);

        llDurationSeekBar.setVisibility(View.VISIBLE);
        setViewVisibility(R.id.titlebar_layout, true);
    }

    private boolean mIsReversing = false;

    private void reverseVideo(final MediaObject mediaObject) {
        mIsReversing = true;
        VideoConfig videoConfig = ExportHandler.getExportConfig(0);
        // videoConfig.setSize(848,480);可指定的输出分辨率,一般不用设置，
        // 需根据系统版本动态变化,好的机型可以设置更高分辨率(最高1280*720)
        final String strTempOutPath = PathUtils.getTempFileNameForSdcard(
                "reverse", "mp4"); // 生成临时文件路径，这个路径，用于替换倒放前媒体信息
        ExportUtils.reverseSave(this, mediaObject, strTempOutPath, videoConfig, new ExportListener() {
            private HorizontalProgressDialog horiProgressSave = null;
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
                            SysAlertDialog.showAlertDialog(
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
                    MediaObject outputmo = null;
                    try {
                        outputmo = scene.addMedia(strTempOutPath);
                        computeShowRect(outputmo, mediaObject);
                        VideoOb oldvo = (VideoOb) mediaObject.getTag();
                        oldvo.setVideoObjectPack(null);
                        VideoOb newvo = new VideoOb(0, outputmo.getTrimEnd(), 0, outputmo.getTrimEnd(), 0,
                                outputmo.getTrimEnd(), 0, null, oldvo.getCropMode());
                        mediaObject.setTag(oldvo);
                        newvo.setVideoObjectPack(new VideoObjectPack(mediaObject, true,
                                oldvo.rStart, oldvo.rStart + outputmo.getTrimEnd()));
                        outputmo.setTag(newvo);

                        scene.setTransition(mMediaAdapter.getItem(mIndex).getTransition());

                        mMediaAdapter.getMediaList().set(mIndex, scene);
                        mMediaAdapter.notifyDataSetChanged();
                        mSceneList.set(mIndex, scene);
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
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

    private final float MIN_TRANSITION_LIMIT = 0.5f;

    private boolean checkMediaDuration(int addIndex) {
        if (addIndex < 1 || addIndex > (mSceneList.size() - 1)) {
            return false;
        }
        Scene sceneFront = mSceneList.get(addIndex - 1);
        Scene sceneBelow = mSceneList.get(addIndex);
        if (sceneFront.getDuration() < MIN_TRANSITION_LIMIT || sceneBelow.getDuration() < MIN_TRANSITION_LIMIT) {
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
        stopVideo();
        Scene scene = mMediaAdapter.getItem(mIndex);
        if (null != scene) {
            try {
                mMediaAdapter.removeItem(scene);
                mSceneList.remove(mIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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


    /***
     * 检测显示比例按钮是否可用（ mv 时不可用）
     */
    private void setIVProportionState() {
        if (mUIConfig.isHideProportion()) {
            mIvProportion.setVisibility(View.GONE);
        } else {
            if (!mUIConfig.enableMV) {
                //还原到最初指定显示状态
                mIvProportion.setVisibility(View.VISIBLE);
                mProportionStatus = lastProportionStatus;
            } else {
                mIvProportion.setVisibility(View.GONE);
            }

        }
        mIvProportion.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSbPreview.setHighLights(null);
        if (resultCode == RESULT_OK) {
            mBackDiaglog = true;
            setSeekTo(true);
            if (requestCode == REQUESTCODE_FOR_MEDIA_EFFECT) {   //特效
                MediaObject mediaObject = data.getParcelableExtra(IntentConstants.INTENT_MEDIA_OBJECT);
                if (null != mediaObject) {
                    Scene old = mSceneList.get(mIndex);
                    Transition transition = old.getTransition();
                    Scene scene = VirtualVideo.createScene();
                    scene.addMedia(mediaObject);
                    if (null != transition) {     //还原转场
                        scene.setTransition(transition);
                    }
                    mMediaAdapter.getMediaList().set(mIndex, scene);
                    mMediaAdapter.notifyDataSetChanged();
                    mSceneList.set(mIndex, scene);
                    onListViewItemSelected();
                    reload();
                }
            } else if (requestCode == REQUESTCODE_FOR_MEDIA_FILTER_CONFIG || requestCode == REQUESTCODE_FOR_MEDIA_FILTER) {
                //result_ok 真实的
                //调色、滤镜
                Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                boolean isAllPartApply = data.getBooleanExtra(IntentConstants.INTENT_ALL_APPLY, false);
                if (null != scene) {
                    if (isAllPartApply) {
                        //应用调色到全部片段
                        MediaObject mediaObject = scene.getAllMedia().get(0);
                        Object obj = mediaObject.getTag();
                        if (obj instanceof VideoOb) {
                            VideoOb videoOb = (VideoOb) obj;
                            IMediaParamImp mediaParamImp = videoOb.getMediaParamImp();
                            if (null != mediaParamImp) {
                                boolean isFilterResult = requestCode == REQUESTCODE_FOR_MEDIA_FILTER;
                                //找出其他的片段中的
                                int len = mMediaAdapter.getMediaList().size();
                                for (int i = 0; i < len; i++) {
                                    if (i != mIndex) {
                                        Scene item = mMediaAdapter.getMediaList().get(i);
                                        MediaObject src = item.getAllMedia().get(0);
                                        mModel.applyMediaParamToAll(src, mediaParamImp, isFilterResult);
                                        mSceneList.set(i, item);
                                    }
                                }
                            }
                        }
                    }
                    mMediaAdapter.getMediaList().set(mIndex, scene);
                    mMediaAdapter.notifyDataSetChanged();
                    mSceneList.set(mIndex, scene);
                    onListViewItemSelected();
                    reload();
                }
            } else if (requestCode == REQUESTCODE_FOR_SPEED) {
                Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                float speed = scene.getAllMedia().get(0).getSpeed();
                if (data.getBooleanExtra(IntentConstants.INTENT_ALL_APPLY, false)) {
                    for (Scene sen : mSceneList) {
                        for (MediaObject mediaObject : sen.getAllMedia()) {
                            if (mediaObject.getMediaType().equals(MediaType.MEDIA_VIDEO_TYPE)) {
                                float oldSpeed = mediaObject.getSpeed();
                                mediaObject.setSpeed(speed);
                                VideoOb temp = (VideoOb) mediaObject.getTag();
                                if (null != temp) {
                                    float ft = mediaObject.getSpeed() / oldSpeed;
                                    temp.nStart = temp.nStart / ft;
                                    temp.nEnd = temp.nEnd / ft;
                                    mediaObject.setTag(temp);
                                }
                                //修正特效有效时间线
                                EffectManager.fixEffect(mediaObject, mediaObject.getEffectInfos());
                            }
                        }
                    }
                }
                scene.setTransition(mMediaAdapter.getItem(mIndex).getTransition());
                mMediaAdapter.getMediaList().set(mIndex, scene);
                mMediaAdapter.notifyDataSetChanged();
                mSceneList.set(mIndex, scene);

                onListViewItemSelected();
                reload();
            } else if (requestCode == REQUESTCODE_FOR_EDIT) {
                onEditResult(data);
            } else if (requestCode == REQUESTCODE_FOR_APPEND) {
                ArrayList<MediaObject> tempMedias = data
                        .getParcelableArrayListExtra(IntentConstants.EXTRA_MEDIA_LIST);
                int isextPic = data.getIntExtra(
                        IntentConstants.EXTRA_EXT_ISEXTPIC, 0);
                int len = tempMedias.size();
                mMainView.setVisibility(View.VISIBLE);
                setViewVisibility(R.id.llParteditAdd, false);
                for (int i = 0; i < len; i++) {
                    MediaObject mo = tempMedias.get(i);
                    int addPosition;
                    addPosition = mIndex + i + 1;
                    Scene scene = VirtualVideo.createScene();
                    scene.addMedia(mo);
                    mMediaAdapter.addItem(addPosition, scene);
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
                float duration = data.getFloatExtra(IntentConstants.EXTRA_EXT_APPLYTOALL_DURATION, 0);

                if (duration != 0) {
                    for (int n = 0; n < mSceneList.size(); n++) {
                        for (MediaObject media : mSceneList.get(n).getAllMedia()) {
                            if (media.getMediaType() == MediaType.MEDIA_IMAGE_TYPE) {
                                VideoOb temp = (VideoOb) media.getTag();
                                if (temp.isExtPic == 0 && !media.isMotionImage()) { //普通静态图片有效
                                    media.setIntrinsicDuration(duration);
                                    media.setTimeRange(0, duration);
                                    temp.nStart = media.getTrimStart();
                                    temp.nEnd = media.getIntrinsicDuration();
                                    temp.rStart = temp.nStart;
                                    temp.rEnd = temp.nEnd;
                                    temp.TStart = temp.nStart;
                                    temp.TEnd = temp.nEnd;
                                    EffectManager.fixEffect(media, media.getEffectInfos());
                                    mMediaAdapter.getMediaList().set(n, mSceneList.get(n));
                                    mSceneList.set(n, mSceneList.get(n));
                                }
                            }
                        }
                    }
                } else {
                    Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
                    mMediaAdapter.getMediaList().set(mIndex, scene);
                    mSceneList.set(mIndex, scene);
                }
                reload();
                mMediaAdapter.notifyDataSetChanged();
                onListViewItemSelected();
                playVideo();
            } else if (requestCode == REQUESTCODE_FOR_EDIT_PIC) {
                MediaObject media = data
                        .getParcelableExtra(IntentConstants.EXTRA_MEDIA_OBJECTS);
                VideoOb nvb = new VideoOb(media.getTrimStart(), media.getTrimEnd(),
                        media.getTrimStart(), media.getTrimEnd(),
                        media.getTrimStart(), media.getTrimEnd(),
                        1, (ExtPicInfo) data.getParcelableExtra(IntentConstants.EXTRA_EXT_PIC_INFO), VideoOb.DEFAULT_CROP);
                Scene scene = VirtualVideo.createScene();
                media.setTag(nvb);
                scene.addMedia(media);
                mMediaAdapter.getMediaList().set(mIndex, scene);
                mSceneList.set(mIndex, scene);
                mCurrentScene = mMediaAdapter.getItem(mIndex);
                mGridVideosArray.post(new Runnable() {

                    @Override
                    public void run() {
                        initListView(mIndex);
                    }
                });
                reload();
            } else if (requestCode == REQUESTCODE_FOR_SORT) {
                mMediaAdapter.clear();
                mSceneList = data.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_SCENE);
                for (Scene scene : mSceneList) {
                    mMediaAdapter.addItem(scene);
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
                mMediaAdapter.getMediaList().set(mIndex, newScene);
                reload();
                mMediaAdapter.notifyDataSetChanged();
                onListViewItemSelected();

            } else if (requestCode == REQUESTCODE_FOR_TRANSITION) {
                ArrayList<Transition> arrTransitions = data.getParcelableArrayListExtra(IntentConstants.INTENT_EXTRA_TRANSITION);
                if (data.getBooleanExtra(IntentConstants.TRANSITION_APPLY_TO_ALL, false)) {
                    int len = arrTransitions.size();
                    for (int nTemp = 0; nTemp < len; nTemp++) {
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
            //result_cancel
            if (requestCode == REQUESTCODE_FOR_ADVANCED_EDIT) {
                if (null != shortVideoInfoImp) {
                    setResult(RESULT_CANCELED);
                } else {
                    setResult(RESULT_OK, data);
                }
                finish();
                return;
            } else {
                playVideo();
            }
            setSeekTo(false);
        }
        updateView();
    }


    /**
     * 裁剪-旋转编辑完成
     */
    private void onEditResult(Intent data) {
        if (null != data) {
            boolean isApplyToAll = data.getBooleanExtra(IntentConstants.INTENT_ALL_APPLY, false);
            Scene old = mMediaAdapter.getItem(mIndex);
            if (isApplyToAll) {
                RCInfo param = data.getParcelableExtra(IntentConstants.INTENT_ALL_APPLY_PARAM);
                if (null != param) { //裁剪应用到全部
                    mModel.fixAllMediaRC(param, mSceneList);
                } else {
                    isApplyToAll = false;
                }
            }

            Scene scene = data.getParcelableExtra(IntentConstants.INTENT_EXTRA_SCENE);
            mSceneList.set(mIndex, scene);
            if (isApplyToAll) { //全部更新
                mMediaAdapter.updateThumb();
            } else { //单个更新
                mMediaAdapter.onClear(old);
                mMediaAdapter.getMediaList().set(mIndex, scene);
                mMediaAdapter.notifyDataSetChanged();
            }
        }
        mGridVideosArray.post(new Runnable() {
            @Override
            public void run() {
                initListView(mIndex);
            }
        });
        reload();
    }


    /**
     * 添加到扩展类
     */
    private void addVideoObToMedia(MediaObject media, int isextpic, ExtPicInfo info) {
        if (media.getTag() == null) {
            media.setTag(new VideoOb(media.getTrimStart(), media.getTrimEnd(), media
                    .getTrimStart(), media.getTrimEnd(), media.getTrimStart(),
                    media.getTrimEnd(), isextpic, info, VideoOb.DEFAULT_CROP));
        }
    }

    /**
     * 设置适配器
     *
     * @param index 默认选中项
     */
    private void initListView(int index) {
        mGridVideosArray.setAdapter(mMediaAdapter);
        mCurrentScene = mMediaAdapter.getItem(mIndex);
        int len = mSceneList.size();
        if (mAddItemIndex >= len) {
            mAddItemIndex = len - 1;
        }
        onDragItemClick(index);
    }


    /**
     * 更新预览视频播放器比例
     */
    private void updatePreviewFrameAspect(int nVideoWidth, int nVideoHeight) {
        if (mVideoPreview != null) {
            mCurAspect = (float) (nVideoWidth / (nVideoHeight + 0.0));
        }
    }


    @Override
    public void onBackPressed() {
        if (mFragCurrent != null) {
            if (mFragCurrent == mTransitionFragment) {
                onBack();
                return;
            }
        }
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
            if (mUIConfig.isEnableWizard()) {
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
    protected void onDestroy() {
        if (!mIsContinue) {
            SysAlertDialog.cancelLoadingDialog();
        }
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.cleanUp();
            mMediaPlayer = null;
        }
        if (null != mVirtualVideo) {
            mVirtualVideo.release();
            mVirtualVideo = null;
        }
        if (mMediaAdapter != null) {
            mMediaAdapter.onDestroy();
            mMediaAdapter = null;
        }

        unregisterReceiver(mReceiver);
        if (null != TempVideoParams.getInstance() && mUIConfig.isEnableWizard()) {
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
        if (null != mSceneList) {
            mSceneList.clear();
            mSceneList = null;
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        SysAlertDialog.cancelLoadingDialog();
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mMediaPlayer) {
            mLastPlayPostion = mMediaPlayer.getCurrentPosition();
            mLastPlaying = mMediaPlayer.isPlaying();
            pauseVideo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mParsedata = true;
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
            if (mLastPlaying) {
                playVideo();
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
        mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_pause);
        if (mSplitHandler.isSpliting()) {
            mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_pause);
        }
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
        mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_play);
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
        mIvVideoPlayState.setBackgroundResource(R.drawable.btn_edit_play);

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
                mTvCurTime.setText(getTime(progress));
                mTvTotalTime.setText(getTime(mPlaybackDurationMs));
                mMediaPlayer.seekTo(Utils.ms2s(progress));
            }
        }
    };

    private DragItemListener mDragItemListener = new DragItemListener() {
        @Override
        public void onRemove(int position) {
            if (mMediaAdapter.getCount() == 1) {
                int msgResId = R.string.just_only_one_scene;
                try {
                    Scene scene = mMediaAdapter.getItem(0);
                    if (null != scene) {
                        List<MediaObject> list = scene.getAllMedia();
                        if (null != list && list.size() >= 1) {
                            MediaType mediaType = list.get(0).getMediaType();
                            if (mediaType == MediaType.MEDIA_VIDEO_TYPE) {
                                msgResId = R.string.just_only_one_video;
                            } else if (mediaType == MediaType.MEDIA_IMAGE_TYPE) {
                                msgResId = R.string.just_only_one_image;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                onToast(getString(msgResId));
            } else {
                mIndex = position;
                onCreateDialog(DIALOG_REMOVE_ID).show();
            }

        }

        @Override
        public boolean isExt(int position) {
            if (null == mSceneList || position >= mSceneList.size()) {
                return false;
            }
            Scene scene = mSceneList.get(position);
            if (null == scene || scene.getAllMedia().isEmpty()) {
                return false;
            }
            MediaObject mo = scene.getAllMedia().get(0);
            Object object = mo.getTag();
            if (!(object instanceof VideoOb)) {
                return false;
            }
            VideoOb vo = (VideoOb) object;
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
            for (Scene scene : mMediaAdapter.getMediaList()) {
                for (MediaObject mediaObject : scene.getAllMedia()) {
                    mediaObject.setAudioMute(true);
                }
            }
        }
        mMediaPlayer.reset();
        mVirtualVideo.reset();
        mSbPreview.setHighLights(null);
//        setAllMediaAspectRatio(AspectRatioFitMode.KEEP_ASPECTRATIO);
        for (Scene scene : mMediaAdapter.getMediaList()) {
            for (MediaObject mediaObject : scene.getAllMedia()) {
                if (mIsEnableBackground) {
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO);
                } else {
                    mediaObject.setAspectRatioFitMode(AspectRatioFitMode.KEEP_ASPECTRATIO_EXPANDING);
                }
            }
            mVirtualVideo.addScene(scene);
        }
        mMediaPlayer.setPreviewAspectRatio(mCurProportion);
        try {
            mVirtualVideo.build(mMediaPlayer);
        } catch (InvalidStateException e) {
            e.printStackTrace();
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
        if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_SQUARE) {
            mProportionStatus = ProportionDialog.ORIENTATION_SQUARE;
        } else if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_LANDSCAPE) {
            mProportionStatus = ProportionDialog.ORIENTATION_LANDSCAPE;
            // 横屏
        } else if (mProportionDialog.mIndex == ProportionDialog.ORIENTATION_PORTRAIT) {
            mProportionStatus = ProportionDialog.ORIENTATION_PORTRAIT;
            // 竖屏
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
                        //恢复为自动模式
                        mProportionDialog.mIndex = ProportionDialog.ORIENTATION_AUTO;
                        mProportionStatus = ProportionDialog.ORIENTATION_AUTO;
                        break;
                    }
                }

            }

        } else {
            mProportionStatus = ProportionDialog.ORIENTATION_AUTO;
        }
        lastProportionStatus = mProportionStatus;
        reload();
        playVideo();
        mProportionDialog.resetStatus();
    }


    /**
     * 排序
     */
    private void onSortMedia() {
        Intent intent = new Intent(this, SortMediaActivity.class);
        intent.putExtra(IntentConstants.INTENT_EXTRA_SCENE, mMediaAdapter.getMediaList());
        startActivityForResult(intent, REQUESTCODE_FOR_SORT);
        overridePendingTransition(0, 0);
    }

    private DraggableAddGridView.AddItemOnClickListener mAddItemListener = new DraggableAddGridView.AddItemOnClickListener() {

        @Override
        public void onClick(int index) {
            mAddItemIndex = index + 1;
            onTransition(index);
        }

        @Override
        public void addItemClick(int type) {
            if (type == 1) {
                mAddItemIndex = -1;
                if (mIsUseCustomUI) {
                    SdkEntryHandler.getInstance().onSelectVideo(
                            EditPreviewActivity.this);
                } else {
                    int maxCount = getMaxAppend();
                    if (maxCount == 0) {
                        onToast(getString(R.string.media_un_exceed_num, mUIConfig.mediaCountLimit));
                    } else {
                        SelectMediaActivity.appendMedia(EditPreviewActivity.this, true,
                                getIntent().getBooleanExtra(SelectMediaActivity.LOTTIE_IMAGE, false), maxCount, REQUESTCODE_FOR_APPEND);
                    }
                }
            } else if (type == 2) {
                //排序界面时，禁用当前activity中的广播追加的回调
                mParsedata = false;
                onSortMedia();
            }
        }

        @Override
        public void reorderAddItem(ArrayList<Scene> arr, int drag) {
            mSceneList = arr;
            onDragItemClick(drag);
        }
    };

    private int getMaxAppend() {
        return mUIConfig.mediaCountLimit > 0 ? (mUIConfig.mediaCountLimit - mSceneList.size()) : -1;
    }

    /**
     * 是否解析自定义相册发送的广播
     */
    private boolean mParsedata = true;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mParsedata) {
                String action = intent.getAction();
                if (TextUtils.equals(action, SdkEntry.ALBUM_CUSTOMIZE)) {
                    boardcastResult(intent);
                }
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
                        MediaObject mo = null;
                        try {
                            mo = scene.addMedia(nMediaKey);
                            if (scene != null && mo != null) {
                                mo.setTag(VideoOb.createVideoOb(mo.getMediaPath()));
                                alMedias.add(scene);
                            }
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                            onToast(getString(R.string.media_exception));
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
                    int addPosition;
                    if (mAddItemIndex != -1) {
                        addPosition = mAddItemIndex + i;
                    } else {
                        addPosition = mSceneList.size();
                    }
                    mMediaAdapter.addItem(addPosition, scene);
                    mSceneList.add(addPosition, scene);
                }
                reload();
                playVideo();
                initListView(mIndex);
            }
        });

    }


    @Override
    public void onTransitionDurationChanged(float duration, boolean isApplyToAll) {
        if (isApplyToAll) {
            for (Scene scene : mSceneList) {
                if (scene.getTransition() != null) {
                    scene.getTransition().setDuration(duration);
                }
            }
        } else {
            Transition transition = mSceneList.get(mAddItemIndex - 1).getTransition();
            if (transition != null) {
                transition.setDuration(duration);
            }
        }
        reload();
        if (isApplyToAll) {
            seekToPosition(0, true);
        } else {
            seekToPosition(mIndex, true);
        }
    }

    @Override
    public void onTransitionChanged(ArrayList<Transition> listTransition, boolean isApplyToAll) {
        if (isApplyToAll) {
            int len = Math.min(listTransition.size(), mSceneList.size());
            for (int nTemp = 0; nTemp < len; nTemp++) {
                mSceneList.get(nTemp).setTransition(listTransition.get(nTemp));
            }
        } else {
            int index = mAddItemIndex - 1;
            if (index >= 0 && !listTransition.isEmpty()) {
                mSceneList.get(index).setTransition(listTransition.get(0));
            }
        }
        mAddNewTran = true;
        reload();
        if (isApplyToAll) {
            seekToPosition(0, true);
        } else {
            seekToPosition(mAddItemIndex - 1, true);
        }
    }

    @Override
    public void onBack() {
        if (mFragCurrent == mTransitionFragment && null != mTransitionFragment) {
            for (int n = 0; n < mSceneList.size(); n++) {
                Scene scene = mSceneList.get(n);
                scene.setTransition(mDefaultTransitionList.get(n));
            }
            reload();
            seekToPosition(0, true);
        }
        returnToMenu();
    }

    @Override
    public void onSure() {
        mBackDiaglog = true;
        returnToMenu();
    }
}
