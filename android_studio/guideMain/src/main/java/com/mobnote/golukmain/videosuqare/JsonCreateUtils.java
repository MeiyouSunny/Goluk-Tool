package com.mobnote.golukmain.videosuqare;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 1.编辑器必须显示空白处
 *
 * 2.所有代码必须使用TAB键缩进
 *
 * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
 *
 * 4.注释必须在行首写.(枚举除外)
 *
 * 5.函数使用块注释,代码逻辑使用行注释
 *
 * 6.文件头部必须写功能说明
 *
 * 7.所有代码文件头部必须包含规则说明
 *
 * 组织logic需要传递的json数据类
 *
 * 2015年4月17日
 *
 * @author xuhw
 */
public class JsonCreateUtils {

	/**
	 * 组织广场列表json请求字符串
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param type
	 *            视频类型：0.全部 1.直播 2.点播
	 * @param attribute
	 *            属性标签：[“1”,”2”,”3”] 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料
	 *            6.堵车预警 7.惊险十分 8.疯狂超车 9.感人瞬间 10.传递正能量
	 * @param operation
	 *            操作：0.首次进入30条 1.下拉30条 2.上拉30条
	 * @param timestamp
	 *            时间戳：首次进入为空
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getSquareListRequestJson(String channel, String type,
			List<String> attribute, String operation, String timestamp) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("type", type);
			obj.put("operation", operation);
			obj.put("timestamp", timestamp);
			obj.put("attributeUncode", attribute.get(0));
			
			JSONArray arr = new JSONArray();
			for(int i=0; i<attribute.size(); i++){
				arr.put(attribute.get(i));
			}
			
			GolukDebugUtils.d("","SSS=====$$$$$$$$$$$$=========arr.toString()=="+arr.toString());
			String attributestr = URLEncoder.encode(arr.toString(), "UTF-8");
			obj.put("attribute", attributestr);

			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getSquareListRequestJson(String channel, String type,
			String attribute, String operation, String timestamp) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("type", type);
			obj.put("operation", operation);
			obj.put("timestamp", timestamp);
			obj.put("attribute", attribute);
			
			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * 组织热门列表json请求字符串
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param operation
	 *            操作：0.首次进入30条 1.下拉30条 2.上拉30条
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getHotListRequestJson(String channel, String operation) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("operation", operation);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * 组织点击次数上报json字符串
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param mDataList
	 *            视频数据列表数据
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getClickVideoUploadRequestJson(String channel,
			List<VideoSquareInfo> mDataList) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			JSONArray arr = new JSONArray();
			for (int i = 0; i < mDataList.size(); i++) {
				JSONObject a = new JSONObject();
				VideoEntity mVideoEntity = mDataList.get(i).mVideoEntity;
				if (null != mVideoEntity) {
					a.put("videoid", mVideoEntity.videoid);
					a.put("number", mVideoEntity.clicknumber);
				}
				arr.put(a);
			}

			String str = URLEncoder.encode(arr.toString(), "UTF-8");
			obj.put("videolist", str);
			json = obj.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * 组织点赞json字符串
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param type
	 *            点赞类型：0.取消点赞 1.点赞
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getClickPraiseRequestJson(String channel,
			String videoid, String type) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("videoid", videoid);
			obj.put("type", type);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * 组织举报json字符串
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @param reporttype
	 *            举报类型：1.色情低俗 2.谣言惑众 3.政治敏感 4.其他原因
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getReportRequestJson(String channel, String videoid,
			String reporttype) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("videoid", videoid);
			obj.put("reporttype", reporttype);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getRecomJson(String channel, String videoid,
			String reason) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("videoid", videoid);
			obj.put("reason", reason);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * 组织获取分享地址json字符串
	 * 
	 * @param videoid
	 *            视频id
	 * @param type
	 *            视频类型：1.直播 2.点播
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getShareUrlRequestJson(String videoid, String type) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("videoid", videoid);
			obj.put("type", type);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	/**
	 * 组织分享请求json字符串
	 * 
	 * @param channel
	 *            分享渠道：1.视频广场 2.微信 3.微博 4.QQ
	 * @param videoid
	 *            视频id
	 * @return json字符串
	 * @author xuhw
	 * @date 2015年4月17日
	 */
	public static String getShareVideoUpRequestJson(String channel,
			String videoid) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("channel", channel);
			obj.put("videoid", videoid);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getJXListJson(String jxid, String pagesize) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("jxid", jxid);
			obj.put("pagesize", pagesize);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getZTJson(String ztid) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("ztid", ztid);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	//最新进入单视频
	public static String getVideoDetailJson(String videoid) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("videoid", videoid);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	//获取用户信息
	public static String getUserInfoJson(String otheruid) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("otheruid", otheruid);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getJHJson(String ztid, String operation, String timestamp, String pagesize) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("ztid", ztid);
			obj.put("operation", operation);
			obj.put("timestamp",timestamp);
			obj.put("pagesize", pagesize);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getCommentJson(String topicid, String topictype, String operation, String timestamp, String pagesize) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("topicid", topicid);
			obj.put("topictype", topictype);
			obj.put("operation",operation);
			obj.put("timestamp",timestamp);
			obj.put("pagesize", pagesize);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String addCommentJson(String topicid, String topictype, String text, String replyid, String replyname) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("topicid", topicid);
			obj.put("topictype", topictype);
			String str="";
			try {
				str = URLEncoder.encode(text, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			obj.put("text", str);
			obj.put("replyid", replyid);
			obj.put("replyname", replyname);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String delCommentJson(String id) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("id", id);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getTagJson(String ztype, String ztid) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("ztype", ztype);
			obj.put("ztid", ztid);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getUserCenterJson(String otheruid) {
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("otheruid", otheruid);

			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
	
	public static String getUserCenterShareVideoJson(String otheruid, String operation, String timestamp){
		String json = "";
		try {
			JSONObject obj = new JSONObject();
			obj.put("otheruid", otheruid);
			obj.put("operation", operation);
			obj.put("timestamp", timestamp);
			json = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

}
