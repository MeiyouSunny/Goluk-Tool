package com.rd.veuisdk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.lib.ui.PreviewFrameLayout;
import com.rd.veuisdk.R;
import com.rd.veuisdk.ae.model.AETemplateInfo;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * AE 模板列表
 *
 * @author JIAN
 * @create 2018/10/12
 * @Describe
 */
public class AEModeAdapter extends BaseRVAdapter<AEModeAdapter.ViewHolder> {
    private List<AETemplateInfo> list;
    private String TAG = "AEModeAdapter";
    private LayoutInflater mLayoutInflater;

    /**
     * @param context
     */
    public AEModeAdapter(Context context) {
        list = new ArrayList<>();
    }


    /**
     * @param tmp
     */
    public void addAll(List<AETemplateInfo> tmp) {
        //暂不做方向上的处理
        list.clear();
        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        notifyDataSetChanged();

    }

    public void recycle() {
        list.clear();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public AEModeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (null == mLayoutInflater) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.item_ae_mode_layout, parent, false);

        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new AEModeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AEModeAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);
        AETemplateInfo info = list.get(position);
        holder.mPreviewFrameLayout.setAspectRatio(info.getCoverAsp());
        SimpleDraweeViewUtils.setCover(holder.mImageView, info.getIconPath(), false, info.getCoverWidth() / 2, info.getCoverHeight() / 2);
        holder.mText.setText(info.getName());

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * @param position
     * @return
     */
    public AETemplateInfo getItem(int position) {
        if (0 <= position && position <= (getItemCount() - 1)) {
            return list.get(position);
        }
        return null;

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;
        SimpleDraweeView mImageView;
        PreviewFrameLayout mPreviewFrameLayout;

        ViewHolder(View itemView) {
            super(itemView);
            mPreviewFrameLayout = (PreviewFrameLayout) itemView.findViewById(R.id.previewFrame);
            mText = (TextView) itemView.findViewById(R.id.tvTitle);
            mImageView = (SimpleDraweeView) itemView.findViewById(R.id.ivItemImage);
        }
    }

    class ViewClickListener extends BaseRVAdapter.BaseItemClickListener {

        @Override
        public void onClick(View v) {
            lastCheck = position;
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onItemClick(position, getItem(position));
            }
        }
    }


}
