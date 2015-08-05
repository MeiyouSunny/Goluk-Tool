package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParserUtils {
	
	public static List<JXListItemDataInfo> parser(String jsonStr) {
		List<JXListItemDataInfo> jxlistdata = new ArrayList<JXListItemDataInfo>();
		try {
			JSONObject json = new JSONObject(jsonStr);
			boolean success = json.optBoolean("success");
			if (success) {
				JSONObject data = json.getJSONObject("data");
				if (null != data) {
					String result = data.optString("result");
					String count = data.optString("count");
					JSONArray list  = data.optJSONArray("list");
					if (null != list) {
						for (int i=0; i<list.length(); i++) {
							JSONObject object = (JSONObject) list.opt(i);
							if (null != object) {
								String jxid = object.optString("jxid");
								String jxdate = object.optString("jxdate");
								JSONArray jxlist  = object.optJSONArray("jxlist");
								if (null != jxlist) {
									for (int j=0; j<jxlist.length(); j++) {
										boolean flag = false;
										if(j == 0) {
											flag = true;
										}
										JSONObject jxlistitem = (JSONObject) jxlist.opt(i);
										JXListItemDataInfo info = new JXListItemDataInfo(jxlistitem, jxdate, flag);
										jxlistdata.add(info);
									}
								}
							}
						}
					}
				}
			}else {
				String msg = json.optString("msg");
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return jxlistdata;
	}
	
}
