package com.rd.veuisdk.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.ui.CircleAnimationView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 画中画
 */
public class CollageAdapter extends BaseRVAdapter<CollageAdapter.ViewHolder> {
    private List<CollageInfo> list;
    private String TAG = "CollageAdapter";
    private LayoutInflater mLayoutInflater;


    private TextView tvAdded;
    /**
     *
     * @param tvAdded
     * @param tmp
     */
    public CollageAdapter(TextView tvAdded, List<CollageInfo> tmp) {
        this.tvAdded = tvAdded;
        list = new ArrayList<>();
        list.addAll(tmp);
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(List<CollageInfo> tmp, int checked) {
        //暂不做方向上的处理
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            tvAdded.setVisibility(View.VISIBLE);
            if(tvTitle!=null){
                tvTitle.setVisibility(View.GONE);
            }
            list.addAll(tmp);
        } else {
            tvAdded.setVisibility(View.GONE);
            if(tvTitle!=null){
                tvTitle.setVisibility(View.VISIBLE);
            }
        }
        lastCheck = checked;
        mId.clear();
        notifyDataSetChanged();

    }

    private TextView tvTitle;

    public void setTitleView(TextView tvtitle){
        this.tvTitle =tvtitle;
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public CollageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_collage_layout, parent, false);

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new CollageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CollageAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);

        CollageInfo info = getItem(position);
        String coverUrl = info.getThumbPath();
        SimpleDraweeViewUtils.setCover(holder.mImageView, coverUrl);
        holder.mImageView.setChecked(position == lastCheck);
        if (info.getSubInfo().getTimelinefrom() <= mDuration
                && info.getSubInfo().getTimelineTo() >= mDuration) {
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
        int  tmp = 0;
        int i = 0;
        for (; i < list.size(); i++) {
            CollageInfo info = list.get(i);
            if (info.getSubInfo().getTimelinefrom() < mDuration
                    && info.getSubInfo().getTimelineTo() > mDuration) {
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

    /**
     * @param position
     * @return
     */
    private CollageInfo getItem(int position) {
        return list.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleAnimationView mImageView;
        ViewHolder(View itemView) {
            super(itemView);
            mImageView = (CircleAnimationView) itemView.findViewById(R.id.ivItemImage);
            mImageView.setCircle(false);
        }
    }

    class ViewClickListener extends  BaseItemClickListener {
        @Override
        public void onClick(View v) {
            LogUtil.i(TAG, "onClick: >>" + position + " " + lastCheck);
            if (enableRepeatClick|| lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position,getItem(position));
                }
            }
        }
    }


}
