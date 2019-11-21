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
import com.rd.veuisdk.model.IDirInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 图库-视频
 */
public class DirAdapter extends BaseRVAdapter<DirAdapter.ViewHolder> {
    private List<IDirInfo> list;
    private LayoutInflater mLayoutInflater;

    /**
     * @param context
     * @param enableRepeat
     */
    public DirAdapter(Context context, boolean enableRepeat) {
        list = new ArrayList<>();
        enableRepeatClick = enableRepeat;
        mLayoutInflater = LayoutInflater.from(context);
    }


    /**
     * @param tmp
     */
    public void addAll(List<IDirInfo> tmp) {
        //暂不做方向上的处理
        list.clear();

        if (null != tmp && tmp.size() > 0) {
            list.addAll(tmp);
        }
        notifyDataSetChanged();

    }


    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public DirAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_gallery_dir_layout, parent, false);
        ViewClickListener viewClickListener = new ViewClickListener();
        view.setOnClickListener(viewClickListener);
        view.setTag(viewClickListener);
        return new DirAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(DirAdapter.ViewHolder holder, int position) {
        ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
        viewClickListener.setPosition(position);

        IDirInfo item = getItem(position);


        holder.tvName.setText(item.getBucketName());

        List<ImageItem> tmp = item.getList();
        int len = 0;
        if (null != tmp) {
            len = tmp.size();
        }
        holder.tvNum.setText(Integer.toString(len));
        if (len > 0 && tmp.get(0).image.isValid()) {
            SimpleDraweeViewUtils.setCover(holder.thumbnail, tmp.get(0).image.getDataPath());
        } else {
            SimpleDraweeViewUtils.setCover(holder.thumbnail, R.drawable.gallery_image_failed);
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
    private IDirInfo getItem(int position) {
        return list.get(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView thumbnail;
        TextView tvName, tvNum;
        PreviewFrameLayout pflConvertView;

        ViewHolder(View itemView) {
            super(itemView);
            pflConvertView = (PreviewFrameLayout) itemView.findViewById(R.id.aspDir);
            pflConvertView.setAspectRatio(1f);
            thumbnail = (SimpleDraweeView) itemView.findViewById(R.id.ivPhotoListThumbnail);
            tvName = (TextView) itemView.findViewById(R.id.tvDirName);
            tvNum = (TextView) itemView.findViewById(R.id.tvDirNum);

        }
    }

    class ViewClickListener extends BaseItemClickListener {
        @Override
        public void onClick(View v) {
            if (enableRepeatClick || lastCheck != position) {
                lastCheck = position;
                notifyDataSetChanged();
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onItemClick(position, getItem(position));
                }
            }
        }
    }


}
