package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.FileUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.recorder.AudioPlayer;
import com.rd.recorder.AudioPlayer.OnCompletionListener;
import com.rd.recorder.AudioPlayer.OnPreparedListener;
import com.rd.recorder.AudioPlayer.onProgressListener;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.utils.Log;
import com.rd.veuisdk.MoreMusicActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.fragment.MyMusicFragment;
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

public class MyMusicAdapter extends BaseAdapter {
    private boolean mCanAutoPlay = false;
    private final int DEFALUTPOSITION = -1;
    private ArrayList<TreeNode> mTreeNodeGroups = new ArrayList<>(),
            mListTreeNode = new ArrayList<>();
    private LayoutInflater mGroupInflater;
    private final String EXTENSION = "mp3";

    /**
     * 清空全部数据
     */
    public void clear() {
        mTempPosition = DEFALUTPOSITION;
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        mTreeNodeGroups.clear();
        mListTreeNode.clear();
        notifyDataSetChanged();
    }

    public void reset() {
        mTempPosition = DEFALUTPOSITION;
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
        }
        notifyDataSetChanged();
    }


    public void addTreeNode(TreeNode treeNode) {
        if (null != mListTreeNode) {
            mListTreeNode.add(treeNode);
        }
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
        return new ArrayList<>(mTreeNodeGroups);
    }

    private ListView mListView;

    public void setListView(ListView mlistivew) {
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
        if (null == convertView || position >= getCount()) {
            return;
        }

        Utils.$(convertView, R.id.artname).setVisibility(View.GONE);

        MyMusicInfo myMusicInfo = getItem(position).childs;
        if (null == myMusicInfo) {
            LogUtil.e(TAG, "onItemClick: " + position);
            return;
        }
        WebMusicInfo info = myMusicInfo.getmInfo();

        if (mTempPosition != DEFALUTPOSITION) {

            if (mTempPosition == position && (!bForceClick)) {
                Log.i(TAG, "onItemClick repeat click...");
                return;
            } else {
                if (isRunning) {
                    setCanAutoPlay(true);
                } else {
                    setCanAutoPlay(false);
                }
                View itemView = getItemView();
                if (null != itemView) {
                    ExpRangeSeekBar msb = Utils.$(itemView, R.id.mrangseekbar);
                    msb.setOnRangeSeekBarChangeListener(null);
                    Utils.$(itemView, R.id.llRangeSeekBar).setVisibility(View.GONE);
                    Utils.$(itemView, R.id.item_add).setVisibility(View.INVISIBLE);
                    Utils.$(itemView, R.id.iv_select_music_state).setVisibility(View.GONE);
                    Utils.$(itemView, R.id.child_hintview).setVisibility(View.GONE);
                    if (mTreeNodeGroups.get(position).tag == 3) {
                        Utils.$(itemView, R.id.cbHistoryCheck).setVisibility(View.VISIBLE);
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


        mduration = Utils.s2ms(VirtualVideo.getMediaInfo(info.getLocalPath(), null));

        max = mduration;
        ExpRangeSeekBar seekBar = Utils.$(convertView, R.id.mrangseekbar);
        if (null != seekBar) {
            seekBar.setDuration(mduration);
            seekBar.setHandleValue(0, mduration);
            seekBar.resetProgress();
            seekBar.setAutoScroll();
            seekBar.canTouchRight();
        }
        onPrePare(0, mduration);
        Utils.$(convertView, R.id.iv_select_music_state).setVisibility(View.VISIBLE);
        Utils.$(convertView, R.id.llRangeSeekBar).setVisibility(View.VISIBLE);
        View addbtn = Utils.$(convertView, R.id.item_add);
        addbtn.setVisibility(View.VISIBLE);
        Utils.$(convertView, R.id.child_hintview).setVisibility(View.VISIBLE);

        addbtn.setOnClickListener(mAddMusicListener);
        seekBar.setOnRangeSeekBarChangeListener(onrangListener);

        Intent in = new Intent(MyMusicFragment.ACTION_SHOW);
        if (mTempPosition == (getCount() - 1)
                || mTempPosition == (getCount() - 2)) {
            in.putExtra(MyMusicFragment.BCANSHOW, false);
            mContext.sendBroadcast(in);
        } else {
            in.putExtra(MyMusicFragment.BCANSHOW, true);
            mContext.sendBroadcast(in);
        }

    }


    private ArrayList<String> mDownloading = new ArrayList<>();

    private void onDownMusic(final int position, final WebMusicInfo info) {
        if (null == info || TextUtils.isEmpty(info.getMusicUrl())) {
            return;
        }
        if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            SysAlertDialog.showAutoHideDialog(mContext, 0,
                    R.string.please_check_network, Toast.LENGTH_SHORT);
        } else {
            if (null == mDownloading) {
                mDownloading = new ArrayList<>();
            }
            if (mDownloading.contains(info.getMusicUrl())) {
                // 下载中
                return;
            }
            DownLoadUtils down = new DownLoadUtils(mContext, info.getMusicUrl()
                    .hashCode(), info.getMusicUrl(), EXTENSION);
            down.setMethod(false);

            View v = getItemView();

            if (null != v) {
                Utils.$(v, R.id.music_state).setVisibility(View.GONE);
                CircleProgressBarView bar = Utils.$(v, R.id.music_pbar);
                if (null != bar) {
                    bar.setVisibility(View.VISIBLE);
                    bar.setProgress(1);
                }
            }

            down.DownFile(new IDownFileListener() {
                @Override
                public void onProgress(long arg0, int arg1) {
                    if (null != mHandler) {
                        onDownloadProgress(arg1);
                    }
                }

                @Override
                public void Finished(long arg0, String arg1) {
                    File ftarget = new File(info.getLocalPath());
                    File fold = new File(arg1);
                    FileUtils.deleteAll(ftarget);
                    if (fold.renameTo(ftarget)) {
                        if (null != mHandler) {
                            info.checkExists();
                            mHandler.obtainMessage(MSG_FINISHED, position).sendToTarget();
                        }
                        if (null != mDownloading) {
                            mDownloading.remove(info.getMusicUrl());
                        }
                    } else {
                        Canceled(arg0);
                    }
                }

                @Override
                public void Canceled(long arg0) {
                    if (null != mDownloading) {
                        mDownloading.remove(info.getMusicUrl());
                    }
                    if (null != mHandler) {
                        mHandler.obtainMessage(MSG_CANCELED, position).sendToTarget();
                    }

                }
            });
            mDownloading.add(info.getMusicUrl());
        }

    }


    public void onResume() {
        isRunning = true;
    }

    /**
     * fragment是否在前台
     */
    private boolean isRunning = false;

    public void onPause() {
        isRunning = false;
        if (null != mAudioPlayer) {
            mAudioPlayer.stop();
        }
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.setHandleValue(min, max);
                m_sbRanger.resetProgress();
            }
            ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
            state.setImageResource(R.drawable.edit_music_play);
        }
    }

    public void onStart() {
        mIsStoped = false;
        isRunning = true;
    }

    public void onStartReload() {
        mIsStoped = false;
        if (mTreeNodeGroups != null && mTreeNodeGroups.size() != 0) {
            this.notifyDataSetChanged();
        }
    }

    private boolean mIsStoped = false;

    public void onStop() {
        isRunning = false;
        mIsStoped = true;
        if (null != mAudioPlayer) {
            mAudioPlayer.stop();
        }
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.resetProgress();
            }
        }
        DownLoadUtils.forceCancelAll();
        if (null != mDownloading) {
            mLastMusic = "";
            mDownloading.clear();
            mDownloading = null;
        }

    }

    public void onDestroy() {
        mHandler = null;
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
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
            View itemView = getItemView();
            if (null != mAudioPlayer) {
                if (mAudioPlayer.isPlaying()) {
                    // 暂停播放
                    mAudioPlayer.pause();
                    if (null != itemView) {
                        ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
                        state.setImageResource(R.drawable.edit_music_play);
                    }
                } else {
                    // 继续播放
                    mAudioPlayer.start();
                    setCanAutoPlay(true);
                    if (null != itemView) {
                        ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
                        state.setImageResource(R.drawable.edit_music_pause);
                    }
                }
            } else {
                if (isRunning) {
                    setCanAutoPlay(true);
                    TreeNode node = getItem(mTempPosition);
                    if (null != node) {
                        MyMusicInfo info = node.childs;
                        if (null != info) {
                            String path = info.getmInfo().getLocalPath();
                            if (!TextUtils.isEmpty(path)) {
                                mduration = Utils.s2ms(VirtualVideo.getMediaInfo(path, null));
                                if (mduration > 10) {
                                    onPrePare(0, mduration);
                                }
                            }
                        }
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
                ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
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
            mAudioPlayer.seekTo(0);
        }
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
            if (null != m_sbRanger) {
                m_sbRanger.setHandleValue(min, max);
                m_sbRanger.resetProgress();
            }
            ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
            state.setImageResource(R.drawable.edit_music_play);
        }

    }

    private String TAG = "MyMusicAdapter";
    private onProgressListener mOnProgressListener = new onProgressListener() {

        @Override
        public void onProgress(int arg0) {
            View itemView = getItemView();
            if (null != itemView && null != mAudioPlayer) {
                ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
                m_sbRanger.setProgress(min + mAudioPlayer.getCurrentPosition());
            }
        }
    };

    private OnPreparedListener mOnPrepareListener = new OnPreparedListener() {

        @Override
        public void onPrepared(AudioPlayer arg0) {
            mHandler.sendEmptyMessage(PREPARED);
        }
    };
    private OnCompletionListener mOnCompleteListener = new OnCompletionListener() {

        @Override
        public void onCompletion(AudioPlayer arg0) {
            onCompleted();

        }
    };
    private AudioPlayer.OnErrorListener mOnErrorListener = new AudioPlayer.OnErrorListener() {

        @Override
        public boolean onError(AudioPlayer arg0, int arg1, int arg2) {
            return false;
        }
    };
    private AudioPlayer.OnInfoListener mOnInfoListener = new AudioPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(AudioPlayer arg0, int arg1, int arg2) {
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
            mCanAutoPlay = true;
            onPrePare(tempStart, tempEnd);
        }

        private int tempStart = 0, tempEnd = 0;

        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue) {
            tempStart = minValue;
            tempEnd = maxValue;
            min = tempStart;
            max = tempEnd;
        }

        @Override
        public void onActionDown(int currentprogress) {
            itemView = getItemView();
            if (null != itemView) {
                ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
                state.setImageResource(R.drawable.edit_music_play);
                ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
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
        }
        View itemView = getItemView();
        if (null != itemView) {
            ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
            state.setImageResource(R.drawable.edit_music_pause);
        }

    }

    private void preparePlayer() {
        View itemView = getItemView();
        if (null != itemView) {
            ExpRangeSeekBar seekBar = Utils.$(itemView, R.id.mrangseekbar);
            ImageView state = Utils.$(itemView, R.id.iv_select_music_state);
            seekBar.setDuration(mduration);
            seekBar.setHandleValue(min, max);
            seekBar.setAutoScroll();
            seekBar.canTouchRight();
            seekBar.resetProgress();
            state.setImageResource(R.drawable.edit_music_play);
        }
    }

    /**
     * 下载进度
     */
    private void onDownloadProgress(int progress) {
        View item = getItemView();
        if (null != item) {
            CircleProgressBarView barView = Utils.$(item, R.id.music_pbar);
            if (null != barView) {
                barView.setProgress(progress);
                if (barView.getVisibility() != View.VISIBLE) {
                    barView.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private final int PREPARED = 1;
    private int mduration;
    private final int MSG_FINISHED = 2002;
    private final int MSG_CANCELED = 2003;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case PREPARED:
                    preparePlayer();
                    if (mCanAutoPlay) {
                        onMediaPlay();
                    }
                    break;
                case MSG_FINISHED: {
                    View v = getItemView();
                    if (null != v) {
                        Utils.$(v, R.id.music_state).setVisibility(View.GONE);
                        Utils.$(v, R.id.music_down_layout).setVisibility(View.GONE);
                        Utils.$(v, R.id.music_pbar).setVisibility(View.GONE);
                    }
                    notifyDataSetChanged();
                    if (!mIsStoped) {
                        onItemClick(getItemView(), mTempPosition, true);
                    }
                }
                break;
                case MSG_CANCELED: {
                    View v = getItemView();
                    if (null != v) {
                        Utils.$(v, R.id.music_state).setVisibility(View.VISIBLE);
                        Utils.$(v, R.id.music_down_layout).setVisibility(View.VISIBLE);
                        Utils.$(v, R.id.music_pbar).setVisibility(View.GONE);
                        Utils.$(v, R.id.item_add).setVisibility(View.GONE);
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
    };

    private class ItemHolder {

        TextView songname, artname, tvDuration;
        //        TextView songname, title, artname, tvDuration;
        View content, layout, child_hint;
        View addbtn;
        ImageView state;
        CheckBox delete;
        ExpRangeSeekBar mBar;
        FrameLayout mLayout;
        FrameLayout music_down_layout;

        public ItemHolder(View view) {
//            title = Utils.$(view, R.id.node_title);
            state = Utils.$(view, R.id.iv_select_music_state);
            delete = Utils.$(view, R.id.cbHistoryCheck);
            songname = Utils.$(view, R.id.songname);
            artname = Utils.$(view, R.id.artname);
            addbtn = Utils.$(view, R.id.item_add);
            content = Utils.$(view, R.id.mymusic_item_content);
            layout = Utils.$(view, R.id.mymusic_item);
            mBar = Utils.$(view, R.id.mrangseekbar);
            mLayout = Utils.$(view, R.id.llRangeSeekBar);
            tvDuration = Utils.$(view, R.id.duration);
            music_down_layout = Utils.$(view, R.id.music_down_layout);
            child_hint = Utils.$(view, R.id.child_hintview);

        }
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
        ItemHolder vh = null;
        final TreeNode node = getItem(position);
        if (null == convertView) {
            convertView = mGroupInflater.inflate(R.layout.rdveuisdk_mymusic_child, null);
            vh = new ItemHolder(convertView);
            vh.mBar.canTouchRight();
            StateListener listener = new StateListener();
            vh.state.setOnClickListener(listener);
            vh.state.setTag(listener);
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
            vh = (ItemHolder) convertView.getTag();
        }
        convertView.setBackgroundColor(Color.TRANSPARENT);
        if (node.type == TreeNode.SECTION) {
            vh.content.setVisibility(View.GONE);
        } else {
            vh.content.setVisibility(View.VISIBLE);
            MyMusicInfo info = node.childs;
            vh.songname.setText(info.getmInfo().getMusicName());
            info.getmInfo().checkExists();
            boolean isdownloading = (null != mDownloading && mDownloading
                    .contains(info.getmInfo().getMusicUrl()));
            int visiblity = (info.getmInfo().exists() || isdownloading) ? View.GONE : View.VISIBLE;
            //如果文件存在 全部不显示 如果正在下载只显示进度
            vh.music_down_layout.setVisibility(visiblity);
            if (visiblity != View.VISIBLE) {
                if (isdownloading) {
                    vh.music_down_layout.setVisibility(View.VISIBLE);
                    Utils.$(convertView, R.id.music_state).setVisibility(View.GONE);
                    Utils.$(convertView, R.id.music_pbar).setVisibility(View.VISIBLE);
                } else {
                    Utils.$(convertView, R.id.music_state).setVisibility(View.GONE);
                    Utils.$(convertView, R.id.music_pbar).setVisibility(View.GONE);
                }
            } else {
                Utils.$(convertView, R.id.music_state).setVisibility(View.VISIBLE);
                Utils.$(convertView, R.id.music_pbar).setVisibility(View.GONE);
            }

            if (node.tag == 2 || node.tag == 0) {
            } else {
                vh.artname.setText("");
            }
            vh.tvDuration.setText(DateTimeUtils.stringForTime(info.getmInfo().getDuration()));

            if (info.getmInfo().exists() && position == mTempPosition) {
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
                ExpRangeSeekBar m_sbRanger = Utils.$(itemView, R.id.mrangseekbar);
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
