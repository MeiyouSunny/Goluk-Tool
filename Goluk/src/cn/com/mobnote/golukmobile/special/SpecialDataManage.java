package cn.com.mobnote.golukmobile.special;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpecialDataManage {

	/**
	 * @throws JSONException
	 *             解析专题列表数据json
	 * @Title: getListData
	 * @param data
	 * @return List<SpecialInfo>
	 * @author 曾浩
	 * @throws
	 */
	public List<SpecialInfo> getListData(String response) throws JSONException {
		JSONObject resource = new JSONObject(response);
		List<SpecialInfo> specials = null;
		SpecialInfo item = null;

		if (resource != null) {
			boolean success = resource.getBoolean("success");
			if (success) {
				specials = new ArrayList<SpecialInfo>();
				JSONObject data = resource.getJSONObject("data");
				String result = data.optString("result");
				if ("0".equals(result)) {

					// 解析视频列表集合
					JSONArray videolist = data.getJSONArray("videolist");
					if (videolist != null && videolist.length() > 0) {
						for (int i = 0; i < videolist.length(); i++) {
							JSONObject video = videolist.getJSONObject(i).getJSONObject("video");
							JSONObject user = videolist.getJSONObject(i).getJSONObject("user");
							item = new SpecialInfo();
							item.author = user.optString("nickname");
							item.describe = video.optString("describe");
							item.imagepath = video.optString("picture");
							item.videotype = "2";
							item.videopath = video.optString("ondemandwebaddress");
							item.videoid = video.optString("videoid");
							specials.add(item);

						}
					}

				}
			}
		}

		return specials;
	}

	public SpecialInfo getSpecialHead(String response) {
		JSONObject resource = null;
		SpecialInfo item = null;
		try {
			resource = new JSONObject(response);
			if (resource != null) {
				boolean success;
				success = resource.getBoolean("success");
				if (success) {
					JSONObject data = resource.getJSONObject("data");
					String result = data.optString("result");
					if ("0".equals(result)) {
						JSONObject head = data.getJSONObject("head");
						// 解析head
						if (head != null && !"".equals(head)) {
							item = new SpecialInfo();

							// 图片
							if ("1".equals(head.get("showhead"))) {
								item.imagepath = head.optString("headimg");
							} else {
								// 视频
								item.imagepath = head.optString("headvideoimg");
								item.videopath = head.optString("headvideo");
							}
							item.describe = head.optString("ztIntroduction");// 描述
							item.author = "";// 没有作者
						} else {
							return null;
						}

					}
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return item;
	}

	/**
	 * @throws JSONException
	 *             获取评论列表
	 * @Title: getComments
	 * @return List<CommentInfo>
	 * @author 曾浩
	 * @throws
	 */
	public Map<String, Object> getComments(String response) throws JSONException {
		JSONObject resource = new JSONObject(response);
		Map<String, Object> result = new HashMap<String, Object>();

		boolean success = resource.getBoolean("success");
		if (success) {
			JSONObject data = resource.getJSONObject("data");
			JSONObject comment = data.getJSONObject("comment");
			result.put("iscomment", comment.optString("iscomment"));
			result.put("comcount", comment.optString("comcount"));
			result.put("outurl", data.getJSONObject("head").optString("outurl"));
			result.put("ztitle", data.getJSONObject("head").optString("ztitle"));
			result.put("outurlname", data.getJSONObject("head").optString("outurlname"));
			CommentInfo ci = null;
			JSONArray comlist = comment.getJSONArray("comlist");

			List<CommentInfo> list = new ArrayList<CommentInfo>();

			if (comlist.length() > 0) {
				for (int i = 0; i < comlist.length(); i++) {
					JSONObject json = comlist.getJSONObject(i);

					ci = new CommentInfo();
					ci.authorid = json.optString("authorid");
					ci.avatar = json.optString("avatar");
					ci.commentid = json.optString("commentid");
					ci.name = json.optString("name");
					ci.text = json.optString("text");
					ci.time = json.optString("time");

					list.add(ci);
				}
			}

			result.put("comments", list);

			return result;
		} else {
			return null;
		}
	}

	/**
	 * 获取聚合的视频列表
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
	 * @throws JSONException
	 *             获取聚合头部信息
	 * @Title: getClusterHead
	 * @Description: TODO
	 * @param response
	 * @return JSONObject
	 * @author 曾浩
	 * @throws
	 */
	public SpecialInfo getClusterHead(String response) {
		JSONObject resource;
		SpecialInfo item = null;
		try {
			resource = new JSONObject(response);

			boolean success = resource.getBoolean("success");
			if (success) {
				JSONObject data = resource.getJSONObject("data");
				String result = data.optString("result");
				if ("0".equals(result)) {
					JSONObject head = data.getJSONObject("head");
					// 解析head
					if (head != null && !"".equals(head)) {
						item = new SpecialInfo();

						// 图片
						if ("1".equals(head.get("showhead"))) {
							item.imagepath = head.optString("headimg");
						} else {
							// 视频
							item.imagepath = head.optString("headvideoimg");
							item.videopath = head.optString("headvideo");
						}
						item.outurl = head.optString("outurl");
						item.outurlname = head.optString("outurlname");
						item.describe = head.optString("ztIntroduction");// 描述
						item.author = "";// 没有作者
						item.videotype = head.optString("showhead");
						return item;
					} else {
						return null;
					}
				} else {
					return null;
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return item;

	}

}
