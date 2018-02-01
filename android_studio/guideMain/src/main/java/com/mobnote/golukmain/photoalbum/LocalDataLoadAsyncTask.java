package com.mobnote.golukmain.photoalbum;

import android.os.AsyncTask;

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
//			try {
//				String fileName = files.get(i);
//
//				VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(fileName);
//
//				int currType = 0;
//				if(!TextUtils.isEmpty(fileName)){
//					if(fileName.startsWith("NRM")){
//						currType = 1;
//					}else if(fileName.startsWith("URG")){
//						currType = 2;
//					}else if(fileName.startsWith("WND")){
//						currType = 4;
//					}
//				}
//
//
//				String videoPath = mFilePath + videoPaths[currType] + fileName;
//				String resolution = "";
//				int period = 8;
//
//				String periodStr = "";
//				String dateStr = "";
//				String size = "";
//				if (videoFileInfoBean == null) {
//					int hp = 0;
//					String hpStr = "";
//					File videoFile = new File(videoPath);
//					// if (videoFile.exists()) {
//					size = String.format("%.1f", videoFile.length() / 1024.f / 1024.f) + "MB";
//					// 判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
//					String[] names = fileName.split("_");
//
//					if (names.length == 3) {
//						hpStr = names[0].substring(3, 4);
//						periodStr = names[2].substring(0, names[2].lastIndexOf("."));
//						dateStr = names[1];
//						dateStr = "20" + names[1];
//					} else if (names.length == 7) {
//						hpStr = names[5];
//						periodStr = names[6];
//						periodStr = periodStr.substring(0, periodStr.lastIndexOf("."));
//						dateStr = names[2];
//					} else if (names.length == 8 && currType == 1) {
//						hpStr = names[6];
//						periodStr = names[7];
//						periodStr = periodStr.substring(0, periodStr.lastIndexOf("."));
//						dateStr = names[1];
//					}
//
//					if (TextUtils.isDigitsOnly(hpStr)) {
//						hp = Integer.valueOf(hpStr);
//					}
//					if (1 == hp) {
//						resolution = "1080p";
//					} else if (2 == hp) {
//						resolution = "720p";
//					} else {
//						resolution = "480p";
//					}
//				} else {
//					dateStr = videoFileInfoBean.timestamp;
//					periodStr = videoFileInfoBean.period;
//					size = videoFileInfoBean.filesize;
//					resolution = videoFileInfoBean.resolution;
//
//				}
//				String time = FileInfoManagerUtils.countFileDateToString(dateStr);
//
//				String tabTime = time.substring(0, 10);
//
//				// 保存分组数据
//				if (!mGroupListName.contains(tabTime)) {
//					mGroupListName.add(tabTime);
//				}
//				if (TextUtils.isDigitsOnly(periodStr)) {
//					period = Integer.valueOf(periodStr);
//				}
//				VideoInfo mVideoInfo = new VideoInfo();
//				mVideoInfo.videoCreateDate = time;
//				mVideoInfo.videoSize = size;
//				mVideoInfo.isSelect = false;
//				mVideoInfo.videoPath = videoPath;
//				mVideoInfo.countTime = Utils.minutesTimeToString(period);
//				mVideoInfo.videoHP = resolution;
//				mVideoInfo.filename = fileName;
//				mVideoInfo.isNew = SettingUtils.getInstance().getBoolean("Local_" + fileName, true);
//
//				mLocalListData.add(mVideoInfo);
//				}
//				}

//			} catch (Exception e) {
//				continue;
//			}

            // 保存视频信息
            videoInfoTemp = GolukVideoUtils.getVideoInfo(files.get(i));
            if (videoInfoTemp != null) {
                mLocalListData.add(videoInfoTemp);
                String tabTime = videoInfoTemp.videoCreateDate.substring(0, 10);

                // 保存分组数据
                if (!mGroupListName.contains(tabTime)) {
                    mGroupListName.add(tabTime);

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
