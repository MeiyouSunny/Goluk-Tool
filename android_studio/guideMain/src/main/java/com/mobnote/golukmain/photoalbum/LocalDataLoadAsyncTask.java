package com.mobnote.golukmain.photoalbum;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.util.GolukVideoUtils;
import com.mobnote.util.SortByDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocalDataLoadAsyncTask extends AsyncTask<String, String, String> {
    private DataCallBack mDataCallBack = null;
    private int type = 0;
    private String mFilePath = null;
    private List<VideoInfo> mLocalListData = null;
    private List<String> mGroupListName = null;

    public LocalDataLoadAsyncTask(int type, DataCallBack dataCallBack) {
        this.mDataCallBack = dataCallBack;
        this.type = type;
        this.mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/";
        this.mGroupListName = new ArrayList<String>();
        this.mLocalListData = new ArrayList<VideoInfo>();
    }

    GolukVideoInfoDbManager mGolukVideoInfoDbManager = GolukVideoInfoDbManager.getInstance();

    @Override
    protected String doInBackground(String... arg0) {
        String[] videoPaths = {"", "loop/", "urgent/", "", "wonderful/"};
//		String[] filePaths = { "", "loop/loop.txt", "urgent/urgent.txt", "", "wonderful/wonderful.txt" };
//		String file = mFilePath + filePaths[type];

//		List<String> files = FileInfoManagerUtils.getVideoConfigFile(file);

        List<String> files = new ArrayList<String>();
        files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + videoPaths[1], "(.+?(mp|MP)4)"));
        files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + videoPaths[2], "(.+?(mp|MP)4)"));
        files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + videoPaths[4], "(.+?(mp|MP)4)"));

        if (null == files || files.size() <= 0) {
            return null;
        }

        Collections.sort(files, new SortByDate());

        int fLen = files.size();
        VideoInfo videoInfoTemp;
        for (int i = 0; i < fLen; i++) {
            // 保存视频信息
            videoInfoTemp = GolukVideoUtils.getVideoInfo(files.get(i));
            if (videoInfoTemp != null) {
                mLocalListData.add(videoInfoTemp);
                if (!TextUtils.isEmpty(videoInfoTemp.videoCreateDate) && videoInfoTemp.videoCreateDate.length() >= 10) {
                    String tabTime = videoInfoTemp.videoCreateDate.substring(0, 10);
                    // 保存分组数据
                    if (!mGroupListName.contains(tabTime)) {
                        mGroupListName.add(tabTime);
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mDataCallBack.onSuccess(type, mLocalListData, mGroupListName);
    }

}
