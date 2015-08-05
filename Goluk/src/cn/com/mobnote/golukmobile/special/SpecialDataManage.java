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
	 * 解析专题列表数据json
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
						specials.add(item);
					} else {
						return null;
					}

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
							item.videopath = video.optString("ondemandsdkaddress");
							specials.add(item);

						}
					}

				}
			}
		}

		return specials;
	}

	/**
	 * @throws JSONException
	 *  获取评论列表
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

}
