package com.mobnote.util;

import android.text.TextUtils;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.util.Utils;
import com.mobnote.golukmain.fileinfo.GolukVideoInfoDbManager;
import com.mobnote.golukmain.fileinfo.VideoFileInfoBean;
import com.mobnote.golukmain.photoalbum.FileInfoManagerUtils;
import com.mobnote.t1sp.util.FileUtil;

import java.io.File;

/**
 * Created by leege100 on 16/5/23.
 */
public class GolukVideoUtils {
    public static VideoInfo getVideoInfo(String fileName) {
        try {

            GolukVideoInfoDbManager mGolukVideoInfoDbManager = GolukVideoInfoDbManager.getInstance();
            VideoFileInfoBean videoFileInfoBean = mGolukVideoInfoDbManager.selectSingleData(fileName);

            int currType = 0;
            if (!TextUtils.isEmpty(fileName)) {
                // 后面的判断是T1SP的文件前缀
                if (fileName.startsWith("NRM") || fileName.startsWith(FileUtil.LOOP_VIDEO_PREFIX)) {
                    currType = 1;
                } else if (fileName.startsWith("URG") || fileName.startsWith(FileUtil.URGENT_VIDEO_PREFIX)) {
                    currType = 2;
                } else if (fileName.startsWith("WND") || fileName.startsWith(FileUtil.WONDERFUL_VIDEO_PREFIX)) {
                    currType = 4;
                }
            }

            String[] videoPaths = {"", "loop/", "urgent/", "", "wonderful/"};
            String mFilePath = android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/";
            String videoPath = mFilePath + videoPaths[currType] + fileName;
            String resolution = "1080p"; // 默认1080P
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

                if (fileName.contains("_")) {
                    // 传统视频文件,如 WND3_171101112822_0030.mp4
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
                    } else if (names.length == 8 && currType == 1) {
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
                } else if (fileName.contains("-")) {
                    // T1SP视频文件, 如 SHARE171109-173846F.MP4
                    int startIndex = 0;
                    if (fileName.contains(FileUtil.WONDERFUL_VIDEO_PREFIX)) {
                        startIndex = FileUtil.WONDERFUL_VIDEO_PREFIX.length();
                    } else if (fileName.contains(FileUtil.URGENT_VIDEO_PREFIX)) {
                        startIndex = FileUtil.URGENT_VIDEO_PREFIX.length();
                    } else if (fileName.contains(FileUtil.LOOP_VIDEO_PREFIX)) {
                        startIndex = FileUtil.LOOP_VIDEO_PREFIX.length();
                    }

                    dateStr = fileName.substring(startIndex, fileName.indexOf("F."));
                    dateStr = "20" + dateStr;
                    dateStr = dateStr.replace("-", "");
                }

            } else {
                dateStr = videoFileInfoBean.timestamp;
                periodStr = videoFileInfoBean.period;
                size = videoFileInfoBean.filesize;
                resolution = videoFileInfoBean.resolution;

            }

            String time = FileInfoManagerUtils.countFileDateToString(dateStr);

            if (!TextUtils.isEmpty(periodStr) && TextUtils.isDigitsOnly(periodStr)) {
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
            return mVideoInfo;
        } catch (Exception e) {
        }
        return null;
    }

}
