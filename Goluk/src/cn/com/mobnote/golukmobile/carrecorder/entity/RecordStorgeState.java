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
  * 录制存储状态
  *
  * 2015年4月7日
  *
  * @author xuhw
  */
public class RecordStorgeState {
	/** SD卡是否在位:YES(1)|NO(0)。仅当SD卡在位时，下列字段才有意义。 */
	public int SDCardActive;
	/**  SD卡剩余容量太小，不能继续录制(1) | SD卡容量正常(0)  */
	public int isSpaceTooSmall;
	/**  SD卡总容量，单位为MB  */
	public double totalSdSize;
	/**  用户自有文件大小，单位为MB  */
	public double userFilesSize;
	/**  SD卡剩余容量，单位为MB  */
	public double leftSize;
	/**  行驶录像配额上限，单位为MB  */
	public double normalRecQuota;
	/**  当前行驶录像总容量，单位为MB  */
	public double normalRecSize;
	/**  紧急录像配额上限，单位为MB  */
	public double urgentRecQuota;
	/**  当前紧急录像总容量，单位为MB  */
	public double urgentRecSize;
	/**  精彩录像配额上限，单位为MB  */
	public double wonderfulRecQuota;
	/**  当前精彩录像总容量，单位为MB  */
	public double wonderfulRecSize;
	/**  抓图配额上限，单位为MB  */
	public double picQuota;
	/**  当前抓图总容量，单位为MB  */
	public double picSize;
}
