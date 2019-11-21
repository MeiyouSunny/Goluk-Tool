package com.rd.veuisdk.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
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
import com.rd.gallery.IVideo;
import com.rd.gallery.ImageManager;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.ExtPhotoActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BucketListAdapter;
import com.rd.veuisdk.adapter.MediaListAdapter;
import com.rd.veuisdk.fragment.PhotoSelectFragment.IMediaSelector;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.BounceGridView;
import com.rd.veuisdk.ui.BucketListView;
import com.rd.veuisdk.ui.ExtProgressDialog;
import com.rd.veuisdk.utils.FileUtils;
import com.rd.veuisdk.utils.SysAlertDialog;
import com.rd.veuisdk.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoSelectFragment extends BaseV4Fragment {
    private MediaListAdapter mAdapter;
    private ExtProgressDialog mPdMediaScanning;
    private IImageList mIlVideos;

    private GalleryImageFetcher mGifVideoThumbnail; // 获取视频缩略图
    private ArrayList<ImageItem> mVideos = new ArrayList<ImageItem>();
    private ArrayList<String> mBucketIdList = new ArrayList<String>();
    private BucketListAdapter mBucketListAdapter;

    private IMediaSelector mMediaSelector;

    // 视频源列表
    private BounceGridView mGridVideosSelector;
    private RelativeLayout mRlNoVideos;
    private TextView tvBucketName;
    private ImageView ivSelectBucket;
    private IStateCallBack mStateCallBack;
    private MediaListAdapter.IAdapterListener mIAdapterListener;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mMediaSelector = (IMediaSelector) activity;
        mStateCallBack = (IStateCallBack) activity;
        mIAdapterListener = (MediaListAdapter.IAdapterListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = getString(R.string.select_media_title_video);
        initImageFetcher();
    }

    public void resetAdapter() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 实现获取视频缩略图相关
     */
    private void initImageFetcher() {
        ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
                Utils.VIDEO_THUMBNAIL_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.1f);
        mGifVideoThumbnail = new GalleryImageFetcher(getActivity(),
                getResources().getDimensionPixelSize(
                        R.dimen.video_list_grid_item_width), getResources()
                .getDimensionPixelSize(
                        R.dimen.video_list_grid_item_height));
        mGifVideoThumbnail.setLoadingImage(null);
        mGifVideoThumbnail.addImageCache(getActivity(), cacheParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.video_select_layout, container, false);
        mGridVideosSelector = $(R.id.gridVideosSelector);
        mRlNoVideos = $(R.id.rlNoVideos);
        tvBucketName = $(R.id.tvVideoBuckname);
        ivSelectBucket = $(R.id.ivVideoBucket);

        tvBucketName.setText(R.string.all_video);

        LinearLayout llVideoBucket = $(R.id.llVideoBucket);
        llVideoBucket.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        mAdapter = new MediaListAdapter(getActivity(),
                mGifVideoThumbnail, true);
        mAdapter.setIAdapterListener((MediaListAdapter.IAdapterListener) getActivity());
        mBucketListAdapter = new BucketListAdapter(getActivity(), true);
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
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        mGridVideosSelector.setAdapter(mAdapter);

        return mRoot;
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            onIImageItemClick(position);
        }
    };
    public static final int CODE_EXT_PIC = 105;

    /**
     * 响应 某照片点击事件
     *
     * @param position
     */
    private void onIImageItemClick(int position) {

        if (position == 0 && mIAdapterListener.isAppend()
                && !mStateCallBack.isHideText()) {
            ExtPhotoActivity.onTextPic(getActivity(), CODE_EXT_PIC);
        } else {
            if (mAdapter.getCount() > 0) {
                ImageItem item = mAdapter.getItem(position);
                item.selected = !item.selected;
                int returnType = mMediaSelector.addMediaItem(item);
                if (returnType == 0) {
                    mAdapter.notifyDataSetChanged();
                } else if (returnType == 2) {
                    item.selected = !item.selected;
                } else if (returnType == 1) {
                    mMediaSelector.onImport();
                }
                mMediaSelector.onRefreshCount();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mVideos.size() > 0) {
            mAdapter.addAll(mVideos);
        } else {
            loadMedias();
        }
    }

    private void loadMedias() {
        rebakeVideos(false, false, true);

    }

    /**
     * 执行视频列表获取
     *
     * @param unmounted
     * @param scanning
     */
    private void rebakeVideos(boolean unmounted, boolean scanning, boolean isFirst) {
        if (mIlVideos != null) {
            mIlVideos.close();
            mIlVideos = null;
        }

        if (mPdMediaScanning != null) {
            mPdMediaScanning.cancel();
            mPdMediaScanning = null;
        }

        if (scanning) {
            mPdMediaScanning = SysAlertDialog.showProgressDialog(
                    getActivity(), getResources().getString(R.string.wait),
                    true, true, null);
        }
        ArrayList<String> mBucketNameList = new ArrayList<>();
        ImageManager.ImageListParam ilpParam = ImageManager.allVideos(
                !unmounted && !scanning, true);
        mIlVideos = ImageManager.makeImageList(getActivity()
                .getContentResolver(), ilpParam);
        if (mIlVideos != null) {
            HashMap<String, String> hmAllBucketIds = mIlVideos.getBucketIds();
            for (Map.Entry<String, String> entry : hmAllBucketIds.entrySet()) {
                String strBucketId = entry.getKey();
                if (strBucketId == null) {
                    continue;
                }
                if (isFirst) {
                    mBucketIdList.add(strBucketId);
                    mBucketNameList.add(hmAllBucketIds.get(strBucketId));
                }
            }
            refreshGridViewData(true);
            mIlVideos.close();
        }

    }

    /**
     * 刷新Gridview数据，并获取所有视频item对象hash
     *
     * @param bGetVideoHashable
     * @return
     */
    private void refreshGridViewData(boolean bGetVideoHashable) {
        if (bGetVideoHashable) {
            SysAlertDialog.showLoadingDialog(getActivity(), R.string.isloading);
        }
        mVideos.clear();
        mGifVideoThumbnail.setExitTasksEarly(true);
        if (mIlVideos == null) {
            mGifVideoThumbnail.setExitTasksEarly(false);
            return;
        }
        for (int nTmp = 0; nTmp < mIlVideos.getCount(); nTmp++) {
            IImage videoInfo = null;
            try {
                videoInfo = mIlVideos.getImageAt(nTmp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (videoInfo == null || TextUtils.isEmpty(videoInfo.getDataPath())) {
                continue;
            }
            if (videoInfo.getId() <= 0 || ((IVideo) videoInfo).getDuration() < 1500) {
                continue;
            }
            File fv = new File(videoInfo.getDataPath());
            if (fv.exists() && !fv.getName().endsWith(".wmv")) {
                ImageItem ii = new ImageItem(videoInfo);
                mVideos.add(ii);
            }
        }

        mAdapter.addAll(mVideos);
        mGifVideoThumbnail.setExitTasksEarly(false);
        if (bGetVideoHashable) {
            SysAlertDialog.cancelLoadingDialog();
        }
        mhandler.sendEmptyMessage(GETVIDEO_NO);
    }

    private PopupWindow mPopupWindow;
    private View mPopupView;
    private long m_lLastClickTime;

    /**
     * 弹出分类窗口
     *
     * @param view
     */
    private void showPopupWindow(View view) {
        if (SystemClock.uptimeMillis() - m_lLastClickTime < 1000) {
            // 防止频繁调用
            return;
        }
        m_lLastClickTime = SystemClock.uptimeMillis();
        View contentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.popup_window, null);
        mPopupView = view;

        final ImageManager.ImageListParam ilpParam = ImageManager.allVideos(
                true, true);
        mIlVideos = ImageManager.makeImageList(getActivity()
                .getContentResolver(), ilpParam);

        mBucketIdList.clear();
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> bucketNameList = new ArrayList<>();
                HashMap<String, String> hmAllBucketIds = mIlVideos.getBucketIds();
                for (Map.Entry<String, String> entry : hmAllBucketIds.entrySet()) {
                    String strBucketId = entry.getKey();
                    if (strBucketId == null) {
                        continue;
                    }
                    ilpParam.mBucketId = strBucketId;
                    mIlVideos = ImageManager.makeImageList(getActivity().getContentResolver(), ilpParam);
                    if (null == mIlVideos) {
                        continue;
                    }
                    int count = 0;
                    for (int nTmp = 0; nTmp < mIlVideos.getCount(); nTmp++) {
                        IVideo videoInfo = (IVideo) mIlVideos.getImageAt(nTmp);
                        if (videoInfo == null || TextUtils.isEmpty(videoInfo.getDataPath())) {
                            continue;
                        }
                        if (videoInfo.getId() <= 0 || videoInfo.getDuration() < 1500) {
                            continue;
                        }
                        if (FileUtils.isExist(videoInfo.getDataPath())) {
                            count++;
                        }
                    }
                    if (count < 1) {
                        continue;
                    }
                    mBucketIdList.add(strBucketId);
                    bucketNameList.add(hmAllBucketIds.get(strBucketId));
                }
                mhandler.obtainMessage(SHOW_POPUP, bucketNameList).sendToTarget();
            }
        });
        BucketListView lvBuckets = Utils.$(contentView, R.id.lvBucket);
        lvBuckets.setAdapter(mBucketListAdapter);

        mPopupWindow = new PopupWindow(contentView, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, true);

        lvBuckets.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0) {
                    tvBucketName.setText(R.string.allvideo);
                    rebakeVideos(false, false, false);
                } else {
                    tvBucketName.setText(mBucketListAdapter.getItem(position - 1));
                    ImageManager.ImageListParam ilpParam = ImageManager
                            .allVideos(true, false);

                    ilpParam.mBucketId = mBucketIdList.get(position - 1); // 某个分类id,获取指定分类的视频或图片
                    mIlVideos = ImageManager.makeImageList(getActivity()
                            .getContentResolver(), ilpParam);
                    refreshGridViewData(true);
                }
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.setTouchable(true);

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        ColorDrawable colorDrawable = new ColorDrawable(getResources()
                .getColor(R.color.white));
        mPopupWindow.setBackgroundDrawable(colorDrawable);

        mPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                ivSelectBucket.setImageResource(R.drawable.select_bucket_dropdown);
            }
        });
    }


    private final int GETVIDEO_NO = 6;
    private final int SHOW_POPUP = 11;
    private Handler mhandler = new Handler(Looper.getMainLooper()) {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GETVIDEO_NO:
                    if (getActivity() != null && !getActivity().isDestroyed()) {
                        mRlNoVideos
                                .setVisibility(mAdapter.getCount() > 0 ? View.GONE
                                        : View.VISIBLE);
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


    @Override
    public void onDestroy() {
        if (null != mIlVideos) {
            mIlVideos.close();
            mIlVideos = null;
        }
        mAdapter.recycle();
        mGridVideosSelector.setAdapter(null);
        mGifVideoThumbnail.cleanUpCache();
        mGifVideoThumbnail = null;
        super.onDestroy();
    }

    public void refresh() {
        tvBucketName.setText(R.string.all_video);
        rebakeVideos(false, false, false);
    }

}
