package com.mobnote.golukmain.newest;

import android.text.TextUtils;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.newest.bean.NewestRetBean;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class VideoSquareNewestListRequest extends GolukFastjsonRequest<NewestRetBean> {

   public VideoSquareNewestListRequest(int requestType, IRequestResultListener listener) {
       super(requestType, NewestRetBean.class, listener);
   }

   @Override
   protected String getPath() {
       return "/navidog4MeetTrans/shareVideo.htm";
   }

   @Override
   protected String getMethod() {
       return "shareVideoSquare";
   }

   public void get(String protocol, String uid, String mobileid, String channel, String type,
                   List<String> attribute, String operation, String timestamp) {
       HashMap<String, String> headers = (HashMap<String, String>) getHeader();

       headers.put("xieyi", protocol);
       if(!TextUtils.isEmpty(uid)) {
           headers.put("uid", uid);
       } else {
           headers.put("uid", "");
       }

       if(!TextUtils.isEmpty(mobileid)) {
           headers.put("mobileid", mobileid);
       } else {
           headers.put("mobileid", "");
       }

       if(!TextUtils.isEmpty(channel)) {
           headers.put("channel", channel);
       } else {
           headers.put("channel", "");
       }

       if(!TextUtils.isEmpty(type)) {
           headers.put("type", type);
       } else {
           headers.put("type", "");
       }
//       headers.put("attributeUncode", attribute.get(0));
//       JSONArray arr = new JSONArray();
//       arr.put("0");
//       try {
//           String attributestr = URLEncoder.encode(arr.toString(), "UTF-8");
//           headers.put("attribute", attributestr);
//       } catch(Exception e) {
//            e.printStackTrace();
//       }

 //      obj.put("attribute", attributestr);
//       if(!TextUtils.isEmpty(attribute)) {
           headers.put("attribute", "[\"0\"]");
//       } else {
 //          headers.put("attribute", "");
   //    }


       headers.put("operation", operation);
       headers.put("timestamp", timestamp);

       get();
   }
}

