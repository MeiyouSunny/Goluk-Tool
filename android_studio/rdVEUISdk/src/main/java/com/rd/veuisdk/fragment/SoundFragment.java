package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rd.lib.utils.CoreUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.IVideoEditorHandler.EditorPreivewPositionListener;
import com.rd.veuisdk.MoreMusicActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.TempVideoParams;
import com.rd.veuisdk.VideoEditActivity;
import com.rd.veuisdk.adapter.SoundAdapter;
import com.rd.veuisdk.database.HistoryMusicCloud;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.SoundInfo;
import com.rd.veuisdk.ui.IThumbLineListener;
import com.rd.veuisdk.ui.ThumbNailLine;
import com.rd.veuisdk.ui.VerticalSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;
import com.rd.veuisdk.videoeditor.widgets.IViewTouchListener;
import com.rd.veuisdk.videoeditor.widgets.ScrollViewListener;
import com.rd.veuisdk.videoeditor.widgets.TimelineHorizontalScrollView;

import java.util.ArrayList;

/**
 * 音效
 */
public class SoundFragment extends RBaseFragment implements View.OnClickListener {

    public static SoundFragment newInstance() {
        SoundFragment subtitleFragment = new SoundFragment();
        Bundle bundle = new Bundle();
        subtitleFragment.setArguments(bundle);
        return subtitleFragment;
    }

    public SoundFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "SoundFragment";
        mPageName = getString(R.string.sound);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mEditorHandler = (IVideoEditorHandler) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_sound_layout, container, false);
        initView();
        init();
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //添加点击监听
        //添加 取消 删除 播放 滑动
        mTvAddSubtitle.setOnClickListener(this);
        mBtnCancelEdit.setOnClickListener(this);
        mDelete.setOnClickListener(this);
        mPlayState.setOnClickListener(this);
        mScrollView.addScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(new IViewTouchListener() {

            @Override
            public void onActionDown() {
                mEditorHandler.pause();
                int nprogress = mScrollView.getProgress();
                mEditorHandler.seekTo(nprogress);
                setProgressText(nprogress);
            }

            @Override
            public void onActionMove() {
                int nprogress = mScrollView.getProgress();
                mEditorHandler.seekTo(nprogress);
                setProgressText(nprogress);
            }

            @Override
            public void onActionUp() {
                mScrollView.resetForce();
                int nprogress = mScrollView.getProgress();
                setProgressText(nprogress);
            }
        });
    }

    private Context mContext;
    /**
     * 编辑界面控制类对象
     */
    private IVideoEditorHandler mEditorHandler;


    private View mAddLayout;
    private ImageView mPlayState;
    private TimelineHorizontalScrollView mScrollView;
    private LinearLayout mIMediaLinearLayout;
    //缩略图组件
    private ThumbNailLine mSubtitleLine;
    //虚拟视频播放器
    private VirtualVideoView mPlayer;
    private ArrayList<SoundInfo> mSoundInfos = new ArrayList<>();
    private ArrayList<SoundInfo> mOldSoundInfos = new ArrayList<>();
    private SoundInfo mSoundInfo;
    //底部
    private ViewGroup mReycleParent;
    private TextView tvAdded;
    private RecyclerView mRecyclerView;
    private SoundAdapter mAdapter;
    //添加
    private View mDelete;  //删除
    private Button mTvAddSubtitle;//新增、完成 公用一个
    private Button mBtnCancelEdit; //取消、编辑共用一个
    private TextView mTvProgress;//显示进展
    private boolean isRestoreData = false;//是否恢复数据
    private int mStateSize = 0;
    //是否编辑数据
    private boolean mIsUpdate = false;
    //添加音效id
    private int id = 0;
    //是否增强模式取缩略图 （耗时）
    private boolean bExMode = false;
    /**
     * 当前状态 0   1 新增  2编辑
     */
    private int mStatus = 0;
    /**
     * 视频总的时间长度
     */
    private int duration = 0;
    //显示功能名字
    private TextView tvTitle;


    private void initView() {
        //设置标题
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.sound);
        //底部
        mReycleParent = (ViewGroup) findViewById(R.id.recycleParent);
        tvAdded = (TextView) findViewById(R.id.tvAdded);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new SoundAdapter();
        mAdapter.setOnItemClickListener(new OnItemClickListener<SoundInfo>() {
            @Override
            public void onItemClick(int position, SoundInfo item) {
                if (mEditorHandler.isPlaying()) {
                    mEditorHandler.pause();
                }
                Log.e(TAG, "onItemClick: " + item);
                //跳到指定位置
                mEditorHandler.seekTo(item.getStart());
                onScrollProgress(item.getStart());

                //当前编辑项高亮
                mSubtitleLine.showCurrent(item.getId());
                mVsbAudioFactor.setProgress(item.getMixFactor());

                mEditAudioIdCurrent = item.getId();

                //选中当前片段(编辑)
                onEditSound(item);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        //添加/完成     取消 /编辑      删除     进展
        mTvAddSubtitle = (Button) mRoot.findViewById(R.id.btn_add_item);
        mBtnCancelEdit = (Button) mRoot.findViewById(R.id.btn_edit_item);
        mDelete = (Button) mRoot.findViewById(R.id.btn_del_item);
        mTvProgress = (TextView) mRoot.findViewById(R.id.tvAddProgress);
        //整个界面
        mAddLayout = mRoot.findViewById(R.id.add_layout);
        //播放
        mPlayState = (ImageView) mRoot.findViewById(R.id.ivPlayerState);
        mStateSize = mContext.getResources().getDimensionPixelSize(R.dimen.add_sub_play_state_size);
        //滑动
        mScrollView = (TimelineHorizontalScrollView) mRoot
                .findViewById(R.id.priview_subtitle_line);
        mScrollView.enableUserScrolling(true);
        mIMediaLinearLayout = (LinearLayout) mRoot
                .findViewById(R.id.subtitleline_media);
        //缩略图
        mSubtitleLine = (ThumbNailLine) mRoot.findViewById(R.id.subline_view);
        mSubtitleLine.setEnableRepeat(true);
        mSubtitleLine.setSubtitleThumbNailListener(new IThumbLineListener() {

            private int tempId, tempStart, tempEnd;

            @Override
            public void onTouchUp() {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                }
                float start = Utils.ms2s(tempStart);
                float end = Utils.ms2s(tempEnd);


                tempId = 0;

            }

            ;

            @Override
            public void updateThumb(int id, int start, int end) {
                mIsUpdate = true;
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                }
                tempId = id;
                tempStart = start;
                tempEnd = end;
            }


            @Override
            public void onCheckItem(boolean changed, int id) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                }

            }
        });

        mDisplay = CoreUtils.getMetrics();
    }

    private void init() {
        mPlayer = mEditorHandler.getEditor();
        duration = Utils.s2ms(mPlayer.getDuration());
        mDuration = mEditorHandler.getDuration();
        mAddLayout.setVisibility(View.VISIBLE);

        onInitThumbTimeLine();
        setImage(R.drawable.edit_music_play);
        mTvAddSubtitle.setText(R.string.add);
        if (null != mEditorHandler) {
            mEditorHandler.registerEditorPostionListener(mEditorPreivewPositionListener);
            mEditorHandler.reload(false);
            mEditorHandler.seekTo(0);
        }
        mSubtitleLine.setCantouch(true);
        mSubtitleLine.setMoveItem(true);
        onScrollProgress(0);
        checkTitleLayout();
        mAdapter.addAll(mSoundInfos, tvAdded, -1);
    }

    private ScrollViewListener mThumbOnScrollListener = new ScrollViewListener() {

        @Override
        public void onScrollProgress(View view, int scrollX, int scrollY,
                                     boolean appScroll) {
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            int nprogress = mScrollView.getProgress();
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(nprogress);
                }
                setProgressText(nprogress);
            }
        }

        @Override
        public void onScrollEnd(View view, int scrollX, int scrollY,
                                boolean appScroll) {
            int nprogress = mScrollView.getProgress();
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.seekTo(nprogress);
                }
            }
            setProgressText(nprogress);
        }

        @Override
        public void onScrollBegin(View view, int scrollX, int scrollY,
                                  boolean appScroll) {
            int nprogress = mScrollView.getProgress();
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            if (!appScroll) {
                if (mEditorHandler != null) {
                    mEditorHandler.pause();
                    mEditorHandler.seekTo(nprogress);
                }
                setProgressText(nprogress);
            }
        }
    };

    private EditorPreivewPositionListener mEditorPreivewPositionListener = new EditorPreivewPositionListener() {

        @Override
        public void onEditorPreviewComplete() {
            //预览结束
            onScrollCompleted();
            if (mTvAddSubtitle.getText().toString().equals(mContext.getString(R.string.complete))) {
                //保存当前
                onSaveSound();
                //滑动UI到开始时刻（与播放器预览效果一致）
                mEditorHandler.seekTo(0);
                onScrollProgress(0);
            }
        }

        @Override
        public void onEditorPrepred() {
            if (!isRestoreData) {
                onInitThumbTimeLine(mEditorHandler.getSnapshotEditor());
            }
        }

        @Override
        public void onEditorGetPosition(int nPosition, int nDuration) {
            onScrollProgress(nPosition);
            if (null != mSoundInfo && mSoundInfo.getStart() < nPosition) {
                mSubtitleLine.update(mSoundInfo.getId(), mSoundInfo.getStart(), nPosition);
            }
        }
    };

    private DisplayMetrics mDisplay;
    private int mDuration = 1000;
    private int[] mSizeParams;
    private int mHalfWidth = 0;

    /**
     * 设置缩略图时间轴
     */
    private void onInitThumbTimeLine() {

        mHalfWidth = mDisplay.widthPixels / 2;
        mScrollView.setHalfParentWidth(mHalfWidth - mStateSize);
        mSizeParams = mSubtitleLine.setDuration(mDuration, mScrollView.getHalfParentWidth());
        mScrollView.setLineWidth(mSizeParams[0]);
        mScrollView.setDuration(mDuration);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mSizeParams[0]
                + 2 * mSubtitleLine.getpadding(), mSizeParams[1]);

        lp.setMargins(mScrollView.getHalfParentWidth() - mSubtitleLine.getpadding(),
                0, mHalfWidth - mSubtitleLine.getpadding(), 0);

        mSubtitleLine.setLayoutParams(lp);

        FrameLayout.LayoutParams lframe = new FrameLayout.LayoutParams(lp.width
                + lp.leftMargin + lp.rightMargin, lp.height);
        lframe.setMargins(0, 0, 0, 0);

        mIMediaLinearLayout.setLayoutParams(lframe);
    }

    /**
     * 初始化缩略图时间轴
     */
    private void onInitThumbTimeLine(VirtualVideo virtualVideo) {
        mSubtitleLine.setVirtualVideo(virtualVideo, false);
        mSubtitleLine.prepare(mScrollView.getHalfParentWidth() + mHalfWidth);
        onScrollProgress(0);
        mHandler.postDelayed(resetDataRunnable, 100);
    }

    /**
     * 恢复数据
     */
    private Runnable resetDataRunnable = new Runnable() {

        @Override
        public void run() {
            ArrayList<SoundInfo> sublist = TempVideoParams
                    .getInstance().getSoundInfoList();
            mSoundInfos.clear();
            mOldSoundInfos.clear();
            for (SoundInfo s : sublist) {
                mSoundInfos.add(s);
                mOldSoundInfos.add(s);
                if (s.getId() >= id) {
                    id = s.getId() + 1;
                }
                mSubtitleLine.addRect(s.getStart(), s.getEnd(), "", s.getId());
                mSubtitleLine.setShowCurrentFalse();
            }
            Message message = Message.obtain();
            message.what = UPDATA;
            mHandler.sendMessage(message);
            mSubtitleLine.setStartThumb(mScrollView.getScrollX());
            isRestoreData = true;
        }
    };

    /**
     * 设置时间
     *
     * @param progress
     */
    private void setProgressText(int progress) {
        mTvProgress.setText(DateTimeUtils.stringForMillisecondTime(progress));
        mSubtitleLine.setDuration(progress);
        mAdapter.setDuration(progress);
    }

    /**
     * 暂停
     */
    private void pauseVideo() {
        mEditorHandler.pause();
        setImage(R.drawable.edit_music_play);
    }

    /**
     * 播放控制器图标
     *
     * @param resId
     */
    public void setImage(@DrawableRes int resId) {
        if (isRunning && null != mPlayState) {
            mPlayState.setImageResource(resId);
        }
    }

    /**
     * 提示是否放弃保存
     */
    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(mContext,
                mContext.getString(R.string.dialog_tips),
                mContext.getString(R.string.cancel_all_changed),
                mContext.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, mContext.getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackToActivity(false);
                        dialog.dismiss();
                    }
                }, false, null).show();
    }

    /**
     * 退出音效
     */
    private void onBackToActivity(boolean save) {
        mIsUpdate = false;
        if (!save) {
            TempVideoParams.getInstance().setSoundInfoList(mOldSoundInfos);
        }
        mSoundInfos.clear();
        mOldSoundInfos.clear();
        mSoundInfo = null;
        mAddLayout.setVisibility(View.GONE);
        if (null != mSubtitleLine) {
            mSubtitleLine.recycle();
        }
        onScrollTo(0);
        setProgressText(0);
        mEditorHandler.reload(true);
        mEditorHandler.unregisterEditorProgressListener(mEditorPreivewPositionListener);
        mSubtitleLine.clearAll();
        mEditorHandler.onBack();
        isRestoreData = false;
    }

    /**
     * 播放中的进度
     *
     * @param progress (单位ms)
     */
    private void onScrollProgress(int progress) {
        onScrollTo(getScrollX(progress));
        setProgressText(progress);
    }

    /**
     * 跳到进度
     *
     * @param progress
     * @return
     */
    private int getScrollX(long progress) {
        return (int) (progress * (mSubtitleLine.getThumbWidth() / mDuration));
    }

    /**
     * 播放完成
     */
    private void onScrollCompleted() {
        onScrollTo((int) mSubtitleLine.getThumbWidth());
        setProgressText(mDuration);
        mPlayState.setImageResource(R.drawable.edit_music_play);
    }

    /**
     * 设置播放进度
     *
     * @param mScrollX
     */
    private void onScrollTo(int mScrollX) {
        mScrollView.appScrollTo(mScrollX, true);
    }

    /**
     * 判断 是否改变数据
     *
     * @return
     */
    private boolean isChange() {
        if (mSoundInfos.size() != mOldSoundInfos.size()) {
            return false;
        }
        int len = mSoundInfos.size();
        for (int i = 0; i < len; i++) {
            if (!mSoundInfos.get(i).equals(mOldSoundInfos.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add_item) {
            onAdd();
        } else if (id == R.id.btn_edit_item) {
            onCancel();
        } else if (id == R.id.btn_del_item) {
            onDelete();
        } else if (id == R.id.ivPlayerState) {
            onStateChange();
        }
    }

    @Override
    public void onLeftClick() {
        //判断数据有没有变化
        if (mIsUpdate || !isChange()) {
            onShowAlert();
        } else {
            onBackToActivity(false);
        }
    }

    @Override
    public void onRightClick() {
        if (mStatus == 2 || mStatus == 1) {
            if (mEditorHandler != null && mEditorHandler.isPlaying()) {
                pauseVideo();
            }
            //保存
            onSaveSound();

        } else {
            onBackToActivity(true);
        }
    }

    /**
     * 底部显示
     */
    private void checkTitleLayout() {
        if (mSoundInfos.size() > 0) {
            tvTitle.setVisibility(View.GONE);
            mReycleParent.setVisibility(View.VISIBLE);
        } else {
            mReycleParent.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 恢复添加按钮的UI
     */
    private void resetMenuUI() {
        mTvAddSubtitle.setText(R.string.add);
        mBtnCancelEdit.setVisibility(View.GONE);
        mDelete.setVisibility(View.GONE);
        if (mShowFactor) {
            mllAudioFactor.setVisibility(View.GONE);
        }
    }

    /**
     * 编辑UI
     */
    private void editUI() {
        mTvAddSubtitle.setText(R.string.complete);
        mDelete.setVisibility(View.VISIBLE);
        mBtnCancelEdit.setVisibility(View.GONE);
        if (mShowFactor) {
            mllAudioFactor.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 添加音效
     */
    private void onAdd() {
        if (mEditorHandler != null && mEditorHandler.isPlaying()) {
            pauseVideo();
        }

        String menu = mTvAddSubtitle.getText().toString();
        /**
         * 新增
         */
        if (menu.equals(mContext.getString(R.string.add))) {
            mStatus = 1;
            HistoryMusicCloud.getInstance().initilize(mContext);
            MoreMusicActivity.onSound(getContext(), mSoundTypeUrl, mSoundUrl, VideoEditActivity.REQUSET_SOUND);
        } else if (menu.equals(mContext.getString(R.string.complete))) {
            onSaveSound();
        }
    }

    /**
     * 取消音效
     */
    private void onCancel() {
        pauseVideo();
        //取消

        //恢复按钮状态
        resetMenuUI();
    }

    /**
     * 删除音效
     */
    private void onDelete() {
        pauseVideo();
        if (mSoundInfo != null) {
            for (int i = 0; i < mSoundInfos.size(); i++) {
                if (mSoundInfos.get(i).getId() == mSoundInfo.getId()) {
                    mSoundInfos.remove(i);
                    mSubtitleLine.removeById(mSoundInfo.getId());
                    break;
                }
            }
            mSoundInfo = null;
        }
        mEditAudioIdCurrent = -1;
        mStatus = 0;
        TempVideoParams.getInstance().setSoundInfoList(mSoundInfos);
        //恢复UI
        resetMenuUI();
        checkTitleLayout();
        mAdapter.addAll(mSoundInfos, tvAdded, -1);
    }

    /**
     * 编辑存在的音效
     *
     * @param soundInfo
     */
    private void onEditSound(SoundInfo soundInfo) {
        if (soundInfo != null) {
            mStatus = 2;
            if (mEditorHandler != null) {
                mEditorHandler.pause();
            }
            //编辑是构造新对象
            mSoundInfo = new SoundInfo(soundInfo);
            checkTitleLayout();
            editUI();
        }
    }

    /**
     * 调整播放那个状态
     */
    private void onStateChange() {
        if (mEditorHandler.isPlaying()) {
            pauseVideo();
        } else {
            if (Math.abs(mEditorHandler.getCurrentPosition() - mEditorHandler.getDuration()) < 300) {
                mEditorHandler.seekTo(0);
            }
            if (mStatus != 0) {
                onSaveSound();
            }
            onMusicChecked(mScrollView.getProgress(), true, true);
        }
    }

    /**
     * 保存音效
     */
    private void onSaveSound() {
        if (mStatus == 1 || mStatus == 2) {
            //新增中 或者 编辑中
            save();
        }
        mSubtitleLine.setShowCurrentFalse();
        mSoundInfo = null;
        //更新UI
        resetMenuUI();
        checkTitleLayout();
        mAdapter.addAll(mSoundInfos, tvAdded, -1);
        mStatus = 0;
        mEditAudioIdCurrent = -1;

        //从新加载播放器中的声音
        mEditorHandler.reload(true);
    }

    /**
     * 保存
     */
    private void save() {
        if (mSoundInfo == null) {
            return;
        }
        mSoundInfo.setMixFactor(mMixFactor);
        int[] mTime = mSubtitleLine.getCurrent(mSoundInfo.getId());
        if (mTime != null) {
            int t1 = mTime[1] - mTime[0];
            int d = Utils.s2ms(mSoundInfo.getmMusic().getIntrinsicDuration());
            int t2 = d - mSoundInfo.getTrmeStart();
            if (t1 > t2) {
                mSoundInfo.setTrmeEnd(d);
            } else {
                mSoundInfo.setTrmeEnd(mSoundInfo.getTrmeStart() + t1);
            }
            mSoundInfo.setStart(mTime[0]);
            mSoundInfo.setEnd(mTime[1]);
            int i = 0;
            for (; i < mSoundInfos.size(); i++) {
                if (mSoundInfos.get(i).getId() == mSoundInfo.getId()) {
                    mSoundInfos.set(i, mSoundInfo);
                    break;
                }
            }
        }
        TempVideoParams.getInstance().setSoundInfoList(mSoundInfos);
    }

    @Override
    public void onDestroyView() {
        mScrollView.removeScrollListener(mThumbOnScrollListener);
        mScrollView.setViewTouchListener(null);
        onScrollProgress(0);
        super.onDestroyView();
        isRestoreData = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubtitleLine != null) {
            mSubtitleLine.recycle(true);
        }
        mHandler.removeCallbacks(resetDataRunnable);
        mRoot = null;
        isRestoreData = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoEditActivity.REQUSET_SOUND) {
            if (resultCode == Activity.RESULT_OK) {
                AudioMusicInfo audioMusic = (AudioMusicInfo) data
                        .getParcelableExtra(MoreMusicActivity.MUSIC_INFO);
                String musicName = audioMusic.getName();
                //保存   全部装换成 毫秒
                //当前时间
                float progress = mScrollView.getProgress();
                mSoundInfo = new SoundInfo();
                mSoundInfo.setName(musicName);
                mSoundInfo.setStart((int) progress);
                mSoundInfo.setEnd(duration);
                mSoundInfo.setTrmeStart(audioMusic.getStart());
                mSoundInfo.setTrmeEnd(audioMusic.getDuration());
                mSoundInfo.setId(id++);
                mSoundInfo.setPath(audioMusic.getPath());
                mSoundInfo.setMixFactor(mMixFactor);
                //添加滑条
                mSubtitleLine.addRect((int) mSoundInfo.getStart(), (int) mSoundInfo.getStart() + 10, "", mSoundInfo.getId());
                //刷新UI
                mDelete.setVisibility(View.VISIBLE);
                mSoundInfos.add(mSoundInfo);
                mAdapter.addAll(mSoundInfos, tvAdded, -1);
                TempVideoParams.getInstance().setSoundInfoList(mSoundInfos);
                editUI();
                checkTitleLayout();
                //播放
                onMusicChecked((int) progress, false, true);


                mEditAudioIdCurrent = mSoundInfo.getId();
                mStatus = 1;
            } else {
                mEditorHandler.reload(false);
                mEditorHandler.seekTo(0);
                mEditorHandler.start();
                mStatus = 0;
            }
        }
    }

    /**
     * 选中一首配乐
     *
     * @param to        跳转到哪里
     * @param onlyMusic 仅重新加载音乐
     * @param play      播放
     */
    private void onMusicChecked(int to, boolean onlyMusic, boolean play) {
        mEditorHandler.removeMvMusic(true);
        mEditorHandler.reload(onlyMusic);
        mEditorHandler.seekTo(to);
//        mEditorHandler.getEditorVideo().setOriginalMixFactor(100 - mMixFactor);
        if (play) {
            mEditorHandler.start();
            setImage(R.drawable.edit_music_pause);
        }
    }

    /**
     * 音量
     */
    private LinearLayout mllAudioFactor;
    private VerticalSeekBar mVsbAudioFactor;
    private int mMixFactor = 50;
    private boolean mShowFactor = false;
    private int mEditAudioIdCurrent = -1;

    public void setSeekBar(LinearLayout layout) {
        this.mllAudioFactor = layout;
        mVsbAudioFactor = (VerticalSeekBar) layout.findViewById(R.id.vsbAudioFactor);
        mVsbAudioFactor.progress(50);
        mVsbAudioFactor.setOnProgressListener(new VerticalSeekBar.VerticalSeekBarProgressListener() {

            SoundInfo info;

            @Override
            public void onProgress(int progress) {
                mMixFactor = progress;
                // 如果选中了某一个配音片段
                if (mEditAudioIdCurrent != -1) {
                    // 如果当前有选中的配音片段，那设置的是该配音片段  ，        //实时调整播放器中的音频的音量大小
                    if (info != null) {
                        info.setMixFactor(progress);
                    }
                }
            }

            @Override
            public void onStartTouch() {
                if (mEditAudioIdCurrent != -1) {
                    for (SoundInfo s : mSoundInfos) {
                        if (s.getId() == mEditAudioIdCurrent) {
                            info = s;
                            break;
                        }
                    }
                }
            }

            @Override
            public void onStopTouch() {
                info = null;
            }
        });

    }

    public void setShowFactor(boolean showFactor) {
        this.mShowFactor = showFactor;
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATA:
                    mAdapter.addAll(mSoundInfos, tvAdded, -1);
                    checkTitleLayout();
                    resetMenuUI();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private static final int UPDATA = 150;

    /**
     * 设置音效地址
     */
    private String mSoundTypeUrl;
    private String mSoundUrl;

    public void setUrl(String typeUrl, String url) {
        mSoundTypeUrl = TextUtils.isEmpty(typeUrl) ? "" : typeUrl;
        mSoundUrl = TextUtils.isEmpty(url) ? "" : url;
    }

    //隐藏编辑框
    public void setHideEdit() {
        if (mSubtitleLine != null) {
            mSubtitleLine.setHideCurrent();
            mAdapter.addAll(mSoundInfos, tvAdded, -1);
        }
    }

}
