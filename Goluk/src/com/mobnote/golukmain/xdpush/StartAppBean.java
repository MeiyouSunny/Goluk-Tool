package com.mobnote.golukmain.xdpush;

import java.io.Serializable;

/**
 * 网页启动App数据 体
 * */
public class StartAppBean implements Serializable {

	/** */
	private static final long serialVersionUID = 1L;
	public String uri;
	public String dataStr;
	public String host;
	public String path;

	/** 功能参数 */

	/** 类型，判断是单视频还是专题 */
	public String type;
	/** 专题id 或 视频 id */
	public String id;
	/** title */
	public String title;

}
