package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.videosuqare.LiveVideoData;
import cn.com.mobnote.golukmobile.videosuqare.UserEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

public class JsonParserUtils {
	
	public static List<JXListItemDataInfo> parserJXData(String jsonStr) {
		List<JXListItemDataInfo> jxlistdata = new ArrayList<JXListItemDataInfo>();
		try {
			JSONObject json = new JSONObject(jsonStr);
			boolean success = json.optBoolean("success");
			if (success) {
				JSONObject data = json.optJSONObject("data");
				if (null != data) {
					String result = data.optString("result");
					String count = data.optString("count");
					JSONArray list  = data.optJSONArray("list");
					if (null != list) {
						for (int i=0; i<list.length(); i++) {
							JSONObject object = (JSONObject) list.opt(i);
							if (null != object) {
								String jxid = object.optString("jxid");
								String jxdate = object.optString("jxdate");
								JSONArray jxlist  = object.optJSONArray("jxlist");
								if (null != jxlist) {
									for (int j=0; j<jxlist.length(); j++) {
										String date = "";
										if(j == 0) {
											date = jxdate;
										}
										JSONObject jxlistitem = (JSONObject) jxlist.opt(j);
										JXListItemDataInfo info = new JXListItemDataInfo(jxlistitem, date, jxid);
										jxlistdata.add(info);
									}
								}
							}
						}
					}
				}
			}else {
				String msg = json.optString("msg");
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jxlistdata;
	}
	
	public static int parserJXCount(String jsonStr) {
		try {
			JSONObject json = new JSONObject(jsonStr);
			boolean success = json.optBoolean("success");
			if (success) {
				JSONObject data = json.optJSONObject("data");
				if (null != data) {
					String count = data.getString("count");
					int number = Integer.parseInt(count);
					return number;
				}
			}else {
				String msg = json.optString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public static NewestListHeadDataInfo parserNewestHeadData(String jsonStr) {
		NewestListHeadDataInfo info = new NewestListHeadDataInfo();
		try {
			JSONObject json = new JSONObject(jsonStr);
			boolean success = json.optBoolean("success");
			if (success) {
				JSONObject data = json.optJSONObject("data");
				if (null != data) {
					JSONArray category  = data.optJSONArray("category");
					if (null != category) {
						for (int i=0; i<category.length(); i++) {
							JSONObject item = category.getJSONObject(i);
							CategoryDataInfo cate = new CategoryDataInfo(item);
							info.categoryList.add(cate);
						}
					}
					JSONObject live = json.optJSONObject("live");
					if (null != live) {
						LiveInfo liveinfo = new LiveInfo(live);
						info.mLiveDataInfo = liveinfo;
					}
				}
			}else {
				String msg = json.optString("msg");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return info;
	}
	
	public static List<VideoSquareInfo> parserNewestItemData(String jsonStr) {
		List<VideoSquareInfo> mDataList = new ArrayList<VideoSquareInfo>();
		try {
			JSONObject obj = new JSONObject(jsonStr);
			if(null != obj){
				boolean success = obj.optBoolean("success");
				if(success){
					JSONObject data = obj.optJSONObject("data");
					if(null != data){
						String result = data.optString("result");
						if("0".equals(result)){
							JSONArray videolist = data.optJSONArray("videolist");
							if(null != videolist){
								long time = System.currentTimeMillis();
								for(int i=0; i<videolist.length(); i++){
									JSONObject videoinfo = videolist.optJSONObject(i);
									if(null != videoinfo){
										VideoEntity mVideoEntity = new VideoEntity();
										JSONObject video = videoinfo.optJSONObject("video");
										if(null != video){
											LiveVideoData lvd = new LiveVideoData();
											JSONObject live = video.optJSONObject("videodata");
											if(null != live){
												lvd.active = live.optString("active");
												lvd.aid = live.optString("aid");
												lvd.flux = live.optString("flux");
												lvd.lat= live.optString("lat");
												lvd.lon = live.optString("lon");
												lvd.mid = live.optString("mid");
												lvd.open = live.optString("open");
												lvd.restime = live.optString("restime");
												lvd.speed = live.optString("speed");
												lvd.tag = live.optString("tag");
												lvd.talk = live.optString("talk");
												lvd.vtype = live.optString("vtype");
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
											mVideoEntity.reason = video.optString("reason");
											mVideoEntity.iscomment = video.optString("iscomment");
											mVideoEntity.comcount = video.optString("comcount");
											
											JSONArray comment = video.optJSONArray("comment");
											if(null != comment){
												for(int j=0; j<comment.length(); j++){
													JSONObject item = comment.getJSONObject(j);
													CommentDataInfo comminfo = new CommentDataInfo(item);
													mVideoEntity.commentList.add(comminfo);
												}
											}
											
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
										mDataList.add(mVideoSquareInfo);
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
		
		return mDataList;
	}
	
}
