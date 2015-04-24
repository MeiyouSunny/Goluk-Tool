package cn.com.mobnote.golukmobile.videosuqare;

 /**
  * 1.编辑器必须显示空白处
  *
  * 2.所有代码必须使用TAB键缩进
  *
  * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
  *
  * 4.注释必须在行首写.(枚举除外)
  *
  * 5.函数使用块注释,代码逻辑使用行注释
  *
  * 6.文件头部必须写功能说明
  *
  * 7.所有代码文件头部必须包含规则说明
  *
  * 视频广场视频属性信息
  *
  * 2015年4月14日
  *
  * @author xuhw
  */
public class VideoEntity {
	/** 视频唯一id */
	public String videoid;
	/** 视频类型：1.直播 2.点播 */
	public String type;
	/** 视频上传时间  视频分享时间 */
	public String sharingtime;
	/** 视频描述 */
	public String describe;
	/** 视频图片 */
	public String picture;
	/** 点击次数 */
	public String clicknumber;
	/** 点赞次数 */
	public String praisenumber;
	/** 直播起始时间 (不一定有)*/
	public String starttime;
	/** 直播时间  (不一定有)*/
	public String livetime;
	/** 直播web地址 */
	public String livewebaddress;
	/** 直播sdk地址 */
	public String livesdkaddress;
	/** 点播web地址 */
	public String ondemandwebaddress;
	/** 点播sdk地址 */
	public String ondemandsdkaddress;
	/** 是否点过赞：0.否1.是 */
	public String ispraise;
	
	public LiveVideoData livevideodata;
}
