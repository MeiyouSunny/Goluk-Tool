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
  * IPC设备存储状态
  *
  * 2015年4月3日
  *
  * @author xuhw
  */
public class DeviceState {
	/** SD卡总容量，单位为MB。仅当SDActive为YES时，此字段有意义 */
	public double totalSizeOnSD;
	/** 当前镜头状态。 1 –正常 2 –异常 */
	public int cameraStatus;
	/** SD卡剩余容量，单位为MB。仅当SDActive为YES时，此字段有意义。*/
	public double leftSizeOnSD;
	/** 当前登录用户数 */
	public int onlineUsers;
	/** SD卡在位YES(1) | SD卡不存在NO(0) */
	public int SDPresent;
	/** SD卡剩余容量太小，不能继续录制(1) | SD卡容量正常(0) */
	public int isSpaceTooSmall;
}