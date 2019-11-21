package com.rd.veuisdk.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.vecore.VirtualVideo;
import com.rd.vecore.models.MediaObject;
import com.rd.vecore.models.MediaType;
import com.rd.vecore.models.Scene;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.VideoOb;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.BitmapUtils;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 已选择媒体Adapter
 */
public class MediaCheckedAdapter extends RecyclerView.Adapter<MediaCheckedAdapter.ViewHolder> {

    private List<MediaObject> list;
    private LayoutInflater mLayoutInflater;
    private boolean bHideMediaType = false;
    private boolean bHideMediaDuration = false;

    /**
     * 是否可以相应单个单个媒体的编辑事件
     *
     * @param enableEditClick true 响应编辑事件；false 不响应
     */
    public void setEnableEditClick(boolean enableEditClick) {
        this.enableEditClick = enableEditClick;
    }

    private boolean enableEditClick = true;

    /**
     * 是否隐藏媒体类型
     *
     * @param bHideMediaType
     */
    public void setHideMediaType(boolean bHideMediaType) {
        this.bHideMediaType = bHideMediaType;
    }

    /**
     * 是否隐藏媒体时间
     *
     * @param bHideMediaDuration
     */
    public void setHideMediaDuration(boolean bHideMediaDuration) {
        this.bHideMediaDuration = bHideMediaDuration;
    }


    public interface OnItemClickListener {

        void onDelete(int position);

        void onItemClick(int position);
    }

    /**
     * 设置响应点击
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private OnItemClickListener mOnItemClickListener;

    public MediaCheckedAdapter() {
        list = new ArrayList<>();
    }

    public List<MediaObject> getList() {
        return list;
    }

    /**
     * 添加已选择的媒体
     */
    public void addAll(List<MediaObject> mediaObjects) {
        //暂不做方向上的处理
        list.clear();
        if (null != mediaObjects && mediaObjects.size() > 0) {
            list.addAll(mediaObjects);
        }
        notifyDataSetChanged();
    }

    /**
     * 添加已选择媒体
     */
    public void add(MediaObject tmp) {
        list.add(tmp);
        notifyDataSetChanged();
    }

    /**
     * 更新指定位置的媒体
     *
     * @param index       index
     * @param mediaObject media object to update.
     */
    public void update(int index, MediaObject mediaObject) {
        if (index >= 0 && null != mediaObject) {
            list.set(index, mediaObject);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_media_checked_layout, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        ViewDeleteClickListener viewDeleteClickListener = new ViewDeleteClickListener();
        viewHolder.delete.setOnClickListener(viewDeleteClickListener);
        viewHolder.delete.setTag(viewDeleteClickListener);

        if (enableEditClick) {
            ViewClickListener viewClickListener = new ViewClickListener();
            viewHolder.mImageView.setOnClickListener(viewClickListener);
            viewHolder.mImageView.setTag(viewClickListener);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mPreviewFrameLayout.setAspectRatio(1f);
        if (enableEditClick) {
            ViewClickListener viewClickListener = (ViewClickListener) holder.mImageView.getTag();
            viewClickListener.setPosition(position);
        }
        ViewDeleteClickListener deleteClickListener = (ViewDeleteClickListener) holder.delete.getTag();
        deleteClickListener.setPosition(position);
        holder.tvNum.setText(Integer.toString(position + 1));

        final MediaObject mediaObject = getItem(position);
        if (bHideMediaDuration && bHideMediaType) {
            holder.buttomLayout.setVisibility(View.GONE);
        } else {
            holder.buttomLayout.setVisibility(View.VISIBLE);
            //隐藏媒体时间
            if (bHideMediaDuration) {
                holder.tvDuration.setVisibility(View.GONE);
            } else {
                holder.tvDuration.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(DateTimeUtils.stringForTime(mediaObject.getDuration()));
            }

            //隐藏媒体类型
            if (bHideMediaType) {
                holder.ivType.setVisibility(View.GONE);
            } else {
                holder.ivType.setVisibility(View.VISIBLE);
                if (mediaObject.getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                    holder.ivType.setImageResource(R.drawable.edit_item_video);
                } else {
                    Object tmp = mediaObject.getTag();
                    VideoOb videoOb;
                    if (tmp instanceof VideoOb && (videoOb = (VideoOb) tmp) != null && videoOb.isExtPic == 1) {
                        holder.ivType.setImageResource(R.drawable.edit_item_text);
                    } else {
                        holder.ivType.setImageResource(R.drawable.edit_item_image);
                    }
                }
            }
        }
        String fileName = PathUtils.TEMP + "_cover_"
                + mediaObject.hashCode() + "_" + mediaObject.getAngle() + "_" + mediaObject.getClipRectF() + ".jpg";
        final String dst = new File(new File(PathUtils.getRdTempPath()), fileName).getAbsolutePath();
        if (!FileUtils.isExist(dst)) {
            threadPoolExecutor.execute(new ThreadPoolUtils.ThreadPoolRunnable() {
                @Override
                public void onBackground() {
                    VirtualVideo virtualVideo = new VirtualVideo();
                    Scene scene = VirtualVideo.createScene();
                    scene.addMedia(mediaObject);
                    virtualVideo.addScene(scene);
                    int w = 200;
                    int h = (int) (w / ((mediaObject.getWidth() + 0.0f) / mediaObject.getHeight()));
                    Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    //取0.2秒, 封面时长0.3秒
                    if (virtualVideo.getSnapshot(holder.mImageView.getContext(), 0.2f, bmp)) {
                        BitmapUtils.saveBitmapToFile(bmp, dst, false);
                    }
                    bmp.recycle();
                }

                @Override
                public void onEnd() {
                    if (!FileUtils.isExist(dst)) {
                        SimpleDraweeViewUtils.setCover(holder.mImageView, mediaObject.getMediaPath());
                    } else {
                        SimpleDraweeViewUtils.setCover(holder.mImageView, dst);
                    }
                }
            });
        } else {
            SimpleDraweeViewUtils.setCover(holder.mImageView, dst);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * 选中的视频的数目
     *
     * @return
     */
    public int getVideoCount() {
        int count = 0;
        int len = getItemCount();
        for (int i = 0; i < len; i++) {
            if (getItem(i).getMediaType() == MediaType.MEDIA_VIDEO_TYPE) {
                count++;
            }
        }
        return count;
    }

    public MediaObject getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return list.get(position);
        }
        return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View delete;
        SimpleDraweeView mImageView;

        PreviewFrameLayout mPreviewFrameLayout;
        TextView tvDuration, tvNum;
        ImageView ivType;
        View buttomLayout;

        ViewHolder(View itemView) {
            super(itemView);
            buttomLayout = itemView.findViewById(R.id.buttomLayout);
            ivType = itemView.findViewById(R.id.ivItemType);
            mPreviewFrameLayout = itemView.findViewById(R.id.previewFrame);
            delete = itemView.findViewById(R.id.part_delete);
            tvDuration = itemView.findViewById(R.id.item_duration);
            tvNum = itemView.findViewById(R.id.tv_media_num);
            mImageView = itemView.findViewById(R.id.cover);
        }
    }

    class ViewClickListener implements View.OnClickListener {
        private int position;


        public void setPosition(int p) {
            position = p;
        }

        @Override
        public void onClick(View v) {
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onItemClick(position);
            }
        }
    }

    class ViewDeleteClickListener implements View.OnClickListener {
        private int position;

        public void setPosition(int p) {
            position = p;
        }

        @Override
        public void onClick(View v) {
            try {
                list.remove(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
            notifyDataSetChanged();
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onDelete(position);
            }
        }
    }

    /**
     * 清除操作
     */
    public void purge() {
        threadPoolExecutor.purge();
    }

    private static ThreadPoolExecutor threadPoolExecutor; //独立线程池
    private static final BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue(100);

    static {
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            }
        };
        threadPoolExecutor = new ThreadPoolExecutor(
                1, 1, 2, TimeUnit.SECONDS, blockingQueue, handler);
    }
}
