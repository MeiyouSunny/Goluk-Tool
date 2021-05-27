package com.rd.veuisdk.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.model.WordInfo;
import com.rd.veuisdk.net.SubUtils;
import com.rd.veuisdk.ui.CheckSimpleView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 字幕
 */
public class SubtitleAdapter extends BaseRVAdapter<SubtitleAdapter.ViewHolder> {
    private List<WordInfo> list;
    private String TAG = "SubtitleAdapter";
    private LayoutInflater mLayoutInflater;


    /**
     */
    public SubtitleAdapter(   ) {
        list = new ArrayList<>();
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(ArrayList<WordInfo> tmp, int checked) {
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        lastCheck = checked;
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public SubtitleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_mo_layout, parent, false);

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new SubtitleAdapter.ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(SubtitleAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);

        StyleInfo info = SubUtils.getInstance().getStyleInfo(list.get(position).getStyleId());
        if(null!=info) {
            SimpleDraweeViewUtils.setCover(holder.mImageView, info.icon);
        }
        holder.mImageView.setChecked(position == lastCheck);

        if (list.get(position).getStart() <= mDuration && list.get(position).getEnd() >= mDuration) {
            holder.mImageView.setBelong(true);
            mId.put(position, position);
        } else {
            holder.mImageView.setBelong(false);
        }

    }

    private SparseArray<Integer> mId = new SparseArray<>();
    private int mDuration = 0;

    //设置时间
    public void setDuration(int duration) {
        mDuration = duration;
        int  tmp = 0;
        int i = 0;
        for (; i < list.size(); i++) {
            WordInfo info = list.get(i);
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
        if (i >= list.size() && mId.size() > 0 && tmp != mId.size()) {
            mId.clear();
            notifyDataSetChanged();
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
    private WordInfo getItem(int position) {
        return list.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CheckSimpleView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = (CheckSimpleView) itemView.findViewById(R.id.ivItemImage);
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
