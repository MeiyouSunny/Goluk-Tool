package cn.com.mobnote.golukmobile.photoalbum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.Utils;
import android.os.AsyncTask;

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

	@Override
	protected String doInBackground(String... arg0) {
		String[] videoPaths = { "", "loop/", "urgent/", "", "wonderful/" };
		String[] filePaths = { "", "loop/loop.txt", "urgent/urgent.txt", "", "wonderful/wonderful.txt" };
		String file = mFilePath + filePaths[type];

		List<String> files = FileInfoManagerUtils.getVideoConfigFile(file);
		if (null == files || files.size() <= 0) {
			return null;
		}
		files = FileInfoManagerUtils.bubbleSort(files, true);
		if (null == files || files.size() <= 0) {
			return null;
		}

		int fLen = files.size() - 1;
		for (int i = fLen; i >= 0; i--) {
			try {
				String fileName = files.get(i);
				String videoPath = mFilePath + videoPaths[type] + fileName;
				File videoFile = new File(videoPath);
				if (videoFile.exists()) {

					String size = FileInfoManagerUtils.getFileSize(videoFile);
					// 判断视频类别,WND1_,URG1_文件已这种格式开头为 8s/紧急
					String[] names = fileName.split("_");
					String vt = names[0];
					int hp = 0;
					try {
						hp = Integer.valueOf(vt.substring(3, 4));
					} catch (Exception e) {
					}
					// 视频时长,秒
					int period = 8;
					if (names.length > 2) {
						String p = names[2];
						try {
							period = Integer.valueOf(p.substring(0, p.lastIndexOf(".")));
						} catch (Exception e) {
						}
					}
					String time = FileInfoManagerUtils.countFileDateToString(fileName);

					String tabTime = time.substring(0, 10);
					// 保存分组数据
					if (!mGroupListName.contains(tabTime)) {
						mGroupListName.add(tabTime);
					}

					VideoInfo mVideoInfo = new VideoInfo();
					mVideoInfo.videoCreateDate = time;
					mVideoInfo.videoSize = size;
					mVideoInfo.isSelect = false;
					mVideoInfo.videoPath = videoPath;
					mVideoInfo.countTime = Utils.minutesTimeToString(period);
					if (1 == hp) {
						mVideoInfo.videoHP = "1080";
					} else if (2 == hp) {
						mVideoInfo.videoHP = "720";
					} else {
						mVideoInfo.videoHP = "480";
					}
					mVideoInfo.filename = fileName;
					mVideoInfo.isNew = SettingUtils.getInstance().getBoolean("Local_" + fileName, true);

					mLocalListData.add(mVideoInfo);
				}

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
