package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.cache.GalleryImageFetcher;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownFileListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class TTFAdapter extends BaseAdapter {

    public static final String ACTION_TTF = "action_ttf";

    private ArrayList<TtfInfo> mTTFList = new ArrayList<TtfInfo>();
    private Context mContext;
    private LayoutInflater mInflater;
    private GalleryImageFetcher mFetcher;
    private float mDensity;

    public TTFAdapter(Context c, GalleryImageFetcher _fetcher) {
        mContext = c;
        mDensity = mContext.getResources().getDisplayMetrics().density;
        mTTFList.clear();
        mInflater = LayoutInflater.from(mContext);
        mFetcher = _fetcher;

    }

    public void add(ArrayList<TtfInfo> _list) {
        mTTFList.clear();
        mTTFList.addAll(_list);
        mMaps.clear();
        notifyDataSetChanged();
    }

    public void setCheck(int mPosition) {
        mCheckPosition = mPosition;
        notifyDataSetChanged();
    }

    private int mCheckPosition = 0;

    @Override
    public int getCount() {
        return mTTFList.size();
    }

    @Override
    public TtfInfo getItem(int position) {
        return mTTFList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 还原到初始状态，一个都不选中
     */
    public void ToReset() {
        setCheck(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;
        onDownLoadListener listener;
        if (null == convertView) {
            vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_ttf_layout, null);
            vh.tv = (TextView) convertView.findViewById(R.id.ttf_tv);
            vh.img = (ImageView) convertView.findViewById(R.id.ttf_img);
            vh.ivState = (ImageView) convertView.findViewById(R.id.ttf_state);
            vh.progressBar = (CircleProgressBarView) convertView
                    .findViewById(R.id.ttf_pbar);
            listener = new onDownLoadListener();
            vh.ivState.setOnClickListener(listener);
            vh.ivState.setTag(listener);
            convertView.setTag(vh);

        } else {
            vh = (ViewHolder) convertView.getTag();
            listener = (onDownLoadListener) vh.ivState.getTag();
        }

        TtfInfo info = getItem(position);
        if (null != info) {
            if (position == 0) {
                vh.tv.setVisibility(View.VISIBLE);
                vh.tv.setText(info.local_path);
                vh.img.setVisibility(View.GONE);
            } else {
                vh.tv.setText("");
                vh.tv.setVisibility(View.GONE);
                vh.img.setVisibility(View.VISIBLE);
            }

            if (mDensity > 2.01) {
                mFetcher.loadImage(PathUtils.getRdTtfPath() + "/icon/icon_2_"
                        + info.code + "_n_@3x.png", vh.img);
            } else {
                mFetcher.loadImage(PathUtils.getRdTtfPath() + "/icon/icon_2_"
                        + info.code + "_n_@2x.png", vh.img);
            }

            vh.tv.setTextColor(mContext.getResources().getColor(
                    R.color.transparent_white));

            if (info.isdownloaded() || position == 0) {
                vh.progressBar.setVisibility(View.GONE);
                vh.ivState.setVisibility(View.GONE);

                if (mCheckPosition == position) {
                    if (position == 0) {
                        vh.tv.setTextColor(mContext.getResources().getColor(
                                R.color.main_orange));

                    } else {
                        try {
                            FileInputStream fis;

                            if (mDensity > 2.01) {
                                fis = new FileInputStream(
                                        info.local_path.substring(0,
                                                info.local_path
                                                        .lastIndexOf("/"))
                                                + "/selected/icon_2_"
                                                + info.code + "_s_@3x.png");
                            } else {
                                fis = new FileInputStream(
                                        info.local_path.substring(0,
                                                info.local_path
                                                        .lastIndexOf("/"))
                                                + "/selected/icon_2_"
                                                + info.code + "_s_@2x.png");
                            }
                            Bitmap bm = BitmapFactory.decodeStream(fis);
                            vh.img.setImageBitmap(bm);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                boolean isloading = mMaps.containsKey((long) info.id);
                if (isloading) {
                    vh.ivState.setVisibility(View.GONE);
                    vh.progressBar.setVisibility(View.VISIBLE);
                    vh.progressBar.setProgress(mMaps.get((long) info.id).getProgress());
                } else {
                    vh.ivState.setVisibility(View.VISIBLE);
                    vh.progressBar.setVisibility(View.GONE);
                }
                listener.setP(position, vh.ivState, vh.progressBar);
            }

        }

        return convertView;
    }

    private GridView listview;

    public void setListview(GridView _listview) {
        listview = _listview;
        notifyDataSetChanged();
    }

    private View getChildAt(int position) {

        try {
            return listview.getChildAt(position
                    - listview.getFirstVisiblePosition());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private class onDownLoadListener implements OnClickListener {

        private int position;
        private ImageView ivState;
        private CircleProgressBarView progressBar;

        public void setP(int _p, ImageView ivState, CircleProgressBarView pb) {
            position = _p;
            this.ivState = ivState;
            progressBar = pb;
        }

        @Override
        public void onClick(View v) {

            onDown(position, ivState, progressBar);

        }

    }

    private HashMap<Long, LineProgress> mMaps = new HashMap<Long, LineProgress>();
    private ArrayList<Integer> mArrPosition = new ArrayList<Integer>();

    public static final String TTF_ITEM = "ttf_item";
    public static final String TTF_ITEM_POSITION = "ttf_item_position";

    /**
     * 执行下载
     *
     */
    public void onDown(final int position, ImageView ivState, CircleProgressBarView progressBar) {
        if (mMaps.size() > 3) { // 最多同时下载3个
            return;
        }
        if (CoreUtils.checkNetworkInfo(ivState.getContext()) == CoreUtils.UNCONNECTED) {
            com.rd.veuisdk.utils.Utils.autoToastNomal(ivState.getContext(),
                    R.string.please_check_network);
            return;
        }
        for (int p : mArrPosition) {
            if (p == position) {
                return;
            }
        }

        final TtfInfo info = getItem(position);
        mArrPosition.add(position);
        DownLoadUtils utils = new DownLoadUtils(info.id, info.url, "zipp");
        utils.setConfig(0, 10, 500);
        utils.DownFile(new IDownFileListener() {

            @Override
            public void onProgress(long mid, int progress) {
                // Log.e("onprogres...." + mid, progress + "......");
                LineProgress line = mMaps.get(mid);
                if (null != line) {
                    line.setProgress(progress);
                    mMaps.put(mid, line);
                    mhandler.sendMessage(mhandler.obtainMessage(PROGRESS, mid));
                }
            }

            @Override
            public void Canceled(long mid) {
                // Log.e("Canceled....", mid + ".........");
                mMaps.remove(mid);
                mhandler.sendMessage(mhandler.obtainMessage(CANCEL,
                        String.valueOf(mid)));

            }

            @Override
            public void Finished(long mid, String localPath) {
                mCheckPosition = position;
                mArrPosition.remove((Object) position);
                File fold = new File(localPath);
                File zip = new File(fold.getParent() + "/" + info.code
                        + ".zipp");
                fold.renameTo(zip);
                String dirpath = null;
                if (zip.exists()) { // 解压
                    try {
                        dirpath = FileUtils.unzip(zip.getAbsolutePath(),
                                PathUtils.getRdTtfPath());
                        if (!TextUtils.isEmpty(dirpath)) {
                            zip.delete(); // 删除原mv的临时文件
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Log.e("onfisish....", localPath + "......." +
                // info.toString());
                // 更新单个

                info.local_path = dirpath.substring(0,
                        dirpath.lastIndexOf("selected/"))
                        + info.code + ".ttf";
                long re = TTFData.getInstance().replace(info);
                // Log.e("onfisish....", re + "....." + localPath);
                mMaps.remove(mid);

                mhandler.sendMessage(mhandler.obtainMessage(FINISHED,
                        String.valueOf(mid)));
                Intent bsen = new Intent(ACTION_TTF);
                bsen.putExtra(TTF_ITEM, info.local_path);
                bsen.putExtra(TTF_ITEM_POSITION, position);
                mContext.sendBroadcast(bsen);

            }
        });
        mMaps.put((long) info.id, new LineProgress(position, 0));
        ivState.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        progressBar.setProgress(0);

    }

    private final int PROGRESS = 2, FINISHED = 3, CANCEL = 4;
    private long lastReflesh = System.currentTimeMillis();
    private Handler mhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    if (System.currentTimeMillis() - lastReflesh > 1500) { // 减少刷新频率

                        LineProgress temp = mMaps.get(Long.parseLong(msg.obj
                                .toString()));

                        // Log.e("progress...1111111.", msg.arg1 + "..........."
                        // + msg.arg2 + "////" + temp.toString());
                        View child = getChildAt(temp.getPosition());
                        if (null != child) {
                            View ivState = child.findViewById(R.id.ttf_state);
                            ivState.setVisibility(View.GONE);
                            CircleProgressBarView progressBar = (CircleProgressBarView) child
                                    .findViewById(R.id.ttf_pbar);
                            progressBar.setProgress(temp.getProgress());
                            progressBar.setVisibility(View.VISIBLE);
                            // Log.e("progress....", temp.toString());

                        } else {

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

                default:
                    break;
            }
        }

        ;
    };

    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != mMaps && mMaps.size() > 0) {
            mMaps.clear();
            DownLoadUtils.forceCancelAll();
        }
    }

    private class ViewHolder {
        private ImageView img, ivState;
        private TextView tv;
        private CircleProgressBarView progressBar;
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
