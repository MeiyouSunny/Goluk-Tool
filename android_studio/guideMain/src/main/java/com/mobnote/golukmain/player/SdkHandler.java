package com.mobnote.golukmain.player;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;

import com.mobnote.golukmain.photoalbum.FragmentAlbum;
import com.mobnote.golukmain.photoalbum.PhotoAlbumActivity;
import com.mobnote.golukmain.photoalbum.PhotoAlbumConfig;
import com.mobnote.util.GolukUtils;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.callback.ISdkCallBack;

/**
 * SDK回调演示用handler
 */
public class SdkHandler {
    private String TAG;

    public SdkHandler() {
        TAG = this.toString();
    }

    public ISdkCallBack getCallBack() {
        return isdk;
    }

    /**
     * 响应视频导出<br>
     * 1.读取视频信息并写入系统相册<br>
     * 2.播放该视频
     *
     * @param context   应用上下文
     * @param videoPath 视频路径
     */
    private void onVideoExport(Context context, String videoPath) {
        if (!TextUtils.isEmpty(videoPath)) {
            // 读取导出视频的媒体信息，如宽度，持续时间等
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            int duration= 0 ;
            try {
                retriever.setDataSource(videoPath);
                int nVideoWidth = Integer
                        .parseInt(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                int nVideoHeight = Integer
                        .parseInt(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                duration = Integer
                        .parseInt(retriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                // 写入系统相册
                insertToGalleryr(context, videoPath, duration, nVideoWidth,
                        nVideoHeight);
            } catch (Exception ex) {
            } finally {
                retriever.release();
            }
            // 播放该视频
            //SdkEntry.playVideo(context, videoPath);
            GolukUtils.startVideoShareActivity(context, PhotoAlbumConfig.PHOTO_BUM_IPC_WND,
                    videoPath, videoPath, false, duration, "",
                    null);
        } else {
            Log.d(TAG, "获取视频地址失败");
        }
    }

    private ISdkCallBack isdk = new ISdkCallBack() {

        /**
         * 目标视频的路径
         *
         * @param context
         *            应用上下文
         * @param exportType
         *            回调类型 来自简单录制 {@link SdkEntry#CAMERA_EXPORT}<br>
         *            来自录制编辑{@link SdkEntry#CAMERA_EDIT_EXPORT}<br>
         *            来自编辑导出{@link SdkEntry#EDIT_EXPORT}<br>
         *            来自普通截取视频导出{@link SdkEntry#TRIMVIDEO_EXPORT}<br>
         *            来自定长截取视频导出{@link SdkEntry#TRIMVIDEO_DURATION_EXPORT}<br>
         * @param videoPath
         */
        @Override
        public void onGetVideoPath(Context context, int exportType,
                                   String videoPath) {
            onVideoExport(context, videoPath);
        }

        @Override
        public void onGetVideoTrimTime(Context context, int exportType, float startTime, float endTime) {

        }


        /**
         * 响应确认截取按钮
         *
         * @param context
         *            应用上下文
         * @param exportType
         *            来自普通截取的确认 {@link SdkEntry#TRIMVIDEO_EXPORT}<br>
         *            来自定长截取的确认 {@link SdkEntry#TRIMVIDEO_DURATION_EXPORT}<br>
         */
        @Override
        public void onGetVideoTrim(Context context, int exportType) {
        }

        /**
         * 响应进入相册（只显示照片、图片）
         *
         * @param context
         *            应用上下文
         */
        @Override
        public void onGetPhoto(Context context) {
        }

        /**
         * 响应进入相册（只显示视频）
         *
         * @param context
         *            应用上下文
         */
        @Override
        public void onGetVideo(Context context) {
            Intent intent = new Intent(context, PhotoAlbumActivity.class);
            intent.putExtra("from", "local");
            intent.putExtra(FragmentAlbum.PARENT_VIEW,true);
            intent.putExtra(FragmentAlbum.SELECT_MODE,true);
            context.startActivity(intent);
        }

    };

    /**
     * 将视频信息存入相册数据库
     *
     * @param path     视频路径
     * @param duration 视频持续时间
     * @param width    视频宽度
     * @param height   视频高度
     */
    private void insertToGalleryr(Context context, String path, int duration,
                                  int width, int height) {

    }

}
