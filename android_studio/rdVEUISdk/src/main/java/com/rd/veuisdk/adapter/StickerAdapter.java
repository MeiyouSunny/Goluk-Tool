package com.rd.veuisdk.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rd.lib.utils.LogUtil;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.StickerInfo;
import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.net.StickerUtils;
import com.rd.veuisdk.utils.apng.ApngImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴纸-适配器
 */
public class StickerAdapter extends BaseRVAdapter<StickerAdapter.ViewHolder> {
    private List<StickerInfo> list;
    private String TAG = "StickerAdapter";
    private LayoutInflater mLayoutInflater;


    /**
     * @param context
     */
    public StickerAdapter(Context context) {
        list = new ArrayList<>();
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(ArrayList<StickerInfo> tmp, int checked) {
        //暂不做方向上的处理
        list.clear();
        mDuration = 0;
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        lastCheck = checked;
        mId.clear();
        notifyDataSetChanged();
    }


    public int getIndex() {
        return lastCheck;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public StickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_sticker_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new StickerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StickerAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);

        StyleInfo info = StickerUtils.getInstance().getStyleInfo(list.get(position).getStyleId());
        if(null != info) {
            ApngImageLoader.getInstance().displayApng(info.icon, holder.mIcon, new ApngImageLoader.ApngConfig(0, true, false));
        } else {
            ApngImageLoader.getInstance().displayApng(list.get(position).getIcon(), holder.mIcon, new ApngImageLoader.ApngConfig(0, true, false));
        }
        if (lastCheck == position) {
            holder.mSelect.setVisibility(View.VISIBLE);
        } else {
            holder.mSelect.setVisibility(View.GONE);
        }
        if (lastCheck != position && list.get(position).getStart() < mDuration
                && list.get(position).getEnd() > mDuration) {
            mId.put(position, position);
            holder.mPrompt.setVisibility(View.VISIBLE);
        } else {
            holder.mPrompt.setVisibility(View.GONE);
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
            StickerInfo info = list.get(i);
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
    private StickerInfo getItem(int position) {
        return list.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mIcon;
        ImageView mPrompt;
        ImageView mSelect;

        ViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mPrompt = (ImageView) itemView.findViewById(R.id.prompt);
            mSelect = (ImageView) itemView.findViewById(R.id.select);
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
