package cn.com.mobnote.golukmobile.usercenter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.CommentInfo;

public class UserCenterDataFormat {
	
	/**
	 * 获取用户信息
	 * @param str
	 * @return
	 */
	public UserInfo getUserInfo(String str){
		try {
			UserInfo user = new UserInfo();
			
			JSONObject json = new JSONObject(str);
			JSONObject UserInfo = json.getJSONObject("UserInfo");
			Boolean success = UserInfo.getBoolean("success");
			if(success){
				JSONObject data = UserInfo.getJSONObject("data");
				if("0".equals(data.getString("result"))){
					user.praisemenumber = data.getString("praisemenumber");
					user.sharevideonumber = data.getString("sharevideonumber");
					JSONObject u = data.getJSONObject("user");
					user.uid = u.getString("uid");
					user.customavatar = u.getString("");
					user.headportrait = u.getString("headportrait");
					user.sex = u.getString("sex");
					user.introduce = u.getString("introduce");
					user.nickname = u.getString("nickname");
					return user;
				}else{
					return null;
				}
				
			}else{
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
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
	public List<ClusterInfo> getClusterList(String response) {
		List<ClusterInfo> clusters = null;
		ClusterInfo item = null;

		JSONObject resource;
		try {
			resource = new JSONObject(response);

			if (resource != null) {
				boolean success = resource.getBoolean("success");
				if (success) {
					clusters = new ArrayList<ClusterInfo>();
					JSONObject data = resource.getJSONObject("data");
					String result = data.optString("result");
					if ("0".equals(result)) {

						// 解析视频列表集合
						JSONArray videolist = data.getJSONArray("videolist");
						System.out.println(videolist.length());
						if (videolist != null && videolist.length() > 0) {
							for (int i = 0; i < videolist.length(); i++) {
								JSONObject video = videolist.getJSONObject(i).getJSONObject("video");
								JSONObject user = videolist.getJSONObject(i).getJSONObject("user");
								item = new ClusterInfo();
								item.author = user.optString("nickname");
								item.describe = video.optString("describe");
								item.imagepath = video.optString("picture");
								item.clicknumber = video.optString("clicknumber");
								item.videoid = video.optString("videoid");
								item.praisenumber = video.optString("praisenumber");
								item.sharingtime = video.optString("sharingtime");
								item.headportrait = user.optString("headportrait");
								item.ispraise = video.optString("ispraise");
								item.videotype = "2";
								item.videopath = video.optString("ondemandwebaddress");
								item.uid = video.optString("uid");

								JSONObject comment = video.getJSONObject("comment");
								item.iscomment = comment.optString("iscomment");
								item.comments = comment.optString("comcount");
								JSONArray comlist = comment.getJSONArray("comlist");

								if (comlist.length() > 0) {
									for (int j = 0; j < comlist.length(); j++) {
										JSONObject json = comlist.getJSONObject(j);

										CommentInfo ci = new CommentInfo();
										ci.authorid = json.optString("authorid");
										ci.avatar = json.optString("avatar");
										ci.commentid = json.optString("commentid");
										ci.name = json.optString("name");
										ci.text = json.optString("text");
										ci.time = json.optString("time");
										if (j == 0) {
											item.ci1 = ci;
										} else if (j == 1) {
											item.ci2 = ci;
										} else if (j == 2) {
											item.ci3 = ci;
										}
									}
								}

								clusters.add(item);

							}
						}

					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return clusters;
	}
	
	/**
	 * 获取赞我的人数据
	 * @param str
	 * @return
	 */
	public List<PraiseInfo> getPraises(String str){
		try {
			List<PraiseInfo> result = new ArrayList<PraiseInfo>();
			
			JSONObject json = new JSONObject(str);
			JSONObject PraiseList = json.getJSONObject("PraiseList");
			Boolean success = PraiseList.getBoolean("success");
			if(success){
				JSONObject data = PraiseList.getJSONObject("data");
				if("0".equals(data.getString("result"))){
					JSONArray praiselist = data.getJSONArray("praiselist");
					PraiseInfo praiseinfo = null;
					for (int i = 0; i < praiselist.length(); i++) {
						JSONObject map = praiselist.getJSONObject(i);
						praiseinfo = new PraiseInfo();
						praiseinfo.uid = map.getString("uid");
						praiseinfo.nickname = map.getString("nickname");
						praiseinfo.headportrait = map.getString("headportrait");
						praiseinfo.introduce = map.getString("introduce");
						praiseinfo.picture = map.getString("picture");
						result.add(praiseinfo);
					}
					return result;
				}else{
					return null;
				}
				
			}else{
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
