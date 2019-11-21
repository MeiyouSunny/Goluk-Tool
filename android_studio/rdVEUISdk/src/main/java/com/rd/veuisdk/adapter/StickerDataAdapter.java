package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rd.cache.ImageCache;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.http.MD5;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.StickerData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.ui.CircleProgressBar;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.ExtListItemStyle;
import com.rd.veuisdk.utils.CacheUtils;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.apng.ApngDrawable;
import com.rd.veuisdk.utils.apng.ApngImageLoader;
import com.rd.veuisdk.utils.apng.assist.ApngListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.rd.veuisdk.adapter.StyleAdapter.ACTION_HAS_DOWNLOAD_ING;
import static com.rd.veuisdk.adapter.StyleAdapter.ACTION_SUCCESS_CAPTION;
import static com.rd.veuisdk.adapter.StyleAdapter.ACTION_SUCCESS_SPECIAL;
import static com.rd.veuisdk.adapter.StyleAdapter.DOWNLOADED_ITEM_POSITION;
import static com.rd.veuisdk.adapter.StyleAdapter.ITEM_IS_DOWNLOADING;

public class StickerDataAdapter extends BaseRVAdapter<StickerDataAdapter.StickerDataHolder> {

    private Context mContext;
    private ArrayList<StyleInfo> mArrStyleInfo = new ArrayList<StyleInfo>();
    private boolean mIsSub = false;
    public boolean isCustomApi = false;

    public StickerDataAdapter(Context context, boolean _isSub, boolean isCustomApi) {
        lastCheck = 0;
        this.isCustomApi = isCustomApi;
        mContext = context;
        mIsSub = _isSub;
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(mContext,
                CacheUtils.STYLE_ANIM_CACHE_DIR);
        // 缓冲占用系统内存的10%
        cacheParams.setMemCacheSizePercent(0.05f);
        cacheParams.setFormat(Bitmap.CompressFormat.PNG);
    }

    @Override
    public StickerDataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_data_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new StickerDataHolder(view);
    }

    @Override
    public void onBindViewHolder(final StickerDataHolder holder, int position) {
        onDownLoadListener listener = new onDownLoadListener();
        listener.setP(position, holder.mProgressBarView, holder);
        holder.mIcon.setOnClickListener(listener);
        holder.mBorderView.setSelected(lastCheck == position);
        StyleInfo info = mArrStyleInfo.get(position);
        if (null != info) {
            if (isCustomApi) {
                //网络icon
                holder.mLoad.setVisibility(View.VISIBLE);
                ApngImageLoader.getInstance().displayApng(info.icon, holder.mIcon, new ApngImageLoader.ApngConfig(0, true, false), new ApngListener(){
                    @Override
                    public void onAnimationStart(ApngDrawable apngDrawable){
                        holder.mLoad.setVisibility(View.GONE);
                    }
                });
            } else {
                String path = info.icon;
                File f = new File(path);
                if (null != f && f.exists()) {  //防止图片缓存影响重新加载
                    holder.mLoad.setVisibility(View.VISIBLE);
                    ApngImageLoader.getInstance().displayApng("file://" + f.getAbsolutePath(), holder.mIcon, new ApngImageLoader.ApngConfig(0, true, false), new ApngListener() {
                        @Override
                        public void onAnimationStart(ApngDrawable apngDrawable) {
                            holder.mLoad.setVisibility(View.GONE);
                        }
                    });
                }
            }
            if (info.isdownloaded) {
                holder.mProgressBarView.setVisibility(View.GONE);
            } else {
                boolean isloading = maps.containsKey(info.pid);
                if (isloading) {
                    holder.mProgressBarView.setVisibility(View.VISIBLE);
                    holder.mProgressBarView.setProgress(maps.get(info.pid).getProgress());
                } else {
                    holder.mProgressBarView.setVisibility(View.GONE);
                }
            }
        }
    }

    //局部刷新
    private int PROGRESS = 100;//进度
    private int BACKGROUND = 101;//背景

    @Override
    public void onBindViewHolder(StickerDataHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            int p = (int) payloads.get(0);
            StyleInfo info = mArrStyleInfo.get(position);
            if (p == PROGRESS) {
                boolean isloading = maps.containsKey(info.pid);
                if (isloading) {
                    holder.mProgressBarView.setVisibility(View.VISIBLE);
                    holder.mProgressBarView.setProgress(maps.get(info.pid).getProgress());
                } else {
                    holder.mProgressBarView.setVisibility(View.GONE);
                }
            } else if (p == BACKGROUND) {
                holder.mBorderView.setSelected(lastCheck == position);
            }
        }
    }

    /**
     * 刷新图标，清空缓存
     */
    public void updateIcon() {
        notifyDataSetChanged();
    }

    public void addStyles(ArrayList<StyleInfo> list) {
        lastCheck = 0;
        mArrStyleInfo.clear();
        for (StyleInfo info : list) {
            mArrStyleInfo.add(info);
        }
        notifyDataSetChanged();
    }

    public int getPosition(int styleId) {
        int index = 0;
        int len = getItemCount();
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

    public int getCheckId() {
        return lastCheck;
    }

    public StyleInfo getItem(int position) {
        return mArrStyleInfo.get(position);
    }

    public void setCheckItem(int nposition) {
        if (nposition != lastCheck) {
            int tmp = lastCheck;
            lastCheck = nposition;
            notifyItemChanged(tmp, BACKGROUND);
            notifyItemChanged(lastCheck, BACKGROUND);
        }
    }

    private class onDownLoadListener implements View.OnClickListener {

        private int p;
        private CircleProgressBarView pbar;
        private StickerDataHolder holder;

        public void setP(int _p, CircleProgressBarView pb, StickerDataHolder _holder) {
            p = _p;
            pbar = pb;
            holder = _holder;
        }

        @Override
        public void onClick(View v) {
            if (getItem(p).isdownloaded == false) {
                setCheckItem(p);
                onDown(p, pbar);
            } else {
                ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
                viewClickListener.setPosition(p);
                viewClickListener.onClick(holder.itemView);
            }
        }

    }

    private HashMap<Long, LineProgress> maps = new HashMap<Long, LineProgress>();

    /**
     * 执行下载
     */
    public void onDown(final int p, CircleProgressBarView pbar) {
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
                            notifyItemChanged(p, PROGRESS);
                            //notifyDataSetChanged();
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
                    pbar.setVisibility(View.VISIBLE);
                    pbar.setProgress(0);
                    notifyItemChanged(p, PROGRESS);
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
                    intent.putExtra(DOWNLOADED_ITEM_POSITION, p);
                    mContext.sendBroadcast(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            maps.remove(mid);
            notifyItemChanged(p, PROGRESS);
        }
        if (maps.size() == 0) {
            sendLoading(mContext, false);
        }
    }

    /**
     * 更新下载进度
     */
    private void updateProgress(long key) {
        LineProgress temp = maps.get(key);
        notifyItemChanged(temp.getPosition(), PROGRESS);
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

    @Override
    public int getItemCount() {
        return mArrStyleInfo.size();
    }

    class StickerDataHolder extends RecyclerView.ViewHolder {

        private ExtListItemStyle mBorderView;
        private CircleProgressBarView mProgressBarView;
        private ImageView mIcon;
        private CircleProgressBar mLoad;

        StickerDataHolder(View itemView) {
            super(itemView);
            mBorderView = itemView.findViewById(R.id.item_border);
            mProgressBarView = itemView.findViewById(R.id.ttf_pbar);
            mIcon = itemView.findViewById(R.id.icon);
            mLoad = itemView.findViewById(R.id.tv_loading);

            mLoad.setIndeterminate(true);
        }
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
