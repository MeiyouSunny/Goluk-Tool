package cn.com.mobnote.golukmobile.carrecorder.entity;

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
  * 视频文件信息
  *
  * 2015年3月18日
  *
  * @author xuhw
  */
public class VideoFileInfo {
	/**文件唯一标识  */
	public int id;
	/**视频文件录制起始时间（秒）  */
	public long time;
	/**时长(秒)  */
	public int period;
	/**起因事件类型  */
	public int type;
	/**字长(字节)  */
	public int size;
	/**文件名  */
	public String location;
	/**分辨率  1:1080p 2:720p*/
	public int resolution;
	/**是否具有录像截图文件。是(1) | 否(0) */
	public int withSnapshot;
	/**是否具有gps文件。是(1) | 否(0)  */
	public int withGps;
}
