package cn.com.mobnote.golukmobile.videosuqare;

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
							String videocount = data.getString("videocount");
							JSONArray videolist = data.getJSONArray("videolist");
							if(null != videolist){
								for(int i=0; i<videolist.length(); i++){
									JSONObject videoinfo = videolist.getJSONObject(i);
									if(null != videoinfo){
										VideoEntity mVideoEntity = new VideoEntity();
										JSONObject video = videoinfo.getJSONObject("video");
										if(null != video){
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
										}
										
										UserEntity mUserEntity = new UserEntity();
										JSONObject user = videoinfo.getJSONObject("user");
										if(null != user){
											mUserEntity.uid = user.optString("uid");
											mUserEntity.nickname = user.optString("nickname");
											mUserEntity.headportrait = user.optString("headportrait");
										}
										
										VideoSquareInfo mVideoSquareInfo = new VideoSquareInfo();
										mVideoSquareInfo.mVideoEntity=mVideoEntity;
										mVideoSquareInfo.mUserEntity=mUserEntity;
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
