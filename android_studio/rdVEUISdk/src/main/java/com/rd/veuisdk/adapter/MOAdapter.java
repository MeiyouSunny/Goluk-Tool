package com.rd.veuisdk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.MOInfo;
import com.rd.veuisdk.ui.CheckSimpleView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 马赛克、水印
 */
public class MOAdapter extends BaseRVAdapter<MOAdapter.ViewHolder> {
    private List<MOInfo> list;
    private String TAG = "MOAdapter";
    private LayoutInflater mLayoutInflater;

    public interface IThumb {
        int getThumb(int styleId);
    }


    private IThumb mThumb;

    /**
     */
    public void setThumb(IThumb thumb) {
        mThumb = thumb;
    }


    /**
     * @param context
     */
    public MOAdapter(Context context) {
        list = new ArrayList<>();
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(ArrayList<MOInfo> tmp, TextView tvAdded, int checked) {
        //暂不做方向上的处理
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            tvAdded.setVisibility(View.VISIBLE);
            list.addAll(tmp);
        } else {
            tvAdded.setVisibility(View.GONE);
        }
        lastCheck = checked;
        mId.clear();
        notifyDataSetChanged();

    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public MOAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_mo_layout, parent, false);

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new MOAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MOAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        SimpleDraweeViewUtils.setCover(holder.mImageView, mThumb.getThumb(getItem(position).getStyleId()));
        holder.mImageView.setChecked(position == lastCheck);

        if (list.get(position).getStart() <= mDuration && list.get(position).getEnd() >= mDuration) {
            mId.put(position, position);
            holder.mImageView.setBelong(true);
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
            MOInfo info = list.get(i);
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

    /**
     * @param position
     * @return
     */
    private MOInfo getItem(int position) {
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
