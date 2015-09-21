package cn.com.mobnote.golukmobile.usercenter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.golukmobile.newest.JsonParserUtils;
import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.CommentInfo;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;

public class UserCenterDataFormat {
	
	/**
	 * 获取用户信息
	 * @param str
	 * @return
	 */
	public UCUserInfo getUserInfo(String str){
		try {
			UCUserInfo user = new UCUserInfo();
			
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
					user.customavatar = u.getString("customavatar");
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
	public List<VideoSquareInfo> getClusterList(String response) {
		List<VideoSquareInfo> clusters = null;
		
		JSONObject resource;
		JSONObject resData;
		try {
			resData = new JSONObject(response);
			resource = resData.getJSONObject("ShareVideoList");
			if (resource != null) {
				clusters = JsonParserUtils.parserNewestItemDataByJsonObj(resource);
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
						praiseinfo.videoid = map.getString("videoid");
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
