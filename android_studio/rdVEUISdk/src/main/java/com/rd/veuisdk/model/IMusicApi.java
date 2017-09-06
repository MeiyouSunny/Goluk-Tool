package com.rd.veuisdk.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.rd.http.MD5;
import com.rd.veuisdk.utils.PathUtils;

/**
 * 新版配乐2->自定义网络音乐地址
 * 
 * @author JIAN
 * @date 2017-5-17 下午4:49:47
 */
public class IMusicApi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public IMusicApi(String menu, ArrayList<WebMusicInfo> webs) {
		this.menu = menu;
		this.webs = webs;
	}

	public IMusicApi(JSONObject jobj) throws JSONException {

		this.menu = jobj.getString("name");
		JSONArray jarr = jobj.getJSONArray("musiclist");
		int len = 0;
		if (null != jarr && (len = jarr.length()) > 0) {
			JSONObject jtemp;
			webs = new ArrayList<WebMusicInfo>();
			WebMusicInfo info;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < len; i++) {
				jtemp = jarr.getJSONObject(i);
				info = new WebMusicInfo();
				info.setMusicUrl(jtemp.optString("url"));
				info.setMusicName(jtemp.optString("name"));
				info.setDuration(jtemp.optInt("times") * 1000);
				sb = new StringBuffer(100);
				sb.append(PathUtils.getRdMusic());
				sb.append("/");
				sb.append(MD5.getMD5(info.getMusicUrl()));
				sb.append(".mp3");
				info.setLocalPath(sb.toString());
				info.checkExists();
				webs.add(info);
			}

		}

	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public ArrayList<WebMusicInfo> getWebs() {
		return webs;
	}

	public void setWebs(ArrayList<WebMusicInfo> webs) {
		this.webs = webs;
	}

	private String menu;
	private ArrayList<WebMusicInfo> webs;

}
