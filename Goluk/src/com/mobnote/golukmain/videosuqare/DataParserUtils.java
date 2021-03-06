package com.mobnote.golukmain.videosuqare;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataParserUtils {
	
	/**
	 * 解析视频广场列表数据
	 * @param json
	 * @return
	 * @author xuhw
	 * @date 2015年4月14日
	 */
	public static List<VideoSquareInfo> parserVideoSquareListData(String json){
		List<VideoSquareInfo> list = new ArrayList<VideoSquareInfo>();
		try {
			JSONObject obj = new JSONObject(json);
			if(null != obj){
				boolean success = obj.getBoolean("success");
				if(success){
					JSONObject data = obj.getJSONObject("data");
					if(null != data){
						String result = data.getString("result");
						if("0".equals(result)){
							JSONArray videolist = data.getJSONArray("videolist");
							if(null != videolist){
								long time = System.currentTimeMillis();
								for(int i=0; i<videolist.length(); i++){
									JSONObject videoinfo = videolist.getJSONObject(i);
									if(null != videoinfo){
										VideoEntity mVideoEntity = new VideoEntity();
										JSONObject video = videoinfo.getJSONObject("video");
										JSONObject live = video.getJSONObject("videodata");
										if(null != video){
											LiveVideoData lvd = new LiveVideoData();
											if(null != live){
												lvd.active = live.getString("active");
												lvd.aid = live.getString("aid");
												lvd.flux = live.getString("flux");
												lvd.lat= live.getString("lat");
												lvd.lon = live.getString("lon");
												lvd.mid = live.getString("mid");
												lvd.open = live.getString("open");
												lvd.restime = live.getString("restime");
												lvd.speed = live.getString("speed");
												lvd.tag = live.getString("tag");
												lvd.talk = live.getString("talk");
												lvd.vtype = live.getString("vtype");
											}
											mVideoEntity.videoid = video.optString("videoid");
											mVideoEntity.type = video.optString("type");
											mVideoEntity.sharingtime = video.optString("sharingtime");
											mVideoEntity.describe = video.optString("describe");
											mVideoEntity.picture = video.optString("picture");
											mVideoEntity.clicknumber = video.optString("clicknumber");
											mVideoEntity.praisenumber = video.optString("praisenumber");
											mVideoEntity.starttime = video.optString("starttime");
											mVideoEntity.livetime = video.optString("livetime");
											mVideoEntity.livewebaddress = video.optString("livewebaddress");
											mVideoEntity.livesdkaddress = video.optString("livesdkaddress");
											mVideoEntity.ondemandwebaddress = video.optString("ondemandwebaddress");
											mVideoEntity.ondemandsdkaddress = video.optString("ondemandsdkaddress");
											mVideoEntity.ispraise = video.optString("ispraise");
											mVideoEntity.livevideodata = lvd;
											
										}
										
										UserEntity mUserEntity = new UserEntity();
										JSONObject user = videoinfo.getJSONObject("user");
										if(null != user){
											mUserEntity.uid = user.optString("uid");
											mUserEntity.nickname = user.optString("nickname");
											mUserEntity.headportrait = user.optString("headportrait");
											mUserEntity.sex = user.optString("sex");
										}
										
										long id = time + i;
										VideoSquareInfo mVideoSquareInfo = new VideoSquareInfo();
										mVideoSquareInfo.mVideoEntity=mVideoEntity;
										mVideoSquareInfo.mUserEntity=mUserEntity;
										mVideoSquareInfo.id = ""+id;
										list.add(mVideoSquareInfo);
									}
								}
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	

}
