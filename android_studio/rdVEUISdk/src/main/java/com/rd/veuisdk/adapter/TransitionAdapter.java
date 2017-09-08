package com.rd.veuisdk.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.TransitionInfo;

import java.util.ArrayList;

/**
 * 转场列表adapter
 */
public class TransitionAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private String TAG = "TransitionAdapter";
    private ArrayList<TransitionInfo> mTransitionInfos;
    private int edColor, normalColor;
    private final ResizeOptions mResizeOptions = new ResizeOptions(120, 120);

    public TransitionAdapter(Context context) {
        edColor = ContextCompat.getColor(context, R.color.main_orange);
        normalColor = ContextCompat.getColor(context, R.color.transparent_white);
        mTransitionInfos = new ArrayList<>();
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void updateData(ArrayList<TransitionInfo> mlist) {
        mTransitionInfos.clear();
        if (null != mlist) {
            mTransitionInfos.addAll(mlist);
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mTransitionInfos.size();
    }

    @Override
    public TransitionInfo getItem(int i) {
        return mTransitionInfos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private int checkedId = -1;

    public void setChecked(int checkId) {
        checkedId = checkId;
        notifyDataSetChanged();
    }

    public void recycle() {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearMemoryCaches();
        //imagePipeline.clearDiskCaches();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder vh;
        if (null == view) {
            view = mLayoutInflater.inflate(R.layout.transiton_item_layout, null);
            vh = new ViewHolder();
            vh.mIcon = (SimpleDraweeView) view.findViewById(R.id.transition_item_icon);
            vh.mText = (CheckedTextView) view.findViewById(R.id.transition_item_text);
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }


        TransitionInfo info = getItem(i);
        if (null != info) {
            vh.mText.setText(info.getText());
            String url = info.getIconPath();

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setRotationOptions(RotationOptions.autoRotate())
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(mResizeOptions)
                    .build();
            DraweeController placeHolderDraweeController = Fresco.newDraweeControllerBuilder()
                    .setOldController(vh.mIcon.getController())
                    .setImageRequest(request)
                    .build();
            vh.mIcon.setController(placeHolderDraweeController);
        }

        RoundingParams roundingParams = vh.mIcon.getHierarchy().getRoundingParams();
        if (checkedId == i) {
            roundingParams.setBorderColor(edColor);
            vh.mText.setChecked(true);
        } else {
            vh.mText.setChecked(false);
            roundingParams.setBorderColor(normalColor);
        }
        vh.mIcon.getHierarchy().setRoundingParams(roundingParams);

        return view;
    }

    class ViewHolder {
        SimpleDraweeView mIcon;
        CheckedTextView mText;
    }
}
