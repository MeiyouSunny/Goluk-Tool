package com.rd.veuisdk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.gallery.IVideo;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图库-视频
 */
public class GalleryAdapter extends BaseRVAdapter<GalleryAdapter.ViewHolder> {
    private List<ImageItem> list;
    private String TAG = "GalleryAdapter";
    private LayoutInflater mLayoutInflater;
    private int lastCheck = -1;

    /**
     * @param context
     */
    public GalleryAdapter(Context context) {
        list = new ArrayList<>();
        enableRepeatClick = true;
        mLayoutInflater = LayoutInflater.from(context);
    }


    /**
     * @param tmp
     */
    public void addAll(List<ImageItem> tmp) {
        addAll(false, tmp);
    }

    /**
     * @param hasOtherGallery
     * @param tmp
     */
    public void addAll(boolean hasOtherGallery, List<ImageItem> tmp) {
        //暂不做方向上的处理
        list.clear();
        if (hasOtherGallery) {
            list.add(null);
        }
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        notifyDataSetChanged();

    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_gallery_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new GalleryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);

        ImageItem item = getItem(position);

        if (null != item) {
            holder.tvOtherGallery.setVisibility(View.GONE);
            holder.mThumbNailLayout.setVisibility(View.VISIBLE);
            if (item.image.isValid()) {

                SimpleDraweeViewUtils.setCover(holder.thumbnail, item.image.getDataPath());


            } else {
                if (item.image instanceof IVideo) {
                    SimpleDraweeViewUtils.setCover(holder.thumbnail, R.drawable.gallery_video_failed);
                } else {
                    SimpleDraweeViewUtils.setCover(holder.thumbnail, R.drawable.gallery_image_failed);
                }
            }
            if (item.image instanceof IVideo) {
                holder.tvDuration.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(DateTimeUtils
                        .stringForTime((int) ((IVideo) item.image)
                                .getDuration()));
            } else {
                holder.tvDuration.setVisibility(View.GONE);
                holder.tvDuration.setText(null);
            }
        } else {
            holder.tvOtherGallery.setVisibility(View.VISIBLE);
            holder.mThumbNailLayout.setVisibility(View.GONE);
        }

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * @param position
     * @return
     */
    private ImageItem getItem(int position) {
        return list.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView thumbnail;
        TextView tvDuration;
        PreviewFrameLayout pflConvertView;
        RelativeLayout mThumbNailLayout;
        TextView tvOtherGallery;

        ViewHolder(View itemView) {
            super(itemView);
            pflConvertView = (PreviewFrameLayout) itemView.findViewById(R.id.itemGalleryPreview);
            pflConvertView.setAspectRatio(1f);
            thumbnail = (SimpleDraweeView) itemView.findViewById(R.id.ivPhotoListThumbnail);
            mThumbNailLayout = (RelativeLayout) itemView.findViewById(R.id.frameThumbnailLayout);
            tvOtherGallery = (TextView) itemView.findViewById(R.id.tvOtherGallery);
            tvDuration = (TextView) itemView.findViewById(R.id.ivVideoDuration);

        }
    }

    class ViewClickListener extends BaseItemClickListener {
        @Override
        public void onClick(View v) {
            if (enableRepeatClick || lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }


}
