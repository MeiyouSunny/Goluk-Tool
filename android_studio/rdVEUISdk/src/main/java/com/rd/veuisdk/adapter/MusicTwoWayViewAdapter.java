package com.rd.veuisdk.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rd.cache.GalleryImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.veuisdk.R;
import com.rd.veuisdk.model.MusicItemData;
import com.rd.veuisdk.utils.Utils;

import java.util.List;

public class MusicTwoWayViewAdapter extends BaseAdapter {

    private static final String TAG = "--MusicTwoWayViewAdapter-->";

    // 获取视频缩略图
    protected GalleryImageFetcher mImageFetcher;

    private Context mContext;

    private ViewHolder mHolder = null;

    private List<MusicItemData> mMusicItemDatas;

    /**
     * 配乐的比例调整
     */
    public interface OnMusicTrackClickListener {

        // 点击显示比例调整的界面
        void onMusicTrackClick();
    }

    private OnMusicTrackClickListener mOnMusicClickListener;

    private int mTxColorChecked, mTxColor;

    private int mCheckIndex = -1;

    public void setCheckIndex(int _checkIndex) {
        mCheckIndex = _checkIndex;
        notifyDataSetChanged();
    }

    public int getCheckIndex() {
        return mCheckIndex;
    }

    public MusicItemData getCheckObject() {
        if (mCheckIndex >= 0 && mCheckIndex <= mMusicItemDatas.size() - 1) {
            return getItem(mCheckIndex);
        }
        return null;

    }

    public void setOnMusicTrackClicListener(OnMusicTrackClickListener mOnMusicClickListener) {
        this.mOnMusicClickListener = mOnMusicClickListener;
    }

    private LayoutInflater mInflater;

    public MusicTwoWayViewAdapter(Context mContext, List<MusicItemData> list) {
        this.mContext = mContext;
        mCheckIndex = -1;
        Resources res = mContext.getResources();
        mTxColorChecked = res.getColor(R.color.white);
        mTxColor = res.getColor(R.color.border_no_checked);
        mInflater = LayoutInflater.from(mContext);
        this.mMusicItemDatas = list;
        initImageFetcher();
    }

    public void updataMusicItemData(List<MusicItemData> datas) {

        this.mMusicItemDatas = datas;
    }

    @Override
    public int getCount() {
        return mMusicItemDatas.size();
    }

    @Override
    public MusicItemData getItem(int position) {
        return mMusicItemDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            mHolder = new ViewHolder();

            convertView = mInflater.inflate(
                    R.layout.rdveuisdk_music_horizontal_item_layout, null);

            mHolder.thumbnail = (ImageView) convertView
                    .findViewById(R.id.iv_music_item_thubmnail);

            mHolder.title = (TextView) convertView
                    .findViewById(R.id.tv_music_item_text);

            mHolder.selectedView = (ImageView) convertView
                    .findViewById(R.id.iv_music_item_selected);

            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        MusicItemData data = (MusicItemData) getItem(position);

        mHolder.title.setText(data.getTitle());
        mHolder.title.setBackgroundColor(data.isSelected() ? mContext
                .getResources().getColor(R.color.music_text_selected) : mContext
                .getResources().getColor(R.color.sub_menu_bgcolor));

        if (mImageFetcher != null) {

            mImageFetcher.loadBitmapForAlbumArt(data.getPath(),
                    mHolder.thumbnail);
        }

        if (mCheckIndex == position) {
            mHolder.title.setTextColor(mTxColorChecked);

        } else {
            mHolder.title.setTextColor(mTxColor);
        }

        mHolder.selectedView.setVisibility(data.isSelected() ? View.VISIBLE
                : View.GONE);

        if (mHolder.selectedView.getVisibility() == View.VISIBLE) {

            mHolder.selectedView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mOnMusicClickListener != null) {
                        mOnMusicClickListener.onMusicTrackClick();
                    }
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {

        public ImageView thumbnail;

        public TextView title;

        public ImageView selectedView;

    }

    /**
     * 实现获取视频缩略图相关
     */
    private void initImageFetcher() {
        ImageCacheParams cacheParams = new ImageCacheParams(mContext,
                Utils.VIDEO_THUMBNAIL_CACHE_DIR);
        // 缓冲占用系统内存的25%
        cacheParams.setMemCacheSizePercent(0.15f);
        cacheParams.setFormat(CompressFormat.PNG);
        mImageFetcher = new GalleryImageFetcher(mContext, mContext.getResources()
                .getDimensionPixelSize(R.dimen.music_item_width), mContext
                .getResources()
                .getDimensionPixelSize(R.dimen.music_item_height));
        mImageFetcher.setLoadingImage(R.drawable.music_item_thumbnail);
        mImageFetcher.addImageCache(mContext, cacheParams);
    }

}
