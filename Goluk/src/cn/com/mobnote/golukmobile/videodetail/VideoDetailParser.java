package cn.com.mobnote.golukmobile.videodetail;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.cluster.bean.UserLabelBean;

public class VideoDetailParser {

	public static VideoJson parseDataFromJson(String json) {
		try {
			VideoJson object = null;
			JSONObject obj = new JSONObject(json);
			if (true == obj.getBoolean("success")) {
				object = new VideoJson();
				object.msg = obj.optString("msg");
				object.success = obj.optBoolean("success");
				JSONObject json_data = obj.getJSONObject("data");
				VideoAllData data = new VideoAllData();
				// if(null != json_data){
				VideoSquareDetailInfo videoDetailInfo = new VideoSquareDetailInfo();
				data.result = json_data.optString("result");
				JSONObject json_avideo = json_data.optJSONObject("avideo");
				if (null != json_avideo) {
					JSONObject json_user = json_avideo.optJSONObject("user");
					if (null != json_user) {
						VideoUserInfo user = new VideoUserInfo();
						user.uid = json_user.optString("uid");
						user.nickname = json_user.optString("nickname");
						user.headportrait = json_user.optString("headportrait");
						user.customavatar = json_user.optString("customavatar");
						user.sex = json_user.optString("sex");
						JSONObject json_label = json_user.optJSONObject("label");
						if(null != json_label) {
							UserLabelBean label = new UserLabelBean();
							label.approve = json_label.optString("approve");
							label.approvelabel = json_label.optString("approvelabel");
							label.tarento = json_label.optString("tarento");
							label.headplusv = json_label.optString("headplusv");
							label.headplusvdes = json_label.optString("headplusvdes");
							user.mUserLabel = label;
						}
						videoDetailInfo.user = user;
					}
					JSONObject json_video = json_avideo.optJSONObject("video");
					if (null != json_video) {
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
						video.ondemandwebaddress = json_video.optString("ondemandwebaddress");
						video.ondemandsdkaddress = json_video.optString("ondemandsdkaddress");
						video.ispraise = json_video.optString("ispraise");
						video.reason = json_video.optString("reason");
						video.mLocation = json_video.optString("location");
						JSONObject json_comment = json_video.optJSONObject("comment");
						if (null != json_comment) {
							List<VideoListInfo> com_list = new ArrayList<VideoListInfo>();
							VideoCommentInfo comment = new VideoCommentInfo();
							comment.comcount = json_comment.optString("comcount");
							comment.iscomment = json_comment.optString("iscomment");
							JSONArray json_list = json_comment.getJSONArray("comlist");
							for (int i = 0; i < json_list.length(); i++) {
								JSONObject json_comlist = json_list.getJSONObject(i);
								VideoListInfo list = new VideoListInfo();
								list.avatar = json_comlist.optString("avatar");
								list.name = json_comlist.optString("name");
								list.text = json_comlist.optString("text");
								list.time = json_comlist.optString("time");
								list.authorid = json_comlist.optString("authorid");
								list.commentid = json_comlist.optString("commentid");
								com_list.add(list);
							}
							comment.comlist = com_list;
							video.comment = comment;
						}
						JSONObject json_videodata = json_video.optJSONObject("videodata");
						if (null != json_videodata) {
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
						JSONObject json_recom = json_video.optJSONObject("gen");
						if(null != json_recom) {
							VideoRecommend recommend = new VideoRecommend();
							recommend.topicid = json_recom.optString("topicid");
							recommend.topicname = json_recom.optString("topicname");
							recommend.chanid = json_recom.optString("chanid");
							recommend.chaname = json_recom.optString("chaname");
							recommend.isreward = json_recom.optString("isreward");
							recommend.atflag = json_recom.optString("atflag");
							recommend.atreason = json_recom.optString("atreason");
							recommend.atgold = json_recom.optString("atgold");
							recommend.sysflag = json_recom.optString("sysflag");
							recommend.sysreason = json_recom.optString("sysreason");
							recommend.sysgold = json_recom.optString("sysgold");
							recommend.isrecommend = json_recom.optString("isrecommend");
							recommend.reason = json_recom.optString("reason");
							video.recom = recommend;
						}
						videoDetailInfo.video = video;
					}
					data.avideo = videoDetailInfo;
					
					JSONObject headObj = json_data.optJSONObject("head");
					if (null != headObj) {
						ZTHead head = new ZTHead();
						final String ztitle = headObj.optString("ztitle");
						head.ztitle = ztitle;
						videoDetailInfo.head = head;
					}
					
				}
				// link
				JSONObject json_link = json_data.optJSONObject("link");
				if (null != json_link) {
					VideoLink videoLink = new VideoLink();
					videoLink.outurl = json_link.optString("outurl");
					videoLink.outurlname = json_link.optString("outurlname");
					videoLink.showurl = json_link.optString("showurl");
					data.link = videoLink;
				}
				object.data = data;
				// }
			}
			return object;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
