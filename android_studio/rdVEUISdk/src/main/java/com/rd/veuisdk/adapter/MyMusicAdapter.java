package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.recorder.AudioPlayer;
import com.rd.recorder.AudioPlayer.OnCompletionListener;
import com.rd.recorder.AudioPlayer.OnPreparedListener;
import com.rd.recorder.AudioPlayer.onProgressListener;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.MoreMusicActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.MyMusicFragment;
import com.rd.veuisdk.hb.views.PinnedSectionListView;
import com.rd.veuisdk.hb.views.PinnedSectionListView.PinnedSectionListAdapter;
import com.rd.veuisdk.model.AudioMusicInfo;
import com.rd.veuisdk.model.MyMusicInfo;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.ExpRangeSeekBar;
import com.rd.veuisdk.ui.RangeSeekBar;
import com.rd.veuisdk.ui.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyMusicAdapter extends BaseAdapter implements
        PinnedSectionListAdapter, SectionIndexer {
    private boolean mCanAutoPlay = false;
    private final int DEFALUTPOSITION = -1;
    private ArrayList<TreeNode> mTreeNodeGroups = new ArrayList<TreeNode>(),
            mSections = new ArrayList<TreeNode>();
    private LayoutInflater mGroupInflater;

    /**
     * 清空全部数据
     */
    public void clear() {
        mTempPosition = DEFALUTPOSITION;
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        mTreeNodeGroups.clear();
        mSections.clear();
        notifyDataSetChanged();
    }

    public void reset() {
        mTempPosition = DEFALUTPOSITION;
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == TreeNode.SECTION;
    }

    public void onSectionAdded(TreeNode section, int sectionPosition) {
        mSections.add(section);
    }

    @Override
    public Object[] getSections() {
        return mSections.toArray();
    }

    @Override
    public int getPositionForSection(int section) {
        if (section >= mSections.size()) {
            section = mSections.size() - 1;
        }
        if (section < 0) {
            return 0;
        } else {
            return mSections.get(section).listPosition;
        }

    }

    @Override
    public int getSectionForPosition(int position) {
        if (position >= getCount()) {
            position = getCount() - 1;
        }
        return getItem(position).sectionPosition;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private Context mContext;

    public MyMusicAdapter(Context context) {
        mContext = context;
        mTempPosition = DEFALUTPOSITION;
        mGroupInflater = LayoutInflater.from(mContext);

    }

    private String mLastMusic = "";

    public void replace(ArrayList<TreeNode> list, String lastMp3) {
        mTreeNodeGroups.clear();
        mTreeNodeGroups.addAll(list);
        mLastMusic = lastMp3;
        mTempPosition = DEFALUTPOSITION;
        this.notifyDataSetChanged();

    }

    public ArrayList<TreeNode> getData() {

        return new ArrayList<TreeNode>(mTreeNodeGroups);
    }

    private PinnedSectionListView mListView;

    public void getView(PinnedSectionListView mlistivew) {
        mListView = mlistivew;
    }

    public static class TreeNode {
        public static final int ITEM = 0;
        public static final int SECTION = 1;

        @Override
        public String toString() {
            return "TreeNode [type=" + type + ", text=" + text + ", childs="
                    + (null != childs ? childs.toString() : "null")
                    + ", sectionPosition=" + sectionPosition
                    + ", listPosition=" + listPosition + "]";
        }

        public int type = 0;
        public String text;
        public MyMusicInfo childs;
        public int sectionPosition;
        public int listPosition;
        public int tag = 0;
        public boolean selected = false;
    }

    private int min = 0, max = 10000;

    /**
     * @param convertView
     * @param position
     * @param bForceClick 下载完成，响应再次点击 播放音频
     */
    public void onItemClick(View convertView, int position, boolean bForceClick) {
        // Log.e("onItemClick", "..." + position + "...." + mTempPosition);
        if (null == convertView) {
            return;
        }
        // if (mTempPosition != position) {
        // onSendMessage();
        ImageView tempstate = (ImageView) convertView
                .findViewById(R.id.iv_select_music_state);
        convertView.findViewById(R.id.artname).setVisibility(View.GONE);
        ExpRangeSeekBar tempm_rbBar = (ExpRangeSeekBar) convertView
                .findViewById(R.id.mrangseekbar);
        FrameLayout templayout = (FrameLayout) convertView
                .findViewById(R.id.llRangeSeekBar);
        View addbtn = convertView.findViewById(R.id.item_add);
        View temp_child_hintView = convertView
                .findViewById(R.id.child_hintview);
        View cbCheckBox = convertView.findViewById(R.id.cbHistoryCheck);

        ExpRangeSeekBar msb;
        WebMusicInfo info = getItem(position).childs.getmInfo();

        if (mTempPosition != DEFALUTPOSITION) {

            if (mTempPosition == position && (!bForceClick)) {
                Log.e("onitemclick", "不响应重复点击");
                return;
            } else {

                setCanAutoPlay(true);
                View itemView = getItemView();
                if (null != itemView) {
                    msb = (ExpRangeSeekBar) itemView
                            .findViewById(R.id.mrangseekbar);
                    msb.setOnRangeSeekBarChangeListener(null);
                    FrameLayout lrangseekbar = (FrameLayout) itemView
                            .findViewById(R.id.llRangeSeekBar);
                    View laddbtn = itemView.findViewById(R.id.item_add);
                    laddbtn.setVisibility(View.INVISIBLE);
                    lrangseekbar.setVisibility(View.GONE);
                    ImageView state = (ImageView) itemView
                            .findViewById(R.id.iv_select_music_state);
                    state.setVisibility(View.GONE);
                    itemView.findViewById(R.id.child_hintview).setVisibility(
                            View.GONE);

                    if (mTreeNodeGroups.get(position).tag == 3) {
                        itemView.findViewById(R.id.cbHistoryCheck)
                                .setVisibility(View.VISIBLE);
                    }
                }
            }

        }

        if (null != mAudioPlayer) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.stop();
            }
            mAudioPlayer.reset();
        }
        mTempPosition = position;

        info.checkExists();
        if (!info.exists()) {
            onDownMusic(position, info);
            return;
        }

        // Log.e("iscurrent", iscurrent + "");

        mduration = Utils.s2ms(VirtualVideo.getMediaInfo(info.getLocalPath(), null));

        max = mduration;
        if (null != tempm_rbBar) {
            tempm_rbBar.setDuration(mduration);
            tempm_rbBar.setHandleValue(0, mduration);
            tempm_rbBar.resetProgress();
            tempm_rbBar.setAutoScroll();
            tempm_rbBar.canTouchRight();
        }
        onPrePare(0, mduration);

        tempstate.setVisibility(View.VISIBLE);

        addbtn.setVisibility(View.VISIBLE);
        templayout.setVisibility(View.VISIBLE);
        temp_child_hintView.setVisibility(View.VISIBLE);

        addbtn.setOnClickListener(mAddMusicListener);
        tempm_rbBar.setOnRangeSeekBarChangeListener(onrangListener);

        Intent in = new Intent(MyMusicFragment.ACTION_SHOW);
        if (mTempPosition == (getCount() - 1)
                || mTempPosition == (getCount() - 2)) {

            in.putExtra(MyMusicFragment.BCANSHOW, false);
            mContext.sendBroadcast(in);
        } else {
            in.putExtra(MyMusicFragment.BCANSHOW, true);
            mContext.sendBroadcast(in);
        }
        // }

    }

    private final String EXTENSION = "mp3";
    private ArrayList<String> mDownloading = new ArrayList<String>();

    private void onDownMusic(final int position, final WebMusicInfo info) {
        // android.util.Log.e("onDownMusic",
        // position + "..." + info.getMusicName());
        if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            SysAlertDialog.showAutoHideDialog(mContext, 0,
                    R.string.please_check_network, Toast.LENGTH_SHORT);
        } else {
            if (null == mDownloading) {
                mDownloading = new ArrayList<String>();
            }
            if (mDownloading.contains(info.getMusicUrl())) {
                // 下载中
                return;
            }
            DownLoadUtils down = new DownLoadUtils(info.getMusicUrl()
                    .hashCode(), info.getMusicUrl(), EXTENSION);
            down.setMethod(false);

            View v = getItemView();

            if (null != v) {
                v.findViewById(R.id.music_state).setVisibility(View.GONE);
                CircleProgressBarView bar = (CircleProgressBarView) v
                        .findViewById(R.id.music_pbar);
                if (null != bar) {
                    bar.setVisibility(View.VISIBLE);
                    bar.setProgress(1);
                }
            }

            down.DownFile(new IDownFileListener() {

                @Override
                public void onProgress(long arg0, int arg1) {
                    // Log.e("onProgress", "onProgress" + arg0);
                    if (null != mHandler) {
                        mHandler.obtainMessage(MSG_PROGRESS, (int) arg0, arg1)
                                .sendToTarget();
                    }
                }

                @Override
                public void Finished(long arg0, String arg1) {
                    // Log.e("Finished", "Finished" + arg0);
                    File ftarget = new File(info.getLocalPath());
                    File fold = new File(arg1);
                    FileUtils.deleteAll(ftarget);
                    if (fold.renameTo(ftarget)) {
                        if (null != mHandler) {
                            info.checkExists();
                            mHandler.obtainMessage(MSG_FINISHED, position)
                                    .sendToTarget();
                        }
                    } else {
                        Canceled(arg0);
                    }
                }

                @Override
                public void Canceled(long arg0) {
                    // Log.e("Canceled", "Canceled" + arg0);
                    if (null != mDownloading) {
                        mDownloading.remove(info.getMusicUrl());
                    }
                    if (null != mHandler) {
                        mHandler.obtainMessage(MSG_CANCELED, position)
                                .sendToTarget();
                    }

                }
            });
            mDownloading.add(info.getMusicUrl());
        }

    }

    public void onPause() {
//		Log.e("onPause", this.toString());
        if (null != mAudioPlayer) {
            mAudioPlayer.stop();
            // Log.e("onPause", "player pause");
        }
        View itemView = getItemView();
        if (null != itemView) {

            ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                    .findViewById(R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.setHandleValue(min, max);
                m_sbRanger.resetProgress();
            }

            ImageView state = (ImageView) itemView
                    .findViewById(R.id.iv_select_music_state);
            state.setImageResource(R.drawable.edit_music_play);
        }
    }

    public void onStart() {
        mIsStoped = false;
    }

    public void onStartReload() {
        mIsStoped = false;
        if (mTreeNodeGroups != null && mTreeNodeGroups.size() != 0) {
            this.notifyDataSetChanged();
        }
    }

    private boolean mIsStoped = false;

    public void onStop() {
        mIsStoped = true;
        if (null != mAudioPlayer) {
            // Log.e("onstop", "stopmusic");
            mAudioPlayer.stop();
        }
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                    .findViewById(R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.resetProgress();
            }
        }
        // Log.e("onstop", "xxxxxxxxxxxxxxxxxxxxxx" + this.toString());
        DownLoadUtils.forceCancelAll();
        if (null != mDownloading) {
            // Log.e("onstop", "yyyyyyyyyyyyyyyyyyyyyyy");
            mLastMusic = "";
            mDownloading.clear();
            mDownloading = null;
        }

    }

    public void onDestroy() {
        mHandler = null;
        // Log.e("onDestroy", "onDestroy" + this.toString());
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                    .findViewById(R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.setOnRangeSeekBarChangeListener(null);
            }
        }
        if (null != mAudioPlayer) {
            mAudioPlayer.setOnCompletionListener(null);
            mAudioPlayer.setProgressListener(null);
            mAudioPlayer.setOnPreparedListener(null);
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
    }

    private AudioPlayer mAudioPlayer;

    public int mTempPosition = DEFALUTPOSITION;

    private class StateListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // Log.e("state click....", mAudioPlayer.isPlaying() + ".........."
            // + mAudioPlayer.getCurrentPosition() + "...."
            // + mAudioPlayer.getDuration());
            View itemView = getItemView();
            if (null != mAudioPlayer) {
                if (mAudioPlayer.isPlaying()) {
                    // 暂停播放
                    mAudioPlayer.pause();
                    if (null != itemView) {
                        ImageView state = (ImageView) itemView
                                .findViewById(R.id.iv_select_music_state);
                        state.setImageResource(R.drawable.edit_music_play);
                    }
                } else {
                    // 继续播放
                    mAudioPlayer.start();
                    setCanAutoPlay(true);

                    if (null != itemView) {
                        ImageView state = (ImageView) itemView
                                .findViewById(R.id.iv_select_music_state);
                        state.setImageResource(R.drawable.edit_music_pause);
                    }

                }
            }
        }
    }

    ;

    private OnClickListener mAddMusicListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            View itemView = getItemView();
            if (null != itemView) {
                ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                        .findViewById(R.id.mrangseekbar);
                if (null != m_sbRanger) {
                    MyMusicInfo info = getItem(mTempPosition).childs;
                    WebMusicInfo minfo = info.getmInfo();
                    onSendAddMessage(new AudioMusicInfo(minfo.getLocalPath(),
                            minfo.getMusicName(),
                            (int) ((long) m_sbRanger.getSelectedMinValue()),
                            (int) ((long) m_sbRanger.getSelectedMaxValue()),
                            (int) minfo.getDuration()));
                }
            }
        }
    };

    private void onCompleted() {
        setCanAutoPlay(false);
        if (null != mAudioPlayer) {
            // mAudioPlayer.stop();
            // mAudioPlayer.reset();
            mAudioPlayer.seekTo(0);
        }
        View itemView = getItemView();

        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                    .findViewById(R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.setHandleValue(min, max);
                m_sbRanger.resetProgress();
            }
            ImageView state = (ImageView) itemView
                    .findViewById(R.id.iv_select_music_state);
            state.setImageResource(R.drawable.edit_music_play);
        }
        // onPrePare(min, max);

    }

    private String TAG = "MyMusicAdapter";
    private onProgressListener mOnProgressListener = new onProgressListener() {

        @Override
        public void onProgress(int arg0) {
            View itemView = getItemView();
            if (null != itemView && null != mAudioPlayer) {
                ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                        .findViewById(R.id.mrangseekbar);
//                android.util.Log.e(TAG, "onProgress: " + min + "...." + mAudioPlayer.getCurrentPosition());
                m_sbRanger.setProgress(min + mAudioPlayer.getCurrentPosition());
            }
        }
    };

    private OnPreparedListener mOnPrepareListener = new OnPreparedListener() {

        @Override
        public void onPrepared(AudioPlayer arg0) {
            // Log.e("mOnPrepareListener", arg0.getDuration() + "..");
            mHandler.sendEmptyMessage(PREPARED);
        }
    };
    private OnCompletionListener mOnCompleteListener = new OnCompletionListener() {

        @Override
        public void onCompletion(AudioPlayer arg0) {
            // Log.e("onCompletion", arg0.getDuration() + "..");
            onCompleted();

        }
    };
    private AudioPlayer.OnErrorListener mOnErrorListener = new AudioPlayer.OnErrorListener() {

        @Override
        public boolean onError(AudioPlayer arg0, int arg1, int arg2) {
            // Log.e("mOnErrorListener", arg0.getDuration() + ".." + arg1 + "......" +
            // arg2);
            return false;
        }
    };
    private AudioPlayer.OnInfoListener mOnInfoListener = new AudioPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(AudioPlayer arg0, int arg1, int arg2) {
            // Log.e("onInfo", arg0.getDuration() + ".." + arg1 + "...." +
            // arg2);
            return false;
        }
    };

    /**
     * 每次切换Item 截取音频时间段重新归0(0,duration)
     *
     * @param start
     * @param end
     */
    private void onPrePare(int start, int end) {
//		Log.e("mOnPrepareListener", start + "------------/" + end);
        if (mAudioPlayer == null) {
            mAudioPlayer = new AudioPlayer();
        } else {
            mAudioPlayer.stop();
            mAudioPlayer.reset();
        }

        TreeNode node = getItem(mTempPosition);
        if (null != node) {
            MyMusicInfo info = node.childs;
            if (null != info) {
                String path = info.getmInfo().getLocalPath();
                if (!TextUtils.isEmpty(path)) {
                    try {
                        mAudioPlayer.setDataSource(path);
                        mduration = Utils.s2ms(VirtualVideo.getMediaInfo(path, null));

                        mAudioPlayer.setTimeRange(start, end);
                        min = start;
                        max = end;
                        mAudioPlayer.setProgressListener(mOnProgressListener);

                        mAudioPlayer.setOnPreparedListener(mOnPrepareListener);
                        mAudioPlayer.setOnErrorListener(mOnErrorListener);
                        mAudioPlayer.setOnInfoListener(mOnInfoListener);
                        mAudioPlayer.setOnCompletionListener(mOnCompleteListener);
                        mAudioPlayer.prepareAsync();

                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private View getItemView() {
        if (null == mListView) {
            return null;
        }
        int mtep = mTempPosition - mListView.getFirstVisiblePosition();
        if (mtep < 0) {
            return null;
        }
        try {
            // Log.e("getItemView",
            // "childat-->" + mtep + "...temp:" + mTempPosition
            // + "...firt:" + mListView.getFirstVisiblePosition());
            return mListView.getChildAt(mtep);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;

    }

    private OnRangeSeekBarChangeListener onrangListener = new OnRangeSeekBarChangeListener() {
        View itemView;

        @Override
        public void onPlay(int currentProgress) {
            tempStart = currentProgress;
            min = tempStart;
//            Log.e("ranglistern,....", "onPlay.mminValue" + currentProgress);
            mCanAutoPlay = true;
            onPrePare(tempStart, tempEnd);
        }

        private int tempStart = 0, tempEnd = 0;

        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar bar,
                                                int minValue, int maxValue) {
            tempStart = minValue;
            tempEnd = maxValue;
            min = tempStart;
            max = tempEnd;
//            Log.e(".ranglistern", minValue
//                    + "   onRangeSeekBarValuesChanged,...." + maxValue);
        }

        @Override
        public void onActionDown(int currentprogress) {
//            Log.e(".ranglistern", "   onActionDown,...." + currentprogress);
            itemView = getItemView();
            if (null != itemView) {

                ImageView state = (ImageView) itemView
                        .findViewById(R.id.iv_select_music_state);
                state.setImageResource(R.drawable.edit_music_play);
                ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                        .findViewById(R.id.mrangseekbar);
                if (null != m_sbRanger) {
                    m_sbRanger.resetProgress();

                }
            }
            onPlayerPause();

        }
    };

    /***
     * 暂停音频播放器
     */
    private void onPlayerPause() {
        if (null != mAudioPlayer) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
            }

        }
    }

    private void onMediaPlay() {

        if (null != mAudioPlayer) {
            mAudioPlayer.start();
            // Log.e("onMediaPlay", "..." + mAudioPlayer.getCurrentPosition());
        } else {
            // Log.e("onMediaPlay", "failed....");
        }

        View itemView = getItemView();
        if (null != itemView) {
            ImageView state = (ImageView) itemView
                    .findViewById(R.id.iv_select_music_state);
            state.setImageResource(R.drawable.edit_music_pause);
        }

    }

    private void preparePlayer() {
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                    .findViewById(R.id.mrangseekbar);

            ImageView state = (ImageView) itemView
                    .findViewById(R.id.iv_select_music_state);
//            Log.e("preparePlayer", "..." + mduration);
            m_sbRanger.setDuration(mduration);
            m_sbRanger.setHandleValue(min, max);
            m_sbRanger.setAutoScroll();
            m_sbRanger.canTouchRight();
            m_sbRanger.resetProgress();
            state.setImageResource(R.drawable.edit_music_play);
        }
    }

    private final int PREPARED = 1;
    private int mduration;
    private final int MSG_PROGRESS = 2001;
    private final int MSG_FINISHED = 2002;
    private final int MSG_CANCELED = 2003;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PREPARED:
                    preparePlayer();
                    // Log.e("handler", mduration + "prepared" + mCanAutoPlay);
                    if (mCanAutoPlay) {
                        onMediaPlay();
                    }
                    break;
                case MSG_PROGRESS:
                    // Log.e("MSG_PROGRESS", "MSG_PROGRESS---" + mTempPosition);
                    View item = getItemView();
                    if (null != item) {
                        CircleProgressBarView pbar = (CircleProgressBarView) item
                                .findViewById(R.id.music_pbar);
                        if (null != pbar) {
                            pbar.setProgress(msg.arg2);
                            if (pbar.getVisibility() != View.VISIBLE) {
                                pbar.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    break;
                case MSG_FINISHED: {
                    // Log.e("MSG_FINISHED", "MSG_FINISHED---" + mTempPosition);
                    View v = getItemView();
                    if (null != v) {
                        v.findViewById(R.id.music_state).setVisibility(View.GONE);
                        v.findViewById(R.id.music_down_layout).setVisibility(
                                View.GONE);
                        v.findViewById(R.id.music_pbar).setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                    if (!mIsStoped) {
                        onItemClick(getItemView(), mTempPosition, true);
                    }
                }
                break;
                case MSG_CANCELED: {
                    // Log.e("MSG_CANCELED", "MSG_CANCELED---" + mTempPosition);
                    View v = getItemView();
                    if (null != v) {
                        v.findViewById(R.id.music_state)
                                .setVisibility(View.VISIBLE);
                        v.findViewById(R.id.music_down_layout).setVisibility(
                                View.VISIBLE);
                        v.findViewById(R.id.music_pbar).setVisibility(View.GONE);
                        v.findViewById(R.id.item_add).setVisibility(View.GONE);
                    }
                    mTempPosition = DEFALUTPOSITION;
                    notifyDataSetChanged();
                    if (!mIsStoped) {
                        onItemClick(getItemView(), mTempPosition, true);
                    }
                }
                break;

                default:
                    break;
            }
        }

        ;
    };

    private class ChildViewHolder {

        TextView songname, title, artname, tvDuration;
        View content, layout, child_hint;
        View addbtn;
        ImageView state;
        CheckBox delete;
        ExpRangeSeekBar mBar;
        FrameLayout mLayout;
        FrameLayout music_down_layout;

    }

    @Override
    public int getCount() {
        return mTreeNodeGroups.size();
    }

    @Override
    public TreeNode getItem(int position) {
        try {

            return mTreeNodeGroups.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            int re = mTreeNodeGroups.size() - 1;
            if (re >= 0) {
                return mTreeNodeGroups.get(re);
            }
            return null;

        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ChildViewHolder vh = null;
        StateListener mStateListener;
        final TreeNode node = getItem(position);
        if (null == convertView) {
            convertView = mGroupInflater.inflate(R.layout.rdveuisdk_mymusic_child,
                    null);
            vh = new ChildViewHolder();
            vh.title = (TextView) convertView.findViewById(R.id.node_title);
            vh.state = (ImageView) convertView
                    .findViewById(R.id.iv_select_music_state);
            vh.delete = (CheckBox) convertView
                    .findViewById(R.id.cbHistoryCheck);
            vh.songname = (TextView) convertView.findViewById(R.id.songname);
            vh.artname = (TextView) convertView.findViewById(R.id.artname);
            vh.addbtn = convertView.findViewById(R.id.item_add);
            vh.content = convertView.findViewById(R.id.mymusic_item_content);
            vh.layout = convertView.findViewById(R.id.mymusic_item);
            vh.mBar = (ExpRangeSeekBar) convertView
                    .findViewById(R.id.mrangseekbar);
            vh.mBar.canTouchRight();
            vh.mLayout = (FrameLayout) convertView
                    .findViewById(R.id.llRangeSeekBar);
            vh.tvDuration = (TextView) convertView.findViewById(R.id.duration);
            vh.music_down_layout = (FrameLayout) convertView
                    .findViewById(R.id.music_down_layout);
            mStateListener = new StateListener();
            vh.state.setOnClickListener(mStateListener);
            vh.state.setTag(mStateListener);
            vh.delete.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    node.selected = isChecked;
                }
            });
            vh.child_hint = convertView.findViewById(R.id.child_hintview);
            convertView.setTag(vh);
        } else {
            vh = (ChildViewHolder) convertView.getTag();
            mStateListener = (StateListener) vh.state.getTag();
        }

        if (node.type == TreeNode.SECTION) {
            vh.content.setVisibility(View.GONE);
            vh.title.setVisibility(View.VISIBLE);
            vh.title.setText(node.text);
            View divider = convertView.findViewById(R.id.viewDivider);
            divider.setVisibility(View.GONE);
            View topdivider = convertView.findViewById(R.id.viewTopDivider);
            topdivider.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(parent.getResources().getColor(
                    R.color.transparent));

            if (node.tag == 2 || node.tag == 3) {
                vh.title.setVisibility(View.GONE);
            }
        } else {
            View divider = convertView.findViewById(R.id.viewDivider);
            divider.setVisibility(View.VISIBLE);
            View topdivider = convertView.findViewById(R.id.viewTopDivider);
            topdivider.setVisibility(View.GONE);
            convertView.setBackgroundColor(0);
            vh.title.setVisibility(View.GONE);
            vh.content.setVisibility(View.VISIBLE);
            MyMusicInfo info = node.childs;
            vh.songname.setText(info.getmInfo().getMusicName());
            info.getmInfo().checkExists();
            boolean isdownloading = (null != mDownloading && mDownloading
                    .contains(info.getmInfo().getMusicUrl()));
            int vi = (info.getmInfo().exists() || isdownloading) ? View.GONE
                    : View.VISIBLE;
            vh.music_down_layout.setVisibility(vi);
            if (vi != View.VISIBLE) {
                convertView.findViewById(R.id.music_state).setVisibility(
                        View.GONE);
                convertView.findViewById(R.id.music_pbar).setVisibility(
                        View.GONE);
            } else {
                if (isdownloading) {
                    convertView.findViewById(R.id.music_state).setVisibility(
                            View.GONE);
                    convertView.findViewById(R.id.music_pbar).setVisibility(
                            View.VISIBLE);
                } else {
                    convertView.findViewById(R.id.music_state).setVisibility(
                            View.VISIBLE);
                    convertView.findViewById(R.id.music_pbar).setVisibility(
                            View.GONE);
                }

            }
            if (node.tag == 2 || node.tag == 0) {
                // vh.artname.setText(info.getmInfo().getArtName());
            } else {
                vh.artname.setText("");
            }

            vh.tvDuration.setText(DateTimeUtils.stringForTime(info.getmInfo()
                    .getDuration()));

            // Log.e("getview->" + info.getmInfo().getMusicName(), position
            // + "...." + mTempPosition + "...."
            // + info.getmInfo().getLocalPath() + "...."
            // + info.getmInfo().exists() + "..." + vi + ".....duration:"
            // + mduration + "...." + min + "<>" + max);

            if (position == mTempPosition) {
                if (mduration > 0) {
                    vh.mBar.setDuration(mduration);
                }
                vh.state.setVisibility(View.VISIBLE);
                vh.mLayout.setVisibility(View.VISIBLE);
                vh.mBar.setHandleValue(min, max);
                vh.mBar.setOnRangeSeekBarChangeListener(onrangListener);
                vh.child_hint.setVisibility(View.VISIBLE);
                vh.addbtn.setVisibility(View.VISIBLE);
                vh.addbtn.setOnClickListener(mAddMusicListener);
                vh.artname.setVisibility(View.GONE);

            } else {
                if (node.tag == 3) {
                    vh.delete.setVisibility(View.VISIBLE);
                    vh.delete.setChecked(node.selected);
                }
                vh.mLayout.setVisibility(View.GONE);
                vh.state.setVisibility(View.GONE);
                vh.addbtn.setVisibility(View.INVISIBLE);
                vh.child_hint.setVisibility(View.GONE);
                vh.addbtn.setOnClickListener(null);
                vh.mBar.setOnRangeSeekBarChangeListener(null);
                if (TextUtils
                        .equals(mLastMusic, info.getmInfo().getLocalPath())
                        && mTempPosition == DEFALUTPOSITION) {
                    onItemClick(convertView, position, true);
                }
            }

        }

        return convertView;
    }

    public void setCanAutoPlay(boolean canAutoPlay) {
        mCanAutoPlay = canAutoPlay;
    }

    public int getCheckId() {
        return mTempPosition;
    }

    public void setSelectAll(boolean select) {
        for (TreeNode tn : mTreeNodeGroups) {
            tn.selected = select;
        }
        notifyDataSetChanged();
    }

    public AudioMusicInfo getCheckedMusic() {
        if (mTempPosition != DEFALUTPOSITION) {
            View itemView = getItemView();
            if (null != itemView) {
                ExpRangeSeekBar m_sbRanger = (ExpRangeSeekBar) itemView
                        .findViewById(R.id.mrangseekbar);
                TreeNode mitemNode = getItem(mTempPosition);
                WebMusicInfo mInfo = mitemNode.childs.getmInfo();

                return new AudioMusicInfo(mInfo.getLocalPath(),
                        mInfo.getMusicName(),
                        (int) ((long) m_sbRanger.getSelectedMinValue()),
                        (int) ((long) m_sbRanger.getSelectedMaxValue()),
                        (int) mInfo.getDuration());
            }

        }
        return null;
    }

    private void onSendAddMessage(AudioMusicInfo info) {
        Intent addIntent = new Intent(MoreMusicActivity.ACTION_ADD_LISTENER);
        addIntent.putExtra(MoreMusicActivity.CONTENT_STRING, info);
        mContext.sendBroadcast(addIntent);
    }

}
