package com.mobnote.t1sp.util;

import android.os.AsyncTask;
import android.os.Environment;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.photoalbum.FileInfoManagerUtils;
import com.mobnote.util.GolukVideoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询本地最新两个抓拍视频信息
 */
public class LocalWonderfulVideoQueryTask extends AsyncTask<Void, String, String> {

    private LocalWonderfulQueryListener mListener;
    private List<VideoInfo> mLocalListData;

    public LocalWonderfulVideoQueryTask(LocalWonderfulQueryListener listener) {
        mListener = listener;
        mLocalListData = new ArrayList<VideoInfo>(2);
    }

    @Override
    protected String doInBackground(Void... voids) {
        final String dirPath = Environment.getExternalStorageDirectory().getPath() + "/goluk/video/wonderful/";
        List<String> files = new ArrayList<String>();
        files.addAll(FileInfoManagerUtils.getFileNames(dirPath, "(.+?(mp|MP)4)"));

        if (null == files || files.size() <= 0) {
            return null;
        }

        int fLen = files.size();
        VideoInfo videoInfoTemp;
        for (int i = 0; i < fLen; i++) {
            // 保存视频信息
            videoInfoTemp = GolukVideoUtils.getVideoInfo(files.get(i));
            if (videoInfoTemp != null) {
                mLocalListData.add(videoInfoTemp);
            }
        }

//        Collections.sort(mLocalListData, new Comparator<VideoInfo>() {
//            @Override
//            public int compare(VideoInfo lhs, VideoInfo rhs) {
//                return GolukUtils.parseStringToMilli(rhs.videoCreateDate) > GolukUtils.parseStringToMilli(lhs.videoCreateDate) ? 1 : -1;
//            }
//        });

        final List<VideoInfo> videoInfos = new ArrayList<>(2);
        if (mLocalListData.size() >= 1)
            videoInfos.add(mLocalListData.get(0));
        if (mLocalListData.size() >= 2)
            videoInfos.add(mLocalListData.get(1));
        mLocalListData = videoInfos;

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mListener != null)
            mListener.onWonderfulVideoQueryed(mLocalListData);
    }

    public interface LocalWonderfulQueryListener {
        void onWonderfulVideoQueryed(List<VideoInfo> videoInfos);
    }

}
