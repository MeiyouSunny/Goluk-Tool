package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.AEMediaInfo;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * AE 媒体
 *
 * @author JIAN
 * @create 2018/10/12
 * @Describe
 */
public class AEMediaAdapter extends RecyclerView.Adapter<AEMediaAdapter.ViewHolder> {
    private List<AEMediaInfo> list;
    private String TAG = "AEModeAdapter";
    private LayoutInflater mLayoutInflater;
    private int lastCheck = 0;
    private int mColorNormal, mColorSelected;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private OnItemClickListener mOnItemClickListener;

    /**
     * @param context
     */
    public AEMediaAdapter(Context context) {
        Resources res = context.getResources();
        mColorNormal = res.getColor(R.color.borderline_color);
        mColorSelected = res.getColor(R.color.main_orange);
        list = new ArrayList<>();
    }


    /**
     * @param index
     * @param tmp
     */
    public void update(int index, AEMediaInfo tmp) {
        if (index >= 0 && null != tmp) {
            list.set(index, tmp);
        }
        notifyDataSetChanged();
    }

    /**
     * @param tmp
     */
    public void update(List<AEMediaInfo> tmp) {
        list.clear();
        list.addAll(tmp);
        notifyDataSetChanged();


    }

    @Override
    public int getItemViewType(int position) {

        return list.get(position).getType().ordinal();
    }

    @Override
    public AEMediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view;
        if (viewType == AEMediaInfo.MediaType.IMAGE.ordinal()) {
            view = mLayoutInflater.inflate(R.layout.item_ae_media_image_layout, parent, false);
        } else if (viewType == AEMediaInfo.MediaType.VIDEO.ordinal()) {
            view = mLayoutInflater.inflate(R.layout.item_ae_media_video_layout, parent, false);
        } else {
            view = mLayoutInflater.inflate(R.layout.item_ae_media_word_layout, parent, false);
        }

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new AEMediaAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AEMediaAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        AEMediaInfo info = list.get(position);

        if (null != info.getMediaObject()) {
            int viewType = getItemViewType(position);
            if (viewType == AEMediaInfo.MediaType.TEXT.ordinal()) {
                //文字
                holder.mText.setVisibility(View.VISIBLE);
                holder.mImageView.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(info.getText())) {
                    holder.mText.setTextSize(13);
                    holder.mText.setText(info.getText());
//                    holder.mText.setTextColor(info.getAETextLayerInfo().getTextColor());
//                    holder.mText.setTextSize(holder.mText.getResources().getDimension(R.dimen.text_size_14));
                    String tmpTTF = info.getTtf();
                    if (TextUtils.isEmpty(tmpTTF) || !tmpTTF.startsWith("/")) {
                        tmpTTF = info.getAETextLayerInfo().getTtfPath();
                    }
                    try {
                        Typeface typeface = Typeface.createFromFile(tmpTTF);
                        holder.mText.setTypeface(typeface);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        Typeface typeface = Typeface.createFromFile(info.getTtf());
                        holder.mText.setTypeface(typeface);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else {
                holder.mText.setVisibility(View.GONE);
                holder.mImageView.setVisibility(View.VISIBLE);
                SimpleDraweeViewUtils.setCover(holder.mImageView, !TextUtils.isEmpty(info.getThumbPath()) ? info.getThumbPath() : info.getMediaObject().getMediaPath());
            }
        } else {
            holder.mText.setVisibility(View.VISIBLE);
            holder.mImageView.setVisibility(View.GONE);
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
    public AEMediaInfo getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return list.get(position);
        }
        return null;

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;
        SimpleDraweeView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.tvMediaType);
            mImageView = (SimpleDraweeView) itemView.findViewById(R.id.ivItemImage);
        }
    }

    class ViewClickListener implements View.OnClickListener {
        private int position;

        /**
         * @param p
         */
        public void setPosition(int p) {
            position = p;
        }

        @Override
        public void onClick(View v) {
            LogUtil.i(TAG, "onClick: >>" + position + " " + lastCheck);

            lastCheck = position;
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onItemClick(position);
            }
        }
    }


}
