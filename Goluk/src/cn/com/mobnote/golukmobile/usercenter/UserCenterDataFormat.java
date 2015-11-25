package cn.com.mobnote.golukmobile.usercenter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.cluster.bean.UserLabelBean;
import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.user.User;

public class UserCenterDataFormat {

	/**
	 * 获取用户信息
	 * 
	 * @param str
	 * @return
	 */
	public UCUserInfo getUserInfo(String str) {
		try {
			UCUserInfo user = new UCUserInfo();

			JSONObject json = new JSONObject(str);
			JSONObject UserInfo = json.getJSONObject("UserInfo");
			Boolean success = UserInfo.getBoolean("success");
			if (success) {
				JSONObject data = UserInfo.getJSONObject("data");
				if ("0".equals(data.optString("result"))) {
					user.praisemenumber = data.optString("praisemenumber");
					user.sharevideonumber = data.optString("sharevideonumber");
					JSONObject u = data.getJSONObject("user");
					if(u != null){
						if(u.has("label")){
							JSONObject label = u.getJSONObject("label");
							if(label!=null){
								UserLabelBean ulb = new UserLabelBean();
								ulb.approve = label.optString("approve");
								ulb.approvelabel = label.optString("approvelabel");
								ulb.headplusv = label.optString("headplusv");
								ulb.headplusvdes = label.optString("headplusvdes");
								ulb.tarento = label.optString("tarento");
								ulb.tarentodes = label.optString("tarentodes");
								user.label = ulb ;
							}
						}
						user.uid = u.optString("uid");
						user.customavatar = u.optString("customavatar");
						user.headportrait = u.optString("headportrait");
						user.sex = u.optString("sex");
						user.introduce = u.optString("introduce");
						if(u.has("nickname")){
							user.nickname = u.optString("nickname");
						}
						return user;
					}else{
						return null;
					}
					
				} else {
					return null;
				}

			} else {
				return null;
			}
		} catch (JSONException e) {
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
	public List<VideoSquareInfo> getClusterList(String response) {
		List<VideoSquareInfo> clusters = null;

		JSONObject resource;
		JSONObject resData;
		try {
			resData = new JSONObject(response);
			if (resData != null) {
				resource = resData.getJSONObject("ShareVideoList");
				if (resource != null) {
					clusters = JsonParserUtils.parserNewestItemDataByJsonObj(resource);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return clusters;
	}

	/**
	 * 获取赞我的人数据
	 * 
	 * @param str
	 * @return
	 */
	public List<PraiseInfo> getPraises(String str) {
		try {
			List<PraiseInfo> result = new ArrayList<PraiseInfo>();

			JSONObject json = new JSONObject(str);
			JSONObject PraiseList = json.getJSONObject("PraiseList");
			if (PraiseList != null) {
				Boolean success = PraiseList.getBoolean("success");
				if (success) {
					JSONObject data = PraiseList.getJSONObject("data");
					if ("0".equals(data.getString("result"))) {
						JSONArray praiselist = data.getJSONArray("praiselist");
						PraiseInfo praiseinfo = null;
						for (int i = 0; i < praiselist.length(); i++) {
							JSONObject map = praiselist.getJSONObject(i);
							praiseinfo = new PraiseInfo();
							praiseinfo.uid = map.getString("uid");
							praiseinfo.nickname = map.getString("nickname");
							praiseinfo.headportrait = map.getString("headportrait");
							praiseinfo.customavatar = map.optString("customavatar");
							praiseinfo.introduce = map.getString("introduce");
							praiseinfo.picture = map.getString("picture");
							praiseinfo.videoid = map.getString("videoid");
							if(map.has("label")){
								JSONObject label = map.getJSONObject("label");
								UserLabelBean ulb = new UserLabelBean();
								ulb.approve = label.optString("approve");
								ulb.approvelabel = label.optString("approvelabel");
								ulb.headplusv = label.optString("headplusv");
								ulb.headplusvdes = label.optString("headplusvdes");
								ulb.tarento = label.optString("tarento");
								praiseinfo.label = ulb;
							}
							result.add(praiseinfo);
						}
						return result;
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
