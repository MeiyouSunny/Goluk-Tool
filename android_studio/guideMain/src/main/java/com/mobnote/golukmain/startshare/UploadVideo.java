package com.mobnote.golukmain.startshare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.helper.IUploadRequestListener;
import com.mobnote.golukmain.helper.QCloudHelper;
import com.mobnote.golukmain.helper.UpLoadVideoRequest;
import com.mobnote.util.GolukUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import cn.com.tiros.debug.GolukDebugUtils;


public class UploadVideo implements IUploadRequestListener {
    /**
     * 上传视频更新进度
     */
    private final int MSG_H_UPLOAD_PROGRESS = 2;
    /**
     * 上传成功
     */
    private final int MSG_H_UPLOAD_SUCESS = 3;
    /**
     * 上传视频失败
     */
    private final int MSG_H_UPLOAD_ERROR = 4;
    private final int MSG_H_COUNT = 7;
    /**
     * 重新上传
     */
    private final int MSG_H_RETRY_UPLOAD = 8;

    private Context mContext = null;
    private final int UPLOAD_FAILED_UP = 3;
    private int uploadCount = 0;
    private boolean isUploading = false;
    /**
     * 统计
     */
    private int finishShowCount = 0;
    /**
     * 要上传的文件路径
     */
    private String mVideoPath = null;
    /**
     * 2/3 紧急/精彩
     */
    private int mVideoType = 0;
    /**
     * 文件名称
     */
    private String mVideoName;
    private boolean mIsExit = false;
    private String mVideoVid = "";
    private String mSignTime = "";
    /**
     * 上传视频是否完成
     */
    private boolean mIsUploadSucess = false;
    private AlertDialog mErrorDialog = null;

    private IUploadVideoFn mFn = null;

    /**
     * 退出提示框
     */
    private AlertDialog mExitPromptDialog = null;

    /**
     * 腾讯云上传文件
     */
    private UpLoadVideoRequest mUpLoadVideoRequest;

    public void setListener(IUploadVideoFn fn) {
        mFn = fn;
    }

    public void setExit() {
        mIsExit = true;
        exit(true);
    }

    public Handler mBaseHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_H_UPLOAD_PROGRESS:
                    int percent = ((Integer) msg.obj).intValue();
                    sendData(IUploadVideoFn.EVENT_PROCESS, percent);
                    break;
                case MSG_H_UPLOAD_SUCESS:
                    // 回调文件上传成功
                    sendData(IUploadVideoFn.EVENT_UPLOAD_SUCESS, msg.obj);
                    break;
                case MSG_H_UPLOAD_ERROR:
                    if (mIsExit) {
                        return;
                    }
                    // 上传失败
                    uploadFailed();
                    break;
                case MSG_H_COUNT:
                    finishShowCount++;
                    if (finishShowCount >= 3) {
                        mBaseHandler.removeMessages(MSG_H_COUNT);
                        finishShowCount = 0;
                    } else {
                        mBaseHandler.sendEmptyMessageDelayed(MSG_H_COUNT, 1000);
                    }
                    break;
                case MSG_H_RETRY_UPLOAD:
                    if (mIsExit) {
                        return;
                    }
                    uploadVideoFile(mVideoPath);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public UploadVideo(Context context, GolukApplication application, final String videoName) {
        mContext = context;
        mVideoName = videoName;
        createThumb();
    }

    public void setUploadInfo(final String videoPath, final int videoType, final String videoName) {
        mVideoPath = videoPath;
        mVideoType = videoType;
        mVideoName = videoName;

        uploadCount = 0;

        createThumb();

        uploadVideoFile(mVideoPath);

        initState();
    }

    private void initState() {

    }

    public static final String PATH_FS1 = "/goluk";
    final String fileFolder = Environment.getExternalStorageDirectory().getPath() + PATH_FS1 + "/Cache";
    private String thumbFile = fileFolder + "/thumb11.jpg";
    private Bitmap mShortBitmap = null;

    public Bitmap getThumbBitmap() {
        return mShortBitmap;
    }

    private final int THUMB_WIDTH = 854;
    private final int THUMB_HEIGHT = 480;

    //

    /**
     * 创建视频缩略图
     *
     * @author jyf
     * @date 2015年8月14日
     */
    private void createThumb() {
        if (null != mShortBitmap) {
            return;
        }
        String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
        thumbFile = filePath + File.separator + mVideoName;
        mShortBitmap = ImageManager.getBitmapFromCache(thumbFile, THUMB_WIDTH, THUMB_HEIGHT);
        File image = new File(thumbFile);
        if (image.exists() && null != image && null != mShortBitmap) {
            return;
        }
        thumbFile = fileFolder + "/thumb11.jpg";
        mShortBitmap = getSelfBitmap();
        if (null == mShortBitmap) {
            return;
        }
        try {
            File file = new File(fileFolder);
            file.mkdirs();
            file = new File(thumbFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            mShortBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            mShortBitmap = ImageManager.getBitmapFromCache(thumbFile, THUMB_WIDTH, THUMB_HEIGHT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自己抓取视频第一幀图片
     *
     * @return
     * @author jyf
     * @date 2015年8月14日
     */
    private Bitmap getSelfBitmap() {
        if (null == mVideoPath || mVideoPath.equals("")) {
            return null;
        }

//		Bitmap temp = ThumbnailUtils.createVideoThumbnail(mVideoPath, Thumbnails.MINI_KIND);
//		if (temp == null) {
//			temp = GolukUtils.createVideoThumbnail(mVideoPath);
//		}

        Bitmap temp = GolukUtils.createVideoThumbnail(mVideoPath);
        return temp;
    }

    public String getVideoId() {
        return this.mVideoVid;
    }

    public String getSignTime() {
        return this.mSignTime;
    }

    public String getThumbPath() {
        return thumbFile;
    }

    /**
     * 上传视频文件，支持两种上传途径
     * 1. 上传至自建文件服务器
     * 2. 上传至云服务器（目前是腾讯云的微视频）
     *
     * @param videoPath
     */
    public void uploadVideoFile(String videoPath) {
        if (null == videoPath || "".equals(videoPath)) {
            return;
        }
        mVideoPath = videoPath;
        uploadCount++;
        GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------uploadVideoFile :" + uploadCount);
        isUploading = true;
        uploadToCloud();
    }

    public boolean isUploading() {
        return isUploading;
    }

    // CC上传失败，提示用户重试或退出
    public void showExitDialog() {
        dimissErrorDialog();

        mExitPromptDialog = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.user_dialog_hint_title))
                .setMessage(mContext.getString(R.string.str_uploading_videos_is_interrupted))
                .setPositiveButton(mContext.getString(R.string.str_button_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        click_Exit();
                    }

                }).setNegativeButton(mContext.getString(R.string.user_cancle), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dimissExitDialog();
                    }

                }).create();
        mExitPromptDialog.show();
    }

    private void click_Exit() {
        dimissExitDialog();
        dimissErrorDialog();
        // GlobalWindow.getInstance().toFailed("视频上传取消");
        exit(false);
        sendData(IUploadVideoFn.EVENT_EXIT, false);
    }

    // CC上传失败，提示用户重试或退出
    private void uploadFailed() {
        dimissErrorDialog();
        if (mIsExit) {
            return;
        }
        // GlobalWindow.getInstance().toFailed("视频上传失败")
        //
        //
        // ;

        mErrorDialog = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.user_dialog_hint_title))
                .setMessage(mContext.getString(R.string.str_upload_fail))
                .setPositiveButton(mContext.getString(R.string.str_try_again), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uploadVideoFile(mVideoPath);
                        dimissErrorDialog();
                        GolukUtils.showToast(mContext, mContext.getString(R.string.str_re_upload));
                    }

                }).setNegativeButton(mContext.getString(R.string.user_cancle), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        click_Exit();
                    }

                }).create();
        mErrorDialog.setCanceledOnTouchOutside(false);
        mErrorDialog.setCancelable(false);
        mErrorDialog.show();
    }

    private void sendData(int event, Object data) {
        if (null != mFn) {
            mFn.CallBack_UploadVideo(event, data);
        }
    }

    private void dimissErrorDialog() {
        if (null != mErrorDialog) {
            mErrorDialog.dismiss();
            mErrorDialog = null;
        }
    }

    public void setExit(boolean isExit) {
        mIsExit = isExit;
    }

    // 上传视频成功
    private void uploadSucess(Object obj) {
        mIsUploadSucess = true;
        isUploading = false;
        GolukDebugUtils.e("", "upload service--VideoShareActivity-handleStatus---上传完成---FINISH----");
        // 通知上传成功
        Message msg = mBaseHandler.obtainMessage();
        msg.what = MSG_H_UPLOAD_SUCESS;
        msg.obj = obj;
        mBaseHandler.sendMessage(msg);
    }

    // 上传视频失败
    private void uploadError() {
        if (mIsExit) {
            return;
        }
        GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------uploadError :uploadCount:  " + uploadCount);
        if (uploadCount >= UPLOAD_FAILED_UP) {
            // 报错
            mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_ERROR);
            isUploading = false;
        } else {
            mBaseHandler.sendEmptyMessageDelayed(MSG_H_RETRY_UPLOAD, 500);
        }
    }

    private void updateFreshProgress(int per) {
        if (mIsExit) {
            return;
        }
        Message msg = new Message();
        msg.what = MSG_H_UPLOAD_PROGRESS;
        msg.obj = per;
        mBaseHandler.sendMessage(msg);
    }

    private void dimissExitDialog() {
        if (null != mExitPromptDialog) {
            mExitPromptDialog.dismiss();
            mExitPromptDialog = null;
        }
    }

    private void exit(boolean isdestroyTopwindow) {
        mIsExit = isdestroyTopwindow;
        // 取消上传
        if (isUploading && mUpLoadVideoRequest != null) {
            mUpLoadVideoRequest.Cancel();
        }
        this.dimissErrorDialog();
        this.dimissExitDialog();
        mBaseHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 文件上传成功
     */
    private static final int UPLOAD_STATE_SUCESS = 1;
    /**
     * 文件上传进度
     */
    private static final int UPLOAD_STATE_PROGRESS = 2;

    public void videoUploadCallBack(int success, Object param1, Object param2) {
        GolukDebugUtils.e("", "jyf-----VideoShareActivity-------------videoUploadCallBack :success:  " + success);
        if (UPLOAD_STATE_SUCESS == success) {
            // 保存视频上传ID
            mVideoVid = (String) param2;
            uploadSucess(param1);
            GolukDebugUtils.e("", "视频上传返回id--VideoShareActivity-videoUploadCallBack---vid---" + mVideoVid);
        } else if (UPLOAD_STATE_PROGRESS == success) {
            // 上传进度
            final int per = (Integer) param1;
            updateFreshProgress(per);
        } else {
            // 文件上传失败
            uploadError();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 云存储服务
    private void uploadToCloud() {
        // 请求签名
        mUpLoadVideoRequest = new UpLoadVideoRequest(this);
        HashMap<String, String> path = new HashMap<String, String>();
        path.put(QCloudHelper.PHOTO_BUCKET, thumbFile);
        path.put(QCloudHelper.VIDEO_BUCKET, mVideoPath);
        mUpLoadVideoRequest.upLoad(path);
    }

    @Override
    public void onUploadSucceed(HashMap<String, String> urlMap) {
        // TODO Auto-generated method stub
        mVideoVid = mUpLoadVideoRequest.getVideoId();
        mSignTime = mUpLoadVideoRequest.getSignTime();
        videoUploadCallBack(UPLOAD_STATE_SUCESS, urlMap, mVideoVid);
    }

    @Override
    public void onUploadProgress(int percent) {
        // TODO Auto-generated method stub
        GolukDebugUtils.d("goluk", "上传中! ret:" + percent);
        Message msg = new Message();
        msg.what = MSG_H_UPLOAD_PROGRESS;
        msg.obj = percent;
        mBaseHandler.sendMessage(msg);
    }

    @Override
    public void onUploadFailed(int errorCode, String errorMsg) {
        // TODO Auto-generated method stub
        GolukDebugUtils.e("goluk", "上传结果:失败! ret:" + errorCode + " msg:" + errorMsg);
        if (null != mContext && mContext instanceof VideoShareActivity) {
            ((VideoShareActivity) mContext).zhugeShareVideo(errorMsg);
        }
        mBaseHandler.sendEmptyMessage(MSG_H_UPLOAD_ERROR);
    }
}
