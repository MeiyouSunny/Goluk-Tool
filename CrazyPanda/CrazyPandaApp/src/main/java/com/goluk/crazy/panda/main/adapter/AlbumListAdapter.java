package com.goluk.crazy.panda.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goluk.crazy.panda.R;
import com.goluk.crazy.panda.main.fragment.FragmentAlbum;

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

    public AlbumListAdapter(Context context, List dataList) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mDataList = dataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_IMAGE.ordinal()) {
            return new ImageViewHolder(mLayoutInflater.inflate(R.layout.fragment_album_list_item_image, parent, false));
        } else {
            return new TextViewHolder(mLayoutInflater.inflate(R.layout.fragment_album_list_item_text, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TextViewHolder) {
            TextViewHolder tvHolder = (TextViewHolder)holder;
            FragmentAlbum.AlbumItemText item = (FragmentAlbum.AlbumItemText) mDataList.get(position);
            tvHolder.tvFragmentAlbumListItemTextDate.setText(item.nDate);
            tvHolder.tvFragmentAlbumListTextLocation.setText(item.nLocation);
        } else if (holder instanceof ImageViewHolder) {
            ImageViewHolder ivHolder = (ImageViewHolder)holder;
            FragmentAlbum.AlbumItemImage item = (FragmentAlbum.AlbumItemImage) mDataList.get(position);
            ivHolder.tvFragmentAlbumListItemImage.setText(item.nDesciption);
            Glide.with(mContext).fromResource().load(item.nImagePath).placeholder(R.mipmap.test_today0).into(ivHolder.ivFragmentAlbumListItemImage);
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
        ImageView ivFragmentAlbumListItemImage;
        @BindView(R.id.tv_fragment_album_list_item_image)
        TextView tvFragmentAlbumListItemImage;

        ImageViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class TextViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_fragment_album_list_item_text_date)
        TextView tvFragmentAlbumListItemTextDate;
        @BindView(R.id.tv_fragment_album_list_text_location)
        TextView tvFragmentAlbumListTextLocation;
        LinearLayout nAlbumListItem;

        TextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            nAlbumListItem = (LinearLayout) view.findViewById(R.id.ll_fragment_album_list_item_text);
            StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(1080, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            nAlbumListItem.setLayoutParams(layoutParams);
        }
    }
}
