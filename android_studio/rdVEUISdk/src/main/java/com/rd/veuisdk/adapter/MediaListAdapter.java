package com.rd.veuisdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rd.cache.GalleryImageFetcher;
import com.rd.gallery.IVideo;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.lib.ui.RotateImageView;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.ExtImageView;
import com.rd.veuisdk.utils.DateTimeUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;

/**
 * 图库adapter
 */
public class MediaListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ImageItem> mArrImageItems;
    private GalleryImageFetcher mGifVideoThumbnail; // 获取视频缩略图
    private boolean bHideText;

    public MediaListAdapter(Context context, GalleryImageFetcher fetcher, boolean hideText) {
        mContext = context;
        mGifVideoThumbnail = fetcher;
        mArrImageItems = new ArrayList<>();
        bHideText = hideText;
    }

    public void addAll(ArrayList<ImageItem> list) {
        mArrImageItems.clear();
        if (mIAdapterListener.isAppend() && !bHideText) {
            mArrImageItems.add(null);
        }
        mArrImageItems.addAll(list);
         notifyDataSetChanged();
    }

    public void clear() {
        if (mArrImageItems != null) {
            mArrImageItems.clear();
            mArrImageItems = null;
        }
        mArrImageItems = new ArrayList<>();
    }

    @Override
    public int getCount() {
        if (null != mArrImageItems) {
            return mArrImageItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public ImageItem getItem(int position) {
        return mArrImageItems != null ? mArrImageItems.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    public void recycle() {
        mGifVideoThumbnail = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        AddClickListener listener;
        if (null == convertView) {
            PreviewFrameLayout pflConvertView = (PreviewFrameLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.select_photo_list_item, null);
            holder = new ViewHolder();
            holder.thumbnail = Utils.$(pflConvertView, R.id.ivPhotoListThumbnail);
            holder.btnAdd = Utils.$(pflConvertView, R.id.part_add);
            holder.btnAdd.setRepeatClickIntervalTime(400);
            holder.tvDuration = Utils.$(pflConvertView, R.id.ivVideoDur);
            listener = new AddClickListener();
            holder.btnAdd.setOnClickListener(listener);
            pflConvertView.setAspectRatio(1f);


            holder.btnAdd.setTag(listener);
            convertView = pflConvertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            listener = (AddClickListener) holder.btnAdd.getTag();
        }
        listener.setPosition(position);
        ImageItem item = getItem(position);

        holder.btnAdd.setVisibility(mIAdapterListener.isShowAddBtn() ? View.VISIBLE : View.GONE);

        if (null != item) {
            if (item.image.isValid()) {
                if (null != mGifVideoThumbnail) {
                    if (item.image.equals(holder.thumbnail.getTag())) {
                        mGifVideoThumbnail.loadImage(item.image, holder.thumbnail);
                    } else {
                        mGifVideoThumbnail.loadImage(item.image, holder.thumbnail);
                        holder.thumbnail.setTag(item.image);
                    }
                }
            } else {
                if (item.image instanceof IVideo) {
                    holder.thumbnail.setImageResource(R.drawable.gallery_video_failed);
                } else {
                    holder.thumbnail.setImageResource(R.drawable.gallery_image_failed);
                }
            }
            if (item.image instanceof IVideo) {
                holder.tvDuration.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(DateTimeUtils.stringForTime((int) ((IVideo) item.image).getDuration()));
            } else {
                holder.tvDuration.setVisibility(View.GONE);
                holder.tvDuration.setText(null);
            }
        } else {
            holder.btnAdd.setVisibility(View.GONE);
            holder.thumbnail.setImageResource(R.drawable.word_broad);
            holder.tvDuration.setVisibility(View.GONE);
        }
        return convertView;
    }


    private class ViewHolder {
        RotateImageView thumbnail;
        ExtImageView btnAdd;
        TextView tvDuration;
    }

    class AddClickListener implements View.OnClickListener {
        private int position = 0;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {

            if (null != mIAdapterListener) {
                mIAdapterListener.onAdd(getItem(position));
            }
        }
    }

    public void setIAdapterListener(IAdapterListener IAdapterListener) {
        mIAdapterListener = IAdapterListener;
    }

    private IAdapterListener mIAdapterListener;

    /**
     * 状态回调
     */
    public static interface IAdapterListener {

        /**
         * 点击添加按钮
         *
         * @param item
         */
        void onAdd(ImageItem item);

        boolean isShowAddBtn();

        boolean isAppend();
    }


}
