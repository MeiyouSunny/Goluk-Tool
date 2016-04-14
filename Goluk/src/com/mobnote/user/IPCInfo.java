package com.mobnote.user;

import java.io.Serializable;

/**
 * ipc升级
 * @author mobnote
 *
 */
public class IPCInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String version;
	public String path;
	public String url;
	public String md5;
	public String filesize;
	public String releasetime;
	public String appcontent;
	public String isnew;
}
