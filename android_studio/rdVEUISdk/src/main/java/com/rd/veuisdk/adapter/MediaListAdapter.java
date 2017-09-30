package com.rd.veuisdk.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.rd.gallery.IVideo;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.NumItemView;
import com.rd.veuisdk.ui.SubFunctionUtils;
import com.rd.veuisdk.utils.DateTimeUtils;

import java.util.ArrayList;

public class MediaListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ImageItem> mArrImageItems;
    private ResizeOptions mResizeOptions = new ResizeOptions(120, 120);

    public MediaListAdapter(Context c) {
        this.mContext = c;
        mArrImageItems = new ArrayList<ImageItem>();
    }

    public void addAll(ArrayList<ImageItem> list) {
        mArrImageItems.clear();
        if (SelectMediaActivity.mIsAppend && !SubFunctionUtils.isHideText()) {
            mArrImageItems.add(null);
        }
        mArrImageItems.addAll(list);
        this.notifyDataSetChanged();
    }

    public void clear() {
        if (mArrImageItems != null) {
            mArrImageItems.clear();
            mArrImageItems = null;
        }
        mArrImageItems = new ArrayList<ImageItem>();
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        PreviewFrameLayout pflConvertView = (PreviewFrameLayout) convertView;
        if (null == convertView) {
            pflConvertView = (PreviewFrameLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.select_photo_list_item, null);
            holder = new ViewHolder();
            holder.thumbnail = (SimpleDraweeView) pflConvertView
                    .findViewById(R.id.ivPhotoListThumbnail);
            pflConvertView.setAspectRatio(1f);
            convertView = pflConvertView;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageItem item = mArrImageItems.get(position);
        TextView videoDur = (TextView) convertView
                .findViewById(R.id.ivVideoDur);

        if (null != item) {
            if (item.image.isValid()) {
                if (item.image.equals(holder.thumbnail.getTag())) {

                } else {
                    ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://"+item.image.getDataPath()))
                            .setRotationOptions(RotationOptions.autoRotate())
                            .setLocalThumbnailPreviewsEnabled(true)
                            .setResizeOptions(mResizeOptions)
                            .build();
                    DraweeController placeHolderDraweeController = Fresco.newDraweeControllerBuilder()
                            .setOldController(holder.thumbnail.getController())
                            .setImageRequest(request)
                            .build();
                    holder.thumbnail.setController(placeHolderDraweeController);
                    holder.thumbnail.setTag(item.image);
                }

            } else {
                if (item.image instanceof IVideo) {
                    holder.thumbnail.setImageResource(R.drawable.gallery_video_failed);
                } else {
                    holder.thumbnail.setImageResource(R.drawable.gallery_image_failed);
                }
            }

            if (item.image instanceof IVideo) {
                videoDur.setVisibility(View.VISIBLE);
                videoDur.setText(DateTimeUtils
                        .stringForTime((int) ((IVideo) item.image)
                                .getDuration()));
            } else {
                videoDur.setVisibility(View.GONE);
                videoDur.setText(null);
            }
            refreashItemSelectedState(convertView, item);
        } else {
            holder.thumbnail.setImageResource(R.drawable.word_broad);
            videoDur.setVisibility(View.GONE);
            NumItemView selected = (NumItemView) convertView
                    .findViewById(R.id.ivPhotoListSelected);
            selected.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void refreashItemSelectedState(View convertView, ImageItem item) {
        if (null != convertView) {
            NumItemView selected = (NumItemView) convertView
                    .findViewById(R.id.ivPhotoListSelected);
            if (item.selected) {
                selected.setVisibility(View.VISIBLE);
                selected.setPosition(item.position);
            } else {
                selected.setVisibility(View.GONE);
            }
            selected.setSelected(item.selected);

        }
    }

    private class ViewHolder {
        com.facebook.drawee.view.SimpleDraweeView thumbnail;
    }

}
