package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.rd.cache.GalleryImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.SpecialData;
import com.rd.veuisdk.database.SubData;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.ExtListItemStyle;
import com.rd.veuisdk.utils.CacheUtils;
import com.rd.veuisdk.utils.CommonStyleUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.WebpUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class SpecialStyleAdapter extends BaseAdapter {
    public static final String ACTION_SPECIAL = "special_downLoaded";
    public static final String ACTION_SUB = "sub_downLoaded";
    public static final String DOWNLOADED_ITEM_POSITION = "downloaded_item_position";
    public static final String ACTION_SHOW_RIGHT = "action_show_right_btn";
    private Context mContext;
    private LayoutInflater mInflater;
    private String TAG = SpecialStyleAdapter.class.getName();
    private ArrayList<StyleInfo> mArrStyleInfo = new ArrayList<StyleInfo>();

    public void addStyles(ArrayList<StyleInfo> _list) {
        mCheckedId = 0;
        mArrStyleInfo.clear();
        int len = _list.size();
        for (int i = 0; i < len; i++) {
            StyleInfo si = _list.get(i);
            mArrStyleInfo.add(si);
        }
        mHandler.sendEmptyMessage(PREPARE_ED);

    }

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

    private GalleryImageFetcher mFetcher;
    private boolean mIsSub = false;

    public SpecialStyleAdapter(Context context, boolean _isSub) {
        mCheckedId = 0;
        mContext = context;
        mIsSub = _isSub;
        ImageCacheParams cacheParams = new ImageCacheParams(mContext,
                CacheUtils.STYLE_ANIM_CACHE_DIR);
        // 缓冲占用系统内存的10%
        cacheParams.setMemCacheSizePercent(0.05f);
        cacheParams.setFormat(CompressFormat.PNG);
        mFetcher = new GalleryImageFetcher(mContext, CacheUtils.HEAD_USER_WIDTH,
                CacheUtils.HEAD_USER_HEIGHT);
        mFetcher.addImageCache(mContext, cacheParams);
        mFetcher.setImageFadeIn(true);
        mInflater = LayoutInflater.from(mContext);

    }

    /**
     * 刷新图标，清空缓存
     */
    public void updateIcon() {
        notifyDataSetChanged();
    }


    public void onResume() {
        if (null != mFetcher) {
            mFetcher.setExitTasksEarly(false);
        }
    }

    public void onPasue() {
        if (null != mFetcher) {
            mFetcher.setExitTasksEarly(true);
            mFetcher.flushCache();
        }
    }

    @Override
    public int getCount() {
        return mArrStyleInfo.size();
    }

    @Override
    public StyleInfo getItem(int position) {
        if (position >= mArrStyleInfo.size()) {
            Log.e(TAG, "getItem()->info==null");
            return null;
        }
        return mArrStyleInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setCheckItem(int nposition) {
        // Log.e("setCheckItem", this.toString() + "..." + nposition + "..."
        // + mCheckedId);
        if (nposition != mCheckedId) {
            mCheckedId = nposition;
            notifyDataSetChanged();
        }

    }

    public int getCheckId() {
        // Log.e("getCheckId", this.toString() + "..." + mCheckedId);
        return mCheckedId;
    }

    private int mCheckedId = 0;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;
        onDownLoadListener listener;
        if (null == convertView) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.style_item_layout, null);
            vh.image = (ExtListItemStyle) convertView
                    .findViewById(R.id.style_item_src);
            vh.state = (ImageView) convertView.findViewById(R.id.ttf_state);
            vh.pbar = (CircleProgressBarView) convertView
                    .findViewById(R.id.ttf_pbar);
            vh.src = (ImageView) convertView.findViewById(R.id.style_src);
            listener = new onDownLoadListener();
            vh.state.setOnClickListener(listener);
            vh.state.setTag(listener);
            convertView.setTag(vh);

        } else {
            vh = (ViewHolder) convertView.getTag();
            listener = (onDownLoadListener) vh.state.getTag();
        }
        vh.image.setSelected(mCheckedId == position);

        StyleInfo info = getItem(position);
        if (null != info) {

            if (null != vh.src) {
                String path;
                if (mIsSub) {
                    path = PathUtils.getRdSubPath() + "/icon/" + info.code
                            + ".png";
                } else {
                    path = PathUtils.getRdSpecialPath() + "/icon/" + info.code
                            + ".png";
                }
                File f = new File(path);
                if (null != f && f.exists()) {  //防止图片缓存影响重新加载
                    mFetcher.loadImage(path, vh.src);
                }
            }

            if (info.isdownloaded) {
                vh.pbar.setVisibility(View.GONE);
                vh.state.setVisibility(View.GONE);
            } else {
                boolean isloading = maps.containsKey(info.pid);
                if (isloading) {
                    vh.state.setVisibility(View.GONE);
                    vh.pbar.setVisibility(View.VISIBLE);
                    vh.pbar.setProgress(maps.get(info.pid).getProgress());
                } else {
                    vh.state.setVisibility(View.VISIBLE);
                    vh.pbar.setVisibility(View.GONE);
                }
                listener.setP(position, vh.state, vh.pbar);
            }
        }

//		Log.e("getview", position + "..." + mCheckedId + "...." + info.pid);
        return convertView;
    }

    private class ViewHolder {
        private ImageView state, src;
        private ExtListItemStyle image;
        private CircleProgressBarView pbar;
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

    private HashMap<Long, LineProgress> maps = new HashMap<Long, LineProgress>();
    private final String _STREXTENSION = ".zipp";

    /**
     * 执行下载
     */
    public void onDown(final int p, ImageView state, CircleProgressBarView pbar) {

        if (maps.size() < 3) { // 最多同时下载3个
            Context context = state.getContext();
            if (CoreUtils.checkNetworkInfo(context.getApplicationContext()) == CoreUtils.UNCONNECTED) {
                com.rd.veuisdk.utils.Utils.autoToastNomal(context,
                        R.string.please_check_network);
            } else {

                final StyleInfo info = getItem(p);

                if (null != info && !maps.containsKey((long) info.pid)) {

                    if (CoreUtils.checkNetworkInfo(context
                            .getApplicationContext()) == CoreUtils.UNCONNECTED) {

                        com.rd.veuisdk.utils.Utils.autoToastNomal(context,
                                R.string.please_check_network);

                    } else {
                        DownLoadUtils utils = new DownLoadUtils(info.pid,
                                info.caption, _STREXTENSION);
                        utils.setMethod(false);
                        utils.setConfig(0, 20, 500);
                        utils.DownFile(new IDownFileListener() {

                            @Override
                            public void onProgress(long mid, int progress) {
                                LineProgress line = maps.get(mid);
                                if (null != line) {
                                    line.setProgress(progress);
                                    maps.put(mid, line);
                                    mHandler.obtainMessage(PROGRESS, mid)
                                            .sendToTarget();
                                }
                            }

                            @Override
                            public void Canceled(long mid) {
                                // Log.e("Canceled....", mid + ".........");
                                maps.remove(mid);
                                mHandler.obtainMessage(CANCEL,
                                        String.valueOf(mid)).sendToTarget();
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
                    }
                }
            }
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
    private void onItemDownloaded(StyleInfo info, int p, long mid,
                                  String localPath) {
        File fold = new File(localPath);
        File zip = new File(fold.getParent() + "/" + info.code + _STREXTENSION);
        fold.renameTo(zip);
        if (zip.exists()) { // 解压
            try {
                String dirpath = null;

                if (mIsSub) {
                    dirpath = FileUtils.unzip(zip.getAbsolutePath(),
                            PathUtils.getRdSubPath());
                } else {
                    dirpath = FileUtils.unzip(zip.getAbsolutePath(),
                            PathUtils.getRdSpecialPath());
                }

                if (!TextUtils.isEmpty(dirpath)) {
                    info.isdownloaded = true;
                    if (mIsSub) {
                        if (new File(dirpath + info.code + "0.webp").exists()) {
                            WebpUtils.locWebpSaveToLocPng(dirpath + info.code
                                    + "0.webp", dirpath + info.code + "0.png");
                        }
                    } else {
                        File file = new File(dirpath);
                        File[] subFile = file.listFiles();

                        for (int i = 0; i < subFile.length; i++) {
                            // 判断是否为文件夹
                            if (!subFile[i].isDirectory()) {
                                String filename = subFile[i].getName();
                                if (filename.trim().toLowerCase()
                                        .endsWith(".webp")) {
                                    WebpUtils
                                            .locWebpSaveToLocPng(
                                                    dirpath + filename,
                                                    dirpath
                                                            + filename
                                                            .substring(
                                                                    0,
                                                                    filename.lastIndexOf("."))
                                                            + ".png");
                                }
                            }
                        }
                    }
                    File config = new File(dirpath,
                            CommonStyleUtils.CONFIG_JSON);
                    CommonStyleUtils.getConfig(config, info);

                    info.mlocalpath = config.getParent();
                    zip.delete(); // 删除原mv的临时文件
                    Intent bsen;

                    if (mIsSub) {
                        bsen = new Intent(ACTION_SUB);
                        SubData.getInstance().replace(info);
                    } else {
                        bsen = new Intent(ACTION_SPECIAL);
                        SpecialData.getInstance().replace(info);
                    }
                    info.isdownloaded = true;
                    bsen.putExtra(SpecialStyleAdapter.DOWNLOADED_ITEM_POSITION,
                            p);
                    maps.remove(mid);
                    mContext.sendBroadcast(bsen);
                    mHandler.obtainMessage(FINISHED, String.valueOf(mid))
                            .sendToTarget();

                }
            } catch (IOException e) {
                e.printStackTrace();
                maps.remove(mid);
                mHandler.obtainMessage(CANCEL, String.valueOf(mid))
                        .sendToTarget();
            }

        }
        if (maps.size() == 0) {
            sendLoading(mContext, false);
        }
    }

    private GridView mListView;

    public void setListview(GridView _listview) {
        mListView = _listview;
    }

    private View getChildAt(int position) {

        try {
            return mListView.getChildAt(position
                    - mListView.getFirstVisiblePosition());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static final int PROGRESS = 2, FINISHED = 3, CANCEL = 4,
            PREPARE_ED = 5;
    ;
    private long lastReflesh = System.currentTimeMillis();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    if (System.currentTimeMillis() - lastReflesh > 1000) { // 减少刷新频率

                        LineProgress temp = maps.get(Long.parseLong(msg.obj
                                .toString()));
                        if (null != temp) {

                            View child = getChildAt(temp.getPosition());
                            if (null != child) {
                                View state = child.findViewById(R.id.ttf_state);
                                state.setVisibility(View.GONE);
                                CircleProgressBarView pbar = (CircleProgressBarView) child
                                        .findViewById(R.id.ttf_pbar);
                                pbar.setProgress(temp.getProgress());
                                pbar.setVisibility(View.VISIBLE);
                            }
                        }
                        lastReflesh = System.currentTimeMillis();
                    }
                    break;
                case FINISHED:
                    notifyDataSetChanged();
                    break;
                case CANCEL:
                    notifyDataSetChanged();
                    break;
                case PREPARE_ED:
                    onResume();
                    notifyDataSetChanged();

                default:
                    break;
            }
        }
    };

    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != mFetcher) {
            mFetcher.cleanUpCache();
            mFetcher = null;
        }
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
        sendLoading(mContext, false);
    }

    /**
     * 是否有下载
     *
     * @param context
     * @param isloading
     */
    private void sendLoading(Context context, boolean isloading) {
        Intent in = new Intent(ACTION_SHOW_RIGHT);
        in.putExtra(ITEM_IS_DOWNLOADING, isloading);
        context.sendBroadcast(in);
    }

    public static final String ITEM_IS_DOWNLOADING = "item_is_downloading";

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

        @Override
        public String toString() {
            return "LineProgress [position=" + position + ", progress="
                    + progress + "]";
        }

        public LineProgress(int position, int progress) {

            this.position = position;
            this.progress = progress;

        }

    }

}
