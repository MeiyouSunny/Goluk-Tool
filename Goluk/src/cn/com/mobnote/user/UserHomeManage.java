package cn.com.mobnote.user;

public class UserHomeManage {

	private String homeContent;
	private String homeCountWatch;
	private String homeCountZan;
	public String getHomeContent() {
		return homeContent;
	}
	public void setHomeContent(String homeContent) {
		this.homeContent = homeContent;
	}
	public String getHomeCountWatch() {
		return homeCountWatch;
	}
	public void setHomeCountWatch(String homeCountWatch) {
		this.homeCountWatch = homeCountWatch;
	}
	public String getHomeCountZan() {
		return homeCountZan;
	}
	public void setHomeCountZan(String homeCountZan) {
		this.homeCountZan = homeCountZan;
	}
	@Override
	public String toString() {
		return "UserHomeManage [homeContent=" + homeContent
				+ ", homeCountWatch=" + homeCountWatch + ", homeCountZan="
				+ homeCountZan + "]";
	}
	
}
