package com.rd.veuisdk.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.IStickerSortApi;
import com.rd.veuisdk.ui.ExtListItemStyle;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;

public class StickerSortAdapter extends BaseRVAdapter<StickerSortAdapter.StickerSortHolder> {

    private ArrayList<IStickerSortApi> mISticker = new ArrayList<>();

    @Override
    public StickerSortHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker_sort_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new StickerSortHolder(view);
    }

    @Override
    public void onBindViewHolder(StickerSortHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        if (lastCheck == position) {
            SimpleDraweeViewUtils.setCover(holder.mSrc, mISticker.get(position).getIcon());
        } else {
            SimpleDraweeViewUtils.setCover(holder.mSrc, mISticker.get(position).getIconP());
        }
    }

    public void addAll(ArrayList<IStickerSortApi> icon, int checked) {
        mISticker.clear();
        if (null != icon && icon.size() > 0) {
            mISticker.addAll(icon);
        }
        lastCheck = checked;
        notifyDataSetChanged();
    }

    private String getItem(int position) {
        return mISticker.get(position).getId();
    }

    public String getCurrent(){
        if (mISticker.size() > 0) {
            if (lastCheck == -1 || lastCheck >= mISticker.size()) {
                return mISticker.get(0).getId();
            }
            return mISticker.get(lastCheck).getId();
        }
        return null;
    }

    //设置选中
    public void setCurrent(String category) {
        if (!TextUtils.isEmpty(category)) {
            for (int i = 0; i < mISticker.size(); i++) {
                if (category.equals(mISticker.get(i).getId())) {
                    lastCheck = i;
                    break;
                }
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return mISticker.size();
    }

    class StickerSortHolder extends RecyclerView.ViewHolder {

        private SimpleDraweeView mSrc;
        private ExtListItemStyle mBorderView;

        StickerSortHolder(View itemView) {
            super(itemView);
            mSrc = (SimpleDraweeView) itemView.findViewById(R.id.sdv_src);
            mBorderView = (ExtListItemStyle) itemView.findViewById(R.id.item_border);
        }
    }

    class ViewClickListener extends BaseItemClickListener {

        @Override
        public void onClick(View v) {
            if (lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }

}
