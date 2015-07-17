package cn.com.mobnote.golukmobile.photoalbum;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.carrecorder.entity.DoubleVideoInfo;
import cn.com.mobnote.golukmobile.carrecorder.entity.VideoInfo;

public class VideoDataManagerUtils {
	
	/**
	 * 单个视频数据对象转双个
	 * @param datalist
	 * @return
	 * @author xuhw
	 * @date 2015年3月25日
	 */
	public static List<DoubleVideoInfo> videoInfo2Double(List<VideoInfo> datalist){
		List<DoubleVideoInfo> doublelist = new ArrayList<DoubleVideoInfo>();
		int i=0;
		while(i<datalist.size()){
			String groupname1 = "";
			String groupname2 = "";
			VideoInfo _videoInfo1 = null;
			VideoInfo _videoInfo2 = null;
			_videoInfo1 = datalist.get(i);
			groupname1 = _videoInfo1.videoCreateDate.substring(0, 10);
			
			if((i+1) < datalist.size()){
				_videoInfo2 = datalist.get(i+1);
				groupname2 = _videoInfo2.videoCreateDate.substring(0, 10);
			}
			
			if(groupname1.equals(groupname2)){
				i += 2;
			}else{
				i++;
				_videoInfo2=null;
			}
			
			DoubleVideoInfo dub = new DoubleVideoInfo(_videoInfo1, _videoInfo2);
			doublelist.add(dub);
		}
		
		return doublelist;
	}

}
