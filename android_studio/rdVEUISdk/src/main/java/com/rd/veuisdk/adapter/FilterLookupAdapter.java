package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rd.lib.utils.FileUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.WebFilterInfo;
import com.rd.veuisdk.ui.ExtCircleSimpleDraweeView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 相机录制-滤镜(lookup)
 * 180711
 */
public class FilterLookupAdapter extends BaseRVAdapter<FilterLookupAdapter.ViewHolder> {
    private List<WebFilterInfo> list;
    private String TAG = "FilterAdapter";
    private LayoutInflater mLayoutInflater;
    private int mColorNormal, mColorSelected;

    /**
     * @param context
     */
    public FilterLookupAdapter(Context context) {
        Resources res = context.getResources();
        mColorNormal = res.getColor(R.color.borderline_color);
        mColorSelected = res.getColor(R.color.main_orange);
        list = new ArrayList<>();
    }


    public int getCurrentId() {
        return lastCheck;
    }

    /**
     * @param tmp
     * @param checked
     */
    public void addAll(boolean isVer, List<WebFilterInfo> tmp, int checked) {
        //暂不做方向上的处理
        setOrientation(isVer);
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        lastCheck = checked;
        download_progress = 100;
        notifyDataSetChanged();
    }


    private boolean isVer = true;

    /**
     * 录制界面横竖切换
     *
     * @param isVer
     */
    public void setOrientation(boolean isVer) {
        this.isVer = isVer;
    }

    @Override
    public int getItemViewType(int position) {
        return isVer ? 0 : 1;
    }

    @Override
    public FilterLookupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = null;
        if (viewType == 0) {
            view = mLayoutInflater.inflate(R.layout.fresco_list_item, parent, false);
        } else {
            view = mLayoutInflater.inflate(R.layout.fresco_list_item_land, parent, false);
        }

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        FilterLookupAdapter.ViewHolder viewHolder = new FilterLookupAdapter.ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            updateCheckProgress(holder, position);
        }
    }

    /**
     * 更新选中状态和进度
     *
     * @param holder
     * @param position
     */
    private void updateCheckProgress(ViewHolder holder, int position) {
        if (position == lastCheck) {
            //被选中
            holder.mImageView.setProgress(download_progress);
            holder.mImageView.setChecked(true);
            holder.mText.setTextColor(mColorSelected);
        } else {
            //未选中
            holder.mImageView.setProgress(0);
            holder.mImageView.setChecked(false);
            holder.mText.setTextColor(mColorNormal);
        }
    }

    @Override
    public void onBindViewHolder(FilterLookupAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        WebFilterInfo info = list.get(position);
        updateCheckProgress(holder, position);
        if (position == 0) {
            try {
                holder.mImageView.setImageResource(Integer.parseInt(info.getCover()));
            } catch (Exception ex) {
                holder.mImageView.setImageResource(R.drawable.camera_effect_0);
            }
        } else {
            String coverUrl = info.getCover();
            if (!TextUtils.isEmpty(coverUrl)) {
                SimpleDraweeViewUtils.setCover(holder.mImageView, coverUrl);
            } else {
                SimpleDraweeViewUtils.setCover(holder.mImageView, info.getResId());
            }
        }
        holder.mText.setText(info.getName());


    }


    /***
     * 设置为选中状态
     */
    public void onItemChecked(int nItemId) {
        lastCheck = nItemId;
        notifyDataSetChanged();
    }

    private int download_progress = 100;

    public void setdownStart(int nItemId) {
        lastCheck = nItemId;
        download_progress = 1;
        notifyDataSetChanged();
    }

    public void setdownProgress(int nItemId, int progress) {
        lastCheck = nItemId;
        download_progress = progress;
        notifyItemRangeChanged(nItemId, 1, nItemId + "");

    }

    public void setdownEnd(int nItemId) {
        lastCheck = nItemId;
        download_progress = 100;
        notifyDataSetChanged();
    }

    public void setdownFailed(int nItemId) {
        lastCheck = nItemId;
        download_progress = 0;
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
    public WebFilterInfo getItem(int position) {
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
            super.onClick(v);
            if (lastCheck != position || enableRepeatClick) {
                WebFilterInfo info = getItem(position);
                if (null != info) {
                    if (position >= 1) {
                        if (TextUtils.isEmpty(info.getLocalPath()) || !FileUtils.isExist(info.getLocalPath())) {
                            //未下载
                            download_progress = 1;
                        } else {
                            //已下载
                            download_progress = 100;
                        }
                    } else {
                        //无效果
                        download_progress = 100;
                    }
                }
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, null);
                }
            }
        }
    }


}
