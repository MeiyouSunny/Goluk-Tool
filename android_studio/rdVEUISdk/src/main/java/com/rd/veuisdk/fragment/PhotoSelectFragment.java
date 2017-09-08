package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rd.cache.GalleryImageFetcher;
import com.rd.cache.ImageCache.ImageCacheParams;
import com.rd.gallery.IImage;
import com.rd.gallery.IImageList;
import com.rd.gallery.ImageManager;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.ExtPhotoActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.adapter.BucketListAdapter;
import com.rd.veuisdk.adapter.MediaListAdapter;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.BounceGridView;
import com.rd.veuisdk.ui.BucketListView;
import com.rd.veuisdk.ui.SubFunctionUtils;
import com.rd.veuisdk.utils.StorageUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PhotoSelectFragment extends BaseV4Fragment {
    private final String TAG = "PhotoSelectFragment";
    private MediaListAdapter mAdapterMedias;
    private boolean mMediaLoading, mMediaBreakLoad; // 媒体加载中...
    private GalleryImageFetcher mGifVideoThumbnail; // 获取视频缩略图
    private SparseArray<IImage> mPhotoSelected = new SparseArray<IImage>();
    private ArrayList<ImageItem> mPhotos = new ArrayList<ImageItem>();
    private IImageList ilTmp;

    private ArrayList<String> mBucketNameList = new ArrayList<String>();
    private ArrayList<String> mBucketIdList = new ArrayList<String>();
    private BucketListAdapter mBucketListAdapter;
    private IMediaSelector mMediaSelector;

    // 视频源列表
    BounceGridView mGridVideosSelector;
    RelativeLayout mRlNoVideos;
    TextView tvBucketName;
    ImageView ivSelectBucket;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMediaSelector = (IMediaSelector) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.select_media_title_photo);
        initImageFetcher();
        mAdapterMedias = new MediaListAdapter(getActivity(), mGifVideoThumbnail);
    }

    public void resetAdapter() {
        if (mAdapterMedias != null) {
            mAdapterMedias.notifyDataSetChanged();
        }
    }

    /**
     * 实现获取视频缩略图相关
     */
    private void initImageFetcher() {
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
                Utils.VIDEO_THUMBNAIL_CACHE_DIR);
        // 缓冲占用系统内存的25%
        cacheParams.setMemCacheSizePercent(0.05f);

        mGifVideoThumbnail = new GalleryImageFetcher(getActivity(),
                getResources().getDimensionPixelSize(
                        R.dimen.video_list_grid_item_width), getResources()
                .getDimensionPixelSize(
                        R.dimen.video_list_grid_item_height));

        mGifVideoThumbnail.setLoadingImage(null);
        mGifVideoThumbnail.addImageCache(getActivity(), cacheParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.photo_select_layout, null);

        mGridVideosSelector = (BounceGridView) findViewById(R.id.gridVideosSelector);
        mRlNoVideos = (RelativeLayout) findViewById(R.id.rlNoVideos);
        tvBucketName = (TextView) findViewById(R.id.tvPhotoBuckname);
        ivSelectBucket = (ImageView) findViewById(R.id.ivSelectBucket);

        tvBucketName.setText(R.string.ablum);

        LinearLayout llPhotoBucket = (LinearLayout) mRoot.findViewById(R.id.llPhotoBucket);
        llPhotoBucket.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        mPhotoSelected.clear();

        mGridVideosSelector.setOnItemClickListener(itemClickListener);
        mGridVideosSelector.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view,
                                             int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    mGifVideoThumbnail.setPauseWork(true);
                } else {
                    mGifVideoThumbnail.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        });
        mGridVideosSelector.setAdapter(mAdapterMedias);

        mBucketListAdapter = new BucketListAdapter(getActivity(),
                mBucketNameList, false);

        return mRoot;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPhotos.size() > 0) {
            mAdapterMedias.addAll(mPhotos);
        } else {
            loadMedias();
        }
    }

    private void loadMedias() {
        rebakePhotos(true);
    }

    /**
     * 加载图片
     */
    protected void rebakePhotos(final boolean isDCIM) {
        ThreadPoolUtils.executeEx(new Runnable() {
            public void run() {
                doLoadPhotoBuckets(isDCIM);
            }
        });
    }

    protected void cancelLoadPhotos() {
        synchronized (this) {
            mMediaBreakLoad = true;
        }
    }

    protected void doLoadPhotoBuckets(boolean isDCIM) {
        synchronized (this) {
            if (mMediaLoading) {
                return;
            }
            mMediaLoading = true;
            mMediaBreakLoad = false;
            mPhotos.clear();
        }
        if (getActivity() == null) {
            return;
        }
        ImageManager.ImageListParam ilpParam = ImageManager.allPhotos(
                StorageUtils.isAvailable(false), true);
        ilTmp = ImageManager.makeImageList(getActivity().getContentResolver(),
                ilpParam);
        HashMap<String, String> hmBucketIds;
        if (isDCIM) {
            hmBucketIds = ilTmp.getDCIMBucketIds();
        } else {
            hmBucketIds = ilTmp.getBucketIds();
        }
        ilTmp.close();

        for (Map.Entry<String, String> entry : hmBucketIds.entrySet()) {
            final String strBucketId = entry.getKey();
            if (strBucketId == null) {
                continue;
            }
            loadPhotoList(strBucketId);
            synchronized (this) {
                if (mMediaBreakLoad) {
                    break;
                }
            }
        }

        synchronized (this) {
            mMediaLoading = false;
        }

        if (null == mPhotos || mPhotos.size() == 0) {
            mhandler.sendEmptyMessage(GETPHOTO_NO);
        } else {
            mhandler.sendEmptyMessage(GETPHOTO_YES);
        }
    }

    private final int GETPHOTO_NO = 5;
    private final int GETPHOTO_YES = 6;
    private final int SHOW_POPUP = 11;

    private Handler mhandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GETPHOTO_NO:
                    mRlNoVideos.setVisibility(View.VISIBLE);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(photosRunnable);
                    }
                    break;
                case GETPHOTO_YES:
                    mRlNoVideos.setVisibility(View.GONE);
                    break;
                case SHOW_POPUP:
                    mPopupWindow.showAsDropDown(mPopupView);
                    ivSelectBucket.setImageResource(R.drawable.select_bucket_dropup);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 加载指定bucket图片
     *
     * @param strBucketId
     */
    protected synchronized void loadPhotoList(String strBucketId) {
        Activity activity = getActivity();
        if (activity != null) {
            ImageManager.ImageListParam ilpParam = ImageManager
                    .allPhotos(StorageUtils.isAvailable(false));
            ilpParam.mBucketId = strBucketId;
            IImageList ilImages = ImageManager.makeImageList(
                    activity.getContentResolver(), ilpParam);
            try {
                for (int nTmp = 0; nTmp < ilImages.getCount(); nTmp++) {
                    final IImage img = ilImages.getImageAt(nTmp);
                    ImageItem ii = new ImageItem(img);
                    if (img.isValid()) {
                        mPhotos.add(ii);
                        ii.selected = mPhotoSelected.get(ii.imageItemKey) != null;
                        if (ii.selected) {
                            mMediaSelector.replaceItem(ii);
                            mPhotoSelected.append(ii.imageItemKey, ii.image);
                        }
                    }
                    synchronized (this) {
                        if (mMediaBreakLoad || getActivity() == null) {
                            return;
                        }
                    }
                }
                // 图片按日期降序排序
                Collections.sort(mPhotos, imageComparator);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(photosRunnable);
                }
            } finally {
                ilImages.close();
            }
        }
    }

    private Runnable photosRunnable = new Runnable() {

        @Override
        public void run() {
            mAdapterMedias.addAll(mPhotos);
        }
    };

    /**
     * 日期降序比较器
     */
    private static Comparator<ImageItem> imageComparator = new Comparator<ImageItem>() {

        @Override
        public int compare(ImageItem lhs, ImageItem rhs) {
            return lhs.image.getDateTaken() > rhs.image.getDateTaken() ? -1 : 1;
        }
    };

    public void onBackPressed() {
        cancelLoadPhotos();
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            onIImageItemClick(view, position);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mGifVideoThumbnail.setExitTasksEarly(false);
    }

    ;

    @Override
    public void onPause() {
        super.onPause();
        cancelLoadPhotos();
        mGifVideoThumbnail.setPauseWork(false);
        mGifVideoThumbnail.setExitTasksEarly(true);
        mGifVideoThumbnail.flushCache();
    }

    public SparseArray<IImage> getMedia() {
        return mPhotoSelected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mGridVideosSelector.setAdapter(null);
        mGifVideoThumbnail.closeCache();
        mGifVideoThumbnail = null;
        super.onDestroy();
    }

    /**
     * 响应 某照片点击事件
     *
     * @param view
     * @param position
     */
    protected void onIImageItemClick(View view, int position) {
        if (position == 0 && SelectMediaActivity.mIsAppend
                && !SubFunctionUtils.isHideText()) {

            getActivity().startActivityForResult(
                    new Intent(getActivity(), ExtPhotoActivity.class),
                    VideoSelectFragment.CODE_EXT_PIC);

        } else {
            if (mAdapterMedias.getCount() > 0) {
                ImageItem item = mAdapterMedias.getItem(position);
                if (item != null) {
                    item.selected = !item.selected;
                    mAdapterMedias.refreashItemSelectedState(view, item);
                    if (item.selected) {
                        if (mPhotoSelected.get(item.imageItemKey) == null) {
                            int returnType = mMediaSelector.addMediaItem(item);
                            if (returnType == 0) { // 正常选取
                                mPhotoSelected.append(item.imageItemKey,
                                        item.image);
                                mAdapterMedias.notifyDataSetChanged();
                            } else if (returnType == 2) { // 选择到了上限
                                item.selected = !item.selected;
                                mAdapterMedias.refreashItemSelectedState(
                                        view, item);
                            } else if (returnType == 1) { // 上限为1，选取后直接确定
                                mPhotoSelected.append(item.imageItemKey,
                                        item.image);
                                mMediaSelector.onImport();
                            }
                            mMediaSelector.onRefreshCount();
                        }
                    } else {
                        if (mPhotoSelected.get(item.imageItemKey) != null) {
                            mPhotoSelected.remove(item.imageItemKey);
                            mMediaSelector.removeMediaItem(item);
                            mMediaSelector.resetPosition();
                            mAdapterMedias.notifyDataSetChanged();
                            mMediaSelector.onRefreshCount();
                        }
                    }
                }
            }
        }
    }

    private PopupWindow mPopupWindow;
    private View mPopupView;

    /**
     * 弹出分类窗口
     *
     * @param view
     * @return
     */
    private void showPopupWindow(View view) {
        mPopupView = view;
        View contentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.popup_window, null);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
                ImageManager.ImageListParam ilpParam = ImageManager.allPhotos(
                        StorageUtils.isAvailable(false), true);
                ilTmp = ImageManager.makeImageList(getActivity()
                        .getContentResolver(), ilpParam);
                HashMap<String, String> hmBucketIds = ilTmp.getBucketIds();
                ilTmp.close();
                mBucketNameList.clear();
                mBucketIdList.clear();

                for (Map.Entry<String, String> entry : hmBucketIds.entrySet()) {
                    String strBucketId = entry.getKey();
                    if (strBucketId == null) {
                        continue;
                    }
                    mBucketIdList.add(strBucketId);
                    mBucketNameList.add(hmBucketIds.get(strBucketId));
                }
                mhandler.sendEmptyMessage(SHOW_POPUP);
            }
        });
        thread.start();

        BucketListView lv = (BucketListView) (contentView
                .findViewById(R.id.lvBucket));
        lv.setAdapter(mBucketListAdapter);
        mPopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 第一行显示全部图片
                if (position == 0) {
                    tvBucketName.setText(R.string.allphoto);

                    rebakePhotos(false);
                } else if (position == 1) {

                    tvBucketName.setText(R.string.ablum);

                    rebakePhotos(true);
                } else {
                    tvBucketName.setText(mBucketNameList.get(position - 2));

                    mPhotos.clear();
                    mMediaBreakLoad = false;
                    loadPhotoList(mBucketIdList.get(position - 2));
                }
                mAdapterMedias.notifyDataSetChanged();
                if (null == mPhotos || mPhotos.size() == 0) {
                    mhandler.sendEmptyMessage(GETPHOTO_NO);
                } else {
                    mhandler.sendEmptyMessage(GETPHOTO_YES);
                }
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.setTouchable(true);

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        ColorDrawable colorDrawable = new ColorDrawable(getResources()
                .getColor(R.color.white));
        colorDrawable.setAlpha(0);
        mPopupWindow.setBackgroundDrawable(colorDrawable);

        mPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                ivSelectBucket
                        .setImageResource(R.drawable.select_bucket_dropdown);
            }
        });

    }

    public void refresh() {

        tvBucketName.setText(R.string.ablum);

        rebakePhotos(true);
    }

    public void showPopup(View v) {
        showPopupWindow(v);
    }

    /**
     * 媒体选择器抽象接口<br>
     * 用于fragement交互
     *
     * @author abreal
     */
    public static interface IMediaSelector {
        /**
         * 添加媒体项，主要是媒体项的唯一标识值
         *
         * @param item
         */
        int addMediaItem(ImageItem item);

        /**
         * 添加通知相册直接返回
         */
        void onImport();

        /**
         * 添加通知相册更新计数
         */
        void onRefreshCount();

        /**
         * 删除媒体项，主要是媒体项的唯一标识值
         *
         * @param item
         */
        void removeMediaItem(ImageItem item);

        /**
         * 重置媒体项的顺序
         */
        void resetPosition();

        /**
         * 根据imageItemKey，将list的旧媒体项替换为新加载的媒体项
         */
        void replaceItem(ImageItem item);

    }
}
