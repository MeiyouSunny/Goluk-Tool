package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownListener;
import com.rd.lib.utils.CoreUtils;
import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.database.TTFData;
import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.net.TTFUtils;
import com.rd.veuisdk.ui.CircleProgressBarView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 字体
 */
public class TTFAdapter extends BaseAdapter {

    public static final String ACTION_TTF = "action_ttf";
    private ArrayList<TtfInfo> mTTFList = new ArrayList<TtfInfo>();
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean isHDIcon = false;
    private boolean bCustomApi = false; //是否是自定义的网络字体
    private int textColorN;
    private int textColorP;

    /**
     * @param c
     * @param customApi 是否自定义字体网络接口
     */
    public TTFAdapter(Context c, boolean customApi) {
        bCustomApi = customApi;
        mContext = c;
        mTTFList.clear();
        mInflater = LayoutInflater.from(mContext);
        textColorN = mContext.getResources().getColor(
                R.color.transparent_white);
        textColorP = mContext.getResources().getColor(
                R.color.main_orange);
        isHDIcon = TTFUtils.isHDIcon(mContext);
    }

    public void add(ArrayList<TtfInfo> _list) {
        mTTFList.clear();
        mTTFList.addAll(_list);
        mSparseArray.clear();
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
            vh.tv = Utils.$(convertView, R.id.ttf_tv);
            vh.cover = Utils.$(convertView, R.id.ttf_img);
            vh.ivState = Utils.$(convertView, R.id.ttf_state);
            vh.progressBar = Utils.$(convertView, R.id.ttf_pbar);
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
                vh.cover.setVisibility(View.GONE);
            } else {
                vh.tv.setText("");
                vh.tv.setVisibility(View.GONE);
                vh.cover.setVisibility(View.VISIBLE);

                //字体icon
                if (bCustomApi) {
                    //435, 84  原图，*0.65x    0.4X 174， 34    0.45X  195,38
                    loadCover(vh.cover, info.icon);
                } else {
                    if (FileUtils.isExist(info.icon)) {
                        loadCover(vh.cover, info.icon);
                    }
                }
            }
            vh.tv.setTextColor(textColorN);
            vh.ivState.setVisibility(View.GONE);
            if (info.isdownloaded() || position == 0) {
                vh.progressBar.setVisibility(View.GONE);
                if (mCheckPosition == position) {
                    if (position == 0) {
                        if (!bCustomApi) {
                            vh.tv.setTextColor(textColorP);
                        } else {
                            vh.ivState.setVisibility(View.VISIBLE);
                            vh.ivState.setImageResource(R.drawable.public_menu_sure);
                        }
                    } else {
                        if (!bCustomApi) {
                            getFixIcon(info.local_path, info.code, vh.cover);
                        } else {
                            //网络字体已下载的被选中状态
                            vh.ivState.setVisibility(View.VISIBLE);
                            vh.ivState.setImageResource(R.drawable.public_menu_sure);
                        }
                    }
                }
            } else {
                LineProgress lineProgress = mSparseArray.get(info.id);
                if (null != lineProgress) {
                    vh.ivState.setVisibility(View.GONE);
                    vh.progressBar.setVisibility(View.VISIBLE);
                    vh.progressBar.setProgress(lineProgress.getProgress());
                } else {
                    vh.ivState.setImageResource(R.drawable.down_btn);
                    vh.ivState.setVisibility(View.VISIBLE);
                    vh.progressBar.setVisibility(View.GONE);
                }
                listener.setP(position, vh.ivState, vh.progressBar);
            }

        }

        return convertView;
    }

    /***
     * 选中状态下的图片
     * @param local_path
     * @param code
     * @param img
     */
    @Deprecated
    private void getFixIcon(String local_path, String code, SimpleDraweeView img) {
        String path;
        if (isHDIcon) {
            path = local_path.substring(0, local_path.lastIndexOf("/")) + "/selected/icon_2_" + code + "_s_@3x.png";
        } else {
            path = local_path.substring(0, local_path.lastIndexOf("/")) + "/selected/icon_2_" + code + "_s_@2x.png";
        }
        loadCover(img, path);
    }

    private void loadCover(SimpleDraweeView img, String path) {
        SimpleDraweeViewUtils.setCover(img, path, false, 348, 67);
    }

    private GridView listview;

    public void setListview(GridView _listview) {
        listview = _listview;
        notifyDataSetChanged();
    }

    private View getChildAt(int position) {
        try {
            return listview.getChildAt(position - listview.getFirstVisiblePosition());
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

    private int mItemHeight = 0;

    public void setItemHeight(int height) {
        mItemHeight = height;
    }

    private SparseArray<LineProgress> mSparseArray = new SparseArray<LineProgress>();
    private ArrayList<Integer> mArrPosition = new ArrayList<Integer>();

    public static final String TTF_ITEM = "ttf_item";
    public static final String TTF_ITEM_POSITION = "ttf_item_position";
    private String TAG = "TTFAdapter";

    /**
     * 执行下载
     */

    public void onDown(final int position, ImageView ivState, CircleProgressBarView progressBar) {
        if (mSparseArray.size() > 3) { // 最多同时下载3个
            return;
        }
        if (null != mContext && CoreUtils.checkNetworkInfo(mContext) == CoreUtils.UNCONNECTED) {
            Utils.autoToastNomal(mContext, R.string.please_check_network);
            return;
        }
        for (Integer p : mArrPosition) {
            if (p == position) {
                return;
            }
        }

        final TtfInfo info = getItem(position);
        if (info.isdownloaded()) {
            //防止已下载再次点击
            return;
        }
        mArrPosition.add(position);
        String localPath = PathUtils.getTempFileNameForSdcard(PathUtils.TEMP + "_" + info.code
                , bCustomApi ? "zip" : "zipp");
        DownLoadUtils utils = new DownLoadUtils(mContext, info.id, info.url, localPath);
        utils.setConfig(0, 10, 500);
        utils.DownFile(new IDownListener() {

            @Override
            public void onFailed(long mid, int i) {
                Log.e(TAG, "onFailed: " + mid + ">>" + i);
                mSparseArray.remove((int) mid);
                notifyDataSetChanged();
            }

            @Override
            public void onProgress(long mid, int progress) {
                LogUtil.i(TAG, "onProgress:" + mid + " >" + progress);
                int key = (int) mid;
                LineProgress line = mSparseArray.get(key);
                if (null != line) {
                    line.setProgress(progress);
                    View child = getChildAt(line.getPosition());
                    if (null != child) {
                        View ivState = Utils.$(child, R.id.ttf_state);
                        if (null != ivState) {
                            ivState.setVisibility(View.GONE);
                        }
                        CircleProgressBarView progressBar = Utils.$(child, R.id.ttf_pbar);
                        if (null != progressBar) {
                            progressBar.setProgress(progress);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                        line.setProgress(progress);
                    }

                }
            }

            @Override
            public void Canceled(long mid) {
                Log.e(TAG, "Canceled: " + mid);
                mSparseArray.remove((int) mid);
                notifyDataSetChanged();
            }

            @Override
            public void Finished(long mid, String localPath) {
                LogUtil.i(TAG, "Finished:" + mid);
                mCheckPosition = position;
                mArrPosition.remove((Object) position);
                try {
                    // 解压
                    String dirpath = FileUtils.unzip(localPath, PathUtils.getRdTtfPath());
                    //字体路径
                    info.local_path = new File(dirpath, info.code + ".ttf").getAbsolutePath();
                    // 更新单个
                    TTFData.getInstance().replace(info);
                    notifyDataSetChanged();
                    if (null != mContext) {
                        Intent intent = new Intent(ACTION_TTF);
                        intent.putExtra(TTF_ITEM, info.local_path);
                        intent.putExtra(TTF_ITEM_POSITION, position);
                        mContext.sendBroadcast(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mSparseArray.remove((int) mid);
            }
        });
        mSparseArray.put((int) info.id, new LineProgress(position, 0));
        if (null != ivState) {
            ivState.setVisibility(View.GONE);
        }
        if (null != progressBar) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
        }
    }


    /**
     * 退出全部下载
     */
    public void onDestory() {
        if (null != mSparseArray && mSparseArray.size() > 0) {
            mSparseArray.clear();
            DownLoadUtils.forceCancelAll();
        }
    }

    private class ViewHolder {
        private ImageView ivState;
        private SimpleDraweeView cover;
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
