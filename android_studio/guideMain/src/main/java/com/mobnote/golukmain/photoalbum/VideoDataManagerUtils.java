package com.mobnote.golukmain.photoalbum;

import android.text.TextUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.carrecorder.entity.DoubleVideoInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoFileInfo;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.GFileUtils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.com.tiros.api.FileUtils;
import cn.com.tiros.debug.GolukDebugUtils;

public class VideoDataManagerUtils {

	public static List<String> getGroupName(List<VideoInfo> mDataList) {
		List<String> mGroupListName = new ArrayList<String>();
		for (VideoInfo info : mDataList) {
			String time = info.videoCreateDate;
			String tabTime = null;
			if (time.length() > 10) {
				tabTime = time.substring(0, 10);
			} else {
				tabTime = time;
			}

			if (!mGroupListName.contains(tabTime)) {
				mGroupListName.add(tabTime);
			}
		}

		return mGroupListName;
	}

	/**
	 * IPC视频文件信息转列表显示视频信息
	 * 
	 * @param mVideoFileInfo
	 *            IPC视频文件信息
	 * @return 列表显示视频信息
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static VideoInfo getVideoInfo(VideoFileInfo mVideoFileInfo) {
		VideoInfo info = new VideoInfo();
		// 文件选择状态
		info.isSelect = false;
		info.id = mVideoFileInfo.id;
		info.videoSize = Utils.getSizeShow(mVideoFileInfo.size);
		info.countTime = Utils.minutesTimeToString(mVideoFileInfo.period);
		info.videoHP = mVideoFileInfo.resolution;
		if (TextUtils.isEmpty(mVideoFileInfo.timestamp)) {
			info.videoCreateDate = Utils.getTimeStr(mVideoFileInfo.time * 1000);
		} else {
			info.videoCreateDate = FileInfoManagerUtils.countFileDateToString(mVideoFileInfo.timestamp);
		}

		info.videoPath = mVideoFileInfo.location;
		info.filename = mVideoFileInfo.location;
		info.time = mVideoFileInfo.time;
		info.isNew = SettingUtils.getInstance().getBoolean("Cloud_" + mVideoFileInfo.location, true);

		String fileName = mVideoFileInfo.location;
		fileName = fileName.replace(".mp4", ".jpg");
		String filePath = GolukApplication.getInstance().getCarrecorderCachePath() + File.separator + "image";
		GFileUtils.makedir(filePath);
		File file = new File(filePath + File.separator + fileName);
		if (file.exists()) {
			// info.videoBitmap = ImageManager.getBitmapFromCache(filePath +
			// File.separator + fileName, 194, 109);
		} else {
			if (1 == mVideoFileInfo.withSnapshot) {
				GolukApplication
						.getInstance()
						.getIPCControlManager()
						.downloadFile(fileName, "IPC_IMAGE" + mVideoFileInfo.id, FileUtils.javaToLibPath(filePath),
								mVideoFileInfo.time);
				GolukDebugUtils.e("xuhw", "TTT====111111=====filename=" + fileName + "===tag=" + mVideoFileInfo.id);
			}
		}

		return info;
	}

	/**
	 * 单个视频数据对象转双个
	 * 
	 * @param datalist
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static List<DoubleVideoInfo> videoInfo2Double(List<VideoInfo> datalist) {
		List<DoubleVideoInfo> doublelist = new ArrayList<DoubleVideoInfo>();
		int i = 0;
		while (i < datalist.size()) {
			String groupname1 = "";
			String groupname2 = "";
			VideoInfo _videoInfo1 = null;
			VideoInfo _videoInfo2 = null;
			_videoInfo1 = datalist.get(i);
			if (_videoInfo1.videoCreateDate.length() >= 10)
				groupname1 = _videoInfo1.videoCreateDate.substring(0, 10);

			if ((i + 1) < datalist.size()) {
				_videoInfo2 = datalist.get(i + 1);
				if (_videoInfo2.videoCreateDate.length() >= 10)
					groupname2 = _videoInfo2.videoCreateDate.substring(0, 10);
			}

			if (groupname1.equals(groupname2)) {
				i += 2;
			} else {
				i++;
				_videoInfo2 = null;
			}

			DoubleVideoInfo dub = new DoubleVideoInfo(_videoInfo1, _videoInfo2);
			doublelist.add(dub);
		}

		return doublelist;
	}

}
