package cn.com.mobnote.golukmobile.carrecorder.entity;

import java.io.Serializable;

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
  * 音视频配置信息
  *
  * 2015年4月7日
  *
  * @author xuhw
  */
public class VideoConfigState {
	/** 0:主码流，1:子码流 */
	public int bitstreams;
	/** 分辨率 */
	public String resolution;
	/** 帧数：fps */
	public int frameRate;
	/** 码率:kbits/s */
	public int bitrate;
	/** 1:有音频，0:没有音频 */
	public int AudioEnabled;
}
