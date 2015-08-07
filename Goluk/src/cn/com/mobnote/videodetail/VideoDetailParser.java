package cn.com.mobnote.videodetail;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoDetailParser {

	/*public static VideoSquareDetailInfo parserVideoSquareDetailData(String json){
		VideoSquareDetailInfo videoSquareDetailInfo = new VideoSquareDetailInfo();
		try {
			JSONObject obj = new JSONObject(json);
			if(null != obj){
				boolean success = obj.optBoolean("success");
				if(success){
					JSONObject data = obj.getJSONObject("data");
					if(null != data){
						String result = data.getString("result");
						if("0".equals(result)){
							JSONObject aVideo = data.getJSONObject("avideo");
							if(null != aVideo){
								VideoInfo videoInfo = new VideoInfo();
								String videoStr = aVideo.optString("video");
								String userStr = aVideo.optString("user");
								//解析video
								JSONObject video = new JSONObject(videoStr);
								String commentStr = video.optString("comment");
								JSONObject comment = new JSONObject(commentStr);
								String videodataStr = video.optString("videodata");
								JSONObject videodata = new JSONObject(videodataStr);
								if(null != video){
									VideoCommentInfo videoComment = new VideoCommentInfo();
									//comment
									if(null != comment){
										videoComment.iscomment = comment.optString("iscomment");
										videoComment.comcount = comment.optString("comcount");
										//解析list
										JSONArray list = comment.getJSONArray("comlist");
										GolukDebugUtils.i("detail", "------list----"+list);
										if(null != list){
											VideoListInfo videoList = new VideoListInfo();
											for(int i=0;i<list.length();i++){
												JSONObject infoList = list.getJSONObject(i);
												GolukDebugUtils.i("detail", "------infoList----"+infoList);
												if(null != infoList){
													GolukDebugUtils.i("detail", "------videoList.headimg----"+infoList.optString("headimg"));
													GolukDebugUtils.i("detail", "------videoList.nickname----"+infoList.optString("nickname"));
													GolukDebugUtils.i("detail", "------videoList.content----"+infoList.optString("content"));
													GolukDebugUtils.i("detail", "------videoList.commtime----"+infoList.optString("commtime"));
													videoList.headimg = infoList.optString("headimg");
													videoList.nickname = infoList.optString("nickname");
													videoList.content = infoList.optString("content");
													videoList.commtime = infoList.optString("commtime");
												}
												GolukDebugUtils.i("detail", "---------videoList--------"+videoList.toString());
												GolukDebugUtils.i("detail", "---------videoList--------"+videoList.content);
//												videoComment.list.add(videoList);
//												GolukDebugUtils.i("detail", "------videoComment.list.size()-----"+videoComment.list.size());
											}
										}
									}
									//videoData
									VideoDataInfo videoDataInfo = new VideoDataInfo();
									if(null != videodata){
										GolukDebugUtils.i("detail", "---------aid------"+videodata.optString("aid"));
										videoDataInfo.aid = videodata.optString("aid");
										videoDataInfo.mid = videodata.optString("mid");
										videoDataInfo.activie = videodata.optString("activie");
										videoDataInfo.tag = videodata.optString("tag");
										videoDataInfo.open = videodata.optString("open");
										videoDataInfo.lon = videodata.optString("lon");
										videoDataInfo.lat = videodata.optString("lat");
										videoDataInfo.speed = videodata.optString("speed");
										videoDataInfo.talk = videodata.optString("talk");
										videoDataInfo.voice = videodata.optString("voice");
										videoDataInfo.vtype = videodata.optString("vtype");
										videoDataInfo.restime = videodata.optString("restime");
										videoDataInfo.flux = videodata.optString("flux");
									}
									GolukDebugUtils.i("detail", "------videoInfo.videoid-------"+video.optString("videoid"));
									//video
									videoInfo.videoid = video.optString("videoid");
									videoInfo.type = video.optString("type");
									videoInfo.sharingtime = video.optString("sharingtime");
									videoInfo.describe = video.optString("describe");
									videoInfo.picture = video.optString("picture");
									videoInfo.clicknumber = video.optString("clicknumber");
									videoInfo.praisenumber = video.optString("praisenumber");
									videoInfo.starttime = video.optString("starttime");
									videoInfo.livetime = video.optString("livetime");
									videoInfo.livewebaddress = video.optString("livewebaddress");
									videoInfo.livesdkaddress = video.optString("livesdkaddress");
									videoInfo.ondemandwebaddress= video.optString("ondemandwebaddress");
									videoInfo.ondemandsdkaddress = video.optString("ondemandsdkaddress");
									videoInfo.ispraise = video.optString("ispraise");
									videoInfo.reason = video.optString("reason");
									videoInfo.videodata = videoDataInfo;
									videoInfo.comment = videoComment;
								}
								//解析user
								VideoUserInfo videoUser = new VideoUserInfo();
								JSONObject user = new JSONObject(userStr);
								if(null != user){
									GolukDebugUtils.i("detail", "---------videoUser.uid------"+user.optString("uid"));
									videoUser.uid = user.optString("uid");
									videoUser.nickname = user.optString("nickname");
									videoUser.headportrait = user.optString("headportrait");
									videoUser.sex = user.optString("sex");
								}
								
								VideoSquareDetailInfo mVideoSquareDetail = new VideoSquareDetailInfo();
								mVideoSquareDetail.video = videoInfo;
								mVideoSquareDetail.user = videoUser;
								GolukDebugUtils.i("detail", "------------final----------"+mVideoSquareDetail.video.comment.comcount);
								
							}
						}
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return videoSquareDetailInfo;
	}*/
	
	
	public static VideoJson parseDataFromJson(String json){
		VideoJson object = new VideoJson();
		VideoSquareDetailInfo videoDetailInfo = new VideoSquareDetailInfo();
		List<VideoListInfo> com_list = new ArrayList<VideoListInfo>();
		try {
			JSONObject obj = new JSONObject(json);
			if(true == obj.getBoolean("success")){
				object.msg = obj.optString("msg");
				object.success = obj.optBoolean("success");
				JSONObject json_data = obj.optJSONObject("data");
				VideoAllData data = new VideoAllData();
				if(null != json_data){
					data.result = json_data.optString("result");
					JSONObject json_avideo = json_data.optJSONObject("avideo");
					if(null != json_avideo){
						JSONObject json_user = json_avideo.optJSONObject("user");
						if(null != json_user){
							VideoUserInfo user = new VideoUserInfo();
							user.uid = json_user.optString("uid");
							user.nickname = json_user.optString("nickname");
							user.headportrait = json_user.optString("headportrait");
							user.sex = json_user.optString("sex");
							videoDetailInfo.user = user;
						}
						JSONObject json_video = json_avideo.optJSONObject("video");
						if(null != json_video){
							VideoInfo video = new VideoInfo();
							video.videoid = json_video.optString("videoid");
							video.type = json_video.optString("type");
							video.sharingtime = json_video.optString("sharingtime");
							video.describe = json_video.optString("describe");
							video.picture = json_video.optString("picture");
							video.clicknumber = json_video.optString("clicknumber");
							video.praisenumber = json_video.optString("praisenumber");
							video.starttime = json_video.optString("starttime");
							video.livetime = json_video.optString("livetime");
							video.livewebaddress = json_video.optString("livewebaddress");
							video.livesdkaddress = json_video.optString("livesdkaddress");
							video.ondemandwebaddress= json_video.optString("ondemandwebaddress");
							video.ondemandsdkaddress = json_video.optString("ondemandsdkaddress");
							video.ispraise = json_video.optString("ispraise");
							video.reason = json_video.optString("reason");
							JSONObject json_comment = json_video.optJSONObject("comment");
							if(null != json_comment){
								VideoCommentInfo comment = new VideoCommentInfo();
								comment.comcount = json_comment.optString("comcount");
								comment.iscomment = json_comment.optString("iscomment");
								JSONArray json_list = json_comment.getJSONArray("comlist");
								for(int i=0;i<json_list.length();i++){
									JSONObject json_comlist = json_list.getJSONObject(i);
									VideoListInfo list = new VideoListInfo();
									list.headimg = json_comlist.optString("headimg");
									list.nickname = json_comlist.optString("nickname");
									list.content = json_comlist.optString("content");
									list.commtime = json_comlist.optString("commtime");
									com_list.add(list);
									comment.list  = com_list;
								}
								video.comment = comment;
							}
							JSONObject json_videodata = json_video.optJSONObject("videodata");
							if(null != json_videodata){
								VideoDataInfo vd = new VideoDataInfo();
								vd.aid = json_videodata.optString("aid");
								vd.mid = json_videodata.optString("mid");
								vd.activie = json_videodata.optString("activie");
								vd.tag = json_videodata.optString("tag");
								vd.open = json_videodata.optString("open");
								vd.lon = json_videodata.optString("lon");
								vd.lat = json_videodata.optString("lat");
								vd.speed = json_videodata.optString("speed");
								vd.talk = json_videodata.optString("talk");
								vd.voice = json_videodata.optString("voice");
								vd.vtype = json_videodata.optString("vtype");
								vd.restime = json_videodata.optString("restime");
								vd.flux = json_videodata.optString("flux");
								video.videodata = vd;
							}
							videoDetailInfo.video = video;
						}
						data.avideo = videoDetailInfo;
					}
					object.data = data;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
}
