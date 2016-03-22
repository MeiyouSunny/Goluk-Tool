package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.Utils;
import cn.com.mobnote.golukmobile.fileinfo.GolukVideoInfoDbManager;
import cn.com.mobnote.golukmobile.fileinfo.VideoFileInfoBean;
import cn.com.mobnote.util.SortByDate;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

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
		String[] videoPaths = {"","loop/", "urgent/", "", "wonderful/" };
//		String[] filePaths = { "", "loop/loop.txt", "urgent/urgent.txt", "", "wonderful/wonderful.txt" };
//		String file = mFilePath + filePaths[type];

//		List<String> files = FileInfoManagerUtils.getVideoConfigFile(file);
		
		List<String> files = new ArrayList<String>();
		files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + videoPaths[1], "(.+?mp4)"));
		files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + videoPaths[2], "(.+?mp4)"));
		files.addAll(FileInfoManagerUtils.getFileNames(mFilePath + videoPaths[4], "(.+?mp4)"));
		
		if (null == files || files.size() <= 0) {
			return null;
		}

		Collections.sort(files, new SortByDate());

		int fLen = files.size();
		for (int i = 0 ; i < fLen; i++) {
			try {
				String fileName = files.get(i);

				VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(fileName);
				String videoPath = mFilePath + videoPaths[type] + fileName;
				String resolution = "";
				int period = 8;

				String periodStr = "";
				String dateStr = "";
				String size = "";
				if (videoFileInfoBean == null) {
					int hp = 0;
					String hpStr = "";
					File videoFile = new File(videoPath);
					// if (videoFile.exists()) {
					size = String.format("%.1f", videoFile.length() / 1024.f / 1024.f) + "MB";
					// 判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
					String[] names = fileName.split("_");

					if (names.length == 3) {
						hpStr = names[0].substring(3, 4);
						periodStr = names[2].substring(0, names[2].lastIndexOf("."));
						dateStr = names[1];
						dateStr = "20" + names[1];
					} else if (names.length == 7) {
						hpStr = names[5];
						periodStr = names[6];
						periodStr = periodStr.substring(0, periodStr.lastIndexOf("."));
						dateStr = names[2];
					} else if (names.length == 8 && type == 1) {
						hpStr = names[6];
						periodStr = names[7];
						periodStr = periodStr.substring(0, periodStr.lastIndexOf("."));
						dateStr = names[1];
					}

					if (TextUtils.isDigitsOnly(hpStr)) {
						hp = Integer.valueOf(hpStr);
					}
					if (1 == hp) {
						resolution = "1080p";
					} else if (2 == hp) {
						resolution = "720p";
					} else {
						resolution = "480p";
					}
				} else {
					dateStr = videoFileInfoBean.timestamp;
					periodStr = videoFileInfoBean.period;
					size = videoFileInfoBean.filesize;
					resolution = videoFileInfoBean.resolution;
					
				}
				String time = FileInfoManagerUtils.countFileDateToString(dateStr);

				String tabTime = time.substring(0, 10);

				// 保存分组数据
				if (!mGroupListName.contains(tabTime)) {
					mGroupListName.add(tabTime);
				}
				if (TextUtils.isDigitsOnly(periodStr)) {
					period = Integer.valueOf(periodStr);
				}
				VideoInfo mVideoInfo = new VideoInfo();
				mVideoInfo.videoCreateDate = time;
				mVideoInfo.videoSize = size;
				mVideoInfo.isSelect = false;
				mVideoInfo.videoPath = videoPath;
				mVideoInfo.countTime = Utils.minutesTimeToString(period);
				mVideoInfo.videoHP = resolution;
				mVideoInfo.filename = fileName;
				mVideoInfo.isNew = SettingUtils.getInstance().getBoolean("Local_" + fileName, true);

				mLocalListData.add(mVideoInfo);
//				}

			} catch (Exception e) {
				continue;
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
