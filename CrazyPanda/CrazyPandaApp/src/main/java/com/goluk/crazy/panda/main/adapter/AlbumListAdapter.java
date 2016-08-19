package com.goluk.crazy.panda.main.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goluk.crazy.panda.R;
import com.goluk.crazy.panda.main.fragment.FragmentAlbum;
import com.goluk.crazy.panda.utils.DeviceUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by DELL-PC on 2016/8/18.
 */
public class AlbumListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    enum ITEM_TYPE {
        ITEM_TYPE_IMAGE,
        ITEM_TYPE_TEXT
    }

    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private List mDataList;
    private int mDeviceWidth;

    public AlbumListAdapter(Context context, List dataList) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDataList = dataList;
        mDeviceWidth = DeviceUtils.getDeviceWith((Activity)context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal()) {
            return new ImageViewHolder(mLayoutInflater.inflate(R.layout.fragment_album_list_item_image, parent, false), mDeviceWidth);
        } else {
            return new TextViewHolder(mLayoutInflater.inflate(R.layout.fragment_album_list_item_text, parent, false), mDeviceWidth);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextViewHolder) {
            TextViewHolder tvHolder = (TextViewHolder)holder;
            FragmentAlbum.AlbumItemText item = (FragmentAlbum.AlbumItemText) mDataList.get(position);
            tvHolder.nListItemTextDateTV.setText(item.nDate);
            tvHolder.nListTextLocationTV.setText(item.nLocation);
        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder ivHolder = (ImageViewHolder)holder;
            FragmentAlbum.AlbumItemImage item = (FragmentAlbum.AlbumItemImage) mDataList.get(position);
            ivHolder.nListItemImageTV.setText(item.nDesciption);
            Glide.with(mContext).fromResource().load(item.nImagePath).placeholder(R.mipmap.test_today0).into(ivHolder.nListItemImageIV);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataList.get(position) instanceof FragmentAlbum.AlbumItemText) {
            return ITEM_TYPE.ITEM_TYPE_TEXT.ordinal();
        } else {
            return ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_fragment_album_list_item_image)
        ImageView nListItemImageIV;
        @BindView(R.id.tv_fragment_album_list_item_image)
        TextView nListItemImageTV;
        @BindView(R.id.rl_fragment_album_list_item_image)
        RelativeLayout nListItemImageRL;

        ImageViewHolder(View view, int deviceWidth) {
            super(view);
            ButterKnife.bind(this, view);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, deviceWidth / 3);
            nListItemImageIV.setLayoutParams(layoutParams);
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_fragment_album_list_item_text_date)
        TextView nListItemTextDateTV;
        @BindView(R.id.tv_fragment_album_list_text_location)
        TextView nListTextLocationTV;
        @BindView(R.id.ll_fragment_album_list_item_text)
        LinearLayout nAlbumListItemLL;

        TextViewHolder(View view, int deviceWidth) {
            super(view);
            ButterKnife.bind(this, view);
            StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(deviceWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            nAlbumListItemLL.setLayoutParams(layoutParams);
        }
    }
}
