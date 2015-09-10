package cn.com.mobnote.golukmobile.helper;

import java.util.LinkedList;

import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;

/**
 * @描述 云服务请求接口
 * @作者 卜长清，buchangqing@goluk.com
 * @日期 2015-09-09
 * @版本 1.0
 */
public interface ICloudHelper {
	/**
	 * 获取文件签名
	 * @param params 参数表
	 * @return
	 */
	public String fileSign(LinkedList<BasicNameValuePair> params);
	
	/**
	 * 获取视频签名
	 * @param params 参数表
	 * @return
	 */
	public String videoSign(LinkedList<BasicNameValuePair> params);
}
