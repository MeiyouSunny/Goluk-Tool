package cn.com.mobnote.golukmobile.cluster.bean;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.newest.CommentDataInfo;
import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.videosuqare.LiveVideoData;
import cn.com.mobnote.golukmobile.videosuqare.UserEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoExtra;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

public class VolleyDataFormat {

	/**
	 * 获取视频列表数据
	 * 
	 * @Title: getClusterList
	 * @Description: TODO
	 * @param response
	 * @return List<ClusterInfo>
	 * @author 曾浩
	 * @throws
	 */
	public List<VideoSquareInfo> getClusterList(List<VideoListBean> list) {
		List<VideoSquareInfo> clusters = null;

		if(list != null && list.size()>0){
			clusters = new ArrayList<VideoSquareInfo>();
			long time = System.currentTimeMillis();
			for (int i = 0; i < list.size(); i++) {
				VideoListBean videoinfo = list.get(i);
				if (null != videoinfo) {
					VideoEntity mVideoEntity = new VideoEntity();
					VideoBean video = videoinfo.video;
					if (null != video) {
						LiveVideoData lvd = new LiveVideoData();
						VideoDataBean live = video.videodata;
						if (null != live) {
							lvd.active = live.active;
							lvd.aid = live.aid;
							lvd.flux = live.flux;
							lvd.lat = live.lat;
							lvd.lon = live.lon;
							lvd.mid = live.mid;
							lvd.open = live.open;
							lvd.restime = live.restime;
							lvd.speed = live.speed;
							lvd.tag = live.tag;
							lvd.talk = live.talk;
							lvd.vtype = live.vtype;
						}
						mVideoEntity.videoid = video.videoid;
						mVideoEntity.type = video.type;
						mVideoEntity.sharingtime = video.sharingtime;
						mVideoEntity.describe = video.describe;
						mVideoEntity.picture = video.picture;
						mVideoEntity.clicknumber = video.clicknumber;
						mVideoEntity.praisenumber = video.praisenumber;
						mVideoEntity.starttime = video.starttime;
						mVideoEntity.livetime = video.livetime;
						mVideoEntity.livewebaddress = video.livewebaddress;
						mVideoEntity.livesdkaddress = video.livesdkaddress;
						mVideoEntity.ondemandwebaddress = video.ondemandwebaddress;
						mVideoEntity.ondemandsdkaddress = video.ondemandsdkaddress;
						mVideoEntity.ispraise = video.ispraise;
						mVideoEntity.livevideodata = lvd;
						mVideoEntity.location = video.location;
						mVideoEntity.reason = video.reason;
						mVideoEntity.isopen = "0";
						
						GenBean gb = video.gen;
						if(gb != null){
							VideoExtra ve = new VideoExtra();
							ve.topicid = gb.topicid;
							ve.channelid = gb.channelid;
							ve.isrecommend = gb.isrecommend;
							ve.isreward = gb.isreward;
							ve.topicname = gb.topicname;
							mVideoEntity.videoExtra = ve;
						}
						

						CommentBean comment = video.comment;
						if (null != comment) {
							mVideoEntity.iscomment = comment.iscomment;
							mVideoEntity.comcount = comment.comcount;

							List<ComlistBean> comlist = comment.comlist;
							if (null != comlist) {
								for (int j = 0; j < comlist.size(); j++) {
									ComlistBean item = comlist.get(j);
									CommentDataInfo comminfo = new CommentDataInfo();
									
									comminfo.commentid = item.commentid;
									comminfo.authorid = item.authorid;
									comminfo.name = item.name;
									comminfo.avatar = item.avatar;
									comminfo.time = item.text;
									comminfo.text = item.text;
									comminfo.replyid = item.replyid;
									comminfo.replyname = item.replyname;
									mVideoEntity.commentList.add(comminfo);
								}
							}
						}

					}

					UserEntity mUserEntity = new UserEntity();
					UserBean user = videoinfo.user;
					if (null != user) {
						mUserEntity.uid = user.uid;
						mUserEntity.nickname = user.nickname;
						mUserEntity.headportrait = user.headportrait;
						mUserEntity.sex = user.sex;
						mUserEntity.mCustomAvatar = user.customavatar;
					}

					long id = time + i;
					VideoSquareInfo mVideoSquareInfo = new VideoSquareInfo();
					mVideoSquareInfo.mVideoEntity = mVideoEntity;
					mVideoSquareInfo.mUserEntity = mUserEntity;
					mVideoSquareInfo.id = "" + id;
					clusters.add(mVideoSquareInfo);
				}
			}
		}
		return clusters;
	}
	
}
