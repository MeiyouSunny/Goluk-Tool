package com.rd.veuisdk.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.Music;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.utils.AudioRecorder;
import com.rd.veuisdk.IVEMediaObjectsProvider;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.SubInfo;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.ui.VerticalSeekBar;
import com.rd.veuisdk.ui.VerticalSeekBar.VerticalSeekBarProgressListener;
import com.rd.veuisdk.utils.AppConfiguration;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 配音界面
 */
public class AudioFragment extends BaseFragment implements
        IVEMediaObjectsProvider {
    @SuppressWarnings("unused")
    private static final String TAG = "AudioFragment";

    /**
     * 最小录制时间(ms)
     */
    private static final int MIN_RECORD_TIME = 100;
    /**
     * 某段录音录制结束后需要偏移的时间(ms)
     */
    private static final int OFFSET_END_POSTION = 25;

    /**
     * 重配
     */
    private TextView mResetTextView;

    /**
     * 试听
     */
    private TextView mAuditionTextView;

    /**
     * 录音
     */
    private ImageView mAudioRecordImageView;

    /**
     * 配音提示
     */
    private TextView mAudioRecordTipTextView;

    /**
     * 媒体捕获控件
     */
    private AudioRecorder mAudioRecorder = null;

    /**
     * 用来引用录制到其中的文件
     */
    private File mAudioFile = null;

    /**
     * 配音时的标示控件
     */
    private TimelineHorizontalScrollView mScrollView;

    /**
     * 配音标示链表容器布局
     */
    private LinearLayout mIMediaLinearLayout;

    /**
     * 配音标示缩略图
     */
    private ThumbNailLine mSubLine;

    private int mDuration = 0;

    /**
     * 视频编辑预览处理器
     */
    private IVideoEditorHandler mhlrVideoEditor;

    /**
     * 是否正在录音
     */
    private boolean mIsRecording = false;

    /**
     * 录音的ｉｄ，非常的重要，不仅配音链表要用到．而且在配音的过程中配音标示也用到，添加删除都靠这个变量
     */
    private static int mRecordId = -1;
    /**
     * 记录播放时间
     */
    private int mStartRecordingPosition = 0;

    /**
     * 预览播放状态的回调
     */
    private EditorPreivewPositionListener mEditorPreivewPositionListener;

    /**
     * 保存配音片段的开始和结束时间
     */
    private AudioInfo mAudioInfo = null;

    /**
     * 当前选中的配音片段的ｉｄ
     */
    private int mEditAudioIdCurrent = -1;

    /**
     * 音频管理器
     */
    private AudioManager mAudioManager;

    /**
     * 静音标识
     */
    private boolean mbMusicStreamMute;

    /**
     * 播放控制状态按钮
     */
    private ImageView mPlayState;

    /**
     * 显示播放预览时间
     */
    private TextView mCurrentPositionTextView;
    private int[] mArrSubLineParam = null;

    /**
     *
     */
    private VerticalSeekBar mVsbAudioFactor;

    /**
     * 存放配音的音频信息
     */
    private ArrayList<AudioInfo> mTempList = new ArrayList<AudioInfo>(),
            mOldList = new ArrayList<AudioInfo>();// 记录当前的配音的信息(重配、删除针对此集合)

    private View mRightView;

    /**
     * 按住配音提示
     */
    private TextView mtvAudioRecord;

    private TextView mtvCenterTime;
    /**
     *
     */
    private LinearLayout mllAudioFactor;

    private boolean mShowFactor = false;

    /**
     * 横屏模式
     */
    private boolean mIsLandscape;

    // //备份数据防止重配->other->配音

    public AudioFragment() {
    }

    @SuppressLint("ValidFragment")
    public AudioFragment(View mRightView) {
        this.mRightView = mRightView;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mhlrVideoEditor = (IVideoEditorHandler) activity;
        mAudioManager = (AudioManager) activity
                .getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.audio);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIsLandscape = true;
        } else {
            mIsLandscape = false;
        }
        mStateSize = getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        initEditorPreivewPositionListener();
    }

    /**
     * 初始化预览播放回调
     */
    private void initEditorPreivewPositionListener() {
        mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

            @Override
            public void onEditorGetPosition(int nPosition, int nDuration) {


                //Log.e(TAG, "onEditorGetPosition: "+nPosition+"....."+nDuration +".........."+mDuration);
                int progress = nPosition;

                int last = TempVideoParams.getInstance().getThemeLast();
                if (progress > nDuration - last) {
                    if (mIsRecording) {
                        stopRecording(nPosition);
                    }
                }

                // 让配音缩略图滚动起来
                onScrollProgress(nPosition);
//				 Log.e("onEditorGetPosition " + mIsRecording, nPosition
//				 + "......" + nDuration + "..........." + mRecordId);
                // 是否在录音
                if (mIsRecording) {
                    if (mSubLine != null) {
                        mSubLine.update(mRecordId, mStartRecordingPosition,
                                nPosition);
                    }
                }
                // 设置当前时间
                if (mIsLandscape) {
                    mtvCenterTime
                            .setText(DateTimeUtils.stringForTime(nPosition));
                } else {
                    mCurrentPositionTextView.setText(DateTimeUtils
                            .stringForMillisecondTime(nPosition, true, true));
                }

            }

            @Override
            public void onEditorPreviewComplete() {
                // Log.e("onEditorPreviewComplte", "....." + mIsRecording
                // + mRecordId);
                if (mIsRecording) {
                    if (mSubLine != null) {
                        mSubLine.update(mRecordId, mStartRecordingPosition,
                                mDuration);
                    }
                    stopRecording(mDuration);
                }
                mPlayState.setImageResource(R.drawable.edit_music_play);
            }

            @Override
            public void onEditorPrepred() {
            }
        };
        mhlrVideoEditor
                .registerEditorPostionListener(mEditorPreivewPositionListener);
        mDuration = mhlrVideoEditor.getDuration();
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_video_edit_audio, null);

        initLayout(mRoot);
        onInitThumbTimeLine();
        return mRoot;
    }

    private void resetItem() {
        if (mTempList.size() == 0) {
            mAuditionTextView.setVisibility(View.INVISIBLE);
            mResetTextView.setVisibility(View.INVISIBLE);
        } else {
            mAuditionTextView.setVisibility(View.VISIBLE);
            mResetTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化布局
     */
    private void initLayout(final View layout) {
        if (mIsLandscape) {
            mResetTextView = (TextView) mRightView
                    .findViewById(R.id.tv_audio_reset);
            mAuditionTextView = (TextView) mRightView
                    .findViewById(R.id.tv_audio_audition);
            mAudioRecordImageView = (ImageView) mRightView
                    .findViewById(R.id.iv_audio_record);
            mtvAudioRecord = (TextView) mRightView
                    .findViewById(R.id.tv_audio_record);
            if (mtvCenterTime != null) {
                mtvCenterTime.setVisibility(View.VISIBLE);
            }

        } else {
            mAudioRecordTipTextView = (TextView) layout
                    .findViewById(R.id.tv_audio_tip);
            mResetTextView = (TextView) layout
                    .findViewById(R.id.tv_audio_reset);
            mAuditionTextView = (TextView) layout
                    .findViewById(R.id.tv_audio_audition);
            mAudioRecordImageView = (ImageView) layout
                    .findViewById(R.id.iv_audio_record);
            mtvAudioRecord = (TextView) layout
                    .findViewById(R.id.tv_audio_record);
            mCurrentPositionTextView = (TextView) layout
                    .findViewById(R.id.tv_audio_current_time);
            mCurrentPositionTextView.setText("00:00.0");

            if (AppConfiguration.isFirstShowAudio()) {
                mAudioRecordImageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mAudioRecordTipTextView.setVisibility(View.VISIBLE);
                        int[] locationImage = new int[2];
                        int[] locationRoot = new int[2];
                        mAudioRecordImageView.getLocationOnScreen(locationImage);
                        mRoot.getLocationOnScreen(locationRoot);
                        DisplayMetrics dm = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                        int halfScreenWidth = dm.widthPixels / 2;
                        int x = halfScreenWidth - mAudioRecordTipTextView.getMeasuredWidth() / 2;
                        int y = (int) (locationImage[1] - locationRoot[1] - mAudioRecordTipTextView.getMeasuredHeight() - 5 * dm.density);
                        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(mAudioRecordTipTextView.getLayoutParams());
                        margin.setMargins(x, y, x + margin.width, y + margin.height);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
                        mAudioRecordTipTextView.setLayoutParams(layoutParams);
                    }
                }, 200);
            } else {
                mAudioRecordTipTextView.setVisibility(View.INVISIBLE);
            }
        }

        mPlayState = (ImageView) layout.findViewById(R.id.ivPlayerState);
        mPlayState.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mhlrVideoEditor != null) {
                    if (mhlrVideoEditor.isPlaying()) {
                        editorPause();
                    } else {
                        editorStart();
                    }
                }
            }
        });


        //重配
        mResetTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (m_alAudioTracks != null) {
                    // 清空配音链表
                    m_alAudioTracks.clear();
                }

                // 重新绘制界面
                if (mSubLine != null) {
                    mSubLine.clearAll();
                }
                mTempList.clear();
                mhlrVideoEditor.reload(true);
                mhlrVideoEditor.seekTo(0);
                mPlayState.setImageResource(R.drawable.edit_music_play);
                onScrollProgress(0);
                resetItem();
            }
        });

        mAuditionTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveAudioData();

                // 播放录音
                mhlrVideoEditor.reload(false);
                editorStart();
                mhlrVideoEditor.seekTo(0);
            }
        });

        mAudioRecordImageView.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("NewApi")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                && AppConfiguration.isFirstShowAudio()) {
                            int hasReadPermission = getActivity()
                                    .checkSelfPermission(
                                            Manifest.permission.RECORD_AUDIO);

                            List<String> permissions = new ArrayList<String>();
                            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                                permissions.add(Manifest.permission.RECORD_AUDIO);
                            }

                            if (!permissions.isEmpty()) {
                                requestPermissions(permissions
                                        .toArray(new String[permissions.size()]), 1);
                            }
                        }
                        int progress = mhlrVideoEditor.getCurrentPosition();

                        if (isInEditingRegion(progress, true)) {
                            removeAudio();
                            return false;
                        }

                        int header = TempVideoParams.getInstance().getThemeHeader();
                        if (progress < header) {
                            onToast(getString(R.string.addaudio_video_head_failed));
                            return false;
                        }
                        int last = TempVideoParams.getInstance().getThemeLast();
                        if (progress > mDuration - last) {
                            onToast(getString(R.string.addaudio_video_end_failed));
                            return false;
                        }
                        int nVideoDuration = mDuration - header - last;
                        if (progress > (header + (nVideoDuration - Math.min(
                                nVideoDuration / 20, 500)))) {
                            onToast(getString(R.string.addaudio_video_between_failed));
                            return false;
                        }

                        // 如果不在选中区域中，说明是想录音
                        if (!isInEditingRegion(progress, true)) {
                            // 开始录音
                            startRecording();
                            return true;
                        } else {
                            return false;
                        }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Log.e("ACTION_UP", mEditAudioIdCurrent + "----"
                        // + mIsRecording);
                        if (mEditAudioIdCurrent == -1 && mIsRecording) {
                            // 停止录音
                            stopRecording(mhlrVideoEditor.getCurrentPosition());
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        resetItem();

        // 录音标示

        mScrollView = (TimelineHorizontalScrollView) layout
                .findViewById(R.id.audio_horizontal_scrollview);
        mScrollView.enableUserScrolling(true);
        mScrollView.addScrollListener(thumbOnScrollListener);

        mIMediaLinearLayout = (LinearLayout) layout
                .findViewById(R.id.audio_subtitleline_media);

        mSubLine = (ThumbNailLine) layout.findViewById(R.id.audio_subline_view);
        mSubLine.setSubtitleThumbNailListener(iSublistener);
        mSubLine.setMoveItem(false);
        mSubLine.setIsAudio(true);

        mScrollView.postDelayed(new Runnable() {

            @Override
            public void run() {
                onInitThumbTimeLine(
                        mhlrVideoEditor.getSnapshotEditor());
            }
        }, 200);

    }

    private void removeAudio() {
        if (mEditAudioIdCurrent == -1) {
            return;
        }
        // 删除选中的配音片段
        mSubLine.removeById(mEditAudioIdCurrent);
        int index = getIndexById(mEditAudioIdCurrent);
        if (index != -1) {
            // 现在可以从临时链表中将配音删除
            mTempList.remove(index);
        }
        resetItem();
        mEditAudioIdCurrent = -1;

        int nLastPosition = mhlrVideoEditor.getCurrentPosition();
        boolean isPlaying = mhlrVideoEditor.isPlaying();
        mhlrVideoEditor.reload(true);
        if (isPlaying) {
            editorStart();
        }
        mhlrVideoEditor.seekTo(nLastPosition);

        // 删除完之后，将图片设置为配音
        setAudioRecordImageViewStatue(true);
    }

    private void editorStart() {
        mhlrVideoEditor.start();
        mPlayState.setImageResource(R.drawable.edit_music_pause);
        // ArrayList<SubInfo> mlist = mSubLine.getData();
        //
        // for (int i = 0; i < mlist.size(); i++) {
        //
        // Log.d("editorStart", " item--" + i + "...."
        // + mlist.get(i).toString());
        // }
    }

    private void editorPause() {
        mhlrVideoEditor.pause();
        mPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * 初始化缩略图时间轴
     */
    private void onInitThumbTimeLine(VirtualVideo enhplayer) {


        mSubLine.setVirtualVideo(enhplayer);

        mSubLine.setStartThumb(mScrollView.getScrollX());


    }

    private int mStateSize = 0;

    /**
     * 初始化缩略图时间轴
     */
    private void onInitThumbTimeLine() {
        mDuration = mhlrVideoEditor.getDuration();
        int half;
        if (mIsLandscape) {
            half = CoreUtils.getMetrics().widthPixels / 2;
            mScrollView.setHalfParentWidth(half - mStateSize
                    - CoreUtils.dpToPixel(65));
        } else {
            half = CoreUtils.getMetrics().widthPixels / 2;
            mScrollView.setHalfParentWidth((half - mStateSize));
        }

        mArrSubLineParam = mSubLine.setDuration(mDuration,
                mScrollView.getHalfParentWidth());

        mScrollView.setLineWidth(mArrSubLineParam[0]);
        mScrollView.setDuration(mDuration);
        mScrollView.setViewTouchListener(new IViewTouchListener() {
            boolean isplaying = false;

            @Override
            public void onActionDown() {
                isplaying = mhlrVideoEditor.isPlaying();
                editorPause();
                mhlrVideoEditor.seekTo(getProgress(mScrollView.getScrollX()));

            }

            @Override
            public void onActionMove() {
                int sx = mScrollView.getScrollX();
                mSubLine.setStartThumb(sx);
                mhlrVideoEditor.seekTo(getProgress(sx));
                onScrollChanged();
            }

            @Override
            public void onActionUp() {
                int sx = mScrollView.getScrollX();
                mSubLine.setStartThumb(sx);
                mhlrVideoEditor.seekTo(getProgress(sx));
                onScrollChanged();
                if (isplaying) {
                    isplaying = false;
                }
                mScrollView.resetForce();
            }

        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                mArrSubLineParam[0] + 2 * mSubLine.getpadding(),
                mArrSubLineParam[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mSubLine.getpadding(),
                0, half - mSubLine.getpadding(), 0);

        mSubLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);
        mIMediaLinearLayout.setLayoutParams(lframe);

        mSubLine.prepare(mScrollView.getHalfParentWidth() + half);

        mSubLine.setStartThumb(mScrollView.getScrollX());

        // 设置可以触摸
        mSubLine.setCantouch(true);

        reStartAudio();

        ArrayList<SubInfo> sublist = new ArrayList<SubInfo>();
        int len = mTempList.size();
        for (int i = 0; i < len; i++) {
            AudioInfo info = mTempList.get(i);
            sublist.add(new SubInfo(info.getStartRecordTime(), info.getEndRecordTime(), info.getAudioInfoId()));
        }
        mSubLine.prepareData(sublist);
        mSubLine.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mTempList.size() > 0) {
                    mhlrVideoEditor.seekTo(30);
                    onScrollProgress(30);
                } else {
                    mhlrVideoEditor.seekTo(0);
                    onScrollProgress(0);
                }
            }
        }, 150);

    }

    /**
     * 设置录音按状态 flag == true为录音状态，flag==false为删除状态
     */
    private void setAudioRecordImageViewStatue(boolean flag) {
        if (flag) {
            // 设置录音按钮为录音状态
            if (mIsLandscape) {

            } else {
                mAudioRecordTipTextView.setText(R.string.audio_tip);
            }
            mAudioRecordImageView
                    .setImageResource(R.drawable.audio_record_record);
            mllAudioFactor.setVisibility(View.INVISIBLE);
            mtvAudioRecord.setText(R.string.audio_press);
        } else {
            // 设置录音按钮为删除状态
            if (mIsLandscape) {

            } else {
                mAudioRecordTipTextView.setText(R.string.audio_remove_tip);
            }
            mtvAudioRecord.setText(R.string.del);
            mAudioRecordImageView
                    .setImageResource(R.drawable.audio_record_delete);
            if (mShowFactor) {
                mllAudioFactor.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 判断当前位置是否是在录音时间之内，　用于设置录音按钮的状态
     *
     * @param currentPosition   当前播放位置
     * @param bCheckCurrentEdit 是否检查当前编辑项
     * @return
     */
    private boolean isInEditingRegion(final int currentPosition,
                                      boolean bCheckCurrentEdit) {
        if (mTempList.size() > 0) {

            if (mEditAudioIdCurrent >= 0 && bCheckCurrentEdit
                    && getIndexById(mEditAudioIdCurrent) != -1) {
                return true;
            }
            mEditAudioIdCurrent = -1;
            mSubLine.clearCurrent();
            for (int nTmp = mTempList.size() - 1; nTmp >= 0; nTmp--) {
                AudioInfo item = mTempList.get(nTmp);
                if (item != null
                        && currentPosition >= item.getStartRecordTime()
                        && currentPosition <= item.getEndRecordTime()) {

                    if (iSublistener != null) {
                        iSublistener.onCheckItem(true, item.getAudioInfoId());
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private IThumbLineListener iSublistener = new IThumbLineListener() {

        @Override
        public void onTouchUp() {
        }

        ;

        @Override
        public void updateThumb(int id, int start, int end) {
            // Log.d(TAG, "ISubtitleThumbNailListener.updateThumb");
        }

        @Override
        public void onTouch(int id, int start, int end) {
            // Log.d(TAG, "ISubtitleThumbNailListener.onTouch");
        }

        @Override
        public void onCheckItem(boolean changed, int id) {
            if (mIsRecording) {
                return;
            }
            mEditAudioIdCurrent = id;
            try {
                mAudioInfo = mTempList.get(getIndexById(mEditAudioIdCurrent));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mAudioInfo != null) {
                // 将当前选中的配音片段的配音比例同步一下
                mVsbAudioFactor.progress(mAudioInfo.getSeekBarValue());
                if (mSubLine != null) {
                    // if (isCheckItem)
                    {
                        mSubLine.editSub(mEditAudioIdCurrent);
                    }
                }
                // 设置为删除状态
                setAudioRecordImageViewStatue(false);
            } else {
                mEditAudioIdCurrent = -1;
            }

        }
    };

    // private boolean isCheckItem = true;

    protected void onScrollChanged() {
        if (mhlrVideoEditor != null) {
            // 设置当前时间
            if (mIsLandscape) {
                mtvCenterTime.setText(DateTimeUtils
                        .stringForTime(mhlrVideoEditor.getCurrentPosition()));
            } else {
                mCurrentPositionTextView.setText(DateTimeUtils
                        .stringForMillisecondTime(
                                mhlrVideoEditor.getCurrentPosition(), true,
                                true));
            }

            if (!mIsRecording) {
                // 如果当前位置在录音区间
                if (isInEditingRegion(mhlrVideoEditor.getCurrentPosition(),
                        false)) {
                    // 设置为删除状态
                    setAudioRecordImageViewStatue(false);
                } else {
                    // 设置为录音状态
                    setAudioRecordImageViewStatue(true);
                }
            }
        }
    }

    private int getProgress(int scrollX) {
        return (int) ((long) scrollX * mDuration / mArrSubLineParam[0]);
    }

    private ScrollViewListener thumbOnScrollListener = new ScrollViewListener() {
        int sx = 0;

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {

            // Log.e("onScrollProgress", scrollX + "...." + appScroll);

            sx = mScrollView.getScrollX();
            if (!appScroll && mhlrVideoEditor != null && !mIsRecording) {
                mhlrVideoEditor.seekTo(getProgress(scrollX));
            }
            if (null != mSubLine) {
                mSubLine.setStartThumb(sx);
            }
            onScrollChanged();

        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY,
                                boolean appScroll) {
            // Log.e("onScrollEnd", scrollX + "...." + appScroll);
            sx = mScrollView.getScrollX();
            if (!appScroll && mhlrVideoEditor != null && !mIsRecording) {
                mhlrVideoEditor.seekTo(getProgress(scrollX));
            }
            if (null != mSubLine) {
                mSubLine.setStartThumb(sx);
            }
            onScrollChanged();
        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY,
                                  boolean appScroll) {
            // Log.e("onScrollBegin", scrollX + "...." + appScroll);
            if (null != mSubLine && !mIsRecording) {
                mSubLine.setStartThumb(mScrollView.getScrollX());
            }
            onScrollChanged();
        }
    };

    /**
     * 播放中的进度
     *
     * @param progress
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getItemScrollX(progress));
    }

    /**
     * 设置播放进度
     *
     * @param mScrollX
     */
    private void onScrollTo(int mScrollX) {
        mScrollView.appScrollTo(mScrollX, true);
    }

    private int getItemScrollX(long progress) {
        return (int) (progress * (mArrSubLineParam[0]) / mDuration);
    }

    private boolean isrunning = false;

    @Override
    public void onResume() {
        super.onResume();
        isrunning = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mShowFactor = false;
        if (null != mllAudioFactor) {
            mllAudioFactor.setVisibility(View.GONE);
        }
        isrunning = false;
    }

    @Override
    public void onPause() {
        super.onPause();

        boolean isPlaying = mhlrVideoEditor.isPlaying();
        mhlrVideoEditor.reload(false);
        if (isPlaying) {
            mhlrVideoEditor.start();
        }
    }

    @Override
    public void onDestroy() {
        if (mhlrVideoEditor != null && mEditorPreivewPositionListener != null) {
            mhlrVideoEditor
                    .unregisterEditorProgressListener(mEditorPreivewPositionListener);
            mEditorPreivewPositionListener = null;
            mhlrVideoEditor = null;
        }
        if (null != mSubLine) {
            mSubLine.recycle(false);
            mSubLine = null;
        }
        super.onDestroy();
    }

    @Override
    public List<Music> getMusicObjects() {
        if (isrunning) {
            addAudioData(mTempList);
        } else {
            addAudioData(TempVideoParams.getInstance().getAudios());
        }
        return m_alAudioTracks;
    }

    @Override
    public List<MediaObject> getMediaObjects() {
        return null;
    }

    /**
     * 保存配音
     */
    public void saveAudioData() {
        ArrayList<AudioInfo> temp = new ArrayList<AudioInfo>();
        temp.addAll(mTempList);

        // for (int i = 0; i < temp.size(); i++) {
        // AudioInfo info = temp.get(i);
        // Log.e("." + i,
        // info.getAudioInfoId() + "...getSeekBarValue."
        // + info.getSeekBarValue() + ".getMixFactor..."
        // + info.getAudio().getMixFactor());
        // }

        TempVideoParams.getInstance().setAudioList(temp);
    }

    /**
     * 还原配音链表
     */
    private void reStartAudio() {
        mOldList.clear();
        mTempList.clear();
        ArrayList<AudioInfo> temp = TempVideoParams.getInstance().getAudios();

        int len = temp.size();
        for (int i = 0; i < len; i++) {
            AudioInfo info = temp.get(i);
            mTempList.add(new AudioInfo(info));
            mOldList.add(new AudioInfo(info));
        }

    }

    private ArrayList<Music> m_alAudioTracks = new ArrayList<Music>();

    private void addAudioData(ArrayList<AudioInfo> temp) {
        /** 配音链表 */

        if (m_alAudioTracks != null) {
            m_alAudioTracks.clear();
            int len = temp.size();
            for (int i = 0; i < len; i++) {
                AudioInfo audio = temp.get(i);
                if (audio != null) {
                    m_alAudioTracks.add(audio.getAudio());
                }
            }
        }
    }

    private void onToast(String msg) {
        Utils.autoToastNomal(getActivity(), msg);
    }

    private void initMediaRecorder() {
        // 设置录制的音频文件的存放位置
        mAudioFile = new File(PathUtils.getTempFileNameForSdcard("recording",
                "mp3"));
        // 初始化Mp3音频录制
        mAudioRecorder = new AudioRecorder(mAudioFile);
    }

    /**
     * 开始录音
     */
    private void startRecording() {

        // Log.e("startRecording.",
        // "当前精度:" + mhlrVideoEditor.getCurrentPosition() + "......");
        mScrollView.setCanTouch(false);
        int sx = mScrollView.getScrollX();
        mScrollView.scrollTo(sx, 0);
        mhlrVideoEditor.seekTo(getProgress(sx));
        // // 录音的时候静音
        setMusicStreamMute(true);

        // 隐藏提示信息
        // mAudioRecordTipTextView.setVisibility(View.INVISIBLE);

        // 一旦开始录音，就将当前选中的配音片段的标记失效
        mEditAudioIdCurrent = -1;
        try {
            // 初始化MediaRecorder
            initMediaRecorder();
            if (mAudioRecorder != null) {
                mAudioRecorder.start();
                mIsRecording = true;
                System.currentTimeMillis();
            }
        } catch (Exception ex) {
            SysAlertDialog.showAutoHideDialog(getActivity(), null,
                    getString(R.string.error_record_audio_retry),
                    Toast.LENGTH_SHORT);
            return;
        }
        mStartRecordingPosition = mhlrVideoEditor.getCurrentPosition();
        if (!mhlrVideoEditor.isPlaying()) {
            editorStart();
        }
        if (Math.abs(mStartRecordingPosition - mDuration) < 100) {
            mStartRecordingPosition = 0;
        }
        ++mRecordId;

        // // 开始录音的时候，创建一个新的对象
        mAudioInfo = new AudioInfo(mRecordId, mAudioFile.getAbsolutePath());
        mAudioInfo.setStartRecordTime(mStartRecordingPosition);
        // 画一个小区域
        mSubLine.addRect(mStartRecordingPosition, mStartRecordingPosition + 10,
                "", mRecordId);
    }

    private int getIndexById(int id) {
        int len = mTempList.size();
        int index = -1;
        for (int i = 0; i < len; i++) {
            if (mTempList.get(i).getAudioInfoId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 停止录音
     *
     * @param end 配音结束时刻，防止完全播放再获取当前播放器进度变为0
     */
    private void stopRecording(int end) {
        // 显示提示信息
        // mAudioRecordTipTextView.setVisibility(View.VISIBLE);

        AppConfiguration.setIsFirstAudio();
        if (mIsLandscape) {

        } else {
            mAudioRecordTipTextView.setVisibility(View.INVISIBLE);
        }
        editorPause();
        doEndRecording(end);
    }

    /**
     * @param end 配音的结束点
     */
    private void doEndRecording(int end) {

        mScrollView.setCanTouch(true);
        if (mAudioRecorder != null) {
            try {
                mAudioRecorder.stop();
            } catch (Exception e) {
            }
            if (mAudioFile != null) {
                // 如果配音文件有错或者时间为０，则提示一下
                File temp = new File(mAudioFile.getAbsolutePath());
                if (mAudioInfo == null || temp.length() <= MIN_RECORD_TIME) {
                    SysAlertDialog.showAutoHideDialog(getActivity(), null,
                            getString(R.string.record_audio_too_short_retry),
                            Toast.LENGTH_SHORT);
                    onRecordingFailed();
                    if (temp != null) {
                        temp.delete();
                    }
                } else {
                    // 停止录音之后，将录音文件添加到视频编辑预览处理器
                    onRecordingFinish(mAudioInfo, end);
                }
            }
            mAudioRecorder = null;
            mIsRecording = false;
        }
        setMusicStreamMute(false);
    }

    /**
     * 由于录音时间太短而导致录音失败时，做相关的后续处理
     */
    private void onRecordingFailed() {
        // Log.e("remove..", mRecordId + "");
        setMusicStreamMute(false);
        mSubLine.removeById(mRecordId);
    }

    /**
     * 配音完成
     *
     * @param info
     * @param end  配音结束时刻
     */
    private void onRecordingFinish(AudioInfo info, int end) {

        if (info != null) {
            int endRecordingPosition = end;
            info.setEndRecordTime(endRecordingPosition);

            mSubLine.update(mRecordId, Math.round(info.getStartRecordTime()),
                    Math.round(info.getEndRecordTime()));
            // Log.e("onRecordingFinish",
            // mRecordId + "start-" + info.getStartRecordTime() + "--end-"
            // + info.getEndRecordTime());
            // 同步滚动组件到实际配音结束位置
            info.setSeekBarValue(mVsbAudioFactor.getProgress());
            mTempList.add(info);
            endRecordingPosition += OFFSET_END_POSTION;
            endRecordingPosition = Math.min(mDuration, endRecordingPosition);
            onScrollProgress(endRecordingPosition);
            mhlrVideoEditor.seekTo(endRecordingPosition);
            resetItem();
        }
    }

    /**
     * 设定音乐流是否禁音
     *
     * @param bMute
     */
    @SuppressWarnings("deprecation")
    public synchronized void setMusicStreamMute(boolean bMute) {
        if (mbMusicStreamMute != bMute) {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, bMute);
            mbMusicStreamMute = bMute;
        }
    }

    /**
     * 放弃保存
     */
    public void onAudioFragmentClear() {
        TempVideoParams.getInstance().setAudioList(mOldList);
        // 重新绘制界面
        if (mSubLine != null) {
            mSubLine.clearAll();
        }
        mTempList.clear();
        mhlrVideoEditor.reload(true);
        // mhlrVideoEditor.seekTo(0);
        mEditAudioIdCurrent = -1;

    }

    public void setCenterTime(TextView tv) {
        mtvCenterTime = tv;
    }

    public void setShowFactor(boolean showFactor) {
        this.mShowFactor = showFactor;
    }

    public void setSeekBar(LinearLayout llAudioFactor) {
        this.mllAudioFactor = llAudioFactor;
        mVsbAudioFactor = (VerticalSeekBar) llAudioFactor
                .findViewById(R.id.vsbAudioFactor);
        mVsbAudioFactor.progress(50);
        mVsbAudioFactor
                .setOnProgressListener(new VerticalSeekBarProgressListener() {
                    AudioInfo info;

                    @Override
                    public void onProgress(int progress) {
                        // 如果选中了某一个配音片段
                        if (mEditAudioIdCurrent != -1) {
                            // 如果当前有选中的配音片段，那设置的是该配音片段
                            if (info != null) {
                                info.setSeekBarValue(progress);
                            }
                        }
                    }

                    @Override
                    public void onStartTouch() {
                        int index = getIndexById(mEditAudioIdCurrent);
                        if (-1 != index) {
                            info = mTempList.get(index);
                        }

                    }

                    @Override
                    public void onStopTouch() {
                        info = null;

                    }
                });

    }

    public void resetAlreadyRecordAudioObject() {
        addAudioData(mOldList);
        // isCheckItem = false;
        // 在点击了重配之后，配音又没有保存，那就要把上一次保存的配音再重置回来

        if (mhlrVideoEditor != null) {
            mhlrVideoEditor.reload(true);
            editorStart();
            mhlrVideoEditor.seekTo(0);
        }
    }

    /**
     * 判断是否改变配音段
     *
     * @return
     */
    public boolean hasChanged() {// haschanged
        int len = mTempList.size();
        if (len != mOldList.size()) {
            return true;
        }
        boolean changed = false;
        for (int i = 0; i < len; i++) {
            if (!mTempList.get(i).equals(mOldList.get(i))) {
                changed = true;
                break;
            }

        }

        return changed;
    }
}