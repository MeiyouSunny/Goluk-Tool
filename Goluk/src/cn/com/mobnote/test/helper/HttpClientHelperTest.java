package cn.com.mobnote.test.helper;

import android.test.AndroidTestCase;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import cn.com.mobnote.golukmobile.helper.HttpClientHelper;
import cn.com.tiros.debug.GolukDebugUtils;


public class HttpClientHelperTest extends AndroidTestCase {
	public void testGet() throws Exception {  
		HttpClientHelper helper = new HttpClientHelper();
		LinkedList<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();  
		params.add(new BasicNameValuePair("appid", "10002984"));  
		params.add(new BasicNameValuePair("uid", "100"));
		
		helper.setServer("192.168.2.104:9090");
		helper.setService("navidog4MeetTrans/videosign.htm");
		
		String content = helper.get(params);
	    
	    Log.e("goluk", "Http GET response content:" + content);
//	    GolukDebugUtils.e("goluk", "Http GET response content:: " + content);
    }
}
