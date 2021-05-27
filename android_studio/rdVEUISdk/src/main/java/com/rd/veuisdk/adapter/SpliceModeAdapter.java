package com.rd.veuisdk.adapter;

import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.SpliceModeInfo;
import com.rd.veuisdk.ui.CheckSimpleView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.List;

/**
 * 拼接-模板
 */
public class SpliceModeAdapter extends BaseRVAdapter<SpliceModeAdapter.ViewHolder> {
    private List<SpliceModeInfo> mList;
    private LayoutInflater mLayoutInflater;


    public SpliceModeAdapter(List<SpliceModeInfo> list, int checked) {
        this.mList = list;
        lastCheck = checked;
    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public SpliceModeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_splice_mode_layout, parent, false);

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new SpliceModeAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(SpliceModeAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);

        SpliceModeInfo info = getItem(position);
        String bg = position == lastCheck ? info.getBg_p() : info.getBg_n();
        Uri uri = Uri.parse("asset:///" + bg);
        SimpleDraweeViewUtils.setCover(holder.mImageView, uri);

    }


    @Override
    public int getItemCount() {
        return mList.size();
    }

    /**
     * @param position
     * @return
     */
    private SpliceModeInfo getItem(int position) {
        return mList.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView mImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = (SimpleDraweeView) itemView.findViewById(R.id.ivItemImage);
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
