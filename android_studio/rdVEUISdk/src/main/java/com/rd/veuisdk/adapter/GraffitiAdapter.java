package com.rd.veuisdk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.GraffitiInfo;
import com.rd.veuisdk.ui.CheckSimpleView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 涂鸦
 */
public class GraffitiAdapter extends BaseRVAdapter<GraffitiAdapter.ViewHolder> {
    private List<GraffitiInfo> list;
    private String TAG = "GraffitiAdapter";
    private LayoutInflater mLayoutInflater;


    /**
     * @param context
     */
    public GraffitiAdapter(Context context) {
        list = new ArrayList<>();
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(List<GraffitiInfo> tmp, int checked) {
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        lastCheck = checked;
        mId.clear();
        notifyDataSetChanged();
    }

    public void setChecked(int position) {
        if (lastCheck != position) {
            lastCheck = position;
            if (lastCheck >= 0) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
            mId.clear();
            notifyDataSetChanged();
        }
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public GraffitiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_mo_layout, parent, false);

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new GraffitiAdapter.ViewHolder(view);
    }

    public int getCheckedIndex() {
        return lastCheck;
    }


    @Override
    public void onBindViewHolder(GraffitiAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        SimpleDraweeViewUtils.setCover(holder.mImageView, getItem(position).getPath());
        holder.mImageView.setChecked(position == lastCheck);

        if (getItem(position).getTimelineFrom() <= mDuration
                && getItem(position).getTimelineTo() >= mDuration) {
            holder.mImageView.setBelong(true);
            mId.put(position, position);
        } else {
            holder.mImageView.setBelong(false);
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    private SparseArray<Integer> mId = new SparseArray<>();
    private int mDuration = 0;

    //设置时间
    public void setDuration(int duration) {
        mDuration = duration;
        notifyDataSetChanged();
        mDuration = duration;
        int  tmp = 0;
        int i = 0;
        for (; i < list.size(); i++) {
            GraffitiInfo info = list.get(i);
            if (info.getTimelineFrom() < mDuration && info.getTimelineTo() > mDuration) {
                if (mId.get(i) == null) {
                    mId.clear();
                    notifyDataSetChanged();
                    break;
                } else {
                    tmp++;
                }
            }
        }
        if (i >= list.size() && mId.size() > 0 && tmp != mId.size()) {
            mId.clear();
            notifyDataSetChanged();
        }
    }

    private GraffitiInfo getItem(int position) {
        return list.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckSimpleView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = Utils.$(itemView, R.id.ivItemImage);
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
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }


}
