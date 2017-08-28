package cn.com.mobnote.module.msgreport;

public interface IMessageReportFn {

	/** 网络改变通知，需要平台在应用起来时通知当前网络状态，并且在发生改变时再次通知 */
	public static final int REPORT_CMD_NET_STATA_CHG = 0;
	/** 非实时上传，使用http，可靠上传 */
	public static final int REPORT_CMD_LOG_REPORT_HTTP = 1;
	/** 实时上传，使用UDP，非可靠上传 */
	public static final int REPORT_CMD_LOG_REPORT_REAL = 2;

	public static final String KEY_WIFI_BIND = "key_wifi_bind";
	/** 绑定成功后上报设备号 */
	public static final String KEY_ACTIVATION_TIME = "activation_time";
	public static final String KEY_RTSP_REVIEW = "rtsp_view";
}
