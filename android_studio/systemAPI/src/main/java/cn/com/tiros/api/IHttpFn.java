package cn.com.tiros.api;

public interface IHttpFn {
	/** 测试服务器 */
	public static final String PRIVATE_URL_SIGN1 = "server.goluk.cn";
	/** 开发服务器 */
	public static final String PRIVATE_URL_SIGN2 = "svr.goluk.cn";
	/** 正式服务器 */
	public static final String PRIVATE_URL_SIGN3 = "s.goluk.cn";

	/** 连接失败 */
	public static final int NSEERR_DICONNECT = 600;
	/** 客户端错误 */
	public static final int NSEERR_CLIENTERR = 601;
	/** 应答超时 */
	public static final int NSEERR_TIMEOUT_RESPONSE = 700;
	/** 数据接收超时 */
	public static final int NSEERR_TIMEOUT_DATA = 701;
	/** 下载的数据长度与服务器返回长度不等 */
	public static final int NSEERR_TOTALLENERR = 702;

	/** http请求当前状态 */
	public static final int NETSTATE_RUNNING = 0; // 当前运行状态
	public static final int NETSTATE_WAIT = 1; // 等待状态

	/** 网络未连接 */
	public static final int SYS_HTTPERR_DISCONNECT = 600;
	/** http客户端错误 */
	public static final int SYS_HTTPERR_CLIENTERR = 601;
	/** http服务器非正常应答 */
	public static final int SYS_HTTPERR_SERVERERR = 602;
	/** HTTP应答超时错误 */
	public static final int SYS_HTTPERR_TIMEOUT_RESPONSE = 700;
	/** HTTP接收数据超时 */
	public static final int SYS_HTTPERR_TIMEOUT_DATA = 701;

	/** 网络不可用（自定义错误码600系列） */
	public static final int SYS_HTTPERRTYPE_UNAVAILABLE = 0x01;
	/** http应答错误（标准http应答错误码） */
	public static final int SYS_HTTPERRTYPE_RESPONSE = 0x02;
	/** 超时（自定义错误码700系列） */
	public static final int SYS_HTTPERRTYPE_TIMEOUT = 0x03;

	/** 请求 */
	public static final int SYS_EVT_HTTP_REQUEST = 0x0001;
	/** 应答 */
	public static final int SYS_EVT_HTTP_RESPONSE = 0x0002;
	/** 数据体 | dwParam1 : http body size (uint32) dwParam2 : http body (void *) */
	public static final int SYS_EVT_HTTP_BODY = 0x0003;
	/** 完成 */
	public static final int SYS_EVT_HTTP_COMPLETED = 0x0004;
	/** 错误 | dwParam1 : 错误类型 dwParam2: 错误码, 600之后为自定义错误 */
	public static final int SYS_EVT_HTTP_ERROR = 0x0005;
	/** 文件上传进度 */
	public static final int SYS_EVT_HTTP_POSTFILE_PROGRESS = 0x006;
	/** GZIP标识 */
	public static final int SYS_EVT_HTTP_DATA_GZIP = 0x007;

	public static final int M_EVT_HTTP_REQUEST = 0x0001;
	public static final int M_EVT_HTTP_RESPONSE = 0x0002;
	public static final int M_EVT_HTTP_RESPONSE_STRING = 0x003;
	public static final int M_EVT_HTTP_BODY = 0x0004;
	public static final int M_EVT_HTTP_COMPLETED = 0x0005;
	public static final int M_EVT_HTTP_ERROR = 0x0006;
	public static final int M_EVT_HTTP_DATA_GZIP = 0x007;
	public static final int M_EVT_HTTP_UPLOADFILE_PROGRESS = 0x008;

}
