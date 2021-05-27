package com.rd.veuisdk.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.model.SoundInfo;

import java.util.ArrayList;

public class SoundAdapter extends BaseRVAdapter<SoundAdapter.SoundViewHolder> {

    private ArrayList<SoundInfo> mMusicInfos;

    public SoundAdapter() {
        mMusicInfos = new ArrayList<>();
    }

    public void addAll(ArrayList<SoundInfo> tmp, TextView tvAdded, int checked) {
        //暂不做方向上的处理
        mMusicInfos.clear();
        if (null != tmp && tmp.size() > 0) {
            tvAdded.setVisibility(View.VISIBLE);
            mMusicInfos.addAll(tmp);
        } else {
            tvAdded.setVisibility(View.GONE);
        }
        lastCheck = checked;
        mId.clear();
        mId.clear();
        notifyDataSetChanged();
    }

    @Override
    public SoundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new SoundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SoundViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        SoundInfo info = mMusicInfos.get(position);
        holder.mTvItemName.setText(info.getName());
        if (position == lastCheck) {
            holder.mTvItemName.setEnabled(true);
        } else {
            holder.mTvItemName.setEnabled(false);
        }
        if (info.getStart() <= mDuration && info.getEnd() >= mDuration) {
            mId.put(position, position);
            holder.mIvPromptPoint.setVisibility(View.VISIBLE);
        } else {
            holder.mIvPromptPoint.setVisibility(View.GONE);
        }
    }

    private SparseArray<Integer> mId = new SparseArray<>();
    private int mDuration = 0;

    //设置时间
    public void setDuration(int duration) {
        mDuration = duration;
        int  tmp = 0;
        int i = 0;
        for (; i < mMusicInfos.size(); i++) {
            SoundInfo info = mMusicInfos.get(i);
            if (info.getStart() < mDuration && info.getEnd() > mDuration) {
                if (mId.get(i) == null) {
                    mId.clear();
                    notifyDataSetChanged();
                    break;
                } else {
                    tmp++;
                }
            }
        }
        if (i >= mMusicInfos.size() && mId.size() > 0 && tmp != mId.size()) {
            mId.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return mMusicInfos.size();
    }

    private SoundInfo getItem(int position) {
        return mMusicInfos.get(position);
    }

    class SoundViewHolder extends RecyclerView.ViewHolder {

        TextView mTvItemName;
        ImageView mIvPromptPoint;

        public SoundViewHolder(View itemView) {
            super(itemView);
            mTvItemName = itemView.findViewById(R.id.tv_item_name);
            mIvPromptPoint = itemView.findViewById(R.id.iv_prompt_point);
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
