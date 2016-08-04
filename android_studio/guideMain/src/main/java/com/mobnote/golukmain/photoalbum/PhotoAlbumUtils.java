package com.mobnote.golukmain.photoalbum;

import java.util.List;
import com.mobnote.golukmain.carrecorder.entity.VideoInfo;

public class PhotoAlbumUtils {
	/**
	 * 查询文件录制起始时间
	 * @param filename
	 * @param list
	 * @return
	 */
	public static long findtime(String filename,List<VideoInfo> list) {
		long time = 0;
		if (null != list) {
			for (int i = 0; i < list.size(); i++) {
				if (filename.equals(list.get(i).filename)) {
					return list.get(i).time;
				}
			}
		}
		return time;
	}

}
