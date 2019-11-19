package com.mobnote.golukmain.photoalbum;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.t1sp.util.GolukUtils;
import com.mobnote.util.GolukVideoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.Utils;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.util.SortByDate;

import android.os.AsyncTask;
import android.text.TextUtils;

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
        GolukVideoInfoDbManager.getInstance().initDb(GolukApplication.getInstance());
        String[] videoPaths = {"loop/", "urgent/", "reduce", "wonderful/"};
//		String[] filePaths = { "", "loop/loop.txt", "urgent/urgent.txt", "", "wonderful/wonderful.txt" };
//		String file = mFilePath + filePaths[type];

//		List<String> files = FileInfoManagerUtils.getVideoConfigFile(file);

        List<String> files = new ArrayList<String>();
        for(String dir: videoPaths) {
            files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + dir, "(.+?(mp|MP)4)"));
        }

        List<String> files = new ArrayList<String>();
        for (String dir : videoPaths) {
            files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + dir, "(.+?(mp|MP)4)"));
        }

        if (null == files || files.size() <= 0) {
            return null;
        }

        //Collections.sort(files, new SortByDate());

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

        Collections.sort(mLocalListData, new Comparator<VideoInfo>() {
            @Override
            public int compare(VideoInfo lhs, VideoInfo rhs) {
                return (int) (GolukUtils.parseStringToMilli(rhs.videoCreateDate) - GolukUtils.parseStringToMilli(lhs.videoCreateDate));
            }
        });

		return null;
	}

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mDataCallBack.onSuccess(type, mLocalListData, mGroupListName);
    }

}
