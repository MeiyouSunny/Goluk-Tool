package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.ExtListItemStyle;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 样式 (字幕、贴纸)
 */
public class StyleAdapter extends BaseRVAdapter<StyleAdapter.ViewHolder> {
    public static final String ACTION_SUCCESS_SPECIAL = "Sticker_download_success";
    public static final String ACTION_SUCCESS_CAPTION = "Caption_download_success";
    public static final String DOWNLOADED_ITEM_POSITION = "downloaded_item_position";
    public static final String ACTION_HAS_DOWNLOAD_ING = "at least 1 downloading";
    public static final String ITEM_IS_DOWNLOADING = "item_is_downloading";
    private Context mContext;
    private ArrayList<StyleInfo> mArrStyleInfo = new ArrayList<>();
    private String TAG = "StyleAdapter";
    private LayoutInflater mInflater;

    public int getPosition(int styleId) {
        int index = 0;
        int len = getCount();
        StyleInfo temp;
        for (int i = 0; i < len; i++) {
            temp = getItem(i);
            if (temp.pid == styleId) {
                index = i;
                break;
            }
        }
        return index;
    }

    private boolean mIsSub = false;
    public boolean isCustomApi = false;

    /**
     * @param context
     * @param _isSub
     * @param isCustomApi 是否启用自定义的网络接口（特效、字幕）
     * @param list
     */
    public StyleAdapter(Context context, boolean _isSub, boolean isCustomApi, ArrayList<StyleInfo> list) {
        mCheckedId = 0;
        this.isCustomApi = isCustomApi;
        mContext = context;
        mIsSub = _isSub;
        mInflater = LayoutInflater.from(context);

        mArrStyleInfo.clear();
        for (StyleInfo info : list) {
            mArrStyleInfo.add(info);
        }
    }

    /**
     * 刷新图标，清空缓存
     */
    public void updateIcon() {
        notifyDataSetChanged();
    }


    public int getCount() {
        return mArrStyleInfo.size();
    }

    public StyleInfo getItem(int position) {
        return mArrStyleInfo.get(position);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.style_item_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new ViewHolder(view);
    }

    class ViewClickListener extends BaseItemClickListener {

        @Override
        public void onClick(View v) {
            if (lastCheck != position) {
                setCheckItem(position);
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {

        Log.e(TAG, "onBindViewHolder: " + position);
        ViewClickListener viewClickListener = (ViewClickListener) vh.itemView.getTag();
        viewClickListener.setPosition(position);
        onDownLoadListener listener = new onDownLoadListener();
        vh.mState.setOnClickListener(listener);

        vh.mBorderView.setSelected(mCheckedId == position);

        StyleInfo info = getItem(position);
        if (null != info) {
            if (isCustomApi) {
                //网络icon
                SimpleDraweeViewUtils.setCover(vh.mSrc, info.icon);
            } else {
                String path = info.icon;
                File f = new File(path);
                if (null != f && f.exists()) {  //防止图片缓存影响重新加载
                    SimpleDraweeViewUtils.setCover(vh.mSrc, path);
                }
            }
            if (info.isdownloaded) {
                vh.mProgressBarView.setVisibility(View.GONE);
                vh.mState.setVisibility(View.GONE);
            } else {
                boolean isloading = maps.containsKey(info.pid);
                if (isloading) {
                    vh.mState.setVisibility(View.GONE);
                    vh.mProgressBarView.setVisibility(View.VISIBLE);
                    vh.mProgressBarView.setProgress(maps.get(info.pid).getProgress());
                } else {
                    vh.mState.setVisibility(View.VISIBLE);
                    vh.mProgressBarView.setVisibility(View.GONE);
                }
                listener.setP(position, vh.mState, vh.mProgressBarView);
            }
        }


    }


    @Override
    public int getItemCount() {
        return mArrStyleInfo.size();
    }


    public void setCheckItem(int nposition) {
        if (nposition != mCheckedId) {
            mCheckedId = nposition;
            notifyDataSetChanged();
        }

    }

    public int getCheckId() {
        return mCheckedId;
    }

    private int mCheckedId = 0;


    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mState;
        private SimpleDraweeView mSrc;
        private ExtListItemStyle mBorderView;
        private CircleProgressBarView mProgressBarView;

        public ViewHolder(View itemView) {
            super(itemView);
            mBorderView = Utils.$(itemView, R.id.item_border);
            mState = Utils.$(itemView, R.id.ttf_state);
            mProgressBarView = Utils.$(itemView, R.id.ttf_pbar);
            mSrc = Utils.$(itemView, R.id.sdv_src);
        }
    }

    private class onDownLoadListener implements OnClickListener {

        private int p;
        private ImageView state;
        private CircleProgressBarView pbar;

        public void setP(int _p, ImageView state, CircleProgressBarView pb) {
            p = _p;
            this.state = state;
            pbar = pb;
        }

        @Override
        public void onClick(View v) {

            onDown(p, state, pbar);

        }

    }

    private HashMap<Long, LineProgress> maps = new HashMap<>();

    /**
     * 执行下载
     */
    public void onDown(final int p, ImageView state, CircleProgressBarView pbar) {
        if (maps.size() < 2) {
            // 最多同时下载2个
            if (CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
                com.rd.veuisdk.utils.Utils.autoToastNomal(mContext, R.string.please_check_network);
            } else {
                final StyleInfo info = getItem(p);
                if (null != info && !maps.containsKey((long) info.pid)) {
                    String tmpLocal = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP + "_" + MD5.getMD5(info.caption), "zip");
                    DownLoadUtils utils = new DownLoadUtils(mContext, info.pid, info.caption, tmpLocal);
                    utils.setMethod(false);
                    utils.setConfig(0, 20, 500);
                    utils.DownFile(new IDownFileListener() {
                        @Override
                        public void onProgress(long mid, int progress) {
                            LineProgress line = maps.get(mid);
                            if (null != line) {
                                line.setProgress(progress);
                                maps.put(mid, line);
                                updateProgress(mid);
                            }
                        }

                        @Override
                        public void Canceled(long mid) {
                            maps.remove(mid);
                            notifyDataSetChanged();
                            if (maps.size() == 0) {
                                sendLoading(mContext, false);
                            }
                        }

                        @Override
                        public void Finished(long mid, String localPath) {
                            onItemDownloaded(info, p, mid, localPath);
                        }
                    });
                    maps.put((long) info.pid, new LineProgress(p, 0));
                    state.setVisibility(View.GONE);
                    pbar.setVisibility(View.VISIBLE);
                    pbar.setProgress(0);
                    notifyDataSetChanged();
                    sendLoading(mContext, true);
                } else {
                    Log.e(TAG, "onDown: isdownloading " + info.pid);
                    com.rd.veuisdk.utils.Utils.autoToastNomal(mContext, R.string.dialog_download_ing);
                }
            }
        } else {
            com.rd.veuisdk.utils.Utils.autoToastNomal(mContext, R.string.download_thread_limit_msg);
        }
    }

    /**
     * 下载完成
     *
     * @param info
     * @param p
     * @param mid
     * @param localPath
     */
    private void onItemDownloaded(StyleInfo info, int p, long mid, String localPath) {
        File zip = new File(localPath);
        if (FileUtils.isExist(zip)) {
            try {
                // 解压
                String dirpath = FileUtils.unzip(zip, new File(mIsSub ? PathUtils.getRdSubPath() : PathUtils.getRdSpecialPath()));
                if (!TextUtils.isEmpty(dirpath)) {
                    File config = new File(dirpath, CommonStyleUtils.CONFIG_JSON);
                    info.mlocalpath = config.getParent();
                    CommonStyleUtils.getConfig(config, info);
                    info.isdownloaded = true;
                    zip.delete(); // 删除原mv的临时文件
                    Intent intent;
                    if (mIsSub) {
                        intent = new Intent(ACTION_SUCCESS_CAPTION);
                        SubData.getInstance().replace(info);
                    } else {
                        intent = new Intent(ACTION_SUCCESS_SPECIAL);
                        StickerData.getInstance().replace(info);
                    }
                    info.isdownloaded = true;
                    intent.putExtra(StyleAdapter.DOWNLOADED_ITEM_POSITION, p);
                    mContext.sendBroadcast(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            maps.remove(mid);
            notifyDataSetChanged();
        }
        if (maps.size() == 0) {
            sendLoading(mContext, false);
        }
    }

    private RecyclerView mListView;

    public void setListview(RecyclerView _listview) {
        mListView = _listview;
    }

    private View getChildAt(int position) {
        try {
            return mListView.getChildAt(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 更新下载进度
     */
    private void updateProgress(long key) {
        LineProgress temp = maps.get(key);
        if (null != temp) {
            View child = getChildAt(temp.getPosition());
            if (null != child) {
                Utils.$(child, R.id.ttf_state).setVisibility(View.GONE);
                CircleProgressBarView pbar = Utils.$(child, R.id.ttf_pbar);
                pbar.setProgress(temp.getProgress());
                pbar.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != mArrStyleInfo) {
            mArrStyleInfo.clear();
        }
        clearDownloading();
    }

    /**
     * 关闭当前清除全部下载
     */
    public void clearDownloading() {
        if (null != maps && maps.size() > 0) {
            maps.clear();
            DownLoadUtils.forceCancelAll();
        }
    }

    /**
     * 是否有下载
     *
     * @param context
     * @param isloading
     */
    private void sendLoading(Context context, boolean isloading) {
        Intent in = new Intent(ACTION_HAS_DOWNLOAD_ING);
        in.putExtra(ITEM_IS_DOWNLOADING, isloading);
        context.sendBroadcast(in);
    }


    /**
     * 下载进度
     *
     * @author JIAN
     */
    private class LineProgress {

        private int position, progress;

        public int getPosition() {
            return position;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public LineProgress(int position, int progress) {
            this.position = position;
            this.progress = progress;
        }

    }

}
