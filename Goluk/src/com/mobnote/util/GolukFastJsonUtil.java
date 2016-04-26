package com.mobnote.util;

import java.util.List;

import com.alibaba.fastjson.JSON;

public class GolukFastJsonUtil {

	public static <T> T getParseObj(String jsonString, Class<T> cls) {
		T t = null;
		try {
			t = JSON.parseObject(jsonString, cls);
		} catch (Exception e) {

		}

		return t;
	}

	public static String setParseObj(Object obj) {
		return JSON.toJSONString(obj);
	}
	
	public static <T> List<T> parseList(String param, Class<T> cls) {
		return JSON.parseArray(param, cls);
	}

}
