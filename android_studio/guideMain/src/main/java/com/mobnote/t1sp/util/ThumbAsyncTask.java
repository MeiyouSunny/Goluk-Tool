package com.mobnote.t1sp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import com.mobnote.golukmain.R;
import com.mobnote.util.GlideUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 本地视频封面加载Task
 */
public class ThumbAsyncTask extends AsyncTask<String, Void, String> {

    private Context mContext;
    ImageView mImageView;

    public ThumbAsyncTask(Context context, ImageView imageView) {
        mContext = context;
        mImageView = imageView;
    }

    @Override
    protected String doInBackground(String... strings) {
        String videoPath = strings[0];
        String thumbPath = getCachePathByVideoPath(videoPath);
        File thumbFile = new File(thumbPath);
        if (thumbFile.exists())
            return thumbPath;

        Bitmap bitmap = ThumbUtil.getLocalVideoThumb(videoPath);
        saveBitmapToThumbCache(videoPath, bitmap);
        return thumbPath;
    }

    private void saveBitmapToThumbCache(String videoPath, Bitmap bitmap) {
        if (TextUtils.isEmpty(videoPath) || bitmap == null)
            return;

        String thumbPath = getCachePathByVideoPath(videoPath);
        File thumbFile = new File(thumbPath);

        try {
            if (!thumbFile.exists()) {
                thumbFile.getParentFile().mkdirs();
                thumbFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(thumbFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    private String getCachePathByVideoPath(String videoPath) {
        String videoName = FileUtil.getFileNameFromPath(videoPath);
        String thumbPath = FileUtil.getThumbCacheByVideoName(videoName);
        return thumbPath;
    }

    @Override
    protected void onPostExecute(String thumbPath) {
        super.onPostExecute(thumbPath);
        if (mImageView != null && !TextUtils.isEmpty(thumbPath)) {
            GlideUtils.loadImage(mContext, mImageView, thumbPath, R.drawable.album_default_img);
        }
    }

}
