package com.rd.veuisdk.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.rd.veuisdk.adapter.BucketListAdapter;
import com.rd.veuisdk.adapter.MediaListAdapter;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.BounceGridView;
import com.rd.veuisdk.ui.BucketListView;
import com.rd.veuisdk.utils.StorageUtils;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotoSelectFragment extends BaseV4Fragment {
    private final String TAG = "PhotoSelectFragment";
    private MediaListAdapter mAdapterMedias;
    private boolean mMediaLoading, mMediaBreakLoad; // 媒体加载中...
    private GalleryImageFetcher mGifVideoThumbnail; // 获取视频缩略图
    private ArrayList<ImageItem> mPhotos = new ArrayList<ImageItem>();
    private IImageList ilTmp;

    private ArrayList<String> mBucketIdList = new ArrayList<>();
    private BucketListAdapter mBucketListAdapter;
    private IMediaSelector mMediaSelector;

    // 视频源列表
    private BounceGridView mGridVideosSelector;
    private RelativeLayout mRlNoVideos;
    private TextView tvBucketName;
    private ImageView ivSelectBucket;
    private IStateCallBack mStateCallBack;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMediaSelector = (IMediaSelector) context;
        mStateCallBack = (IStateCallBack) context;
        mIAdapterListener = (MediaListAdapter.IAdapterListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.select_media_title_photo);
        initImageFetcher();

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
        // 缓冲占用系统内存的5%
        cacheParams.setMemCacheSizePercent(0.05f);

        mGifVideoThumbnail = new GalleryImageFetcher(getActivity(),
                getResources().getDimensionPixelSize(
                        R.dimen.video_list_grid_item_width), getResources()
                .getDimensionPixelSize(
                        R.dimen.video_list_grid_item_height));

        mGifVideoThumbnail.setLoadingImage(null);
        mGifVideoThumbnail.addImageCache(getActivity(), cacheParams);
    }

    private MediaListAdapter.IAdapterListener mIAdapterListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.photo_select_layout, container, false);
        mAdapterMedias = new MediaListAdapter(getActivity(), mGifVideoThumbnail, mStateCallBack.isHideText());
        mAdapterMedias.setIAdapterListener(mIAdapterListener);
        mGridVideosSelector = $(R.id.gridVideosSelector);
        mRlNoVideos = $(R.id.rlNoVideos);
        tvBucketName = $(R.id.tvPhotoBuckname);
        ivSelectBucket = $(R.id.ivSelectBucket);
        tvBucketName.setText(R.string.album);
        LinearLayout llPhotoBucket = $(R.id.llPhotoBucket);
        llPhotoBucket.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });
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
                false);

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

    public void cancelLoadPhotos() {
        mMediaBreakLoad = true;
    }

    private void doLoadPhotoBuckets(boolean isDCIM) {
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
            String strBucketId = entry.getKey();
            if (strBucketId == null) {
                continue;
            }
            loadPhotoList(strBucketId);
            if (mMediaBreakLoad) {
                break;
            }
        }
        synchronized (this) {
            mMediaLoading = false;
        }
        if (!mMediaBreakLoad) {
            udpateUI();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void udpateUI() {
        if (null != getActivity() && !getActivity().isDestroyed()) {
            if (null == mPhotos || mPhotos.size() == 0) {
                mhandler.sendEmptyMessage(GETPHOTO_NO);
            } else {
                mhandler.sendEmptyMessage(GETPHOTO_YES);
            }
        }
    }

    private final int GETPHOTO_NO = 5;
    private final int GETPHOTO_YES = 6;
    private final int SHOW_POPUP = 11;

    private Handler mhandler = new Handler(Looper.getMainLooper()) {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GETPHOTO_NO:
                    if (getActivity() != null && !getActivity().isDestroyed()) {
                        mRlNoVideos.setVisibility(View.VISIBLE);
                        getActivity().runOnUiThread(photosRunnable);
                    }
                    break;
                case GETPHOTO_YES:
                    if (getActivity() != null && !getActivity().isDestroyed()) {
                        mRlNoVideos.setVisibility(View.GONE);
                        getActivity().runOnUiThread(photosRunnable);
                    }
                    break;
                case SHOW_POPUP:
                    if (getActivity() != null && !getActivity().isDestroyed()) {
                        List<String> list = (List<String>) msg.obj;
                        mBucketListAdapter.update(list);
                        mPopupWindow.showAsDropDown(mPopupView);
                        ivSelectBucket.setImageResource(R.drawable.select_bucket_dropup);
                    }
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
    private synchronized void loadPhotoList(String strBucketId) {
        Activity activity = getActivity();
        if (activity != null) {
            ImageManager.ImageListParam ilpParam = ImageManager
                    .allPhotos(StorageUtils.isAvailable(false));
            ilpParam.mBucketId = strBucketId;
            IImageList ilImages = ImageManager.makeImageList(
                    activity.getContentResolver(), ilpParam);
            try {
                if (null != ilImages) {
                    for (int nTmp = 0; nTmp < ilImages.getCount(); nTmp++) {
                        IImage img = ilImages.getImageAt(nTmp);
                        if (img.isValid()) {
                            mPhotos.add(new ImageItem(img));
                        }
                        if (mMediaBreakLoad || getActivity() == null) {
                            return;
                        }
                    }
                    // 图片按日期降序排序
                    Collections.sort(mPhotos, imageComparator);
                }
            } finally {
                if (null != ilImages) {
                    ilImages.close();
                }
            }
            udpateUI();
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
            long l1 = lhs.image.getDateTaken();
            long l2 = rhs.image.getDateTaken();
            return l1 > l2 ? -1 : ((l1 == l2) ? 0 : 1);
        }
    };

    public void onBackPressed() {
        cancelLoadPhotos();
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            onIImageItemClick(position);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        mGifVideoThumbnail.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelLoadPhotos();
        mGifVideoThumbnail.setPauseWork(false);
        mGifVideoThumbnail.setExitTasksEarly(true);
        mGifVideoThumbnail.flushCache();
    }


    @Override
    public void onDestroy() {
        mAdapterMedias.recycle();
        mGridVideosSelector.setAdapter(null);
        mGifVideoThumbnail.cleanUpCache();
        mGifVideoThumbnail = null;
        super.onDestroy();
    }

    /**
     * 响应 某照片点击事件
     *
     * @param position
     */
    private void onIImageItemClick(int position) {
        if (position == 0 && mIAdapterListener.isAppend() && !mStateCallBack.isHideText()) {
            ExtPhotoActivity.onTextPic(getContext(), VideoSelectFragment.CODE_EXT_PIC);
        } else {
            if (mAdapterMedias.getCount() > 0) {
                ImageItem item = mAdapterMedias.getItem(position);
                if (item != null) {
                    item.selected = !item.selected;
                    int returnType = mMediaSelector.addMediaItem(item);
                    if (returnType == 0) { // 正常选取
                        mAdapterMedias.notifyDataSetChanged();
                    } else if (returnType == 2) { // 选择到了上限
                        item.selected = !item.selected;
                    } else if (returnType == 1) { // 上限为1，选取后直接确定
                        mMediaSelector.onImport();
                    }
                    mMediaSelector.onRefreshCount();
                }
            }
        }
    }

    private PopupWindow mPopupWindow;
    private View mPopupView;

    /**
     * 弹出分类窗口
     */
    private void showPopupWindow(View view) {
        mPopupView = view;
        View contentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.popup_window, null);
        mhandler.removeMessages(SHOW_POPUP);
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }
                ArrayList<String> bucketNameList = new ArrayList<>();
                ImageManager.ImageListParam ilpParam = ImageManager.allPhotos(
                        StorageUtils.isAvailable(false), true);
                ilTmp = ImageManager.makeImageList(getActivity()
                        .getContentResolver(), ilpParam);
                HashMap<String, String> hmBucketIds = ilTmp.getBucketIds();
                ilTmp.close();
                mBucketIdList.clear();
                for (Map.Entry<String, String> entry : hmBucketIds.entrySet()) {
                    String strBucketId = entry.getKey();
                    if (strBucketId == null) {
                        continue;
                    }
                    mBucketIdList.add(strBucketId);
                    bucketNameList.add(hmBucketIds.get(strBucketId));
                }
                mhandler.obtainMessage(SHOW_POPUP, bucketNameList).sendToTarget();
            }
        });
        BucketListView lv = Utils.$(contentView, R.id.lvBucket);
        lv.setAdapter(mBucketListAdapter);
        mPopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                // 第一行显示全部图片
                if (position == 0) {
                    tvBucketName.setText(R.string.allphoto);
                    rebakePhotos(false);
                } else if (position == 1) {
                    tvBucketName.setText(R.string.album);
                    rebakePhotos(true);
                } else {
                    final int index = position - 2;
                    tvBucketName.setText(mBucketListAdapter.getItem(index));
                    mPhotos.clear();
                    mMediaBreakLoad = false;
                    ThreadPoolUtils.executeEx(new Runnable() {
                        @Override
                        public void run() {
                            loadPhotoList(mBucketIdList.get(index));
                        }
                    });
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
        tvBucketName.setText(R.string.album);
        rebakePhotos(true);
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


    }
}
