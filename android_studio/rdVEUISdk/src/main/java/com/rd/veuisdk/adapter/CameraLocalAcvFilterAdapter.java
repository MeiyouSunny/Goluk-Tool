package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ui.ExtCircleSimpleDraweeView;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * 相机录制本地-滤镜(acv)
 * 180711
 */
public class CameraLocalAcvFilterAdapter extends BaseRVAdapter<CameraLocalAcvFilterAdapter.ViewHolder> {

    public static class FilterItem {
        public FilterItem(@DrawableRes int drawId, String str, String effct) {
            this.drawId = drawId;
            this.str = str;
            this.effect = effct;
        }

        @DrawableRes
        public int drawId;
        public String str;
        public String effect;
    }

    private List<FilterItem> list;
    private String TAG = "CameraLocalAcvFilterAdapter";
    private int mColorNormal, mColorSelected;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public int getCurrentId() {
        return getChecked();
    }


    public CameraLocalAcvFilterAdapter(Context context) {
        Resources res = context.getResources();
        mColorNormal = res.getColor(R.color.borderline_color);
        mColorSelected = res.getColor(R.color.main_orange);
        list = new ArrayList<>();
        lastCheck = 0;
    }

    private boolean isVer = true;


    public void addAll(boolean isVer, List<FilterItem> tmp) {
        this.isVer = isVer;
        setOrientation(isVer);
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        notifyDataSetChanged();

    }

    /**
     * 录制界面横竖切换
     */
    public void setOrientation(boolean isVer) {
        this.isVer = isVer;
    }

    @Override
    public int getItemViewType(int position) {
        return isVer ? 0 : 1;
    }

    @Override
    public CameraLocalAcvFilterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == 0) {
            view = getLayoutInflater(parent.getContext()).inflate(R.layout.fresco_list_item, parent, false);
        } else {
            view = getLayoutInflater(parent.getContext()).inflate(R.layout.fresco_list_item_land, parent, false);
        }
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        FilterItem info = list.get(position);
        if (position == lastCheck) {
            //被选中
            holder.mImageView.setChecked(true);
            holder.mText.setTextColor(mColorSelected);
        } else {
            //未选中
            holder.mImageView.setChecked(false);
            holder.mText.setTextColor(mColorNormal);
        }
        holder.mImageView.setImageResource(info.drawId);
        holder.mText.setText(info.str);
    }


    /***
     * 设置为选中状态
     */
    public void onItemChecked(int position) {
        lastCheck = position;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public FilterItem getItem(int position) {
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
            mText = Utils.$(itemView, R.id.tvItemCaption);
            mImageView = Utils.$(itemView, R.id.ivItemImage);
        }
    }

    class ViewClickListener extends BaseRVAdapter.BaseItemClickListener {

        @Override
        public void onClick(View v) {
            LogUtil.i(TAG, "onClick: >>" + position + " " + lastCheck);
            if (lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, null);
                }
            }
        }
    }


}

