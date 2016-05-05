package com.mobnote.golukmain.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.http.request.GolukFastjsonRequest;
import com.mobnote.golukmain.search.bean.SearchRetBean;
import com.umeng.socialize.utils.Log;


public class SearchRequest extends GolukFastjsonRequest<SearchRetBean> {
	public SearchRequest(int requestType, IRequestResultListener listener) {
		super(requestType, SearchRetBean.class, listener);
	}

	@Override
	protected String getPath() {
		return "/cdcSearch/search.htm";
	}

	@Override
	protected String getMethod() {
		return "searchUser";
	}

	public void get (String xieyi,String terms,String operation,String index,String pagesize,String commuid){
		HashMap<String, String> headers = (HashMap<String, String>) getHeader();
		Log.i("searchRequest","searchRequest");
		headers.put("xieyi", xieyi);
		try {
			terms = URLEncoder.encode(terms, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		headers.put("terms", terms);
		headers.put("operation", operation);
		headers.put("index", index);
		headers.put("pagesize", pagesize);
		headers.put("commuid", commuid);
		get();
	}
}

