package com.mobnote.golukmain.helper;

import com.elvishew.xlog.XLog;
import com.mobnote.eventbus.EventGetShareSignTokenInvalid;
import com.mobnote.golukmain.helper.bean.SignBean;
import com.mobnote.golukmain.helper.bean.SignDataBean;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.log.app.LogConst;
import com.mobnote.util.GolukUtils;
import com.tencent.cos.model.COSRequest;
import com.tencent.cos.model.COSResult;
import com.tencent.cos.model.PutObjectRequest;
import com.tencent.cos.model.PutObjectResult;
import com.tencent.cos.task.listener.IUploadTaskListener;

import java.io.File;
import java.util.HashMap;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class UpLoadVideoRequest extends UpLoadRequest implements IRequestResultListener {

    private String mVideoPath;
    private String mPhotoPath;
    private PutObjectRequest mPhotoUploadTask;
    private PutObjectRequest mVideoUploadTask;
    private SignDataBean mSignDataBean;
    /**
     * 上传视频生成的封面地址和视频地址，QCloudHelper.VIDEO_BUCKET ，QCloudHelper.PHOTO_BUCKET分别作为key
     */
    private HashMap<String, String> mUrl;
    private long mTotalSize = 0;
    private long mCoverFileSize = 0;

    public UpLoadVideoRequest(IUploadRequestListener listener) {
        super(listener);
        mUrl = new HashMap<String, String>();
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean upLoad(HashMap<String, String> pathMap) {
        // TODO Auto-generated method stub
        if (pathMap == null || pathMap.size() != 2 || !pathMap.containsKey(QCloudHelper.PHOTO_BUCKET)
                || !pathMap.containsKey(QCloudHelper.VIDEO_BUCKET)) {
            return false;
        }
        mVideoPath = pathMap.get(QCloudHelper.VIDEO_BUCKET);
        mPhotoPath = pathMap.get(QCloudHelper.PHOTO_BUCKET);
        File coverFile = new File(mPhotoPath);
        mCoverFileSize = coverFile.length();
        mTotalSize += mCoverFileSize;
        File videoFile = new File(mVideoPath);
        mTotalSize += videoFile.length();
        UpLoadVideoSignRequest request = new UpLoadVideoSignRequest(IPageNotifyFn.PageType_UploadVideo, this);
        request.setTag(this);
        request.get();
        XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Start request upload sign info");
        return true;
    }

    @Override
    public void onLoadComplete(int requestType, Object result) {
        // TODO Auto-generated method stub
        XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Sign info: %s:", result);
        if (requestType == IPageNotifyFn.PageType_UploadVideo) {
            SignBean signBean = (SignBean) result;
            if (signBean != null && signBean.data != null) {
                if (!GolukUtils.isTokenValid(signBean.data.result)) {
                    EventBus.getDefault().post(new EventGetShareSignTokenInvalid());
                    return;
                }
            }
            if (signBean != null && signBean.success && signBean.data != null) {
                mSignDataBean = signBean.data;
                if (!uploadPhotoToCloud()) {
                    mListener.onUploadFailed(-1, "upload photo failed");
                    XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Upload photo failed");
                }
            } else {
                mListener.onUploadFailed(-1, "get sign failed");
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Get sign info failed");
            }
        }
    }

    private boolean uploadPhotoToCloud() {
        String space = mSignDataBean.envsync + QCloudHelper.PHOTO_BUCKET;
        mPhotoUploadTask = new PutObjectRequest();
        mPhotoUploadTask.setBucket(mSignDataBean.photoBucket);
        mPhotoUploadTask.setCosPath(String.format("%s%s.png", mSignDataBean.coverpath, mSignDataBean.videoid));
        mPhotoUploadTask.setSrcPath(mPhotoPath);
        mPhotoUploadTask.setSign(mSignDataBean.coversign);
        mPhotoUploadTask.setInsertOnly("0");
        mPhotoUploadTask.setListener(new IUploadTaskListener() {
            @Override
            public void onProgress(COSRequest cosRequest, long sendSize, long totalSize) {
                int percent = (int) ((sendSize * 100) / (mTotalSize * 1.0f));
                mListener.onUploadProgress(percent);
            }

            @Override
            public void onCancel(COSRequest cosRequest, COSResult cosResult) {

            }

            @Override
            public void onSuccess(COSRequest cosRequest, COSResult cosResult) {
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Photo onUploadSucceed");
                PutObjectResult result = (PutObjectResult) cosResult;
                GolukDebugUtils.e("goluk", "上传成功! ret:" + result.code);
                mUrl.put(QCloudHelper.PHOTO_BUCKET, result.source_url);
                mPhotoUploadTask = null;
                if (!uploadVideoToCloud()) {
                    mListener.onUploadFailed(-1, "upload video failed");
                    XLog.tag(LogConst.TAG_SHARE_VIDEO).i("upload video failed");
                }
            }

            @Override
            public void onFailed(COSRequest cosRequest, COSResult cosResult) {
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Photo onUploadFailed,cosRequestId:" + cosRequest.getRequestId() + ",result code:" + cosResult.code);
                mListener.onUploadFailed(-1, "upload photo failed");
                mPhotoUploadTask = null;
            }

        });


        // 上传
        QCloudHelper helper = QCloudHelper.getInstance();
        return helper.upload(mPhotoUploadTask,mSignDataBean.region);
    }

    private boolean uploadVideoToCloud() {

        mVideoUploadTask = new PutObjectRequest();
        String space = mSignDataBean.envsync + QCloudHelper.VIDEO_BUCKET;
        mVideoUploadTask.setBucket(mSignDataBean.videoBucket);
        mVideoUploadTask.setSign(mSignDataBean.videosign);
        String remotePath = String.format("%s%s.mp4", mSignDataBean.videopath, mSignDataBean.videoid);
        mVideoUploadTask.setCosPath(remotePath);
        mVideoUploadTask.setSrcPath(mVideoPath);
        mVideoUploadTask.setListener(new IUploadTaskListener() {
            @Override
            public void onProgress(COSRequest cosRequest, long sendSize, long totalSize) {
                int percent = (int) (((sendSize + mCoverFileSize) * 100) / (mTotalSize * 1.0f));
                mListener.onUploadProgress(percent);
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Video onUploadProgress %d", percent);
            }

            @Override
            public void onCancel(COSRequest cosRequest, COSResult cosResult) {

            }

            @Override
            public void onSuccess(COSRequest cosRequest, COSResult cosResult) {
                PutObjectResult result = (PutObjectResult) cosResult;
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Video onUploadSucceed:%s", result.source_url);
                GolukDebugUtils.e("goluk", "上传成功! ret:" + result.code);
                mUrl.put(QCloudHelper.VIDEO_BUCKET, result.source_url);
                mListener.onUploadSucceed(mUrl);
                mVideoUploadTask = null;
            }

            @Override
            public void onFailed(COSRequest cosRequest, COSResult cosResult) {
                mListener.onUploadFailed(cosResult.code, cosResult.msg);
                mVideoUploadTask = null;
                XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Video onUploadFailed %d, %s,%s", cosResult.code, cosResult.msg, cosResult.requestId);

            }
        });


        // 上传
        QCloudHelper helper = QCloudHelper.getInstance();
        boolean result = helper.upload(mVideoUploadTask,mSignDataBean.region);
        XLog.tag(LogConst.TAG_SHARE_VIDEO).i("Start upload video to cloud");
        return result;
    }

    public String getVideoId() {
        return mSignDataBean.videoid;
    }

    public String getSignTime() {
        return mSignDataBean.signtime;
    }

    public void Cancel() {
        mQCloudHelper.cancel(mPhotoUploadTask);
        mQCloudHelper.cancel(mVideoUploadTask);
        HttpManager.getInstance().cancelAll(this);
    }
}
