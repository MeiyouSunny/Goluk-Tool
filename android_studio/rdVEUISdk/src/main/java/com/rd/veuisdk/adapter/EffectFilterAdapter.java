package com.rd.veuisdk.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.downfile.utils.DownLoadUtils;
import com.rd.downfile.utils.IDownListener;
import com.rd.http.MD5;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.VirtualVideoView;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.database.EffectData;
import com.rd.veuisdk.model.EffectFilterInfo;
import com.rd.veuisdk.model.type.EffectType;
import com.rd.veuisdk.ui.ExtCircleSimpleDraweeView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.EffectManager;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜特效
 * 181229
 */
public class EffectFilterAdapter extends BaseRVAdapter<EffectFilterAdapter.ViewHolder> {
    private List<EffectFilterInfo> list;
    private String TAG = "EffectFilterAdapter";
    private LayoutInflater mLayoutInflater;
    private int mColorNormal, mColorSelected;

    private ICallBack mCallBack;
    private Context mContext;
    //时间轴支持多个滤镜
    private boolean enableMultiEffect = false;

    /**
     * @param context
     */
    public EffectFilterAdapter(Context context) {
        mContext = context;
        Resources res = context.getResources();
        mColorNormal = res.getColor(R.color.borderline_color);
        mColorSelected = res.getColor(R.color.main_orange);
        list = new ArrayList<>();
        enableRepeatClick = true;
    }


    /**
     * @param enableMulti 是否支持多个特效
     * @param callBack
     */
    public void setCallBack(boolean enableMulti, ICallBack callBack) {
        enableMultiEffect = enableMulti;
        mCallBack = callBack;
    }


    /**
     * @param tmp
     * @param checked
     */
    public void addAll(List<EffectFilterInfo> tmp, int checked) {
        //暂不做方向上的处理
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        if (enableMultiEffect) {
            //只有下载过程中，显示进度需要
            lastCheck = -100;
        } else {
            lastCheck = checked;
        }
        download_progress = 100;
        notifyDataSetChanged();

    }

    private final int TAG_LONG_TOUCH = 1;//滤镜特效 ，长按
    private final int TAG_CLICK = 0; //转场特效，单击

    @Override
    public int getItemViewType(int position) {
        if (position >= 1) {
            EffectFilterInfo filterInfo = getItem(position);
            if (null != filterInfo && (TextUtils.equals(EffectType.DINGGE, filterInfo.getType()) || TextUtils.equals(EffectType.ZHUANCHANG, filterInfo.getType()))) {
                return TAG_CLICK;       // 单击
            }
            return TAG_LONG_TOUCH;    //  长按
        }
        return TAG_CLICK;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateCheckProgress(holder, position);
        }

    }

    //更新进度
    private void updateCheckProgress(ViewHolder holder, int position) {
        if (position == lastCheck) {
            holder.mImageView.setProgress(download_progress);
            holder.mImageView.setChecked(true);
            holder.mText.setTextColor(mColorSelected);
        } else {
            holder.mImageView.setProgress(100);
            holder.mImageView.setChecked(false);
            holder.mText.setTextColor(mColorNormal);
        }
    }

    @Override
    public EffectFilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.effect_list_item, parent, false);
        ViewHolder viewHolder = new EffectFilterAdapter.ViewHolder(view);
        if (enableMultiEffect) {
            if (viewType == TAG_LONG_TOUCH) { //长按
                VHTouchListener mVHTouchListener = new VHTouchListener();
                viewHolder.mImageView.setOnTouchListener(mVHTouchListener);
                viewHolder.mImageView.setTag(mVHTouchListener);
            } else { //单击
                ViewClickListener viewClickListener = new ViewClickListener();
                viewHolder.mImageView.setOnClickListener(viewClickListener);
                viewHolder.mImageView.setTag(viewClickListener);
            }
        } else {
            ViewClickListener viewClickListener = new ViewClickListener();
            viewHolder.mImageView.setOnClickListener(viewClickListener);
            viewHolder.mImageView.setTag(viewClickListener);
        }
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(EffectFilterAdapter.ViewHolder holder, int position) {
        if (enableMultiEffect) {
            holder.mImageView.setChecked(false);
            holder.mText.setTextColor(mColorNormal);
            if (position == 0) {
                SimpleDraweeViewUtils.setCover(holder.mImageView, R.drawable.effect_revoke);
                holder.mText.setText(R.string.effect_filter_revoke);
                ViewClickListener viewClickListener = (ViewClickListener) holder.mImageView.getTag();
                viewClickListener.setPosition(position);
            } else if (position == 1) {
                SimpleDraweeViewUtils.setCover(holder.mImageView, R.drawable.effect_eraser);
                holder.mText.setText(R.string.effect_filter_eraser);
                VHTouchListener touchListener = (VHTouchListener) holder.mImageView.getTag();
                touchListener.setPosition(position);
            } else {
                EffectFilterInfo info = list.get(position - 2);
                if (info.getDuration() > 0 || TextUtils.equals(EffectType.DINGGE, info.getType()) || TextUtils.equals(EffectType.ZHUANCHANG, info.getType())) {
                    //定格、转场 都有duration，响应单击事件即可
                    ViewClickListener viewClickListener = (ViewClickListener) holder.mImageView.getTag();
                    viewClickListener.setPosition(position);
                } else {
                    VHTouchListener touchListener = (VHTouchListener) holder.mImageView.getTag();
                    touchListener.setPosition(position);
                }
                updateCheckProgress(holder, position);
                SimpleDraweeViewUtils.setCover(holder.mImageView, info.getCover());
                holder.mImageView.setChecked(false);
                holder.mText.setTextColor(mColorNormal);
                holder.mText.setText(info.getName());
            }
        } else {
            updateCheckProgress(holder, position);
            ViewClickListener viewClickListener = (ViewClickListener) holder.mImageView.getTag();
            viewClickListener.setPosition(position);
            if (position == 0) {
                SimpleDraweeViewUtils.setCover(holder.mImageView, R.drawable.effect_time_none);
                holder.mText.setText(R.string.none);
            } else {
                EffectFilterInfo info = getItem(position);
                String coverUrl = info.getCover();
                SimpleDraweeViewUtils.setCover(holder.mImageView, coverUrl);
                holder.mText.setText(info.getName());
            }
        }
    }


    private int download_progress = 100;


    private void setDownProgress(int nItemId, int progress) {
        lastCheck = nItemId;
        download_progress = progress;
        notifyItemRangeChanged(nItemId, 1, nItemId + "");
    }

    private void setDownEnd(int nItemId) {
        lastCheck = nItemId;
        download_progress = 100;
        notifyDataSetChanged();
    }

    private void setDownFailed(int nItemId) {
        lastCheck = nItemId;
        download_progress = 0;
        notifyDataSetChanged();
        Utils.autoToastNomal(mContext, R.string.please_check_network);
    }


    @Override
    public int getItemCount() {
        if (enableMultiEffect) {
            return list.size() + 2;
        }
        return list.size() + 1;
    }

    /**
     * @param position
     * @return
     */
    public EffectFilterInfo getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            int index = 0;
            if (enableMultiEffect) {
                index = position - 2;
            } else {
                index = position - 1;
            }
            if (index >= 0) {
                return list.get(index);
            }
        }
        return null;

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;
        ExtCircleSimpleDraweeView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mText = Utils.$(itemView, R.id.tvItemCaption);
            mImageView = Utils.$(itemView, R.id.ivItemImage);
        }
    }

    private SparseArray<DownLoadUtils> mDownloading = null;

    /**
     * @param running
     */
    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void recycle() {
        isRunning = false;
        if (null != mDownloading) {
            mDownloading.clear();
            mDownloading = null;
        }
    }

    private boolean isRunning = false;


    private String getDstPath(String url) {
        return PathUtils.getRdFilterPath() + "/" + MD5.getMD5(url);
    }

    /**
     * 下载
     *
     * @param isBeginTouch 是否响应长按 true 长按；false 单击
     */
    private void down(final View view, final Context context, int itemId, final EffectFilterInfo info, final boolean isBeginTouch) {
        if (null == mDownloading) {
            mDownloading = new SparseArray<>();
        }
        if (null == mDownloading.get(itemId)) {
            /**
             * 支持指定下载文件的存放位置
             */
            final DownLoadUtils download = new DownLoadUtils(context, itemId, info.getFile(), getDstPath(info.getFile()));
            download.DownFile(new IDownListener() {

                @Override
                public void onFailed(long mid, int code) {
                    int key = (int) mid;
                    if (isRunning) {
                        setDownFailed(key);
                        if (code == DownLoadUtils.RESULT_NET_UNCONNECTED) {
                            Utils.autoToastNomal(context, R.string.please_check_network);
                        }
                    }
                    if (null != mDownloading) {
                        mDownloading.remove(key);
                    }
                }

                @Override
                public void onProgress(long mid, int progress) {
                    if (isRunning) {
                        setDownProgress((int) mid, progress);
                    }
                }

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void Finished(final long mid, String localPath) {
                    if (null != mDownloading) {
                        mDownloading.remove((int) mid);
                    }
                    if (isRunning && !((Activity) mContext).isDestroyed()) {
                        try {
                            String dst = com.rd.lib.utils.FileUtils.unzip(localPath, PathUtils.getRdFilterPath());
                            final int index = (int) mid;
                            //设置本地路径
                            final EffectFilterInfo effectFilterInfo = getItem(index);
                            if (null != effectFilterInfo) {
                                effectFilterInfo.setLocalPath(dst);

                                if (TextUtils.equals(effectFilterInfo.getType(), EffectType.DINGGE)) {
                                    //定格 : 异步获取一帧画面，再重新注册 （此画面必须和单个滤镜绑定，用来恢复草稿时需要）
                                    creatThumb(new IThumbCallBack() {
                                        @Override
                                        public void onSuccess(final String path) {
                                            if (!((Activity) mContext).isDestroyed()) {
                                                //注册
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        registerFilter(effectFilterInfo, isBeginTouch, mid, view, index, path, true);
                                                    }
                                                });

                                            }
                                        }
                                    });
                                } else {
                                    registerFilter(effectFilterInfo, isBeginTouch, mid, view, index, null, true);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void Canceled(long mid) {
                    Log.e(TAG, "Canceled: xxx" + mid);
                    if (isRunning) {
                        setDownFailed((int) mid);
                    }
                    if (null != mDownloading) {
                        mDownloading.remove((int) mid);
                    }
                }
            });

            if (isRunning) {
                mDownloading.put(itemId, download);
            }
        } else {
            Log.e(TAG, "download " + info.getFile() + "  is mDownloading");
        }
    }

    private interface IThumbCallBack {
        //创建缩略图成功
        void onSuccess(String path);
    }

    private void creatThumb(final IThumbCallBack callBack) {
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                VirtualVideo virtualVideo = mCallBack.getThumbVirtualVideo();
                float progress = mCallBack.getPlayer().getCurrentPosition();
                float asp = 1f;
                try {
                    asp = mCallBack.getPlayer().getVideoWidth() / (mCallBack.getPlayer().getVideoHeight() + 0.0f);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String path = null;

                int maxWH = SdkEntry.getSdkService().getExportConfig().getVideoMaxWH();
                int w = asp > 1 ? maxWH : (int) (maxWH * asp);
                Bitmap bitmap = Bitmap.createBitmap(w, (int) (w / asp), Bitmap.Config.ARGB_8888);
                if (virtualVideo.getSnapshot(mContext, progress, bitmap, false)) {
                    Bitmap tmp = null;
                    {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(180);
                        matrix.postScale(-1, 1);//翻转
                        matrix.postTranslate(bitmap.getWidth(), 0);
                        try {
                            tmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        } catch (OutOfMemoryError ex) {
                            ex.printStackTrace();
                        }
                    }
                    path = PathUtils.getTempFileNameForSdcard("temp_ding_ge", "jpg");
                    if (tmp != null) {
                        BitmapUtils.saveBitmapToFile(tmp, path);
                    } else {
                        BitmapUtils.saveBitmapToFile(bitmap, path);
                    }
                }
                bitmap.recycle();
                callBack.onSuccess(path);
            }
        });
    }


    /**
     * 下载完成，注册特效滤镜
     *
     * @param effectFilterInfo
     * @param isBeginTouch
     * @param mid
     * @param view
     * @param index
     */
    private void registerFilter(EffectFilterInfo effectFilterInfo, boolean isBeginTouch, long mid, View view, int index, String path, boolean replaceDb) {
        //解析已下载的资源信息（注册）
        EffectManager.getInstance().init(mContext, effectFilterInfo, mCallBack.getPlayer(), path);
        EffectManager.getInstance().add(effectFilterInfo.getFile(), effectFilterInfo.getCoreFilterId());
        if (replaceDb) {
            EffectData.getInstance().replace(effectFilterInfo);
        }
        EffectManager.getInstance().setFilterList(list);

        if (isBeginTouch) {         //更新单个即可
            setDownProgress((int) mid, 100);
            onTouchBeginImp(view, effectFilterInfo);
        } else {
            setDownEnd(index); //单击时才需要notif all
            mCallBack.onItemClick(index);
        }
    }


    class ViewClickListener extends BaseItemClickListener {

        @Override
        public void onClick(View v) {
            if (enableMultiEffect) {
                if (position == 0) { //撤销
                    mCallBack.onRevoke();
                    return;
                }
            }
            if (lastCheck != position || enableRepeatClick) {
                lastCheck = position;
                if (position > 0) {
                    final EffectFilterInfo info = getItem(position);
                    if (FileUtils.isExist(info.getLocalPath())) {
                        if (TextUtils.equals(EffectType.DINGGE, info.getType())) {
                            //定格 （创建缩略图,并注册）
                            creatThumb(new IThumbCallBack() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                                @Override
                                public void onSuccess(final String path) {
                                    if (!((Activity) mContext).isDestroyed()) {
                                        //注册
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                registerFilter(info, false, 0, null, position, path, false);
                                            }
                                        });
                                    }
                                }
                            });

                        } else {
                            notifyDataSetChanged();
                            mCallBack.onItemClick(position);
                        }

                    } else {
                        //不存在下载，文件
                        down(v, v.getContext(), position, info, false);
                    }
                } else {
                    notifyDataSetChanged();
                    mCallBack.onItemClick(position);
                }
            }
        }
    }


    private boolean isDown = false;
    private boolean bTouchBeginEd = false;

    /**
     * 长按 支持多个滤镜
     */
    private void onTouchBeginImp(View view, EffectFilterInfo filterInfo) {
        lastCheck = -100;
        bTouchBeginEd = true;
        if (isDown) {
            mCallBack.onTouchBegin(view, filterInfo);
        }
    }

    private class BeginRunnable implements Runnable {
        public int position = 0;
        private View mView;

        public void setPosition(int position, View view) {
            this.position = position;
            mView = view;
        }

        @Override
        public void run() {
            lastCheck = position;
            if (position >= 2) {
                EffectFilterInfo filterInfo = getItem(position);
                if (FileUtils.isExist(filterInfo.getLocalPath())) {
                    onTouchBeginImp(mView, filterInfo);
                } else {
                    //不存在下载，文件
                    down(mView, mContext, position, filterInfo, true);
                }
            } else if (position == 1) {
                bTouchBeginEd = true;
                mCallBack.onTouchBeginEarser();
            }
        }
    }

    private BeginRunnable mBeginRunnable;
    private final int MSG_UPDATEUI = 650;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATEUI:
                    notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };


    class VHTouchListener implements View.OnTouchListener {

        private int position;

        public void setPosition(int p) {
            position = p;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                if (isDown) {
                    return false;
                }
                bTouchBeginEd = false;
                isDown = true;
                if (null != mBeginRunnable) {
                    mHandler.removeCallbacks(mBeginRunnable);
                } else {
                    mBeginRunnable = new BeginRunnable();
                }
                if (position >= 1) {
                    //包含橡皮檫
                    mBeginRunnable.setPosition(position, v);
                    mHandler.postDelayed(mBeginRunnable, ViewConfiguration.getLongPressTimeout());
                }
                return true;
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                isDown = false;
                lastCheck = -100;
                if (bTouchBeginEd) {
                    bTouchBeginEd = false;
                    if (position >= 2) {
                        mCallBack.onTouchEnd(v);
                    } else if (position == 1) {
                        mCallBack.onTouchEndEarser();
                    } else {
                        mCallBack.onRevoke();
                    }
                } else {
                    if (null != mBeginRunnable) {
                        mHandler.removeCallbacks(mBeginRunnable);
                    }
                }
                mHandler.sendEmptyMessage(MSG_UPDATEUI);
            }
            return false;
        }
    }


    public interface ICallBack {


        VirtualVideoView getPlayer();


        /**
         * 生成一张封面 （部分定格（特效滤镜）需要，时间以当前添加位置为准（即播放器进度））
         */
        VirtualVideo getThumbVirtualVideo();

        /**
         * 新增单个特效
         */
        void onItemClick(int position);


        /**
         * 长按 新增
         */
        void onTouchBegin(View view, EffectFilterInfo filterInfo);

        /**
         * 结束长按
         */
        void onTouchEnd(View view);

        /**
         * 开始擦除
         */
        void onTouchBeginEarser();

        /**
         * 停止擦除
         */
        void onTouchEndEarser();

        /**
         * 撤销
         */
        void onRevoke();


    }


}
