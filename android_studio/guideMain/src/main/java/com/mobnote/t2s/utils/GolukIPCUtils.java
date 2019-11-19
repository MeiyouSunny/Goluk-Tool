package com.mobnote.t2s.utils;

import android.text.TextUtils;

import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import goluk.com.t1s.api.bean.FileInfo;

import static com.mobnote.t2s.files.IpcQuery.TYPE_CAPTURE;
import static com.mobnote.t2s.files.IpcQuery.TYPE_NORMAL;
import static com.mobnote.t2s.files.IpcQuery.TYPE_TIMESLAPSE;
import static com.mobnote.t2s.files.IpcQuery.TYPE_URGENT;

/**
 * Created by leege100 on 16/6/7.
 */
public class GolukIPCUtils {

//    /**
//     * 根据视频名获取远程视频地址
//     *
//     * @param fileName 视频名
//     * @return
//     */
//    public static String getRemoteVideoUrl(String fileName) {
//        if (!GolukIPCSdk.getInstance().isSdkValid())
//            return null;
//
//        if (TextUtils.isEmpty(fileName))
//            return "";
//
//        String[] names = fileName.split("_");
//        if (names != null && names.length > 3) {
//            if (names[0].equals("NRM")) {
//                fileName = names[0] + "_" + names[1];
//            } else {
//                fileName = names[0] + "_" + names[2];
//            }
//            return Const.REMOTE_VIDEO_FILE_URL + fileName;
//        }
//
//        return "";
//    }
//
//    /**
//     * 根据视频名获取远程视频封面
//     *
//     * @param fileName 视频名
//     * @return
//     */
//    public static String getRemoteVideoThumbUrl(String fileName) {
//        if (!GolukIPCSdk.getInstance().isSdkValid())
//            return "";
//
//        if (TextUtils.isEmpty(fileName))
//            return "";
//
//        String[] names = fileName.split("_");
//        if (names != null && names.length > 3) {
//            if (names[0].equals("NRM")) {
//                fileName = names[0] + "_" + names[1];
//            } else {
//                fileName = names[0] + "_" + names[2];
//            }
//            return Const.REMOTE_VIDEO_THUMB_URL + fileName;
//        }
//
//        return "";
//    }

    private static boolean videoFileIsNormalType(String videoName) {
        return !TextUtils.isEmpty(videoName) && videoName.contains("NRM");
    }

    private static boolean videoFileIsUrgentType(String videoName) {
        return !TextUtils.isEmpty(videoName) && videoName.contains("URG");
    }

    /**
     * F4的FileInfo转换为VideoInfo
     */
    public static VideoInfo parseF4FileInfo(FileInfo fileInfo, int videoType) {
        VideoInfo videoInfo = new VideoInfo();
        videoInfo.type = videoType;
        videoInfo.filename = fileInfo.name;
        videoInfo.videoUrl = fileInfo.videoUrl;
        videoInfo.thumbUrl = fileInfo.thumbUrl;
        videoInfo.relativePath = fileInfo.path;
        videoInfo.videoCreateDate = fileInfo.time.replaceAll("/", "-");
        videoInfo.time = parseStringToMilli(videoInfo.videoCreateDate);

        String[] nameSplits = fileInfo.name.split("_");
        if (fileInfo.name.startsWith("NRM_TL"))
            videoInfo.videoHP = nameSplits[2];
        else
            videoInfo.videoHP = nameSplits[1];

        return videoInfo;
    }

    public static List<FileInfo> filterFilesByType(List<FileInfo> files, int type) {
        List<FileInfo> list = new ArrayList<>();
        for (FileInfo fileInfo : files) {
            String fileName = fileInfo.name;
            if (type == TYPE_NORMAL && fileName.startsWith(FileConst.VIDEO_NRM) && !fileName.startsWith(FileConst.VIDEO_TIMESLAPSE)) {
                list.add(fileInfo);
            } else if (type == TYPE_TIMESLAPSE && fileName.startsWith(FileConst.VIDEO_TIMESLAPSE)) {
                list.add(fileInfo);
            } else if (type == TYPE_CAPTURE && fileName.startsWith(FileConst.VIDEO_WND)) {
                list.add(fileInfo);
            } else if (type == TYPE_URGENT && fileName.startsWith(FileConst.VIDEO_URG)) {
                list.add(fileInfo);
            }
        }

        return list;
    }

    public static long parseStringToMilli(String timeString) {
        if (TextUtils.isDigitsOnly(timeString))
            return -1;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date time = sdf.parse(timeString);
            return time.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }

}
