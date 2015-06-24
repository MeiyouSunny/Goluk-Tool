package cn.com.mobnote.user;

import java.io.Serializable;

/**
 * ipc升级
 * @author mobnote
 *
 */
public class IPCInfo implements Serializable {

	public String version;
	public String path;
	public String url;
	public String md5;
	public String filesize;
	public String releasetime;
	public String appcontent;
	public String isnew;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getFilesize() {
		return filesize;
	}
	public void setFilesize(String filesize) {
		this.filesize = filesize;
	}
	public String getReleasetime() {
		return releasetime;
	}
	public void setReleasetime(String releasetime) {
		this.releasetime = releasetime;
	}
	public String getAppcontent() {
		return appcontent;
	}
	public void setAppcontent(String appcontent) {
		this.appcontent = appcontent;
	}
	public String getIsnew() {
		return isnew;
	}
	public void setIsnew(String isnew) {
		this.isnew = isnew;
	}
	
}
