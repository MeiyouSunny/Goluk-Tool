package cn.com.mobnote.golukmobile.carrecorder.entity;

public class RecordStorgeState {
	/** SD卡是否在位:YES(1)|NO(0)。仅当SD卡在位时，下列字段才有意义。 */
	public int SDCardActive;
	/**  SD卡剩余容量太小，不能继续录制(1) | SD卡容量正常(0)  */
	public int isSpaceTooSmall;
	/**  SD卡总容量，单位为Byte  */
	public double totalSdSize;
	/**  用户自有文件大小，单位为Byte  */
	public double userFilesSize;
	/**  SD卡剩余容量，单位为Byte  */
	public double leftSize;
	/**  行驶录像配额上限，单位为Byte  */
	public double normalRecQuota;
	/**  当前行驶录像总容量，单位为Byte  */
	public double normalRecSize;
	/**  紧急录像配额上限，单位为Byte  */
	public double urgentRecQuota;
	/**  当前紧急录像总容量，单位为Byte  */
	public double urgentRecSize;
	/**  精彩录像配额上限，单位为Byte  */
	public double wonderfulRecQuota;
	/**  当前精彩录像总容量，单位为Byte  */
	public double wonderfulRecSize;
	/**  抓图配额上限，单位为Byte  */
	public double picQuota;
	/**  当前抓图总容量，单位为Byte  */
	public double picSize;
}
