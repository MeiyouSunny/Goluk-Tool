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
  * Kit和紧急视频触发的事件信息
  *
  * 2015年4月23日
  *
  * @author xuhw
  */
public class ExternalEventsDataInfo {
	/** 事件唯一索引 */
	public double  id;
	/** 起因事件类型 */
    public int  type;
    /** 分辨率 */
    public String resolution;
    /** 时长(秒) */
    public int  period;
    /** 开始时间 */
    public int  time;
    /** 字长(字节) */
    public double  size;
    /** 虚拟物理地址。对IPC的SD卡上的录像文件，则表示文件名。 */
    public String location;
    /** 是否具有录像起始时刻的截图文件。是(1) | 否(0)。*/
    public int  withSnapshot;
    /** 是否具有录像过程中的gps文件。是(1) | 否(0)。*/
    public int  withGps;
}
