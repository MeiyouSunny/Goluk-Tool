package com.goluk.videoedit.utils;

import java.util.List;

import com.goluk.videoedit.bean.ProjectItemBean;

public class VideoEditUtils {

	public static int generateIndexTag(List<ProjectItemBean> list) {
		if(null == list || list.size() == 0) {
			return 0;
		} else {
			return list.size();
		}
	}
}
