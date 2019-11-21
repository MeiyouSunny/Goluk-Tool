package com.rd.veuisdk.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Random;

/**
 * 转场列表adapter
 */
public class TransitionAdapter extends BaseRVAdapter<TransitionAdapter.ViewHolder> {
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


    /**
     * @param list
     * @param checkedIndex
     */
    public void updateData(List<TransitionInfo> list, int checkedIndex) {
        mTransitionInfos.clear();
        if (null != list && list.size() > 0) {
            mTransitionInfos.addAll(list);
        }
        lastCheck = checkedIndex;
        notifyDataSetChanged();
    }

    public TransitionInfo getItem(int position) {
        if (position < getItemCount()) {
            return mTransitionInfos.get(position);
        }
        return null;
    }


    /**
     * 获取一个可用的随机数(list的下标)，基于adapter的
     *
     * @return
     */
    public int getRandomIndex() {
        Random random = new Random();
        try {
            int len = getItemCount();
            List<Integer> list = new ArrayList<>();
            int tmp = 8;
            for (int i = 0; i < 8; i++) {
                list.add(i);
            }
            for (int i = tmp; i < len; i++) {
                TransitionInfo info = mTransitionInfos.get(i);
                if (info.isExistFile()) {
                    //已下载
                    list.add(i);
                }
            }
            int max = list.size();
            //避免出现无转场 即0
            int nTmp = random.nextInt(max - 1) + 1;
            int index = list.get(nTmp);
            return index;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.transiton_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TransitionInfo info = mTransitionInfos.get(position);
        if (null != info) {
            holder.mText.setText(info.getName());
            String url = info.getCover();
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                    .setRotationOptions(RotationOptions.autoRotate())
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(mResizeOptions)
                    .build();
            DraweeController placeHolderDraweeController = Fresco.newDraweeControllerBuilder()
                    .setOldController(holder.mIcon.getController())
                    .setImageRequest(request)
                    .build();
            holder.mIcon.setController(placeHolderDraweeController);
        }

        RoundingParams roundingParams = holder.mIcon.getHierarchy().getRoundingParams();
        if (lastCheck == position) {
            roundingParams.setBorderColor(edColor);
            holder.mText.setChecked(true);
        } else {
            holder.mText.setChecked(false);
            roundingParams.setBorderColor(normalColor);
        }
        holder.mIcon.getHierarchy().setRoundingParams(roundingParams);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, mTransitionInfos.get(position));
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mTransitionInfos.size();
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
        SimpleDraweeView mIcon;
        CheckedTextView mText;

        public ViewHolder(View itemView) {
            super(itemView);
            mIcon = (SimpleDraweeView) itemView.findViewById(R.id.transition_item_icon);
            mText = (CheckedTextView) itemView.findViewById(R.id.transition_item_text);
        }
    }
}
