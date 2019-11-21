package com.rd.veuisdk.demo.zishuo.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.demo.zishuo.ZishuoStyle;

import java.util.ArrayList;

/**
 * 字说风格适配器
 */
public class ZishuoStyleAdapter extends BaseRVAdapter<ZishuoStyleAdapter.ViewHolder> {

    private final ResizeOptions mResizeOptions = new ResizeOptions(120, 120);
    private ArrayList<ZishuoStyle> mStyles;
    private int edColor, normalColor;

    public ZishuoStyleAdapter(ArrayList<ZishuoStyle> zishuoStyles, Context context) {
        this.mStyles = zishuoStyles;
        edColor = ContextCompat.getColor(context, R.color.main_orange);
        normalColor = ContextCompat.getColor(context, R.color.transparent_white);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_zishuo_style, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ZishuoStyle zishuoStyle = mStyles.get(position);
        String url = zishuoStyle.getCover();
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setRotationOptions(RotationOptions.autoRotate())
                .setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(mResizeOptions)
                .build();
        DraweeController placeHolderDraweeController = Fresco.newDraweeControllerBuilder()
                .setOldController(holder.mCover.getController())
                .setImageRequest(request)
                .build();
        holder.mCover.setController(placeHolderDraweeController);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, zishuoStyle);
                }
            }
        });
        RoundingParams roundingParams = holder.mCover.getHierarchy().getRoundingParams();
        if (lastCheck == position) {
            roundingParams.setBorderColor(edColor);
        } else {
            roundingParams.setBorderColor(normalColor);
        }
        holder.mCover.getHierarchy().setRoundingParams(roundingParams);
    }

    @Override
    public int getItemCount() {
        return mStyles.size();
    }

    public void setChecked(int checkId) {
        lastCheck = checkId;
        notifyDataSetChanged();
    }

    public void recycle() {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearMemoryCaches();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView mCover;

        public ViewHolder(View itemView) {
            super(itemView);
            mCover = itemView.findViewById(R.id.sdv_icon);
        }
    }

}
