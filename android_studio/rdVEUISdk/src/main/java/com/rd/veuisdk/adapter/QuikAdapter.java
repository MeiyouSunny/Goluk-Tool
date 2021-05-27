package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.quik.QuikHandler;
import com.rd.veuisdk.ui.ExtCircleSimpleDraweeView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class QuikAdapter extends BaseRVAdapter<QuikAdapter.ViewHolder> {
    private List<QuikHandler.EffectInfo> list;
    private String TAG = "QuikAdapter";
    private LayoutInflater mLayoutInflater;
    private int mColorNormal, mColorSelected;

    /**
     * @param tmp
     */
    public void updateItem(List<QuikHandler.EffectInfo> tmp) {
        if (null != tmp && list != null) {
            list.clear();
            list.addAll(tmp);
            notifyDataSetChanged();
        }

    }



    /**
     * @param context
     */
    public QuikAdapter(Context context) {
        Resources res = context.getResources();
        mColorNormal = res.getColor(R.color.borderline_color);
        mColorSelected = res.getColor(R.color.main_orange);
        list = new ArrayList<>();
        lastCheck = 0;
    }


    public int getCurrentId() {
        return lastCheck;
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(boolean isVer, List<QuikHandler.EffectInfo> tmp, int checked) {
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        lastCheck = checked;
        notifyDataSetChanged();

    }

    private boolean isVer = true;


    @Override
    public int getItemViewType(int position) {
        return isVer ? 0 : 1;
    }

    @Override
    public QuikAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
//        Log.e(TAG, "onCreateViewHolder: " + viewType);
        View view = null;
        if (viewType == 0) {
            view = mLayoutInflater.inflate(R.layout.fresco_list_item, parent, false);
        } else {
            view = mLayoutInflater.inflate(R.layout.fresco_list_item_land, parent, false);
        }

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        QuikAdapter.ViewHolder viewHolder = new QuikAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(QuikAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        QuikHandler.EffectInfo info = list.get(position);
        if (position == lastCheck) {
            //被选中
            holder.mImageView.setProgress(100);
            holder.mImageView.setChecked(true);
            holder.mText.setTextColor(mColorSelected);
        } else {
            //未选中
            holder.mImageView.setProgress(0);
            holder.mImageView.setChecked(false);
            holder.mText.setTextColor(mColorNormal);
        }

        SimpleDraweeViewUtils.setCover(holder.mImageView, info.icon);
        if (null != info.mQuikTemplate) {
            holder.mText.setText(info.mQuikTemplate.name());
        } else {
            holder.mText.setText(R.string.none);
        }

    }


    /***
     * 设置为选中状态
     * @param nItemId
     */
    public void onItemChecked(int nItemId) {
        lastCheck = nItemId;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * @param position
     * @return
     */
    public QuikHandler.EffectInfo getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return list.get(position);
        }
        return null;

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;
        ExtCircleSimpleDraweeView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(R.id.tvItemCaption);
            mImageView = (ExtCircleSimpleDraweeView) itemView.findViewById(R.id.ivItemImage);
        }
    }

    class ViewClickListener extends BaseItemClickListener {
        @Override
        public void onClick(View v) {
            LogUtil.i(TAG, "onClick: >>" + position + " " + lastCheck);
            if (lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position,null);
                }
            }
        }
    }


}
