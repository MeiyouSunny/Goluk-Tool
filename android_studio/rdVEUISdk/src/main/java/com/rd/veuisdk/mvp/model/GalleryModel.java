package com.rd.veuisdk.mvp.model;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.rd.gallery.IImage;
import com.rd.gallery.IImageList;
import com.rd.gallery.IVideo;
import com.rd.gallery.ImageManager;
import com.rd.lib.utils.ThreadPoolUtils;
import com.rd.veuisdk.model.DirInfo;
import com.rd.veuisdk.model.IDirInfo;
import com.rd.veuisdk.model.ImageItem;
import com.rd.veuisdk.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图库数据
 *
 * @author JIAN
 * @date 2019/3/15
 * @Description
 */
public class GalleryModel implements IGalleryModel {
    private Handler mHandler;
    String TAG = "GalleryModel";
    List<IDirInfo> mDirList;

    public GalleryModel(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mDirList = new ArrayList<>();
    }

    private Context mContext;


    /**
     * 加载指定文件夹下的视频
     *
     * @param bucketId
     * @param iDirData
     */
    private void loadVideoList(String bucketId, DirInfo iDirData) {
        List<ImageItem> mVideos = new ArrayList<ImageItem>();
        boolean unmounted = false, scanning = false;
        ImageManager.ImageListParam ilpParam = ImageManager.allVideos(
                !unmounted && !scanning, true);
        if (!TextUtils.isEmpty(bucketId)) {
            ilpParam.mBucketId = bucketId;
        }
        IImageList mIlVideos = ImageManager.makeImageList(mContext
                .getContentResolver(), ilpParam);
        if (mIlVideos == null) {
            return;
        }
        int len = mIlVideos.getCount();
        for (int nTmp = 0; nTmp < len; nTmp++) {
            IVideo videoInfo = (IVideo) mIlVideos.getImageAt(nTmp);
            if (videoInfo == null || TextUtils.isEmpty(videoInfo.getDataPath())) {
                continue;
            }
            if (videoInfo.getId() <= 0 || videoInfo.getDuration() < 1500) {
                continue;
            }
            File fv = new File(videoInfo.getDataPath());
            if (fv.exists() && !fv.getName().endsWith(".wmv")) {
                ImageItem ii = new ImageItem(videoInfo);
                mVideos.add(ii);
            }
        }
        mIlVideos.close();
        iDirData.setList(mVideos);
    }


    /**
     * 执行视频列表获取
     *
     * @param unmounted
     * @param scanning
     */
    private void rebakeVideos(boolean unmounted, boolean scanning) {

        if (isResultAsOneIDir) {
            DirInfo data = new DirInfo("", "");
            loadVideoList(null, data);
            mDirList.add(data);
        } else {
            ImageManager.ImageListParam ilpParam = ImageManager.allVideos(
                    !unmounted && !scanning, true);
            IImageList mIlVideos = ImageManager.makeImageList(mContext
                    .getContentResolver(), ilpParam);
            HashMap<String, String> hmAllBucketIds = mIlVideos.getBucketIds();
            mIlVideos.close();

            for (Map.Entry<String, String> entry : hmAllBucketIds.entrySet()) {
                String strBucketId = entry.getKey();
                if (strBucketId == null) {
                    continue;
                }
                DirInfo data = new DirInfo(entry.getValue(), entry.getKey());
                loadVideoList(strBucketId, data);
                mDirList.add(data);
            }
        }


    }

    private boolean isResultAsOneIDir = false;

    @Override
    public void initData(boolean resultAsOneIDir, final boolean isVideo, @NonNull final ICallBack callBack) {
        this.isResultAsOneIDir = resultAsOneIDir;
        mMediaBreakLoad = false;
        ThreadPoolUtils.executeEx(new Runnable() {
            @Override
            public void run() {

                if (isVideo) {
                    rebakeVideos(false, false);
                } else {
                    doLoadPhotoBuckets(isResultAsOneIDir);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess(mDirList);
                    }
                });

            }
        });
    }

    /**
     * 日期降序比较器
     */
    private Comparator<ImageItem> imageComparator = new Comparator<ImageItem>() {

        @Override
        public int compare(ImageItem lhs, ImageItem rhs) {
            return lhs.image.getDateTaken() > rhs.image.getDateTaken() ? -1 : 1;
        }
    };
    private boolean mMediaLoading, mMediaBreakLoad; // 媒体加载中...


    private void cancelLoadPhotos() {
        mMediaBreakLoad = true;
    }


    private void doLoadPhotoBuckets(boolean isDCIM) {
        synchronized (this) {
            if (mMediaLoading) {
                return;
            }
            mMediaLoading = true;
            mMediaBreakLoad = false;
        }
        if (mContext == null) {
            return;
        }
        ImageManager.ImageListParam ilpParam = ImageManager.allPhotos(
                StorageUtils.isAvailable(false), true);
        IImageList ilTmp = ImageManager.makeImageList(mContext.getContentResolver(),
                ilpParam);
        HashMap<String, String> hmBucketIds;
        if (isDCIM) {
            hmBucketIds = ilTmp.getDCIMBucketIds();
        } else {
            hmBucketIds = ilTmp.getBucketIds();
        }
        ilTmp.close();
        for (Map.Entry<String, String> entry : hmBucketIds.entrySet()) {
            String bucketId = entry.getKey();
            if (bucketId == null) {
                continue;
            }
            DirInfo iDir = new DirInfo(entry.getValue(), entry.getKey());
            loadPhotoList(bucketId, iDir);
            synchronized (this) {
                if (mMediaBreakLoad) {
                    break;
                }
            }
            mDirList.add(iDir);
        }

        synchronized (this) {
            mMediaLoading = false;
        }


    }

    /**
     * 加载指定bucket图片
     *
     * @param strBucketId
     */
    private synchronized void loadPhotoList(String strBucketId, DirInfo dir) {
        if (mContext != null) {
            ImageManager.ImageListParam ilpParam = ImageManager
                    .allPhotos(StorageUtils.isAvailable(false));
            ilpParam.mBucketId = strBucketId;
            IImageList ilImages = ImageManager.makeImageList(
                    mContext.getContentResolver(), ilpParam);
            try {
                int len = ilImages.getCount();
                List<ImageItem> tmp = new ArrayList<>();
                for (int nTmp = 0; nTmp < len; nTmp++) {
                    IImage img = ilImages.getImageAt(nTmp);
                    ImageItem ii = new ImageItem(img);
                    if (img.isValid()) {
                        tmp.add(ii);
                    }
                    if (mMediaBreakLoad) {
                        return;
                    }
                }
                // 图片按日期降序排序
                Collections.sort(tmp, imageComparator);
                dir.setList(tmp);
            } finally {
                ilImages.close();
            }
        }
    }


    @Override
    public void recycle() {
        cancelLoadPhotos();
        mDirList.clear();
    }
}
