package com.rd.veuisdk.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
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

import com.rd.gallery.IImage;
import com.rd.gallery.IImageList;
import com.rd.gallery.IVideo;
import com.rd.gallery.ImageManager;
import com.rd.veuisdk.ExtPhotoActivity;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SelectMediaActivity;
import com.rd.veuisdk.adapter.BucketListAdapter;
import com.rd.veuisdk.adapter.MediaListAdapter;
import com.rd.veuisdk.fragment.PhotoSelectFragment.IMediaSelector;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.ui.BounceGridView;
import com.rd.veuisdk.ui.BucketListView;
import com.rd.veuisdk.ui.ExtProgressDialog;
import com.rd.veuisdk.ui.SubFunctionUtils;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoSelectFragment extends BaseV4Fragment {
    private final String TAG = "VideoSelectFragment";
    private MediaListAdapter mAdapterMedias;

    private ExtProgressDialog mPdMediaScanning;
    private IImageList mIlVideos;

    private SparseArray<IImage> mVideoSelected = new SparseArray<IImage>();
    private ArrayList<ImageItem> mVideos = new ArrayList<ImageItem>();
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
        mPageName = getString(R.string.select_media_title_video);
    }

    public void resetAdapter() {
        if (mAdapterMedias != null) {
            mAdapterMedias.notifyDataSetChanged();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.video_select_layout, null);

        mGridVideosSelector = (BounceGridView) findViewById(R.id.gridVideosSelector);
        mRlNoVideos = (RelativeLayout) findViewById(R.id.rlNoVideos);
        tvBucketName = (TextView) findViewById(R.id.tvVideoBuckname);
        ivSelectBucket = (ImageView) findViewById(R.id.ivVideoBucket);

        tvBucketName.setText(R.string.all_video);

        LinearLayout llVideoBucket = (LinearLayout) findViewById(R.id.llVideoBucket);
        llVideoBucket.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        mAdapterMedias = new MediaListAdapter(getActivity()
        );
        mBucketListAdapter = new BucketListAdapter(getActivity(),
                mBucketNameList, true);
        mVideoSelected.clear();

        mGridVideosSelector.setOnItemClickListener(itemClickListener);
        mGridVideosSelector.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view,
                                             int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                } else {
                }
            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }
        });
        mGridVideosSelector.setAdapter(mAdapterMedias);

        return mRoot;
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            onIImageItemClick(view, position);
        }
    };
    public static final int CODE_EXT_PIC = 105;

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
                    CODE_EXT_PIC);

        } else {
            if (mAdapterMedias.getCount() > 0) {
                ImageItem item = mAdapterMedias.getItem(position);
                item.selected = !item.selected;
                mAdapterMedias.refreashItemSelectedState(view, item);
                if (item.selected) {
                    if (mVideoSelected.get(item.imageItemKey) == null) {
                        int returnType = mMediaSelector.addMediaItem(item);
                        if (returnType == 0) {
                            mVideoSelected
                                    .append(item.imageItemKey, item.image);
                            mAdapterMedias.notifyDataSetChanged();
                        } else if (returnType == 2) {
                            item.selected = !item.selected;
                            mAdapterMedias.refreashItemSelectedState(view,
                                    item);
                        } else if (returnType == 1) {
                            mVideoSelected
                                    .append(item.imageItemKey, item.image);
                            mMediaSelector.onImport();
                        }
                        mMediaSelector.onRefreshCount();
                    }
                } else {
                    if (mVideoSelected.get(item.imageItemKey) != null) {
                        mVideoSelected.remove(item.imageItemKey);
                        mMediaSelector.removeMediaItem(item);
                        mMediaSelector.resetPosition();
                        mAdapterMedias.notifyDataSetChanged();
                        mMediaSelector.onRefreshCount();
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mVideos.size() > 0) {
            mAdapterMedias.addAll(mVideos);
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
    private void rebakeVideos(boolean unmounted, boolean scanning,
                              boolean isFirst) {
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
        ImageManager.ImageListParam ilpParam = ImageManager.allVideos(
                !unmounted && !scanning, true);
        mIlVideos = ImageManager.makeImageList(getActivity()
                .getContentResolver(), ilpParam);
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

    /**
     * 刷新Gridview数据，并获取所有视频item对象hash
     *
     * @param bGetVideoHashable
     * @return
     */
    private void refreshGridViewData(final boolean bGetVideoHashable) {
        if (bGetVideoHashable) {
            SysAlertDialog.showLoadingDialog(getActivity(), R.string.isloading);
        }
        mVideos.clear();
        if (mIlVideos == null) {
            return;
        }
        for (int nTmp = 0; nTmp < mIlVideos.getCount(); nTmp++) {
            IVideo videoInfo = (IVideo) mIlVideos.getImageAt(nTmp);
            if (videoInfo == null || TextUtils.isEmpty(videoInfo.getDataPath())) {
                continue;
            }
            if (videoInfo.getId() <= 0 || videoInfo.getDuration() < 1500) {
                // || videoInfo.getWidth() == 0 || videoInfo.getHeight() == 0
                continue;
            }
            if (new File(videoInfo.getDataPath()).exists()) {
                ImageItem ii = new ImageItem(videoInfo);
                mVideos.add(ii);
                ii.selected = mVideoSelected.get(ii.imageItemKey) != null;
                if (ii.selected) {
                    mMediaSelector.replaceItem(ii);
                    mVideoSelected.append(ii.imageItemKey, ii.image);
                }
            }
        }

        mAdapterMedias.addAll(mVideos);
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
        final HashMap<String, String> hmAllBucketIds = mIlVideos
                .getBucketIds();
        mBucketIdList.clear();
        mBucketNameList.clear();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (Map.Entry<String, String> entry : hmAllBucketIds
                        .entrySet()) {
                    String strBucketId = entry.getKey();
                    if (strBucketId == null) {
                        continue;
                    }
                    ilpParam.mBucketId = strBucketId;
                    mIlVideos = ImageManager.makeImageList(getActivity()
                            .getContentResolver(), ilpParam);
                    int count = 0;
                    for (int nTmp = 0; nTmp < mIlVideos.getCount(); nTmp++) {
                        IVideo videoInfo = (IVideo) mIlVideos.getImageAt(nTmp);
                        if (videoInfo == null
                                || TextUtils.isEmpty(videoInfo.getDataPath())) {
                            continue;
                        }
                        if (videoInfo.getId() <= 0
                                || videoInfo.getDuration() < 1500) {
                            // || videoInfo.getWidth() == 0
                            // || videoInfo.getHeight() == 0
                            continue;
                        }
                        if (new File(videoInfo.getDataPath()).exists()) {
                            count++;
                        }
                    }
                    if (count < 1) {
                        continue;
                    }
                    mBucketIdList.add(strBucketId);
                    mBucketNameList.add(hmAllBucketIds.get(strBucketId));
                }
                mhandler.sendEmptyMessage(SHOW_POPUP);
            }
        });
        thread.start();

        BucketListView lvBuckets = (BucketListView) (contentView.findViewById(R.id.lvBucket));
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
                    tvBucketName.setText(mBucketNameList.get(position - 1));

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

    public void showPopup(View v) {
        showPopupWindow(v);
    }

    private final int GETVIDEO_NO = 6;
    private final int SHOW_POPUP = 11;
    private Handler mhandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GETVIDEO_NO:
                    mRlNoVideos
                            .setVisibility(mAdapterMedias.getCount() > 0 ? View.GONE
                                    : View.VISIBLE);
                    break;
                case SHOW_POPUP:
                    mPopupWindow.showAsDropDown(mPopupView);
                    ivSelectBucket.setImageResource(R.drawable.select_bucket_dropup);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public SparseArray<IImage> getMedia() {
        return mVideoSelected;
    }

    @Override
    public void onDestroyView() {
        mVideoSelected.clear();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (null != mIlVideos) {
            mIlVideos.close();
            mIlVideos = null;
        }
        mGridVideosSelector.setAdapter(null);
        super.onDestroy();
    }

    public void refresh() {
        tvBucketName.setText(R.string.all_video);
        rebakeVideos(false, false, false);
    }

}
