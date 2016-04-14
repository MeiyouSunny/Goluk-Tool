package com.mobnote.golukmain.videosuqare;

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
  * 播放器播放状态
  *
  * 2015年4月18日
  *
  * @author xuhw
  */
public enum PlayerState {
	noallow, //不可使用
	allowbuffer, //允许缓冲
	buffing, //缓冲中
	bufferend, //缓冲结束，可以播放
	playing, //播放中
	pause //暂停
}
